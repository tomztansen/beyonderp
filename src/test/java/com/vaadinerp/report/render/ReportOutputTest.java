package com.vaadinerp.report.render;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportOutputTest {

    @Test
    void pdfFactorySetsContentType() {
        ReportOutput o = ReportOutput.pdf(new byte[]{1, 2, 3});
        assertThat(o.contentType()).isEqualTo("application/pdf");
        assertThat(o.bytes()).hasSize(3);
    }

    @Test
    void htmlFactorySetsContentType() {
        ReportOutput o = ReportOutput.html("<b>x</b>");
        assertThat(o.contentType()).isEqualTo("text/html;charset=UTF-8");
        assertThat(new String(o.bytes(), java.nio.charset.StandardCharsets.UTF_8)).contains("<b>x</b>");
    }
}
