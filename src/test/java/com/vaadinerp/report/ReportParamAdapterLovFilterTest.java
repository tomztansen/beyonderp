package com.vaadinerp.report;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FieldFilterMeta;
import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamAdapterLovFilterTest {

    @Test
    void setsStaticLovFilterOnFieldMeta() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("cust");
        p.setParamType("COMBOBOX");
        p.setLovCode("MSCUSTOMER");
        p.setLovFilterColumn("custgroup");
        p.setLovFilterValue("Exp_3rd");

        FieldMeta f = ReportParamAdapter.toFieldMeta(p);

        assertThat(f.getFilters()).hasSize(1);
        FieldFilterMeta flt = f.getFilters().get(0);
        assertThat(flt.getFilterColumn()).isEqualTo("custgroup");
        assertThat(flt.getSourceType()).isEqualTo("STATIC");
        assertThat(flt.getSourceName()).isEqualTo("Exp_3rd");
    }

    @Test
    void noFilterWhenEmpty() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("x");
        p.setLovCode("L");
        FieldMeta f = ReportParamAdapter.toFieldMeta(p);
        assertThat(f.getFilters() == null || f.getFilters().isEmpty()).isTrue();
    }
}
