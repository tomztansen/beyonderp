package com.vaadinerp.components;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.views.FormBuilderView;

import java.util.*;

public class FormLayoutUtils {

    /**
     * Menghitung jumlah kolom terbanyak dalam form (maxColsInForm) berdasarkan
     * seluruh field di dalam form yang terbagi per rowGroup.
     */
    public static int calculateMaxColsInForm(List<FieldMeta> allFields) {
        if (allFields == null || allFields.isEmpty()) {
            return 1;
        }
        Map<Integer, List<FieldMeta>> groups = new HashMap<>();
        for (FieldMeta field : allFields) {
            if (field.isHideInForm() || (field.getIsDetail() != null && field.getIsDetail())) {
                continue; // abaikan field hidden atau detail grid dari hitungan kolom form utama
            }
            int rg = field.getRowGroup() != null ? field.getRowGroup() : 1;
            groups.computeIfAbsent(rg, k -> new ArrayList<>()).add(field);
        }

        int maxCols = 1;
        for (List<FieldMeta> groupFields : groups.values()) {
            int rowCols = calculateRowMinCols(groupFields);
            if (rowCols > maxCols) {
                maxCols = rowCols;
            }
        }
        return maxCols;
    }

    /**
     * Menghitung minimum kolom yang dibutuhkan untuk satu baris (rowGroup).
     */
    private static int calculateRowMinCols(List<FieldMeta> rowFields) {
        if (rowFields == null || rowFields.isEmpty()) {
            return 1;
        }
        int explicitSum = 0;
        int emptyCount = 0;
        for (FieldMeta f : rowFields) {
            if (f.getColSpan() != null && f.getColSpan() > 0) {
                explicitSum += f.getColSpan();
            } else {
                emptyCount++;
            }
        }
        if (emptyCount == rowFields.size()) {
            // Semua field colSpan-nya kosong (null/<=0) -> kolom sama dengan jumlah field
            return Math.max(1, rowFields.size());
        }
        return Math.max(1, explicitSum + emptyCount);
    }

    /**
     * Data penataan kolom dan span untuk suatu baris form.
     */
    public static class RowLayoutConfig {
        private final int cols;
        private final Map<FieldMeta, Integer> fieldSpans = new HashMap<>();

        public RowLayoutConfig(int cols) {
            this.cols = Math.max(1, cols);
        }

        public int getCols() {
            return cols;
        }

        public int getSpan(FieldMeta field) {
            return fieldSpans.getOrDefault(field, 1);
        }

        public void setSpan(FieldMeta field, int span) {
            fieldSpans.put(field, Math.max(1, Math.min(cols, span)));
        }
    }

    /**
     * Menghitung konfigurasi kolom (cols) dan span tiap field dalam 1 rowGroup.
     *
     * @param rowFields     Daftar field dalam baris ini (sudah diurutkan berdasarkan colOrder)
     * @param maxColsInForm Jumlah kolom acuan terbanyak di seluruh form (hasil calculateMaxColsInForm)
     * @return RowLayoutConfig berisi jumlah kolom rowLayout dan span tiap field
     */
    public static RowLayoutConfig calculateRowConfig(List<FieldMeta> rowFields, int maxColsInForm) {
        if (rowFields == null || rowFields.isEmpty()) {
            return new RowLayoutConfig(1);
        }

        int explicitSum = 0;
        int emptyCount = 0;
        List<FieldMeta> emptyFields = new ArrayList<>();

        for (FieldMeta f : rowFields) {
            if (f.getColSpan() != null && f.getColSpan() > 0) {
                explicitSum += f.getColSpan();
            } else {
                emptyCount++;
                emptyFields.add(f);
            }
        }

        int rowCols;
        RowLayoutConfig config;

        // Skenario 1: Semua field di baris ini colSpan-nya kosong / null (emptyCount == rowFields.size())
        // Maka dinamisasikan rata persis seperti perilaku sekarang ("dinamis seperti sekarang")
        if (emptyCount == rowFields.size()) {
            rowCols = Math.max(1, rowFields.size());
            config = new RowLayoutConfig(rowCols);
            for (FieldMeta f : rowFields) {
                if ("TEXTAREA".equalsIgnoreCase(f.getComponentType()) && rowFields.size() == 1) {
                    config.setSpan(f, rowCols);
                } else {
                    config.setSpan(f, 1);
                }
            }
            return config;
        }

        // Skenario 2 & 3: Ada field dengan colSpan eksplisit (>0) dan mungkin ada juga yang kosong
        rowCols = Math.max(maxColsInForm, explicitSum + emptyCount);
        config = new RowLayoutConfig(rowCols);

        // Pasang span untuk field yang eksplisit
        for (FieldMeta f : rowFields) {
            if (f.getColSpan() != null && f.getColSpan() > 0) {
                int span = f.getColSpan();
                // Jika TEXTAREA tunggal di baris dengan colspan=1, biarkan selebar baris kecuali diatur lain
                if ("TEXTAREA".equalsIgnoreCase(f.getComponentType()) && span == 1 && rowFields.size() == 1 && rowCols > 1) {
                    span = rowCols;
                }
                config.setSpan(f, span);
            }
        }

        // Pasang span untuk field yang kosong (auto-fill sisa kolom)
        if (emptyCount > 0) {
            int remaining = Math.max(emptyCount, rowCols - explicitSum);
            int baseSpan = remaining / emptyCount;
            int remainder = remaining % emptyCount;

            for (int i = 0; i < emptyFields.size(); i++) {
                FieldMeta emptyField = emptyFields.get(i);
                int span = baseSpan + (i < remainder ? 1 : 0);
                if ("TEXTAREA".equalsIgnoreCase(emptyField.getComponentType()) && rowFields.size() == 1) {
                    span = rowCols;
                }
                config.setSpan(emptyField, span);
            }
        }

        return config;
    }

