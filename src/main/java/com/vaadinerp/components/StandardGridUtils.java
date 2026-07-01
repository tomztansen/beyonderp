package com.vaadinerp.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StandardGridUtils {

    public static class FilterCriteria {
        public String operator = "Contains";
        public String value = "";
    }

    /**
     * Generic method to attach standard search filter header row, sorting, and column reordering to any Grid<T>.
     * Returns a Runnable that can be called to re-apply filtering and refresh grid items from dataSupplier.
     */
    public static <T> Runnable attachGridFilters(
            Grid<T> grid,
            Map<Grid.Column<T>, Function<T, String>> colGetterMap,
            Supplier<List<T>> dataSupplier) {

        grid.setColumnReorderingAllowed(true);
        colGetterMap.keySet().forEach(col -> col.setSortable(true));

        HeaderRow filterRow = grid.appendHeaderRow();
        Map<Grid.Column<T>, FilterCriteria> filterValues = new HashMap<>();

        Runnable applyFilters = () -> {
            List<T> masterList = dataSupplier.get();
            if (masterList == null) return;

            List<T> filtered = masterList.stream().filter(item -> {
                for (Map.Entry<Grid.Column<T>, FilterCriteria> entry : filterValues.entrySet()) {
                    Grid.Column<T> col = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    String op = criteria.operator;
                    String query = criteria.value != null ? criteria.value.trim().toLowerCase() : "";

                    Function<T, String> getter = colGetterMap.get(col);
                    if (getter == null) continue;

                    String rawVal = getter.apply(item);
                    String strVal = rawVal != null ? rawVal.toLowerCase() : "";

                    if ("Blank".equals(op)) {
                        if (!strVal.isEmpty()) return false;
                        continue;
                    }
                    if ("Not blank".equals(op)) {
                        if (strVal.isEmpty()) return false;
                        continue;
                    }

                    if (query.isEmpty()) continue;

                    switch (op) {
                        case "Contains":
                            if (!strVal.contains(query)) return false;
                            break;
                        case "Not contains":
                            if (strVal.contains(query)) return false;
                            break;
                        case "Equals":
                            if (!strVal.equals(query)) return false;
                            break;
                        case "Not equal":
                            if (strVal.equals(query)) return false;
                            break;
                        case "Starts with":
                            if (!strVal.startsWith(query)) return false;
                            break;
                        case "Ends with":
                            if (!strVal.endsWith(query)) return false;
                            break;
                    }
                }
                return true;
            }).collect(Collectors.toList());

            grid.setItems(filtered);
        };

        for (Map.Entry<Grid.Column<T>, Function<T, String>> entry : colGetterMap.entrySet()) {
            Grid.Column<T> col = entry.getKey();
            FilterCriteria criteria = new FilterCriteria();
            filterValues.put(col, criteria);

            TextField filterField = new TextField();
            filterField.setPlaceholder("Filter...");
            filterField.setValueChangeMode(ValueChangeMode.EAGER);
            filterField.setWidthFull();
            filterField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

            Button filterButton = new Button(VaadinIcon.FILTER.create());
            filterButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            filterButton.getStyle().set("cursor", "pointer");
            filterButton.getElement().setProperty("title", "Contains");
            filterField.setPrefixComponent(filterButton);

            ContextMenu contextMenu = new ContextMenu(filterButton);
            contextMenu.setOpenOnClick(true);

            Runnable applyOperatorUI = () -> {
                String op = criteria.operator;
                filterButton.getElement().setProperty("title", op);
                boolean needsInput = !("Blank".equals(op) || "Not blank".equals(op));
                if (!needsInput) {
                    filterField.setValue("");
                    filterField.setPlaceholder(op);
                    filterField.setReadOnly(true);
                } else {
                    filterField.setPlaceholder("Filter...");
                    filterField.setReadOnly(false);
                }
            };

            com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<com.vaadin.flow.component.contextmenu.MenuItem>> listener = e -> {
                if (e.getSource().getText() != null) {
                    criteria.operator = e.getSource().getText();
                    applyOperatorUI.run();
                    applyFilters.run();
                }
            };

            contextMenu.addItem("Contains", listener);
            contextMenu.addItem("Not contains", listener);
            contextMenu.addItem("Equals", listener);
            contextMenu.addItem("Not equal", listener);
            contextMenu.addItem("Starts with", listener);
            contextMenu.addItem("Ends with", listener);
            contextMenu.addItem("Blank", listener);
            contextMenu.addItem("Not blank", listener);

            filterField.addValueChangeListener(e -> {
                criteria.value = e.getValue();
                applyFilters.run();
            });

            filterRow.getCell(col).setComponent(filterField);
        }

        return applyFilters;
    }

    /**
     * Attaches standard search filters in header row, column sorting, and column reordering
     * to a Grid<Map<String, Object>>. Returns a Runnable to refresh grid items.
     */
    public static Runnable attachMapGridFilters(
            Grid<Map<String, Object>> grid,
            Map<Grid.Column<Map<String, Object>>, String> colKeyMap,
            Supplier<List<Map<String, Object>>> dataSupplier) {

        Map<Grid.Column<Map<String, Object>>, Function<Map<String, Object>, String>> getterMap = new HashMap<>();
        colKeyMap.forEach((col, key) -> getterMap.put(col, map -> map.get(key) != null ? map.get(key).toString() : ""));
        return attachGridFilters(grid, getterMap, dataSupplier);
    }

    /**
     * Menerapkan urutan kolom tersimpan user ke Grid secara aman.
     * Mengabaikan kolom yang sudah dihapus di metadata dan otomatis menaruh kolom baru di akhir urutan.
     */
    public static <T> void applySafeColumnOrder(
            Grid<T> grid,
            Map<Grid.Column<T>, String> columnToFieldNameMap,
            List<String> savedUserFieldOrder) {
        if (savedUserFieldOrder == null || savedUserFieldOrder.isEmpty()) {
            return;
        }

        Map<String, Grid.Column<T>> fieldNameToColMap = new HashMap<>();
        for (Map.Entry<Grid.Column<T>, String> entry : columnToFieldNameMap.entrySet()) {
            fieldNameToColMap.put(entry.getValue(), entry.getKey());
        }

        List<Grid.Column<T>> reconciledOrder = new ArrayList<>();
        Set<Grid.Column<T>> processedCols = new HashSet<>();

        // 1. Identifikasi dan letakkan kolom non-metadata (seperti kolom seleksi checkbox internal) di depan
        for (Grid.Column<T> col : grid.getColumns()) {
            if (!columnToFieldNameMap.containsKey(col)) {
                reconciledOrder.add(col);
                processedCols.add(col);
            }
        }

        // 2. Tambahkan kolom yang sesuai dengan urutan user
        for (String fieldName : savedUserFieldOrder) {
            Grid.Column<T> col = fieldNameToColMap.get(fieldName);
            if (col != null && !processedCols.contains(col)) {
                reconciledOrder.add(col);
                processedCols.add(col);
            }
        }

        // 3. Tambahkan kolom lain di Grid yang belum masuk (kolom baru dari metadata / kolom sistem)
        for (Grid.Column<T> col : grid.getColumns()) {
            if (!processedCols.contains(col)) {
                reconciledOrder.add(col);
            }
        }

        // 4. Terapkan ke Grid
        if (!reconciledOrder.isEmpty() && reconciledOrder.size() == grid.getColumns().size()) {
            grid.setColumnOrder(reconciledOrder);
        }
    }
}
