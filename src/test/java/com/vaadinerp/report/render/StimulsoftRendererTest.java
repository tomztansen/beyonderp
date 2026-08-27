package com.vaadinerp.report.render;

import com.stimulsoft.report.StiReport;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class StimulsoftRendererTest {

    @Test
    void bindDataAddsSingleJsonDatabaseNamedDynamicData() throws Exception {
        StiReport report = new StiReport();
        List<Map<String, Object>> data = List.of(Map.of("id", 1, "name", "A"));

        StimulsoftRenderer.bindData(report, data);

        assertThat(report.getDictionary().getDatabases().size()).isEqualTo(1);
        assertThat(report.getDictionary().getDatabases().get(0).getName()).isEqualTo("DynamicData");
    }
}
