package com.vaadinerp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Antrean WhatsApp (outbox pattern, sama seperti {@link EmailOutboxService}) --
 * pemicu (Groovy script dll.) cukup INSERT baris ke sys_whatsapp_outbox lewat
 * queueMessage()/queueApprovalRequest(), pengiriman sungguhan lewat REST API
 * OpenWA dikerjakan terpisah oleh worker terjadwal di sini.
 *
 * Keandalan dijaga dengan pola yang sama persis dengan EmailOutboxService:
 * fixedDelay (tidak tumpang tindih), batas baris per putaran, gagal-koneksi ke
 * server OpenWA tidak menghabiskan jatah percobaan (batch dihentikan, dicoba
 * lagi utuh), gagal-pesan (nomor invalid, sesi disconnect, dst) cuma menaikkan
 * attempt_count baris itu sendiri.
 */
@Service
public class WhatsAppOutboxService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppOutboxService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final int BATCH_SIZE = 50;

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.whatsapp.api-url:}")
    private String apiUrl;

    @Value("${app.whatsapp.api-key:}")
    private String apiKey;

    @Value("${app.whatsapp.session-id:}")
    private String sessionId;

    public WhatsAppOutboxService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Masukkan pesan WhatsApp ke antrean, pakai sesi/nomor default dari
     * app.whatsapp.session-id. Cuma INSERT, tidak pernah menahan pemanggil
     * menunggu OpenWA.
     *
     * @param chatId  ID chat WhatsApp tujuan, format {@code <nomor>@c.us} untuk
     *                personal atau {@code <groupId>@g.us} untuk grup.
     * @param message isi pesan teks biasa.
     * @param inputBy user id pemicu, untuk audit.
     */
    public void queueMessage(String chatId, String message, String inputBy) {
        queueMessage(chatId, message, null, inputBy);
    }

    /**
     * Sama seperti queueMessage(chatId, message, inputBy), tapi bisa menimpa
     * nomor pengirim per pesan lewat sessionIdOverride -- isi dengan nama sesi
     * OpenWA yang mau dipakai (lihat dashboard OpenWA > Sessions), atau null
     * untuk pakai default app.whatsapp.session-id. Nilai yang dipakai
     * di-resolve SEKARANG (saat antre), bukan saat kirim -- jadi tercatat
     * permanen di kolom session_id, tidak berubah walau default di
     * application.properties diganti belakangan.
     */
    public void queueMessage(String chatId, String message, String sessionIdOverride, String inputBy) {
        if (chatId == null || chatId.trim().isEmpty()) {
            throw new IllegalArgumentException("chatId tidak boleh kosong");
        }
        String effectiveSessionId = (sessionIdOverride != null && !sessionIdOverride.isBlank())
                ? sessionIdOverride.trim()
                : sessionId;
        if (effectiveSessionId == null || effectiveSessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "sessionId tidak diisi dan app.whatsapp.session-id juga belum dikonfigurasi");
        }
        jdbcTemplate.update(
                "INSERT INTO public.sys_whatsapp_outbox (chat_id, message, session_id, inputby, inputdt) "
                        + "VALUES (?, ?, ?, ?, ?)",
                chatId.trim(), message != null ? message : "", effectiveSessionId, inputBy, LocalDateTime.now());
    }

    /**
     * Sama seperti queueMessage(), tapi juga mencatat satu baris "menunggu
     * balasan approve/reject" di wa_approval_request -- dipakai kalau pesan ini
     * adalah permintaan approval, bukan notifikasi biasa. Saat balasan masuk
     * (lihat WhatsAppInboxService), procName dipanggil sebagai stored procedure
     * dengan procParams (JSON) kalau user membalas APPROVE.
     *
     * ponytail: pencocokan balasan berdasarkan chat_id + status PENDING
     * terbaru -- kalau nomor yang sama punya lebih dari satu approval
     * menunggu bersamaan, ini ambigu. Tambahkan kode referensi di pesan +
     * parsing kalau kebutuhan itu muncul.
     */
    public void queueApprovalRequest(String chatId, String message, String procName, String procParams,
            String inputBy) {
        queueApprovalRequest(chatId, message, procName, procParams, null, inputBy);
    }

    /** Sama seperti queueApprovalRequest(), dengan sessionIdOverride -- lihat queueMessage(). */
    public void queueApprovalRequest(String chatId, String message, String procName, String procParams,
            String sessionIdOverride, String inputBy) {
        if (chatId == null || chatId.trim().isEmpty()) {
            throw new IllegalArgumentException("chatId tidak boleh kosong");
        }
        if (procName == null || procName.trim().isEmpty()) {
            throw new IllegalArgumentException("procName tidak boleh kosong");
        }
        queueMessage(chatId, message, sessionIdOverride, inputBy);
        jdbcTemplate.update(
                "INSERT INTO public.wa_approval_request (chat_id, prompt_text, proc_name, proc_params, inputby, inputdt) "
                        + "VALUES (?, ?, ?, ?::jsonb, ?, ?)",
                chatId.trim(), message, procName.trim(), procParams != null ? procParams : "{}", inputBy,
                LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 20000)
    public void processOutbox() {
        if (apiUrl == null || apiUrl.isBlank()) {
            return; // fitur belum dikonfigurasi -- diam saja, sama seperti EmailOutboxService
        }

        List<Map<String, Object>> batch = jdbcTemplate.queryForList(
                "SELECT * FROM public.sys_whatsapp_outbox WHERE status = 'PENDING' ORDER BY id LIMIT ?", BATCH_SIZE);

        for (Map<String, Object> row : batch) {
            Long id = ((Number) row.get("id")).longValue();
            try {
                sendOneMessage(row);
                markSent(id);
            } catch (ResourceAccessException ex) {
                // Server OpenWA sendiri tidak terjangkau -- bukan salah pesan ini, jangan
                // sentuh attempt_count, hentikan batch, coba lagi utuh putaran berikutnya.
                log.warn("Server OpenWA tidak terjangkau, batch outbox WhatsApp dihentikan: {}", ex.getMessage());
                break;
            } catch (Exception ex) {
                markFailedOrRetry(id, row, extractErrorMessage(ex));
            }
        }
    }

    private void sendOneMessage(Map<String, Object> row) {
        String chatId = (String) row.get("chat_id");
        String message = (String) row.get("message");
        String rowSessionId = (String) row.get("session_id");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("X-API-Key", apiKey);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("chatId", chatId);
        body.put("text", message != null ? message : "");

        String url = apiUrl.replaceAll("/+$", "") + "/api/sessions/" + rowSessionId + "/messages/send-text";
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    private String extractErrorMessage(Exception ex) {
        if (ex instanceof HttpStatusCodeException httpEx) {
            HttpStatus status = HttpStatus.resolve(httpEx.getStatusCode().value());
            return "HTTP " + httpEx.getStatusCode().value() + (status != null ? " " + status.getReasonPhrase() : "")
                    + ": " + httpEx.getResponseBodyAsString();
        }
        return ex.getMessage();
    }

    private void markSent(Long id) {
        jdbcTemplate.update(
                "UPDATE public.sys_whatsapp_outbox SET status='SENT', sent_at=?, updatedt=? WHERE id=?",
                LocalDateTime.now(), LocalDateTime.now(), id);
    }

    private void markFailedOrRetry(Long id, Map<String, Object> row, String errorMessage) {
        int attempts = ((Number) row.get("attempt_count")).intValue() + 1;
        String status = attempts >= MAX_ATTEMPTS ? "FAILED" : "PENDING";
        jdbcTemplate.update(
                "UPDATE public.sys_whatsapp_outbox SET status=?, attempt_count=?, last_error=?, updatedt=? WHERE id=?",
                status, attempts, errorMessage, LocalDateTime.now(), id);
    }
}
