package com.vaadinerp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadinerp.service.WhatsAppInboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Endpoint yang dipanggil OpenWA (bukan sebaliknya) tiap ada event WhatsApp,
 * lihat docs/06-api-specification.md § Webhook Events di repo OpenWA untuk
 * bentuk payload-nya. Rute ini WAJIB ditambahkan ke vaadin.excludeUrls di
 * application.properties, sama seperti /stimulsoft-java/**, supaya tidak
 * ditangkap router Vaadin.
 */
@RestController
@RequestMapping("/api/wa-webhook")
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final WhatsAppInboxService inboxService;
    private final ObjectMapper objectMapper;

    @Value("${app.whatsapp.webhook-secret:}")
    private String webhookSecret;

    public WhatsAppWebhookController(WhatsAppInboxService inboxService, ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody String rawBody,
            @RequestHeader(value = "X-OpenWA-Signature", required = false) String signature) {
        if (webhookSecret != null && !webhookSecret.isBlank() && !isValidSignature(rawBody, signature)) {
            return ResponseEntity.status(401).build();
        }

        try {
            JsonNode envelope = objectMapper.readTree(rawBody);
            String event = envelope.path("event").asText("");
            if ("message.received".equals(event)) {
                JsonNode data = envelope.path("data");
                String chatId = data.path("from").asText(null);
                String type = data.path("type").asText("");
                if (chatId != null && "text".equals(type)) {
                    inboxService.processIncomingReply(chatId, data.path("body").asText(null));
                }
            }
        } catch (Exception ex) {
            log.error("Gagal proses webhook WhatsApp: {}", ex.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private boolean isValidSignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computedHex = HexFormat.of().formatHex(computed);
            String provided = signatureHeader.substring("sha256=".length());
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    provided.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("Gagal verifikasi signature webhook WhatsApp: {}", ex.getMessage());
            return false;
        }
    }
}
