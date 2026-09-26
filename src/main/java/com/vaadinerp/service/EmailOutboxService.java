package com.vaadinerp.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Antrean email (outbox pattern) -- fitur pemicu (Groovy script dll.) cukup
 * INSERT baris ke sys_email_outbox lewat queueEmail(), pengiriman sungguhan
 * dikerjakan terpisah oleh worker terjadwal di sini. Dipisah supaya SMTP yang
 * lambat/down tidak pernah menahan permintaan pengguna.
 *
 * Keandalan yang sengaja dijaga:
 * - fixedDelay (bukan fixedRate): putaran berikutnya menunggu putaran ini
 *   selesai, tidak pernah tumpang tindih.
 * - Batasi 50 baris per putaran: backlog besar tidak diproses sekaligus.
 * - Gagal-koneksi (server SMTP tak terjangkau) TIDAK menghabiskan jatah
 *   percobaan tiap email -- batch langsung dihentikan, dicoba lagi utuh di
 *   putaran berikutnya. Gagal-pesan (alamat ditolak, attachment hilang, dst)
 *   cuma menaikkan attempt_count email itu sendiri, baris lain tetap lanjut.
 * - Attachment di-stream langsung dari disk (FileSystemResource), tidak
 *   pernah dibaca penuh ke memori.
 */
@Service
public class EmailOutboxService {

    private static final Logger log = LoggerFactory.getLogger(EmailOutboxService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final int BATCH_SIZE = 50;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ObjectProvider<FileStorageService> fileStorageServiceProvider;

    @Value("${app.mail.from:noreply@localhost}")
    private String fromAddress;

    @Value("${app.mail.from-name:}")
    private String fromName;

    public EmailOutboxService(JdbcTemplate jdbcTemplate,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            ObjectProvider<FileStorageService> fileStorageServiceProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.mailSenderProvider = mailSenderProvider;
        this.fileStorageServiceProvider = fileStorageServiceProvider;
    }

    /**
     * Masukkan email ke antrean. Tidak mengirim apa pun di sini -- cuma INSERT,
     * cepat, tidak pernah menahan pemanggil menunggu SMTP.
     *
     * @param toAddresses  alamat tujuan, dipisah koma; wajib diisi.
     * @param ccAddresses  alamat CC, dipisah koma; boleh null/kosong.
     * @param subject      subjek email.
     * @param htmlBody     isi email dalam HTML; versi teks polos untuk fallback
     *                     dibuat otomatis saat pengiriman, tidak perlu ditulis manual.
     * @param attachments  nama file yang SUDAH ada di app.upload.dir, dipisah
     *                     koma; boleh null/kosong. Bukan file baru -- cuma
     *                     referensi ke file yang sudah diupload lewat form lain.
     * @param inputBy      user id pemicu, untuk audit.
     */
    public void queueEmail(String toAddresses, String ccAddresses, String subject, String htmlBody,
            String attachments, String inputBy) {
        if (toAddresses == null || toAddresses.trim().isEmpty()) {
            throw new IllegalArgumentException("Alamat tujuan (to) tidak boleh kosong");
        }
        jdbcTemplate.update(
                "INSERT INTO public.sys_email_outbox " +
                        "(to_addresses, cc_addresses, subject, body, attachments, inputby, inputdt) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                toAddresses.trim(),
                (ccAddresses != null && !ccAddresses.trim().isEmpty()) ? ccAddresses.trim() : null,
                subject != null ? subject : "",
                htmlBody != null ? htmlBody : "",
                (attachments != null && !attachments.trim().isEmpty()) ? attachments.trim() : null,
                inputBy,
                LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 120000, initialDelay = 30000)
    public void processOutbox() {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return; // spring-boot-starter-mail belum dikonfigurasi (host kosong) -- diam saja
        }

        List<Map<String, Object>> batch = jdbcTemplate.queryForList(
                "SELECT * FROM public.sys_email_outbox " +
                        "WHERE status = 'PENDING' ORDER BY id LIMIT ?",
                BATCH_SIZE);

        for (Map<String, Object> row : batch) {
            Long id = ((Number) row.get("id")).longValue();
            try {
                sendOneEmail(mailSender, row);
                markSent(id);
            } catch (Exception ex) {
                if (isConnectionFailure(ex)) {
                    // Server SMTP sendiri tidak terjangkau -- ini bukan salah email
                    // ini. Jangan sentuh attempt_count siapa pun, hentikan seluruh
                    // batch, coba lagi utuh di putaran berikutnya.
                    log.warn("SMTP server tidak terjangkau, batch outbox dihentikan: {}", ex.getMessage());
                    break;
                }
                markFailedOrRetry(id, row, ex.getMessage());
            }
        }
    }

    private void sendOneEmail(JavaMailSender mailSender, Map<String, Object> row) throws Exception {
        String toRaw = (String) row.get("to_addresses");
        String ccRaw = (String) row.get("cc_addresses");
        String subject = (String) row.get("subject");
        String htmlBody = (String) row.get("body");
        String attachmentsRaw = (String) row.get("attachments");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress, (fromName != null && !fromName.isBlank()) ? fromName : fromAddress);
        helper.setTo(splitAddresses(toRaw));
        String[] cc = splitAddresses(ccRaw);
        if (cc.length > 0) {
            helper.setCc(cc);
        }
        helper.setSubject(subject != null ? subject : "");
        // (teks polos, html) -> multipart/alternative otomatis; teks polos cuma
        // fallback untuk klien email yang tidak render HTML, dibuat sekali di
        // sini, tidak disimpan terpisah di database.
        helper.setText(stripHtmlTags(htmlBody), htmlBody != null ? htmlBody : "");

        if (attachmentsRaw != null && !attachmentsRaw.trim().isEmpty()) {
            FileStorageService fs = fileStorageServiceProvider.getIfAvailable();
            if (fs == null) {
                throw new IllegalStateException("FileStorageService tidak tersedia untuk resolve attachment");
            }
            Path uploadDir = fs.getUploadDir();
            for (String filename : attachmentsRaw.split(",")) {
                String name = filename.trim();
                if (name.isEmpty())
                    continue;
                Path filePath = uploadDir.resolve(name).normalize();
                if (!filePath.startsWith(uploadDir) || !filePath.toFile().exists()) {
                    throw new java.io.FileNotFoundException("Attachment tidak ditemukan: " + name);
                }
                // FileSystemResource -> JavaMail stream langsung dari disk, tidak
                // pernah dibaca penuh ke memori JVM.
                helper.addAttachment(name, new org.springframework.core.io.FileSystemResource(filePath.toFile()));
            }
        }

        mailSender.send(message);
    }

