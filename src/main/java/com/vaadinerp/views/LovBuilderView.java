package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.meta.LovMetaRepository;

@Route("lov-builder")
public class LovBuilderView extends VerticalLayout {

    private final LovMetaRepository lovMetaRepository;
    private final com.vaadinerp.service.DynamicDataService dataService;

    private final Grid<LovMeta> grid = new Grid<>(LovMeta.class, false);

    private final TextField lovCodeField = new TextField("LOV Code (Unique)");
    private final TextField lovNameField = new TextField("LOV Name");
    private final TextArea tableNameField = new TextArea("Table Name / Query");
    private final TextField valueColumnField = new TextField("Value Column (Saved Value)");
    private final TextField labelColumnField = new TextField("Label Column (Display Text)");
    private final TextField searchColumnField = new TextField("Search Column(s)");
    private final TextArea gridColumnsField = new TextArea("Grid Columns Configuration");

    private final TextField searchGridField = new TextField();
    // Lazy loaded via repository directly

    // Tabel isian kolom popup; gridColumnsField (teks "kolom:Judul:lebar,...") tetap sumber yang disimpan.
    private static final class ColDef {
        final String name;
        boolean show;
        String header;
        String width;

        ColDef(String name, boolean show, String header, String width) {
            this.name = name;
            this.show = show;
            this.header = header;
            this.width = width;
        }
    }

    private final List<ColDef> colDefs = new ArrayList<>();
    private final ListDataProvider<ColDef> colDefProvider = new ListDataProvider<>(colDefs);
    private final Grid<ColDef> colGrid = new Grid<>();

    private LovMeta currentLovMeta;

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
        
        searchGridField.setPlaceholder("Search LOV...");
        searchGridField.setWidth("250px");
        searchGridField.setClearButtonVisible(true);
        searchGridField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        searchGridField.addValueChangeListener(e -> filterGrid());

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

        Button btnNew = new com.vaadinerp.components.SafeButton("New", VaadinIcon.PLUS_CIRCLE.create(),
                e -> clearForm());
        btnNew.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnNew.getStyle().set("color", "#22c55e");

        Button btnSave = new com.vaadinerp.components.SafeButton("Save", VaadinIcon.DOWNLOAD.create(),
                e -> saveLovMeta());
        btnSave.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("color", "#3b82f6");

        Button btnDelete = new com.vaadinerp.components.SafeButton("Delete", VaadinIcon.CLOSE_CIRCLE.create(),
                e -> deleteLovMeta());
        btnDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        Button btnCopy = new com.vaadinerp.components.SafeButton("Copy", VaadinIcon.COPY.create(), e -> copyLovMeta());
        btnCopy.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCopy.getStyle().set("color", "#f59e0b");

        Button btnRefresh = new com.vaadinerp.components.SafeButton("Refresh", VaadinIcon.REFRESH.create(),
                e -> refreshAll());
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnRefresh.getStyle().set("color", "#6b7280");

