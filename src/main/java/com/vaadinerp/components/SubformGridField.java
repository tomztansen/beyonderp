package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.util.FormulaEvaluator;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.*;
import java.util.stream.Collectors;

public class SubformGridField extends CustomField<List<Map<String, Object>>> {

    private final DynamicDataService dataService;
    private final FieldMeta fieldMeta;
    private FormMeta childFormDef;

    public FieldMeta getFieldMeta() {
        return fieldMeta;
    }

    private final List<Map<String, Object>> items = new ArrayList<>();
    private final List<Map<String, Object>> deletedItems = new ArrayList<>();
    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final Button btnAdd = new Button("Tambah Baris", VaadinIcon.PLUS.create());
    private final Button btnDelete = new Button("Delete Row", VaadinIcon.TRASH.create());
    private final HorizontalLayout extraActionsContainer = new HorizontalLayout();
    private final Map<String, Component> editorComponents = new HashMap<>();

    private final Map<String, FilterCriteria> filterValues = new HashMap<>();
    private final Map<Grid.Column<Map<String, Object>>, String> columnToFieldNameMap = new HashMap<>();
    private final Map<Grid.Column<Map<String, Object>>, java.util.function.Function<Map<String, Object>, String>> colGetterMap = new java.util.concurrent.ConcurrentHashMap<>();
    private Map<String, Object> draggedItem;

    // Registrations for listener deduplication
    private com.vaadin.flow.shared.Registration gridDragStartReg;
    private com.vaadin.flow.shared.Registration gridDropReg;
    private com.vaadin.flow.shared.Registration gridDragEndReg;
    private com.vaadin.flow.shared.Registration gridColReorderReg;
    private java.util.function.Supplier<Map<String, Object>> headerRecordSupplier;

    public void setHeaderRecordSupplier(java.util.function.Supplier<Map<String, Object>> supplier) {
        this.headerRecordSupplier = supplier;
    }

    public com.vaadin.flow.component.Component findParentView() {
        com.vaadin.flow.component.Component c = this;
        while (c != null) {
            if (c instanceof com.vaadinerp.views.GenericFormView || c instanceof com.vaadinerp.views.GenericMasterDetailFormView) {
                return c;
            }
            c = c.getParent().orElse(null);
        }
        return null;
    }

    public void setComponentEnabled(String fieldName, boolean enabled) {
        if (fieldName == null) return;
        String name = fieldName;
        if (name.startsWith("detail.") || name.startsWith("row.")) {
            name = name.substring(name.indexOf('.') + 1);
        }
        Component comp = editorComponents.get(name);
        if (comp != null && comp instanceof com.vaadin.flow.component.HasEnabled hasEnabled) {
            hasEnabled.setEnabled(enabled);
        }
    }

    public void setComponentReadOnly(String fieldName, boolean readOnly) {
        if (fieldName == null) return;
        String name = fieldName;
        if (name.startsWith("detail.") || name.startsWith("row.")) {
            name = name.substring(name.indexOf('.') + 1);
        }
        Component comp = editorComponents.get(name);
        if (comp != null) {
            com.vaadinerp.components.ComponentFactory.setComponentReadOnly(comp, readOnly);
        }
    }

    private static class FilterCriteria {
        String operator = "Contains";
        String value = "";
    }

    public SubformGridField(String label, FieldMeta fieldMeta, DynamicDataService dataService) {
        this.dataService = dataService;
        this.fieldMeta = fieldMeta;
        setLabel(label);

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(true);

        // Fetch child FormMeta
        if (fieldMeta.getLovCode() != null) {
            this.childFormDef = dataService.getFormMetaRepository()
                    .findById(fieldMeta.getLovCode()).orElse(null);
        }

        // Toolbar
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setSpacing(true);

        // btnAdd.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
        // ButtonVariant.LUMO_SMALL);
        // btnAdd.getStyle().set("font-weight", "500").set("color", "#374151");
        // if (btnAdd.getIcon() instanceof com.vaadin.flow.component.icon.Icon icAdd) {
        // icAdd.getStyle().set("color", "#22c55e").set("font-size", "1.1rem");
        // }

        // btnDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
        // ButtonVariant.LUMO_SMALL);
        // btnDelete.getStyle().set("font-weight", "500").set("color", "#374151");
        // if (btnDelete.getIcon() instanceof com.vaadin.flow.component.icon.Icon icDel)
        // {
        // icDel.getStyle().set("color", "#ef4444").set("font-size", "1.1rem");
        // }

        // Button btnResetSubformGrid = new Button("Reset Layout Grid",
        // VaadinIcon.ROTATE_LEFT.create());
        // btnResetSubformGrid.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
        // ButtonVariant.LUMO_SMALL);
        // btnResetSubformGrid.getStyle().set("font-weight", "500").set("color",
        // "#374151");
        // if (btnResetSubformGrid.getIcon() instanceof
        // com.vaadin.flow.component.icon.Icon icReset) {
        // icReset.getStyle().set("color", "#3b82f6").set("font-size", "1.1rem");
        // }
        // btnResetSubformGrid.addClickListener(e -> {
        // if (childFormDef != null) {
        // dataService.resetUserGridOrder(childFormDef.getFormCode(), "subformGrid");
        // grid.setSelectionMode(Grid.SelectionMode.MULTI);
        // columnToFieldNameMap.clear();
        // colGetterMap.clear();
        // editorComponents.clear();
        // filterValues.clear();
        // buildGridColumns();
        // Notification.show("Layout grid subform dikembalikan ke default!", 2000,
        // Notification.Position.BOTTOM_END);
        // }
        // });

        // extraActionsContainer.setSpacing(true);

        btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        btnDelete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        Button btnResetSubformGrid = new Button("Reset Layout Grid", VaadinIcon.ROTATE_LEFT.create());
        btnResetSubformGrid.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnResetSubformGrid.addClickListener(e -> {
            if (childFormDef != null) {
                dataService.resetUserGridOrder(childFormDef.getFormCode(), "subformGrid");
                grid.setSelectionMode(Grid.SelectionMode.MULTI);
                columnToFieldNameMap.clear();
                colGetterMap.clear();
                editorComponents.clear();
                filterValues.clear();
                buildGridColumns();
                Notification.show("Layout grid subform dikembalikan ke default!", 2000,
                        Notification.Position.BOTTOM_END);
            }
        });

        extraActionsContainer.setSpacing(true);

        toolbar.add(btnAdd, btnDelete, extraActionsContainer);

        refreshExtraActions();

        com.vaadin.flow.component.html.Anchor btnExportSubformExcel = com.vaadinerp.components.StandardGridUtils
                .createExportExcelButton(grid,
                        this.fieldMeta != null && this.fieldMeta.getFieldName() != null
                                ? this.fieldMeta.getFieldName() + "_export"
                                : "subform_export",
                        colGetterMap);
        btnExportSubformExcel.getStyle().set("margin-left", "auto");
        toolbar.add(btnExportSubformExcel, btnResetSubformGrid);

        // Setup Grid
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setMultiSort(true);
        grid.setAllRowsVisible(true);
        grid.setPageSize(500);
        grid.setDataProvider(createDataProvider());
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(grid);

        if (childFormDef != null) {
            buildGridColumns();
        }

        layout.add(toolbar, grid);
        add(layout);
        setupGridListeners();
    }

