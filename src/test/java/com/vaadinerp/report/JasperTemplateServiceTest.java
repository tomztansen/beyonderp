package com.vaadinerp.report;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;

class JasperTemplateServiceTest {

    // JRXML 7 carries no XML namespace — JasperReports 7 dropped it, and a file
    // saved by Jaspersoft Studio 7 has a bare <jasperReport> root. Declaring the
    // old JR6 namespace here makes the loader reject the document.
    private static final String VALID_JRXML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<jasperReport name=\"t\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"555\" " +
        "leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\">" +
        "<detail><band height=\"20\"/></detail></jasperReport>";

    @Test
    void compilesValidJrxml() throws Exception {
        JasperTemplateService svc = new JasperTemplateService();
        JasperReport jr = svc.compileForUpload(
                new ByteArrayInputStream(VALID_JRXML.getBytes(StandardCharsets.UTF_8)));
        assertThat(jr).isNotNull();
        assertThat(jr.getName()).isEqualTo("t");
    }

    @Test
    void rejectsInvalidJrxml() {
        JasperTemplateService svc = new JasperTemplateService();
        assertThatThrownBy(() -> svc.compileForUpload(
                new ByteArrayInputStream("<not-jasper/>".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(JRException.class);
    }
}
