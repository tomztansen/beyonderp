package com.vaadinerp.dashboard;

import java.util.ArrayList;
import java.util.List;

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

    public record WidgetOptions(String x, String y, String series, String emit, List<String> listen,
            String compare, String good, String format, List<String> columns) {
        public WidgetOptions {
            if (listen == null) listen = List.of();
            if (columns == null) columns = List.of();
        }
        public static WidgetOptions parse(String json) {
            JsonNode n = read(json);
            return new WidgetOptions(text(n, "x"), text(n, "y"), text(n, "series"), text(n, "emit"),
                    list(n, "listen"), text(n, "compare"), text(n, "good"), text(n, "format"), list(n, "columns"));
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
