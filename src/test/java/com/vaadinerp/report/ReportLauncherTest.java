package com.vaadinerp.report;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportLauncherTest {

    @Test
    void pdfContentTypeMapsToPdfFilename() {
        assertThat(ReportLauncher.outputFilename("application/pdf")).isEqualTo("report.pdf");
    }

    @Test
    void htmlContentTypeMapsToHtmlFilename() {
        assertThat(ReportLauncher.outputFilename("text/html;charset=UTF-8")).isEqualTo("report.html");
    }

    @Test
    void unknownContentTypeMapsToBin() {
        assertThat(ReportLauncher.outputFilename("application/octet-stream")).isEqualTo("report.bin");
    }

    @Test
    void nullContentTypeMapsToBin() {
        assertThat(ReportLauncher.outputFilename(null)).isEqualTo("report.bin");
    }

    @Test
    void tabIdIsPrefixedReportCode() {
        assertThat(ReportLauncher.tabId("RPT_BOM_DOC_STD")).isEqualTo("RPT_OUT_RPT_BOM_DOC_STD");
    }
}
