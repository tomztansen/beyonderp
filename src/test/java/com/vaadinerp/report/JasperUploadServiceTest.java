package com.vaadinerp.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;

class JasperUploadServiceTest {

    // JRXML 7 carries no XML namespace — see JasperTemplateServiceTest.
    private static final String VALID_JRXML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<jasperReport name=\"t\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"555\" " +
        "leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\">" +
        "<detail><band height=\"20\"/></detail></jasperReport>";

    private JasperUploadService svc(String uploadDir) {
        ReportResolver r = new ReportResolver(null);
        r.setUploadDirForTest(uploadDir);
        com.vaadinerp.meta.ReportMetaRepository repo = org.mockito.Mockito.mock(com.vaadinerp.meta.ReportMetaRepository.class);
        return new JasperUploadService(new JasperTemplateService(), r, repo);
    }

    @Test
    void savesValidJrxmlToTemplatePath(@TempDir Path dir) {
        JasperUploadService s = svc(dir.toString());
        s.saveUpload("INV", "report.jrxml", VALID_JRXML.getBytes(StandardCharsets.UTF_8));
        assertThat(new File(dir.toFile(), "jasper/INV.jrxml")).exists();
    }

    /** .jasper is a serialized Java object; accepting one would hand an attacker a
     *  deserialization gadget (CVE-2023-46750). It must be refused, not stored. */
    @Test
    void rejectsJasperBinaryUpload(@TempDir Path dir) {
        JasperUploadService s = svc(dir.toString());
        assertThatThrownBy(() -> s.saveUpload("INV", "report.jasper", new byte[]{1, 2, 3}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(".jrxml");
        assertThat(new File(dir.toFile(), "INV.jasper")).doesNotExist();
    }

    @Test
    void rejectsInvalidJrxml(@TempDir Path dir) {
        JasperUploadService s = svc(dir.toString());
        // Validasi isi .jrxml kini di validateUpload(); saveUpload hanya menjaga ekstensi.
        assertThatThrownBy(() -> s.validateUpload("<nope/>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(RuntimeException.class);
    }
}
