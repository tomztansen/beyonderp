package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class ReportMetaNewFieldsTest {

    @Test
    void reportMetaHasCategoryDescriptionRoles() {
        ReportMeta r = new ReportMeta();
        r.setCategory("Sales");
        r.setDescription("Invoice listing");
        r.setAllowedRoles(Set.of("ADMIN", "SALES"));
        assertThat(r.getCategory()).isEqualTo("Sales");
        assertThat(r.getDescription()).isEqualTo("Invoice listing");
        assertThat(r.getAllowedRoles()).containsExactlyInAnyOrder("ADMIN", "SALES");
    }

    @Test
    void reportParamHasFilterColumnAndOperator() {
        ReportParamMeta p = new ReportParamMeta();
        p.setFilterColumn("trx_date");
        p.setOperator(">=");
        assertThat(p.getFilterColumn()).isEqualTo("trx_date");
        assertThat(p.getOperator()).isEqualTo(">=");
    }
}
