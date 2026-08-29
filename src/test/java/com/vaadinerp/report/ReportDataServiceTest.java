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

    /** A form whose view_table is a bare view name, reached via its form_code as source key.
     *  Regression: this used to emit "SELECT * FROM dynamic.MST_CST" (the form_code as a table). */
    @Test
    void wrapsBareViewNameFromFormInSelect() {
        DynamicDataService dyn = mock(DynamicDataService.class);
        when(dyn.getQualifiedTableName("mscustomer")).thenReturn("dynamic.mscustomer");

        ReportMeta r = new ReportMeta();
        r.setTableName("MST_CST");          // form_code, not a table
        FormMeta form = new FormMeta();
        form.setViewTable("mscustomer");    // bare name, not a SELECT

        String sql = ReportDataService.resolveBaseQuery(r, form, dyn);
        assertThat(sql).isEqualTo("SELECT * FROM dynamic.mscustomer");
        assertThat(sql).doesNotContain("MST_CST");
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
