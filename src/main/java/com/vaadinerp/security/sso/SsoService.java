package com.vaadinerp.security.sso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Alur OIDC Authorization Code + PKCE untuk SSO (Authentik), tanpa pustaka pihak ketiga:
 * cukup HttpClient JDK + Jackson. Token ditukar lewat back-channel (server ke IdP) dan
 * identitas diambil dari endpoint userinfo ({@code preferred_username} = username aplikasi).
 * Endpoint ditemukan sekali dari dokumen discovery issuer lalu di-cache.
 */
@Service
public class SsoService {

    private static final Logger log = LoggerFactory.getLogger(SsoService.class);

    private final SsoProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    // HTTP/1.1 dipaksa: klien JDK default mencoba upgrade h2c pada URL http:// dan Authentik membalas
    // "400 Invalid HTTP request received." pada POST ke token endpoint.
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10)).build();
    private final SecureRandom random = new SecureRandom();

    private volatile String authorizationEndpoint;
    private volatile String tokenEndpoint;
    private volatile String userinfoEndpoint;
    private volatile String endSessionEndpoint;
    private volatile String jwksUri;

    // Kunci publik IdP (JWKS) untuk memverifikasi logout_token; dimuat saat dibutuhkan, dibatasi 1x per 30 detik.
    private static final long JWKS_MIN_REFRESH_MS = 30_000;
    private volatile Map<String, RSAPublicKey> jwksKeys = Map.of();
    private volatile long jwksFetchedAtMs;

    /** Hasil penukaran kode: username aplikasi dan pengenal pengguna di IdP ({@code sub}, untuk back-channel logout). */
    public record SsoIdentity(String username, String sub) {
    }

    public SsoService(SsoProperties props) {
        this.props = props;
    }

    public boolean isEnabled() {
        return props.isConfigured();
    }

    public String buttonLabel() {
        return props.getButtonLabel();
    }

    /** Redirect URI tetap dari konfigurasi ({@code app.sso.redirect-uri}), atau null bila tidak diisi. */
    public String configuredRedirectUri() {
        String r = props.getRedirectUri();
        return (r == null || r.isBlank()) ? null : r.trim();
    }

    private synchronized void ensureDiscovery() {
        if (authorizationEndpoint != null) {
            return;
        }
        try {
            String issuer = props.getIssuer();
            if (!issuer.endsWith("/")) {
                issuer = issuer + "/";
            }
            HttpResponse<String> r = http.send(
                    HttpRequest.newBuilder(URI.create(issuer + ".well-known/openid-configuration"))
                            .GET().timeout(Duration.ofSeconds(10)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (r.statusCode() != 200) {
                throw new IllegalStateException("discovery HTTP " + r.statusCode());
            }
            JsonNode d = mapper.readTree(r.body());
            authorizationEndpoint = d.get("authorization_endpoint").asText();
            tokenEndpoint = d.get("token_endpoint").asText();
            userinfoEndpoint = d.get("userinfo_endpoint").asText();
            endSessionEndpoint = d.path("end_session_endpoint").asText(null);
            jwksUri = d.path("jwks_uri").asText(null);
        } catch (Exception e) {
            throw new IllegalStateException("Gagal membaca konfigurasi SSO (discovery): " + e.getMessage(), e);
        }
    }

    public String newState() {
        return randomUrlToken(32);
    }

    public String newCodeVerifier() {
        return randomUrlToken(48);
    }

    private String randomUrlToken(int numBytes) {
        byte[] b = new byte[numBytes];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static String s256Challenge(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** URL authorize lengkap (PKCE S256) untuk mengarahkan browser ke Authentik. */
    public String buildAuthorizeUrl(String redirectUri, String state, String codeVerifier) {
        ensureDiscovery();
        return authorizationEndpoint
                + "?response_type=code"
                + "&client_id=" + enc(props.getClientId())
                + "&redirect_uri=" + enc(redirectUri)
                + "&scope=" + enc("openid email profile")
                + "&state=" + enc(state)
                + "&code_challenge=" + enc(s256Challenge(codeVerifier))
                + "&code_challenge_method=S256";
    }

    /**
     * Tukar authorization code menjadi token, lalu ambil {@code preferred_username} (username aplikasi) dan
     * {@code sub} (pengenal pengguna di IdP) dari userinfo.
     *
     * @return identitas, atau {@code null} bila gagal (sudah dicatat di log). {@code sub} bisa null bila IdP
     *         tidak mengirimnya; sesi itu hanya tidak bisa diputus lewat back-channel logout.
     */
    public SsoIdentity exchange(String code, String redirectUri, String codeVerifier) {
        ensureDiscovery();
        try {
            String form = "grant_type=authorization_code"
                    + "&code=" + enc(code)
                    + "&redirect_uri=" + enc(redirectUri)
                    + "&client_id=" + enc(props.getClientId())
                    + "&client_secret=" + enc(props.getClientSecret())
                    + "&code_verifier=" + enc(codeVerifier);
            HttpResponse<String> tr = http.send(
                    HttpRequest.newBuilder(URI.create(tokenEndpoint))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                            .timeout(Duration.ofSeconds(15)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (tr.statusCode() != 200) {
                log.warn("SSO token endpoint HTTP {}: {}", tr.statusCode(), tr.body());
                return null;
            }
            String accessToken = mapper.readTree(tr.body()).path("access_token").asText(null);
            if (accessToken == null || accessToken.isBlank()) {
                log.warn("SSO token response tanpa access_token");
                return null;
            }

            HttpResponse<String> ur = http.send(
                    HttpRequest.newBuilder(URI.create(userinfoEndpoint))
                            .header("Authorization", "Bearer " + accessToken)
                            .GET().timeout(Duration.ofSeconds(15)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (ur.statusCode() != 200) {
                log.warn("SSO userinfo HTTP {}: {}", ur.statusCode(), ur.body());
                return null;
            }
            JsonNode ui = mapper.readTree(ur.body());
            String username = ui.path("preferred_username").asText(null);
            if (username == null || username.isBlank()) {
                // Tanpa fallback ke "sub": di Authentik itu hash acak yang tak pernah cocok dengan username
                // aplikasi. Lebih baik gagal jelas (fail closed) agar mudah didiagnosis dari log.
                log.warn("SSO userinfo tanpa klaim preferred_username (cek scope 'profile' pada provider)");
                return null;
            }
            String sub = ui.path("sub").asText(null);
            if (sub == null || sub.isBlank()) {
                log.warn("SSO userinfo tanpa klaim sub: sesi {} tidak bisa diputus lewat back-channel logout", username);
                sub = null;
            }
            return new SsoIdentity(username.trim(), sub);
        } catch (Exception e) {
            log.warn("SSO exchange gagal: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Kunci publik RSA IdP untuk {@code kid} (dari JWKS di discovery). Dipakai memverifikasi tanda tangan logout_token.
     * Bila kid belum dikenal, JWKS dimuat ulang (dibatasi sekali per 30 detik) untuk menangkap rotasi kunci.
     *
     * @return kunci, atau {@code null} bila tidak dikenal
     */
    public synchronized RSAPublicKey rsaKey(String kid) {
        RSAPublicKey k = kid == null ? null : jwksKeys.get(kid);
        if (k != null) {
            return k;
        }
        long now = System.currentTimeMillis();
        if (now - jwksFetchedAtMs >= JWKS_MIN_REFRESH_MS) {
            jwksFetchedAtMs = now;
            refreshJwks();
        }
        if (kid == null) {
            return jwksKeys.size() == 1 ? jwksKeys.values().iterator().next() : null;
        }
        return jwksKeys.get(kid);
    }

    private void refreshJwks() {
        try {
            ensureDiscovery();
            if (jwksUri == null || jwksUri.isBlank()) {
                log.warn("SSO discovery tanpa jwks_uri");
                return;
            }
            HttpResponse<String> r = http.send(
                    HttpRequest.newBuilder(URI.create(jwksUri)).GET().timeout(Duration.ofSeconds(10)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (r.statusCode() != 200) {
                log.warn("SSO JWKS HTTP {}", r.statusCode());
                return;
            }
            Map<String, RSAPublicKey> fresh = new HashMap<>();
            KeyFactory kf = KeyFactory.getInstance("RSA");
            for (JsonNode jwk : mapper.readTree(r.body()).path("keys")) {
                if (!"RSA".equals(jwk.path("kty").asText()) || jwk.hasNonNull("use") && !"sig".equals(jwk.get("use").asText())) {
                    continue;
                }
                String id = jwk.path("kid").asText(null);
                if (id == null || id.isBlank()) {
                    continue;
                }
                BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.path("n").asText()));
                BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.path("e").asText()));
                fresh.put(id, (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(n, e)));
            }
            if (!fresh.isEmpty()) {
                jwksKeys = Map.copyOf(fresh);
            }
        } catch (Exception e) {
            log.warn("SSO JWKS gagal dimuat: {}", e.getMessage());
        }
    }

    /** URL logout SSO (end-session) global, atau null bila IdP tidak menyediakannya. */
    public String endSessionUrl() {
        ensureDiscovery();
        return endSessionEndpoint;
    }

    /**
     * URL portal pengguna Authentik ({@code <base>/if/user/}), diturunkan dari issuer tanpa
     * memanggil jaringan. Dipakai untuk "logout aplikasi saja": sesi app berakhir, user kembali
     * ke portal, sesi SSO tetap hidup.
     */
    public String portalUrl() {
        String issuer = props.getIssuer();
        if (issuer == null || issuer.isBlank()) {
            return null;
        }
        int i = issuer.indexOf("/application/o/");
        String base = i > 0 ? issuer.substring(0, i) : issuer;
        return base + "/if/user/";
    }

    /**
     * URL halaman ganti password Authentik ({@code <base>/if/flow/default-password-change/}), diturunkan
     * dari issuer. Dipakai tombol "Change Password" untuk sesi SSO: password SSO hanya ada di Authentik,
     * jadi mengubah hash lokal tidak berguna.
     */
    public String changePasswordUrl() {
        String issuer = props.getIssuer();
        if (issuer == null || issuer.isBlank()) {
            return null;
        }
        int i = issuer.indexOf("/application/o/");
        String base = i > 0 ? issuer.substring(0, i) : issuer;
        return base + "/if/flow/default-password-change/";
    }

    private static String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
