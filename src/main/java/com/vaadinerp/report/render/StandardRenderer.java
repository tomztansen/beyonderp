package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;
import org.springframework.stereotype.Component;

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

    /**
     * Render band-based bila report punya {@code elements}: TITLE / PAGE_HEADER di atas,
     * COLUMN_HEADER + DETAIL sebagai tabel (DETAIL diulang per baris data), lalu
     * SUMMARY / PAGE_FOOTER. Tanpa elements, jatuh ke dump semua kolom seperti semula
     * supaya report lama tetap tampil.
     */
    public static String renderHtml(List<Map<String, Object>> data, ReportMeta report,
                                    List<ReportElementMeta> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"report-standard\">");
        if (report != null && report.getReportTitle() != null) {
            sb.append("<h2>").append(esc(report.getReportTitle())).append("</h2>");
        }
        if (elements == null || elements.isEmpty()) {
            appendAllColumns(sb, data);
        } else {
            appendBands(sb, data, elements);
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
                                    List<ReportElementMeta> elements) {
        List<Map<String, Object>> rows = (data != null) ? data : List.of();
        Map<String, List<ReportElementMeta>> bands = groupByBand(elements);

        for (String band : LEADING_BANDS) appendFreeBand(sb, bands.get(band), rows);

        List<ReportElementMeta> header = bands.get("COLUMN_HEADER");
        List<ReportElementMeta> detail = bands.get("DETAIL");
        if (header != null || detail != null) {
            sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\" style=\"width:100%\">");
            if (header != null) {
                sb.append("<thead><tr>");
                for (ReportElementMeta el : header) appendCell(sb, "th", el, null, rows);
                sb.append("</tr></thead>");
            }
            if (detail != null) {
                sb.append("<tbody>");
                for (Map<String, Object> row : rows) {
                    sb.append("<tr>");
                    for (ReportElementMeta el : detail) appendCell(sb, "td", el, row, rows);
                    sb.append("</tr>");
                }
                sb.append("</tbody>");
            }
            sb.append("</table>");
        }

        for (String band : TRAILING_BANDS) appendFreeBand(sb, bands.get(band), rows);
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
        return ReportOutput.html(renderHtml(ctx.data(), head, ctx.elements()));
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        return render(ctx); // export lanjutan (PDF/Excel) menyusul
    }
}
