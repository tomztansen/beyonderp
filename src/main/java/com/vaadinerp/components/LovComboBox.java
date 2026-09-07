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
    /**
     * Batas label yang ditahan. Combo ini lazy: baris diambil per halaman, dan dulu
     * setiap baris yang pernah digulir tersimpan permanen selama form dibuka.
     */
    private static final int MAX_LABELS = 2000;

    private final Map<String, String> valueToLabelMap = new HashMap<>();
    /** Hanya berisi record yang sedang terpilih; sisanya diambil ulang saat diminta. */
    private final Map<String, Map<String, Object>> valueToRecordMap = new HashMap<>();
    private final Map<String, FilterCondition> activeFilters = new HashMap<>();

    public LovComboBox(String label, String lovCode, DynamicDataService dataService) {
        super(label);
        this.lovCode = lovCode;
        this.dataService = dataService;

        // Map value (key) to its display label
        setItemLabelGenerator(val -> valueToLabelMap.getOrDefault(val, val));
        setClearButtonVisible(true);
        setPlaceholder("Select...");
        setWidthFull();
        getStyle().set("min-width", "0").set("max-width", "100%").set("box-sizing", "border-box");

        setupLazyDataProvider();
    }

    private void setupLazyDataProvider() {
        LovMeta lovMeta = dataService != null && lovCode != null ? dataService.getLovMeta(lovCode).orElse(null) : null;
        if (lovMeta == null) {
            setItems(new ArrayList<>());
            return;
        }

        setItems(com.vaadin.flow.data.provider.DataProvider.fromFilteringCallbacks(
                query -> {
                    String filter = query.getFilter().orElse("");
                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    List<Map<String, Object>> records = dataService.fetchLovDataPaged(
                            lovMeta.getTableName(),
                            lovMeta.getSearchColumn(),
                            filter,
                            activeFilters.values(),
                            offset,
                            limit);

                    List<String> pageItems = new ArrayList<>();
                    for (Map<String, Object> rec : records) {
                        Object valObj = getCaseInsensitive(rec, lovMeta.getValueColumn());
                        if (valObj == null && rec.containsKey("id"))
                            valObj = rec.get("id");
                        Object lblObj = getCaseInsensitive(rec, lovMeta.getLabelColumn());
                        if (lblObj == null || lblObj.toString().trim().isEmpty()) {
                            if (getCaseInsensitive(rec, "code") != null)
                                lblObj = getCaseInsensitive(rec, "code");
                            else if (getCaseInsensitive(rec, "name") != null)
                                lblObj = getCaseInsensitive(rec, "name");
                        }

                        String val = valObj != null ? valObj.toString().trim() : "";
                        String lbl = lblObj != null ? lblObj.toString().trim() : val;

                        if (!val.isEmpty()) {
                            valueToLabelMap.put(val, lbl);
                            pageItems.add(val);
                        }
                    }
                    // Gulir jauh di LOV besar tidak boleh menumpuk label tanpa batas.
                    // Nilai yang sedang terpilih dipertahankan supaya kotaknya tidak
                    // berubah jadi angka; sisanya terisi lagi dari halaman berikutnya.
                    if (valueToLabelMap.size() > MAX_LABELS) {
                        String selected = getValue();
                        String selectedLabel = selected != null ? valueToLabelMap.get(selected) : null;
                        valueToLabelMap.clear();
                        if (selected != null && selectedLabel != null) {
                            valueToLabelMap.put(selected, selectedLabel);
                        }
                    }
                    return pageItems.stream();
                },
                query -> {
                    String filter = query.getFilter().orElse("");
                    return dataService.countLovData(
                            lovMeta.getTableName(),
                            lovMeta.getSearchColumn(),
                            filter,
                            activeFilters.values());
                }));
    }

    private Object getCaseInsensitive(Map<String, Object> map, String key) {
        if (map == null || key == null)
            return null;
        if (map.containsKey(key))
            return map.get(key);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void setValue(String value) {
        if (value != null && !value.isEmpty()) {
            if (!valueToLabelMap.containsKey(value)) {
                LovMeta lovMeta = dataService != null && lovCode != null ? dataService.getLovMeta(lovCode).orElse(null)
                        : null;
                if (lovMeta != null) {
                    Map<String, Object> rec = dataService.fetchLovRecord(lovMeta.getTableName(),
                            lovMeta.getValueColumn(), value);
                    if (rec != null) {
                        // Hanya record terpilih yang pernah dibaca, jadi jangan simpan
                        // pilihan-pilihan sebelumnya.
                        valueToRecordMap.clear();
                        valueToRecordMap.put(value, rec);
                        Object lblObj = getCaseInsensitive(rec, lovMeta.getLabelColumn());
                        valueToLabelMap.put(value, lblObj != null ? lblObj.toString() : value);
                    } else {
                        valueToLabelMap.put(value, value);
                    }
                } else {
                    valueToLabelMap.put(value, value);
                }
            }
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
        if (getDataProvider() != null) {
            getDataProvider().refreshAll();
        } else {
            setupLazyDataProvider();
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
            LovMeta lovMeta = dataService != null && lovCode != null ? dataService.getLovMeta(lovCode).orElse(null)
                    : null;
            if (lovMeta != null) {
                Map<String, Object> rec = dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(),
                        val);
                if (rec != null) {
                    valueToRecordMap.put(val, rec);
                    return rec;
                }
            }
        }
        return null;
    }
}
