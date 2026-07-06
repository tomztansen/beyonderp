package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadinerp.meta.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route("action-builder")
@com.vaadin.flow.router.RouteAlias("form-action-builder")
public class FormActionBuilderView extends VerticalLayout {

    private final FormActionMetaRepository actionRepository;
    private final FormMetaRepository formRepository;
    private final LovMetaRepository lovRepository;

    private final Grid<FormActionMeta> grid = new Grid<>(FormActionMeta.class, false);
    private final ComboBox<String> formFilter = new ComboBox<>("Filter Form");

    private final ComboBox<String> formCodeCombo = new ComboBox<>("Form Code Target");
    private final TextField actionCodeField = new TextField("Action Code (Unik)");
    private final TextField actionLabelField = new TextField("Label Tombol");
    private final ComboBox<String> targetScopeCombo = new ComboBox<>("Posisi Toolbar");
    private final ComboBox<String> iconNameCombo = new ComboBox<>("Ikon Tombol");
    private final ComboBox<String> buttonStyleCombo = new ComboBox<>("Gaya / Warna Tombol");
    private final ComboBox<String> sourceLovCodeCombo = new ComboBox<>("Sumber Data Pop-up (LOV / Form)");
    private final TextArea filterMappingField = new TextArea("Filter Mapping");
    private final TextArea targetMappingField = new TextArea("Target Mapping");

    private FormActionMeta currentAction;

    @Autowired
    public FormActionBuilderView(FormActionMetaRepository actionRepository,
                                 FormMetaRepository formRepository,
                                 LovMetaRepository lovRepository) {
        this.actionRepository = actionRepository;
        this.formRepository = formRepository;
        this.lovRepository = lovRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("Extra Toolbar Builder (Form Action Metadata)");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "5px");

        HorizontalLayout toolbar = buildToolbar();

        setupFilterAndCombos();
        setupGrid();
        VerticalLayout editorLayout = buildEditorLayout();

        VerticalLayout leftPane = new VerticalLayout(formFilter, grid);
        leftPane.setSizeFull();
        leftPane.setPadding(false);

        SplitLayout splitLayout = new SplitLayout(leftPane, editorLayout);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(55);

        add(title, toolbar, splitLayout);

