package com.vaadinerp.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.service.DynamicDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LovComboBox extends ComboBox<String> {

    private final String lovCode;
    private final DynamicDataService dataService;
    private final Map<String, String> valueToLabelMap = new HashMap<>();
    private final Map<String, FilterCondition> activeFilters = new HashMap<>();

    private final List<String> currentItems = new ArrayList<>();

    public LovComboBox(String label, String lovCode, DynamicDataService dataService) {
        super(label);
        this.lovCode = lovCode;
        this.dataService = dataService;
        
        // Map value (key) to its display label
        setItemLabelGenerator(val -> valueToLabelMap.getOrDefault(val, val));
        setClearButtonVisible(true);
        setPlaceholder("Pilih...");
        
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
                    currentItems.add(val);
                }
            }
            setItems(new ArrayList<>(currentItems));
        } else {
            currentItems.clear();
            setItems(new ArrayList<>());
        }
    }
}