    public void refreshExtraActions() {
        extraActionsContainer.removeAll();
        if (dataService == null)
            return;
        List<com.vaadinerp.meta.FormActionMeta> actions = new ArrayList<>();
        if (fieldMeta.getLovCode() != null) {
            actions.addAll(dataService.getFormActions(fieldMeta.getLovCode(), "DETAIL_TOOLBAR"));
        }
        if (fieldMeta.getFormMeta() != null && fieldMeta.getFormMeta().getFormCode() != null) {
            for (com.vaadinerp.meta.FormActionMeta act : dataService
                    .getFormActions(fieldMeta.getFormMeta().getFormCode(), "DETAIL_TOOLBAR")) {
                if (actions.stream().noneMatch(a -> (a.getId() != null && a.getId().equals(act.getId()))
                        || (a.getActionCode() != null && a.getActionCode().equalsIgnoreCase(act.getActionCode())))) {
                    actions.add(act);
                }
            }
        }
        java.util.Map<String, java.util.List<com.vaadinerp.meta.FormActionMeta>> groupedActions = new java.util.LinkedHashMap<>();
        List<com.vaadinerp.meta.FormActionMeta> standaloneActions = new ArrayList<>();

        for (com.vaadinerp.meta.FormActionMeta act : actions) {
            if (act.getMenuGroup() != null && !act.getMenuGroup().isBlank()) {
                groupedActions.computeIfAbsent(act.getMenuGroup().trim(), k -> new ArrayList<>()).add(act);
            } else {
                standaloneActions.add(act);
            }
        }

        for (com.vaadinerp.meta.FormActionMeta act : standaloneActions) {
            com.vaadin.flow.component.icon.Icon icon = null;
            if (act.getIconName() != null && !act.getIconName().isBlank()) {
                try {
                    icon = com.vaadin.flow.component.icon.VaadinIcon.valueOf(act.getIconName().toUpperCase()).create();
                } catch (Exception ignored) {
                }
            }
            if (icon != null) {
                icon.getStyle().set("color", "white").set("font-size", "1.1rem");
            }
            Button actBtn = icon != null ? new Button(act.getActionLabel(), icon) : new Button(act.getActionLabel());
            actBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            actBtn.addClickListener(e -> {
                if (grid.getEditor().isOpen()) {
                    grid.getEditor().cancel();
                }
                Map<String, Object> headerBean = headerRecordSupplier != null && headerRecordSupplier.get() != null
                        ? headerRecordSupplier.get()
                        : new HashMap<>();
                DynamicPickerPopupDialog dlg = new DynamicPickerPopupDialog(act, dataService, headerBean,
                        selectedRecords -> {
                            for (Map<String, Object> srcRec : selectedRecords) {
                                Map<String, Object> newRow = new HashMap<>();
                                newRow.put("_tempId", java.util.UUID.randomUUID().toString());
                                newRow.put("lineno", getMaxLineNoFromItems(items));
                                applyTargetMapping(newRow, srcRec, act.getTargetMapping());
                                items.add(newRow);
                            }
                            grid.getDataProvider().refreshAll();
                        });
                dlg.open();
            });
            extraActionsContainer.add(actBtn);
        }

        for (Map.Entry<String, List<com.vaadinerp.meta.FormActionMeta>> entry : groupedActions.entrySet()) {
            com.vaadin.flow.component.menubar.MenuBar menuBar = new com.vaadin.flow.component.menubar.MenuBar();
            menuBar.addThemeVariants(com.vaadin.flow.component.menubar.MenuBarVariant.LUMO_SMALL,
                    com.vaadin.flow.component.menubar.MenuBarVariant.LUMO_PRIMARY);

            HorizontalLayout menuBtnLayout = new HorizontalLayout();
            menuBtnLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
            menuBtnLayout.setSpacing(true);
            com.vaadin.flow.component.icon.Icon grpIcon = VaadinIcon.FILE_TEXT.create();
            grpIcon.getStyle().set("color", "white").set("font-size", "1.1rem");
            com.vaadin.flow.component.icon.Icon chevronIcon = VaadinIcon.CHEVRON_DOWN.create();
            chevronIcon.setSize("12px");
            chevronIcon.getStyle().set("color", "white");
            com.vaadin.flow.component.html.Span grpSpan = new com.vaadin.flow.component.html.Span(entry.getKey());
            grpSpan.getStyle().set("font-weight", "500").set("color", "white");
            menuBtnLayout.add(grpIcon, grpSpan, chevronIcon);
            com.vaadin.flow.component.contextmenu.MenuItem parentItem = menuBar.addItem(menuBtnLayout);

            com.vaadin.flow.component.contextmenu.SubMenu subMenu = parentItem.getSubMenu();
            for (com.vaadinerp.meta.FormActionMeta act : entry.getValue()) {
                com.vaadin.flow.component.icon.Icon icon = null;
                if (act.getIconName() != null && !act.getIconName().isBlank()) {
                    try {
                        icon = VaadinIcon.valueOf(act.getIconName().toUpperCase()).create();
                    } catch (Exception ignored) {
                    }
                }
                if (icon == null) {
                    icon = VaadinIcon.FILE_TEXT.create();
                }
                icon.getStyle().set("color", "#2563eb").set("font-size", "1rem");
                subMenu.addItem(
                        new HorizontalLayout(icon, new com.vaadin.flow.component.html.Span(act.getActionLabel())),
                        e -> {
                            if (grid.getEditor().isOpen()) {
                                grid.getEditor().cancel();
                            }
                            Map<String, Object> headerBean = headerRecordSupplier != null
                                    && headerRecordSupplier.get() != null ? headerRecordSupplier.get()
                                            : new HashMap<>();
                            DynamicPickerPopupDialog dlg = new DynamicPickerPopupDialog(act, dataService, headerBean,
                                    selectedRecords -> {
                                        for (Map<String, Object> srcRec : selectedRecords) {
                                            Map<String, Object> newRow = new HashMap<>();
                                            newRow.put("_tempId", java.util.UUID.randomUUID().toString());
                                            newRow.put("lineno", getMaxLineNoFromItems(items));
                                            applyTargetMapping(newRow, srcRec, act.getTargetMapping());
                                            items.add(newRow);
                                        }
                                        grid.getDataProvider().refreshAll();
                                    });
                            dlg.open();
                        });
            }
            extraActionsContainer.add(menuBar);
        }
    }

