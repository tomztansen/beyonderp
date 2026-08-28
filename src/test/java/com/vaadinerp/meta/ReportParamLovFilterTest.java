package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamLovFilterTest {
    @Test
    void hasLovFilterFields() {
        ReportParamMeta p = new ReportParamMeta();
        p.setLovFilterColumn("custgroup");
        p.setLovFilterValue("Exp_3rd");
        p.setLovFilterOperator("=");
        assertThat(p.getLovFilterColumn()).isEqualTo("custgroup");
        assertThat(p.getLovFilterValue()).isEqualTo("Exp_3rd");
        assertThat(p.getLovFilterOperator()).isEqualTo("=");
    }
}
