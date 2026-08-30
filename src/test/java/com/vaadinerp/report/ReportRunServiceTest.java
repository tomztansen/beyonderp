package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.report.render.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportRunServiceTest {

    @Test
    void stimulsoftReturnsViewerUrlWithCode() {
        ReportRunService svc = new ReportRunService(mock(ReportResolver.class),
                mock(ReportDataService.class), mock(ReportRendererRegistry.class));
        ReportMeta r = new ReportMeta();
        r.setReportCode("INV");
        r.setEngineType("STIMULSOFT");

        ReportRunResult res = svc.run(r, Map.of(), "PDF", false);

        assertThat(res.stimulsoftViewer()).isTrue();
        assertThat(res.viewerUrl()).contains("/stimulsoft-java/viewer?code=INV");
    }

    @Test
    void standardRendersViaRegistry() {
        ReportResolver resolver = mock(ReportResolver.class);
        ReportDataService data = mock(ReportDataService.class);
        ReportRendererRegistry registry = mock(ReportRendererRegistry.class);

        ReportMeta r = new ReportMeta();
        r.setReportCode("PO");
        r.setEngineType("STANDARD");

        when(data.fetchData(eq(r), anyMap(), anyBoolean())).thenReturn(List.<Map<String, Object>>of());
        ReportRenderer standard = new ReportRenderer() {
            public String engine() { return "STANDARD"; }
            public ReportOutput render(ReportContext c) { return ReportOutput.html("<p>ok</p>"); }
            public ReportOutput export(ReportContext c, String f) { return render(c); }
        };
        when(registry.forEngine("STANDARD")).thenReturn(standard);

        ReportRunService svc = new ReportRunService(resolver, data, registry);
        ReportRunResult res = svc.run(r, Map.of(), "PDF", false);

        assertThat(res.stimulsoftViewer()).isFalse();
        assertThat(new String(res.output().bytes())).contains("ok");
    }
}