    private String[] splitAddresses(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new String[0];
        }
        List<String> result = new ArrayList<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result.toArray(new String[0]);
    }

    private String stripHtmlTags(String html) {
        if (html == null)
            return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * Gagal-koneksi (server SMTP tak terjangkau) dikenali dari jenis exception
     * ASLI di akar rantai penyebab -- bukan dari jenis Spring MailSendException
     * pembungkusnya, karena itu dipakai untuk semua jenis kegagalan pengiriman.
     */
    private boolean isConnectionFailure(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof java.net.ConnectException
                    || cur instanceof java.net.UnknownHostException
                    || cur instanceof java.net.SocketTimeoutException) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private void markSent(Long id) {
        jdbcTemplate.update(
                "UPDATE public.sys_email_outbox SET status='SENT', sent_at=?, updatedt=? WHERE id=?",
                LocalDateTime.now(), LocalDateTime.now(), id);
    }

    private void markFailedOrRetry(Long id, Map<String, Object> row, String errorMessage) {
        int attempts = ((Number) row.get("attempt_count")).intValue() + 1;
        String status = attempts >= MAX_ATTEMPTS ? "FAILED" : "PENDING";
        jdbcTemplate.update(
                "UPDATE public.sys_email_outbox SET status=?, attempt_count=?, last_error=?, updatedt=? WHERE id=?",
                status, attempts, errorMessage, LocalDateTime.now(), id);
    }
}
