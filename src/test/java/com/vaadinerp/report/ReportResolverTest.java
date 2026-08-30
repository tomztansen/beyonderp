package com.vaadinerp.report;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ReportResolverTest {

    private ReportResolver newResolver(String uploadDir) {
        ReportResolver r = new ReportResolver(null); // repo tak dipakai di test ini
        r.setUploadDirForTest(uploadDir);
        return r;
    }

    @Test
    void rejectsInvalidCode() {
        ReportResolver r = newResolver("./uploads");
        assertThat(r.isValidReportCode("INV_2024")).isTrue();
        assertThat(r.isValidReportCode("../etc/passwd")).isFalse();
        assertThat(r.isValidReportCode("a b")).isFalse();
        assertThat(r.isValidReportCode(null)).isFalse();
    }

    @Test
    void masterExtensionByEngine() {
        ReportResolver r = newResolver("./uploads");
        assertThat(r.masterExtension("STIMULSOFT", null)).isEqualTo("mrt");
        assertThat(r.masterExtension("JASPER", "anything.jrxml")).isEqualTo("jrxml");
        assertThat(r.masterExtension("JASPER", null)).isEqualTo("jasper");
        assertThat(r.masterExtension("STANDARD", null)).isNull();
    }

    @Test
    void resolveMasterTemplateThrowsOnTraversal() {
        ReportResolver r = newResolver("./uploads");
        assertThatThrownBy(() -> r.resolveMasterTemplate("../x", "STIMULSOFT", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolveMasterTemplateBuildsExpectedPath() {
        ReportResolver r = newResolver("/tmp/up");
        java.io.File f = r.resolveMasterTemplate("INV", "STIMULSOFT", null);
        assertThat(f.getPath().replace('\\', '/')).endsWith("/up/stimulsoft/INV.mrt");
    }
}