        toolbar.add(searchGridField, btnNew, btnSave, btnDelete, btnCopy, btnRefresh);
        return toolbar;
    }

    private void setupGrid() {
        grid.setSizeFull();
        grid.setPageSize(50);
        grid.addColumn(meta -> meta != null ? meta.getLovCode() : "").setHeader("LOV Code").setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(meta -> meta != null ? meta.getLovName() : "").setHeader("Name").setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(meta -> meta != null ? meta.getTableName() : "").setHeader("Table Name").setSortable(true)
                .setAutoWidth(true);
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
        tableNameField.setHeight("100px");
        tableNameField.setPlaceholder("e.g. master_customer or SELECT id, code, name FROM master_customer");
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
            if (e.isFromClient()) {
                rebuildColDefs();
            }
        });
        valueColumnField.setWidthFull();
        labelColumnField.setWidthFull();
        searchColumnField.setWidthFull();
        gridColumnsField.setWidthFull();
        gridColumnsField.setPlaceholder("e.g. code:Kode:100px,name:Nama:200px");
        gridColumnsField.setHeight("100px");
        gridColumnsField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                rebuildColDefs();
            }
        });

        searchColumnField.setPlaceholder("e.g. name,description");

        HorizontalLayout valueLabelRow = new HorizontalLayout(valueColumnField, labelColumnField);
        valueLabelRow.setWidthFull();

        setupColGrid();
        Span colGridTitle = new Span("Grid Columns Configuration");
        colGridTitle.getStyle().set("font-size", "var(--lumo-font-size-s)").set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)");
        Span colGridHelp = new Span("Tick the columns shown in the popup, set label and width, and use the arrows "
                + "to reorder. Not used when a form has the same code as this LOV: then use Show in Grid and "
                + "Col Order in Form Builder.");
        colGridHelp.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("color",
                "var(--lumo-secondary-text-color)");
        Details rawDetails = new Details("Advanced: raw text", gridColumnsField);
        rawDetails.setOpened(false);

        editorLayout.add(
                lovCodeField,
                lovNameField,
                tableNameField,
                valueLabelRow,
                searchColumnField,
                colGridTitle,
                colGrid,
                colGridHelp,
                rawDetails);
        return editorLayout;
    }

    private void setupColGrid() {
        colGrid.setDataProvider(colDefProvider);
        colGrid.setWidthFull();
        colGrid.setHeight("260px");
        colGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        colGrid.addComponentColumn(def -> {
            Checkbox cb = new Checkbox(def.show);
            cb.addValueChangeListener(e -> {
                def.show = e.getValue();
                syncRaw();
            });
            return cb;
        }).setHeader("Show").setFlexGrow(0).setWidth("70px");
        colGrid.addColumn(def -> def.name).setHeader("Column").setAutoWidth(true).setFlexGrow(1);
        colGrid.addComponentColumn(def -> cellField(def.header, "Label", v -> def.header = v)).setHeader("Label")
                .setFlexGrow(1);
        colGrid.addComponentColumn(def -> cellField(def.width, "150px", v -> def.width = v)).setHeader("Width")
                .setFlexGrow(0).setWidth("110px");
        colGrid.addComponentColumn(def -> {
            Button up = new com.vaadinerp.components.SafeButton(new Icon(VaadinIcon.ARROW_UP));
            Button down = new com.vaadinerp.components.SafeButton(new Icon(VaadinIcon.ARROW_DOWN));
            up.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            down.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            up.addClickListener(e -> moveColDef(def, -1));
            down.addClickListener(e -> moveColDef(def, 1));
            return new HorizontalLayout(up, down);
        }).setHeader("Order").setFlexGrow(0).setWidth("110px");
    }

    private TextField cellField(String value, String placeholder, Consumer<String> setter) {
        TextField tf = new TextField();
        tf.setValue(value != null ? value : "");
        tf.setPlaceholder(placeholder);
        tf.setWidthFull();
        tf.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL);
        tf.addValueChangeListener(e -> {
            // ',' dan ':' adalah pemisah format teks, jadi tidak boleh ikut tersimpan di judul/lebar.
            setter.accept(e.getValue() == null ? "" : e.getValue().replace(",", " ").replace(":", " ").trim());
            syncRaw();
        });
        return tf;
    }

    private void moveColDef(ColDef def, int delta) {
        int from = colDefs.indexOf(def);
        int to = from + delta;
        if (from < 0 || to < 0 || to >= colDefs.size()) {
            return;
        }
        java.util.Collections.swap(colDefs, from, to);
        colDefProvider.refreshAll();
        syncRaw();
    }

    /** Tabel -> teks "kolom:Judul:lebar,..." (hanya yang dicentang, urut sesuai tabel). */
    private void syncRaw() {
        StringBuilder sb = new StringBuilder();
        for (ColDef d : colDefs) {
            if (!d.show) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            boolean hasWidth = d.width != null && !d.width.isBlank();
            boolean hasHeader = d.header != null && !d.header.isBlank();
            sb.append(d.name);
            if (hasWidth) {
                sb.append(':').append(hasHeader ? d.header : d.name).append(':').append(d.width);
            } else if (hasHeader) {
                sb.append(':').append(d.header);
            }
        }
        gridColumnsField.setValue(sb.toString());
    }

    /** Teks -> tabel, lalu kolom lain dari tabel/query LOV ditambahkan di bawah (tidak dicentang). */
    private void rebuildColDefs() {
        colDefs.clear();
        Set<String> seen = new HashSet<>();
        String raw = gridColumnsField.getValue() != null ? gridColumnsField.getValue() : "";
        for (String part : raw.split(",")) {
            String[] p = part.split(":");
            String name = p[0].trim();
            if (name.isEmpty() || !seen.add(name.toLowerCase())) {
                continue;
            }
            colDefs.add(new ColDef(name, true, p.length > 1 ? p[1].trim() : "", p.length > 2 ? p[2].trim() : ""));
        }
        String table = tableNameField.getValue() != null ? tableNameField.getValue().trim() : "";
        if (!table.isEmpty()) {
            for (String c : dataService.getColumnsForQueryOrTable(table)) {
                if (seen.add(c.toLowerCase())) {
                    colDefs.add(new ColDef(c, false, c.substring(0, 1).toUpperCase() + c.substring(1).replace("_", " "),
                            ""));
                }
            }
        }
        colDefProvider.refreshAll();
    }

    private void refreshGrid() {
        filterGrid();
    }

    private void refreshAll() {
        com.vaadinerp.components.ComponentFactory.clearLovCache(null);
        refreshGrid();
        if (currentLovMeta != null && currentLovMeta.getLovCode() != null) {
            String code = currentLovMeta.getLovCode();
            lovMetaRepository.findById(code).ifPresentOrElse(
                    this::populateForm,
                    this::clearForm
            );
        }
        Notification.show("LOV list and cache refreshed!", 1500, Notification.Position.BOTTOM_END);
    }

    private void filterGrid() {
        grid.setItems(query -> {
            int page = query.getPage();
            int pageSize = query.getPageSize();
            // Default sort code ascending if not specified
            org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "lovCode");
            
            if (!query.getSortOrders().isEmpty()) {
                com.vaadin.flow.data.provider.QuerySortOrder order = query.getSortOrders().get(0);
                sort = org.springframework.data.domain.Sort.by(
                    order.getDirection() == com.vaadin.flow.data.provider.SortDirection.ASCENDING ? 
                        org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC,
                    order.getSorted()
                );
            }
            
            org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, pageSize, sort);
            String search = searchGridField.getValue() != null ? searchGridField.getValue().trim() : "";
            
            if (search.isEmpty()) {
                return lovMetaRepository.findAll(pageRequest).stream();
            } else {
                return lovMetaRepository.searchAll(search, pageRequest).stream();
            }
        });
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
        rebuildColDefs();
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
        rebuildColDefs();
    }

    private void saveLovMeta() {
        String code = lovCodeField.getValue().trim();
        if (code.isEmpty()) {
            Notification.show("LOV Code cannot be empty!", 3000, Notification.Position.MIDDLE);
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
            Notification.show("LOV saved successfully!", 3000, Notification.Position.TOP_CENTER);
            refreshGrid();
            populateForm(lovMeta);
        } catch (Exception e) {
            Notification.show("Failed to save LOV: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void deleteLovMeta() {
        if (currentLovMeta != null && currentLovMeta.getLovCode() != null) {
            String code = currentLovMeta.getLovCode();
            String name = currentLovMeta.getLovName() != null ? currentLovMeta.getLovName() : code;

            ConfirmDialog confirmDlg = new ConfirmDialog();
            confirmDlg.setHeader("Confirm Delete");
            confirmDlg.setText("Are you sure you want to delete LOV definition '" + code + "' (" + name + ")?");
            confirmDlg.setCancelable(true);
            confirmDlg.setConfirmText("Delete");
            confirmDlg.setConfirmButtonTheme("error primary");
            confirmDlg.addConfirmListener(ev -> {
                try {
                    lovMetaRepository.deleteById(code);
                    com.vaadinerp.components.ComponentFactory.clearLovCache(null);
                    Notification.show("LOV deleted successfully!", 3000, Notification.Position.TOP_CENTER);
                    refreshGrid();
                    clearForm();
                } catch (Exception e) {
                    Notification.show("Failed to delete LOV: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            });
            confirmDlg.open();
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
            Notification.show("Please change the LOV Code and press Save.", 4000, Notification.Position.MIDDLE);
            lovCodeField.focus();
            grid.deselectAll();
        } else {
            Notification.show("Please select the LOV to copy first.", 3000, Notification.Position.MIDDLE);
        }
    }
}
