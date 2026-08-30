package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import net.sf.jasperreports.engine.JRException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Menyimpan template Jasper yang di-upload. Hanya .jrxml yang diterima;
 * .jasper ditolak untuk mencegah Java deserialization vulnerability (CVE-2023-46750).
 * .jrxml divalidasi (compile) saat upload agar file rusak ditolak dini.
 */
@Service
public class JasperUploadService {

    private final JasperTemplateService templates;
    private final ReportResolver resolver;
    private final ReportMetaRepository reportMetaRepository;

    public JasperUploadService(JasperTemplateService templates, ReportResolver resolver, ReportMetaRepository reportMetaRepository) {
        this.templates = templates;
        this.resolver = resolver;
        this.reportMetaRepository = reportMetaRepository;
    }

    public void validateUpload(byte[] bytes) {
        try {
            templates.compileForUpload(new ByteArrayInputStream(bytes));
        } catch (JRException e) {
            throw new RuntimeException("Invalid .jrxml file: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void saveUpload(String code, String filename, byte[] bytes) {
        String lower = filename == null ? "" : filename.toLowerCase();
        if (!lower.endsWith(".jrxml")) {
            throw new IllegalArgumentException(
                    "Only .jrxml files are accepted. " +
                    ".jasper files are blocked to prevent deserialization attacks (CVE-2023-46750).");
        }
        File target = resolver.resolveMasterTemplate(code, "JASPER", filename);
        try {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            Files.write(target.toPath(), bytes);
            
            // Update the template_path in the database so ReportResolver knows it's a .jrxml
            Optional<ReportMeta> optionalReport = reportMetaRepository.findById(code);
            if (optionalReport.isPresent()) {
                ReportMeta report = optionalReport.get();
                report.setTemplatePath(filename); // Ensure it ends with .jrxml
                reportMetaRepository.save(report);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save template: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteUpload(String code, String filename) {
        File target = resolver.resolveMasterTemplate(code, "JASPER", filename);
        if (target.exists()) {
            target.delete();
        }
        
        Optional<ReportMeta> optionalReport = reportMetaRepository.findById(code);
        if (optionalReport.isPresent()) {
            ReportMeta report = optionalReport.get();
            report.setTemplatePath(null);
            reportMetaRepository.save(report);
        }
    }
}
