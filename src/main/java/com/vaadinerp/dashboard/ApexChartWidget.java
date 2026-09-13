package com.vaadinerp.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

import com.vaadinerp.dashboard.DashboardModel.ChartConfig;
import com.vaadinerp.dashboard.DashboardModel.WidgetOptions;

/** Chart ApexCharts generik: BAR/LINE/PIE/COMBO/GAUGE. Klik titik -> nilai emit baris itu; highlight = index nilai emit aktif. */
@Tag("apex-widget")
@NpmPackage(value = "apexcharts", version = "^4.5.0")
@JsModule("./components/apex-widget.js")
public class ApexChartWidget extends Component implements DashboardWidget, HasSize {

    private static final ObjectMapper JSON = new ObjectMapper();

    @DomEvent("widget-select")
    public static class SelectEvent extends ComponentEvent<ApexChartWidget> {
        private final int index;
        public SelectEvent(ApexChartWidget source, boolean fromClient, @EventData("event.detail.index") double index) {
            super(source, fromClient);
            this.index = (int) index;
        }
        public int getIndex() { return index; }
    }

    private final String widgetType;
    private final WidgetOptions opt;
    private final List<BiConsumer<Object, String>> listeners = new ArrayList<>();
    private ChartConfig last = ChartData.build("BAR", WidgetOptions.parse(null), List.of());
    private int lastIdx = -1; // F1: simpan highlight terakhir untuk re-send saat re-attach
    private List<Map<String, Object>> rows = List.of();

    public ApexChartWidget(String widgetType, WidgetOptions opt) {
        this.widgetType = widgetType;
        this.opt = opt;
        setWidthFull();
        // Listener DOM didaftarkan sekali di sini; refresh hanya setConfig
        addListener(SelectEvent.class, e -> {
            int i = e.getIndex();
            if (opt.emit() == null || i < 0 || i >= last.emitValues().size()) return;
            Object v = last.emitValues().get(i);
            String label = i < last.categories().size() ? last.categories().get(i) : String.valueOf(v);
            for (BiConsumer<Object, String> l : listeners) l.accept(v, label);
        });
    }

    @Override public Component asComponent() { return this; }

    @Override
    public void setData(List<Map<String, Object>> rows) {
        this.rows = rows == null ? List.of() : new ArrayList<>(rows);
        last = ChartData.build(widgetType, opt, rows);
        push();
    }

    @Override
    public Map<String, Object> selectedRow() {
        if (lastIdx < 0 || lastIdx >= rows.size()) return Map.of();
        if (opt.x() == null) return rows.get(lastIdx);
        String cat = last.categories().size() > lastIdx ? last.categories().get(lastIdx) : null;
        if (cat == null) return Map.of();
        for (Map<String, Object> row : rows) {
            if (row == null) continue;
            Object xVal = KpiWidget.pick(row, opt.x());
            if (cat.equals(String.valueOf(xVal))) return row;
        }
        return Map.of();
    }

    // F1: extracted dari setData agar bisa dipanggil ulang di onAttach
    private void push() {
        // F5: kirim hanya field yang dibutuhkan JS; emitValues tidak dibaca browser
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", last.type());
        payload.put("categories", last.categories());
        payload.put("series", last.series());
        payload.put("stacked", last.stacked());
        payload.put("horizontal", last.horizontal());
        payload.put("max", last.max());
        payload.put("colors", last.colors());
        try {
            getElement().callJsFunction("setConfig", JSON.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Chart config serialization failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void highlight(Object value) {
        lastIdx = -1;
        if (value != null) {
            for (int i = 0; i < last.emitValues().size(); i++)
                if (sameValue(last.emitValues().get(i), value)) { lastIdx = i; break; }
        }
        getElement().callJsFunction("highlight", lastIdx);
    }

    // F1: TabSheet detach/re-attach — Flow tidak replay callJsFunction; kirim ulang manual
    @Override
    protected void onAttach(AttachEvent e) {
        super.onAttach(e);
        if (!e.isInitialAttach()) { // TabSheet memasang ulang elemen: kirim ulang config + highlight
            push();
            getElement().callJsFunction("highlight", lastIdx);
        }
    }

    /** 13L vs 13 vs "13" dianggap sama (nilai datang dari JDBC, klik, atau LOV string). */
    static boolean sameValue(Object a, Object b) {
        if (Objects.equals(a, b)) return true;
        if (a == null || b == null) return false;
        return String.valueOf(a).trim().equals(String.valueOf(b).trim());
    }

    @Override public void addSelectListener(BiConsumer<Object, String> l) { listeners.add(l); }
}
