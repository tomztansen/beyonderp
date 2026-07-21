package com.vaadinerp.components;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.service.StandardFormatService;

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
            } else if (component instanceof com.vaadin.flow.component.textfield.IntegerField
                    || component instanceof FormattedIntegerField) {
                if (component instanceof com.vaadin.flow.component.textfield.IntegerField i)
                    i.setRequiredIndicatorVisible(req);
                else
                    ((FormattedIntegerField) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadin.flow.component.textfield.BigDecimalField
                    || component instanceof FormattedBigDecimalField) {
                if (component instanceof com.vaadin.flow.component.textfield.BigDecimalField b)
                    b.setRequiredIndicatorVisible(req);
                else
                    ((FormattedBigDecimalField) component).setRequiredIndicatorVisible(req);
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
                ((com.vaadin.flow.component.combobox.MultiSelectComboBox<?>) component)
                        .setRequiredIndicatorVisible(req);
            } else if (component instanceof com.vaadinerp.components.BandboxField) {
                ((com.vaadinerp.components.BandboxField<?, ?>) component).setRequiredIndicatorVisible(req);
            } else if (component instanceof SubformGridField) {
                ((SubformGridField) component).setRequiredIndicatorVisible(req);
            }
        }

        // Tambahkan event listener generic untuk SEMUA komponen
        // Khusus untuk menangani deselection (nilai dihapus/null) atau transfer nilai
        // untuk komponen standar
        if (component instanceof com.vaadin.flow.component.HasValue) {
            java.util.List<com.vaadinerp.meta.FieldLovTargetMeta> lovTargets = field.getLovTargets();
            if (lovTargets != null && !lovTargets.isEmpty() && updateFieldValue != null) {
                @SuppressWarnings("unchecked")
                com.vaadin.flow.component.HasValue<?, Object> hasValue = (com.vaadin.flow.component.HasValue<?, Object>) component;
                hasValue.addValueChangeListener(event -> {
                    if (event.getValue() == null || event.getValue().toString().isEmpty()) {
                        for (com.vaadinerp.meta.FieldLovTargetMeta target : lovTargets) {
                            updateFieldValue.accept(target.getTargetField(), null);
                        }
                    } else {
                        Object selectedVal = event.getValue();
                        Map<String, Object> row = null;

                        if (component instanceof com.vaadinerp.components.BandboxField) {
                            BandboxField<?, ?> bandbox = (BandboxField<?, ?>) component;
                            if (bandbox.getSelectedItem() != null) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> item = (Map<String, Object>) bandbox.getSelectedItem();
                                row = item;
                            }
                        } else if (component instanceof com.vaadinerp.components.LovComboBox lovCombo) {
                            row = lovCombo.getSelectedRecord();
                        } else if (component instanceof com.vaadinerp.components.LovSelect lovSel) {
                            row = lovSel.getSelectedRecord();
                        }

                        com.vaadinerp.meta.LovMeta lovMeta = null;
                        if (field.getLovCode() != null && !field.getLovCode().trim().isEmpty()) {
                            lovMeta = dataService.getLovMeta(field.getLovCode()).orElse(null);
                        }

                        if (row == null && lovMeta != null) {
                            row = dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(),
                                    selectedVal);
                        }

                        for (com.vaadinerp.meta.FieldLovTargetMeta target : lovTargets) {
                            String action = target.getActionType();
                            if ("QUERY_LOV".equalsIgnoreCase(action)) {
                                String targetFieldName = target.getTargetField();
                                if (field.getFormMeta() != null && field.getFormMeta().getFields() != null) {
                                    FieldMeta targetFieldMeta = field.getFormMeta().getFields().stream()
                                            .filter(f -> f.getFieldName().equalsIgnoreCase(targetFieldName))
                                            .findFirst().orElse(null);
                                    if (targetFieldMeta != null && targetFieldMeta.getLovCode() != null) {
                                        com.vaadinerp.meta.LovMeta targetLovMeta = dataService
                                                .getLovMeta(targetFieldMeta.getLovCode()).orElse(null);
                                        if (targetLovMeta != null) {
                                            String lookupCol = target.getLookupColumn();
                                            if (lookupCol != null && !lookupCol.isEmpty()) {
                                                Map<String, Object> match = dataService.fetchLovRecord(
                                                        targetLovMeta.getTableName(), lookupCol, selectedVal);
                                                if (match != null) {
                                                    Object val = match.get(targetLovMeta.getValueColumn());
                                                    updateFieldValue.accept(targetFieldName, val);
                                                } else {
                                                    updateFieldValue.accept(targetFieldName, null);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // COPY action
                                Object valToSet = selectedVal;
                                if (target.getSourceColumn() != null) {
                                    String srcCol = target.getSourceColumn().trim();
                                    if ("_label".equalsIgnoreCase(srcCol) || "label".equalsIgnoreCase(srcCol)
                                            || (field.getFieldName() + "_label").equalsIgnoreCase(srcCol)) {
                                        if (component instanceof com.vaadinerp.components.BandboxField<?, ?> bandbox) {
                                            valToSet = bandbox.getDisplayLabel();
                                        } else if (component instanceof com.vaadinerp.components.LovComboBox lovCombo) {
                                            valToSet = lovCombo.getDisplayLabel();
                                        } else if (component instanceof com.vaadinerp.components.LovSelect lovSel) {
                                            valToSet = lovSel.getDisplayLabel();
                                        } else if (component instanceof com.vaadinerp.components.LovChosenBox lovChosen) {
                                            valToSet = lovChosen.getDisplayLabel();
                                        } else if (row != null && lovMeta != null && lovMeta.getLabelColumn() != null) {
                                            valToSet = row.get(lovMeta.getLabelColumn());
                                        }
                                    } else if (row != null) {
                                        valToSet = row.get(srcCol);
                                        if (valToSet == null) {
                                            for (Map.Entry<String, Object> entry : row.entrySet()) {
                                                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(srcCol)) {
                                                    valToSet = entry.getValue();
                                                    break;
                                                }
                                            }
                                        }
                                        if (valToSet == null && srcCol.toLowerCase().endsWith("_label")
                                                && srcCol.length() > 6) {
                                            String baseCol = srcCol.substring(0, srcCol.length() - 6);
                                            Object baseVal = getCaseInsensitiveVal(row, baseCol);
                                            if (baseVal != null && !baseVal.toString().trim().isEmpty()
                                                    && field.getLovCode() != null && dataService != null) {
                                                com.vaadinerp.meta.FormMeta sourceForm = dataService
                                                        .getFormMetaRepository().findById(field.getLovCode().trim())
                                                        .orElse(null);
                                                if (sourceForm == null)
                                                    sourceForm = dataService.getFormMetaRepository()
                                                            .findById(field.getLovCode().trim().toLowerCase())
                                                            .orElse(null);
                                                if (sourceForm == null)
                                                    sourceForm = dataService.getFormMetaRepository()
                                                            .findById(field.getLovCode().trim().toUpperCase())
                                                            .orElse(null);
                                                if (sourceForm == null) {
                                                    com.vaadinerp.meta.LovMeta lm = dataService
                                                            .getLovMeta(field.getLovCode().trim()).orElse(null);
                                                    if (lm != null && lm.getTableName() != null) {
                                                        sourceForm = dataService.getFormMetaRepository()
                                                                .findById(lm.getTableName().trim()).orElse(null);
                                                    }
                                                }
                                                if (sourceForm != null && sourceForm.getFields() != null) {
                                                    com.vaadinerp.meta.FieldMeta childField = sourceForm.getFields()
                                                            .stream()
                                                            .filter(f -> f.getFieldName() != null
                                                                    && f.getFieldName().equalsIgnoreCase(baseCol))
                                                            .findFirst().orElse(null);
                                                    if (childField != null && childField.getLovCode() != null) {
                                                        valToSet = formatFieldValueWithLov(childField, baseVal,
                                                                dataService);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                updateFieldValue.accept(target.getTargetField(), valToSet);
                            }
                        }
                    }
                });
            }
        }

        // === APPLY METADATA VALIDATION RULES & DEFAULT SAFEGUARDS ===
        if (component instanceof com.vaadin.flow.component.HasValue<?, ?>) {
            @SuppressWarnings("unchecked")
            com.vaadin.flow.component.HasValue<?, Object> valComp = (com.vaadin.flow.component.HasValue<?, Object>) component;
            valComp.addValueChangeListener(ev -> validateFieldRule(field, component));
        }

        if (hideLabel) {
            if (component instanceof com.vaadin.flow.component.HasSize hasSize) {
                hasSize.setWidthFull();
            }
            component.getElement().getStyle()
                    .set("min-width", "0")
                    .set("max-width", "100%")
                    .set("width", "100%")
                    .set("box-sizing", "border-box")
                    .set("overflow", "hidden");
        }

        return component;
    }

    public static boolean validateFieldRule(FieldMeta field, Component component) {
        if (!(component instanceof com.vaadin.flow.component.HasValue<?, ?> valComp)) {
            return true;
        }
        Object val = valComp.getValue();
        String rawRule = field.getValidationRule();
        boolean hasExplicitRule = (rawRule != null && !rawRule.trim().isEmpty() && !rawRule.equalsIgnoreCase("NONE"));

        boolean isInvalid = false;
        String errMsg = "";
        String customErrMsg = null;

        // Default Security Safeguard: Prevent Buffer/Memory Overflow if input string
        // exceeds default limit
        if (val != null) {
            String strVal = val.toString();
            boolean hasMaxLenRule = (hasExplicitRule && rawRule != null && rawRule.toUpperCase().contains("MAX_LEN:"));
            if (!hasMaxLenRule && strVal.length() > 40000) {
                isInvalid = true;
                errMsg = "Panjang teks maksimal adalah 40000 karakter (batas default sistem)!";
            }
        }

        if (!hasExplicitRule && !isInvalid) {
            if (component instanceof com.vaadin.flow.component.HasValidation hv && hv.isInvalid()) {
                hv.setInvalid(false);
            }
            return true;
        }

        if (!isInvalid && hasExplicitRule && rawRule != null) {
            String rule = rawRule;
            int pipeIdx = rule.indexOf('|');
            if (pipeIdx > 0) {
                customErrMsg = rule.substring(pipeIdx + 1).trim();
                rule = rule.substring(0, pipeIdx).trim();
            }

            if (val == null || val.toString().trim().isEmpty()) {
                if ("NOT_BLANK".equalsIgnoreCase(rule)) {
                    isInvalid = true;
                    errMsg = "Kolom ini tidak boleh kosong!";
                }
            } else {
                String strVal = val.toString().trim();

                // STRING / TEXT RULES
                if (rule.equalsIgnoreCase("EMAIL")) {
                    if (!strVal.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        isInvalid = true;
                        errMsg = "Format alamat email tidak valid!";
                    }
                } else if (rule.equalsIgnoreCase("ALPHANUMERIC")) {
                    if (!strVal.matches("^[a-zA-Z0-9 ]+$")) {
                        isInvalid = true;
                        errMsg = "Hanya boleh huruf dan angka!";
                    }
                } else if (rule.toUpperCase().startsWith("REGEX:")) {
                    String regex = rule.substring(6).trim();
                    try {
                        if (strVal.length() > 2000) {
                            isInvalid = true;
                            errMsg = "Input terlalu panjang untuk dievaluasi pola regex (maksimal 2000 karakter)!";
                        } else if (!strVal.matches(regex)) {
                            isInvalid = true;
                            errMsg = "Format input tidak sesuai!";
                        }
                    } catch (java.util.regex.PatternSyntaxException e) {
                        System.err.println(">>> REGEX SYNTAX ERROR in field [" + field.getFieldName() + "]: " + regex);
                    } catch (Exception e) {
                        System.err.println(">>> ERROR EVALUATING REGEX in field [" + field.getFieldName() + "]: "
                                + e.getMessage());
                    }
                } else if (rule.toUpperCase().startsWith("MIN_LEN:")) {
                    try {
                        int min = Integer.parseInt(rule.substring(8).trim());
                        if (strVal.length() < min) {
                            isInvalid = true;
                            errMsg = "Minimal " + min + " karakter!";
                        }
                    } catch (Exception ignored) {
                    }
                } else if (rule.toUpperCase().startsWith("MAX_LEN:")) {
                    try {
                        int max = Integer.parseInt(rule.substring(8).trim());
                        if (strVal.length() > max) {
                            isInvalid = true;
                            errMsg = "Maksimal " + max + " karakter!";
                        }
                    } catch (Exception ignored) {
                    }
                } else if (rule.toUpperCase().startsWith("DISALLOW:")) {
                    String disallowed = rule.substring(9).trim();
                    if (strVal.equalsIgnoreCase(disallowed)) {
                        isInvalid = true;
                        errMsg = "Pilihan '" + disallowed + "' tidak diperbolehkan!";
                    }
                }

                // NUMBER RULES
                double d = Double.NaN;
                if (val instanceof Number num) {
                    d = num.doubleValue();
                } else {
                    try {
                        d = Double.parseDouble(strVal.replace(".", "").replace(",", "."));
                    } catch (Exception ignored) {
                    }
                }

                if (!Double.isNaN(d)) {
                    if (rule.equalsIgnoreCase("POSITIVE_NUM") && d <= 0) {
                        isInvalid = true;
                        errMsg = "Angka harus lebih besar dari 0!";
                    } else if (rule.equalsIgnoreCase("NON_NEGATIVE") && d < 0) {
                        isInvalid = true;
                        errMsg = "Angka tidak boleh negatif!";
                    } else if (rule.toUpperCase().startsWith("MIN:")) {
                        try {
                            double min = Double.parseDouble(rule.substring(4).trim());
                            if (d < min) {
                                isInvalid = true;
                                errMsg = "Nilai minimal adalah " + (long) min;
                            }
                        } catch (Exception ignored) {
                        }
                    } else if (rule.toUpperCase().startsWith("MAX:")) {
                        try {
                            double max = Double.parseDouble(rule.substring(4).trim());
                            if (d > max) {
                                isInvalid = true;
                                errMsg = "Nilai maksimal adalah " + (long) max;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                // DATE RULES
                java.time.LocalDate date = null;
                if (val instanceof java.time.LocalDate ld)
                    date = ld;
                else if (val instanceof java.time.LocalDateTime ldt)
                    date = ldt.toLocalDate();

                if (date != null) {
                    java.time.DayOfWeek dow = date.getDayOfWeek();
                    if (rule.equalsIgnoreCase("ONLY_SUNDAY") && dow != java.time.DayOfWeek.SUNDAY) {
                        isInvalid = true;
                        errMsg = "Tanggal wajib jatuh pada hari Minggu!";
                    } else if (rule.equalsIgnoreCase("NOT_SUNDAY") && dow == java.time.DayOfWeek.SUNDAY) {
                        isInvalid = true;
                        errMsg = "Tanggal tidak boleh jatuh pada hari Minggu!";
                    } else if (rule.equalsIgnoreCase("WEEKDAYS")
                            && (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY)) {
                        isInvalid = true;
                        errMsg = "Tanggal wajib hari kerja (Senin-Jumat)!";
                    } else if (rule.equalsIgnoreCase("WEEKEND")
                            && (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY)) {
                        isInvalid = true;
                        errMsg = "Tanggal wajib akhir pekan (Sabtu/Minggu)!";
                    } else if (rule.equalsIgnoreCase("PAST_DATE") && date.isAfter(java.time.LocalDate.now())) {
                        isInvalid = true;
                        errMsg = "Tanggal tidak boleh mendahului hari ini!";
                    } else if (rule.equalsIgnoreCase("FUTURE_DATE") && date.isBefore(java.time.LocalDate.now())) {
                        isInvalid = true;
                        errMsg = "Tanggal harus di masa depan!";
                    }
                }
            }
        }

        if (isInvalid && customErrMsg != null && !customErrMsg.isEmpty()) {
            errMsg = customErrMsg;
        }

        if (component instanceof com.vaadin.flow.component.HasValidation hvComp) {
            hvComp.setInvalid(isInvalid);
            hvComp.setErrorMessage(errMsg);
        }
        return !isInvalid;
    }

    private static final java.util.Map<String, java.util.Map<String, String>> lovLabelCache = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clearLovCache(String lovCode) {
        if (lovCode != null && !lovCode.trim().isEmpty()) {
            lovLabelCache.remove(lovCode.trim());
        } else {
            lovLabelCache.clear();
        }
    }

    public static String formatFieldValueWithLov(FieldMeta field, Object val,
            com.vaadinerp.service.DynamicDataService dataService) {
        if (val == null)
            return "";
        String strVal = val.toString().trim();
        if (strVal.isEmpty())
            return "";
        if (field != null && field.getLovCode() != null && !field.getLovCode().trim().isEmpty()
                && dataService != null) {
            String lovCode = field.getLovCode().trim();
            java.util.Map<String, String> map = lovLabelCache.computeIfAbsent(lovCode,
                    code -> new java.util.concurrent.ConcurrentHashMap<>());
            if (strVal.contains(",")) {
                return java.util.Arrays.stream(strVal.split(","))
                        .map(String::trim)
                        .map(item -> {
                            if (map.containsKey(item))
                                return map.get(item);
                            return fetchSingleLovLabel(lovCode, item, dataService);
                        })
                        .collect(java.util.stream.Collectors.joining(", "));
            }
            if (map.containsKey(strVal)) {
                return map.get(strVal);
            }
            return fetchSingleLovLabel(lovCode, strVal, dataService);
        }
        return formatFieldValue(field, val);
    }

    private static String fetchSingleLovLabel(String lovCode, String val,
            com.vaadinerp.service.DynamicDataService dataService) {
        com.vaadinerp.meta.LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
        if (lovMeta != null) {
            Map<String, Object> rec = dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(), val);
            if (rec != null) {
                String valCol = lovMeta.getValueColumn() != null && !lovMeta.getValueColumn().isBlank()
                        ? lovMeta.getValueColumn().trim()
                        : "id";
                String lblCol = lovMeta.getLabelColumn() != null && !lovMeta.getLabelColumn().isBlank()
                        ? lovMeta.getLabelColumn().trim()
                        : valCol;
                Object l = getCaseInsensitiveVal(rec, lblCol);
                if (l == null || l.toString().trim().isEmpty()) {
                    if (getCaseInsensitiveVal(rec, "code") != null)
                        l = getCaseInsensitiveVal(rec, "code");
                    else if (getCaseInsensitiveVal(rec, "name") != null)
                        l = getCaseInsensitiveVal(rec, "name");
                    else
                        l = val;
                }
                java.util.Map<String, String> cache = lovLabelCache.get(lovCode);
                if (cache != null && l != null) {
                    cache.put(val, l.toString().trim());
                }
                return l != null ? l.toString().trim() : val;
            }
        }
        java.util.Map<String, String> cache = lovLabelCache.get(lovCode);
        if (cache != null && val != null) {
            cache.put(val, val);
        }
        return val;
    }

    private static Object getCaseInsensitiveVal(Map<String, Object> map, String key) {
        if (map == null || key == null)
            return null;
        if (map.containsKey(key))
            return map.get(key);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key))
                return e.getValue();
        }
        return null;
    }

    public static String formatFieldValue(FieldMeta field, Object val) {
        if (val == null)
            return "";
        String pattern = field != null ? field.getDisplayFormat() : null;
        boolean hasCustomFormat = pattern != null && !pattern.trim().isEmpty() && !pattern.equalsIgnoreCase("NONE");
        if (pattern != null && hasCustomFormat) {
            pattern = pattern.trim();
        } else {
            pattern = "";
        }

        String compType = field != null && field.getComponentType() != null ? field.getComponentType().toUpperCase()
                : "";
        if (!hasCustomFormat) {
            if ("DATEBOX".equals(compType) || val instanceof java.time.LocalDate || val instanceof java.sql.Date) {
                pattern = StandardFormatService.getStandardFormat("DATEBOX", "dd/MM/yyyy");
            } else if ("DATETIMEBOX".equals(compType) || val instanceof java.time.LocalDateTime
                    || val instanceof java.sql.Timestamp) {
                pattern = StandardFormatService.getStandardFormat("DATETIMEBOX", "dd/MM/yyyy HH:mm");
            } else if ("TIMEBOX".equals(compType) || val instanceof java.time.LocalTime
                    || val instanceof java.sql.Time) {
                pattern = StandardFormatService.getStandardFormat("TIMEBOX", "HH:mm");
            } else if ("INTBOX".equals(compType) || val instanceof Integer || val instanceof Long
                    || val instanceof Short) {
                pattern = StandardFormatService.getStandardFormat("INTBOX", "#,##0");
            } else if ("DECIMALBOX".equals(compType) || val instanceof Double || val instanceof Float
                    || val instanceof java.math.BigDecimal) {
                pattern = StandardFormatService.getStandardFormat("DECIMALBOX", "#,##0.00");
            }
            hasCustomFormat = pattern != null && !pattern.isEmpty();
        }

        // Date formatting
        if (val instanceof java.time.LocalDateTime ldt) {
            String fmt = hasCustomFormat ? pattern : "dd/MM/yyyy HH:mm";
            try {
                return ldt.format(java.time.format.DateTimeFormatter.ofPattern(fmt));
            } catch (Exception e) {
                return ldt.toString();
            }
        }
        if (val instanceof java.sql.Timestamp ts) {
            String fmt = hasCustomFormat ? pattern : "dd/MM/yyyy HH:mm";
            try {
                return ts.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern(fmt));
            } catch (Exception e) {
                return ts.toString();
            }
        }
        if (val instanceof java.time.LocalDate ld) {
            String fmt = hasCustomFormat ? pattern : "dd/MM/yyyy";
            try {
                return ld.format(java.time.format.DateTimeFormatter.ofPattern(fmt));
            } catch (Exception e) {
                return ld.toString();
            }
        }
        if (val instanceof java.sql.Date sd) {
            String fmt = hasCustomFormat ? pattern : "dd/MM/yyyy";
            try {
                return sd.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern(fmt));
            } catch (Exception e) {
                return sd.toString();
            }
        }
        if (val instanceof java.time.LocalTime lt) {
            String fmt = hasCustomFormat ? pattern : "HH:mm";
            try {
                return lt.format(java.time.format.DateTimeFormatter.ofPattern(fmt));
            } catch (Exception e) {
                return lt.toString();
            }
        }
        if (val instanceof java.sql.Time st) {
            String fmt = hasCustomFormat ? pattern : "HH:mm";
            try {
                return st.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern(fmt));
            } catch (Exception e) {
                return st.toString();
            }
        }

        // Check string parsing for dates if string looks like date
        if (val instanceof String sVal && hasCustomFormat) {
            if (sVal.matches("\\d{4}-\\d{2}-\\d{2}")) {
                try {
                    return java.time.LocalDate.parse(sVal)
                            .format(java.time.format.DateTimeFormatter.ofPattern(pattern));
                } catch (Exception ignored) {
                }
            }
            if (sVal.matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}.*")) {
                try {
                    return java.time.LocalDateTime.parse(sVal.replace(" ", "T").substring(0, 19))
                            .format(java.time.format.DateTimeFormatter.ofPattern(pattern));
                } catch (Exception ignored) {
                }
            }
        }

        // Numeric formatting
        if (val instanceof Number || (val instanceof String && hasCustomFormat)) {
            java.math.BigDecimal numVal = null;
            if (val instanceof Number num)
                numVal = new java.math.BigDecimal(num.toString());
            else if (hasCustomFormat) {
                try {
                    numVal = new java.math.BigDecimal(val.toString().replace(".", "").replace(",", "."));
                } catch (Exception ignored) {
                }
            }
            if (numVal != null && hasCustomFormat && pattern != null && !pattern.isEmpty()) {
                try {
                    boolean hasRp = pattern.startsWith("Rp ") || pattern.startsWith("Rp");
                    String cleanPattern = pattern;
                    if (hasRp)
                        cleanPattern = pattern.replace("Rp ", "").replace("Rp", "").trim();
                    java.util.Locale locale = java.util.Locale.of("id", "ID");
                    java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(locale);
                    java.text.DecimalFormat df = new java.text.DecimalFormat(cleanPattern, symbols);
                    String formatted = df.format(numVal);
                    return hasRp ? "Rp " + formatted : formatted;
                } catch (Exception ignored) {
                }
            }
        }

        return val.toString();
    }

    private static void applyNumberFormatting(Component comp, String fmt) {
        if (fmt == null || fmt.trim().isEmpty() || "NONE".equalsIgnoreCase(fmt))
            return;
        String cleanFmt = fmt.trim();
        com.vaadin.flow.component.html.Span prefix = null;
        com.vaadin.flow.component.html.Span suffix = null;

        if (cleanFmt.toUpperCase().startsWith("RP ") || cleanFmt.toUpperCase().startsWith("RP")) {
            prefix = new com.vaadin.flow.component.html.Span("Rp ");
            prefix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
        } else if (cleanFmt.startsWith("$") || cleanFmt.toUpperCase().startsWith("USD")) {
            prefix = new com.vaadin.flow.component.html.Span("$ ");
            prefix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
        }

        if (cleanFmt.endsWith("%")) {
            suffix = new com.vaadin.flow.component.html.Span("%");
            suffix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
        } else if (cleanFmt.toUpperCase().endsWith(" KG")) {
            suffix = new com.vaadin.flow.component.html.Span(" kg");
            suffix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
        } else if (cleanFmt.toUpperCase().endsWith(" PCS")) {
            suffix = new com.vaadin.flow.component.html.Span(" pcs");
            suffix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
        }

        if (comp instanceof IntegerField intField) {
            intField.setPlaceholder("");
            intField.getElement().setAttribute("allowed-char-pattern", "[0-9\\-+]");
            if (prefix != null)
                intField.setPrefixComponent(prefix);
            if (suffix != null)
                intField.setSuffixComponent(suffix);
        } else if (comp instanceof BigDecimalField decField) {
            decField.setPlaceholder("");
            decField.getElement().setAttribute("allowed-char-pattern", "[0-9\\-+.,eE]");
            if (prefix != null)
                decField.setPrefixComponent(prefix);
            if (suffix != null)
                decField.setSuffixComponent(suffix);
        } else if (comp instanceof TextField txtField) {
            txtField.setPlaceholder("");
            if (cleanFmt.contains("#") || cleanFmt.contains("0")) {
                txtField.setAllowedCharPattern("[0-9\\-+.,eE]");
            }
            if (prefix != null)
                txtField.setPrefixComponent(prefix);
            if (suffix != null)
                txtField.setSuffixComponent(suffix);
        }
    }

    private static DatePicker.DatePickerI18n createIndonesianDatePickerI18n(String dateFormat) {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setMonthNames(java.util.Arrays.asList("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
                "Agustus", "September", "Oktober", "November", "Desember"));
        i18n.setWeekdays(java.util.Arrays.asList("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"));
        i18n.setWeekdaysShort(java.util.Arrays.asList("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"));
        i18n.setToday("Hari Ini");
        i18n.setCancel("Batal");
        i18n.setDateFormat(dateFormat != null && !dateFormat.trim().isEmpty() ? dateFormat.trim() : "dd/MM/yyyy");
        return i18n;
    }

    private static Component createInternal(FieldMeta field, DynamicDataService dataService,
            BiConsumer<String, Object> updateFieldValue, boolean hideLabel) {
        String label = hideLabel ? "" : field.getFieldLabel();
        String type = field.getComponentType();
        if (type == null)
            type = "TEXTBOX";

        String fmt = field.getDisplayFormat();
        boolean hasFmt = fmt != null && !fmt.trim().isEmpty() && !fmt.equalsIgnoreCase("NONE");

        switch (type.toUpperCase()) {
            case "TEXTBOX":
            case "TEXTFIELD":
            case "TEXT":
            case "VARCHAR":
            case "STRING":
                TextField textField = new TextField(label);
                if (field.getSequenceCode() != null && !field.getSequenceCode().trim().isEmpty()) {
                    textField.setPlaceholder("⚡ [AUTO: " + field.getSequenceCode() + "]");
                    textField.setTooltipText("Nomor akan dibuat otomatis oleh sistem saat data disimpan (Sequence: "
                            + field.getSequenceCode() + ")");
                    textField.setReadOnly(true);
                } else {
                    textField.setReadOnly(field.isReadonly());
                }
                textField.setRequiredIndicatorVisible(field.isRequired());
                applyNumberFormatting(textField, fmt);
                return textField;
            case "INTBOX":
            case "INTEGERFIELD":
            case "INT":
            case "INTEGER":
            case "SERIAL":
                FormattedIntegerField intField = new FormattedIntegerField(label);
                intField.setReadOnly(field.isReadonly());
                intField.setDisplayFormat(hasFmt ? fmt : StandardFormatService.getStandardFormat("INTBOX", "#,##0"));
                return intField;
            case "DECIMALBOX":
            case "BIGDECIMALFIELD":
            case "DECIMAL":
            case "NUMERIC":
            case "FLOAT":
            case "DOUBLE":
            case "REAL":
            case "MONEY":
                FormattedBigDecimalField decimalField = new FormattedBigDecimalField(label);
                decimalField.setReadOnly(field.isReadonly());
                decimalField.setDisplayFormat(
                        hasFmt ? fmt : StandardFormatService.getStandardFormat("DECIMALBOX", "#,##0.00"));
                return decimalField;
            case "DATEBOX":
            case "DATEPICKER":
            case "DATE":
                DatePicker datePicker = new DatePicker(label);
                datePicker.setReadOnly(field.isReadonly());
                datePicker.setLocale(java.util.Locale.of("id", "ID"));
                datePicker.setI18n(createIndonesianDatePickerI18n(
                        hasFmt ? fmt : StandardFormatService.getStandardFormat("DATEBOX", "dd/MM/yyyy")));
                return datePicker;
            case "DATETIMEBOX":
            case "DATETIMEPICKER":
            case "TIMESTAMP":
            case "TIMESTAMPTZ":
                String dtFmt = hasFmt && fmt != null ? fmt
                        : StandardFormatService.getStandardFormat("DATETIMEBOX", "dd/MM/yyyy HH:mm");
                if (dtFmt == null)
                    dtFmt = "dd/MM/yyyy HH:mm";
                DateTimePicker dateTimePicker = new DateTimePicker(label);
                dateTimePicker.setReadOnly(field.isReadonly());
                dateTimePicker.setLocale(java.util.Locale.of("id", "ID"));
                dateTimePicker.setDatePickerI18n(
                        createIndonesianDatePickerI18n(dtFmt.trim().split(" ")[0]));
                return dateTimePicker;
            case "TIMEBOX":
            case "TIMEPICKER":
            case "TIME":
                TimePicker timePicker = new TimePicker(label);
                timePicker.setReadOnly(field.isReadonly());
                timePicker.setLocale(java.util.Locale.of("id", "ID"));
                return timePicker;
            case "CHECKBOX":
            case "BOOLEAN":
            case "BOOL":
                Checkbox checkbox = new Checkbox(label);
                checkbox.setReadOnly(field.isReadonly());
                checkbox.getStyle()
                        .set("margin", "0")
                        .set("padding", "0")
                        .set("cursor", "pointer")
                        .set("align-self", "flex-start");
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
                ComboBox<String> comboBox = new ComboBox<String>(label) {
                    private final java.util.List<String> items = new java.util.ArrayList<>();
                    {
                        setItems(new java.util.ArrayList<>(items));
                    }

                    @Override
                    public void setValue(String value) {
                        if (value != null && !value.isEmpty() && !items.contains(value)) {
                            items.add(value);
                            setItems(new java.util.ArrayList<>(items));
                        }
                        super.setValue(value);
                    }
                };
                comboBox.setReadOnly(field.isReadonly());

                // Aktifkan tombol 'X' untuk menghapus pilihan jika dibutuhkan
                comboBox.setClearButtonVisible(true);

                // (Opsional) Teks bantuan abu-abu di dalam input
                comboBox.setPlaceholder("Ketik untuk mencari...");

                // Auto-select atau tambahkan value saat Enter ditekan (berkat fitur Custom
                // Value)
                comboBox.setAllowCustomValue(true);
                comboBox.addCustomValueSetListener(e -> {
                    comboBox.setValue(e.getDetail());
                });

                return comboBox;

            case "LISTBOX":
                if (field.getLovCode() != null && !field.getLovCode().trim().isEmpty()) {
                    LovSelect lovSel = new LovSelect(label, field.getLovCode(), dataService);
                    lovSel.setReadOnly(field.isReadonly());
                    return lovSel;
                }
                Select<String> select = new Select<String>() {
                    private final java.util.List<String> items = new java.util.ArrayList<>();
                    {
                        setItems(new java.util.ArrayList<>(items));
                    }

                    @Override
                    public void setValue(String value) {
                        if (value != null && !value.isEmpty() && !items.contains(value)) {
                            items.add(value);
                            setItems(new java.util.ArrayList<>(items));
                        }
                        super.setValue(value);
                    }
                };
                select.setLabel(label);
                select.setEnabled(!field.isReadonly());
                select.setPlaceholder("Pilih item...");
                return select;
            case "CHOSENBOX":
                if (field.getLovCode() != null && !field.getLovCode().trim().isEmpty()) {
                    LovChosenBox lovChosen = new LovChosenBox(label, field.getLovCode(), dataService);
                    lovChosen.setReadOnly(field.isReadonly());
                    return lovChosen;
                }
                MultiSelectComboBox<String> chosenBox = new MultiSelectComboBox<String>(label) {
                    private final java.util.List<String> items = new java.util.ArrayList<>();
                    {
                        setItems(new java.util.ArrayList<>(items));
                    }

                    @Override
                    public void setValue(java.util.Set<String> values) {
                        if (values != null && !values.isEmpty()) {
                            boolean added = false;
                            for (String v : values) {
                                if (v != null && !v.isEmpty() && !items.contains(v)) {
                                    items.add(v);
                                    added = true;
                                }
                            }
                            if (added)
                                setItems(new java.util.ArrayList<>(items));
                        }
                        super.setValue(values);
                    }
                };
                chosenBox.setReadOnly(field.isReadonly());
                chosenBox.setAllowCustomValue(true);
                chosenBox.addCustomValueSetListener(e -> {
                    java.util.Set<String> current = new java.util.HashSet<>(chosenBox.getValue());
                    current.add(e.getDetail());
                    chosenBox.setValue(current);
                });
                return chosenBox;
            case "BANDBOX":
                BandboxField<Map<String, Object>, Object> bandbox = new BandboxField<>(label);
                String lovCode = field.getLovCode();

                com.vaadinerp.meta.LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
                if (lovMeta != null) {
                    com.vaadinerp.meta.FormMeta targetForm = dataService.getFormMetaRepository().findById(lovCode)
                            .orElse(null);
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

                            com.vaadinerp.meta.FieldMeta targetField = (targetForm != null
                                    && targetForm.getFields() != null)
                                            ? targetForm.getFields().stream()
                                                    .filter(f -> f.getFieldName().equalsIgnoreCase(colName))
                                                    .findFirst().orElse(null)
                                            : null;

                            com.vaadin.flow.component.grid.Grid.Column<Map<String, Object>> col = bandbox.getGrid()
                                    .addColumn(row -> {
                                        Object valObj = getCaseInsensitiveVal(row, colName);
                                        return formatFieldValueWithLov(targetField, valObj, dataService);
                                    })
                                    .setHeader(colHeader)
                                    .setAutoWidth(true)
                                    .setFlexGrow(1)
                                    .setResizable(true);

                            if (targetField != null) {
                                col.setSortable(targetField.isSortable());
                                col.setComparator((map1, map2) -> {
                                    Object val1 = getCaseInsensitiveVal(map1, colName);
                                    Object val2 = getCaseInsensitiveVal(map2, colName);
                                    if (val1 == null && val2 == null)
                                        return 0;
                                    if (val1 == null)
                                        return -1;
                                    if (val2 == null)
                                        return 1;
                                    String fLovCode = targetField.getLovCode();
                                    if (fLovCode != null && !fLovCode.trim().isEmpty()) {
                                        String s1 = formatFieldValueWithLov(targetField, val1, dataService);
                                        String s2 = formatFieldValueWithLov(targetField, val2, dataService);
                                        return s1.compareToIgnoreCase(s2);
                                    }
                                    if (val1 instanceof Comparable && val2 instanceof Comparable) {
                                        @SuppressWarnings("unchecked")
                                        Comparable<Object> comp1 = (Comparable<Object>) val1;
                                        return comp1.compareTo(val2);
                                    }
                                    return val1.toString().compareTo(val2.toString());
                                });
                            } else {
                                col.setWidth(colWidth);
                            }
                        }
                    } else {
                        List<String> allCols = dataService.getColumnsForQueryOrTable(lovMeta.getTableName());
                        if (allCols.isEmpty()) {
                            bandbox.getGrid()
                                    .addColumn(row -> {
                                        Object valObj = getCaseInsensitiveVal(row, lovMeta.getValueColumn());
                                        return valObj != null ? valObj.toString() : "";
                                    })
                                    .setHeader("Kode")
                                    .setAutoWidth(true).setResizable(true);
                            bandbox.getGrid()
                                    .addColumn(row -> {
                                        Object valObj = getCaseInsensitiveVal(row, lovMeta.getLabelColumn());
                                        return valObj != null ? valObj.toString() : "";
                                    })
                                    .setHeader("Nama")
                                    .setAutoWidth(true).setResizable(true);
                        } else {
                            for (String colName : allCols) {
                                String header = colName.substring(0, 1).toUpperCase()
                                        + colName.substring(1).replace("_", " ");
                                bandbox.getGrid()
                                        .addColumn(row -> {
                                            Object valObj = getCaseInsensitiveVal(row, colName);
                                            return valObj != null ? valObj.toString() : "";
                                        })
                                        .setHeader(header)
                                        .setAutoWidth(true).setResizable(true);
                            }
                        }
                    }

                    // 2. Konfigurasi Fetch Data (Filter) secara dinamis dari database
                    String searchCol = lovMeta.getSearchColumn();
                    bandbox.setDataFetchCallback(keyword -> {
                        return dataService.fetchLovDataWithFilters(lovMeta.getTableName(), searchCol, keyword,
                                bandbox.getActiveFilters().values());
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
                    bandbox.setItemLabelGenerator(row -> {
                        if (row == null)
                            return "";
                        Object val = row.get(lblCol);
                        if (val != null && !val.toString().trim().isEmpty()) {
                            return val.toString();
                        }
                        if (row.containsKey("code") && row.get("code") != null)
                            return row.get("code").toString();
                        if (row.containsKey("name") && row.get("name") != null)
                            return row.get("name").toString();
                        if (row.containsKey(lovMeta.getValueColumn()) && row.get(lovMeta.getValueColumn()) != null) {
                            return row.get(lovMeta.getValueColumn()).toString();
                        }
                        return row.values().stream().filter(java.util.Objects::nonNull).findFirst()
                                .map(Object::toString).orElse("");
                    });
                } else {
                    // Fallback static jika LovMeta tidak ditemukan di DB
                    bandbox.getGrid().addColumn(row -> row.get("code") != null ? row.get("code").toString() : "")
                            .setHeader("Kode");
                    bandbox.getGrid().addColumn(row -> row.get("name") != null ? row.get("name").toString() : "")
                            .setHeader("Nama");
                }

                return bandbox;
            case "SUBFORM_GRID":
                SubformGridField subformGrid = new SubformGridField(label, field, dataService);
                subformGrid.setReadOnly(field.isReadonly());
                return subformGrid;
            case "FILE_UPLOAD":
                FileUploadField fileUpload = new FileUploadField(label,
                        dataService != null ? dataService.getFileStorageService() : null, false);
                fileUpload.setReadOnly(field.isReadonly());
                return fileUpload;
            case "IMAGE_UPLOAD":
                FileUploadField imageUpload = new FileUploadField(label,
                        dataService != null ? dataService.getFileStorageService() : null, true);
                imageUpload.setReadOnly(field.isReadonly());
                return imageUpload;
            default:
                TextField defaultField = new TextField(label);
                defaultField.setReadOnly(field.isReadonly());
                defaultField.setRequiredIndicatorVisible(field.isRequired());
                return defaultField;
        }
    }

    public static void setComponentReadOnly(Component component, boolean ro) {
        if (component == null) return;
        if (component instanceof com.vaadin.flow.component.HasValueAndElement hve) {
            hve.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.select.Select<?> sel) {
            sel.setEnabled(!ro);
        } else if (component instanceof SubformGridField sg) {
            sg.setReadOnly(ro);
        } else if (component instanceof BandboxField bf) {
            bf.setReadOnly(ro);
        } else if (component instanceof FileUploadField fu) {
            fu.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.HasEnabled he) {
            he.setEnabled(!ro);
        }
    }

    public static void applyReadonlyMode(Component component, FieldMeta field, boolean isNewRecord) {
        if (component == null || field == null) return;
        boolean ro = field.isReadonlyFor(isNewRecord);

        // Jika auto-sequence aktif, jangan ubah read-only dari true ke false
        if (component instanceof com.vaadin.flow.component.textfield.TextField tf) {
            if (field.getSequenceCode() != null && !field.getSequenceCode().trim().isEmpty()) {
                tf.setReadOnly(true);
            } else {
                tf.setReadOnly(ro);
            }
        } else if (component instanceof FormattedIntegerField i) {
            i.setReadOnly(ro);
        } else if (component instanceof FormattedBigDecimalField b) {
            b.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.datepicker.DatePicker dp) {
            dp.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.datetimepicker.DateTimePicker dtp) {
            dtp.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.timepicker.TimePicker tp) {
            tp.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.checkbox.Checkbox cb) {
            cb.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.textfield.TextArea ta) {
            ta.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.combobox.ComboBox<?> cob) {
            cob.setReadOnly(ro);
        } else if (component instanceof com.vaadin.flow.component.select.Select<?> sel) {
            sel.setEnabled(!ro);
        } else if (component instanceof com.vaadin.flow.component.combobox.MultiSelectComboBox<?> mcb) {
            mcb.setReadOnly(ro);
        } else if (component instanceof SubformGridField sg) {
            sg.setReadOnly(ro);
        } else if (component instanceof BandboxField bf) {
            bf.setReadOnly(ro);
        } else if (component instanceof FileUploadField fu) {
            fu.setReadOnly(ro);
        }
    }
}
