package com.vaadinerp.report;

import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.service.DynamicDataService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportDataServiceTest {

    private DynamicDataService dynStub() {
        DynamicDataService dyn = mock(DynamicDataService.class);
        when(dyn.getQualifiedTableName("invoice")).thenReturn("dynamic.invoice");
        return dyn;
    }

    @Test
    void prefersCustomDataQuery() {
        ReportMeta r = new ReportMeta();
        r.setDataQuery("SELECT * FROM v_inv WHERE id = :id");
        r.setTableName("invoice");
        FormMeta form = new FormMeta();
        form.setViewTable("v_should_not_be_used");

        String sql = ReportDataService.resolveBaseQuery(r, form, dynStub());
        assertThat(sql).isEqualTo("SELECT * FROM v_inv WHERE id = :id");
    }

    @Test
    void fallsBackToFormViewTable() {
        ReportMeta r = new ReportMeta();
        r.setTableName("invoice");
        FormMeta form = new FormMeta();
        form.setViewTable("SELECT * FROM v_inv");

        String sql = ReportDataService.resolveBaseQuery(r, form, dynStub());
        assertThat(sql).isEqualTo("SELECT * FROM v_inv");
    }

    @Test
    void fallsBackToTableName() {
        ReportMeta r = new ReportMeta();
        r.setTableName("invoice");

        String sql = ReportDataService.resolveBaseQuery(r, null, dynStub());
        assertThat(sql).isEqualTo("SELECT * FROM dynamic.invoice");
    }

    @Test
    void returnsNullWhenNoSource() {
        ReportMeta r = new ReportMeta();
        String sql = ReportDataService.resolveBaseQuery(r, null, dynStub());
        assertThat(sql).isNull();
    }
}
