package com.vaadinerp.dashboard;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * drill_filter_mapping -> parameter awal form/report tujuan. Format sama dengan hyperlink field:
 * "row.kolom" (baris terpilih), "filter.dim" (state filter dashboard), selain itu literal;
 * "_TAB_TITLE" boleh memuat {row.x} / {filter.y}. Nilai null dilewati supaya tidak memfilter kosong.
 */
public final class DrillMapping {
    private DrillMapping() {}

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(row|filter)\\.([a-zA-Z0-9_.]+)\\}");

    public record DrillTarget(Map<String, Object> extra, String tabTitle) {}

    public static DrillTarget resolve(String mappingJson, Map<String, Object> row, Map<String, Object> filters) {
        Map<String, Object> r = row == null ? Map.of() : row;
        Map<String, Object> f = filters == null ? Map.of() : filters;
        Map<String, Object> extra = new LinkedHashMap<>();
        String title = null;
        if (mappingJson == null || mappingJson.isBlank()) return new DrillTarget(extra, null);
        Map<String, String> cfg;
        try {
            cfg = JSON.readValue(mappingJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return new DrillTarget(extra, null);
        }
        for (Map.Entry<String, String> e : cfg.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (k == null || v == null) continue;
            if ("_TAB_TITLE".equals(k)) {
                title = substitute(v, r, f);
            } else if (v.startsWith("row.")) {
                Object val = valueCaseInsensitive(r, v.substring(4));
                if (val != null) extra.put(k, val);
            } else if (v.startsWith("filter.")) {
                Object val = valueCaseInsensitive(f, v.substring(7));
                if (val != null) extra.put(k, val);
            } else {
                extra.put(k, v);
            }
        }
        return new DrillTarget(extra, title);
    }

    private static String substitute(String template, Map<String, Object> row, Map<String, Object> filters) {
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object val = valueCaseInsensitive("row".equals(m.group(1)) ? row : filters, m.group(2));
            m.appendReplacement(sb, Matcher.quoteReplacement(val == null ? "" : String.valueOf(val)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    static Object valueCaseInsensitive(Map<String, Object> m, String key) {
        if (m == null || key == null) return null;
        for (Map.Entry<String, Object> e : m.entrySet())
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) return e.getValue();
        return null;
    }
}
