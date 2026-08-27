package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamMetaTest {

    @Test
    void gettersReturnSetValues() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("id");
        p.setParamLabel("Invoice ID");
        p.setParamType("NUMBER");
        p.setSource("FORM_FIELD");
        p.setSourceKey("invoice_id");
        p.setLovCode("BRANCH");
        p.setRequired(true);
        p.setColOrder(1);

        assertThat(p.getParamName()).isEqualTo("id");
        assertThat(p.getParamLabel()).isEqualTo("Invoice ID");
        assertThat(p.getParamType()).isEqualTo("NUMBER");
        assertThat(p.getSource()).isEqualTo("FORM_FIELD");
        assertThat(p.getSourceKey()).isEqualTo("invoice_id");
        assertThat(p.getLovCode()).isEqualTo("BRANCH");
        assertThat(p.isRequired()).isTrue();
        assertThat(p.getColOrder()).isEqualTo(1);
    }
}
