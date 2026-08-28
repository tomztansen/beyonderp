package com.vaadinerp.views;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
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

    // Editor tab has two toggled containers: the definition form and the design surface.
    private final VerticalLayout editorTab = new VerticalLayout();
    private final VerticalLayout editorForm = new VerticalLayout();
    private final VerticalLayout designSurface = new VerticalLayout();

    private Runnable reapplyFilters = () -> {};

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

        tabs.add("Report List", buildListLayout());
        tabs.add("Editor", editorTab);
        tabs.setSizeFull();
        add(tabs);
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

    private VerticalLayout buildListLayout() {
        HorizontalLayout toolbar = new HorizontalLayout(
            tbBtn("New", com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE, e -> openEditor(null)),
            tbBtn("Edit", com.vaadin.flow.component.icon.VaadinIcon.EDIT, e -> withSelected(this::openEditor)),
            tbBtn("Design", com.vaadin.flow.component.icon.VaadinIcon.MAGIC, e -> withSelected(this::openDesigner)),
            tbBtn("Delete", com.vaadin.flow.component.icon.VaadinIcon.CLOSE_CIRCLE, e -> withSelected(this::deleteReport)),
            tbBtn("Preview", com.vaadin.flow.component.icon.VaadinIcon.EYE, e -> withSelected(this::preview)),
            tbBtn("Refresh", com.vaadin.flow.component.icon.VaadinIcon.REFRESH, e -> refreshGrid()),
            StandardGridUtils.createExportExcelButton(grid, "report_list")
        );
        toolbar.setPadding(true);
        VerticalLayout listLayout = new VerticalLayout(toolbar, grid);
        listLayout.setSizeFull();
        return listLayout;
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

        paramGrid.addColumn(ReportParamMeta::getParamName).setHeader("Name");
        paramGrid.addColumn(ReportParamMeta::getParamLabel).setHeader("Label");
        paramGrid.addColumn(ReportParamMeta::getParamType).setHeader("Type");
        paramGrid.addColumn(ReportParamMeta::getSource).setHeader("Source");
        paramGrid.addColumn(p -> p.isRequired() ? "Yes" : "No").setHeader("Required");
        paramGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        paramGrid.setAllRowsVisible(true);

        HorizontalLayout paramToolbar = new HorizontalLayout(
            new SafeButton("Add Parameter", e -> openParamDialog(null)),
            new SafeButton("Edit Parameter", e -> {
                ReportParamMeta sel = paramGrid.asSingleSelect().getValue();
                if (sel == null) { Notification.show("Please select a parameter."); return; }
                openParamDialog(sel);
            }),
            new SafeButton("Remove Parameter", e -> {
                ReportParamMeta sel = paramGrid.asSingleSelect().getValue();
                if (sel == null) { Notification.show("Please select a parameter."); return; }
                paramState.remove(sel);
                paramGrid.setItems(paramState);
            })
        );

        HorizontalLayout editorToolbar = new HorizontalLayout(
            tbBtn("Save", com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD, e -> saveReport()),
            tbBtn("Back to List", com.vaadin.flow.component.icon.VaadinIcon.ARROW_LEFT,
                    e -> { refreshGrid(); tabs.setSelectedIndex(0); })
        );
        editorToolbar.setWidthFull();
        editorToolbar.setPadding(true);
        editorToolbar.setSpacing(false);

        editorForm.setPadding(false);
        editorForm.add(editorToolbar, new com.vaadin.flow.component.html.H4("Report Definition"), meta,
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
        paramGrid.setItems(paramState);
        tabs.setSelectedIndex(1);
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

    private void openParamDialog(ReportParamMeta existing) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle(existing == null ? "Add Parameter" : "Edit Parameter");
        dlg.setWidth("460px");

        TextField name = new TextField("Parameter Name");
        name.setHelperText("Used as :name in the query");
        TextField label = new TextField("Label");
        Select<String> type = new Select<>();
        type.setLabel("Component Type");
        type.setItems(COMPONENT_TYPES);
        type.setValue("TEXTBOX");
        TextField lovCode = new TextField("LOV Code (for COMBOBOX/LISTBOX/CHOSENBOX/BANDBOX)");
        Select<String> source = new Select<>();
        source.setLabel("Source");
        source.setItems(PARAM_SOURCES);
        source.setValue("USER_INPUT");
        TextField sourceKey = new TextField("Source Key (form field, or $CURRENT_USER / CURRENT_DATE)");
        TextField defaultValue = new TextField("Default Value");
        Checkbox required = new Checkbox("Required");

        if (existing != null) {
            name.setValue(nz(existing.getParamName()));
            label.setValue(nz(existing.getParamLabel()));
            type.setValue(existing.getParamType() != null ? existing.getParamType() : "TEXTBOX");
            lovCode.setValue(nz(existing.getLovCode()));
            source.setValue(existing.getSource() != null ? existing.getSource() : "USER_INPUT");
            sourceKey.setValue(nz(existing.getSourceKey()));
            defaultValue.setValue(nz(existing.getDefaultValue()));
            required.setValue(existing.isRequired());
        }

        FormLayout form = new FormLayout(name, label, type, lovCode, source, sourceKey, defaultValue, required);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        dlg.add(form);

        SafeButton ok = new SafeButton(existing == null ? "Add" : "Update", e -> {
            if (name.getValue() == null || name.getValue().trim().isEmpty()) {
                Notification.show("Parameter name is required.");
                return;
            }
            ReportParamMeta p = existing != null ? existing : new ReportParamMeta();
            p.setParamName(name.getValue().trim());
            p.setParamLabel(label.getValue());
            p.setParamType(type.getValue());
            p.setLovCode(lovCode.getValue() == null || lovCode.getValue().isBlank() ? null : lovCode.getValue().trim());
            p.setSource(source.getValue());
            p.setSourceKey(sourceKey.getValue() == null || sourceKey.getValue().isBlank() ? null : sourceKey.getValue().trim());
            p.setDefaultValue(defaultValue.getValue());
            p.setRequired(required.getValue());
            if (existing == null) paramState.add(p);
            paramGrid.setItems(paramState);
            dlg.close();
        });
        SafeButton cancel = new SafeButton("Cancel", e -> dlg.close());
        dlg.getFooter().add(cancel, ok);
        dlg.open();
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
        } else {
            designSurface.add(new com.vaadin.flow.component.html.Span(
                    "Standard reports are designed with band elements (Report Builder)."));
        }
        tabs.setSelectedIndex(1);
    }

    private void preview(ReportMeta report) { /* Task 6 */ }
}
