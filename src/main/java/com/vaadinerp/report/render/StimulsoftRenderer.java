package com.vaadinerp.report.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stimulsoft.report.StiReport;
import com.stimulsoft.report.dictionary.databases.StiJsonDatabase;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renderer engine STIMULSOFT. Stimulsoft ditampilkan via web viewer (embed) di
 * layer controller/UI; kelas ini menyediakan {@link #bindData} yang menyuntik
 * datasource JSON "DynamicData" ke report.
 */
@Component
public class StimulsoftRenderer implements ReportRenderer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String engine() {
        return "STIMULSOFT";
    }

    private static final String DYNAMIC_DATA_NAME = "DynamicData";

    /**
     * Ganti database "DynamicData" dengan data terbaru. Database lain yang didesain di
     * Stimulsoft Designer (dipakai sub-report Page lain, dsb.) dibiarkan utuh -- dulu
     * bindData() ini clear() semua database, jadi mematikan sub-report yang punya
     * data source sendiri. Sekarang cuma hapus-lalu-pasang ulang "DynamicData" saja.
     */
    public static StiReport bindData(StiReport report, List<Map<String, Object>> data) throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put(DYNAMIC_DATA_NAME, data != null ? data : List.of());
        String json = MAPPER.writeValueAsString(root);

        report.getDictionary().removeDatabase(DYNAMIC_DATA_NAME);
        StiJsonDatabase db = new StiJsonDatabase(DYNAMIC_DATA_NAME, "");
        db.setJsonData(json);
        report.getDictionary().getDatabases().add(db);
        report.getDictionary().synchronize();
        return report;
    }

    private static final java.util.List<String> SYSTEM_KEYS = java.util.List.of(
            com.vaadinerp.report.ReportParamResolver.P_CURRENT_USER,
            com.vaadinerp.report.ReportParamResolver.P_CURRENT_USER_NAME,
            com.vaadinerp.report.ReportParamResolver.P_CURRENT_ROLE);

    /**
     * Identitas dari objek user di sesi HTTP server (atribut SPRING_MVC_USER). Bukan dari URL, jadi tidak
     * bisa dipalsukan. Objek lain / null = kosong.
     */
    public static Map<String, Object> systemParamsFromSessionUser(Object sessionUser) {
        if (!(sessionUser instanceof com.vaadinerp.security.entity.AppUser u)) {
            return Map.of();
        }
        return com.vaadinerp.report.ReportParamResolver.currentUserParams(u.getUsername(), u.getFullName(), u.getRoles());
    }

    /**
     * Parameter dari URL tanpa kunci identitas (siapa pun bisa mengetik &CURRENT_USER=...), lalu diganti nilai
     * tepercaya dari sesi. Peta asli tidak diubah.
     */
    public static Map<String, Object> paramsWithTrustedIdentity(Map<String, Object> urlParams, Map<String, Object> trusted) {
        Map<String, Object> out = urlParams != null ? new HashMap<>(urlParams) : new HashMap<>();
        SYSTEM_KEYS.forEach(out::remove);
        if (trusted != null) out.putAll(trusted);
        return out;
    }

    /**
     * Tambahkan CURRENT_USER / CURRENT_USER_NAME / CURRENT_ROLE sebagai kolom di setiap baris DynamicData,
     * supaya template memakai {DynamicData.CURRENT_USER_NAME} di band mana pun. Kolom bernama sama dari query
     * tidak ditimpa. Baris asli tidak diubah; tanpa nilai atau tanpa data mengembalikan apa adanya.
     */
    public static List<Map<String, Object>> withSystemColumns(List<Map<String, Object>> data, Map<String, Object> system) {
        if (data == null || data.isEmpty() || system == null || system.isEmpty()) {
            return data;
        }
        List<Map<String, Object>> out = new java.util.ArrayList<>(data.size());
        for (Map<String, Object> row : data) {
            Map<String, Object> copy = new java.util.LinkedHashMap<>(row);
            system.forEach(copy::putIfAbsent);
            out.add(copy);
        }
        return out;
    }

    @Override
    public ReportOutput render(ReportContext ctx) {
        throw new UnsupportedOperationException(
                "Stimulsoft is rendered via the web viewer (see StimulsoftJavaController)");
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        throw new UnsupportedOperationException("Export Stimulsoft via the viewer toolbar");
    }
}
