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
        m.put("self", "Nilai field pemicu (trigger) saat ini. Berperilaku seperti nilai aslinya "
                + "(if (self), self == 'Y'); kalau fieldnya LOV, properti record-nya bisa diakses langsung, mis. self.itemname");
        // Database
        m.put("db", "db.find(table, keyColumn, keyValue) -> Map\ndb.getValue(sql, args...) -> Object");
        m.put("lov", "lov(fieldName) -> Map — ambil record LOV untuk nilai field itu saat ini, tanpa perlu tahu nama tabel/kolom kuncinya");
        m.put("uploadDir", "String path folder upload SEMENTARA (file yang baru diupload, belum disimpan) — "
                + "gabungkan dengan nama file dari field FILE_UPLOAD (mis. header.dokumen) pakai "
                + "java.nio.file.Paths.get(uploadDir, namaFile) untuk baca isinya");
        // Baca / tulis komponen form
        m.put("getElementValue", "getElementValue(ref, selectedOnly) -> List<Map>");
        m.put("sendEmail", "sendEmail(to, cc, subject, htmlBody, attachments) — antre email untuk dikirim "
                + "(bukan langsung kirim, worker terjadwal yang memprosesnya). to/cc dipisah koma kalau lebih "
                + "dari satu alamat, isi null/\"\" kalau cc/attachments tidak dipakai. attachments = nama file "
                + "yang SUDAH ada di folder upload (dipisah koma), bukan file baru");
        m.put("renderReport", "renderReport(reportCode, params[, format[, fileName]]) -> String — render report "
                + "Jasper/Standard ke file (format PDF default, atau XLSX) di folder report_out, TANPA "
                + "before/after script report. Parameter SYSTEM ($CURRENT_USER, CURRENT_DATE) dan default "
                + "Designer terisi otomatis seperti di Report Runner; params dari script menimpanya. "
                + "Nama file sama = ditimpa. Hasilnya langsung dipakai sebagai "
                + "attachments sendEmail(). Stimulsoft tidak didukung");
        m.put("plain", "plain(value) -> value without the header wrappers. header.xxx values are wrapped objects "
                + "(they behave like numbers/maps), so JsonOutput.toJson writes text without quotes and empty values as "
                + "{} -> invalid JSON. Always wrap what you build from header/selectedRows before JsonOutput.toJson: "
                + "JsonOutput.toJson(plain([[table: 'thx', data: [des: header.des, wh: header.mswarehouseid]]])). "
                + "Works on single values, maps and lists (nested); returns a copy. Dates/times become ISO text "
                + "(\"2026-10-01\", \"2026-10-01T11:30:05\") instead of a Java bean object");
        m.put("downloadCsv", "downloadCsv(fileName, rows[, headers[, delimiter]]) -> boolean — send query results to "
                + "the user's browser as a CSV download. rows = list of maps, e.g. db.queryForList(\"SELECT idno AS \\\"Serial No\\\" "
                + "FROM ...\"). Column titles come from the column names of the first row (use SQL aliases to rename). "
                + "headers: a map column->title picks, orders and renames columns ([idno: 'Serial No']); false = no "
                + "title row. delimiter defaults to ';' (Excel Indonesia), or use ',' '|' or a tab. UTF-8 with BOM, "
                + "fields are quoted/escaped, null = empty, cells starting with = + - @ are neutralised. Limit: "
                + "50,000 rows (otherwise nothing is downloaded and an error is shown). IMPORTANT: db.queryForList loads "
                + "ALL rows into memory before this check, so put a LIMIT in the query (e.g. LIMIT 50001); and if the "
                + "SQL itself fails, db.queryForList returns an empty list, which shows up here as \"No data to "
                + "export\". Needs a browser session: not available in scheduled jobs");
        m.put("downloadFile", "downloadFile(path[, downloadName]) -> boolean — send a file from report_out to the "
                + "user's browser as a download, typically the result of renderReport(): "
                + "def f = renderReport('RPT_X', [P_ID: header.id], 'XLSX', 'X_' + header.idno); downloadFile(f). "
                + "Only files inside report_out are allowed (no other path). Returns false and shows an error if the "
                + "file is invalid or missing. Needs a browser session: not available in scheduled jobs. "
                + "Include the user or time in the renderReport file name so two users do not overwrite each "
                + "other's file");
        m.put("runScheduledJob", "runScheduledJob(jobCode) -> String — run a scheduled job NOW (the saved version) "
                + "through the same queue as the normal schedule. A job that is already running is not started "
                + "twice. For an Extra Toolbar button on the job form: runScheduledJob(header.job_code)");
        m.put("jobCode", "Code of the job that is currently running (only in scheduled job scripts)");
        m.put("sendWhatsApp", "sendWhatsApp(chatId, message, sessionId) — antre notifikasi WhatsApp (worker "
                + "terjadwal yang mengirim). chatId format '<nomor>@c.us' (personal) atau '<groupId>@g.us' (grup). "
                + "sessionId isi null untuk pakai nomor default (app.whatsapp.session-id), atau nama sesi OpenWA "
                + "lain (lihat dashboard OpenWA > Sessions) untuk kirim dari nomor berbeda");
        m.put("sendWhatsAppApproval", "sendWhatsAppApproval(chatId, message, procName, jsonParams, sessionId) — "
                + "antre pesan WhatsApp berisi permintaan approval. Kalau nomor itu membalas APPROVE/OK/YA, "
                + "procName dipanggil sebagai stored procedure dengan jsonParams; balasan REJECT/TOLAK cuma "
                + "menutup permintaannya. Satu permintaan PENDING per nomor pada satu waktu. sessionId sama "
                + "seperti di sendWhatsApp — null untuk default");
        m.put("setElementValue", "setElementValue(ref, value)");
        m.put("setElementReadonly", "setElementReadonly(ref, true|false)");
        m.put("setElementEnabled", "setElementEnabled(ref, true|false)");
        m.put("setElementDisabled", "setElementDisabled(ref, true|false)");
        m.put("setElementVisible", "setElementVisible(ref, true|false) — untuk kolom subform grid, "
                + "menyembunyikan SELURUH kolom (semua baris), bukan cuma satu sel");
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
        m.put("Sembunyikan field header berdasar mode (ON_CHANGE)",
                """
                        // Trigger Field diisi nama field mode-nya, mis. header.mode
                        setElementVisible("harga_manual", header.mode == "MANUAL")""");
        m.put("Kirim report sebagai lampiran email",
                """
                        def file = renderReport("RPT_SPK_DOC_JSP", [P_ORDER_ID: header.id], "PDF", "SPK_" + header.idno)
                        sendEmail("user@example.com", null, "SPK " + header.idno, "<p>Attached is the SPK.</p>", file)""");
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
        m.put("Sembunyikan kolom subform berdasar status header",
                """
                        // Nama kolom langsung tanpa prefix -- target-nya subform ini sendiri,
                        // seluruh kolom (semua baris), bukan cuma baris yang sedang diedit
                        setElementVisible("harga_beli", header.status == "PURCHASE")
                        setElementVisible("harga_jual", header.status == "SALES")""");
        m.put("Lookup tabel lain",
                """
                        def item = db.find('lov_item', 'item_code', row.item_code)
                        if (item != null) {
                            row.price = item.default_price
                        }""");
        return m;
    }

    /** Potongan kode untuk script job terjadwal (GROOVY_EDITOR di form job). */
    public static Map<String, String> scheduledJobSnippets() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Send a summary email",
                """
                        def rows = db.queryForList("SELECT to_char(now(), 'DD Mon YYYY') AS today")
                        sendEmail('someone@growthsteel.com', null, 'Daily summary ' + rows[0].today,
                                '<p>Hello, this is your daily summary.</p>', null)
                        """);
        m.put("Send a report as attachment",
                """
                        def file = renderReport('REPORT_CODE', [:], 'PDF', 'DAILY_' + jobCode)
                        sendEmail('someone@growthsteel.com', null, 'Daily report', '<p>See attachment.</p>', file)
                        """);
        return m;
    }
}
