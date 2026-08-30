package com.vaadinerp.report.render;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Export PDF/Excel dari STANDARD renderer. */
class StandardRendererExportTest {

    private static ReportContext ctx(List<Map<String, Object>> data) {
        return new ReportContext("RPT", "STANDARD", null, data, Map.of(),
                "A4", "PORTRAIT", "Daftar Item", List.of(), null);
    }

    private static Map<String, Object> row(String code, Object qty) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", code);
        m.put("qty", qty);
        return m;
    }

    @Test
    void pdfExportProducesPdfBytes() {
        ReportOutput out = new StandardRenderer().export(ctx(List.of(row("A1", 5))), "PDF");

        assertThat(out.contentType()).isEqualTo("application/pdf");
        assertThat(new String(out.bytes(), 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void blankFormatFallsBackToPdf() {
        assertThat(new StandardRenderer().export(ctx(List.of(row("A1", 5))), "").contentType())
                .isEqualTo("application/pdf");
    }

    @Test
    void htmlFormatStillRendersHtml() {
        ReportOutput out = new StandardRenderer().export(ctx(List.of(row("A1", 5))), "HTML");

        assertThat(out.contentType()).startsWith("text/html");
        assertThat(new String(out.bytes())).contains("Daftar Item").contains("A1");
    }

    /** Baris dengan urutan key berbeda tidak boleh menggeser kolom. */
    @Test
    void excelExportKeepsColumnsAlignedAcrossRows() throws Exception {
        Map<String, Object> reordered = new LinkedHashMap<>();
        reordered.put("qty", 7);
        reordered.put("code", "B2");

        ReportOutput out = new StandardRenderer().export(ctx(List.of(row("A1", 5), reordered)), "EXCEL");
        assertThat(out.contentType()).contains("spreadsheetml");

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(
                new java.io.ByteArrayInputStream(out.bytes()))) {
            org.apache.poi.ss.usermodel.Sheet sh = wb.getSheetAt(0);
            assertThat(sh.getRow(0).getCell(0).getStringCellValue()).isEqualTo("code");
            assertThat(sh.getRow(0).getCell(1).getStringCellValue()).isEqualTo("qty");
            assertThat(sh.getRow(1).getCell(0).getStringCellValue()).isEqualTo("A1");
            assertThat(sh.getRow(1).getCell(1).getNumericCellValue()).isEqualTo(5d);
            assertThat(sh.getRow(2).getCell(0).getStringCellValue()).isEqualTo("B2");
            assertThat(sh.getRow(2).getCell(1).getNumericCellValue()).isEqualTo(7d);
        }
    }
}
