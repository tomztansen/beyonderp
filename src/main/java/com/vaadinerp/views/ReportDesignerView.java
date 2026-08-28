package com.vaadinerp.views;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadinerp.components.SafeButton;
import com.vaadinerp.components.StandardGridUtils;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.meta.ReportParamMeta;
import com.vaadinerp.report.ReportResolver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Report Designer: filterable report list (grid + toolbar) + editor tab (metadata,
 * parameters, save-first) and Stimulsoft design surface. Jasper upload and preview are
 * added in the next task.
 */
@Route("report-designer")
public class ReportDesignerView extends VerticalLayout {

    private final ReportMetaRepository reportMetaRepository;
    private final FormMetaRepository formMetaRepository;
    private final ReportResolver reportResolver;
    private final com.vaadinerp.report.ReportRunService reportRunService;
    private final com.vaadinerp.report.JasperUploadService jasperUploadService;
    private final com.vaadinerp.security.service.SessionSecurityService securityService;

    private final Grid<ReportMeta> grid = new Grid<>(ReportMeta.class, false);
    private final TabSheet tabs = new TabSheet();
    private final HorizontalLayout mainToolbar = new HorizontalLayout();

    // Editor tab has two toggled containers: the definition form and the design surface.
    private final VerticalLayout editorTab = new VerticalLayout();
    private final VerticalLayout editorForm = new VerticalLayout();
    private final VerticalLayout designSurface = new VerticalLayout();

    private Runnable reapplyFilters = () -> {};
    private Runnable paramReapply = () -> {};

    // Editor state
    private String editingCode = null;
    private final List<ReportParamMeta> paramState = new ArrayList<>();
    private ReportBuilderView embeddedBuilder = null; // active only during STANDARD design

    // Metadata inputs
    private final TextField codeField = new TextField("Report Code");
    private final TextField titleField = new TextField("Report Title");
    private final ComboBox<FormMeta> sourceCombo = new ComboBox<>("Source Table / Form");
    private final TextArea queryArea = new TextArea("Custom SQL Query (overrides source)");
    private final Select<String> pageSelect = new Select<>();
    private final Select<String> orientSelect = new Select<>();
    private final Select<String> engineSelect = new Select<>();
    private final ComboBox<String> categoryCombo = new ComboBox<>("Category");
    private final TextArea descriptionArea = new TextArea("Description");
    private final com.vaadin.flow.component.combobox.MultiSelectComboBox<String> rolesSelect =
            new com.vaadin.flow.component.combobox.MultiSelectComboBox<>("Allowed Roles");
    private final Grid<ReportParamMeta> paramGrid = new Grid<>(ReportParamMeta.class, false);

    private static final List<String> COMPONENT_TYPES = List.of(
            "TEXTBOX", "TEXTAREA", "INTEGERFIELD", "DECIMAL", "DATE", "DATETIME", "TIME",
            "CHECKBOX", "COMBOBOX", "LISTBOX", "CHOSENBOX", "BANDBOX");
    private static final List<String> PARAM_SOURCES = List.of("USER_INPUT", "FORM_FIELD", "SYSTEM");

