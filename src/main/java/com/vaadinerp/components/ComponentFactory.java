package com.vaadinerp.components;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.service.DynamicDataService;

public class ComponentFactory {

    public static Component create(FieldMeta field, DynamicDataService dataService,
            BiConsumer<String, Object> updateFieldValue) {
        return create(field, dataService, updateFieldValue, false);
    }

    public static Component create(FieldMeta field, DynamicDataService dataService,
            BiConsumer<String, Object> updateFieldValue, boolean hideLabel) {
        Component component = createInternal(field, dataService, updateFieldValue, hideLabel);

        if (component instanceof com.vaadin.flow.component.HasValue) {
            boolean req = field.isRequired();
            if (component instanceof com.vaadin.flow.component.textfield.TextField) {
                ((com.vaadin.flow.component.textfield.TextField) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.textfield.IntegerField) {
                ((com.vaadin.flow.component.textfield.IntegerField) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.textfield.BigDecimalField) {
                ((com.vaadin.flow.component.textfield.BigDecimalField) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.datepicker.DatePicker) {
                ((com.vaadin.flow.component.datepicker.DatePicker) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.checkbox.Checkbox) {
                ((com.vaadin.flow.component.checkbox.Checkbox) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.textfield.TextArea) {
                ((com.vaadin.flow.component.textfield.TextArea) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.combobox.ComboBox) {
                ((com.vaadin.flow.component.combobox.ComboBox<?>) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.select.Select) {
                ((com.vaadin.flow.component.select.Select<?>) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.combobox.MultiSelectComboBox) {
                ((com.vaadin.flow.component.combobox.MultiSelectComboBox<?>) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadinerp.components.BandboxField) {
                ((com.vaadinerp.components.BandboxField<?, ?>) component).setRequiredIndicatorVisible(req);
            }
        }

        // Tambahkan event listener generic untuk SEMUA komponen 
        // Khusus untuk menangani deselection (nilai dihapus/null) atau transfer nilai untuk komponen standar
        if (component instanceof com.vaadin.flow.component.HasValue) {
            java.util.List<com.vaadinerp.meta.FieldLovTargetMeta> lovTargets = field.getLovTargets();
            if (lovTargets != null && !lovTargets.isEmpty() && updateFieldValue != null) {
                @SuppressWarnings("unchecked")
                com.vaadin.flow.component.HasValue<?, Object> hasValue = (com.vaadin.flow.component.HasValue<?, Object>) component;
                hasValue.addValueChangeListener(event -> {
                    // Hanya jalankan mapping jika perubahan dipicu oleh user (dari client), 
                    // kecuali untuk BandboxField yang merupakan CustomField (selalu bernilai isFromClient = false di server-side)
                    if (!event.isFromClient() && !(component instanceof com.vaadinerp.components.BandboxField)) {
                        return;
                    }
                    if (event.getValue() == null || event.getValue().toString().isEmpty()) {
                        // Jika komponen di-deselect (nilainya dikosongkan), kosongkan juga semua targetnya
                        for (com.vaadinerp.meta.FieldLovTargetMeta target : lovTargets) {
                            updateFieldValue.accept(target.getTargetField(), null);
                        }
                    } else {
                        if ("BANDBOX".equalsIgnoreCase(field.getComponentType())) {
                            com.vaadinerp.meta.LovMeta lovMeta = dataService.getLovMeta(field.getLovCode()).orElse(null);
                            if (lovMeta != null) {
                                BandboxField<?, ?> bandbox = (BandboxField<?, ?>) component;
                                Object selectedVal = event.getValue();
                                Map<String, Object> row = null;
                                if (bandbox.getSelectedItem() != null) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> item = (Map<String, Object>) bandbox.getSelectedItem();
                                    String valCol = lovMeta.getValueColumn();
                                    if (item.get(valCol) != null && item.get(valCol).toString().equals(selectedVal.toString())) {
                                        row = item;
                                    }
                                }
                                if (row == null) {
                                    // Programmatic set, fetch dari DB
                                    row = dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(), selectedVal);
                                }
                                if (row != null) {
                                    for (com.vaadinerp.meta.FieldLovTargetMeta target : lovTargets) {
                                        String srcLovCol = target.getSourceColumn();
                                        String targetFormField = target.getTargetField();
                                        Object val = row.get(srcLovCol);
                                        updateFieldValue.accept(targetFormField, val);
                                    }
                                }
                            }
                        } else {
                            // Komponen standar (textbox, combobox, dll)
                            for (com.vaadinerp.meta.FieldLovTargetMeta target : lovTargets) {
                                String action = target.getActionType();
                                if ("QUERY_LOV".equalsIgnoreCase(action)) {
                                    String targetFieldName = target.getTargetField();
                                    if (field.getFormMeta() != null && field.getFormMeta().getFields() != null) {
                                        FieldMeta targetFieldMeta = field.getFormMeta().getFields().stream()
                                            .filter(f -> f.getFieldName().equalsIgnoreCase(targetFieldName))
                                            .findFirst().orElse(null);
                                        if (targetFieldMeta != null && "BANDBOX".equalsIgnoreCase(targetFieldMeta.getComponentType())) {
                                            String targetLovCode = targetFieldMeta.getLovCode();
                                            com.vaadinerp.meta.LovMeta targetLovMeta = dataService.getLovMeta(targetLovCode).orElse(null);
                                            if (targetLovMeta != null) {
                                                String lookupCol = target.getLookupColumn();
                                                if (lookupCol != null && !lookupCol.isEmpty()) {
                                                    // Query exact match pada tabel LOV target
                                                    Map<String, Object> match = dataService.fetchLovRecord(targetLovMeta.getTableName(), lookupCol, event.getValue());
                                                    if (match != null) {
                                                        Object val = match.get(targetLovMeta.getValueColumn());
                                                        updateFieldValue.accept(targetFieldName, val);
                                                    } else {
                                                        // Reset target jika tidak ada yang cocok
                                                        updateFieldValue.accept(targetFieldName, null);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Default/COPY
                                    updateFieldValue.accept(target.getTargetField(), event.getValue());
                                }
                            }
                        }
                    }
                });
            }
        }

        return component;
    }

    private static Component createInternal(FieldMeta field, DynamicDataService dataService,
            BiConsumer<String, Object> updateFieldValue, boolean hideLabel) {
        String label = hideLabel ? "" : field.getFieldLabel();
        String type = field.getComponentType();
        if (type == null)
            type = "TEXTBOX";

        switch (type.toUpperCase()) {
            case "TEXTBOX":
                TextField textField = new TextField(label);
                textField.setReadOnly(field.isReadonly());
                textField.setRequiredIndicatorVisible(field.isRequired());
                return textField;
            case "INTBOX":
                IntegerField intField = new IntegerField(label);
                intField.setReadOnly(field.isReadonly());
                return intField;
            case "DECIMALBOX":
                BigDecimalField decimalField = new BigDecimalField(label);
                decimalField.setReadOnly(field.isReadonly());
                return decimalField;
            case "DATEBOX":
                DatePicker datePicker = new DatePicker(label);
                datePicker.setReadOnly(field.isReadonly());
                return datePicker;
            case "CHECKBOX":
                Checkbox checkbox = new Checkbox(label);
                checkbox.setReadOnly(field.isReadonly());
                return checkbox;
            case "TEXTAREA":
                TextArea textArea = new TextArea(label);
                textArea.setReadOnly(field.isReadonly());
                return textArea;
            case "COMBOBOX":
                if (field.getLovCode() != null && !field.getLovCode().trim().isEmpty()) {
                    LovComboBox lovCombo = new LovComboBox(label, field.getLovCode(), dataService);
                    lovCombo.setReadOnly(field.isReadonly());
                    return lovCombo;
                }
                ComboBox<String> comboBox = new ComboBox<>(label);
                comboBox.setReadOnly(field.isReadonly());
                List<String> comboItems = Arrays.asList("A Option A", "A Optionx A", "B Option B", "C Option C");
                comboBox.setItems(comboItems);

                // Aktifkan tombol 'X' untuk menghapus pilihan jika dibutuhkan
                comboBox.setClearButtonVisible(true);

                // (Opsional) Teks bantuan abu-abu di dalam input
                comboBox.setPlaceholder("Ketik untuk mencari...");

                // Auto-select match pertama saat Enter ditekan (berkat fitur Custom Value)
                comboBox.setAllowCustomValue(true);
                comboBox.addCustomValueSetListener(e -> {
                    String typedValue = e.getDetail().toLowerCase();
                    comboItems.stream()
                            .filter(item -> item.toLowerCase().contains(typedValue))
                            .findFirst()
                            .ifPresent(comboBox::setValue);
                });

                return comboBox;

            case "LISTBOX":
                if (field.getLovCode() != null && !field.getLovCode().trim().isEmpty()) {
                    LovSelect lovSel = new LovSelect(label, field.getLovCode(), dataService);
                    lovSel.setReadOnly(field.isReadonly());
                    return lovSel;
                }
                Select<String> select = new Select<>();
                select.setLabel(label);
                select.setEnabled(!field.isReadonly());
                select.setItems("Item A", "Item B", "Item C");
                select.setPlaceholder("Pilih item...");

                // Custom filter untuk case-insensitive
                // select.setFilter((item, filterText) ->
                // item.toLowerCase().contains(filterText.toLowerCase()));
                return select;
            case "CHOSENBOX":
                MultiSelectComboBox<String> chosenBox = new MultiSelectComboBox<>(label);
                chosenBox.setReadOnly(field.isReadonly());
                chosenBox.setItems("Tag 1", "Tag 2", "Tag 3");
                return chosenBox;
            case "BANDBOX":
                BandboxField<Map<String, Object>, Object> bandbox = new BandboxField<>(label);
                String lovCode = field.getLovCode();

                com.vaadinerp.meta.LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
                if (lovMeta != null) {
                    // 1. Dinamis menambahkan kolom ke Grid berdasarkan gridColumns (misal:
                    // "dept_code:Kode:100px,dept_name:Nama:200px")
                    String gridColsStr = lovMeta.getGridColumns();
                    if (gridColsStr != null && !gridColsStr.isBlank()) {
                        String[] colDefs = gridColsStr.split(",");
                        for (String colDef : colDefs) {
                            String[] parts = colDef.split(":");
                            String colName = parts[0];
                            String colHeader = parts.length > 1 ? parts[1] : colName;
                            String colWidth = parts.length > 2 ? parts[2] : "150px";

                            bandbox.getGrid()
                                    .addColumn(row -> row.get(colName) != null ? row.get(colName).toString() : "")
                                    .setHeader(colHeader)
                                    .setWidth(colWidth);
                        }
                    } else {
                        bandbox.getGrid()
                                .addColumn(row -> row.get(lovMeta.getValueColumn()) != null
                                        ? row.get(lovMeta.getValueColumn()).toString()
                                        : "")
                                .setHeader("Kode");
                        bandbox.getGrid()
                                .addColumn(row -> row.get(lovMeta.getLabelColumn()) != null
                                        ? row.get(lovMeta.getLabelColumn()).toString()
                                        : "")
                                .setHeader("Nama");
                    }

                    // 2. Konfigurasi Fetch Data (Filter) secara dinamis dari database
                    String searchCol = lovMeta.getSearchColumn();
                    bandbox.setDataFetchCallback(keyword -> {
                        return dataService.fetchLovDataWithFilters(lovMeta.getTableName(), searchCol, keyword, bandbox.getActiveFilters());
                    });

                    // Item Finder untuk memulihkan record berdasarkan value/key-nya
                    bandbox.setItemFinder(val -> {
                        return dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(), val);
                    });

                    // 3. Value Generator (ID untuk disimpan ke database)
                    String valCol = lovMeta.getValueColumn();
                    bandbox.setItemValueGenerator(row -> row.get(valCol));

                    // 4. Display Label Generator
                    String lblCol = lovMeta.getLabelColumn();
                    bandbox.setItemLabelGenerator(row -> row.get(lblCol) != null ? row.get(lblCol).toString() : "");
                } else {
                    // Fallback static jika LovMeta tidak ditemukan di DB
                    bandbox.getGrid().addColumn(row -> row.get("code") != null ? row.get("code").toString() : "")
                            .setHeader("Kode");
                    bandbox.getGrid().addColumn(row -> row.get("name") != null ? row.get("name").toString() : "")
                            .setHeader("Nama");
                }

                return bandbox;
            default:
                return new TextField(field.getFieldLabel());
        }
    }
}
