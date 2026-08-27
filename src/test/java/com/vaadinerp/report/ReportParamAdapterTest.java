package com.vaadinerp.report;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamAdapterTest {

    @Test
    void mapsLovParamToComboboxWithLovCode() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("branch");
        p.setLabel("Cabang");
        p.setDataType("LOV");
        p.setLovCode("BRANCH");
        p.setRequired(true);

        FieldMeta f = ReportParamAdapter.toFieldMeta(p);

        assertThat(f.getFieldName()).isEqualTo("branch");
        assertThat(f.getFieldLabel()).isEqualTo("Cabang");
        assertThat(f.getLovCode()).isEqualTo("BRANCH");
        assertThat(f.isRequired()).isTrue();
        assertThat(f.getComponentType()).isEqualTo("COMBOBOX");
    }

    @Test
    void resolvesComponentTypePerDataType() {
        assertThat(ReportParamAdapter.resolveComponentType("DATE", null)).isEqualTo("DATE");
        assertThat(ReportParamAdapter.resolveComponentType("NUMBER", null)).isEqualTo("NUMERIC");
        assertThat(ReportParamAdapter.resolveComponentType("BOOLEAN", null)).isEqualTo("CHECKBOX");
        assertThat(ReportParamAdapter.resolveComponentType("TEXT", null)).isEqualTo("TEXT");
        assertThat(ReportParamAdapter.resolveComponentType(null, null)).isEqualTo("TEXT");
    }

    @Test
    void lovCodePresenceForcesCombobox() {
        assertThat(ReportParamAdapter.resolveComponentType("TEXT", "BRANCH")).isEqualTo("COMBOBOX");
    }

    @Test
    void fallsBackLabelToParamName() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("id");
        FieldMeta f = ReportParamAdapter.toFieldMeta(p);
        assertThat(f.getFieldLabel()).isEqualTo("id");
    }
}
