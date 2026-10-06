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

    private final JasperTemplateService templates;
    private final javax.sql.DataSource dataSource;

    public JasperRenderer(JasperTemplateService templates, javax.sql.DataSource dataSource) {
        this.templates = templates;
        this.dataSource = dataSource;
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
            throw new RuntimeException("Failed to render Jasper report: " + e.getMessage(), e);
        }
    }
}
