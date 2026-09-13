package com.vaadinerp.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Record murni untuk metadata dashboard. Parsing JSON longgar: rusak/kosong -> nilai default, tidak melempar. */
public final class DashboardModel {
    private DashboardModel() {}

    private static final ObjectMapper JSON = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static JsonNode read(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return JSON.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode n, String field) {
        return n != null && n.hasNonNull(field) ? n.get(field).asText() : null;
    }

    private static List<String> list(JsonNode n, String field) {
        List<String> out = new ArrayList<>();
        if (n != null && n.get(field) != null && n.get(field).isArray())
            n.get(field).forEach(e -> out.add(e.asText()));
        return out;
    }

    public record ChartOptions(boolean stacked, boolean horizontal, Map<String, String> seriesTypes, Double max,
            List<String> colors) {
        public static final ChartOptions EMPTY = new ChartOptions(false, false, Map.of(), null, List.of());
        public ChartOptions {
            if (seriesTypes == null) seriesTypes = Map.of();
            if (colors == null) colors = List.of();
        }
        static ChartOptions parse(JsonNode n) {
            if (n == null) return EMPTY;
            Map<String, String> st = new LinkedHashMap<>();
            if (n.get("series_types") != null && n.get("series_types").isObject())
                n.get("series_types").fields().forEachRemaining(e -> st.put(e.getKey(), e.getValue().asText()));
            Double max = n.hasNonNull("max") && n.get("max").isNumber() ? n.get("max").asDouble() : null;
            return new ChartOptions(n.path("stacked").asBoolean(false), n.path("horizontal").asBoolean(false), st, max,
                    list(n, "colors"));
        }
    }

    public record WidgetOptions(String x, String y, String series, String emit, List<String> listen,
            String compare, String good, String format, List<String> columns, ChartOptions chart) {
        public WidgetOptions {
            if (listen == null) listen = List.of();
            if (columns == null) columns = List.of();
            if (chart == null) chart = ChartOptions.EMPTY;
        }
        public static WidgetOptions parse(String json) {
            JsonNode n = read(json);
            return new WidgetOptions(text(n, "x"), text(n, "y"), text(n, "series"), text(n, "emit"),
                    list(n, "listen"), text(n, "compare"), text(n, "good"), text(n, "format"), list(n, "columns"),
                    ChartOptions.parse(n));
        }
    }

    public record WidgetDef(String widgetCode, String title, String reportCode, String widgetType,
            WidgetOptions options, String drillFormCode, String drillReportCode, String drillFilterMapping) {}

    public record ItemDef(WidgetDef widget, int rowOrder, int colSpan) {}

    public record ParamDef(String name, String type, String lov, String defaultValue) {
        public static List<ParamDef> parseList(String json) {
            List<ParamDef> out = new ArrayList<>();
            JsonNode n = read(json);
            if (n == null || !n.isArray()) return out;
            for (JsonNode p : n) {
                String name = text(p, "name");
                if (name == null || name.isBlank()) continue;
                out.add(new ParamDef(name.trim(), text(p, "type") != null ? text(p, "type").toUpperCase() : "TEXT",
                        text(p, "lov"), text(p, "default")));
            }
            return out;
        }
    }

    public record ChartSeries(String name, String type, List<Object> data) {}

    public record ChartConfig(String type, List<String> categories, List<ChartSeries> series, List<Object> emitValues,
            boolean stacked, boolean horizontal, Double max, List<String> colors) {}

    public record DashboardDef(String dashboardCode, String title, String roleCode, int displayOrder,
            int refreshSeconds, List<ParamDef> params, List<ItemDef> items) {
        public DashboardDef {
            if (params == null) params = List.of();
            if (items == null) items = List.of();
        }
    }

    public record MergedDashboard(String title, int refreshSeconds, List<ParamDef> params, List<ItemDef> items) {
        public MergedDashboard {
            if (params == null) params = List.of();
            if (items == null) items = List.of();
        }
    }
}
