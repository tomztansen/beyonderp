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
    private final com.vaadinerp.service.ScriptExecutorService scriptExecutor;
    private final com.vaadinerp.security.service.SessionSecurityService securityService;

    public ReportRunService(ReportResolver resolver, ReportDataService dataService,
                            ReportRendererRegistry registry,
                            com.vaadinerp.service.ScriptExecutorService scriptExecutor,
                            com.vaadinerp.security.service.SessionSecurityService securityService) {
        this.resolver = resolver;
        this.dataService = dataService;
        this.registry = registry;
        this.scriptExecutor = scriptExecutor;
        this.securityService = securityService;
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
        afterRun(report, params, data != null ? data.size() : 0); // titik ekstensi (no-op)
        return ReportRunResult.rendered(out);
    }

    private static void appendParam(StringBuilder url, String key, Object value) {
        url.append("&")
           .append(java.net.URLEncoder.encode(key, java.nio.charset.StandardCharsets.UTF_8))
           .append("=")
           .append(java.net.URLEncoder.encode(value.toString(), java.nio.charset.StandardCharsets.UTF_8));
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReportRunService.class);

    /**
     * Titik ekstensi sebelum report dijalankan. 
     */
    protected void beforeRun(ReportMeta report, Map<String, Object> params) {
        if (report.getBeforeScript() != null && !report.getBeforeScript().isBlank()) {
            String username = securityService != null && securityService.getCurrentUser() != null
                ? securityService.getCurrentUser().getUsername() : "system";
            scriptExecutor.executeReportScript(report.getBeforeScript(), params, username, log);
        }
    }

    /** Titik ekstensi setelah report dijalankan (mis. audit). */
    protected void afterRun(ReportMeta report, Map<String, Object> params, int rowCount) {
        if (report.getAfterScript() != null && !report.getAfterScript().isBlank()) {
            String username = securityService != null && securityService.getCurrentUser() != null
                ? securityService.getCurrentUser().getUsername() : "system";
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    scriptExecutor.executeReportScript(report.getAfterScript(), params, username, log);
                } catch (Exception e) {
                    log.error("Error executing afterScript for report {}: {}", report.getReportCode(), e.getMessage());
                }
            });
        }
    }
}
