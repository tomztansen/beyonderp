package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Renderer engine STANDARD: render data ke HTML "kertas". */
@Component
public class StandardRenderer implements ReportRenderer {

    @Override
    public String engine() {
        return "STANDARD";
    }

    /**
     * Render sederhana: judul + tabel semua kolom.
     * Band detail berbasis {@code elements} (TITLE/HEADER/DETAIL/SUMMARY) dipindahkan
     * dari ReportViewerView secara bertahap; tanda tangan sudah menerima {@code elements}.
     */
    public static String renderHtml(List<Map<String, Object>> data, ReportMeta report,
                                    List<ReportElementMeta> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"report-standard\">");
        if (report != null && report.getReportTitle() != null) {
            sb.append("<h2>").append(esc(report.getReportTitle())).append("</h2>");
        }
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
        sb.append("</table></div>");
        return sb.toString();
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public ReportOutput render(ReportContext ctx) {
        return ReportOutput.html(renderHtml(ctx.data(), null, java.util.List.of()));
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        return render(ctx); // export lanjutan (PDF/Excel) menyusul
    }
}
