package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.meta.LovMetaRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Route("lov-builder")
public class LovBuilderView extends VerticalLayout {

    private final LovMetaRepository lovMetaRepository;
    private final com.vaadinerp.service.DynamicDataService dataService;

    private final Grid<LovMeta> grid = new Grid<>(LovMeta.class, false);

    private final TextField lovCodeField = new TextField("LOV Code (Unique)");
    private final TextField lovNameField = new TextField("LOV Name");
    private final TextField tableNameField = new TextField("Table Name / Query");
    private final TextField valueColumnField = new TextField("Value Column (Saved Value)");
    private final TextField labelColumnField = new TextField("Label Column (Display Text)");
    private final TextField searchColumnField = new TextField("Search Column(s)");
    private final TextArea gridColumnsField = new TextArea("Grid Columns Configuration");

    private LovMeta currentLovMeta;

    @Autowired
    public LovBuilderView(LovMetaRepository lovMetaRepository, com.vaadinerp.service.DynamicDataService dataService) {
        this.lovMetaRepository = lovMetaRepository;
        this.dataService = dataService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "5px");

        HorizontalLayout toolbar = buildToolbar();

        setupGrid();
        VerticalLayout editorLayout = buildEditorLayout();

        SplitLayout splitLayout = new SplitLayout(grid, editorLayout);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(60);

        add(title, toolbar, splitLayout);

