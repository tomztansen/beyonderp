package com.vaadinerp.report;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Muat/compile template Jasper. Hanya .jrxml yang di-load (di-compile dulu).
 * File .jasper ditolak untuk mencegah Java deserialization vulnerability.
 * Hasil di-cache by path+mtime agar compile hanya sekali per versi file.
 */
@Service
public class JasperTemplateService {

    /** Maksimum 50 template di-cache; entry yang tidak diakses >30 menit di-evict otomatis. */
    private final Cache<String, JasperReport> cache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    /** Compile .jrxml dari stream (dipakai validasi saat upload). */
    public JasperReport compileForUpload(InputStream jrxml) throws JRException {
        return JasperCompileManager.compileReport(jrxml);
    }

    /**
     * .jrxml → compile. Hasil di-cache by path+mtime.
     * .jasper ditolak untuk mencegah deserialization vulnerability.
     */
    public JasperReport loadCompiled(File template) throws JRException {
        if (!template.getName().toLowerCase().endsWith(".jrxml")) {
            throw new JRException(
                    "Only .jrxml files are accepted. " +
                    ".jasper files are blocked to prevent deserialization attacks.");
        }
        String key = template.getAbsolutePath() + "#" + template.lastModified();
        return cache.get(key, k -> {
            try {
                return JasperCompileManager.compileReport(template.getAbsolutePath());
            } catch (JRException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