    // Shared, bounded, daemon pool for off-UI preview rendering (replaces per-click raw threads:
    // won't delay JVM shutdown, rapid clicks queue instead of spawning unbounded threads).
    private static final java.util.concurrent.ExecutorService PREVIEW_EXECUTOR =
            java.util.concurrent.Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r, "report-preview");
                t.setDaemon(true);
                return t;
            });

    public ReportDesignerView(ReportMetaRepository reportMetaRepository,
                              FormMetaRepository formMetaRepository,
                              ReportResolver reportResolver,
                              com.vaadinerp.report.ReportRunService reportRunService,
                              com.vaadinerp.report.JasperUploadService jasperUploadService,
                              com.vaadinerp.security.service.SessionSecurityService securityService) {
        this.reportMetaRepository = reportMetaRepository;
        this.formMetaRepository = formMetaRepository;
        this.reportResolver = reportResolver;
        this.reportRunService = reportRunService;
        this.jasperUploadService = jasperUploadService;
        this.securityService = securityService;
        setSizeFull();

        setupGrid();
        setupEditorForm();

        editorTab.setSizeFull();
        editorTab.setPadding(false);
        designSurface.setSizeFull();
        designSurface.setPadding(false);
        designSurface.setVisible(false);
        editorTab.add(editorForm, designSurface);

        VerticalLayout listLayout = new VerticalLayout(grid);
        listLayout.setSizeFull();
        listLayout.setPadding(false);
        tabs.add("Report List", listLayout);
        tabs.add("Editor", editorTab);
        tabs.setSizeFull();

        mainToolbar.setWidthFull();
        mainToolbar.setPadding(true);
        mainToolbar.setSpacing(true);
        tabs.addSelectedChangeListener(e -> updateToolbar());
        updateToolbar();

        setPadding(false);
        add(mainToolbar, tabs);
        setFlexGrow(1, tabs);
    }

    /** One toolbar above the tabs (like GenericFormView), contents depend on the active tab. */
    private void updateToolbar() {
        mainToolbar.removeAll();
        if (tabs.getSelectedIndex() == 1) { // Editor tab
            mainToolbar.add(
                tbBtn("Save", com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD, e -> saveReport()),
                tbBtn("Cancel", com.vaadin.flow.component.icon.VaadinIcon.BAN, e -> cancelEdit()),
                tbBtn("Back to List", com.vaadin.flow.component.icon.VaadinIcon.ARROW_LEFT,
                        e -> { showForm(); refreshGrid(); tabs.setSelectedIndex(0); })
            );
        } else { // Report List tab
            mainToolbar.add(
                tbBtn("New", com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE, e -> openEditor(null)),
                tbBtn("Edit", com.vaadin.flow.component.icon.VaadinIcon.EDIT, e -> withSelected(this::openEditor)),
                tbBtn("Design", com.vaadin.flow.component.icon.VaadinIcon.MAGIC, e -> withSelected(this::openDesigner)),
                tbBtn("Delete", com.vaadin.flow.component.icon.VaadinIcon.CLOSE_CIRCLE, e -> withSelected(this::deleteReport)),
                tbBtn("Preview", com.vaadin.flow.component.icon.VaadinIcon.EYE, e -> withSelected(this::preview)),
                tbBtn("Refresh", com.vaadin.flow.component.icon.VaadinIcon.REFRESH, e -> refreshGrid()),
                StandardGridUtils.createExportExcelButton(grid, "report_list")
            );
        }
    }

    // ---------- Report list ----------

    private void setupGrid() {
        Grid.Column<ReportMeta> colCode = grid.addColumn(ReportMeta::getReportCode).setHeader("Code").setAutoWidth(true);
        Grid.Column<ReportMeta> colTitle = grid.addColumn(ReportMeta::getReportTitle).setHeader("Title").setAutoWidth(true);
        Grid.Column<ReportMeta> colEngine = grid.addColumn(this::engineOf).setHeader("Engine").setAutoWidth(true);
        Grid.Column<ReportMeta> colSource = grid.addColumn(ReportMeta::getTableName).setHeader("Source").setAutoWidth(true);
        Grid.Column<ReportMeta> colCategory = grid.addColumn(ReportMeta::getCategory).setHeader("Category").setAutoWidth(true);
        Grid.Column<ReportMeta> colPage = grid.addColumn(ReportMeta::getPageSize).setHeader("Page Size").setAutoWidth(true);
        Grid.Column<ReportMeta> colOrient = grid.addColumn(ReportMeta::getOrientation).setHeader("Orientation").setAutoWidth(true);
        Grid.Column<ReportMeta> colRoles = grid.addColumn(this::rolesText).setHeader("Roles").setAutoWidth(true);
        Grid.Column<ReportMeta> colDesc = grid.addColumn(ReportMeta::getDescription).setHeader("Description").setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.MULTI); // kolom centang seperti grid form
        grid.setSizeFull();
        grid.addItemDoubleClickListener(e -> openEditor(e.getItem())); // double-click = edit

        Map<Grid.Column<ReportMeta>, Function<ReportMeta, String>> colGetters = new LinkedHashMap<>();
        colGetters.put(colCode, r -> nz(r.getReportCode()));
        colGetters.put(colTitle, r -> nz(r.getReportTitle()));
        colGetters.put(colEngine, this::engineOf);
        colGetters.put(colSource, r -> nz(r.getTableName()));
        colGetters.put(colCategory, r -> nz(r.getCategory()));
        colGetters.put(colPage, r -> nz(r.getPageSize()));
        colGetters.put(colOrient, r -> nz(r.getOrientation()));
        colGetters.put(colRoles, this::rolesText);
        colGetters.put(colDesc, r -> nz(r.getDescription()));
        this.reapplyFilters = StandardGridUtils.attachGridFilters(grid, colGetters, reportMetaRepository::findAll);
        StandardGridUtils.enableRowClickSelection(grid); // klik sel → row terselect (seperti grid form)
        refreshGrid();
    }

    private SafeButton tbBtn(String text, com.vaadin.flow.component.icon.VaadinIcon icon,
                            com.vaadin.flow.component.ComponentEventListener<
                                    com.vaadin.flow.component.ClickEvent<com.vaadin.flow.component.button.Button>> listener) {
        SafeButton b = new SafeButton(text, listener);
        b.setIcon(icon.create());
        b.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        return b;
    }

    private String engineOf(ReportMeta r) {
        return r.getEngineType() != null ? r.getEngineType() : "STANDARD";
    }

    private String nz(String s) {
        return s != null ? s : "";
    }

    private String rolesText(ReportMeta r) {
        return r.getAllowedRoles() == null || r.getAllowedRoles().isEmpty()
                ? "" : String.join(", ", r.getAllowedRoles());
    }

    /** Cari FormMeta by tableName tanpa mengasumsikan unik (findByTableName melempar bila >1). */
    private FormMeta findFormByTableName(String tableName) {
        if (tableName == null || tableName.isBlank()) return null;
        return formMetaRepository.findAll().stream()
                .filter(f -> tableName.equalsIgnoreCase(f.getTableName()))
                .findFirst().orElse(null);
    }

    private void refreshGrid() {
        reapplyFilters.run();
    }

    private void withSelected(java.util.function.Consumer<ReportMeta> action) {
        java.util.Set<ReportMeta> sel = grid.getSelectedItems();
        if (sel.isEmpty()) {
            Notification.show("Please select a report first.");
            return;
        }
        action.accept(sel.iterator().next());
    }

    // ---------- Editor form (built once) ----------

    private void setupEditorForm() {
        codeField.setRequiredIndicatorVisible(true);
        titleField.setRequiredIndicatorVisible(true);

        sourceCombo.setItems(formMetaRepository.findAll());
        sourceCombo.setItemLabelGenerator(f -> f.getFormTitle() + " (" + f.getTableName() + ")");
        sourceCombo.setClearButtonVisible(true);

        queryArea.setMinHeight("90px");
        queryArea.setPlaceholder("SELECT * FROM ... WHERE col = :param");

        pageSelect.setLabel("Page Size");
        pageSelect.setItems("A4", "LETTER");
        pageSelect.setValue("A4");
        orientSelect.setLabel("Orientation");
        orientSelect.setItems("PORTRAIT", "LANDSCAPE");
        orientSelect.setValue("PORTRAIT");
        engineSelect.setLabel("Engine");
        engineSelect.setItems("STANDARD", "STIMULSOFT", "JASPER");
        engineSelect.setValue("STANDARD");

        categoryCombo.setAllowCustomValue(true);
        categoryCombo.setItems(reportMetaRepository.findAll().stream()
                .map(ReportMeta::getCategory).filter(c -> c != null && !c.isBlank()).distinct().sorted().toList());
        categoryCombo.addCustomValueSetListener(e -> categoryCombo.setValue(e.getDetail()));
        descriptionArea.setMinHeight("60px");
        try {
            rolesSelect.setItems(com.vaadinerp.config.SpringContextHolder
                    .getBean(com.vaadinerp.security.repository.AppRoleRepository.class)
                    .findAll().stream().map(com.vaadinerp.security.entity.AppRole::getRoleCode)
                    .filter(java.util.Objects::nonNull).sorted().toList());
        } catch (Exception ignored) {}
        rolesSelect.setHelperText("Empty = only SUPER_ADMIN can run this report");

        FormLayout meta = new FormLayout(codeField, titleField, categoryCombo, sourceCombo, queryArea,
                descriptionArea, rolesSelect, pageSelect, orientSelect, engineSelect);
        meta.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 4));
        meta.setColspan(titleField, 3);
        meta.setColspan(sourceCombo, 2);
        meta.setColspan(queryArea, 2);
        meta.setColspan(descriptionArea, 2);
        meta.setColspan(rolesSelect, 2);

        // Inline-editable parameter grid with the SAME look/feel as the report list grid
        paramGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        paramGrid.setAllRowsVisible(true);
        com.vaadin.flow.data.binder.Binder<ReportParamMeta> pBinder =
                new com.vaadin.flow.data.binder.Binder<>(ReportParamMeta.class);
        paramGrid.getEditor().setBinder(pBinder);
        paramGrid.getEditor().setBuffered(false);

        TextField edName = new TextField();
        Grid.Column<ReportParamMeta> pColName = paramGrid.addColumn(ReportParamMeta::getParamName)
                .setHeader("Name").setEditorComponent(edName);
        pBinder.forField(edName).bind(ReportParamMeta::getParamName, ReportParamMeta::setParamName);

        TextField edLabel = new TextField();
        Grid.Column<ReportParamMeta> pColLabel = paramGrid.addColumn(ReportParamMeta::getParamLabel)
                .setHeader("Label").setEditorComponent(edLabel);
        pBinder.forField(edLabel).bind(ReportParamMeta::getParamLabel, ReportParamMeta::setParamLabel);

        Select<String> edType = new Select<>();
        edType.setItems(COMPONENT_TYPES);
        Grid.Column<ReportParamMeta> pColType = paramGrid.addColumn(ReportParamMeta::getParamType)
                .setHeader("Type").setEditorComponent(edType);
        pBinder.forField(edType).bind(ReportParamMeta::getParamType, ReportParamMeta::setParamType);

        ComboBox<String> edLov = new ComboBox<>();
        edLov.setClearButtonVisible(true);
        try {
            edLov.setItems(com.vaadinerp.config.SpringContextHolder
                    .getBean(com.vaadinerp.meta.LovMetaRepository.class)
                    .findAll().stream().map(com.vaadinerp.meta.LovMeta::getLovCode)
                    .filter(java.util.Objects::nonNull).sorted().toList());
        } catch (Exception ignored) {}
        Grid.Column<ReportParamMeta> pColLov = paramGrid.addColumn(ReportParamMeta::getLovCode)
                .setHeader("LOV Code").setEditorComponent(edLov);
        pBinder.forField(edLov).bind(ReportParamMeta::getLovCode, ReportParamMeta::setLovCode);

        Select<String> edSource = new Select<>();
        edSource.setItems(PARAM_SOURCES);
        Grid.Column<ReportParamMeta> pColSource = paramGrid.addColumn(ReportParamMeta::getSource)
                .setHeader("Source").setEditorComponent(edSource);
        pBinder.forField(edSource).bind(ReportParamMeta::getSource, ReportParamMeta::setSource);

        TextField edSourceKey = new TextField();
        Grid.Column<ReportParamMeta> pColKey = paramGrid.addColumn(ReportParamMeta::getSourceKey)
                .setHeader("Source Key").setEditorComponent(edSourceKey);
        pBinder.forField(edSourceKey).bind(ReportParamMeta::getSourceKey, ReportParamMeta::setSourceKey);

        TextField edDefault = new TextField();
        Grid.Column<ReportParamMeta> pColDef = paramGrid.addColumn(ReportParamMeta::getDefaultValue)
                .setHeader("Default").setEditorComponent(edDefault);
        pBinder.forField(edDefault).bind(ReportParamMeta::getDefaultValue, ReportParamMeta::setDefaultValue);

        Checkbox edRequired = new Checkbox();
        Grid.Column<ReportParamMeta> pColReq = paramGrid.addColumn(p -> p.isRequired() ? "Yes" : "No")
                .setHeader("Required").setEditorComponent(edRequired);
        pBinder.forField(edRequired).bind(ReportParamMeta::isRequired, ReportParamMeta::setRequired);

        TextField edFilterCol = new TextField();
        Grid.Column<ReportParamMeta> pColFilter = paramGrid.addColumn(ReportParamMeta::getFilterColumn)
                .setHeader("Filter Column").setEditorComponent(edFilterCol);
        pBinder.forField(edFilterCol).bind(ReportParamMeta::getFilterColumn, ReportParamMeta::setFilterColumn);

        Select<String> edOperator = new Select<>();
        edOperator.setItems("", "=", "!=", "LIKE", "ILIKE", ">=", "<=", ">", "<", "IN");
        Grid.Column<ReportParamMeta> pColOp = paramGrid.addColumn(ReportParamMeta::getOperator)
                .setHeader("Operator").setEditorComponent(edOperator);
        pBinder.forField(edOperator).bind(ReportParamMeta::getOperator, ReportParamMeta::setOperator);

        // LOV Filter: expand-row (inline sub-grid) — dukung banyak filter STATIC/FIELD per parameter
        paramGrid.addComponentColumn(p -> {
            com.vaadin.flow.component.button.Button b =
                    new com.vaadin.flow.component.button.Button(com.vaadin.flow.component.icon.VaadinIcon.FILTER.create());
            b.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE,
                    com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);
            int n = (p.getFilters() != null) ? p.getFilters().size() : 0;
            b.getElement().setAttribute("title", "LOV Filters" + (n > 0 ? " (" + n + ")" : ""));
            if (n > 0) b.setText(String.valueOf(n));
            b.addClickListener(e -> paramGrid.setDetailsVisible(p, !paramGrid.isDetailsVisible(p)));
            return b;
        }).setHeader("Filters").setWidth("90px").setFlexGrow(0);

        paramGrid.setDetailsVisibleOnClick(false);
        paramGrid.setItemDetailsRenderer(new com.vaadin.flow.data.renderer.ComponentRenderer<>(p ->
                new com.vaadinerp.components.ParamFilterEditor(p, () ->
                        paramState.stream().map(ReportParamMeta::getParamName)
                                .filter(nm -> nm != null && !nm.equals(p.getParamName()))
                                .collect(java.util.stream.Collectors.toList()))));

        // Same treatment as report list grid: filter header, sort, resize, clipboard, row-click select
        Map<Grid.Column<ReportParamMeta>, Function<ReportParamMeta, String>> pGetters = new LinkedHashMap<>();
        pGetters.put(pColName, p -> nz(p.getParamName()));
        pGetters.put(pColLabel, p -> nz(p.getParamLabel()));
        pGetters.put(pColType, p -> nz(p.getParamType()));
        pGetters.put(pColLov, p -> nz(p.getLovCode()));
        pGetters.put(pColSource, p -> nz(p.getSource()));
        pGetters.put(pColKey, p -> nz(p.getSourceKey()));
        pGetters.put(pColDef, p -> nz(p.getDefaultValue()));
        pGetters.put(pColReq, p -> p.isRequired() ? "Yes" : "No");
        pGetters.put(pColFilter, p -> nz(p.getFilterColumn()));
        pGetters.put(pColOp, p -> nz(p.getOperator()));
        this.paramReapply = StandardGridUtils.attachGridFilters(paramGrid, pGetters, () -> new ArrayList<>(paramState));
        StandardGridUtils.enableRowClickSelection(paramGrid);

        // Double-click a row to edit; editor writes back live (unbuffered)
        paramGrid.addItemDoubleClickListener(e -> paramGrid.getEditor().editItem(e.getItem()));
        paramGrid.getEditor().addCloseListener(e -> paramGrid.getDataProvider().refreshItem(e.getItem()));

        HorizontalLayout paramToolbar = new HorizontalLayout(
            tbBtn("Add Parameter", com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE, e -> {
                ReportParamMeta np = new ReportParamMeta();
                np.setParamName("param" + (paramState.size() + 1));
                np.setParamType("TEXTBOX");
                np.setSource("USER_INPUT");
                paramState.add(np);
                paramReapply.run();
                paramGrid.getEditor().editItem(np);
            }),
            tbBtn("Remove Parameter", com.vaadin.flow.component.icon.VaadinIcon.TRASH, e -> {
                java.util.Set<ReportParamMeta> sel = paramGrid.getSelectedItems();
                if (sel.isEmpty()) { Notification.show("Please select a parameter."); return; }
                if (paramGrid.getEditor().isOpen()) paramGrid.getEditor().cancel();
                paramState.removeAll(sel);
                paramReapply.run();
            }),
            StandardGridUtils.createExportExcelButton(paramGrid, "report_parameters")
        );
        paramToolbar.setPadding(false);

        editorForm.setPadding(false);
        editorForm.add(new com.vaadin.flow.component.html.H4("Report Definition"), meta,
                new com.vaadin.flow.component.html.H4("Parameters"), paramToolbar, paramGrid);
    }

    private void showForm() {
        embeddedBuilder = null;
        designSurface.setVisible(false);
        designSurface.removeAll();
        editorForm.setVisible(true);
    }

    private void openEditor(ReportMeta report) {
        loadReportState(report);
        showForm();
        tabs.setSelectedIndex(1);
    }

    /** Load metadata + parameters into the editor state (shared by Edit and Design). */
    private void loadReportState(ReportMeta report) {
        paramState.clear();
        if (report == null) {
            editingCode = null;
            codeField.clear();
            codeField.setReadOnly(false);
            titleField.clear();
            sourceCombo.clear();
            queryArea.clear();
            pageSelect.setValue("A4");
            orientSelect.setValue("PORTRAIT");
            engineSelect.setValue("STANDARD");
            categoryCombo.clear();
            descriptionArea.clear();
            rolesSelect.clear();
        } else {
            editingCode = report.getReportCode();
            codeField.setValue(nz(report.getReportCode()));
            codeField.setReadOnly(true);
            titleField.setValue(nz(report.getReportTitle()));
            sourceCombo.setValue(findFormByTableName(report.getTableName()));
            queryArea.setValue(nz(report.getDataQuery()));
            pageSelect.setValue(report.getPageSize() != null ? report.getPageSize() : "A4");
            orientSelect.setValue(report.getOrientation() != null ? report.getOrientation() : "PORTRAIT");
            engineSelect.setValue(engineOf(report));
            categoryCombo.setValue(report.getCategory());
            descriptionArea.setValue(nz(report.getDescription()));
            rolesSelect.setValue(report.getAllowedRoles() != null ? report.getAllowedRoles() : java.util.Set.of());
            if (report.getParams() != null) {
                for (ReportParamMeta p : report.getParams()) paramState.add(cloneParam(p));
            }
        }
        paramReapply.run();
    }

    private void cancelEdit() {
        if (editingCode != null) {
            reportMetaRepository.findById(editingCode).ifPresent(this::openEditor);
        } else {
            openEditor(null);
        }
        Notification.show("Unsaved changes discarded.");
    }

    private ReportParamMeta cloneParam(ReportParamMeta s) {
        ReportParamMeta c = new ReportParamMeta();
        c.setParamName(s.getParamName());
        c.setParamLabel(s.getParamLabel());
        c.setParamType(s.getParamType());
        c.setLovCode(s.getLovCode());
        c.setSource(s.getSource());
        c.setSourceKey(s.getSourceKey());
        c.setDefaultValue(s.getDefaultValue());
        c.setRequired(s.isRequired());
        c.setColOrder(s.getColOrder());
        c.setFilterColumn(s.getFilterColumn());
        c.setOperator(s.getOperator());
        c.setLovFilterColumn(s.getLovFilterColumn());
        c.setLovFilterValue(s.getLovFilterValue());
        c.setLovFilterOperator(s.getLovFilterOperator());
        if (s.getFilters() != null) {
            for (com.vaadinerp.meta.ReportParamFilterMeta sf : s.getFilters()) {
                com.vaadinerp.meta.ReportParamFilterMeta cf = new com.vaadinerp.meta.ReportParamFilterMeta();
                cf.setFilterColumn(sf.getFilterColumn());
                cf.setSourceType(sf.getSourceType());
                cf.setSourceName(sf.getSourceName());
                cf.setComparisonOperator(sf.getComparisonOperator());
                cf.setLogicalOperator(sf.getLogicalOperator());
                cf.setParamMeta(c);
                c.getFilters().add(cf);
            }
        }
        return c;
    }

    private void saveReport() {
        String code = codeField.getValue() == null ? "" : codeField.getValue().trim();
        String title = titleField.getValue() == null ? "" : titleField.getValue().trim();
        if (code.isEmpty() || title.isEmpty()) {
            Notification.show("Report Code and Title are required.");
            return;
        }
        if (!reportResolver.isValidReportCode(code)) {
            Notification.show("Report Code may only contain letters, digits, underscore and hyphen.");
            return;
        }
        FormMeta src = sourceCombo.getValue();
        String query = queryArea.getValue();

        ReportMeta rep = (editingCode == null)
                ? new ReportMeta()
                : reportMetaRepository.findById(editingCode).orElse(new ReportMeta());
        rep.setReportCode(code);
        rep.setReportTitle(title);
        rep.setTableName(src != null ? src.getTableName() : rep.getTableName());
        rep.setDataQuery(query == null || query.isBlank() ? null : query);
        rep.setPageSize(pageSelect.getValue());
        rep.setOrientation(orientSelect.getValue());
        rep.setEngineType(engineSelect.getValue());
        rep.setCategory(categoryCombo.getValue() == null || categoryCombo.getValue().isBlank() ? null : categoryCombo.getValue().trim());
        rep.setDescription(descriptionArea.getValue() == null || descriptionArea.getValue().isBlank() ? null : descriptionArea.getValue());
        rep.setAllowedRoles(new java.util.HashSet<>(rolesSelect.getValue()));

        if (rep.getParams() == null) rep.setParams(new ArrayList<>());
        rep.getParams().clear(); // orphanRemoval deletes the old ones
        for (int i = 0; i < paramState.size(); i++) {
            ReportParamMeta p = paramState.get(i);
            p.setReportMeta(rep);
            p.setColOrder(i + 1);
            if (p.getFilters() != null) {
                p.getFilters().removeIf(fl -> fl.getFilterColumn() == null || fl.getFilterColumn().isBlank()
                        || fl.getSourceName() == null || fl.getSourceName().isBlank());
                for (com.vaadinerp.meta.ReportParamFilterMeta fl : p.getFilters()) fl.setParamMeta(p);
            }
            rep.getParams().add(p);
        }

        // Band elements from the embedded STANDARD designer (single Save covers everything).
        // When not designing (embeddedBuilder == null), existing elements are left untouched.
        if (embeddedBuilder != null) {
            if (rep.getElements() == null) rep.setElements(new ArrayList<>());
            rep.getElements().clear();
            for (ReportElementMeta el : embeddedBuilder.collectElements()) {
                el.setReportMeta(rep);
                rep.getElements().add(el);
            }
        }

        try {
            reportMetaRepository.save(rep);
            editingCode = rep.getReportCode();
            codeField.setReadOnly(true);
            refreshGrid();
            Notification.show("Report saved. Select it in the list to design.");
        } catch (Exception ex) {
            Notification.show("Failed to save report: " + ex.getMessage());
        }
    }

    private void deleteReport(ReportMeta report) {
        ConfirmDialog dlg = new ConfirmDialog();
        dlg.setHeader("Delete Report");
        dlg.setText("Delete report " + report.getReportCode() + " including its template and parameters?");
        dlg.setConfirmText("Delete");
        dlg.setCancelable(true);
        dlg.addConfirmListener(e -> {
            reportMetaRepository.deleteById(report.getReportCode());
            try {
                if (report.getEngineType() != null && !"STANDARD".equalsIgnoreCase(report.getEngineType())) {
                    java.io.File f = reportResolver.resolveMasterTemplate(
                            report.getReportCode(), report.getEngineType(), report.getTemplatePath());
                    if (f.exists()) f.delete();
                }
            } catch (Exception ignored) {}
            refreshGrid();
            Notification.show("Report deleted.");
        });
        dlg.open();
    }

    private void openDesigner(ReportMeta report) {
        loadReportState(report);     // load metadata+params so the single top Save persists everything
        embeddedBuilder = null;
        editorForm.setVisible(false);
        designSurface.removeAll();
        designSurface.setVisible(true);

        String engine = engineOf(report);
        if ("STIMULSOFT".equalsIgnoreCase(engine)) {
            IFrame ifr = new IFrame("/stimulsoft-java/designer?code=" + report.getReportCode());
            ifr.setSizeFull();
            ifr.getStyle().set("border", "none");
            designSurface.add(ifr);
            designSurface.setFlexGrow(1, ifr);
        } else if ("JASPER".equalsIgnoreCase(engine)) {
            designSurface.add(buildJasperUpload(report.getReportCode()));
        } else { // STANDARD — embed band designer (canvas only); the single top toolbar Save persists it
            ReportBuilderView rb = new ReportBuilderView(reportMetaRepository, formMetaRepository, this::refreshGrid);
            rb.setEmbeddedMode(true);
            rb.loadReport(report.getReportCode());
            rb.setSizeFull();
            embeddedBuilder = rb;
            designSurface.add(rb);
            designSurface.setFlexGrow(1, rb);
        }
        tabs.setSelectedIndex(1);
    }

    private com.vaadin.flow.component.Component buildJasperUpload(String code) {
        com.vaadin.flow.component.upload.receivers.MemoryBuffer buffer =
                new com.vaadin.flow.component.upload.receivers.MemoryBuffer();
        com.vaadin.flow.component.upload.Upload upload = new com.vaadin.flow.component.upload.Upload(buffer);
        upload.setAcceptedFileTypes(".jasper", ".jrxml");
        upload.setMaxFiles(1);
        upload.addSucceededListener(e -> {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                jasperUploadService.saveUpload(code, e.getFileName(), bytes);
                Notification.show("Template uploaded: " + e.getFileName());
            } catch (Exception ex) {
                Notification.show(ex.getMessage() != null ? ex.getMessage() : "Upload failed");
            }
        });
        VerticalLayout box = new VerticalLayout(
                new com.vaadin.flow.component.html.H4("Jasper Template Upload"),
                new com.vaadin.flow.component.html.Span(
                        "Upload a .jasper (compiled) or .jrxml (source, validated on upload), authored in "
                        + "JasperSoft Studio matching the runtime JasperReports version."),
                upload);
        box.setPadding(false);
        return box;
    }

    private void preview(ReportMeta report) {
        String user = (securityService != null && securityService.getCurrentUser() != null)
                ? securityService.getCurrentUser().getUsername() : null;
        Map<String, Object> params = new java.util.HashMap<>(
                com.vaadinerp.report.ReportParamResolver.resolveAuto(report.getParams(), java.util.Map.of(), user));
        if (report.getParams() != null) {
            for (ReportParamMeta p : report.getParams()) {
                params.putIfAbsent(p.getParamName(), p.getDefaultValue()); // ensure all :params bound for preview
            }
        }

        if ("STIMULSOFT".equalsIgnoreCase(engineOf(report))) {
            com.vaadinerp.report.ReportRunResult res = reportRunService.run(report, params, true);
            getUI().ifPresent(ui -> ui.getPage().open(res.viewerUrl(), "_blank"));
            return;
        }

        // Standard / Jasper: render off the UI thread, show in a dialog when ready
        com.vaadin.flow.component.UI ui = com.vaadin.flow.component.UI.getCurrent();
        com.vaadin.flow.component.dialog.Dialog d = new com.vaadin.flow.component.dialog.Dialog();
        d.setHeaderTitle("Preview: " + report.getReportCode());
        d.setWidth("80vw");
        d.setHeight("80vh");
        com.vaadin.flow.component.progressbar.ProgressBar pb = new com.vaadin.flow.component.progressbar.ProgressBar();
        pb.setIndeterminate(true);
        d.add(pb);
        d.open();

        PREVIEW_EXECUTOR.submit(() -> {
            try {
                com.vaadinerp.report.ReportRunResult res = reportRunService.run(report, params, true);
                ui.access(() -> {
                    d.removeAll();
                    com.vaadinerp.report.render.ReportOutput out = res.output();
                    if (out.contentType().startsWith("text/html")) {
                        d.add(new com.vaadin.flow.component.Html("<div style=\"overflow:auto\">"
                                + new String(out.bytes(), java.nio.charset.StandardCharsets.UTF_8) + "</div>"));
                    } else {
                        String b64 = java.util.Base64.getEncoder().encodeToString(out.bytes());
                        IFrame ifr = new IFrame("data:" + out.contentType() + ";base64," + b64);
                        ifr.setSizeFull();
                        ifr.setHeight("70vh");
                        d.add(ifr);
                    }
                });
            } catch (org.springframework.dao.QueryTimeoutException te) {
                ui.access(() -> {
                    d.removeAll();
                    d.add(new com.vaadin.flow.component.html.Span(
                            "The report query took too long and was stopped. Please narrow your filter/parameters."));
                });
            } catch (Exception ex) {
                ui.access(() -> {
                    d.removeAll();
                    d.add(new com.vaadin.flow.component.html.Span(
                            "Failed to render report: " + (ex.getMessage() != null ? ex.getMessage() : ex)));
                });
            }
        });
    }
}
