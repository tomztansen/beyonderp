package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StandardRendererGroupTest {

    private ReportElementMeta element(String band, String type, String value, int order) {
        ReportElementMeta el = new ReportElementMeta();
        el.setBandType(band);
        el.setElementType(type);
        el.setElementValue(value);
        el.setColOrder(order);
        return el;
    }

    private Map<String, Object> row(Object bomId, String material, Object qty) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bom_id", bomId);
        m.put("material", material);
        m.put("qty", qty);
        return m;
    }

    private List<ReportElementMeta> bomElements() {
        List<ReportElementMeta> els = new ArrayList<>();
        els.add(element("GROUP_HEADER", "LABEL", "BOM No:", 1));
        els.add(element("GROUP_HEADER", "FIELD", "bom_id", 2));
        els.add(element("COLUMN_HEADER", "LABEL", "Material", 1));
        els.add(element("COLUMN_HEADER", "LABEL", "Qty", 2));
        els.add(element("DETAIL", "FIELD", "material", 1));
        els.add(element("DETAIL", "FIELD", "qty", 2));
        els.add(element("GROUP_FOOTER", "SYSTEM", "SUM(qty)", 1));
        return els;
    }

    private final List<Map<String, Object>> twoGroups = List.of(
            row(38, "PASIR", 10),
            row(38, "RESIN", 5),
            row(42, "SLEEVE", 7));

    @Test
    void groupHeaderRenderedOncePerGroup() {
        ReportMeta r = new ReportMeta();
        r.setReportTitle("BOM");
        String html = StandardRenderer.renderHtml(twoGroups, r, bomElements(), "bom_id");

        // Dua kelompok → label group header muncul dua kali, bukan tiga (sejumlah baris).
        assertThat(html.split("BOM No:", -1).length - 1).isEqualTo(2);
    }

    @Test
    void everyDetailRowStillRendered() {
        String html = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "bom_id");
        assertThat(html).contains("PASIR").contains("RESIN").contains("SLEEVE");
    }

    @Test
    void pageBreakBetweenGroupsButNotBeforeFirst() {
        String html = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "bom_id");
        // Dua kelompok → tepat satu page break (di antara keduanya).
        assertThat(html.split("page-break-before", -1).length - 1).isEqualTo(1);
    }

    @Test
    void groupFooterAggregatesOnlyItsOwnGroup() {
        String html = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "bom_id");
        // Grup 38 → 10+5 = 15; grup 42 → 7. Total keseluruhan (22) tidak boleh muncul.
        assertThat(html).contains("15").contains("7");
        assertThat(html).doesNotContain(">22<");
    }

    @Test
    void groupOrderFollowsFirstAppearance() {
        List<Map<String, Object>> rows = List.of(row(42, "SLEEVE", 7), row(38, "PASIR", 10));
        String html = StandardRenderer.renderHtml(rows, null, bomElements(), "bom_id");
        assertThat(html.indexOf("SLEEVE")).isLessThan(html.indexOf("PASIR"));
    }

    @Test
    void nullGroupByFallsBackToUngroupedOutput() {
        String grouped = StandardRenderer.renderHtml(twoGroups, null, bomElements(), null);
        String legacy = StandardRenderer.renderHtml(twoGroups, null, bomElements());
        assertThat(grouped).isEqualTo(legacy);
        assertThat(grouped).doesNotContain("page-break-before");
    }

    @Test
    void blankGroupByTreatedAsNull() {
        String blank = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "   ");
        String legacy = StandardRenderer.renderHtml(twoGroups, null, bomElements());
        assertThat(blank).isEqualTo(legacy);
    }

    @Test
    void unknownGroupColumnProducesSingleGroup() {
        String html = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "no_such_column");
        // Semua baris punya nilai group null → satu kelompok, tanpa page break.
        assertThat(html).doesNotContain("page-break-before");
        assertThat(html).contains("PASIR").contains("SLEEVE");
    }

    @Test
    void titleAndSummaryRenderedOnceOutsideGroups() {
        List<ReportElementMeta> els = new ArrayList<>(bomElements());
        els.add(element("TITLE", "LABEL", "BILL OF MATERIAL", 1));
        els.add(element("SUMMARY", "SYSTEM", "COUNT()", 1));

        String html = StandardRenderer.renderHtml(twoGroups, null, els, "bom_id");

        assertThat(html.split("BILL OF MATERIAL", -1).length - 1).isEqualTo(1);
        // SUMMARY dihitung atas seluruh data (3 baris), bukan per kelompok.
        assertThat(html).contains(">3<");
    }
}
