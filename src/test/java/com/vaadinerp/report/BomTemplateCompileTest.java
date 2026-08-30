package com.vaadinerp.report;

import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class BomTemplateCompileTest {

    @Test
    void bomTemplateCompiles() throws Exception {
        File f = new File("src/main/resources/report-templates/RPT_BOM_DOC_JSP.jrxml");
        assumeTrue(f.exists(), "template not present in this checkout");
        try (FileInputStream in = new FileInputStream(f)) {
            JasperReport jr = new JasperTemplateService().compileForUpload(in);
            assertThat(jr).isNotNull();
            assertThat(jr.getName()).isEqualTo("RPT_BOM_DOC_JSP");
        }
    }
}
