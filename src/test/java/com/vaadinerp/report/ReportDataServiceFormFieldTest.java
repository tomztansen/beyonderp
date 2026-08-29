package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportDataServiceFormFieldTest {

    private ReportParamMeta formFieldParam(String name, String column, String operator) {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName(name);
        p.setSource("FORM_FIELD");
        p.setSourceKey("id");
        p.setFilterColumn(column);
        p.setOperator(operator);
        return p;
    }

    @Test
    void formFieldUsesInClauseNotAny() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "=")),
                Map.of("bom_id", List.of(38, 42)), bind);

        // Spring hanya meng-expand Collection pada bentuk IN (...), bukan = ANY(...).
        assertThat(where).isEqualTo(" WHERE id IN (:bom_id)");
        assertThat(where).doesNotContain("ANY");
        assertThat(bind).containsEntry("bom_id", List.of(38, 42));
    }

    @Test
    void storedOperatorIsIgnoredForFormField() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "LIKE")),
                Map.of("bom_id", List.of(38)), bind);

        assertThat(where).isEqualTo(" WHERE id IN (:bom_id)");
        // LIKE tidak boleh membungkus nilai dengan %..%
        assertThat(bind).containsEntry("bom_id", List.of(38));
    }

    @Test
    void singleElementListStillUsesInClause() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "=")),
                Map.of("bom_id", List.of(38)), bind);

        assertThat(where).isEqualTo(" WHERE id IN (:bom_id)");
    }

    @Test
    void emptyCollectionProducesNoClause() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "=")),
                Map.of("bom_id", List.of()), bind);

        assertThat(where).isEmpty();
        assertThat(bind).isEmpty();
    }

    @Test
    void formFieldWithoutFilterColumnIsModelAAndSkipped() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", null, null)),
                Map.of("bom_id", List.of(38)), bind);

        assertThat(where).isEmpty();
    }

    @Test
    void userInputParamKeepsItsOperator() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("name");
        p.setSource("USER_INPUT");
        p.setFilterColumn("itemname");
        p.setOperator("LIKE");

        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p), Map.of("name", "bolt"), bind);

        assertThat(where).isEqualTo(" WHERE itemname LIKE :name");
        assertThat(bind).containsEntry("name", "%bolt%");
    }

    @Test
    void multipleParamsJoinedWithAnd() {
        ReportParamMeta user = new ReportParamMeta();
        user.setParamName("name");
        user.setSource("USER_INPUT");
        user.setFilterColumn("itemname");
        user.setOperator("=");

        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "="), user),
                Map.of("bom_id", List.of(38), "name", "bolt"), bind);

        assertThat(where).isEqualTo(" WHERE id IN (:bom_id) AND itemname = :name");
    }
}
