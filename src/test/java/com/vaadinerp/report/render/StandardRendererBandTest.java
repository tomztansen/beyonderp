package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StandardRendererBandTest {

    private static ReportElementMeta el(String band, String type, String value, int order) {
        ReportElementMeta e = new ReportElementMeta();
        e.setBandType(band);
        e.setElementType(type);
        e.setElementValue(value);
        e.setColOrder(order);
        return e;
    }

    private static final List<Map<String, Object>> DATA = List.of(
            Map.of("name", "Item A", "price", 1500),
            Map.of("name", "Item B", "price", 2500));

    @Test
    void detailBandRendersOnlyChosenFieldsPerRow() {
        String html = StandardRenderer.renderHtml(DATA, null, List.of(
                el("COLUMN_HEADER", "LABEL", "Nama", 1),
                el("DETAIL", "FIELD", "name", 1)));

        assertThat(html).contains("Nama").contains("Item A").contains("Item B");
        // price was not placed on any band, so it must not leak into the output
        assertThat(html).doesNotContain("1500").doesNotContain("price");
    }

    @Test
    void summaryBandComputesAggregatesOverAllRows() {
        String html = StandardRenderer.renderHtml(DATA, null, List.of(
                el("DETAIL", "FIELD", "name", 1),
                el("SUMMARY", "SYSTEM", "SUM(price)", 1),
                el("SUMMARY", "SYSTEM", "COUNT()", 2)));

        assertThat(html).contains("4000");   // 1500 + 2500
        assertThat(html).contains(">2<");    // row count
    }

    @Test
    void formatPatternAppliedToNumericField() {
        ReportElementMeta e = el("DETAIL", "FIELD", "price", 1);
        e.setFormatPattern("#,##0");
        String html = StandardRenderer.renderHtml(List.of(Map.of("price", 1500)), null, List.of(e));
        assertThat(html).contains("1,500");
    }

    @Test
    void bandAttributesBecomeInlineStyle() {
        ReportElementMeta e = el("TITLE", "LABEL", "INVOICE", 1);
        e.setAlignment("CENTER");
        e.setFontWeight("BOLD");
        e.setColumnWidth("50%");
        String html = StandardRenderer.renderHtml(DATA, null, List.of(e));
        assertThat(html).contains("text-align:center").contains("font-weight:bold").contains("width:50%");
    }

    @Test
    void elementValuesAreEscaped() {
        String html = StandardRenderer.renderHtml(
                List.of(Map.of("name", "<script>")), null,
                List.of(el("DETAIL", "FIELD", "name", 1)));
        assertThat(html).contains("&lt;script&gt;").doesNotContain("<script>");
    }

    @Test
    void withoutElementsFallsBackToAllColumns() {
        ReportMeta r = new ReportMeta();
        r.setReportTitle("Dump");
        String html = StandardRenderer.renderHtml(DATA, r, List.of());
        assertThat(html).contains("Dump").contains("Item A").contains("1500").contains("price");
    }
}
