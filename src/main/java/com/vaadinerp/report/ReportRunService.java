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
        java.util.List<ReportMessage> messages = new java.util.ArrayList<>();
        // CURRENT_USER / CURRENT_USER_NAME / CURRENT_ROLE otomatis (Jasper & STANDARD); Stimulsoft ditunda.
        if (!"STIMULSOFT".equalsIgnoreCase(engine)) {
            params = ReportParamResolver.withSystemParams(params, currentUserParams());
        }
        // Preview (Report Designer) tidak boleh ikut memicu before/after script -- itu bukan
        // "report benar-benar dijalankan", cuma pratinjau. Kalau tidak dijaga, klik Preview
        // berulang kali ikut menambah print_count dsb. seolah-olah report itu di-run beneran.
        if (!sample) {
            beforeRun(report, params, messages);
        }

        if ("STIMULSOFT".equalsIgnoreCase(engine)) {
            StringBuilder url = new StringBuilder("/stimulsoft-java/viewer?code=").append(report.getReportCode());
            if (params != null) {
                for (Map.Entry<String, Object> e : params.entrySet()) {
                    Object v = e.getValue();
                    if (v == null) continue;
                    // _label / .kolom turunan parameter LOV hanya untuk script dan engine query;
                    // tidak perlu (dan tidak boleh membengkakkan) URL viewer.
                    if (ReportParamResolver.isLovExtraKey(e.getKey(), report.getParams())) continue;
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
            return ReportRunResult.stimulsoft(url.toString(), messages);
        }

        Rendered r = render(report, params, format, sample, engine);
        if (!sample) {
            afterRun(report, params, r.data() != null ? r.data().size() : 0, messages);
        }
        return ReportRunResult.rendered(r.output(), isDataEmpty(r.data(), r.output()), messages);
    }

    /**
     * Kosong menurut siapa: bila aplikasi memegang datanya sendiri (STANDARD / Jasper dengan query
     * dari pengaturan report) -> daftar barisnya. Bila null (Jasper menjalankan query di dalam
     * .jrxml lewat JDBC) data itu memang tidak ada di sini, jadi bukan "kosong": ikuti pernyataan
     * renderer (tanpa halaman). Tidak diketahui = tidak dianggap kosong, supaya tidak ada
     * peringatan palsu.
     */
    static boolean isDataEmpty(List<Map<String, Object>> data, com.vaadinerp.report.render.ReportOutput output) {
        if (data != null) {
            return data.isEmpty();
        }
        return output != null && Boolean.TRUE.equals(output.noData());
    }

    /**
     * Render ke file TANPA before/after script -- dipakai renderReport() di Groovy (mis. untuk
     * lampiran email). Itu bukan "report dicetak", jadi tidak boleh ikut menaikkan printcount,
     * mengalokasikan NDT, dst. Stimulsoft ditolak: hasilnya URL viewer, bukan file.
     */
    public ReportOutput renderWithoutScripts(ReportMeta report, Map<String, Object> params, String format) {
        String engine = report.getEngineType() != null ? report.getEngineType() : "STANDARD";
        if ("STIMULSOFT".equalsIgnoreCase(engine)) {
            throw new IllegalArgumentException("Report " + report.getReportCode()
                    + " uses Stimulsoft, which has no file output to attach.");
        }
        return render(report, ReportParamResolver.withSystemParams(params, currentUserParams()), format, false, engine)
                .output();
    }

    /** Identitas pengguna dari sesi saat ini; kosong bila tidak ada sesi (mis. Scheduled Job) atau gagal dibaca. */
    private Map<String, Object> currentUserParams() {
        try {
            com.vaadinerp.security.entity.AppUser u = securityService != null ? securityService.getCurrentUser() : null;
            return u == null ? Map.of()
                    : ReportParamResolver.currentUserParams(u.getUsername(), u.getFullName(), u.getRoles());
        } catch (Exception e) {
            return Map.of();
        }
    }

    private record Rendered(ReportOutput output, List<Map<String, Object>> data) {
    }

    /**
     * Sumber data report Jasper: "Custom SQL Query" di pengaturan report kalau terisi; kalau kosong dan
     * template punya query sendiri, query template itu (dijalankan Jasper lewat JDBC); selain itu
     * perilaku lama (view/tabel form). Dengan begitu report boleh menyimpan query di salah satu tempat.
     */
    static boolean useTemplateQuery(ReportMeta report, String engine, File template) {
        return "JASPER".equalsIgnoreCase(engine)
                && (report.getDataQuery() == null || report.getDataQuery().isBlank())
                && JrxmlQuery.hasOwnQuery(template);
    }

    private Rendered render(ReportMeta report, Map<String, Object> params, String format, boolean sample,
            String engine) {
        // File template milik report ini, atau milik report sumber bila memakai template report lain.
        ReportMeta tpl = "STANDARD".equalsIgnoreCase(engine) ? report : resolver.templateOwner(report);
        File template = "STANDARD".equalsIgnoreCase(engine)
                ? null
                : resolver.resolveMasterTemplate(tpl.getReportCode(), engine, tpl.getTemplatePath());
        // null = Jasper menjalankan query di dalam .jrxml lewat JDBC (lihat useTemplateQuery).
        List<Map<String, Object>> data = useTemplateQuery(report, engine, template)
                ? null
                : dataService.fetchData(report, params, sample);
        Map<String, File> subreports = new java.util.HashMap<>();
        if ("JASPER".equalsIgnoreCase(engine)) {
            for (com.vaadinerp.report.SubreportConfig.Entry e
                    : com.vaadinerp.report.SubreportConfig.parse(tpl.getSubreportsJson())) {
                subreports.put(e.paramName(), resolver.resolveSubreportFile(tpl.getReportCode(), e.paramName()));
            }
        }
        ReportContext ctx = new ReportContext(report.getReportCode(), engine, template, data, params,
                report.getPageSize(), report.getOrientation(), report.getReportTitle(),
                report.getElements(), report.getGroupBy(), subreports);
        ReportRenderer renderer = registry.forEngine(engine);
        // Normalisasi di satu titik ini -- UI (ReportRunnerView) pakai "EXCEL", sebagian
        // renderer (JasperRenderer) cuma cek "XLSX", jadi "EXCEL" dulu jatuh ke default PDF.
        String normalizedFormat = "EXCEL".equalsIgnoreCase(format) ? "XLSX" : format;
        ReportOutput out = renderer.export(ctx, normalizedFormat != null ? normalizedFormat : "PDF");
        return new Rendered(out, data);
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
        beforeRun(report, params, null);
    }

    protected void beforeRun(ReportMeta report, Map<String, Object> params, List<ReportMessage> messages) {
        if (report.getBeforeScript() != null && !report.getBeforeScript().isBlank()) {
            String username = securityService != null && securityService.getCurrentUser() != null
                ? securityService.getCurrentUser().getUsername() : "system";
            scriptExecutor.executeReportScript(report.getBeforeScript(), params, username, log, messages);
        }
    }

    /**
     * Titik ekstensi setelah report dijalankan (mis. audit, hitung berapa kali di-print).
     * Synchronous dan sengaja TIDAK ditangkap di sini: kalau script ini gagal (mis. UPDATE
     * print_count gagal), exception-nya nyebrang ke run() dan report yang SUDAH dirender di
     * atas TIDAK PERNAH sampai ke pemanggil -- report dianggap gagal walau outputnya sempat jadi.
     * Sebelumnya ini fire-and-forget (CompletableFuture.runAsync) -- report selalu terkirim ke
     * user apa pun hasil script-nya, jadi tidak bisa dipakai buat hal yang wajib berhasil.
     */
    protected void afterRun(ReportMeta report, Map<String, Object> params, int rowCount) {
        afterRun(report, params, rowCount, null);
    }

    protected void afterRun(ReportMeta report, Map<String, Object> params, int rowCount, List<ReportMessage> messages) {
        if (report.getAfterScript() != null && !report.getAfterScript().isBlank()) {
            String username = securityService != null && securityService.getCurrentUser() != null
                ? securityService.getCurrentUser().getUsername() : "system";
            scriptExecutor.executeReportScript(report.getAfterScript(), params, username, log, messages);
        }
    }
}
