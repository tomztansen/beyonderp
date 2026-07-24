package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
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
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.timepicker.TimePicker;
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
    private String selectedFormCode = null;
    private FieldMetaTemp draggedField = null;
    private String draggedPaletteType = null;
    private String draggedSubformPaletteType = null;

    private List<FieldMetaTemp> fieldsList = new ArrayList<>();

    // Main Layout Components
    private final VerticalLayout canvas = new VerticalLayout();
    private final com.vaadin.flow.component.grid.Grid<FieldMetaTemp> listCanvas = new com.vaadin.flow.component.grid.Grid<>(FieldMetaTemp.class, false);
    private final VerticalLayout canvasPanel = new VerticalLayout();
    private final ComboBox<FieldMetaTemp> canvasSearchField = new ComboBox<>();
    private final Button toggleViewBtn = new Button(VaadinIcon.TABLE.create());
    private boolean isListView = false;

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
    private final TextField propFieldWidth = new TextField("Field Width (e.g. 50%, 100px)");
    private final ComboBox<String> propReadonlyMode = new ComboBox<>("Read-only Mode");
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
        public String fieldName;
        public String fieldLabel;
        public String componentType;
        public String lovCode;
        public int rowGroup = 1;
        public int colIndex = 1;
        public Integer colSpan;
        public String fieldWidth = "100%";
        public boolean isRequired;
        public boolean isReadonly;
        public String readonlyMode = "NONE";
        public boolean showInGrid = true;
        public boolean hideInForm;
        public boolean isDetail;
        public boolean isSortable = true;
        public String formula;
        public String validationRule;
        public String displayFormat;
        public String sequenceCode;
        public boolean saveOnInsert = true;
        public boolean saveOnUpdate = true;
        public boolean isAuditLog;
        public String onAddScript;
        public List<FieldFilterMetaTemp> filters = new ArrayList<>();
        public List<FieldLovTargetMetaTemp> lovTargets = new ArrayList<>();

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
    private final java.util.Map<String, TextField> filterFields = new java.util.HashMap<>();
    private java.util.List<FormMeta> allHistoryItems = new java.util.ArrayList<>();

    @org.springframework.beans.factory.annotation.Autowired
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
                        "      position: relative;" +
                        "      display: flex;" +
                        "      align-items: center;" +
                        "      justify-content: center;" +
                        "      height: 4px;" +
                        "      background-color: transparent;" +
                        "      border-radius: 4px;" +
                        "      transition: all 0.15s ease;" +
                        "      margin: 3px 0;" +
                        "      overflow: hidden;" +
                        "    }" +
                        "    .dragging-active .row-drop-zone {" +
                        "      background-color: #f1f5f9;" +
                        "      border: 1px dashed #94a3b8;" +
                        "      height: 14px;" +
                        "    }" +
                        "    .row-drop-zone::after {" +
                        "      content: attr(data-label);" +
                        "      font-size: 11px;" +
                        "      font-weight: 700;" +
                        "      color: #ffffff;" +
                        "      letter-spacing: 0.3px;" +
                        "      opacity: 0;" +
                        "      transition: opacity 0.15s ease;" +
                        "      pointer-events: none;" +
                        "      white-space: nowrap;" +
                        "      text-shadow: 0 1px 2px rgba(0,0,0,0.3);" +
                        "    }" +
                        "    .row-drop-zone.drag-over {" +
                        "      background: linear-gradient(90deg, #4f46e5, #6366f1, #4f46e5) !important;" +
                        "      height: 26px !important;" +
                        "      border: 1px solid #818cf8 !important;" +
                        "      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4) !important;" +
                        "    }" +
                        "    .row-drop-zone.drag-over::after {" +
                        "      opacity: 1;" +
                        "    }" +
                        "    .field-card {" +
                        "      position: relative;" +
                        "    }" +
                        "    .field-card.card-drag-over::before {" +
                        "      content: attr(data-card-label);" +
                        "      position: absolute;" +
                        "      top: -12px;" +
                        "      right: 8px;" +
                        "      background: #4f46e5;" +
                        "      color: #ffffff;" +
                        "      font-size: 10px;" +
                        "      font-weight: 700;" +
                        "      padding: 3px 10px;" +
                        "      border-radius: 12px;" +
                        "      box-shadow: 0 2px 8px rgba(79, 70, 229, 0.45);" +
                        "      z-index: 100;" +
                        "      pointer-events: none;" +
                        "      white-space: nowrap;" +
                        "      animation: fadeInBadge 0.15s ease;" +
                        "    }" +
                        "    @keyframes fadeInBadge {" +
                        "      from { opacity: 0; transform: translateY(4px); }" +
                        "      to { opacity: 1; transform: translateY(0); }" +
                        "    }" +
                        "  `;" +
                        "  document.head.appendChild(style);" +
                        "}");

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("");
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
        Grid.Column<FormMeta> codeCol = historyGrid.addColumn(FormMeta::getFormCode).setHeader("Form Code")
                .setSortable(true).setAutoWidth(true).setKey("formCode");
        Grid.Column<FormMeta> titleCol = historyGrid.addColumn(FormMeta::getFormTitle).setHeader("Judul Form")
                .setSortable(true).setAutoWidth(true).setKey("formTitle");
        Grid.Column<FormMeta> typeCol = historyGrid.addColumn(FormMeta::getFormType).setHeader("Form Type")
                .setSortable(true).setAutoWidth(true).setKey("formType");
        Grid.Column<FormMeta> tableCol = historyGrid.addColumn(FormMeta::getTableName).setHeader("Table Name")
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
            filterFields.put(fieldName, filterField);
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
        historisTab = tabSheet.add("History", historisLayout);

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
                tableNameField.setPlaceholder("Leave blank for virtual form/view");
            }
        });
        tableNameField.setRequiredIndicatorVisible(false);
        tableNameField.setPlaceholder("Leave blank for virtual form/view");

        Button btnAutoGenerateFields = new Button("⚡ Auto-Generate Fields dari Tabel/View",
                VaadinIcon.DATABASE.create());
        btnAutoGenerateFields.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnAutoGenerateFields.addClickListener(e -> openAutoGenerateDialog());

        Button btnRelayoutCanvas = new Button("🔀 Atur Ulang Layout Berjejer (3 Kolom, 4 Kolom, dst)",
                VaadinIcon.GRID_BIG_O.create());
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
        // We defer layouting the workspace to after panel creations

        // COLUMN A: PALETTE
        VerticalLayout palettePanel = new VerticalLayout();
        palettePanel.setWidthFull(); // handled by splitlayout
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
        canvasPanel.setHeightFull();
        canvasPanel.setHeightFull();
        canvasPanel.getStyle().set("flex-grow", "1")
                .set("background-color", "#f8fafc")
                .set("background-image",
                        "linear-gradient(to right, #e2e8f0 1px, transparent 1px), linear-gradient(to bottom, #e2e8f0 1px, transparent 1px)")
                .set("background-size", "20px 20px")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("overflow-y", "auto")
                .set("position", "relative");

        H4 canvasTitle = new H4("Kanvas Desain Form (WYSIWYG)");
        canvasTitle.getStyle().set("margin-top", "0").set("margin-bottom", "0");

        Button togglePaletteBtn = new Button(VaadinIcon.MENU.create());
        togglePaletteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        togglePaletteBtn.addClickListener(e -> palettePanel.setVisible(!palettePanel.isVisible()));

        Button togglePropsBtn = new Button(VaadinIcon.COG_O.create());
        togglePropsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        togglePropsBtn.addClickListener(e -> propertiesPanel.setVisible(!propertiesPanel.isVisible()));

        Button fullscreenBtn = new Button(VaadinIcon.EXPAND_FULL.create());
        fullscreenBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        fullscreenBtn.setTooltipText("Toggle Full Screen Canvas");
        fullscreenBtn.addClickListener(e -> {
            canvasPanel.getElement().executeJs(
                    "if (!document.fullscreenElement) {" +
                            "  this.requestFullscreen().catch(err => console.log(err));" +
                            "} else {" +
                            "  if (document.exitFullscreen) document.exitFullscreen();" +
                            "}");
        });

        canvasSearchField.setPlaceholder("Jump to field...");
        canvasSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        canvasSearchField.setClearButtonVisible(true);
        canvasSearchField.setItemLabelGenerator(f -> "[" + f.componentType + "] " + f.fieldName + " (" + f.fieldLabel + ")");
        canvasSearchField.setWidth("300px");
        canvasSearchField.addValueChangeListener(e -> {
            if (!e.isFromClient()) return;
            
            FieldMetaTemp selected = e.getValue();
            if (selected != null) {
                selectField(selected);
            }
            if (isListView) {
                // Not filtering list view using combobox anymore
            } else {
                rebuildCanvas(); // Rebuild to apply highlights
            }
        });

        toggleViewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        toggleViewBtn.setTooltipText("Toggle List/Visual View");
        toggleViewBtn.addClickListener(e -> {
            isListView = !isListView;
            toggleViewBtn.setIcon(isListView ? VaadinIcon.PAINTBRUSH.create() : VaadinIcon.TABLE.create());
            canvas.setVisible(!isListView);
            listCanvas.setVisible(isListView);
            if (isListView) {
                refreshListCanvas();
            }
        });

        HorizontalLayout rightHeaderButtons = new HorizontalLayout(canvasSearchField, toggleViewBtn, fullscreenBtn, togglePropsBtn);
        rightHeaderButtons.setSpacing(true);
        rightHeaderButtons.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout canvasHeader = new HorizontalLayout(togglePaletteBtn, canvasTitle, rightHeaderButtons);
        canvasHeader.setWidthFull();
        canvasHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        canvasHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        canvasHeader.getStyle().set("margin-bottom", "15px");
        canvasPanel.add(canvasHeader);

        setupListCanvas();
        listCanvas.setVisible(false);
        canvasPanel.add(listCanvas);

        canvas.setWidthFull();
        canvas.setPadding(false);
        canvas.setSpacing(true);
        canvasPanel.add(canvas);

        // COLUMN C: PROPERTIES EDITOR
        propertiesPanel.setWidthFull(); // handled by splitlayout
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

        SplitLayout leftSplit = new SplitLayout(palettePanel, canvasPanel);
        leftSplit.setSplitterPosition(15);
        leftSplit.setSizeFull();

        SplitLayout mainSplit = new SplitLayout(leftSplit, propertiesPanel);
        mainSplit.setSplitterPosition(75);
        mainSplit.setSizeFull();
        mainSplit.getStyle().set("margin-top", "15px");

        Details formMetaDetails = new Details("Konfigurasi Utama Form (Klik untuk Menyembunyikan/Menampilkan)",
                formMetaLayout);
        formMetaDetails.setOpened(true);
        formMetaDetails.setWidthFull();
        formMetaDetails.getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "0 15px")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px");

        transaksiLayout.add(formMetaDetails, mainSplit);
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

        com.vaadin.flow.component.dnd.DragSource<Button> dragSource = com.vaadin.flow.component.dnd.DragSource.create(btn);
        dragSource.setDraggable(true);
        dragSource.addDragStartListener(e -> {
            draggedPaletteType = type;
        });
        dragSource.addDragEndListener(e -> {
            draggedPaletteType = null;
        });

        return btn;
    }

    private FieldMetaTemp createFieldMetaTemp(String type) {
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
        temp.colSpan = "SUBFORM_GRID".equalsIgnoreCase(type) ? null : 1;
        temp.isRequired = false;
        temp.isReadonly = false;
        temp.showInGrid = !"SUBFORM_GRID".equalsIgnoreCase(type);
        temp.isSortable = true;
        return temp;
    }

    private void addPaletteComponent(String type) {
        FieldMetaTemp temp = createFieldMetaTemp(type);
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
        propLovCode.setGridConfigurator(grid -> {
            grid.addColumn(row -> row.get("code") != null ? row.get("code").toString() : "")
                    .setHeader("Code").setWidth("150px");
            grid.addColumn(row -> row.get("name") != null ? row.get("name").toString() : "")
                    .setHeader("Name").setWidth("250px");
            grid.addColumn(row -> row.get("type") != null ? row.get("type").toString() : "")
                    .setHeader("Type").setWidth("150px");
        });

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

        propReadonlyMode.setItems("NONE", "EDIT", "ADD", "EDIT_AND_ADD");
        propReadonlyMode.setItemLabelGenerator(item -> switch (item) {
            case "NONE" -> "Bisa Add & Edit (Normal)";
            case "EDIT" -> "Read-only saat Edit saja";
            case "ADD" -> "Read-only saat Add saja";
            case "EDIT_AND_ADD" -> "Selalu Read-only (Edit & Add)";
            default -> item != null ? item : "Normal";
        });
        propReadonlyMode.setClearButtonVisible(false);

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
        propValidationRule.setPlaceholder("Select / Type rule...");

        propSequenceCode.setAllowCustomValue(true);
        propSequenceCode.addCustomValueSetListener(e -> propSequenceCode.setValue(e.getDetail()));
        propSequenceCode.setPlaceholder("Select / Type sequence code...");
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

        propertiesForm.add(propFieldName, propFieldLabel, propComponentType, propLovCode, propBtnEditLov,
                propBtnFilters, propBtnLovTargets, propRowGroup, propColSpan, propFieldWidth, propReadonlyMode,
                propFormula, propDisplayFormat, propValidationRule, propSequenceCode, propBtnCustomValidation,
                checkBoxLayout,
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
                selectedField.colSpan = e.getValue() != null && e.getValue() > 0 ? e.getValue() : null;
                rebuildCanvas();
            }
        });
        propFieldWidth.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        propFieldWidth.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.fieldWidth = e.getValue();
                rebuildCanvas();
            }
        });
        propReadonlyMode.addValueChangeListener(e -> {
            if (selectedField != null && (!isSelectingField || e.isFromClient())) {
                String val = e.getValue();
                selectedField.readonlyMode = (val == null || val.trim().isEmpty()) ? "NONE" : val;
                selectedField.isReadonly = "EDIT_AND_ADD".equalsIgnoreCase(selectedField.readonlyMode);
                if (e.isFromClient()) {
                    propIsReadonly.setValue(selectedField.isReadonly);
                }
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
                selectedField.readonlyMode = e.getValue() ? "EDIT_AND_ADD" : "NONE";
                propReadonlyMode.setValue(selectedField.readonlyMode);
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
                propColSpan.setValue(temp.colSpan != null && temp.colSpan > 0 ? temp.colSpan : null);
                propFieldWidth.setValue(temp.fieldWidth != null ? temp.fieldWidth : "100%");
                propIsRequired.setValue(temp.isRequired);
                propIsReadonly.setValue(temp.isReadonly);
                String rm = temp.readonlyMode;
                if (rm == null || rm.trim().isEmpty() || "DEFAULT".equalsIgnoreCase(rm)) {
                    rm = temp.isReadonly ? "EDIT_AND_ADD" : "NONE";
                }
                propReadonlyMode.setValue(rm);
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
        
        // Preserve current selection if any
        FieldMetaTemp currentSearch = canvasSearchField.getValue();
        canvasSearchField.setItems(fieldsList);
        if (currentSearch != null && fieldsList.contains(currentSearch)) {
            canvasSearchField.setValue(currentSearch);
        }

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
                int maxColsDetail = com.vaadinerp.components.FormLayoutUtils.calculateMaxColsInFormTemp(detailFields);
                com.vaadinerp.components.FormLayoutUtils.RowLayoutConfigTemp rowConfig = com.vaadinerp.components.FormLayoutUtils
                        .calculateRowConfigTemp(detailFields, maxColsDetail);
                int cols = rowConfig.getCols();
                com.vaadinerp.components.FormLayoutUtils.applyResponsiveSteps(detailRowLayout, cols);

                int seq = 1;
                for (FieldMetaTemp temp : detailFields) {
                    Component card = buildFieldCard(temp, seq++);
                    detailRowLayout.add(card);
                    int span = rowConfig.getSpan(temp);
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

        Map<Integer, List<FieldMetaTemp>> groups = new HashMap<>();
        for (FieldMetaTemp f : fieldsList) {
            if (!f.isDetail) {
                f.rowGroup = mapping.getOrDefault(f.rowGroup, 1);
                groups.computeIfAbsent(f.rowGroup, k -> new ArrayList<>()).add(f);
            }
        }

        // Fix colIndex
        for (List<FieldMetaTemp> group : groups.values()) {
            group.sort(java.util.Comparator.comparingInt(f -> f.colIndex)); // Preserve existing relative order
            int nextAvail = 1;
            for (FieldMetaTemp f : group) {
                if (f.colIndex < nextAvail) {
                    f.colIndex = nextAvail;
                }
                nextAvail = f.colIndex + 1;
            }
        }
    }

    private Component createRowDropZone(int targetRowGroup) {
        Div zone = new Div();
        zone.addClassName("row-drop-zone");
        zone.setWidthFull();
        zone.getElement().setAttribute("data-label",
                "⬇ Pindahkan sebagai Baris " + targetRowGroup + " (Row Group " + targetRowGroup + ")");

        DropTarget<Div> dropTarget = DropTarget.create(zone);
        dropTarget.setActive(true);

        zone.getElement().addEventListener("dragenter", e -> {
            zone.addClassName("drag-over");
        });

        zone.getElement().addEventListener("dragleave", e -> {
            zone.removeClassName("drag-over");
        });

        dropTarget.addDropListener(e -> {
            boolean droppedFromPalette = false;
            if (draggedPaletteType != null) {
                draggedField = createFieldMetaTemp(draggedPaletteType);
                draggedPaletteType = null;
                droppedFromPalette = true;
                fieldsList.add(draggedField); // Add temporarily so reorder logic finds it
            }

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

    private void setupListCanvas() {
        listCanvas.setWidthFull();
        listCanvas.setHeightFull();
        listCanvas.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES, com.vaadin.flow.component.grid.GridVariant.LUMO_COMPACT);

        com.vaadin.flow.component.grid.Grid.Column<FieldMetaTemp> colName = listCanvas.addColumn(f -> f.fieldName).setHeader("Field Name").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        com.vaadin.flow.component.grid.Grid.Column<FieldMetaTemp> colLabel = listCanvas.addColumn(f -> f.fieldLabel).setHeader("Label").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        com.vaadin.flow.component.grid.Grid.Column<FieldMetaTemp> colType = listCanvas.addColumn(f -> f.componentType).setHeader("Type").setSortable(true).setAutoWidth(true);
        listCanvas.addComponentColumn(f -> {
            com.vaadin.flow.component.textfield.IntegerField rowField = new com.vaadin.flow.component.textfield.IntegerField();
            rowField.setValue(f.rowGroup);
            rowField.setWidth("110px");
            rowField.setStepButtonsVisible(true);
            rowField.addValueChangeListener(e -> {
                if (e.getValue() != null && e.isFromClient()) {
                    f.rowGroup = e.getValue();
                    rebuildCanvas();
                }
            });
            return rowField;
        }).setHeader("Row").setSortable(true).setComparator((a, b) -> Integer.compare(a.rowGroup, b.rowGroup)).setAutoWidth(true);
        
        listCanvas.addComponentColumn(f -> {
            com.vaadin.flow.component.textfield.IntegerField colField = new com.vaadin.flow.component.textfield.IntegerField();
            colField.setValue(f.colIndex);
            colField.setWidth("110px");
            colField.setStepButtonsVisible(true);
            colField.addValueChangeListener(e -> {
                if (e.getValue() != null && e.isFromClient()) {
                    f.colIndex = e.getValue();
                    rebuildCanvas();
                }
            });
            return colField;
        }).setHeader("Col").setSortable(true).setComparator((a, b) -> Integer.compare(a.colIndex, b.colIndex)).setAutoWidth(true);
        listCanvas.addColumn(f -> f.fieldWidth).setHeader("Width").setSortable(true).setAutoWidth(true);

        com.vaadin.flow.component.grid.HeaderRow filterRow = listCanvas.appendHeaderRow();
        
        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Filter...");
        nameFilter.setClearButtonVisible(true);
        nameFilter.setWidthFull();
        nameFilter.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        nameFilter.addValueChangeListener(e -> refreshListCanvas(nameFilter.getValue(), null, null));
        filterRow.getCell(colName).setComponent(nameFilter);

        TextField labelFilter = new TextField();
        labelFilter.setPlaceholder("Filter...");
        labelFilter.setClearButtonVisible(true);
        labelFilter.setWidthFull();
        labelFilter.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        labelFilter.addValueChangeListener(e -> refreshListCanvas(null, labelFilter.getValue(), null));
        filterRow.getCell(colLabel).setComponent(labelFilter);

        TextField typeFilter = new TextField();
        typeFilter.setPlaceholder("Filter...");
        typeFilter.setClearButtonVisible(true);
        typeFilter.setWidthFull();
        typeFilter.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        typeFilter.addValueChangeListener(e -> refreshListCanvas(null, null, typeFilter.getValue()));
        filterRow.getCell(colType).setComponent(typeFilter);

        listCanvas.addItemDoubleClickListener(e -> {
            selectField(e.getItem());
        });
    }

    private String listFilterName = "";
    private String listFilterLabel = "";
    private String listFilterType = "";

    private void refreshListCanvas(String nameF, String labelF, String typeF) {
        if (nameF != null) listFilterName = nameF.trim().toLowerCase();
        if (labelF != null) listFilterLabel = labelF.trim().toLowerCase();
        if (typeF != null) listFilterType = typeF.trim().toLowerCase();

        List<FieldMetaTemp> filteredFields = new ArrayList<>();
        for (FieldMetaTemp f : fieldsList) {
            if (f.isDetail) continue;
            boolean matchName = listFilterName.isEmpty() || (f.fieldName != null && f.fieldName.toLowerCase().contains(listFilterName));
            boolean matchLabel = listFilterLabel.isEmpty() || (f.fieldLabel != null && f.fieldLabel.toLowerCase().contains(listFilterLabel));
            boolean matchType = listFilterType.isEmpty() || (f.componentType != null && f.componentType.toLowerCase().contains(listFilterType));
            
            if (matchName && matchLabel && matchType) {
                filteredFields.add(f);
            }
        }
        listCanvas.setItems(filteredFields);
    }

    private void refreshListCanvas() {
        refreshListCanvas(null, null, null);
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

        int maxColsInForm = com.vaadinerp.components.FormLayoutUtils.calculateMaxColsInFormTemp(targetFields);

        int sequence = 1;
        for (int i = 0; i < numRows; i++) {
            Integer rg = rowGroupsOrder.get(i);
            List<FieldMetaTemp> groupFields = groups.get(rg);
            FormLayout rowLayout = new FormLayout();
            rowLayout.setWidthFull();

            com.vaadinerp.components.FormLayoutUtils.RowLayoutConfigTemp rowConfig = com.vaadinerp.components.FormLayoutUtils
                    .calculateRowConfigTemp(groupFields, maxColsInForm);
            int cols = rowConfig.getCols();
            com.vaadinerp.components.FormLayoutUtils.applyResponsiveSteps(rowLayout, cols);

            groupFields.sort(java.util.Comparator.comparingInt(f -> f.colIndex));

            int currentVisCol = 1;
            for (FieldMetaTemp temp : groupFields) {
                while (currentVisCol < temp.colIndex && currentVisCol <= cols) {
                    Component emptyCard = buildEmptyColumnPlaceholder(rg, currentVisCol);
                    rowLayout.add(emptyCard);
                    currentVisCol++;
                }
                
                Component card = buildFieldCard(temp, sequence++);
                rowLayout.add(card);
                int span = rowConfig.getSpan(temp);
                if (span > 1) {
                    rowLayout.setColspan(card, Math.min(span, Math.max(1, cols - currentVisCol + 1)));
                }
                currentVisCol += span;
            }

            while (currentVisCol <= cols) {
                Component emptyCard = buildEmptyColumnPlaceholder(rg, currentVisCol);
                rowLayout.add(emptyCard);
                currentVisCol++;
            }

            canvas.add(rowLayout);

            // Drop zone after this row
            canvas.add(createRowDropZone(rg + 1));
        }
    }

    private Component buildEmptyColumnPlaceholder(int targetRowGroup, int targetColIndex) {
        VerticalLayout placeholder = new VerticalLayout();
        Span text = new Span("+ Tarik Field ke Sini");
        placeholder.add(text);

        placeholder.getStyle()
                .set("border", "2px dashed #cbd5e1")
                .set("border-radius", "8px")
                .set("background-color", "rgba(255,255,255,0.5)")
                .set("color", "#94a3b8")
                .set("font-weight", "600")
                .set("font-size", "0.85rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("min-height", "118px")
                .set("height", "118px")
                .set("box-sizing", "border-box")
                .set("transition", "all 0.2s ease")
                .set("cursor", "crosshair")
                .set("margin-top", "0")
                .set("align-self", "flex-start")
                .set("padding", "0");

        DropTarget<VerticalLayout> dropTarget = DropTarget.create(placeholder);
        dropTarget.setActive(true);

        placeholder.getElement().addEventListener("dragenter", e -> {
            placeholder.getStyle().set("border-color", "#6366f1");
            placeholder.getStyle().set("background-color", "#e0e7ff");
            placeholder.getStyle().set("color", "#4f46e5");
        });

        placeholder.getElement().addEventListener("dragleave", e -> {
            placeholder.getStyle().set("border-color", "#cbd5e1");
            placeholder.getStyle().set("background-color", "rgba(255,255,255,0.5)");
            placeholder.getStyle().set("color", "#94a3b8");
        });

        dropTarget.addDropListener(e -> {
            placeholder.getStyle().set("border-color", "#cbd5e1");
            placeholder.getStyle().set("background-color", "rgba(255,255,255,0.5)");
            placeholder.getStyle().set("color", "#94a3b8");

            boolean droppedFromPalette = false;
            if (draggedPaletteType != null) {
                draggedField = createFieldMetaTemp(draggedPaletteType);
                draggedPaletteType = null;
                droppedFromPalette = true;
                fieldsList.add(draggedField); // Add temporarily
            }

            if (draggedField != null && !draggedField.isDetail) {
                fieldsList.remove(draggedField);
                draggedField.rowGroup = targetRowGroup;
                draggedField.colIndex = targetColIndex;

                int insertIdx = 0;
                for (int j = 0; j < fieldsList.size(); j++) {
                    FieldMetaTemp f = fieldsList.get(j);
                    if (f.isDetail)
                        continue;
                    if (f.rowGroup < targetRowGroup || (f.rowGroup == targetRowGroup && f.colIndex < targetColIndex)) {
                        insertIdx = j + 1;
                    }
                }
                fieldsList.add(insertIdx, draggedField);

                normalizeRowGroups();
                if (selectedField != null) {
                    propRowGroup.setValue(selectedField.rowGroup);
                }
                canvas.removeClassName("dragging-active");
                rebuildCanvas();
                Notification.show("Field dipindah ke kolom " + targetColIndex + " pada Baris " + targetRowGroup, 2000,
                        Notification.Position.BOTTOM_END);
            }
            draggedField = null;
        });

        return placeholder;
    }

    private Component buildFieldCard(FieldMetaTemp temp, int sequence) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("field-card");
        card.getElement().setAttribute("data-card-label",
                "🎯 Gabung ke Baris " + temp.rowGroup + " (Row Group " + temp.rowGroup + ")");
        card.setPadding(true);
        card.setSpacing(false);

        boolean isSelected = (selectedField == temp);
        boolean isMultiLine = "TEXTAREA".equalsIgnoreCase(temp.componentType)
                || "SUBFORM_GRID".equalsIgnoreCase(temp.componentType);

        FieldMetaTemp searchSelected = canvasSearchField.getValue();
        boolean isSearchActive = searchSelected != null;
        boolean matchesSearch = isSearchActive && searchSelected == temp;
            
        String cardOpacity = "1.0";
        if (temp.hideInForm) cardOpacity = "0.65";
        if (isSearchActive && !matchesSearch) cardOpacity = "0.2";

        card.getStyle()
                .set("border", isSelected ? "2px solid #6366f1" : (isSearchActive && matchesSearch ? "2px solid #eab308" : "1px dashed #cbd5e1"))
                .set("border-radius", "8px")
                .set("background-color", isSelected ? "#f8fafc" : (isSearchActive && matchesSearch ? "#fefce8" : "#ffffff"))
                .set("cursor", "grab")
                .set("position", "relative")
                .set("box-shadow", isSelected ? "0 4px 12px rgba(99, 102, 241, 0.1)" : "none")
                .set("opacity", cardOpacity)
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

        if (temp.readonlyMode != null && !"NONE".equalsIgnoreCase(temp.readonlyMode)
                && !temp.readonlyMode.trim().isEmpty()) {
            String roText = "RO: " + ("EDIT_AND_ADD".equalsIgnoreCase(temp.readonlyMode) ? "ALL" : temp.readonlyMode);
            Span roBadge = new Span(roText);
            roBadge.getStyle()
                    .set("font-size", "0.6rem")
                    .set("font-weight", "700")
                    .set("background-color", "#fef3c7")
                    .set("color", "#d97706")
                    .set("padding", "2px 6px")
                    .set("border-radius", "4px")
                    .set("flex-shrink", "0");
            badgesBox.add(roBadge);
        } else if (temp.isReadonly) {
            Span roBadge = new Span("RO: ALL");
            roBadge.getStyle()
                    .set("font-size", "0.6rem")
                    .set("font-weight", "700")
                    .set("background-color", "#fef3c7")
                    .set("color", "#d97706")
                    .set("padding", "2px 6px")
                    .set("border-radius", "4px")
                    .set("flex-shrink", "0");
            badgesBox.add(roBadge);
        }

        if (temp.isRequired) {
            Span reqBadge = new Span("*Req");
            reqBadge.getStyle()
                    .set("font-size", "0.6rem")
                    .set("font-weight", "700")
                    .set("background-color", "#fee2e2")
                    .set("color", "#dc2626")
                    .set("padding", "2px 6px")
                    .set("border-radius", "4px")
                    .set("flex-shrink", "0");
            badgesBox.add(reqBadge);
        }

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
                .set("flex-shrink", "0")
                .set("position", "relative")
                .set("z-index", "10");
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

        // Hitboxes for Drag and Drop Zones (Left: Insert Before, Center: Swap, Right: Insert After)
        card.getStyle().set("position", "relative");

        Div leftHitbox = new Div();
        leftHitbox.getStyle()
                .set("position", "absolute").set("top", "0").set("bottom", "0").set("left", "0").set("width", "30%")
                .set("z-index", "1");

        Div rightHitbox = new Div();
        rightHitbox.getStyle()
                .set("position", "absolute").set("top", "0").set("bottom", "0").set("right", "0").set("width", "30%")
                .set("z-index", "1");

        Div centerHitbox = new Div();
        centerHitbox.getStyle()
                .set("position", "absolute").set("top", "0").set("bottom", "0").set("left", "30%").set("width", "40%")
                .set("z-index", "1");

        // Forward clicks to card selection
        leftHitbox.addClickListener(e -> selectField(temp));
        rightHitbox.addClickListener(e -> selectField(temp));
        centerHitbox.addClickListener(e -> selectField(temp));

        card.add(leftHitbox, centerHitbox, rightHitbox);

        // Make hitboxes DropTargets
        DropTarget<Div> leftTarget = DropTarget.create(leftHitbox);
        DropTarget<Div> rightTarget = DropTarget.create(rightHitbox);
        DropTarget<Div> centerTarget = DropTarget.create(centerHitbox);

        leftTarget.setActive(true);
        rightTarget.setActive(true);
        centerTarget.setActive(true);

        // Visual Cues
        leftHitbox.getElement().addEventListener("dragenter", e -> {
            card.getStyle().set("border-left", "4px solid #10b981");
            card.getStyle().set("background-color", "#d1fae5");
        });
        leftHitbox.getElement().addEventListener("dragleave", e -> {
            card.getStyle()
                    .set("border-left", "")
                    .set("border", isSelected ? "2px solid #6366f1" : "1px dashed #cbd5e1")
                    .set("background-color", isSelected ? "#f8fafc" : "#ffffff");
        });

        rightHitbox.getElement().addEventListener("dragenter", e -> {
            card.getStyle().set("border-right", "4px solid #10b981");
            card.getStyle().set("background-color", "#d1fae5");
        });
        rightHitbox.getElement().addEventListener("dragleave", e -> {
            card.getStyle()
                    .set("border-right", "")
                    .set("border", isSelected ? "2px solid #6366f1" : "1px dashed #cbd5e1")
                    .set("background-color", isSelected ? "#f8fafc" : "#ffffff");
        });

        centerHitbox.getElement().addEventListener("dragenter", e -> {
            card.getStyle().set("border", "2px dashed #6366f1");
            card.getStyle().set("background-color", "#e0e7ff");
        });
        centerHitbox.getElement().addEventListener("dragleave", e -> {
            card.getStyle()
                    .set("border", isSelected ? "2px solid #6366f1" : "1px dashed #cbd5e1")
                    .set("background-color", isSelected ? "#f8fafc" : "#ffffff");
        });

        // Drop Logic Helper
        com.vaadin.flow.function.SerializableConsumer<Boolean> handleInsert = (isBefore) -> {
            card.getStyle()
                    .set("border-left", "").set("border-right", "")
                    .set("border", isSelected ? "2px solid #6366f1" : "1px dashed #cbd5e1")
                    .set("background-color", isSelected ? "#f8fafc" : "#ffffff");

            boolean droppedFromPalette = false;
            if (draggedPaletteType != null) {
                draggedField = createFieldMetaTemp(draggedPaletteType);
                draggedPaletteType = null;
                droppedFromPalette = true;
                fieldsList.add(draggedField);
            }

            if (draggedField != null && draggedField != temp) {
                draggedField.rowGroup = temp.rowGroup;
                int targetCol = temp.colIndex;
                if (!isBefore) {
                    targetCol += 1;
                }
                draggedField.colIndex = targetCol;
                
                for (FieldMetaTemp f : fieldsList) {
                    if (f != draggedField && f.rowGroup == temp.rowGroup && f.colIndex >= targetCol) {
                        f.colIndex++;
                    }
                }
                if (selectedField != null) {
                    propRowGroup.setValue(selectedField.rowGroup);
                }
                rebuildCanvas();
                Notification.show("Field inserted successfully.", 2000, Notification.Position.BOTTOM_END);
            }
            draggedField = null;
        };

        leftTarget.addDropListener(e -> handleInsert.accept(true));
        rightTarget.addDropListener(e -> handleInsert.accept(false));

        centerTarget.addDropListener(e -> {
            card.getStyle()
                    .set("border", isSelected ? "2px solid #6366f1" : "1px dashed #cbd5e1")
                    .set("background-color", isSelected ? "#f8fafc" : "#ffffff");
            
            boolean droppedFromPalette = false;
            if (draggedPaletteType != null) {
                draggedField = createFieldMetaTemp(draggedPaletteType);
                draggedPaletteType = null;
                droppedFromPalette = true;
                fieldsList.add(draggedField); // Add temporarily
            }

            if (draggedField != null && draggedField != temp) {
                if (droppedFromPalette) {
                    Notification.show("Please drop new components into empty slots (+ Tarik Field ke Sini)", 3000, Notification.Position.MIDDLE);
                    fieldsList.remove(draggedField);
                    draggedField = null;
                    return;
                }

                FieldMetaTemp sourceField = draggedField;
                FieldMetaTemp targetField = temp;

                Div overlay = new Div();
                overlay.getStyle()
                        .set("position", "absolute")
                        .set("top", "0").set("left", "0").set("width", "100%").set("height", "100%")
                        .set("background", "rgba(0,0,0,0.4)")
                        .set("z-index", "9999")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("border-radius", "8px");

                Div dialogBox = new Div();
                dialogBox.getStyle()
                        .set("background", "white")
                        .set("padding", "20px")
                        .set("border-radius", "8px")
                        .set("box-shadow", "0 10px 25px rgba(0,0,0,0.2)")
                        .set("min-width", "250px")
                        .set("max-width", "90%");
                
                H4 title = new H4("Confirm Swap");
                title.getStyle().set("margin-top", "0");
                
                com.vaadin.flow.component.html.Paragraph msg = new com.vaadin.flow.component.html.Paragraph("Are you sure you want to swap '" + sourceField.fieldName + "' with '" + targetField.fieldName + "'?");
                
                Button btnCancel = new Button("Cancel", ev -> canvasPanel.remove(overlay));
                Button btnSwap = new Button("Swap", ev -> {
                    int tempRow = sourceField.rowGroup;
                    int tempCol = sourceField.colIndex;
                    
                    sourceField.rowGroup = targetField.rowGroup;
                    sourceField.colIndex = targetField.colIndex;
                    
                    targetField.rowGroup = tempRow;
                    targetField.colIndex = tempCol;
                    
                    if (selectedField != null) {
                        propRowGroup.setValue(selectedField.rowGroup);
                    }
                    canvasPanel.remove(overlay);
                    rebuildCanvas();
                    Notification.show("Fields swapped successfully.", 2000, Notification.Position.BOTTOM_END);
                });
                btnSwap.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                
                HorizontalLayout btns = new HorizontalLayout(btnCancel, btnSwap);
                btns.setJustifyContentMode(JustifyContentMode.END);
                btns.getStyle().set("margin-top", "20px");
                
                dialogBox.add(title, msg, btns);
                overlay.add(dialogBox);
                
                canvasPanel.add(overlay);
            }
            draggedField = null;
        });

        if ("SUBFORM_GRID".equals(temp.componentType) && temp.lovCode != null && !temp.lovCode.trim().isEmpty()) {
            com.vaadin.flow.component.contextmenu.ContextMenu ctxMenu = new com.vaadin.flow.component.contextmenu.ContextMenu();
            ctxMenu.setTarget(card);
            ctxMenu.addItem("Open Subform Builder in New Tab", e -> {
                com.vaadin.flow.component.UI.getCurrent().getChildren()
                    .filter(c -> c instanceof com.vaadinerp.views.PortalView)
                    .findFirst()
                    .ifPresent(portal -> {
                        com.vaadinerp.views.PortalView pView = (com.vaadinerp.views.PortalView) portal;
                        int copyNum = pView.getNextDuplicateNumber("FORM_BUILDER");
                        String newTabId = "FORM_BUILDER_DUP_" + copyNum;
                        com.vaadinerp.security.entity.AppMenu mockMenu = new com.vaadinerp.security.entity.AppMenu();
                        mockMenu.setMenuCode("FORM_BUILDER");
                        mockMenu.setMenuTitle("Form Metadata Builder (" + copyNum + ")");
                        pView.openMenuTab(mockMenu, temp.lovCode, newTabId);
                    });
            });
        }

        return card;
    }

    private Component createPreviewComponent(FieldMetaTemp temp) {
        String label = temp.fieldLabel != null && !temp.fieldLabel.isEmpty() ? temp.fieldLabel : temp.fieldName;
        switch (temp.componentType.toUpperCase()) {
            case "TEXTBOX":
                TextField tf = new TextField(label);
                tf.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
                tf.setReadOnly(true);
                return tf;
            case "INTBOX":
                IntegerField iff = new IntegerField(label);
                iff.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
                iff.setPlaceholder("");
                iff.setReadOnly(true);
                return iff;
            case "DECIMALBOX":
                BigDecimalField bdf = new BigDecimalField(label);
                bdf.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
                bdf.setPlaceholder("");
                bdf.setReadOnly(true);
                return bdf;
            case "DATEBOX":
                DatePicker dp = new DatePicker(label);
                dp.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
                dp.setReadOnly(true);
                return dp;
            case "DATETIMEBOX":
                DateTimePicker dtp = new DateTimePicker(label);
                dtp.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
                dtp.setReadOnly(true);
                return dtp;
            case "TIMEBOX":
                TimePicker tp = new TimePicker(label);
                tp.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
                tp.setReadOnly(true);
                return tp;
            case "CHECKBOX":
                Checkbox cb = new Checkbox(label);
                cb.setReadOnly(true);
                return cb;
            case "TEXTAREA":
                TextArea ta = new TextArea(label);
                ta.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
                ta.setReadOnly(true);
                return ta;
            case "COMBOBOX":
                ComboBox<String> cob = new ComboBox<>(label);
                cob.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
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
                sel.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
                sel.setEnabled(false);
                if (temp.lovCode != null) {
                    sel.setPlaceholder("LOV: " + temp.lovCode);
                } else {
                    sel.setPlaceholder("Pilih...");
                }
                return sel;
            case "BANDBOX":
                TextField bf = new TextField(label);
                bf.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
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
                msc.setWidth(temp.fieldWidth != null && !temp.fieldWidth.trim().isEmpty() ? temp.fieldWidth : "100%");
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
                mockGrid.addColumn(s -> s).setHeader("Sample Detail Column...");
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
                        ruleField.setLabel("Regex Pattern (e.g. ^[0-9]{15}$)");
                    else if (val.startsWith("MIN_LEN") || val.startsWith("MAX_LEN"))
                        ruleField.setLabel("Character Count (e.g. 5)");
                    else
                        ruleField.setLabel("Numeric Value (e.g. 1500000)");
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
                Notification.show("Please select a validation type first!", 3000, Notification.Position.MIDDLE);
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

        Button btnCancel = new Button("Cancel", e -> dialog.close());

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
        com.vaadin.flow.component.details.Details aiDetails = new com.vaadin.flow.component.details.Details(
                "✨ Asisten AI (Bahasa Manusia ke Groovy Script)");
        aiDetails.setOpened(true);
        aiDetails.setWidthFull();
        aiDetails.getStyle().set("background", "var(--lumo-contrast-5pct)").set("padding", "10px").set("border-radius",
                "8px");

        VerticalLayout aiLayout = new VerticalLayout();
        aiLayout.setPadding(false);
        aiLayout.setSpacing(true);
        Span aiHelp = new Span(
                "Ketik instruksi aturan dalam bahasa Indonesia (misal: 'baris pertama status centang, lainnya false' atau 'baris 1 sampai 3 aktif'):");
        aiHelp.getStyle().set("font-size", "0.85em").set("color", "var(--lumo-secondary-text-color)");

        TextField aiInput = new TextField();
        aiInput.setPlaceholder("Contoh: jika baris pertama maka status = true, baris kedua dst false...");
        aiInput.setWidthFull();

        Button btnGenerateAi = new Button("✨ Buatkan Aturan (AI)", VaadinIcon.LIGHTBULB.create());
        btnGenerateAi.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS,
                ButtonVariant.LUMO_SMALL);

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
                "Kalkulasi Matematika (row.total = row.qty * row.price)");
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
            if (val == null)
                return;
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
                scriptArea.setValue(
                        curr + (curr.isEmpty() || curr.endsWith(" ") || curr.endsWith("\n") ? "" : " ") + e.getValue());
                rowVarPicker.clear();
            }
        });

        headerVarPicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                String curr = scriptArea.getValue();
                scriptArea.setValue(
                        curr + (curr.isEmpty() || curr.endsWith(" ") || curr.endsWith("\n") ? "" : " ") + e.getValue());
                headerVarPicker.clear();
            }
        });

        // AI Generator logic (Ollama Integration)
        btnGenerateAi.addClickListener(e -> {
            String prompt = aiInput.getValue().trim();
            if (prompt.isEmpty()) {
                Notification.show("Ketik instruksi untuk AI terlebih dahulu!", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Siapkan UI untuk loading state
            btnGenerateAi.setEnabled(false);
            btnGenerateAi.setText("⏳ Memikirkan...");

            // Menggunakan Java Reflection untuk membaca fungsi db.* secara dinamis
            StringBuilder dbFunctions = new StringBuilder();
            try {
                for (java.lang.reflect.Method m : com.vaadinerp.service.ScriptExecutorService.DatabaseHelper.class.getDeclaredMethods()) {
                    if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) && !m.getName().startsWith("get")) {
                        dbFunctions.append("- db.").append(m.getName()).append("(");
                        java.lang.reflect.Parameter[] params = m.getParameters();
                        for (int i = 0; i < params.length; i++) {
                            dbFunctions.append(params[i].getType().getSimpleName()).append(" ").append(params[i].getName());
                            if (i < params.length - 1) dbFunctions.append(", ");
                        }
                        dbFunctions.append(") mengembalikan ").append(m.getReturnType().getSimpleName()).append("\n");
                    }
                }
            } catch (Exception ex) {
                dbFunctions.append("- db.find(String tableName, String keyColumn, Object keyValue)\n- db.getValue(String sql, Object[] args)\n");
            }

            // Siapkan konteks (System Prompt)
            String sysPrompt = "Kamu adalah asisten ahli pembuat Groovy Script untuk ERP.\n" +
                "Aturan wajib:\n" +
                "1. Jika instruksi user relevan dengan logika kode, balas HANYA dengan kode Groovy murni. Tanpa penjelasan, tanpa markdown (```).\n" +
                "2. Jika instruksi user TIDAK relevan (sekadar bertanya/mengobrol di luar kode), berikan jawaban dalam bahasa Indonesia, tetapi WAJIB awali setiap baris jawaban dengan komentar ganda (//) agar tidak memicu error sintaks.\n" +
                "3. Variabel 'row' mewakili data baris saat ini (Map).\n" +
                "4. Variabel 'header' mewakili data form utama.\n" +
                "5. Gunakan 'rowIndex' (int) untuk nomor urut baris (mulai dari 1).\n" +
                "Fungsi database dinamis (terbaca dari Java Reflection):\n" + dbFunctions.toString() +
                "Kolom child/row yang valid: " + String.join(", ", childCols) + "\n" +
                "Kolom header yang valid: " + String.join(", ", headerCols);

            // Jalankan Asynchronous agar UI tidak freeze
            com.vaadin.flow.component.UI ui = e.getSource().getUI().orElse(com.vaadin.flow.component.UI.getCurrent());
            
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> payloadMap = new java.util.HashMap<>();
                    payloadMap.put("model", "qwen2.5:7b");
                    payloadMap.put("system", sysPrompt);
                    payloadMap.put("prompt", prompt);
                    payloadMap.put("stream", false);

                    String jsonPayload = mapper.writeValueAsString(payloadMap);

                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://172.16.0.63:11434/api/generate"))
                            .header("Content-Type", "application/json")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                            .timeout(java.time.Duration.ofSeconds(60)) // Proses AI bisa butuh waktu agak lama
                            .build();

                    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                            .connectTimeout(java.time.Duration.ofSeconds(10))
                            .build();

                    java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.body());
                        String aiResponse = rootNode.path("response").asText();
                        
                        // Bersihkan markdown ```groovy jika AI membandel
                        aiResponse = aiResponse.replaceAll("(?s)^```[a-zA-Z]*\\n?", "").replaceAll("(?s)\\n?```$", "").trim();

                        final String finalCode = "// ✨ Di-generate oleh Ollama (qwen2.5:7b) dari perintah: \"" + prompt + "\"\n" + aiResponse;

                        ui.access(() -> {
                            String existingScript = scriptArea.getValue() != null ? scriptArea.getValue().trim() : "";
                            if (!existingScript.isEmpty()) {
                                scriptArea.setValue(existingScript + "\n\n" + finalCode);
                            } else {
                                scriptArea.setValue(finalCode);
                            }
                            btnGenerateAi.setEnabled(true);
                            btnGenerateAi.setText("✨ Buatkan Aturan (AI)");
                            Notification.show("✅ Berhasil generate dari Ollama!", 3000, Notification.Position.BOTTOM_END);
                            aiInput.clear();
                        });
                    } else {
                        ui.access(() -> {
                            scriptArea.setValue("// ❌ Gagal memanggil Ollama.\n// HTTP Status: " + response.statusCode() + "\n// Response:\n" + response.body());
                            btnGenerateAi.setEnabled(true);
                            btnGenerateAi.setText("✨ Buatkan Aturan (AI)");
                            Notification.show("Gagal memanggil AI!", 4000, Notification.Position.MIDDLE);
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    ui.access(() -> {
                        scriptArea.setValue("// ❌ Terjadi kesalahan saat memanggil Ollama di IP 172.16.0.63:11434.\n// Pastikan server Ollama menyala dan bisa diping dari server ini.\n// Error: " + ex.getMessage());
                        btnGenerateAi.setEnabled(true);
                        btnGenerateAi.setText("✨ Buatkan Aturan (AI)");
                        Notification.show("Koneksi ke Ollama gagal: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                    });
                }
            });
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
                            scriptText, r, i, dummyHeader, simRows, null);
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
                Notification.show("✅ Simulasi sukses! Lihat tabel hasil di bawah.", 3000,
                        Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("❌ Error saat simulasi: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        layout.add(aiDetails, pickersLayout, scriptArea, btnSimulate, simGrid);

        Button btnSave = new Button("Simpan Script", VaadinIcon.CHECK.create(), e -> {
            fieldTemp.onAddScript = scriptArea.getValue().trim();
            Notification.show("On-Add-Row Script disimpan ke memori sementara (jangan lupa Klik Simpan Form)!", 4000,
                    Notification.Position.BOTTOM_END);
            dialog.close();
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnClear = new Button("Hapus Script", VaadinIcon.TRASH.create(), e -> {
            scriptArea.clear();
            fieldTemp.onAddScript = null;
            dialog.close();
        });
        btnClear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button btnCancel = new Button("Cancel", e -> dialog.close());

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
        comparisonOpField.setItems("=", ">", "<", ">=", "<=", "LIKE", "ILIKE", "!=", "IS NULL", "IS NOT NULL");
        comparisonOpField.setValue("=");
        comparisonOpField.setWidth("110px");

        ComboBox<String> filterColField = new ComboBox<>("Target Kolom (dari LOV)");
        filterColField.setAllowCustomValue(true);
        filterColField.addCustomValueSetListener(e -> filterColField.setValue(e.getDetail()));
        filterColField.setPlaceholder("Pilih / ketik kolom target...");
        filterColField.setWidth("180px");
        if (fieldTemp.lovCode != null && !fieldTemp.lovCode.trim().isEmpty()) {
            try {
                java.util.List<String> cols = getLovColumns(fieldTemp.lovCode);
                if (cols != null) {
                    java.util.List<String> cleanCols = cols.stream().filter(c -> c != null && !c.startsWith("_"))
                            .collect(java.util.stream.Collectors.toList());
                    filterColField.setItems(cleanCols);
                }
            } catch (Exception ignored) {
            }
        }

        ComboBox<String> sourceTypeField = new ComboBox<>("Source Type");
        sourceTypeField.setItems("FIELD", "QUERY", "STATIC");
        sourceTypeField.setValue("FIELD");
        sourceTypeField.setWidth("110px");

        ComboBox<String> sourceNameField = new ComboBox<>("Source Name / Value");
        sourceNameField.setAllowCustomValue(true);
        sourceNameField.addCustomValueSetListener(e -> sourceNameField.setValue(e.getDetail()));
        sourceNameField.setPlaceholder("Pilih / ketik sumber...");
        sourceNameField.setWidth("180px");

        java.util.List<String> formFieldNames = fieldsList.stream()
                .map(f -> f.fieldName)
                .filter(n -> n != null && !n.trim().isEmpty())
                .collect(java.util.stream.Collectors.toList());
        sourceNameField.setItems(formFieldNames);

        sourceTypeField.addValueChangeListener(e -> {
            String st = e.getValue();
            if ("FIELD".equals(st)) {
                sourceNameField.setItems(formFieldNames);
                sourceNameField.setPlaceholder("Pilih field di form ini...");
            } else if ("STATIC".equals(st)) {
                sourceNameField.setItems("NULL", "true", "false", "ACTIVE", "1", "0");
                sourceNameField.setPlaceholder("Ketik / pilih nilai statis...");
            } else {
                sourceNameField.setItems(new ArrayList<>());
                sourceNameField.setPlaceholder("Ketik parameter query...");
            }
        });

        comparisonOpField.addValueChangeListener(e -> {
            String op = e.getValue();
            if ("IS NULL".equals(op) || "IS NOT NULL".equals(op)) {
                sourceTypeField.setValue("STATIC");
                sourceNameField.setValue("(" + op + ")");
                sourceNameField.setEnabled(false);
                sourceTypeField.setEnabled(false);
            } else {
                sourceNameField.setEnabled(true);
                sourceTypeField.setEnabled(true);
                if ("STATIC".equals(sourceTypeField.getValue()) && sourceNameField.getValue() != null
                        && sourceNameField.getValue().startsWith("(IS ")) {
                    sourceNameField.clear();
                }
            }
        });

        Grid<FieldFilterMetaTemp> filtersGrid = new Grid<>();
        filtersGrid.setSizeFull();
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(filtersGrid);
        filtersGrid.addColumn(FieldFilterMetaTemp::getLogicalOperator).setHeader("Logika").setWidth("80px")
                .setFlexGrow(0);
        filtersGrid.addColumn(FieldFilterMetaTemp::getFilterColumn).setHeader("Target Kolom").setFlexGrow(1);
        filtersGrid.addColumn(FieldFilterMetaTemp::getComparisonOperator).setHeader("Op").setWidth("80px")
                .setFlexGrow(0);
        filtersGrid.addColumn(FieldFilterMetaTemp::getSourceType).setHeader("Source Type").setWidth("100px")
                .setFlexGrow(0);
        filtersGrid.addColumn(FieldFilterMetaTemp::getSourceName).setHeader("Source Name").setFlexGrow(1);

        final FieldFilterMetaTemp[] currentEditing = new FieldFilterMetaTemp[1];
        Button btnAddFilter = new Button("Add", VaadinIcon.PLUS.create());
        Button btnCancelEdit = new Button("Batal Edit", VaadinIcon.CLOSE.create());
        btnCancelEdit.setVisible(false);

        Runnable resetForm = () -> {
            currentEditing[0] = null;
            filterColField.clear();
            sourceNameField.clear();
            logicalOpField.setValue("AND");
            comparisonOpField.setValue("=");
            sourceTypeField.setValue("FIELD");
            sourceNameField.setEnabled(true);
            sourceTypeField.setEnabled(true);
            btnAddFilter.setText("Add");
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
        }).setHeader("Action").setWidth("120px").setFlexGrow(0);

        filtersGrid.setItems(fieldTemp.filters);

        btnAddFilter.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddFilter.addClickListener(e -> {
            String col = filterColField.getValue().trim();
            String srcType = sourceTypeField.getValue();
            String srcName = sourceNameField.getValue().trim();
            String logOp = logicalOpField.getValue();
            String compOp = comparisonOpField.getValue();

            if ("IS NULL".equals(compOp) || "IS NOT NULL".equals(compOp)) {
                if (srcName.isEmpty())
                    srcName = "(" + compOp + ")";
                if (srcType == null)
                    srcType = "STATIC";
            }

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
            Notification.show("Form Code dan Title tidak boleh kosong! (Table Name wajib jika Auto-Generate aktif)",
                    3000,
                    Notification.Position.MIDDLE);
            return;
        }

        if (fieldsList.isEmpty()) {
            Notification.show("At least 1 field is required to save!", 3000, Notification.Position.MIDDLE);
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
            field.setReadonlyMode(
                    temp.readonlyMode != null ? temp.readonlyMode : (temp.isReadonly ? "EDIT_AND_ADD" : "NONE"));
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
            field.setFieldWidth(temp.fieldWidth);
            field.setColOrder(temp.rowGroup * 1000 + temp.colIndex * 10);

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

        // Only remove fields from formMeta.getFields() if they were actually deleted by
        // the user
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

            // 2. Generate/Alter physical table under schema "dynamic" in PostgreSQL if
            // toggle checked
            if (autoCreateDbCheckbox.getValue() && tableName != null && !tableName.isEmpty()) {
                dynamicDataService.generatePhysicalTable(formMeta);
            }

            String msgTable = (tableName != null && !tableName.isEmpty())
                    ? (" dan tabel dynamic." + tableName + " siap digunakan!")
                    : " (Tanpa tabel fisik)";
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
            if (lov.getValueColumn() != null && !lov.getValueColumn().trim().isEmpty()
                    && !list.contains(lov.getValueColumn().trim()))
                list.add(lov.getValueColumn().trim());
            if (lov.getLabelColumn() != null && !lov.getLabelColumn().trim().isEmpty()
                    && !list.contains(lov.getLabelColumn().trim()))
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
        if (form == null)
            form = formMetaRepository.findById(lovCode.trim().toLowerCase()).orElse(null);
        if (form == null)
            form = formMetaRepository.findById(lovCode.trim().toUpperCase()).orElse(null);
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
        if (lovCode == null || list == null)
            return;
        List<String> extraLabels = new ArrayList<>();
        FormMeta sourceForm = formMetaRepository.findById(lovCode.trim()).orElse(null);
        if (sourceForm == null)
            sourceForm = formMetaRepository.findById(lovCode.trim().toLowerCase()).orElse(null);
        if (sourceForm == null)
            sourceForm = formMetaRepository.findById(lovCode.trim().toUpperCase()).orElse(null);
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
            if (col != null && col.toLowerCase().endsWith("id") && !col.equalsIgnoreCase("id")
                    && !col.equalsIgnoreCase("_label")) {
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
        }).setHeader("Action").setWidth("120px").setFlexGrow(0);

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
                .set("z-index", "100")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.08)");

        // 1. TAMBAH BUTTON
        Button btnNew = new Button("Add");
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
                    Notification.show("Please select a form from the History tab to edit!", 3000,
                            Notification.Position.MIDDLE);
                }
            } else {
                Notification.show("Anda sudah berada di tab desain/edit form.", 3000, Notification.Position.MIDDLE);
            }
        });

        // 2. HAPUS BUTTON
        Button btnDelete = new Button("Delete");
        Icon iconDelete = VaadinIcon.CLOSE_CIRCLE.create();
        iconDelete.getStyle().set("color", "#ef4444").set("font-size", "1.2rem");
        btnDelete.setIcon(iconDelete);
        btnDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnDelete.getStyle().set("font-weight", "500").set("color", "#374151");
        btnDelete.addClickListener(e -> {
            if (tabSheet.getSelectedTab() == historisTab) {
                java.util.Set<FormMeta> selectedItems = historyGrid.getSelectedItems();
                if (selectedItems != null && !selectedItems.isEmpty()) {
                    showConfirmDialog("Confirm Delete",
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
                                    Notification.show("Failed to delete: " + ex.getMessage(), 5000,
                                            Notification.Position.MIDDLE);
                                }
                            });
                } else {
                    Notification.show("Please select a form row from the grid first to delete.", 3000,
                            Notification.Position.MIDDLE);
                }
            } else {
                String formCode = formCodeField.getValue().trim();
                if (!formCode.isEmpty() && formMetaRepository.existsById(formCode)) {
                    showConfirmDialog("Confirm Delete", "Apakah Anda yakin ingin menghapus definisi form ini?",
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
                                    Notification.show("Failed to delete: " + ex.getMessage(), 5000,
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
                Notification.show("Please select a saved form to copy.", 3000, Notification.Position.MIDDLE);
            }
        });

        // 3. SIMPAN BUTTON
        Button btnSave = new Button("Save");
        Icon iconSave = VaadinIcon.DOWNLOAD.create();
        iconSave.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnSave.setIcon(iconSave);
        btnSave.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("font-weight", "600").set("color", "#3b82f6");
        btnSave.addClickListener(e -> {
            saveFormDefinition();
        });

        // 4. BATAL BUTTON
        Button btnCancel = new Button("Cancel");
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

        Button btnCancel = new Button("Cancel", event -> dialog.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(btnCancel, btnConfirm);
        dialog.open();
    }

    public void loadFormDefinition(FormMeta selectedForm) {
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
                    temp.colIndex = field.getColOrder() != null && field.getColOrder() >= 1000 ? (field.getColOrder() % 1000) / 10 : 1;
                    temp.colSpan = field.getColSpan();
                    temp.fieldWidth = field.getFieldWidth();
                    temp.isRequired = field.isRequired();
                    temp.isReadonly = field.isReadonly();
                    temp.readonlyMode = field.getReadonlyMode() != null ? field.getReadonlyMode()
                            : (field.isReadonly() ? "EDIT_AND_ADD" : "NONE");
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

        Button btnSave = new Button("Save", VaadinIcon.DOWNLOAD.create());
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
                Notification.show("Failed to save LOV: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        Button btnCancel = new Button("Cancel", e -> dialog.close());

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
        if (detailTableNameField.isVisible() && detailTableNameField.getValue() != null
                && !detailTableNameField.getValue().trim().isEmpty()) {
            availableTables.add(detailTableNameField.getValue().trim() + " (Detail Table)");
        }
        if (availableTables.isEmpty()) {
            Notification.show("Silakan isi Target Table Name, View Table, atau Detail Table Name terlebih dahulu!",
                    4000, Notification.Position.MIDDLE);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚡ Auto-Generate Components dari Database");
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Select<String> tableSelect = new Select<>();
        tableSelect.setLabel("Select Source Table / View:");
        tableSelect.setItems(availableTables);
        tableSelect.setValue(availableTables.get(0));
        tableSelect.setWidthFull();

        Select<String> layoutSelect = new Select<>();
        layoutSelect.setLabel("Select Display Layout Mode:");
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
                "Custom (Input Jumlah Kolom Berjejer...)");
        layoutSelect.setValue("Standar Form (2 Kolom / Berjejer)");
        layoutSelect.setWidthFull();

        com.vaadin.flow.component.textfield.IntegerField customColsInput = new com.vaadin.flow.component.textfield.IntegerField(
                "Jumlah Kolom Custom (Berjejer):");
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

        Checkbox chkExcludeAudit = new Checkbox(
                "Sembunyikan / Lewati Kolom Audit Default (inputby, inputdt, updateby, updatedt, dll)", true);

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
            if (selected == null || selected.isEmpty())
                return;
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
                Notification.show("Tabel '" + tableName + "' tidak ditemukan atau tidak memiliki kolom di database!",
                        4000, Notification.Position.MIDDLE);
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
                targetCols = (customColsInput.getValue() != null && customColsInput.getValue() > 0)
                        ? customColsInput.getValue()
                        : 2;
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
                if (colName == null || colName.trim().isEmpty())
                    continue;
                if (existingFields.contains(colName.toLowerCase())) {
                    continue;
                }
                if (excludeAudit && isAuditField(colName)) {
                    continue;
                }

                String dataType = (String) col.get("data_type");
                if (dataType == null)
                    dataType = "varchar";
                dataType = dataType.toLowerCase();

                FieldMetaTemp temp = new FieldMetaTemp();
                temp.fieldName = colName;
                temp.fieldLabel = formatLabelFromColumnName(colName);
                temp.componentType = mapDataTypeToComponentType(dataType);
                temp.colSpan = "SUBFORM_GRID".equalsIgnoreCase(temp.componentType) ? null : 1;
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

                if ("id".equalsIgnoreCase(colName)
                        || (pkField.getValue() != null && pkField.getValue().equalsIgnoreCase(colName))) {
                    temp.isReadonly = true;
                    temp.readonlyMode = "EDIT_AND_ADD";
                }
                if (col.get("is_nullable") != null && "NO".equalsIgnoreCase(col.get("is_nullable").toString())
                        && !temp.isReadonly) {
                    temp.isRequired = true;
                }

                fieldsList.add(temp);
                existingFields.add(colName.toLowerCase());
                addedCount++;
            }

            if (addedCount > 0) {
                rebuildCanvas();
                Notification.show("Berhasil menambahkan " + addedCount + " komponen baru dari tabel " + tableName + "!",
                        3000, Notification.Position.BOTTOM_END);
            } else {
                Notification.show(
                        "Semua kolom dari tabel " + tableName + " sudah ada di kanvas (tidak ada kolom baru).", 3000,
                        Notification.Position.MIDDLE);
            }
            dialog.close();
        });

        Button btnCancelDialog = new Button("Cancel", e -> dialog.close());
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
        colsSelect.setLabel("Number of Columns per Row:");
        colsSelect.setItems(1, 2, 3, 4, 5, 6, 8, 10, 12);
        colsSelect.setValue(3);
        colsSelect.setWidthFull();

        Span infoSpan = new Span("💡 Sistem akan mengurutkan ulang kolom form yang ada di kanvas ke dalam "
                + colsSelect.getValue() + " kolom berjejer per baris. Kolom detail tabel (jika ada) tidak diubah.");
        infoSpan.getStyle().set("color", "#64748b").set("font-size", "0.85rem");

        colsSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                infoSpan.setText("💡 Sistem akan mengurutkan ulang kolom form yang ada di kanvas ke dalam "
                        + e.getValue() + " kolom berjejer per baris. Kolom detail tabel (jika ada) tidak diubah.");
            }
        });

        layout.add(colsSelect, infoSpan);
        dialog.add(layout);

        Button btnApply = new Button("Terapkan Layout", VaadinIcon.CHECK.create());
        btnApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnApply.addClickListener(e -> {
            int targetCols = colsSelect.getValue() != null ? colsSelect.getValue() : 2;
            int currentGroup = 1;
            int currentVisCol = 1;
            
            for (FieldMetaTemp f : fieldsList) {
                if (f.isDetail) continue;

                int span = f.colSpan != null ? f.colSpan : 1;
                span = Math.min(span, targetCols);
                
                if (currentVisCol > 1 && (currentVisCol - 1 + span) > targetCols) {
                    currentGroup++;
                    currentVisCol = 1;
                }
                
                f.rowGroup = currentGroup;
                f.colIndex = currentVisCol;
                
                currentVisCol += span;
                
                if (currentVisCol > targetCols) {
                    currentGroup++;
                    currentVisCol = 1;
                }
            }
            rebuildCanvas();
            Notification.show("Layout berhasil diatur menjadi " + targetCols + " kolom berjejer!", 3000,
                    Notification.Position.TOP_CENTER);
            dialog.close();
        });

        Button btnCancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(btnCancel, btnApply);
        dialog.open();
    }

    private boolean isAuditField(String colName) {
        if (colName == null)
            return false;
        String lower = colName.toLowerCase().trim();
        return lower.equals("inputby") || lower.equals("inputdt") || lower.equals("input_by")
                || lower.equals("input_date") ||
                lower.equals("updateby") || lower.equals("updatedt") || lower.equals("update_by")
                || lower.equals("update_date") ||
                lower.equals("created_by") || lower.equals("created_at") || lower.equals("created_date") ||
                lower.equals("updated_by") || lower.equals("updated_at") || lower.equals("updated_date") ||
                lower.equals("modified_by") || lower.equals("modified_at") || lower.equals("modified_date") ||
                lower.equals("is_deleted") || lower.equals("delete_by") || lower.equals("delete_date") ||
                lower.equals("user_input") || lower.equals("date_input") || lower.equals("user_update")
                || lower.equals("date_update") ||
                lower.equals("version");
    }

    private String formatLabelFromColumnName(String colName) {
        if (colName == null)
            return "";
        String[] parts = colName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0)
                    sb.append(" ");
                sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private String mapDataTypeToComponentType(String dataType) {
        if (dataType == null)
            return "TEXTBOX";
        if (dataType.contains("bool"))
            return "CHECKBOX";
        if (dataType.contains("int") || dataType.contains("serial"))
            return "INTBOX";
        if (dataType.contains("num") || dataType.contains("dec") || dataType.contains("real")
                || dataType.contains("double") || dataType.contains("float") || dataType.contains("money"))
            return "DECIMALBOX";
        if (dataType.contains("time") && !dataType.contains("date") && !dataType.contains("stamp"))
            return "TIMEBOX";
        if (dataType.contains("timestamp") || dataType.contains("timestamptz"))
            return "DATETIMEBOX";
        if (dataType.contains("date"))
            return "DATEBOX";
        if (dataType.equals("text") || dataType.contains("character varying") || dataType.contains("varchar")) {
            return "TEXTBOX";
        }
        return "TEXTBOX";
    }

    public void setFilterFormCode(String formCode) {
        FilterCriteria criteria = filterValues.get("formCode");
        TextField field = filterFields.get("formCode");
        if (criteria != null && field != null) {
            criteria.operator = "Equals";
            criteria.value = formCode;
            field.setValue(formCode);
            applyHistoryFilters();
        }
    }
}
