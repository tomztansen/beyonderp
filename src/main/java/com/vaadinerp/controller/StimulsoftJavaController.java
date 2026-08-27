package com.vaadinerp.controller;

import com.stimulsoft.report.StiReport;
import com.stimulsoft.report.StiSerializeManager;
import com.stimulsoft.web.classes.StiRequestParams;
import com.stimulsoft.web.enums.StiWebViewMode;
import com.stimulsoft.webdesigner.StiWebDesigerHandlerJk;
import com.stimulsoft.webdesigner.StiWebDesignerOptions;
import com.stimulsoft.webviewer.StiWebViewerOptions;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.File;

@Controller
public class StimulsoftJavaController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Autowired
    private com.vaadinerp.service.DynamicDataService dynamicDataService;

    @Autowired
    private com.vaadinerp.meta.ReportMetaRepository reportMetaRepository;

    @GetMapping("/stimulsoft-java/viewer")
    public String viewerAction(
            @RequestParam String code,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String param,
            jakarta.servlet.http.HttpServletRequest request,
            Model model) throws Exception {

        if (!isValidReportCode(code)) {
            model.addAttribute("errorTitle", "Invalid Report Code");
            model.addAttribute("errorMessage", "Kode laporan mengandung karakter yang tidak diizinkan: " + code);
            return "stimulsoft-error";
        }

        File file = new File(uploadDir, code + ".mrt");
        if (!file.exists()) {
            model.addAttribute("errorTitle", "Report Template Missing");
            model.addAttribute("errorMessage", "File template (" + code + ".mrt) tidak ditemukan di server.\nPastikan Anda sudah menyimpannya melalui Report Builder.");
            return "stimulsoft-error";
        }
        StiReport report = StiSerializeManager.deserializeReport(file);

        try {
            com.vaadinerp.meta.ReportMeta meta = reportMetaRepository.findById(code).orElse(null);

            java.util.Map<String, Object> params = new java.util.HashMap<>();
            if (id != null) params.put("id", id);
            if (param != null) params.put("param", param);

            java.util.List<java.util.Map<String, Object>> rawData;
            if (meta != null) {
                // Menggunakan fetchReportData agar parameter (:id, :param) tersubstitusi ke query laporan
                rawData = dynamicDataService.fetchReportData(meta, params, false);
            } else {
                rawData = dynamicDataService.fetchTableData(code);
            }
            if (rawData == null) rawData = new java.util.ArrayList<>();

            java.util.Map<String, Object> dataRoot = new java.util.HashMap<>();
            dataRoot.put("DynamicData", rawData);
            String jsonData = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dataRoot);

            report.getDictionary().getDatabases().clear();
            com.stimulsoft.report.dictionary.databases.StiJsonDatabase jsonDb = new com.stimulsoft.report.dictionary.databases.StiJsonDatabase("DynamicData", "");
            jsonDb.setJsonData(jsonData);
            report.getDictionary().getDatabases().add(jsonDb);
            report.getDictionary().synchronize();
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

        model.addAttribute("report", report);
        model.addAttribute("options", options);
        return "viewer";
    }

    @GetMapping("/stimulsoft-java/designer")
    public String designerAction(@RequestParam String code, Model model, jakarta.servlet.http.HttpServletRequest request) throws Exception {
        if (!isValidReportCode(code)) {
            model.addAttribute("errorTitle", "Invalid Report Code");
            model.addAttribute("errorMessage", "Kode laporan mengandung karakter yang tidak diizinkan: " + code);
            return "stimulsoft-error";
        }

        com.vaadinerp.meta.ReportMeta metaTest = reportMetaRepository.findById(code).orElse(null);

        // Data contoh terbatas (LIMIT 50) untuk preview di designer, bukan seluruh tabel
        java.util.List<java.util.Map<String, Object>> tempRawData;
        if (metaTest != null) {
            tempRawData = dynamicDataService.fetchReportData(metaTest, new java.util.HashMap<>(), true);
        } else {
            tempRawData = dynamicDataService.fetchTableData(code);
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
                        page.setName(com.stimulsoft.report.StiNameCreation.createName(report, com.stimulsoft.report.StiNameCreation.generateName(page)));
                    }
                    
                    java.util.Map<String, Object> dataRoot = new java.util.HashMap<>();
                    dataRoot.put("DynamicData", rawData);
                    String jsonData = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dataRoot);
                    
                    report.getDictionary().getDatabases().clear();
                    com.stimulsoft.report.dictionary.databases.StiJsonDatabase jsonDb = new com.stimulsoft.report.dictionary.databases.StiJsonDatabase("DynamicData", "");
                    jsonDb.setJsonData(jsonData);
                    report.getDictionary().getDatabases().add(jsonDb);
                    report.getDictionary().synchronize();
                } catch (Exception e) {
                    System.err.println("GET EDITED REPORT ERROR: " + e.getMessage());
                    e.printStackTrace();
                    com.stimulsoft.report.components.simplecomponents.StiText errText = new com.stimulsoft.report.components.simplecomponents.StiText();
                    errText.setText("JAVA EXCEPTION: " + e.getMessage() + "\n\nPlease check server console for details.");
                    errText.setWidth(10);
                    errText.setHeight(5);
                    report.getPages().get(0).getComponents().add(errText);
                }
                return report;
            }

            @Override
            public void onSaveReportTemplate(StiReport report, StiRequestParams requestParams, HttpServletRequest requestHandler) {
                try {
                    File file = new File(uploadDir, code + ".mrt");
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
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

        model.addAttribute("handler", handler);
        model.addAttribute("options", options);
        return "designer";
    }

    /**
     * Mencegah path traversal: kode laporan dipakai langsung sebagai nama file (.mrt),
     * jadi hanya boleh huruf, angka, underscore, dan strip.
     */
    private boolean isValidReportCode(String code) {
        return code != null && code.matches("^[A-Za-z0-9_-]+$");
    }
}
