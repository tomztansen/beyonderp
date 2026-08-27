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
        f.setFieldLabel(p.getLabel() != null ? p.getLabel() : p.getParamName());
        f.setRequired(p.isRequired());
        f.setLovCode(p.getLovCode());
        f.setComponentType(resolveComponentType(p.getDataType(), p.getLovCode()));
        return f;
    }

    /**
     * Petakan dataType parameter → componentType yang dikenali ComponentFactory.
     * LOV (atau adanya lovCode) → COMBOBOX; ComponentFactory memakai lovCode untuk
     * membangun combo LOV.
     */
    public static String resolveComponentType(String dataType, String lovCode) {
        if (lovCode != null && !lovCode.trim().isEmpty()) return "COMBOBOX";
        if (dataType == null) return "TEXT";
        switch (dataType.trim().toUpperCase()) {
            case "LOV":     return "COMBOBOX";
            case "DATE":    return "DATE";
            case "NUMBER":  return "NUMERIC";
            case "BOOLEAN": return "CHECKBOX";
            default:        return "TEXT";
        }
    }
}
