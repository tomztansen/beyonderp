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
    private final ComboBox<String> tableSelect = new ComboBox<>("Pilih Tabel Dinamis (Schema: dynamic)");
    
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

    private List<Map<String, Object>> currentDataList = new ArrayList<>();
    private List<Map<String, Object>> currentSchemaList = new ArrayList<>();
    private List<Map<String, Object>> currentConstraintList = new ArrayList<>();
    private List<Map<String, Object>> currentTriggerList = new ArrayList<>();

    private String currentTable;
    private final SessionSecurityService securityService;

    private Runnable schemaFilterRefresher;
    private Runnable triggerFilterRefresher;
    private Runnable constraintFilterRefresher;

    public DbExplorerView(DynamicDataService dynamicDataService, SessionSecurityService securityService) {
        this.dynamicDataService = dynamicDataService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("Database Manager (Schema: dynamic)");
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
            } else {
                clearView();
                btnAddConstraint.setEnabled(false);
                btnAddTrigger.setEnabled(false);
                btnAddColumn.setEnabled(false);
            }
        });

        // TabSheet for Workspace
        TabSheet explorerTabs = new TabSheet();
        explorerTabs.setSizeFull();

        // 1. Data Tab
        VerticalLayout dataLayout = new VerticalLayout(recordCount, dataGrid);
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

        HorizontalLayout columnHeader = new HorizontalLayout(schemaInfo, btnAddColumn);
        columnHeader.setAlignItems(Alignment.CENTER);
        columnHeader.setSpacing(true);

        btnAddConstraint.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddConstraint.setEnabled(false);
        btnAddConstraint.addClickListener(e -> openConstraintDialog(null));

        HorizontalLayout constraintHeader = new HorizontalLayout(constraintInfo, btnAddConstraint);
        constraintHeader.setAlignItems(Alignment.CENTER);
        constraintHeader.setSpacing(true);

        btnAddTrigger.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddTrigger.setEnabled(false);
        btnAddTrigger.addClickListener(e -> openTriggerDialog(null));

        HorizontalLayout triggerHeader = new HorizontalLayout(triggerInfo, btnAddTrigger);
        triggerHeader.setAlignItems(Alignment.CENTER);
        triggerHeader.setSpacing(true);

        schemaLayout.add(
            columnHeader, schemaGrid, 
            new H4("Database Constraints"), constraintHeader, constraintsGrid,
            new H4("Active Database Triggers"), triggerHeader, triggersGrid
        );

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
            .onPrint(() -> Notification.show("Mengekspor dokumentasi skema dynamic." + (currentTable != null ? currentTable : "") + "...", 3000, Notification.Position.BOTTOM_END));

        // Penerapan hak akses menu RBAC berdasarkan sesi user yang aktif
        actionToolbar.applyAuthority(securityService != null ? securityService.getAuthorityForMenu("DB_EXPLORER") : StandardActionToolbar.MenuAccessAuthority.fullAccess());

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
        
        List<String> columns = dynamicDataService.fetchTableColumns(tableName);
        if (columns.isEmpty()) {
            recordCount.setText("Tabel tidak memiliki kolom atau terjadi kesalahan");
            currentDataList = new ArrayList<>();
            dataGrid.setItems(currentDataList);
            return;
        }

        Map<Grid.Column<Map<String, Object>>, String> colKeyMap = new HashMap<>();
        for (String col : columns) {
            Grid.Column<Map<String, Object>> gc = dataGrid.addColumn(row -> row.get(col) != null ? row.get(col).toString() : "")
                .setHeader(col)
                .setAutoWidth(true)
                .setSortable(true);
            colKeyMap.put(gc, col);
        }

        currentDataList = dynamicDataService.fetchTableData(tableName);
        recordCount.setText("Menampilkan " + currentDataList.size() + " baris data dari dynamic." + tableName);
        Runnable r = StandardGridUtils.attachMapGridFilters(dataGrid, colKeyMap, () -> currentDataList);
        r.run();
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
            constraintInfo.setText("Menampilkan " + currentConstraintList.size() + " constraint aktif pada tabel dynamic." + tableName);
        }
        refreshConstraintsGrid();

        // Load Triggers
        currentTriggerList = dynamicDataService.fetchTableTriggers(tableName);
        if (currentTriggerList.isEmpty()) {
            triggerInfo.setText("Tidak ada database trigger aktif pada tabel dynamic." + tableName);
        } else {
            triggerInfo.setText("Menampilkan " + currentTriggerList.size() + " trigger aktif pada tabel dynamic." + tableName);
        }
        refreshTriggersGrid();
    }

    private void openConstraintDialog(Map<String, Object> existingRow) {
        if (this.currentTable == null) return;
        boolean isEdit = existingRow != null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isEdit ? "Edit Constraint: dynamic." + this.currentTable : "Tambah Constraint Baru: dynamic." + this.currentTable);
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
                nameField.setValue(prefix + "_" + this.currentTable + "_" + (localColField.getValue() != null ? localColField.getValue() : "col"));
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
            String oldName = existingRow.get("constraint_name") != null ? existingRow.get("constraint_name").toString() : "";
            String oldType = existingRow.get("constraint_type") != null ? existingRow.get("constraint_type").toString() : "UNIQUE";
            String oldCol = existingRow.get("column_name") != null ? existingRow.get("column_name").toString() : "";
            String oldRefTable = existingRow.get("foreign_table") != null ? existingRow.get("foreign_table").toString() : "";
            String oldRefCol = existingRow.get("foreign_column") != null ? existingRow.get("foreign_column").toString() : "";
            String oldExpr = existingRow.get("check_expression") != null ? existingRow.get("check_expression").toString() : "";

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
                    String oldName = existingRow.get("constraint_name") != null ? existingRow.get("constraint_name").toString() : "";
                    dynamicDataService.updateTableConstraint(this.currentTable, oldName, name, type, localCol, refTable, refCol, expr);
                    Notification.show("Constraint " + name + " berhasil diperbarui secara fisik!", 3000, Notification.Position.TOP_CENTER);
                } else {
                    dynamicDataService.addTableConstraint(this.currentTable, name, type, localCol, refTable, refCol, expr);
                    Notification.show("Constraint " + name + " berhasil dibuat secara fisik!", 3000, Notification.Position.TOP_CENTER);
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
        if (this.currentTable == null || constraintName.isEmpty()) return;
        try {
            dynamicDataService.dropTableConstraint(this.currentTable, constraintName);
            Notification.show("Constraint " + constraintName + " berhasil dihapus secara fisik!", 3000, Notification.Position.TOP_CENTER);
            loadTableSchema(this.currentTable);
        } catch (Exception ex) {
            Notification.show("Gagal menghapus constraint: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void openTriggerDialog(Map<String, Object> existingRow) {
        if (this.currentTable == null) return;
        boolean isEdit = existingRow != null;
        String trgTitle = isEdit ? "Edit Trigger: dynamic." + this.currentTable : "Tambah Trigger Baru: dynamic." + this.currentTable;

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
            String existingName = existingRow.get("trigger_name") != null ? existingRow.get("trigger_name").toString() : "";
            String existingTiming = existingRow.get("action_timing") != null ? existingRow.get("action_timing").toString() : "BEFORE";
            String existingEvents = existingRow.get("event_manipulation") != null ? existingRow.get("event_manipulation").toString() : "";

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
            bodyField.setValue("-- Contoh: Validasi atau isi audit log sebelum simpan\nBEGIN\n    -- NEW.kolom = nilai;\n    RETURN NEW;\nEND;");
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
                Notification.show("Nama trigger, Event, dan Body tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }

            TriggerDefinition td = new TriggerDefinition();
            td.setTriggerName(name);
            td.setTiming(timing);
            td.setEvents(new ArrayList<>(evs));
            td.setTriggerBody(body);

            try {
                dynamicDataService.addOrUpdateTableTrigger(this.currentTable, td);
                Notification.show("Trigger " + name + " berhasil disimpan secara fisik!", 3000, Notification.Position.TOP_CENTER);
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
        if (this.currentTable == null || triggerName.isEmpty()) return;
        try {
            dynamicDataService.dropTableTrigger(this.currentTable, triggerName);
            Notification.show("Trigger " + triggerName + " berhasil dihapus secara fisik!", 3000, Notification.Position.TOP_CENTER);
            loadTableSchema(this.currentTable);
        } catch (Exception ex) {
            Notification.show("Gagal menghapus trigger: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void openColumnDialog(Map<String, Object> existingRow) {
        if (this.currentTable == null) return;
        boolean isEdit = existingRow != null;
        String colTitle = isEdit ? "Edit Kolom: dynamic." + this.currentTable : "Tambah Kolom Baru: dynamic." + this.currentTable;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(colTitle);
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        TextField nameField = new TextField("Nama Kolom");
        ComboBox<String> typeField = new ComboBox<>("Tipe Data SQL");
        typeField.setItems("VARCHAR(255)", "TEXT", "INTEGER", "BIGINT", "DECIMAL(19,2)", "DATE", "TIMESTAMP", "BOOLEAN");
        typeField.setValue("VARCHAR(255)");

        Checkbox nullableField = new Checkbox("Nullable (Bolehkah Kosong?)");
        nullableField.setValue(true);

        TextField defaultField = new TextField("Default Value (Kosongkan jika tidak ada)");
        defaultField.setPlaceholder("e.g. '0' atau 'Belum diisi'");

        if (isEdit && existingRow != null) {
            String oldName = existingRow.get("column_name") != null ? existingRow.get("column_name").toString() : "";
            String oldType = existingRow.get("data_type") != null ? existingRow.get("data_type").toString().toUpperCase() : "VARCHAR(255)";
            boolean oldNull = "YES".equalsIgnoreCase(existingRow.get("is_nullable") != null ? existingRow.get("is_nullable").toString() : "YES");
            String oldDef = existingRow.get("column_default") != null ? existingRow.get("column_default").toString() : "";

            nameField.setValue(oldName);
            typeField.setValue(oldType);
            nullableField.setValue(oldNull);
            defaultField.setValue(oldDef);
        }

        FormLayout form = new FormLayout();
        form.add(nameField, typeField, defaultField);
        layout.add(form, nullableField);

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
                    String oldCol = existingRow.get("column_name") != null ? existingRow.get("column_name").toString() : "";
                    dynamicDataService.alterTableColumn(this.currentTable, oldCol, name, type, isNull, defVal);
                    Notification.show("Kolom " + name + " berhasil diperbarui secara fisik!", 3000, Notification.Position.TOP_CENTER);
                } else {
                    dynamicDataService.addTableColumn(this.currentTable, name, type, isNull, defVal);
                    Notification.show("Kolom " + name + " berhasil ditambahkan secara fisik!", 3000, Notification.Position.TOP_CENTER);
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
        if (this.currentTable == null || columnName.isEmpty()) return;
        try {
            dynamicDataService.dropTableColumn(this.currentTable, columnName);
            Notification.show("Kolom " + columnName + " berhasil dihapus secara fisik!", 3000, Notification.Position.TOP_CENTER);
            loadTableData(this.currentTable);
            loadTableSchema(this.currentTable);
        } catch (Exception ex) {
            Notification.show("Gagal menghapus kolom: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void clearView() {
        currentDataList = new ArrayList<>();
        currentSchemaList = new ArrayList<>();
        currentConstraintList = new ArrayList<>();
        currentTriggerList = new ArrayList<>();
        while (dataGrid.getHeaderRows().size() > 1) {
            dataGrid.removeHeaderRow(dataGrid.getHeaderRows().get(dataGrid.getHeaderRows().size() - 1));
        }
        dataGrid.removeAllColumns();
        dataGrid.setItems(new ArrayList<>());
        if (schemaFilterRefresher != null) schemaFilterRefresher.run(); else schemaGrid.setItems(new ArrayList<>());
        if (constraintFilterRefresher != null) constraintFilterRefresher.run(); else constraintsGrid.setItems(new ArrayList<>());
        if (triggerFilterRefresher != null) triggerFilterRefresher.run(); else triggersGrid.setItems(new ArrayList<>());
        recordCount.setText("Pilih tabel untuk melihat data");
        schemaInfo.setText("Pilih tabel untuk melihat struktur kolom");
        triggerInfo.setText("Belum ada trigger terdaftar");
        constraintInfo.setText("Belum ada constraint terdaftar");
    }

    private void refreshSchemaGrid() {
        if (schemaGrid.getColumns().isEmpty()) {
            Grid.Column<Map<String, Object>> c1 = schemaGrid.addColumn(row -> row.get("column_name") != null ? row.get("column_name").toString() : "").setHeader("Nama Kolom");
            Grid.Column<Map<String, Object>> c2 = schemaGrid.addColumn(row -> row.get("data_type") != null ? row.get("data_type").toString() : "").setHeader("Tipe Data SQL");
            Grid.Column<Map<String, Object>> c3 = schemaGrid.addColumn(row -> row.get("is_nullable") != null ? row.get("is_nullable").toString() : "").setHeader("Nullable");
            Grid.Column<Map<String, Object>> c4 = schemaGrid.addColumn(row -> row.get("column_default") != null ? row.get("column_default").toString() : "").setHeader("Default Value");

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

            Map<Grid.Column<Map<String, Object>>, String> colMap = new HashMap<>();
            colMap.put(c1, "column_name");
            colMap.put(c2, "data_type");
            colMap.put(c3, "is_nullable");
            colMap.put(c4, "column_default");
            schemaFilterRefresher = StandardGridUtils.attachMapGridFilters(schemaGrid, colMap, () -> currentSchemaList);
        }
        if (schemaFilterRefresher != null) {
            schemaFilterRefresher.run();
        } else {
            schemaGrid.setItems(currentSchemaList);
        }
    }

    private void refreshTriggersGrid() {
        if (triggersGrid.getColumns().isEmpty()) {
            Grid.Column<Map<String, Object>> c1 = triggersGrid.addColumn(row -> row.get("trigger_name") != null ? row.get("trigger_name").toString() : "").setHeader("Nama Trigger");
            Grid.Column<Map<String, Object>> c2 = triggersGrid.addColumn(row -> row.get("action_timing") != null ? row.get("action_timing").toString() : "").setHeader("Timing");
            Grid.Column<Map<String, Object>> c3 = triggersGrid.addColumn(row -> row.get("event_manipulation") != null ? row.get("event_manipulation").toString() : "").setHeader("Event");

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

            Map<Grid.Column<Map<String, Object>>, String> colMap = new HashMap<>();
            colMap.put(c1, "trigger_name");
            colMap.put(c2, "action_timing");
            colMap.put(c3, "event_manipulation");
            triggerFilterRefresher = StandardGridUtils.attachMapGridFilters(triggersGrid, colMap, () -> currentTriggerList);
        }
        if (triggerFilterRefresher != null) {
            triggerFilterRefresher.run();
        } else {
            triggersGrid.setItems(currentTriggerList);
        }
    }

    private void refreshConstraintsGrid() {
        if (constraintsGrid.getColumns().isEmpty()) {
            Grid.Column<Map<String, Object>> c1 = constraintsGrid.addColumn(row -> row.get("constraint_name") != null ? row.get("constraint_name").toString() : "").setHeader("Nama Constraint");
            Grid.Column<Map<String, Object>> c2 = constraintsGrid.addColumn(row -> row.get("constraint_type") != null ? row.get("constraint_type").toString() : "").setHeader("Tipe");
            Grid.Column<Map<String, Object>> c3 = constraintsGrid.addColumn(row -> row.get("column_name") != null ? row.get("column_name").toString() : "").setHeader("Kolom Target");
            Grid.Column<Map<String, Object>> c4 = constraintsGrid.addColumn(row -> row.get("foreign_table") != null ? row.get("foreign_table").toString() : "").setHeader("Tabel Relasi (FK)");
            Grid.Column<Map<String, Object>> c5 = constraintsGrid.addColumn(row -> row.get("foreign_column") != null ? row.get("foreign_column").toString() : "").setHeader("Kolom Relasi (FK)");
            Grid.Column<Map<String, Object>> c6 = constraintsGrid.addColumn(row -> row.get("check_expression") != null ? row.get("check_expression").toString() : "").setHeader("Ekspresi (Check)");

            constraintsGrid.addComponentColumn(row -> {
                String name = row.get("constraint_name") != null ? row.get("constraint_name").toString() : "";
                boolean isPk = "PRIMARY KEY".equalsIgnoreCase(row.get("constraint_type") != null ? row.get("constraint_type").toString() : "");

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

            Map<Grid.Column<Map<String, Object>>, String> colMap = new HashMap<>();
            colMap.put(c1, "constraint_name");
            colMap.put(c2, "constraint_type");
            colMap.put(c3, "column_name");
            colMap.put(c4, "foreign_table");
            colMap.put(c5, "foreign_column");
            colMap.put(c6, "check_expression");
            constraintFilterRefresher = StandardGridUtils.attachMapGridFilters(constraintsGrid, colMap, () -> currentConstraintList);
        }
        if (constraintFilterRefresher != null) {
            constraintFilterRefresher.run();
        } else {
            constraintsGrid.setItems(currentConstraintList);
        }
    }

    public void refreshTables() {
        tableSelect.setItems(dynamicDataService.fetchDynamicTables());
    }
}
