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

    /** Ganti seluruh database report dengan satu StiJsonDatabase "DynamicData" berisi {@code data}. */
    public static StiReport bindData(StiReport report, List<Map<String, Object>> data) throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("DynamicData", data != null ? data : List.of());
        String json = MAPPER.writeValueAsString(root);

        report.getDictionary().getDatabases().clear();
        StiJsonDatabase db = new StiJsonDatabase("DynamicData", "");
        db.setJsonData(json);
        report.getDictionary().getDatabases().add(db);
        report.getDictionary().synchronize();
        return report;
    }

    @Override
    public ReportOutput render(ReportContext ctx) {
        throw new UnsupportedOperationException(
                "Stimulsoft dirender via web viewer (lihat StimulsoftJavaController)");
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        throw new UnsupportedOperationException("Export Stimulsoft via toolbar viewer");
    }
}
