package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadinerp.components.ComponentFactory;
import com.vaadinerp.components.LovComboBox;
import com.vaadinerp.components.LovSelect;
import com.vaadinerp.components.BandboxField;
import com.vaadinerp.components.SubformGridField;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.FieldFilterMeta;

import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.util.FormulaEvaluator;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.icon.Icon;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Route("generic")
public class GenericFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final FormMetaRepository formMetaRepository;
    private final DynamicDataService dynamicDataService;
    private com.vaadinerp.security.service.SessionSecurityService securityService;
    private Binder<Map<String, Object>> formBinder;

    private VerticalLayout formLayout;
    private Grid<Map<String, Object>> grid;
    private HorizontalLayout toolbar;
    private final HorizontalLayout extraActionsContainer = new HorizontalLayout();
    private HorizontalLayout gridToolbar;
    private H3 title;

    private TabSheet tabSheet;
    private Tab historisTab;
    private Tab transaksiTab;
    private VerticalLayout historisLayout;
    private VerticalLayout transaksiLayout;

    private Runnable closeHandler;

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    /**
     * Menyembunyikan judul H3 di dalam view.
     * Dipanggil ketika view di-embed di dalam tab portal yang sudah menampilkan judul.
     */
    public void hideTitle() {
        title.setVisible(false);
    }

    private java.util.List<Map<String, Object>> gridItems = new java.util.ArrayList<>();
    private java.util.List<Map<String, Object>> allGridItems = new java.util.ArrayList<>();

    private static class FilterCriteria {
        String operator = "Contains";
        String value = "";
        public String getOperator() { return operator; }
        public String getValue() { return value; }
    }

    private java.util.Map<String, FilterCriteria> filterValues = new java.util.HashMap<>();
    private Map<String, Object> draggedItem;
    private com.vaadinerp.components.PaginationBar paginationBar;
    private String currentSortField;
    private String currentSortDir;

    // Peta untuk menyimpan referensi komponen form untuk update lintas-field
    private Map<String, Component> formComponents = new HashMap<>();

    // ====== TAMBAHAN UNTUK COLUMN REORDERING ======
    // Mapping dari Grid.Column -> nama field, dipakai saat event reorder terjadi
    // untuk menerjemahkan urutan Column kembali ke nama field metadata.
    private Map<Grid.Column<Map<String, Object>>, String> columnToFieldNameMap = new HashMap<>();
    private String currentFormCode;
    private FormMeta currentFormDef;

    private final Map<String, Map<String, String>> lovLabelMapCache = new HashMap<>();
    private final Map<String, String> fieldNameToLovCodeMap = new HashMap<>();

    // Flag to prevent cascading filter listeners from clearing child LOV values
    // during data loading
    private boolean isLoadingExistingData = false;

    // Registrations for listener deduplication
    private com.vaadin.flow.shared.Registration gridDoubleClickReg;
    private com.vaadin.flow.shared.Registration gridDragStartReg;
    private com.vaadin.flow.shared.Registration gridDropReg;
    private com.vaadin.flow.shared.Registration gridDragEndReg;
    private com.vaadin.flow.shared.Registration gridColReorderReg;

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
                    .map(String::trim)
                    .map(item -> map.getOrDefault(item, item))
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        return map.getOrDefault(strVal, strVal);
    }

    private boolean isEvaluatingFormulas = false;
    private final java.util.Map<Grid.Column<Map<String, Object>>, java.util.function.Function<Map<String, Object>, String>> colGetterMap = new java.util.concurrent.ConcurrentHashMap<>();

    public GenericFormView(FormMetaRepository formMetaRepository, DynamicDataService dynamicDataService) {
        this(formMetaRepository, dynamicDataService, null);
    }

    public GenericFormView(FormMetaRepository formMetaRepository, DynamicDataService dynamicDataService,
            com.vaadinerp.security.service.SessionSecurityService securityService) {
        this.formMetaRepository = formMetaRepository;
        this.dynamicDataService = dynamicDataService;
        this.securityService = securityService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "4px").set("padding", "8px 12px");

        title = new H3("Loading...");
        title.getStyle().set("margin", "0").set("padding", "0");
        toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setSpacing(false);

        formLayout = new VerticalLayout();
        formLayout.setWidthFull();
        formLayout.setPadding(false);
        formLayout.setSpacing(false);
        formLayout.getStyle().set("gap", "6px");

        gridToolbar = new HorizontalLayout();

        grid = new Grid<>();
        grid.setSizeFull();
        grid.setMinHeight("300px");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setPageSize(25);
        com.vaadinerp.components.StandardGridUtils.enableCellClipboardCopy(grid);

        // Setup TabSheet layouts
        historisLayout = new VerticalLayout();
        historisLayout.setSizeFull();
        historisLayout.setPadding(false);
        historisLayout.setSpacing(false);
        historisLayout.getStyle().set("gap", "4px");
        paginationBar = new com.vaadinerp.components.PaginationBar(e -> applyFilters());
        historisLayout.add(gridToolbar, grid, paginationBar);
        historisLayout.expand(grid);

        transaksiLayout = new VerticalLayout();
        transaksiLayout.setWidthFull();
        transaksiLayout.setPadding(false);
        transaksiLayout.setSpacing(false);
        transaksiLayout.getStyle().set("gap", "6px");
        transaksiLayout.add(formLayout);

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        historisTab = tabSheet.add("Historis", historisLayout);
        transaksiTab = tabSheet.add("Transaksi", transaksiLayout);

        tabSheet.addSelectedChangeListener(event -> {
            if (currentFormDef != null) {
                if (event.getSelectedTab() == historisTab) {
                    refreshGridData(currentFormDef);
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
    }

    @Override
    public void setParameter(BeforeEvent event, String formCode) {
        FormMeta formDef = formMetaRepository.findById(formCode).orElse(null);
        if (formDef == null) {
            title.setText("Form " + formCode + " not found!");
            return;
        }

        this.currentFormCode = formCode;
        this.currentFormDef = formDef;

        formBinder = new Binder<>();
        formBinder.setBean(new HashMap<>());
        formBinder.addValueChangeListener(e -> evaluateFormulas());

        title.setText(formDef.getFormTitle() != null ? formDef.getFormTitle() : "Form: " + formCode);

        buildToolbar(formDef);
        buildForm(formDef, event != null ? event.getLocation().getQueryParameters() : null);
        buildInlineEditingGrid(formDef);
        executeOnLoadActions("ON_LOAD_NEW");
    }

    private void buildToolbar(FormMeta formDef) {
        toolbar.removeAll();
        toolbar.getStyle()
                .set("background-color", "#f3f4f6")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "6px")
                .set("padding", "6px 12px")
                .set("align-items", "center")
                .set("gap", "15px");

        // 1. TAMBAH BUTTON
        Button btnNew = new Button("Tambah");
        Icon iconNew = VaadinIcon.PLUS_CIRCLE.create();
        iconNew.getStyle().set("color", "#22c55e").set("font-size", "1.2rem");
        btnNew.setIcon(iconNew);
        btnNew.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnNew.getStyle().set("font-weight", "500").set("color", "#374151");
        btnNew.addClickListener(e -> {
            formBinder.setBean(new HashMap<>());
            clearAllComponents();
            tabSheet.setSelectedTab(transaksiTab);
            executeOnLoadActions("ON_LOAD_NEW");
        });

        // 1.5. EDIT BUTTON
        Button btnEdit = new Button("Edit");
        Icon iconEdit = VaadinIcon.EDIT.create();
        iconEdit.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnEdit.setIcon(iconEdit);
        btnEdit.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnEdit.getStyle().set("font-weight", "500").set("color", "#374151");
        btnEdit.addClickListener(e -> {
            if (tabSheet.getSelectedTab() == historisTab) {
                java.util.Set<Map<String, Object>> selectedItems = grid.getSelectedItems();
                if (selectedItems == null || selectedItems.isEmpty()) {
                    Notification.show("Pilih data yang akan diedit terlebih dahulu di tab Historis!", 3000, Notification.Position.MIDDLE);
                    return;
                }
                loadAndEditData(selectedItems.iterator().next());
            } else {
                Notification.show("Silakan pilih data di tab Historis terlebih dahulu.", 3000, Notification.Position.MIDDLE);
                tabSheet.setSelectedTab(historisTab);
            }
        });

        // 2. HAPUS BUTTON
        Button btnDelete = new Button("Hapus");
        Icon iconDelete = VaadinIcon.CLOSE_CIRCLE.create();
        iconDelete.getStyle().set("color", "#ef4444").set("font-size", "1.2rem");
        btnDelete.setIcon(iconDelete);
        btnDelete.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnDelete.getStyle().set("font-weight", "500").set("color", "#374151");
        btnDelete.addClickListener(e -> {
            if (tabSheet.getSelectedTab() == historisTab) {
                java.util.Set<Map<String, Object>> selectedItems = grid.getSelectedItems();
                if (selectedItems != null && !selectedItems.isEmpty()) {
                    showConfirmDialog("Konfirmasi Hapus",
                            "Apakah Anda yakin ingin menghapus " + selectedItems.size() + " data yang dipilih ini?",
                            () -> {
                                toolbar.setEnabled(false);
                                try {
                                    for (Map<String, Object> selected : selectedItems) {
                                        dynamicDataService.deleteData(formDef, selected);
                                    }
                                    Notification.show("Data berhasil dihapus!", 3000, Notification.Position.TOP_CENTER);
                                    refreshGridData(formDef);
                                    grid.deselectAll();
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
                    showConfirmDialog("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus data transaksi ini?",
                            () -> {
                                toolbar.setEnabled(false);
                                try {
                                    dynamicDataService.deleteData(formDef, bean);
                                    Notification.show("Data berhasil dihapus!", 3000, Notification.Position.TOP_CENTER);
                                    formBinder.setBean(new HashMap<>());
                                    clearAllComponents();
                                    refreshGridData(formDef);
                                    tabSheet.setSelectedTab(historisTab);
                                } catch (Exception ex) {
                                    Notification.show("Gagal menghapus: " + ex.getMessage(), 5000,
                                            Notification.Position.MIDDLE);
                                } finally {
                                    toolbar.setEnabled(true);
                                }
                            });
                } else {
                    Notification.show("Tidak ada data tersimpan yang terpilih untuk dihapus.", 3000,
                            Notification.Position.MIDDLE);
                }
            }
        });

        // 3. SIMPAN BUTTON
        Button btnSave = new Button("Simpan");
        Icon iconSave = VaadinIcon.DOWNLOAD.create(); // matches floppy disk design
        iconSave.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnSave.setIcon(iconSave);
        btnSave.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("font-weight", "600").set("color", "#3b82f6");
        btnSave.setDisableOnClick(true);
        btnSave.addClickListener(e -> {
            toolbar.setEnabled(false);
            try {
                boolean binderOk = formBinder.validate().isOk();
                boolean requiredOk = true;

                // Programmatic double-check for all required fields
                boolean rulesOk = true;
                for (FieldMeta field : formDef.getFields()) {
                    Component comp = formComponents.get(field.getFieldName());
                    if (comp != null && !com.vaadinerp.components.ComponentFactory.validateFieldRule(field, comp)) {
                        rulesOk = false;
                    }
                    if (field.isRequired() && !"CHECKBOX".equalsIgnoreCase(field.getComponentType())) {
                        if (comp instanceof com.vaadin.flow.component.HasValue) {
                            Object val = ((com.vaadin.flow.component.HasValue<?, ?>) comp).getValue();
                            if (val == null || val.toString().trim().isEmpty()) {
                                requiredOk = false;
                                if (comp instanceof com.vaadin.flow.component.HasValidation) {
                                    ((com.vaadin.flow.component.HasValidation) comp).setInvalid(true);
                                    ((com.vaadin.flow.component.HasValidation) comp)
                                            .setErrorMessage(field.getFieldLabel() + " wajib diisi");
                                }
                            }
                        }
                    }
                }

                // Subform validation check
                boolean subformsOk = true;
                for (FieldMeta field : formDef.getFields()) {
                    if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) {
                        Component comp = formComponents.get(field.getFieldName());
                        if (comp instanceof SubformGridField) {
                            SubformGridField subformField = (SubformGridField) comp;
                            if (!subformField.validateRows()) {
                                subformsOk = false;
                                break;
                            }
                        }
                    }
                }

                if (binderOk && requiredOk && rulesOk && subformsOk) {
                    Map<String, Object> parentData = formBinder.getBean();
                    // Inject active and deleted records into the parent data map before saving
                    for (FieldMeta field : formDef.getFields()) {
                        if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) {
                            Component comp = formComponents.get(field.getFieldName());
                            if (comp instanceof SubformGridField) {
                                SubformGridField subformField = (SubformGridField) comp;
                                parentData.put(field.getFieldName(), subformField.getItems());
                                parentData.put(field.getFieldName() + "_deleted", subformField.getDeletedValues());
                            }
                        }
                    }

                    dynamicDataService.saveData(formDef, parentData);
                    Notification.show("Data berhasil disimpan!", 3000, Notification.Position.TOP_CENTER);

                    formBinder.setBean(new HashMap<>());
                    clearAllComponents();
                    refreshGridData(formDef);
                    tabSheet.setSelectedTab(historisTab);
                } else {
                    java.util.List<String> errMsgs = new java.util.ArrayList<>();
                    if (!binderOk) {
                        formBinder.validate().getValidationErrors().forEach(err -> errMsgs.add(err.getErrorMessage()));
                    }
                    if (!requiredOk) {
                        errMsgs.add("Kolom wajib diisi belum lengkap");
                    }
                    if (!rulesOk) {
                        errMsgs.add("Terdapat inputan yang tidak memenuhi aturan validasi");
                    }
                    if (!subformsOk) {
                        errMsgs.add("Data rincian wajib belum lengkap");
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
        btnCancel.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnCancel.getStyle().set("font-weight", "500").set("color", "#374151");
        btnCancel.addClickListener(e -> {
            formBinder.setBean(new HashMap<>());
            clearAllComponents();
            tabSheet.setSelectedTab(historisTab);
        });

        // 5. TUTUP BUTTON
        Button btnClose = new Button("Tutup");
        Icon iconClose = VaadinIcon.SIGN_OUT.create();
        iconClose.getStyle().set("color", "#22c55e").set("font-size", "1.2rem");
        btnClose.setIcon(iconClose);
        btnClose.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
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
        btnPrint.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnPrint.getStyle().set("font-weight", "500").set("color", "#374151");
        btnPrint.addClickListener(e -> {
            Notification.show("Fitur Cetak belum diimplementasikan.", 3000, Notification.Position.TOP_CENTER);
        });

        // 7. REFRESH BUTTON
        Button btnRefresh = new Button("Refresh");
        Icon iconRefresh = VaadinIcon.REFRESH.create();
        iconRefresh.getStyle().set("color", "#3b82f6").set("font-size", "1.2rem");
        btnRefresh.setIcon(iconRefresh);
        btnRefresh.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnRefresh.getStyle().set("font-weight", "500").set("color", "#374151");
        btnRefresh.setDisableOnClick(true);
        btnRefresh.addClickListener(e -> {
            try {
                refreshExtraToolbarButtons();
                if (formComponents != null) {
                    for (Component comp : formComponents.values()) {
                        if (comp instanceof com.vaadinerp.components.SubformGridField subGrid) {
                            subGrid.refreshExtraActions();
                        }
                    }
                }
                if (tabSheet.getSelectedTab() == historisTab) {
                    refreshGridData(formDef);
                    Notification.show("Data berhasil diperbarui!", 1500, Notification.Position.BOTTOM_END);
                } else {
                    Map<String, Object> bean = formBinder.getBean();
                    String pk = formDef.getPrimaryKey() != null ? formDef.getPrimaryKey() : "id";
                    if (bean != null && bean.containsKey(pk) && bean.get(pk) != null
                            && !bean.get(pk).toString().trim().isEmpty()) {
                        Object idVal = bean.get(pk);
                        String srcTable = (formDef.getViewTable() != null && !formDef.getViewTable().trim().isEmpty()) ? formDef.getViewTable().trim() : formDef.getTableName();
                        Map<String, Object> freshRecord = dynamicDataService.fetchLovRecord(srcTable, pk,
                                idVal);
                        if (freshRecord != null) {
                            formBinder.setBean(new HashMap<>(freshRecord));
                            loadSubformGridData(freshRecord);
                            evaluateFormulas();
                            Notification.show("Data berhasil direfresh!", 1500, Notification.Position.BOTTOM_END);
                        } else {
                            Notification.show("Gagal merefresh: Data tidak ditemukan di database.", 3000,
                                    Notification.Position.MIDDLE);
                        }
                    } else {
                        formBinder.setBean(new HashMap<>());
                        clearAllComponents();
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

        // 8. DEBUG CONTEXT BUTTON
        Button btnDebug = new Button("Debug Context");
        Icon iconDebug = VaadinIcon.BUG.create();
        iconDebug.getStyle().set("color", "#8b5cf6").set("font-size", "1.2rem");
        btnDebug.setIcon(iconDebug);
        btnDebug.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnDebug.getStyle().set("font-weight", "500").set("color", "#6b7280");
        btnDebug.setVisible(securityService != null && securityService.isSuperAdmin());
        btnDebug.addClickListener(e -> {
            Map<String, Object> bean = formBinder != null ? formBinder.getBean() : null;
            com.vaadinerp.components.FormDebugUtils.showDebugDialog(bean);
        });

        extraActionsContainer.setSpacing(true);
        toolbar.add(btnNew, btnEdit, btnDelete, btnSave, btnCancel, btnRefresh, btnClose, btnPrint, btnDebug, extraActionsContainer);
        refreshExtraToolbarButtons();
    }

    private void loadAndEditData(Map<String, Object> selectedRow) {
        if (selectedRow != null && currentFormDef != null) {
            String pk = currentFormDef.getPrimaryKey() != null ? currentFormDef.getPrimaryKey() : "id";
            Object pkVal = getValueCaseInsensitive(selectedRow, pk);
            Map<String, Object> freshRow = selectedRow;
            if (pkVal != null && !pkVal.toString().trim().isEmpty()) {
                try {
                    String srcTable = (currentFormDef.getViewTable() != null && !currentFormDef.getViewTable().trim().isEmpty()) ? currentFormDef.getViewTable().trim() : currentFormDef.getTableName();
                    Map<String, Object> dbRow = dynamicDataService.fetchLovRecord(srcTable, pk, pkVal);
                    if (dbRow != null && !dbRow.isEmpty()) {
                        freshRow = dbRow;
                    }
                } catch (Exception ex) {
                    Notification.show("Error memuat data: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            }
            Map<String, Object> formValues = new HashMap<>(freshRow);

            isLoadingExistingData = true;
            try {
                formBinder.setBean(formValues);
            } finally {
                isLoadingExistingData = false;
            }
            updateFieldsReadonlyStatus(false);
            loadSubformGridData(formValues);
            evaluateFormulas();
            tabSheet.setSelectedTab(transaksiTab);
            executeOnLoadActions("ON_LOAD_EDIT");
        }
    }

    private void refreshExtraToolbarButtons() {
        extraActionsContainer.removeAll();
        if (currentFormDef == null || dynamicDataService == null) return;
        List<com.vaadinerp.meta.FormActionMeta> masterActions = dynamicDataService.getFormActions(currentFormDef.getFormCode(),
                "MASTER_TOOLBAR");
        if (masterActions == null || masterActions.isEmpty()) return;

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
            com.vaadin.flow.component.icon.Icon icon = null;
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
            actBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
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
                com.vaadin.flow.component.icon.Icon icon = null;
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
                subMenu.addItem(new HorizontalLayout(icon, new com.vaadin.flow.component.html.Span(act.getActionLabel())), e -> executeToolbarAction(act));
            }
            extraActionsContainer.add(menuBar);
        }
    }

    private void executeToolbarAction(com.vaadinerp.meta.FormActionMeta act) {
        Map<String, Object> headerBean = formBinder != null ? formBinder.getBean() : new HashMap<>();
        List<Map<String, Object>> selectedRows = new ArrayList<>();
        if (grid != null && grid.getSelectedItems() != null) {
            selectedRows.addAll(grid.getSelectedItems());
        }
        if ("GROOVY_SCRIPT".equalsIgnoreCase(act.getActionType()) || (act.getScriptContent() != null && !act.getScriptContent().isBlank())) {
            if (dynamicDataService != null && dynamicDataService.getScriptExecutorService() != null) {
                dynamicDataService.getScriptExecutorService().executeActionScript(act, headerBean, selectedRows, this);
            }
        } else {
            com.vaadinerp.components.DynamicPickerPopupDialog dlg = new com.vaadinerp.components.DynamicPickerPopupDialog(
                    act, dynamicDataService, headerBean, selectedRecords -> {
                        for (Map<String, Object> srcRec : selectedRecords) {
                            applyTargetMapping(headerBean, srcRec, act.getTargetMapping());
                        }
                        if (formBinder != null)
                            formBinder.readBean(headerBean);
                    });
            dlg.open();
        }
    }

    private void executeOnLoadActions(String scope) {
        if (currentFormDef == null || dynamicDataService == null) return;
        List<com.vaadinerp.meta.FormActionMeta> actions = dynamicDataService.getFormActions(currentFormCode, scope);
        if (actions == null || actions.isEmpty()) return;

        Map<String, Object> headerBean = formBinder != null ? formBinder.getBean() : new HashMap<>();
        for (com.vaadinerp.meta.FormActionMeta act : actions) {
            try {
                List<Map<String, Object>> fetchedRecords = dynamicDataService.fetchLovDataWithActionFilters(
                        act.getSourceLovCode(),
                        act.getFilterMapping(),
                        headerBean,
                        ""
                );
                if (fetchedRecords != null && !fetchedRecords.isEmpty()) {
                    Map<String, Object> srcRec = fetchedRecords.get(0);
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
            } catch (Exception ex) {
                System.err.println("Error executing OnLoad action [" + act.getActionCode() + "] in scope [" + scope + "]: " + ex.getMessage());
            }
        }
    }

    private void applyTargetMapping(Map<String, Object> destRow, Map<String, Object> srcRecord, String targetMapping) {
        if (targetMapping == null || targetMapping.trim().isEmpty())
            return;
        String clean = targetMapping.trim();
        if (clean.startsWith("{") && clean.endsWith("}")) {
            clean = clean.substring(1, clean.length() - 1).trim();
        }
        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length < 2)
                kv = pair.split("=");
            if (kv.length == 2) {
                String destCol = kv[0].replaceAll("[\"']", "").trim();
                String srcCol = kv[1].replaceAll("[\"']", "").trim();
                if (srcCol.toLowerCase().startsWith("source.")) {
                    srcCol = srcCol.substring(7);
                }
                Object val = null;
                if (srcRecord != null) {
                    val = getValueCaseInsensitive(srcRecord, srcCol);
                }
                putValueCaseInsensitive(destRow, destCol, val);
            }
        }
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

    private void buildForm(FormMeta formDef, com.vaadin.flow.router.QueryParameters queryParameters) {
        formLayout.removeAll();
        formComponents.clear();

        java.util.function.BiConsumer<String, Object> updateFieldValue = (targetFieldName, value) -> {
            if (isLoadingExistingData)
                return;
            Component targetComponent = formComponents.get(targetFieldName);
            if (targetComponent instanceof com.vaadin.flow.component.HasValue) {
                @SuppressWarnings("unchecked")
                com.vaadin.flow.component.HasValue<?, Object> hasValue = (com.vaadin.flow.component.HasValue<?, Object>) targetComponent;

                if (value == null) {
                    if (targetComponent instanceof com.vaadin.flow.component.textfield.TextField ||
                            targetComponent instanceof com.vaadin.flow.component.textfield.TextArea) {
                        hasValue.setValue("");
                    } else {
                        // For other components, we try to set null, but catch if it fails
                        try {
                            hasValue.setValue(null);
                        } catch (Exception ignored) {
                        }
                    }
                } else {
                    Object convertedValue = convertToFieldValue(value, targetComponent);
                    try {
                        hasValue.setValue(convertedValue);
                    } catch (Exception ignored) {
                    }
                }
            }
        };

        // 1. Build and bind all form fields first, grouped by rowGroup
        Map<Integer, List<FieldMeta>> groups = new HashMap<>();
        List<Integer> rowGroupsOrder = new ArrayList<>();
        for (FieldMeta field : formDef.getFields()) {
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
            boolean allDefault = groupFields.stream().allMatch(f -> f.getColSpan() == null || f.getColSpan() <= 1);
            int cols = (allDefault && groupFields.size() > 0 && groupFields.size() <= 6) ? groupFields.size() : 12;
            rowLayout.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("500px", Math.max(1, cols / 2)),
                    new FormLayout.ResponsiveStep("800px", cols));

            if (formDef.getLabelWidth() != null && !formDef.getLabelWidth().trim().isEmpty()) {
                rowLayout.getElement().getStyle().set("--vaadin-form-layout-label-width", formDef.getLabelWidth());
            }

            for (FieldMeta field : groupFields) {
                Component input = ComponentFactory.create(field, dynamicDataService, updateFieldValue);
                if (field.isHideInForm()) {
                    input.setVisible(false);
                }

                int span = (field.getColSpan() != null && field.getColSpan() > 0) ? field.getColSpan() : 1;
                if ("TEXTAREA".equalsIgnoreCase(field.getComponentType()) && (field.getColSpan() == null || field.getColSpan() <= 1)) {
                    span = cols;
                }

                // Checkbox: bungkus dengan Div agar label muncul di ATAS (sama persis seperti TextField/ComboBox)
                // sehingga checkbox square sejajar satu baris dengan input box di sampingnya
                if (input instanceof com.vaadin.flow.component.checkbox.Checkbox cb) {
                    String cbLabel = cb.getLabel();
                    cb.setLabel(null); // hapus label dari checkbox agar tidak duplikat

                    com.vaadin.flow.component.html.Span labelSpan = new com.vaadin.flow.component.html.Span(cbLabel != null ? cbLabel : "");
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

                    com.vaadin.flow.component.html.Div cbWrapper = new com.vaadin.flow.component.html.Div(labelSpan, cbBox);
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

                if ("TEXTAREA".equalsIgnoreCase(field.getComponentType()) && input instanceof com.vaadin.flow.component.textfield.TextArea ta) {
                    ta.getStyle().set("resize", "vertical");
                }
            }
            formLayout.add(rowLayout);
        }

        // 2. Setup cascading listeners and initial/static/query filters
        for (FieldMeta field : formDef.getFields()) {
            if (field.getFilters() != null) {
                for (FieldFilterMeta filter : field.getFilters()) {
                    Component targetComponent = formComponents.get(field.getFieldName());
                    if (targetComponent == null) continue;

                    if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                        String sourceFieldName = filter.getSourceName();
                        Component sourceComponent = formComponents.get(sourceFieldName);
                        if (sourceComponent instanceof com.vaadin.flow.component.HasValue) {
                            @SuppressWarnings("unchecked")
                            com.vaadin.flow.component.HasValue<?, Object> hasValueSource = (com.vaadin.flow.component.HasValue<?, Object>) sourceComponent;
                            hasValueSource.addValueChangeListener(event -> {
                                Object newValue = event.getValue();
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                        String.valueOf(filter.getId()), filter.getFilterColumn(), newValue,
                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            });
                            // Apply initial value immediately
                            Object initVal = hasValueSource.getValue();
                            if (initVal != null) {
                                com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                        String.valueOf(filter.getId()), filter.getFilterColumn(), initVal,
                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                applyFilterToComponent(targetComponent, condition);
                            }
                        } else {
                            // Fallback: Jika field tidak ditemukan di form, perlakukan sebagai STATIC value (atasi human error saat salah pilih FIELD di Form Builder)
                            com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                    String.valueOf(filter.getId()), filter.getFilterColumn(), filter.getSourceName(),
                                    filter.getLogicalOperator(), filter.getComparisonOperator());
                            applyFilterToComponent(targetComponent, condition);
                        }
                    } else if ("STATIC".equalsIgnoreCase(filter.getSourceType())) {
                        com.vaadinerp.components.FilterCondition condition = new com.vaadinerp.components.FilterCondition(
                                String.valueOf(filter.getId()), filter.getFilterColumn(), filter.getSourceName(),
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

        // Setup cascading from parent fields to SubformGrid fields
        for (FieldMeta field : formDef.getFields()) {
            Component sourceComponent = formComponents.get(field.getFieldName());
            if (sourceComponent instanceof com.vaadin.flow.component.HasValue) {
                @SuppressWarnings("unchecked")
                com.vaadin.flow.component.HasValue<?, Object> hasValueSource = (com.vaadin.flow.component.HasValue<?, Object>) sourceComponent;
                hasValueSource.addValueChangeListener(event -> {
                    Object newValue = event.getValue();
                    for (Component comp : formComponents.values()) {
                        if (comp instanceof SubformGridField) {
                            ((SubformGridField) comp).setParentFieldValue(field.getFieldName(), newValue);
                        }
                    }
                });
            }
        }

        // 4. Preset field values from URL query parameters (memicu cascading secara
        // otomatis)
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

    private void buildInlineEditingGrid(FormMeta formDef) {
        if (gridDoubleClickReg != null)
            gridDoubleClickReg.remove();
        if (gridDragStartReg != null)
            gridDragStartReg.remove();
        if (gridDropReg != null)
            gridDropReg.remove();
        if (gridDragEndReg != null)
            gridDragEndReg.remove();
        if (gridColReorderReg != null)
            gridColReorderReg.remove();

        com.vaadinerp.components.StandardGridUtils.cleanGridBeforeRebuild(grid);
        grid.addSelectionListener(e -> {
            System.out.println(">>> SELECTION EVENT DITERIMA SERVER: " + e.getAllSelectedItems().size());
        });
        // grid.setSelectionMode(Grid.SelectionMode.MULTI);

        columnToFieldNameMap.clear(); // reset mapping setiap kali grid dibangun ulang
        colGetterMap.clear();

        // 1. Konfigurasi Toolbar Grid
        gridToolbar.removeAll();
        gridToolbar.setWidthFull();
        gridToolbar.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        com.vaadin.flow.component.html.H4 sectionTitle = new com.vaadin.flow.component.html.H4("Riwayat Data");
        sectionTitle.getStyle().set("margin", "0");
        sectionTitle.getStyle().set("flex-grow", "1");

        Button btnResetGridToolbar = new Button("Reset Layout Grid", VaadinIcon.ROTATE_LEFT.create());
        btnResetGridToolbar.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);
        btnResetGridToolbar.addClickListener(e -> {
            dynamicDataService.resetUserGridOrder(currentFormCode, "mainGrid");
            buildInlineEditingGrid(formDef);
            Notification.show("Layout grid dikembalikan ke default!", 2000, Notification.Position.BOTTOM_END);
        });

        com.vaadin.flow.component.html.Anchor btnExportExcel = com.vaadinerp.components.StandardGridUtils
                .createExportExcelButton(grid, currentFormCode != null ? currentFormCode + "_export" : "data_export", colGetterMap);

        com.vaadin.flow.component.checkbox.Checkbox cbPilihSemuaHalIni = new com.vaadin.flow.component.checkbox.Checkbox();
        com.vaadin.flow.component.html.Span lblPilihSemuaHalIni = new com.vaadin.flow.component.html.Span("Pilih Semua (Hal Ini)");
        lblPilihSemuaHalIni.getStyle().set("font-size", "var(--lumo-font-size-s)").set("font-weight", "500").set("cursor", "pointer").set("user-select", "none").set("white-space", "nowrap");
        lblPilihSemuaHalIni.addClickListener(e -> cbPilihSemuaHalIni.setValue(!Boolean.TRUE.equals(cbPilihSemuaHalIni.getValue())));

        HorizontalLayout boxHalIni = new HorizontalLayout(cbPilihSemuaHalIni, lblPilihSemuaHalIni);
        boxHalIni.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        boxHalIni.setSpacing(false);
        boxHalIni.getStyle().set("gap", "4px").set("margin-left", "10px");

        com.vaadin.flow.component.checkbox.Checkbox cbPilihSemua = new com.vaadin.flow.component.checkbox.Checkbox();
        com.vaadin.flow.component.html.Span lblPilihSemua = new com.vaadin.flow.component.html.Span("Pilih Semua");
        lblPilihSemua.getStyle().set("font-size", "var(--lumo-font-size-s)").set("font-weight", "500").set("cursor", "pointer").set("user-select", "none").set("white-space", "nowrap");
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
                    grid.asMultiSelect().select(pageItems);
                    Notification.show("Memilih " + pageItems.size() + " baris di halaman ini.", 1500, Notification.Position.BOTTOM_END);
                } else {
                    if (!cbPilihSemua.getValue()) {
                        grid.deselectAll();
                    } else {
                        cbPilihSemua.setValue(false);
                        grid.deselectAll();
                    }
                }
            }
        });

        cbPilihSemua.addValueChangeListener(e -> {
            if (e.isFromClient() && currentFormDef != null) {
                if (Boolean.TRUE.equals(e.getValue())) {
                    java.util.List<Map<String, Object>> allItems = dynamicDataService.fetchGridDataPaged(
                            currentFormDef, 0, Integer.MAX_VALUE, filterValues, currentSortField, currentSortDir);
                    grid.asMultiSelect().select(allItems);
                    cbPilihSemuaHalIni.setValue(true);
                    Notification.show("Memilih seluruh " + allItems.size() + " baris data.", 2000, Notification.Position.BOTTOM_END);
                } else {
                    cbPilihSemuaHalIni.setValue(false);
                    grid.deselectAll();
                }
            }
        });

        grid.addSelectionListener(e -> {
            if (e.getAllSelectedItems().isEmpty()) {
                cbPilihSemuaHalIni.setValue(false);
                cbPilihSemua.setValue(false);
            }
        });

        gridToolbar.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        gridToolbar.getStyle().set("flex-wrap", "nowrap").set("overflow-x", "auto");
        gridToolbar.add(sectionTitle, boxHalIni, boxAll, btnExportExcel, btnResetGridToolbar);

        // Double Click Listener to load data into form and switch tab
        com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority auth = securityService != null
                && currentFormCode != null
                        ? securityService.getAuthorityForMenu(currentFormCode)
                        : com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority.fullAccess();
        if (auth.canEdit) {
            gridDoubleClickReg = grid.addItemDoubleClickListener(event -> {
                loadAndEditData(event.getItem());
            });
        }

        // LinkedHashMap to hold columns for Header Filter Row
        java.util.Map<String, Grid.Column<Map<String, Object>>> columnsMap = new java.util.LinkedHashMap<>();

        // Urutkan field berdasarkan colOrder (kalau ada), supaya urutan kolom
        // saat render awal mengikuti urutan yang sudah pernah disimpan dari reorder
        // sebelumnya.
        java.util.List<FieldMeta> sortedFields = new java.util.ArrayList<>(formDef.getFields());
        sortedFields.sort((f1, f2) -> {
            Integer o1 = f1.getColOrder() != null ? f1.getColOrder() : Integer.MAX_VALUE;
            Integer o2 = f2.getColOrder() != null ? f2.getColOrder() : Integer.MAX_VALUE;
            return o1.compareTo(o2);
        });

        for (FieldMeta field : sortedFields) {
            if (field.isShowInGrid()) {
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
                Grid.Column<Map<String, Object>> col = grid.addColumn(valueGetter::apply)
                        .setHeader(field.getFieldLabel())
                        .setAutoWidth(true)
                        .setFlexGrow(1)
                        .setResizable(true)
                        .setKey(fieldName); // setKey supaya bisa diakses ulang kalau perlu
                colGetterMap.put(col, valueGetter);

                // Setup Comparator for Sorting
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
                col.setSortable(field.isSortable());

                columnsMap.put(fieldName, col);
                columnToFieldNameMap.put(col, fieldName); // <-- daftarkan mapping untuk reorder
            }
        }

        // 2. Setup Header Filter Row (Pencarian per kolom)
        com.vaadin.flow.component.grid.HeaderRow filterRow = grid.appendHeaderRow();
        columnsMap.forEach((fieldName, col) -> {
            FilterCriteria criteria = new FilterCriteria();
            filterValues.put(fieldName, criteria);

            TextField filterField = new TextField();
            filterField.setPlaceholder("Filter...");
            filterField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
            filterField.setWidthFull();
            filterField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL);

            // Buat icon filter sebagai prefix di TextField
            Button filterButton = new Button(com.vaadin.flow.component.icon.VaadinIcon.FILTER.create());
            filterButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE);
            filterButton.getStyle().set("cursor", "pointer");
            filterButton.getElement().setProperty("title", "Contains"); // tooltip default
            filterField.setPrefixComponent(filterButton);

            // Menu popup ketika icon diklik
            com.vaadin.flow.component.contextmenu.ContextMenu contextMenu = new com.vaadin.flow.component.contextmenu.ContextMenu(
                    filterButton);
            contextMenu.setOpenOnClick(true);

            Runnable applyOperatorUI = () -> {
                String op = criteria.operator;
                filterButton.getElement().setProperty("title", op); // Update tooltip
                boolean needsInput = !("Blank".equals(op) || "Not blank".equals(op));
                if (!needsInput) {
                    filterField.setValue("");
                    filterField.setPlaceholder(op); // Tampilkan info Blank/Not blank
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
                    if (paginationBar != null) paginationBar.resetPage();
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
                if (paginationBar != null) paginationBar.resetPage();
                applyFilters();
            });

            filterRow.getCell(col).setComponent(filterField);
        });

        // 3. Konfigurasi Drag and Drop untuk Row Reordering
        grid.setRowsDraggable(true);

        gridDragStartReg = grid.addDragStartListener(event -> {
            if (!event.getDraggedItems().isEmpty()) {
                draggedItem = event.getDraggedItems().get(0);
                grid.setDropMode(GridDropMode.BETWEEN);
            }
        });

        gridDropReg = grid.addDropListener(event -> {
            Map<String, Object> targetItem = event.getDropTargetItem().orElse(null);
            if (targetItem != null && draggedItem != null && targetItem != draggedItem) {
                int indexDragged = findIndexByReference(allGridItems, draggedItem);
                int indexTarget = findIndexByReference(allGridItems, targetItem);

                if (indexDragged >= 0 && indexTarget >= 0) {
                    allGridItems.remove(indexDragged);
                    int newIndex = findIndexByReference(allGridItems, targetItem);
                    if (event.getDropLocation() == GridDropLocation.BELOW) {
                        allGridItems.add(newIndex + 1, draggedItem);
                    } else {
                        allGridItems.add(newIndex, draggedItem);
                    }
                    updateGridDataProvider();
                }
            }
            draggedItem = null;
        });

        gridDragEndReg = grid.addDragEndListener(event -> {
            draggedItem = null;
            grid.setDropMode(null);
        });

        grid.addSortListener(event -> {
            if (!event.getSortOrder().isEmpty()) {
                com.vaadin.flow.component.grid.GridSortOrder<Map<String, Object>> order = event.getSortOrder().get(0);
                if (order.getSorted().getKey() != null) {
                    currentSortField = order.getSorted().getKey();
                    currentSortDir = order.getDirection() == com.vaadin.flow.data.provider.SortDirection.DESCENDING ? "DESC" : "ASC";
                    if (paginationBar != null) paginationBar.resetPage();
                    applyFilters();
                }
            } else {
                currentSortField = null;
                currentSortDir = null;
                if (paginationBar != null) paginationBar.resetPage();
                applyFilters();
            }
        });

        // ====== 4. AKTIFKAN COLUMN REORDERING (Drag-and-drop GESER KOLOM) ======
        grid.setColumnReorderingAllowed(true);

        gridColReorderReg = grid.addColumnReorderListener(event -> {
            java.util.List<Grid.Column<Map<String, Object>>> newOrder = event.getColumns();

            // Terjemahkan urutan Column baru menjadi daftar nama field, sesuai urutan
            // hasil drag user.
            java.util.List<String> orderedFieldNames = new java.util.ArrayList<>();
            for (Grid.Column<Map<String, Object>> col : newOrder) {
                String fieldName = columnToFieldNameMap.get(col);
                if (fieldName != null) {
                    orderedFieldNames.add(fieldName);
                }
            }

            try {
                dynamicDataService.saveUserGridOrder(currentFormCode, "mainGrid", orderedFieldNames);
                Notification.show("Urutan kolom disimpan", 1500, Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(),
                        3000, Notification.Position.MIDDLE);
            }
        });
        // =========================================================================

        // Set Grid Items dari database
        refreshGridData(formDef);

        // Terapkan preferensi urutan kolom per user jika ada
        java.util.List<String> userOrder = dynamicDataService.getUserGridOrder(currentFormCode, "mainGrid");
        com.vaadinerp.components.StandardGridUtils.applySafeColumnOrder(grid, columnToFieldNameMap, userOrder);

        com.vaadinerp.components.StandardGridUtils.attachSelectAllHeader(grid, () -> {
            if (currentFormDef == null) return java.util.Collections.emptyList();
            int offset = paginationBar != null ? paginationBar.getOffset() : 0;
            int limit = paginationBar != null ? paginationBar.getLimit() : 1000;
            return dynamicDataService.fetchGridDataPaged(currentFormDef, offset, limit, filterValues, currentSortField, currentSortDir);
        });
    }

    private void refreshGridData(FormMeta formDef) {
        lovLabelMapCache.clear();
        com.vaadinerp.components.ComponentFactory.clearLovCache(null);
        if (currentSortField == null && formDef != null) {
            currentSortField = formDef.getDefaultSortField();
            currentSortDir = formDef.getDefaultSortDirection();
        }
        applyFilters();
    }

    private void applyFilters() {
        if (currentFormDef == null) return;
        long totalRecords = dynamicDataService.countGridData(currentFormDef, filterValues);
        if (paginationBar != null) {
            paginationBar.setTotalRecords(totalRecords);
        }
        com.vaadin.flow.data.provider.DataProvider<Map<String, Object>, ?> dp = grid.getDataProvider();
        if (dp instanceof com.vaadin.flow.data.provider.CallbackDataProvider) {
            dp.refreshAll();
        } else {
            updateGridDataProvider();
        }
    }

    private void updateGridDataProvider() {
        if (currentFormDef == null) return;
        com.vaadin.flow.data.provider.CallbackDataProvider<Map<String, Object>, Void> dataProvider =
                new com.vaadin.flow.data.provider.CallbackDataProvider<>(
                        query -> {
                            int queryOffset = query.getOffset();
                            int queryLimit = query.getLimit();
                            int effectiveOffset = (paginationBar != null ? paginationBar.getOffset() : 0) + queryOffset;

                            String sortField = currentSortField;
                            String sortDir = currentSortDir;
                            if (!query.getSortOrders().isEmpty()) {
                                com.vaadin.flow.data.provider.QuerySortOrder order = query.getSortOrders().get(0);
                                sortField = order.getSorted();
                                sortDir = order.getDirection() == com.vaadin.flow.data.provider.SortDirection.DESCENDING ? "DESC" : "ASC";
                            }

                            java.util.List<Map<String, Object>> chunk = dynamicDataService.fetchGridDataPaged(
                                    currentFormDef, effectiveOffset, queryLimit, filterValues, sortField, sortDir);

                            allGridItems.clear();
                            allGridItems.addAll(chunk);
                            gridItems.clear();
                            gridItems.addAll(chunk);

                            return chunk.stream();
                        },
                        query -> {
                            long total = dynamicDataService.countGridData(currentFormDef, filterValues);
                            if (paginationBar != null) {
                                paginationBar.setTotalRecords(total);
                                long pageLimit = paginationBar.getLimit();
                                long remaining = total - paginationBar.getOffset();
                                if (remaining < 0) remaining = 0;
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
                        }
                );
        grid.setDataProvider(dataProvider);
        com.vaadinerp.components.StandardGridUtils.attachSelectAllHeader(grid, () -> {
            if (currentFormDef == null) return java.util.Collections.emptyList();
            int offset = paginationBar != null ? paginationBar.getOffset() : 0;
            int limit = paginationBar != null ? paginationBar.getLimit() : 1000;
            return dynamicDataService.fetchGridDataPaged(currentFormDef, offset, limit, filterValues, currentSortField, currentSortDir);
        });
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
                if (field.getFormula() != null && !field.getFormula().trim().isEmpty()) {
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

    @SuppressWarnings("unchecked")
    private <V> V convertToFieldValue(Object value, Component component) {
        if (component instanceof SubformGridField) {
            if (value instanceof List) {
                return (V) value;
            }
            return (V) new ArrayList<Map<String, Object>>();
        }
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
            if (value instanceof java.time.LocalDate) {
                return (V) value;
            }
            if (value instanceof java.time.LocalDateTime) {
                return (V) ((java.time.LocalDateTime) value).toLocalDate();
            }
            if (value instanceof java.sql.Timestamp) {
                return (V) ((java.sql.Timestamp) value).toLocalDateTime().toLocalDate();
            }
            if (value instanceof java.sql.Date) {
                return (V) ((java.sql.Date) value).toLocalDate();
            }
            if (value instanceof java.util.Date) {
                return (V) new java.sql.Date(((java.util.Date) value).getTime()).toLocalDate();
            }
            try {
                return (V) java.time.LocalDate.parse(value.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.datetimepicker.DateTimePicker) {
            if (value instanceof java.time.LocalDateTime) {
                return (V) value;
            }
            if (value instanceof java.time.LocalDate) {
                return (V) ((java.time.LocalDate) value).atStartOfDay();
            }
            if (value instanceof java.sql.Timestamp) {
                return (V) ((java.sql.Timestamp) value).toLocalDateTime();
            }
            if (value instanceof java.util.Date) {
                return (V) new java.sql.Timestamp(((java.util.Date) value).getTime()).toLocalDateTime();
            }
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
            if (value instanceof java.time.LocalTime) {
                return (V) value;
            }
            if (value instanceof java.sql.Time) {
                return (V) ((java.sql.Time) value).toLocalTime();
            }
            try {
                return (V) java.time.LocalTime.parse(value.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.textfield.BigDecimalField
                || component instanceof com.vaadinerp.components.FormattedBigDecimalField) {
            if (value instanceof java.math.BigDecimal) {
                return (V) value;
            }
            try {
                return (V) new java.math.BigDecimal(value.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.textfield.IntegerField
                || component instanceof com.vaadinerp.components.FormattedIntegerField) {
            if (value instanceof Integer) {
                return (V) value;
            }
            if (value instanceof Number) {
                return (V) Integer.valueOf(((Number) value).intValue());
            }
            try {
                return (V) Integer.valueOf(Integer.parseInt(value.toString()));
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.checkbox.Checkbox) {
            if (value instanceof Boolean) {
                return (V) value;
            }
            return (V) Boolean.valueOf(value.toString().equalsIgnoreCase("true") || value.toString().equals("1"));
        }
        if (component instanceof com.vaadin.flow.component.combobox.MultiSelectComboBox) {
            if (value instanceof java.util.Set) {
                return (V) value;
            }
            if (value instanceof java.util.Collection) {
                return (V) new java.util.HashSet<>((java.util.Collection<?>) value);
            }
            java.util.Set<String> set = new java.util.HashSet<>();
            String[] parts = value.toString().split(",");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    set.add(part.trim());
                }
            }
            return (V) set;
        }
        if (component instanceof com.vaadin.flow.component.textfield.TextField ||
                component instanceof com.vaadin.flow.component.textfield.TextArea ||
                component instanceof com.vaadin.flow.component.combobox.ComboBox ||
                component instanceof com.vaadin.flow.component.select.Select ||
                component instanceof com.vaadin.flow.component.listbox.ListBox ||
                component instanceof com.vaadin.flow.component.radiobutton.RadioButtonGroup) {
            return (V) value.toString();
        }
        try {
            return (V) value;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <V> void bindComponent(Binder<Map<String, Object>> binder, Component editComponent, FieldMeta field) {
        if (editComponent instanceof com.vaadinerp.components.SubformGridField subformGrid) {
            subformGrid.setHeaderRecordSupplier(() -> binder.getBean());
        }
        com.vaadin.flow.component.HasValue<?, V> hasValue = (com.vaadin.flow.component.HasValue<?, V>) editComponent;
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
                        if (selItem instanceof Map) selMap = (Map<String, Object>) selItem;
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
                                putValueCaseInsensitive(map, field.getFieldName() + "." + entry.getKey(), entry.getValue());
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
                    if (selItem instanceof Map) selMap = (Map<String, Object>) selItem;
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
                            putValueCaseInsensitive(bean, field.getFieldName() + "." + entry.getKey(), entry.getValue());
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

    private int findIndexByReference(java.util.List<Map<String, Object>> list, Map<String, Object> item) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == item) {
                return i;
            }
        }
        return -1;
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

    private void loadSubformGridData(Map<String, Object> parentValues) {
        if (currentFormDef == null)
            return;
        String pk = currentFormDef.getPrimaryKey() != null ? currentFormDef.getPrimaryKey() : "id";
        Object parentPkValue = parentValues.get(pk);

        for (FieldMeta field : currentFormDef.getFields()) {
            if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) {
                Component comp = formComponents.get(field.getFieldName());
                if (comp instanceof SubformGridField) {
                    SubformGridField subformField = (SubformGridField) comp;

                    // Pre-populate parent filter values
                    for (FieldMeta parentField : currentFormDef.getFields()) {
                        Component parentComp = formComponents.get(parentField.getFieldName());
                        if (parentComp instanceof com.vaadin.flow.component.HasValue) {
                            Object val = ((com.vaadin.flow.component.HasValue<?, ?>) parentComp).getValue();
                            subformField.setParentFieldValue(parentField.getFieldName(), val);
                        }
                    }

                    List<Map<String, Object>> childData = new ArrayList<>();
                    if (parentPkValue != null && field.getLovCode() != null && field.getFormula() != null) {
                        FormMeta childForm = formMetaRepository.findById(field.getLovCode()).orElse(null);
                        if (childForm != null) {
                            String childTableName = (childForm.getViewTable() != null && !childForm.getViewTable().trim().isEmpty())
                                    ? childForm.getViewTable().trim()
                                    : childForm.getTableName();
                            String childFkColumn = field.getFormula();
                            childData = dynamicDataService.fetchDetailTableData(childTableName, childFkColumn,
                                    parentPkValue);
                        }
                    }
                    subformField.setValue(childData);
                    subformField.clearDeletedValues();
                }
            }
        }
    }

    private void clearAllComponents() {
        if (formComponents != null) {
            for (Component comp : formComponents.values()) {
                if (comp instanceof com.vaadin.flow.component.HasValue) {
                    ((com.vaadin.flow.component.HasValue<?, ?>) comp).clear();
                } else if (comp instanceof SubformGridField) {
                    ((SubformGridField) comp).setValue(new ArrayList<>());
                }
            }
        }
        clearSubformGrids();
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

    private void clearSubformGrids() {
        if (currentFormDef == null)
            return;
        for (FieldMeta field : currentFormDef.getFields()) {
            if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) {
                Component comp = formComponents.get(field.getFieldName());
                if (comp instanceof SubformGridField) {
                    SubformGridField subformField = (SubformGridField) comp;
                    subformField.setValue(new ArrayList<>());
                    subformField.clearDeletedValues();

                    // Clear parent filter values
                    for (FieldMeta parentField : currentFormDef.getFields()) {
                        subformField.setParentFieldValue(parentField.getFieldName(), null);
                    }
                }
            }
        }
    }
}
