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
        searchField.setPlaceholder("Cari data...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> loadData(e.getValue()));

        Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> loadData(searchField.getValue()));

        HorizontalLayout searchToolbar = new HorizontalLayout(searchField, refreshBtn);
        searchToolbar.setWidthFull();
        searchToolbar.setFlexGrow(1, searchField);

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setSizeFull();

        setupColumns();

        VerticalLayout content = new VerticalLayout();
        if (actionMeta.getFilterMapping() != null && !actionMeta.getFilterMapping().isBlank() && dataService != null && dataService.isCurrentUserSuperAdmin()) {
            com.vaadin.flow.component.details.Details diagDetails = new com.vaadin.flow.component.details.Details();
            diagDetails.setSummaryText("🔍 Diagnostik Filter Aktif: " + actionMeta.getFilterMapping());
            com.vaadin.flow.component.html.Pre diagText = new com.vaadin.flow.component.html.Pre(dataService.evaluateFilterMappingDiagnostic(actionMeta.getFilterMapping(), headerRecord));
            diagText.getStyle().set("font-size", "12px").set("color", "#4b5563").set("background", "#f3f4f6").set("padding", "8px").set("border-radius", "4px").set("white-space", "pre-wrap").set("margin", "0");
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

        Button btnOk = new Button("Pilih & Tambahkan", VaadinIcon.CHECK.create(), e -> {
            Set<Map<String, Object>> selected = grid.getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                Notification.show("Pilih minimal 1 data terlebih dahulu!", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (onSelectCallback != null) {
                onSelectCallback.accept(new ArrayList<>(selected));
            }
            close();
        });
        btnOk.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new Button("Batal", e -> close());

        getFooter().add(btnCancel, btnOk);

        loadData("");
    }

    private void setupColumns() {
        String lovCode = actionMeta.getSourceLovCode();
        if (lovCode == null) return;

        LovMeta lovMeta = dataService.getLovMeta(lovCode).orElse(null);
        FormMeta targetForm = dataService.getFormMetaRepository().findById(lovCode).orElse(null);

        if (lovMeta != null && lovMeta.getGridColumns() != null && !lovMeta.getGridColumns().isBlank()) {
            String[] colDefs = lovMeta.getGridColumns().split(",");
            for (String colDef : colDefs) {
                String[] parts = colDef.split(":");
                String colName = parts[0].trim();
                String colHeader = parts.length > 1 ? parts[1].trim() : colName;
                String colWidth = parts.length > 2 ? parts[2].trim() : "150px";

                FieldMeta targetField = (targetForm != null && targetForm.getFields() != null)
                        ? targetForm.getFields().stream()
                                .filter(f -> f.getFieldName().equalsIgnoreCase(colName))
                                .findFirst().orElse(null)
                        : null;

                Grid.Column<Map<String, Object>> col = grid.addColumn(row -> {
                    Object valObj = getCaseInsensitiveVal(row, colName);
                    return ComponentFactory.formatFieldValueWithLov(targetField, valObj, dataService);
                }).setHeader(colHeader).setAutoWidth(true).setResizable(true);

                if (targetField != null) {
                    col.setSortable(targetField.isSortable());
                }
            }
        } else {
            String table = lovMeta != null ? lovMeta.getTableName() : lovCode;
            List<String> allCols = dataService.getColumnsForQueryOrTable(table);
            for (String colName : allCols) {
                String header = colName.substring(0, 1).toUpperCase() + colName.substring(1).replace("_", " ");
                grid.addColumn(row -> {
                    Object valObj = getCaseInsensitiveVal(row, colName);
                    return valObj != null ? valObj.toString() : "";
                }).setHeader(header).setAutoWidth(true).setResizable(true);
            }
        }
    }

    private void loadData(String searchTerm) {
        List<Map<String, Object>> records = dataService.fetchLovDataWithActionFilters(
                actionMeta.getSourceLovCode(),
                actionMeta.getFilterMapping(),
                headerRecord,
                searchTerm
        );
        grid.setItems(records);
    }

    private Object getCaseInsensitiveVal(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        if (map.containsKey(key)) return map.get(key);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key)) return e.getValue();
        }
        return null;
    }
}
