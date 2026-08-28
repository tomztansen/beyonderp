package com.vaadinerp.report;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportParamMeta;

/**
 * Memetakan definisi parameter report ke {@link FieldMeta} sehingga
 * {@code ComponentFactory.create(fieldMeta, ...)} bisa merender komponen input
 * yang tepat (termasuk LOV, DatePicker, dsb.) tanpa ubahan.
 */
public final class ReportParamAdapter {

    private ReportParamAdapter() {}

    public static FieldMeta toFieldMeta(ReportParamMeta p) {
        FieldMeta f = new FieldMeta();
        f.setFieldName(p.getParamName());
        f.setFieldLabel(p.getParamLabel() != null ? p.getParamLabel() : p.getParamName());
        f.setRequired(p.isRequired());
        f.setLovCode(p.getLovCode());
        f.setComponentType(resolveComponentType(p.getParamType(), p.getLovCode()));
        if (p.getLovFilterColumn() != null && !p.getLovFilterColumn().isBlank()
                && p.getLovFilterValue() != null && !p.getLovFilterValue().isBlank()) {
            com.vaadinerp.meta.FieldFilterMeta flt = new com.vaadinerp.meta.FieldFilterMeta();
            flt.setFilterColumn(p.getLovFilterColumn().trim());
            flt.setSourceType("STATIC");
            flt.setSourceName(p.getLovFilterValue().trim());
            flt.setComparisonOperator((p.getLovFilterOperator() != null && !p.getLovFilterOperator().isBlank())
                    ? p.getLovFilterOperator().trim() : "=");
            flt.setLogicalOperator("AND");
            f.setFilters(new java.util.ArrayList<>(java.util.List.of(flt)));
        }
        return f;
    }

    /**
     * paramType diteruskan apa adanya sebagai componentType (mendukung TEXTBOX/COMBOBOX/BANDBOX/
     * CHOSENBOX/LISTBOX/DATE/NUMERIC/CHECKBOX/… penuh yang dikenal ComponentFactory).
     * Alias {@code STRING}/kosong → {@code TEXT}; kosong + ada lovCode → {@code COMBOBOX}.
     * Komponen LOV-driven memakai lovCode yang di-set di FieldMeta.
     */
    public static String resolveComponentType(String paramType, String lovCode) {
        if (paramType != null && !paramType.trim().isEmpty()) {
            String t = paramType.trim().toUpperCase();
            if (t.equals("STRING")) return "TEXT";
            return t;
        }
        if (lovCode != null && !lovCode.trim().isEmpty()) return "COMBOBOX";
        return "TEXT";
    }
}
