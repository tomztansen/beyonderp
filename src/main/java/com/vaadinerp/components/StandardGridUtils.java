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
import java.util.LinkedHashMap;
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
     * Cleans up extra header rows (such as filter rows) and removes all columns.
     * This prevents accumulating empty header rows whenever a Grid is rebuilt.
     * 
     * IMPORTANT: Always call this before rebuilding grid columns to prevent
     * header row accumulation and memory leaks.
     */
    public static void cleanGridBeforeRebuild(Grid<?> grid) {
        if (grid == null) return;
        
        // Save current grid configuration
        Grid.SelectionMode selectionMode = grid.getSelectionMode();
        
        // Temporarily set selection mode to NONE to cleanly detach multi-selection RPC model
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        
        // Remove all header rows except the first one (default header)
        List<HeaderRow> headerRows = new ArrayList<>(grid.getHeaderRows());
        for (int i = headerRows.size() - 1; i >= 1; i--) {
            grid.removeHeaderRow(headerRows.get(i));
        }
        
        // Remove all columns
        grid.removeAllColumns();
        
        // Restore selection mode
        if (selectionMode != null) {
            grid.setSelectionMode(selectionMode);
        }
    }

    /**
     * Safely appends a header row by ensuring no duplicate filter rows exist.
     * Always call this instead of grid.appendHeaderRow() directly when adding filter rows.
     */
    public static HeaderRow safeAppendHeaderRow(Grid<?> grid) {
        if (grid == null) return null;
        
        // Remove any extra header rows (keep only default)
        while (grid.getHeaderRows().size() > 1) {
            grid.removeHeaderRow(grid.getHeaderRows().get(grid.getHeaderRows().size() - 1));
        }
        
        // Now it's safe to append a new header row
        return grid.appendHeaderRow();
    }

    /**
     * Generic method to attach standard search filter header row, sorting, and column reordering to any Grid<T>.
     * Returns a Runnable that can be called to re-apply filtering and refresh grid items from dataSupplier.
     */
    public static <T> Runnable attachGridFilters(
            Grid<T> grid,
            Map<Grid.Column<T>, Function<T, String>> colGetterMap,
            Supplier<List<T>> dataSupplier) {

        if (grid == null || colGetterMap == null || colGetterMap.isEmpty()) {
            return () -> {};
        }

        // Enable column reordering and sorting
        grid.setColumnReorderingAllowed(true);
        colGetterMap.keySet().forEach(col -> {
            if (col != null) {
                col.setSortable(true);
                col.setResizable(true);
            }
        });

        // Safely add filter header row
        HeaderRow filterRow = safeAppendHeaderRow(grid);
        Map<Grid.Column<T>, FilterCriteria> filterValues = new LinkedHashMap<>();

        // Create filter application logic
        Runnable applyFilters = () -> {
            List<T> masterList = dataSupplier.get();
            if (masterList == null) {
                grid.setItems(new ArrayList<>());
                return;
            }

            // Check if any filter is active
            boolean hasActiveFilter = filterValues.values().stream().anyMatch(criteria -> {
                if ("Blank".equals(criteria.operator) || "Not blank".equals(criteria.operator)) {
                    return true;
                }
                return criteria.value != null && !criteria.value.trim().isEmpty();
            });

            if (!hasActiveFilter) {
                // No active filters, show all items
                grid.setItems(new ArrayList<>(masterList));
                return;
            }

            // Apply filters
            List<T> filtered = masterList.stream().filter(item -> {
                for (Map.Entry<Grid.Column<T>, FilterCriteria> entry : filterValues.entrySet()) {
                    Grid.Column<T> col = entry.getKey();
                    FilterCriteria criteria = entry.getValue();
                    
                    if (col == null) continue;
                    
                    String op = criteria.operator;
                    String query = criteria.value != null ? criteria.value.trim().toLowerCase() : "";

                    Function<T, String> getter = colGetterMap.get(col);
                    if (getter == null) continue;

                    String rawVal = getter.apply(item);
                    String strVal = rawVal != null ? rawVal.toLowerCase() : "";

                    // Handle Blank/Not blank first
                    if ("Blank".equals(op)) {
                        if (!strVal.isEmpty()) return false;
                        continue;
                    }
                    if ("Not blank".equals(op)) {
                        if (strVal.isEmpty()) return false;
                        continue;
                    }

                    // Skip if no query value for other operators
                    if (query.isEmpty()) continue;

                    // Apply operator
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
                        default:
                            break;
                    }
                }
                return true;
            }).collect(Collectors.toList());

            grid.setItems(filtered);
        };

        for (Map.Entry<Grid.Column<T>, Function<T, String>> entry : colGetterMap.entrySet()) {
            Grid.Column<T> col = entry.getKey();
            if (col == null) continue;
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
