package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportMetaUsageScopeTest {

    @Test
    void defaultsToRunnerOnly() {
        ReportMeta r = new ReportMeta();
        assertThat(r.getUsageScope()).isEqualTo("RUNNER");
        assertThat(r.isUsableFrom("RUNNER")).isTrue();
        assertThat(r.isUsableFrom("FORM")).isFalse();
    }

    @Test
    void bothIsUsableFromEitherSide() {
        ReportMeta r = new ReportMeta();
        r.setUsageScope("BOTH");
        assertThat(r.isUsableFrom("FORM")).isTrue();
        assertThat(r.isUsableFrom("RUNNER")).isTrue();
    }

    @Test
    void formOnlyIsHiddenFromRunner() {
        ReportMeta r = new ReportMeta();
        r.setUsageScope("FORM");
        assertThat(r.isUsableFrom("FORM")).isTrue();
        assertThat(r.isUsableFrom("RUNNER")).isFalse();
    }

    @Test
    void nullScopeBehavesAsRunner() {
        ReportMeta r = new ReportMeta();
        r.setUsageScope(null);
        assertThat(r.isUsableFrom("RUNNER")).isTrue();
        assertThat(r.isUsableFrom("FORM")).isFalse();
    }

    @Test
    void comparisonIgnoresCaseAndPadding() {
        ReportMeta r = new ReportMeta();
        r.setUsageScope("  both  ");
        assertThat(r.isUsableFrom("form")).isTrue();
    }

    @Test
    void groupByStoresColumnName() {
        ReportMeta r = new ReportMeta();
        r.setGroupBy("bom_id");
        assertThat(r.getGroupBy()).isEqualTo("bom_id");
    }

    @Test
    void formSourceKeyPrefersTableNameThenFormCode() {
        FormMeta withTable = new FormMeta();
        withTable.setFormCode("BOM_ALL");
        withTable.setTableName("mhbom");
        assertThat(withTable.reportSourceKey()).isEqualTo("mhbom");

        FormMeta viewOnly = new FormMeta();
        viewOnly.setFormCode("SO_LINE");
        viewOnly.setViewTable("select * from tssalesline");
        assertThat(viewOnly.reportSourceKey()).isEqualTo("SO_LINE");

        FormMeta empty = new FormMeta();
        empty.setFormCode("NOTHING");
        assertThat(empty.reportSourceKey()).isNull();
    }
}
