package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamMetaTest {

    @Test
    void gettersReturnSetValues() {
        ReportParamMeta p = new ReportParamMeta();
        p.setReportCode("INV");
        p.setParamName("id");
        p.setLabel("Invoice ID");
        p.setDataType("NUMBER");
        p.setSource("FORM_FIELD");
        p.setSourceKey("invoice_id");
        p.setRequired(true);
        p.setColOrder(1);

        assertThat(p.getReportCode()).isEqualTo("INV");
        assertThat(p.getParamName()).isEqualTo("id");
        assertThat(p.getDataType()).isEqualTo("NUMBER");
        assertThat(p.getSource()).isEqualTo("FORM_FIELD");
        assertThat(p.getSourceKey()).isEqualTo("invoice_id");
        assertThat(p.isRequired()).isTrue();
        assertThat(p.getColOrder()).isEqualTo(1);
    }
}
