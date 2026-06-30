package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route("report-builder")
public class ReportBuilderView extends VerticalLayout {

    private final ReportMetaRepository reportMetaRepository;
    @SuppressWarnings("unused")
    private final FormMetaRepository formMetaRepository;
    private final Runnable onReportSavedListener;

    // Load & Save Toolbar
    private final ComboBox<ReportMeta> loadReportCombo = new ComboBox<>("Load Existing Report");
    private final TextField reportCodeField = new TextField("Report Code (Unique)");
    private final TextField reportTitleField = new TextField("Report Title");
    private final ComboBox<FormMeta> tableCombo = new ComboBox<>("Source Table / Form");
    private final Select<String> pageSizeSelect = new Select<>();
    private final Select<String> orientationSelect = new Select<>();

    // State
    private ReportElementMetaTemp selectedElement = null;
    private ReportElementMetaTemp draggedElement = null;
    private final List<ReportElementMetaTemp> elementsList = new ArrayList<>();

    // UI layouts
    private final Div pageCanvas = new Div(); // Simulated paper
    
    // Properties Panel
    private final VerticalLayout propertiesPanel = new VerticalLayout();
    private final FormLayout propertiesForm = new FormLayout();
    private final Span propPlaceholderLabel = new Span("Pilih elemen di kanvas untuk mengkonfigurasi propertinya.");
    private final TextField propElementValue = new TextField("Element Value / Text");
    private final ComboBox<String> propElementFieldCombo = new ComboBox<>("Table Field");
    private final TextField propColumnWidth = new TextField("Width (e.g. 120px or 25%)");
    private final ComboBox<String> propAlignment = new ComboBox<>("Alignment");
    private final ComboBox<String> propFontWeight = new ComboBox<>("Font Weight");
    private final ComboBox<String> propFormatPattern = new ComboBox<>("Format Pattern");

    public static class ReportElementMetaTemp {
        Long id;
        String bandType; // TITLE, PAGE_HEADER, COLUMN_HEADER, DETAIL, PAGE_FOOTER, SUMMARY
        String elementType; // LABEL, FIELD, SYSTEM
        String elementValue;
        String columnWidth = "120px";
        String alignment = "LEFT";
        String fontWeight = "NORMAL";
        String formatPattern;
        int colOrder = 0;
    }

    public ReportBuilderView(ReportMetaRepository reportMetaRepository, FormMetaRepository formMetaRepository) {
        this(reportMetaRepository, formMetaRepository, null);
    }

    public ReportBuilderView(ReportMetaRepository reportMetaRepository, FormMetaRepository formMetaRepository, Runnable onReportSavedListener) {
        this.reportMetaRepository = reportMetaRepository;
        this.formMetaRepository = formMetaRepository;
        this.onReportSavedListener = onReportSavedListener;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("Jasper-Style Report Designer");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "5px");

        // 1. Top Section (Load & Reset)
        HorizontalLayout loadLayout = new HorizontalLayout();
        loadLayout.setWidthFull();
        loadLayout.setAlignItems(Alignment.END);
        loadLayout.setSpacing(true);
        loadLayout.getStyle().set("margin-bottom", "10px");

        loadReportCombo.setWidth("350px");
        loadReportCombo.setPlaceholder("Pilih laporan untuk diedit...");
        loadReportCombo.setItems(reportMetaRepository.findAll());
        loadReportCombo.setItemLabelGenerator(r -> r.getReportTitle() + " (" + r.getReportCode() + ")");

        Button btnClear = new Button("Buat Baru (Reset)", VaadinIcon.REFRESH.create());
        btnClear.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClear.addClickListener(e -> {
            loadReportCombo.clear();
            reportCodeField.clear();
            reportCodeField.setReadOnly(false);
            reportTitleField.clear();
            tableCombo.clear();
            pageSizeSelect.setValue("A4");
            orientationSelect.setValue("PORTRAIT");
            elementsList.clear();
            selectElement(null);
            rebuildCanvas();
            Notification.show("Report Designer di-reset.", 3000, Notification.Position.TOP_CENTER);
        });

        loadLayout.add(loadReportCombo, btnClear);

