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

    // Metadata inputs
    private final TextField codeField = new TextField("Report Code");
    private final TextField titleField = new TextField("Report Title");
    private final ComboBox<FormMeta> sourceCombo = new ComboBox<>("Source Table / Form");
    private final TextArea queryArea = new TextArea("Custom SQL Query (overrides source)");
    private final Select<String> pageSelect = new Select<>();
    private final Select<String> orientSelect = new Select<>();
    private final Select<String> engineSelect = new Select<>();
    private final Grid<ReportParamMeta> paramGrid = new Grid<>(ReportParamMeta.class, false);

    private static final List<String> COMPONENT_TYPES = List.of(
            "TEXTBOX", "TEXTAREA", "INTEGERFIELD", "DECIMAL", "DATE", "DATETIME", "TIME",
            "CHECKBOX", "COMBOBOX", "LISTBOX", "CHOSENBOX", "BANDBOX");
    private static final List<String> PARAM_SOURCES = List.of("USER_INPUT", "FORM_FIELD", "SYSTEM");

    public ReportDesignerView(ReportMetaRepository reportMetaRepository,
                              FormMetaRepository formMetaRepository,
                              ReportResolver reportResolver) {
        this.reportMetaRepository = reportMetaRepository;
        this.formMetaRepository = formMetaRepository;
        this.reportResolver = reportResolver;
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
        Grid.Column<ReportMeta> colCode = grid.addColumn(ReportMeta::getReportCode).setHeader("Code");
        Grid.Column<ReportMeta> colTitle = grid.addColumn(ReportMeta::getReportTitle).setHeader("Title");
        Grid.Column<ReportMeta> colEngine = grid.addColumn(this::engineOf).setHeader("Engine");
        Grid.Column<ReportMeta> colSource = grid.addColumn(ReportMeta::getTableName).setHeader("Source");
        grid.setSelectionMode(Grid.SelectionMode.MULTI); // kolom centang seperti grid form
        grid.setSizeFull();
        grid.addItemDoubleClickListener(e -> openEditor(e.getItem())); // double-click = edit

        Map<Grid.Column<ReportMeta>, Function<ReportMeta, String>> colGetters = new LinkedHashMap<>();
        colGetters.put(colCode, r -> nz(r.getReportCode()));
        colGetters.put(colTitle, r -> nz(r.getReportTitle()));
        colGetters.put(colEngine, this::engineOf);
        colGetters.put(colSource, r -> nz(r.getTableName()));
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

        FormLayout meta = new FormLayout(codeField, titleField, sourceCombo, queryArea,
                pageSelect, orientSelect, engineSelect);
        meta.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 4));
        meta.setColspan(titleField, 3);
        meta.setColspan(sourceCombo, 2);
        meta.setColspan(queryArea, 2);

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

        TextField edLov = new TextField();
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
        designSurface.setVisible(false);
        designSurface.removeAll();
        editorForm.setVisible(true);
    }

    private void openEditor(ReportMeta report) {
        showForm();
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
            if (report.getParams() != null) {
                for (ReportParamMeta p : report.getParams()) paramState.add(cloneParam(p));
            }
        }
        paramReapply.run();
        tabs.setSelectedIndex(1);
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

        if (rep.getParams() == null) rep.setParams(new ArrayList<>());
        rep.getParams().clear(); // orphanRemoval deletes the old ones
        for (int i = 0; i < paramState.size(); i++) {
            ReportParamMeta p = paramState.get(i);
            p.setReportMeta(rep);
            p.setColOrder(i + 1);
            rep.getParams().add(p);
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
        editorForm.setVisible(false);
        designSurface.removeAll();
        designSurface.setVisible(true);

        designSurface.add(new HorizontalLayout(
            new SafeButton("Back to List", e -> { showForm(); refreshGrid(); tabs.setSelectedIndex(0); })));

        String engine = engineOf(report);
        if ("STIMULSOFT".equalsIgnoreCase(engine)) {
            IFrame ifr = new IFrame("/stimulsoft-java/designer?code=" + report.getReportCode());
            ifr.setSizeFull();
            ifr.getStyle().set("border", "none");
            designSurface.add(ifr);
            designSurface.setFlexGrow(1, ifr);
        } else if ("JASPER".equalsIgnoreCase(engine)) {
            designSurface.add(new com.vaadin.flow.component.html.Span("Jasper upload is added in the next step."));
        } else { // STANDARD — embed the band designer (Report Builder), pre-loaded
            ReportBuilderView rb = new ReportBuilderView(reportMetaRepository, formMetaRepository, this::refreshGrid);
            rb.setSizeFull();
            rb.loadReport(report.getReportCode());
            designSurface.add(rb);
            designSurface.setFlexGrow(1, rb);
        }
        tabs.setSelectedIndex(1);
    }

    private void preview(ReportMeta report) { /* Task 6 */ }
}
