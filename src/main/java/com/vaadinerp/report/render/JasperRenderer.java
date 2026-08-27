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

    public JasperRenderer(JasperTemplateService templates) {
        this.templates = templates;
    }

    @Override
    public String engine() {
        return "JASPER";
    }

    private JasperPrint fill(ReportContext ctx) throws JRException {
        JasperReport jr = templates.loadCompiled(ctx.template());
        Map<String, Object> params = ctx.params() != null ? new HashMap<>(ctx.params()) : new HashMap<>();
        List<Map<String, Object>> data = ctx.data() != null ? ctx.data() : new java.util.ArrayList<>();
        @SuppressWarnings({"unchecked", "rawtypes"})
        JRMapCollectionDataSource ds = new JRMapCollectionDataSource((java.util.Collection) data);
        return JasperFillManager.fillReport(jr, params, ds);
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
            throw new RuntimeException("Gagal render Jasper: " + e.getMessage(), e);
        }
    }
}
