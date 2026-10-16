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
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

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
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final SecureRandom random = new SecureRandom();

    private volatile String authorizationEndpoint;
    private volatile String tokenEndpoint;
    private volatile String userinfoEndpoint;
    private volatile String endSessionEndpoint;

    public SsoService(SsoProperties props) {
        this.props = props;
    }

    public boolean isEnabled() {
        return props.isConfigured();
    }

    public String buttonLabel() {
        return props.getButtonLabel();
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
     * Tukar authorization code menjadi token, lalu ambil {@code preferred_username} dari userinfo.
     *
     * @return username aplikasi, atau {@code null} bila gagal (sudah dicatat di log).
     */
    public String exchangeAndGetUsername(String code, String redirectUri, String codeVerifier) {
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
                username = ui.path("sub").asText(null);
            }
            return (username == null || username.isBlank()) ? null : username.trim();
        } catch (Exception e) {
            log.warn("SSO exchange gagal: {}", e.getMessage());
            return null;
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

    private static String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
