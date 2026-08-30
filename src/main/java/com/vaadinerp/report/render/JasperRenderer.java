package com.vaadinerp.report.render;

import com.vaadinerp.report.JasperTemplateService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
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

    private JasperPrint fill(ReportContext ctx) throws JRException {
        JasperReport jr = templates.loadCompiled(ctx.template());
        Map<String, Object> params = ctx.params() != null ? new HashMap<>(ctx.params()) : new HashMap<>();
        
        if (ctx.data() == null) {
            // Data is null, meaning no external query was defined in the application.
            // Let Jasper execute its own internal <query> via JDBC.
            try (java.sql.Connection conn = dataSource.getConnection()) {
                return JasperFillManager.fillReport(jr, params, conn);
            } catch (java.sql.SQLException e) {
                throw new JRException("Failed to obtain JDBC connection for internal Jasper query", e);
            }
        } else {
            List<Map<String, Object>> data = ctx.data();
            @SuppressWarnings({"unchecked", "rawtypes"})
            JRMapCollectionDataSource ds = new JRMapCollectionDataSource((java.util.Collection) data);
            return JasperFillManager.fillReport(jr, params, ds);
        }
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
                exporter.exportReport();
                return new ReportOutput(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
            }
            // default PDF
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            return ReportOutput.pdf(pdf);
        } catch (JRException e) {
            throw new RuntimeException("Failed to render Jasper report: " + e.getMessage(), e);
        }
    }
}
