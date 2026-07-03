package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.service.DynamicDataService.ColumnDefinition;
import com.vaadinerp.service.DynamicDataService.TriggerDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import com.vaadinerp.components.StandardGridUtils;

import java.util.HashSet;
import java.util.List;

public class TableDesignerView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    private final Runnable onTableCreatedListener;

    // Table Meta
    private final TextField tableNameField = new TextField("Nama Tabel (Schema: dynamic)");
    private final Checkbox includeAuditColsCheckbox = new Checkbox("Sertakan Kolom Audit Default (inputby, inputdt, updateby, updatedt)", true);

    // Column Inputs
    private final TextField colNameInput = new TextField("Nama Kolom");
    private final ComboBox<String> colTypeInput = new ComboBox<>("Tipe Data SQL");
    private final Checkbox isNullableInput = new Checkbox("Nullable (Bisa Null)");
    private final Checkbox isPkInput = new Checkbox("Primary Key");
    private final TextField defaultValueInput = new TextField("Default Value (SQL)");

    // Columns Grid
    private final Grid<ColumnDefinition> columnsGrid = new Grid<>();
    private final List<ColumnDefinition> columnsList = new ArrayList<>();
    private Runnable columnsGridRefresher;

    // Trigger Inputs
    private final Checkbox enableTriggerCheckbox = new Checkbox("Aktifkan Database Trigger PL/pgSQL");
    private final VerticalLayout triggerPanel = new VerticalLayout();
    private final TextField triggerNameField = new TextField("Nama Trigger");
    private final ComboBox<String> triggerTiming = new ComboBox<>("Timing");
    private final CheckboxGroup<String> triggerEvents = new CheckboxGroup<>("Trigger Events");
    private final TextArea triggerBodyArea = new TextArea("Trigger PL/pgSQL Body (Di dalam blok Function)");

    public TableDesignerView(DynamicDataService dynamicDataService) {
        this(dynamicDataService, null);
    }

    public TableDesignerView(DynamicDataService dynamicDataService, Runnable onTableCreatedListener) {
        this.dynamicDataService = dynamicDataService;
        this.onTableCreatedListener = onTableCreatedListener;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("Table & Trigger Designer");
        title.getStyle().set("margin-top", "0");

        // Table metadata
        tableNameField.setWidth("350px");
        tableNameField.setPlaceholder("e.g. salary_logs");
        tableNameField.addValueChangeListener(e -> updateTriggerTemplate());

        includeAuditColsCheckbox.addValueChangeListener(e -> {
            if (e.getValue()) {
                addAuditColumnsToList();
            } else {
                removeAuditColumnsFromList();
            }
        });

        // Add default PK column initially
        ColumnDefinition idCol = new ColumnDefinition();
        idCol.setColumnName("id");
        idCol.setDataType("SERIAL");
        idCol.setPrimaryKey(true);
        idCol.setNullable(false);
        columnsList.add(idCol);

        if (includeAuditColsCheckbox.getValue()) {
            addAuditColumnsToList();
        }

        // Column Builder Layout
        FormLayout colInputLayout = new FormLayout();
        colInputLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("800px", 4)
        );

        colTypeInput.setItems("VARCHAR(255)", "VARCHAR(50)", "INTEGER", "BIGINT", "DECIMAL(19, 2)", "NUMERIC(15, 2)", "DATE", "TIMESTAMP", "BOOLEAN", "TEXT");
        colTypeInput.setAllowCustomValue(true);
        colTypeInput.setValue("VARCHAR(255)");
        colTypeInput.addCustomValueSetListener(e -> colTypeInput.setValue(e.getDetail()));

        isNullableInput.setValue(true);
        isPkInput.addValueChangeListener(e -> {
            if (e.getValue()) {
                isNullableInput.setValue(false);
            }
        });

        HorizontalLayout flagsLayout = new HorizontalLayout(isNullableInput, isPkInput);
        flagsLayout.setSpacing(true);
        flagsLayout.getStyle().set("margin-top", "25px");

        Button btnAddColumn = new Button("Tambah Kolom", VaadinIcon.PLUS.create());
        btnAddColumn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAddColumn.getStyle().set("margin-top", "20px");
        btnAddColumn.addClickListener(e -> addColumnToList());

        colInputLayout.add(colNameInput, colTypeInput, defaultValueInput, flagsLayout, btnAddColumn);

        // Setup Grid
        setupGrid();

        // Setup Trigger Panel
        setupTriggerPanel();

        // Main Build Button
        Button btnBuild = new Button("Build Physical Table & Trigger", VaadinIcon.CONNECT.create());
        btnBuild.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnBuild.setWidthFull();
        btnBuild.addClickListener(e -> buildTableAndTrigger());

        HorizontalLayout colHdr = new HorizontalLayout(new H4("Rancang Kolom Tabel"), StandardGridUtils.createExportExcelButton(columnsGrid, "designer_columns_export"));
        colHdr.setAlignItems(Alignment.CENTER);
        add(title, tableNameField, includeAuditColsCheckbox, colHdr, colInputLayout, columnsGrid, enableTriggerCheckbox, triggerPanel, btnBuild);
    }

    private void setupGrid() {
        columnsGrid.setHeight("250px");
        Grid.Column<ColumnDefinition> c1 = columnsGrid.addColumn(ColumnDefinition::getColumnName).setHeader("Nama Kolom");
        Grid.Column<ColumnDefinition> c2 = columnsGrid.addColumn(ColumnDefinition::getDataType).setHeader("Tipe Data");
        Grid.Column<ColumnDefinition> c3 = columnsGrid.addColumn(col -> col.isPrimaryKey() ? "YES" : "NO").setHeader("PK");
        Grid.Column<ColumnDefinition> c4 = columnsGrid.addColumn(col -> col.isNullable() ? "YES" : "NO").setHeader("Nullable");
        Grid.Column<ColumnDefinition> c5 = columnsGrid.addColumn(ColumnDefinition::getDefaultValue).setHeader("Default Value");

        columnsGrid.addComponentColumn(col -> {
            Button btnDel = new Button(VaadinIcon.TRASH.create());
            btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDel.setTooltipText("Hapus Kolom");
            btnDel.addClickListener(e -> {
                columnsList.remove(col);
                if (columnsGridRefresher != null) columnsGridRefresher.run();
            });
            // Don't allow deleting PK 'id'
            btnDel.setEnabled(!"id".equalsIgnoreCase(col.getColumnName()));
            return btnDel;
        }).setHeader("Aksi").setWidth("80px").setFlexGrow(0);

        Map<Grid.Column<ColumnDefinition>, Function<ColumnDefinition, String>> getterMap = new HashMap<>();
        getterMap.put(c1, col -> col.getColumnName() != null ? col.getColumnName() : "");
        getterMap.put(c2, col -> col.getDataType() != null ? col.getDataType() : "");
        getterMap.put(c3, col -> col.isPrimaryKey() ? "YES" : "NO");
        getterMap.put(c4, col -> col.isNullable() ? "YES" : "NO");
        getterMap.put(c5, col -> col.getDefaultValue() != null ? col.getDefaultValue() : "");
        this.columnsGridRefresher = StandardGridUtils.attachGridFilters(columnsGrid, getterMap, () -> columnsList);
        columnsGridRefresher.run();
    }

    private void setupTriggerPanel() {
        triggerPanel.setWidthFull();
        triggerPanel.setPadding(true);
        triggerPanel.setSpacing(true);
        triggerPanel.getStyle()
                .set("background", "#f3f4f6")
                .set("border", "1px dashed #cbd5e1")
                .set("border-radius", "8px")
                .set("margin-bottom", "10px");
        triggerPanel.setVisible(false);

        enableTriggerCheckbox.addValueChangeListener(e -> triggerPanel.setVisible(e.getValue()));

        FormLayout triggerForm = new FormLayout();
        triggerForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("800px", 3)
        );

        triggerTiming.setItems("BEFORE", "AFTER");
        triggerTiming.setValue("BEFORE");

        triggerEvents.setItems("INSERT", "UPDATE", "DELETE");
        triggerEvents.select("INSERT", "UPDATE");

        triggerForm.add(triggerNameField, triggerTiming, triggerEvents);

        triggerBodyArea.setWidthFull();
        triggerBodyArea.setHeight("250px");
        triggerBodyArea.getStyle().set("font-family", "monospace");
        updateTriggerTemplate();

        triggerPanel.add(new H4("Pengaturan Database Trigger PL/pgSQL"), triggerForm, triggerBodyArea);
    }

    private void updateTriggerTemplate() {
        String rawTable = tableNameField.getValue().trim();
        String table = rawTable.isEmpty() ? "nama_tabel" : rawTable;
        
        String template = "BEGIN\n" +
                "    -- Contoh Trigger: Mengisi audit log otomatis sebelum simpan data\n" +
                "    -- NEW.updated_at = NOW();\n" +
                "    \n" +
                "    -- Kembalikan record target (NEW) agar query berlanjut\n" +
                "    RETURN NEW;\n" +
                "END;";
        
        triggerBodyArea.setValue(template);
        triggerNameField.setValue("trg_" + table + "_audit");
    }

    private void addColumnToList() {
        String colName = colNameInput.getValue().trim();
        String colType = colTypeInput.getValue();
        String defVal = defaultValueInput.getValue().trim();

        if (colName.isEmpty() || colType == null) {
            Notification.show("Nama kolom dan Tipe data tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Duplicate Check
        boolean exists = columnsList.stream().anyMatch(c -> c.getColumnName().equalsIgnoreCase(colName));
        if (exists) {
            Notification.show("Kolom '" + colName + "' sudah ditambahkan!", 3000, Notification.Position.MIDDLE);
            return;
        }

        ColumnDefinition col = new ColumnDefinition();
        col.setColumnName(colName);
        col.setDataType(colType);
        col.setPrimaryKey(isPkInput.getValue());
        col.setNullable(isNullableInput.getValue());
        col.setDefaultValue(defVal.isEmpty() ? null : defVal);

        columnsList.add(col);
        if (columnsGridRefresher != null) columnsGridRefresher.run();

        // Reset Inputs
        colNameInput.clear();
        colTypeInput.setValue("VARCHAR(255)");
        defaultValueInput.clear();
        isNullableInput.setValue(true);
        isPkInput.setValue(false);

        Notification.show("Kolom ditambahkan!", 2000, Notification.Position.BOTTOM_END);
    }

    private void buildTableAndTrigger() {
        String table = tableNameField.getValue().trim();
        if (table.isEmpty()) {
            Notification.show("Nama tabel tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (columnsList.size() <= 1) {
            Notification.show("Rancang minimal 1 kolom di luar kolom PK 'id'!", 3000, Notification.Position.MIDDLE);
            return;
        }

        TriggerDefinition trigger = null;
        if (enableTriggerCheckbox.getValue()) {
            String trgName = triggerNameField.getValue().trim();
            String trgTiming = triggerTiming.getValue();
            String trgBody = triggerBodyArea.getValue().trim();
            HashSet<String> events = new HashSet<>(triggerEvents.getValue());

            if (trgName.isEmpty() || trgBody.isEmpty() || events.isEmpty()) {
                Notification.show("Nama trigger, Event, dan Body trigger PL/pgSQL harus diatur!", 3000, Notification.Position.MIDDLE);
                return;
            }

            trigger = new TriggerDefinition();
            trigger.setTriggerName(trgName);
            trigger.setTiming(trgTiming);
            trigger.setEvents(new ArrayList<>(events));
            trigger.setTriggerBody(trgBody);
        }

        try {
            // Build Table & Trigger via DynamicDataService
            dynamicDataService.createPhysicalTableAndTrigger(table, columnsList, trigger);
            
            Notification.show("Tabel dynamic." + table + " dan Database Trigger berhasil dibentuk!", 4000, Notification.Position.TOP_CENTER);

            // Reset UI
            tableNameField.clear();
            columnsList.clear();
            
            // Add ID PK back
            ColumnDefinition idCol = new ColumnDefinition();
            idCol.setColumnName("id");
            idCol.setDataType("SERIAL");
            idCol.setPrimaryKey(true);
            idCol.setNullable(false);
            if (includeAuditColsCheckbox.getValue()) {
                addAuditColumnsToList();
            } else if (columnsGridRefresher != null) {
                columnsGridRefresher.run();
            }
            enableTriggerCheckbox.setValue(false);
            triggerPanel.setVisible(false);
            updateTriggerTemplate();

            if (onTableCreatedListener != null) {
                onTableCreatedListener.run();
            }
        } catch (Exception ex) {
            Notification.show("Gagal membuild Tabel/Trigger: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void addAuditColumnsToList() {
        removeAuditColumnsFromList();
        ColumnDefinition col1 = new ColumnDefinition();
        col1.setColumnName("inputby");
        col1.setDataType("VARCHAR(255)");
        col1.setNullable(true);
        col1.setPrimaryKey(false);

        ColumnDefinition col2 = new ColumnDefinition();
        col2.setColumnName("inputdt");
        col2.setDataType("TIMESTAMP");
        col2.setNullable(true);
        col2.setPrimaryKey(false);

        ColumnDefinition col3 = new ColumnDefinition();
        col3.setColumnName("updateby");
        col3.setDataType("VARCHAR(255)");
        col3.setNullable(true);
        col3.setPrimaryKey(false);

        ColumnDefinition col4 = new ColumnDefinition();
        col4.setColumnName("updatedt");
        col4.setDataType("TIMESTAMP");
        col4.setNullable(true);
        col4.setPrimaryKey(false);

        columnsList.add(col1);
        columnsList.add(col2);
        columnsList.add(col3);
        columnsList.add(col4);
        if (columnsGridRefresher != null) columnsGridRefresher.run();
    }

    private void removeAuditColumnsFromList() {
        columnsList.removeIf(col -> {
            String name = col.getColumnName();
            return name != null && (name.equalsIgnoreCase("inputby") || name.equalsIgnoreCase("inputdt") ||
                                    name.equalsIgnoreCase("updateby") || name.equalsIgnoreCase("updatedt"));
        });
        if (columnsGridRefresher != null) columnsGridRefresher.run();
    }
}
