package com.vaadinerp.components;

import com.vaadin.flow.component.select.Select;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.service.DynamicDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LovSelect extends Select<String> {

    private final String lovCode;
    private final DynamicDataService dataService;
    private final Map<String, String> valueToLabelMap = new HashMap<>();
    private final Map<String, Map<String, Object>> valueToRecordMap = new HashMap<>();
    private final Map<String, FilterCondition> activeFilters = new HashMap<>();

    private final List<String> currentItems = new ArrayList<>();

    public LovSelect(String label, String lovCode, DynamicDataService dataService) {
        setLabel(label);
        this.lovCode = lovCode;
        this.dataService = dataService;
        
        setItemLabelGenerator(val -> valueToLabelMap.getOrDefault(val, val));
        setPlaceholder("Pilih...");
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
    public void setValue(String value) {
        if (value != null && !value.isEmpty() && !currentItems.contains(value)) {
            currentItems.add(value);
            if (!valueToLabelMap.containsKey(value)) {
                LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
                if (lovMeta != null) {
                    Map<String, Object> rec = dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(), value);
                    if (rec != null) {
                        valueToRecordMap.put(value, rec);
                    }
                    Object lblObj = getCaseInsensitive(rec, lovMeta.getLabelColumn());
                    if (lblObj != null) {
                        valueToLabelMap.put(value, lblObj.toString());
                    } else {
                        valueToLabelMap.put(value, value);
                    }
                } else {
                    valueToLabelMap.put(value, value);
                }
            }
            setItems(new ArrayList<>(currentItems));
        }
        super.setValue(value);
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
        LovMeta lovMeta = dataService != null && lovCode != null ? dataService.getLovMeta(lovCode).orElse(null) : null;
        if (lovMeta != null) {
            List<Map<String, Object>> records = dataService.fetchLovDataPaged(
                    lovMeta.getTableName(),
                    lovMeta.getSearchColumn(),
                    "",
                    activeFilters.values(),
                    0,
                    200
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
        String val = getValue();
        if (val != null && valueToLabelMap.containsKey(val)) {
            return valueToLabelMap.get(val);
        }
        return val != null ? val : "";
    }

    public Map<String, Object> getSelectedRecord() {
        String val = getValue();
        if (val != null && valueToRecordMap.containsKey(val)) {
            return valueToRecordMap.get(val);
        }
        if (val != null && !val.isEmpty()) {
            LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
            if (lovMeta != null) {
                Map<String, Object> rec = dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(), val);
                if (rec != null) {
                    valueToRecordMap.put(val, rec);
                    return rec;
                }
            }
        }
        return null;
    }
}
