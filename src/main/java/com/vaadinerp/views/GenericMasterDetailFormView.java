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
    private TabSheet tabSheet;
    private Tab historisTab;
    private Tab transaksiTab;
    private H3 title;
    
    private String currentFormCode;
    private FormMeta currentFormDef;
    private Runnable closeHandler;

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

    private final HorizontalLayout masterGridToolbar = new HorizontalLayout();

    // Details Grid Filters and Reordering State
    private final List<Map<String, Object>> filteredDetailsList = new ArrayList<>();
    private final Map<String, FilterCriteria> detailsFilterValues = new HashMap<>();
    private Map<String, Object> draggedDetailItem;
    private final Map<Grid.Column<Map<String, Object>>, String> detailsColumnToFieldNameMap = new HashMap<>();
    private boolean isEvaluatingFormulas = false;
    private boolean isEvaluatingGridFormulas = false;

    private final Map<String, Map<String, String>> lovLabelMapCache = new HashMap<>();
    private final Map<String, String> fieldNameToLovCodeMap = new HashMap<>();

    // Flag to prevent cascading filter listeners from clearing child LOV values during data loading
    private boolean isLoadingExistingData = false;

    private Object getMapValIgnoreCase(Map<String, Object> rec, String col) {
        if (col == null || rec == null) return null;
        if (rec.containsKey(col)) return rec.get(col);
        for (Map.Entry<String, Object> e : rec.entrySet()) {
            if (e.getKey().equalsIgnoreCase(col)) return e.getValue();
        }
        return null;
    }

    private String getLovDisplayLabel(String lovCode, String val) {
        if (val == null || val.trim().isEmpty() || lovCode == null || lovCode.trim().isEmpty()) return val != null ? val : "";
        String strVal = val.trim();
        Map<String, String> map = lovLabelMapCache.computeIfAbsent(lovCode, code -> {
            Map<String, String> res = new HashMap<>();
            dynamicDataService.getLovMeta(code).ifPresent(lovMeta -> {
                java.util.List<Map<String, Object>> records = dynamicDataService.fetchAllLovRecords(lovMeta);
                String valCol = lovMeta.getValueColumn() != null && !lovMeta.getValueColumn().isBlank() ? lovMeta.getValueColumn().trim() : "id";
                String lblCol = lovMeta.getLabelColumn() != null && !lovMeta.getLabelColumn().isBlank() ? lovMeta.getLabelColumn().trim() : valCol;
                for (Map<String, Object> rec : records) {
                    Object v = getMapValIgnoreCase(rec, valCol);
                    if (v == null && rec.containsKey("id")) v = rec.get("id");
                    if (v != null) {
                        Object l = getMapValIgnoreCase(rec, lblCol);
                        if (l == null || l.toString().trim().isEmpty()) {
                            if (getMapValIgnoreCase(rec, "code") != null) l = getMapValIgnoreCase(rec, "code");
                            else if (getMapValIgnoreCase(rec, "name") != null) l = getMapValIgnoreCase(rec, "name");
                            else l = v;
                        }
                        res.put(v.toString().trim(), l.toString().trim());
                    }
                }
            });
            return res;
        });

        if (strVal.contains(",")) {
            return java.util.Arrays.stream(strVal.split(","))
                    .map(String::trim)
                    .map(item -> map.getOrDefault(item, item))
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        return map.getOrDefault(strVal, strVal);
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    public GenericMasterDetailFormView(FormMetaRepository formMetaRepository, DynamicDataService dynamicDataService) {
        this(formMetaRepository, dynamicDataService, null);
    }

    public GenericMasterDetailFormView(FormMetaRepository formMetaRepository, DynamicDataService dynamicDataService, com.vaadinerp.security.service.SessionSecurityService securityService) {
        this.formMetaRepository = formMetaRepository;
        this.dynamicDataService = dynamicDataService;
        this.securityService = securityService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        title = new H3("Loading Master-Detail...");
        
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

        masterGrid.setWidthFull();
        masterGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        masterGrid.setAllRowsVisible(true);

        // Setup Tab Sheet layouts
        VerticalLayout historisLayout = new VerticalLayout();
        historisLayout.setSizeFull();
        historisLayout.setPadding(true);
        historisLayout.setSpacing(true);
        historisLayout.add(masterGridToolbar, masterGrid);
        
        VerticalLayout transaksiLayout = new VerticalLayout();
        transaksiLayout.setWidthFull();
        
        formLayout.setWidthFull();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        
        // Details Toolbar
        HorizontalLayout detailsToolbar = new HorizontalLayout();
        detailsToolbar.setWidthFull();
        detailsToolbar.setAlignItems(Alignment.CENTER);
        H4 detailTitle = new H4("Rincian / Details");
        detailTitle.getStyle().set("margin", "0").set("flex-grow", "1");
        
        Button btnAddRow = new Button("Tambah Baris", VaadinIcon.PLUS.create());
        btnAddRow.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        
        Button btnDeleteRow = new Button("Hapus Baris", VaadinIcon.TRASH.create());
        btnDeleteRow.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        Button btnResetDetailsGrid = new Button("Reset Layout", VaadinIcon.ROTATE_LEFT.create());
        btnResetDetailsGrid.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnResetDetailsGrid.addClickListener(e -> {
            if (currentFormDef != null) {
                dynamicDataService.resetUserGridOrder(currentFormCode, "detailsGrid");
                buildDetailsGrid(currentFormDef);
                Notification.show("Layout grid rincian dikembalikan ke default!", 2000, Notification.Position.BOTTOM_END);
            }
        });

        detailsToolbar.add(detailTitle, btnAddRow, btnDeleteRow, btnResetDetailsGrid);

        detailsGrid.setWidthFull();
        detailsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        detailsGrid.setAllRowsVisible(true);
        
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
                if (bean != null && bean.containsKey(pk) && bean.get(pk) != null && !bean.get(pk).toString().trim().isEmpty()) {
                    isUpdate = true;
                }
                updateTitle(currentFormDef, isUpdate);
            }
        });

        add(title, toolbar, tabSheet);

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
                "  if(!targetRow) { if(attempt < 60) setTimeout(function(){tryEdit(attempt+1)},30); return; } " +
                "  try { targetRow.scrollIntoView({behavior:'smooth', block:'center'}); } catch(e){} " +
                "  var cells = targetRow.querySelectorAll('td'); " +
                "  if(cells.length > 0) { " +
                "    cells[0].dispatchEvent(new MouseEvent('dblclick', {bubbles:true, cancelable:true})); " +
                "  } " +
                "} " +
                "tryEdit(0);",
                detailsGrid.getElement(), detailsList.size() - 1
            );
        });

        btnDeleteRow.addClickListener(e -> {
            java.util.Set<Map<String, Object>> selectedItems = detailsGrid.getSelectedItems();
            if (selectedItems != null && !selectedItems.isEmpty()) {
                showConfirmDialog("Konfirmasi Hapus Rincian", "Apakah Anda yakin ingin menghapus " + selectedItems.size() + " baris rincian terpilih ini?", () -> {
                    detailsList.removeAll(selectedItems);
                    deletedDetailsList.addAll(selectedItems);
                    applyDetailsFilters();
                    detailsGrid.deselectAll();
                });
            } else {
                Notification.show("Pilih baris rincian yang ingin dihapus terlebih dahulu.", 3000, Notification.Position.MIDDLE);
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

        formBinder = new Binder<>();
        formBinder.setBean(new HashMap<>());
        formBinder.addValueChangeListener(e -> evaluateFormulas());

        title.setText(formDef.getFormTitle() != null ? formDef.getFormTitle() : "Form: " + formCode);

        buildToolbar(formDef);
        buildMasterForm(formDef);
        buildMasterGrid(formDef);
        buildDetailsGrid(formDef);
        refreshMasterGridData();
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
                    showConfirmDialog("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus " + selectedItems.size() + " data Master-Detail yang dipilih ini?", () -> {
                        toolbar.setEnabled(false);
                        try {
                            for (Map<String, Object> selected : selectedItems) {
                                dynamicDataService.deleteData(formDef, selected);
                            }
                            Notification.show("Data Master-Detail berhasil dihapus!", 3000, Notification.Position.TOP_CENTER);
                            refreshMasterGridData();
                            masterGrid.deselectAll();
                        } catch (Exception ex) {
                            Notification.show("Gagal menghapus: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                        } finally {
                            toolbar.setEnabled(true);
                        }
                    });
                } else {
                    Notification.show("Pilih baris data pada grid terlebih dahulu untuk dihapus.", 3000, Notification.Position.MIDDLE);
                }
            } else {
                Map<String, Object> bean = formBinder.getBean();
                String pk = formDef.getPrimaryKey() != null ? formDef.getPrimaryKey() : "id";
                if (bean != null && bean.containsKey(pk) && bean.get(pk) != null && !bean.get(pk).toString().trim().isEmpty()) {
                    showConfirmDialog("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus data transaksi Master-Detail ini?", () -> {
                        toolbar.setEnabled(false);
                        try {
                            dynamicDataService.deleteData(formDef, bean);
                            Notification.show("Data Master-Detail berhasil dihapus!", 3000, Notification.Position.TOP_CENTER);
                            formBinder.setBean(new HashMap<>());
                            clearAllComponents();
                            detailsList.clear();
                            deletedDetailsList.clear();
                            applyDetailsFilters();
                            refreshMasterGridData();
                            tabSheet.setSelectedTab(historisTab);
                        } catch (Exception ex) {
                            Notification.show("Gagal menghapus: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                        } finally {
                            toolbar.setEnabled(true);
                        }
                    });
                } else {
                    Notification.show("Tidak ada data master terpilih untuk dihapus.", 3000, Notification.Position.MIDDLE);
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
                                    ((com.vaadin.flow.component.HasValidation) comp).setErrorMessage(field.getFieldLabel() + " wajib diisi");
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
                    dynamicDataService.saveMasterDetailData(formDef, formBinder.getBean(), detailsList, deletedDetailsList);
                    Notification.show("Data berhasil disimpan secara transactional!", 3000, Notification.Position.TOP_CENTER);
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
                    String finalMsg = errMsgs.isEmpty() ? "Silakan periksa kembali inputan form Anda." : String.join(" | ", errMsgs);
                    Notification n = Notification.show("⚠️ Gagal Menyimpan: " + finalMsg, 6000, Notification.Position.MIDDLE);
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
                    if (cleanMsg.contains("ERROR:")) cleanMsg = cleanMsg.substring(cleanMsg.indexOf("ERROR:") + 6);
                    if (cleanMsg.contains("Where: PL/pgSQL")) cleanMsg = cleanMsg.substring(0, cleanMsg.indexOf("Where: PL/pgSQL"));
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

        // 5. TUTUP BUTTON
        Button btnClose = new Button("Tutup");
        Icon iconClose = VaadinIcon.SIGN_OUT.create();
        iconClose.getStyle().set("color", "#22c55e").set("font-size", "1.2rem");
        btnClose.setIcon(iconClose);
        btnClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnClose.getStyle().set("font-weight", "500").set("color", "#374151");
        btnClose.addClickListener(e -> {
            if (closeHandler != null) {
                closeHandler.run();
            }
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
                if (tabSheet.getSelectedTab() == historisTab) {
                    refreshMasterGridData();
                    Notification.show("Data berhasil diperbarui!", 1500, Notification.Position.BOTTOM_END);
                } else {
                    if (detailsGrid.getEditor().isOpen()) {
                        detailsGrid.getEditor().cancel();
                    }
                    Map<String, Object> bean = formBinder.getBean();
                    String pk = formDef.getPrimaryKey() != null ? formDef.getPrimaryKey() : "id";
                    if (bean != null && bean.containsKey(pk) && bean.get(pk) != null && !bean.get(pk).toString().trim().isEmpty()) {
                        Object idVal = bean.get(pk);
                        Map<String, Object> freshRecord = dynamicDataService.fetchLovRecord(formDef.getTableName(), pk, idVal);
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
                            Notification.show("Gagal merefresh: Data tidak ditemukan di database.", 3000, Notification.Position.MIDDLE);
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

        com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority auth =
                securityService != null && currentFormCode != null
                        ? securityService.getAuthorityForMenu(currentFormCode)
                        : com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority.fullAccess();

        if (!auth.canAdd) {
            btnNew.setVisible(false);
            btnNew.setEnabled(false);
        }
        if (!auth.canEdit) {
            btnSave.setVisible(false);
            btnSave.setEnabled(false);
        }
        if (!auth.canDelete) {
            btnDelete.setVisible(false);
            btnDelete.setEnabled(false);
        }
        if (!auth.canPrint) {
            btnPrint.setVisible(false);
            btnPrint.setEnabled(false);
        }

        toolbar.add(btnNew, btnDelete, btnSave, btnCancel, btnRefresh, btnClose, btnPrint);
    }

    private void buildMasterForm(FormMeta formDef) {
        formLayout.removeAll();
        formComponents.clear();

        java.util.function.BiConsumer<String, Object> updateFieldValue = (targetFieldName, value) -> {
            if (isLoadingExistingData) return;
            Component targetComponent = formComponents.get(targetFieldName);
            if (targetComponent instanceof HasValue) {
                @SuppressWarnings("unchecked")
                HasValue<?, Object> hasValue = (HasValue<?, Object>) targetComponent;
                hasValue.setValue(value);
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
        rowGroupsOrder.sort(Integer::compareTo);

        for (Integer rg : rowGroupsOrder) {
            List<FieldMeta> groupFields = groups.get(rg);
            FormLayout rowLayout = new FormLayout();
            rowLayout.setWidthFull();
            int cols = groupFields.size();
            rowLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", Math.max(1, cols / 2)),
                new FormLayout.ResponsiveStep("800px", cols)
            );

            if (formDef.getLabelWidth() != null && !formDef.getLabelWidth().trim().isEmpty()) {
                rowLayout.getElement().getStyle().set("--vaadin-form-layout-label-width", formDef.getLabelWidth());
            }

            for (FieldMeta field : groupFields) {
                Component input = ComponentFactory.create(field, dynamicDataService, updateFieldValue);
                if (field.isHideInForm()) {
                    input.setVisible(false);
                }
                rowLayout.add(input);
                formComponents.put(field.getFieldName(), input);
                bindComponent(formBinder, input, field);

                if ("TEXTAREA".equalsIgnoreCase(field.getComponentType())) {
                    rowLayout.setColspan(input, cols);
                }
            }
            formLayout.add(rowLayout);
        }
    }

    private void buildMasterGrid(FormMeta formDef) {
        masterGrid.removeAllColumns();
        masterGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        columnToFieldNameMap.clear();
        filterValues.clear();

        // 1. Konfigurasi Toolbar Master Grid
        masterGridToolbar.removeAll();
        masterGridToolbar.setWidthFull();
        masterGridToolbar.setAlignItems(Alignment.CENTER);
        
        H4 sectionTitle = new H4("Riwayat Data");
        sectionTitle.getStyle().set("margin", "0");
        sectionTitle.getStyle().set("flex-grow", "1");

        Button btnResetMasterGrid = new Button("Reset Layout", VaadinIcon.ROTATE_LEFT.create());
        btnResetMasterGrid.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnResetMasterGrid.addClickListener(e -> {
            if (currentFormDef != null) {
                dynamicDataService.resetUserGridOrder(currentFormCode, "masterGrid");
                buildMasterGrid(currentFormDef);
                Notification.show("Layout grid master dikembalikan ke default!", 2000, Notification.Position.BOTTOM_END);
            }
        });

        masterGridToolbar.add(sectionTitle, btnResetMasterGrid);

        masterGrid.addItemDoubleClickListener(event -> {
            Map<String, Object> selectedMaster = event.getItem();
            if (selectedMaster != null) {
                String pk = formDef.getPrimaryKey() != null ? formDef.getPrimaryKey() : "id";
                Object masterId = getValueCaseInsensitive(selectedMaster, pk);
                Map<String, Object> freshMaster = selectedMaster;
                if (masterId != null && !masterId.toString().trim().isEmpty()) {
                    try {
                        Map<String, Object> dbRow = dynamicDataService.fetchLovRecord(formDef.getTableName(), pk, masterId);
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
                evaluateFormulas();
                
                // Fetch details
                detailsList.clear();
                deletedDetailsList.clear();
                if (masterId != null) {
                    detailsList.addAll(dynamicDataService.fetchDetailTableData(
                            formDef.getDetailTableName(), 
                            formDef.getDetailForeignKey(), 
                            masterId
                    ));
                }
                
                applyDetailsFilters();
                tabSheet.setSelectedTab(transaksiTab);
            }
        });

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

            Grid.Column<Map<String, Object>> col = masterGrid.addColumn(map -> {
                Object valObj = getValueCaseInsensitive(map, fieldName);
                return com.vaadinerp.components.ComponentFactory.formatFieldValueWithLov(field, valObj, dynamicDataService);
            })
                    .setHeader(field.getFieldLabel())
                    .setAutoWidth(true)
                    .setFlexGrow(1)
                    .setResizable(true)
                    .setKey(fieldName);

            col.setComparator((map1, map2) -> {
                Object val1 = map1.get(fieldName);
                Object val2 = map2.get(fieldName);
                if (val1 == null && val2 == null) return 0;
                if (val1 == null) return -1;
                if (val2 == null) return 1;
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

            com.vaadin.flow.component.contextmenu.ContextMenu contextMenu = new com.vaadin.flow.component.contextmenu.ContextMenu(filterButton);
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

            contextMenu.addItem(new com.vaadin.flow.component.html.Hr(), e -> {});
            contextMenu.addItem(col.isFrozen() ? "Unfreeze Column" : "Freeze Column", event -> {
                boolean nextFrozen = !col.isFrozen();
                col.setFrozen(nextFrozen);
                event.getSource().setText(nextFrozen ? "Unfreeze Column" : "Freeze Column");
                com.vaadin.flow.component.notification.Notification.show(
                    nextFrozen ? "Kolom dibekukan" : "Kolom dilepas", 2000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END
                );
            });

            filterField.addValueChangeListener(e -> {
                criteria.value = e.getValue();
                applyFilters();
            });

            filterRow.getCell(col).setComponent(filterField);
        });

        // 3. Row Drag and Drop
        masterGrid.setRowsDraggable(true);
        masterGrid.addDragStartListener(event -> {
            if (!event.getDraggedItems().isEmpty()) {
                draggedItem = event.getDraggedItems().get(0);
                masterGrid.setDropMode(com.vaadin.flow.component.grid.dnd.GridDropMode.BETWEEN);
            }
        });

        masterGrid.addDropListener(event -> {
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
                    applyFilters();
                }
            }
            draggedItem = null;
        });

        masterGrid.addDragEndListener(event -> {
            draggedItem = null;
            masterGrid.setDropMode(null);
        });

        // 4. Column Reordering
        masterGrid.setColumnReorderingAllowed(true);
        masterGrid.addColumnReorderListener(event -> {
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
                Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        java.util.List<String> masterUserOrder = dynamicDataService.getUserGridOrder(currentFormCode, "masterGrid");
        com.vaadinerp.components.StandardGridUtils.applySafeColumnOrder(masterGrid, columnToFieldNameMap, masterUserOrder);
    }

    private void buildDetailsGrid(FormMeta formDef) {
        detailsGrid.removeAllColumns();
        detailsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        detailsColumnToFieldNameMap.clear();
        detailsFilterValues.clear();
        detailEditorComponents.clear();
        
        List<FieldMeta> detailFields = formDef.getFields().stream()
                .filter(FieldMeta::isDetail)
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
            if (isLoadingExistingData) return;
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
                if (convertedVal != null && !convertedVal.equals(hv.getValue()) || (convertedVal == null && hv.getValue() != null)) {
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
            Grid.Column<Map<String, Object>> col = detailsGrid.addColumn(map -> {
                Object valObj = getValueCaseInsensitive(map, fieldName);
                return com.vaadinerp.components.ComponentFactory.formatFieldValueWithLov(field, valObj, dynamicDataService);
            })
                    .setHeader(field.getFieldLabel())
                    .setAutoWidth(true)
                    .setFlexGrow(1)
                    .setResizable(true)
                    .setKey(fieldName);

            // Setup Comparator for Sorting
            col.setComparator((map1, map2) -> {
                Object val1 = map1.get(fieldName);
                Object val2 = map2.get(fieldName);
                if (val1 == null && val2 == null) return 0;
                if (val1 == null) return -1;
                if (val2 == null) return 1;
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
            detailsColumnToFieldNameMap.put(col, fieldName);

            // Create inline editor component
            Component editorComp = ComponentFactory.create(field, dynamicDataService, detailUpdateFieldValue, true);
            col.setEditorComponent(editorComp);
            detailEditorComponents.put(fieldName, editorComp);
            
            @SuppressWarnings("unchecked")
            HasValue<?, Object> hasValue = (HasValue<?, Object>) editorComp;
            Binder.BindingBuilder<Map<String, Object>, Object> builder = gridBinder.forField(hasValue);
            if (field.isRequired()) {
                builder.asRequired(field.getFieldLabel() + " wajib diisi");
            }
            builder.bind(map -> convertToFieldValue(map.get(fieldName), editorComp),
                         (map, val) -> map.put(fieldName, val));
        }

        // Setup cascading filters (Master to Master, Master to Detail, Detail to Detail)
        for (FieldMeta field : formDef.getFields()) {
            if (field.getFilters() != null) {
                for (com.vaadinerp.meta.FieldFilterMeta filter : field.getFilters()) {
                    if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                        String sourceFieldName = filter.getSourceName();
                        Component sourceComponent = formComponents.get(sourceFieldName);
                        if (sourceComponent == null && field.isDetail()) {
                            sourceComponent = detailEditorComponents.get(sourceFieldName);
                        }
                        Component targetComponent = field.isDetail() ? detailEditorComponents.get(field.getFieldName()) : formComponents.get(field.getFieldName());
                        
                        if (sourceComponent instanceof HasValue && targetComponent != null) {
                            @SuppressWarnings("unchecked")
                            HasValue<?, Object> hasValueSource = (HasValue<?, Object>) sourceComponent;
                            hasValueSource.addValueChangeListener(event -> {
                                Object newValue = event.getValue();
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(String.valueOf(filter.getId()), filter.getFilterColumn(), newValue, filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            });
                            
                            // Apply initial/current filter value
                            Object initVal = hasValueSource.getValue();
                            if (initVal != null) {
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(String.valueOf(filter.getId()), filter.getFilterColumn(), initVal, filter.getLogicalOperator(), filter.getComparisonOperator());
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
                            double calculated = com.vaadinerp.util.FormulaEvaluator.evaluate(field.getFormula(), tempRow);
                            Component comp = detailEditorComponents.get(field.getFieldName());
                            if (comp instanceof HasValue) {
                                @SuppressWarnings("unchecked")
                                HasValue<?, Object> hasValue = (HasValue<?, Object>) comp;
                                Object converted = convertToFieldValue(calculated, comp);
                                if (converted != null && !converted.equals(hasValue.getValue())) {
                                    hasValue.setValue(converted);
                                }
                            }
                        } catch (Exception ignored) {}
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

            com.vaadin.flow.component.contextmenu.ContextMenu contextMenu = new com.vaadin.flow.component.contextmenu.ContextMenu(filterButton);
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

            contextMenu.addItem(new com.vaadin.flow.component.html.Hr(), e -> {});
            contextMenu.addItem(col.isFrozen() ? "Unfreeze Column" : "Freeze Column", event -> {
                boolean nextFrozen = !col.isFrozen();
                col.setFrozen(nextFrozen);
                event.getSource().setText(nextFrozen ? "Unfreeze Column" : "Freeze Column");
                com.vaadin.flow.component.notification.Notification.show(
                    nextFrozen ? "Kolom dibekukan" : "Kolom dilepas", 2000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END
                );
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
        detailsGrid.addDragStartListener(event -> {
            if (!event.getDraggedItems().isEmpty()) {
                draggedDetailItem = event.getDraggedItems().get(0);
                detailsGrid.setDropMode(com.vaadin.flow.component.grid.dnd.GridDropMode.BETWEEN);
            }
        });

        detailsGrid.addDropListener(event -> {
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

        detailsGrid.addDragEndListener(event -> {
            draggedDetailItem = null;
            detailsGrid.setDropMode(null);
        });

        // 4. Column Reordering for Details Grid
        detailsGrid.setColumnReorderingAllowed(true);
        detailsGrid.addColumnReorderListener(event -> {
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
                Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        java.util.List<String> detailsUserOrder = dynamicDataService.getUserGridOrder(currentFormCode, "detailsGrid");
        com.vaadinerp.components.StandardGridUtils.applySafeColumnOrder(detailsGrid, detailsColumnToFieldNameMap, detailsUserOrder);
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
                
                if ("Blank".equals(op)) {
                    if (!strVal.isEmpty()) return false;
                    continue;
                }
                if ("Not blank".equals(op)) {
                    if (strVal.isEmpty()) return false;
                    continue;
                }
                
                if (query == null || query.trim().isEmpty()) {
                    continue;
                }
                
                query = query.toLowerCase();
                
                switch (op) {
                    case "Contains":
                        if (!strVal.contains(query)) return false;
                        break;
                    case "Not contains":
                        if (strVal.contains(query)) return false;
                        break;
                    case "Equals":
                        if (!strVal.equals(query)) return false;
                        break;
                    case "Not equal":
                        if (strVal.equals(query)) return false;
                        break;
                    case "Starts with":
                        if (!strVal.startsWith(query)) return false;
                        break;
                    case "Ends with":
                        if (!strVal.endsWith(query)) return false;
                        break;
                }
            }
            return true;
        }).collect(Collectors.toList());
        
        filteredDetailsList.clear();
        filteredDetailsList.addAll(filtered);
        
        com.vaadin.flow.data.provider.DataProvider<Map<String, Object>, ?> dp = detailsGrid.getDataProvider();
        if (dp instanceof com.vaadin.flow.data.provider.ListDataProvider) {
            com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>> ldp = 
                (com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>>) dp;
            if (ldp.getItems() == filteredDetailsList) {
                ldp.refreshAll();
            } else {
                detailsGrid.setItems(filteredDetailsList);
            }
        } else {
            detailsGrid.setItems(filteredDetailsList);
        }
    }

    private void calculateRowTotal(Map<String, Object> row) {
        if (currentFormDef == null) return;
        try {
            Object qtyVal = row.get("qty");
            Object priceVal = row.get("price");
            if (qtyVal != null && priceVal != null) {
                double qty = Double.parseDouble(qtyVal.toString());
                double price = Double.parseDouble(priceVal.toString());
                row.put("total_price", qty * price);
                row.put("amount", qty * price);
            }
        } catch (Exception ignored) {}

        for (FieldMeta field : currentFormDef.getFields()) {
            if (field.isDetail() && field.getFormula() != null && !field.getFormula().trim().isEmpty()) {
                try {
                    double calculated = com.vaadinerp.util.FormulaEvaluator.evaluate(field.getFormula(), row);
                    row.put(field.getFieldName(), calculated);
                } catch (Exception ignored) {}
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
                    double calculated = com.vaadinerp.util.FormulaEvaluator.evaluate(field.getFormula(), bean);
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
            allMasterGridItems.clear();
            allMasterGridItems.addAll(dynamicDataService.fetchGridData(currentFormDef));
            applyFilters();
        }
    }

    private void applyFilters() {
        List<Map<String, Object>> filtered = allMasterGridItems.stream().filter(item -> {
            for (Map.Entry<String, FilterCriteria> entry : filterValues.entrySet()) {
                String fieldName = entry.getKey();
                FilterCriteria criteria = entry.getValue();
                
                String op = criteria.operator;
                String query = criteria.value;
                
                Object val = item.get(fieldName);
                String strVal = val != null ? val.toString() : "";
                String lovCode = fieldNameToLovCodeMap.get(fieldName);
                if (lovCode != null && !strVal.isEmpty()) {
                    strVal = getLovDisplayLabel(lovCode, strVal);
                }
                strVal = strVal.toLowerCase();
                
                if ("Blank".equals(op)) {
                    if (!strVal.isEmpty()) return false;
                    continue;
                }
                if ("Not blank".equals(op)) {
                    if (strVal.isEmpty()) return false;
                    continue;
                }
                
                if (query == null || query.trim().isEmpty()) {
                    continue;
                }
                
                query = query.toLowerCase();
                
                switch (op) {
                    case "Contains":
                        if (!strVal.contains(query)) return false;
                        break;
                    case "Not contains":
                        if (strVal.contains(query)) return false;
                        break;
                    case "Equals":
                        if (!strVal.equals(query)) return false;
                        break;
                    case "Not equal":
                        if (strVal.equals(query)) return false;
                        break;
                    case "Starts with":
                        if (!strVal.startsWith(query)) return false;
                        break;
                    case "Ends with":
                        if (!strVal.endsWith(query)) return false;
                        break;
                }
            }
            return true;
        }).collect(Collectors.toList());
        
        masterGridItems.clear();
        masterGridItems.addAll(filtered);
        masterGrid.setItems(new ArrayList<>(masterGridItems));
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
            return null;
        }
        if (component instanceof com.vaadin.flow.component.datepicker.DatePicker) {
            if (value instanceof java.time.LocalDate) return (V) value;
            if (value instanceof java.time.LocalDateTime) return (V) ((java.time.LocalDateTime) value).toLocalDate();
            if (value instanceof java.sql.Timestamp) return (V) ((java.sql.Timestamp) value).toLocalDateTime().toLocalDate();
            if (value instanceof java.sql.Date) return (V) ((java.sql.Date) value).toLocalDate();
            if (value instanceof java.util.Date) return (V) new java.sql.Date(((java.util.Date) value).getTime()).toLocalDate();
            try { return (V) java.time.LocalDate.parse(value.toString()); } catch (Exception e) { return null; }
        }
        if (component instanceof com.vaadin.flow.component.datetimepicker.DateTimePicker) {
            if (value instanceof java.time.LocalDateTime) return (V) value;
            if (value instanceof java.time.LocalDate) return (V) ((java.time.LocalDate) value).atStartOfDay();
            if (value instanceof java.sql.Timestamp) return (V) ((java.sql.Timestamp) value).toLocalDateTime();
            if (value instanceof java.util.Date) return (V) new java.sql.Timestamp(((java.util.Date) value).getTime()).toLocalDateTime();
            try { return (V) java.time.LocalDateTime.parse(value.toString().replace(" ", "T")); } catch (Exception e) {
                try { return (V) java.time.LocalDate.parse(value.toString()).atStartOfDay(); } catch (Exception ex) { return null; }
            }
        }
        if (component instanceof com.vaadin.flow.component.timepicker.TimePicker) {
            if (value instanceof java.time.LocalTime) return (V) value;
            if (value instanceof java.sql.Time) return (V) ((java.sql.Time) value).toLocalTime();
            try { return (V) java.time.LocalTime.parse(value.toString()); } catch (Exception e) { return null; }
        }
        if (component instanceof com.vaadin.flow.component.textfield.BigDecimalField || component instanceof com.vaadinerp.components.FormattedBigDecimalField) {
            if (value instanceof java.math.BigDecimal) return (V) value;
            try { return (V) new java.math.BigDecimal(value.toString()); } catch (Exception e) { return null; }
        }
        if (component instanceof com.vaadin.flow.component.textfield.IntegerField || component instanceof com.vaadinerp.components.FormattedIntegerField) {
            if (value instanceof Integer) return (V) value;
            if (value instanceof Number) return (V) Integer.valueOf(((Number) value).intValue());
            try { return (V) Integer.valueOf(Integer.parseInt(value.toString())); } catch (Exception e) { return null; }
        }
        if (component instanceof com.vaadin.flow.component.checkbox.Checkbox) {
            if (value instanceof Boolean) return (V) value;
            return (V) Boolean.valueOf(value.toString().equalsIgnoreCase("true") || value.toString().equals("1"));
        }
        if (component instanceof com.vaadin.flow.component.textfield.TextField ||
            component instanceof com.vaadin.flow.component.textfield.TextArea ||
            component instanceof com.vaadin.flow.component.combobox.ComboBox ||
            component instanceof com.vaadin.flow.component.select.Select) {
            return (V) value.toString();
        }
        return (V) value;
    }

    @SuppressWarnings("unchecked")
    private <V> void bindComponent(Binder<Map<String, Object>> binder, Component editComponent, FieldMeta field) {
        HasValue<?, V> hasValue = (HasValue<?, V>) editComponent;
        Binder.BindingBuilder<Map<String, Object>, V> builder = binder.forField(hasValue);
        if (field.isRequired()) {
            builder.asRequired(field.getFieldLabel() + " wajib diisi");
        }
        builder.bind(
                map -> convertToFieldValue(getValueCaseInsensitive(map, field.getFieldName()), editComponent),
                (map, value) -> putValueCaseInsensitive(map, field.getFieldName(), value));
    }

    private Object getValueCaseInsensitive(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        if (map.containsKey(key)) return map.get(key);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void putValueCaseInsensitive(Map<String, Object> map, String key, Object value) {
        if (map == null || key == null) return;
        for (String k : map.keySet()) {
            if (k != null && k.equalsIgnoreCase(key)) {
                map.put(k, value);
                return;
            }
        }
        map.put(key, value);
    }

    private void updateTitle(FormMeta formDef, boolean isUpdate) {
        if (formDef == null) return;
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
        btnConfirm.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY, com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);

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
                            Component sourceComponent = formComponents.get(sourceFieldName);
                            if (sourceComponent instanceof HasValue) {
                                Object val = ((HasValue<?, ?>) sourceComponent).getValue();
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(String.valueOf(filter.getId()), filter.getFilterColumn(), val, filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            }
                        }
                    }
                }
            }
        }
    }
}
