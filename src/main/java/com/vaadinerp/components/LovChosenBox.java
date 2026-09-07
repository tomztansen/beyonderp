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
    /**
     * Batas label yang ditahan. Combo ini lazy: baris diambil per halaman, dan dulu
     * setiap baris yang pernah digulir tersimpan permanen selama form dibuka.
     */
    private static final int MAX_LABELS = 2000;

    private final Map<String, String> valueToLabelMap = new HashMap<>();
    /** Hanya berisi record yang sedang terpilih; sisanya diambil saat diminta. */
    private final Map<String, Map<String, Object>> valueToRecordMap = new HashMap<>();
    private final Map<String, FilterCondition> activeFilters = new HashMap<>();

    public LovChosenBox(String label, String lovCode, DynamicDataService dataService) {
        super(label);
        this.lovCode = lovCode;
        this.dataService = dataService;

        setItemLabelGenerator(val -> valueToLabelMap.getOrDefault(val, val));
        setClearButtonVisible(true);
        setPlaceholder("Select multiple...");
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
                    // Label nilai yang sedang terpilih dipertahankan supaya chip-nya
                    // tidak berubah jadi angka; sisanya terisi lagi dari halaman
                    // berikutnya.
                    if (valueToLabelMap.size() > MAX_LABELS) {
                        java.util.Set<String> selected = getValue();
                        Map<String, String> keep = new HashMap<>();
                        if (selected != null) {
                            for (String s : selected) {
                                String lbl = valueToLabelMap.get(s);
                                if (lbl != null) {
                                    keep.put(s, lbl);
                                }
                            }
                        }
                        valueToLabelMap.clear();
                        valueToLabelMap.putAll(keep);
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
    public void setValue(java.util.Set<String> values) {
        if (values != null && !values.isEmpty()) {
            for (String v : values) {
                if (v != null && !v.isEmpty() && !valueToLabelMap.containsKey(v)) {
                    LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
                    if (lovMeta != null) {
                        Map<String, Object> rec = dataService.fetchLovRecord(lovMeta.getTableName(),
                                lovMeta.getValueColumn(), v);
                        if (rec != null) {
                            valueToRecordMap.put(v, rec);
                            Object lblObj = getCaseInsensitive(rec, lovMeta.getLabelColumn());
                            valueToLabelMap.put(v, lblObj != null ? lblObj.toString() : v);
                        }
                    }
                }
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
        if (!isAttached())
            return;

        if (getDataProvider() != null) {
            getDataProvider().refreshAll();
        } else {
            setupLazyDataProvider();
        }
    }

    public String getDisplayLabel() {
        java.util.Set<String> vals = getValue();
        if (vals == null || vals.isEmpty())
            return "";
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
            LovMeta lovMeta = null;
            for (String val : vals) {
                Map<String, Object> rec = valueToRecordMap.get(val);
                if (rec == null && dataService != null && lovCode != null) {
                    // Record tidak lagi ditahan untuk setiap baris yang digulir, jadi
                    // yang dipilih user dari dropdown diambil di sini saat dibutuhkan.
                    if (lovMeta == null) {
                        lovMeta = dataService.getLovMeta(lovCode).orElse(null);
                    }
                    if (lovMeta != null) {
                        rec = dataService.fetchLovRecord(lovMeta.getTableName(), lovMeta.getValueColumn(), val);
                        if (rec != null) {
                            valueToRecordMap.put(val, rec);
                        }
                    }
                }
                if (rec != null) {
                    list.add(rec);
                }
            }
            // Buang record milik pilihan lama.
            valueToRecordMap.keySet().retainAll(vals);
        }
        return list;
    }
}
