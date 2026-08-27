package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportMeta;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class StandardRendererTest {

    @Test
    void renderHtmlContainsDetailValues() {
        ReportMeta r = new ReportMeta();
        r.setReportTitle("Daftar Item");
        List<Map<String, Object>> data = List.of(
                Map.of("code", "A1", "name", "Item A"),
                Map.of("code", "B2", "name", "Item B"));

        String html = StandardRenderer.renderHtml(data, r, java.util.List.of());

        assertThat(html).contains("Daftar Item");
        assertThat(html).contains("Item A").contains("Item B");
    }

    @Test
    void renderHtmlEscapesHtmlSpecialChars() {
        ReportMeta r = new ReportMeta();
        r.setReportTitle("T");
        List<Map<String, Object>> data = List.of(Map.of("x", "<script>"));
        String html = StandardRenderer.renderHtml(data, r, java.util.List.of());
        assertThat(html).contains("&lt;script&gt;").doesNotContain("<script>");
    }
}
