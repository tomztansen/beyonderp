package com.vaadinerp.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadinerp.dashboard.DashboardModel.*;

/**
 * Gabung N dashboard (home multi-role) jadi satu layout:
 * urut display_order lalu row_order, widget yang sudah muncul dilewati (col_span dari
 * kemunculan pertama), refresh = terkecil > 0, params digabung by name (pertama menang).
 */
public final class DashboardMerger {
    private DashboardMerger() {}

    public static MergedDashboard merge(List<DashboardDef> dashboards) {
        List<DashboardDef> sorted = new ArrayList<>(dashboards);
        sorted.sort(Comparator.comparingInt(DashboardDef::displayOrder));

        Map<String, ItemDef> items = new LinkedHashMap<>();
        Map<String, ParamDef> params = new LinkedHashMap<>();
        int refresh = 0;
        List<String> titles = new ArrayList<>();
        for (DashboardDef d : sorted) {
            titles.add(d.title());
            List<ItemDef> byRow = new ArrayList<>(d.items());
            byRow.sort(Comparator.comparingInt(ItemDef::rowOrder));
            for (ItemDef it : byRow) items.putIfAbsent(it.widget().widgetCode(), it);
            for (ParamDef p : d.params()) params.putIfAbsent(p.name(), p);
            if (d.refreshSeconds() > 0 && (refresh == 0 || d.refreshSeconds() < refresh)) refresh = d.refreshSeconds();
        }
        return new MergedDashboard(String.join(", ", titles), refresh, new ArrayList<>(params.values()),
                new ArrayList<>(items.values()));
    }
}
