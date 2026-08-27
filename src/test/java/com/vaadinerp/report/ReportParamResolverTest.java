package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamResolverTest {

    private ReportParamMeta p(String name, String source, String key) {
        ReportParamMeta m = new ReportParamMeta();
        m.setParamName(name); m.setSource(source); m.setSourceKey(key);
        return m;
    }

    @Test
    void resolvesFormFieldFromRecord() {
        Map<String, Object> out = ReportParamResolver.resolveAuto(
                List.of(p("id", "FORM_FIELD", "invoice_id")),
                Map.of("invoice_id", 123), "bob");
        assertThat(out).containsEntry("id", 123);
    }

    @Test
    void resolvesSystemCurrentUser() {
        Map<String, Object> out = ReportParamResolver.resolveAuto(
                List.of(p("u", "SYSTEM", "$CURRENT_USER")), Map.of(), "bob");
        assertThat(out).containsEntry("u", "bob");
    }

    @Test
    void ignoresUserInput() {
        Map<String, Object> out = ReportParamResolver.resolveAuto(
                List.of(p("x", "USER_INPUT", null)), Map.of(), "bob");
        assertThat(out).isEmpty();
    }

    @Test
    void listsUserInputParams() {
        List<ReportParamMeta> ui = ReportParamResolver.userInputParams(
                List.of(p("a", "USER_INPUT", null), p("b", "FORM_FIELD", "k")));
        assertThat(ui).hasSize(1);
        assertThat(ui.get(0).getParamName()).isEqualTo("a");
    }
}
