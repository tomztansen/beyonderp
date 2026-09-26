package com.vaadinerp.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormActionMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.service.DynamicDataService;

import java.util.*;
import java.util.function.Consumer;

public class DynamicPickerPopupDialog extends Dialog {

    private final FormActionMeta actionMeta;
    private final DynamicDataService dataService;
    private final Map<String, Object> headerRecord;
    private final Consumer<List<Map<String, Object>>> onSelectCallback;

    private final Grid<Map<String, Object>> grid;
    private final TextField searchField;
    private final Map<String, String> columnFilters = new HashMap<>();

    public DynamicPickerPopupDialog(FormActionMeta actionMeta,
            DynamicDataService dataService,
            Map<String, Object> headerRecord,
            Consumer<List<Map<String, Object>>> onSelectCallback) {
        this.actionMeta = actionMeta;
        this.dataService = dataService;
        this.headerRecord = headerRecord;
        this.onSelectCallback = onSelectCallback;

        setHeaderTitle(actionMeta.getActionLabel() != null ? actionMeta.getActionLabel() : "Pick Data");
        setWidth("80vw");
        setHeight("80vh");

        searchField = new TextField();
        searchField.setPlaceholder("Find data locally...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setSizeFull();
        grid.addItemClickListener(event -> {
            if (grid.getSelectedItems().contains(event.getItem())) {
                grid.deselect(event.getItem());
            } else {
                grid.select(event.getItem());
            }
        });

        searchField.addValueChangeListener(e -> applyFilters());

        Button refreshBtn = new com.vaadinerp.components.SafeButton("Refresh", VaadinIcon.REFRESH.create(), e -> loadData());

        HorizontalLayout searchToolbar = new HorizontalLayout(searchField, refreshBtn);
        searchToolbar.setWidthFull();
        searchToolbar.setFlexGrow(1, searchField);

        setupColumns();

        VerticalLayout content = new VerticalLayout();
        if (actionMeta.getFilterMapping() != null && !actionMeta.getFilterMapping().isBlank() && dataService != null
                && dataService.isCurrentUserSuperAdmin()) {
            com.vaadin.flow.component.details.Details diagDetails = new com.vaadin.flow.component.details.Details();
            diagDetails.setSummaryText("🔍 Diagnostik Filter Aktif: " + actionMeta.getFilterMapping());
            com.vaadin.flow.component.html.Pre diagText = new com.vaadin.flow.component.html.Pre(
                    dataService.evaluateFilterMappingDiagnostic(actionMeta.getFilterMapping(), headerRecord));
            diagText.getStyle().set("font-size", "12px").set("color", "#4b5563").set("background", "#f3f4f6")
                    .set("padding", "8px").set("border-radius", "4px").set("white-space", "pre-wrap")
                    .set("margin", "0");
            diagDetails.add(diagText);
            diagDetails.setOpened(true);
            diagDetails.setWidthFull();
            content.add(diagDetails);
        }
        content.add(searchToolbar, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);
        add(content);

        Button btnOk = new com.vaadinerp.components.SafeButton("Select & Add", VaadinIcon.CHECK.create(), e -> {
            Set<Map<String, Object>> selected = grid.getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                Notification.show("Please select at least 1 data first!", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (this.onSelectCallback != null) {
                if (actionMeta.getCopySourceLovCode() != null && !actionMeta.getCopySourceLovCode().trim().isEmpty()) {
                    List<Map<String, Object>> aggregatedData = new ArrayList<>();
                    if (dataService != null) {
                        for (Map<String, Object> pickedRow : selected) {
                            List<Map<String, Object>> children = dataService.fetchLovDataWithActionFilters(
                                    actionMeta.getCopySourceLovCode(),
                                    actionMeta.getCopyFilterMapping(),
                                    headerRecord,
                                    pickedRow,
                                    "");
                            if (children != null) {
                                aggregatedData.addAll(children);
                            }
                        }
                    }
                    this.onSelectCallback.accept(aggregatedData);
                } else {
                    this.onSelectCallback.accept(new ArrayList<>(selected));
                }
            }
            close();
        });
        btnOk.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new com.vaadinerp.components.SafeButton("Cancel", e -> close());

        getFooter().add(btnCancel, btnOk);

        loadData();
    }

    private void setupColumns() {
        String lovCode = actionMeta.getSourceLovCode();
        if (lovCode == null)
            return;

        LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
        FormMeta targetForm = dataService.getFormMetaRepository().findById(lovCode).orElse(null);
        String table = lovMeta != null ? lovMeta.getTableName() : lovCode;
        List<String> allCols = dataService.getColumnsForQueryOrTable(table);

        Map<String, String[]> colDefMap = new HashMap<>();
        if (lovMeta != null && lovMeta.getGridColumns() != null && !lovMeta.getGridColumns().isBlank()) {
            String[] colDefs = lovMeta.getGridColumns().split(",");
            for (String colDef : colDefs) {
                String[] parts = colDef.split(":");
                colDefMap.put(parts[0].trim().toLowerCase(), parts);
            }
        }

        Map<String, Grid.Column<Map<String, Object>>> columnsMap = new LinkedHashMap<>();

        for (String rawColName : allCols) {
            String colNameLower = rawColName.toLowerCase();
            // colDefMap terisi dari field form (Show in Grid) atau dari gridColumns LOV --
            // kalau ada definisinya, itu jadi whitelist: kolom lain di tabel disembunyikan.
            // colDefMap kosong (LOV/tabel tanpa definisi apa pun) berarti tampilkan semua,
            // supaya tidak ada dari LOV mentah yang tiba-tiba grid-nya kosong.
            if (!colDefMap.isEmpty() && !colDefMap.containsKey(colNameLower)) {
                continue;
            }
            String colHeader = rawColName.substring(0, 1).toUpperCase() + rawColName.substring(1).replace("_", " ");
            String colWidth = "";

            if (colDefMap.containsKey(colNameLower)) {
                String[] parts = colDefMap.get(colNameLower);
                if (parts.length > 1) colHeader = parts[1].trim();
                if (parts.length > 2) colWidth = parts[2].trim();
            }

            final String finalColName = rawColName;
            FieldMeta targetField = (targetForm != null && targetForm.getFields() != null)
                    ? targetForm.getFields().stream()
                            .filter(f -> f.getFieldName().equalsIgnoreCase(finalColName))
                            .findFirst().orElse(null)
                    : null;

            Grid.Column<Map<String, Object>> col = grid.addColumn(row -> {
                Object valObj = getCaseInsensitiveVal(row, finalColName);
                if (targetField != null) {
                    return ComponentFactory.formatFieldValueWithLov(targetField, valObj, dataService);
                }
                return valObj != null ? valObj.toString() : "";
            }).setHeader(colHeader).setResizable(true);

            if (!colWidth.isEmpty()) {
                col.setWidth(colWidth);
                col.setAutoWidth(false);
            } else {
                col.setAutoWidth(true);
            }
            if (targetField != null) {
                col.setSortable(targetField.isSortable());
            } else {
                col.setSortable(true);
            }
            columnsMap.put(rawColName, col);
        }

        com.vaadin.flow.component.grid.HeaderRow filterRow = grid.appendHeaderRow();
        columnsMap.forEach((colName, col) -> {
            TextField filterField = new TextField();
            filterField.setPlaceholder("Filter...");
            filterField.setClearButtonVisible(true);
            filterField.setWidthFull();
            filterField.setValueChangeMode(ValueChangeMode.EAGER);
            filterField.addValueChangeListener(e -> {
                columnFilters.put(colName, e.getValue() != null ? e.getValue().toLowerCase() : "");
                applyFilters();
            });
            filterRow.getCell(col).setComponent(filterField);
        });
    }

    private void loadData() {
        try {
            List<Map<String, Object>> records = dataService.fetchLovDataWithActionFilters(
                    actionMeta.getSourceLovCode(),
                    actionMeta.getFilterMapping(),
                    headerRecord,
                    "");
            grid.setItems(records);

            applyFilters();
        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification notif = com.vaadin.flow.component.notification.Notification
                    .show(
                            e.getMessage(), 10000, com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
            notif.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
            grid.setItems(new java.util.ArrayList<>());
        }
    }

    private Object getCaseInsensitiveVal(Map<String, Object> map, String key) {
        if (map == null || key == null)
            return null;
        if (map.containsKey(key))
            return map.get(key);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key))
                return e.getValue();
        }
        return null;
    }

    private void applyFilters() {
        if (grid.getDataProvider() instanceof com.vaadin.flow.data.provider.ListDataProvider) {
            @SuppressWarnings("unchecked")
            com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>> dp = (com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>>) grid.getDataProvider();
            
            String globalTerm = searchField.getValue() != null ? searchField.getValue().toLowerCase() : "";
            
            dp.setFilter(row -> {
                // Global filter
                if (!globalTerm.isEmpty()) {
                    boolean matchGlobal = false;
                    for (Object val : row.values()) {
                        if (val != null && val.toString().toLowerCase().contains(globalTerm)) {
                            matchGlobal = true;
                            break;
                        }
                    }
                    if (!matchGlobal) return false;
                }
                
                // Column filters
                for (Map.Entry<String, String> entry : columnFilters.entrySet()) {
                    String term = entry.getValue();
                    if (term != null && !term.isEmpty()) {
                        Object val = getCaseInsensitiveVal(row, entry.getKey());
                        if (val == null) return false;
                        
                        // Handle formatFieldValueWithLov equivalent local filtering string comparison
                        String strVal = val.toString().toLowerCase();
                        if (!strVal.contains(term)) {
                            return false;
                        }
                    }
                }
                return true;
            });
        }
    }
}
