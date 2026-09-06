package com.vaadinerp.components;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Katalog DSL Groovy: keterangan singkat tiap nama dan potongan kode siap pakai.
 *
 * Daftar nama yang berlaku per scope ada di {@code ScriptExecutorService}
 * (ROW_SCRIPT_NAMES / ACTION_SCRIPT_NAMES) — di sini hanya keterangannya, supaya
 * cheat sheet selalu dibangun dari daftar yang sama dengan yang dipakai pemeriksa
 * nama dan tidak bisa menyimpang sendiri.
 *
 * Tanda tangan di bawah disalin dari implementasi closure-nya, bukan dikarang.
 */
public final class GroovyDsl {

    private GroovyDsl() {
    }

    private static final Map<String, String> SIGNATURES = buildSignatures();

    private static Map<String, String> buildSignatures() {
        Map<String, String> m = new LinkedHashMap<>();
        // Data
        m.put("header", "Map nilai field header/master — header.qty");
        m.put("form", "Alias untuk header");
        m.put("row", "Map baris yang sedang diproses — row.total = 0");
        m.put("rowIndex", "Nomor urut baris, mulai dari 1");
        m.put("items", "List semua baris yang sudah ada di grid");
        m.put("selectedRows", "List baris yang tercentang di grid");
        m.put("ctx", "Konteks aksi — ctx.getUserId()");
        // Database
        m.put("db", "db.find(table, keyColumn, keyValue) -> Map\ndb.getValue(sql, args...) -> Object");
        // Baca / tulis komponen form
        m.put("getElementValue", "getElementValue(ref, selectedOnly) -> List<Map>");
        m.put("setElementValue", "setElementValue(ref, value)");
        m.put("setElementReadonly", "setElementReadonly(ref, true|false)");
        m.put("setElementEnabled", "setElementEnabled(ref, true|false)");
        m.put("setElementDisabled", "setElementDisabled(ref, true|false)");
        m.put("clearForm", "clearForm() — kosongkan seluruh form");
        m.put("refreshForm", "refreshForm() — muat ulang data form");
        // Pesan ke user
        m.put("showSuccess", "showSuccess(title, message)");
        m.put("showError", "showError(title, message)");
        m.put("msgBox", "msgBox(args...) — kotak pesan sederhana");
        m.put("prompt", "prompt(args...) — minta input dari user");
        m.put("showYesNoDialog", "showYesNoDialog(title, message, callback)");
        m.put("showOptionsDialog", "showOptionsDialog(args...) — pilihan ganda");
        m.put("showDialog", "Alias untuk showOptionsDialog");
        // Lain-lain
        m.put("showMainTab", "showMainTab(tabId, title[, url[, extra]])");
        m.put("executeProcedure", "executeProcedure(procName, callbackOrJson, args...) -> boolean");
        m.put("JsonOutput", "Kelas groovy.json.JsonOutput");
        m.put("JsonSlurper", "Kelas groovy.json.JsonSlurper");
        return m;
    }

    /** Keterangan satu nama, atau string kosong bila belum didokumentasikan. */
    public static String signature(String name) {
        return SIGNATURES.getOrDefault(name, "");
    }

    /**
     * Potongan kode untuk script level form dan toolbar. Disusun dari pola yang
     * benar-benar dipakai di meta_form_action, bukan contoh karangan.
     */
    public static Map<String, String> actionSnippets() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Batalkan simpan dengan pesan (BEFORE_SAVE)",
                """
                        if (header.quantity == null) {
                            showError("Validation", "Quantity is required.")
                            return false
                        }
                        return true""");
        m.put("Jumlahkan baris detail lalu validasi (BEFORE_SAVE)",
                """
                        def detailRows = getElementValue("subform_grid1", false)
                        if (detailRows == null || detailRows.isEmpty()) {
                            showError("Validation", "Details not yet filled in.")
                            return false
                        }
                        def total = 0.0
                        for (r in detailRows) {
                            def qty = r['quantity']
                            if (qty != null && qty.toString().trim() != "") {
                                total += new BigDecimal(qty.toString())
                            }
                        }
                        if (total > header.quantity) {
                            showError("Validation", "Total detail exceeds header quantity.")
                            return false
                        }
                        return true""");
        m.put("Isi nilai default saat New (ON_LOAD_NEW)",
                """
                        setElementValue("header.status", "DRAFT")
                        setElementValue("header.trans_date", new java.util.Date())""");
        m.put("Kunci field mengikuti checkbox (ON_CHANGE)",
                """
                        // Trigger Field diisi nama checkbox-nya, mis. is_harga_manual
                        setElementReadonly("harga_satuan", !header.is_harga_manual)""");
        m.put("Jalankan stored procedure (AFTER_SAVE)",
                "executeProcedure('update_production_order_status', { success -> }, header.id)");
        m.put("Cek hak akses user sebelum simpan",
                """
                        def countAdmin = db.getValue(\"""
                            select count('x') from public.app_user_roles a
                            where a.username = ? and a.role_code in ('SUPER_ADMIN','ADMIN')
                            \""", ctx.getUserId())
                        if (countAdmin != null && countAdmin > 0) {
                            return true
                        }
                        showError("Access", "You are not allowed to save this record.")
                        return false""");
        m.put("Ambil satu baris dari tabel lain",
                """
                        def item = db.find('msitem', 'itemid', header.itemid)
                        if (item != null) {
                            setElementValue("header.itemname", item.itemname)
                        }""");
        return m;
    }

    /** Potongan kode untuk On-Add-Row Script (scope baris subform). */
    public static Map<String, String> rowSnippets() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Baris pertama aktif, sisanya tidak",
                """
                        if (rowIndex == 1) {
                            row.status = true
                        } else {
                            row.status = false
                        }""");
        m.put("Ambil nilai dari header",
                "row.perseries = header.qty != null ? header.qty : 1");
        m.put("Kalkulasi antar kolom di baris yang sama",
                "row.total = (row.qty != null ? row.qty : 0) * (row.price != null ? row.price : 0)");
        m.put("Lookup tabel lain",
                """
                        def item = db.find('lov_item', 'item_code', row.item_code)
                        if (item != null) {
                            row.price = item.default_price
                        }""");
        return m;
    }
}
