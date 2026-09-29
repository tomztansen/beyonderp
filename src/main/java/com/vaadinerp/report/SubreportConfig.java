package com.vaadinerp.report;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Konfigurasi banyak subreport Jasper untuk satu {@code ReportMeta}, disimpan sebagai JSON
 * array di kolom {@code subreports_json}. File fisiknya sendiri deterministik dari
 * {@code paramName} (lihat {@link ReportResolver#resolveSubreportFile}), jadi kolom ini cuma
 * menyimpan daftar param name + nama file asli (buat ditampilkan di UI Designer).
 */
public final class SubreportConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SubreportConfig() {
    }

    public record Entry(String paramName, String displayName) {
    }

    public static List<Entry> parse(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            Entry[] arr = MAPPER.readValue(json, Entry[].class);
            List<Entry> list = new ArrayList<>();
            for (Entry e : arr) {
                list.add(e);
            }
            return list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static String toJson(List<Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(entries);
        } catch (Exception ex) {
            return null;
        }
    }
}
