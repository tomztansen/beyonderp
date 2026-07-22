package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadinerp.components.StandardGridUtils;
import com.vaadinerp.service.DynamicDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProcedureDesignerView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    private final Grid<Map<String, Object>> routinesGrid = new Grid<>();
    private final ComboBox<String> schemaFilter = new ComboBox<>("Filter Schema");
    private final TextField searchField = new TextField("Search Routine / Argument");
    private final Span recordCountSpan = new Span();
    private List<Map<String, Object>> allRoutinesList = new ArrayList<>();

    public ProcedureDesignerView(DynamicDataService dynamicDataService) {
        this.dynamicDataService = dynamicDataService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header Toolbar
        schemaFilter.setItems("ALL", "dynamic", "public");
        schemaFilter.setValue("dynamic");
        schemaFilter.setWidth("180px");
        schemaFilter.addValueChangeListener(e -> loadRoutines());

        searchField.setPlaceholder("Ketik nama atau argumen...");
        searchField.setWidth("280px");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterGrid());

        Button btnRefresh = new Button("Refresh List", VaadinIcon.REFRESH.create());
        btnRefresh.addClickListener(e -> loadRoutines());

        Button btnCreateNew = new Button("⚡ Create New Procedure / Function", VaadinIcon.PLUS.create());
        btnCreateNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnCreateNew.addClickListener(e -> openProcedureDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(schemaFilter, searchField, btnRefresh, btnCreateNew);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setWidthFull();

        // Grid Configuration
        routinesGrid.setSizeFull();
        routinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        StandardGridUtils.enableCellClipboardCopy(routinesGrid);

        routinesGrid.addColumn(row -> row.get("schema_name")).setHeader("Schema").setWidth("120px").setFlexGrow(0).setSortable(true);
        routinesGrid.addColumn(row -> row.get("routine_type")).setHeader("Type").setWidth("120px").setFlexGrow(0).setSortable(true);
        routinesGrid.addColumn(row -> row.get("procedure_name")).setHeader("Routine / Procedure Name").setWidth("260px").setFlexGrow(1).setSortable(true);
        routinesGrid.addColumn(row -> row.get("identity_args")).setHeader("Daftar Argumen").setWidth("350px").setFlexGrow(2);

        routinesGrid.addComponentColumn(row -> {
            Button btnEdit = new Button("Edit Code", VaadinIcon.EDIT.create());
            btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnEdit.addClickListener(e -> openProcedureDialog(row));

            Button btnDrop = new Button("Delete", VaadinIcon.TRASH.create());
            btnDrop.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDrop.addClickListener(e -> confirmDropRoutine(row));

            HorizontalLayout actions = new HorizontalLayout(btnEdit, btnDrop);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Action").setWidth("210px").setFlexGrow(0);

        HorizontalLayout footer = new HorizontalLayout(recordCountSpan);
        footer.setAlignItems(Alignment.CENTER);

        add(new H4("Daftar Stored Procedure & Function PostgreSQL"), toolbar, routinesGrid, footer);

        loadRoutines();
    }

    private void loadRoutines() {
        String schema = schemaFilter.getValue();
        allRoutinesList = dynamicDataService.fetchDatabaseRoutines(schema);
        filterGrid();
    }

    private void filterGrid() {
        String query = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        List<Map<String, Object>> filtered = allRoutinesList.stream().filter(row -> {
            if (query.isEmpty()) return true;
            String name = row.get("procedure_name") != null ? row.get("procedure_name").toString().toLowerCase() : "";
            String args = row.get("identity_args") != null ? row.get("identity_args").toString().toLowerCase() : "";
            return name.contains(query) || args.contains(query);
        }).collect(Collectors.toList());

        routinesGrid.setItems(filtered);
        recordCountSpan.setText("Total Routines: " + filtered.size() + " dari " + allRoutinesList.size());
    }

    public void openProcedureDialog(Map<String, Object> existingRow) {
        boolean isNew = (existingRow == null);
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Create New Stored Procedure / Function" : "Edit Stored Procedure / Function");
        dialog.setWidth("85vw");
        dialog.setHeight("85vh");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        TextArea codeArea = new TextArea("Script Lengkap CREATE OR REPLACE PROCEDURE / FUNCTION (PostgreSQL DDL):");
        codeArea.setSizeFull();
        codeArea.getStyle().set("font-family", "Consolas, Courier New, monospace");
        codeArea.getStyle().set("font-size", "13px");

        if (isNew) {
            ComboBox<String> schemaSelect = new ComboBox<>("Schema", "dynamic", "public");
            schemaSelect.setValue("dynamic");
            ComboBox<String> typeSelect = new ComboBox<>("Tipe Routine", "PROCEDURE", "FUNCTION");
            typeSelect.setValue("PROCEDURE");
            TextField nameField = new TextField("Nama Routine");
            nameField.setPlaceholder("contoh: update_stock_barang");
            TextField argsField = new TextField("Daftar Argumen (Tanpa Kurung)");
            argsField.setPlaceholder("contoh: p_item_id bigint, p_qty numeric");
            argsField.setWidth("300px");

            Button btnGenerateTemplate = new Button("⚡ Generate Template Code", VaadinIcon.CODE.create());
            btnGenerateTemplate.addClickListener(e -> {
                String sch = schemaSelect.getValue() != null ? schemaSelect.getValue() : "dynamic";
                String typ = typeSelect.getValue() != null ? typeSelect.getValue() : "PROCEDURE";
                String nm = nameField.getValue() != null && !nameField.getValue().isBlank() ? nameField.getValue().trim().toLowerCase() : "nama_routine";
                String ag = argsField.getValue() != null ? argsField.getValue().trim() : "";

                StringBuilder sb = new StringBuilder();
                sb.append("CREATE OR REPLACE ").append(typ).append(" \"").append(sch).append("\".\"").append(nm).append("\"(").append(ag).append(")\n");
                if ("FUNCTION".equalsIgnoreCase(typ)) {
                    sb.append("RETURNS void\n");
                }
                sb.append("LANGUAGE plpgsql\nAS $BODY$ \nDECLARE\n    -- Deklarasi variabel di sini\n");
                sb.append("BEGIN\n\n    -- Tulis logika SQL / PLPGSQL di sini\n\n");
                if ("FUNCTION".equalsIgnoreCase(typ)) {
                    sb.append("    RETURN;\n");
                }
                sb.append("END;\n$BODY$;\n");
                codeArea.setValue(sb.toString());
            });

            HorizontalLayout builderBar = new HorizontalLayout(schemaSelect, typeSelect, nameField, argsField, btnGenerateTemplate);
            builderBar.setAlignItems(Alignment.BASELINE);
            builderBar.setWidthFull();
            dialogLayout.add(builderBar);

            // Generate initial default template
            btnGenerateTemplate.click();
        } else if (existingRow != null) {
            Long oid = existingRow.get("oid") instanceof Number n ? n.longValue() : null;
            String existingDef = dynamicDataService.fetchRoutineDefinitionByOid(oid);
            codeArea.setValue(existingDef);
        }

        dialogLayout.add(codeArea);

        Button btnSave = new Button("💾 Save & Execute to Database", VaadinIcon.CHECK.create());
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnSave.addClickListener(e -> {
            String sql = codeArea.getValue();
            if (sql == null || sql.trim().isEmpty()) {
                Notification.show("SQL Script cannot be empty!", 3000, Notification.Position.MIDDLE);
                return;
            }
            try {
                dynamicDataService.executeProcedureScript(sql);
                Notification.show("✅ Stored Procedure / Function saved and activated on server!", 3500, Notification.Position.BOTTOM_END);
                dialog.close();
                loadRoutines();
            } catch (Exception ex) {
                Notification.show("❌ Error saat mengeksekusi script: " + ex.getMessage(), 6000, Notification.Position.MIDDLE);
            }
        });

        Button btnCancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(btnCancel, btnSave);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void confirmDropRoutine(Map<String, Object> row) {
        String schemaName = row.get("schema_name") != null ? row.get("schema_name").toString() : "";
        String procedureName = row.get("procedure_name") != null ? row.get("procedure_name").toString() : "";
        String identityArgs = row.get("identity_args") != null ? row.get("identity_args").toString() : "";
        String routineType = row.get("routine_type") != null ? row.get("routine_type").toString() : "PROCEDURE";

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Delete " + routineType);
        dialog.add(new Span("Are you sure you want to permanently delete this " + routineType.toLowerCase() + " from the server?"));
        dialog.add(new VerticalLayout(
                new Span("Schema: " + schemaName),
                new Span("Nama: " + procedureName),
                new Span("Argumen: (" + identityArgs + ")")
        ));

        Button btnYes = new Button("Yes, Delete Now", VaadinIcon.TRASH.create(), e -> {
            try {
                String dropSql = "DROP " + routineType + " IF EXISTS \"" + schemaName + "\".\"" + procedureName + "\"(" + identityArgs + ")" + ("FUNCTION".equalsIgnoreCase(routineType) ? " CASCADE;" : ";");
                dynamicDataService.executeProcedureScript(dropSql);
                Notification.show("✅ " + routineType + " '" + procedureName + "' berhasil dihapus dari database!", 3000, Notification.Position.BOTTOM_END);
                dialog.close();
                loadRoutines();
            } catch (Exception ex) {
                Notification.show("❌ Error deleting: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        btnYes.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button btnNo = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(btnNo, btnYes);
        dialog.open();
    }
}
