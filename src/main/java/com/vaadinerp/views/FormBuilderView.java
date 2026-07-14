package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.FieldFilterMeta;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.meta.LovMetaRepository;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.components.BandboxField;
import com.vaadinerp.components.FileUploadField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("builder")
public class FormBuilderView extends VerticalLayout {

    private final FormMetaRepository formMetaRepository;
    private final LovMetaRepository lovMetaRepository;
    private final DynamicDataService dynamicDataService;
    private final Runnable onFormSavedListener;

    // Main UI components
    private HorizontalLayout toolbar;
    private TabSheet tabSheet;
    private Tab historisTab;
    private Tab transaksiTab;
    private final Grid<FormMeta> historyGrid = new Grid<>();

    private final TextField formCodeField = new TextField("Form Code (Unique)");
    private final TextField formTitleField = new TextField("Form Title");
    private final ComboBox<String> formTypeCombo = new ComboBox<>("Form Type");
    private final TextField tableNameField = new TextField("Target Table Name");
    private final Checkbox autoCreateDbCheckbox = new Checkbox("Auto-Generate / Sync Physical Table (DDL)", false);
    private final TextField viewTableField = new TextField("View Table / Query (Optional)");
    private final TextField pkField = new TextField("Primary Key Column");
    private final TextField labelWidthField = new TextField("Label Width (e.g. 150px)");
    private final TextField detailTableNameField = new TextField("Detail Table Name");
    private final TextField detailPkField = new TextField("Detail PK Column");
    private final TextField detailFkField = new TextField("Detail Foreign Key Column");
    private final TextField defaultSortField = new TextField("Default Sort Field");
    private final ComboBox<String> defaultSortDirection = new ComboBox<>("Default Sort Direction");
    private final MultiSelectComboBox<com.vaadinerp.meta.FormActionMeta> assignedActionsCombo = new MultiSelectComboBox<>(
            "Pilih & Pasangkan Extra Toolbar dari Katalog (Chosenbox)");

    // Selected Field State
    private FieldMetaTemp selectedField = null;
    private boolean isSelectingField = false;
    private FieldMetaTemp draggedField = null;
    private final List<FieldMetaTemp> fieldsList = new ArrayList<>();

    // Main Layout Components
    private final VerticalLayout canvas = new VerticalLayout();
    private final VerticalLayout propertiesPanel = new VerticalLayout();
    private final FormLayout propertiesForm = new FormLayout();
    private final Span propPlaceholderLabel = new Span("Pilih komponen di kanvas untuk mengkonfigurasi propertinya.");

    // Properties Panel Inputs
    private final TextField propFieldName = new TextField("Field Name (DB Column)");
    private final TextField propFieldLabel = new TextField("Field Label (UI)");
    private final ComboBox<String> propComponentType = new ComboBox<>("Component Type");
    private final BandboxField<Map<String, Object>, String> propLovCode = new BandboxField<>("LOV Code (Optional)");
    private final Button propBtnEditLov = new Button("Edit LOV Config", VaadinIcon.EDIT.create());
    private final IntegerField propRowGroup = new IntegerField("Row Group");
    private final ComboBox<Integer> propColSpan = new ComboBox<>("Colspan (Lebar Kolom)");
    private final Checkbox propIsRequired = new Checkbox("Required");
    private final Checkbox propIsReadonly = new Checkbox("Read-only");
    private final Checkbox propShowInGrid = new Checkbox("Show in Grid");
    private final Checkbox propHideInForm = new Checkbox("Hide in Form");
    private final Checkbox propIsDetail = new Checkbox("Is Detail Grid Column");
    private final Checkbox propIsSortable = new Checkbox("Sortable in Grid");
    private final TextField propFormula = new TextField("Formula (e.g. qty * price)");
    private final ComboBox<String> propValidationRule = new ComboBox<>("Validation Rule (e.g. ONLY_SUNDAY)");
    private final ComboBox<String> propSequenceCode = new ComboBox<>("⚡ Auto-Sequence Code");
    private final TextField propDisplayFormat = new TextField("Kolom Format (misal dd/MM/yyyy atau #,##0.00)");
    private final Checkbox propSaveOnInsert = new Checkbox("Save on Insert");
    private final Checkbox propSaveOnUpdate = new Checkbox("Save on Edit/Update");
    private final Checkbox propIsAuditLog = new Checkbox("Audit Log");
    private final Button propBtnFilters = new Button("Configure Filters", VaadinIcon.FILTER.create());
    private final Button propBtnLovTargets = new Button("Configure LOV Targets", VaadinIcon.LINK.create());
    private final Button propBtnCustomValidation = new Button("🛡️ Atur Validasi Dinamis", VaadinIcon.SHIELD.create());
    private final Button propBtnOnAddScript = new Button("⚡ On-Add-Row Script & AI", VaadinIcon.CODE.create());

    // Temporary Classes to hold builder state
    public static class FieldMetaTemp {
        String fieldName;
        String fieldLabel;
        String componentType;
        String lovCode;
        int rowGroup = 1;
        int colSpan = 1;
        boolean isRequired;
        boolean isReadonly;
        boolean showInGrid = true;
        boolean hideInForm;
        boolean isDetail;
        boolean isSortable = true;
        String formula;
        String validationRule;
        String displayFormat;
        String sequenceCode;
        boolean saveOnInsert = true;
        boolean saveOnUpdate = true;
        boolean isAuditLog;
        String onAddScript;
        List<FieldFilterMetaTemp> filters = new ArrayList<>();
        List<FieldLovTargetMetaTemp> lovTargets = new ArrayList<>();

        public String getOnAddScript() {
            return onAddScript;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldLabel() {
            return fieldLabel;
        }

        public String getComponentType() {
            return componentType;
        }

        public String getLovCode() {
            return lovCode;
        }

        public int getRowGroup() {
            return rowGroup;
        }

        public boolean isRequired() {
            return isRequired;
        }

        public boolean isReadonly() {
            return isReadonly;
        }

        public boolean isShowInGrid() {
            return showInGrid;
        }

        public boolean isHideInForm() {
            return hideInForm;
        }

        public boolean isDetail() {
            return isDetail;
        }

        public boolean isSortable() {
            return isSortable;
        }

        public String getFormula() {
            return formula;
        }

        public String getValidationRule() {
            return validationRule;
        }

        public String getDisplayFormat() {
            return displayFormat;
        }

        public String getSequenceCode() {
            return sequenceCode;
        }

        public boolean isSaveOnInsert() {
            return saveOnInsert;
        }

        public boolean isSaveOnUpdate() {
            return saveOnUpdate;
        }

        public boolean isAuditLog() {
            return isAuditLog;
        }
    }

    public static class FieldFilterMetaTemp {
        String filterColumn;
        String sourceType; // FIELD, QUERY, STATIC
        String sourceName;
        String logicalOperator = "AND";
        String comparisonOperator = "=";

        public String getFilterColumn() {
            return filterColumn;
        }

        public String getSourceType() {
            return sourceType;
        }

        public String getSourceName() {
            return sourceName;
        }

        public String getLogicalOperator() {
            return logicalOperator;
        }

        public String getComparisonOperator() {
            return comparisonOperator;
        }
    }

    public static class FieldLovTargetMetaTemp {
        String sourceColumn;
        String targetField;
        String actionType = "COPY";
        String lookupColumn;

        public String getSourceColumn() {
            return sourceColumn;
        }

        public String getTargetField() {
            return targetField;
        }

        public String getActionType() {
            return actionType;
        }

        public String getLookupColumn() {
            return lookupColumn;
        }
    }

    private static class FilterCriteria {
        String operator = "Contains";
        String value = "";
    }

    private java.util.Map<String, FilterCriteria> filterValues = new java.util.HashMap<>();
    private java.util.List<FormMeta> allHistoryItems = new java.util.ArrayList<>();

    public FormBuilderView(FormMetaRepository formMetaRepository, LovMetaRepository lovMetaRepository,
            DynamicDataService dynamicDataService) {
        this(formMetaRepository, lovMetaRepository, dynamicDataService, null);
    }

    public FormBuilderView(FormMetaRepository formMetaRepository, LovMetaRepository lovMetaRepository,
            DynamicDataService dynamicDataService, Runnable onFormSavedListener) {
        this.formMetaRepository = formMetaRepository;
        this.lovMetaRepository = lovMetaRepository;
        this.dynamicDataService = dynamicDataService;
        this.onFormSavedListener = onFormSavedListener;

        getElement().executeJs(
                "const id = 'form-builder-drag-styles';" +
                        "if (!document.getElementById(id)) {" +
                        "  const style = document.createElement('style');" +
                        "  style.id = id;" +
                        "  style.innerHTML = `" +
                        "    .row-drop-zone {" +
                        "      height: 4px;" +
                        "      background-color: transparent;" +
                        "      border-radius: 3px;" +
                        "      transition: all 0.15s ease;" +
                        "      margin: 2px 0;" +
                        "    }" +
                        "    .dragging-active .row-drop-zone {" +
                        "      background-color: #e2e8f0;" +
                        "      border: 1px dashed #cbd5e1;" +
                        "      height: 8px;" +
                        "    }" +
                        "    .row-drop-zone.drag-over {" +
                        "      background-color: #6366f1 !important;" +
                        "      height: 16px !important;" +
                        "      border: none !important;" +
                        "    }" +
                        "  `;" +
                        "  document.head.appendChild(style);" +
                        "}");

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("Form Builder");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "5px");

        // 1. TOOLBAR SETUP
        toolbar = new HorizontalLayout();
        buildToolbar();

        // 2. TABS SETUP
        tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        // TAB 1: HISTORIS (GRID OF FORMS)
        VerticalLayout historisLayout = new VerticalLayout();
        historisLayout.setSizeFull();
        historisLayout.setPadding(true);
        historisLayout.setSpacing(true);

        H4 historyTitle = new H4("Daftar Definisi Form Terdaftar");
        historyTitle.getStyle().set("margin", "0");

        historyGrid.setWidthFull();
        historyGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        historyGrid.setAllRowsVisible(true);
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(historyGrid);
        Grid.Column<FormMeta> codeCol = historyGrid.addColumn(FormMeta::getFormCode).setHeader("Kode Form")
                .setSortable(true).setAutoWidth(true).setKey("formCode");
        Grid.Column<FormMeta> titleCol = historyGrid.addColumn(FormMeta::getFormTitle).setHeader("Judul Form")
                .setSortable(true).setAutoWidth(true).setKey("formTitle");
        Grid.Column<FormMeta> typeCol = historyGrid.addColumn(FormMeta::getFormType).setHeader("Tipe Form")
                .setSortable(true).setAutoWidth(true).setKey("formType");
        Grid.Column<FormMeta> tableCol = historyGrid.addColumn(FormMeta::getTableName).setHeader("Nama Tabel")
                .setSortable(true).setAutoWidth(true).setKey("tableName");
        Grid.Column<FormMeta> dtlTableCol = historyGrid.addColumn(FormMeta::getDetailTableName)
                .setHeader("Tabel Detail").setSortable(true).setAutoWidth(true).setKey("detailTableName");

        com.vaadin.flow.component.grid.HeaderRow filterRow = historyGrid.appendHeaderRow();
        java.util.Map<String, Grid.Column<FormMeta>> colsMap = new java.util.HashMap<>();
        colsMap.put("formCode", codeCol);
        colsMap.put("formTitle", titleCol);
        colsMap.put("formType", typeCol);
        colsMap.put("tableName", tableCol);
        colsMap.put("detailTableName", dtlTableCol);

        colsMap.forEach((fieldName, col) -> {
            FilterCriteria criteria = new FilterCriteria();
            filterValues.put(fieldName, criteria);

            TextField filterField = new TextField();
            filterField.setPlaceholder("Filter...");
            filterField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
            filterField.setWidthFull();
            filterField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL);

            Button filterButton = new Button(VaadinIcon.FILTER.create());
            filterButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            filterButton.getStyle().set("cursor", "pointer");
            filterButton.getElement().setProperty("title", "Contains");
            filterField.setPrefixComponent(filterButton);

            com.vaadin.flow.component.contextmenu.ContextMenu contextMenu = new com.vaadin.flow.component.contextmenu.ContextMenu(
                    filterButton);
            contextMenu.setOpenOnClick(true);

            Runnable applyOperatorUI = () -> {
                String op = criteria.operator;
                filterButton.getElement().setProperty("title", op);
                boolean needsInput = !("Blank".equals(op) || "Not blank".equals(op));
                if (!needsInput) {
                    filterField.setValue("");
                    filterField.setPlaceholder(op);
                    filterField.setReadOnly(true);
                } else {
                    filterField.setPlaceholder("Filter...");
                    filterField.setReadOnly(false);
                }
            };

            com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<com.vaadin.flow.component.contextmenu.MenuItem>> listener = event -> {
                if (event.getSource().getText() != null) {
                    criteria.operator = event.getSource().getText();
                    applyOperatorUI.run();
                    applyHistoryFilters();
                }
            };

            contextMenu.addItem("Contains", listener);
            contextMenu.addItem("Not contains", listener);
            contextMenu.addItem("Equals", listener);
            contextMenu.addItem("Not equal", listener);
            contextMenu.addItem("Starts with", listener);
            contextMenu.addItem("Ends with", listener);
            contextMenu.addItem("Blank", listener);
            contextMenu.addItem("Not blank", listener);

            contextMenu.addItem(new com.vaadin.flow.component.html.Hr(), e -> {
            });
            contextMenu.addItem(col.isFrozen() ? "Unfreeze Column" : "Freeze Column", event -> {
                boolean nextFrozen = !col.isFrozen();
                col.setFrozen(nextFrozen);
                event.getSource().setText(nextFrozen ? "Unfreeze Column" : "Freeze Column");
            });

            filterField.addValueChangeListener(e -> {
                criteria.value = e.getValue();
                applyHistoryFilters();
            });

            filterRow.getCell(col).setComponent(filterField);
        });

        historyGrid.addItemDoubleClickListener(event -> {
            FormMeta selectedForm = event.getItem();
            if (selectedForm != null) {
                loadFormDefinition(selectedForm);
                tabSheet.setSelectedTab(transaksiTab);
            }
        });

        historisLayout.add(historyTitle, historyGrid);
        historisTab = tabSheet.add("Historis", historisLayout);

        // TAB 2: TRANSAKSI / DESAINER
        VerticalLayout transaksiLayout = new VerticalLayout();
        transaksiLayout.setWidthFull();
        transaksiLayout.setPadding(true);
        transaksiLayout.setSpacing(true);

