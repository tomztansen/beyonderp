package com.vaadinerp.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.service.DynamicDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LovComboBox extends ComboBox<String> {

    /** Bisa diganti lewat setLovCode() (LOV Switch). */
    private String lovCode;
    private final DynamicDataService dataService;
    private LovMeta cachedLovMeta;
    private String cachedLovCode;
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

    /** LovMeta untuk lovCode yang berlaku sekarang; dicache sampai lovCode berganti. */
    private LovMeta currentLovMeta() {
        if (dataService == null || lovCode == null) {
            return null;
        }
        if (!lovCode.equals(cachedLovCode)) {
            cachedLovMeta = dataService.getLovMeta(lovCode).orElse(null);
            cachedLovCode = lovCode;
        }
        return cachedLovMeta;
    }

    private void setupLazyDataProvider() {
        // LovMeta dibaca di dalam callback, bukan ditangkap sekali di sini, supaya
        // setLovCode() cukup refreshAll() tanpa memasang data provider baru.
        setItems(com.vaadin.flow.data.provider.DataProvider.fromFilteringCallbacks(
                query -> {
                    String filter = query.getFilter().orElse("");
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    LovMeta lovMeta = currentLovMeta();
                    if (lovMeta == null) {
                        return java.util.stream.Stream.empty();
                    }

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
                    LovMeta lovMeta = currentLovMeta();
                    if (lovMeta == null) {
                        return 0;
                    }
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

    /** Pastikan label value ini ada, diambil dari LOV yang berlaku sekarang. */
    private void ensureLabel(String value) {
        if (value == null || value.isEmpty() || valueToLabelMap.containsKey(value)) {
            return;
        }
        LovMeta lovMeta = currentLovMeta();
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

    @Override
    public void setValue(String value) {
        ensureLabel(value);
        super.setValue(value);
    }

    public String getLovCode() {
        return lovCode;
    }

    /**
     * Ganti sumber LOV (LOV Switch). Nilai yang sedang terpilih TIDAK dikosongkan di
     * sini -- itu keputusan pemanggil; labelnya di-resolve ulang dari LOV baru.
     */
    public void setLovCode(String newLovCode) {
        if (Objects.equals(lovCode, newLovCode)) {
            return;
        }
        lovCode = newLovCode;
        valueToLabelMap.clear();
        valueToRecordMap.clear();
        ensureLabel(getValue());
        if (getDataProvider() != null) {
            getDataProvider().refreshAll();
        }
        // Pasang ulang generator supaya teks nilai yang sedang tampil ikut dirender ulang.
        setItemLabelGenerator(val -> valueToLabelMap.getOrDefault(val, val));
    }

    public void setFilterValue(FilterCondition condition) {
        // Nilai kosong tetap disimpan: query LOV mengubahnya jadi "tidak ada data" sampai sumbernya diisi.
        activeFilters.put(condition.getFilterId(), condition);
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
            LovMeta lovMeta = currentLovMeta();
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