        loadReportCombo.addValueChangeListener(event -> {
            ReportMeta selectedReport = event.getValue();
            if (selectedReport != null) {
                reportCodeField.setValue(selectedReport.getReportCode() != null ? selectedReport.getReportCode() : "");
                reportCodeField.setReadOnly(true);
                reportTitleField.setValue(selectedReport.getReportTitle() != null ? selectedReport.getReportTitle() : "");
                pageSizeSelect.setValue(selectedReport.getPageSize() != null ? selectedReport.getPageSize() : "A4");
                orientationSelect.setValue(selectedReport.getOrientation() != null ? selectedReport.getOrientation() : "PORTRAIT");

                // Find FormMeta corresponding to table
                FormMeta matchedForm = formMetaRepository.findAll().stream()
                        .filter(f -> f.getTableName().equalsIgnoreCase(selectedReport.getTableName()))
                        .findFirst().orElse(null);
                tableCombo.setValue(matchedForm);

                // Load elements
                elementsList.clear();
                if (selectedReport.getElements() != null) {
                    for (ReportElementMeta el : selectedReport.getElements()) {
                        ReportElementMetaTemp temp = new ReportElementMetaTemp();
                        temp.id = el.getId();
                        temp.bandType = el.getBandType();
                        temp.elementType = el.getElementType();
                        temp.elementValue = el.getElementValue();
                        temp.columnWidth = el.getColumnWidth() != null ? el.getColumnWidth() : "120px";
                        temp.alignment = el.getAlignment() != null ? el.getAlignment() : "LEFT";
                        temp.fontWeight = el.getFontWeight() != null ? el.getFontWeight() : "NORMAL";
                        temp.formatPattern = el.getFormatPattern();
                        temp.colOrder = el.getColOrder() != null ? el.getColOrder() : 0;
                        elementsList.add(temp);
                    }
                }
                selectElement(null);
                rebuildCanvas();
                Notification.show("Laporan berhasil dimuat: " + selectedReport.getReportCode(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        // 2. Report General Metadata Form
        FormLayout reportMetaLayout = new FormLayout();
        reportMetaLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3),
                new FormLayout.ResponsiveStep("900px", 5)
        );

        tableCombo.setItems(formMetaRepository.findAll());
        tableCombo.setItemLabelGenerator(f -> f.getFormTitle() + " (dynamic." + f.getTableName() + ")");
        tableCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                elementsList.clear();
                selectElement(null);
                rebuildCanvas();
            }
        });

        pageSizeSelect.setLabel("Page Size");
        pageSizeSelect.setItems("A4", "LETTER");
        pageSizeSelect.setValue("A4");

        orientationSelect.setLabel("Orientation");
        orientationSelect.setItems("PORTRAIT", "LANDSCAPE");
        orientationSelect.setValue("PORTRAIT");
        orientationSelect.addValueChangeListener(e -> updateCanvasOrientation());

        reportMetaLayout.add(reportCodeField, reportTitleField, tableCombo, pageSizeSelect, orientationSelect);

        // 3. Workspace Layout
        HorizontalLayout workspace = new HorizontalLayout();
        workspace.setSizeFull();
        workspace.getStyle().set("margin-top", "15px");

        // COLUMN B: WYSIWYG Print Canvas
        VerticalLayout canvasPanel = new VerticalLayout();
        canvasPanel.setHeightFull();
        canvasPanel.getStyle().set("flex-grow", "1")
                .set("background-color", "#f1f5f9")
                .set("border", "1px solid #cbd5e1")
                .set("border-radius", "8px")
                .set("padding", "20px")
                .set("overflow", "auto");

        H4 canvasTitle = new H4("Kanvas Laporan (Bands)");
        canvasTitle.getStyle().set("margin-top", "0").set("margin-bottom", "10px");
        canvasPanel.add(canvasTitle);

