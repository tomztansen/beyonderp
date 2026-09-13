package com.vaadinerp.dashboard;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.vaadinerp.dashboard.DashboardModel.WidgetDef;

/** Satu state filter global per dashboard: panel parameter dan klik chart menulis ke sini. Tidak thread-safe -- selalu di bawah lock session Vaadin. */
public final class FilterState {
    private final Map<String, Object> values = new LinkedHashMap<>();
    private final Map<String, String> labels = new LinkedHashMap<>();

    public void put(String dim, Object value, String label) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            remove(dim);
            return;
        }
        values.put(dim, value);
        labels.put(dim, label != null ? label : String.valueOf(value));
    }

    public void remove(String dim) {
        values.remove(dim);
        labels.remove(dim);
    }

    public void clear() {
        values.clear();
        labels.clear();
    }

    /** @return true bila nilai sekarang aktif, false bila baru saja dilepas (klik ulang). */
    public boolean toggle(String dim, Object value, String label) {
        if (Objects.equals(values.get(dim), value)) {
            remove(dim);
            return false;
        }
        put(dim, value, label);
        return true;
    }

    public Object get(String dim) {
        return values.get(dim);
    }

    public Map<String, String> labels() {
        return new LinkedHashMap<>(labels);
    }

    /** Semua nama yang dideklarasikan selalu ada (null bila tidak diset) supaya NamedParameterJdbcTemplate tidak menolak. */
    public Map<String, Object> paramsFor(Collection<String> declared) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String d : declared) out.put(d, null);
        out.putAll(values);
        return out;
    }

    public static boolean reloads(WidgetDef w, String changedDim) {
        return w.options().listen().contains(changedDim);
    }

    public static boolean highlights(WidgetDef w, String changedDim) {
        return changedDim != null && changedDim.equals(w.options().emit());
    }
}
