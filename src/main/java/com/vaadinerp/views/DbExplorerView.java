package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Hr;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.service.DynamicDataService.TriggerDefinition;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import com.vaadinerp.components.StandardGridUtils;
import com.vaadinerp.components.StandardActionToolbar;
import com.vaadinerp.security.service.SessionSecurityService;

import java.util.List;
import java.util.Map;

@Route("explorer")
public class DbExplorerView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    private final ComboBox<String> tableSelect = new ComboBox<>("Pilih Tabel Dinamis");

    // Tab 1 Components (Data)
    private final Grid<Map<String, Object>> dataGrid = new Grid<>();
    private final Span recordCount = new Span("Pilih tabel untuk melihat data");

    // Tab 2 Components (Schema Structure, Triggers, & Constraints)
    private final Grid<Map<String, Object>> schemaGrid = new Grid<>();
    private final Grid<Map<String, Object>> triggersGrid = new Grid<>();
    private final Grid<Map<String, Object>> constraintsGrid = new Grid<>();

    private final Span schemaInfo = new Span("Pilih tabel untuk melihat struktur kolom");
    private final Span triggerInfo = new Span("Belum ada trigger terdaftar");
    private final Span constraintInfo = new Span("Belum ada constraint terdaftar");
    private final Button btnAddConstraint = new Button("Tambah Constraint", VaadinIcon.PLUS.create());
    private final Button btnAddTrigger = new Button("Tambah Trigger", VaadinIcon.PLUS.create());
    private final Button btnAddColumn = new Button("Tambah Kolom", VaadinIcon.PLUS.create());
    private final Button btnAddAuditCols = new Button("⚡ Tambah Kolom Audit Default", VaadinIcon.TIME_FORWARD.create());

    private List<Map<String, Object>> currentDataList = new ArrayList<>();
    private List<Map<String, Object>> currentSchemaList = new ArrayList<>();
    private List<Map<String, Object>> currentConstraintList = new ArrayList<>();
    private List<Map<String, Object>> currentTriggerList = new ArrayList<>();

    private String currentTable;
    private final SessionSecurityService securityService;

    private Runnable schemaFilterRefresher;
    private Runnable triggerFilterRefresher;
    private Runnable constraintFilterRefresher;
    private com.vaadin.flow.shared.Registration dataGridReorderReg;
    private final Map<Grid.Column<Map<String, Object>>, String> schemaColMap = new HashMap<>();
    private final Map<Grid.Column<Map<String, Object>>, String> triggerColMap = new HashMap<>();
    private final Map<Grid.Column<Map<String, Object>>, String> constraintColMap = new HashMap<>();

    private com.vaadinerp.components.PaginationBar paginationBar;
    private String currentSortField;
    private String currentSortDir;

    private static class FilterCriteria {
        String operator = "Contains";
        String value = "";
        public String getOperator() { return operator; }
        public String getValue() { return value; }
    }
    private final Map<String, FilterCriteria> dataFilterValues = new HashMap<>();

    public DbExplorerView(DynamicDataService dynamicDataService, SessionSecurityService securityService) {
        this.dynamicDataService = dynamicDataService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("Database Manager");
        title.getStyle().set("margin-top", "0");

        tableSelect.setWidth("350px");
        tableSelect.setPlaceholder("Cari tabel...");
        tableSelect.setItems(dynamicDataService.fetchDynamicTables());

        tableSelect.addValueChangeListener(event -> {
            String selectedTable = event.getValue();
            this.currentTable = selectedTable;
            if (selectedTable != null) {
                loadTableData(selectedTable);
                loadTableSchema(selectedTable);
                btnAddConstraint.setEnabled(true);
                btnAddTrigger.setEnabled(true);
                btnAddColumn.setEnabled(true);
                btnAddAuditCols.setEnabled(true);
            } else {
                clearView();
                btnAddConstraint.setEnabled(false);
                btnAddTrigger.setEnabled(false);
                btnAddColumn.setEnabled(false);
                btnAddAuditCols.setEnabled(false);
            }
        });

        // TabSheet for Workspace
        TabSheet explorerTabs = new TabSheet();
        explorerTabs.setSizeFull();

        // 1. Data Tab
        Button btnResetDataGrid = new Button("Reset Layout", VaadinIcon.ROTATE_LEFT.create());
        btnResetDataGrid.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnResetDataGrid.addClickListener(e -> {
            if (currentTable != null) {
                dynamicDataService.resetUserGridOrder("DB_EXPLORER_" + currentTable, "dataGrid");
                loadTableData(currentTable);
                Notification.show("Layout grid data di-reset ke urutan default", 2000, Notification.Position.BOTTOM_END);
            } else {
                Notification.show("Pilih tabel terlebih dahulu", 2000, Notification.Position.MIDDLE);
            }
        });
        HorizontalLayout dataHeader = new HorizontalLayout(recordCount, btnResetDataGrid, StandardGridUtils.createExportExcelButton(dataGrid, "db_data_export"));
        dataHeader.setAlignItems(Alignment.CENTER);
        paginationBar = new com.vaadinerp.components.PaginationBar(e -> applyDataFilters());
        VerticalLayout dataLayout = new VerticalLayout(dataHeader, dataGrid, paginationBar);
        dataLayout.setSizeFull();
        dataLayout.setPadding(false);
        dataGrid.setSizeFull();
        dataGrid.getStyle().set("border-radius", "8px");

        // 2. Schema Tab
        VerticalLayout schemaLayout = new VerticalLayout();
        schemaLayout.setSizeFull();
        schemaLayout.setPadding(false);
        schemaLayout.setSpacing(true);

        schemaGrid.setHeight("200px");
        schemaGrid.getStyle().set("border-radius", "8px");

        triggersGrid.setHeight("180px");
        triggersGrid.getStyle().set("border-radius", "8px");

        constraintsGrid.setHeight("180px");
        constraintsGrid.getStyle().set("border-radius", "8px");

        btnAddColumn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddColumn.setEnabled(false);
        btnAddColumn.addClickListener(e -> openColumnDialog(null));

        btnAddAuditCols.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btnAddAuditCols.setEnabled(false);
        btnAddAuditCols.setTooltipText("Langsung tambahkan kolom inputby, inputdt, updateby, updatedt, dan version ke tabel ini");
        btnAddAuditCols.addClickListener(e -> {
            if (this.currentTable != null) {
                dynamicDataService.ensureAuditColumnsExist(this.currentTable);
                loadTableSchema(this.currentTable);
                loadTableData(this.currentTable);
                Notification.show("Kolom audit default (inputby, inputdt, updateby, updatedt, version) berhasil ditambahkan ke tabel '" + this.currentTable + "'!", 3500, Notification.Position.BOTTOM_END);
            }
        });

        Button btnResetSchemaGrid = new Button("Reset Layout", VaadinIcon.ROTATE_LEFT.create());
        btnResetSchemaGrid.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnResetSchemaGrid.addClickListener(e -> {
            dynamicDataService.resetUserGridOrder("DB_EXPLORER", "schemaGrid");
            schemaGrid.removeAllColumns();
            refreshSchemaGrid();
            Notification.show("Layout grid skema di-reset", 2000, Notification.Position.BOTTOM_END);
        });

        HorizontalLayout columnHeader = new HorizontalLayout(schemaInfo, btnAddColumn, btnAddAuditCols, btnResetSchemaGrid, StandardGridUtils.createExportExcelButton(schemaGrid, "schema_export"));
        columnHeader.setAlignItems(Alignment.CENTER);
        columnHeader.setSpacing(true);

        btnAddConstraint.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddConstraint.setEnabled(false);
        btnAddConstraint.addClickListener(e -> openConstraintDialog(null));

        Button btnResetConstraintGrid = new Button("Reset Layout", VaadinIcon.ROTATE_LEFT.create());
        btnResetConstraintGrid.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnResetConstraintGrid.addClickListener(e -> {
            dynamicDataService.resetUserGridOrder("DB_EXPLORER", "constraintsGrid");
            constraintsGrid.removeAllColumns();
            refreshConstraintsGrid();
            Notification.show("Layout grid constraint di-reset", 2000, Notification.Position.BOTTOM_END);
        });

        HorizontalLayout constraintHeader = new HorizontalLayout(constraintInfo, btnAddConstraint, btnResetConstraintGrid, StandardGridUtils.createExportExcelButton(constraintsGrid, "constraints_export"));
        constraintHeader.setAlignItems(Alignment.CENTER);
        constraintHeader.setSpacing(true);

        btnAddTrigger.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddTrigger.setEnabled(false);
        btnAddTrigger.addClickListener(e -> openTriggerDialog(null));

        Button btnResetTriggerGrid = new Button("Reset Layout", VaadinIcon.ROTATE_LEFT.create());
        btnResetTriggerGrid.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnResetTriggerGrid.addClickListener(e -> {
            dynamicDataService.resetUserGridOrder("DB_EXPLORER", "triggersGrid");
            triggersGrid.removeAllColumns();
            refreshTriggersGrid();
            Notification.show("Layout grid trigger di-reset", 2000, Notification.Position.BOTTOM_END);
        });

        HorizontalLayout triggerHeader = new HorizontalLayout(triggerInfo, btnAddTrigger, btnResetTriggerGrid, StandardGridUtils.createExportExcelButton(triggersGrid, "triggers_export"));
        triggerHeader.setAlignItems(Alignment.CENTER);
        triggerHeader.setSpacing(true);

        schemaLayout.add(
                columnHeader, schemaGrid,
                new H4("Database Constraints"), constraintHeader, constraintsGrid,
                new H4("Active Database Triggers"), triggerHeader, triggersGrid);

        explorerTabs.add("Data Tabel", dataLayout);
        explorerTabs.add("Struktur Skema, Constraints & Trigger", schemaLayout);
        explorerTabs.add("Buat Tabel & Trigger Baru", new TableDesignerView(dynamicDataService, this::refreshTables));

        StandardActionToolbar actionToolbar = new StandardActionToolbar()
                .onRefresh(() -> {
                    if (currentTable != null) {
                        loadTableData(currentTable);
                        loadTableSchema(currentTable);
                    }
                    refreshTables();
                    Notification.show("Data & Skema diperbarui!", 1500, Notification.Position.BOTTOM_END);
                })
                .onNew(() -> openColumnDialog(null))
                .onClose(() -> Notification.show("Menutup Explorer...", 1500, Notification.Position.MIDDLE))
                .onPrint(() -> Notification.show(
                        "Mengekspor dokumentasi skema dynamic." + (currentTable != null ? currentTable : "") + "...",
                        3000, Notification.Position.BOTTOM_END));

        // Penerapan hak akses menu RBAC berdasarkan sesi user yang aktif
        actionToolbar.applyAuthority(this.securityService != null ? this.securityService.getAuthorityForMenu("DB_EXPLORER")
                : StandardActionToolbar.MenuAccessAuthority.fullAccess());

        HorizontalLayout toolbar = new HorizontalLayout(tableSelect, actionToolbar);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);
        toolbar.setSpacing(true);

        add(title, toolbar, explorerTabs);
    }

    private void loadTableData(String tableName) {
        while (dataGrid.getHeaderRows().size() > 1) {
            dataGrid.removeHeaderRow(dataGrid.getHeaderRows().get(dataGrid.getHeaderRows().size() - 1));
        }
        dataGrid.removeAllColumns();
        dataFilterValues.clear();
        currentSortField = null;
        currentSortDir = null;
        if (paginationBar != null) {
            paginationBar.resetPage();
        }

        List<String> columns = dynamicDataService.fetchTableColumns(tableName);
        if (columns.isEmpty()) {
            recordCount.setText("Tabel tidak memiliki kolom atau terjadi kesalahan");
            currentDataList = new ArrayList<>();
            dataGrid.setItems(currentDataList);
            if (paginationBar != null) paginationBar.setTotalRecords(0);
            return;
        }

        Map<Grid.Column<Map<String, Object>>, String> colKeyMap = new HashMap<>();
        for (String col : columns) {
            Grid.Column<Map<String, Object>> gc = dataGrid
                    .addColumn(row -> row.get(col) != null ? row.get(col).toString() : "")
                    .setHeader(col)
                    .setKey(col)
                    .setAutoWidth(true)
                    .setSortable(true);
            colKeyMap.put(gc, col);
        }

        if (dataGridReorderReg != null) dataGridReorderReg.remove();
        dataGrid.setColumnReorderingAllowed(true);
        dataGridReorderReg = dataGrid.addColumnReorderListener(event -> {
            List<String> orderedFieldNames = new ArrayList<>();
            for (Grid.Column<Map<String, Object>> col : event.getColumns()) {
                String fieldName = colKeyMap.get(col);
                if (fieldName != null) orderedFieldNames.add(fieldName);
            }
            try {
                dynamicDataService.saveUserGridOrder("DB_EXPLORER_" + tableName, "dataGrid", orderedFieldNames);
                Notification.show("Urutan kolom data disimpan", 1500, Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        attachServerSideDataGridFilters(dataGrid, colKeyMap);
        applyDataFilters();

        List<String> userOrder = dynamicDataService.getUserGridOrder("DB_EXPLORER_" + tableName, "dataGrid");
        StandardGridUtils.applySafeColumnOrder(dataGrid, colKeyMap, userOrder);
    }

    private void attachServerSideDataGridFilters(Grid<Map<String, Object>> grid, Map<Grid.Column<Map<String, Object>>, String> colKeyMap) {
        com.vaadin.flow.component.grid.HeaderRow filterRow = grid.appendHeaderRow();
        colKeyMap.forEach((col, fieldName) -> {
            FilterCriteria criteria = new FilterCriteria();
            dataFilterValues.put(fieldName, criteria);

            TextField filterField = new TextField();
            filterField.setPlaceholder("Filter...");
            filterField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
            filterField.setWidthFull();
            filterField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL);

            Button filterButton = new Button(com.vaadin.flow.component.icon.VaadinIcon.FILTER.create());
            filterButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE);
            filterButton.getStyle().set("cursor", "pointer");
            filterButton.getElement().setProperty("title", "Contains");
            filterField.setPrefixComponent(filterButton);

            com.vaadin.flow.component.contextmenu.ContextMenu contextMenu = new com.vaadin.flow.component.contextmenu.ContextMenu(filterButton);
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

            com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<com.vaadin.flow.component.contextmenu.MenuItem>> listener = event -> {
                if (event.getSource().getText() != null) {
                    criteria.operator = event.getSource().getText();
                    applyOperatorUI.run();
                    if (paginationBar != null) paginationBar.resetPage();
                    applyDataFilters();
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

            contextMenu.addItem(new com.vaadin.flow.component.html.Hr(), e -> {});
            contextMenu.addItem(col.isFrozen() ? "Unfreeze Column" : "Freeze Column", event -> {
                boolean nextFrozen = !col.isFrozen();
                col.setFrozen(nextFrozen);
                event.getSource().setText(nextFrozen ? "Unfreeze Column" : "Freeze Column");
                Notification.show(nextFrozen ? "Kolom dibekukan" : "Kolom dilepas", 2000, Notification.Position.BOTTOM_END);
            });

            filterField.addValueChangeListener(e -> {
                criteria.value = e.getValue();
                if (paginationBar != null) paginationBar.resetPage();
                applyDataFilters();
            });

            filterRow.getCell(col).setComponent(filterField);
        });

        grid.addSortListener(event -> {
            if (!event.getSortOrder().isEmpty()) {
                com.vaadin.flow.component.grid.GridSortOrder<Map<String, Object>> order = event.getSortOrder().get(0);
                if (order.getSorted().getKey() != null) {
                    currentSortField = order.getSorted().getKey();
                    currentSortDir = order.getDirection() == com.vaadin.flow.data.provider.SortDirection.DESCENDING ? "DESC" : "ASC";
                    if (paginationBar != null) paginationBar.resetPage();
                    applyDataFilters();
                }
            } else {
                currentSortField = null;
                currentSortDir = null;
                if (paginationBar != null) paginationBar.resetPage();
                applyDataFilters();
            }
        });
    }

    private void applyDataFilters() {
        if (currentTable == null || currentTable.trim().isEmpty()) return;
        long totalRecords = dynamicDataService.countTableDataPaged(currentTable, dataFilterValues);
        if (paginationBar != null) {
            paginationBar.setTotalRecords(totalRecords);
        }
        int offset = paginationBar != null ? paginationBar.getOffset() : 0;
        int limit = paginationBar != null ? paginationBar.getLimit() : 50;

        List<Map<String, Object>> pagedData = dynamicDataService.fetchTableDataPaged(
                currentTable, offset, limit, dataFilterValues, currentSortField, currentSortDir);

        currentDataList = new ArrayList<>(pagedData);
        dataGrid.setDataProvider(new com.vaadin.flow.data.provider.ListDataProvider<>(currentDataList));
        recordCount.setText("Menampilkan " + pagedData.size() + " dari total " + totalRecords + " baris data (dynamic." + currentTable + ")");
    }

    private void loadTableSchema(String tableName) {
        // Load Column Details
        currentSchemaList = dynamicDataService.fetchTableSchemaDetails(tableName);
        schemaInfo.setText("Menampilkan " + currentSchemaList.size() + " definisi kolom untuk dynamic." + tableName);
        refreshSchemaGrid();

        // Load Constraints
        currentConstraintList = dynamicDataService.fetchTableConstraints(tableName);
        if (currentConstraintList.isEmpty()) {
            constraintInfo.setText("Tidak ada database constraint aktif pada tabel dynamic." + tableName);
        } else {
            constraintInfo.setText("Menampilkan " + currentConstraintList.size()
                    + " constraint aktif pada tabel dynamic." + tableName);
        }
        refreshConstraintsGrid();

        // Load Triggers
        currentTriggerList = dynamicDataService.fetchTableTriggers(tableName);
        if (currentTriggerList.isEmpty()) {
            triggerInfo.setText("Tidak ada database trigger aktif pada tabel dynamic." + tableName);
        } else {
            triggerInfo.setText(
                    "Menampilkan " + currentTriggerList.size() + " trigger aktif pada tabel dynamic." + tableName);
        }
        refreshTriggersGrid();
    }

    private void openConstraintDialog(Map<String, Object> existingRow) {
        if (this.currentTable == null)
            return;
        boolean isEdit = existingRow != null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isEdit ? "Edit Constraint: dynamic." + this.currentTable
                : "Tambah Constraint Baru: dynamic." + this.currentTable);
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        TextField nameField = new TextField("Nama Constraint");
        ComboBox<String> typeField = new ComboBox<>("Tipe Constraint");
        typeField.setItems("FOREIGN KEY", "UNIQUE", "CHECK");
        typeField.setValue("UNIQUE");

        ComboBox<String> localColField = new ComboBox<>("Kolom Lokal");
        localColField.setItems(dynamicDataService.fetchTableColumns(this.currentTable));

        ComboBox<String> refTableField = new ComboBox<>("Tabel Referensi (Target FK)");
        refTableField.setItems(dynamicDataService.fetchDynamicTables());

        ComboBox<String> refColField = new ComboBox<>("Kolom Referensi (Target FK)");

        TextField checkExprField = new TextField("Check Expression (SQL)");
        checkExprField.setPlaceholder("e.g. salary_amount > 0");
        checkExprField.setVisible(false);

        FormLayout form = new FormLayout();
        form.add(nameField, typeField, localColField, refTableField, refColField, checkExprField);

        refTableField.setVisible(false);
        refColField.setVisible(false);

        Runnable toggleFields = () -> {
            String type = typeField.getValue();
            String prefix = type != null ? type.toLowerCase().replace(" ", "_") : "uq";
            if (!isEdit) {
                nameField.setValue(prefix + "_" + this.currentTable + "_"
                        + (localColField.getValue() != null ? localColField.getValue() : "col"));
            }

            if ("FOREIGN KEY".equals(type)) {
                localColField.setVisible(true);
                refTableField.setVisible(true);
                refColField.setVisible(true);
                checkExprField.setVisible(false);
            } else if ("UNIQUE".equals(type)) {
                localColField.setVisible(true);
                refTableField.setVisible(false);
                refColField.setVisible(false);
                checkExprField.setVisible(false);
            } else if ("CHECK".equals(type)) {
                localColField.setVisible(false);
                refTableField.setVisible(false);
                refColField.setVisible(false);
                checkExprField.setVisible(true);
                if (!isEdit) {
                    nameField.setValue("chk_" + this.currentTable + "_val");
                }
            }
        };

        typeField.addValueChangeListener(e -> toggleFields.run());
        localColField.addValueChangeListener(e -> toggleFields.run());

        refTableField.addValueChangeListener(e -> {
            String selectedRefTable = e.getValue();
            if (selectedRefTable != null) {
                refColField.setItems(dynamicDataService.fetchTableColumns(selectedRefTable));
            } else {
                refColField.setItems(new ArrayList<>());
            }
        });

        toggleFields.run();

        if (isEdit && existingRow != null) {
            String oldName = existingRow.get("constraint_name") != null ? existingRow.get("constraint_name").toString()
                    : "";
            String oldType = existingRow.get("constraint_type") != null ? existingRow.get("constraint_type").toString()
                    : "UNIQUE";
            String oldCol = existingRow.get("column_name") != null ? existingRow.get("column_name").toString() : "";
            String oldRefTable = existingRow.get("foreign_table") != null ? existingRow.get("foreign_table").toString()
                    : "";
            String oldRefCol = existingRow.get("foreign_column") != null ? existingRow.get("foreign_column").toString()
                    : "";
            String oldExpr = existingRow.get("check_expression") != null
                    ? existingRow.get("check_expression").toString()
                    : "";

            typeField.setValue(oldType);
            nameField.setValue(oldName);
            if ("FOREIGN KEY".equalsIgnoreCase(oldType)) {
                localColField.setValue(oldCol);
                refTableField.setValue(oldRefTable);
                refColField.setValue(oldRefCol);
            } else if ("UNIQUE".equalsIgnoreCase(oldType)) {
                localColField.setValue(oldCol);
            } else if ("CHECK".equalsIgnoreCase(oldType)) {
                checkExprField.setValue(oldExpr);
            }
        }

        Button btnApply = new Button("Apply Constraint", VaadinIcon.CHECK.create());
        btnApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnApply.addClickListener(e -> {
            String name = nameField.getValue().trim();
            String type = typeField.getValue();
            String localCol = localColField.getValue();
            String refTable = refTableField.getValue();
            String refCol = refColField.getValue();
            String expr = checkExprField.getValue().trim();

            if (name.isEmpty()) {
                Notification.show("Nama constraint tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                if (isEdit && existingRow != null) {
                    String oldName = existingRow.get("constraint_name") != null
                            ? existingRow.get("constraint_name").toString()
                            : "";
                    dynamicDataService.updateTableConstraint(this.currentTable, oldName, name, type, localCol, refTable,
                            refCol, expr);
                    Notification.show("Constraint " + name + " berhasil diperbarui secara fisik!", 3000,
                            Notification.Position.TOP_CENTER);
                } else {
                    dynamicDataService.addTableConstraint(this.currentTable, name, type, localCol, refTable, refCol,
                            expr);
                    Notification.show("Constraint " + name + " berhasil dibuat secara fisik!", 3000,
                            Notification.Position.TOP_CENTER);
                }
                loadTableSchema(this.currentTable);
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan constraint: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        Button btnCancel = new Button("Batal", e -> dialog.close());

        layout.add(form);
        dialog.add(layout);
        dialog.getFooter().add(btnCancel, btnApply);
        dialog.open();
    }

    private void dropConstraint(String constraintName) {
        if (this.currentTable == null || constraintName.isEmpty())
            return;
        try {
            dynamicDataService.dropTableConstraint(this.currentTable, constraintName);
            Notification.show("Constraint " + constraintName + " berhasil dihapus secara fisik!", 3000,
                    Notification.Position.TOP_CENTER);
            loadTableSchema(this.currentTable);
        } catch (Exception ex) {
            Notification.show("Gagal menghapus constraint: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void openTriggerDialog(Map<String, Object> existingRow) {
        if (this.currentTable == null)
            return;
        boolean isEdit = existingRow != null;
        String trgTitle = isEdit ? "Edit Trigger: dynamic." + this.currentTable
                : "Tambah Trigger Baru: dynamic." + this.currentTable;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(trgTitle);
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        TextField nameField = new TextField("Nama Trigger");
        ComboBox<String> timingField = new ComboBox<>("Timing");
        timingField.setItems("BEFORE", "AFTER");
        timingField.setValue("BEFORE");

        CheckboxGroup<String> eventsField = new CheckboxGroup<>("Trigger Events");
        eventsField.setItems("INSERT", "UPDATE", "DELETE");
        eventsField.select("INSERT", "UPDATE");

        TextArea bodyField = new TextArea("PL/pgSQL Body (Di dalam blok Function)");
        bodyField.setWidthFull();
        bodyField.setHeight("200px");
        bodyField.getStyle().set("font-family", "monospace");

        if (isEdit && existingRow != null) {
            String existingName = existingRow.get("trigger_name") != null ? existingRow.get("trigger_name").toString()
                    : "";
            String existingTiming = existingRow.get("action_timing") != null
                    ? existingRow.get("action_timing").toString()
                    : "BEFORE";
            String existingEvents = existingRow.get("event_manipulation") != null
                    ? existingRow.get("event_manipulation").toString()
                    : "";

            nameField.setValue(existingName);
            nameField.setEnabled(false);
            timingField.setValue(existingTiming);

            if (!existingEvents.isEmpty()) {
                String[] evs = existingEvents.split(",\\s*");
                eventsField.deselectAll();
                eventsField.select(evs);
            }

            bodyField.setValue(dynamicDataService.fetchTriggerBody(this.currentTable, existingName));
        } else {
            nameField.setValue("trg_" + this.currentTable + "_custom");
            bodyField.setValue(
                    "-- Contoh: Validasi atau isi audit log sebelum simpan\nBEGIN\n    -- NEW.kolom = nilai;\n    RETURN NEW;\nEND;");
        }

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("300px", 2));
        form.add(nameField, timingField);
        form.add(eventsField, 2);

        layout.add(form, bodyField);

        Button btnApply = new Button("Simpan Trigger", VaadinIcon.CHECK.create());
        btnApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnApply.addClickListener(e -> {
            String name = nameField.getValue().trim();
            String timing = timingField.getValue();
            Set<String> evs = eventsField.getValue();
            String body = bodyField.getValue().trim();

            if (name.isEmpty() || evs == null || evs.isEmpty() || body.isEmpty()) {
                Notification.show("Nama trigger, Event, dan Body tidak boleh kosong!", 3000,
                        Notification.Position.MIDDLE);
                return;
            }

            TriggerDefinition td = new TriggerDefinition();
            td.setTriggerName(name);
            td.setTiming(timing);
            td.setEvents(new ArrayList<>(evs));
            td.setTriggerBody(body);

            try {
                dynamicDataService.addOrUpdateTableTrigger(this.currentTable, td);
                Notification.show("Trigger " + name + " berhasil disimpan secara fisik!", 3000,
                        Notification.Position.TOP_CENTER);
                loadTableSchema(this.currentTable);
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan trigger: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        Button btnCancel = new Button("Batal", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(btnCancel, btnApply);
        dialog.open();
    }

    private void dropTrigger(String triggerName) {
        if (this.currentTable == null || triggerName.isEmpty())
            return;
        try {
            dynamicDataService.dropTableTrigger(this.currentTable, triggerName);
            Notification.show("Trigger " + triggerName + " berhasil dihapus secara fisik!", 3000,
                    Notification.Position.TOP_CENTER);
            loadTableSchema(this.currentTable);
        } catch (Exception ex) {
            Notification.show("Gagal menghapus trigger: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void openColumnDialog(Map<String, Object> existingRow) {
        if (this.currentTable == null)
            return;
        boolean isEdit = existingRow != null;
        String colTitle = isEdit ? "Edit Kolom: dynamic." + this.currentTable
                : "Tambah Kolom Baru: dynamic." + this.currentTable;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(colTitle);
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        TextField nameField = new TextField("Nama Kolom");
        ComboBox<String> typeField = new ComboBox<>("Tipe Data SQL");
        typeField.setItems("VARCHAR(255)", "VARCHAR(50)", "SERIAL", "BIGSERIAL", "TEXT", "INTEGER", "BIGINT", "DECIMAL(19,2)", "DATE", "TIMESTAMP",
                "BOOLEAN");
        typeField.setAllowCustomValue(true);
        typeField.addCustomValueSetListener(e -> typeField.setValue(e.getDetail()));
        typeField.setValue("VARCHAR(255)");

        Checkbox nullableField = new Checkbox("Nullable (Bolehkah Kosong?)");
        nullableField.setValue(true);

        TextField defaultField = new TextField("Default Value (Kosongkan jika tidak ada)");
        defaultField.setPlaceholder("e.g. '0' atau 'Belum diisi'");

        if (isEdit && existingRow != null) {
            String oldName = existingRow.get("column_name") != null ? existingRow.get("column_name").toString() : "";
            String oldType = existingRow.get("data_type") != null
                    ? existingRow.get("data_type").toString().toUpperCase()
                    : "VARCHAR(255)";
            boolean oldNull = "YES".equalsIgnoreCase(
                    existingRow.get("is_nullable") != null ? existingRow.get("is_nullable").toString() : "YES");
            String oldDef = existingRow.get("column_default") != null ? existingRow.get("column_default").toString()
                    : "";

            nameField.setValue(oldName);
            typeField.setValue(oldType);
            nullableField.setValue(oldNull);
            defaultField.setValue(oldDef);
        }

        FormLayout form = new FormLayout();
        form.add(nameField, typeField, defaultField);
        layout.add(form, nullableField);

        if (!isEdit) {
            Button btnQuickAudit = new Button("⚡ Langsung Tambah Semua Kolom Audit Default (inputby, inputdt, updateby, updatedt)", VaadinIcon.TIME_FORWARD.create());
            btnQuickAudit.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            btnQuickAudit.setWidthFull();
            btnQuickAudit.getStyle().set("margin-top", "10px");
            btnQuickAudit.addClickListener(e -> {
                if (this.currentTable != null) {
                    dynamicDataService.ensureAuditColumnsExist(this.currentTable);
                    loadTableSchema(this.currentTable);
                    loadTableData(this.currentTable);
                    dialog.close();
                    Notification.show("Kolom audit default berhasil ditambahkan ke tabel '" + this.currentTable + "'!", 3500, Notification.Position.BOTTOM_END);
                }
            });
            layout.add(new Hr(), btnQuickAudit);
        }

        Button btnApply = new Button("Simpan Kolom", VaadinIcon.CHECK.create());
        btnApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnApply.addClickListener(e -> {
            String name = nameField.getValue().trim();
            String type = typeField.getValue();
            boolean isNull = nullableField.getValue();
            String defVal = defaultField.getValue().trim();

            if (name.isEmpty() || type == null || type.isEmpty()) {
                Notification.show("Nama kolom dan Tipe Data tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                if (isEdit && existingRow != null) {
                    String oldCol = existingRow.get("column_name") != null ? existingRow.get("column_name").toString()
                            : "";
                    dynamicDataService.alterTableColumn(this.currentTable, oldCol, name, type, isNull, defVal);
                    Notification.show("Kolom " + name + " berhasil diperbarui secara fisik!", 3000,
                            Notification.Position.TOP_CENTER);
                } else {
                    dynamicDataService.addTableColumn(this.currentTable, name, type, isNull, defVal);
                    Notification.show("Kolom " + name + " berhasil ditambahkan secara fisik!", 3000,
                            Notification.Position.TOP_CENTER);
                }
                loadTableData(this.currentTable);
                loadTableSchema(this.currentTable);
                dialog.close();
            } catch (Exception ex) {
                Throwable cause = ex;
                while (cause.getCause() != null && cause.getCause() != cause) {
                    cause = cause.getCause();
                }
                String msg = cause.getMessage() != null ? cause.getMessage() : ex.getMessage();
                Notification.show("Gagal menyimpan kolom: " + msg, 5000, Notification.Position.MIDDLE);
            }
        });

        Button btnCancel = new Button("Batal", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(btnCancel, btnApply);
        dialog.open();
    }

    private void dropColumn(String columnName) {
        if (this.currentTable == null || columnName.isEmpty())
            return;
        try {
            dynamicDataService.dropTableColumn(this.currentTable, columnName);
            Notification.show("Kolom " + columnName + " berhasil dihapus secara fisik!", 3000,
                    Notification.Position.TOP_CENTER);
            loadTableData(this.currentTable);
            loadTableSchema(this.currentTable);
        } catch (Exception ex) {
            Notification.show("Gagal menghapus kolom: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void clearView() {
        dataFilterValues.clear();
        currentSortField = null;
        currentSortDir = null;
        if (paginationBar != null) {
            paginationBar.setTotalRecords(0);
            paginationBar.resetPage();
        }
        currentDataList = new ArrayList<>();
        currentSchemaList = new ArrayList<>();
        currentConstraintList = new ArrayList<>();
        currentTriggerList = new ArrayList<>();
        while (dataGrid.getHeaderRows().size() > 1) {
            dataGrid.removeHeaderRow(dataGrid.getHeaderRows().get(dataGrid.getHeaderRows().size() - 1));
        }
        dataGrid.removeAllColumns();
        dataGrid.setItems(new ArrayList<>());
        if (schemaFilterRefresher != null)
            schemaFilterRefresher.run();
        else
            schemaGrid.setItems(new ArrayList<>());
        if (constraintFilterRefresher != null)
            constraintFilterRefresher.run();
        else
            constraintsGrid.setItems(new ArrayList<>());
        if (triggerFilterRefresher != null)
            triggerFilterRefresher.run();
        else
            triggersGrid.setItems(new ArrayList<>());
        recordCount.setText("Pilih tabel untuk melihat data");
        schemaInfo.setText("Pilih tabel untuk melihat struktur kolom");
        triggerInfo.setText("Belum ada trigger terdaftar");
        constraintInfo.setText("Belum ada constraint terdaftar");
    }

    private void refreshSchemaGrid() {
        if (schemaGrid.getColumns().isEmpty()) {
            schemaColMap.clear();
            Grid.Column<Map<String, Object>> c1 = schemaGrid
                    .addColumn(row -> row.get("column_name") != null ? row.get("column_name").toString() : "")
                    .setHeader("Nama Kolom");
            Grid.Column<Map<String, Object>> c2 = schemaGrid
                    .addColumn(row -> row.get("data_type") != null ? row.get("data_type").toString() : "")
                    .setHeader("Tipe Data SQL");
            Grid.Column<Map<String, Object>> c3 = schemaGrid
                    .addColumn(row -> row.get("is_nullable") != null ? row.get("is_nullable").toString() : "")
                    .setHeader("Nullable");
            Grid.Column<Map<String, Object>> c4 = schemaGrid
                    .addColumn(row -> row.get("column_default") != null ? row.get("column_default").toString() : "")
                    .setHeader("Default Value");

            schemaGrid.addComponentColumn(row -> {
                String colName = row.get("column_name") != null ? row.get("column_name").toString() : "";
                Button btnEdit = new Button(VaadinIcon.EDIT.create());
                btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnEdit.setTooltipText("Edit Kolom");
                btnEdit.addClickListener(e -> openColumnDialog(row));

                Button btnDel = new Button(VaadinIcon.TRASH.create());
                btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                btnDel.setTooltipText("Hapus Kolom");
                btnDel.addClickListener(e -> dropColumn(colName));

                return new HorizontalLayout(btnEdit, btnDel);
            }).setHeader("Aksi").setWidth("110px").setFlexGrow(0);

            schemaColMap.put(c1, "column_name");
            schemaColMap.put(c2, "data_type");
            schemaColMap.put(c3, "is_nullable");
            schemaColMap.put(c4, "column_default");

            schemaGrid.setColumnReorderingAllowed(true);
            schemaGrid.addColumnReorderListener(event -> {
                List<String> orderedFieldNames = new ArrayList<>();
                for (Grid.Column<Map<String, Object>> col : event.getColumns()) {
                    String fieldName = schemaColMap.get(col);
                    if (fieldName != null) orderedFieldNames.add(fieldName);
                }
                try {
                    dynamicDataService.saveUserGridOrder("DB_EXPLORER", "schemaGrid", orderedFieldNames);
                    Notification.show("Urutan kolom skema disimpan", 1500, Notification.Position.BOTTOM_END);
                } catch (Exception ex) {
                    Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });

            schemaFilterRefresher = StandardGridUtils.attachMapGridFilters(schemaGrid, schemaColMap, () -> currentSchemaList);
        }
        if (schemaFilterRefresher != null) {
            schemaFilterRefresher.run();
        } else {
            schemaGrid.setItems(currentSchemaList);
        }
        List<String> userOrder = dynamicDataService.getUserGridOrder("DB_EXPLORER", "schemaGrid");
        StandardGridUtils.applySafeColumnOrder(schemaGrid, schemaColMap, userOrder);
    }

    private void refreshTriggersGrid() {
        if (triggersGrid.getColumns().isEmpty()) {
            triggerColMap.clear();
            Grid.Column<Map<String, Object>> c1 = triggersGrid
                    .addColumn(row -> row.get("trigger_name") != null ? row.get("trigger_name").toString() : "")
                    .setHeader("Nama Trigger");
            Grid.Column<Map<String, Object>> c2 = triggersGrid
                    .addColumn(row -> row.get("action_timing") != null ? row.get("action_timing").toString() : "")
                    .setHeader("Timing");
            Grid.Column<Map<String, Object>> c3 = triggersGrid.addColumn(
                    row -> row.get("event_manipulation") != null ? row.get("event_manipulation").toString() : "")
                    .setHeader("Event");

            triggersGrid.addComponentColumn(row -> {
                String name = row.get("trigger_name") != null ? row.get("trigger_name").toString() : "";
                Button btnEdit = new Button(VaadinIcon.EDIT.create());
                btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnEdit.setTooltipText("Edit Trigger");
                btnEdit.addClickListener(e -> openTriggerDialog(row));

                Button btnDel = new Button(VaadinIcon.TRASH.create());
                btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                btnDel.setTooltipText("Hapus Trigger");
                btnDel.addClickListener(e -> dropTrigger(name));

                return new HorizontalLayout(btnEdit, btnDel);
            }).setHeader("Aksi").setWidth("110px").setFlexGrow(0);

            triggerColMap.put(c1, "trigger_name");
            triggerColMap.put(c2, "action_timing");
            triggerColMap.put(c3, "event_manipulation");

            triggersGrid.setColumnReorderingAllowed(true);
            triggersGrid.addColumnReorderListener(event -> {
                List<String> orderedFieldNames = new ArrayList<>();
                for (Grid.Column<Map<String, Object>> col : event.getColumns()) {
                    String fieldName = triggerColMap.get(col);
                    if (fieldName != null) orderedFieldNames.add(fieldName);
                }
                try {
                    dynamicDataService.saveUserGridOrder("DB_EXPLORER", "triggersGrid", orderedFieldNames);
                    Notification.show("Urutan kolom trigger disimpan", 1500, Notification.Position.BOTTOM_END);
                } catch (Exception ex) {
                    Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });

            triggerFilterRefresher = StandardGridUtils.attachMapGridFilters(triggersGrid, triggerColMap,
                    () -> currentTriggerList);
        }
        if (triggerFilterRefresher != null) {
            triggerFilterRefresher.run();
        } else {
            triggersGrid.setItems(currentTriggerList);
        }
        List<String> userOrder = dynamicDataService.getUserGridOrder("DB_EXPLORER", "triggersGrid");
        StandardGridUtils.applySafeColumnOrder(triggersGrid, triggerColMap, userOrder);
    }

    private void refreshConstraintsGrid() {
        if (constraintsGrid.getColumns().isEmpty()) {
            constraintColMap.clear();
            Grid.Column<Map<String, Object>> c1 = constraintsGrid
                    .addColumn(row -> row.get("constraint_name") != null ? row.get("constraint_name").toString() : "")
                    .setHeader("Nama Constraint");
            Grid.Column<Map<String, Object>> c2 = constraintsGrid
                    .addColumn(row -> row.get("constraint_type") != null ? row.get("constraint_type").toString() : "")
                    .setHeader("Tipe");
            Grid.Column<Map<String, Object>> c3 = constraintsGrid
                    .addColumn(row -> row.get("column_name") != null ? row.get("column_name").toString() : "")
                    .setHeader("Kolom Target");
            Grid.Column<Map<String, Object>> c4 = constraintsGrid
                    .addColumn(row -> row.get("foreign_table") != null ? row.get("foreign_table").toString() : "")
                    .setHeader("Tabel Relasi (FK)");
            Grid.Column<Map<String, Object>> c5 = constraintsGrid
                    .addColumn(row -> row.get("foreign_column") != null ? row.get("foreign_column").toString() : "")
                    .setHeader("Kolom Relasi (FK)");
            Grid.Column<Map<String, Object>> c6 = constraintsGrid
                    .addColumn(row -> row.get("check_expression") != null ? row.get("check_expression").toString() : "")
                    .setHeader("Ekspresi (Check)");

            constraintsGrid.addComponentColumn(row -> {
                String name = row.get("constraint_name") != null ? row.get("constraint_name").toString() : "";
                boolean isPk = "PRIMARY KEY".equalsIgnoreCase(
                        row.get("constraint_type") != null ? row.get("constraint_type").toString() : "");

                Button btnEdit = new Button(VaadinIcon.EDIT.create());
                btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnEdit.setTooltipText("Edit Constraint");
                btnEdit.setEnabled(!isPk);
                btnEdit.addClickListener(e -> openConstraintDialog(row));

                Button btnDel = new Button(VaadinIcon.TRASH.create());
                btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                btnDel.setTooltipText("Hapus Constraint");
                btnDel.setEnabled(!isPk);
                btnDel.addClickListener(e -> dropConstraint(name));

                return new HorizontalLayout(btnEdit, btnDel);
            }).setHeader("Aksi").setWidth("110px").setFlexGrow(0);

            constraintColMap.put(c1, "constraint_name");
            constraintColMap.put(c2, "constraint_type");
            constraintColMap.put(c3, "column_name");
            constraintColMap.put(c4, "foreign_table");
            constraintColMap.put(c5, "foreign_column");
            constraintColMap.put(c6, "check_expression");

            constraintsGrid.setColumnReorderingAllowed(true);
            constraintsGrid.addColumnReorderListener(event -> {
                List<String> orderedFieldNames = new ArrayList<>();
                for (Grid.Column<Map<String, Object>> col : event.getColumns()) {
                    String fieldName = constraintColMap.get(col);
                    if (fieldName != null) orderedFieldNames.add(fieldName);
                }
                try {
                    dynamicDataService.saveUserGridOrder("DB_EXPLORER", "constraintsGrid", orderedFieldNames);
                    Notification.show("Urutan kolom constraint disimpan", 1500, Notification.Position.BOTTOM_END);
                } catch (Exception ex) {
                    Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });

            constraintFilterRefresher = StandardGridUtils.attachMapGridFilters(constraintsGrid, constraintColMap,
                    () -> currentConstraintList);
        }
        if (constraintFilterRefresher != null) {
            constraintFilterRefresher.run();
        } else {
            constraintsGrid.setItems(currentConstraintList);
        }
        List<String> userOrder = dynamicDataService.getUserGridOrder("DB_EXPLORER", "constraintsGrid");
        StandardGridUtils.applySafeColumnOrder(constraintsGrid, constraintColMap, userOrder);
    }

    public void refreshTables() {
        tableSelect.setItems(dynamicDataService.fetchDynamicTables());
    }
}
