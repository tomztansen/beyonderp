package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;

import java.time.LocalDate;
import java.util.ArrayList;
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

    private static String sourceOf(ReportParamMeta p) {
        return p.getSource() == null ? "USER_INPUT" : p.getSource().trim().toUpperCase();
    }

    /** SYSTEM keyword → nilai. Dipakai bersama oleh resolveAuto dan resolveFromRows. */
    private static void putSystem(ReportParamMeta p, String currentUser, Map<String, Object> out) {
        String key = p.getSourceKey() == null ? "" : p.getSourceKey().trim().toUpperCase();
        if (key.equals("$CURRENT_USER")) {
            out.put(p.getParamName(), currentUser);
        } else if (key.equals("CURRENT_DATE")) {
            out.put(p.getParamName(), LocalDate.now());
        }
    }

    /** Satu record form → nilai skalar untuk FORM_FIELD. Dipakai Report Runner. */
    public static Map<String, Object> resolveAuto(List<ReportParamMeta> params,
                                                  Map<String, Object> record, String currentUser) {
        Map<String, Object> out = new HashMap<>();
        if (params == null) return out;
        for (ReportParamMeta p : params) {
            String source = sourceOf(p);
            if ("FORM_FIELD".equals(source)) {
                if (record != null && p.getSourceKey() != null && record.containsKey(p.getSourceKey())) {
                    out.put(p.getParamName(), record.get(p.getSourceKey()));
                }
            } else if ("SYSTEM".equals(source)) {
                putSystem(p, currentUser, out);
            }
            // USER_INPUT: diisi user via ReportParameterForm
        }
        return out;
    }

    /**
     * Baris terpilih di grid → nilai FORM_FIELD berupa List, baik satu baris maupun banyak.
     * Aturan tunggal ini mencegah report berjalan saat user mencentang satu baris lalu gagal
     * saat mencentang baris kedua. Duplikat dan null dibuang; key tidak dimasukkan bila
     * hasilnya kosong, sehingga parameter yang tidak terisi tetap terdeteksi validasi required.
     */
    public static Map<String, Object> resolveFromRows(List<ReportParamMeta> params,
                                                      List<Map<String, Object>> rows, String currentUser) {
        Map<String, Object> out = new HashMap<>();
        if (params == null) return out;
        List<Map<String, Object>> safeRows = (rows == null) ? List.of() : rows;
        for (ReportParamMeta p : params) {
            String source = sourceOf(p);
            if ("FORM_FIELD".equals(source)) {
                String key = p.getSourceKey();
                if (key == null || key.isBlank()) continue;
                List<Object> values = new ArrayList<>();
                for (Map<String, Object> row : safeRows) {
                    if (row == null) continue;
                    Object v = row.get(key);
                    if (v != null && !values.contains(v)) values.add(v);
                }
                if (!values.isEmpty()) out.put(p.getParamName(), values);
            } else if ("SYSTEM".equals(source)) {
                putSystem(p, currentUser, out);
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
