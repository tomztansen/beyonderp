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
        java.util.List<com.vaadinerp.meta.FieldFilterMeta> flts = new java.util.ArrayList<>();
        // Model baru: daftar filter (STATIC + FIELD/cascading)
        if (p.getFilters() != null) {
            for (com.vaadinerp.meta.ReportParamFilterMeta pf : p.getFilters()) {
                if (pf.getFilterColumn() == null || pf.getFilterColumn().isBlank()) continue;
                com.vaadinerp.meta.FieldFilterMeta flt = new com.vaadinerp.meta.FieldFilterMeta();
                flt.setFilterColumn(pf.getFilterColumn().trim());
                flt.setSourceType(pf.getSourceType() != null ? pf.getSourceType().trim() : "STATIC");
                flt.setSourceName(pf.getSourceName() != null ? pf.getSourceName().trim() : "");
                flt.setComparisonOperator((pf.getComparisonOperator() != null && !pf.getComparisonOperator().isBlank())
                        ? pf.getComparisonOperator().trim() : "=");
                flt.setLogicalOperator((pf.getLogicalOperator() != null && !pf.getLogicalOperator().isBlank())
                        ? pf.getLogicalOperator().trim() : "AND");
                flts.add(flt);
            }
        }
        // Fallback lama: kolom lov_filter_* datar (kalau belum dimigrasi)
        if (flts.isEmpty() && p.getLovFilterColumn() != null && !p.getLovFilterColumn().isBlank()
                && p.getLovFilterValue() != null && !p.getLovFilterValue().isBlank()) {
            com.vaadinerp.meta.FieldFilterMeta flt = new com.vaadinerp.meta.FieldFilterMeta();
            flt.setFilterColumn(p.getLovFilterColumn().trim());
            flt.setSourceType("STATIC");
            flt.setSourceName(p.getLovFilterValue().trim());
            flt.setComparisonOperator((p.getLovFilterOperator() != null && !p.getLovFilterOperator().isBlank())
                    ? p.getLovFilterOperator().trim() : "=");
            flt.setLogicalOperator("AND");
            flts.add(flt);
        }
        if (!flts.isEmpty()) f.setFilters(flts);
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
