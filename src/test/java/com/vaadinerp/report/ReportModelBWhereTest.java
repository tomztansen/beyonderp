package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class ReportModelBWhereTest {

    private ReportParamMeta p(String name, String col, String op) {
        ReportParamMeta m = new ReportParamMeta();
        m.setParamName(name); m.setFilterColumn(col); m.setOperator(op);
        return m;
    }

    @Test
    void buildsEqualsAndBindsValue() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p("branch", "branch_id", "=")), Map.of("branch", 7), bind);
        assertThat(where).isEqualTo(" WHERE branch_id = :branch");
        assertThat(bind).containsEntry("branch", 7);
    }

    @Test
    void likeWrapsWithPercent() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p("kw", "name", "ILIKE")), Map.of("kw", "abc"), bind);
        assertThat(where).isEqualTo(" WHERE name ILIKE :kw");
        assertThat(bind).containsEntry("kw", "%abc%");
    }

    @Test
    void rangeTwoParams() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p("d1", "trx_date", ">="), p("d2", "trx_date", "<=")),
                Map.of("d1", "2026-01-01", "d2", "2026-01-31"), bind);
        assertThat(where).isEqualTo(" WHERE trx_date >= :d1 AND trx_date <= :d2");
        assertThat(bind).containsKeys("d1", "d2");
    }

    @Test
    void inUsesAny() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p("st", "status", "IN")), Map.of("st", List.of("A", "B")), bind);
        assertThat(where).isEqualTo(" WHERE status = ANY(:st)");
    }

    @Test
    void skipsParamsWithoutValueOrModelB() {
        Map<String, Object> bind = new HashMap<>();
        ReportParamMeta noValue = p("x", "col", "=");
        ReportParamMeta modelA = new ReportParamMeta();
        modelA.setParamName("y"); // no filterColumn → Model A
        String where = ReportDataService.buildModelBWhere(
                List.of(noValue, modelA), new HashMap<>(), bind);
        assertThat(where).isEmpty();
    }

    @Test
    void rejectsInvalidOperator() {
        assertThatThrownBy(() -> ReportDataService.buildModelBWhere(
                List.of(p("x", "col", "DROP")), Map.of("x", 1), new HashMap<>()))
                .isInstanceOf(RuntimeException.class);
    }
}
