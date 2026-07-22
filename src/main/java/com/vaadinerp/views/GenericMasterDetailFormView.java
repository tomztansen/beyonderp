package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadinerp.components.ComponentFactory;
import com.vaadinerp.components.LovComboBox;
import com.vaadinerp.components.LovSelect;
import com.vaadinerp.components.BandboxField;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.util.FormulaEvaluator;
import com.vaadin.flow.component.notification.Notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("masterdetail")
public class GenericMasterDetailFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final FormMetaRepository formMetaRepository;
    private final DynamicDataService dynamicDataService;
    private com.vaadinerp.security.service.SessionSecurityService securityService;

    private Binder<Map<String, Object>> formBinder;
    private final VerticalLayout formLayout = new VerticalLayout();
    private final Grid<Map<String, Object>> masterGrid = new Grid<>();
    private final Grid<Map<String, Object>> detailsGrid = new Grid<>();

    private final List<Map<String, Object>> detailsList = new ArrayList<>();
    private final List<Map<String, Object>> deletedDetailsList = new ArrayList<>();
    private final Map<String, Component> formComponents = new HashMap<>();
    private final Map<String, Component> detailEditorComponents = new HashMap<>();

    private HorizontalLayout toolbar;
    private final HorizontalLayout extraActionsContainer = new HorizontalLayout();
    private TabSheet tabSheet;
    private Tab historisTab;
    private Tab transaksiTab;
    private H3 title;

    private String currentFormCode;
    private FormMeta currentFormDef;
    private Runnable closeHandler;
    private com.vaadin.flow.router.QueryParameters queryParameters;

    // Master Grid Filters and Reordering State
    private final List<Map<String, Object>> masterGridItems = new ArrayList<>();
    private final List<Map<String, Object>> allMasterGridItems = new ArrayList<>();

    private static class FilterCriteria {
        String operator = "Contains";
        String value = "";
    }

    private final Map<String, FilterCriteria> filterValues = new HashMap<>();
    private Map<String, Object> draggedItem;
    private final Map<Grid.Column<Map<String, Object>>, String> columnToFieldNameMap = new HashMap<>();
    private com.vaadinerp.components.PaginationBar paginationBar;
    private String currentSortField;
    private String currentSortDir;

    private final HorizontalLayout masterGridToolbar = new HorizontalLayout();
    private final HorizontalLayout dynamicDetailsActionsLayout = new HorizontalLayout();

    // Details Grid Filters and Reordering State
    private final List<Map<String, Object>> filteredDetailsList = new ArrayList<>();
    private final Map<String, FilterCriteria> detailsFilterValues = new HashMap<>();
    private Map<String, Object> draggedDetailItem;
    private final Map<Grid.Column<Map<String, Object>>, String> detailsColumnToFieldNameMap = new HashMap<>();
    private boolean isEvaluatingFormulas = false;
    private boolean isEvaluatingGridFormulas = false;
    private final Map<Grid.Column<Map<String, Object>>, java.util.function.Function<Map<String, Object>, String>> masterColGetterMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Grid.Column<Map<String, Object>>, java.util.function.Function<Map<String, Object>, String>> detailColGetterMap = new java.util.concurrent.ConcurrentHashMap<>();

    private final Map<String, Map<String, String>> lovLabelMapCache = new HashMap<>();
    private final Map<String, String> fieldNameToLovCodeMap = new HashMap<>();

    // Flag to prevent cascading filter listeners from clearing child LOV values
    // during data loading
    private boolean isLoadingExistingData = false;

    // Registrations for listener deduplication
    private com.vaadin.flow.shared.Registration masterDoubleClickReg;
    private com.vaadin.flow.shared.Registration masterDragStartReg;
    private com.vaadin.flow.shared.Registration masterDropReg;
    private com.vaadin.flow.shared.Registration masterDragEndReg;
    private com.vaadin.flow.shared.Registration masterColReorderReg;

    private com.vaadin.flow.shared.Registration detailDragStartReg;
    private com.vaadin.flow.shared.Registration detailDropReg;
    private com.vaadin.flow.shared.Registration detailDragEndReg;
    private com.vaadin.flow.shared.Registration detailColReorderReg;

    private Object getMapValIgnoreCase(Map<String, Object> rec, String col) {
        if (col == null || rec == null)
            return null;
        if (rec.containsKey(col))
            return rec.get(col);
        for (Map.Entry<String, Object> e : rec.entrySet()) {
            if (e.getKey().equalsIgnoreCase(col))
                return e.getValue();
        }
        return null;
    }

    private String getLovDisplayLabel(String lovCode, String val) {
        if (val == null || val.trim().isEmpty() || lovCode == null || lovCode.trim().isEmpty())
            return val != null ? val : "";
        String strVal = val.trim();
        Map<String, String> map = lovLabelMapCache.computeIfAbsent(lovCode, code -> {
            Map<String, String> res = new HashMap<>();
            dynamicDataService.getLovMeta(code).ifPresent(lovMeta -> {
                java.util.List<Map<String, Object>> records = dynamicDataService.fetchAllLovRecords(lovMeta);
                String valCol = lovMeta.getValueColumn() != null && !lovMeta.getValueColumn().isBlank()
                        ? lovMeta.getValueColumn().trim()
                        : "id";
                String lblCol = lovMeta.getLabelColumn() != null && !lovMeta.getLabelColumn().isBlank()
                        ? lovMeta.getLabelColumn().trim()
                        : valCol;
                for (Map<String, Object> rec : records) {
                    Object v = getMapValIgnoreCase(rec, valCol);
                    if (v == null && rec.containsKey("id"))
                        v = rec.get("id");
                    if (v != null) {
                        Object l = getMapValIgnoreCase(rec, lblCol);
                        if (l == null || l.toString().trim().isEmpty()) {
                            if (getMapValIgnoreCase(rec, "code") != null)
                                l = getMapValIgnoreCase(rec, "code");
                            else if (getMapValIgnoreCase(rec, "name") != null)
                                l = getMapValIgnoreCase(rec, "name");
                            else
                                l = v;
                        }
                        res.put(v.toString().trim(), l.toString().trim());
                    }
                }
            });
            return res;
        });

        if (strVal.contains(",")) {
            return java.util.Arrays.stream(strVal.split(","))
                    .map(s -> s != null ? s.trim() : "")
                    .map(item -> map.getOrDefault(item, item))
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        return map.getOrDefault(strVal, strVal);
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    /**
     * Menyembunyikan judul H3 di dalam view.
     * Dipanggil ketika view di-embed di dalam tab portal yang sudah menampilkan
     * judul.
     */
    public void hideTitle() {
        title.setVisible(false);
    }

    /**
     * Menyembunyikan tab Historis dan langsung mengarahkan tampilan ke tab
     * Transaksi.
     */
    public void hideHistorisTab() {
        if (historisTab != null) {
            historisTab.setVisible(false);
        }
        if (tabSheet != null && transaksiTab != null) {
            tabSheet.setSelectedTab(transaksiTab);
        }
    }

    public void refreshBinderBean(Map<String, Object> updatedBean) {
        if (formBinder != null && updatedBean != null) {
            formBinder.readBean(updatedBean);
        }
    }

    public void setComponentEnabled(String fieldName, boolean enabled) {
        Component comp = null;
        if (fieldName.startsWith("header.")) {
            comp = formComponents != null ? formComponents.get(fieldName.substring(7)) : null;
        } else if (fieldName.startsWith("detail.")) {
            comp = detailEditorComponents != null ? detailEditorComponents.get(fieldName.substring(7)) : null;
        } else {
            comp = formComponents != null ? formComponents.get(fieldName) : null;
            if (comp == null) {
                comp = detailEditorComponents != null ? detailEditorComponents.get(fieldName) : null;
            }
        }

        if (comp != null && comp instanceof com.vaadin.flow.component.HasEnabled hasEnabled) {
            hasEnabled.setEnabled(enabled);
        }
    }

    public void setComponentReadOnly(String fieldName, boolean readOnly) {
        Component comp = null;
        if (fieldName.startsWith("header.")) {
            comp = formComponents != null ? formComponents.get(fieldName.substring(7)) : null;
        } else if (fieldName.startsWith("detail.")) {
            comp = detailEditorComponents != null ? detailEditorComponents.get(fieldName.substring(7)) : null;
        } else {
            comp = formComponents != null ? formComponents.get(fieldName) : null;
            if (comp == null) {
                comp = detailEditorComponents != null ? detailEditorComponents.get(fieldName) : null;
            }
        }

        if (comp != null) {
            com.vaadinerp.components.ComponentFactory.setComponentReadOnly(comp, readOnly);
        }
    }

    public GenericMasterDetailFormView(FormMetaRepository formMetaRepository, DynamicDataService dynamicDataService) {
        this(formMetaRepository, dynamicDataService, null);
    }

    public GenericMasterDetailFormView(FormMetaRepository formMetaRepository, DynamicDataService dynamicDataService,
            com.vaadinerp.security.service.SessionSecurityService securityService) {
        this.formMetaRepository = formMetaRepository;
        this.dynamicDataService = dynamicDataService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "4px").set("padding", "8px 12px");

        title = new H3("Loading Master-Detail...");
        title.getStyle().set("margin", "0").set("padding", "0");

        // Setup Toolbar
        toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.getStyle()
                .set("background-color", "#f3f4f6")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "6px")
                .set("padding", "6px 12px")
                .set("align-items", "center")
                .set("gap", "15px");

        masterGrid.setSizeFull();
        masterGrid.setMinHeight("300px");
        masterGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        masterGrid.setPageSize(25);
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(masterGrid);

        // Setup Tab Sheet layouts
        VerticalLayout historisLayout = new VerticalLayout();
        historisLayout.setSizeFull();
        historisLayout.setPadding(false);
        historisLayout.setSpacing(false);
        historisLayout.getStyle().set("gap", "4px");
        paginationBar = new com.vaadinerp.components.PaginationBar(e -> applyFilters());
        historisLayout.add(masterGridToolbar, masterGrid, paginationBar);
        historisLayout.expand(masterGrid);

        VerticalLayout transaksiLayout = new VerticalLayout();
        transaksiLayout.setWidthFull();
        transaksiLayout.setPadding(false);
        transaksiLayout.setSpacing(false);
        transaksiLayout.getStyle().set("gap", "6px");

        formLayout.setWidthFull();
        formLayout.setPadding(false);
        formLayout.setSpacing(false);
        formLayout.getStyle().set("gap", "6px");

        // Details Toolbar
        HorizontalLayout detailsToolbar = new HorizontalLayout();
        detailsToolbar.setWidthFull();
        detailsToolbar.setAlignItems(Alignment.CENTER);
        H4 detailTitle = new H4("Rincian / Details");
        detailTitle.getStyle().set("margin", "0");

        Button btnAddRow = new Button("Tambah Baris", VaadinIcon.PLUS.create());
        btnAddRow.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnAddRow.getStyle().set("font-weight", "500").set("color", "#374151");
        if (btnAddRow.getIcon() instanceof com.vaadin.flow.component.icon.Icon icAdd) {
            icAdd.getStyle().set("color", "#22c55e").set("font-size", "1.1rem");
        }

        Button btnDeleteRow = new Button("Hapus Baris", VaadinIcon.TRASH.create());
        btnDeleteRow.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnDeleteRow.getStyle().set("font-weight", "500").set("color", "#374151");
        if (btnDeleteRow.getIcon() instanceof com.vaadin.flow.component.icon.Icon icDel) {
            icDel.getStyle().set("color", "#ef4444").set("font-size", "1.1rem");
        }

        Button btnResetDetailsGrid = new Button("Reset Layout Grid", VaadinIcon.ROTATE_LEFT.create());
        btnResetDetailsGrid.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnResetDetailsGrid.getStyle().set("font-weight", "500").set("color", "#374151");
        if (btnResetDetailsGrid.getIcon() instanceof com.vaadin.flow.component.icon.Icon icReset) {
            icReset.getStyle().set("color", "#3b82f6").set("font-size", "1.1rem");
        }
        btnResetDetailsGrid.addClickListener(e -> {
            if (currentFormDef != null) {
                dynamicDataService.resetUserGridOrder(currentFormCode, "detailsGrid");
                buildDetailsGrid(currentFormDef);
                Notification.show("Layout grid rincian dikembalikan ke default!", 2000,
                        Notification.Position.BOTTOM_END);
            }
        });

        com.vaadin.flow.component.html.Anchor btnExportDetailsExcel = com.vaadinerp.components.StandardGridUtils
                .createExportExcelButton(detailsGrid, "details_export", detailColGetterMap);
        btnExportDetailsExcel.getStyle().set("margin-left", "auto");
        detailsToolbar.add(detailTitle, btnAddRow, btnDeleteRow, dynamicDetailsActionsLayout, btnExportDetailsExcel,
                btnResetDetailsGrid);

        detailsGrid.setWidthFull();
        detailsGrid.setMinHeight("300px");
        detailsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(detailsGrid);

        transaksiLayout.add(formLayout, detailsToolbar, detailsGrid);

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        historisTab = tabSheet.add("Historis", historisLayout);
        transaksiTab = tabSheet.add("Transaksi", transaksiLayout);

        tabSheet.addSelectedChangeListener(event -> {
            if (currentFormDef != null) {
                if (event.getSelectedTab() == historisTab) {
                    refreshMasterGridData();
                }
                boolean isUpdate = false;
                Map<String, Object> bean = formBinder != null ? formBinder.getBean() : null;
                String pk = currentFormDef.getPrimaryKey() != null ? currentFormDef.getPrimaryKey() : "id";
                if (bean != null && bean.containsKey(pk) && bean.get(pk) != null
                        && !bean.get(pk).toString().trim().isEmpty()) {
                    isUpdate = true;
                }
                updateTitle(currentFormDef, isUpdate);
            }
        });

        add(title, toolbar, tabSheet);
        expand(tabSheet);

        // Bind Details Row Add/Delete Action
        btnAddRow.addClickListener(e -> {
            Editor<Map<String, Object>> editor = detailsGrid.getEditor();
            if (editor.isOpen()) {
                Map<String, Object> prevItem = editor.getItem();
                editor.cancel();
                if (prevItem != null) {
                    calculateRowTotal(prevItem);
                    detailsGrid.getDataProvider().refreshItem(prevItem);
                }
            }
            Map<String, Object> newRow = new HashMap<>();
            newRow.put("_tempId", java.util.UUID.randomUUID().toString());

            // Populate cross-scope LOV target mappings from Header to new Detail row
            Map<String, Object> headerBean = formBinder != null ? formBinder.getBean() : null;
            if (headerBean != null && currentFormDef != null && currentFormDef.getFields() != null) {
                for (FieldMeta field : currentFormDef.getFields()) {
                    if (!field.isDetail() && field.getLovTargets() != null) {
                        Object selRecord = getValueCaseInsensitive(headerBean, field.getFieldName() + "_record");
                        for (com.vaadinerp.meta.FieldLovTargetMeta target : field.getLovTargets()) {
                            if (target.getTargetField() != null
                                    && target.getTargetField().toLowerCase().startsWith("detail.")) {
                                String dCol = target.getTargetField().substring(7);
                                Object val = null;
                                if (selRecord instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> selMap = (Map<String, Object>) selRecord;
                                    val = getValueCaseInsensitive(selMap, target.getSourceColumn());
                                }
                                if (val == null) {
                                    val = getValueCaseInsensitive(headerBean,
                                            field.getFieldName() + "." + target.getSourceColumn());
                                }
                                if (val != null) {
                                    putValueCaseInsensitive(newRow, dCol, val);
                                }
                            }
                        }
                    }
                }
            }
            executeOnLoadActions("ON_DETAIL_ADD", newRow);
            calculateRowTotal(newRow);

            detailsList.add(newRow);
            applyDetailsFilters();
            applyMasterFiltersToDetailEditors(currentFormDef);

            UI.getCurrent().getPage().executeJs(
                    "var grid = $0; var targetRowIdx = $1; " +
                            "function tryEdit(attempt) { " +
                            "  var body = grid.shadowRoot.querySelector('[part=\"body\"]'); " +
                            "  if(!body) { if(attempt < 60) setTimeout(function(){tryEdit(attempt+1)},30); return; } " +
                            "  var rows = body.querySelectorAll('[part=\"row\"]'); " +
                            "  var targetRow = null; " +
                            "  for(var i=0; i<rows.length; i++) { " +
                            "    if(rows[i].getAttribute('part').indexOf('row') !== -1) { " +
                            "      if(i === rows.length - 1 || rows[i].getAttribute('rowindex') == targetRowIdx) { " +
                            "        targetRow = rows[i]; break; " +
                            "      } " +
                            "    } " +
                            "  } " +
                            "  if(!targetRow) { if(attempt < 60) setTimeout(function(){tryEdit(attempt+1)},30); return; } "
                            +
                            "  try { targetRow.scrollIntoView({behavior:'smooth', block:'center'}); } catch(e){} " +
                            "  var cells = targetRow.querySelectorAll('td'); " +
                            "  if(cells.length > 0) { " +
                            "    cells[0].dispatchEvent(new MouseEvent('dblclick', {bubbles:true, cancelable:true})); "
                            +
                            "  } " +
                            "} " +
                            "tryEdit(0);",
                    detailsGrid.getElement(), detailsList.size() - 1);
        });

        btnDeleteRow.addClickListener(e -> {
            java.util.Set<Map<String, Object>> selectedItems = detailsGrid.getSelectedItems();
            if (selectedItems != null && !selectedItems.isEmpty()) {
                showConfirmDialog("Konfirmasi Hapus Rincian",
                        "Apakah Anda yakin ingin menghapus " + selectedItems.size() + " baris rincian terpilih ini?",
                        () -> {
                            detailsList.removeAll(selectedItems);
                            deletedDetailsList.addAll(selectedItems);
                            applyDetailsFilters();
                            detailsGrid.deselectAll();
                        });
            } else {
                Notification.show("Pilih baris rincian yang ingin dihapus terlebih dahulu.", 3000,
                        Notification.Position.MIDDLE);
            }
        });
    }

    @Override
    public void setParameter(BeforeEvent event, String formCode) {
        FormMeta formDef = formMetaRepository.findById(formCode).orElse(null);
        if (formDef == null || !"MASTER_DETAIL".equalsIgnoreCase(formDef.getFormType())) {
            title.setText("Form Master-Detail " + formCode + " tidak ditemukan!");
            return;
        }

        this.currentFormCode = formCode;
        this.currentFormDef = formDef;
        this.queryParameters = event != null && event.getLocation() != null ? event.getLocation().getQueryParameters()
                : null;

        formBinder = new Binder<>();
        formBinder.setBean(new HashMap<>());
        formBinder.addValueChangeListener(e -> evaluateFormulas());

        title.setText(formDef.getFormTitle() != null ? formDef.getFormTitle() : "Form: " + formCode);

        buildToolbar(formDef);
        buildMasterForm(formDef);
        buildMasterGrid(formDef);
        buildDetailsGrid(formDef);
        buildDetailsActions(formDef);
        refreshMasterGridData();
        executeOnLoadActions("ON_LOAD_NEW");
    }

    private void buildDetailsActions(FormMeta formDef) {
        dynamicDetailsActionsLayout.removeAll();
        if (formDef == null || dynamicDataService == null)
            return;
        List<com.vaadinerp.meta.FormActionMeta> actions = dynamicDataService.getFormActions(formDef.getFormCode(),
                "DETAIL_TOOLBAR");
        if (actions == null || actions.isEmpty())
            return;

        java.util.Map<String, java.util.List<com.vaadinerp.meta.FormActionMeta>> groupedActions = new java.util.LinkedHashMap<>();
        List<com.vaadinerp.meta.FormActionMeta> standaloneActions = new ArrayList<>();

        for (com.vaadinerp.meta.FormActionMeta act : actions) {
            if (act.getMenuGroup() != null && !act.getMenuGroup().isBlank()) {
                groupedActions.computeIfAbsent(act.getMenuGroup().trim(), k -> new ArrayList<>()).add(act);
            } else {
                standaloneActions.add(act);
            }
        }

        for (com.vaadinerp.meta.FormActionMeta act : standaloneActions) {
            Icon icon = null;
            if (act.getIconName() != null && !act.getIconName().isBlank()) {
                try {
                    icon = VaadinIcon.valueOf(act.getIconName().toUpperCase()).create();
                } catch (Exception ignored) {
                }
            }
            if (icon != null) {
                icon.getStyle().set("color", "#2563eb").set("font-size", "1.1rem");
            }
            Button actBtn = icon != null ? new Button(act.getActionLabel(), icon) : new Button(act.getActionLabel());
            actBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            actBtn.getStyle().set("font-weight", "500").set("color", "#374151");
            actBtn.addClickListener(e -> {
                if ("GROOVY_SCRIPT".equalsIgnoreCase(act.getActionType())
                        || (act.getScriptContent() != null && !act.getScriptContent().isBlank())) {
                    executeToolbarAction(act);
                } else {
                    Editor<Map<String, Object>> editor = detailsGrid.getEditor();
                    if (editor.isOpen()) {
                        editor.cancel();
                    }
                    Map<String, Object> headerBean = formBinder.getBean();
                    com.vaadinerp.components.DynamicPickerPopupDialog dlg = new com.vaadinerp.components.DynamicPickerPopupDialog(
                            act, dynamicDataService, headerBean, selectedRecords -> {
                                for (Map<String, Object> srcRec : selectedRecords) {
                                    Map<String, Object> newRow = new HashMap<>();
                                    newRow.put("_tempId", java.util.UUID.randomUUID().toString());
                                    newRow.put("lineno", getMaxLineNoFromList(detailsList));
                                    applyTargetMapping(newRow, newRow, srcRec, act.getTargetMapping());
                                    detailsList.add(newRow);
                                }
                                applyDetailsFilters();
                                applyMasterFiltersToDetailEditors(currentFormDef);
                            });
                    dlg.open();
                }
            });
            dynamicDetailsActionsLayout.add(actBtn);
        }

        for (Map.Entry<String, List<com.vaadinerp.meta.FormActionMeta>> entry : groupedActions.entrySet()) {
            com.vaadin.flow.component.menubar.MenuBar menuBar = new com.vaadin.flow.component.menubar.MenuBar();
            menuBar.addThemeVariants(com.vaadin.flow.component.menubar.MenuBarVariant.LUMO_SMALL,
                    com.vaadin.flow.component.menubar.MenuBarVariant.LUMO_TERTIARY);

            HorizontalLayout menuBtnLayout = new HorizontalLayout();
            menuBtnLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
            menuBtnLayout.setSpacing(true);
            com.vaadin.flow.component.icon.Icon grpIcon = VaadinIcon.FILE_TEXT.create();
            grpIcon.getStyle().set("color", "#2563eb").set("font-size", "1.1rem");
            com.vaadin.flow.component.icon.Icon chevronIcon = VaadinIcon.CHEVRON_DOWN.create();
            chevronIcon.setSize("12px");
            com.vaadin.flow.component.html.Span grpSpan = new com.vaadin.flow.component.html.Span(entry.getKey());
            grpSpan.getStyle().set("font-weight", "500").set("color", "#374151");
            menuBtnLayout.add(grpIcon, grpSpan, chevronIcon);
            com.vaadin.flow.component.contextmenu.MenuItem parentItem = menuBar.addItem(menuBtnLayout);

            com.vaadin.flow.component.contextmenu.SubMenu subMenu = parentItem.getSubMenu();
            for (com.vaadinerp.meta.FormActionMeta act : entry.getValue()) {
                Icon icon = null;
                if (act.getIconName() != null && !act.getIconName().isBlank()) {
                    try {
                        icon = VaadinIcon.valueOf(act.getIconName().toUpperCase()).create();
                    } catch (Exception ignored) {
                    }
                }
                if (icon == null) {
                    icon = VaadinIcon.FILE_TEXT.create();
                }
                icon.getStyle().set("color", "#2563eb").set("font-size", "1rem");
                com.vaadin.flow.component.Component itemLabel = new HorizontalLayout(icon,
                        new com.vaadin.flow.component.html.Span(act.getActionLabel()));
                subMenu.addItem(itemLabel, e -> {
                    if ("GROOVY_SCRIPT".equalsIgnoreCase(act.getActionType())
                            || (act.getScriptContent() != null && !act.getScriptContent().isBlank())) {
                        executeToolbarAction(act);
                    } else {
                        Editor<Map<String, Object>> editor = detailsGrid.getEditor();
                        if (editor.isOpen()) {
                            editor.cancel();
                        }
                        Map<String, Object> headerBean = formBinder.getBean();
                        com.vaadinerp.components.DynamicPickerPopupDialog dlg = new com.vaadinerp.components.DynamicPickerPopupDialog(
                                act, dynamicDataService, headerBean, selectedRecords -> {
                                    for (Map<String, Object> srcRec : selectedRecords) {
                                        Map<String, Object> newRow = new HashMap<>();
                                        newRow.put("_tempId", java.util.UUID.randomUUID().toString());
                                        newRow.put("lineno", getMaxLineNoFromList(detailsList));
                                        applyTargetMapping(newRow, newRow, srcRec, act.getTargetMapping());
                                        detailsList.add(newRow);
                                    }
                                    applyDetailsFilters();
                                    applyMasterFiltersToDetailEditors(currentFormDef);
                                });
                        dlg.open();
                    }
                });
            }
            dynamicDetailsActionsLayout.add(menuBar);
        }
    }

    @SafeVarargs
    private void executeOnLoadActions(String scope, Map<String, Object>... targetRows) {
        if (currentFormDef == null || dynamicDataService == null)
            return;
        List<com.vaadinerp.meta.FormActionMeta> actions = dynamicDataService.getFormActions(currentFormCode, scope);
        if (actions == null || actions.isEmpty())
            return;

        Map<String, Object> headerBean = formBinder != null ? formBinder.getBean() : new HashMap<>();
        for (com.vaadinerp.meta.FormActionMeta act : actions) {
            try {
                List<Map<String, Object>> fetchedRecords = dynamicDataService.fetchLovDataWithActionFilters(
                        act.getSourceLovCode(),
                        act.getFilterMapping(),
                        headerBean,
                        "");
                if (fetchedRecords != null && !fetchedRecords.isEmpty()) {
                    Map<String, Object> srcRec = fetchedRecords.get(0);
                    if ("ON_DETAIL_ADD".equalsIgnoreCase(scope)) {
                        for (Map<String, Object> targetRow : targetRows) {
                            if (targetRow != null) {
                                applyTargetMapping(targetRow, targetRow, srcRec, act.getTargetMapping());
                                calculateRowTotal(targetRow);
                            }
                        }
                    } else {
                        // ON_LOAD_NEW, ON_LOAD_EDIT
                        if (headerBean != null) {
                            applyTargetMapping(headerBean, srcRec, act.getTargetMapping());
                            if (formBinder != null) {
                                isLoadingExistingData = true;
                                try {
                                    formBinder.readBean(headerBean);
                                } finally {
                                    isLoadingExistingData = false;
                                }
                            }
                            evaluateFormulas();
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error executing OnLoad action [" + act.getActionCode() + "] in scope [" + scope
                        + "]: " + ex.getMessage());
            }
        }
    }

    private void applyTargetMapping(Map<String, Object> destRow, Map<String, Object> srcRecord, String targetMapping) {
        applyTargetMapping(destRow, null, srcRecord, targetMapping);
    }

    private void applyTargetMapping(Map<String, Object> destRow, Map<String, Object> detailRow,
            Map<String, Object> srcRecord, String targetMapping) {
        if (targetMapping == null || targetMapping.trim().isEmpty())
            return;
        String clean = targetMapping.trim();
        if (clean.startsWith("{") && clean.endsWith("}")) {
            clean = clean.substring(1, clean.length() - 1).trim();
        }
        String[] pairs = com.vaadinerp.util.FormulaEvaluator.splitTopLevelComma(clean);
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length < 2)
                kv = pair.split("=", 2);
            if (kv.length == 2) {
                String destCol = kv[0].replaceAll("[\"']", "").trim();
                String srcCol = kv[1].replaceAll("[\"']", "").trim();
                if (srcCol.toLowerCase().startsWith("source.")) {
                    srcCol = srcCol.substring(7);
                }
                Map<String, Object> evalRow = (destCol.toLowerCase().startsWith("detail.") && detailRow != null)
                        ? detailRow
                        : destRow;
                Object val = null;
                if (srcRecord != null && !srcCol.toLowerCase().contains("coalesce(")
                        && !srcCol.toLowerCase().contains("ifnull(") && !srcCol.contains("+") && !srcCol.contains("*")
                        && !srcCol.contains("/")) {
                    val = getValueCaseInsensitive(srcRecord, srcCol);
                }
                if (val == null && srcCol != null && !srcCol.isBlank()) {
                    if (srcCol.toLowerCase().contains("coalesce(") || srcCol.toLowerCase().contains("ifnull(")
                            || ((srcCol.contains("+") || srcCol.contains("*") || srcCol.contains("/")
                                    || (srcCol.contains("-") && !srcCol.startsWith("-")))
                                    && srcCol.matches(".*[a-zA-Z\\(\\)].*"))) {
                        val = com.vaadinerp.util.FormulaEvaluator.evaluateTargetExpression(srcCol, srcRecord, evalRow);
                    } else if ((srcCol.startsWith("'") && srcCol.endsWith("'"))
                            || (srcCol.startsWith("\"") && srcCol.endsWith("\""))) {
                        if (srcCol.length() >= 2)
                            val = srcCol.substring(1, srcCol.length() - 1);
                    } else if ("true".equalsIgnoreCase(srcCol) || "false".equalsIgnoreCase(srcCol)) {
                        val = Boolean.parseBoolean(srcCol);
                    } else if ("null".equalsIgnoreCase(srcCol)) {
                        val = null;
                    } else {
                        try {
                            if (srcCol.contains(".")) {
                                val = new java.math.BigDecimal(srcCol);
                            } else {
                                val = Integer.parseInt(srcCol);
                            }
                        } catch (Exception e) {
                            try {
                                val = Long.parseLong(srcCol);
                            } catch (Exception e2) {
                                // Bukan angka literal, biarkan val null
                            }
                        }
                    }
                }
                if (destCol.toLowerCase().startsWith("detail.")) {
                    String dCol = destCol.substring(7);
                    if (detailRow != null) {
                        putValueCaseInsensitive(detailRow, dCol, val);
                        calculateRowTotal(detailRow);
                    } else {
                        for (Map<String, Object> row : detailsList) {
                            putValueCaseInsensitive(row, dCol, val);
                            calculateRowTotal(row);
                        }
                        applyDetailsFilters();
                    }
                } else {
                    putValueCaseInsensitive(destRow, destCol, val);
                }
            }
        }
    }

    private int getMaxLineNoFromList(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty())
            return 0;
        int max = 0;
        for (Map<String, Object> row : list) {
            if (row != null) {
                Object val = getValueCaseInsensitive(row, "lineno");
                if (val == null)
                    val = getValueCaseInsensitive(row, "seq");
                if (val == null)
                    val = getValueCaseInsensitive(row, "no");
                if (val instanceof Number) {
                    if (((Number) val).intValue() > max)
                        max = ((Number) val).intValue();
                } else if (val != null) {
                    try {
                        int v = Integer.parseInt(val.toString());
                        if (v > max)
                            max = v;
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return max;
    }

    private void buildToolbar(FormMeta formDef) {
        toolbar.removeAll();

        // 1. TAMBAH BUTTON
        Button btnNew = new Button("Tambah");
        Icon iconNew = VaadinIcon.PLUS_CIRCLE.create();
        iconNew.getStyle().set("color", "#22c55e").set("font-size", "1.2rem");
        btnNew.setIcon(iconNew);
        btnNew.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnNew.getStyle().set("font-weight", "500").set("color", "#374151");
        btnNew.addClickListener(e -> {
            formBinder.setBean(new HashMap<>());
            clearAllComponents();
            detailsList.clear();
            deletedDetailsList.clear();
            applyDetailsFilters();
            tabSheet.setSelectedTab(transaksiTab);
            executeOnLoadActions("ON_LOAD_NEW");
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
                java.util.Set<Map<String, Object>> selectedItems = masterGrid.getSelectedItems();
                if (selectedItems == null || selectedItems.isEmpty()) {
                    Notification.show("Pilih data yang akan diedit terlebih dahulu di tab Historis!", 3000,
                            Notification.Position.MIDDLE);
                    return;
                }
                loadAndEditData(selectedItems.iterator().next());
            } else {
                Notification.show("Silakan pilih data di tab Historis terlebih dahulu.", 3000,
                        Notification.Position.MIDDLE);
                tabSheet.setSelectedTab(historisTab);
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
                java.util.Set<Map<String, Object>> selectedItems = masterGrid.getSelectedItems();
                if (selectedItems != null && !selectedItems.isEmpty()) {
                    showConfirmDialog("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus " + selectedItems.size()
                            + " data Master-Detail yang dipilih ini?", () -> {
                                toolbar.setEnabled(false);
                                try {
                                    for (Map<String, Object> selected : selectedItems) {
                                        dynamicDataService.deleteData(formDef, selected);
                                    }
                                    Notification.show("Data Master-Detail berhasil dihapus!", 3000,
                                            Notification.Position.TOP_CENTER);
                                    refreshMasterGridData();
                                    masterGrid.deselectAll();
                                } catch (Exception ex) {
                                    Notification.show("Gagal menghapus: " + ex.getMessage(), 5000,
                                            Notification.Position.MIDDLE);
                                } finally {
                                    toolbar.setEnabled(true);
                                }
                            });
                } else {
                    Notification.show("Pilih baris data pada grid terlebih dahulu untuk dihapus.", 3000,
                            Notification.Position.MIDDLE);
                }
            } else {
                Map<String, Object> bean = formBinder.getBean();
                String pk = formDef.getPrimaryKey() != null ? formDef.getPrimaryKey() : "id";
                if (bean != null && bean.containsKey(pk) && bean.get(pk) != null
                        && !bean.get(pk).toString().trim().isEmpty()) {
                    showConfirmDialog("Konfirmasi Hapus",
                            "Apakah Anda yakin ingin menghapus data transaksi Master-Detail ini?", () -> {
                                toolbar.setEnabled(false);
                                try {
                                    dynamicDataService.deleteData(formDef, bean);
                                    Notification.show("Data Master-Detail berhasil dihapus!", 3000,
                                            Notification.Position.TOP_CENTER);
                                    formBinder.setBean(new HashMap<>());
                                    clearAllComponents();
                                    detailsList.clear();
                                    deletedDetailsList.clear();
                                    applyDetailsFilters();
                                    refreshMasterGridData();
                                    tabSheet.setSelectedTab(historisTab);
                                } catch (Exception ex) {
                                    Notification.show("Gagal menghapus: " + ex.getMessage(), 5000,
                                            Notification.Position.MIDDLE);
                                } finally {
                                    toolbar.setEnabled(true);
                                }
                            });
                } else {
                    Notification.show("Tidak ada data master terpilih untuk dihapus.", 3000,
                            Notification.Position.MIDDLE);
                }
            }
        });

        // 3. SIMPAN BUTTON
        Button btnSave = new Button("Simpan");
        Icon iconSave = VaadinIcon.DOWNLOAD.create();
        iconSave.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnSave.setIcon(iconSave);
        btnSave.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("font-weight", "600").set("color", "#3b82f6");
        btnSave.setDisableOnClick(true);
        btnSave.addClickListener(e -> {
            toolbar.setEnabled(false);
            try {
                boolean binderOk = formBinder.validate().isOk();
                boolean masterRequiredOk = true;

                // Programmatic double-check for all required master fields
                boolean rulesOk = true;
                for (FieldMeta field : formDef.getFields()) {
                    if (!field.isDetail()) {
                        Component comp = formComponents.get(field.getFieldName());
                        if (comp != null && !com.vaadinerp.components.ComponentFactory.validateFieldRule(field, comp)) {
                            rulesOk = false;
                        }
                    }
                    if (!field.isDetail() && field.isRequired()) {
                        Component comp = formComponents.get(field.getFieldName());
                        if (comp instanceof com.vaadin.flow.component.HasValue) {
                            Object val = ((com.vaadin.flow.component.HasValue<?, ?>) comp).getValue();
                            if (val == null || val.toString().trim().isEmpty()) {
                                masterRequiredOk = false;
                                if (comp instanceof com.vaadin.flow.component.HasValidation) {
                                    ((com.vaadin.flow.component.HasValidation) comp).setInvalid(true);
                                    ((com.vaadin.flow.component.HasValidation) comp)
                                            .setErrorMessage(field.getFieldLabel() + " wajib diisi");
                                }
                            }
                        }
                    }
                }

                boolean detailsOk = true;

                if (detailsGrid.getEditor().isOpen()) {
                    detailsOk = detailsGrid.getEditor().getBinder().validate().isOk();
                    if (detailsOk) {
                        Map<String, Object> activeItem = detailsGrid.getEditor().getItem();
                        detailsGrid.getEditor().cancel();
                        if (activeItem != null) {
                            calculateRowTotal(activeItem);
                        }
                    }
                }

                // Check other rows in detailsList programmatically
                for (Map<String, Object> row : detailsList) {
                    for (FieldMeta field : formDef.getFields()) {
                        if (field.isDetail() && field.isRequired()) {
                            Object val = row.get(field.getFieldName());
                            if (val == null || val.toString().trim().isEmpty()) {
                                detailsOk = false;
                                break;
                            }
                        }
                    }
                }

                if (binderOk && masterRequiredOk && rulesOk && detailsOk) {
                    dynamicDataService.saveMasterDetailData(formDef, formBinder.getBean(), detailsList,
                            deletedDetailsList);
                    Notification.show("Data berhasil disimpan secara transactional!", 3000,
                            Notification.Position.TOP_CENTER);
                    formBinder.setBean(new HashMap<>());
                    clearAllComponents();
                    detailsList.clear();
                    deletedDetailsList.clear();
                    applyDetailsFilters();
                    refreshMasterGridData();
                    tabSheet.setSelectedTab(historisTab);
                } else {
                    java.util.List<String> errMsgs = new java.util.ArrayList<>();
                    if (!binderOk) {
                        formBinder.validate().getValidationErrors().forEach(err -> errMsgs.add(err.getErrorMessage()));
                    }
                    if (!masterRequiredOk) {
                        errMsgs.add("Kolom master wajib diisi belum lengkap");
                    }
                    if (!rulesOk) {
                        errMsgs.add("Aturan validasi kolom master tidak terpenuhi");
                    }
                    if (!detailsOk) {
                        errMsgs.add("Kolom rincian wajib diisi belum lengkap");
                    }
                    String finalMsg = errMsgs.isEmpty() ? "Silakan periksa kembali inputan form Anda."
                            : String.join(" | ", errMsgs);
                    Notification n = Notification.show("⚠️ Gagal Menyimpan: " + finalMsg, 6000,
                            Notification.Position.MIDDLE);
                    n.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception ex) {
                Throwable rootCause = ex;
                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }
                ex.printStackTrace();
                String cleanMsg = rootCause.getMessage();
                if (cleanMsg != null) {
                    if (cleanMsg.contains("ERROR:"))
                        cleanMsg = cleanMsg.substring(cleanMsg.indexOf("ERROR:") + 6);
                    if (cleanMsg.contains("Where: PL/pgSQL"))
                        cleanMsg = cleanMsg.substring(0, cleanMsg.indexOf("Where: PL/pgSQL"));
                    cleanMsg = cleanMsg.trim();
                } else {
                    cleanMsg = "Terjadi kesalahan internal pada sistem.";
                }
                Notification n = Notification.show("⚠️ " + cleanMsg, 8000, Notification.Position.MIDDLE);
                n.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
            } finally {
                toolbar.setEnabled(true);
                btnSave.setEnabled(true);
            }
        });

        // 4. BATAL BUTTON
        Button btnCancel = new Button("Batal");
        Icon iconCancel = VaadinIcon.BAN.create();
        iconCancel.getStyle().set("color", "#ef4444").set("font-size", "1.2rem");
        btnCancel.setIcon(iconCancel);
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCancel.getStyle().set("font-weight", "500").set("color", "#374151");
        btnCancel.addClickListener(e -> {
            if (detailsGrid.getEditor().isOpen()) {
                detailsGrid.getEditor().cancel();
            }
            formBinder.setBean(new HashMap<>());
            clearAllComponents();
            detailsList.clear();
            deletedDetailsList.clear();
            applyDetailsFilters();
            tabSheet.setSelectedTab(historisTab);
        });


        // 6. CETAK BUTTON
        Button btnPrint = new Button("Cetak");
        Icon iconPrint = VaadinIcon.PRINT.create();
        iconPrint.getStyle().set("color", "#374151").set("font-size", "1.2rem");
        btnPrint.setIcon(iconPrint);
        btnPrint.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnPrint.getStyle().set("font-weight", "500").set("color", "#374151");
        btnPrint.addClickListener(e -> {
            Notification.show("Fitur Cetak belum diimplementasikan.", 3000, Notification.Position.TOP_CENTER);
        });

        // 7. REFRESH BUTTON
        Button btnRefresh = new Button("Refresh");
        Icon iconRefresh = VaadinIcon.REFRESH.create();
        iconRefresh.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnRefresh.setIcon(iconRefresh);
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnRefresh.getStyle().set("font-weight", "500").set("color", "#374151");
        btnRefresh.setDisableOnClick(true);
        btnRefresh.addClickListener(e -> {
            try {
                refreshExtraToolbarButtons();
                buildDetailsActions(currentFormDef);
                if (tabSheet.getSelectedTab() == historisTab) {
                    refreshMasterGridData();
                    Notification.show("Data berhasil diperbarui!", 1500, Notification.Position.BOTTOM_END);
                } else {
                    if (detailsGrid.getEditor().isOpen()) {
                        detailsGrid.getEditor().cancel();
                    }
                    Map<String, Object> bean = formBinder.getBean();
                    String pk = formDef.getPrimaryKey() != null ? formDef.getPrimaryKey() : "id";
                    if (bean != null && bean.containsKey(pk) && bean.get(pk) != null
                            && !bean.get(pk).toString().trim().isEmpty()) {
                        Object idVal = bean.get(pk);
                        String srcTable = (formDef.getViewTable() != null && !formDef.getViewTable().trim().isEmpty())
                                ? formDef.getViewTable().trim()
                                : formDef.getTableName();
                        Map<String, Object> freshRecord = dynamicDataService.fetchLovRecord(srcTable, pk, idVal);
                        if (freshRecord != null) {
                            formBinder.setBean(new HashMap<>(freshRecord));
                            evaluateFormulas();

                            detailsList.clear();
                            deletedDetailsList.clear();
                            detailsList.addAll(dynamicDataService.fetchDetailTableData(
                                    formDef.getDetailTableName(), formDef.getDetailForeignKey(), idVal));
                            applyDetailsFilters();

                            Notification.show("Data berhasil direfresh!", 1500, Notification.Position.BOTTOM_END);
                        } else {
                            Notification.show("Gagal merefresh: Data tidak ditemukan di database.", 3000,
                                    Notification.Position.MIDDLE);
                        }
                    } else {
                        formBinder.setBean(new HashMap<>());
                        clearAllComponents();
                        detailsList.clear();
                        deletedDetailsList.clear();
                        applyDetailsFilters();
                        Notification.show("Form dibersihkan!", 1500, Notification.Position.BOTTOM_END);
                    }
                }
            } catch (Exception ex) {
                Notification.show("Gagal menyegarkan: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            } finally {
                btnRefresh.setEnabled(true);
            }
        });

        com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority auth = securityService != null
                && currentFormCode != null
                        ? securityService.getAuthorityForMenu(currentFormCode)
                        : com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority.fullAccess();

        if (!auth.canAdd) {
            btnNew.setVisible(false);
            btnNew.setEnabled(false);
        }
        if (!auth.canEdit) {
            btnSave.setVisible(false);
            btnSave.setEnabled(false);
            btnEdit.setVisible(false);
            btnEdit.setEnabled(false);
        }
        if (!auth.canDelete) {
            btnDelete.setVisible(false);
            btnDelete.setEnabled(false);
        }
        if (!auth.canPrint) {
            btnPrint.setVisible(false);
            btnPrint.setEnabled(false);
        }
        
        if (!auth.canAdd && !auth.canEdit) {
            btnCancel.setVisible(false);
            btnCancel.setEnabled(false);
        }

        // 8. DEBUG CONTEXT BUTTON
        Button btnDebug = new Button("Debug Context");
        Icon iconDebug = VaadinIcon.BUG.create();
        iconDebug.getStyle().set("color", "#8b5cf6").set("font-size", "1.2rem");
        btnDebug.setIcon(iconDebug);
        btnDebug.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnDebug.getStyle().set("font-weight", "500").set("color", "#6b7280");
        btnDebug.setVisible(securityService != null && securityService.isSuperAdmin());
        btnDebug.addClickListener(e -> {
            Map<String, Object> bean = formBinder != null ? formBinder.getBean() : null;
            com.vaadinerp.components.FormDebugUtils.showDebugDialog(bean);
        });

        extraActionsContainer.setSpacing(true);
        toolbar.add(btnNew, btnEdit, btnDelete, btnSave, btnCancel, btnRefresh, btnPrint, btnDebug,
                extraActionsContainer);
        refreshExtraToolbarButtons();
    }

    private void loadAndEditData(Map<String, Object> selectedMaster) {
        if (selectedMaster != null && currentFormDef != null) {
            String pk = currentFormDef.getPrimaryKey() != null ? currentFormDef.getPrimaryKey() : "id";
            Object masterId = getValueCaseInsensitive(selectedMaster, pk);
            Map<String, Object> freshMaster = selectedMaster;
            if (masterId != null && !masterId.toString().trim().isEmpty()) {
                try {
                    String srcTable = (currentFormDef.getViewTable() != null
                            && !currentFormDef.getViewTable().trim().isEmpty()) ? currentFormDef.getViewTable().trim()
                                    : currentFormDef.getTableName();
                    Map<String, Object> dbRow = dynamicDataService.fetchLovRecord(srcTable, pk, masterId);
                    if (dbRow != null && !dbRow.isEmpty()) {
                        freshMaster = dbRow;
                    }
                } catch (Exception ex) {
                    Notification.show("Error memuat data: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            }
            Map<String, Object> formValues = new HashMap<>(freshMaster);

            // Set flag to prevent cascading listeners from clearing child LOV values
            isLoadingExistingData = true;
            try {
                formBinder.setBean(formValues);
            } finally {
                isLoadingExistingData = false;
            }
            updateFieldsReadonlyStatus(false);
            evaluateFormulas();

            // Fetch details
            detailsList.clear();
            deletedDetailsList.clear();
            if (masterId != null) {
                detailsList.addAll(dynamicDataService.fetchDetailTableData(
                        currentFormDef.getDetailTableName(),
                        currentFormDef.getDetailForeignKey(),
                        masterId));
            }

            applyDetailsFilters();
            tabSheet.setSelectedTab(transaksiTab);
            executeOnLoadActions("ON_LOAD_EDIT");
        }
    }

    private void refreshExtraToolbarButtons() {
        extraActionsContainer.removeAll();
        if (currentFormDef == null || dynamicDataService == null)
            return;
        List<com.vaadinerp.meta.FormActionMeta> masterActions = dynamicDataService
                .getFormActions(currentFormDef.getFormCode(), "MASTER_TOOLBAR");
        if (masterActions == null || masterActions.isEmpty())
            return;

        java.util.Map<String, java.util.List<com.vaadinerp.meta.FormActionMeta>> groupedActions = new java.util.LinkedHashMap<>();
        List<com.vaadinerp.meta.FormActionMeta> standaloneActions = new ArrayList<>();

        for (com.vaadinerp.meta.FormActionMeta act : masterActions) {
            if (act.getMenuGroup() != null && !act.getMenuGroup().isBlank()) {
                groupedActions.computeIfAbsent(act.getMenuGroup().trim(), k -> new ArrayList<>()).add(act);
            } else {
                standaloneActions.add(act);
            }
        }

        for (com.vaadinerp.meta.FormActionMeta act : standaloneActions) {
            Icon icon = null;
            if (act.getIconName() != null && !act.getIconName().isBlank()) {
                try {
                    icon = VaadinIcon.valueOf(act.getIconName().toUpperCase()).create();
                } catch (Exception ignored) {
                }
            }
            if (icon != null) {
                icon.getStyle().set("color", "#2563eb").set("font-size", "1.2rem");
            }
            Button actBtn = icon != null ? new Button(act.getActionLabel(), icon) : new Button(act.getActionLabel());
            actBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            actBtn.getStyle().set("font-weight", "500").set("color", "#374151");
            actBtn.addClickListener(e -> executeToolbarAction(act));
            extraActionsContainer.add(actBtn);
        }

        for (Map.Entry<String, List<com.vaadinerp.meta.FormActionMeta>> entry : groupedActions.entrySet()) {
            com.vaadin.flow.component.menubar.MenuBar menuBar = new com.vaadin.flow.component.menubar.MenuBar();
            menuBar.addThemeVariants(com.vaadin.flow.component.menubar.MenuBarVariant.LUMO_TERTIARY);

            HorizontalLayout menuBtnLayout = new HorizontalLayout();
            menuBtnLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
            menuBtnLayout.setSpacing(true);
            com.vaadin.flow.component.icon.Icon grpIcon = VaadinIcon.FILE_TEXT.create();
            grpIcon.getStyle().set("color", "#2563eb").set("font-size", "1.2rem");
            com.vaadin.flow.component.icon.Icon chevronIcon = VaadinIcon.CHEVRON_DOWN.create();
            chevronIcon.setSize("14px");
            com.vaadin.flow.component.html.Span grpSpan = new com.vaadin.flow.component.html.Span(entry.getKey());
            grpSpan.getStyle().set("font-weight", "500").set("color", "#374151");
            menuBtnLayout.add(grpIcon, grpSpan, chevronIcon);
            com.vaadin.flow.component.contextmenu.MenuItem parentItem = menuBar.addItem(menuBtnLayout);

            com.vaadin.flow.component.contextmenu.SubMenu subMenu = parentItem.getSubMenu();
            for (com.vaadinerp.meta.FormActionMeta act : entry.getValue()) {
                Icon icon = null;
                if (act.getIconName() != null && !act.getIconName().isBlank()) {
                    try {
                        icon = VaadinIcon.valueOf(act.getIconName().toUpperCase()).create();
                    } catch (Exception ignored) {
                    }
                }
                if (icon == null) {
                    icon = VaadinIcon.FILE_TEXT.create();
                }
                icon.getStyle().set("color", "#2563eb").set("font-size", "1rem");
                subMenu.addItem(
                        new HorizontalLayout(icon, new com.vaadin.flow.component.html.Span(act.getActionLabel())),
                        e -> executeToolbarAction(act));
            }
            extraActionsContainer.add(menuBar);
        }
    }

    private void executeToolbarAction(com.vaadinerp.meta.FormActionMeta act) {
        Map<String, Object> headerBean = formBinder != null ? formBinder.getBean() : new HashMap<>();
        List<Map<String, Object>> selectedRows = new ArrayList<>();
        if (masterGrid != null && masterGrid.getSelectedItems() != null) {
            selectedRows.addAll(masterGrid.getSelectedItems());
        }
        if (selectedRows.isEmpty() && detailsGrid != null && detailsGrid.getSelectedItems() != null) {
            selectedRows.addAll(detailsGrid.getSelectedItems());
        }
        if ("GROOVY_SCRIPT".equalsIgnoreCase(act.getActionType())
                || (act.getScriptContent() != null && !act.getScriptContent().isBlank())) {
            if (dynamicDataService != null && dynamicDataService.getScriptExecutorService() != null) {
                dynamicDataService.getScriptExecutorService().executeActionScript(act, headerBean, selectedRows, this);
            }
        } else {
            com.vaadinerp.components.DynamicPickerPopupDialog dlg = new com.vaadinerp.components.DynamicPickerPopupDialog(
                    act, dynamicDataService, headerBean, selectedRecords -> {
                        boolean hasDetailMapping = act.getTargetMapping() != null
                                && act.getTargetMapping().toLowerCase().contains("detail.");
                        for (Map<String, Object> srcRec : selectedRecords) {
                            if (hasDetailMapping && detailsList != null) {
                                Map<String, Object> newDetailRow = new HashMap<>();
                                newDetailRow.put("_tempId", java.util.UUID.randomUUID().toString());
                                newDetailRow.put("lineno", getMaxLineNoFromList(detailsList));
                                applyTargetMapping(headerBean, newDetailRow, srcRec, act.getTargetMapping());
                                detailsList.add(newDetailRow);
                            } else {
                                applyTargetMapping(headerBean, null, srcRec, act.getTargetMapping());
                            }
                        }
                        if (hasDetailMapping) {
                            if (detailsGrid != null && detailsGrid.getDataProvider() != null) {
                                detailsGrid.getDataProvider().refreshAll();
                            }
                            applyDetailsFilters();
                        }
                        if (formBinder != null)
                            formBinder.readBean(headerBean);
                    });
            dlg.open();
        }
    }

    private void buildMasterForm(FormMeta formDef) {
        formLayout.removeAll();
        formComponents.clear();

        java.util.function.BiConsumer<String, Object> updateFieldValue = (targetFieldName, value) -> {
            if (isLoadingExistingData)
                return;
            if (targetFieldName != null && targetFieldName.toLowerCase().startsWith("detail.")) {
                String detailCol = targetFieldName.substring(7);
                for (Map<String, Object> row : detailsList) {
                    putValueCaseInsensitive(row, detailCol, value);
                    calculateRowTotal(row);
                }
                applyDetailsFilters();
                return;
            }
            Component targetComponent = formComponents.get(targetFieldName);
            if (targetComponent instanceof HasValue) {
                @SuppressWarnings("unchecked")
                HasValue<?, Object> hasValue = (HasValue<?, Object>) targetComponent;
                Object converted = convertToFieldValue(value, targetComponent);
                try {
                    hasValue.setValue(converted);
                } catch (Exception ignored) {
                }
            }
        };

        // Group master fields (where isDetail is false)
        List<FieldMeta> masterFields = formDef.getFields().stream()
                .filter(f -> !f.isDetail())
                .collect(Collectors.toList());

        Map<Integer, List<FieldMeta>> groups = new HashMap<>();
        List<Integer> rowGroupsOrder = new ArrayList<>();
        for (FieldMeta field : masterFields) {
            int rg = field.getRowGroup() != null ? field.getRowGroup() : 1;
            groups.computeIfAbsent(rg, k -> new ArrayList<>()).add(field);
            if (!rowGroupsOrder.contains(rg)) {
                rowGroupsOrder.add(rg);
            }
        }
        rowGroupsOrder.sort(java.util.Comparator.naturalOrder());

        int maxColsInForm = com.vaadinerp.components.FormLayoutUtils.calculateMaxColsInForm(masterFields);

        for (Integer rg : rowGroupsOrder) {
            List<FieldMeta> groupFields = groups.get(rg);
            FormLayout rowLayout = new FormLayout();
            rowLayout.setWidthFull();

            com.vaadinerp.components.FormLayoutUtils.RowLayoutConfig rowConfig = com.vaadinerp.components.FormLayoutUtils
                    .calculateRowConfig(groupFields, maxColsInForm);
            int cols = rowConfig.getCols();
            com.vaadinerp.components.FormLayoutUtils.applyResponsiveSteps(rowLayout, cols);

            if (formDef.getLabelWidth() != null && !formDef.getLabelWidth().trim().isEmpty()) {
                rowLayout.getElement().getStyle().set("--vaadin-form-layout-label-width", formDef.getLabelWidth());
            }

            for (FieldMeta field : groupFields) {
                Component input = ComponentFactory.create(field, dynamicDataService, updateFieldValue);
                if (field.isHideInForm()) {
                    input.setVisible(false);
                }

                int span = rowConfig.getSpan(field);

                // Checkbox: bungkus dengan Div agar label muncul di ATAS (sama persis seperti
                // TextField/ComboBox)
                // sehingga checkbox square sejajar satu baris dengan input box di sampingnya
                if (input instanceof com.vaadin.flow.component.checkbox.Checkbox cb) {
                    String cbLabel = cb.getLabel();
                    cb.setLabel(null);

                    com.vaadin.flow.component.html.Span labelSpan = new com.vaadin.flow.component.html.Span(
                            cbLabel != null ? cbLabel : "");
                    labelSpan.getStyle()
                            .set("color", "var(--lumo-secondary-text-color)")
                            .set("font-weight", "500")
                            .set("font-size", "var(--lumo-font-size-s)")
                            .set("line-height", "1")
                            .set("padding-top", "var(--lumo-space-m)")
                            .set("padding-bottom", "var(--lumo-space-xs)")
                            .set("overflow", "hidden")
                            .set("white-space", "nowrap")
                            .set("text-overflow", "ellipsis")
                            .set("align-self", "flex-start");

                    com.vaadin.flow.component.html.Div cbBox = new com.vaadin.flow.component.html.Div(cb);
                    cbBox.getStyle()
                            .set("height", "var(--lumo-size-m, 36px)")
                            .set("min-height", "var(--lumo-size-m, 36px)")
                            .set("display", "flex")
                            .set("align-items", "center")
                            .set("justify-content", "flex-start")
                            .set("width", "100%");

                    com.vaadin.flow.component.html.Div cbWrapper = new com.vaadin.flow.component.html.Div(labelSpan,
                            cbBox);
                    cbWrapper.getStyle()
                            .set("display", "flex")
                            .set("flex-direction", "column")
                            .set("justify-content", "flex-start")
                            .set("align-items", "flex-start")
                            .set("width", "100%");

                    rowLayout.add(cbWrapper);
                    if (span > 1) {
                        rowLayout.setColspan(cbWrapper, Math.min(span, cols));
                    }
                } else {
                    rowLayout.add(input);
                    if (span > 1) {
                        rowLayout.setColspan(input, Math.min(span, cols));
                    }
                }

                formComponents.put(field.getFieldName(), input);
                bindComponent(formBinder, input, field);

                if ("TEXTAREA".equalsIgnoreCase(field.getComponentType())
                        && input instanceof com.vaadin.flow.component.textfield.TextArea ta) {
                    ta.getStyle().set("resize", "vertical");
                }
            }
            formLayout.add(rowLayout);
        }

        // Setup cascading from master fields to detail editors and SubformGridField
        // components
        for (FieldMeta field : formDef.getFields()) {
            Component sourceComponent = formComponents.get(field.getFieldName());
            if (sourceComponent instanceof com.vaadin.flow.component.HasValue) {
                @SuppressWarnings("unchecked")
                com.vaadin.flow.component.HasValue<?, Object> hasValueSource = (com.vaadin.flow.component.HasValue<?, Object>) sourceComponent;
                hasValueSource.addValueChangeListener(event -> {
                    Object newValue = event.getValue();
                    applyMasterFiltersToDetailEditors(formDef);
                    for (Component comp : formComponents.values()) {
                        if (comp instanceof com.vaadinerp.components.SubformGridField sgf) {
                            sgf.setParentFieldValue(field.getFieldName(), newValue);
                        }
                    }
                    for (Component comp : detailEditorComponents.values()) {
                        if (comp instanceof com.vaadinerp.components.SubformGridField sgf) {
                            sgf.setParentFieldValue(field.getFieldName(), newValue);
                        }
                    }
                });
            }
        }

        if (queryParameters != null && !queryParameters.getParameters().isEmpty()) {
            Map<String, Object> bean = formBinder.getBean();
            if (bean == null) {
                bean = new HashMap<>();
                formBinder.setBean(bean);
            }
            for (Map.Entry<String, java.util.List<String>> entry : queryParameters.getParameters().entrySet()) {
                String fieldName = entry.getKey();
                Component comp = formComponents.get(fieldName);
                if (comp instanceof com.vaadin.flow.component.HasValue && entry.getValue() != null
                        && !entry.getValue().isEmpty()) {
                    presetFieldFromQuery(comp, entry.getValue().get(0));
                }
            }
        }
    }

    public void applyInitialParameters(Object extra) {
        if (extra == null)
            return;
        if (extra instanceof String strExtra) {
            String trimmed = strExtra.trim();
            if ("HIDE_HISTORIS".equalsIgnoreCase(trimmed) || "HIDE_HISTORY".equalsIgnoreCase(trimmed)) {
                hideHistorisTab();
                return;
            }
            if (trimmed.contains("=")) {
                String[] pairs = trimmed.split("[&,]");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        applySingleParameter(kv[0].trim(), kv[1].trim());
                    }
                }
            }
        } else if (extra instanceof Map<?, ?> mapExtra) {
            for (Map.Entry<?, ?> entry : mapExtra.entrySet()) {
                String key = entry.getKey() != null ? entry.getKey().toString().trim() : "";
                Object val = entry.getValue();
                if (key.isEmpty())
                    continue;
                if ("HIDE_HISTORIS".equalsIgnoreCase(key) || "HIDE_HISTORY".equalsIgnoreCase(key)
                        || "HIDEHISTORIS".equalsIgnoreCase(key.replace("_", ""))
                        || "HIDEHISTORY".equalsIgnoreCase(key.replace("_", ""))) {
                    if (val == null || "true".equalsIgnoreCase(val.toString())
                            || "HIDE_HISTORIS".equalsIgnoreCase(val.toString()) || "1".equals(val.toString())) {
                        hideHistorisTab();
                    }
                } else {
                    applySingleParameter(key, val);
                }
            }
        }
    }

    private void applySingleParameter(String key, Object val) {
        if (formComponents == null || key == null || key.isEmpty() || val == null)
            return;
        Component comp = formComponents.get(key);
        if (comp == null && detailEditorComponents != null) {
            comp = detailEditorComponents.get(key);
        }
        if (comp == null) {
            for (Map.Entry<String, Component> entry : formComponents.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    comp = entry.getValue();
                    break;
                }
            }
        }
        if (comp == null && detailEditorComponents != null) {
            for (Map.Entry<String, Component> entry : detailEditorComponents.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    comp = entry.getValue();
                    break;
                }
            }
        }
        if (comp instanceof com.vaadin.flow.component.HasValue) {
            presetFieldFromQuery(comp, val);
        }
    }

    private void presetFieldFromQuery(Component targetComponent, Object value) {
        if (targetComponent instanceof com.vaadin.flow.component.HasValue && value != null) {
            @SuppressWarnings("unchecked")
            com.vaadin.flow.component.HasValue<?, Object> hasValue = (com.vaadin.flow.component.HasValue<?, Object>) targetComponent;
            Object converted = convertToFieldValue(value, targetComponent);
            if (converted != null) {
                try {
                    hasValue.setValue(converted);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void buildMasterGrid(FormMeta formDef) {
        if (masterDoubleClickReg != null)
            masterDoubleClickReg.remove();
        if (masterDragStartReg != null)
            masterDragStartReg.remove();
        if (masterDropReg != null)
            masterDropReg.remove();
        if (masterDragEndReg != null)
            masterDragEndReg.remove();
        if (masterColReorderReg != null)
            masterColReorderReg.remove();

        com.vaadinerp.components.StandardGridUtils.cleanGridBeforeRebuild(masterGrid);
        masterGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        columnToFieldNameMap.clear();
        masterColGetterMap.clear();
        filterValues.clear();

        // 1. Konfigurasi Toolbar Master Grid
        masterGridToolbar.removeAll();
        masterGridToolbar.setWidthFull();
        masterGridToolbar.setAlignItems(Alignment.CENTER);
        masterGridToolbar.getStyle().set("padding", "0").set("margin-top", "10px").set("margin-bottom", "5px");

        H4 sectionTitle = new H4(
                "Riwayat Data (" + (currentFormDef != null ? currentFormDef.getFormTitle() : "") + ")");
        sectionTitle.getStyle().set("margin", "0");
        sectionTitle.getStyle().set("flex-grow", "1");

        Button btnResetMasterGrid = new Button("Reset Layout Grid", VaadinIcon.ROTATE_LEFT.create());
        btnResetMasterGrid.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnResetMasterGrid.addClickListener(e -> {
            if (currentFormDef != null) {
                dynamicDataService.resetUserGridOrder(currentFormCode, "masterGrid");
                buildMasterGrid(formDef);
                Notification.show("Layout grid master dikembalikan ke default!", 2000,
                        Notification.Position.BOTTOM_END);
            }
        });

        com.vaadin.flow.component.checkbox.Checkbox cbPilihSemuaHalIni = new com.vaadin.flow.component.checkbox.Checkbox();
        com.vaadin.flow.component.html.Span lblPilihSemuaHalIni = new com.vaadin.flow.component.html.Span(
                "Pilih Semua (Hal Ini)");
        lblPilihSemuaHalIni.getStyle().set("font-size", "var(--lumo-font-size-s)").set("font-weight", "500")
                .set("cursor", "pointer").set("user-select", "none").set("white-space", "nowrap");
        lblPilihSemuaHalIni.addClickListener(
                e -> cbPilihSemuaHalIni.setValue(!Boolean.TRUE.equals(cbPilihSemuaHalIni.getValue())));

        HorizontalLayout boxHalIni = new HorizontalLayout(cbPilihSemuaHalIni, lblPilihSemuaHalIni);
        boxHalIni.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        boxHalIni.setSpacing(false);
        boxHalIni.getStyle().set("gap", "4px").set("margin-left", "10px");

        com.vaadin.flow.component.checkbox.Checkbox cbPilihSemua = new com.vaadin.flow.component.checkbox.Checkbox();
        com.vaadin.flow.component.html.Span lblPilihSemua = new com.vaadin.flow.component.html.Span("Pilih Semua");
        lblPilihSemua.getStyle().set("font-size", "var(--lumo-font-size-s)").set("font-weight", "500")
                .set("cursor", "pointer").set("user-select", "none").set("white-space", "nowrap");
        lblPilihSemua.addClickListener(e -> cbPilihSemua.setValue(!Boolean.TRUE.equals(cbPilihSemua.getValue())));

        HorizontalLayout boxAll = new HorizontalLayout(cbPilihSemua, lblPilihSemua);
        boxAll.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        boxAll.setSpacing(false);
        boxAll.getStyle().set("gap", "4px").set("margin-left", "12px");

        cbPilihSemuaHalIni.addValueChangeListener(e -> {
            if (e.isFromClient() && currentFormDef != null) {
                if (Boolean.TRUE.equals(e.getValue())) {
                    int offset = paginationBar != null ? paginationBar.getOffset() : 0;
                    int limit = paginationBar != null ? paginationBar.getLimit() : 1000;
                    java.util.List<Map<String, Object>> pageItems = dynamicDataService.fetchGridDataPaged(
                            currentFormDef, offset, limit, filterValues, currentSortField, currentSortDir);
                    masterGrid.asMultiSelect().select(pageItems);
                    Notification.show("Memilih " + pageItems.size() + " baris di halaman ini.", 1500,
                            Notification.Position.BOTTOM_END);
                } else {
                    if (!cbPilihSemua.getValue()) {
                        masterGrid.deselectAll();
                    } else {
                        cbPilihSemua.setValue(false);
                        masterGrid.deselectAll();
                    }
                }
            }
        });

        cbPilihSemua.addValueChangeListener(e -> {
            if (e.isFromClient() && currentFormDef != null) {
                if (Boolean.TRUE.equals(e.getValue())) {
                    java.util.List<Map<String, Object>> allItems = dynamicDataService.fetchGridDataPaged(
                            currentFormDef, 0, Integer.MAX_VALUE, filterValues, currentSortField, currentSortDir);
                    masterGrid.asMultiSelect().select(allItems);
                    cbPilihSemuaHalIni.setValue(true);
                    Notification.show("Memilih seluruh " + allItems.size() + " baris data.", 2000,
                            Notification.Position.BOTTOM_END);
                } else {
                    cbPilihSemuaHalIni.setValue(false);
                    masterGrid.deselectAll();
                }
            }
        });

        masterGrid.addSelectionListener(e -> {
            if (e.getAllSelectedItems().isEmpty()) {
                cbPilihSemuaHalIni.setValue(false);
                cbPilihSemua.setValue(false);
            }
        });

        com.vaadin.flow.component.html.Anchor btnExportMasterExcel = com.vaadinerp.components.StandardGridUtils
                .createExportExcelButton(masterGrid,
                        currentFormCode != null ? currentFormCode + "_master_export" : "master_export",
                        masterColGetterMap);
        masterGridToolbar.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        masterGridToolbar.getStyle().set("flex-wrap", "nowrap").set("overflow-x", "auto");
        masterGridToolbar.add(sectionTitle, boxHalIni, boxAll, btnExportMasterExcel, btnResetMasterGrid);

        com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority auth = securityService != null
                && currentFormCode != null
                        ? securityService.getAuthorityForMenu(currentFormCode)
                        : com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority.fullAccess();

        if (auth.canEdit) {
            masterDoubleClickReg = masterGrid.addItemDoubleClickListener(event -> {
                loadAndEditData(event.getItem());
            });
        }

        // LinkedHashMap to hold columns for Header Filter Row
        java.util.Map<String, Grid.Column<Map<String, Object>>> columnsMap = new java.util.LinkedHashMap<>();

        // Sort fields by colOrder
        List<FieldMeta> sortedFields = formDef.getFields().stream()
                .filter(f -> !f.isDetail() && f.isShowInGrid())
                .collect(Collectors.toList());
        sortedFields.sort((f1, f2) -> {
            Integer o1 = f1.getColOrder() != null ? f1.getColOrder() : Integer.MAX_VALUE;
            Integer o2 = f2.getColOrder() != null ? f2.getColOrder() : Integer.MAX_VALUE;
            return o1.compareTo(o2);
        });

        for (FieldMeta field : sortedFields) {
            String fieldName = field.getFieldName();
            String lovCode = field.getLovCode();
            if (lovCode != null && !lovCode.trim().isEmpty()) {
                fieldNameToLovCodeMap.put(fieldName, lovCode);
            }

            java.util.function.Function<Map<String, Object>, String> valueGetter = map -> {
                Object valObj = getValueCaseInsensitive(map, fieldName);
                String formatted = com.vaadinerp.components.ComponentFactory.formatFieldValueWithLov(field, valObj,
                        dynamicDataService);
                return formatted != null ? formatted : "";
            };
            Grid.Column<Map<String, Object>> col = masterGrid.addColumn(valueGetter::apply)
                    .setHeader(field.getFieldLabel())
                    .setAutoWidth(true)
                    .setFlexGrow(1)
                    .setResizable(true)
                    .setKey(fieldName);
            masterColGetterMap.put(col, valueGetter);

            col.setComparator((map1, map2) -> {
                Object val1 = map1.get(fieldName);
                Object val2 = map2.get(fieldName);
                if (val1 == null && val2 == null)
                    return 0;
                if (val1 == null)
                    return -1;
                if (val2 == null)
                    return 1;
                if (lovCode != null && !lovCode.trim().isEmpty()) {
                    String s1 = getLovDisplayLabel(lovCode, val1.toString());
                    String s2 = getLovDisplayLabel(lovCode, val2.toString());
                    return s1.compareToIgnoreCase(s2);
                }
                if (val1 instanceof Comparable && val2 instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> comp1 = (Comparable<Object>) val1;
                    return comp1.compareTo(val2);
                }
                return val1.toString().compareTo(val2.toString());
            });
            col.setSortable(true);

            columnsMap.put(fieldName, col);
            columnToFieldNameMap.put(col, fieldName);
        }

        // 2. Setup Header Filter Row
        com.vaadin.flow.component.grid.HeaderRow filterRow = masterGrid.appendHeaderRow();
        columnsMap.forEach((fieldName, col) -> {
            FilterCriteria criteria = new FilterCriteria();
            filterValues.put(fieldName, criteria);

            TextField filterField = new TextField();
            filterField.setPlaceholder("Filter...");
            filterField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
            filterField.setWidthFull();
            filterField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL);

            Button filterButton = new Button(com.vaadin.flow.component.icon.VaadinIcon.FILTER.create());
            filterButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE);
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
                    if (paginationBar != null)
                        paginationBar.resetPage();
                    applyFilters();
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
                com.vaadin.flow.component.notification.Notification.show(
                        nextFrozen ? "Kolom dibekukan" : "Kolom dilepas", 2000,
                        com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END);
            });

            filterField.addValueChangeListener(e -> {
                criteria.value = e.getValue();
                if (paginationBar != null)
                    paginationBar.resetPage();
                applyFilters();
            });

            filterRow.getCell(col).setComponent(filterField);
        });

        // 3. Row Drag and Drop
        masterGrid.setRowsDraggable(true);
        masterDragStartReg = masterGrid.addDragStartListener(event -> {
            if (!event.getDraggedItems().isEmpty()) {
                draggedItem = event.getDraggedItems().get(0);
                masterGrid.setDropMode(com.vaadin.flow.component.grid.dnd.GridDropMode.BETWEEN);
            }
        });

        masterDropReg = masterGrid.addDropListener(event -> {
            Map<String, Object> targetItem = event.getDropTargetItem().orElse(null);
            if (targetItem != null && draggedItem != null && targetItem != draggedItem) {
                int indexDragged = findIndexByReference(allMasterGridItems, draggedItem);
                int indexTarget = findIndexByReference(allMasterGridItems, targetItem);

                if (indexDragged >= 0 && indexTarget >= 0) {
                    allMasterGridItems.remove(indexDragged);
                    int newIndex = findIndexByReference(allMasterGridItems, targetItem);
                    if (event.getDropLocation() == com.vaadin.flow.component.grid.dnd.GridDropLocation.BELOW) {
                        allMasterGridItems.add(newIndex + 1, draggedItem);
                    } else {
                        allMasterGridItems.add(newIndex, draggedItem);
                    }
                    updateMasterGridDataProvider();
                }
            }
            draggedItem = null;
        });

        masterDragEndReg = masterGrid.addDragEndListener(event -> {
            draggedItem = null;
            masterGrid.setDropMode(null);
        });

        masterGrid.addSortListener(event -> {
            if (!event.getSortOrder().isEmpty()) {
                com.vaadin.flow.component.grid.GridSortOrder<Map<String, Object>> order = event.getSortOrder().get(0);
                if (order.getSorted().getKey() != null) {
                    currentSortField = order.getSorted().getKey();
                    currentSortDir = order.getDirection() == com.vaadin.flow.data.provider.SortDirection.DESCENDING
                            ? "DESC"
                            : "ASC";
                    if (paginationBar != null)
                        paginationBar.resetPage();
                    applyFilters();
                }
            } else {
                currentSortField = null;
                currentSortDir = null;
                if (paginationBar != null)
                    paginationBar.resetPage();
                applyFilters();
            }
        });

        // 4. Column Reordering
        masterGrid.setColumnReorderingAllowed(true);
        masterColReorderReg = masterGrid.addColumnReorderListener(event -> {
            java.util.List<Grid.Column<Map<String, Object>>> newOrder = event.getColumns();
            java.util.List<String> orderedFieldNames = new java.util.ArrayList<>();
            for (Grid.Column<Map<String, Object>> col : newOrder) {
                String fieldName = columnToFieldNameMap.get(col);
                if (fieldName != null) {
                    orderedFieldNames.add(fieldName);
                }
            }
            try {
                dynamicDataService.saveUserGridOrder(currentFormCode, "masterGrid", orderedFieldNames);
                Notification.show("Urutan kolom disimpan", 1500, Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000,
                        Notification.Position.MIDDLE);
            }
        });

        applyFilters();
        java.util.List<String> masterUserOrder = dynamicDataService.getUserGridOrder(currentFormCode, "masterGrid");
        com.vaadinerp.components.StandardGridUtils.applySafeColumnOrder(masterGrid, columnToFieldNameMap,
                masterUserOrder);

        com.vaadinerp.components.StandardGridUtils.attachSelectAllHeader(masterGrid, () -> {
            if (currentFormDef == null)
                return java.util.Collections.emptyList();
            int offset = paginationBar != null ? paginationBar.getOffset() : 0;
            int limit = paginationBar != null ? paginationBar.getLimit() : 1000;
            return dynamicDataService.fetchGridDataPaged(currentFormDef, offset, limit, filterValues, currentSortField,
                    currentSortDir);
        });
    }

    private void buildDetailsGrid(FormMeta formDef) {
        if (detailDragStartReg != null)
            detailDragStartReg.remove();
        if (detailDropReg != null)
            detailDropReg.remove();
        if (detailDragEndReg != null)
            detailDragEndReg.remove();
        if (detailColReorderReg != null)
            detailColReorderReg.remove();

        com.vaadinerp.components.StandardGridUtils.cleanGridBeforeRebuild(detailsGrid);
        detailsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        detailsColumnToFieldNameMap.clear();
        detailColGetterMap.clear();
        detailsFilterValues.clear();
        detailEditorComponents.clear();

        List<FieldMeta> detailFields = formDef.getFields().stream()
                .filter(f -> f != null && f.isDetail())
                .collect(Collectors.toList());

        // Sort fields by colOrder
        detailFields.sort((f1, f2) -> {
            Integer o1 = f1.getColOrder() != null ? f1.getColOrder() : Integer.MAX_VALUE;
            Integer o2 = f2.getColOrder() != null ? f2.getColOrder() : Integer.MAX_VALUE;
            return o1.compareTo(o2);
        });

        Editor<Map<String, Object>> editor = detailsGrid.getEditor();
        Binder<Map<String, Object>> gridBinder = new Binder<>();
        editor.setBinder(gridBinder);
        editor.setBuffered(false);

        java.util.Map<String, Grid.Column<Map<String, Object>>> columnsMap = new java.util.LinkedHashMap<>();

        java.util.function.BiConsumer<String, Object> detailUpdateFieldValue = (targetFieldName, val) -> {
            if (isLoadingExistingData)
                return;
            Component targetComp = detailEditorComponents.get(targetFieldName);
            if (targetComp == null) {
                for (java.util.Map.Entry<String, Component> e : detailEditorComponents.entrySet()) {
                    if (e.getKey() != null && e.getKey().equalsIgnoreCase(targetFieldName)) {
                        targetComp = e.getValue();
                        break;
                    }
                }
            }
            if (targetComp instanceof HasValue) {
                @SuppressWarnings("unchecked")
                HasValue<?, Object> hv = (HasValue<?, Object>) targetComp;
                Object convertedVal = convertToFieldValue(val, targetComp);
                if (convertedVal != null && !convertedVal.equals(hv.getValue())
                        || (convertedVal == null && hv.getValue() != null)) {
                    hv.setValue(convertedVal);
                }
            }
            if (detailsGrid.getEditor().isOpen() && detailsGrid.getEditor().getItem() != null) {
                putValueCaseInsensitive(detailsGrid.getEditor().getItem(), targetFieldName, val);
            }
        };

        for (FieldMeta field : detailFields) {
            String fieldName = field.getFieldName();
            String lovCode = field.getLovCode();
            java.util.function.Function<Map<String, Object>, String> valueGetter = map -> {
                Object valObj = getValueCaseInsensitive(map, fieldName);
                String formatted = com.vaadinerp.components.ComponentFactory.formatFieldValueWithLov(field, valObj,
                        dynamicDataService);
                return formatted != null ? formatted : "";
            };
            Grid.Column<Map<String, Object>> col = detailsGrid.addColumn(valueGetter::apply)
                    .setHeader(field.getFieldLabel())
                    .setAutoWidth(true)
                    .setFlexGrow(1)
                    .setResizable(true)
                    .setKey(fieldName);

            columnsMap.put(fieldName, col);
            detailsColumnToFieldNameMap.put(col, fieldName);
            detailColGetterMap.put(col, valueGetter);

            // Create inline editor component FIRST
            Component editorComp = ComponentFactory.create(field, dynamicDataService, detailUpdateFieldValue, true);
            col.setEditorComponent(editorComp);
            detailEditorComponents.put(fieldName, editorComp);

            @SuppressWarnings("unchecked")
            HasValue<?, Object> hasValue = (HasValue<?, Object>) editorComp;
            Binder.BindingBuilder<Map<String, Object>, Object> builder = gridBinder.forField(hasValue);
            if (field.isRequired()) {
                builder.asRequired(field.getFieldLabel() + " wajib diisi");
            }
            builder.bind(map -> convertToFieldValue(getValueCaseInsensitive(map, fieldName), editorComp),
                    (map, val) -> putValueCaseInsensitive(map, fieldName, val));

            // Setup Comparator and Sortable AFTER editor binding to prevent override
            col.setComparator((map1, map2) -> {
                Object val1 = map1.get(fieldName);
                Object val2 = map2.get(fieldName);
                if (val1 == null && val2 == null)
                    return 0;
                if (val1 == null)
                    return -1;
                if (val2 == null)
                    return 1;
                if (lovCode != null && !lovCode.trim().isEmpty()) {
                    String s1 = getLovDisplayLabel(lovCode, val1.toString());
                    String s2 = getLovDisplayLabel(lovCode, val2.toString());
                    return s1.compareToIgnoreCase(s2);
                }
                if (val1 instanceof Comparable && val2 instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> comp1 = (Comparable<Object>) val1;
                    return comp1.compareTo(val2);
                }
                return val1.toString().compareTo(val2.toString());
            });
            col.setSortable(true);
        }

        // Setup cascading and static filters (Master to Master, Master to Detail,
        // Detail to Detail)
        for (FieldMeta field : formDef.getFields()) {
            if (field.getFilters() != null) {
                for (com.vaadinerp.meta.FieldFilterMeta filter : field.getFilters()) {
                    Component targetComponent = field.isDetail() ? detailEditorComponents.get(field.getFieldName())
                            : formComponents.get(field.getFieldName());
                    if (targetComponent == null)
                        continue;

                    if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                        String sourceFieldName = filter.getSourceName();
                        String lookupKey = sourceFieldName;
                        if (lookupKey != null
                                && (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header."))) {
                            lookupKey = lookupKey.replaceAll("[\"']", "")
                                    .substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                        } else if (lookupKey != null
                                && (lookupKey.startsWith("detail.") || lookupKey.startsWith("\"detail."))) {
                            lookupKey = lookupKey.replaceAll("[\"']", "")
                                    .substring(lookupKey.indexOf("detail.") + "detail.".length()).trim();
                        }
                        Component sourceComponent = lookupKey != null ? formComponents.get(lookupKey) : null;
                        if (sourceComponent == null && field.isDetail() && lookupKey != null) {
                            sourceComponent = detailEditorComponents.get(lookupKey);
                        }
                        if (sourceComponent == null && sourceFieldName != null) {
                            sourceComponent = formComponents.get(sourceFieldName);
                            if (sourceComponent == null && field.isDetail())
                                sourceComponent = detailEditorComponents.get(sourceFieldName);
                        }
                        if (sourceComponent == null
                                && ("unique".equalsIgnoreCase(lookupKey) || "isunique".equalsIgnoreCase(lookupKey))) {
                            sourceComponent = formComponents.get("unique");
                            if (sourceComponent == null)
                                sourceComponent = formComponents.get("isunique");
                            if (sourceComponent == null && field.isDetail()) {
                                sourceComponent = detailEditorComponents.get("unique");
                                if (sourceComponent == null)
                                    sourceComponent = detailEditorComponents.get("isunique");
                            }
                        }

                        if (sourceComponent instanceof HasValue) {
                            @SuppressWarnings("unchecked")
                            HasValue<?, Object> hasValueSource = (HasValue<?, Object>) sourceComponent;
                            hasValueSource.addValueChangeListener(event -> {
                                Object newValue = event.getValue();
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                        String.valueOf(filter.getId()), filter.getFilterColumn(), newValue,
                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            });

                            // Apply initial/current filter value
                            Object initVal = hasValueSource.getValue();
                            if (initVal != null) {
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                        String.valueOf(filter.getId()), filter.getFilterColumn(), initVal,
                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            }
                        } else {
                            Map<String, Object> currentMasterRecordData = formBinder != null ? formBinder.getBean()
                                    : null;
                            Object fallbackVal = filter.getSourceName();
                            if (currentMasterRecordData != null && lookupKey != null
                                    && currentMasterRecordData.containsKey(lookupKey)) {
                                fallbackVal = currentMasterRecordData.get(lookupKey);
                            }
                            com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                    String.valueOf(filter.getId()), filter.getFilterColumn(), fallbackVal,
                                    filter.getLogicalOperator(), filter.getComparisonOperator());
                            applyFilterToComponent(targetComponent, condition);
                        }
                    } else if ("STATIC".equalsIgnoreCase(filter.getSourceType())) {
                        Object staticVal = filter.getSourceName();
                        String lookupKey = staticVal != null ? staticVal.toString() : "";
                        if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.")
                                || lookupKey.startsWith("detail.") || lookupKey.startsWith("\"detail.")) {
                            if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.")) {
                                lookupKey = lookupKey.replaceAll("[\"']", "")
                                        .substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                            } else {
                                lookupKey = lookupKey.replaceAll("[\"']", "")
                                        .substring(lookupKey.indexOf("detail.") + "detail.".length()).trim();
                            }
                            Component sc = formComponents.get(lookupKey);
                            if (sc == null && field.isDetail())
                                sc = detailEditorComponents.get(lookupKey);
                            if (sc == null && ("unique".equalsIgnoreCase(lookupKey)
                                    || "isunique".equalsIgnoreCase(lookupKey))) {
                                sc = formComponents.get("unique");
                                if (sc == null)
                                    sc = formComponents.get("isunique");
                                if (sc == null && field.isDetail()) {
                                    sc = detailEditorComponents.get("unique");
                                    if (sc == null)
                                        sc = detailEditorComponents.get("isunique");
                                }
                            }
                            Map<String, Object> currentMasterRecordData = formBinder != null ? formBinder.getBean()
                                    : null;
                            if (sc instanceof com.vaadin.flow.component.HasValue<?, ?> hv) {
                                staticVal = hv.getValue();
                            } else if (currentMasterRecordData != null
                                    && currentMasterRecordData.containsKey(lookupKey)) {
                                staticVal = currentMasterRecordData.get(lookupKey);
                            }
                        }
                        com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                String.valueOf(filter.getId()), filter.getFilterColumn(), staticVal,
                                filter.getLogicalOperator(), filter.getComparisonOperator());
                        applyFilterToComponent(targetComponent, condition);
                    } else if ("QUERY".equalsIgnoreCase(filter.getSourceType())) {
                        String paramName = filter.getSourceName();
                        if (queryParameters != null && queryParameters.getParameters().containsKey(paramName)) {
                            java.util.List<String> vals = queryParameters.getParameters().get(paramName);
                            if (vals != null && !vals.isEmpty()) {
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                        String.valueOf(filter.getId()), filter.getFilterColumn(), vals.get(0),
                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            }
                        }
                    }
                }
            }
        }

        gridBinder.addValueChangeListener(event -> {
            if (isEvaluatingGridFormulas) {
                return;
            }
            isEvaluatingGridFormulas = true;
            try {
                // Get current values from all editor components
                Map<String, Object> tempRow = new HashMap<>();
                for (Map.Entry<String, Component> entry : detailEditorComponents.entrySet()) {
                    String fName = entry.getKey();
                    Component comp = entry.getValue();
                    if (comp instanceof HasValue) {
                        tempRow.put(fName, ((HasValue<?, ?>) comp).getValue());
                    }
                }

                // Evaluate formula fields
                for (FieldMeta field : detailFields) {
                    if (field.getFormula() != null && !field.getFormula().trim().isEmpty()) {
                        try {
                            double calculated = FormulaEvaluator.evaluate(field.getFormula(), tempRow);
                            Component comp = detailEditorComponents.get(field.getFieldName());
                            if (comp instanceof HasValue) {
                                @SuppressWarnings("unchecked")
                                HasValue<?, Object> hasValue = (HasValue<?, Object>) comp;
                                Object converted = convertToFieldValue(calculated, comp);
                                if (converted != null && !converted.equals(hasValue.getValue())) {
                                    hasValue.setValue(converted);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            } finally {
                isEvaluatingGridFormulas = false;
            }
        });

        // Auto-edit on row click
        detailsGrid.addItemClickListener(event -> {
            Map<String, Object> item = event.getItem();
            if (item != null) {
                if (editor.isOpen()) {
                    Map<String, Object> prevItem = editor.getItem();
                    if (prevItem == item) {
                        return; // already editing this item
                    }
                    editor.cancel();
                    if (prevItem != null) {
                        calculateRowTotal(prevItem);
                        detailsGrid.getDataProvider().refreshItem(prevItem);
                    }
                }
                applyMasterFiltersToDetailEditors(formDef);
                isLoadingExistingData = true;
                try {
                    editor.editItem(item);
                } finally {
                    isLoadingExistingData = false;
                }
            }
        });

        // 2. Setup Header Filter Row for Details Grid
        com.vaadin.flow.component.grid.HeaderRow filterRow = detailsGrid.appendHeaderRow();
        columnsMap.forEach((fieldName, col) -> {
            FilterCriteria criteria = new FilterCriteria();
            detailsFilterValues.put(fieldName, criteria);

            TextField filterField = new TextField();
            filterField.setPlaceholder("Filter...");
            filterField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
            filterField.setWidthFull();
            filterField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL);

            Button filterButton = new Button(com.vaadin.flow.component.icon.VaadinIcon.FILTER.create());
            filterButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE);
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
                    applyDetailsFilters();
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
                com.vaadin.flow.component.notification.Notification.show(
                        nextFrozen ? "Kolom dibekukan" : "Kolom dilepas", 2000,
                        com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END);
            });

            filterField.addValueChangeListener(e -> {
                if (detailsGrid.getEditor().isOpen()) {
                    detailsGrid.getEditor().cancel();
                }
                criteria.value = e.getValue();
                applyDetailsFilters();
            });

            filterRow.getCell(col).setComponent(filterField);
        });

        // 3. Row Drag and Drop for Details Grid
        detailsGrid.setRowsDraggable(true);
        detailDragStartReg = detailsGrid.addDragStartListener(event -> {
            if (!event.getDraggedItems().isEmpty()) {
                draggedDetailItem = event.getDraggedItems().get(0);
                detailsGrid.setDropMode(com.vaadin.flow.component.grid.dnd.GridDropMode.BETWEEN);
            }
        });

        detailDropReg = detailsGrid.addDropListener(event -> {
            Map<String, Object> targetItem = event.getDropTargetItem().orElse(null);
            if (targetItem != null && draggedDetailItem != null && targetItem != draggedDetailItem) {
                int indexDragged = findIndexByReference(detailsList, draggedDetailItem);
                int indexTarget = findIndexByReference(detailsList, targetItem);

                if (indexDragged >= 0 && indexTarget >= 0) {
                    detailsList.remove(indexDragged);
                    int newIndex = findIndexByReference(detailsList, targetItem);
                    if (event.getDropLocation() == com.vaadin.flow.component.grid.dnd.GridDropLocation.BELOW) {
                        detailsList.add(newIndex + 1, draggedDetailItem);
                    } else {
                        detailsList.add(newIndex, draggedDetailItem);
                    }
                    applyDetailsFilters();
                }
            }
            draggedDetailItem = null;
        });

        detailDragEndReg = detailsGrid.addDragEndListener(event -> {
            draggedDetailItem = null;
            detailsGrid.setDropMode(null);
        });

        // 4. Column Reordering for Details Grid
        detailsGrid.setColumnReorderingAllowed(true);
        detailColReorderReg = detailsGrid.addColumnReorderListener(event -> {
            java.util.List<Grid.Column<Map<String, Object>>> newOrder = event.getColumns();
            java.util.List<String> orderedFieldNames = new java.util.ArrayList<>();
            for (Grid.Column<Map<String, Object>> col : newOrder) {
                String fieldName = detailsColumnToFieldNameMap.get(col);
                if (fieldName != null) {
                    orderedFieldNames.add(fieldName);
                }
            }
            try {
                dynamicDataService.saveUserGridOrder(currentFormCode, "detailsGrid", orderedFieldNames);
                Notification.show("Urutan kolom disimpan", 1500, Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000,
                        Notification.Position.MIDDLE);
            }
        });

        // 5. Enable multi-sort on details grid
        detailsGrid.setMultiSort(true);

        applyDetailsFilters();
        java.util.List<String> detailsUserOrder = dynamicDataService.getUserGridOrder(currentFormCode, "detailsGrid");
        com.vaadinerp.components.StandardGridUtils.applySafeColumnOrder(detailsGrid, detailsColumnToFieldNameMap,
                detailsUserOrder);
    }

    private void applyDetailsFilters() {
        List<Map<String, Object>> filtered = detailsList.stream().filter(item -> {
            for (Map.Entry<String, FilterCriteria> entry : detailsFilterValues.entrySet()) {
                String fieldName = entry.getKey();
                FilterCriteria criteria = entry.getValue();

                String op = criteria.operator;
                String query = criteria.value;

                Object val = item.get(fieldName);
                String strVal = val != null ? val.toString().toLowerCase() : "";

                String lovLabelVal = "";
                if (currentFormDef != null && currentFormDef.getFields() != null) {
                    for (FieldMeta f : currentFormDef.getFields()) {
                        if (fieldName.equalsIgnoreCase(f.getFieldName()) && f.getLovCode() != null
                                && !f.getLovCode().trim().isEmpty()) {
                            String lbl = com.vaadinerp.components.ComponentFactory.formatFieldValueWithLov(f, val,
                                    dynamicDataService);
                            if (lbl != null)
                                lovLabelVal = lbl.toLowerCase();
                            break;
                        }
                    }
                }

                if ("Blank".equals(op)) {
                    if (!strVal.isEmpty() && !lovLabelVal.isEmpty())
                        return false;
                    continue;
                }
                if ("Not blank".equals(op)) {
                    if (strVal.isEmpty() && lovLabelVal.isEmpty())
                        return false;
                    continue;
                }

                if (query == null || query.trim().isEmpty()) {
                    continue;
                }

                query = query.toLowerCase();

                switch (op) {
                    case "Contains":
                        if (!strVal.contains(query) && !lovLabelVal.contains(query))
                            return false;
                        break;
                    case "Not contains":
                        if (strVal.contains(query) || lovLabelVal.contains(query))
                            return false;
                        break;
                    case "Equals":
                        if (!strVal.equals(query) && !lovLabelVal.equals(query))
                            return false;
                        break;
                    case "Not equal":
                        if (strVal.equals(query) || lovLabelVal.equals(query))
                            return false;
                        break;
                    case "Starts with":
                        if (!strVal.startsWith(query) && !lovLabelVal.startsWith(query))
                            return false;
                        break;
                    case "Ends with":
                        if (!strVal.endsWith(query) && !lovLabelVal.endsWith(query))
                            return false;
                        break;
                }
            }
            return true;
        }).collect(Collectors.toList());

        filteredDetailsList.clear();
        filteredDetailsList.addAll(filtered);

        com.vaadin.flow.data.provider.DataProvider<Map<String, Object>, ?> dp = detailsGrid.getDataProvider();
        if (dp instanceof com.vaadin.flow.data.provider.ListDataProvider) {
            @SuppressWarnings("unchecked")
            com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>> ldp = (com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>>) dp;
            java.util.Collection<Map<String, Object>> backingItems = ldp.getItems();
            if (backingItems == filteredDetailsList) {
                // Same backing list — just refresh to preserve sort state
                ldp.refreshAll();
            } else if (backingItems instanceof java.util.List && backingItems.size() == 0
                    && filteredDetailsList.size() == 0) {
                // Both empty, no need to reset
                ldp.refreshAll();
            } else {
                // Different backing collection — must reset, but preserve sort order
                java.util.List<com.vaadin.flow.component.grid.GridSortOrder<Map<String, Object>>> currentSort = detailsGrid
                        .getSortOrder();
                detailsGrid.setItems(filteredDetailsList);
                if (currentSort != null && !currentSort.isEmpty()) {
                    detailsGrid.sort(currentSort);
                }
            }
        } else {
            // First time or non-list provider — set items and preserve any sort
            java.util.List<com.vaadin.flow.component.grid.GridSortOrder<Map<String, Object>>> currentSort = detailsGrid
                    .getSortOrder();
            detailsGrid.setItems(filteredDetailsList);
            if (currentSort != null && !currentSort.isEmpty()) {
                detailsGrid.sort(currentSort);
            }
        }
    }

    private void calculateRowTotal(Map<String, Object> row) {
        if (currentFormDef == null)
            return;
        try {
            Object qtyVal = row.get("qty");
            Object priceVal = row.get("price");
            if (qtyVal != null && priceVal != null) {
                double qty = Double.parseDouble(qtyVal.toString());
                double price = Double.parseDouble(priceVal.toString());
                row.put("total_price", qty * price);
                row.put("amount", qty * price);
            }
        } catch (Exception ignored) {
        }

        for (FieldMeta field : currentFormDef.getFields()) {
            if (field.isDetail() && field.getFormula() != null && !field.getFormula().trim().isEmpty()) {
                try {
                    double calculated = FormulaEvaluator.evaluate(field.getFormula(), row);
                    row.put(field.getFieldName(), calculated);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void evaluateFormulas() {
        if (isEvaluatingFormulas || currentFormDef == null || formBinder.getBean() == null) {
            return;
        }
        isEvaluatingFormulas = true;
        try {
            Map<String, Object> bean = formBinder.getBean();
            // sync component values to bean
            for (Map.Entry<String, Component> entry : formComponents.entrySet()) {
                String fieldName = entry.getKey();
                Component comp = entry.getValue();
                if (comp instanceof com.vaadin.flow.component.HasValue) {
                    bean.put(fieldName, ((com.vaadin.flow.component.HasValue<?, ?>) comp).getValue());
                }
            }

            for (FieldMeta field : currentFormDef.getFields()) {
                if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) {
                    continue;
                }
                if (!field.isDetail() && field.getFormula() != null && !field.getFormula().trim().isEmpty()) {
                    double calculated = FormulaEvaluator.evaluate(field.getFormula(), bean);
                    bean.put(field.getFieldName(), calculated);
                    Component comp = formComponents.get(field.getFieldName());
                    if (comp instanceof com.vaadin.flow.component.HasValue) {
                        @SuppressWarnings("unchecked")
                        com.vaadin.flow.component.HasValue<?, Object> hasValue = (com.vaadin.flow.component.HasValue<?, Object>) comp;
                        Object converted = convertToFieldValue(calculated, comp);
                        if (converted != null && !converted.equals(hasValue.getValue())) {
                            hasValue.setValue(converted);
                        }
                    }
                }
            }
        } finally {
            isEvaluatingFormulas = false;
        }
    }

    private void refreshMasterGridData() {
        if (currentFormDef != null) {
            lovLabelMapCache.clear();
            com.vaadinerp.components.ComponentFactory.clearLovCache(null);
            if (currentSortField == null) {
                currentSortField = currentFormDef.getDefaultSortField();
                currentSortDir = currentFormDef.getDefaultSortDirection();
            }
            applyFilters();
        }
    }

    private void applyFilters() {
        if (currentFormDef == null)
            return;
        long totalRecords = dynamicDataService.countGridData(currentFormDef, filterValues);
        if (paginationBar != null) {
            paginationBar.setTotalRecords(totalRecords);
        }
        com.vaadin.flow.data.provider.DataProvider<Map<String, Object>, ?> dp = masterGrid.getDataProvider();
        if (dp instanceof com.vaadin.flow.data.provider.CallbackDataProvider) {
            dp.refreshAll();
        } else {
            updateMasterGridDataProvider();
        }
    }

    private void updateMasterGridDataProvider() {
        if (currentFormDef == null)
            return;
        com.vaadin.flow.data.provider.CallbackDataProvider<Map<String, Object>, Void> dataProvider = new com.vaadin.flow.data.provider.CallbackDataProvider<>(
                query -> {
                    int queryOffset = query.getOffset();
                    int queryLimit = query.getLimit();
                    int effectiveOffset = (paginationBar != null ? paginationBar.getOffset() : 0) + queryOffset;

                    String sortField = currentSortField;
                    String sortDir = currentSortDir;
                    if (!query.getSortOrders().isEmpty()) {
                        com.vaadin.flow.data.provider.QuerySortOrder order = query.getSortOrders().get(0);
                        sortField = order.getSorted();
                        sortDir = order.getDirection() == com.vaadin.flow.data.provider.SortDirection.DESCENDING
                                ? "DESC"
                                : "ASC";
                    }

                    java.util.List<Map<String, Object>> chunk = dynamicDataService.fetchGridDataPaged(
                            currentFormDef, effectiveOffset, queryLimit, filterValues, sortField, sortDir);

                    allMasterGridItems.clear();
                    allMasterGridItems.addAll(chunk);
                    masterGridItems.clear();
                    masterGridItems.addAll(chunk);

                    return chunk.stream();
                },
                query -> {
                    long total = dynamicDataService.countGridData(currentFormDef, filterValues);
                    if (paginationBar != null) {
                        paginationBar.setTotalRecords(total);
                        long pageLimit = paginationBar.getLimit();
                        long remaining = total - paginationBar.getOffset();
                        if (remaining < 0)
                            remaining = 0;
                        return (int) Math.min(pageLimit, remaining);
                    }
                    return (int) Math.min(total, Integer.MAX_VALUE);
                },
                item -> {
                    if (item == null)
                        return 0;
                    String pk = currentFormDef != null && currentFormDef.getPrimaryKey() != null
                            ? currentFormDef.getPrimaryKey()
                            : "id";
                    Object pkVal = item.get(pk);
                    return pkVal != null ? pkVal : System.identityHashCode(item);
                });
        masterGrid.setDataProvider(dataProvider);
        com.vaadinerp.components.StandardGridUtils.attachSelectAllHeader(masterGrid, () -> {
            if (currentFormDef == null)
                return java.util.Collections.emptyList();
            int offset = paginationBar != null ? paginationBar.getOffset() : 0;
            int limit = paginationBar != null ? paginationBar.getLimit() : 1000;
            return dynamicDataService.fetchGridDataPaged(currentFormDef, offset, limit, filterValues, currentSortField,
                    currentSortDir);
        });
    }

    private int findIndexByReference(List<Map<String, Object>> list, Map<String, Object> item) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private <V> V convertToFieldValue(Object value, Component component) {
        if (value == null) {
            if (component instanceof com.vaadin.flow.component.checkbox.Checkbox) {
                return (V) Boolean.FALSE;
            }
            if (component instanceof com.vaadin.flow.component.textfield.TextField ||
                    component instanceof com.vaadin.flow.component.textfield.TextArea) {
                return (V) "";
            }
            return null;
        }
        if (component instanceof com.vaadin.flow.component.datepicker.DatePicker) {
            if (value instanceof java.time.LocalDate)
                return (V) value;
            if (value instanceof java.time.LocalDateTime)
                return (V) ((java.time.LocalDateTime) value).toLocalDate();
            if (value instanceof java.sql.Timestamp)
                return (V) ((java.sql.Timestamp) value).toLocalDateTime().toLocalDate();
            if (value instanceof java.sql.Date)
                return (V) ((java.sql.Date) value).toLocalDate();
            if (value instanceof java.util.Date)
                return (V) new java.sql.Date(((java.util.Date) value).getTime()).toLocalDate();
            try {
                return (V) java.time.LocalDate.parse(value.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.datetimepicker.DateTimePicker) {
            if (value instanceof java.time.LocalDateTime)
                return (V) value;
            if (value instanceof java.time.LocalDate)
                return (V) ((java.time.LocalDate) value).atStartOfDay();
            if (value instanceof java.sql.Timestamp)
                return (V) ((java.sql.Timestamp) value).toLocalDateTime();
            if (value instanceof java.util.Date)
                return (V) new java.sql.Timestamp(((java.util.Date) value).getTime()).toLocalDateTime();
            try {
                return (V) java.time.LocalDateTime.parse(value.toString().replace(" ", "T"));
            } catch (Exception e) {
                try {
                    return (V) java.time.LocalDate.parse(value.toString()).atStartOfDay();
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        if (component instanceof com.vaadin.flow.component.timepicker.TimePicker) {
            if (value instanceof java.time.LocalTime)
                return (V) value;
            if (value instanceof java.sql.Time)
                return (V) ((java.sql.Time) value).toLocalTime();
            try {
                return (V) java.time.LocalTime.parse(value.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.textfield.BigDecimalField
                || component instanceof com.vaadinerp.components.FormattedBigDecimalField) {
            if (value instanceof java.math.BigDecimal)
                return (V) value;
            try {
                return (V) new java.math.BigDecimal(value.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.textfield.IntegerField
                || component instanceof com.vaadinerp.components.FormattedIntegerField) {
            if (value instanceof Integer)
                return (V) value;
            if (value instanceof Number)
                return (V) Integer.valueOf(((Number) value).intValue());
            try {
                return (V) Integer.valueOf(Integer.parseInt(value.toString()));
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.checkbox.Checkbox) {
            if (value instanceof Boolean)
                return (V) value;
            return (V) Boolean.valueOf(value.toString().equalsIgnoreCase("true") || value.toString().equals("1"));
        }
        if (component instanceof com.vaadin.flow.component.textfield.TextField ||
                component instanceof com.vaadin.flow.component.textfield.TextArea ||
                component instanceof com.vaadin.flow.component.combobox.ComboBox ||
                component instanceof com.vaadin.flow.component.select.Select ||
                component instanceof com.vaadin.flow.component.listbox.ListBox ||
                component instanceof com.vaadin.flow.component.radiobutton.RadioButtonGroup ||
                component instanceof com.vaadinerp.components.LovComboBox ||
                component instanceof com.vaadinerp.components.LovSelect ||
                component instanceof com.vaadinerp.components.BandboxField) {
            return (V) (value != null ? value.toString() : null);
        }
        return (V) value;
    }

    @SuppressWarnings("unchecked")
    private <V> void bindComponent(Binder<Map<String, Object>> binder, Component editComponent, FieldMeta field) {
        if (editComponent instanceof com.vaadinerp.components.SubformGridField subformGrid) {
            subformGrid.setHeaderRecordSupplier(() -> binder.getBean());
        }
        HasValue<?, V> hasValue = (HasValue<?, V>) editComponent;
        Binder.BindingBuilder<Map<String, Object>, V> builder = binder.forField(hasValue);
        if (field.isRequired()) {
            builder.asRequired(field.getFieldLabel() + " wajib diisi");
        }
        builder.bind(
                map -> convertToFieldValue(getValueCaseInsensitive(map, field.getFieldName()), editComponent),
                (map, value) -> {
                    putValueCaseInsensitive(map, field.getFieldName(), value);
                    if (value != null && !(value instanceof Map)) {
                        putValueCaseInsensitive(map, field.getFieldName() + ".id", value);
                    }
                    String dispLabel = null;
                    Map<String, Object> selMap = null;
                    if (editComponent instanceof com.vaadinerp.components.BandboxField<?, ?> bandbox) {
                        dispLabel = bandbox.getDisplayLabel();
                        Object selItem = bandbox.getSelectedItem();
                        if (selItem instanceof Map)
                            selMap = (Map<String, Object>) selItem;
                    } else if (editComponent instanceof com.vaadinerp.components.LovComboBox lovCombo) {
                        dispLabel = lovCombo.getDisplayLabel();
                        selMap = lovCombo.getSelectedRecord();
                    } else if (editComponent instanceof com.vaadinerp.components.LovSelect lovSel) {
                        dispLabel = lovSel.getDisplayLabel();
                        selMap = lovSel.getSelectedRecord();
                    } else if (editComponent instanceof com.vaadinerp.components.LovChosenBox lovChosen) {
                        dispLabel = lovChosen.getDisplayLabel();
                    }
                    if (dispLabel != null) {
                        putValueCaseInsensitive(map, field.getFieldName() + "_label", dispLabel);
                    }
                    if (selMap != null) {
                        putValueCaseInsensitive(map, field.getFieldName() + "_record", selMap);
                        for (Map.Entry<String, Object> entry : selMap.entrySet()) {
                            if (entry.getKey() != null && entry.getValue() != null) {
                                putValueCaseInsensitive(map, field.getFieldName() + "." + entry.getKey(),
                                        entry.getValue());
                            }
                        }
                    }
                });
        hasValue.addValueChangeListener(e -> {
            Map<String, Object> bean = binder.getBean();
            if (bean != null) {
                putValueCaseInsensitive(bean, field.getFieldName(), e.getValue());
                if (e.getValue() != null && !(e.getValue() instanceof Map)) {
                    putValueCaseInsensitive(bean, field.getFieldName() + ".id", e.getValue());
                }
                String dispLabel = null;
                Map<String, Object> selMap = null;
                if (editComponent instanceof com.vaadinerp.components.BandboxField<?, ?> bandbox) {
                    dispLabel = bandbox.getDisplayLabel();
                    Object selItem = bandbox.getSelectedItem();
                    if (selItem instanceof Map)
                        selMap = (Map<String, Object>) selItem;
                } else if (editComponent instanceof com.vaadinerp.components.LovComboBox lovCombo) {
                    dispLabel = lovCombo.getDisplayLabel();
                    selMap = lovCombo.getSelectedRecord();
                } else if (editComponent instanceof com.vaadinerp.components.LovSelect lovSel) {
                    dispLabel = lovSel.getDisplayLabel();
                    selMap = lovSel.getSelectedRecord();
                } else if (editComponent instanceof com.vaadinerp.components.LovChosenBox lovChosen) {
                    dispLabel = lovChosen.getDisplayLabel();
                }
                if (dispLabel != null) {
                    putValueCaseInsensitive(bean, field.getFieldName() + "_label", dispLabel);
                }
                if (selMap != null) {
                    putValueCaseInsensitive(bean, field.getFieldName() + "_record", selMap);
                    for (Map.Entry<String, Object> entry : selMap.entrySet()) {
                        if (entry.getKey() != null && entry.getValue() != null) {
                            putValueCaseInsensitive(bean, field.getFieldName() + "." + entry.getKey(),
                                    entry.getValue());
                        }
                    }
                }
            }
        });
    }

    private Object getValueCaseInsensitive(Map<String, Object> map, String key) {
        if (map == null || key == null)
            return null;
        if (map.containsKey(key))
            return map.get(key);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void putValueCaseInsensitive(Map<String, Object> map, String key, Object value) {
        if (map == null || key == null)
            return;
        for (String k : map.keySet()) {
            if (k != null && k.equalsIgnoreCase(key)) {
                map.put(k, value);
                return;
            }
        }
        map.put(key, value);
    }

    private void updateTitle(FormMeta formDef, boolean isUpdate) {
        if (formDef == null)
            return;
        String baseTitle = formDef.getFormTitle() != null ? formDef.getFormTitle() : "Form: " + formDef.getFormCode();
        if (tabSheet.getSelectedTab() == transaksiTab) {
            title.setText(baseTitle + (isUpdate ? " - [Mode: Ubah / Update]" : " - [Mode: Tambah / Insert]"));
        } else {
            title.setText(baseTitle);
        }
    }

    private void showConfirmDialog(String titleText, String message, Runnable confirmAction) {
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle(titleText);
        dialog.add(new com.vaadin.flow.component.html.Paragraph(message));

        Button btnConfirm = new Button("Ya, Hapus", event -> {
            confirmAction.run();
            dialog.close();
        });
        btnConfirm.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);

        Button btnCancel = new Button("Batal", event -> dialog.close());
        btnCancel.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(btnCancel, btnConfirm);
        dialog.open();
    }

    private void applyFilterToComponent(Component targetComponent, com.vaadinerp.components.FilterCondition condition) {
        if (targetComponent == null) {
            return;
        }
        if (targetComponent instanceof LovComboBox) {
            LovComboBox combo = (LovComboBox) targetComponent;
            combo.setFilterValue(condition);
            if (!isLoadingExistingData) {
                combo.clear();
            }
        } else if (targetComponent instanceof LovSelect) {
            LovSelect select = (LovSelect) targetComponent;
            select.setFilterValue(condition);
            if (!isLoadingExistingData) {
                select.clear();
            }
        } else if (targetComponent instanceof BandboxField) {
            BandboxField<?, ?> bandbox = (BandboxField<?, ?>) targetComponent;
            bandbox.setFilterValue(condition);
            if (!isLoadingExistingData) {
                bandbox.clear();
            }
        }
    }

    private void clearAllComponents() {
        if (formComponents != null) {
            for (Component comp : formComponents.values()) {
                if (comp instanceof com.vaadin.flow.component.HasValue) {
                    ((com.vaadin.flow.component.HasValue<?, ?>) comp).clear();
                } else if (comp instanceof com.vaadinerp.components.SubformGridField) {
                    ((com.vaadinerp.components.SubformGridField) comp).setValue(new ArrayList<>());
                }

                if (comp instanceof com.vaadin.flow.component.HasEnabled hasEnabled) {
                    hasEnabled.setEnabled(true);
                }
            }
        }
        updateFieldsReadonlyStatus(true);
    }

    private void updateFieldsReadonlyStatus(boolean isNewRecord) {
        if (currentFormDef != null && formComponents != null) {
            for (FieldMeta field : currentFormDef.getFields()) {
                Component comp = formComponents.get(field.getFieldName());
                if (comp != null) {
                    com.vaadinerp.components.ComponentFactory.applyReadonlyMode(comp, field, isNewRecord);
                }
            }
        }
    }

    private void applyMasterFiltersToDetailEditors(FormMeta formDef) {
        for (FieldMeta field : formDef.getFields()) {
            if (field.isDetail() && field.getFilters() != null) {
                Component targetComponent = detailEditorComponents.get(field.getFieldName());
                if (targetComponent != null) {
                    for (com.vaadinerp.meta.FieldFilterMeta filter : field.getFilters()) {
                        if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                            String sourceFieldName = filter.getSourceName();
                            String lookupKey = sourceFieldName;
                            if (lookupKey != null
                                    && (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header."))) {
                                lookupKey = lookupKey.replaceAll("[\"']", "")
                                        .substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                            } else if (lookupKey != null
                                    && (lookupKey.startsWith("detail.") || lookupKey.startsWith("\"detail."))) {
                                lookupKey = lookupKey.replaceAll("[\"']", "")
                                        .substring(lookupKey.indexOf("detail.") + "detail.".length()).trim();
                            }
                            Component sourceComponent = lookupKey != null ? formComponents.get(lookupKey) : null;
                            if (sourceComponent == null && lookupKey != null) {
                                sourceComponent = detailEditorComponents.get(lookupKey);
                            }
                            if (sourceComponent == null && sourceFieldName != null) {
                                sourceComponent = formComponents.get(sourceFieldName);
                                if (sourceComponent == null)
                                    sourceComponent = detailEditorComponents.get(sourceFieldName);
                            }
                            if (sourceComponent == null && ("unique".equalsIgnoreCase(lookupKey)
                                    || "isunique".equalsIgnoreCase(lookupKey))) {
                                sourceComponent = formComponents.get("unique");
                                if (sourceComponent == null)
                                    sourceComponent = formComponents.get("isunique");
                                if (sourceComponent == null) {
                                    sourceComponent = detailEditorComponents.get("unique");
                                    if (sourceComponent == null)
                                        sourceComponent = detailEditorComponents.get("isunique");
                                }
                            }
                            if (sourceComponent instanceof HasValue) {
                                Object val = ((HasValue<?, ?>) sourceComponent).getValue();
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                        String.valueOf(filter.getId()), filter.getFilterColumn(), val,
                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            } else {
                                Map<String, Object> currentMasterRecordData = formBinder != null ? formBinder.getBean()
                                        : null;
                                Object fallbackVal = filter.getSourceName();
                                if (currentMasterRecordData != null && lookupKey != null
                                        && currentMasterRecordData.containsKey(lookupKey)) {
                                    fallbackVal = currentMasterRecordData.get(lookupKey);
                                }
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                        String.valueOf(filter.getId()), filter.getFilterColumn(), fallbackVal,
                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            }
                        } else if ("STATIC".equalsIgnoreCase(filter.getSourceType())) {
                            Object staticVal = filter.getSourceName();
                            String lookupKey = staticVal != null ? staticVal.toString() : "";
                            if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.")
                                    || lookupKey.startsWith("detail.") || lookupKey.startsWith("\"detail.")) {
                                if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.")) {
                                    lookupKey = lookupKey.replaceAll("[\"']", "")
                                            .substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                                } else {
                                    lookupKey = lookupKey.replaceAll("[\"']", "")
                                            .substring(lookupKey.indexOf("detail.") + "detail.".length()).trim();
                                }
                                Component sc = formComponents.get(lookupKey);
                                if (sc == null)
                                    sc = detailEditorComponents.get(lookupKey);
                                if (sc == null && ("unique".equalsIgnoreCase(lookupKey)
                                        || "isunique".equalsIgnoreCase(lookupKey))) {
                                    sc = formComponents.get("unique");
                                    if (sc == null)
                                        sc = formComponents.get("isunique");
                                    if (sc == null) {
                                        sc = detailEditorComponents.get("unique");
                                        if (sc == null)
                                            sc = detailEditorComponents.get("isunique");
                                    }
                                }
                                Map<String, Object> currentMasterRecordData = formBinder != null ? formBinder.getBean()
                                        : null;
                                if (sc instanceof com.vaadin.flow.component.HasValue<?, ?> hv) {
                                    staticVal = hv.getValue();
                                } else if (currentMasterRecordData != null
                                        && currentMasterRecordData.containsKey(lookupKey)) {
                                    staticVal = currentMasterRecordData.get(lookupKey);
                                }
                            }
                            com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                    String.valueOf(filter.getId()), filter.getFilterColumn(), staticVal,
                                    filter.getLogicalOperator(), filter.getComparisonOperator());
                            applyFilterToComponent(targetComponent, condition);
                        }
                    }
                }
            }
        }
    }
}
