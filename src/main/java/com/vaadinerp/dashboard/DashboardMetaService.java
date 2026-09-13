package com.vaadinerp.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.vaadinerp.dashboard.DashboardModel.*;

/** Baca metadata dashboard lewat JDBC (tanpa entity: ddl-auto=validate). Dipanggil per buka tab; tidak di-cache. */
@Service
public class DashboardMetaService {
    private static final Logger log = LoggerFactory.getLogger(DashboardMetaService.class);
    private final JdbcTemplate jdbc;

    public DashboardMetaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean exists(String code) {
        if (code == null || code.isBlank()) return false;
        try {
            Integer n = jdbc.queryForObject("SELECT count(*) FROM public.meta_dashboard WHERE dashboard_code = ?", Integer.class, code);
            return n != null && n > 0;
        } catch (Exception e) {
            return false; // tabel belum ada -> bukan dashboard
        }
    }

    public Optional<DashboardDef> find(String code) {
        try {
            List<DashboardDef> l = load("WHERE d.dashboard_code = ?", code);
            return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
        } catch (Exception e) {
            log.warn("Dashboard metadata unavailable, run sql/dashboard.sql: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<DashboardDef> forRoles(Set<String> roles, boolean superAdmin) {
        try {
            if (superAdmin) return load("", new Object[0]);
            if (roles == null || roles.isEmpty()) return List.of();
            String in = String.join(",", roles.stream().map(r -> "?").toList());
            return load("WHERE d.role_code IN (" + in + ")", roles.toArray());
        } catch (Exception e) {
            log.warn("Dashboard metadata unavailable, run sql/dashboard.sql: {}", e.getMessage());
            return List.of();
        }
    }

    /** Dashboard untuk Home: hanya yang show_on_home, milik role user (SUPER_ADMIN: semua). Kosong bila tabel belum ada. */
    public List<DashboardDef> forHome(Set<String> roles, boolean superAdmin) {
        try {
            if (superAdmin) return load("WHERE d.show_on_home", new Object[0]);
            if (roles == null || roles.isEmpty()) return List.of();
            String in = String.join(",", roles.stream().map(r -> "?").toList());
            return load("WHERE d.show_on_home AND d.role_code IN (" + in + ")", roles.toArray());
        } catch (Exception e) {
            log.warn("Dashboard metadata unavailable, run sql/dashboard.sql: {}", e.getMessage());
            return List.of();
        }
    }

    private List<DashboardDef> load(String where, Object... args) {
        List<Map<String, Object>> heads = jdbc.queryForList(
                "SELECT d.dashboard_code, d.title, d.role_code, d.display_order, d.refresh_seconds, d.params_json"
                        + " FROM public.meta_dashboard d " + where + " ORDER BY d.display_order, d.dashboard_code", args);
        List<DashboardDef> out = new ArrayList<>();
        for (Map<String, Object> h : heads) {
            String code = (String) h.get("dashboard_code");
            List<ItemDef> items = new ArrayList<>();
            for (Map<String, Object> r : jdbc.queryForList(
                    "SELECT i.row_order, i.col_span, w.widget_code, w.title, w.report_code, w.widget_type, w.options_json,"
                            + " w.drill_form_code, w.drill_report_code, w.drill_filter_mapping"
                            + " FROM public.meta_dashboard_item i JOIN public.meta_dashboard_widget w ON w.widget_code = i.widget_code"
                            + " WHERE i.dashboard_code = ? ORDER BY i.row_order, i.id", code)) {
                WidgetDef w = new WidgetDef((String) r.get("widget_code"), (String) r.get("title"),
                        (String) r.get("report_code"), String.valueOf(r.get("widget_type")).toUpperCase(),
                        WidgetOptions.parse((String) r.get("options_json")), (String) r.get("drill_form_code"),
                        (String) r.get("drill_report_code"), (String) r.get("drill_filter_mapping"));
                items.add(new ItemDef(w, ((Number) r.get("row_order")).intValue(), ((Number) r.get("col_span")).intValue()));
            }
            out.add(new DashboardDef(code, (String) h.get("title"), (String) h.get("role_code"),
                    ((Number) h.get("display_order")).intValue(), ((Number) h.get("refresh_seconds")).intValue(),
                    ParamDef.parseList((String) h.get("params_json")), items));
        }
        return out;
    }
}
