package com.vaadinerp.service;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Penanda server (DEV/STAGING/PROD) dari properti {@code app.environment}. Bersifat milik server yang
 * menjalankan aplikasi, bukan data, jadi tidak ikut tersalin kalau database dari produksi di-restore ke dev.
 *
 * Nilai kosong atau tidak dikenal TIDAK dianggap PROD: dev yang lupa disetel harus tampak salah (pita merah),
 * bukan diam-diam menyerupai produksi. Objek ini tidak punya state yang berubah, jadi aman dipakai bersama
 * oleh semua sesi.
 */
@Component
public class EnvironmentInfo {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentInfo.class);
    private static final String DEV_FAVICON = "icons/favicon-dev.ico";

    private enum Kind { PROD, DEV, STAGING, UNSET }

    private final Kind kind;

    public EnvironmentInfo(@Value("${app.environment:}") String raw) {
        this.kind = parse(raw);
        LOG.info("Environment: {} (app.environment='{}')", kind, raw == null ? "" : raw.trim());
        if (kind == Kind.UNSET) {
            LOG.warn("app.environment kosong/tidak dikenal; isi PROD, DEV, atau STAGING.");
        }
    }

    private static Kind parse(String raw) {
        String v = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        return switch (v) {
            case "PROD" -> Kind.PROD;
            case "DEV" -> Kind.DEV;
            case "STAGING" -> Kind.STAGING;
            default -> Kind.UNSET;
        };
    }

    public boolean isProd() {
        return kind == Kind.PROD;
    }

    /** Teks badge, null untuk PROD (produksi dibiarkan bersih). */
    public String badgeText() {
        return switch (kind) {
            case PROD -> null;
            case DEV -> "DEV";
            case STAGING -> "STAGING";
            case UNSET -> "ENV NOT SET";
        };
    }

    /** Warna latar badge; gelap cukup agar teks putihnya terbaca (kontras >= 4.5:1). */
    public String color() {
        return switch (kind) {
            case DEV -> "#c2410c";
            case STAGING -> "#6d28d9";
            case UNSET -> "#b91c1c";
            case PROD -> null;
        };
    }

    public String titlePrefix() {
        return isProd() ? "" : "[" + badgeText() + "] ";
    }

    public String faviconPath() {
        return isProd() ? null : DEV_FAVICON;
    }

    /**
     * JS untuk dijalankan sekali per halaman: awalan judul tab (dipasang ulang setiap judul berubah, mis.
     * saat navigasi dengan @PageTitle) dan favicon dev. Seluruhnya jalan di browser; server tidak memegang
     * listener maupun state. Penjaga window flag mencegah observer ganda kalau dipanggil lagi di halaman
     * yang sama. Null untuk PROD.
     */
    public String pageScript() {
        if (isProd()) {
            return null;
        }
        // $0 = awalan judul, $1 = path favicon (dikirim sebagai parameter, bukan digabung ke string JS)
        return "if (window.__envMarked) return; window.__envMarked = true;"
                + "var t = document.querySelector('title');"
                + "if (!t) { t = document.createElement('title'); document.head.appendChild(t); }"
                + "var fix = function () { if (!document.title.startsWith($0)) document.title = $0 + document.title; };"
                + "new MutationObserver(fix).observe(t, {childList: true, characterData: true, subtree: true});"
                + "fix();"
                + "document.querySelectorAll(\"link[rel~='icon']\").forEach(function (l) { l.href = $1; });";
    }
}