    // =================================================================================
    // SUPPORT UNTUK PREVIEW DI FORM BUILDER (FieldMetaTemp)
    // =================================================================================

    public static int calculateMaxColsInFormTemp(List<FormBuilderView.FieldMetaTemp> allFields) {
        if (allFields == null || allFields.isEmpty()) {
            return 1;
        }
        Map<Integer, List<FormBuilderView.FieldMetaTemp>> groups = new HashMap<>();
        for (FormBuilderView.FieldMetaTemp field : allFields) {
            if (field.hideInForm || field.isDetail) {
                continue;
            }
            int rg = field.rowGroup > 0 ? field.rowGroup : 1;
            groups.computeIfAbsent(rg, k -> new ArrayList<>()).add(field);
        }

        int maxCols = 1;
        for (List<FormBuilderView.FieldMetaTemp> groupFields : groups.values()) {
            int rowCols = calculateRowMinColsTemp(groupFields);
            if (rowCols > maxCols) {
                maxCols = rowCols;
            }
        }
        return maxCols;
    }

    private static int calculateRowMinColsTemp(List<FormBuilderView.FieldMetaTemp> rowFields) {
        if (rowFields == null || rowFields.isEmpty()) {
            return 1;
        }
        int explicitSum = 0;
        int emptyCount = 0;
        for (FormBuilderView.FieldMetaTemp f : rowFields) {
            if (f.colSpan != null && f.colSpan > 0) {
                explicitSum += f.colSpan;
            } else {
                emptyCount++;
            }
        }
        if (emptyCount == rowFields.size()) {
            return Math.max(1, rowFields.size());
        }
        return Math.max(1, explicitSum + emptyCount);
    }

    public static class RowLayoutConfigTemp {
        private final int cols;
        private final Map<FormBuilderView.FieldMetaTemp, Integer> fieldSpans = new HashMap<>();

        public RowLayoutConfigTemp(int cols) {
            this.cols = Math.max(1, cols);
        }

        public int getCols() {
            return cols;
        }

        public int getSpan(FormBuilderView.FieldMetaTemp field) {
            return fieldSpans.getOrDefault(field, 1);
        }

        public void setSpan(FormBuilderView.FieldMetaTemp field, int span) {
            fieldSpans.put(field, Math.max(1, Math.min(cols, span)));
        }
    }

    public static RowLayoutConfigTemp calculateRowConfigTemp(List<FormBuilderView.FieldMetaTemp> rowFields, int maxColsInForm) {
        if (rowFields == null || rowFields.isEmpty()) {
            return new RowLayoutConfigTemp(1);
        }

        int explicitSum = 0;
        int emptyCount = 0;
        List<FormBuilderView.FieldMetaTemp> emptyFields = new ArrayList<>();

        for (FormBuilderView.FieldMetaTemp f : rowFields) {
            if (f.colSpan != null && f.colSpan > 0) {
                explicitSum += f.colSpan;
            } else {
                emptyCount++;
                emptyFields.add(f);
            }
        }

        int rowCols;
        RowLayoutConfigTemp config;

        if (emptyCount == rowFields.size()) {
            rowCols = Math.max(1, rowFields.size());
            config = new RowLayoutConfigTemp(rowCols);
            for (FormBuilderView.FieldMetaTemp f : rowFields) {
                if ("TEXTAREA".equalsIgnoreCase(f.componentType) && rowFields.size() == 1) {
                    config.setSpan(f, rowCols);
                } else {
                    config.setSpan(f, 1);
                }
            }
            return config;
        }

        rowCols = Math.max(maxColsInForm, explicitSum + emptyCount);
        config = new RowLayoutConfigTemp(rowCols);

        for (FormBuilderView.FieldMetaTemp f : rowFields) {
            if (f.colSpan != null && f.colSpan > 0) {
                int span = f.colSpan;
                if ("TEXTAREA".equalsIgnoreCase(f.componentType) && span == 1 && rowFields.size() == 1 && rowCols > 1) {
                    span = rowCols;
                }
                config.setSpan(f, span);
            }
        }

        if (emptyCount > 0) {
            int remaining = Math.max(emptyCount, rowCols - explicitSum);
            int baseSpan = remaining / emptyCount;
            int remainder = remaining % emptyCount;

            for (int i = 0; i < emptyFields.size(); i++) {
                FormBuilderView.FieldMetaTemp emptyField = emptyFields.get(i);
                int span = baseSpan + (i < remainder ? 1 : 0);
                if ("TEXTAREA".equalsIgnoreCase(emptyField.componentType) && rowFields.size() == 1) {
                    span = rowCols;
                }
                config.setSpan(emptyField, span);
            }
        }

        return config;
    }

    /**
     * Menerapkan pengaturan ResponsiveSteps pada FormLayout berdasarkan jumlah kolom.
     */
    public static void applyResponsiveSteps(FormLayout formLayout, int cols) {
        if (formLayout == null) return;
        int c = Math.max(1, cols);
        if (c == 1) {
            formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        } else if (c <= 2) {
            formLayout.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("500px", c)
            );
        } else {
            formLayout.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("500px", Math.max(1, c / 2)),
                    new FormLayout.ResponsiveStep("800px", c)
            );
        }
    }
}
