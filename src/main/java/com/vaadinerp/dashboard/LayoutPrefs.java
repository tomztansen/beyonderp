package com.vaadinerp.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.vaadinerp.dashboard.DashboardModel.ItemDef;

/** Preferensi layout per user per dashboard: {"order":[..],"span":{..},"hidden":[..],"params":{..}}. Murni. */
public final class LayoutPrefs {
    private static final ObjectMapper JSON = new ObjectMapper();

    public final List<String> order = new ArrayList<>();
    public final Map<String, Integer> span = new LinkedHashMap<>();
    public final Set<String> hidden = new LinkedHashSet<>();
    public final Map<String, String> params = new LinkedHashMap<>();

    public static LayoutPrefs parse(String json) {
        LayoutPrefs p = new LayoutPrefs();
        if (json == null || json.isBlank()) return p;
        try {
            JsonNode n = JSON.readTree(json);
            if (n.get("order") != null) n.get("order").forEach(e -> p.order.add(e.asText()));
            if (n.get("hidden") != null) n.get("hidden").forEach(e -> p.hidden.add(e.asText()));
            if (n.get("span") != null) n.get("span").fields().forEachRemaining(e -> p.span.put(e.getKey(), e.getValue().asInt()));
            if (n.get("params") != null) n.get("params").fields().forEachRemaining(e -> p.params.put(e.getKey(), e.getValue().asText()));
        } catch (Exception ignored) {
            return new LayoutPrefs(); // rusak -> default
        }
        return p;
    }

    public static LayoutPrefs fromLayout(List<ItemDef> visibleInOrder, Set<String> hidden, Map<String, String> params) {
        LayoutPrefs p = new LayoutPrefs();
        for (ItemDef it : visibleInOrder) {
            p.order.add(it.widget().widgetCode());
            p.span.put(it.widget().widgetCode(), it.colSpan());
        }
        if (hidden != null) p.hidden.addAll(hidden);
        if (params != null) p.params.putAll(params);
        return p;
    }

    public boolean isEmpty() {
        return order.isEmpty() && span.isEmpty() && hidden.isEmpty() && params.isEmpty();
    }

    public String toJson() {
        try {
            ObjectNode n = JSON.createObjectNode();
            n.putPOJO("order", order);
            n.putPOJO("span", span);
            n.putPOJO("hidden", hidden);
            n.putPOJO("params", params);
            return JSON.writeValueAsString(n);
        } catch (Exception e) {
            return "{}";
        }
    }

    /** Urut sesuai order (sisa mengikuti default), span ditimpa (1..12), hidden dibuang, kode asing diabaikan. */
    public List<ItemDef> apply(List<ItemDef> defaults) {
        Map<String, ItemDef> byCode = new LinkedHashMap<>();
        for (ItemDef it : defaults) byCode.put(it.widget().widgetCode(), it);
        List<ItemDef> out = new ArrayList<>();
        Set<String> placed = new LinkedHashSet<>();
        for (String code : order) {
            ItemDef it = byCode.get(code);
            if (it != null && !hidden.contains(code) && placed.add(code)) out.add(withSpan(it));
        }
        for (ItemDef it : defaults) {
            String code = it.widget().widgetCode();
            if (!hidden.contains(code) && placed.add(code)) out.add(withSpan(it));
        }
        return out;
    }

    public List<ItemDef> hiddenItems(List<ItemDef> defaults) {
        List<ItemDef> out = new ArrayList<>();
        for (ItemDef it : defaults) if (hidden.contains(it.widget().widgetCode())) out.add(it);
        return out;
    }

    private ItemDef withSpan(ItemDef it) {
        Integer s = span.get(it.widget().widgetCode());
        if (s == null) return it;
        return new ItemDef(it.widget(), it.rowOrder(), Math.max(1, Math.min(12, s)));
    }
}