        // Form Metadata Setup Panel
        FormLayout formMetaLayout = new FormLayout();
        formMetaLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3),
                new FormLayout.ResponsiveStep("900px", 6));
        formMetaLayout.setWidthFull();
        pkField.setValue("id");
        labelWidthField.setValue("150px");
        detailPkField.setValue("id");
        defaultSortDirection.setItems("ASC", "DESC");
        defaultSortDirection.setValue("ASC");

        assignedActionsCombo.setWidthFull();
        assignedActionsCombo.setItemLabelGenerator(act -> act.getActionLabel() + " (" + act.getActionCode() + ") "
                + (act.getFormMeta() != null ? "[" + act.getFormMeta().getFormCode() + "]" : "[Katalog Global]"));
        if (dynamicDataService != null && dynamicDataService.getFormActionMetaRepository() != null) {
            assignedActionsCombo.setItems(dynamicDataService.getFormActionMetaRepository().findAll());
        }

        autoCreateDbCheckbox.getStyle().set("margin-top", "10px");
        autoCreateDbCheckbox.addValueChangeListener(e -> {
            boolean checked = Boolean.TRUE.equals(e.getValue());
            tableNameField.setRequiredIndicatorVisible(checked);
            if (checked) {
                tableNameField.setPlaceholder("e.g. m_customer");
            } else {
                tableNameField.setPlaceholder("Kosongkan untuk form virtual / view");
            }
        });
        tableNameField.setRequiredIndicatorVisible(false);
        tableNameField.setPlaceholder("Kosongkan untuk form virtual / view");

        Button btnAutoGenerateFields = new Button("⚡ Auto-Generate Fields dari Tabel/View", VaadinIcon.DATABASE.create());
        btnAutoGenerateFields.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnAutoGenerateFields.addClickListener(e -> openAutoGenerateDialog());

        Button btnRelayoutCanvas = new Button("🔀 Atur Ulang Layout Berjejer (3 Kolom, 4 Kolom, dst)", VaadinIcon.GRID_BIG_O.create());
        btnRelayoutCanvas.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnRelayoutCanvas.getStyle().set("font-weight", "500").set("color", "#3b82f6");
        btnRelayoutCanvas.addClickListener(e -> openRelayoutDialog());

        HorizontalLayout actionButtonsLayout = new HorizontalLayout(btnAutoGenerateFields, btnRelayoutCanvas);
        actionButtonsLayout.setSpacing(true);
        actionButtonsLayout.getStyle().set("margin-top", "10px");

        formMetaLayout.add(formCodeField, formTitleField, formTypeCombo, tableNameField, viewTableField, pkField,
                labelWidthField,
                defaultSortField, defaultSortDirection, assignedActionsCombo, detailTableNameField, detailPkField,
                detailFkField, autoCreateDbCheckbox, actionButtonsLayout);
        formMetaLayout.setColspan(assignedActionsCombo, 2);
        formMetaLayout.setColspan(autoCreateDbCheckbox, 2);
        formMetaLayout.setColspan(actionButtonsLayout, 2);

        formTypeCombo.setItems("SINGLE", "MASTER_DETAIL");
        formTypeCombo.setValue("SINGLE");
        detailTableNameField.setVisible(false);
        detailPkField.setVisible(false);
        detailFkField.setVisible(false);

        formTypeCombo.addValueChangeListener(event -> {
            boolean isMD = "MASTER_DETAIL".equals(event.getValue());
            detailTableNameField.setVisible(isMD);
            detailPkField.setVisible(isMD);
            detailFkField.setVisible(isMD);
            rebuildCanvas();
        });

        // Workspace setup
        HorizontalLayout workspace = new HorizontalLayout();
        workspace.setSizeFull();
        workspace.getStyle().set("margin-top", "15px");

        // COLUMN A: PALETTE
        VerticalLayout palettePanel = new VerticalLayout();
        palettePanel.setWidth("180px");
        palettePanel.setHeightFull();
        palettePanel.setPadding(false);
        palettePanel.setSpacing(true);
        palettePanel.getStyle().set("border-right", "1px solid #e2e8f0").set("padding-right", "15px");

        H4 paletteTitle = new H4("Palet Komponen");
        paletteTitle.getStyle().set("margin-top", "0").set("margin-bottom", "10px");
        palettePanel.add(paletteTitle);

        palettePanel.add(
                createPaletteButton("Text Box", VaadinIcon.INPUT, "TEXTBOX"),
                createPaletteButton("Int Box", VaadinIcon.ELLIPSIS_H, "INTBOX"),
                createPaletteButton("Decimal Box", VaadinIcon.MONEY, "DECIMALBOX"),
                createPaletteButton("Date Box", VaadinIcon.CALENDAR, "DATEBOX"),
                createPaletteButton("Date Time Box", VaadinIcon.CALENDAR_CLOCK, "DATETIMEBOX"),
                createPaletteButton("Time Box", VaadinIcon.CLOCK, "TIMEBOX"),
                createPaletteButton("Check Box", VaadinIcon.CHECK_SQUARE_O, "CHECKBOX"),
                createPaletteButton("Text Area", VaadinIcon.ALIGN_LEFT, "TEXTAREA"),
                createPaletteButton("Combo Box", VaadinIcon.COMBOBOX, "COMBOBOX"),
                createPaletteButton("List Box", VaadinIcon.LIST_SELECT, "LISTBOX"),
                createPaletteButton("Band Box", VaadinIcon.SEARCH, "BANDBOX"),
                createPaletteButton("Chosen Box", VaadinIcon.TAGS, "CHOSENBOX"),
                createPaletteButton("Subform Grid", VaadinIcon.GRID, "SUBFORM_GRID"),
                createPaletteButton("File Upload", VaadinIcon.UPLOAD, "FILE_UPLOAD"),
                createPaletteButton("Image Upload", VaadinIcon.PICTURE, "IMAGE_UPLOAD"));

        // COLUMN B: CANVAS PREVIEW
        VerticalLayout canvasPanel = new VerticalLayout();
        canvasPanel.setHeightFull();
        canvasPanel.getStyle().set("flex-grow", "1")
                .set("background-color", "#f8fafc")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("overflow-y", "auto");

        H4 canvasTitle = new H4("Kanvas Desain Form (WYSIWYG)");
        canvasTitle.getStyle().set("margin-top", "0").set("margin-bottom", "15px");
        canvasPanel.add(canvasTitle);

        canvas.setWidthFull();
        canvas.setPadding(false);
        canvas.setSpacing(true);
        canvasPanel.add(canvas);

        // COLUMN C: PROPERTIES EDITOR
        propertiesPanel.setWidth("320px");
        propertiesPanel.setHeightFull();
        propertiesPanel.setPadding(true);
        propertiesPanel.getStyle().set("border-left", "1px solid #e2e8f0")
                .set("padding-left", "15px")
                .set("background-color", "#ffffff");

        H4 propertiesTitle = new H4("Editor Properti");
        propertiesTitle.getStyle().set("margin-top", "0").set("margin-bottom", "15px");
        propertiesPanel.add(propertiesTitle);

        propPlaceholderLabel.getStyle().set("color", "#64748b").set("font-size", "0.9rem").set("text-align", "center");
        propertiesPanel.add(propPlaceholderLabel);

        setupPropertiesForm();
        propertiesPanel.add(propertiesForm);

        workspace.add(palettePanel, canvasPanel, propertiesPanel);
        workspace.setFlexGrow(1, canvasPanel);

        transaksiLayout.add(formMetaLayout, workspace);
        transaksiTab = tabSheet.add("Desain Form", transaksiLayout);

        add(title, toolbar, tabSheet);
        setFlexGrow(1, tabSheet);

        // Initial rebuild
        refreshHistoryGrid();
        rebuildCanvas();
        selectField(null);
    }

    private Button createPaletteButton(String label, VaadinIcon icon, String type) {
        Button btn = new Button(label, icon.create());
        btn.setWidthFull();
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getStyle().set("justify-content", "flex-start").set("color", "#334155").set("font-weight", "500");
        btn.addClickListener(e -> addPaletteComponent(type));
        return btn;
    }

    private void addPaletteComponent(String type) {
        int count = 1;
        String defaultName = type.toLowerCase() + count;
        while (fieldExists(defaultName)) {
            count++;
            defaultName = type.toLowerCase() + count;
        }

        FieldMetaTemp temp = new FieldMetaTemp();
        temp.fieldName = defaultName;
        temp.fieldLabel = "Label " + type + " " + count;
        temp.componentType = type;
        temp.rowGroup = 1;
        temp.isRequired = false;
        temp.isReadonly = false;
        temp.showInGrid = !"SUBFORM_GRID".equalsIgnoreCase(type);
        temp.isSortable = true;

        fieldsList.add(temp);
        rebuildCanvas();
        selectField(temp);
        Notification.show(type + " ditambahkan ke kanvas!", 2000, Notification.Position.BOTTOM_END);
    }

    private boolean fieldExists(String name) {
        return fieldsList.stream().anyMatch(f -> f.fieldName.equalsIgnoreCase(name));
    }

    private List<Map<String, Object>> fetchLovItems(String keyword, boolean isSubform) {
        List<Map<String, Object>> items = new ArrayList<>();
        String lowerKeyword = keyword != null ? keyword.toLowerCase() : "";

        if (isSubform) {
            formMetaRepository.findAll().forEach(form -> {
                String code = form.getFormCode();
                String title = form.getFormTitle();
                if (code.toLowerCase().contains(lowerKeyword)
                        || (title != null && title.toLowerCase().contains(lowerKeyword))) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", code);
                    map.put("name", title != null ? title : "");
                    map.put("type", "Form Definition");
                    items.add(map);
                }
            });
        } else {
            lovMetaRepository.findAll().forEach(lov -> {
                String code = lov.getLovCode();
                String name = lov.getLovName();
                if (code.toLowerCase().contains(lowerKeyword)
                        || (name != null && name.toLowerCase().contains(lowerKeyword))) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", code);
                    map.put("name", name != null ? name : "");
                    map.put("type", "Standard LOV");
                    items.add(map);
                }
            });
            formMetaRepository.findAll().forEach(form -> {
                String code = form.getFormCode();
                String title = form.getFormTitle();
                if (code.toLowerCase().contains(lowerKeyword)
                        || (title != null && title.toLowerCase().contains(lowerKeyword))) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", code);
                    map.put("name", title != null ? title : "");
                    map.put("type", "Form Definition");
                    items.add(map);
                }
            });
        }
        return items;
    }

    private void setupPropertiesForm() {
        propertiesForm.setVisible(false);
        propertiesForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        propComponentType.setItems("TEXTBOX", "INTBOX", "DECIMALBOX", "DATEBOX", "DATETIMEBOX", "TIMEBOX", "CHECKBOX",
                "TEXTAREA", "COMBOBOX", "LISTBOX", "BANDBOX", "CHOSENBOX", "SUBFORM_GRID", "FILE_UPLOAD",
                "IMAGE_UPLOAD");

        // Configure propLovCode BandboxField
        propLovCode.getGrid().addColumn(row -> row.get("code") != null ? row.get("code").toString() : "")
                .setHeader("Kode").setWidth("150px");
        propLovCode.getGrid().addColumn(row -> row.get("name") != null ? row.get("name").toString() : "")
                .setHeader("Nama").setWidth("250px");
        propLovCode.getGrid().addColumn(row -> row.get("type") != null ? row.get("type").toString() : "")
                .setHeader("Tipe").setWidth("150px");

        propLovCode.setItemLabelGenerator(row -> row.get("code") != null ? row.get("code").toString() : "");
        propLovCode.setItemValueGenerator(row -> row.get("code") != null ? row.get("code").toString() : "");

        propLovCode.setDataFetchCallback(keyword -> {
            boolean isSubform = selectedField != null && "SUBFORM_GRID".equalsIgnoreCase(selectedField.componentType);
            return fetchLovItems(keyword, isSubform);
        });

        propLovCode.setItemFinder(val -> {
            boolean isSubform = selectedField != null && "SUBFORM_GRID".equalsIgnoreCase(selectedField.componentType);
            List<Map<String, Object>> items = fetchLovItems(val != null ? val : "", isSubform);
            return items.stream()
                    .filter(item -> val != null && val.equalsIgnoreCase(item.get("code").toString()))
                    .findFirst()
                    .orElse(null);
        });

        propLovCode.setPlaceholder("Pilih LOV jika ada...");

        propBtnEditLov.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        propBtnEditLov.setWidthFull();
        propBtnEditLov.addClickListener(e -> {
            if (selectedField != null && selectedField.lovCode != null && !selectedField.lovCode.trim().isEmpty()) {
                openLovConfigDialog(selectedField.lovCode);
            } else {
                openLovConfigDialog(null);
            }
        });

        propRowGroup.setMin(1);
        propColSpan.setItems(1, 2, 3, 4, 6, 12);
        propColSpan.setClearButtonVisible(true);

        FormLayout checkBoxLayout = new FormLayout();
        checkBoxLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        propIsSortable.setValue(true);
        checkBoxLayout.add(propIsRequired, propIsReadonly, propShowInGrid, propHideInForm, propIsDetail, propIsSortable,
                propSaveOnInsert, propSaveOnUpdate, propIsAuditLog);

        propBtnFilters.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        propBtnFilters.setWidthFull();
        propBtnFilters.addClickListener(e -> {
            if (selectedField != null) {
                openFilterConfigDialog(selectedField);
            }
        });

        propBtnLovTargets.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        propBtnLovTargets.setWidthFull();
        propBtnLovTargets.addClickListener(e -> {
            if (selectedField != null) {
                openLovTargetsConfigDialog(selectedField);
            }
        });

        propValidationRule.setAllowCustomValue(true);
        propValidationRule.addCustomValueSetListener(e -> propValidationRule.setValue(e.getDetail()));
        propValidationRule.setPlaceholder("Pilih / ketik rule...");

        propSequenceCode.setAllowCustomValue(true);
        propSequenceCode.addCustomValueSetListener(e -> propSequenceCode.setValue(e.getDetail()));
        propSequenceCode.setPlaceholder("Pilih / ketik kode sequence...");
        propSequenceCode.setClearButtonVisible(true);
        propSequenceCode.setItems(dynamicDataService.getActiveSequenceCodes());

        propBtnCustomValidation.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        propBtnCustomValidation.setWidthFull();
        propBtnCustomValidation.addClickListener(e -> {
            if (selectedField != null) {
                openDynamicValidationDialog(selectedField);
            }
        });

        propBtnOnAddScript.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        propBtnOnAddScript.setWidthFull();
        propBtnOnAddScript.addClickListener(e -> {
            if (selectedField != null) {
                openOnAddScriptDialog(selectedField);
            }
        });

        propertiesForm.add(propFieldName, propFieldLabel, propComponentType, propLovCode, propBtnEditLov, propBtnFilters, propBtnLovTargets, propRowGroup, propColSpan,
                propFormula, propDisplayFormat, propValidationRule, propSequenceCode, propBtnCustomValidation, checkBoxLayout,
                propBtnOnAddScript);

        // Listeners for live sync
        setupPropertiesListeners();
    }

    private void setupPropertiesListeners() {
        propFieldName.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.fieldName = e.getValue();
                rebuildCanvas();
            }
        });
        propFieldLabel.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.fieldLabel = e.getValue();
                rebuildCanvas();
            }
        });
        propComponentType.addValueChangeListener(e -> {
            if (selectedField != null && (!isSelectingField || e.isFromClient())) {
                selectedField.componentType = e.getValue();
                updatePropertyFieldsState(e.getValue());
                rebuildCanvas();
            }
        });
        propLovCode.addValueChangeListener(e -> {
            if (selectedField != null && (!isSelectingField || e.isFromClient())) {
                selectedField.lovCode = e.getValue();
                updatePropertyFieldsState(selectedField.componentType);
                rebuildCanvas();
            }
        });
        propRowGroup.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.rowGroup = e.getValue() != null ? e.getValue() : 1;
                rebuildCanvas();
            }
        });
        propColSpan.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.colSpan = e.getValue() != null && e.getValue() > 0 ? e.getValue() : 1;
                rebuildCanvas();
            }
        });
        propIsRequired.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.isRequired = e.getValue();
                rebuildCanvas();
            }
        });
        propIsReadonly.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.isReadonly = e.getValue();
                rebuildCanvas();
            }
        });
        propShowInGrid.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.showInGrid = e.getValue();
                rebuildCanvas();
            }
        });
        propHideInForm.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.hideInForm = e.getValue();
                rebuildCanvas();
            }
        });
        propIsDetail.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.isDetail = e.getValue();
                rebuildCanvas();
            }
        });
        propIsSortable.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.isSortable = e.getValue();
            }
        });
        propFormula.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.formula = e.getValue();
            }
        });
        propDisplayFormat.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.displayFormat = e.getValue();
            }
        });
        propValidationRule.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.validationRule = e.getValue();
            }
        });
        propSequenceCode.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.sequenceCode = e.getValue();
            }
        });
        propSaveOnInsert.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.saveOnInsert = e.getValue();
            }
        });
        propSaveOnUpdate.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.saveOnUpdate = e.getValue();
            }
        });
        propIsAuditLog.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.isAuditLog = e.getValue();
            }
        });
    }

    private void updatePropertyFieldsState(String componentType) {
        boolean isSubform = "SUBFORM_GRID".equalsIgnoreCase(componentType);
        boolean isSelection = "COMBOBOX".equalsIgnoreCase(componentType) ||
                "LISTBOX".equalsIgnoreCase(componentType) ||
                "BANDBOX".equalsIgnoreCase(componentType) ||
                "CHOSENBOX".equalsIgnoreCase(componentType);

        if (isSubform) {
            propLovCode.setLabel("Target Child Form");
            propLovCode.setPlaceholder("Pilih Child Form...");
            propLovCode.setEnabled(true);
            propBtnEditLov.setEnabled(false);
            propBtnEditLov.setVisible(false);

            propFormula.setLabel("Child FK Field");
            propFormula.setPlaceholder("e.g. order_id");
            propFormula.setEnabled(true);

            propBtnFilters.setEnabled(false);
            propBtnLovTargets.setEnabled(false);
            propBtnOnAddScript.setEnabled(true);
            propBtnOnAddScript.setVisible(true);

            if (selectedField != null) {
                selectedField.showInGrid = false;
            }
            propShowInGrid.setValue(false);
            propShowInGrid.setEnabled(false);
            propIsDetail.setEnabled(false);
            propIsSortable.setEnabled(false);
            propIsRequired.setEnabled(false);
            propIsReadonly.setEnabled(false);
        } else {
            propLovCode.setLabel("LOV Code (Optional)");
            propLovCode.setPlaceholder("Pilih LOV jika ada...");
            propLovCode.setEnabled(isSelection);
            propBtnEditLov.setEnabled(isSelection);
            propBtnEditLov.setVisible(isSelection);

            propFormula.setLabel("Formula (e.g. qty * price)");
            propFormula.setPlaceholder("");
            propFormula.setEnabled(true);

            propBtnFilters.setEnabled(isSelection && selectedField != null && selectedField.lovCode != null
                    && !selectedField.lovCode.trim().isEmpty());
            propBtnLovTargets.setEnabled(true);
            propBtnOnAddScript.setEnabled(false);
            propBtnOnAddScript.setVisible(false);

            propShowInGrid.setEnabled(true);
            propIsDetail.setEnabled(true);
            propIsSortable.setEnabled(true);
            propIsRequired.setEnabled(true);
            propIsReadonly.setEnabled(true);
        }

        if (componentType != null) {
            switch (componentType.toUpperCase()) {
                case "TEXTBOX":
                case "TEXTAREA":
                    propValidationRule.setItems("NONE", "EMAIL", "ALPHANUMERIC", "MIN_LEN:3", "MIN_LEN:5",
                            "MAX_LEN:20");
                    break;
                case "INTBOX":
                case "DECIMALBOX":
                    propValidationRule.setItems("NONE", "POSITIVE_NUM", "NON_NEGATIVE", "MIN:1", "MIN:100", "MAX:1000");
                    break;
                case "DATEBOX":
                case "DATETIMEBOX":
                    propValidationRule.setItems("NONE", "ONLY_SUNDAY", "NOT_SUNDAY", "WEEKDAYS", "WEEKEND", "PAST_DATE",
                            "FUTURE_DATE");
                    break;
                case "COMBOBOX":
                case "LISTBOX":
                case "BANDBOX":
                case "CHOSENBOX":
                    propValidationRule.setItems("NONE", "NOT_BLANK", "DISALLOW:CANCELLED");
                    break;
                default:
                    propValidationRule.setItems("NONE");
                    break;
            }
        }
    }

    private void selectField(FieldMetaTemp temp) {
        selectedField = temp;
        if (temp == null) {
            propPlaceholderLabel.setVisible(true);
            propertiesForm.setVisible(false);
        } else {
            propPlaceholderLabel.setVisible(false);
            propertiesForm.setVisible(true);

            isSelectingField = true;
            try {
                updatePropertyFieldsState(temp.componentType);

                propFieldName.setValue(temp.fieldName != null ? temp.fieldName : "");
                propFieldLabel.setValue(temp.fieldLabel != null ? temp.fieldLabel : "");
                propComponentType.setValue(temp.componentType);
                propLovCode.setValue(temp.lovCode);
                propRowGroup.setValue(temp.rowGroup);
                propColSpan.setValue(temp.colSpan > 0 ? temp.colSpan : 1);
                propIsRequired.setValue(temp.isRequired);
                propIsReadonly.setValue(temp.isReadonly);
                if ("SUBFORM_GRID".equalsIgnoreCase(temp.componentType)) {
                    temp.showInGrid = false;
                }
                propShowInGrid.setValue(temp.showInGrid);
                propHideInForm.setValue(temp.hideInForm);
                propIsDetail.setValue(temp.isDetail);
                propIsDetail.setVisible("MASTER_DETAIL".equals(formTypeCombo.getValue()));
                propIsSortable.setValue(temp.isSortable);
                propFormula.setValue(temp.formula != null ? temp.formula : "");
                propDisplayFormat.setValue(temp.displayFormat != null ? temp.displayFormat : "");
                propValidationRule.setValue(temp.validationRule != null ? temp.validationRule : "NONE");
                propSequenceCode.setValue(temp.sequenceCode != null ? temp.sequenceCode : "");
                propSaveOnInsert.setValue(temp.saveOnInsert);
                propSaveOnUpdate.setValue(temp.saveOnUpdate);
                propIsAuditLog.setValue(temp.isAuditLog);
            } finally {
                isSelectingField = false;
            }
        }
        rebuildCanvas();
    }

    private void rebuildCanvas() {
        canvas.removeAll();

        boolean isMD = "MASTER_DETAIL".equals(formTypeCombo.getValue());

        if (isMD) {
            // Master / Header Fields Section
            List<FieldMetaTemp> masterFields = fieldsList.stream()
                    .filter(f -> !f.isDetail)
                    .collect(Collectors.toList());

            H4 headerTitle = new H4("Master / Header Fields");
            headerTitle.getStyle().set("margin-top", "10px").set("color", "#4f46e5").set("font-weight", "600");
            canvas.add(headerTitle);

            if (masterFields.isEmpty()) {
                Span emptyMaster = new Span("[Seret/tambah field untuk Header Master di sini]");
                emptyMaster.getStyle().set("color", "#94a3b8").set("font-style", "italic").set("font-size", "0.9rem");
                canvas.add(emptyMaster);
            } else {
                renderGroupedFields(masterFields);
            }

            // Detail / Grid Fields Section
            List<FieldMetaTemp> detailFields = fieldsList.stream()
                    .filter(f -> f.isDetail)
                    .collect(Collectors.toList());

            H4 detailTitle = new H4("Detail / Grid Columns");
            detailTitle.getStyle().set("margin-top", "25px").set("color", "#0891b2").set("font-weight", "600");
            canvas.add(detailTitle);

            if (detailFields.isEmpty()) {
                Span emptyDetail = new Span(
                        "[Tandai 'Is Detail Grid Column' pada properti field untuk memindahkan ke kolom Grid detail]");
                emptyDetail.getStyle().set("color", "#94a3b8").set("font-style", "italic").set("font-size", "0.9rem");
                canvas.add(emptyDetail);
            } else {
                FormLayout detailRowLayout = new FormLayout();
                detailRowLayout.setWidthFull();
                boolean allDefault = detailFields.stream().allMatch(f -> f.colSpan <= 1);
                int cols = (allDefault && detailFields.size() > 0 && detailFields.size() <= 6) ? detailFields.size() : 12;
                detailRowLayout.setResponsiveSteps(
                        new FormLayout.ResponsiveStep("0", 1),
                        new FormLayout.ResponsiveStep("500px", Math.max(1, cols / 2)),
                        new FormLayout.ResponsiveStep("800px", cols));

                int seq = 1;
                for (FieldMetaTemp temp : detailFields) {
                    Component card = buildFieldCard(temp, seq++);
                    detailRowLayout.add(card);
                    int span = temp.colSpan > 0 ? temp.colSpan : 1;
                    if ("TEXTAREA".equalsIgnoreCase(temp.componentType) && temp.colSpan <= 1) {
                        span = cols;
                    }
                    if (span > 1) {
                        detailRowLayout.setColspan(card, Math.min(span, cols));
                    }
                }
                canvas.add(detailRowLayout);
            }
        } else {
            // Standard SINGLE Form
            renderGroupedFields(fieldsList);
        }
    }

    private void normalizeRowGroups() {
        List<FieldMetaTemp> nonDetailFields = fieldsList.stream()
                .filter(f -> !f.isDetail)
                .collect(Collectors.toList());

        List<Integer> uniqueGroups = nonDetailFields.stream()
                .map(f -> f.rowGroup)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        Map<Integer, Integer> mapping = new HashMap<>();
        for (int i = 0; i < uniqueGroups.size(); i++) {
            mapping.put(uniqueGroups.get(i), i + 1);
        }

        for (FieldMetaTemp f : fieldsList) {
            if (!f.isDetail) {
                f.rowGroup = mapping.getOrDefault(f.rowGroup, 1);
            }
        }
    }

    private Component createRowDropZone(int targetRowGroup) {
        Div zone = new Div();
        zone.addClassName("row-drop-zone");
        zone.setWidthFull();

        DropTarget<Div> dropTarget = DropTarget.create(zone);
        dropTarget.setActive(true);

        zone.getElement().addEventListener("dragenter", e -> {
            zone.addClassName("drag-over");
        });

        zone.getElement().addEventListener("dragleave", e -> {
            zone.removeClassName("drag-over");
        });

        dropTarget.addDropListener(e -> {
            if (draggedField != null) {
                if (draggedField.isDetail) {
                    draggedField = null;
                    canvas.removeClassName("dragging-active");
                    rebuildCanvas();
                    return;
                }

                // Shift existing row groups
                for (FieldMetaTemp f : fieldsList) {
                    if (!f.isDetail && f.rowGroup >= targetRowGroup) {
                        f.rowGroup = f.rowGroup + 1;
                    }
                }

                draggedField.rowGroup = targetRowGroup;

                // Reorder in list
                fieldsList.remove(draggedField);

                int insertIdx = 0;
                for (int i = 0; i < fieldsList.size(); i++) {
                    FieldMetaTemp f = fieldsList.get(i);
                    if (f.isDetail)
                        continue;
                    if (f.rowGroup < targetRowGroup) {
                        insertIdx = i + 1;
                    }
                }
                fieldsList.add(insertIdx, draggedField);

                normalizeRowGroups();
                if (selectedField != null) {
                    propRowGroup.setValue(selectedField.rowGroup);
                }
                canvas.removeClassName("dragging-active");
                rebuildCanvas();
                Notification.show("Moved " + draggedField.fieldName + " to new row", 2000,
                        Notification.Position.BOTTOM_END);
            }
            draggedField = null;
        });

        return zone;
    }

    private void renderGroupedFields(List<FieldMetaTemp> targetFields) {
        normalizeRowGroups();

        Map<Integer, List<FieldMetaTemp>> groups = new HashMap<>();
        List<Integer> rowGroupsOrder = new ArrayList<>();
        for (FieldMetaTemp f : targetFields) {
            groups.computeIfAbsent(f.rowGroup, k -> new ArrayList<>()).add(f);
            if (!rowGroupsOrder.contains(f.rowGroup)) {
                rowGroupsOrder.add(f.rowGroup);
            }
        }
        rowGroupsOrder.sort(Integer::compareTo);

        int numRows = rowGroupsOrder.size();

        // Drop zone before the first row
        canvas.add(createRowDropZone(1));

        int sequence = 1;
        for (int i = 0; i < numRows; i++) {
            Integer rg = rowGroupsOrder.get(i);
            List<FieldMetaTemp> groupFields = groups.get(rg);
            FormLayout rowLayout = new FormLayout();
            rowLayout.setWidthFull();
            boolean allDefault = groupFields.stream().allMatch(f -> f.colSpan <= 1);
            int cols = (allDefault && groupFields.size() > 0 && groupFields.size() <= 6) ? groupFields.size() : 12;
            rowLayout.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("500px", Math.max(1, cols / 2)),
                    new FormLayout.ResponsiveStep("800px", cols));

            for (FieldMetaTemp temp : groupFields) {
                Component card = buildFieldCard(temp, sequence++);
                rowLayout.add(card);
                int span = temp.colSpan > 0 ? temp.colSpan : 1;
                if ("TEXTAREA".equalsIgnoreCase(temp.componentType) && temp.colSpan <= 1) {
                    span = cols;
                }
                if (span > 1) {
                    rowLayout.setColspan(card, Math.min(span, cols));
                }
            }
            canvas.add(rowLayout);

            // Drop zone after this row
            canvas.add(createRowDropZone(rg + 1));
        }
    }

    private Component buildFieldCard(FieldMetaTemp temp, int sequence) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);

        boolean isSelected = (selectedField == temp);
        boolean isMultiLine = "TEXTAREA".equalsIgnoreCase(temp.componentType) || "SUBFORM_GRID".equalsIgnoreCase(temp.componentType);

        card.getStyle()
                .set("border", isSelected ? "2px solid #6366f1" : "1px dashed #cbd5e1")
                .set("border-radius", "8px")
                .set("background-color", isSelected ? "#f8fafc" : "#ffffff")
                .set("cursor", "grab")
                .set("position", "relative")
                .set("box-shadow", isSelected ? "0 4px 12px rgba(99, 102, 241, 0.1)" : "none")
                .set("opacity", temp.hideInForm ? "0.65" : "1.0")
                .set("min-width", "110px")
                .set("height", isMultiLine ? "100%" : "118px")
                .set("min-height", "118px")
                .set("max-height", isMultiLine ? "none" : "118px")
                .set("box-sizing", "border-box")
                .set("justify-content", "space-between")
                .set("transition", "all 0.2s ease");

        HorizontalLayout cardHeader = new HorizontalLayout();
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        cardHeader.setSpacing(false);
        cardHeader.getStyle()
                .set("margin-bottom", "6px")
                .set("gap", "4px")
                .set("flex-wrap", "nowrap")
                .set("height", "26px")
                .set("min-height", "26px")
                .set("max-height", "26px");

        HorizontalLayout badgesBox = new HorizontalLayout();
        badgesBox.setAlignItems(FlexComponent.Alignment.CENTER);
        badgesBox.setSpacing(false);
        badgesBox.getStyle()
                .set("gap", "4px")
                .set("flex", "1 1 auto")
                .set("overflow", "hidden")
                .set("flex-wrap", "nowrap");

        Span orderBadge = new Span("#" + sequence);
        orderBadge.getStyle()
                .set("font-size", "0.65rem")
                .set("font-weight", "700")
                .set("background-color", "#f1f5f9")
                .set("color", "#475569")
                .set("padding", "2px 6px")
                .set("border-radius", "4px")
                .set("flex-shrink", "0");

        Span typeBadge = new Span(temp.componentType);
        typeBadge.getStyle()
                .set("font-size", "0.6rem")
                .set("font-weight", "700")
                .set("background-color", isSelected ? "#e0e7ff" : "#f1f5f9")
                .set("color", isSelected ? "#4f46e5" : "#475569")
                .set("padding", "2px 4px")
                .set("border-radius", "4px")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

        if (temp.hideInForm) {
            Span hiddenBadge = new Span("Hidden");
            hiddenBadge.getStyle()
                    .set("font-size", "0.65rem")
                    .set("font-weight", "700")
                    .set("background-color", "#fee2e2")
                    .set("color", "#ef4444")
                    .set("padding", "2px 6px")
                    .set("border-radius", "4px")
                    .set("flex-shrink", "0");
            badgesBox.add(hiddenBadge);
        }
        badgesBox.add(orderBadge, typeBadge);

        Button btnEdit = new Button(VaadinIcon.COG.create());
        btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnEdit.getStyle()
                .set("cursor", "pointer")
                .set("padding", "0")
                .set("min-width", "26px")
                .set("width", "26px")
                .set("height", "26px");
        btnEdit.addClickListener(e -> selectField(temp));

        Button btnDel = new Button(VaadinIcon.TRASH.create());
        btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnDel.getStyle()
                .set("cursor", "pointer")
                .set("padding", "0")
                .set("min-width", "26px")
                .set("width", "26px")
                .set("height", "26px");
        btnDel.addClickListener(e -> {
            fieldsList.remove(temp);
            if (selectedField == temp) {
                selectField(null);
            }
            rebuildCanvas();
        });

        HorizontalLayout actions = new HorizontalLayout(btnEdit, btnDel);
        actions.setSpacing(false);
        actions.getStyle()
                .set("gap", "2px")
                .set("flex-shrink", "0");
        cardHeader.add(badgesBox, actions);

        Component previewComp = createPreviewComponent(temp);
        if (previewComp != null) {
            previewComp.getElement().getStyle().set("margin-top", "auto");
        }
        card.add(cardHeader, previewComp);

        card.getElement().addEventListener("click", e -> selectField(temp));

        // Enable Drag & Drop
        DragSource<Component> dragSource = DragSource.create(card);
        dragSource.setDraggable(true);
        dragSource.addDragStartListener(e -> {
            draggedField = temp;
            card.getStyle().set("opacity", "0.5");
            canvas.addClassName("dragging-active");
        });
        dragSource.addDragEndListener(e -> {
            draggedField = null;
            canvas.removeClassName("dragging-active");
            rebuildCanvas();
        });

        DropTarget<Component> dropTarget = DropTarget.create(card);
        dropTarget.setActive(true);

        card.getElement().addEventListener("dragenter", e -> {
            card.getStyle().set("border", "2px dashed #6366f1");
            card.getStyle().set("background-color", "#e0e7ff");
        });

        card.getElement().addEventListener("dragleave", e -> {
            card.getStyle()
                    .set("border", isSelected ? "2px solid #6366f1" : "1px dashed #cbd5e1")
                    .set("background-color", isSelected ? "#f8fafc" : "#ffffff");
        });

        dropTarget.addDropListener(e -> {
            if (draggedField != null && draggedField != temp) {
                int srcIdx = fieldsList.indexOf(draggedField);
                int destIdx = fieldsList.indexOf(temp);
                if (srcIdx != -1 && destIdx != -1) {
                    fieldsList.remove(draggedField);
                    fieldsList.add(destIdx, draggedField);
                    draggedField.rowGroup = temp.rowGroup;
                    if (selectedField != null) {
                        propRowGroup.setValue(selectedField.rowGroup);
                    }
                    rebuildCanvas();
                    Notification.show(
                            "Field reordered: " + draggedField.fieldName + " moved to row group " + temp.rowGroup, 2000,
                            Notification.Position.BOTTOM_END);
                }
            }
            draggedField = null;
        });

        return card;
    }

    private Component createPreviewComponent(FieldMetaTemp temp) {
        String label = temp.fieldLabel != null && !temp.fieldLabel.isEmpty() ? temp.fieldLabel : temp.fieldName;
        switch (temp.componentType.toUpperCase()) {
            case "TEXTBOX":
                TextField tf = new TextField(label);
                tf.setWidthFull();
                tf.setReadOnly(true);
                return tf;
            case "INTBOX":
                IntegerField iff = new IntegerField(label);
                iff.setWidthFull();
                iff.setPlaceholder("");
                iff.setReadOnly(true);
                return iff;
            case "DECIMALBOX":
                BigDecimalField bdf = new BigDecimalField(label);
                bdf.setWidthFull();
                bdf.setPlaceholder("");
                bdf.setReadOnly(true);
                return bdf;
            case "DATEBOX":
                DatePicker dp = new DatePicker(label);
                dp.setWidthFull();
                dp.setReadOnly(true);
                return dp;
            case "DATETIMEBOX":
                DateTimePicker dtp = new DateTimePicker(label);
                dtp.setWidthFull();
                dtp.setReadOnly(true);
                return dtp;
            case "TIMEBOX":
                TimePicker tp = new TimePicker(label);
                tp.setWidthFull();
                tp.setReadOnly(true);
                return tp;
            case "CHECKBOX":
                Checkbox cb = new Checkbox(label);
                cb.setReadOnly(true);
                return cb;
            case "TEXTAREA":
                TextArea ta = new TextArea(label);
                ta.setWidthFull();
                ta.setReadOnly(true);
                return ta;
            case "COMBOBOX":
                ComboBox<String> cob = new ComboBox<>(label);
                cob.setWidthFull();
                cob.setReadOnly(true);
                if (temp.lovCode != null) {
                    cob.setPlaceholder("LOV: " + temp.lovCode);
                } else {
                    cob.setPlaceholder("Pilih...");
                }
                return cob;
            case "LISTBOX":
                Select<String> sel = new Select<>();
                sel.setLabel(label);
                sel.setWidthFull();
                sel.setEnabled(false);
                if (temp.lovCode != null) {
                    sel.setPlaceholder("LOV: " + temp.lovCode);
                } else {
                    sel.setPlaceholder("Pilih...");
                }
                return sel;
            case "BANDBOX":
                TextField bf = new TextField(label);
                bf.setWidthFull();
                bf.setReadOnly(true);
                if (temp.lovCode != null) {
                    bf.setPlaceholder("LOV Search: " + temp.lovCode);
                } else {
                    bf.setPlaceholder("Cari data...");
                }
                bf.setSuffixComponent(VaadinIcon.SEARCH.create());
                return bf;
            case "CHOSENBOX":
                MultiSelectComboBox<String> msc = new MultiSelectComboBox<>(label);
                msc.setWidthFull();
                msc.setReadOnly(true);
                return msc;
            case "SUBFORM_GRID":
                VerticalLayout subformContainer = new VerticalLayout();
                subformContainer.setWidthFull();
                subformContainer.setPadding(false);
                subformContainer.setSpacing(false);
                Span sTitle = new Span(
                        label + " (Subform Grid: " + (temp.lovCode != null ? temp.lovCode : "None") + ")");
                sTitle.getStyle().set("font-weight", "600").set("color", "#4f46e5");
                Grid<String> mockGrid = new Grid<>();
                mockGrid.setWidthFull();
                mockGrid.setAllRowsVisible(true);
                mockGrid.addColumn(s -> s).setHeader("Contoh Kolom Detail...");
                mockGrid.setItems(java.util.Collections.singletonList("Data detail akan dimuat di sini..."));
                subformContainer.add(sTitle, mockGrid);
                return subformContainer;
            case "FILE_UPLOAD":
                return new FileUploadField(label,
                        dynamicDataService != null ? dynamicDataService.getFileStorageService() : null, false);
            case "IMAGE_UPLOAD":
                return new FileUploadField(label,
                        dynamicDataService != null ? dynamicDataService.getFileStorageService() : null, true);
            default:
                return new TextField(label);
        }
    }

    private void openDynamicValidationDialog(FieldMetaTemp field) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🛡️ Atur Validasi Dinamis - " + field.fieldName);
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        ComboBox<String> typeCombo = new ComboBox<>("Tipe Validasi");
        typeCombo.setItems("REGEX (Format Khusus / Pola Teks)", "MIN (Nilai Angka Minimal)",
                "MAX (Nilai Angka Maksimal)", "MIN_LEN (Panjang Karakter Minimal)",
                "MAX_LEN (Panjang Karakter Maksimal)", "EMAIL (Format Email)",
                "NOT_BLANK (Wajib Isi / Tidak Boleh Kosong)");
        typeCombo.setWidthFull();

        TextField ruleField = new TextField("Rumus / Nilai / Pola Regex");
        ruleField.setPlaceholder("Contoh: ^[0-9]{15}$ atau 1500000");
        ruleField.setWidthFull();

        TextField errorMsgField = new TextField("Pesan Error Kustom (Bahasa Manusia)");
        errorMsgField.setPlaceholder("Contoh: Format NPWP wajib 15 digit angka!");
        errorMsgField.setWidthFull();

        String existing = propValidationRule.getValue();
        if (existing != null && !existing.isEmpty() && !existing.equalsIgnoreCase("NONE")) {
            String rulePart = existing;
            String msgPart = "";
            int pipeIdx = existing.indexOf('|');
            if (pipeIdx > 0) {
                rulePart = existing.substring(0, pipeIdx).trim();
                msgPart = existing.substring(pipeIdx + 1).trim();
            }
            errorMsgField.setValue(msgPart);
            if (rulePart.startsWith("REGEX:")) {
                typeCombo.setValue("REGEX (Format Khusus / Pola Teks)");
                ruleField.setValue(rulePart.substring(6).trim());
            } else if (rulePart.startsWith("MIN:")) {
                typeCombo.setValue("MIN (Nilai Angka Minimal)");
                ruleField.setValue(rulePart.substring(4).trim());
            } else if (rulePart.startsWith("MAX:")) {
                typeCombo.setValue("MAX (Nilai Angka Maksimal)");
                ruleField.setValue(rulePart.substring(4).trim());
            } else if (rulePart.startsWith("MIN_LEN:")) {
                typeCombo.setValue("MIN_LEN (Panjang Karakter Minimal)");
                ruleField.setValue(rulePart.substring(8).trim());
            } else if (rulePart.startsWith("MAX_LEN:")) {
                typeCombo.setValue("MAX_LEN (Panjang Karakter Maksimal)");
                ruleField.setValue(rulePart.substring(8).trim());
            } else if (rulePart.equalsIgnoreCase("EMAIL")) {
                typeCombo.setValue("EMAIL (Format Email)");
            } else if (rulePart.equalsIgnoreCase("NOT_BLANK")) {
                typeCombo.setValue("NOT_BLANK (Wajib Isi / Tidak Boleh Kosong)");
            }
        }

        typeCombo.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val != null) {
                if (val.startsWith("EMAIL") || val.startsWith("NOT_BLANK")) {
                    ruleField.setVisible(false);
                } else {
                    ruleField.setVisible(true);
                    if (val.startsWith("REGEX"))
                        ruleField.setLabel("Pola Regex (e.g. ^[0-9]{15}$)");
                    else if (val.startsWith("MIN_LEN") || val.startsWith("MAX_LEN"))
                        ruleField.setLabel("Jumlah Karakter (e.g. 5)");
                    else
                        ruleField.setLabel("Nilai Angka (e.g. 1500000)");
                }
            }
        });
        if (typeCombo.getValue() != null
                && (typeCombo.getValue().startsWith("EMAIL") || typeCombo.getValue().startsWith("NOT_BLANK"))) {
            ruleField.setVisible(false);
        }

        layout.add(typeCombo, ruleField, errorMsgField);

        Button btnSave = new Button("Terapkan Validasi", VaadinIcon.CHECK.create(), e -> {
            String selectedType = typeCombo.getValue();
            if (selectedType == null) {
                Notification.show("Pilih tipe validasi terlebih dahulu!", 3000, Notification.Position.MIDDLE);
                return;
            }
            String prefix = "";
            if (selectedType.startsWith("REGEX"))
                prefix = "REGEX:";
            else if (selectedType.startsWith("MIN_LEN"))
                prefix = "MIN_LEN:";
            else if (selectedType.startsWith("MAX_LEN"))
                prefix = "MAX_LEN:";
            else if (selectedType.startsWith("MIN"))
                prefix = "MIN:";
            else if (selectedType.startsWith("MAX"))
                prefix = "MAX:";
            else if (selectedType.startsWith("EMAIL"))
                prefix = "EMAIL";
            else if (selectedType.startsWith("NOT_BLANK"))
                prefix = "NOT_BLANK";

            String val = ruleField.getValue().trim();
            if (!prefix.equals("EMAIL") && !prefix.equals("NOT_BLANK") && val.isEmpty()) {
                Notification.show("Rumus / Nilai tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }

            String finalRule = prefix.equals("EMAIL") || prefix.equals("NOT_BLANK") ? prefix : prefix + val;
            String customMsg = errorMsgField.getValue().trim();
            if (!customMsg.isEmpty()) {
                finalRule += "|" + customMsg;
            }

            propValidationRule.setValue(finalRule);
            field.validationRule = finalRule;
            Notification.show("Validasi dinamis diterapkan!", 3000, Notification.Position.BOTTOM_END);
            dialog.close();
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnClear = new Button("Hapus Validasi", VaadinIcon.TRASH.create(), e -> {
            propValidationRule.setValue("NONE");
            field.validationRule = "NONE";
            dialog.close();
        });
        btnClear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button btnCancel = new Button("Batal", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(btnClear, btnCancel, btnSave);
        dialog.open();
    }

    private void openOnAddScriptDialog(FieldMetaTemp fieldTemp) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚡ On-Add-Row Script & AI Assistant - " + fieldTemp.fieldName);
        dialog.setWidth("880px");
        dialog.setHeight("720px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setPadding(false);

        // 1. AI Assistant Panel
        com.vaadin.flow.component.details.Details aiDetails = new com.vaadin.flow.component.details.Details("✨ Asisten AI (Bahasa Manusia ke Groovy Script)");
        aiDetails.setOpened(true);
        aiDetails.setWidthFull();
        aiDetails.getStyle().set("background", "var(--lumo-contrast-5pct)").set("padding", "10px").set("border-radius", "8px");

        VerticalLayout aiLayout = new VerticalLayout();
        aiLayout.setPadding(false);
        aiLayout.setSpacing(true);
        Span aiHelp = new Span("Ketik instruksi aturan dalam bahasa Indonesia (misal: 'baris pertama status centang, lainnya false' atau 'baris 1 sampai 3 aktif'):");
        aiHelp.getStyle().set("font-size", "0.85em").set("color", "var(--lumo-secondary-text-color)");

        TextField aiInput = new TextField();
        aiInput.setPlaceholder("Contoh: jika baris pertama maka status = true, baris kedua dst false...");
        aiInput.setWidthFull();

        Button btnGenerateAi = new Button("✨ Buatkan Aturan (AI)", VaadinIcon.LIGHTBULB.create());
        btnGenerateAi.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        aiLayout.add(aiHelp, aiInput, btnGenerateAi);
        aiDetails.add(aiLayout);

        // 2. Quick Snippets & Variable Pickers
        HorizontalLayout pickersLayout = new HorizontalLayout();
        pickersLayout.setWidthFull();
        pickersLayout.setSpacing(true);

        ComboBox<String> templatePicker = new ComboBox<>("💡 Sisipkan Template");
        templatePicker.setItems(
                "Baris Pertama Centang (if rowIndex == 1)",
                "Baris 1 s/d 3 Centang (if rowIndex <= 3)",
                "Ambil dari Form Header (row.field = header.field)",
                "Lookup Tabel Lain (db.find)",
                "Kalkulasi Matematika (row.total = row.qty * row.price)"
        );
        templatePicker.setWidth("36%");

        ComboBox<String> rowVarPicker = new ComboBox<>("📄 Kolom Baris Ini (row)");
        List<String> childCols = new ArrayList<>();
        if (fieldTemp.lovCode != null && !fieldTemp.lovCode.trim().isEmpty()) {
            FormMeta childForm = dynamicDataService.getFormMetaRepository().findById(fieldTemp.lovCode).orElse(null);
            if (childForm != null && childForm.getFields() != null) {
                for (FieldMeta fm : childForm.getFields()) {
                    childCols.add("row." + fm.getFieldName());
                }
            }
        }
        if (childCols.isEmpty()) {
            childCols.addAll(Arrays.asList("row.status", "row.qty", "row.price", "row.item_code", "row.description"));
        }
        rowVarPicker.setItems(childCols);
        rowVarPicker.setWidth("34%");

        ComboBox<String> headerVarPicker = new ComboBox<>("🏢 Kolom Header (header)");
        List<String> headerCols = new ArrayList<>();
        for (FieldMetaTemp fm : fieldsList) {
            if (!"SUBFORM_GRID".equalsIgnoreCase(fm.componentType)) {
                headerCols.add("header." + fm.fieldName);
            }
        }
        if (headerCols.isEmpty()) {
            headerCols.addAll(Arrays.asList("header.qty", "header.date", "header.customer_id", "header.order_no"));
        }
        headerVarPicker.setItems(headerCols);
        headerVarPicker.setWidth("30%");

        pickersLayout.add(templatePicker, rowVarPicker, headerVarPicker);

        // 3. Code Editor Area
        TextArea scriptArea = new TextArea("Kode Script (Groovy Syntax):");
        scriptArea.setValue(fieldTemp.onAddScript != null ? fieldTemp.onAddScript : "");
        scriptArea.setWidthFull();
        scriptArea.setHeight("260px");
        scriptArea.getStyle().set("font-family", "monospace, Courier New").set("font-size", "13px");

        // Picker listeners to insert text into scriptArea
        templatePicker.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val == null) return;
            String snippet = "";
            if (val.startsWith("Baris Pertama")) {
                snippet = "// Aturan: Baris pertama aktif (true), baris kedua dst false\nif (rowIndex == 1) {\n    row.status = true\n} else {\n    row.status = false\n}\n";
            } else if (val.startsWith("Baris 1 s/d 3")) {
                snippet = "// Aturan: Baris 1 sampai 3 aktif (true), baris selanjutnya false\nif (rowIndex <= 3) {\n    row.status = true\n} else {\n    row.status = false\n}\n";
            } else if (val.startsWith("Ambil dari Form Header")) {
                snippet = "// Mengambil nilai dari kolom di header/master form\nrow.perseries = header.qty != null ? header.qty : 1\n";
            } else if (val.startsWith("Lookup Tabel Lain")) {
                snippet = "// Lookup data dari tabel lain di database\ndef item = db.find('lov_item', 'item_code', row.item_code)\nif (item != null) {\n    row.price = item.default_price\n}\n";
            } else if (val.startsWith("Kalkulasi Matematika")) {
                snippet = "// Kalkulasi antar kolom di baris yang sama\nrow.total = (row.qty != null ? row.qty : 0) * (row.price != null ? row.price : 0)\n";
            }
            String curr = scriptArea.getValue();
            scriptArea.setValue(curr + (curr.isEmpty() || curr.endsWith("\n") ? "" : "\n") + snippet);
            templatePicker.clear();
        });

        rowVarPicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                String curr = scriptArea.getValue();
                scriptArea.setValue(curr + (curr.isEmpty() || curr.endsWith(" ") || curr.endsWith("\n") ? "" : " ") + e.getValue());
                rowVarPicker.clear();
            }
        });

        headerVarPicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                String curr = scriptArea.getValue();
                scriptArea.setValue(curr + (curr.isEmpty() || curr.endsWith(" ") || curr.endsWith("\n") ? "" : " ") + e.getValue());
                headerVarPicker.clear();
            }
        });

        // AI Generator logic
        btnGenerateAi.addClickListener(e -> {
            String prompt = aiInput.getValue().toLowerCase().trim();
            if (prompt.isEmpty()) {
                Notification.show("Ketik instruksi untuk AI terlebih dahulu!", 3000, Notification.Position.MIDDLE);
                return;
            }
            StringBuilder generated = new StringBuilder("// ✨ Di-generate oleh AI Assistant dari perintah: \"" + aiInput.getValue() + "\"\n");
            boolean handled = false;

            // Helper untuk mengekstrak nama kolom target dari kalimat natural language
            String targetCol = "status"; // default
            List<String> reservedWords = Arrays.asList(
                "jika", "maka", "baris", "pertama", "kedua", "ketiga", "ke", "1", "2", "3", "4", "5",
                "dst", "selanjutnya", "lainnya", "sampai", "sd", "s/d", "kolom", "field", "param", "=", "==",
                "true", "false", "centang", "aktif", "tidak", "bukan", "di", "dari", "header",
                "master", "form", "ambil", "isi", "dengan", "sama", "adalah", "saja", "kalo", "kalau",
                "row", "index", "rowindex"
            );
            
            if (prompt.contains("kolom ")) {
                String after = prompt.substring(prompt.indexOf("kolom ") + 6).trim();
                String[] words = after.split("[\\s=,]+");
                if (words.length > 0 && !words[0].isEmpty()) {
                    targetCol = words[0];
                }
            } else if (prompt.contains("field ")) {
                String after = prompt.substring(prompt.indexOf("field ") + 6).trim();
                String[] words = after.split("[\\s=,]+");
                if (words.length > 0 && !words[0].isEmpty()) {
                    targetCol = words[0];
                }
            } else {
                String[] tokens = prompt.split("[\\s=,().]+");
                for (String t : tokens) {
                    if (!t.isEmpty() && !reservedWords.contains(t) && !t.matches("^[0-9]+$")) {
                        targetCol = t;
                        break;
                    }
                }
            }

            boolean valTrue = !prompt.contains("tidak centang") && !prompt.contains("false") && !prompt.contains("bukan");

            if (prompt.contains("baris pertama") || (prompt.contains("baris 1") && !prompt.contains("sampai") && !prompt.contains("sd"))) {
                generated.append("if (rowIndex == 1) {\n    row.").append(targetCol).append(" = ").append(valTrue).append("\n} else {\n    row.").append(targetCol).append(" = ").append(!valTrue).append("\n}\n");
                handled = true;
            } else if (prompt.contains("baris 1") && (prompt.contains("sampai") || prompt.contains("sd") || prompt.contains("-"))) {
                int maxRow = 3;
                if (prompt.contains("2")) maxRow = 2;
                else if (prompt.contains("3")) maxRow = 3;
                else if (prompt.contains("4")) maxRow = 4;
                else if (prompt.contains("5")) maxRow = 5;
                generated.append("if (rowIndex <= ").append(maxRow).append(") {\n    row.").append(targetCol).append(" = ").append(valTrue).append("\n} else {\n    row.").append(targetCol).append(" = ").append(!valTrue).append("\n}\n");
                handled = true;
            }
            if (prompt.contains("ambil") || prompt.contains("dari header") || prompt.contains("dari master")) {
                String toCol = targetCol;
                String fromCol = "qty";
                String[] tokens = prompt.split("[\\s=,().]+");
                List<String> nonReserved = new ArrayList<>();
                for (String t : tokens) {
                    if (!t.isEmpty() && !reservedWords.contains(t) && !t.matches("^[0-9]+$")) {
                        nonReserved.add(t);
                    }
                }
                if (nonReserved.size() >= 1) toCol = nonReserved.get(0);
                if (nonReserved.size() >= 2) fromCol = nonReserved.get(1);
                generated.append("row.").append(toCol).append(" = header.").append(fromCol).append(" != null ? header.").append(fromCol).append(" : 0\n");
                handled = true;
            }
            if (!handled) {
                generated.append("// AI: Struktur umum bersyarat berdasarkan indeks baris\nif (rowIndex == 1) {\n    row.").append(targetCol).append(" = ").append(valTrue).append("\n} else {\n    row.").append(targetCol).append(" = ").append(!valTrue).append("\n}\n");
            }
            scriptArea.setValue(generated.toString());
            Notification.show("✨ Aturan berhasil dibuat oleh AI!", 3000, Notification.Position.BOTTOM_END);
            aiInput.clear();
        });

        // 4. Live Simulator (Dry-Run)
        Button btnSimulate = new Button("▶ Simulasikan Script pada 3 Baris Dummy", VaadinIcon.PLAY.create());
        btnSimulate.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);

        Grid<Map<String, Object>> simGrid = new Grid<>();
        simGrid.setHeight("140px");
        simGrid.setVisible(false);
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(simGrid);

        btnSimulate.addClickListener(e -> {
            String scriptText = scriptArea.getValue().trim();
            if (scriptText.isEmpty()) {
                Notification.show("Script masih kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (dynamicDataService.getScriptExecutorService() == null) {
                Notification.show("ScriptExecutorService tidak aktif!", 3000, Notification.Position.MIDDLE);
                return;
            }
            try {
                List<Map<String, Object>> simRows = new ArrayList<>();
                Map<String, Object> dummyHeader = new HashMap<>();
                dummyHeader.put("qty", 100);
                dummyHeader.put("customer_id", "CUST-DEMO");
                dummyHeader.put("date", new java.util.Date());

                for (int i = 1; i <= 3; i++) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("baris_ke", i);
                    r.put("status", false);
                    r.put("perseries", 0);
                    dynamicDataService.getScriptExecutorService().executeScript(
                            "sim_" + System.currentTimeMillis() + "_" + i,
                            scriptText, r, i, dummyHeader, simRows);
                    simRows.add(r);
                }

                simGrid.removeAllColumns();
                if (!simRows.isEmpty()) {
                    for (String key : simRows.get(0).keySet()) {
                        simGrid.addColumn(row -> row.get(key)).setHeader(key.toUpperCase()).setAutoWidth(true);
                    }
                }
                simGrid.setItems(simRows);
                simGrid.setVisible(true);
                Notification.show("✅ Simulasi sukses! Lihat tabel hasil di bawah.", 3000, Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("❌ Error saat simulasi: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        layout.add(aiDetails, pickersLayout, scriptArea, btnSimulate, simGrid);

        Button btnSave = new Button("Simpan Script", VaadinIcon.CHECK.create(), e -> {
            fieldTemp.onAddScript = scriptArea.getValue().trim();
            Notification.show("On-Add-Row Script disimpan ke memori sementara (jangan lupa Klik Simpan Form)!", 4000, Notification.Position.BOTTOM_END);
            dialog.close();
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnClear = new Button("Hapus Script", VaadinIcon.TRASH.create(), e -> {
            scriptArea.clear();
            fieldTemp.onAddScript = null;
            dialog.close();
        });
        btnClear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button btnCancel = new Button("Batal", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(btnClear, btnCancel, btnSave);
        dialog.open();
    }

    private void openFilterConfigDialog(FieldMetaTemp fieldTemp) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Configure Filters for Field: " + fieldTemp.fieldName);
        dialog.setWidth("900px");
        dialog.setHeight("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        // Inputs
        ComboBox<String> logicalOpField = new ComboBox<>("Logika");
        logicalOpField.setItems("AND", "OR");
        logicalOpField.setValue("AND");
        logicalOpField.setWidth("80px");

        ComboBox<String> comparisonOpField = new ComboBox<>("Operator");
        comparisonOpField.setItems("=", ">", "<", ">=", "<=", "LIKE", "ILIKE", "!=");
        comparisonOpField.setValue("=");
        comparisonOpField.setWidth("100px");

        TextField filterColField = new TextField("Target Kolom");
        filterColField.setPlaceholder("e.g. parent_code");

        ComboBox<String> sourceTypeField = new ComboBox<>("Source Type");
        sourceTypeField.setItems("FIELD", "QUERY", "STATIC");
        sourceTypeField.setValue("FIELD");
        sourceTypeField.setWidth("100px");

        TextField sourceNameField = new TextField("Source Name / Value");
        sourceNameField.setPlaceholder("e.g. parent");

        Grid<FieldFilterMetaTemp> filtersGrid = new Grid<>();
        filtersGrid.setSizeFull();
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(filtersGrid);
        filtersGrid.addColumn(FieldFilterMetaTemp::getLogicalOperator).setHeader("Logika").setWidth("80px")
                .setFlexGrow(0);
        filtersGrid.addColumn(FieldFilterMetaTemp::getFilterColumn).setHeader("Target Kolom").setFlexGrow(1);
        filtersGrid.addColumn(FieldFilterMetaTemp::getComparisonOperator).setHeader("Op").setWidth("80px")
                .setFlexGrow(0);
        filtersGrid.addColumn(FieldFilterMetaTemp::getSourceType).setHeader("Tipe Sumber").setWidth("100px")
                .setFlexGrow(0);
        filtersGrid.addColumn(FieldFilterMetaTemp::getSourceName).setHeader("Nama Sumber").setFlexGrow(1);

        final FieldFilterMetaTemp[] currentEditing = new FieldFilterMetaTemp[1];
        Button btnAddFilter = new Button("Tambah", VaadinIcon.PLUS.create());
        Button btnCancelEdit = new Button("Batal Edit", VaadinIcon.CLOSE.create());
        btnCancelEdit.setVisible(false);

        Runnable resetForm = () -> {
            currentEditing[0] = null;
            filterColField.clear();
            sourceNameField.clear();
            btnAddFilter.setText("Tambah");
            btnAddFilter.setIcon(VaadinIcon.PLUS.create());
            btnCancelEdit.setVisible(false);
        };

        btnCancelEdit.addClickListener(e -> resetForm.run());
        btnCancelEdit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        filtersGrid.addComponentColumn(fTemp -> {
            Button btnEdit = new Button(VaadinIcon.PENCIL.create());
            btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnEdit.addClickListener(e -> {
                currentEditing[0] = fTemp;
                logicalOpField.setValue(fTemp.logicalOperator);
                comparisonOpField.setValue(fTemp.comparisonOperator);
                filterColField.setValue(fTemp.filterColumn);
                sourceTypeField.setValue(fTemp.sourceType);
                sourceNameField.setValue(fTemp.sourceName);
                btnAddFilter.setText("Update");
                btnAddFilter.setIcon(VaadinIcon.CHECK.create());
                btnCancelEdit.setVisible(true);
            });

            Button btnDel = new Button(VaadinIcon.TRASH.create());
            btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDel.addClickListener(e -> {
                fieldTemp.filters.remove(fTemp);
                filtersGrid.setItems(new ArrayList<>(fieldTemp.filters));
                if (currentEditing[0] == fTemp) {
                    resetForm.run();
                }
            });
            HorizontalLayout actions = new HorizontalLayout(btnEdit, btnDel);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Aksi").setWidth("120px").setFlexGrow(0);

        filtersGrid.setItems(fieldTemp.filters);

        btnAddFilter.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddFilter.addClickListener(e -> {
            String col = filterColField.getValue().trim();
            String srcType = sourceTypeField.getValue();
            String srcName = sourceNameField.getValue().trim();
            String logOp = logicalOpField.getValue();
            String compOp = comparisonOpField.getValue();

            if (col.isEmpty() || srcName.isEmpty()) {
                Notification.show("Kolom target dan Nama sumber tidak boleh kosong!", 3000,
                        Notification.Position.MIDDLE);
                return;
            }

            if (currentEditing[0] == null) {
                FieldFilterMetaTemp fTemp = new FieldFilterMetaTemp();
                fTemp.filterColumn = col;
                fTemp.sourceType = srcType;
                fTemp.sourceName = srcName;
                fTemp.logicalOperator = logOp;
                fTemp.comparisonOperator = compOp;
                fieldTemp.filters.add(fTemp);
            } else {
                currentEditing[0].filterColumn = col;
                currentEditing[0].sourceType = srcType;
                currentEditing[0].sourceName = srcName;
                currentEditing[0].logicalOperator = logOp;
                currentEditing[0].comparisonOperator = compOp;
            }

            filtersGrid.setItems(new ArrayList<>(fieldTemp.filters));
            resetForm.run();
        });

        HorizontalLayout btnLayout = new HorizontalLayout(btnAddFilter, btnCancelEdit);
        btnLayout.setSpacing(true);
        btnLayout.setPadding(false);

        HorizontalLayout inputLayout = new HorizontalLayout(logicalOpField, filterColField, comparisonOpField,
                sourceTypeField, sourceNameField, btnLayout);
        inputLayout.setAlignItems(Alignment.END);
        inputLayout.setSpacing(true);
        inputLayout.getStyle().set("flex-wrap", "wrap");

        layout.add(inputLayout, filtersGrid);
        dialog.add(layout);

        Button btnClose = new Button("Selesai", e -> dialog.close());
        dialog.getFooter().add(btnClose);

        dialog.open();
    }

    private void saveFormDefinition() {
        String formCode = formCodeField.getValue().trim();
        String formTitle = formTitleField.getValue().trim();
        String tableName = tableNameField.getValue().trim();
        String pk = pkField.getValue().trim();
        String labelWidth = labelWidthField.getValue().trim();
        String sortField = defaultSortField.getValue().trim();
        String sortDir = defaultSortDirection.getValue();

        if (formCode.isEmpty() || formTitle.isEmpty() || (autoCreateDbCheckbox.getValue() && tableName.isEmpty())) {
            Notification.show("Form Code dan Title tidak boleh kosong! (Table Name wajib jika Auto-Generate aktif)", 3000,
                    Notification.Position.MIDDLE);
            return;
        }

        if (fieldsList.isEmpty()) {
            Notification.show("Harus ada minimal 1 field untuk disimpan!", 3000, Notification.Position.MIDDLE);
            return;
        }

        FormMeta formMeta = formMetaRepository.findById(formCode).orElseGet(FormMeta::new);
        formMeta.setFormCode(formCode);
        formMeta.setFormTitle(formTitle);
        formMeta.setFormType(formTypeCombo.getValue());
        formMeta.setTableName(tableName.isEmpty() ? null : tableName);
        String viewTable = viewTableField.getValue().trim();
        formMeta.setViewTable(viewTable.isEmpty() ? null : viewTable);
        formMeta.setPrimaryKey(pk.isEmpty() ? "id" : pk);
        formMeta.setLabelWidth(labelWidth.isEmpty() ? "150px" : labelWidth);
        formMeta.setDefaultSortField(sortField.isEmpty() ? null : sortField);
        formMeta.setDefaultSortDirection(sortDir);

        java.util.Set<com.vaadinerp.meta.FormActionMeta> chosenActions = assignedActionsCombo.getValue();
        if (chosenActions != null && !chosenActions.isEmpty()) {
            String joined = chosenActions.stream()
                    .map(com.vaadinerp.meta.FormActionMeta::getActionCode)
                    .filter(c -> c != null && !c.trim().isEmpty())
                    .collect(java.util.stream.Collectors.joining(","));
            formMeta.setExtraToolbars(joined);
        } else {
            formMeta.setExtraToolbars(null);
        }

        if ("MASTER_DETAIL".equals(formTypeCombo.getValue())) {
            String dtlTable = detailTableNameField.getValue().trim();
            String dtlPk = detailPkField.getValue().trim();
            String dtlFk = detailFkField.getValue().trim();
            if (dtlTable.isEmpty() || dtlFk.isEmpty()) {
                Notification.show("Nama tabel detail dan foreign key tidak boleh kosong untuk Master-Detail form!",
                        3000, Notification.Position.MIDDLE);
                return;
            }
            formMeta.setDetailTableName(dtlTable);
            formMeta.setDetailPrimaryKey(dtlPk.isEmpty() ? "id" : dtlPk);
            formMeta.setDetailForeignKey(dtlFk);
        } else {
            formMeta.setDetailTableName(null);
            formMeta.setDetailPrimaryKey(null);
            formMeta.setDetailForeignKey(null);
        }

        Map<String, FieldMeta> existingFieldMap = new HashMap<>();
        if (formMeta.getFields() == null) {
            formMeta.setFields(new ArrayList<>());
        } else {
            for (FieldMeta fm : formMeta.getFields()) {
                if (fm.getFieldName() != null) {
                    existingFieldMap.put(fm.getFieldName().trim().toLowerCase(), fm);
                }
            }
        }

        List<FieldMeta> updatedFields = new ArrayList<>();
        int order = 10;
        for (FieldMetaTemp temp : fieldsList) {
            String key = temp.fieldName != null ? temp.fieldName.trim().toLowerCase() : "";
            FieldMeta field = existingFieldMap.remove(key);
            if (field == null) {
                field = new FieldMeta();
                field.setFormMeta(formMeta);
            }
            field.setFieldName(temp.fieldName);
            field.setFieldLabel(temp.fieldLabel);
            field.setComponentType(temp.componentType);
            field.setLovCode(temp.lovCode);
            field.setRequired(temp.isRequired);
            field.setReadonly(temp.isReadonly);
            field.setShowInGrid(temp.showInGrid);
            field.setHideInForm(temp.hideInForm);
            field.setDetail(temp.isDetail);
            field.setSortable(temp.isSortable);
            field.setFormula(temp.formula);
            field.setDisplayFormat(temp.displayFormat);
            field.setValidationRule(temp.validationRule);
            field.setSequenceCode(temp.sequenceCode);
            field.setSaveOnInsert(temp.saveOnInsert);
            field.setSaveOnUpdate(temp.saveOnUpdate);
            field.setAuditLog(temp.isAuditLog);
            field.setOnAddScript(temp.onAddScript);
            field.setRowGroup(temp.rowGroup);
            field.setColSpan(temp.colSpan);
            field.setColOrder(order);
            order += 10;

            List<FieldFilterMeta> filters = new ArrayList<>();
            for (FieldFilterMetaTemp fTemp : temp.filters) {
                FieldFilterMeta f = new FieldFilterMeta();
                f.setFieldMeta(field);
                f.setFilterColumn(fTemp.filterColumn);
                f.setSourceType(fTemp.sourceType);
                f.setSourceName(fTemp.sourceName);
                f.setLogicalOperator(fTemp.logicalOperator);
                f.setComparisonOperator(fTemp.comparisonOperator);
                filters.add(f);
            }
            if (field.getFilters() == null) {
                field.setFilters(filters);
            } else {
                field.getFilters().clear();
                field.getFilters().addAll(filters);
            }

            List<com.vaadinerp.meta.FieldLovTargetMeta> lovTargets = new ArrayList<>();
            if (temp.lovTargets != null) {
                for (FieldLovTargetMetaTemp tTemp : temp.lovTargets) {
                    com.vaadinerp.meta.FieldLovTargetMeta target = new com.vaadinerp.meta.FieldLovTargetMeta();
                    target.setFieldMeta(field);
                    target.setSourceColumn(tTemp.sourceColumn);
                    target.setTargetField(tTemp.targetField);
                    target.setActionType(tTemp.actionType);
                    target.setLookupColumn(tTemp.lookupColumn);
                    lovTargets.add(target);
                }
            }
            if (field.getLovTargets() == null) {
                field.setLovTargets(lovTargets);
            } else {
                field.getLovTargets().clear();
                field.getLovTargets().addAll(lovTargets);
            }

            updatedFields.add(field);
        }

        // Only remove fields from formMeta.getFields() if they were actually deleted by the user
        if (!existingFieldMap.isEmpty()) {
            formMeta.getFields().removeAll(existingFieldMap.values());
        }
        for (FieldMeta fm : updatedFields) {
            if (!formMeta.getFields().contains(fm)) {
                formMeta.getFields().add(fm);
            }
        }

        try {
            // 1. Save metadata definitions (cascades automatically)
            formMetaRepository.save(formMeta);

            // 2. Generate/Alter physical table under schema "dynamic" in PostgreSQL if toggle checked
            if (autoCreateDbCheckbox.getValue() && tableName != null && !tableName.isEmpty()) {
                dynamicDataService.generatePhysicalTable(formMeta);
            }

            String msgTable = (tableName != null && !tableName.isEmpty()) ? (" dan tabel dynamic." + tableName + " siap digunakan!") : " (Tanpa tabel fisik)";
            Notification.show(
                    "Form " + formTitle + " berhasil disimpan" + msgTable, 4000,
                    Notification.Position.TOP_CENTER);

            // Clear inputs and reset read-only status
            formCodeField.clear();
            formCodeField.setReadOnly(false);
            formTitleField.clear();
            formTypeCombo.setValue("SINGLE");
            tableNameField.clear();
            autoCreateDbCheckbox.setValue(false);
            viewTableField.clear();
            pkField.setValue("id");
            labelWidthField.setValue("150px");
            defaultSortField.clear();
            defaultSortDirection.setValue("ASC");
            assignedActionsCombo.clear();
            detailTableNameField.clear();
            detailPkField.setValue("id");
            detailFkField.clear();
            fieldsList.clear();
            selectField(null);
            rebuildCanvas();

            // Refresh history grid and switch to historis tab
            refreshHistoryGrid();
            tabSheet.setSelectedTab(historisTab);

            if (onFormSavedListener != null) {
                onFormSavedListener.run();
            }
        } catch (Exception ex) {
            Notification.show("Gagal menyimpan form: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private List<String> getLovColumns(String lovCode) {
        List<String> list = new ArrayList<>();
        list.add("_label");
        if (lovCode == null || lovCode.trim().isEmpty())
            return list;

        // 1. Try getLovMeta (searches both FormMeta and LovMeta with case-handling)
        LovMeta lov = dynamicDataService.getLovMeta(lovCode.trim()).orElse(null);
        if (lov == null && !lovCode.trim().equalsIgnoreCase(lovCode)) {
            lov = dynamicDataService.getLovMeta(lovCode.trim().toLowerCase()).orElse(null);
        }
        if (lov == null && !lovCode.trim().equalsIgnoreCase(lovCode)) {
            lov = dynamicDataService.getLovMeta(lovCode.trim().toUpperCase()).orElse(null);
        }

        if (lov != null) {
            if (lov.getValueColumn() != null && !lov.getValueColumn().trim().isEmpty() && !list.contains(lov.getValueColumn().trim()))
                list.add(lov.getValueColumn().trim());
            if (lov.getLabelColumn() != null && !lov.getLabelColumn().trim().isEmpty() && !list.contains(lov.getLabelColumn().trim()))
                list.add(lov.getLabelColumn().trim());

            if (lov.getGridColumns() != null && !lov.getGridColumns().trim().isEmpty()) {
                for (String part : lov.getGridColumns().split(",")) {
                    String[] split = part.split(":");
                    if (split.length > 0) {
                        String col = split[0].trim();
                        if (!col.isEmpty() && !list.contains(col)) {
                            list.add(col);
                        }
                    }
                }
            }
            if (lov.getTableName() != null && !lov.getTableName().trim().isEmpty()) {
                for (String col : dynamicDataService.getColumnsForQueryOrTable(lov.getTableName().trim())) {
                    if (col != null && !col.trim().isEmpty() && !list.contains(col.trim()))
                        list.add(col.trim());
                }
            }
            if (list.size() > 1) {
                enrichLovColumnsWithLabels(lovCode, list);
                return list;
            }
        }

        // 2. Try FormMeta fallback directly if getLovMeta didn't yield columns
        FormMeta form = formMetaRepository.findById(lovCode.trim()).orElse(null);
        if (form == null) form = formMetaRepository.findById(lovCode.trim().toLowerCase()).orElse(null);
        if (form == null) form = formMetaRepository.findById(lovCode.trim().toUpperCase()).orElse(null);
        if (form != null) {
            String pk = form.getPrimaryKey() != null ? form.getPrimaryKey() : "id";
            if (!list.contains(pk))
                list.add(pk);
            if (form.getFields() != null) {
                for (FieldMeta field : form.getFields()) {
                    String fName = field.getFieldName();
                    if (fName != null && !list.contains(fName)) {
                        list.add(fName);
                    }
                }
            }
            if (form.getTableName() != null && !form.getTableName().trim().isEmpty()) {
                for (String col : dynamicDataService.getColumnsForQueryOrTable(form.getTableName().trim())) {
                    if (col != null && !col.trim().isEmpty() && !list.contains(col.trim()))
                        list.add(col.trim());
                }
            }
            if (list.size() > 1) {
                enrichLovColumnsWithLabels(lovCode, list);
                return list;
            }
        }

        // 3. Try physical table or query fallback
        for (String col : dynamicDataService.getColumnsForQueryOrTable(lovCode.trim())) {
            if (col != null && !col.trim().isEmpty() && !list.contains(col.trim()))
                list.add(col.trim());
        }
        if (list.size() == 1) {
            for (String col : dynamicDataService.getColumnsForQueryOrTable(lovCode.trim().toLowerCase())) {
                if (col != null && !col.trim().isEmpty() && !list.contains(col.trim()))
                    list.add(col.trim());
            }
        }

        enrichLovColumnsWithLabels(lovCode, list);
        return list;
    }

    private void enrichLovColumnsWithLabels(String lovCode, List<String> list) {
        if (lovCode == null || list == null) return;
        List<String> extraLabels = new ArrayList<>();
        FormMeta sourceForm = formMetaRepository.findById(lovCode.trim()).orElse(null);
        if (sourceForm == null) sourceForm = formMetaRepository.findById(lovCode.trim().toLowerCase()).orElse(null);
        if (sourceForm == null) sourceForm = formMetaRepository.findById(lovCode.trim().toUpperCase()).orElse(null);
        if (sourceForm == null) {
            LovMeta lm = dynamicDataService.getLovMeta(lovCode.trim()).orElse(null);
            if (lm != null && lm.getTableName() != null) {
                sourceForm = formMetaRepository.findById(lm.getTableName().trim()).orElse(null);
            }
        }

        if (sourceForm != null && sourceForm.getFields() != null) {
            for (FieldMeta fm : sourceForm.getFields()) {
                if (fm.getLovCode() != null && !fm.getLovCode().trim().isEmpty() && fm.getFieldName() != null) {
                    String lblCol = fm.getFieldName().trim() + "_label";
                    if (!list.contains(lblCol) && !extraLabels.contains(lblCol)) {
                        extraLabels.add(lblCol);
                    }
                }
            }
        }
        for (String col : new ArrayList<>(list)) {
            if (col != null && col.toLowerCase().endsWith("id") && !col.equalsIgnoreCase("id") && !col.equalsIgnoreCase("_label")) {
                String lblCol = col + "_label";
                if (!list.contains(lblCol) && !extraLabels.contains(lblCol)) {
                    extraLabels.add(lblCol);
                }
            }
        }
        list.addAll(extraLabels);
    }

    private void openLovTargetsConfigDialog(FieldMetaTemp fieldTemp) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Configure LOV Targets for " + fieldTemp.fieldName);
        dialog.setWidth("900px");
        dialog.setHeight("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        List<String> lovCols = new ArrayList<>(getLovColumns(fieldTemp.lovCode));
        if (lovCols.isEmpty()) {
            lovCols.add("value");
        }

        List<String> formFields = fieldsList.stream()
                .map(f -> f.fieldName)
                .filter(name -> !name.equalsIgnoreCase(fieldTemp.fieldName))
                .collect(Collectors.toList());

        if (tableNameField != null && tableNameField.getValue() != null
                && !tableNameField.getValue().trim().isEmpty()) {
            for (String col : dynamicDataService.fetchTableColumns(tableNameField.getValue().trim())) {
                if (!col.equalsIgnoreCase(fieldTemp.fieldName) && !formFields.contains(col)) {
                    formFields.add(col);
                }
            }
        }
        if (detailTableNameField != null && detailTableNameField.getValue() != null
                && !detailTableNameField.getValue().trim().isEmpty()) {
            for (String col : dynamicDataService.fetchTableColumns(detailTableNameField.getValue().trim())) {
                if (!col.equalsIgnoreCase(fieldTemp.fieldName) && !formFields.contains(col)) {
                    formFields.add(col);
                }
            }
        }
        if (viewTableField != null && viewTableField.getValue() != null
                && !viewTableField.getValue().trim().isEmpty()) {
            for (String col : dynamicDataService.fetchTableColumns(viewTableField.getValue().trim())) {
                if (!col.equalsIgnoreCase(fieldTemp.fieldName) && !formFields.contains(col)) {
                    formFields.add(col);
                }
            }
        }

        ComboBox<String> sourceColCombo = new ComboBox<>("Source Column (LOV)");
        sourceColCombo.setItems(lovCols);
        sourceColCombo.setPlaceholder("Select LOV column...");
        if (lovCols.size() == 1) {
            sourceColCombo.setValue(lovCols.get(0));
        }

        ComboBox<String> targetFieldCombo = new ComboBox<>("Target Field (Form)");
        targetFieldCombo.setItems(formFields);
        targetFieldCombo.setPlaceholder("Select target form field...");

        ComboBox<String> actionTypeSelect = new ComboBox<>("Action Type");
        actionTypeSelect.setItems("COPY", "QUERY_LOV");
        actionTypeSelect.setValue("COPY");

        ComboBox<String> lookupColCombo = new ComboBox<>("Lookup Column");
        lookupColCombo.setPlaceholder("Select lookup column...");
        lookupColCombo.setVisible(false);

        targetFieldCombo.addValueChangeListener(e -> {
            String targetFieldName = e.getValue();
            if (targetFieldName != null) {
                FieldMetaTemp targetFieldTemp = fieldsList.stream()
                        .filter(f -> f.fieldName.equalsIgnoreCase(targetFieldName))
                        .findFirst().orElse(null);
                if (targetFieldTemp != null && targetFieldTemp.lovCode != null
                        && !targetFieldTemp.lovCode.trim().isEmpty()) {
                    List<String> targetLovCols = getLovColumns(targetFieldTemp.lovCode);
                    lookupColCombo.setItems(targetLovCols);
                    lookupColCombo.setEnabled(true);
                } else {
                    lookupColCombo.setItems(new ArrayList<>());
                    lookupColCombo.setEnabled(false);
                }
            } else {
                lookupColCombo.setItems(new ArrayList<>());
                lookupColCombo.setEnabled(false);
            }
        });

        actionTypeSelect.addValueChangeListener(e -> {
            boolean isQuery = "QUERY_LOV".equalsIgnoreCase(e.getValue());
            lookupColCombo.setVisible(isQuery);
        });

        Grid<FieldLovTargetMetaTemp> grid = new Grid<>();
        grid.setSizeFull();
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(grid);
        grid.addColumn(FieldLovTargetMetaTemp::getSourceColumn).setHeader("Source Column");
        grid.addColumn(FieldLovTargetMetaTemp::getTargetField).setHeader("Target Field");
        grid.addColumn(FieldLovTargetMetaTemp::getActionType).setHeader("Action Type");
        grid.addColumn(FieldLovTargetMetaTemp::getLookupColumn).setHeader("Lookup Column");

        final FieldLovTargetMetaTemp[] currentEditing = new FieldLovTargetMetaTemp[1];
        Button btnAdd = new Button("Tambah Target", VaadinIcon.PLUS.create());
        Button btnCancelEdit = new Button("Batal Edit", VaadinIcon.CLOSE.create());
        btnCancelEdit.setVisible(false);

        Runnable resetForm = () -> {
            currentEditing[0] = null;
            sourceColCombo.clear();
            targetFieldCombo.clear();
            actionTypeSelect.setValue("COPY");
            lookupColCombo.clear();
            btnAdd.setText("Tambah Target");
            btnAdd.setIcon(VaadinIcon.PLUS.create());
            btnCancelEdit.setVisible(false);
        };

        btnCancelEdit.addClickListener(e -> resetForm.run());
        btnCancelEdit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        grid.addComponentColumn(tTemp -> {
            Button btnEdit = new Button(VaadinIcon.PENCIL.create());
            btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnEdit.addClickListener(e -> {
                currentEditing[0] = tTemp;
                sourceColCombo.setValue(tTemp.sourceColumn);
                targetFieldCombo.setValue(tTemp.targetField);
                actionTypeSelect.setValue(tTemp.actionType);
                if (tTemp.lookupColumn != null) {
                    lookupColCombo.setValue(tTemp.lookupColumn);
                }
                btnAdd.setText("Update");
                btnAdd.setIcon(VaadinIcon.CHECK.create());
                btnCancelEdit.setVisible(true);
            });

            Button btnDel = new Button(VaadinIcon.TRASH.create());
            btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDel.addClickListener(e -> {
                fieldTemp.lovTargets.remove(tTemp);
                grid.setItems(new ArrayList<>(fieldTemp.lovTargets));
                if (currentEditing[0] == tTemp) {
                    resetForm.run();
                }
            });
            HorizontalLayout actions = new HorizontalLayout(btnEdit, btnDel);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Aksi").setWidth("120px").setFlexGrow(0);

        grid.setItems(fieldTemp.lovTargets);

        btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAdd.addClickListener(e -> {
            String srcCol = sourceColCombo.getValue();
            String trgField = targetFieldCombo.getValue();
            String actType = actionTypeSelect.getValue();
            String lookupCol = lookupColCombo.getValue();

            if (srcCol == null || trgField == null) {
                Notification.show("Source column dan Target field harus dipilih!", 3000, Notification.Position.MIDDLE);
                return;
            }

            if ("QUERY_LOV".equalsIgnoreCase(actType) && lookupCol == null) {
                Notification.show("Lookup Column harus dipilih jika Action Type = QUERY_LOV!", 3000,
                        Notification.Position.MIDDLE);
                return;
            }

            if (currentEditing[0] == null) {
                FieldLovTargetMetaTemp tTemp = new FieldLovTargetMetaTemp();
                tTemp.sourceColumn = srcCol;
                tTemp.targetField = trgField;
                tTemp.actionType = actType;
                tTemp.lookupColumn = lookupCol;
                fieldTemp.lovTargets.add(tTemp);
            } else {
                currentEditing[0].sourceColumn = srcCol;
                currentEditing[0].targetField = trgField;
                currentEditing[0].actionType = actType;
                currentEditing[0].lookupColumn = lookupCol;
            }

            grid.setItems(new ArrayList<>(fieldTemp.lovTargets));
            resetForm.run();
        });

        HorizontalLayout btnLayout = new HorizontalLayout(btnAdd, btnCancelEdit);
        btnLayout.setSpacing(true);
        btnLayout.setPadding(false);

        HorizontalLayout inputLayout = new HorizontalLayout(sourceColCombo, targetFieldCombo, actionTypeSelect,
                lookupColCombo, btnLayout);
        inputLayout.setAlignItems(Alignment.END);
        inputLayout.setSpacing(true);
        inputLayout.getStyle().set("flex-wrap", "wrap");

        layout.add(inputLayout, grid);
        dialog.add(layout);

        Button btnClose = new Button("Selesai", e -> dialog.close());
        dialog.getFooter().add(btnClose);
        dialog.open();
    }

    private void buildToolbar() {
        toolbar.removeAll();
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
                .set("z-index", "1000")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.08)");

        // 1. TAMBAH BUTTON
        Button btnNew = new Button("Tambah");
        Icon iconNew = VaadinIcon.PLUS_CIRCLE.create();
        iconNew.getStyle().set("color", "#22c55e").set("font-size", "1.2rem");
        btnNew.setIcon(iconNew);
        btnNew.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnNew.getStyle().set("font-weight", "500").set("color", "#374151");
        btnNew.addClickListener(e -> {
            formCodeField.clear();
            formCodeField.setReadOnly(false);
            formTitleField.clear();
            formTypeCombo.setValue("SINGLE");
            tableNameField.clear();
            autoCreateDbCheckbox.setValue(false);
            viewTableField.clear();
            pkField.setValue("id");
            labelWidthField.setValue("150px");
            defaultSortField.clear();
            defaultSortDirection.setValue("ASC");
            detailTableNameField.clear();
            detailPkField.setValue("id");
            detailFkField.clear();
            fieldsList.clear();
            selectField(null);
            rebuildCanvas();
            tabSheet.setSelectedTab(transaksiTab);
        });

        // 1.5. EDIT BUTTON
        Button btnEdit = new Button("Edit");
        Icon iconEdit = VaadinIcon.EDIT.create();
        iconEdit.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnEdit.setIcon(iconEdit);
        btnEdit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnEdit.getStyle().set("font-weight", "500").set("color", "#374151");
        btnEdit.addClickListener(e -> {
            if (tabSheet.getSelectedTab() == historisTab) {
                java.util.Set<FormMeta> sel = historyGrid.getSelectedItems();
                if (sel != null && !sel.isEmpty()) {
                    FormMeta selectedForm = sel.iterator().next();
                    loadFormDefinition(selectedForm);
                    tabSheet.setSelectedTab(transaksiTab);
                } else {
                    Notification.show("Pilih form yang akan diedit terlebih dahulu di tab Historis!", 3000, Notification.Position.MIDDLE);
                }
            } else {
                Notification.show("Anda sudah berada di tab desain/edit form.", 3000, Notification.Position.MIDDLE);
            }
        });

        // 2. HAPUS BUTTON
        Button btnDelete = new Button("Hapus");
        Icon iconDelete = VaadinIcon.CLOSE_CIRCLE.create();
        iconDelete.getStyle().set("color", "#ef4444").set("font-size", "1.2rem");
        btnDelete.setIcon(iconDelete);
        btnDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnDelete.getStyle().set("font-weight", "500").set("color", "#374151");
        btnDelete.addClickListener(e -> {
            if (tabSheet.getSelectedTab() == historisTab) {
                java.util.Set<FormMeta> selectedItems = historyGrid.getSelectedItems();
                if (selectedItems != null && !selectedItems.isEmpty()) {
                    showConfirmDialog("Konfirmasi Hapus",
                            "Apakah Anda yakin ingin menghapus " + selectedItems.size() + " definisi form ini?", () -> {
                                try {
                                    formMetaRepository.deleteAll(selectedItems);
                                    Notification.show("Definisi form berhasil dihapus!", 3000,
                                            Notification.Position.TOP_CENTER);
                                    refreshHistoryGrid();
                                    historyGrid.deselectAll();
                                    if (onFormSavedListener != null) {
                                        onFormSavedListener.run();
                                    }
                                } catch (Exception ex) {
                                    Notification.show("Gagal menghapus: " + ex.getMessage(), 5000,
                                            Notification.Position.MIDDLE);
                                }
                            });
                } else {
                    Notification.show("Pilih baris form pada grid terlebih dahulu untuk dihapus.", 3000,
                            Notification.Position.MIDDLE);
                }
            } else {
                String formCode = formCodeField.getValue().trim();
                if (!formCode.isEmpty() && formMetaRepository.existsById(formCode)) {
                    showConfirmDialog("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus definisi form ini?",
                            () -> {
                                try {
                                    formMetaRepository.deleteById(formCode);
                                    Notification.show("Definisi form berhasil dihapus!", 3000,
                                            Notification.Position.TOP_CENTER);

                                    // Reset builder
                                    formCodeField.clear();
                                    formCodeField.setReadOnly(false);
                                    formTitleField.clear();
                                    formTypeCombo.setValue("SINGLE");
                                    tableNameField.clear();
                                    autoCreateDbCheckbox.setValue(false);
                                    pkField.setValue("id");
                                    labelWidthField.setValue("150px");
                                    defaultSortField.clear();
                                    defaultSortDirection.setValue("ASC");
                                    detailTableNameField.clear();
                                    detailPkField.setValue("id");
                                    detailFkField.clear();
                                    fieldsList.clear();
                                    selectField(null);
                                    rebuildCanvas();

                                    refreshHistoryGrid();
                                    tabSheet.setSelectedTab(historisTab);
                                    if (onFormSavedListener != null) {
                                        onFormSavedListener.run();
                                    }
                                } catch (Exception ex) {
                                    Notification.show("Gagal menghapus: " + ex.getMessage(), 5000,
                                            Notification.Position.MIDDLE);
                                }
                            });
                } else {
                    Notification.show("Tidak ada form tersimpan yang sedang dibuka untuk dihapus.", 3000,
                            Notification.Position.MIDDLE);
                }
            }
        });

        // 2.5. COPY BUTTON
        Button btnCopy = new Button("Copy");
        Icon iconCopy = VaadinIcon.COPY.create();
        iconCopy.getStyle().set("color", "#f59e0b").set("font-size", "1.2rem");
        btnCopy.setIcon(iconCopy);
        btnCopy.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCopy.getStyle().set("font-weight", "500").set("color", "#374151");
        btnCopy.addClickListener(e -> {
            FormMeta selected = null;
            if (tabSheet.getSelectedTab() == historisTab) {
                java.util.Set<FormMeta> sel = historyGrid.getSelectedItems();
                if (sel != null && !sel.isEmpty()) {
                    selected = sel.iterator().next();
                }
            } else {
                String formCode = formCodeField.getValue().trim();
                if (!formCode.isEmpty() && formMetaRepository.existsById(formCode)) {
                    selected = formMetaRepository.findById(formCode).orElse(null);
                }
            }

            if (selected != null) {
                if (tabSheet.getSelectedTab() == historisTab) {
                    loadFormDefinition(selected);
                    tabSheet.setSelectedTab(transaksiTab);
                }
                formCodeField.clear();
                formCodeField.setReadOnly(false);
                String oldTitle = formTitleField.getValue();
                formTitleField.setValue(oldTitle != null ? oldTitle + " - Copy" : "Copy");
                Notification.show("Silakan masukkan Kode Form baru dan tekan Simpan.", 4000,
                        Notification.Position.MIDDLE);
                formCodeField.focus();
            } else {
                Notification.show("Pilih form yang sudah tersimpan untuk di-copy.", 3000, Notification.Position.MIDDLE);
            }
        });

        // 3. SIMPAN BUTTON
        Button btnSave = new Button("Simpan");
        Icon iconSave = VaadinIcon.DOWNLOAD.create();
        iconSave.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnSave.setIcon(iconSave);
        btnSave.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("font-weight", "600").set("color", "#3b82f6");
        btnSave.addClickListener(e -> {
            saveFormDefinition();
        });

        // 4. BATAL BUTTON
        Button btnCancel = new Button("Batal");
        Icon iconCancel = VaadinIcon.BAN.create();
        iconCancel.getStyle().set("color", "#ef4444").set("font-size", "1.2rem");
        btnCancel.setIcon(iconCancel);
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCancel.getStyle().set("font-weight", "500").set("color", "#374151");
        btnCancel.addClickListener(e -> {
            formCodeField.clear();
            formCodeField.setReadOnly(false);
            formTitleField.clear();
            formTypeCombo.setValue("SINGLE");
            tableNameField.clear();
            autoCreateDbCheckbox.setValue(false);
            viewTableField.clear();
            pkField.setValue("id");
            labelWidthField.setValue("150px");
            defaultSortField.clear();
            defaultSortDirection.setValue("ASC");
            detailTableNameField.clear();
            detailPkField.setValue("id");
            detailFkField.clear();
            fieldsList.clear();
            selectField(null);
            rebuildCanvas();
            tabSheet.setSelectedTab(historisTab);
        });

        // 5. REFRESH BUTTON
        Button btnRefresh = new Button("Refresh");
        Icon iconRefresh = VaadinIcon.REFRESH.create();
        iconRefresh.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnRefresh.setIcon(iconRefresh);
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnRefresh.getStyle().set("font-weight", "500").set("color", "#374151");
        btnRefresh.addClickListener(e -> {
            if (tabSheet.getSelectedTab() == historisTab) {
                refreshHistoryGrid();
                Notification.show("Daftar form diperbarui!", 1500, Notification.Position.BOTTOM_END);
            } else {
                String formCode = formCodeField.getValue().trim();
                if (!formCode.isEmpty() && formMetaRepository.existsById(formCode)) {
                    FormMeta fresh = formMetaRepository.findById(formCode).orElse(null);
                    if (fresh != null) {
                        loadFormDefinition(fresh);
                        Notification.show("Definisi form direfresh dari database!", 1500,
                                Notification.Position.BOTTOM_END);
                    }
                } else {
                    Notification.show("Form belum tersimpan. Kanvas dibersihkan!", 1500,
                            Notification.Position.BOTTOM_END);
                    fieldsList.clear();
                    selectField(null);
                    rebuildCanvas();
                }
            }
        });

        toolbar.add(btnNew, btnEdit, btnCopy, btnDelete, btnSave, btnCancel, btnRefresh);
    }

    private void refreshHistoryGrid() {
        allHistoryItems.clear();
        allHistoryItems.addAll(formMetaRepository.findAll());
        applyHistoryFilters();
    }

    private void applyHistoryFilters() {
        java.util.List<FormMeta> filtered = allHistoryItems.stream().filter(item -> {
            for (java.util.Map.Entry<String, FilterCriteria> entry : filterValues.entrySet()) {
                String fieldName = entry.getKey();
                FilterCriteria criteria = entry.getValue();

                String op = criteria.operator;
                String query = criteria.value;

                String strVal = "";
                if ("formCode".equals(fieldName) && item.getFormCode() != null)
                    strVal = item.getFormCode().toLowerCase();
                if ("formTitle".equals(fieldName) && item.getFormTitle() != null)
                    strVal = item.getFormTitle().toLowerCase();
                if ("formType".equals(fieldName) && item.getFormType() != null)
                    strVal = item.getFormType().toLowerCase();
                if ("tableName".equals(fieldName) && item.getTableName() != null)
                    strVal = item.getTableName().toLowerCase();
                if ("detailTableName".equals(fieldName) && item.getDetailTableName() != null)
                    strVal = item.getDetailTableName().toLowerCase();

                if ("Blank".equals(op)) {
                    if (!strVal.isEmpty())
                        return false;
                    continue;
                }
                if ("Not blank".equals(op)) {
                    if (strVal.isEmpty())
                        return false;
                    continue;
                }

                if (query == null || query.trim().isEmpty()) {
                    continue;
                }

                query = query.toLowerCase();

                switch (op) {
                    case "Contains":
                        if (!strVal.contains(query))
                            return false;
                        break;
                    case "Not contains":
                        if (strVal.contains(query))
                            return false;
                        break;
                    case "Equals":
                        if (!strVal.equals(query))
                            return false;
                        break;
                    case "Not equal":
                        if (strVal.equals(query))
                            return false;
                        break;
                    case "Starts with":
                        if (!strVal.startsWith(query))
                            return false;
                        break;
                    case "Ends with":
                        if (!strVal.endsWith(query))
                            return false;
                        break;
                }
            }
            return true;
        }).collect(java.util.stream.Collectors.toList());

        historyGrid.setItems(filtered);
    }

    private void showConfirmDialog(String titleText, String message, Runnable confirmAction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(titleText);
        dialog.add(new com.vaadin.flow.component.html.Paragraph(message));

        Button btnConfirm = new Button("Ya, Hapus", event -> {
            confirmAction.run();
            dialog.close();
        });
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button btnCancel = new Button("Batal", event -> dialog.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(btnCancel, btnConfirm);
        dialog.open();
    }

    private void loadFormDefinition(FormMeta selectedForm) {
        if (selectedForm != null) {
            // Populate Form Meta fields
            formCodeField.setValue(selectedForm.getFormCode() != null ? selectedForm.getFormCode() : "");
            formCodeField.setReadOnly(true);
            formTitleField.setValue(selectedForm.getFormTitle() != null ? selectedForm.getFormTitle() : "");
            formTypeCombo.setValue(selectedForm.getFormType() != null ? selectedForm.getFormType() : "SINGLE");
            tableNameField.setValue(selectedForm.getTableName() != null ? selectedForm.getTableName() : "");
            autoCreateDbCheckbox.setValue(false);
            viewTableField.setValue(selectedForm.getViewTable() != null ? selectedForm.getViewTable() : "");
            pkField.setValue(selectedForm.getPrimaryKey() != null ? selectedForm.getPrimaryKey() : "id");
            labelWidthField.setValue(selectedForm.getLabelWidth() != null ? selectedForm.getLabelWidth() : "150px");
            defaultSortField
                    .setValue(selectedForm.getDefaultSortField() != null ? selectedForm.getDefaultSortField() : "");
            defaultSortDirection.setValue(
                    selectedForm.getDefaultSortDirection() != null ? selectedForm.getDefaultSortDirection() : "ASC");
            detailTableNameField
                    .setValue(selectedForm.getDetailTableName() != null ? selectedForm.getDetailTableName() : "");
            detailPkField
                    .setValue(selectedForm.getDetailPrimaryKey() != null ? selectedForm.getDetailPrimaryKey() : "id");
            detailFkField
                    .setValue(selectedForm.getDetailForeignKey() != null ? selectedForm.getDetailForeignKey() : "");

            if (dynamicDataService != null && dynamicDataService.getFormActionMetaRepository() != null) {
                assignedActionsCombo.setItems(dynamicDataService.getFormActionMetaRepository().findAll());
                if (selectedForm.getExtraToolbars() != null && !selectedForm.getExtraToolbars().trim().isEmpty()) {
                    java.util.Set<com.vaadinerp.meta.FormActionMeta> selectedSet = new java.util.HashSet<>();
                    for (String code : selectedForm.getExtraToolbars().split(",")) {
                        String clean = code.trim();
                        com.vaadinerp.meta.FormActionMeta act = dynamicDataService.getFormActionMetaRepository()
                                .findByActionCode(clean);
                        if (act != null)
                            selectedSet.add(act);
                    }
                    assignedActionsCombo.setValue(selectedSet);
                } else {
                    assignedActionsCombo.clear();
                }
            }

            // Populate fields list
            fieldsList.clear();
            if (selectedForm.getFields() != null) {
                for (FieldMeta field : selectedForm.getFields()) {
                    FieldMetaTemp temp = new FieldMetaTemp();
                    temp.fieldName = field.getFieldName();
                    temp.fieldLabel = field.getFieldLabel();
                    temp.componentType = field.getComponentType();
                    temp.lovCode = field.getLovCode();
                    temp.rowGroup = field.getRowGroup() != null ? field.getRowGroup() : 1;
                    temp.colSpan = field.getColSpan() != null ? field.getColSpan() : 1;
                    temp.isRequired = field.isRequired();
                    temp.isReadonly = field.isReadonly();
                    temp.showInGrid = field.isShowInGrid();
                    temp.hideInForm = field.isHideInForm();
                    temp.isDetail = field.isDetail();
                    temp.isSortable = field.isSortable();
                    temp.formula = field.getFormula();
                    temp.displayFormat = field.getDisplayFormat();
                    temp.validationRule = field.getValidationRule();
                    temp.sequenceCode = field.getSequenceCode();
                    temp.saveOnInsert = field.isSaveOnInsert();
                    temp.saveOnUpdate = field.isSaveOnUpdate();
                    temp.isAuditLog = field.isAuditLog();
                    temp.onAddScript = field.getOnAddScript();

                    // Load filters
                    temp.filters = new ArrayList<>();
                    if (field.getFilters() != null) {
                        for (FieldFilterMeta filter : field.getFilters()) {
                            FieldFilterMetaTemp fTemp = new FieldFilterMetaTemp();
                            fTemp.filterColumn = filter.getFilterColumn();
                            fTemp.sourceType = filter.getSourceType();
                            fTemp.sourceName = filter.getSourceName();
                            fTemp.logicalOperator = filter.getLogicalOperator() != null ? filter.getLogicalOperator()
                                    : "AND";
                            fTemp.comparisonOperator = filter.getComparisonOperator() != null
                                    ? filter.getComparisonOperator()
                                    : "=";
                            temp.filters.add(fTemp);
                        }
                    }

                    // Load LOV Targets
                    temp.lovTargets = new ArrayList<>();
                    if (field.getLovTargets() != null) {
                        for (com.vaadinerp.meta.FieldLovTargetMeta target : field.getLovTargets()) {
                            FieldLovTargetMetaTemp tTemp = new FieldLovTargetMetaTemp();
                            tTemp.sourceColumn = target.getSourceColumn();
                            tTemp.targetField = target.getTargetField();
                            tTemp.actionType = target.getActionType();
                            tTemp.lookupColumn = target.getLookupColumn();
                            temp.lovTargets.add(tTemp);
                        }
                    }
                    fieldsList.add(temp);
                }
            }
            selectField(null);
            rebuildCanvas();
        }
    }

    private void openLovConfigDialog(String lovCode) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(lovCode == null ? "Create New LOV Config" : "Edit LOV Config: " + lovCode);
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        TextField codeField = new TextField("LOV Code");
        codeField.setWidthFull();
        if (lovCode != null) {
            codeField.setValue(lovCode);
            codeField.setReadOnly(true);
        }

        TextField nameField = new TextField("LOV Name");
        nameField.setWidthFull();
        TextField tableField = new TextField("Table Name / Query");
        tableField.setWidthFull();
        TextField valueColField = new TextField("Value Column");
        valueColField.setWidthFull();
        TextField labelColField = new TextField("Label Column");
        labelColField.setWidthFull();
        TextField searchColField = new TextField("Search Column(s)");
        searchColField.setWidthFull();
        TextArea gridColsField = new TextArea("Grid Columns Configuration");
        gridColsField.setWidthFull();
        gridColsField.setHeight("80px");

        tableField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        tableField.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null && !e.getValue().trim().isEmpty()) {
                java.util.List<String> columns = dynamicDataService.getColumnsForQueryOrTable(e.getValue().trim());
                if (!columns.isEmpty()) {
                    if (valueColField.isEmpty())
                        valueColField.setValue(columns.get(0));
                    if (labelColField.isEmpty()) {
                        String label = columns.get(0);
                        for (String col : columns) {
                            if (col.toLowerCase().contains("name") || col.toLowerCase().contains("title")
                                    || col.toLowerCase().contains("desc")) {
                                label = col;
                                break;
                            }
                        }
                        labelColField.setValue(label);
                    }
                }
            }
        });

        final LovMeta existing = (lovCode != null) ? lovMetaRepository.findById(lovCode).orElse(null) : null;
        if (existing != null) {
            nameField.setValue(existing.getLovName() != null ? existing.getLovName() : "");
            tableField.setValue(existing.getTableName() != null ? existing.getTableName() : "");
            valueColField.setValue(existing.getValueColumn() != null ? existing.getValueColumn() : "");
            labelColField.setValue(existing.getLabelColumn() != null ? existing.getLabelColumn() : "");
            searchColField.setValue(existing.getSearchColumn() != null ? existing.getSearchColumn() : "");
            gridColsField.setValue(existing.getGridColumns() != null ? existing.getGridColumns() : "");
        } else if (lovCode != null) {
            FormMeta formFallback = formMetaRepository.findById(lovCode).orElse(null);
            if (formFallback != null) {
                nameField.setValue(formFallback.getFormTitle() != null ? formFallback.getFormTitle() : "");
                tableField.setValue(formFallback.getViewTable() != null ? formFallback.getViewTable()
                        : (formFallback.getTableName() != null ? formFallback.getTableName() : ""));
                String pk = formFallback.getPrimaryKey() != null ? formFallback.getPrimaryKey() : "id";
                valueColField.setValue(pk);
                labelColField.setValue(pk);
                searchColField.setValue(pk);
                gridColsField.setValue(pk + ":Kode:150px");
            }
        }

        layout.add(codeField, nameField, tableField, new HorizontalLayout(valueColField, labelColField) {
            {
                setWidthFull();
            }
        }, searchColField, gridColsField);
        dialog.add(layout);

        Button btnSave = new Button("Simpan", VaadinIcon.DOWNLOAD.create());
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSave.addClickListener(e -> {
            String newCode = codeField.getValue().trim();
            if (newCode.isEmpty()) {
                Notification.show("LOV Code tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }

            LovMeta meta = lovCode != null && existing != null ? existing : new LovMeta();
            meta.setLovCode(newCode);
            meta.setLovName(nameField.getValue().trim());
            meta.setTableName(tableField.getValue().trim());
            meta.setValueColumn(valueColField.getValue().trim());
            meta.setLabelColumn(labelColField.getValue().trim());
            meta.setSearchColumn(searchColField.getValue().trim());
            meta.setGridColumns(gridColsField.getValue().trim());

            try {
                lovMetaRepository.save(meta);
                Notification.show("LOV " + newCode + " berhasil disimpan!", 3000, Notification.Position.TOP_CENTER);
                propLovCode.setValue(newCode);
                if (selectedField != null) {
                    selectedField.lovCode = newCode;
                }
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan LOV: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        Button btnCancel = new Button("Batal", e -> dialog.close());

        dialog.getFooter().add(btnCancel, btnSave);
        dialog.open();
    }

    private void openAutoGenerateDialog() {
        java.util.List<String> availableTables = new ArrayList<>();
        if (tableNameField.getValue() != null && !tableNameField.getValue().trim().isEmpty()) {
            availableTables.add(tableNameField.getValue().trim() + " (Target Table)");
        }
        if (viewTableField.getValue() != null && !viewTableField.getValue().trim().isEmpty()) {
            availableTables.add(viewTableField.getValue().trim() + " (View Table)");
        }
        if (detailTableNameField.isVisible() && detailTableNameField.getValue() != null && !detailTableNameField.getValue().trim().isEmpty()) {
            availableTables.add(detailTableNameField.getValue().trim() + " (Detail Table)");
        }
        if (availableTables.isEmpty()) {
            Notification.show("Silakan isi Target Table Name, View Table, atau Detail Table Name terlebih dahulu!", 4000, Notification.Position.MIDDLE);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚡ Auto-Generate Components dari Database");
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Select<String> tableSelect = new Select<>();
        tableSelect.setLabel("Pilih Tabel / View Sumber:");
        tableSelect.setItems(availableTables);
        tableSelect.setValue(availableTables.get(0));
        tableSelect.setWidthFull();

        Select<String> layoutSelect = new Select<>();
        layoutSelect.setLabel("Pilih Mode Layout Tampilan:");
        layoutSelect.setItems(
                "Standar Form (1 Kolom / Vertikal)",
                "Standar Form (2 Kolom / Berjejer)",
                "Standar Form (3 Kolom / Berjejer)",
                "Standar Form (4 Kolom / Berjejer)",
                "Standar Form (5 Kolom / Berjejer)",
                "Standar Form (6 Kolom / Berjejer)",
                "Standar Form (8 Kolom / Berjejer)",
                "Standar Form (10 Kolom / Berjejer)",
                "Standar Form (12 Kolom / Berjejer)",
                "Memanjang Horizontal (Inline / Grid-Like)",
                "Custom (Input Jumlah Kolom Berjejer...)"
        );
        layoutSelect.setValue("Standar Form (2 Kolom / Berjejer)");
        layoutSelect.setWidthFull();

        com.vaadin.flow.component.textfield.IntegerField customColsInput = new com.vaadin.flow.component.textfield.IntegerField("Jumlah Kolom Custom (Berjejer):");
        customColsInput.setValue(3);
        customColsInput.setMin(1);
        customColsInput.setMax(20);
        customColsInput.setWidthFull();
        customColsInput.setVisible(false);

        layoutSelect.addValueChangeListener(e -> {
            customColsInput.setVisible("Custom (Input Jumlah Kolom Berjejer...)".equals(e.getValue()));
        });

        Checkbox chkIsDetail = new Checkbox("Jadikan sebagai Kolom Detail Grid (Master-Detail)", false);
        chkIsDetail.setValue(availableTables.get(0).contains("(Detail Table)"));

        Checkbox chkExcludeAudit = new Checkbox("Sembunyikan / Lewati Kolom Audit Default (inputby, inputdt, updateby, updatedt, dll)", true);

        tableSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                chkIsDetail.setValue(e.getValue().contains("(Detail Table)"));
            }
        });

        Span infoSpan = new Span("💡 Kolom yang sudah ada di kanvas dan kolom audit default tidak akan digenerate.");
        infoSpan.getStyle().set("color", "#64748b").set("font-size", "0.85rem");

        layout.add(tableSelect, layoutSelect, customColsInput, chkIsDetail, chkExcludeAudit, infoSpan);
        dialog.add(layout);

        Button btnGenerate = new Button("Generate", VaadinIcon.DATABASE.create());
        btnGenerate.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGenerate.addClickListener(e -> {
            String selected = tableSelect.getValue();
            if (selected == null || selected.isEmpty()) return;
            String tableName = selected;
            if (tableName.endsWith(" (Target Table)")) {
                tableName = tableName.substring(0, tableName.length() - 15).trim();
            } else if (tableName.endsWith(" (View Table)")) {
                tableName = tableName.substring(0, tableName.length() - 13).trim();
            } else if (tableName.endsWith(" (Detail Table)")) {
                tableName = tableName.substring(0, tableName.length() - 15).trim();
            } else if (tableName.contains(" (")) {
                tableName = tableName.substring(0, tableName.lastIndexOf(" (")).trim();
            }

            java.util.List<Map<String, Object>> schemaDetails = dynamicDataService.fetchTableSchemaDetails(tableName);
            if (schemaDetails.isEmpty()) {
                Notification.show("Tabel '" + tableName + "' tidak ditemukan atau tidak memiliki kolom di database!", 4000, Notification.Position.MIDDLE);
                dialog.close();
                return;
            }

            java.util.Set<String> existingFields = fieldsList.stream()
                    .map(f -> f.getFieldName().toLowerCase())
                    .collect(Collectors.toSet());

            int addedCount = 0;
            int currentGroup = 1;
            int colInGroup = 0;

            for (FieldMetaTemp f : fieldsList) {
                if (f.getRowGroup() >= currentGroup) {
                    currentGroup = f.getRowGroup();
                }
            }
            for (FieldMetaTemp f : fieldsList) {
                if (f.getRowGroup() == currentGroup) {
                    colInGroup++;
                }
            }

            String layoutMode = layoutSelect.getValue();
            boolean isDetailMode = chkIsDetail.getValue();
            boolean excludeAudit = chkExcludeAudit.getValue();
            int targetCols = 2;
            if ("Custom (Input Jumlah Kolom Berjejer...)".equals(layoutMode)) {
                targetCols = (customColsInput.getValue() != null && customColsInput.getValue() > 0) ? customColsInput.getValue() : 2;
            } else if (layoutMode != null && layoutMode.startsWith("Standar Form (")) {
                try {
                    String numStr = layoutMode.substring("Standar Form (".length(), layoutMode.indexOf(" Kolom"));
                    targetCols = Integer.parseInt(numStr.trim());
                } catch (Exception ignored) {
                    targetCols = 2;
                }
            }

            for (Map<String, Object> col : schemaDetails) {
                String colName = (String) col.get("column_name");
                if (colName == null || colName.trim().isEmpty()) continue;
                if (existingFields.contains(colName.toLowerCase())) {
                    continue;
                }
                if (excludeAudit && isAuditField(colName)) {
                    continue;
                }

                String dataType = (String) col.get("data_type");
                if (dataType == null) dataType = "varchar";
                dataType = dataType.toLowerCase();

                FieldMetaTemp temp = new FieldMetaTemp();
                temp.fieldName = colName;
                temp.fieldLabel = formatLabelFromColumnName(colName);
                temp.componentType = mapDataTypeToComponentType(dataType);
                temp.showInGrid = !"SUBFORM_GRID".equalsIgnoreCase(temp.componentType);
                temp.isSortable = true;
                temp.saveOnInsert = true;
                temp.saveOnUpdate = true;

                if (isDetailMode) {
                    temp.isDetail = true;
                    temp.rowGroup = 1;
                } else if ("Memanjang Horizontal (Inline / Grid-Like)".equals(layoutMode)) {
                    temp.rowGroup = 1;
                } else {
                    if (colInGroup >= targetCols) {
                        currentGroup++;
                        colInGroup = 0;
                    }
                    temp.rowGroup = currentGroup;
                    colInGroup++;
                }

                if ("id".equalsIgnoreCase(colName) || (pkField.getValue() != null && pkField.getValue().equalsIgnoreCase(colName))) {
                    temp.isReadonly = true;
                }
                if (col.get("is_nullable") != null && "NO".equalsIgnoreCase(col.get("is_nullable").toString()) && !temp.isReadonly) {
                    temp.isRequired = true;
                }

                fieldsList.add(temp);
                existingFields.add(colName.toLowerCase());
                addedCount++;
            }

            if (addedCount > 0) {
                rebuildCanvas();
                Notification.show("Berhasil menambahkan " + addedCount + " komponen baru dari tabel " + tableName + "!", 3000, Notification.Position.BOTTOM_END);
            } else {
                Notification.show("Semua kolom dari tabel " + tableName + " sudah ada di kanvas (tidak ada kolom baru).", 3000, Notification.Position.MIDDLE);
            }
            dialog.close();
        });

        Button btnCancelDialog = new Button("Batal", e -> dialog.close());
        dialog.getFooter().add(btnCancelDialog, btnGenerate);
        dialog.open();
    }

    private void openRelayoutDialog() {
        if (fieldsList.isEmpty()) {
            Notification.show("Belum ada kolom di kanvas untuk diatur layout-nya!", 3000, Notification.Position.MIDDLE);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🔀 Atur Ulang Layout Kolom Form (Berjejer)");
        dialog.setWidth("450px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Select<Integer> colsSelect = new Select<>();
        colsSelect.setLabel("Jumlah Kolom Berjejer per Baris:");
        colsSelect.setItems(1, 2, 3, 4, 5, 6, 8, 10, 12);
        colsSelect.setValue(3);
        colsSelect.setWidthFull();

        Span infoSpan = new Span("💡 Sistem akan mengurutkan ulang kolom form yang ada di kanvas ke dalam " + colsSelect.getValue() + " kolom berjejer per baris. Kolom detail tabel (jika ada) tidak diubah.");
        infoSpan.getStyle().set("color", "#64748b").set("font-size", "0.85rem");

        colsSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                infoSpan.setText("💡 Sistem akan mengurutkan ulang kolom form yang ada di kanvas ke dalam " + e.getValue() + " kolom berjejer per baris. Kolom detail tabel (jika ada) tidak diubah.");
            }
        });

        layout.add(colsSelect, infoSpan);
        dialog.add(layout);

        Button btnApply = new Button("Terapkan Layout", VaadinIcon.CHECK.create());
        btnApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnApply.addClickListener(e -> {
            int targetCols = colsSelect.getValue() != null ? colsSelect.getValue() : 2;
            int currentGroup = 1;
            int colInGroup = 0;
            for (FieldMetaTemp f : fieldsList) {
                if (f.isDetail) continue;
                if (colInGroup >= targetCols) {
                    currentGroup++;
                    colInGroup = 0;
                }
                f.rowGroup = currentGroup;
                colInGroup++;
            }
            rebuildCanvas();
            Notification.show("Layout berhasil diatur menjadi " + targetCols + " kolom berjejer!", 3000, Notification.Position.TOP_CENTER);
            dialog.close();
        });

        Button btnCancel = new Button("Batal", e -> dialog.close());
        dialog.getFooter().add(btnCancel, btnApply);
        dialog.open();
    }

    private boolean isAuditField(String colName) {
        if (colName == null) return false;
        String lower = colName.toLowerCase().trim();
        return lower.equals("inputby") || lower.equals("inputdt") || lower.equals("input_by") || lower.equals("input_date") ||
               lower.equals("updateby") || lower.equals("updatedt") || lower.equals("update_by") || lower.equals("update_date") ||
               lower.equals("created_by") || lower.equals("created_at") || lower.equals("created_date") ||
               lower.equals("updated_by") || lower.equals("updated_at") || lower.equals("updated_date") ||
               lower.equals("modified_by") || lower.equals("modified_at") || lower.equals("modified_date") ||
               lower.equals("is_deleted") || lower.equals("delete_by") || lower.equals("delete_date") ||
               lower.equals("user_input") || lower.equals("date_input") || lower.equals("user_update") || lower.equals("date_update") ||
               lower.equals("version");
    }

    private String formatLabelFromColumnName(String colName) {
        if (colName == null) return "";
        String[] parts = colName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private String mapDataTypeToComponentType(String dataType) {
        if (dataType == null) return "TEXTBOX";
        if (dataType.contains("bool")) return "CHECKBOX";
        if (dataType.contains("int") || dataType.contains("serial")) return "INTBOX";
        if (dataType.contains("num") || dataType.contains("dec") || dataType.contains("real") || dataType.contains("double") || dataType.contains("float") || dataType.contains("money")) return "DECIMALBOX";
        if (dataType.contains("time") && !dataType.contains("date") && !dataType.contains("stamp")) return "TIMEBOX";
        if (dataType.contains("timestamp") || dataType.contains("timestamptz")) return "DATETIMEBOX";
        if (dataType.contains("date")) return "DATEBOX";
        if (dataType.equals("text") || dataType.contains("character varying") || dataType.contains("varchar")) {
            return "TEXTBOX";
        }
        return "TEXTBOX";
    }
}
