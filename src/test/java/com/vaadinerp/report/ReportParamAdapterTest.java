package com.vaadinerp.report;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamAdapterTest {

    @Test
    void mapsLovComboParamWithLovCode() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("branch");
        p.setParamLabel("Cabang");
        p.setParamType("COMBOBOX");
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
    void passesComponentTypeThrough() {
        assertThat(ReportParamAdapter.resolveComponentType("BANDBOX", "BR")).isEqualTo("BANDBOX");
        assertThat(ReportParamAdapter.resolveComponentType("CHOSENBOX", "BR")).isEqualTo("CHOSENBOX");
        assertThat(ReportParamAdapter.resolveComponentType("LISTBOX", "BR")).isEqualTo("LISTBOX");
        assertThat(ReportParamAdapter.resolveComponentType("DATE", null)).isEqualTo("DATE");
        assertThat(ReportParamAdapter.resolveComponentType("STRING", null)).isEqualTo("TEXT");
        assertThat(ReportParamAdapter.resolveComponentType(null, "BR")).isEqualTo("COMBOBOX");
        assertThat(ReportParamAdapter.resolveComponentType(null, null)).isEqualTo("TEXT");
        assertThat(ReportParamAdapter.resolveComponentType("  ", null)).isEqualTo("TEXT");
    }

    @Test
    void fallsBackLabelToParamName() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("id");
        FieldMeta f = ReportParamAdapter.toFieldMeta(p);
        assertThat(f.getFieldLabel()).isEqualTo("id");
    }
}
