package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMetaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;

/**
 * Menentukan file template master untuk sebuah report + memvalidasi reportCode
 * (anti path-traversal). Interface dirancang agar penambahan salinan per-user
 * nanti = satu implementasi tambahan, tanpa membongkar konsumen.
 */
@Component
public class ReportResolver {

    private static final java.util.regex.Pattern CODE = java.util.regex.Pattern.compile("^[A-Za-z0-9_-]+$");

    @SuppressWarnings("unused")
    private final ReportMetaRepository reportMetaRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public ReportResolver(ReportMetaRepository reportMetaRepository) {
        this.reportMetaRepository = reportMetaRepository;
    }

    /** hanya untuk test: override uploadDir tanpa Spring. */
    void setUploadDirForTest(String dir) { this.uploadDir = dir; }

    public boolean isValidReportCode(String code) {
        return code != null && CODE.matcher(code).matches();
    }

    /** Ekstensi file template master per engine; null untuk STANDARD (tak punya file). */
    public String masterExtension(String engineType, String templatePath) {
        if (engineType == null) return null;
        switch (engineType.trim().toUpperCase()) {
            case "STIMULSOFT":
                return "mrt";
            case "JASPER":
                if (templatePath != null && templatePath.trim().toLowerCase().endsWith(".jrxml")) return "jrxml";
                return "jasper";
            default:
                return null; // STANDARD tak punya file template
        }
    }

    public File resolveMasterTemplate(String code, String engineType, String templatePath) {
        if (!isValidReportCode(code)) {
            throw new IllegalArgumentException("Invalid report code: " + code);
        }
        String ext = masterExtension(engineType, templatePath);
        if (ext == null) {
            throw new IllegalStateException("Engine " + engineType + " tidak punya file template");
        }
        File dir = new File(new File(uploadDir, "report_templates"), "master");
        return new File(dir, code + "." + ext);
    }
}
