package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportParamMeta;
import com.vaadinerp.report.ReportParamAdapter;
import com.vaadinerp.report.ReportParamResolver;
import com.vaadinerp.service.DynamicDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Form parameter dinamis: render input untuk tiap parameter USER_INPUT lewat
 * ReportParamAdapter → ComponentFactory (mendukung semua komponen: textbox, combobox,
 * bandbox, chosenbox, listbox, date, dll). collectValues() mengumpulkan nilai by paramName.
 */
public class ReportParameterForm extends VerticalLayout {

    private final Map<String, Component> inputs = new LinkedHashMap<>();

    public ReportParameterForm(List<ReportParamMeta> params, DynamicDataService dyn) {
        setPadding(false);
        setSpacing(false);
        List<ReportParamMeta> userParams = ReportParamResolver.userInputParams(params);

        List<FieldMeta> fields = new ArrayList<>();
        for (ReportParamMeta p : userParams) fields.add(ReportParamAdapter.toFieldMeta(p));

        FormLayout layout = new FormLayout();
        int cols = FormLayoutUtils.calculateMaxColsInForm(fields);
        FormLayoutUtils.applyResponsiveSteps(layout, Math.max(1, cols));

        for (int i = 0; i < userParams.size(); i++) {
            ReportParamMeta p = userParams.get(i);
            Component input = ComponentFactory.create(fields.get(i), dyn, (k, v) -> {});
            inputs.put(p.getParamName(), input);
            layout.add(input);
        }
        add(layout);
    }

    /** Kumpulkan nilai tiap input by paramName. */
    public Map<String, Object> collectValues() {
        Map<String, Object> out = new HashMap<>();
        for (Map.Entry<String, Component> e : inputs.entrySet()) {
            if (e.getValue() instanceof HasValue<?, ?> hv) {
                out.put(e.getKey(), hv.getValue());
            }
        }
        return out;
    }
}
