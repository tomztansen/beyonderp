package com.vaadinerp.controller;

import com.stimulsoft.report.StiReport;
import com.stimulsoft.report.StiSerializeManager;
import com.stimulsoft.web.classes.StiRequestParams;
import com.stimulsoft.web.enums.StiWebViewMode;
import com.stimulsoft.web.proxyee.StiHttpServletRequest;
import com.stimulsoft.web.proxyee.StiHttpServletResponse;
import com.stimulsoft.web.proxyee.StiServletContext;
import com.stimulsoft.webdesigner.StiWebDesigerHandlerJk;
import com.stimulsoft.webdesigner.StiWebDesignerHelper;
import com.stimulsoft.webdesigner.StiWebDesignerOptions;
import com.stimulsoft.webviewer.StiWebViewerHelper;
import com.stimulsoft.webviewer.StiWebViewerOptions;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.net.URL;

@Controller
public class StimulsoftJavaController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Autowired
    private com.vaadinerp.service.DynamicDataService dynamicDataService;

    @Autowired
    private com.vaadinerp.meta.ReportMetaRepository reportMetaRepository;

    @Autowired
    private com.vaadinerp.report.ReportDataService reportDataService;

    @Autowired
    private ServletContext servletContext;

    @GetMapping("/stimulsoft-java/viewer")
    @ResponseBody
    public void viewerAction(
            @RequestParam String code,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        if (!isValidReportCode(code)) {
            writeError(response, "Invalid Report Code",
                    "Kode laporan mengandung karakter yang tidak diizinkan: " + code);
            return;
        }

        File base = new File(uploadDir).getCanonicalFile();
        File file = new File(base, code + ".mrt").getCanonicalFile();
        if (!file.toPath().startsWith(base.toPath())) {
            writeError(response, "Invalid Report Path", "Path tidak valid.");
            return;
        }
        if (!file.exists()) {
            writeError(response, "Report Template Missing",
                    "File template (" + code + ".mrt) tidak ditemukan di server.\n" +
                    "Pastikan Anda sudah menyimpannya melalui Report Builder.");
            return;
        }

        StiReport report = StiSerializeManager.deserializeReport(file);

        try {
            com.vaadinerp.meta.ReportMeta meta = reportMetaRepository.findById(code).orElse(null);
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            request.getParameterMap().forEach((k, v) -> {
                if ("code".equals(k) || v == null || v.length == 0) return;
                // Satu key bisa muncul beberapa kali (baris grid terpilih); ambil semuanya,
                // supaya ReportDataService bisa membangun IN (:param).
                params.put(k, v.length == 1 ? v[0] : java.util.List.of((Object[]) v));
            });
            java.util.List<java.util.Map<String, Object>> rawData;
            if (meta != null) {
                rawData = reportDataService.fetchData(meta, params, false);
            } else {
                rawData = dynamicDataService.fetchTableData(code);
            }
            if (rawData == null) rawData = new java.util.ArrayList<>();
            com.vaadinerp.report.render.StimulsoftRenderer.bindData(report, rawData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        report.render();

        StiWebViewerOptions options = new StiWebViewerOptions();
        String ctx = request.getContextPath();
        options.getServer().setController(ctx + "/stimulsoft_webviewer_action");
        options.getServer().setUseRelativeUrls(false);
        options.getToolbar().setZoom(-1);
        options.getToolbar().setViewMode(StiWebViewMode.Continuous);

        URL requestUrl = new URL(request.getRequestURL().toString());
        String rawHtml = new StiWebViewerHelper().getWebViewer(
                options, null, requestUrl,
                new StiHttpServletRequest(request),
                report,
                new StiServletContext(servletContext));

        // Wrap with responsive CSS so the viewer fills the IFrame container
        String html = "<!DOCTYPE html><html><head>"
                + "<meta charset='UTF-8'>"
                + "<style>"
                + "html,body{margin:0;padding:0;width:100%;height:100%;overflow:auto;}"
                + "</style></head><body>"
                + rawHtml
                + "<script>"
                + "window.addEventListener('load',function(){"
                + "document.querySelectorAll('div[id*=StiViewer],div[id*=sti],div.stv-viewer').forEach(function(el){"
                + "el.style.width='100%';el.style.height='100%';el.style.minHeight='100vh';"
                + "});"
                + "var frame=document.querySelector('iframe[id*=StiViewer],iframe[id*=sti]');"
                + "if(frame){frame.style.width='100%';frame.style.height='100vh';}"
                + "});"
                + "</script>"
                + "</body></html>";

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }

    @GetMapping("/stimulsoft-java/designer")
    @ResponseBody
    public void designerAction(
            @RequestParam String code,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        if (!isValidReportCode(code)) {
            writeError(response, "Invalid Report Code",
                    "Kode laporan mengandung karakter yang tidak diizinkan: " + code);
            return;
        }

        com.vaadinerp.meta.ReportMeta metaTest = reportMetaRepository.findById(code).orElse(null);

        java.util.List<java.util.Map<String, Object>> tempRawData;
        try {
            if (metaTest != null) {
                tempRawData = dynamicDataService.fetchReportData(metaTest, new java.util.HashMap<>(), true);
            } else {
                tempRawData = dynamicDataService.fetchTableData(code);
            }
        } catch (Exception e) {
            System.err.println("DESIGNER SAMPLE DATA ERROR (" + code + "): " + e.getMessage());
            tempRawData = new java.util.ArrayList<>();
        }
        if (tempRawData == null) tempRawData = new java.util.ArrayList<>();
        final java.util.List<java.util.Map<String, Object>> rawData = tempRawData;

        StiWebDesignerOptions options = new StiWebDesignerOptions();
        String ctx = request.getContextPath();
        options.getServer().setController(ctx + "/stimulsoft_webdesigner_action");
        options.getServer().setUseRelativeUrls(false);

        StiWebDesigerHandlerJk handler = new StiWebDesigerHandlerJk() {
            @Override
            public StiReport getEditedReport(HttpServletRequest requestHandler) {
                StiReport report = new StiReport();
                try {
                    File file = new File(uploadDir, code + ".mrt");
                    if (file.exists()) {
                        report = StiSerializeManager.deserializeReport(file);
                    } else {
                        com.stimulsoft.report.components.StiPage page = new com.stimulsoft.report.components.StiPage(report);
                        report.getPages().add(page);
                        page.setName(com.stimulsoft.report.StiNameCreation.createName(report,
                                com.stimulsoft.report.StiNameCreation.generateName(page)));
                    }
                    com.vaadinerp.report.render.StimulsoftRenderer.bindData(report, rawData);
                } catch (Exception e) {
                    System.err.println("GET EDITED REPORT ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
                return report;
            }

            @Override
            public void onSaveReportTemplate(StiReport report, StiRequestParams requestParams,
                    HttpServletRequest requestHandler) {
                try {
                    File file = new File(uploadDir, code + ".mrt");
                    file.getParentFile().mkdirs();
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        StiSerializeManager.serializeReport(report, fos);
                    }
                    System.out.println("Report template saved: " + file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onOpenReportTemplate(StiReport report, HttpServletRequest requestHandler) {}

            @Override
            public void onNewReportTemplate(StiReport report, HttpServletRequest requestHandler) {}
        };

        URL requestUrl = new URL(request.getRequestURL().toString());
        String html = new StiWebDesignerHelper().getWebDesigner(
                options, requestUrl,
                new StiHttpServletRequest(request),
                new StiHttpServletResponse(response),
                handler,
                new StiServletContext(servletContext));

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }

    private void writeError(HttpServletResponse response, String title, String message) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
            "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + escapeHtml(title) + "</title>" +
            "<style>body{margin:0;padding:0;font-family:'Segoe UI',sans-serif;background:#f8fafc;" +
            "display:flex;justify-content:center;align-items:center;height:100vh}" +
            ".box{background:#fff;border-radius:8px;box-shadow:0 4px 6px rgba(0,0,0,.1);" +
            "padding:32px;max-width:600px;width:90%;border-top:4px solid #ef4444}" +
            "h2{color:#ef4444;margin-top:0}.msg{color:#334155;background:#f1f5f9;padding:16px;" +
            "border-radius:4px;font-family:monospace;font-size:.875rem;white-space:pre-wrap;" +
            "border-left:3px solid #64748b;margin-top:16px}" +
            "button{margin-top:24px;background:#3b82f6;color:#fff;border:none;padding:10px 20px;" +
            "border-radius:4px;font-weight:600;cursor:pointer}</style></head>" +
            "<body><div class='box'><h2>" + escapeHtml(title) + "</h2>" +
            "<p>There was a problem loading the report:</p>" +
            "<div class='msg'>" + escapeHtml(message) + "</div>" +
            "<button onclick='window.close()'>Close Window</button></div></body></html>");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private boolean isValidReportCode(String code) {
        return code != null && code.matches("^[A-Za-z0-9_-]+$");
    }
}
