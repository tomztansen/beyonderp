package com.vaadinerp.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

import com.vaadinerp.dashboard.DashboardModel.WidgetOptions;

/** Grid dari List<Map>. Kolom dari options.columns atau semua kolom baris pertama. Klik baris = emit. */
public class TableWidget implements DashboardWidget {
    private final WidgetOptions opt;
    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final List<BiConsumer<Object, String>> listeners = new ArrayList<>();
    private List<String> builtColumns = List.of();

    public TableWidget(WidgetOptions opt) {
        this.opt = opt;
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("260px");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(e -> {
            if (opt.emit() == null || e.getItem() == null) return;
            Object v = e.getItem().get(opt.emit());
            String label = opt.x() != null ? String.valueOf(e.getItem().get(opt.x())) : String.valueOf(v);
            for (BiConsumer<Object, String> l : listeners) l.accept(v, label);
        });
    }

    @Override public Component asComponent() { return grid; }

    @Override
    public void setData(List<Map<String, Object>> rows) {
        List<String> cols = !opt.columns().isEmpty() ? opt.columns()
                : (rows == null || rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet()));
        if (!cols.equals(builtColumns)) { // kolom dibangun sekali; refresh hanya setItems
            grid.removeAllColumns();
            for (String c : cols)
                grid.addColumn(r -> r.get(c) == null ? "" : String.valueOf(r.get(c))).setHeader(c).setAutoWidth(true).setSortable(true);
            builtColumns = cols;
        }
        grid.setItems(rows == null ? List.of() : rows);
    }

    @Override
    public void highlight(Object value) {
        grid.deselectAll();
        if (value == null || opt.emit() == null) return;
        grid.getListDataView().getItems().filter(r -> value.equals(r.get(opt.emit()))).findFirst().ifPresent(grid::select);
    }

    @Override public void addSelectListener(BiConsumer<Object, String> l) { listeners.add(l); }
}