        refreshGrid();
        clearForm();
    }

    private HorizontalLayout buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.getStyle()
                .set("background-color", "#f3f4f6")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "6px")
                .set("padding", "6px 12px")
                .set("align-items", "center")
                .set("gap", "15px");

        Button btnNew = new Button("Baru", VaadinIcon.PLUS_CIRCLE.create(), e -> clearForm());
        btnNew.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnNew.getStyle().set("color", "#22c55e");

        Button btnSave = new Button("Simpan", VaadinIcon.DOWNLOAD.create(), e -> saveAction());
        btnSave.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("color", "#3b82f6");

        Button btnDelete = new Button("Hapus", VaadinIcon.CLOSE_CIRCLE.create(), e -> deleteAction());
        btnDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        toolbar.add(btnNew, btnSave, btnDelete);
        return toolbar;
    }

    private void setupFilterAndCombos() {
        List<String> formCodes = new ArrayList<>(formRepository.findAll().stream()
                .map(FormMeta::getFormCode)
                .sorted()
                .toList());

        List<String> filterItems = new ArrayList<>();
        filterItems.add("[Katalog Global Reusable]");
        filterItems.addAll(formCodes);

        formFilter.setItems(filterItems);
        formFilter.setClearButtonVisible(true);
        formFilter.setPlaceholder("Tampilkan Semua Aksi...");
        formFilter.setWidthFull();
        formFilter.addValueChangeListener(e -> refreshGrid());

        formCodeCombo.setLabel("Form Code Target (Kosongkan = Katalog Reusable)");
        formCodeCombo.setItems(formCodes);
        formCodeCombo.setClearButtonVisible(true);

        targetScopeCombo.setItems("DETAIL_TOOLBAR", "MASTER_TOOLBAR", "ON_LOAD_NEW", "ON_DETAIL_ADD", "ON_LOAD_EDIT");
        targetScopeCombo.setValue("DETAIL_TOOLBAR");

        iconNameCombo.setItems("CHECK_SQUARE_O", "LIST_SELECT", "PLUS", "SEARCH", "CHECK", "DOWNLOAD", "UPLOAD", "COG", "TRASH", "STAR");

        buttonStyleCombo.setItems("PRIMARY", "SUCCESS", "ERROR", "TERTIARY");
        buttonStyleCombo.setValue("PRIMARY");

        List<String> sources = new ArrayList<>();
        lovRepository.findAll().forEach(l -> sources.add(l.getLovCode()));
        formRepository.findAll().forEach(f -> {
            if (!sources.contains(f.getFormCode())) {
                sources.add(f.getFormCode());
            }
        });
        sources.sort(String::compareToIgnoreCase);
        sourceLovCodeCombo.setItems(sources);
    }

    private void setupGrid() {
        grid.setSizeFull();
        grid.addColumn(a -> a.getFormMeta() != null ? a.getFormMeta().getFormCode() : "[Katalog Global]")
                .setHeader("Form Target").setSortable(true).setAutoWidth(true);
        grid.addColumn(FormActionMeta::getActionCode).setHeader("Action Code").setSortable(true).setAutoWidth(true);
        grid.addColumn(FormActionMeta::getActionLabel).setHeader("Label Tombol").setAutoWidth(true);
        grid.addColumn(FormActionMeta::getTargetScope).setHeader("Posisi").setAutoWidth(true);
        grid.addColumn(FormActionMeta::getSourceLovCode).setHeader("Source LOV").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                populateForm(event.getValue());
            } else {
                clearForm();
            }
        });
    }

    private VerticalLayout buildEditorLayout() {
        VerticalLayout editor = new VerticalLayout();
        editor.setPadding(true);
        editor.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "8px")
                .set("border", "1px solid #e2e8f0");

        formCodeCombo.setWidthFull();
        actionCodeField.setWidthFull();
        actionLabelField.setWidthFull();
        targetScopeCombo.setWidthFull();
        iconNameCombo.setWidthFull();
        buttonStyleCombo.setWidthFull();
        sourceLovCodeCombo.setWidthFull();

        filterMappingField.setWidthFull();
        filterMappingField.setPlaceholder("Contoh: status:'Active',customer_id:header.cust_code");
        filterMappingField.setHeight("70px");

        targetMappingField.setWidthFull();
        targetMappingField.setPlaceholder("Contoh: item_code:code,price:sell_price,qty:1");
        targetMappingField.setHeight("80px");

        editor.add(formCodeCombo, actionCodeField, actionLabelField, targetScopeCombo,
                iconNameCombo, buttonStyleCombo, sourceLovCodeCombo, filterMappingField, targetMappingField);

        return editor;
    }

    private void refreshGrid() {
        List<FormActionMeta> items;
        String filterVal = formFilter.getValue();
        if (filterVal != null && !filterVal.isBlank()) {
            if ("[Katalog Global Reusable]".equals(filterVal)) {
                items = actionRepository.findByFormMetaIsNull();
            } else {
                items = actionRepository.findByFormMeta_FormCode(filterVal);
            }
        } else {
            items = actionRepository.findAll();
        }
        grid.setItems(items);
    }

    private void populateForm(FormActionMeta action) {
        this.currentAction = action;
        formCodeCombo.setValue(action.getFormMeta() != null ? action.getFormMeta().getFormCode() : null);
        actionCodeField.setValue(action.getActionCode() != null ? action.getActionCode() : "");
        actionLabelField.setValue(action.getActionLabel() != null ? action.getActionLabel() : "");
        targetScopeCombo.setValue(action.getTargetScope() != null ? action.getTargetScope() : "DETAIL_TOOLBAR");
        iconNameCombo.setValue(action.getIconName());
        buttonStyleCombo.setValue(action.getButtonStyle() != null ? action.getButtonStyle() : "PRIMARY");
        sourceLovCodeCombo.setValue(action.getSourceLovCode());
        filterMappingField.setValue(action.getFilterMapping() != null ? action.getFilterMapping() : "");
        targetMappingField.setValue(action.getTargetMapping() != null ? action.getTargetMapping() : "");
    }

    private void clearForm() {
        grid.asSingleSelect().clear();
        this.currentAction = null;
        formCodeCombo.clear();
        actionCodeField.clear();
        actionLabelField.clear();
        targetScopeCombo.setValue("DETAIL_TOOLBAR");
        iconNameCombo.clear();
        buttonStyleCombo.setValue("PRIMARY");
        sourceLovCodeCombo.clear();
        filterMappingField.clear();
        targetMappingField.clear();
    }

    private void saveAction() {
        if (actionCodeField.getValue().isBlank() || actionLabelField.getValue().isBlank()) {
            Notification.show("Action Code dan Label Tombol wajib diisi!", 3000, Notification.Position.MIDDLE);
            return;
        }

        FormMeta targetForm = null;
        if (formCodeCombo.getValue() != null && !formCodeCombo.getValue().isBlank()) {
            targetForm = formRepository.findById(formCodeCombo.getValue()).orElse(null);
            if (targetForm == null) {
                Notification.show("Form Code Target tidak ditemukan!", 3000, Notification.Position.MIDDLE);
                return;
            }
        }

        if (currentAction == null) {
            currentAction = new FormActionMeta();
        }

        currentAction.setFormMeta(targetForm);
        currentAction.setActionCode(actionCodeField.getValue().trim());
        currentAction.setActionLabel(actionLabelField.getValue().trim());
        currentAction.setTargetScope(targetScopeCombo.getValue());
        currentAction.setIconName(iconNameCombo.getValue());
        currentAction.setButtonStyle(buttonStyleCombo.getValue());
        currentAction.setSourceLovCode(sourceLovCodeCombo.getValue());
        currentAction.setFilterMapping(filterMappingField.getValue());
        currentAction.setTargetMapping(targetMappingField.getValue());

        try {
            actionRepository.save(currentAction);
            Notification.show("Extra Toolbar berhasil disimpan" + (targetForm == null ? " sebagai Katalog Global!" : "!"), 3000, Notification.Position.BOTTOM_END);
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Gagal menyimpan: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void deleteAction() {
        if (currentAction == null || currentAction.getId() == null) {
            Notification.show("Pilih aksi yang akan dihapus dari tabel!", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            actionRepository.delete(currentAction);
            Notification.show("Extra Toolbar berhasil dihapus!", 3000, Notification.Position.BOTTOM_END);
            refreshGrid();
            clearForm();
        } catch (Exception e) {
            Notification.show("Gagal menghapus: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }
}
