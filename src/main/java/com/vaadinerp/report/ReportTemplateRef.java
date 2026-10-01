package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Aturan "report ini memakai template report lain" (kolom meta_report.template_source_code).
 * Murni, tanpa Spring/UI, supaya bisa diuji. Pesan error berbahasa Inggris karena tampil di layar.
 */
public final class ReportTemplateRef {

    private ReportTemplateRef() {
    }

    /** Kode pemilik file template: sumber bila diisi, kalau tidak report itu sendiri. */
    public static String ownerCode(String code, String sourceCode) {
        return sourceCode == null || sourceCode.isBlank() ? code : sourceCode.trim();
    }

    /**
     * Validasi pilihan sumber template.
     *
     * @param source          report sumber hasil lookup (null bila tidak ditemukan)
     * @param selfHasDependents report ini sendiri sedang dipinjam report lain
     * @return pesan error, atau null bila boleh
     */
    public static String validateSource(String selfCode, String selfEngine, String sourceCode, ReportMeta source,
                                        boolean selfHasDependents) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return null;
        }
        String src = sourceCode.trim();
        if (src.equals(selfCode)) {
            return "A report cannot use its own template.";
        }
        if (source == null) {
            return "Source report '" + src + "' was not found.";
        }
        String srcEngine = engineOf(source.getEngineType());
        if ("STANDARD".equals(srcEngine)) {
            return "Source report '" + src + "' has no template file (engine STANDARD).";
        }
        if (!srcEngine.equals(engineOf(selfEngine))) {
            return "Source report '" + src + "' uses engine " + srcEngine + "; this report uses "
                    + engineOf(selfEngine) + ".";
        }
        if (source.getTemplateSourceCode() != null && !source.getTemplateSourceCode().isBlank()) {
            return "Source report '" + src + "' already uses another report's template; choose the original report.";
        }
        if (selfHasDependents) {
            return "Other reports use this report's template, so it cannot use another report's template.";
        }
        return null;
    }

    /** Kode report yang meminjam template milik {@code code}. */
    public static List<String> dependentsOf(Collection<ReportMeta> all, String code) {
        List<String> out = new ArrayList<>();
        if (all == null || code == null) {
            return out;
        }
        for (ReportMeta r : all) {
            if (r.getTemplateSourceCode() != null && code.equals(r.getTemplateSourceCode().trim())) {
                out.add(r.getReportCode());
            }
        }
        return out;
    }

    /**
     * Pesan penolakan hapus bila ada report yang meminjam template dari report yang akan dihapus
     * dan peminjam itu TIDAK ikut dihapus; null bila aman.
     */
    public static String blockDeleteMessage(Collection<ReportMeta> all, Set<String> deleting) {
        StringBuilder sb = new StringBuilder();
        for (String code : deleting) {
            List<String> users = new ArrayList<>(dependentsOf(all, code));
            users.removeAll(deleting);
            if (!users.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append("Report ").append(code).append(" is used as a template by: ")
                        .append(String.join(", ", users)).append(".");
            }
        }
        return sb.length() == 0 ? null
                : sb + " Delete those reports first, or switch them to their own template.";
    }

    private static String engineOf(String engine) {
        return engine == null || engine.isBlank() ? "STANDARD" : engine.trim().toUpperCase();
    }
}
