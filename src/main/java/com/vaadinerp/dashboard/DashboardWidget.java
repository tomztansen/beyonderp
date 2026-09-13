package com.vaadinerp.dashboard;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.vaadin.flow.component.Component;

/** Kontrak semua widget. Data selalu hasil agregat report. Listener didaftarkan sekali saat build. */
public interface DashboardWidget {
    Component asComponent();
    void setData(List<Map<String, Object>> rows);
    /** Tandai nilai emit yang aktif; null = normal. Tahap 1: KPI/TABLE mengabaikan atau menandai baris. */
    void highlight(Object value);
    /** (nilai kolom emit, label untuk chip). */
    void addSelectListener(BiConsumer<Object, String> listener);

    /** Baris yang sedang dipilih/aktif di widget ini. Kosong bila tidak ada. */
    default java.util.Map<String, Object> selectedRow() { return java.util.Map.of(); }
}
