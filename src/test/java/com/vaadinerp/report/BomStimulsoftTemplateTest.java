package com.vaadinerp.report;

import com.stimulsoft.report.StiReport;
import com.stimulsoft.report.StiSerializeManager;
import com.vaadinerp.report.render.StimulsoftRenderer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Stimulsoft BOM template is hand-authored XML (Task 9 shipped no .mrt), so this
 * guards against a broken structure before it reaches the web viewer: the file must
 * deserialize, and the app's live-data binding (bindData + dictionary synchronize)
 * must accept it against the DynamicData source the DataBand references.
 */
class BomStimulsoftTemplateTest {

    @Test
    void templateDeserializesAndBindsData() throws Exception {
        File f = new File("src/main/resources/report-templates/stimulsoft/RPT_BOM_DOC_STI.mrt");
        assumeTrue(f.exists(), "template not present in this checkout");

        StiReport report = StiSerializeManager.deserializeReport(f);
        assertThat(report).isNotNull();

        // Two BOMs' rows, mirroring the shared data_query columns — the same shape the
        // controller feeds at runtime. bindData replaces the design-time database and
        // synchronizes the dictionary; a bad DataSource/column wiring throws here.
        List<Map<String, Object>> rows = List.of(
                Map.of("bom_id", 38, "idno", "BOM00053", "product", "P1", "drawing", "-",
                        "netweight", "0", "material", "MAT-A", "itemgroup", "G1",
                        "qty", 2, "perseries", 1),
                Map.of("bom_id", 49, "idno", "BOM00063", "product", "P2", "drawing", "-",
                        "netweight", "0", "material", "MAT-B", "itemgroup", "G2",
                        "qty", 3, "perseries", 1));

        StimulsoftRenderer.bindData(report, rows);

        assertThat(report.getDictionary().getDataSources().get("DynamicData")).isNotNull();
    }
}
