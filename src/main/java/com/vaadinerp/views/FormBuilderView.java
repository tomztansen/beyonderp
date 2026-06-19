package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

import java.util.ArrayList;
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

    // Load & Save Toolbar
    private final ComboBox<FormMeta> loadFormCombo = new ComboBox<>("Load Existing Form Definition");
    private final TextField formCodeField = new TextField("Form Code (Unique)");
    private final TextField formTitleField = new TextField("Form Title");
    private final ComboBox<String> formTypeCombo = new ComboBox<>("Form Type");
    private final TextField tableNameField = new TextField("Target Table Name");
    private final TextField pkField = new TextField("Primary Key Column");
    private final TextField labelWidthField = new TextField("Label Width (e.g. 150px)");
    private final TextField detailTableNameField = new TextField("Detail Table Name");
    private final TextField detailPkField = new TextField("Detail PK Column");
    private final TextField detailFkField = new TextField("Detail Foreign Key Column");

    // Selected Field State
    private FieldMetaTemp selectedField = null;
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
    private final ComboBox<String> propLovCode = new ComboBox<>("LOV Code (Optional)");
    private final IntegerField propRowGroup = new IntegerField("Row Group");
    private final Checkbox propIsRequired = new Checkbox("Required");
    private final Checkbox propIsReadonly = new Checkbox("Read-only");
    private final Checkbox propShowInGrid = new Checkbox("Show in Grid");
    private final Checkbox propIsDetail = new Checkbox("Is Detail Grid Column");
    private final TextField propFormula = new TextField("Formula (e.g. qty * price)");
    private final Checkbox propSaveOnInsert = new Checkbox("Save on Insert");
    private final Checkbox propSaveOnUpdate = new Checkbox("Save on Edit/Update");
    private final Button propBtnFilters = new Button("Configure Filters", VaadinIcon.FILTER.create());
    private final Button propBtnLovTargets = new Button("Configure LOV Targets", VaadinIcon.LINK.create());

    // Temporary Classes to hold builder state
    public static class FieldMetaTemp {
        String fieldName;
        String fieldLabel;
        String componentType;
        String lovCode;
        int rowGroup = 1;
        boolean isRequired;
        boolean isReadonly;
        boolean showInGrid = true;
        boolean isDetail;
        String formula;
        boolean saveOnInsert = true;
        boolean saveOnUpdate = true;
        List<FieldFilterMetaTemp> filters = new ArrayList<>();
        List<FieldLovTargetMetaTemp> lovTargets = new ArrayList<>();

        public String getFieldName() { return fieldName; }
        public String getFieldLabel() { return fieldLabel; }
        public String getComponentType() { return componentType; }
        public String getLovCode() { return lovCode; }
        public int getRowGroup() { return rowGroup; }
        public boolean isRequired() { return isRequired; }
        public boolean isReadonly() { return isReadonly; }
        public boolean isShowInGrid() { return showInGrid; }
        public boolean isDetail() { return isDetail; }
        public String getFormula() { return formula; }
        public boolean isSaveOnInsert() { return saveOnInsert; }
        public boolean isSaveOnUpdate() { return saveOnUpdate; }
    }

    public static class FieldFilterMetaTemp {
        String filterColumn;
        String sourceType; // FIELD, QUERY, STATIC
        String sourceName;

        public String getFilterColumn() { return filterColumn; }
        public String getSourceType() { return sourceType; }
        public String getSourceName() { return sourceName; }
    }

    public static class FieldLovTargetMetaTemp {
        String sourceColumn;
        String targetField;
        String actionType = "COPY";
        String lookupColumn;

        public String getSourceColumn() { return sourceColumn; }
        public String getTargetField() { return targetField; }
        public String getActionType() { return actionType; }
        public String getLookupColumn() { return lookupColumn; }
    }

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

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("Dynamic Form Builder");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "5px");

        // 1. TOP LOAD SECTION
        HorizontalLayout loadLayout = new HorizontalLayout();
        loadLayout.setWidthFull();
        loadLayout.setAlignItems(Alignment.END);
        loadLayout.setSpacing(true);
        loadLayout.getStyle().set("margin-bottom", "10px");

        loadFormCombo.setWidth("350px");
        loadFormCombo.setPlaceholder("Pilih form untuk diedit...");
        loadFormCombo.setItems(formMetaRepository.findAll());
        loadFormCombo.setItemLabelGenerator(form -> form.getFormTitle() + " (" + form.getFormCode() + ")");

        Button btnClear = new Button("Buat Baru (Reset)", VaadinIcon.REFRESH.create());
        btnClear.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClear.addClickListener(e -> {
            loadFormCombo.clear();
            formCodeField.clear();
            formCodeField.setReadOnly(false);
            formTitleField.clear();
            formTypeCombo.setValue("SINGLE");
            tableNameField.clear();
            pkField.setValue("id");
            labelWidthField.setValue("150px");
            detailTableNameField.clear();
            detailPkField.setValue("id");
            detailFkField.clear();
            fieldsList.clear();
            selectField(null);
            rebuildCanvas();
            Notification.show("Form Builder di-reset untuk membuat form baru.", 3000, Notification.Position.TOP_CENTER);
        });

        loadLayout.add(loadFormCombo, btnClear);

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

        loadFormCombo.addValueChangeListener(event -> {
            FormMeta selectedForm = event.getValue();
            if (selectedForm != null) {
                // Populate Form Meta fields
                formCodeField.setValue(selectedForm.getFormCode() != null ? selectedForm.getFormCode() : "");
                formCodeField.setReadOnly(true); // Don't allow changing form code since it's the primary key
                formTitleField.setValue(selectedForm.getFormTitle() != null ? selectedForm.getFormTitle() : "");
                formTypeCombo.setValue(selectedForm.getFormType() != null ? selectedForm.getFormType() : "SINGLE");
                tableNameField.setValue(selectedForm.getTableName() != null ? selectedForm.getTableName() : "");
                pkField.setValue(selectedForm.getPrimaryKey() != null ? selectedForm.getPrimaryKey() : "id");
                labelWidthField.setValue(selectedForm.getLabelWidth() != null ? selectedForm.getLabelWidth() : "150px");
                detailTableNameField.setValue(selectedForm.getDetailTableName() != null ? selectedForm.getDetailTableName() : "");
                detailPkField.setValue(selectedForm.getDetailPrimaryKey() != null ? selectedForm.getDetailPrimaryKey() : "id");
                detailFkField.setValue(selectedForm.getDetailForeignKey() != null ? selectedForm.getDetailForeignKey() : "");

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
                        temp.isRequired = field.isRequired();
                        temp.isReadonly = field.isReadonly();
                        temp.showInGrid = field.isShowInGrid();
                        temp.isDetail = field.isDetail();
                        temp.formula = field.getFormula();
                        temp.saveOnInsert = field.isSaveOnInsert();
                        temp.saveOnUpdate = field.isSaveOnUpdate();
                        
                        // Load filters
                        temp.filters = new ArrayList<>();
                        if (field.getFilters() != null) {
                            for (FieldFilterMeta filter : field.getFilters()) {
                                FieldFilterMetaTemp fTemp = new FieldFilterMetaTemp();
                                fTemp.filterColumn = filter.getFilterColumn();
                                fTemp.sourceType = filter.getSourceType();
                                fTemp.sourceName = filter.getSourceName();
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
                Notification.show("Form definition loaded: " + selectedForm.getFormCode(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        // 2. FORM METADATA SETUP
        FormLayout formMetaLayout = new FormLayout();
        formMetaLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3),
                new FormLayout.ResponsiveStep("900px", 6)
        );
        pkField.setValue("id");
        labelWidthField.setValue("150px");
        detailPkField.setValue("id");
        formMetaLayout.add(formCodeField, formTitleField, formTypeCombo, tableNameField, pkField, labelWidthField,
                           detailTableNameField, detailPkField, detailFkField);

        // 3. WORKSPACE (3 COLUMNS)
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
            createPaletteButton("Check Box", VaadinIcon.CHECK_SQUARE_O, "CHECKBOX"),
            createPaletteButton("Text Area", VaadinIcon.ALIGN_LEFT, "TEXTAREA"),
            createPaletteButton("Combo Box", VaadinIcon.COMBOBOX, "COMBOBOX"),
            createPaletteButton("List Box", VaadinIcon.LIST_SELECT, "LISTBOX"),
            createPaletteButton("Band Box", VaadinIcon.SEARCH, "BANDBOX"),
            createPaletteButton("Chosen Box", VaadinIcon.TAGS, "CHOSENBOX")
        );

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

        // 4. MAIN SAVE BUTTON
        Button btnSaveForm = new Button("Save Form Definition & Generate Database Table", VaadinIcon.DATABASE.create());
        btnSaveForm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnSaveForm.setWidthFull();
        btnSaveForm.addClickListener(e -> saveFormDefinition());

        add(title, loadLayout, formMetaLayout, workspace, btnSaveForm);
        setFlexGrow(1, workspace);

        // Initial rebuild
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
        temp.showInGrid = true;

        fieldsList.add(temp);
        rebuildCanvas();
        selectField(temp);
        Notification.show(type + " ditambahkan ke kanvas!", 2000, Notification.Position.BOTTOM_END);
    }

    private boolean fieldExists(String name) {
        return fieldsList.stream().anyMatch(f -> f.fieldName.equalsIgnoreCase(name));
    }

    private void setupPropertiesForm() {
        propertiesForm.setVisible(false);
        propertiesForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        propComponentType.setItems("TEXTBOX", "INTBOX", "DECIMALBOX", "DATEBOX", "CHECKBOX", "TEXTAREA", "COMBOBOX", "LISTBOX", "BANDBOX", "CHOSENBOX");

        List<String> lovCodes = lovMetaRepository.findAll().stream().map(LovMeta::getLovCode).collect(Collectors.toList());
        propLovCode.setItems(lovCodes);
        propLovCode.setPlaceholder("Pilih LOV jika ada...");

        propRowGroup.setMin(1);

        HorizontalLayout checkBoxLayout = new HorizontalLayout(propIsRequired, propIsReadonly, propShowInGrid, propIsDetail, propSaveOnInsert, propSaveOnUpdate);
        checkBoxLayout.setSpacing(true);
        checkBoxLayout.setPadding(false);

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

        propertiesForm.add(propFieldName, propFieldLabel, propComponentType, propLovCode, propRowGroup, propFormula, checkBoxLayout, propBtnFilters, propBtnLovTargets);

        // Listeners for live sync
        setupPropertiesListeners();
    }

    private void setupPropertiesListeners() {
        propFieldName.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.fieldName = e.getValue().trim();
                rebuildCanvas();
            }
        });
        propFieldLabel.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.fieldLabel = e.getValue().trim();
                rebuildCanvas();
            }
        });
        propComponentType.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient() && e.getValue() != null) {
                selectedField.componentType = e.getValue();
                boolean isSelection = "COMBOBOX".equalsIgnoreCase(selectedField.componentType) ||
                                     "LISTBOX".equalsIgnoreCase(selectedField.componentType) ||
                                     "BANDBOX".equalsIgnoreCase(selectedField.componentType);
                propLovCode.setEnabled(isSelection);
                propBtnFilters.setEnabled(isSelection && selectedField.lovCode != null && !selectedField.lovCode.trim().isEmpty());
                propBtnLovTargets.setEnabled(true);
                rebuildCanvas();
            }
        });
        propLovCode.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.lovCode = e.getValue();
                boolean isSelection = "COMBOBOX".equalsIgnoreCase(selectedField.componentType) ||
                                     "LISTBOX".equalsIgnoreCase(selectedField.componentType) ||
                                     "BANDBOX".equalsIgnoreCase(selectedField.componentType);
                propBtnFilters.setEnabled(isSelection && selectedField.lovCode != null && !selectedField.lovCode.trim().isEmpty());
                propBtnLovTargets.setEnabled(true);
                rebuildCanvas();
            }
        });
        propRowGroup.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient() && e.getValue() != null) {
                selectedField.rowGroup = e.getValue();
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
        propIsDetail.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.isDetail = e.getValue();
                rebuildCanvas();
            }
        });
        propFormula.addValueChangeListener(e -> {
            if (selectedField != null && e.isFromClient()) {
                selectedField.formula = e.getValue().trim();
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
    }

    private void selectField(FieldMetaTemp temp) {
        selectedField = temp;
        if (temp == null) {
            propPlaceholderLabel.setVisible(true);
            propertiesForm.setVisible(false);
        } else {
            propPlaceholderLabel.setVisible(false);
            propertiesForm.setVisible(true);

            propFieldName.setValue(temp.fieldName != null ? temp.fieldName : "");
            propFieldLabel.setValue(temp.fieldLabel != null ? temp.fieldLabel : "");
            propComponentType.setValue(temp.componentType);
            propLovCode.setValue(temp.lovCode);
            propRowGroup.setValue(temp.rowGroup);
            propIsRequired.setValue(temp.isRequired);
            propIsReadonly.setValue(temp.isReadonly);
            propShowInGrid.setValue(temp.showInGrid);
            propIsDetail.setValue(temp.isDetail);
            propIsDetail.setVisible("MASTER_DETAIL".equals(formTypeCombo.getValue()));
            propFormula.setValue(temp.formula != null ? temp.formula : "");
            propSaveOnInsert.setValue(temp.saveOnInsert);
            propSaveOnUpdate.setValue(temp.saveOnUpdate);

            boolean isSelection = "COMBOBOX".equalsIgnoreCase(temp.componentType) ||
                                 "LISTBOX".equalsIgnoreCase(temp.componentType) ||
                                 "BANDBOX".equalsIgnoreCase(temp.componentType);
            propLovCode.setEnabled(isSelection);
            propBtnFilters.setEnabled(isSelection && temp.lovCode != null && !temp.lovCode.trim().isEmpty());
            propBtnLovTargets.setEnabled(true);
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
                Span emptyDetail = new Span("[Tandai 'Is Detail Grid Column' pada properti field untuk memindahkan ke kolom Grid detail]");
                emptyDetail.getStyle().set("color", "#94a3b8").set("font-style", "italic").set("font-size", "0.9rem");
                canvas.add(emptyDetail);
            } else {
                FormLayout detailRowLayout = new FormLayout();
                detailRowLayout.setWidthFull();
                int cols = detailFields.size();
                detailRowLayout.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("500px", Math.max(1, cols / 2)),
                    new FormLayout.ResponsiveStep("800px", cols)
                );
                
                int seq = 1;
                for (FieldMetaTemp temp : detailFields) {
                    Component card = buildFieldCard(temp, seq++);
                    detailRowLayout.add(card);
                }
                canvas.add(detailRowLayout);
            }
        } else {
            // Standard SINGLE Form
            renderGroupedFields(fieldsList);
        }
    }

    private void renderGroupedFields(List<FieldMetaTemp> targetFields) {
        Map<Integer, List<FieldMetaTemp>> groups = new HashMap<>();
        List<Integer> rowGroupsOrder = new ArrayList<>();
        for (FieldMetaTemp f : targetFields) {
            groups.computeIfAbsent(f.rowGroup, k -> new ArrayList<>()).add(f);
            if (!rowGroupsOrder.contains(f.rowGroup)) {
                rowGroupsOrder.add(f.rowGroup);
            }
        }
        rowGroupsOrder.sort(Integer::compareTo);

        int sequence = 1;
        for (Integer rg : rowGroupsOrder) {
            List<FieldMetaTemp> groupFields = groups.get(rg);
            FormLayout rowLayout = new FormLayout();
            rowLayout.setWidthFull();
            int cols = groupFields.size();
            rowLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", Math.max(1, cols / 2)),
                new FormLayout.ResponsiveStep("800px", cols)
            );

            for (FieldMetaTemp temp : groupFields) {
                Component card = buildFieldCard(temp, sequence++);
                rowLayout.add(card);
                if ("TEXTAREA".equalsIgnoreCase(temp.componentType)) {
                    rowLayout.setColspan(card, cols);
                }
            }
            canvas.add(rowLayout);
        }
    }

    private Component buildFieldCard(FieldMetaTemp temp, int sequence) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        
        boolean isSelected = (selectedField == temp);
        
        card.getStyle()
            .set("border", isSelected ? "2px solid #6366f1" : "1px dashed #cbd5e1")
            .set("border-radius", "8px")
            .set("background-color", isSelected ? "#f8fafc" : "#ffffff")
            .set("cursor", "grab")
            .set("position", "relative")
            .set("box-shadow", isSelected ? "0 4px 12px rgba(99, 102, 241, 0.1)" : "none")
            .set("transition", "all 0.2s ease");

        HorizontalLayout cardHeader = new HorizontalLayout();
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(JustifyContentMode.END);
        cardHeader.setSpacing(true);
        cardHeader.getStyle().set("margin-bottom", "6px");

        Span orderBadge = new Span("#" + sequence);
        orderBadge.getStyle()
            .set("font-size", "0.65rem")
            .set("font-weight", "700")
            .set("background-color", "#f1f5f9")
            .set("color", "#475569")
            .set("padding", "2px 6px")
            .set("border-radius", "4px");

        Span typeBadge = new Span(temp.componentType);
        typeBadge.getStyle()
            .set("font-size", "0.65rem")
            .set("font-weight", "700")
            .set("background-color", isSelected ? "#e0e7ff" : "#f1f5f9")
            .set("color", isSelected ? "#4f46e5" : "#475569")
            .set("padding", "2px 6px")
            .set("border-radius", "4px")
            .set("margin-right", "auto");
        cardHeader.add(orderBadge, typeBadge);

        Button btnEdit = new Button(VaadinIcon.COG.create());
        btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnEdit.getStyle().set("cursor", "pointer");
        btnEdit.addClickListener(e -> selectField(temp));

        Button btnDel = new Button(VaadinIcon.TRASH.create());
        btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnDel.getStyle().set("cursor", "pointer");
        btnDel.addClickListener(e -> {
            fieldsList.remove(temp);
            if (selectedField == temp) {
                selectField(null);
            }
            rebuildCanvas();
        });
        cardHeader.add(btnEdit, btnDel);

        Component previewComp = createPreviewComponent(temp);
        card.add(cardHeader, previewComp);
        
        card.getElement().addEventListener("click", e -> selectField(temp));

        // Enable Drag & Drop
        DragSource<Component> dragSource = DragSource.create(card);
        dragSource.setDraggable(true);
        dragSource.addDragStartListener(e -> {
            draggedField = temp;
            card.getStyle().set("opacity", "0.5");
        });
        dragSource.addDragEndListener(e -> {
            draggedField = null;
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
                    Notification.show("Field reordered: " + draggedField.fieldName + " moved to row group " + temp.rowGroup, 2000, Notification.Position.BOTTOM_END);
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
                iff.setReadOnly(true);
                return iff;
            case "DECIMALBOX":
                BigDecimalField bdf = new BigDecimalField(label);
                bdf.setWidthFull();
                bdf.setReadOnly(true);
                return bdf;
            case "DATEBOX":
                DatePicker dp = new DatePicker(label);
                dp.setWidthFull();
                dp.setReadOnly(true);
                return dp;
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
            default:
                return new TextField(label);
        }
    }

    private void openFilterConfigDialog(FieldMetaTemp fieldTemp) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Configure Filters for Field: " + fieldTemp.fieldName);
        dialog.setWidth("600px");
        dialog.setHeight("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        // Inputs
        TextField filterColField = new TextField("Filter Column (in LOV Table)");
        filterColField.setPlaceholder("e.g. parent_code");

        ComboBox<String> sourceTypeField = new ComboBox<>("Source Type");
        sourceTypeField.setItems("FIELD", "QUERY", "STATIC");
        sourceTypeField.setValue("FIELD");

        TextField sourceNameField = new TextField("Source Name / Value");
        sourceNameField.setPlaceholder("e.g. department (for FIELD) or parent (for QUERY)");

        Grid<FieldFilterMetaTemp> filtersGrid = new Grid<>();
        filtersGrid.setSizeFull();
        filtersGrid.addColumn(FieldFilterMetaTemp::getFilterColumn).setHeader("Kolom Target");
        filtersGrid.addColumn(FieldFilterMetaTemp::getSourceType).setHeader("Tipe Sumber");
        filtersGrid.addColumn(FieldFilterMetaTemp::getSourceName).setHeader("Nama Sumber / Value");

        filtersGrid.addComponentColumn(fTemp -> {
            Button btnDel = new Button(VaadinIcon.TRASH.create());
            btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDel.addClickListener(e -> {
                fieldTemp.filters.remove(fTemp);
                filtersGrid.setItems(new ArrayList<>(fieldTemp.filters));
            });
            return btnDel;
        }).setHeader("Hapus").setWidth("80px").setFlexGrow(0);

        filtersGrid.setItems(fieldTemp.filters);

        Button btnAddFilter = new Button("Tambah Filter", VaadinIcon.PLUS.create());
        btnAddFilter.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnAddFilter.addClickListener(e -> {
            String col = filterColField.getValue().trim();
            String srcType = sourceTypeField.getValue();
            String srcName = sourceNameField.getValue().trim();

            if (col.isEmpty() || srcName.isEmpty()) {
                Notification.show("Kolom target dan Nama sumber tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }

            FieldFilterMetaTemp fTemp = new FieldFilterMetaTemp();
            fTemp.filterColumn = col;
            fTemp.sourceType = srcType;
            fTemp.sourceName = srcName;

            fieldTemp.filters.add(fTemp);
            filtersGrid.setItems(new ArrayList<>(fieldTemp.filters));

            filterColField.clear();
            sourceNameField.clear();
        });

        HorizontalLayout inputLayout = new HorizontalLayout(filterColField, sourceTypeField, sourceNameField, btnAddFilter);
        inputLayout.setAlignItems(Alignment.END);
        inputLayout.setSpacing(true);

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

        if (formCode.isEmpty() || formTitle.isEmpty() || tableName.isEmpty()) {
            Notification.show("Form Code, Title, dan Table Name tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (fieldsList.isEmpty()) {
            Notification.show("Harus ada minimal 1 field untuk disimpan!", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Delete existing form metadata to perform clean overwrite (Hibernate cascade will clear fields and filters)
        if (formMetaRepository.existsById(formCode)) {
            formMetaRepository.deleteById(formCode);
        }

        FormMeta formMeta = new FormMeta();
        formMeta.setFormCode(formCode);
        formMeta.setFormTitle(formTitle);
        formMeta.setFormType(formTypeCombo.getValue());
        formMeta.setTableName(tableName);
        formMeta.setPrimaryKey(pk.isEmpty() ? "id" : pk);
        formMeta.setLabelWidth(labelWidth.isEmpty() ? "150px" : labelWidth);

        if ("MASTER_DETAIL".equals(formTypeCombo.getValue())) {
            String dtlTable = detailTableNameField.getValue().trim();
            String dtlPk = detailPkField.getValue().trim();
            String dtlFk = detailFkField.getValue().trim();
            if (dtlTable.isEmpty() || dtlFk.isEmpty()) {
                Notification.show("Nama tabel detail dan foreign key tidak boleh kosong untuk Master-Detail form!", 3000, Notification.Position.MIDDLE);
                return;
            }
            formMeta.setDetailTableName(dtlTable);
            formMeta.setDetailPrimaryKey(dtlPk.isEmpty() ? "id" : dtlPk);
            formMeta.setDetailForeignKey(dtlFk);
        }

        formMeta.setFields(new ArrayList<>());

        int order = 10;
        for (FieldMetaTemp temp : fieldsList) {
            FieldMeta field = new FieldMeta();
            field.setFormMeta(formMeta);
            field.setFieldName(temp.fieldName);
            field.setFieldLabel(temp.fieldLabel);
            field.setComponentType(temp.componentType);
            field.setLovCode(temp.lovCode);
            field.setRequired(temp.isRequired);
            field.setReadonly(temp.isReadonly);
            field.setShowInGrid(temp.showInGrid);
            field.setDetail(temp.isDetail);
            field.setFormula(temp.formula);
            field.setSaveOnInsert(temp.saveOnInsert);
            field.setSaveOnUpdate(temp.saveOnUpdate);
            field.setRowGroup(temp.rowGroup);
            field.setColOrder(order);
            order += 10;

            List<FieldFilterMeta> filters = new ArrayList<>();
            for (FieldFilterMetaTemp fTemp : temp.filters) {
                FieldFilterMeta f = new FieldFilterMeta();
                f.setFieldMeta(field);
                f.setFilterColumn(fTemp.filterColumn);
                f.setSourceType(fTemp.sourceType);
                f.setSourceName(fTemp.sourceName);
                filters.add(f);
            }
            field.setFilters(filters);

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
            field.setLovTargets(lovTargets);

            formMeta.getFields().add(field);
        }

        try {
            // 1. Save metadata definitions (cascades automatically)
            formMetaRepository.save(formMeta);

            // 2. Generate/Alter physical table under schema "dynamic" in PostgreSQL
            dynamicDataService.generatePhysicalTable(formMeta);

            Notification.show("Form " + formTitle + " berhasil dibuat dan tabel dynamic." + tableName + " siap digunakan!", 4000, Notification.Position.TOP_CENTER);
            
            // Clear inputs and reset read-only status
            loadFormCombo.clear();
            formCodeField.clear();
            formCodeField.setReadOnly(false);
            formTitleField.clear();
            tableNameField.clear();
            pkField.setValue("id");
            labelWidthField.setValue("150px");
            fieldsList.clear();
            selectField(null);
            rebuildCanvas();

            // Refresh combo items
            loadFormCombo.setItems(formMetaRepository.findAll());

            if (onFormSavedListener != null) {
                onFormSavedListener.run();
            }
        } catch (Exception ex) {
            Notification.show("Gagal menyimpan form: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private List<String> getLovColumns(String lovCode) {
        List<String> list = new ArrayList<>();
        if (lovCode == null) return list;
        LovMeta lov = lovMetaRepository.findById(lovCode).orElse(null);
        if (lov == null) return list;
        
        if (lov.getValueColumn() != null) list.add(lov.getValueColumn());
        if (lov.getLabelColumn() != null) list.add(lov.getLabelColumn());
        
        if (lov.getGridColumns() != null) {
            for (String part : lov.getGridColumns().split(",")) {
                String[] split = part.split(":");
                if (split.length > 0) {
                    String col = split[0].trim();
                    if (!list.contains(col)) {
                        list.add(col);
                    }
                }
            }
        }
        return list;
    }

    private void openLovTargetsConfigDialog(FieldMetaTemp fieldTemp) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Configure LOV Targets for " + fieldTemp.fieldName);
        dialog.setWidth("700px");
        dialog.setHeight("550px");

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
                if (targetFieldTemp != null && targetFieldTemp.lovCode != null && !targetFieldTemp.lovCode.trim().isEmpty()) {
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
        grid.addColumn(FieldLovTargetMetaTemp::getSourceColumn).setHeader("Source Column");
        grid.addColumn(FieldLovTargetMetaTemp::getTargetField).setHeader("Target Field");
        grid.addColumn(FieldLovTargetMetaTemp::getActionType).setHeader("Action Type");
        grid.addColumn(FieldLovTargetMetaTemp::getLookupColumn).setHeader("Lookup Column");

        grid.addComponentColumn(tTemp -> {
            Button btnDel = new Button(VaadinIcon.TRASH.create());
            btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDel.addClickListener(e -> {
                fieldTemp.lovTargets.remove(tTemp);
                grid.setItems(new ArrayList<>(fieldTemp.lovTargets));
            });
            return btnDel;
        }).setHeader("Hapus").setWidth("80px").setFlexGrow(0);

        grid.setItems(fieldTemp.lovTargets);

        Button btnAdd = new Button("Tambah Target", VaadinIcon.PLUS.create());
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
                Notification.show("Lookup Column harus dipilih jika Action Type = QUERY_LOV!", 3000, Notification.Position.MIDDLE);
                return;
            }

            FieldLovTargetMetaTemp tTemp = new FieldLovTargetMetaTemp();
            tTemp.sourceColumn = srcCol;
            tTemp.targetField = trgField;
            tTemp.actionType = actType;
            tTemp.lookupColumn = lookupCol;

            fieldTemp.lovTargets.add(tTemp);
            grid.setItems(new ArrayList<>(fieldTemp.lovTargets));

            sourceColCombo.clear();
            targetFieldCombo.clear();
            actionTypeSelect.setValue("COPY");
            lookupColCombo.clear();
        });

        HorizontalLayout inputLayout = new HorizontalLayout(sourceColCombo, targetFieldCombo, actionTypeSelect, lookupColCombo, btnAdd);
        inputLayout.setAlignItems(Alignment.END);
        inputLayout.setSpacing(true);

        layout.add(inputLayout, grid);
        dialog.add(layout);

        Button btnClose = new Button("Selesai", e -> dialog.close());
        dialog.getFooter().add(btnClose);
        dialog.open();
    }
}
