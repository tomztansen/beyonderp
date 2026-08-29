package com.vaadinerp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;

@Controller
@RequestMapping("/api/report/engine")
public class ReportEngineController {

    private final ReportMetaRepository reportMetaRepository;
    private final com.vaadinerp.service.DynamicDataService dynamicDataService;

    public ReportEngineController(ReportMetaRepository reportMetaRepository, com.vaadinerp.service.DynamicDataService dynamicDataService) {
        this.reportMetaRepository = reportMetaRepository;
        this.dynamicDataService = dynamicDataService;
    }

    @GetMapping("/view/{reportCode}")
    @ResponseBody
    public String viewReport(
            @PathVariable String reportCode, 
            @RequestParam(required = false) Long id, 
            @RequestParam(required = false) String param,
            Model model) {
        
        ReportMeta reportMeta = reportMetaRepository.findById(reportCode).orElse(null);
        if (reportMeta == null) {
            return "<h1>Report Not Found</h1>";
        }

        String engineType = reportMeta.getEngineType();
        if (engineType == null) engineType = "STANDARD";

        if ("STIMULSOFT".equalsIgnoreCase(engineType)) {
            // URL-encode user params to prevent injection in the iframe src attribute
            StringBuilder viewerUrl = new StringBuilder("/stimulsoft-java/viewer?code=")
                    .append(java.net.URLEncoder.encode(reportCode, java.nio.charset.StandardCharsets.UTF_8));
            if (id != null) viewerUrl.append("&id=").append(id);
            if (param != null) viewerUrl.append("&param=")
                    .append(java.net.URLEncoder.encode(param, java.nio.charset.StandardCharsets.UTF_8));

            // HTML-escape the URL before embedding in an attribute to prevent XSS
            String safeUrl = org.springframework.web.util.HtmlUtils.htmlEscape(viewerUrl.toString());
            return """
                <html>
                <head>
                    <title>Stimulsoft Viewer</title>
                    <style>
                        body { margin: 0; padding: 0; overflow: hidden; }
                        iframe { width: 100%%; height: 100vh; border: none; }
                    </style>
                </head>
                <body>
                    <iframe src="%s"></iframe>
                </body>
                </html>
            """.formatted(safeUrl);

        } else if ("JASPER".equalsIgnoreCase(engineType)) {
            return """
                <html>
                <head>
                    <title>JasperReports Viewer</title>
                    <style>
                        body { font-family: sans-serif; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; margin: 0; background-color: #f8fafc; }
                        .box { padding: 40px; background: white; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); border-radius: 8px; text-align: center; }
                        .badge { display: inline-block; padding: 5px 10px; background: #ef4444; color: white; border-radius: 4px; font-weight: bold; margin-bottom: 20px;}
                    </style>
                </head>
                <body>
                    <div class="box">
                        <div class="badge">JASPERREPORTS ENGINE</div>
                        <h2>JasperReports PDF Generation</h2>
                        <p>Report Code: <strong>%s</strong></p>
                        <p>Template File: <strong>%s</strong></p>
                        <p>Parameters Passed: id=%s, param=%s</p>
                        <hr style="margin: 20px 0; border: 0; border-top: 1px solid #e2e8f0;">
                        <p style="color: #64748b; font-size: 14px;">(PDF download will be triggered here once the JasperReports Java compiler logic is implemented)</p>
                    </div>
                </body>
                </html>
            """.formatted(
                org.springframework.web.util.HtmlUtils.htmlEscape(reportCode),
                org.springframework.web.util.HtmlUtils.htmlEscape(String.valueOf(reportMeta.getTemplatePath())),
                org.springframework.web.util.HtmlUtils.htmlEscape(String.valueOf(id)),
                org.springframework.web.util.HtmlUtils.htmlEscape(String.valueOf(param))
            );
        }

        return "<h1>Engine Type Not Supported by External Viewer</h1>";
    }

}