        refreshGrid();
        clearForm();
    }

    private HorizontalLayout buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.addClassName("sticky-toolbar");
        toolbar.getStyle()
                .set("background-color", "#f3f4f6")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "6px")
                .set("padding", "6px 12px")
                .set("align-items", "center")
                .set("gap", "15px")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "100")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.08)");

        Button btnNew = new Button("New", VaadinIcon.PLUS_CIRCLE.create(), e -> clearForm());
        btnNew.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnNew.getStyle().set("color", "#22c55e");

        Button btnSave = new Button("Save", VaadinIcon.DOWNLOAD.create(), e -> saveLovMeta());
        btnSave.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("color", "#3b82f6");

        Button btnDelete = new Button("Delete", VaadinIcon.CLOSE_CIRCLE.create(), e -> deleteLovMeta());
        btnDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        Button btnCopy = new Button("Copy", VaadinIcon.COPY.create(), e -> copyLovMeta());
        btnCopy.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCopy.getStyle().set("color", "#f59e0b");

        toolbar.add(btnNew, btnSave, btnDelete, btnCopy);
        return toolbar;
    }

    private void setupGrid() {
        grid.setSizeFull();
        grid.addColumn(meta -> meta != null ? meta.getLovCode() : "").setHeader("LOV Code").setSortable(true).setAutoWidth(true);
        grid.addColumn(meta -> meta != null ? meta.getLovName() : "").setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(meta -> meta != null ? meta.getTableName() : "").setHeader("Table Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(meta -> meta != null ? meta.getValueColumn() : "").setHeader("Value Col").setAutoWidth(true);
        grid.addColumn(meta -> meta != null ? meta.getLabelColumn() : "").setHeader("Label Col").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                populateForm(event.getValue());
            } else {
                clearForm();
            }
        });
    }

    private VerticalLayout buildEditorLayout() {
        VerticalLayout editorLayout = new VerticalLayout();
        editorLayout.setPadding(true);
        editorLayout.getStyle().set("background-color", "#ffffff").set("border-radius", "8px").set("border",
                "1px solid #e2e8f0");

        lovCodeField.setWidthFull();
        lovNameField.setWidthFull();
        tableNameField.setWidthFull();
        tableNameField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        tableNameField.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null && !e.getValue().trim().isEmpty()) {
                java.util.List<String> columns = dataService.getColumnsForQueryOrTable(e.getValue().trim());
                if (!columns.isEmpty()) {
                    if (valueColumnField.isEmpty())
                        valueColumnField.setValue(columns.get(0));
                    if (labelColumnField.isEmpty()) {
                        String label = columns.get(0);
                        for (String col : columns) {
                            if (col.toLowerCase().contains("name") || col.toLowerCase().contains("title")
                                    || col.toLowerCase().contains("desc")) {
                                label = col;
                                break;
                            }
                        }
                        labelColumnField.setValue(label);
                    }
                }
            }
        });
        valueColumnField.setWidthFull();
        labelColumnField.setWidthFull();
        searchColumnField.setWidthFull();
        gridColumnsField.setWidthFull();
        gridColumnsField.setPlaceholder("e.g. code:Kode:100px,name:Nama:200px");
        gridColumnsField.setHeight("100px");

        searchColumnField.setPlaceholder("e.g. name,description");

        HorizontalLayout valueLabelRow = new HorizontalLayout(valueColumnField, labelColumnField);
        valueLabelRow.setWidthFull();

        editorLayout.add(
                lovCodeField,
                lovNameField,
                tableNameField,
                valueLabelRow,
                searchColumnField,
                gridColumnsField);
        return editorLayout;
    }

    private void refreshGrid() {
        grid.setItems(lovMetaRepository.findAll());
    }

    private void clearForm() {
        currentLovMeta = null;
        lovCodeField.clear();
        lovCodeField.setReadOnly(false);
        lovNameField.clear();
        tableNameField.clear();
        valueColumnField.clear();
        labelColumnField.clear();
        searchColumnField.clear();
        gridColumnsField.clear();
        grid.deselectAll();
    }

    private void populateForm(LovMeta lovMeta) {
        currentLovMeta = lovMeta;
        lovCodeField.setValue(lovMeta.getLovCode() != null ? lovMeta.getLovCode() : "");
        lovCodeField.setReadOnly(true);
        lovNameField.setValue(lovMeta.getLovName() != null ? lovMeta.getLovName() : "");
        tableNameField.setValue(lovMeta.getTableName() != null ? lovMeta.getTableName() : "");
        valueColumnField.setValue(lovMeta.getValueColumn() != null ? lovMeta.getValueColumn() : "");
        labelColumnField.setValue(lovMeta.getLabelColumn() != null ? lovMeta.getLabelColumn() : "");
        searchColumnField.setValue(lovMeta.getSearchColumn() != null ? lovMeta.getSearchColumn() : "");
        gridColumnsField.setValue(lovMeta.getGridColumns() != null ? lovMeta.getGridColumns() : "");
    }

    private void saveLovMeta() {
        String code = lovCodeField.getValue().trim();
        if (code.isEmpty()) {
            Notification.show("LOV Code tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
            return;
        }

        LovMeta lovMeta = currentLovMeta;
        if (lovMeta == null) {
            lovMeta = new LovMeta();
            lovMeta.setLovCode(code);
        }

        lovMeta.setLovName(lovNameField.getValue().trim());
        lovMeta.setTableName(tableNameField.getValue().trim());
        lovMeta.setValueColumn(valueColumnField.getValue().trim());
        lovMeta.setLabelColumn(labelColumnField.getValue().trim());
        lovMeta.setSearchColumn(searchColumnField.getValue().trim());
        lovMeta.setGridColumns(gridColumnsField.getValue().trim());

        try {
            lovMetaRepository.save(lovMeta);
            com.vaadinerp.components.ComponentFactory.clearLovCache(null);
            Notification.show("LOV berhasil disimpan!", 3000, Notification.Position.TOP_CENTER);
            refreshGrid();
            populateForm(lovMeta);
        } catch (Exception e) {
            Notification.show("Failed to save LOV: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void deleteLovMeta() {
        if (currentLovMeta != null && currentLovMeta.getLovCode() != null) {
            try {
                lovMetaRepository.deleteById(currentLovMeta.getLovCode());
                com.vaadinerp.components.ComponentFactory.clearLovCache(null);
                Notification.show("LOV berhasil dihapus!", 3000, Notification.Position.TOP_CENTER);
                refreshGrid();
                clearForm();
            } catch (Exception e) {
                Notification.show("Failed to delete LOV: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Please select a LOV to delete first.", 3000, Notification.Position.MIDDLE);
        }
    }

    private void copyLovMeta() {
        if (currentLovMeta != null) {
            lovCodeField.setReadOnly(false);
            lovCodeField.setValue(currentLovMeta.getLovCode() + "_COPY");
            lovNameField.setValue(currentLovMeta.getLovName() + " Copy");
            currentLovMeta = null;
            Notification.show("Silakan ubah LOV Code dan tekan Simpan.", 4000, Notification.Position.MIDDLE);
            lovCodeField.focus();
            grid.deselectAll();
        } else {
            Notification.show("Pilih LOV yang akan di-copy terlebih dahulu.", 3000, Notification.Position.MIDDLE);
        }
    }
}
