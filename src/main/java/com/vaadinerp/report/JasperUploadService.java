package com.vaadinerp.report;

import net.sf.jasperreports.engine.JRException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

/**
 * Menyimpan template Jasper yang di-upload. Hanya .jrxml yang diterima;
 * .jasper ditolak untuk mencegah Java deserialization vulnerability (CVE-2023-46750).
 * .jrxml divalidasi (compile) saat upload agar file rusak ditolak dini.
 */
@Service
public class JasperUploadService {

    private final JasperTemplateService templates;
    private final ReportResolver resolver;

    public JasperUploadService(JasperTemplateService templates, ReportResolver resolver) {
        this.templates = templates;
        this.resolver = resolver;
    }

    public void saveUpload(String code, String filename, byte[] bytes) {
        String lower = filename == null ? "" : filename.toLowerCase();
        if (!lower.endsWith(".jrxml")) {
            throw new IllegalArgumentException(
                    "Only .jrxml files are accepted. " +
                    ".jasper files are blocked to prevent deserialization attacks (CVE-2023-46750).");
        }
        try {
            templates.compileForUpload(new ByteArrayInputStream(bytes)); // early validation
        } catch (JRException e) {
            throw new RuntimeException("Invalid .jrxml file: " + e.getMessage(), e);
        }
        File target = resolver.resolveMasterTemplate(code, "JASPER", filename);
        try {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            Files.write(target.toPath(), bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save template: " + e.getMessage(), e);
        }
    }
}