    private void setupGridListeners() {
        grid.addItemClickListener(event -> {
            if (isReadOnly()) {
                return;
            }
            Map<String, Object> item = event.getItem();
            if (item != null) {
                Editor<Map<String, Object>> editor = grid.getEditor();
                if (editor.isOpen() && editor.getItem() == item) {
                    return; // already editing
                }
                editor.editItem(item);
            }
        });

        btnAdd.addClickListener(e -> {
            if (grid.getEditor().isOpen()) {
                grid.getEditor().cancel();
            }
            Map<String, Object> newRow = new HashMap<>();
            newRow.put("_tempId", java.util.UUID.randomUUID().toString());

            int currentRowIndex = items.size() + 1;
            Map<String, Object> headerData = null;
            if (headerRecordSupplier != null) {
                headerData = headerRecordSupplier.get();
            }
            if (dataService != null && dataService.getScriptExecutorService() != null && this.fieldMeta != null
                    && this.fieldMeta.getOnAddScript() != null) {
                try {
                    dataService.getScriptExecutorService().executeOnAddScript(
                            this.fieldMeta, newRow, currentRowIndex, headerData, items, this);
                } catch (Exception ex) {
                    com.vaadin.flow.component.notification.Notification.show(
                            "Error eksekusi On-Add-Row script: " + ex.getMessage(),
                            4000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
                }
            }

            items.add(newRow);
            grid.getDataProvider().refreshAll();

            grid.getEditor().editItem(newRow);

            Component firstCompToFocus = null;
            if (childFormDef != null && !childFormDef.getFields().isEmpty()) {
                String firstFieldName = null;
                List<String> userOrder = dataService.getUserGridOrder(childFormDef.getFormCode(), "subformGrid");
                if (userOrder != null && !userOrder.isEmpty()) {
                    for (String fname : userOrder) {
                        if (editorComponents.containsKey(fname) && editorComponents.get(fname) != null) {
                            firstFieldName = fname;
                            break;
                        }
                    }
                }
                if (firstFieldName == null) {
                    List<FieldMeta> childFields = childFormDef.getFields().stream()
                            .filter(f -> f.isShowInGrid() && !f.isHideInForm()
                                    && !"HIDDEN".equalsIgnoreCase(f.getComponentType()))
                            .collect(Collectors.toList());

                    childFields.sort((f1, f2) -> {
                        Integer o1 = f1.getColOrder() != null ? f1.getColOrder() : Integer.MAX_VALUE;
                        Integer o2 = f2.getColOrder() != null ? f2.getColOrder() : Integer.MAX_VALUE;
                        return o1.compareTo(o2);
                    });

                    if (!childFields.isEmpty()) {
                        firstFieldName = childFields.get(0).getFieldName();
                    }
                }
                if (firstFieldName != null) {
                    firstCompToFocus = editorComponents.get(firstFieldName);
                }
            }

            UI.getCurrent().getPage().executeJs(
                    "var grid = $0; var targetIdx = $1; var firstEditor = $2; " +
                            "if (grid && typeof grid.scrollToIndex === 'function') { " +
                            "  grid.scrollToIndex(targetIdx); " +
                            "} " +
                            "if (firstEditor) { " +
                            "  function waitAndFocus(attempt) { " +
                            "    var isAttached = firstEditor.isConnected; " +
                            "    if (!isAttached && document.body.contains(firstEditor)) isAttached = true; " +
                            "    if (isAttached) { " +
                            "      setTimeout(function() { " +
                            "        try { " +
                            "          var focusable = firstEditor.querySelector('vaadin-text-field:not([readonly]), vaadin-combo-box:not([readonly]), vaadin-integer-field:not([readonly]), vaadin-big-decimal-field:not([readonly]), vaadin-button, input:not([readonly])'); "
                            +
                            "          var target = focusable || firstEditor.focusElement || firstEditor.inputElement || "
                            +
                            "             (firstEditor.shadowRoot && firstEditor.shadowRoot.querySelector('input:not([readonly]),textarea:not([readonly])')) || "
                            +
                            "             firstEditor; " +
                            "          if (target && typeof target.focus === 'function') { target.focus(); } " +
                            "        } catch(e){} " +
                            "      }, 150); " +
                            "    } else { " +
                            "      if (attempt < 60) setTimeout(function(){ waitAndFocus(attempt + 1); }, 50); " +
                            "    } " +
                            "  } " +
                            "  waitAndFocus(0); " +
                            "}",
                    grid.getElement(), items.size() - 1,
                    firstCompToFocus != null ? firstCompToFocus.getElement() : null);

            updateValue();
        });

        btnDelete.addClickListener(e -> {
            java.util.Set<Map<String, Object>> selectedItems = grid.getSelectedItems();
            if (selectedItems != null && !selectedItems.isEmpty()) {
                if (grid.getEditor().isOpen()) {
                    grid.getEditor().cancel();
                }
                items.removeAll(selectedItems);
                if (childFormDef != null) {
                    String pk = childFormDef.getPrimaryKey() != null ? childFormDef.getPrimaryKey() : "id";
                    for (Map<String, Object> selected : selectedItems) {
                        if (selected.containsKey(pk) && selected.get(pk) != null
                                && !selected.get(pk).toString().trim().isEmpty()) {
                            deletedItems.add(selected);
                        }
                    }
                }
                grid.getDataProvider().refreshAll();
                grid.deselectAll();
                updateValue();
            } else {
                Notification.show("Pilih baris rincian terlebih dahulu.", 3000, Notification.Position.MIDDLE);
            }
        });
    }

    private void applyFilters() {
        if (!(grid.getDataProvider() instanceof ListDataProvider)) {
            return;
        }
        @SuppressWarnings("unchecked")
        ListDataProvider<Map<String, Object>> dp = (ListDataProvider<Map<String, Object>>) grid.getDataProvider();

        dp.setFilter(item -> {
            for (Map.Entry<String, FilterCriteria> entry : filterValues.entrySet()) {
                String fieldName = entry.getKey();
                FilterCriteria criteria = entry.getValue();

                String op = criteria.operator;
                String query = criteria.value;

                Object val = getCaseInsensitiveVal(item, fieldName);
                String strVal = val != null ? val.toString().toLowerCase() : "";

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

                if (query == null || query.trim().isEmpty()) {
                    continue;
                }

                query = query.toLowerCase();

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
                }
            }
            return true;
        });
    }

