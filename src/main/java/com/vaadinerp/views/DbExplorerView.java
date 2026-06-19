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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadinerp.service.DynamicDataService;

import java.util.ArrayList;
import java.util.HashMap;
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

    private String currentTable;

    public DbExplorerView(DynamicDataService dynamicDataService) {
        this.dynamicDataService = dynamicDataService;

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
            } else {
                clearView();
                btnAddConstraint.setEnabled(false);
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
        schemaGrid.addColumn(row -> row.get("column_name") != null ? row.get("column_name").toString() : "").setHeader("Nama Kolom");
        schemaGrid.addColumn(row -> row.get("data_type") != null ? row.get("data_type").toString() : "").setHeader("Tipe Data SQL");
        schemaGrid.addColumn(row -> row.get("is_nullable") != null ? row.get("is_nullable").toString() : "").setHeader("Nullable");
        schemaGrid.addColumn(row -> row.get("column_default") != null ? row.get("column_default").toString() : "").setHeader("Default Value");

        triggersGrid.setHeight("130px");
        triggersGrid.getStyle().set("border-radius", "8px");
        triggersGrid.addColumn(row -> row.get("trigger_name") != null ? row.get("trigger_name").toString() : "").setHeader("Nama Trigger");
        triggersGrid.addColumn(row -> row.get("action_timing") != null ? row.get("action_timing").toString() : "").setHeader("Timing");
        triggersGrid.addColumn(row -> row.get("event_manipulation") != null ? row.get("event_manipulation").toString() : "").setHeader("Event");

        // Constraints Grid configuration
        constraintsGrid.setHeight("180px");
        constraintsGrid.getStyle().set("border-radius", "8px");
        constraintsGrid.addColumn(row -> row.get("constraint_name") != null ? row.get("constraint_name").toString() : "").setHeader("Nama Constraint");
        constraintsGrid.addColumn(row -> row.get("constraint_type") != null ? row.get("constraint_type").toString() : "").setHeader("Tipe");
        constraintsGrid.addColumn(row -> row.get("column_name") != null ? row.get("column_name").toString() : "").setHeader("Kolom Target");
        constraintsGrid.addColumn(row -> row.get("foreign_table") != null ? row.get("foreign_table").toString() : "").setHeader("Tabel Relasi (FK)");
        constraintsGrid.addColumn(row -> row.get("foreign_column") != null ? row.get("foreign_column").toString() : "").setHeader("Kolom Relasi (FK)");
        constraintsGrid.addColumn(row -> row.get("check_expression") != null ? row.get("check_expression").toString() : "").setHeader("Ekspresi (Check)");

        constraintsGrid.addComponentColumn(row -> {
            String name = row.get("constraint_name") != null ? row.get("constraint_name").toString() : "";
            Button btnDel = new Button(VaadinIcon.TRASH.create());
            btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDel.setTooltipText("Hapus Constraint");
            btnDel.addClickListener(e -> dropConstraint(name));
            
            // Primary key constraints shouldn't be dropped easily
            boolean isPk = "PRIMARY KEY".equalsIgnoreCase(row.get("constraint_type") != null ? row.get("constraint_type").toString() : "");
            btnDel.setEnabled(!isPk);
            return btnDel;
        }).setHeader("Hapus").setWidth("80px").setFlexGrow(0);

        btnAddConstraint.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddConstraint.setEnabled(false);
        btnAddConstraint.addClickListener(e -> openAddConstraintDialog());

        HorizontalLayout constraintHeader = new HorizontalLayout(constraintInfo, btnAddConstraint);
        constraintHeader.setAlignItems(Alignment.CENTER);
        constraintHeader.setSpacing(true);

        schemaLayout.add(
            schemaInfo, schemaGrid, 
            new H4("Database Constraints"), constraintHeader, constraintsGrid,
            new H4("Active Database Triggers"), triggerInfo, triggersGrid
        );

        explorerTabs.add("Data Tabel", dataLayout);
        explorerTabs.add("Struktur Skema, Constraints & Trigger", schemaLayout);
        explorerTabs.add("Buat Tabel & Trigger Baru", new TableDesignerView(dynamicDataService, this::refreshTables));

        HorizontalLayout toolbar = new HorizontalLayout(tableSelect);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setSpacing(true);

        add(title, toolbar, explorerTabs);
    }

    private void loadTableData(String tableName) {
        dataGrid.removeAllColumns();
        
        List<String> columns = dynamicDataService.fetchTableColumns(tableName);
        if (columns.isEmpty()) {
            recordCount.setText("Tabel tidak memiliki kolom atau terjadi kesalahan");
            dataGrid.setItems(new ArrayList<>());
            return;
        }

        // Dynamically add columns to the grid
        for (String col : columns) {
            dataGrid.addColumn(row -> row.get(col) != null ? row.get(col).toString() : "")
                .setHeader(col)
                .setAutoWidth(true)
                .setSortable(true);
        }

        List<Map<String, Object>> data = dynamicDataService.fetchTableData(tableName);
        dataGrid.setItems(data);
        recordCount.setText("Menampilkan " + data.size() + " baris data dari dynamic." + tableName);
    }

    private void loadTableSchema(String tableName) {
        // Load Column Details
        List<Map<String, Object>> columns = dynamicDataService.fetchTableSchemaDetails(tableName);
        schemaGrid.setItems(columns);
        schemaInfo.setText("Menampilkan " + columns.size() + " definisi kolom untuk dynamic." + tableName);

        // Load Constraints
        List<Map<String, Object>> constraints = dynamicDataService.fetchTableConstraints(tableName);
        constraintsGrid.setItems(constraints);
        if (constraints.isEmpty()) {
            constraintInfo.setText("Tidak ada database constraint aktif pada tabel dynamic." + tableName);
        } else {
            constraintInfo.setText("Menampilkan " + constraints.size() + " constraint aktif pada tabel dynamic." + tableName);
        }

        // Load Triggers
        List<Map<String, Object>> triggers = dynamicDataService.fetchTableTriggers(tableName);
        triggersGrid.setItems(triggers);
        if (triggers.isEmpty()) {
            triggerInfo.setText("Tidak ada database trigger aktif pada tabel dynamic." + tableName);
        } else {
            triggerInfo.setText("Menampilkan " + triggers.size() + " trigger aktif pada tabel dynamic." + tableName);
        }
    }

    private void openAddConstraintDialog() {
        if (this.currentTable == null) return;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tambah Constraint Baru: dynamic." + this.currentTable);
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        TextField nameField = new TextField("Nama Constraint");
        ComboBox<String> typeField = new ComboBox<>("Tipe Constraint");
        typeField.setItems("FOREIGN KEY", "UNIQUE", "CHECK");
        typeField.setValue("UNIQUE");

        // Conditional forms
        ComboBox<String> localColField = new ComboBox<>("Kolom Lokal");
        localColField.setItems(dynamicDataService.fetchTableColumns(this.currentTable));

        ComboBox<String> refTableField = new ComboBox<>("Tabel Referensi (Target FK)");
        refTableField.setItems(dynamicDataService.fetchDynamicTables());

        ComboBox<String> refColField = new ComboBox<>("Kolom Referensi (Target FK)");

        TextField checkExprField = new TextField("Check Expression (SQL)");
        checkExprField.setPlaceholder("e.g. salary_amount > 0");
        checkExprField.setVisible(false);

        // Sub layouts visibility toggles
        FormLayout form = new FormLayout();
        form.add(nameField, typeField, localColField, refTableField, refColField, checkExprField);

        refTableField.setVisible(false);
        refColField.setVisible(false);

        Runnable toggleFields = () -> {
            String type = typeField.getValue();
            String prefix = type != null ? type.toLowerCase().replace(" ", "_") : "uq";
            nameField.setValue(prefix + "_" + this.currentTable + "_" + (localColField.getValue() != null ? localColField.getValue() : "col"));

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
                nameField.setValue("chk_" + this.currentTable + "_val");
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

        // Initialize state
        toggleFields.run();

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
                dynamicDataService.addTableConstraint(this.currentTable, name, type, localCol, refTable, refCol, expr);
                Notification.show("Constraint " + name + " berhasil dibuat secara fisik!", 3000, Notification.Position.TOP_CENTER);
                loadTableSchema(this.currentTable);
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Gagal membuat constraint: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
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

    private void clearView() {
        dataGrid.removeAllColumns();
        dataGrid.setItems(new ArrayList<>());
        schemaGrid.setItems(new ArrayList<>());
        constraintsGrid.setItems(new ArrayList<>());
        triggersGrid.setItems(new ArrayList<>());
        recordCount.setText("Pilih tabel untuk melihat data");
        schemaInfo.setText("Pilih tabel untuk melihat struktur kolom");
        triggerInfo.setText("Belum ada trigger terdaftar");
        constraintInfo.setText("Belum ada constraint terdaftar");
    }

    public void refreshTables() {
        tableSelect.setItems(dynamicDataService.fetchDynamicTables());
    }
}
