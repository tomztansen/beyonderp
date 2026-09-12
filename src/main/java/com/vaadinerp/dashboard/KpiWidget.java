package com.vaadinerp.dashboard;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import com.vaadinerp.dashboard.DashboardModel.WidgetOptions;

/** Angka besar dari kolom y baris pertama + delta vs kolom compare (▲/▼, hijau/merah, arah dari good). */
public class KpiWidget implements DashboardWidget {
    private final WidgetOptions opt;
    private final Div root = new Div();
    private final Span value = new Span("–");
    private final Span delta = new Span();

    public KpiWidget(WidgetOptions opt) {
        this.opt = opt;
        value.getStyle().set("font-size", "2.4rem").set("font-weight", "700").set("line-height", "1.1");
        delta.getStyle().set("font-size", "0.9rem").set("display", "block").set("margin-top", "4px");
        root.getStyle().set("padding", "8px 4px");
        root.add(value, delta);
    }

    @Override public Component asComponent() { return root; }

    @Override
    public void setData(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) { value.setText("–"); delta.setText(""); return; }
        Map<String, Object> r = rows.get(0);
        Object v = pick(r, opt.y());
        value.setText(format(v));
        delta.setText("");
        if (opt.compare() != null) {
            Object prev = pick(r, opt.compare());
            if (v instanceof Number n && prev instanceof Number p && p.doubleValue() != 0) {
                double pct = (n.doubleValue() - p.doubleValue()) / Math.abs(p.doubleValue()) * 100;
                boolean up = pct >= 0;
                boolean good = "down".equalsIgnoreCase(opt.good()) ? !up : up;
                delta.setText((up ? "▲ " : "▼ ") + new DecimalFormat("0.#").format(Math.abs(pct)) + "% vs previous");
                delta.getStyle().set("color", good ? "#16a34a" : "#dc2626");
            }
        }
    }

    /** Kolom y; kalau kosong pakai kolom numerik pertama. Melempar bila kolom disebut tapi tidak ada (ditampilkan di kartu). */
    static Object pick(Map<String, Object> row, String col) {
        if (col != null && !col.isBlank()) {
            for (Map.Entry<String, Object> e : row.entrySet())
                if (e.getKey().equalsIgnoreCase(col)) return e.getValue();
            throw new IllegalArgumentException("Column '" + col + "' not in result");
        }
        for (Object o : row.values()) if (o instanceof Number) return o;
        return row.isEmpty() ? null : row.values().iterator().next();
    }

    private String format(Object v) {
        if (v == null) return "–";
        if (v instanceof Number n) {
            String pattern = opt.format() != null ? opt.format() : (n instanceof BigDecimal || n instanceof Double || n instanceof Float ? "#,##0.##" : "#,##0");
            return new DecimalFormat(pattern).format(n);
        }
        return String.valueOf(v);
    }

    @Override public void highlight(Object v) { /* KPI tidak punya titik untuk disorot */ }
    @Override public void addSelectListener(BiConsumer<Object, String> l) { /* KPI tidak memancarkan; drill lewat kartu */ }
}
