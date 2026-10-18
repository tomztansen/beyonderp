package com.vaadinerp.views;

import com.vaadinerp.views.GenericFormView.FilterCriteria;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameter terkunci {@code _FIXED_<kolom>=<nilai>} untuk form yang dibuka dengan parameter awal (mis. dari tombol
 * "Add new" di LOV). Satu form bersama bisa dipakai banyak kategori: nilainya
 * <ul>
 * <li>selalu menyaring grid (dan tidak bisa dihapus user dari filter header),</li>
 * <li>mengisi record baru,</li>
 * <li>dipaksa lagi di sisi server saat simpan, apa pun isi tampilannya.</li>
 * </ul>
 * Kelas ini murni (tanpa Vaadin/Spring) supaya bisa diuji sendiri.
 */
final class FixedParams {

    static final String PREFIX = "_FIXED_";

    private FixedParams() {
    }

    private static boolean isFixedKey(String key) {
        return key.length() > PREFIX.length() && key.regionMatches(true, 0, PREFIX, 0, PREFIX.length());
    }

    /** Apakah parameter awal memuat kunci terkunci (walau nilainya nanti ditolak). */
    static boolean has(Object extra) {
        return extra instanceof Map<?, ?> m
                && m.keySet().stream().anyMatch(k -> k != null && isFixedKey(k.toString()));
    }

    /**
     * Kolom -> nilai. Nilai kosong ditolak. Nilai bermuatan % atau _ juga ditolak: di sisi query nilainya menjadi
     * pola ILIKE, jadi karakter itu akan menjadi wildcard dan "terkunci" tidak lagi berarti satu nilai.
     * Nama kolom harus pengenal SQL biasa.
     */
    static Map<String, String> parse(Object extra) {
        Map<String, String> out = new LinkedHashMap<>();
        if (!(extra instanceof Map<?, ?> m)) {
            return out;
        }
        for (Map.Entry<?, ?> e : m.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || !isFixedKey(e.getKey().toString())) {
                continue;
            }
            String column = e.getKey().toString().substring(PREFIX.length()).trim();
            String value = e.getValue().toString().trim();
            if (value.isEmpty() || value.contains("%") || value.contains("_") || !column.matches("^[a-zA-Z0-9_]+$")) {
                continue;
            }
            out.put(column, value);
        }
        return out;
    }

    /**
     * Filter yang dipakai untuk query: {@code base} ditimpa kolom terkunci (yang terkunci menang). Tanpa kolom
     * terkunci, {@code base} sendiri dikembalikan. Peta asli tidak pernah diubah, supaya isian filter header milik
     * user tidak tercemar.
     */
    static Map<String, FilterCriteria> overlay(Map<String, FilterCriteria> base, Map<String, String> fixed) {
        if (fixed.isEmpty()) {
            return base;
        }
        Map<String, FilterCriteria> out = new HashMap<>(base);
        fixed.forEach((column, value) -> {
            out.keySet().removeIf(k -> k.equalsIgnoreCase(column));
            FilterCriteria c = new FilterCriteria();
            c.operator = "Equals";
            c.value = value;
            out.put(column, c);
        });
        return out;
    }

    /** Menimpa nilai kolom terkunci di data yang akan disimpan. */
    static void force(Map<String, Object> data, Map<String, String> fixed) {
        fixed.forEach((column, value) -> {
            data.keySet().removeIf(k -> k != null && k.equalsIgnoreCase(column));
            data.put(column, value);
        });
    }
}
