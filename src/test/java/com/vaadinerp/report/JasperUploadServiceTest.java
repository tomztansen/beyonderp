package com.vaadinerp.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;

class JasperUploadServiceTest {

    private static final String VALID_JRXML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" " +
        "name=\"t\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"555\" " +
        "leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\">" +
        "<detail><band height=\"20\"/></detail></jasperReport>";

    private JasperUploadService svc(String uploadDir) {
        ReportResolver r = new ReportResolver(null);
        r.setUploadDirForTest(uploadDir);
        return new JasperUploadService(new JasperTemplateService(), r);
    }

    @Test
    void savesValidJrxmlToTemplatePath(@TempDir Path dir) {
        JasperUploadService s = svc(dir.toString());
        s.saveUpload("INV", "report.jrxml", VALID_JRXML.getBytes(StandardCharsets.UTF_8));
        assertThat(new File(dir.toFile(), "INV.jrxml")).exists();
    }

    @Test
    void savesJasperBinaryWithoutCompiling(@TempDir Path dir) {
        JasperUploadService s = svc(dir.toString());
        s.saveUpload("INV", "report.jasper", new byte[]{1, 2, 3});
        assertThat(new File(dir.toFile(), "INV.jasper")).exists();
    }

    @Test
    void rejectsInvalidJrxml(@TempDir Path dir) {
        JasperUploadService s = svc(dir.toString());
        assertThatThrownBy(() -> s.saveUpload("INV", "bad.jrxml",
                "<nope/>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(RuntimeException.class);
    }
}
