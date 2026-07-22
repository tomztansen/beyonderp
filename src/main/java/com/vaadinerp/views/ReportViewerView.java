package com.vaadinerp.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.service.DynamicDataService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("report-viewer")
public class ReportViewerView extends VerticalLayout {

    @SuppressWarnings("unused")
    private final ReportMetaRepository reportMetaRepository;
    private final DynamicDataService dynamicDataService;

    // Controls
    private final ComboBox<ReportMeta> selectReportCombo = new ComboBox<>("Select Report");
    private final TextField globalSearchField = new TextField("Search Data (Global)");
    private final Button btnPrint = new Button("Cetak Laporan", VaadinIcon.PRINT.create());

    // Printable Page Container
    private final Div printablePage = new Div();

    public ReportViewerView(ReportMetaRepository reportMetaRepository, DynamicDataService dynamicDataService) {
        this.reportMetaRepository = reportMetaRepository;
        this.dynamicDataService = dynamicDataService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Inject Print Media Styles
        injectPrintStyles();

        // 1. Controls bar
        HorizontalLayout controlsLayout = new HorizontalLayout();
        controlsLayout.setWidthFull();
        controlsLayout.setAlignItems(Alignment.END);
        controlsLayout.setSpacing(true);
        controlsLayout.addClassName("no-print"); // Will be hidden on print

        selectReportCombo.setPlaceholder("Select report...");
        selectReportCombo.setWidth("350px");
        selectReportCombo.setItems(reportMetaRepository.findAll());
        selectReportCombo.setItemLabelGenerator(r -> r.getReportTitle() + " (" + r.getReportCode() + ")");

        globalSearchField.setPlaceholder("Kata kunci...");
        globalSearchField.setWidth("250px");
        globalSearchField.addValueChangeListener(e -> refreshReportData());

        btnPrint.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnPrint.setEnabled(false);
        btnPrint.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs("window.print();");
        });

        controlsLayout.add(selectReportCombo, globalSearchField, btnPrint);

        // 2. Paper Area
        VerticalLayout paperContainer = new VerticalLayout();
        paperContainer.setSizeFull();
        paperContainer.getStyle().set("background-color", "#f1f5f9").set("padding", "20px").set("overflow", "auto");
        
        printablePage.addClassName("printable-page");
        printablePage.getStyle()
                .set("background-color", "#ffffff")
                .set("box-shadow", "0 10px 25px rgba(0,0,0,0.1)")
                .set("margin", "0 auto")
                .set("padding", "40px")
                .set("box-sizing", "border-box")
                .set("display", "none") // hidden until report is loaded
                .set("flex-direction", "column");

        paperContainer.add(printablePage);

        add(controlsLayout, paperContainer);
        setFlexGrow(1, paperContainer);

        // Select Report Listener
        selectReportCombo.addValueChangeListener(event -> {
            ReportMeta selectedReport = event.getValue();
            if (selectedReport != null) {
                btnPrint.setEnabled(true);
                printablePage.getStyle().set("display", "flex");
                updatePageSizeAndOrientation(selectedReport);
                refreshReportData();
            } else {
                btnPrint.setEnabled(false);
                printablePage.getStyle().set("display", "none");
            }
        });
    }

    private void injectPrintStyles() {
        String styles = "<style>" +
                "@media print {\n" +
                "  .sidebar, .menu-item-button, .no-print, vaadin-combo-box, vaadin-button, vaadin-text-field {\n" +
                "    display: none !important;\n" +
                "  }\n" +
                "  body, html {\n" +
                "    background-color: #ffffff !important;\n" +
                "    margin: 0 !important;\n" +
                "    padding: 0 !important;\n" +
                "  }\n" +
                "  .printable-page {\n" +
                "    box-shadow: none !important;\n" +
                "    border: none !important;\n" +
                "    margin: 0 !important;\n" +
                "    padding: 0 !important;\n" +
                "    width: 100% !important;\n" +
                "    min-height: auto !important;\n" +
                "    display: flex !important;\n" +
                "  }\n" +
                "  vaadin-vertical-layout, vaadin-horizontal-layout {\n" +
                "    padding: 0 !important;\n" +
                "    margin: 0 !important;\n" +
                "  }\n" +
                "  table {\n" +
                "    page-break-inside: auto;\n" +
                "  }\n" +
                "  tr {\n" +
                "    page-break-inside: avoid;\n" +
                "    page-break-after: auto;\n" +
                "  }\n" +
                "}" +
                "</style>";
        Html styleBlock = new Html(styles);
        add(styleBlock);
    }

    private void updatePageSizeAndOrientation(ReportMeta report) {
        String orientation = report.getOrientation();
        if ("PORTRAIT".equalsIgnoreCase(orientation)) {
            printablePage.getStyle().set("width", "720px").set("min-height", "990px");
        } else {
            printablePage.getStyle().set("width", "980px").set("min-height", "670px");
        }
    }

    private void refreshReportData() {
        ReportMeta report = selectReportCombo.getValue();
        if (report == null) return;

        printablePage.removeAll();

        // Group elements by bandType
        Map<String, List<ReportElementMeta>> elementsByBand = report.getElements().stream()
                .collect(Collectors.groupingBy(ReportElementMeta::getBandType));

        // Fetch Data from DB
        List<Map<String, Object>> allRecords = dynamicDataService.fetchTableData(report.getTableName());
        List<Map<String, Object>> filteredData = filterData(allRecords, globalSearchField.getValue());

        // 1. RENDER TITLE BAND
        List<ReportElementMeta> titleEls = getSortedBandElements(elementsByBand, "TITLE");
        if (!titleEls.isEmpty()) {
            HorizontalLayout titleBand = new HorizontalLayout();
            titleBand.setWidthFull();
            titleBand.getStyle().set("margin-bottom", "20px").set("gap", "10px");
            for (ReportElementMeta el : titleEls) {
                Span span = createStyledSpan(el, evaluateElement(el, null, filteredData));
                titleBand.add(span);
            }
            printablePage.add(titleBand);
        }

        // 2. RENDER PAGE HEADER BAND
        List<ReportElementMeta> pHeadEls = getSortedBandElements(elementsByBand, "PAGE_HEADER");
        if (!pHeadEls.isEmpty()) {
            HorizontalLayout headerBand = new HorizontalLayout();
            headerBand.setWidthFull();
            headerBand.getStyle().set("margin-bottom", "20px").set("border-bottom", "1px solid #cbd5e1").set("padding-bottom", "5px").set("gap", "10px");
            for (ReportElementMeta el : pHeadEls) {
                Span span = createStyledSpan(el, evaluateElement(el, null, filteredData));
                headerBand.add(span);
            }
            printablePage.add(headerBand);
        }

        // 3. RENDER COLUMN HEADER & DETAIL BANDS (As a Table)
        List<ReportElementMeta> colHeadEls = getSortedBandElements(elementsByBand, "COLUMN_HEADER");
        List<ReportElementMeta> detailEls = getSortedBandElements(elementsByBand, "DETAIL");

        if (!colHeadEls.isEmpty() || !detailEls.isEmpty()) {
            Table table = new Table();
            table.getStyle().set("width", "100%").set("border-collapse", "collapse").set("margin-bottom", "20px");

            // Header row
            if (!colHeadEls.isEmpty()) {
                Header tableHeader = new Header();
                Row headerRow = new Row();
                headerRow.getStyle().set("border-bottom", "2px solid #334155");
                for (ReportElementMeta el : colHeadEls) {
                    Cell th = new Cell(true);
                    th.setText(evaluateElement(el, null, filteredData));
                    applyCellStyle(th, el);
                    th.getStyle().set("background-color", "#f8fafc").set("border-bottom", "2px solid #cbd5e1");
                    headerRow.add(th);
                }
                tableHeader.add(headerRow);
                table.add(tableHeader);
            }

            // Detail rows
            if (!detailEls.isEmpty()) {
                for (Map<String, Object> record : filteredData) {
                    Row detailRow = new Row();
                    detailRow.getStyle().set("border-bottom", "1px solid #e2e8f0");
                    for (ReportElementMeta el : detailEls) {
                        Cell td = new Cell(false);
                        td.setText(evaluateElement(el, record, filteredData));
                        applyCellStyle(td, el);
                        detailRow.add(td);
                    }
                    table.add(detailRow);
                }
            }
            printablePage.add(table);
        }

        // 4. RENDER SUMMARY BAND
        List<ReportElementMeta> summaryEls = getSortedBandElements(elementsByBand, "SUMMARY");
        if (!summaryEls.isEmpty()) {
            HorizontalLayout summaryBand = new HorizontalLayout();
            summaryBand.setWidthFull();
            summaryBand.getStyle().set("margin-top", "20px").set("border-top", "2px solid #334155").set("padding-top", "10px").set("gap", "10px");
            for (ReportElementMeta el : summaryEls) {
                Span span = createStyledSpan(el, evaluateElement(el, null, filteredData));
                summaryBand.add(span);
            }
            printablePage.add(summaryBand);
        }

        // 5. RENDER PAGE FOOTER BAND
        List<ReportElementMeta> pFootEls = getSortedBandElements(elementsByBand, "PAGE_FOOTER");
        if (!pFootEls.isEmpty()) {
            HorizontalLayout footerBand = new HorizontalLayout();
            footerBand.setWidthFull();
            footerBand.getStyle().set("margin-top", "auto").set("border-top", "1px solid #cbd5e1").set("padding-top", "5px").set("gap", "10px");
            for (ReportElementMeta el : pFootEls) {
                Span span = createStyledSpan(el, evaluateElement(el, null, filteredData));
                footerBand.add(span);
            }
            printablePage.add(footerBand);
        }
    }

    private List<ReportElementMeta> getSortedBandElements(Map<String, List<ReportElementMeta>> elementsByBand, String bandType) {
        List<ReportElementMeta> list = elementsByBand.get(bandType);
        if (list == null) return new ArrayList<>();
        return list.stream()
                .sorted((e1, e2) -> Integer.compare(e1.getColOrder() != null ? e1.getColOrder() : 0, e2.getColOrder() != null ? e2.getColOrder() : 0))
                .collect(Collectors.toList());
    }

    private Span createStyledSpan(ReportElementMeta el, String text) {
        Span span = new Span(text);
        span.getStyle().set("width", el.getColumnWidth() != null ? el.getColumnWidth() : "120px");
        span.getStyle().set("font-size", "0.85rem");
        
        if ("RIGHT".equalsIgnoreCase(el.getAlignment())) {
            span.getStyle().set("text-align", "right");
        } else if ("CENTER".equalsIgnoreCase(el.getAlignment())) {
            span.getStyle().set("text-align", "center");
        } else {
            span.getStyle().set("text-align", "left");
        }

        if ("BOLD".equalsIgnoreCase(el.getFontWeight())) {
            span.getStyle().set("font-weight", "700");
        }
        return span;
    }

    private void applyCellStyle(Cell cell, ReportElementMeta el) {
        cell.getStyle().set("padding", "8px").set("font-size", "0.8rem");
        if (el.getColumnWidth() != null && !el.getColumnWidth().isEmpty()) {
            cell.getStyle().set("width", el.getColumnWidth());
        }
        if ("RIGHT".equalsIgnoreCase(el.getAlignment())) {
            cell.getStyle().set("text-align", "right");
        } else if ("CENTER".equalsIgnoreCase(el.getAlignment())) {
            cell.getStyle().set("text-align", "center");
        } else {
            cell.getStyle().set("text-align", "left");
        }
        if ("BOLD".equalsIgnoreCase(el.getFontWeight())) {
            cell.getStyle().set("font-weight", "700");
        }
    }

    private String formatValue(Object val, String pattern) {
        if (val == null) return "";
        if (pattern == null || pattern.trim().isEmpty()) {
            return val.toString();
        }
        
        // Try date formatting
        if (val instanceof java.time.temporal.TemporalAccessor || val instanceof java.util.Date) {
            try {
                if (val instanceof java.time.LocalDate) {
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern(pattern);
                    return ((java.time.LocalDate) val).format(dtf);
                } else if (val instanceof java.time.LocalDateTime) {
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern(pattern);
                    return ((java.time.LocalDateTime) val).format(dtf);
                } else {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern);
                    return sdf.format((java.util.Date) val);
                }
            } catch (Exception e) {
                // fallback
            }
        }
        
        // If string but looks like yyyy-MM-dd
        if (val instanceof String) {
            String sVal = (String) val;
            if (sVal.matches("\\d{4}-\\d{2}-\\d{2}")) {
                try {
                    java.time.LocalDate ldate = java.time.LocalDate.parse(sVal);
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern(pattern);
                    return ldate.format(dtf);
                } catch (Exception ignored) {}
            }
            if (sVal.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(sVal.substring(0, 19));
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern(pattern);
                    return ldt.format(dtf);
                } catch (Exception ignored) {}
            }
        }

        // Try numeric formatting
        BigDecimal numVal = null;
        if (val instanceof Number) {
            numVal = new BigDecimal(val.toString());
        } else {
            try {
                numVal = new BigDecimal(val.toString());
            } catch (Exception ignored) {}
        }
        
        if (numVal != null) {
            try {
                boolean hasRp = pattern.startsWith("Rp ") || pattern.startsWith("Rp");
                String cleanPattern = pattern;
                if (hasRp) {
                    cleanPattern = pattern.replace("Rp ", "").replace("Rp", "").trim();
                }
                
                java.util.Locale locale = java.util.Locale.of("id", "ID");
                java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(locale);
                java.text.DecimalFormat df = new java.text.DecimalFormat(cleanPattern, symbols);
                String formattedNumber = df.format(numVal);
                
                if (hasRp) {
                    return "Rp " + formattedNumber;
                }
                return formattedNumber;
            } catch (Exception e) {
                // fallback
            }
        }

        return val.toString();
    }

    private String evaluateElement(ReportElementMeta el, Map<String, Object> record, List<Map<String, Object>> allRecords) {
        if ("LABEL".equalsIgnoreCase(el.getElementType())) {
            return el.getElementValue();
        }
        if ("FIELD".equalsIgnoreCase(el.getElementType())) {
            if (record == null) return "";
            Object val = record.get(el.getElementValue().toLowerCase());
            return val != null ? formatValue(val, el.getFormatPattern()) : "";
        }
        if ("SYSTEM".equalsIgnoreCase(el.getElementType())) {
            String val = el.getElementValue();
            if ("CURRENT_DATE".equalsIgnoreCase(val)) {
                return formatValue(java.time.LocalDate.now(), el.getFormatPattern());
            }
            if ("PAGE_NUMBER".equalsIgnoreCase(val) || "TOTAL_PAGES".equalsIgnoreCase(val)) {
                return "1";
            }
            if (val.startsWith("SUM(") && val.endsWith(")")) {
                String col = val.substring(4, val.length() - 1).toLowerCase();
                BigDecimal sum = BigDecimal.ZERO;
                for (Map<String, Object> r : allRecords) {
                    Object v = r.get(col);
                    if (v != null) {
                        try {
                            sum = sum.add(new BigDecimal(v.toString()));
                        } catch (Exception ignored) {}
                    }
                }
                return formatValue(sum, el.getFormatPattern());
            }
            if (val.startsWith("AVG(") && val.endsWith(")")) {
                String col = val.substring(4, val.length() - 1).toLowerCase();
                BigDecimal sum = BigDecimal.ZERO;
                int count = 0;
                for (Map<String, Object> r : allRecords) {
                    Object v = r.get(col);
                    if (v != null) {
                        try {
                            sum = sum.add(new BigDecimal(v.toString()));
                            count++;
                        } catch (Exception ignored) {}
                    }
                }
                if (count == 0) return formatValue(BigDecimal.ZERO, el.getFormatPattern());
                BigDecimal avg = sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                return formatValue(avg, el.getFormatPattern());
            }
            if (val.startsWith("COUNT(") && val.endsWith(")")) {
                String col = val.substring(6, val.length() - 1).toLowerCase();
                long count = allRecords.stream().filter(r -> r.get(col) != null).count();
                return formatValue(count, el.getFormatPattern());
            }
        }
        return el.getElementValue();
    }

    private List<Map<String, Object>> filterData(List<Map<String, Object>> rawData, String keyword) {
        if (rawData == null) return new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return rawData;

        String kw = keyword.toLowerCase().trim();
        return rawData.stream()
                .filter(row -> row.values().stream().anyMatch(val -> val != null && val.toString().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
    }

    // Helper classes for HTML table building
    private static class Table extends HtmlContainer {
        public Table() {
            super("table");
        }
    }

    private static class Header extends HtmlContainer {
        public Header() {
            super("thead");
        }
    }

    private static class Row extends HtmlContainer {
        public Row() {
            super("tr");
        }
    }

    private static class Cell extends HtmlContainer {
        public Cell(boolean isHeader) {
            super(isHeader ? "th" : "td");
        }
        public void setText(String text) {
            getElement().setText(text);
        }
    }
}
