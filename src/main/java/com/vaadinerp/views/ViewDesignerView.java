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

public class ViewDesignerView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    private final Grid<Map<String, Object>> viewsGrid = new Grid<>();
    private final ComboBox<String> schemaFilter = new ComboBox<>("Filter Schema");
    private final TextField searchField = new TextField("Search View");
    private final Span recordCountSpan = new Span();
    private List<Map<String, Object>> allViewsList = new ArrayList<>();

    public ViewDesignerView(DynamicDataService dynamicDataService) {
        this.dynamicDataService = dynamicDataService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header Toolbar
        schemaFilter.setItems("ALL", "dynamic", "public");
        schemaFilter.setValue("dynamic");
        schemaFilter.setWidth("180px");
        schemaFilter.addValueChangeListener(e -> loadViews());

        searchField.setPlaceholder("Ketik nama view...");
        searchField.setWidth("280px");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterGrid());

        Button btnRefresh = new Button("Refresh List", VaadinIcon.REFRESH.create());
        btnRefresh.addClickListener(e -> loadViews());

        Button btnCreateNew = new Button("⚡ Create New View", VaadinIcon.PLUS.create());
        btnCreateNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnCreateNew.addClickListener(e -> openViewDialog(null));

        HorizontalLayout toolbar = new HorizontalLayout(schemaFilter, searchField, btnRefresh, btnCreateNew);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setWidthFull();

        // Grid Configuration
        viewsGrid.setSizeFull();
        viewsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        StandardGridUtils.enableCellClipboardCopy(viewsGrid);

        viewsGrid.addColumn(row -> row.get("schema_name")).setHeader("Schema").setWidth("120px").setFlexGrow(0).setSortable(true);
        viewsGrid.addColumn(row -> row.get("view_name")).setHeader("View Name").setWidth("300px").setFlexGrow(1).setSortable(true);

        viewsGrid.addComponentColumn(row -> {
            Button btnEdit = new Button("Edit Code", VaadinIcon.EDIT.create());
            btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnEdit.addClickListener(e -> openViewDialog(row));

            Button btnDrop = new Button("Delete", VaadinIcon.TRASH.create());
            btnDrop.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDrop.addClickListener(e -> confirmDropView(row));

            HorizontalLayout actions = new HorizontalLayout(btnEdit, btnDrop);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Action").setWidth("210px").setFlexGrow(0);

        HorizontalLayout footer = new HorizontalLayout(recordCountSpan);
        footer.setAlignItems(Alignment.CENTER);

        add(new H4("Daftar Database Views PostgreSQL"), toolbar, viewsGrid, footer);

        loadViews();
    }

    private void loadViews() {
        String schema = schemaFilter.getValue();
        allViewsList = dynamicDataService.fetchDatabaseViews(schema);
        filterGrid();
    }

    private void filterGrid() {
        String query = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        List<Map<String, Object>> filtered = allViewsList.stream().filter(row -> {
            if (query.isEmpty()) return true;
            String name = row.get("view_name") != null ? row.get("view_name").toString().toLowerCase() : "";
            return name.contains(query);
        }).collect(Collectors.toList());

        viewsGrid.setItems(filtered);
        recordCountSpan.setText("Total Views: " + filtered.size() + " of " + allViewsList.size());
    }

    public void openViewDialog(Map<String, Object> existingRow) {
        boolean isNew = (existingRow == null);
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Create New Database View" : "Edit Database View");
        dialog.setWidth("85vw");
        dialog.setHeight("85vh");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        TextArea codeArea = new TextArea("Script Lengkap CREATE OR REPLACE VIEW (PostgreSQL DDL):");
        codeArea.setSizeFull();
        codeArea.getStyle().set("font-family", "Consolas, Courier New, monospace");
        codeArea.getStyle().set("font-size", "13px");

        if (isNew) {
            ComboBox<String> schemaSelect = new ComboBox<>("Schema", "dynamic", "public");
            schemaSelect.setValue("dynamic");
            TextField nameField = new TextField("View Name");
            nameField.setPlaceholder("contoh: v_rekap_penjualan");

            Button btnGenerateTemplate = new Button("⚡ Generate Template Code", VaadinIcon.CODE.create());
            btnGenerateTemplate.addClickListener(e -> {
                String sch = schemaSelect.getValue() != null ? schemaSelect.getValue() : "dynamic";
                String nm = nameField.getValue() != null && !nameField.getValue().isBlank() ? nameField.getValue().trim().toLowerCase() : "nama_view";

                StringBuilder sb = new StringBuilder();
                sb.append("CREATE OR REPLACE VIEW \"").append(sch).append("\".\"").append(nm).append("\" AS \n");
                sb.append("SELECT \n    -- tulis kolom di sini\n");
                sb.append("FROM dynamic.table_name\n");
                sb.append("WHERE 1=1; \n");
                codeArea.setValue(sb.toString());
            });

            HorizontalLayout builderBar = new HorizontalLayout(schemaSelect, nameField, btnGenerateTemplate);
            builderBar.setAlignItems(Alignment.BASELINE);
            builderBar.setWidthFull();
            dialogLayout.add(builderBar);

            // Generate initial default template
            btnGenerateTemplate.click();
        } else {
            Long oid = existingRow.get("oid") instanceof Number n ? n.longValue() : null;
            String schemaName = existingRow.get("schema_name") != null ? existingRow.get("schema_name").toString() : "";
            String viewName = existingRow.get("view_name") != null ? existingRow.get("view_name").toString() : "";
            
            String existingDef = dynamicDataService.fetchViewDefinitionByOid(oid);
            if(existingDef != null && !existingDef.isEmpty()) {
                codeArea.setValue("CREATE OR REPLACE VIEW \"" + schemaName + "\".\"" + viewName + "\" AS \n" + existingDef);
            }
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
                dynamicDataService.executeViewScript(sql);
                Notification.show("✅ View saved and activated on server!", 3500, Notification.Position.BOTTOM_END);
                dialog.close();
                loadViews();
            } catch (Exception ex) {
                Notification.show("❌ Error saat mengeksekusi script: " + ex.getMessage(), 6000, Notification.Position.MIDDLE);
            }
        });

        Button btnCancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(btnCancel, btnSave);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void confirmDropView(Map<String, Object> row) {
        String schemaName = row.get("schema_name") != null ? row.get("schema_name").toString() : "";
        String viewName = row.get("view_name") != null ? row.get("view_name").toString() : "";

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Delete View");
        dialog.add(new Span("Are you sure you want to permanently delete the following view from the server?"));
        dialog.add(new VerticalLayout(
                new Span("Schema: " + schemaName),
                new Span("Nama: " + viewName)
        ));

        Button btnYes = new Button("Yes, Delete Now", VaadinIcon.TRASH.create(), e -> {
            try {
                String dropSql = "DROP VIEW IF EXISTS \"" + schemaName + "\".\"" + viewName + "\";";
                dynamicDataService.executeViewScript(dropSql);
                Notification.show("✅ View '" + viewName + "' berhasil dihapus dari database!", 3000, Notification.Position.BOTTOM_END);
                dialog.close();
                loadViews();
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
