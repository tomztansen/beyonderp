package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportParamResolverFromRowsTest {

    private ReportParamMeta param(String name, String source, String key) {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName(name);
        p.setSource(source);
        p.setSourceKey(key);
        return p;
    }

    private Map<String, Object> row(String key, Object value) {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        return m;
    }

    @Test
    void formFieldCollectsValuesFromEveryRow() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(row("id", 38), row("id", 42)), "bob");

        assertThat(out).containsKey("bom_id");
        assertThat(out.get("bom_id")).asList().containsExactly(38, 42);
    }

    @Test
    void singleRowStillProducesAList() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(row("id", 38)), "bob");

        assertThat(out.get("bom_id")).isInstanceOf(List.class);
        assertThat(out.get("bom_id")).asList().containsExactly(38);
    }

    @Test
    void duplicatesRemovedOrderPreserved() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(row("id", 42), row("id", 38), row("id", 42)), "bob");

        assertThat(out.get("bom_id")).asList().containsExactly(42, 38);
    }

    @Test
    void nullValuesDropped() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(row("id", 38), row("id", null)), "bob");

        assertThat(out.get("bom_id")).asList().containsExactly(38);
    }

    @Test
    void noRowsOmitsTheKeyEntirely() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(), "bob");

        assertThat(out).doesNotContainKey("bom_id");
    }

    @Test
    void nullRowsTreatedAsEmpty() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")), null, "bob");

        assertThat(out).isEmpty();
    }

    @Test
    void systemParamsStillResolved() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("u", "SYSTEM", "$CURRENT_USER")),
                List.of(row("id", 1)), "bob");

        assertThat(out).containsEntry("u", "bob");
    }

    @Test
    void userInputIgnored() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("x", "USER_INPUT", null)),
                List.of(row("id", 1)), "bob");

        assertThat(out).isEmpty();
    }

    @Test
    void formFieldWithoutSourceKeyIgnored() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", null)),
                List.of(row("id", 38)), "bob");

        assertThat(out).isEmpty();
    }

    @Test
    void resolveAutoStillReturnsScalarForFormField() {
        // Jalur lama (Report Runner) tidak boleh berubah.
        Map<String, Object> out = ReportParamResolver.resolveAuto(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                row("id", 38), "bob");

        assertThat(out).containsEntry("bom_id", 38);
    }

    @Test
    void mutableRowListAccepted() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row("id", 7));
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")), rows, "bob");

        assertThat(out.get("bom_id")).asList().containsExactly(7);
    }
}
