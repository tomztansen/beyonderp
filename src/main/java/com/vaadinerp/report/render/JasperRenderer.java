package com.vaadinerp.report.render;

import com.vaadinerp.report.JasperTemplateService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Renderer engine JASPER: fill .jasper/.jrxml lalu export PDF/XLSX. */
@Component
public class JasperRenderer implements ReportRenderer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JasperRenderer.class);

    private final JasperTemplateService templates;
    private final javax.sql.DataSource dataSource;
    private final int statementTimeoutSeconds;

    public JasperRenderer(JasperTemplateService templates, javax.sql.DataSource dataSource,
                          @org.springframework.beans.factory.annotation.Value("${app.report.jasper-timeout-seconds:120}")
                          int statementTimeoutSeconds) {
        this.templates = templates;
        this.dataSource = dataSource;
        this.statementTimeoutSeconds = statementTimeoutSeconds;
    }

    /**
     * Batas waktu per query pada koneksi Jasper (subreport dengan query sendiri memakai koneksi ini,
     * di luar timeout ReportDataService). Berlaku untuk sesi koneksi; harus di-RESET sebelum koneksi kembali
     * ke pool. {@code seconds <= 0} = tidak dibatasi.
     */
    static void applyStatementTimeout(java.sql.Connection conn, int seconds) throws java.sql.SQLException {
        if (seconds <= 0) return;
        try (java.sql.Statement st = conn.createStatement()) {
            st.execute("SET statement_timeout = " + (seconds * 1000L));
        }
    }

    /** @return true bila batas sudah bersih (atau memang tidak dipasang); false bila RESET gagal. */
    static boolean resetStatementTimeout(java.sql.Connection conn, int seconds) {
        if (seconds <= 0) return true;
        try (java.sql.Statement st = conn.createStatement()) {
            st.execute("RESET statement_timeout");
            return true;
        } catch (java.sql.SQLException e) {
            log.warn("Gagal mereset statement_timeout setelah render Jasper, koneksi dibuang dari pool: {}",
                    e.getMessage());
            return false;
        }
    }

    /**
     * Buang koneksi dari pool alih-alih mengembalikannya dengan batas waktu report yang masih menempel.
     * Hikari hanya membuang koneksi otomatis untuk SQLSTATE tertentu (08xxx, 57P0x, dll.), jadi kegagalan
     * RESET dengan kode lain akan lolos dan koneksi dipakai ulang oleh pemakai lain.
     */
    void discardConnection(java.sql.Connection conn) {
        try {
            if (dataSource.isWrapperFor(com.zaxxer.hikari.HikariDataSource.class)) {
                dataSource.unwrap(com.zaxxer.hikari.HikariDataSource.class).evictConnection(conn);
                return;
            }
            conn.abort(Runnable::run);
        } catch (java.sql.SQLException | RuntimeException e) {
            log.error("Koneksi dengan statement_timeout tertinggal gagal dibuang dari pool: {}", e.getMessage());
        }
    }

    /** SQLSTATE 57014 (query_canceled) di mana pun dalam rantai penyebab. */
    static boolean isStatementTimeout(Throwable t) {
        for (int i = 0; t != null && i < 10; i++, t = t.getCause()) {
            if (t instanceof java.sql.SQLException se && "57014".equals(se.getSQLState())) return true;
        }
        return false;
    }

    @Override
    public String engine() {
        return "JASPER";
    }

    /**
     * Parameter yang dideklarasikan bertipe List/Collection di template tetapi datang sebagai teks
     * (mis. id yang diketik user: "172" atau "172, 171") diubah menjadi daftar; token angka menjadi Long.
     * Dipakai oleh $X{IN, kolom, param}. Nilai yang sudah berupa daftar atau tidak dikirim tidak disentuh.
     */
    public static void coerceCollectionParams(net.sf.jasperreports.engine.JasperReport jr, Map<String, Object> params) {
        for (net.sf.jasperreports.engine.JRParameter p : jr.getParameters()) {
            if (p.isSystemDefined() || p.getValueClass() == null
                    || !java.util.Collection.class.isAssignableFrom(p.getValueClass())) {
                continue;
            }
            Object v = params.get(p.getName());
            if (!(v instanceof String s)) {
                continue;
            }
            List<Object> out = new java.util.ArrayList<>();
            for (String token : s.split(",")) {
                String t = token.trim();
                if (t.isEmpty()) {
                    continue;
                }
                out.add(t.matches("-?[0-9]{1,18}") ? (Object) Long.valueOf(t) : t);
            }
            params.put(p.getName(), out);
        }
    }

    private JasperPrint fill(ReportContext ctx) throws JRException {
        JasperReport jr = templates.loadCompiled(ctx.template());
        Map<String, Object> params = ctx.params() != null ? new HashMap<>(ctx.params()) : new HashMap<>();
        coerceCollectionParams(jr, params);

        // Opt-in: hanya template yang mendeklarasikan parameter STANDARD_LOCALE memakai locale angka Standard Format;
        // template lain tetap memakai locale server seperti sebelumnya.
        if (java.util.Arrays.stream(jr.getParameters()).anyMatch(p -> "STANDARD_LOCALE".equals(p.getName()))) {
            params.put(JRParameter.REPORT_LOCALE, com.vaadinerp.service.StandardFormatService.numberLocale());
        }

        if (ctx.subreports() != null) {
            for (Map.Entry<String, java.io.File> e : ctx.subreports().entrySet()) {
                JasperReport subreport = templates.loadCompiled(e.getValue());
                params.put(e.getKey(), subreport);
            }
        }

        // Koneksi selalu disediakan (bukan cuma saat report induk tidak punya custom query):
        // subreport bisa punya <query> sendiri di .jrxml-nya sendiri, dieksekusi lewat
        // koneksi ini (elemen Sub-Report di desain diarahkan ke $P{REPORT_CONNECTION}),
        // difilter pakai nilai kolom baris induk lewat subreportParameter di desainnya.
        try (java.sql.Connection conn = dataSource.getConnection()) {
            params.put(JRParameter.REPORT_CONNECTION, conn);
            applyStatementTimeout(conn, statementTimeoutSeconds);
            try {
                if (ctx.data() == null) {
                    // Data is null, meaning no external query was defined in the application.
                    // Let Jasper execute its own internal <query> via JDBC.
                    return JasperFillManager.fillReport(jr, params, conn);
                } else {
                    List<Map<String, Object>> data = ctx.data();
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    JRMapCollectionDataSource ds = new JRMapCollectionDataSource((java.util.Collection) data);
                    return JasperFillManager.fillReport(jr, params, ds);
                }
            } finally {
                if (!resetStatementTimeout(conn, statementTimeoutSeconds)) {
                    discardConnection(conn);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new JRException("Failed to obtain JDBC connection for Jasper report/subreport", e);
        }
    }

    /**
     * Report tanpa halaman = query internal tidak menghasilkan baris dan template tidak punya
     * bagian "No Data". Template yang sengaja mencetak satu halaman saat data kosong (No Data Section,
     * All Sections No Detail, Blank Page) tetap menghasilkan halaman, jadi tidak terdeteksi di sini.
     */
    public static boolean hasNoPages(JasperPrint print) {
        return print == null || print.getPages() == null || print.getPages().isEmpty();
    }

    @Override
    public ReportOutput render(ReportContext ctx) {
        return export(ctx, "PDF");
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        try {
            JasperPrint print = fill(ctx);
            if ("XLSX".equalsIgnoreCase(format)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));

                SimpleXlsxReportConfiguration xlsConfig = new SimpleXlsxReportConfiguration();
                xlsConfig.setIgnoreCellBorder(false);
                xlsConfig.setIgnoreGraphics(false);
                xlsConfig.setWhitePageBackground(false);
                xlsConfig.setDetectCellType(true);
                xlsConfig.setFontSizeFixEnabled(true);
                xlsConfig.setRemoveEmptySpaceBetweenRows(true);
                xlsConfig.setRemoveEmptySpaceBetweenColumns(true);
                xlsConfig.setIgnoreCellBackground(true);
                xlsConfig.setIgnorePageMargins(true);
                xlsConfig.setCollapseRowSpan(true);
                exporter.setConfiguration(xlsConfig);

                exporter.exportReport();
                return new ReportOutput(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray(),
                        hasNoPages(print));
            }
            // default PDF
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            return new ReportOutput("application/pdf", pdf, hasNoPages(print));
        } catch (JRException e) {
            if (isStatementTimeout(e)) {
                throw new RuntimeException("The report query took longer than " + statementTimeoutSeconds
                        + " seconds and was stopped. Narrow the filter and try again.", e);
            }
            throw new RuntimeException("Failed to render Jasper report: " + e.getMessage(), e);
        }
    }
}
