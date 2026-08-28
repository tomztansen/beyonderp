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
    void mapsFilterListStaticAndField() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("cust");
        p.setParamType("COMBOBOX");
        p.setLovCode("MST_CST");

        com.vaadinerp.meta.ReportParamFilterMeta s = new com.vaadinerp.meta.ReportParamFilterMeta();
        s.setFilterColumn("custgroup");
        s.setSourceType("STATIC");
        s.setSourceName("Exp_3rd");
        s.setComparisonOperator("=");
        s.setLogicalOperator("AND");

        com.vaadinerp.meta.ReportParamFilterMeta fld = new com.vaadinerp.meta.ReportParamFilterMeta();
        fld.setFilterColumn("customerregion");
        fld.setSourceType("FIELD");
        fld.setSourceName("grup");
        fld.setComparisonOperator("=");
        fld.setLogicalOperator("AND");

        p.getFilters().add(s);
        p.getFilters().add(fld);

        FieldMeta f = ReportParamAdapter.toFieldMeta(p);
        assertThat(f.getFilters()).hasSize(2);
        assertThat(f.getFilters().get(0).getSourceType()).isEqualTo("STATIC");
        assertThat(f.getFilters().get(0).getSourceName()).isEqualTo("Exp_3rd");
        assertThat(f.getFilters().get(1).getSourceType()).isEqualTo("FIELD");
        assertThat(f.getFilters().get(1).getFilterColumn()).isEqualTo("customerregion");
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
