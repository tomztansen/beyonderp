package com.vaadinerp.security.sso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Memverifikasi {@code logout_token} OIDC Back-Channel Logout 1.0 (§2.4 dan §2.6) dari Authentik, hanya dengan JDK + Jackson.
 *
 * <p>Aturan (semuanya wajib, gagal satu = ditolak):
 * <ul>
 *   <li>algoritma tepat {@code RS256} (menolak {@code none}, HS256, dan lainnya: mencegah serangan konfusi algoritma);</li>
 *   <li>tanda tangan cocok dengan kunci publik IdP ({@code kid} diselesaikan lewat {@code keyResolver}, yaitu JWKS);</li>
 *   <li>{@code iss} termasuk penerbit yang diizinkan, {@code aud} memuat client id aplikasi ini;</li>
 *   <li>{@code iat} ada dan tidak lebih dari {@value #MAX_AGE_SECONDS} detik dari waktu sekarang (lampau maupun depan);
 *       bila ada {@code exp}, tidak boleh sudah lewat;</li>
 *   <li>{@code events} memuat {@value #EVENT} dan {@code nonce} tidak boleh ada;</li>
 *   <li>{@code jti} ada dan belum pernah dipakai (anti-replay, disimpan {@value #REPLAY_TTL_SECONDS} detik);</li>
 *   <li>{@code sub} ada (dipakai memetakan sesi; token hanya berisi {@code sid} tidak didukung).</li>
 * </ul>
 */
public class LogoutTokenVerifier {

    public static final String EVENT = "http://schemas.openid.net/event/backchannel-logout";
    static final long MAX_AGE_SECONDS = 300;
    static final long REPLAY_TTL_SECONDS = 600;

    /** Isi token yang sudah lolos verifikasi. {@code iat} dalam detik epoch. */
    public record Claims(String sub, String sid, long iat, String jti) {
    }

    /** Token ditolak. Pesan hanya untuk log server, jangan dikirim balik ke pemanggil. */
    public static class InvalidLogoutTokenException extends Exception {
        public InvalidLogoutTokenException(String message) {
            super(message);
        }
    }

    private final Set<String> acceptedIssuers;
    private final String audience;
    private final Clock clock;
    private final Function<String, RSAPublicKey> keyResolver;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Long> seenJti = new ConcurrentHashMap<>();

    /**
     * @param acceptedIssuers penerbit yang diterima (tanpa garis miring penutup; dinormalkan di sini)
     * @param audience        client id aplikasi ini
     * @param keyResolver     {@code kid} (boleh null) -> kunci publik RSA, atau null bila tidak dikenal
     */
    public LogoutTokenVerifier(Set<String> acceptedIssuers, String audience, Clock clock,
            Function<String, RSAPublicKey> keyResolver) {
        this.acceptedIssuers = acceptedIssuers.stream().map(LogoutTokenVerifier::normalizeIssuer)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        this.audience = audience;
        this.clock = clock;
        this.keyResolver = keyResolver;
    }

    public Claims verify(String token) throws InvalidLogoutTokenException {
        if (token == null) {
            throw bad("token kosong");
        }
        String[] parts = token.trim().split("\\.", -1);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
            throw bad("bentuk token bukan JWS compact (tiga bagian terisi)");
        }

        JsonNode header = json(parts[0], "header");
        JsonNode payload = json(parts[1], "payload");

        // 1) algoritma: hanya RS256
        String alg = header.path("alg").asText("");
        if (!"RS256".equals(alg)) {
            throw bad("algoritma tidak didukung: " + alg);
        }

        // 2) tanda tangan
        String kid = header.hasNonNull("kid") ? header.get("kid").asText() : null;
        PublicKey key = keyResolver.apply(kid);
        if (key == null) {
            throw bad("kunci tidak dikenal (kid=" + kid + ")");
        }
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(key);
            sig.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
            if (!sig.verify(Base64.getUrlDecoder().decode(parts[2]))) {
                throw bad("tanda tangan tidak cocok");
            }
        } catch (InvalidLogoutTokenException e) {
            throw e;
        } catch (Exception e) {
            throw bad("tanda tangan tidak dapat diverifikasi: " + e.getClass().getSimpleName());
        }

        // 3) klaim (hanya dipercaya setelah tanda tangan sah)
        String iss = normalizeIssuer(payload.path("iss").asText(""));
        if (!acceptedIssuers.contains(iss)) {
            throw bad("iss tidak dikenal: " + iss);
        }
        if (!audienceMatches(payload.get("aud"))) {
            throw bad("aud tidak memuat client id aplikasi ini");
        }

        long now = clock.instant().getEpochSecond();
        JsonNode iatNode = payload.get("iat");
        if (iatNode == null || !iatNode.isNumber()) {
            throw bad("iat tidak ada");
        }
        long iat = iatNode.asLong();
        if (Math.abs(now - iat) > MAX_AGE_SECONDS) {
            throw bad("iat di luar jendela " + MAX_AGE_SECONDS + " detik (iat=" + iat + ", now=" + now + ")");
        }
        JsonNode expNode = payload.get("exp");
        if (expNode != null && expNode.isNumber() && expNode.asLong() < now) {
            throw bad("token sudah kedaluwarsa (exp)");
        }

        JsonNode events = payload.get("events");
        if (events == null || !events.isObject() || !events.has(EVENT)) {
            throw bad("klaim events tidak memuat " + EVENT);
        }
        if (payload.has("nonce")) {
            throw bad("logout token tidak boleh memuat nonce");
        }

        String sub = textOrNull(payload.get("sub"));
        if (sub == null) {
            throw bad("sub tidak ada (hanya sid tidak didukung)");
        }
        String jti = textOrNull(payload.get("jti"));
        if (jti == null) {
            throw bad("jti tidak ada");
        }

        // 4) anti-replay: dicatat hanya untuk token yang SEMUA pemeriksaan di atas lolos
        long nowMs = clock.millis();
        seenJti.values().removeIf(expiry -> expiry < nowMs);
        if (seenJti.putIfAbsent(jti, nowMs + REPLAY_TTL_SECONDS * 1000) != null) {
            throw bad("jti sudah pernah dipakai (replay)");
        }

        return new Claims(sub, textOrNull(payload.get("sid")), iat, jti);
    }

    private boolean audienceMatches(JsonNode aud) {
        if (aud == null || audience == null) {
            return false;
        }
        if (aud.isTextual()) {
            return audience.equals(aud.asText());
        }
        if (aud.isArray()) {
            for (JsonNode a : aud) {
                if (a.isTextual() && audience.equals(a.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private JsonNode json(String b64, String what) throws InvalidLogoutTokenException {
        try {
            JsonNode n = mapper.readTree(Base64.getUrlDecoder().decode(b64));
            if (n == null || !n.isObject()) {
                throw bad(what + " bukan objek JSON");
            }
            return n;
        } catch (InvalidLogoutTokenException e) {
            throw e;
        } catch (Exception e) {
            throw bad(what + " tidak dapat dibaca (" + e.getClass().getSimpleName() + ")");
        }
    }

    private static String textOrNull(JsonNode n) {
        return (n != null && n.isTextual() && !n.asText().isBlank()) ? n.asText() : null;
    }

    static String normalizeIssuer(String iss) {
        String s = iss == null ? "" : iss.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static InvalidLogoutTokenException bad(String message) {
        return new InvalidLogoutTokenException(message);
    }
}
