package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.report.render.ReportContext;
import com.vaadinerp.report.render.ReportOutput;
import com.vaadinerp.report.render.ReportRenderer;
import com.vaadinerp.report.render.ReportRendererRegistry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Orkestrasi menjalankan report untuk Preview/run: resolve template, ambil data,
 * pilih renderer. STIMULSOFT ditampilkan via web viewer (URL); selain itu di-render
 * ke output (Standard HTML / Jasper PDF).
 */
@Service
public class ReportRunService {

    private final ReportResolver resolver;
    private final ReportDataService dataService;
    private final ReportRendererRegistry registry;

    public ReportRunService(ReportResolver resolver, ReportDataService dataService,
                            ReportRendererRegistry registry) {
        this.resolver = resolver;
        this.dataService = dataService;
        this.registry = registry;
    }

    public ReportRunResult run(ReportMeta report, Map<String, Object> params, String format, boolean sample) {
        String engine = report.getEngineType() != null ? report.getEngineType() : "STANDARD";
        beforeRun(report, params); // titik ekstensi (no-op)

        if ("STIMULSOFT".equalsIgnoreCase(engine)) {
            StringBuilder url = new StringBuilder("/stimulsoft-java/viewer?code=").append(report.getReportCode());
            if (params != null) {
                for (Map.Entry<String, Object> e : params.entrySet()) {
                    Object v = e.getValue();
                    if (v == null) continue;
                    // Parameter FORM_FIELD berisi List: ulangi key untuk tiap nilai, karena
                    // toString sebuah List ("[38, 42]") bukan parameter query yang valid.
                    if (v instanceof java.util.Collection<?> c) {
                        for (Object item : c) {
                            if (item != null) appendParam(url, e.getKey(), item);
                        }
                    } else {
                        appendParam(url, e.getKey(), v);
                    }
                }
            }
            return ReportRunResult.stimulsoft(url.toString());
        }

        List<Map<String, Object>> data = dataService.fetchData(report, params, sample);
        File template = "STANDARD".equalsIgnoreCase(engine)
                ? null
                : resolver.resolveMasterTemplate(report.getReportCode(), engine, report.getTemplatePath());
        ReportContext ctx = new ReportContext(report.getReportCode(), engine, template, data, params,
                report.getPageSize(), report.getOrientation(), report.getReportTitle(),
                report.getElements(), report.getGroupBy());
        ReportRenderer renderer = registry.forEngine(engine);
        ReportOutput out = renderer.export(ctx, format != null ? format : "PDF");
        afterRun(report, params, data.size()); // titik ekstensi (no-op)
        return ReportRunResult.rendered(out);
    }

    private static void appendParam(StringBuilder url, String key, Object value) {
        url.append("&")
           .append(java.net.URLEncoder.encode(key, java.nio.charset.StandardCharsets.UTF_8))
           .append("=")
           .append(java.net.URLEncoder.encode(value.toString(), java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Titik ekstensi sebelum report dijalankan. No-op sekarang; wiring Groovy opsional
     * (ScriptExecutorService) ditambahkan di sini bila ada kebutuhan konkret — belum ada
     * field script / kolom DB (YAGNI).
     */
    protected void beforeRun(ReportMeta report, Map<String, Object> params) {}

    /** Titik ekstensi setelah report dijalankan (mis. audit). No-op sekarang. */
    protected void afterRun(ReportMeta report, Map<String, Object> params, int rowCount) {}
}
