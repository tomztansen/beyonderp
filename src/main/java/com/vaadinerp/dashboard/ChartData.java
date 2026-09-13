package com.vaadinerp.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadinerp.dashboard.DashboardModel.ChartConfig;
import com.vaadinerp.dashboard.DashboardModel.ChartSeries;
import com.vaadinerp.dashboard.DashboardModel.WidgetOptions;

/**
 * rows (hasil agregat report) + options -> konfigurasi chart netral (murni, tanpa Vaadin/JS).
 * emitValues[i] = nilai kolom emit untuk kategori ke-i, dipakai memetakan klik kembali ke filter.
 */
public final class ChartData {
    private ChartData() {}

    public static ChartConfig build(String widgetType, WidgetOptions opt, List<Map<String, Object>> rows) {
        String wt = widgetType == null ? "BAR" : widgetType.toUpperCase();
        List<Map<String, Object>> r = rows == null ? List.of() : rows;
        return switch (wt) {
            case "GAUGE" -> gauge(opt, r);
            case "PIE" -> single("pie", opt, r);
            case "LINE" -> single("line", opt, r);
            case "COMBO" -> combo(opt, r);
            default -> single("bar", opt, r);
        };
    }

    /** Satu seri (y) atau pivot per kolom series; kategori = x unik urut kemunculan. */
    private static ChartConfig single(String type, WidgetOptions opt, List<Map<String, Object>> rows) {
        if (opt.series() == null || opt.series().isBlank()) {
            List<String> cats = new ArrayList<>();
            List<Object> data = new ArrayList<>();
            List<Object> emits = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                cats.add(String.valueOf(KpiWidget.pick(row, opt.x())));
                data.add(num(KpiWidget.pick(row, opt.y())));
                emits.add(opt.emit() == null ? null : KpiWidget.pick(row, opt.emit()));
            }
            String name = opt.y() != null ? opt.y() : "value";
            return new ChartConfig(type, cats, List.of(new ChartSeries(name, null, data)), emits,
                    opt.chart().stacked(), opt.chart().horizontal(), opt.chart().max(), opt.chart().colors());
        }
        return pivot(type, opt, rows, null);
    }

    private static ChartConfig combo(WidgetOptions opt, List<Map<String, Object>> rows) {
        if (opt.series() == null || opt.series().isBlank()) return single("bar", opt, rows);
        return pivot("line", opt, rows, "column");
    }

    /** Pivot: kategori = x unik, seri = nilai kolom series unik; sel kosong = 0. */
    private static ChartConfig pivot(String type, WidgetOptions opt, List<Map<String, Object>> rows, String defaultSeriesType) {
        Map<String, Integer> catIdx = new LinkedHashMap<>();
        Map<String, Map<String, Object>> seriesData = new LinkedHashMap<>();
        List<Object> emits = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String cat = String.valueOf(KpiWidget.pick(row, opt.x()));
            if (!catIdx.containsKey(cat)) {
                catIdx.put(cat, catIdx.size());
                emits.add(opt.emit() == null ? null : KpiWidget.pick(row, opt.emit()));
            }
            String s = String.valueOf(KpiWidget.pick(row, opt.series()));
            seriesData.computeIfAbsent(s, k -> new LinkedHashMap<>()).put(cat, num(KpiWidget.pick(row, opt.y())));
        }
        List<String> cats = new ArrayList<>(catIdx.keySet());
        List<ChartSeries> series = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> e : seriesData.entrySet()) {
            List<Object> data = new ArrayList<>();
            for (String c : cats) data.add(e.getValue().getOrDefault(c, 0));
            String st = opt.chart().seriesTypes().getOrDefault(e.getKey(), defaultSeriesType);
            series.add(new ChartSeries(e.getKey(), st, data));
        }
        return new ChartConfig(type, cats, series, emits, opt.chart().stacked(), opt.chart().horizontal(),
                opt.chart().max(), opt.chart().colors());
    }

    /** radialBar: persen = y / max (max default 100). */
    private static ChartConfig gauge(WidgetOptions opt, List<Map<String, Object>> rows) {
        double value = rows.isEmpty() ? 0 : toDouble(KpiWidget.pick(rows.get(0), opt.y()));
        double max = opt.chart().max() != null && opt.chart().max() > 0 ? opt.chart().max() : 100;
        double pct = Math.max(0, Math.min(100, value / max * 100));
        return new ChartConfig("radialBar", List.of(), List.of(new ChartSeries(opt.y() != null ? opt.y() : "value", null, List.of(pct))),
                List.of(), false, false, max, opt.chart().colors());
    }

    private static Object num(Object v) {
        return v == null ? 0 : v;
    }

    private static double toDouble(Object v) {
        return v instanceof Number n ? n.doubleValue() : 0;
    }
}