    private int findIndexByReference(List<Map<String, Object>> list, Map<String, Object> item) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

    private void buildGridColumns() {
        if (gridDragStartReg != null)
            gridDragStartReg.remove();
        if (gridDropReg != null)
            gridDropReg.remove();
        if (gridDragEndReg != null)
            gridDragEndReg.remove();
        if (gridColReorderReg != null)
            gridColReorderReg.remove();

        com.vaadinerp.components.StandardGridUtils.cleanGridBeforeRebuild(grid);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setMultiSort(true);

        List<FieldMeta> childFields = childFormDef.getFields().stream()
                .filter(FieldMeta::isShowInGrid)
                .collect(Collectors.toList());

        // Sort by colOrder
        childFields.sort((f1, f2) -> {
            Integer o1 = f1.getColOrder() != null ? f1.getColOrder() : Integer.MAX_VALUE;
            Integer o2 = f2.getColOrder() != null ? f2.getColOrder() : Integer.MAX_VALUE;
            return o1.compareTo(o2);
        });

        Editor<Map<String, Object>> editor = grid.getEditor();
        Binder<Map<String, Object>> gridBinder = new Binder<>();
        editor.setBinder(gridBinder);
        editor.setBuffered(false);

        Map<String, Grid.Column<Map<String, Object>>> columnsMap = new LinkedHashMap<>();

        java.util.function.BiConsumer<String, Object> subformUpdateFieldValue = (targetFieldName, val) -> {
            Component targetComp = editorComponents.get(targetFieldName);
            if (targetComp == null) {
                String cleanTarget = targetFieldName != null ? targetFieldName.replaceAll("[_\\s-]+", "") : "";
                for (java.util.Map.Entry<String, Component> e : editorComponents.entrySet()) {
                    if (e.getKey() != null && (e.getKey().equalsIgnoreCase(targetFieldName)
                            || e.getKey().replaceAll("[_\\s-]+", "").equalsIgnoreCase(cleanTarget))) {
                        targetComp = e.getValue();
                        break;
                    }
                }
            }
            if (targetComp instanceof HasValue) {
                @SuppressWarnings("unchecked")
                HasValue<?, Object> hv = (HasValue<?, Object>) targetComp;
                Object convertedVal = convertToFieldValue(val, targetComp);
                if (convertedVal != null && !convertedVal.equals(hv.getValue())
                        || (convertedVal == null && hv.getValue() != null)) {
                    hv.setValue(convertedVal);
                }
            }
            if (grid.getEditor().isOpen() && grid.getEditor().getItem() != null) {
                Map<String, Object> activeItem = grid.getEditor().getItem();
                activeItem.put(targetFieldName, val);
                evaluateRowFormulas(activeItem);
            }
        };

        for (FieldMeta field : childFields) {
            String fieldName = field.getFieldName();
            java.util.function.Function<Map<String, Object>, String> valueGetter = map -> {
                String formatted = ComponentFactory.formatFieldValueWithLov(field,
                        getCaseInsensitiveVal(map, fieldName), dataService);
                return formatted != null ? formatted : "";
            };
            Grid.Column<Map<String, Object>> col = grid
                    .addColumn(valueGetter::apply)
                    .setHeader(field.getFieldLabel())
                    .setAutoWidth(true)
                    .setFlexGrow(1)
                    .setResizable(true)
                    .setKey(fieldName);
            colGetterMap.put(col, valueGetter);

            if (field.isHideInForm() || "HIDDEN".equalsIgnoreCase(field.getComponentType())) {
                col.setVisible(false);
            }

            columnsMap.put(fieldName, col);
            columnToFieldNameMap.put(col, fieldName);

            // Editor component
            Component editorComp = ComponentFactory.create(field, dataService, subformUpdateFieldValue, true);
            col.setEditorComponent(editorComp);
            editorComponents.put(fieldName, editorComp);

            if (field.getFilters() != null) {
                for (com.vaadinerp.meta.FieldFilterMeta filter : field.getFilters()) {
                    if ("STATIC".equalsIgnoreCase(filter.getSourceType())) {
                        Object staticVal = filter.getSourceName();
                        String lookupKey = staticVal != null ? staticVal.toString() : "";
                        if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.") || lookupKey.startsWith("detail.") || lookupKey.startsWith("\"detail.")) {
                            if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.")) {
                                lookupKey = lookupKey.replaceAll("[\"']", "").substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                            } else {
                                lookupKey = lookupKey.replaceAll("[\"']", "").substring(lookupKey.indexOf("detail.") + "detail.".length()).trim();
                            }
                            if (headerRecordSupplier != null && headerRecordSupplier.get() != null) {
                                Object hv = getCaseInsensitiveVal(headerRecordSupplier.get(), lookupKey);
                                if (hv == null && ("unique".equalsIgnoreCase(lookupKey) || "isunique".equalsIgnoreCase(lookupKey))) {
                                    hv = getCaseInsensitiveVal(headerRecordSupplier.get(), "unique".equalsIgnoreCase(lookupKey) ? "isunique" : "unique");
                                }
                                if (hv != null) staticVal = hv;
                            }
                        }
                        FilterCondition condition = new FilterCondition(String.valueOf(filter.getId()),
                                filter.getFilterColumn(), staticVal, filter.getLogicalOperator(),
                                filter.getComparisonOperator());
                        applyFilterToSubformEditor(editorComp, condition);
                    } else if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                        String sourceFieldName = filter.getSourceName();
                        String lookupKey = sourceFieldName;
                        if (lookupKey != null && (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header."))) {
                            lookupKey = lookupKey.replaceAll("[\"']", "").substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                        } else if (lookupKey != null && (lookupKey.startsWith("detail.") || lookupKey.startsWith("\"detail."))) {
                            lookupKey = lookupKey.replaceAll("[\"']", "").substring(lookupKey.indexOf("detail.") + "detail.".length()).trim();
                        }
                        Component sourceComp = lookupKey != null ? editorComponents.get(lookupKey) : null;
                        if (sourceComp == null && sourceFieldName != null) sourceComp = editorComponents.get(sourceFieldName);
                        if (sourceComp == null && ("unique".equalsIgnoreCase(lookupKey) || "isunique".equalsIgnoreCase(lookupKey))) {
                            sourceComp = editorComponents.get("unique");
                            if (sourceComp == null) sourceComp = editorComponents.get("isunique");
                        }
                        if (sourceComp instanceof HasValue) {
                            Object val = ((HasValue<?, ?>) sourceComp).getValue();
                            if (val != null) {
                                FilterCondition condition = new FilterCondition(String.valueOf(filter.getId()),
                                        filter.getFilterColumn(), val, filter.getLogicalOperator(),
                                        filter.getComparisonOperator());
                                applyFilterToSubformEditor(editorComp, condition);
                            }
                        } else {
                            Object fallbackVal = filter.getSourceName();
                            if (headerRecordSupplier != null && headerRecordSupplier.get() != null && lookupKey != null) {
                                Object hv = getCaseInsensitiveVal(headerRecordSupplier.get(), lookupKey);
                                if (hv == null && ("unique".equalsIgnoreCase(lookupKey) || "isunique".equalsIgnoreCase(lookupKey))) {
                                    hv = getCaseInsensitiveVal(headerRecordSupplier.get(), "unique".equalsIgnoreCase(lookupKey) ? "isunique" : "unique");
                                }
                                if (hv != null) fallbackVal = hv;
                            }
                            FilterCondition condition = new FilterCondition(String.valueOf(filter.getId()),
                                    filter.getFilterColumn(), fallbackVal, filter.getLogicalOperator(),
                                    filter.getComparisonOperator());
                            applyFilterToSubformEditor(editorComp, condition);
                        }
                    }
                }
            }

            @SuppressWarnings("unchecked")
            HasValue<?, Object> hasValue = (HasValue<?, Object>) editorComp;
            Binder.BindingBuilder<Map<String, Object>, Object> builder = gridBinder.forField(hasValue);
            if (field.isRequired()) {
                builder.asRequired(field.getFieldLabel() + " wajib diisi");
            }
            builder.bind(map -> convertToFieldValue(getCaseInsensitiveVal(map, fieldName), editorComp),
                    (map, val) -> {
                        putCaseInsensitiveVal(map, fieldName, val);
                        evaluateRowFormulas(map);
                        if (!grid.getEditor().isOpen() || grid.getEditor().getItem() != map) {
                            if (grid.getDataProvider() instanceof ListDataProvider) {
                                @SuppressWarnings("unchecked")
                                ListDataProvider<Map<String, Object>> dp = (ListDataProvider<Map<String, Object>>) grid
                                        .getDataProvider();
                                dp.refreshItem(map);
                            }
                        }
                        updateValue();
                    });

            // Setup Comparator and Sortable AFTER editor binding to prevent override
            col.setComparator((map1, map2) -> {
                Object val1 = getCaseInsensitiveVal(map1, fieldName);
                Object val2 = getCaseInsensitiveVal(map2, fieldName);
                if (val1 == null && val2 == null)
                    return 0;
                if (val1 == null)
                    return -1;
                if (val2 == null)
                    return 1;
                String lovCode = field.getLovCode();
                if (lovCode != null && !lovCode.trim().isEmpty()) {
                    String s1 = ComponentFactory.formatFieldValueWithLov(field, val1, dataService);
                    String s2 = ComponentFactory.formatFieldValueWithLov(field, val2, dataService);
                    return s1.compareToIgnoreCase(s2);
                }
                if (val1 instanceof Comparable && val2 instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> comp1 = (Comparable<Object>) val1;
                    return comp1.compareTo(val2);
                }
                return val1.toString().compareTo(val2.toString());
            });
            col.setSortable(true);
        }

        // 2. Setup Header Filter Row
        HeaderRow filterRow = grid.appendHeaderRow();
        columnsMap.forEach((fieldName, col) -> {
            FilterCriteria criteria = new FilterCriteria();
            filterValues.put(fieldName, criteria);

            TextField filterField = new TextField();
            filterField.setPlaceholder("Filter...");
            filterField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
            filterField.setWidthFull();
            filterField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL);

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

            com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<MenuItem>> listener = event -> {
                if (event.getSource().getText() != null) {
                    criteria.operator = event.getSource().getText();
                    applyOperatorUI.run();
                    applyFilters();
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

            contextMenu.addItem(new com.vaadin.flow.component.html.Hr(), e -> {
            });
            contextMenu.addItem(col.isFrozen() ? "Unfreeze Column" : "Freeze Column", event -> {
                boolean nextFrozen = !col.isFrozen();
                col.setFrozen(nextFrozen);
                event.getSource().setText(nextFrozen ? "Unfreeze Column" : "Freeze Column");
                com.vaadin.flow.component.notification.Notification.show(
                        nextFrozen ? "Kolom dibekukan" : "Kolom dilepas", 2000,
                        com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END);
            });

            filterField.addValueChangeListener(e -> {
                if (grid.getEditor().isOpen()) {
                    grid.getEditor().cancel();
                }
                criteria.value = e.getValue();
                applyFilters();
            });

            filterRow.getCell(col).setComponent(filterField);
        });

        // 3. Row Drag and Drop
        grid.setRowsDraggable(true);
        gridDragStartReg = grid.addDragStartListener(event -> {
            if (!event.getDraggedItems().isEmpty()) {
                draggedItem = event.getDraggedItems().get(0);
                grid.setDropMode(com.vaadin.flow.component.grid.dnd.GridDropMode.BETWEEN);
            }
        });

        gridDropReg = grid.addDropListener(event -> {
            Map<String, Object> targetItem = event.getDropTargetItem().orElse(null);
            if (targetItem != null && draggedItem != null && targetItem != draggedItem) {
                int indexDragged = findIndexByReference(items, draggedItem);
                int indexTarget = findIndexByReference(items, targetItem);

                if (indexDragged >= 0 && indexTarget >= 0) {
                    items.remove(indexDragged);
                    int newIndex = findIndexByReference(items, targetItem);
                    if (event.getDropLocation() == com.vaadin.flow.component.grid.dnd.GridDropLocation.BELOW) {
                        items.add(newIndex + 1, draggedItem);
                    } else {
                        items.add(newIndex, draggedItem);
                    }
                    grid.getDataProvider().refreshAll();
                    updateValue();
                }
            }
            draggedItem = null;
        });

        gridDragEndReg = grid.addDragEndListener(event -> {
            draggedItem = null;
            grid.setDropMode(null);
        });

        // 4. Column Reordering
        grid.setColumnReorderingAllowed(true);
        gridColReorderReg = grid.addColumnReorderListener(event -> {
            List<Grid.Column<Map<String, Object>>> newOrder = event.getColumns();
            List<String> orderedFieldNames = new ArrayList<>();
            for (Grid.Column<Map<String, Object>> col : newOrder) {
                String fieldName = columnToFieldNameMap.get(col);
                if (fieldName != null) {
                    orderedFieldNames.add(fieldName);
                }
            }
            try {
                dataService.saveUserGridOrder(childFormDef.getFormCode(), "subformGrid", orderedFieldNames);
                Notification.show("Urutan kolom detail disimpan", 1500, Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000,
                        Notification.Position.MIDDLE);
            }
        });

        if (grid.getDataProvider() != null) {
            grid.getDataProvider().refreshAll();
        }
        List<String> subformUserOrder = dataService.getUserGridOrder(childFormDef.getFormCode(), "subformGrid");
        com.vaadinerp.components.StandardGridUtils.applySafeColumnOrder(grid, columnToFieldNameMap, subformUserOrder);
    }

    private Object convertToFieldValue(Object rawVal, Component comp) {
        if (rawVal == null) {
            if (comp instanceof TextField || comp instanceof com.vaadin.flow.component.textfield.TextArea) {
                return "";
            }
            if (comp instanceof com.vaadin.flow.component.checkbox.Checkbox) {
                return Boolean.FALSE;
            }
            return null;
        }
        if (comp instanceof com.vaadin.flow.component.checkbox.Checkbox) {
            if (rawVal instanceof Boolean) {
                return rawVal;
            }
            if (rawVal instanceof Number) {
                return ((Number) rawVal).intValue() != 0;
            }
            String str = rawVal.toString().trim().toLowerCase();
            return "true".equals(str) || "1".equals(str) || "t".equals(str) || "yes".equals(str) || "y".equals(str)
                    || "on".equals(str);
        }
        if (comp instanceof TextField || comp instanceof com.vaadin.flow.component.textfield.TextArea ||
                comp instanceof com.vaadin.flow.component.combobox.ComboBox ||
                comp instanceof com.vaadin.flow.component.select.Select ||
                comp instanceof com.vaadin.flow.component.listbox.ListBox ||
                comp instanceof com.vaadin.flow.component.radiobutton.RadioButtonGroup) {
            return rawVal.toString();
        }
        if (comp instanceof com.vaadin.flow.component.textfield.IntegerField
                || comp instanceof com.vaadinerp.components.FormattedIntegerField) {
            if (rawVal instanceof Number) {
                return ((Number) rawVal).intValue();
            }
            try {
                return Integer.parseInt(rawVal.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (comp instanceof com.vaadin.flow.component.textfield.BigDecimalField
                || comp instanceof com.vaadinerp.components.FormattedBigDecimalField) {
            if (rawVal instanceof java.math.BigDecimal) {
                return rawVal;
            }
            try {
                return new java.math.BigDecimal(rawVal.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (comp instanceof com.vaadin.flow.component.datepicker.DatePicker) {
            if (rawVal instanceof java.time.LocalDate) {
                return rawVal;
            }
            if (rawVal instanceof java.sql.Date) {
                return ((java.sql.Date) rawVal).toLocalDate();
            }
            if (rawVal instanceof java.util.Date) {
                return new java.sql.Date(((java.util.Date) rawVal).getTime()).toLocalDate();
            }
            try {
                return java.time.LocalDate.parse(rawVal.toString());
            } catch (Exception e) {
                return null;
            }
        }
        return rawVal;
    }

    public List<Map<String, Object>> getDeletedValues() {
        return new ArrayList<>(deletedItems);
    }

    public List<Map<String, Object>> getItems() {
        return new ArrayList<>(items);
    }

    public void setParentFieldValue(String parentFieldName, Object value) {
        if (childFormDef == null || parentFieldName == null)
            return;
        for (FieldMeta field : childFormDef.getFields()) {
            if (field.getFilters() != null) {
                for (com.vaadinerp.meta.FieldFilterMeta filter : field.getFilters()) {
                    if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                        Component editorComp = editorComponents.get(field.getFieldName());
                        if (editorComp != null) {
                            String srcName = filter.getSourceName();
                            String lookupKey = srcName;
                            if (lookupKey != null && (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header."))) {
                                lookupKey = lookupKey.replaceAll("[\"']", "").substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                            } else if (lookupKey != null && (lookupKey.startsWith("detail.") || lookupKey.startsWith("\"detail."))) {
                                lookupKey = lookupKey.replaceAll("[\"']", "").substring(lookupKey.indexOf("detail.") + "detail.".length()).trim();
                            }
                            boolean isMatch = parentFieldName.equalsIgnoreCase(srcName)
                                    || (lookupKey != null && parentFieldName.equalsIgnoreCase(lookupKey))
                                    || ("unique".equalsIgnoreCase(parentFieldName) && "isunique".equalsIgnoreCase(lookupKey))
                                    || ("isunique".equalsIgnoreCase(parentFieldName) && "unique".equalsIgnoreCase(lookupKey));

                            if (isMatch) {
                                FilterCondition condition = new FilterCondition(String.valueOf(filter.getId()),
                                        filter.getFilterColumn(), value, filter.getLogicalOperator(),
                                        filter.getComparisonOperator());
                                applyFilterToSubformEditor(editorComp, condition);
                            } else if (lookupKey != null && editorComponents.get(lookupKey) == null && (srcName == null || editorComponents.get(srcName) == null)) {
                                Object fallbackVal = srcName;
                                boolean foundInSupplier = false;
                                if (headerRecordSupplier != null && headerRecordSupplier.get() != null) {
                                    Object hv = getCaseInsensitiveVal(headerRecordSupplier.get(), lookupKey);
                                    if (hv == null && ("unique".equalsIgnoreCase(lookupKey) || "isunique".equalsIgnoreCase(lookupKey))) {
                                        hv = getCaseInsensitiveVal(headerRecordSupplier.get(), "unique".equalsIgnoreCase(lookupKey) ? "isunique" : "unique");
                                    }
                                    if (hv != null) {
                                        fallbackVal = hv;
                                        foundInSupplier = true;
                                    }
                                }
                                if (foundInSupplier) {
                                    FilterCondition condition = new FilterCondition(String.valueOf(filter.getId()),
                                            filter.getFilterColumn(), fallbackVal, filter.getLogicalOperator(),
                                            filter.getComparisonOperator());
                                    applyFilterToSubformEditor(editorComp, condition);
                                }
                            }
                        }
                    } else if ("STATIC".equalsIgnoreCase(filter.getSourceType())) {
                        Component editorComp = editorComponents.get(field.getFieldName());
                        if (editorComp != null) {
                            Object staticVal = filter.getSourceName();
                            String lookupKey = staticVal != null ? staticVal.toString() : "";
                            if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.") || lookupKey.startsWith("detail.") || lookupKey.startsWith("\"detail.")) {
                                if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.")) {
                                    lookupKey = lookupKey.replaceAll("[\"']", "").substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                                } else {
                                    lookupKey = lookupKey.replaceAll("[\"']", "").substring(lookupKey.indexOf("detail.") + "detail.".length()).trim();
                                }
                                if (headerRecordSupplier != null && headerRecordSupplier.get() != null) {
                                    Object hv = getCaseInsensitiveVal(headerRecordSupplier.get(), lookupKey);
                                    if (hv == null && ("unique".equalsIgnoreCase(lookupKey) || "isunique".equalsIgnoreCase(lookupKey))) {
                                        hv = getCaseInsensitiveVal(headerRecordSupplier.get(), "unique".equalsIgnoreCase(lookupKey) ? "isunique" : "unique");
                                    }
                                    if (hv != null) staticVal = hv;
                                }
                            }
                            FilterCondition condition = new FilterCondition(String.valueOf(filter.getId()),
                                    filter.getFilterColumn(), staticVal, filter.getLogicalOperator(),
                                    filter.getComparisonOperator());
                            applyFilterToSubformEditor(editorComp, condition);
                        }
                    }
                }
            }
        }
    }

    private void applyFilterToSubformEditor(Component editorComp, FilterCondition condition) {
        if (editorComp == null)
            return;
        if (editorComp instanceof BandboxField) {
            ((BandboxField<?, ?>) editorComp).setFilterValue(condition);
        } else if (editorComp instanceof LovComboBox) {
            ((LovComboBox) editorComp).setFilterValue(condition);
        } else if (editorComp instanceof LovSelect) {
            ((LovSelect) editorComp).setFilterValue(condition);
        }
    }

    public void clearDeletedValues() {
        deletedItems.clear();
    }

    public boolean validateRows() {
        if (grid.getEditor().isOpen()) {
            boolean ok = grid.getEditor().getBinder().validate().isOk();
            if (!ok)
                return false;
        }
        if (childFormDef == null)
            return true;
        for (Map<String, Object> row : items) {
            for (FieldMeta field : childFormDef.getFields()) {
                if (field.isRequired()) {
                    Object val = getCaseInsensitiveVal(row, field.getFieldName());
                    if (val == null || val.toString().trim().isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected List<Map<String, Object>> generateModelValue() {
        return new ArrayList<>(items);
    }

    private void evaluateRowFormulas(Map<String, Object> row) {
        if (childFormDef == null)
            return;
        for (FieldMeta field : childFormDef.getFields()) {
            if (field.getFormula() != null && !field.getFormula().trim().isEmpty()) {
                try {
                    double calculated = FormulaEvaluator.evaluate(field.getFormula(), row);
                    row.put(field.getFieldName(), calculated);

                    Component editorComp = editorComponents.get(field.getFieldName());
                    if (editorComp instanceof com.vaadin.flow.component.HasValue) {
                        @SuppressWarnings("unchecked")
                        com.vaadin.flow.component.HasValue<?, Object> hasValue = (com.vaadin.flow.component.HasValue<?, Object>) editorComp;
                        Object converted = convertToFieldValue(calculated, editorComp);
                        if (converted != null && !converted.equals(hasValue.getValue())) {
                            hasValue.setValue(converted);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private ListDataProvider<Map<String, Object>> createDataProvider() {
        return new ListDataProvider<Map<String, Object>>(items) {
            @Override
            public Object getId(Map<String, Object> item) {
                if (item == null) {
                    return 0;
                }
                return System.identityHashCode(item);
            }
        };
    }

    @Override
    protected void setPresentationValue(List<Map<String, Object>> newPresentationValue) {
        items.clear();
        deletedItems.clear();
        if (newPresentationValue != null) {
            for (Map<String, Object> row : newPresentationValue) {
                if (row != null) {
                    evaluateRowFormulas(row);
                    items.add(row);
                }
            }
        }
        java.util.List<com.vaadin.flow.component.grid.GridSortOrder<Map<String, Object>>> currentSort = grid
                .getSortOrder();
        grid.setDataProvider(createDataProvider());
        if (currentSort != null && !currentSort.isEmpty()) {
            grid.sort(currentSort);
        }
        applyFilters();
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        boolean readOnly = isReadOnly();
        btnAdd.setEnabled(!readOnly);
        btnDelete.setEnabled(!readOnly);
        if (readOnly && grid.getEditor().isOpen()) {
            grid.getEditor().cancel();
        }
    }

    private void applyTargetMapping(Map<String, Object> destRow, Map<String, Object> srcRecord, String targetMapping) {
        if (targetMapping == null || targetMapping.trim().isEmpty())
            return;
        String clean = targetMapping.trim();
        if (clean.startsWith("{") && clean.endsWith("}")) {
            clean = clean.substring(1, clean.length() - 1).trim();
        }
        String[] pairs = FormulaEvaluator.splitTopLevelComma(clean);
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length < 2)
                kv = pair.split("=", 2);
            if (kv.length == 2) {
                String destCol = kv[0].replaceAll("[\"']", "").trim();
                String srcCol = kv[1].replaceAll("[\"']", "").trim();
                if (srcCol.toLowerCase().startsWith("source.")) {
                    srcCol = srcCol.substring(7);
                }
                if (destCol.toLowerCase().startsWith("detail.")) {
                    destCol = destCol.substring(7);
                }
                Object val = null;
                if (srcRecord != null && !srcCol.toLowerCase().contains("coalesce(") && !srcCol.toLowerCase().contains("ifnull(") && !srcCol.contains("+") && !srcCol.contains("*") && !srcCol.contains("/")) {
                    val = getCaseInsensitiveVal(srcRecord, srcCol);
                }
                if (val == null && srcCol != null && !srcCol.isBlank()) {
                    if (srcCol.toLowerCase().contains("coalesce(") || srcCol.toLowerCase().contains("ifnull(")
                            || ((srcCol.contains("+") || srcCol.contains("*") || srcCol.contains("/") || (srcCol.contains("-") && !srcCol.startsWith("-")))
                                && srcCol.matches(".*[a-zA-Z\\(\\)].*"))) {
                        val = FormulaEvaluator.evaluateTargetExpression(srcCol, srcRecord, destRow);
                    } else if ((srcCol.startsWith("'") && srcCol.endsWith("'")) || (srcCol.startsWith("\"") && srcCol.endsWith("\""))) {
                        if (srcCol.length() >= 2) val = srcCol.substring(1, srcCol.length() - 1);
                    } else if ("true".equalsIgnoreCase(srcCol) || "false".equalsIgnoreCase(srcCol)) {
                        val = Boolean.parseBoolean(srcCol);
                    } else if ("null".equalsIgnoreCase(srcCol)) {
                        val = null;
                    } else {
                        try {
                            if (srcCol.contains(".")) {
                                val = new java.math.BigDecimal(srcCol);
                            } else {
                                val = Integer.parseInt(srcCol);
                            }
                        } catch (Exception e) {
                            try {
                                val = Long.parseLong(srcCol);
                            } catch (Exception e2) {
                                // Bukan angka literal, biarkan val null
                            }
                        }
                    }
                }
                putCaseInsensitiveVal(destRow, destCol, val);
            }
        }
    }

    private int getMaxLineNoFromItems(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return 0;
        int max = 0;
        for (Map<String, Object> row : list) {
            if (row != null) {
                Object val = getCaseInsensitiveVal(row, "lineno");
                if (val == null) val = getCaseInsensitiveVal(row, "seq");
                if (val == null) val = getCaseInsensitiveVal(row, "no");
                if (val instanceof Number) {
                    if (((Number) val).intValue() > max) max = ((Number) val).intValue();
                } else if (val != null) {
                    try {
                        int v = Integer.parseInt(val.toString());
                        if (v > max) max = v;
                    } catch (Exception ignored) {}
                }
            }
        }
        return max;
    }

    private void putCaseInsensitiveVal(Map<String, Object> map, String key, Object value) {
        if (map == null || key == null)
            return;
        for (String k : map.keySet()) {
            if (k != null && k.equalsIgnoreCase(key)) {
                map.put(k, value);
                return;
            }
        }
        String cleanKey = key.replaceAll("[_\\s-]+", "");
        for (String k : map.keySet()) {
            if (k != null && k.replaceAll("[_\\s-]+", "").equalsIgnoreCase(cleanKey)) {
                map.put(k, value);
                return;
            }
        }
        map.put(key, value);
    }

    private Object getCaseInsensitiveVal(Map<String, Object> map, String key) {
        if (map == null || key == null)
            return null;
        if (map.containsKey(key))
            return map.get(key);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key))
                return e.getValue();
        }
        String cleanKey = key.replaceAll("[_\\s-]+", "");
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().replaceAll("[_\\s-]+", "").equalsIgnoreCase(cleanKey))
                return e.getValue();
        }
        return null;
    }
}
