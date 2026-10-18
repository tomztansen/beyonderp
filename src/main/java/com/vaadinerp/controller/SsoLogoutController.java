package com.vaadinerp.controller;

import com.vaadinerp.security.service.LoginHistoryService;
import com.vaadinerp.security.sso.LogoutTokenVerifier;
import com.vaadinerp.security.sso.LogoutTokenVerifier.Claims;
import com.vaadinerp.security.sso.LogoutTokenVerifier.InvalidLogoutTokenException;
import com.vaadinerp.security.sso.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * OIDC Back-Channel Logout 1.0: Authentik memanggil alamat ini (server ke server, {@code POST} form
 * {@code logout_token=<JWT>}) saat sesi SSO pengguna berakhir (logout, sesi dihapus admin, akun dinonaktifkan,
 * sesi kedaluwarsa). Sesi GMS hasil login SSO milik pengguna itu lalu diputus.
 *
 * <p>Alamat ini didaftarkan di {@code Logout URI} provider Authentik (metode Back-channel) dan harus terjangkau
 * dari Authentik lewat HTTPS. Keamanan: tidak ada sesi/cookie, jadi satu-satunya jaminan keaslian adalah tanda
 * tangan token ({@link LogoutTokenVerifier}); token yang gagal verifikasi selalu dijawab 400 tanpa efek apa pun.
 * Tidak masuk {@code vaadin.excludeUrls} wajib di produksi (terbukti: /api/wa-webhook tetap terjangkau), tetapi
 * didaftarkan di application.properties agar konsisten dengan controller REST lain.
 */
@RestController
public class SsoLogoutController {

    private static final Logger log = LoggerFactory.getLogger(SsoLogoutController.class);

    private final SsoService sso;
    private final LogoutTokenVerifier verifier;
    private final LoginHistoryService loginHistory;

    public SsoLogoutController(SsoService sso, LogoutTokenVerifier verifier, LoginHistoryService loginHistory) {
        this.sso = sso;
        this.verifier = verifier;
        this.loginHistory = loginHistory;
    }

    @PostMapping(path = "/sso/backchannel-logout", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, String>> backchannelLogout(
            @RequestParam(name = "logout_token", required = false) String logoutToken) {
        if (!sso.isEnabled()) {
            return ResponseEntity.notFound().build();
        }
        if (logoutToken == null || logoutToken.isBlank()) {
            log.warn("SSO back-channel logout ditolak: parameter logout_token kosong");
            return invalid();
        }
        try {
            Claims claims = verifier.verify(logoutToken);
            int n = loginHistory.kickSsoSessions(claims.sub(), claims.iat(), "SSO_LOGOUT", "SSO");
            log.info("SSO back-channel logout: sub={}... sesi GMS diputus={}", abbreviate(claims.sub()), n);
            // Spesifikasi: 200 OK bila token sah, juga bila tidak ada sesi yang cocok.
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).build();
        } catch (InvalidLogoutTokenException e) {
            // Alasan rinci hanya di log server, tidak dibocorkan ke pemanggil.
            log.warn("SSO back-channel logout ditolak: {}", e.getMessage());
            return invalid();
        }
    }

    private static ResponseEntity<Map<String, String>> invalid() {
        // Tipe JSON eksplisit: tanpa ini, bila pemanggil tidak mengirim Accept, Spring memilih XML (pustaka XML ada di classpath).
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", "invalid_request"));
    }

    private static String abbreviate(String s) {
        return s == null ? "" : s.substring(0, Math.min(8, s.length()));
    }
}
