package com.vaadinerp.components;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.service.DynamicDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LovChosenBox extends MultiSelectComboBox<String> {

    private final String lovCode;
    private final DynamicDataService dataService;
    private final Map<String, String> valueToLabelMap = new HashMap<>();
    private final Map<String, Map<String, Object>> valueToRecordMap = new HashMap<>();
    private final Map<String, FilterCondition> activeFilters = new HashMap<>();

    private final List<String> currentItems = new ArrayList<>();

    public LovChosenBox(String label, String lovCode, DynamicDataService dataService) {
        super(label);
        this.lovCode = lovCode;
        this.dataService = dataService;
        
        setItemLabelGenerator(val -> valueToLabelMap.getOrDefault(val, val));
        setClearButtonVisible(true);
        setPlaceholder("Pilih beberapa...");
        setWidthFull();
        getStyle().set("min-width", "0").set("max-width", "100%").set("box-sizing", "border-box");
        
        refreshItems();
    }

    private Object getCaseInsensitive(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        if (map.containsKey(key)) return map.get(key);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void setValue(java.util.Set<String> values) {
        if (values != null && !values.isEmpty()) {
            boolean added = false;
            for (String v : values) {
                if (v != null && !v.isEmpty() && !currentItems.contains(v)) {
                    currentItems.add(v);
                    if (!valueToLabelMap.containsKey(v)) {
                        LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
                        if (lovMeta != null) {
                            Map<String, Object> rec = dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(), v);
                            if (rec != null) {
                                valueToRecordMap.put(v, rec);
                            }
                            Object lblObj = getCaseInsensitive(rec, lovMeta.getLabelColumn());
                            if (lblObj != null) {
                                valueToLabelMap.put(v, lblObj.toString());
                            } else {
                                valueToLabelMap.put(v, v);
                            }
                        } else {
                            valueToLabelMap.put(v, v);
                        }
                    }
                    added = true;
                }
            }
            if (added) {
                setItems(new ArrayList<>(currentItems));
            }
        }
        super.setValue(values);
    }

    public void setFilterValue(FilterCondition condition) {
        if (condition.getValue() == null || condition.getValue().toString().trim().isEmpty()) {
            activeFilters.remove(condition.getFilterId());
        } else {
            activeFilters.put(condition.getFilterId(), condition);
        }
        refreshItems();
    }

    public void refreshItems() {
        LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
        if (lovMeta != null) {
            List<Map<String, Object>> records = dataService.fetchLovDataWithFilters(
                    lovMeta.getTableName(),
                    lovMeta.getSearchColumn(),
                    "",
                    activeFilters.values()
            );
            
            valueToLabelMap.clear();
            currentItems.clear();
            for (Map<String, Object> rec : records) {
                Object valObj = getCaseInsensitive(rec, lovMeta.getValueColumn());
                if (valObj == null && rec.containsKey("id")) valObj = rec.get("id");
                Object lblObj = getCaseInsensitive(rec, lovMeta.getLabelColumn());
                if (lblObj == null || lblObj.toString().trim().isEmpty()) {
                    if (getCaseInsensitive(rec, "code") != null) lblObj = getCaseInsensitive(rec, "code");
                    else if (getCaseInsensitive(rec, "name") != null) lblObj = getCaseInsensitive(rec, "name");
                }
                
                String val = valObj != null ? valObj.toString().trim() : "";
                String lbl = lblObj != null ? lblObj.toString().trim() : val;
                
                if (!val.isEmpty()) {
                    valueToLabelMap.put(val, lbl);
                    valueToRecordMap.put(val, rec);
                    currentItems.add(val);
                }
            }
            setItems(new ArrayList<>(currentItems));
        } else {
            currentItems.clear();
            setItems(new ArrayList<>());
        }
    }

    public String getDisplayLabel() {
        java.util.Set<String> vals = getValue();
        if (vals == null || vals.isEmpty()) return "";
        List<String> labels = new ArrayList<>();
        for (String val : vals) {
            labels.add(valueToLabelMap.getOrDefault(val, val));
        }
        return String.join(", ", labels);
    }

    public List<Map<String, Object>> getSelectedRecords() {
        java.util.Set<String> vals = getValue();
        List<Map<String, Object>> list = new ArrayList<>();
        if (vals != null) {
            for (String val : vals) {
                if (valueToRecordMap.containsKey(val)) {
                    list.add(valueToRecordMap.get(val));
                }
            }
        }
        return list;
    }
}
