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
