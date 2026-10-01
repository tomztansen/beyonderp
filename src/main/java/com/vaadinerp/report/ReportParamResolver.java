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
 * BOTH → hybrid: ikut FORM_FIELD kalau ada record/baris (dipanggil dari Form), kalau tidak
 * (dipanggil dari Report Runner, record/rows selalu kosong) jatuh ke USER_INPUT -- tetap
 * ditanyakan lewat ReportParameterForm. Supaya 1 report+param bisa dipakai dari Runner maupun
 * Form tanpa duplikasi konfigurasi.
 */
public final class ReportParamResolver {

    private ReportParamResolver() {}

    /**
     * Kunci tambahan untuk parameter ber-LOV, meniru bean GenericFormView: nilai utama tetap di
     * {@code out[paramName]} (tidak disentuh), ditambah {@code paramName_label} (teks tampilan) dan
     * {@code paramName.kolom} untuk tiap kolom record LOV yang dipilih. Satu record -> nilai
     * biasa (kolom null dilewati, sama seperti GenericFormView); banyak record (ChosenBox) ->
     * daftar nilai per record dengan urutan yang sama (null tetap ada supaya sejajar).
     *
     * @param label   teks tampilan; null = kunci _label tidak ditulis
     * @param records record LOV terpilih; null/kosong = tidak ada kunci kolom
     */
    public static void putLovExtras(Map<String, Object> out, String paramName, String label,
                                    List<Map<String, Object>> records) {
        putLovExtras(out, paramName, label, records, false);
    }

    /**
     * @param alwaysList true untuk komponen multi-pilih (ChosenBox): kolom SELALU berupa daftar,
     *                   juga saat hanya satu record terpilih, supaya script tidak perlu menebak tipenya.
     */
    public static void putLovExtras(Map<String, Object> out, String paramName, String label,
                                    List<Map<String, Object>> records, boolean alwaysList) {
        if (label != null) {
            out.put(paramName + "_label", label);
        }
        if (records == null || records.isEmpty()) {
            return;
        }
        if (records.size() == 1 && !alwaysList) {
            for (Map.Entry<String, Object> e : records.get(0).entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    out.put(paramName + "." + e.getKey(), e.getValue());
                }
            }
            return;
        }
        java.util.Set<String> columns = new java.util.LinkedHashSet<>();
        for (Map<String, Object> r : records) {
            for (String k : r.keySet()) {
                if (k != null) columns.add(k);
            }
        }
        for (String col : columns) {
            List<Object> values = new ArrayList<>(records.size());
            for (Map<String, Object> r : records) {
                values.add(r.get(col));
            }
            out.put(paramName + "." + col, values);
        }
    }

    /**
     * Apakah {@code key} kunci turunan LOV ({@code param_label} / {@code param.kolom}) dari parameter
     * yang dideklarasikan report? Dipakai engine yang menyalin semua params ke luar (Stimulsoft:
     * query string viewer) supaya turunan ini tidak ikut. Parameter yang memang dideklarasikan
     * dengan nama persis itu tetap dianggap parameter biasa.
     */
    public static boolean isLovExtraKey(String key, List<ReportParamMeta> declared) {
        if (key == null || declared == null) {
            return false;
        }
        for (ReportParamMeta p : declared) {
            if (key.equals(p.getParamName())) {
                return false;
            }
        }
        for (ReportParamMeta p : declared) {
            String n = p.getParamName();
            if (n != null && (key.equals(n + "_label") || key.startsWith(n + "."))) {
                return true;
            }
        }
        return false;
    }

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
            if ("FORM_FIELD".equals(source) || "BOTH".equals(source)) {
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
            if ("FORM_FIELD".equals(source) || "BOTH".equals(source)) {
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
                .filter(p -> p.getSource() == null || "USER_INPUT".equalsIgnoreCase(p.getSource().trim())
                        || "BOTH".equalsIgnoreCase(p.getSource().trim()))
                .collect(Collectors.toList());
    }
}
