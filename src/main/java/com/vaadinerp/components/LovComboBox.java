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
    private final Map<String, Object> activeFilters = new HashMap<>();

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

    public void setFilterValue(String filterColumn, Object value) {
        if (value == null || value.toString().trim().isEmpty()) {
            activeFilters.remove(filterColumn);
        } else {
            activeFilters.put(filterColumn, value);
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
                    activeFilters
            );
            
            valueToLabelMap.clear();
            List<String> values = new ArrayList<>();
            for (Map<String, Object> rec : records) {
                Object valObj = rec.get(lovMeta.getValueColumn());
                Object lblObj = rec.get(lovMeta.getLabelColumn());
                
                String val = valObj != null ? valObj.toString() : "";
                String lbl = lblObj != null ? lblObj.toString() : val;
                
                if (!val.isEmpty()) {
                    valueToLabelMap.put(val, lbl);
                    values.add(val);
                }
            }
            setItems(values);
        } else {
            setItems(new ArrayList<>());
        }
    }
}