        // Simulated A4 sheet container
        pageCanvas.getStyle()
                .set("background-color", "#ffffff")
                .set("box-shadow", "0 10px 25px rgba(0,0,0,0.1)")
                .set("margin", "0 auto")
                .set("padding", "30px")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "15px")
                .set("transition", "all 0.3s ease");
        updateCanvasOrientation();

        canvasPanel.add(pageCanvas);

        // COLUMN C: Properties Panel
        propertiesPanel.setWidth("320px");
        propertiesPanel.setHeightFull();
        propertiesPanel.setPadding(true);
        propertiesPanel.getStyle().set("border-left", "1px solid #e2e8f0")
                .set("padding-left", "15px")
                .set("background-color", "#ffffff");

        H4 propertiesTitle = new H4("Properti Elemen");
        propertiesTitle.getStyle().set("margin-top", "0").set("margin-bottom", "15px");
        propertiesPanel.add(propertiesTitle);

        propPlaceholderLabel.getStyle().set("color", "#64748b").set("font-size", "0.9rem").set("text-align", "center");
        propertiesPanel.add(propPlaceholderLabel);

        setupPropertiesForm();
        propertiesPanel.add(propertiesForm);

        workspace.add(canvasPanel, propertiesPanel);
        workspace.setFlexGrow(1, canvasPanel);

        // Save Button
        Button btnSaveReport = new Button("Save Report & Generate Printable Template", VaadinIcon.DATABASE.create());
        btnSaveReport.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnSaveReport.setWidthFull();
        btnSaveReport.addClickListener(e -> saveReportDefinition());

        add(title, loadLayout, reportMetaLayout, workspace, btnSaveReport);
        setFlexGrow(1, workspace);

        rebuildCanvas();
        selectElement(null);
    }

    private void updateCanvasOrientation() {
        String orientation = orientationSelect.getValue();
        if ("PORTRAIT".equalsIgnoreCase(orientation)) {
            pageCanvas.getStyle().set("width", "720px").set("min-height", "990px");
        } else {
            pageCanvas.getStyle().set("width", "980px").set("min-height", "670px");
        }
    }
    private void setupPropertiesForm() {
        propertiesForm.setVisible(false);
        propertiesForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        propAlignment.setItems("LEFT", "CENTER", "RIGHT");
        propFontWeight.setItems("NORMAL", "BOLD");

        propFormatPattern.setAllowCustomValue(true);
        propFormatPattern.setItems(
                "dd/MM/yyyy", "dd-MM-yyyy", "dd MMM yyyy", "dd MMMM yyyy", "yyyy-MM-dd",
                "#,##0", "#,##0.00", "Rp #,##0", "Rp #,##0.00"
        );
        propFormatPattern.addCustomValueSetListener(e -> {
            propFormatPattern.setValue(e.getDetail());
        });

        propertiesForm.add(propElementValue, propElementFieldCombo, propColumnWidth, propAlignment, propFontWeight, propFormatPattern);

        // Listeners for live sync
        propElementValue.addValueChangeListener(e -> {
            if (selectedElement != null && e.isFromClient()) {
                selectedElement.elementValue = e.getValue().trim();
                rebuildCanvas();
            }
        });
        propElementFieldCombo.addValueChangeListener(e -> {
            if (selectedElement != null && e.isFromClient() && e.getValue() != null) {
                selectedElement.elementValue = e.getValue();
                rebuildCanvas();
            }
        });
        propColumnWidth.addValueChangeListener(e -> {
            if (selectedElement != null && e.isFromClient()) {
                selectedElement.columnWidth = e.getValue().trim();
                rebuildCanvas();
            }
        });
        propAlignment.addValueChangeListener(e -> {
            if (selectedElement != null && e.isFromClient() && e.getValue() != null) {
                selectedElement.alignment = e.getValue();
                rebuildCanvas();
            }
        });
        propFontWeight.addValueChangeListener(e -> {
            if (selectedElement != null && e.isFromClient() && e.getValue() != null) {
                selectedElement.fontWeight = e.getValue();
                rebuildCanvas();
            }
        });
        propFormatPattern.addValueChangeListener(e -> {
            if (selectedElement != null && e.isFromClient()) {
                selectedElement.formatPattern = e.getValue();
                rebuildCanvas();
            }
        });
    }

    private void selectElement(ReportElementMetaTemp temp) {
        selectedElement = temp;
        if (temp == null) {
            propPlaceholderLabel.setVisible(true);
            propertiesForm.setVisible(false);
        } else {
            propPlaceholderLabel.setVisible(false);
            propertiesForm.setVisible(true);

            propColumnWidth.setValue(temp.columnWidth != null ? temp.columnWidth : "120px");
            propAlignment.setValue(temp.alignment);
            propFontWeight.setValue(temp.fontWeight);
            propFormatPattern.setValue(temp.formatPattern != null ? temp.formatPattern : "");

            if ("LABEL".equals(temp.elementType)) {
                propFormatPattern.setVisible(false);
            } else {
                propFormatPattern.setVisible(true);
            }

            if ("FIELD".equals(temp.elementType)) {
                propElementValue.setVisible(false);
                propElementFieldCombo.setVisible(true);

                // Populate with columns from table
                FormMeta formMeta = tableCombo.getValue();
                List<String> columns = new ArrayList<>();
                if (formMeta != null && formMeta.getFields() != null) {
                    columns = formMeta.getFields().stream().map(FieldMeta::getFieldName).collect(Collectors.toList());
                }
                propElementFieldCombo.setItems(columns);
                propElementFieldCombo.setValue(temp.elementValue);
            } else {
                propElementValue.setVisible(true);
                propElementFieldCombo.setVisible(false);
                propElementValue.setValue(temp.elementValue != null ? temp.elementValue : "");
                propElementValue.setLabel(temp.elementType.equals("SYSTEM") ? "System Function / Expression" : "Element Value / Text");
            }
        }
        rebuildCanvas();
    }    private void rebuildCanvas() {
        pageCanvas.removeAll();

        // RENDER STACKED BANDS
        pageCanvas.add(buildBandLayout("TITLE", "[TITLE BAND] - Dicetak sekali pada halaman pembuka laporan"));
        pageCanvas.add(buildBandLayout("PAGE_HEADER", "[PAGE HEADER BAND] - Dicetak pada bagian atas setiap halaman"));
        pageCanvas.add(buildBandLayout("COLUMN_HEADER", "[COLUMN HEADER BAND] - Kepala kolom tabel data"));
        pageCanvas.add(buildBandLayout("DETAIL", "[DETAIL BAND] - Diulang untuk setiap baris data transaksi"));
        pageCanvas.add(buildBandLayout("PAGE_FOOTER", "[PAGE FOOTER BAND] - Dicetak pada bagian bawah setiap halaman"));
        pageCanvas.add(buildBandLayout("SUMMARY", "[SUMMARY BAND] - Dicetak sekali di akhir laporan (total/grand total)"));
    }

    private Component buildBandLayout(String bandType, String bandDescription) {
        VerticalLayout bandWrapper = new VerticalLayout();
        bandWrapper.setPadding(false);
        bandWrapper.setSpacing(false);
        bandWrapper.getStyle()
                .set("border", "1px solid #cbd5e1")
                .set("border-radius", "6px")
                .set("background-color", "#f8fafc")
                .set("position", "relative");

        // Band Header Bar
        HorizontalLayout bandHeader = new HorizontalLayout();
        bandHeader.setWidthFull();
        bandHeader.setPadding(true);
        bandHeader.getStyle()
                .set("background-color", "#e2e8f0")
                .set("border-bottom", "1px solid #cbd5e1")
                .set("border-top-left-radius", "5px")
                .set("border-top-right-radius", "5px")
                .set("padding", "6px 12px")
                .set("align-items", "center");

        Span titleLabel = new Span(bandDescription);
        titleLabel.getStyle()
                .set("font-size", "0.75rem")
                .set("font-weight", "700")
                .set("color", "#475569")
                .set("flex-grow", "1");

        Button btnAdd = new Button(VaadinIcon.PLUS.create());
        btnAdd.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        btnAdd.addClickListener(e -> openAddElementDialog(bandType));
        
        bandHeader.add(titleLabel, btnAdd);
        bandWrapper.add(bandHeader);

        // Elements Row
        HorizontalLayout elementsRow = new HorizontalLayout();
        elementsRow.setWidthFull();
        elementsRow.getStyle()
                .set("padding", "15px")
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "10px")
                .set("background-color", "#ffffff")
                .set("border-bottom-left-radius", "5px")
                .set("border-bottom-right-radius", "5px")
                .set("min-height", "60px");

        bandWrapper.add(elementsRow);

        // Filter elements in this band and sort by colOrder
        List<ReportElementMetaTemp> bandElements = elementsList.stream()
                .filter(el -> el.bandType.equals(bandType))
                .sorted((el1, el2) -> Integer.compare(el1.colOrder, el2.colOrder))
                .collect(Collectors.toList());

        if (bandElements.isEmpty()) {
            Span emptyPlaceholder = new Span("Belum ada elemen. Klik '+' untuk menambahkan.");
            emptyPlaceholder.getStyle().set("color", "#94a3b8").set("font-size", "0.8rem");
            elementsRow.add(emptyPlaceholder);
        } else {
            for (int i = 0; i < bandElements.size(); i++) {
                ReportElementMetaTemp el = bandElements.get(i);
                Component elCard = buildElementCard(el, i + 1);
                elementsRow.add(elCard);
            }
        }

        return bandWrapper;
    }

    private Component buildElementCard(ReportElementMetaTemp temp, int sequence) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle().set("width", temp.columnWidth);

        boolean isSelected = (selectedElement == temp);

        card.getStyle()
                .set("border", isSelected ? "2px solid #6366f1" : "1px solid #cbd5e1")
                .set("border-radius", "4px")
                .set("background-color", isSelected ? "#f8fafc" : "#ffffff")
                .set("cursor", "grab")
                .set("min-width", "90px")
                .set("box-shadow", isSelected ? "0 2px 8px rgba(99, 102, 241, 0.1)" : "none");

        HorizontalLayout tools = new HorizontalLayout();
        tools.setWidthFull();
        tools.setJustifyContentMode(JustifyContentMode.END);
        tools.setSpacing(true);
        Span badge = new Span("#" + sequence);
        badge.getStyle()
                .set("font-size", "0.6rem")
                .set("font-weight", "800")
                .set("color", "#4f46e5")
                .set("background-color", "#e0e7ff")
                .set("padding", "1px 4px")
                .set("border-radius", "3px");

        Span typeBadge = new Span();
        if ("LABEL".equals(temp.elementType)) {
            typeBadge.setText("LABEL");
            typeBadge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
        } else if ("FIELD".equals(temp.elementType)) {
            typeBadge.setText("TEXTFIELD");
            typeBadge.getStyle().set("background-color", "#e0e7ff").set("color", "#4f46e5");
        } else {
            typeBadge.setText("SYSTEM");
            typeBadge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
        }
        typeBadge.getStyle()
                .set("font-size", "0.6rem")
                .set("font-weight", "700")
                .set("padding", "1px 4px")
                .set("border-radius", "3px")
                .set("margin-left", "5px")
                .set("margin-right", "auto");

        tools.add(badge, typeBadge);        Button btnEdit = new Button(VaadinIcon.COG.create());
        btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnEdit.getStyle().set("cursor", "pointer");
        btnEdit.addClickListener(e -> selectElement(temp));

        Button btnDel = new Button(VaadinIcon.TRASH.create());
        btnDel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnDel.getStyle().set("cursor", "pointer");
        btnDel.addClickListener(e -> {
            elementsList.remove(temp);
            if (selectedElement == temp) {
                selectElement(null);
            }
            rebuildCanvas();
        });
        tools.add(btnEdit, btnDel);

        Span contentLabel = new Span();
        if ("LABEL".equals(temp.elementType)) {
            contentLabel.setText("\"" + temp.elementValue + "\"");
            contentLabel.getStyle().set("color", "#0f172a");
        } else if ("FIELD".equals(temp.elementType)) {
            contentLabel.setText("{" + temp.elementValue + "}");
            contentLabel.getStyle().set("color", "#2563eb").set("font-weight", "600");
        } else {
            contentLabel.setText("[" + temp.elementValue + "]");
            contentLabel.getStyle().set("color", "#16a34a").set("font-weight", "600");
        }

        contentLabel.getStyle()
                .set("font-size", "0.75rem")
                .set("margin-top", "5px")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

        if ("RIGHT".equalsIgnoreCase(temp.alignment)) {
            contentLabel.getStyle().set("text-align", "right").set("width", "100%");
        } else if ("CENTER".equalsIgnoreCase(temp.alignment)) {
            contentLabel.getStyle().set("text-align", "center").set("width", "100%");
        } else {
            contentLabel.getStyle().set("text-align", "left");
        }

        if ("BOLD".equalsIgnoreCase(temp.fontWeight)) {
            contentLabel.getStyle().set("font-weight", "700");
        }

        card.add(tools, contentLabel);
        card.getElement().addEventListener("click", e -> selectElement(temp));

        // Enable Drag & Drop reordering
        DragSource<Component> dragSource = DragSource.create(card);
        dragSource.setDraggable(true);
        dragSource.addDragStartListener(e -> {
            draggedElement = temp;
            card.getStyle().set("opacity", "0.5");
        });
        dragSource.addDragEndListener(e -> {
            draggedElement = null;
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
                    .set("border", isSelected ? "2px solid #6366f1" : "1px solid #cbd5e1")
                    .set("background-color", isSelected ? "#f8fafc" : "#ffffff");
        });

        dropTarget.addDropListener(e -> {
            if (draggedElement != null && draggedElement != temp) {
                if (draggedElement.bandType.equals(temp.bandType)) {
                    List<ReportElementMetaTemp> bandEls = elementsList.stream()
                            .filter(el -> el.bandType.equals(temp.bandType))
                            .sorted((e1, e2) -> Integer.compare(e1.colOrder, e2.colOrder))
                            .collect(Collectors.toList());

                    int srcIdx = bandEls.indexOf(draggedElement);
                    int destIdx = bandEls.indexOf(temp);
                    if (srcIdx != -1 && destIdx != -1) {
                        elementsList.remove(draggedElement);
                        
                        // Find inserting position
                        int insertPos = elementsList.indexOf(temp);
                        if (srcIdx < destIdx) {
                            elementsList.add(insertPos, draggedElement);
                        } else {
                            elementsList.add(insertPos, draggedElement);
                        }

                        // Reorder order numbers
                        List<ReportElementMetaTemp> reorderedBandEls = elementsList.stream()
                                .filter(el -> el.bandType.equals(temp.bandType))
                                .collect(Collectors.toList());
                        for (int k = 0; k < reorderedBandEls.size(); k++) {
                            reorderedBandEls.get(k).colOrder = k + 1;
                        }

                        rebuildCanvas();
                        Notification.show("Elemen diurutkan ulang!", 2000, Notification.Position.BOTTOM_END);
                    }
                } else {
                    Notification.show("Elemen hanya bisa digeser dalam band yang sama!", 3000, Notification.Position.MIDDLE);
                }
            }
            draggedElement = null;
        });

        return card;
    }

    private void openAddElementDialog(String bandType) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Element to " + bandType);
        dialog.setWidth("420px");

        FormLayout layout = new FormLayout();
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Select<String> typeSelect = new Select<>();
        typeSelect.setLabel("Element Type");
        typeSelect.setItems("Label (Static Text)", "TextField (Database Column)", "System Function (Date, Page, Aggregates)");
        typeSelect.setValue("Label (Static Text)");

        TextField valText = new TextField("Value / Text");
        valText.setPlaceholder("Enter static text...");

        ComboBox<String> fieldCombo = new ComboBox<>("Table Field");
        FormMeta formMeta = tableCombo.getValue();
        List<String> columns = new ArrayList<>();
        if (formMeta != null && formMeta.getFields() != null) {
            columns = formMeta.getFields().stream().map(FieldMeta::getFieldName).collect(Collectors.toList());
        }
        fieldCombo.setItems(columns);
        fieldCombo.setVisible(false);

        Select<String> sysSelect = new Select<>();
        sysSelect.setLabel("System Function");
        sysSelect.setItems("CURRENT_DATE", "PAGE_NUMBER", "TOTAL_PAGES", "SUM()", "AVG()", "COUNT()");
        sysSelect.setValue("CURRENT_DATE");
        sysSelect.setVisible(false);

        ComboBox<String> aggFieldCombo = new ComboBox<>("Aggregate Target Field");
        aggFieldCombo.setItems(columns);
        aggFieldCombo.setVisible(false);
        typeSelect.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val.startsWith("Label")) {
                valText.setVisible(true);
                fieldCombo.setVisible(false);
                sysSelect.setVisible(false);
                aggFieldCombo.setVisible(false);
            } else if (val.startsWith("TextField")) {
                valText.setVisible(false);
                fieldCombo.setVisible(true);
                sysSelect.setVisible(false);
                aggFieldCombo.setVisible(false);
            } else {
                valText.setVisible(false);
                fieldCombo.setVisible(false);
                sysSelect.setVisible(true);
                
                String func = sysSelect.getValue();
                boolean isAgg = "SUM()".equals(func) || "AVG()".equals(func) || "COUNT()".equals(func);
                aggFieldCombo.setVisible(isAgg);
            }
        });

        sysSelect.addValueChangeListener(e -> {
            String func = e.getValue();
            boolean isAgg = "SUM()".equals(func) || "AVG()".equals(func) || "COUNT()".equals(func);
            aggFieldCombo.setVisible(typeSelect.getValue().startsWith("System") && isAgg);
        });

        layout.add(typeSelect, valText, fieldCombo, sysSelect, aggFieldCombo);
        dialog.add(layout);

        Button btnAdd = new Button("Add Element", e -> {
            String selectedType = typeSelect.getValue();
            String elemType = "LABEL";
            String elemValue = "";

            if (selectedType.startsWith("Label")) {
                elemType = "LABEL";
                elemValue = valText.getValue().trim();
                if (elemValue.isEmpty()) {
                    Notification.show("Text tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                    return;
                }
            } else if (selectedType.startsWith("TextField")) {
                elemType = "FIELD";
                elemValue = fieldCombo.getValue();
                if (elemValue == null || elemValue.trim().isEmpty()) {
                    Notification.show("Silakan pilih field tabel!", 3000, Notification.Position.MIDDLE);
                    return;
                }
            } else {
                elemType = "SYSTEM";
                String func = sysSelect.getValue();
                if ("SUM()".equals(func) || "AVG()".equals(func) || "COUNT()".equals(func)) {
                    String col = aggFieldCombo.getValue();
                    if (col == null || col.trim().isEmpty()) {
                        Notification.show("Pilih field target untuk agregasi!", 3000, Notification.Position.MIDDLE);
                        return;
                    }
                    elemValue = func.replace("()", "(" + col + ")");
                } else {
                    elemValue = func;
                }
            }

            ReportElementMetaTemp temp = new ReportElementMetaTemp();
            temp.bandType = bandType;
            temp.elementType = elemType;
            temp.elementValue = elemValue;
            temp.columnWidth = "120px";
            temp.alignment = "LEFT";
            temp.fontWeight = "NORMAL";
            
            // Set order
            long count = elementsList.stream().filter(el -> el.bandType.equals(bandType)).count();
            temp.colOrder = (int) (count + 1);

            elementsList.add(temp);
            rebuildCanvas();
            selectElement(temp);
            dialog.close();
        });
        btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(btnCancel, btnAdd);
        dialog.open();
    }

    private void saveReportDefinition() {
        String reportCode = reportCodeField.getValue().trim();
        String reportTitle = reportTitleField.getValue().trim();
        FormMeta sourceForm = tableCombo.getValue();
        String pageSize = pageSizeSelect.getValue();
        String orientation = orientationSelect.getValue();

        if (reportCode.isEmpty() || reportTitle.isEmpty() || sourceForm == null) {
            Notification.show("Report Code, Title, dan Source Table tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (elementsList.isEmpty()) {
            Notification.show("Silakan tambahkan minimal 1 elemen untuk laporan ini!", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Delete existing report definition (cascade cleans up meta_report_element)
        if (reportMetaRepository.existsById(reportCode)) {
            reportMetaRepository.deleteById(reportCode);
        }

        ReportMeta repMeta = new ReportMeta();
        repMeta.setReportCode(reportCode);
        repMeta.setReportTitle(reportTitle);
        repMeta.setTableName(sourceForm.getTableName());
        repMeta.setPageSize(pageSize);
        repMeta.setOrientation(orientation);
        repMeta.setElements(new ArrayList<>());

        for (int i = 0; i < elementsList.size(); i++) {
            ReportElementMetaTemp temp = elementsList.get(i);
            ReportElementMeta el = new ReportElementMeta();
            el.setReportMeta(repMeta);
            el.setBandType(temp.bandType);
            el.setElementType(temp.elementType);
            el.setElementValue(temp.elementValue);
            el.setColumnWidth(temp.columnWidth);
            el.setAlignment(temp.alignment);
            el.setFontWeight(temp.fontWeight);
            el.setFormatPattern(temp.formatPattern);
            el.setColOrder(i + 1);
            repMeta.getElements().add(el);
        }

        try {
            reportMetaRepository.save(repMeta);
            Notification.show("Laporan " + reportTitle + " berhasil disimpan!", 4000, Notification.Position.TOP_CENTER);
            
            // Clear inputs
            loadReportCombo.clear();
            reportCodeField.clear();
            reportCodeField.setReadOnly(false);
            reportTitleField.clear();
            tableCombo.clear();
            pageSizeSelect.setValue("A4");
            orientationSelect.setValue("PORTRAIT");
            elementsList.clear();
            selectElement(null);
            rebuildCanvas();

            // Refresh loaded items list
            loadReportCombo.setItems(reportMetaRepository.findAll());

            if (onReportSavedListener != null) {
                onReportSavedListener.run();
            }
        } catch (Exception ex) {
            Notification.show("Gagal menyimpan laporan: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}
