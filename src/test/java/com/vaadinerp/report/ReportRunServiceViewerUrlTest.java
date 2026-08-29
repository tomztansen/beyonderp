package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.report.render.ReportRendererRegistry;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReportRunServiceViewerUrlTest {

    private ReportRunService service() {
        return new ReportRunService(mock(ReportResolver.class), mock(ReportDataService.class),
                mock(ReportRendererRegistry.class));
    }

    private ReportMeta stimulsoftReport() {
        ReportMeta r = new ReportMeta();
        r.setReportCode("RPT_BOM_DOC_STI");
        r.setEngineType("STIMULSOFT");
        return r;
    }

    @Test
    void listParameterRepeatsTheKey() {
        ReportRunResult res = service().run(stimulsoftReport(),
                Map.of("bom_id", List.of(38, 42)), false);

        assertThat(res.stimulsoftViewer()).isTrue();
        assertThat(res.viewerUrl()).contains("bom_id=38").contains("bom_id=42");
        // Bentuk toString sebuah List tidak boleh bocor ke URL.
        assertThat(res.viewerUrl()).doesNotContain("[").doesNotContain("%5B");
    }

    @Test
    void scalarParameterUnchanged() {
        ReportRunResult res = service().run(stimulsoftReport(), Map.of("id", 7), false);
        assertThat(res.viewerUrl()).contains("id=7");
    }

    @Test
    void nullValuesSkipped() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", null);
        ReportRunResult res = service().run(stimulsoftReport(), params, false);
        assertThat(res.viewerUrl()).doesNotContain("id=");
    }

    @Test
    void emptyListProducesNoParameter() {
        ReportRunResult res = service().run(stimulsoftReport(),
                Map.of("bom_id", List.of()), false);
        assertThat(res.viewerUrl()).doesNotContain("bom_id");
    }

    @Test
    void valuesAreUrlEncoded() {
        ReportRunResult res = service().run(stimulsoftReport(),
                Map.of("name", List.of("a b", "c&d")), false);
        assertThat(res.viewerUrl()).contains("name=a+b").contains("name=c%26d");
    }

    @Test
    void codeAlwaysPresent() {
        ReportRunResult res = service().run(stimulsoftReport(), Map.of(), false);
        assertThat(res.viewerUrl()).startsWith("/stimulsoft-java/viewer?code=RPT_BOM_DOC_STI");
    }
}
