package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;
import org.springframework.stereotype.Component;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Renderer engine STANDARD: render data ke HTML "kertas". */
@Component
public class StandardRenderer implements ReportRenderer {

    /** Bands stacked outside the data table, in print order. */
    private static final List<String> LEADING_BANDS = List.of("TITLE", "PAGE_HEADER");
    private static final List<String> TRAILING_BANDS = List.of("SUMMARY", "PAGE_FOOTER");

    @Override
    public String engine() {
        return "STANDARD";
    }

    /** Overload lama: tanpa grouping. */
    public static String renderHtml(List<Map<String, Object>> data, ReportMeta report,
                                    List<ReportElementMeta> elements) {
        return renderHtml(data, report, elements, null);
    }

    /**
     * Render band-based bila report punya {@code elements}: TITLE / PAGE_HEADER di atas,
     * COLUMN_HEADER + DETAIL sebagai tabel (DETAIL diulang per baris data), lalu
     * SUMMARY / PAGE_FOOTER. Tanpa elements, jatuh ke dump semua kolom seperti semula
     * supaya report lama tetap tampil.
     *
     * <p>Bila {@code groupBy} terisi, data dipecah per nilai kolom itu (urutan kemunculan
     * dipertahankan) dan tiap kelompok dirender sebagai GROUP_HEADER → tabel → GROUP_FOOTER,
     * dipisah page break. Agregat di band group dihitung atas baris kelompoknya saja.
     */
    public static String renderHtml(List<Map<String, Object>> data, ReportMeta report,
                                    List<ReportElementMeta> elements, String groupBy) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"report-standard\">");
        if (report != null && report.getReportTitle() != null) {
            sb.append("<h2>").append(esc(report.getReportTitle())).append("</h2>");
        }
        if (elements == null || elements.isEmpty()) {
            appendAllColumns(sb, data);
        } else {
            appendBands(sb, data, elements, groupBy);
        }
        sb.append("</div>");
        return sb.toString();
    }

    /** Perilaku lama: tabel berisi seluruh kolom apa adanya. */
    private static void appendAllColumns(StringBuilder sb, List<Map<String, Object>> data) {
        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\">");
        if (data != null && !data.isEmpty()) {
            sb.append("<thead><tr>");
            for (String col : data.get(0).keySet()) sb.append("<th>").append(esc(col)).append("</th>");
            sb.append("</tr></thead><tbody>");
            for (Map<String, Object> row : data) {
                sb.append("<tr>");
                for (Object v : row.values()) sb.append("<td>").append(esc(String.valueOf(v))).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody>");
        }
        sb.append("</table>");
    }

    private static void appendBands(StringBuilder sb, List<Map<String, Object>> data,
                                    List<ReportElementMeta> elements, String groupBy) {
        List<Map<String, Object>> rows = (data != null) ? data : List.of();
        Map<String, List<ReportElementMeta>> bands = groupByBand(elements);

        for (String band : LEADING_BANDS) appendFreeBand(sb, bands.get(band), rows);

        if (groupBy == null || groupBy.isBlank()) {
            appendDataTable(sb, rows, bands, rows);
        } else {
            List<List<Map<String, Object>>> groups = partitionByGroup(rows, groupBy.trim());
            boolean first = true;
            for (List<Map<String, Object>> group : groups) {
                sb.append("<div");
                if (!first) sb.append(" style=\"page-break-before:always\"");
                sb.append('>');
                appendFreeBand(sb, bands.get("GROUP_HEADER"), group);
                appendDataTable(sb, group, bands, group);
                appendFreeBand(sb, bands.get("GROUP_FOOTER"), group);
                sb.append("</div>");
                first = false;
            }
        }

        for (String band : TRAILING_BANDS) appendFreeBand(sb, bands.get(band), rows);
    }

    /** COLUMN_HEADER + DETAIL untuk sekumpulan baris. {@code aggRows} jadi cakupan fungsi agregat. */
    private static void appendDataTable(StringBuilder sb, List<Map<String, Object>> rows,
                                        Map<String, List<ReportElementMeta>> bands,
                                        List<Map<String, Object>> aggRows) {
        List<ReportElementMeta> header = bands.get("COLUMN_HEADER");
        List<ReportElementMeta> detail = bands.get("DETAIL");
        if (header == null && detail == null) return;

        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\" style=\"width:100%\">");
        if (header != null) {
            sb.append("<thead><tr>");
            for (ReportElementMeta el : header) appendCell(sb, "th", el, null, aggRows);
            sb.append("</tr></thead>");
        }
        if (detail != null) {
            sb.append("<tbody>");
            for (Map<String, Object> row : rows) {
                sb.append("<tr>");
                for (ReportElementMeta el : detail) appendCell(sb, "td", el, row, aggRows);
                sb.append("</tr>");
            }
            sb.append("</tbody>");
        }
        sb.append("</table>");
    }

    /**
     * Pecah baris per nilai {@code groupBy}, mempertahankan urutan kemunculan pertama.
     * Baris tanpa kolom itu masuk ke satu kelompok bernilai null, sehingga kolom group
     * yang salah ketik menghasilkan satu kelompok — bukan satu kelompok per baris.
     */
    private static List<List<Map<String, Object>>> partitionByGroup(List<Map<String, Object>> rows,
                                                                    String groupBy) {
        Map<Object, List<Map<String, Object>>> byKey = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object key = (row == null) ? null : row.get(groupBy);
            byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        return new ArrayList<>(byKey.values());
    }

    /** Band di luar tabel: satu baris flex, tiap elemen jadi satu kolom. */
    private static void appendFreeBand(StringBuilder sb, List<ReportElementMeta> band,
                                       List<Map<String, Object>> rows) {
        if (band == null || band.isEmpty()) return;
        sb.append("<div style=\"display:flex;width:100%\">");
        for (ReportElementMeta el : band) {
            sb.append("<div style=\"").append(cellStyle(el)).append("\">")
              .append(esc(valueOf(el, rows.isEmpty() ? null : rows.get(0), rows)))
              .append("</div>");
        }
        sb.append("</div>");
    }

    private static void appendCell(StringBuilder sb, String tag, ReportElementMeta el,
                                   Map<String, Object> row, List<Map<String, Object>> rows) {
        sb.append("<").append(tag).append(" style=\"").append(cellStyle(el)).append("\">")
          .append(esc(valueOf(el, row, rows)))
          .append("</").append(tag).append(">");
    }

    private static Map<String, List<ReportElementMeta>> groupByBand(List<ReportElementMeta> elements) {
        Map<String, List<ReportElementMeta>> bands = new LinkedHashMap<>();
        List<ReportElementMeta> sorted = new ArrayList<>(elements);
        sorted.sort(Comparator.comparing(e -> e.getColOrder() != null ? e.getColOrder() : 0));
        for (ReportElementMeta el : sorted) {
            if (el.getBandType() == null) continue;
            bands.computeIfAbsent(el.getBandType().toUpperCase(), k -> new ArrayList<>()).add(el);
        }
        return bands;
    }

    private static String cellStyle(ReportElementMeta el) {
        StringBuilder s = new StringBuilder();
        if (el.getColumnWidth() != null && !el.getColumnWidth().isBlank()) {
            s.append("width:").append(esc(el.getColumnWidth().trim())).append(';');
        }
        String align = el.getAlignment();
        if (align != null && !align.isBlank()) {
            s.append("text-align:").append(esc(align.trim().toLowerCase())).append(';');
        }
        if ("BOLD".equalsIgnoreCase(el.getFontWeight())) s.append("font-weight:bold;");
        return s.toString();
    }

    /** Nilai satu elemen: teks statis, kolom baris berjalan, atau fungsi sistem. */
    private static String valueOf(ReportElementMeta el, Map<String, Object> row,
                                  List<Map<String, Object>> rows) {
        String type = el.getElementType() != null ? el.getElementType().toUpperCase() : "LABEL";
        String value = el.getElementValue();
        switch (type) {
            case "FIELD":
                if (row == null || value == null) return "";
                return fmt(row.get(value), el.getFormatPattern());
            case "SYSTEM":
                return systemValue(value, el.getFormatPattern(), rows);
            default:
                return value != null ? value : "";
        }
    }

    private static String systemValue(String fn, String pattern, List<Map<String, Object>> rows) {
        if (fn == null) return "";
        String f = fn.trim();
        String upper = f.toUpperCase();
        if (upper.equals("CURRENT_DATE")) {
            return fmt(java.time.LocalDate.now(), pattern);
        }
        // HTML output is a single page.
        if (upper.equals("PAGE_NUMBER") || upper.equals("TOTAL_PAGES")) return "1";

        int open = f.indexOf('('), close = f.lastIndexOf(')');
        if (open < 0 || close < open) return f;
        String agg = upper.substring(0, open).trim();
        String col = f.substring(open + 1, close).trim();

        if (agg.equals("COUNT")) return String.valueOf(rows.size());
        if (col.isEmpty()) return "";

        double sum = 0;
        int n = 0;
        for (Map<String, Object> row : rows) {
            Object v = row.get(col);
            if (v instanceof Number num) {
                sum += num.doubleValue();
                n++;
            } else if (v != null) {
                try {
                    sum += Double.parseDouble(v.toString().trim());
                    n++;
                } catch (NumberFormatException ignored) {
                    // non-numeric cell: skip it rather than fail the whole report
                }
            }
        }
        return switch (agg) {
            case "SUM" -> fmt(sum, pattern);
            case "AVG" -> fmt(n == 0 ? 0d : sum / n, pattern);
            default -> f;
        };
    }

    /** Terapkan formatPattern bila ada; kalau polanya tidak cocok, pakai apa adanya. */
    private static String fmt(Object value, String pattern) {
        if (value == null) return "";
        if (pattern == null || pattern.isBlank()) return String.valueOf(value);
        try {
            if (value instanceof Number num) return new DecimalFormat(pattern).format(num);
            if (value instanceof TemporalAccessor t) return DateTimeFormatter.ofPattern(pattern).format(t);
            if (value instanceof java.util.Date d) {
                return new java.text.SimpleDateFormat(pattern).format(d);
            }
        } catch (Exception ignored) {
            // bad pattern for this value type — fall through to the raw value
        }
        return String.valueOf(value);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public ReportOutput render(ReportContext ctx) {
        ReportMeta head = null;
        if (ctx.reportTitle() != null) {
            head = new ReportMeta();
            head.setReportTitle(ctx.reportTitle());
        }
        return ReportOutput.html(renderHtml(ctx.data(), head, ctx.elements(), ctx.groupBy()));
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        if ("XLSX".equalsIgnoreCase(format) || "EXCEL".equalsIgnoreCase(format)) {
            try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Report");
                List<Map<String, Object>> data = ctx.data();
                if (data != null && !data.isEmpty()) {
                    // Kolom dikunci dari baris pertama lalu diambil by-key: baris dengan urutan
                    // atau jumlah key berbeda tidak menggeser kolom.
                    List<String> cols = new ArrayList<>(data.get(0).keySet());
                    org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                    for (int c = 0; c < cols.size(); c++) {
                        headerRow.createCell(c).setCellValue(cols.get(c));
                    }
                    int rowIdx = 1;
                    for (Map<String, Object> row : data) {
                        org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowIdx++);
                        for (int c = 0; c < cols.size(); c++) {
                            Object val = row.get(cols.get(c));
                            if (val instanceof Number num) {
                                dataRow.createCell(c).setCellValue(num.doubleValue());
                            } else if (val != null) {
                                dataRow.createCell(c).setCellValue(val.toString());
                            }
                        }
                    }
                }
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                wb.write(out);
                return new ReportOutput("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate Excel for Standard Report: " + e.getMessage(), e);
            }
        } else if ("PDF".equalsIgnoreCase(format) || format == null || format.isBlank()) {
            try {
                ReportMeta head = null;
                if (ctx.reportTitle() != null) {
                    head = new ReportMeta();
                    head.setReportTitle(ctx.reportTitle());
                }
                String htmlContent = renderHtml(ctx.data(), head, ctx.elements(), ctx.groupBy());
                String fullHtml = "<!DOCTYPE html><html><head><style>body{font-family:sans-serif;} table{border-collapse:collapse;width:100%;} th,td{border:1px solid #000;padding:4px;}</style></head><body>" + htmlContent + "</body></html>";
                
                java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(fullHtml, "");
                builder.toStream(os);
                builder.run();
                
                return ReportOutput.pdf(os.toByteArray());
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate PDF for Standard Report: " + e.getMessage(), e);
            }
        }
        return render(ctx); // fallback to HTML
    }
}
