package com.vaadinerp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Deteksi balasan approve/reject dari WhatsApp. Dipanggil oleh
 * WhatsAppWebhookController saat event message.received masuk.
 *
 * Kenapa balasan teks, bukan tombol interaktif: OpenWA (dan WhatsApp
 * unofficial client pada umumnya) tidak punya endpoint kirim tombol yang bisa
 * di-tap penerima -- "template" di OpenWA cuma teks dengan variabel, dan
 * "click-button" itu untuk sesi OpenWA sendiri mengklik tombol yang DITERIMA
 * dari bot bisnis lain, bukan untuk kita mengirim tombol ke penerima. Field
 * button{id} pada webhook cuma muncul di engine Baileys untuk prompt gaya
 * WhatsApp Business, dan sifatnya "unverified" menurut dokumentasi OpenWA
 * sendiri. Balasan teks bebas jalan sama di kedua engine dan jauh lebih
 * stabil -- makanya dipilih di sini.
 *
 * Pencocokan: satu baris PENDING per chat_id (lihat catatan ponytail di
 * WhatsAppOutboxService.queueApprovalRequest).
 */
@Service
public class WhatsAppInboxService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppInboxService.class);

    private final JdbcTemplate jdbcTemplate;

    public WhatsAppInboxService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processIncomingReply(String chatId, String bodyText) {
        if (chatId == null || bodyText == null) {
            return;
        }
        String normalized = bodyText.trim().toUpperCase();
        boolean approve = normalized.startsWith("APPROVE") || normalized.equals("YA") || normalized.equals("OK");
        boolean reject = normalized.startsWith("REJECT") || normalized.startsWith("TOLAK");
        if (!approve && !reject) {
            return; // bukan balasan approval, abaikan (chat biasa, dsb.)
        }

        List<Map<String, Object>> pending = jdbcTemplate.queryForList(
                "SELECT * FROM public.wa_approval_request WHERE chat_id = ? AND status = 'PENDING' "
                        + "ORDER BY id DESC LIMIT 1",
                chatId);
        if (pending.isEmpty()) {
            return; // tidak ada approval yang menunggu dari nomor ini
        }

        Map<String, Object> req = pending.get(0);
        Long id = ((Number) req.get("id")).longValue();
        String procName = (String) req.get("proc_name");
        Object procParamsObj = req.get("proc_params");
        String procParams = procParamsObj != null ? procParamsObj.toString() : "{}";
        String newStatus = approve ? "APPROVED" : "REJECTED";

        try {
            if (approve) {
                // procName ditulis oleh script Groovy internal saat memanggil
                // queueApprovalRequest(), bukan dari input WhatsApp -- aman
                // dari injection, sama seperti pola ActionContext.executeProcedure.
                jdbcTemplate.update("CALL " + procName + "(?::json, ?)", procParams, "whatsapp:" + chatId);
            }
            jdbcTemplate.update(
                    "UPDATE public.wa_approval_request SET status=?, reply_text=?, updatedt=now() WHERE id=?",
                    newStatus, bodyText, id);
        } catch (Exception ex) {
            log.error("Gagal proses approval WhatsApp id={} proc={}: {}", id, procName, ex.getMessage());
        }
    }
}
