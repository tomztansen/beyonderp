package com.vaadinerp.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
     * Returns the global client-side JavaScript snippet that enables 1-click & Alt-click
     * cell clipboard copy on every <vaadin-grid-cell-content> across all grids.
     */
    public static String getGlobalCellCopyJs() {
        return "if (window._vaadinGridCellCopyInitialized) return;\n"
                + "window._vaadinGridCellCopyInitialized = true;\n"
                + "const style = document.createElement('style');\n"
                + "style.innerHTML = `\n"
                + "  vaadin-grid-cell-content { position: relative !important; }\n"
                + "  .grid-cell-copy-btn {\n"
                + "    position: absolute; right: 4px; top: 50%; transform: translateY(-50%);\n"
                + "    width: 22px; height: 22px; background-color: rgba(241, 245, 249, 0.95);\n"
                + "    border: 1px solid #cbd5e1; border-radius: 4px; display: none;\n"
                + "    align-items: center; justify-content: center; cursor: pointer;\n"
                + "    z-index: 10; color: #475569; font-size: 11px;\n"
                + "    box-shadow: 0 1px 3px rgba(0,0,0,0.1); transition: all 0.15s ease;\n"
                + "  }\n"
                + "  .grid-cell-copy-btn:hover { background-color: #e2e8f0; color: #1e293b; transform: translateY(-50%) scale(1.08); }\n"
                + "  vaadin-grid-cell-content:hover .grid-cell-copy-btn { display: flex !important; }\n"
                + "  #grid-cell-copy-toast {\n"
                + "    position: fixed; bottom: 24px; right: 24px; background: #1e293b; color: #f8fafc;\n"
                + "    padding: 10px 16px; border-radius: 8px; font-size: 13px; font-family: sans-serif;\n"
                + "    box-shadow: 0 10px 15px -3px rgba(0,0,0,0.3); z-index: 999999;\n"
                + "    transition: opacity 0.3s ease, transform 0.3s ease; opacity: 0; transform: translateY(10px);\n"
                + "    pointer-events: none; display: flex; align-items: center; gap: 8px;\n"
                + "  }\n"
                + "`;\n"
                + "document.head.appendChild(style);\n"
                + "window.showGridCopyToast = function(message) {\n"
                + "  let toast = document.getElementById('grid-cell-copy-toast');\n"
                + "  if (!toast) { toast = document.createElement('div'); toast.id = 'grid-cell-copy-toast'; document.body.appendChild(toast); }\n"
                + "  toast.innerHTML = '<span style=\"font-size: 16px;\">📋</span> <span>' + message + '</span>';\n"
                + "  toast.style.opacity = '1'; toast.style.transform = 'translateY(0)';\n"
                + "  if (toast.timeoutId) clearTimeout(toast.timeoutId);\n"
                + "  toast.timeoutId = setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateY(10px)'; }, 2500);\n"
                + "};\n"
                + "function extractCellText(cellContent) {\n"
                + "  if (!cellContent) return '';\n"
                + "  const clone = cellContent.cloneNode(true);\n"
                + "  const btns = clone.querySelectorAll('.grid-cell-copy-btn, vaadin-checkbox, button, input');\n"
                + "  btns.forEach(b => b.remove());\n"
                + "  return (clone.innerText || clone.textContent || '').trim();\n"
                + "}\n"
                + "function copyTextToClipboard(text) {\n"
                + "  if (!text) return;\n"
                + "  if (navigator.clipboard && navigator.clipboard.writeText) {\n"
                + "    navigator.clipboard.writeText(text).then(() => {\n"
                + "      const shortText = text.length > 35 ? text.substring(0, 35) + '...' : text;\n"
                + "      window.showGridCopyToast('Disalin ke clipboard: \"' + shortText + '\"');\n"
                + "    }).catch(() => fallbackCopy(text));\n"
                + "  } else { fallbackCopy(text); }\n"
                + "}\n"
                + "function fallbackCopy(text) {\n"
                + "  const textArea = document.createElement('textarea');\n"
                + "  textArea.value = text; textArea.style.position = 'fixed'; textArea.style.left = '-999999px';\n"
                + "  document.body.appendChild(textArea); textArea.focus(); textArea.select();\n"
                + "  try { document.execCommand('copy'); const shortText = text.length > 35 ? text.substring(0, 35) + '...' : text; window.showGridCopyToast('Disalin ke clipboard: \"' + shortText + '\"'); } catch (err) {}\n"
                + "  document.body.removeChild(textArea);\n"
                + "}\n"
                + "document.addEventListener('mouseover', function(e) {\n"
                + "  const path = e.composedPath ? e.composedPath() : [e.target];\n"
                + "  const cellContent = path.find(el => el && el.tagName && el.tagName.toLowerCase() === 'vaadin-grid-cell-content');\n"
                + "  if (!cellContent) return;\n"
                + "  if (cellContent.closest('thead') || cellContent.closest('tfoot') || cellContent.getAttribute('slot')?.startsWith('header') || cellContent.getAttribute('slot')?.startsWith('footer')) return;\n"
                + "  if (cellContent.querySelector('vaadin-checkbox, button, input, .grid-cell-copy-btn')) return;\n"
                + "  const text = extractCellText(cellContent);\n"
                + "  if (!text) return;\n"
                + "  const btn = document.createElement('div'); btn.className = 'grid-cell-copy-btn'; btn.innerHTML = '📋'; btn.title = 'Salin teks cell (atau Alt+Klik pada cell)';\n"
                + "  btn.addEventListener('click', function(clickEvent) { clickEvent.stopPropagation(); clickEvent.preventDefault(); const textToCopy = extractCellText(cellContent); if (textToCopy) copyTextToClipboard(textToCopy); });\n"
                + "  cellContent.appendChild(btn);\n"
                + "}, true);\n"
                + "document.addEventListener('click', function(e) {\n"
                + "  if (e.altKey || e.ctrlKey || e.shiftKey) {\n"
                + "    const path = e.composedPath ? e.composedPath() : [e.target];\n"
                + "    const cellContent = path.find(el => el && el.tagName && el.tagName.toLowerCase() === 'vaadin-grid-cell-content');\n"
                + "    if (!cellContent) return;\n"
                + "    if (cellContent.closest('thead') || cellContent.closest('tfoot') || cellContent.getAttribute('slot')?.startsWith('header') || cellContent.getAttribute('slot')?.startsWith('footer')) return;\n"
                + "    const textToCopy = extractCellText(cellContent);\n"
                + "    if (textToCopy) { e.stopPropagation(); e.preventDefault(); copyTextToClipboard(textToCopy); }\n"
                + "  }\n"
                + "}, true);\n"
                + "document.addEventListener('contextmenu', function(e) {\n"
                + "  const path = e.composedPath ? e.composedPath() : [e.target];\n"
                + "  const cellContent = path.find(el => el && el.tagName && el.tagName.toLowerCase() === 'vaadin-grid-cell-content');\n"
                + "  if (!cellContent) return;\n"
                + "  if (cellContent.closest('thead') || cellContent.closest('tfoot') || cellContent.getAttribute('slot')?.startsWith('header') || cellContent.getAttribute('slot')?.startsWith('footer')) return;\n"
                + "  const textToCopy = extractCellText(cellContent);\n"
                + "  if (textToCopy) {\n"
                + "    window._lastHoveredCellText = textToCopy;\n"
                + "  }\n"
                + "}, true);";
    }

    /**
     * Enables cell clipboard copy feature on any Grid.
     * Attaches both client-side hover/click copy and right-click context menu support.
     */
    public static void enableCellClipboardCopy(Grid<?> grid) {
        if (grid == null) return;
        try {
            grid.getElement().executeJs(getGlobalCellCopyJs());
        } catch (Exception ignored) {}
    }

    /**
     * Cleans up extra header rows (such as filter rows) and removes all columns.
     * This prevents accumulating empty header rows whenever a Grid is rebuilt.
     * 
     * IMPORTANT: Always call this before rebuilding grid columns to prevent
     * header row accumulation and memory leaks.
     */
    public static void cleanGridBeforeRebuild(Grid<?> grid) {
        if (grid == null)
            return;
        enableCellClipboardCopy(grid);

        // Save current grid configuration
        Grid.SelectionMode selectionMode = grid.getSelectionMode();

        // Temporarily set selection mode to NONE to cleanly detach multi-selection RPC
        // model
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
     * Always call this instead of grid.appendHeaderRow() directly when adding
     * filter rows.
     */
    public static HeaderRow safeAppendHeaderRow(Grid<?> grid) {
        if (grid == null)
            return null;

        // Remove any extra header rows (keep only default)
        while (grid.getHeaderRows().size() > 1) {
            grid.removeHeaderRow(grid.getHeaderRows().get(grid.getHeaderRows().size() - 1));
        }

        // Now it's safe to append a new header row
        return grid.appendHeaderRow();
    }

    /**
     * Generic method to attach standard search filter header row, sorting, and
     * column reordering to any Grid<T>.
     * Returns a Runnable that can be called to re-apply filtering and refresh grid
     * items from dataSupplier.
     */
    public static <T> Runnable attachGridFilters(
            Grid<T> grid,
            Map<Grid.Column<T>, Function<T, String>> colGetterMap,
            Supplier<List<T>> dataSupplier) {

        if (grid == null || colGetterMap == null || colGetterMap.isEmpty()) {
            return () -> {
            };
        }

        enableCellClipboardCopy(grid);

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

                    if (col == null)
                        continue;

                    String op = criteria.operator;
                    String query = criteria.value != null ? criteria.value.trim().toLowerCase() : "";

                    Function<T, String> getter = colGetterMap.get(col);
                    if (getter == null)
                        continue;

                    String rawVal = getter.apply(item);
                    String strVal = rawVal != null ? rawVal.toLowerCase() : "";

                    // Handle Blank/Not blank first
                    if ("Blank".equals(op)) {
                        if (!strVal.isEmpty())
                            return false;
                        continue;
                    }
                    if ("Not blank".equals(op)) {
                        if (strVal.isEmpty())
                            return false;
                        continue;
                    }

                    // Skip if no query value for other operators
                    if (query.isEmpty())
                        continue;

                    // Apply operator
                    switch (op) {
                        case "Contains":
                            if (!strVal.contains(query))
                                return false;
                            break;
                        case "Not contains":
                            if (strVal.contains(query))
                                return false;
                            break;
                        case "Equals":
                            if (!strVal.equals(query))
                                return false;
                            break;
                        case "Not equal":
                            if (strVal.equals(query))
                                return false;
                            break;
                        case "Starts with":
                            if (!strVal.startsWith(query))
                                return false;
                            break;
                        case "Ends with":
                            if (!strVal.endsWith(query))
                                return false;
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
            if (col == null)
                continue;
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
     * Attaches standard search filters in header row, column sorting, and column
     * reordering
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
     * Mengabaikan kolom yang sudah dihapus di metadata dan otomatis menaruh kolom
     * baru di akhir urutan.
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

        // 1. Identifikasi dan letakkan kolom non-metadata (seperti kolom seleksi
        // checkbox internal) di depan
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

        // 3. Tambahkan kolom lain di Grid yang belum masuk (kolom baru dari metadata /
        // kolom sistem)
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

    /**
     * Membuat tombol Export Excel (berupa Anchor yang mengemas Button) untuk Grid
     * apapun.
     * Mengunduh isi Grid beserta header dalam format Excel CSV (UTF-8 BOM) yang
     * siap dibuka di Microsoft Excel.
     */
    public static <T> Anchor createExportExcelButton(Grid<T> grid, String fileNamePrefix) {
        return createExportExcelButton(grid, fileNamePrefix, null);
    }

    public static <T> Anchor createExportExcelButton(Grid<T> grid, String fileNamePrefix,
            Map<Grid.Column<T>, Function<T, String>> colGetterMap) {
        Button btnExport = new Button("Export Excel", VaadinIcon.FILE_TABLE.create());
        btnExport.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnExport.getStyle().set("cursor", "pointer");

        DownloadHandler downloadHandler = DownloadHandler.fromInputStream(event -> {
            try {
                StringBuilder sb = new StringBuilder();
                // Sep directive for Excel recognition of comma delimiter across locales
                sb.append("sep=,\r\n");

                List<Grid.Column<T>> columns = new ArrayList<>();
                List<String> headers = new ArrayList<>();

                for (Grid.Column<T> col : grid.getColumns()) {
                    if (!col.isVisible())
                        continue;
                    String headerText = col.getHeaderText();
                    String key = col.getKey();
                    if ((headerText == null || headerText.trim().isEmpty()) && (key == null || key.trim().isEmpty())) {
                        continue;
                    }
                    columns.add(col);
                    headers.add(headerText != null && !headerText.trim().isEmpty() ? headerText.trim() : key);
                }

                // Header Row
                for (int i = 0; i < headers.size(); i++) {
                    sb.append(escapeCsv(headers.get(i)));
                    if (i < headers.size() - 1)
                        sb.append(",");
                }
                sb.append("\r\n");

                // Data Rows
                List<T> items = new ArrayList<>();
                Set<T> selected = grid.getSelectedItems();
                if (selected != null && !selected.isEmpty()) {
                    items.addAll(selected);
                } else {
                    try {
                        grid.getListDataView().getItems().forEach(items::add);
                    } catch (Exception ignored) {
                        if (grid.getDataProvider() instanceof com.vaadin.flow.data.provider.ListDataProvider) {
                            items.addAll(((com.vaadin.flow.data.provider.ListDataProvider<T>) grid.getDataProvider()).getItems());
                        }
                    }
                }

                for (T item : items) {
                    if (item == null) continue;
                    for (int i = 0; i < columns.size(); i++) {
                        Grid.Column<T> col = columns.get(i);
                        String val = extractCellValue(item, col, colGetterMap);
                        sb.append(escapeCsv(val));
                        if (i < columns.size() - 1) sb.append(",");
                    }
                    sb.append("\r\n");
                }

                byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
                byte[] finalBytes = new byte[bom.length + bytes.length];
                System.arraycopy(bom, 0, finalBytes, 0, bom.length);
                System.arraycopy(bytes, 0, finalBytes, bom.length, bytes.length);

                String exportFileName = fileNamePrefix + "_" + System.currentTimeMillis() + ".csv";
                return new DownloadResponse(
                        new ByteArrayInputStream(finalBytes),
                        exportFileName,
                        "text/csv",
                        finalBytes.length
                );
            } catch (Exception e) {
                byte[] errBytes = "Error exporting data".getBytes(StandardCharsets.UTF_8);
                return new DownloadResponse(
                        new ByteArrayInputStream(errBytes),
                        "error.csv",
                        "text/csv",
                        errBytes.length
                );
            }
        });

        btnExport.addClickListener(e -> {
            Set<T> sel = grid.getSelectedItems();
            if (sel != null && !sel.isEmpty()) {
                com.vaadin.flow.component.notification.Notification.show("Mengekspor " + sel.size() + " baris terpilih ke Excel...", 2500, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END);
            } else {
                com.vaadin.flow.component.notification.Notification.show("Mengekspor seluruh data yang tampil ke Excel...", 2000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END);
            }
        });

        Anchor anchor = new Anchor(downloadHandler, "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(btnExport);
        return anchor;
    }

    @SuppressWarnings("unchecked")
    private static <T> String extractCellValue(T item, Grid.Column<T> col,
            Map<Grid.Column<T>, Function<T, String>> colGetterMap) {
        if (colGetterMap != null && colGetterMap.containsKey(col) && colGetterMap.get(col) != null) {
            String res = colGetterMap.get(col).apply(item);
            return res != null ? res : "";
        }
        if (item instanceof Map) {
            String key = col.getKey();
            if (key != null) {
                Object val = ((Map<String, Object>) item).get(key);
                if (val == null) {
                    for (Map.Entry<String, Object> entry : ((Map<String, Object>) item).entrySet()) {
                        if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                            val = entry.getValue();
                            break;
                        }
                    }
                }
                return val != null ? val.toString() : "";
            }
        } else {
            String key = col.getKey();
            if (key != null && !key.trim().isEmpty()) {
                try {
                    String getterName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
                    java.lang.reflect.Method method = item.getClass().getMethod(getterName);
                    Object val = method.invoke(item);
                    return val != null ? val.toString() : "";
                } catch (Exception ignored) {
                    try {
                        java.lang.reflect.Field field = item.getClass().getDeclaredField(key);
                        field.setAccessible(true);
                        Object val = field.get(item);
                        return val != null ? val.toString() : "";
                    } catch (Exception ignored2) {
                    }
                }
            }
        }
        return "";
    }

    private static String escapeCsv(String str) {
        if (str == null)
            return "";
        String s = str.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    public static <T> void attachSelectAllHeader(Grid<T> grid, java.util.function.Supplier<java.util.List<T>> pageItemsSupplier) {
        if (grid == null || !(grid.getSelectionModel() instanceof com.vaadin.flow.component.grid.GridMultiSelectionModel<?> multiSel)) {
            return;
        }
        multiSel.setSelectAllCheckboxVisibility(com.vaadin.flow.component.grid.GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
        try {
            Grid.Column<T> selCol = grid.getColumns().stream()
                    .filter(c -> "vaadin-grid-selection-column".equals(c.getKey()) || (c.getKey() != null && c.getKey().contains("selection")))
                    .findFirst()
                    .orElse(null);
            if (selCol != null) {
                for (HeaderRow row : grid.getHeaderRows()) {
                    HeaderRow.HeaderCell cell = row.getCell(selCol);
                    if (cell != null) {
                        cell.setComponent(null);
                        cell.setText("");
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
