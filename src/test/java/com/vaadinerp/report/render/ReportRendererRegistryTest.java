package com.vaadinerp.report.render;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ReportRendererRegistryTest {

    private ReportRenderer stub(String engine) {
        return new ReportRenderer() {
            public String engine() { return engine; }
            public ReportOutput render(ReportContext c) { return ReportOutput.html("x"); }
            public ReportOutput export(ReportContext c, String f) { return ReportOutput.html("x"); }
        };
    }

    @Test
    void returnsRendererForKnownEngineCaseInsensitive() {
        ReportRendererRegistry reg = new ReportRendererRegistry(List.of(stub("STANDARD"), stub("JASPER")));
        assertThat(reg.forEngine("jasper").engine()).isEqualTo("JASPER");
    }

    @Test
    void defaultsToStandardForNullEngine() {
        ReportRendererRegistry reg = new ReportRendererRegistry(List.of(stub("STANDARD"), stub("JASPER")));
        assertThat(reg.forEngine(null).engine()).isEqualTo("STANDARD");
    }

    @Test
    void throwsForUnknownEngine() {
        ReportRendererRegistry reg = new ReportRendererRegistry(List.of(stub("STANDARD")));
        assertThatThrownBy(() -> reg.forEngine("STIMULSOFT"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
