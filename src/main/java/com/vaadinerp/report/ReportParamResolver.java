package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolusi parameter otomatis:
 * FORM_FIELD → ambil dari record form yang terbuka; SYSTEM → keyword ($CURRENT_USER, CURRENT_DATE);
 * USER_INPUT → diabaikan (diisi user via ReportParameterForm).
 */
public final class ReportParamResolver {

    private ReportParamResolver() {}

    public static Map<String, Object> resolveAuto(List<ReportParamMeta> params,
                                                  Map<String, Object> record, String currentUser) {
        Map<String, Object> out = new HashMap<>();
        if (params == null) return out;
        for (ReportParamMeta p : params) {
            String source = p.getSource() == null ? "USER_INPUT" : p.getSource().trim().toUpperCase();
            if ("FORM_FIELD".equals(source)) {
                if (record != null && p.getSourceKey() != null && record.containsKey(p.getSourceKey())) {
                    out.put(p.getParamName(), record.get(p.getSourceKey()));
                }
            } else if ("SYSTEM".equals(source)) {
                String key = p.getSourceKey() == null ? "" : p.getSourceKey().trim().toUpperCase();
                if (key.equals("$CURRENT_USER")) {
                    out.put(p.getParamName(), currentUser);
                } else if (key.equals("CURRENT_DATE")) {
                    out.put(p.getParamName(), LocalDate.now());
                }
            }
            // USER_INPUT: diisi user via ReportParameterForm
        }
        return out;
    }

    public static List<ReportParamMeta> userInputParams(List<ReportParamMeta> params) {
        if (params == null) return List.of();
        return params.stream()
                .filter(p -> p.getSource() == null || "USER_INPUT".equalsIgnoreCase(p.getSource().trim()))
                .collect(Collectors.toList());
    }
}
