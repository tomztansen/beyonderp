package com.vaadinerp.security.sso;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi SSO (OIDC / Authentik), di-bind dari properti {@code app.sso.*}.
 * Diisi lewat env di server (lihat application.properties). Default: SSO mati,
 * sehingga tombol "Login with SSO" hanya muncul ketika sudah dikonfigurasi penuh.
 */
@Component
@ConfigurationProperties(prefix = "app.sso")
public class SsoProperties {

    private boolean enabled;
    /** Issuer OIDC, mis. http://host:9000/application/o/<slug>/ (discovery diturunkan dari sini). */
    private String issuer;
    private String clientId;
    private String clientSecret;
    /**
     * Redirect URI (callback) yang TETAP, mis. https://gms.growthsteel.com/sso/callback. Wajib diisi di
     * produksi: di belakang Nginx aplikasi tidak melihat scheme/port asli, dan alamat yang disusun dari
     * header Host bisa dipengaruhi pemanggil. Kosong = disusun dari request (hanya untuk dev).
     */
    private String redirectUri;
    /**
     * Penerbit (iss) tambahan yang diterima pada token back-channel logout, dipisah koma. Biasanya kosong:
     * yang diterima otomatis hanya {@link #issuer}. Isi bila log menunjukkan "iss tidak dikenal" karena
     * Authentik menulis penerbit token logout dengan alamat berbeda dari yang dipakai saat login.
     */
    private java.util.List<String> logoutAcceptedIssuers = new java.util.ArrayList<>();
    /** Teks tombol pada halaman login. */
    private String buttonLabel = "Login with SSO";

    /** SSO siap dipakai hanya bila diaktifkan dan semua kredensial terisi. */
    public boolean isConfigured() {
        return enabled
                && notBlank(issuer) && notBlank(clientId) && notBlank(clientSecret);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public java.util.List<String> getLogoutAcceptedIssuers() {
        return logoutAcceptedIssuers;
    }

    public void setLogoutAcceptedIssuers(java.util.List<String> logoutAcceptedIssuers) {
        this.logoutAcceptedIssuers = logoutAcceptedIssuers;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }
}
