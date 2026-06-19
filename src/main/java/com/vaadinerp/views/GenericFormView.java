package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
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
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.FieldFilterMeta;
import com.vaadin.flow.router.QueryParameters;

import com.vaadinerp.service.DynamicDataService;
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
import java.util.stream.Collectors;

@Route("generic")
public class GenericFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final FormMetaRepository formMetaRepository;
    private final DynamicDataService dynamicDataService;
    private Binder<Map<String, Object>> formBinder;

    private VerticalLayout formLayout;
    private Grid<Map<String, Object>> grid;
    private HorizontalLayout toolbar;
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

    private java.util.List<Map<String, Object>> gridItems = new java.util.ArrayList<>();
    private java.util.List<Map<String, Object>> allGridItems = new java.util.ArrayList<>();

    private static class FilterCriteria {
        String operator = "Contains";
        String value = "";
    }
    private java.util.Map<String, FilterCriteria> filterValues = new java.util.HashMap<>();
    private Map<String, Object> draggedItem;

    // Peta untuk menyimpan referensi komponen form untuk update lintas-field
    private Map<String, Component> formComponents = new HashMap<>();

    // ====== TAMBAHAN UNTUK COLUMN REORDERING ======
    // Mapping dari Grid.Column -> nama field, dipakai saat event reorder terjadi
    // untuk menerjemahkan urutan Column kembali ke nama field metadata.
    private Map<Grid.Column<Map<String, Object>>, String> columnToFieldNameMap = new HashMap<>();
    private String currentFormCode;
    private FormMeta currentFormDef;
    private boolean isEvaluatingFormulas = false;

    public GenericFormView(FormMetaRepository formMetaRepository, DynamicDataService dynamicDataService) {
        this.formMetaRepository = formMetaRepository;
        this.dynamicDataService = dynamicDataService;
        setWidthFull();
        setPadding(true);
        setSpacing(true);

        title = new H3("Loading...");
        toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setSpacing(true);

        formLayout = new VerticalLayout();
        formLayout.setWidthFull();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        gridToolbar = new HorizontalLayout();

        grid = new Grid<>();
        grid.setWidthFull();
        grid.setAllRowsVisible(true); // Memuat seluruh baris secara vertikal tanpa terpotong

        // Setup TabSheet layouts
        historisLayout = new VerticalLayout();
        historisLayout.setSizeFull();
        historisLayout.setPadding(true);
        historisLayout.setSpacing(true);
        historisLayout.add(gridToolbar, grid);

        transaksiLayout = new VerticalLayout();
        transaksiLayout.setWidthFull();
        transaksiLayout.setPadding(true);
        transaksiLayout.setSpacing(true);
        transaksiLayout.add(formLayout);

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        historisTab = tabSheet.add("Historis", historisLayout);
        transaksiTab = tabSheet.add("Transaksi", transaksiLayout);

        tabSheet.addSelectedChangeListener(event -> {
            if (currentFormDef != null) {
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
            tabSheet.setSelectedTab(transaksiTab);
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
                Map<String, Object> selected = grid.asSingleSelect().getValue();
                if (selected != null) {
                    showConfirmDialog("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus data yang dipilih ini?", () -> {
                        toolbar.setEnabled(false);
                        try {
                            dynamicDataService.deleteData(formDef, selected);
                            Notification.show("Data berhasil dihapus!", 3000, Notification.Position.TOP_CENTER);
                            refreshGridData(formDef);
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
                    showConfirmDialog("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus data transaksi ini?", () -> {
                        toolbar.setEnabled(false);
                        try {
                            dynamicDataService.deleteData(formDef, bean);
                            Notification.show("Data berhasil dihapus!", 3000, Notification.Position.TOP_CENTER);
                            formBinder.setBean(new HashMap<>());
                            refreshGridData(formDef);
                            tabSheet.setSelectedTab(historisTab);
                        } catch (Exception ex) {
                            Notification.show("Gagal menghapus: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                        } finally {
                            toolbar.setEnabled(true);
                        }
                    });
                } else {
                    Notification.show("Tidak ada data tersimpan yang terpilih untuk dihapus.", 3000, Notification.Position.MIDDLE);
                }
            }
        });

        // 3. SIMPAN BUTTON
        Button btnSave = new Button("Simpan");
        Icon iconSave = VaadinIcon.DOWNLOAD.create(); // matches floppy disk design
        iconSave.getStyle().set("color", "#64748b").set("font-size", "1.2rem");
        btnSave.setIcon(iconSave);
        btnSave.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("font-weight", "500").set("color", "#374151");
        btnSave.setDisableOnClick(true);
        btnSave.addClickListener(e -> {
            toolbar.setEnabled(false);
            try {
                boolean binderOk = formBinder.validate().isOk();
                boolean requiredOk = true;

                // Programmatic double-check for all required fields
                for (FieldMeta field : formDef.getFields()) {
                    if (field.isRequired()) {
                        Component comp = formComponents.get(field.getFieldName());
                        if (comp instanceof com.vaadin.flow.component.HasValue) {
                            Object val = ((com.vaadin.flow.component.HasValue<?, ?>) comp).getValue();
                            if (val == null || val.toString().trim().isEmpty()) {
                                requiredOk = false;
                                if (comp instanceof com.vaadin.flow.component.HasValidation) {
                                    ((com.vaadin.flow.component.HasValidation) comp).setInvalid(true);
                                    ((com.vaadin.flow.component.HasValidation) comp).setErrorMessage(field.getFieldLabel() + " wajib diisi");
                                }
                            }
                        }
                    }
                }

                if (binderOk && requiredOk) {
                    dynamicDataService.saveData(formDef, formBinder.getBean());
                    Notification.show("Data berhasil disimpan!", 3000, Notification.Position.TOP_CENTER);
                    formBinder.setBean(new HashMap<>());
                    refreshGridData(formDef);
                    tabSheet.setSelectedTab(historisTab);
                } else {
                    Notification.show("Silakan lengkapi kolom yang wajib diisi.", 3000, Notification.Position.MIDDLE);
                }
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
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
                if (tabSheet.getSelectedTab() == historisTab) {
                    refreshGridData(formDef);
                    Notification.show("Data berhasil diperbarui!", 1500, Notification.Position.BOTTOM_END);
                } else {
                    Map<String, Object> bean = formBinder.getBean();
                    String pk = formDef.getPrimaryKey() != null ? formDef.getPrimaryKey() : "id";
                    if (bean != null && bean.containsKey(pk) && bean.get(pk) != null && !bean.get(pk).toString().trim().isEmpty()) {
                        Object idVal = bean.get(pk);
                        Map<String, Object> freshRecord = dynamicDataService.fetchLovRecord(formDef.getTableName(), pk, idVal);
                        if (freshRecord != null) {
                            formBinder.setBean(new HashMap<>(freshRecord));
                            evaluateFormulas();
                            Notification.show("Data berhasil direfresh!", 1500, Notification.Position.BOTTOM_END);
                        } else {
                            Notification.show("Gagal merefresh: Data tidak ditemukan di database.", 3000, Notification.Position.MIDDLE);
                        }
                    } else {
                        formBinder.setBean(new HashMap<>());
                        Notification.show("Form dibersihkan!", 1500, Notification.Position.BOTTOM_END);
                    }
                }
            } catch (Exception ex) {
                Notification.show("Gagal menyegarkan: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            } finally {
                btnRefresh.setEnabled(true);
            }
        });

        toolbar.add(btnNew, btnDelete, btnSave, btnCancel, btnRefresh, btnClose, btnPrint);
    }

    private void applyFilterToComponent(Component targetComponent, String filterColumn, Object value) {
        if (targetComponent == null) {
            return;
        }
        if (targetComponent instanceof LovComboBox) {
            LovComboBox combo = (LovComboBox) targetComponent;
            combo.setFilterValue(filterColumn, value);
            combo.clear();
        } else if (targetComponent instanceof LovSelect) {
            LovSelect select = (LovSelect) targetComponent;
            select.setFilterValue(filterColumn, value);
            select.clear();
        } else if (targetComponent instanceof BandboxField) {
            BandboxField<?, ?> bandbox = (BandboxField<?, ?>) targetComponent;
            bandbox.setFilterValue(filterColumn, value);
            bandbox.clear();
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
                } catch (Exception ignored) {}
            }
        }
    }

    private void buildForm(FormMeta formDef, com.vaadin.flow.router.QueryParameters queryParameters) {
        formLayout.removeAll();
        formComponents.clear();

        java.util.function.BiConsumer<String, Object> updateFieldValue = (targetFieldName, value) -> {
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
                        } catch (Exception ignored) {}
                    }
                } else {
                    Object convertedValue = value;
                    if (targetComponent instanceof com.vaadin.flow.component.datepicker.DatePicker && !(value instanceof java.time.LocalDate)) {
                        try {
                            convertedValue = java.time.LocalDate.parse(value.toString());
                        } catch (Exception e) {
                            convertedValue = null;
                        }
                    } else if (targetComponent instanceof com.vaadin.flow.component.textfield.BigDecimalField && !(value instanceof java.math.BigDecimal)) {
                        try {
                            convertedValue = new java.math.BigDecimal(value.toString());
                        } catch (Exception e) {
                            convertedValue = null;
                        }
                    } else if (targetComponent instanceof com.vaadin.flow.component.textfield.IntegerField && !(value instanceof Integer)) {
                        try {
                            convertedValue = Integer.parseInt(value.toString());
                        } catch (Exception e) {
                            convertedValue = null;
                        }
                    }
                    try {
                        hasValue.setValue(convertedValue);
                    } catch (Exception ignored) {}
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
                rowLayout.add(input);
                formComponents.put(field.getFieldName(), input);
                bindComponent(formBinder, input, field);

                if ("TEXTAREA".equalsIgnoreCase(field.getComponentType())) {
                    rowLayout.setColspan(input, cols);
                }
            }
            formLayout.add(rowLayout);
        }

        // 2. Setup cascading listeners for FIELD filters
        for (FieldMeta field : formDef.getFields()) {
            if (field.getFilters() != null) {
                for (FieldFilterMeta filter : field.getFilters()) {
                    if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                        String sourceFieldName = filter.getSourceName();
                        Component sourceComponent = formComponents.get(sourceFieldName);
                        Component targetComponent = formComponents.get(field.getFieldName());
                        if (sourceComponent instanceof com.vaadin.flow.component.HasValue && targetComponent != null) {
                            @SuppressWarnings("unchecked")
                            com.vaadin.flow.component.HasValue<?, Object> hasValueSource = (com.vaadin.flow.component.HasValue<?, Object>) sourceComponent;
                            hasValueSource.addValueChangeListener(event -> {
                                Object newValue = event.getValue();
                                applyFilterToComponent(targetComponent, filter.getFilterColumn(), newValue);
                            });
                        }
                    }
                }
            }
        }

        // 3. Setup STATIC and direct QUERY filters
        for (FieldMeta field : formDef.getFields()) {
            if (field.getFilters() != null) {
                for (FieldFilterMeta filter : field.getFilters()) {
                    Component targetComponent = formComponents.get(field.getFieldName());
                    if (targetComponent != null) {
                        if ("STATIC".equalsIgnoreCase(filter.getSourceType())) {
                            applyFilterToComponent(targetComponent, filter.getFilterColumn(), filter.getSourceName());
                        } else if ("QUERY".equalsIgnoreCase(filter.getSourceType())) {
                            String paramName = filter.getSourceName();
                            if (queryParameters != null && queryParameters.getParameters().containsKey(paramName)) {
                                java.util.List<String> vals = queryParameters.getParameters().get(paramName);
                                if (vals != null && !vals.isEmpty()) {
                                    applyFilterToComponent(targetComponent, filter.getFilterColumn(), vals.get(0));
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Preset field values from URL query parameters (memicu cascading secara otomatis)
        if (queryParameters != null && !queryParameters.getParameters().isEmpty()) {
            Map<String, Object> bean = formBinder.getBean();
            if (bean == null) {
                bean = new HashMap<>();
                formBinder.setBean(bean);
            }
            for (Map.Entry<String, java.util.List<String>> entry : queryParameters.getParameters().entrySet()) {
                String fieldName = entry.getKey();
                Component comp = formComponents.get(fieldName);
                if (comp instanceof com.vaadin.flow.component.HasValue && entry.getValue() != null && !entry.getValue().isEmpty()) {
                    presetFieldFromQuery(comp, entry.getValue().get(0));
                }
            }
        }
    }

    private void buildInlineEditingGrid(FormMeta formDef) {
        grid.removeAllColumns();
        columnToFieldNameMap.clear(); // reset mapping setiap kali grid dibangun ulang

        // 1. Konfigurasi Toolbar Grid
        gridToolbar.removeAll();
        gridToolbar.setWidthFull();
        gridToolbar.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        
        com.vaadin.flow.component.html.H4 sectionTitle = new com.vaadin.flow.component.html.H4("Riwayat Data");
        sectionTitle.getStyle().set("margin", "0");
        sectionTitle.getStyle().set("flex-grow", "1");
        gridToolbar.add(sectionTitle);

        // Double Click Listener to load data into form and switch tab
        grid.addItemDoubleClickListener(event -> {
            Map<String, Object> selectedRow = event.getItem();
            if (selectedRow != null) {
                Map<String, Object> formValues = new HashMap<>(selectedRow);
                formBinder.setBean(formValues);
                evaluateFormulas();
                tabSheet.setSelectedTab(transaksiTab);
            }
        });

        // LinkedHashMap to hold columns for Header Filter Row
        java.util.Map<String, Grid.Column<Map<String, Object>>> columnsMap = new java.util.LinkedHashMap<>();

        // Urutkan field berdasarkan colOrder (kalau ada), supaya urutan kolom
        // saat render awal mengikuti urutan yang sudah pernah disimpan dari reorder sebelumnya.
        java.util.List<FieldMeta> sortedFields = new java.util.ArrayList<>(formDef.getFields());
        sortedFields.sort((f1, f2) -> {
            Integer o1 = f1.getColOrder() != null ? f1.getColOrder() : Integer.MAX_VALUE;
            Integer o2 = f2.getColOrder() != null ? f2.getColOrder() : Integer.MAX_VALUE;
            return o1.compareTo(o2);
        });

        for (FieldMeta field : sortedFields) {
            if (field.isShowInGrid()) {
                String fieldName = field.getFieldName();
                Grid.Column<Map<String, Object>> col = grid.addColumn(map -> map.get(fieldName))
                        .setHeader(field.getFieldLabel())
                        .setAutoWidth(true)
                        .setFlexGrow(1)
                        .setKey(fieldName); // setKey supaya bisa diakses ulang kalau perlu

                // Setup Comparator for Sorting
                col.setComparator((map1, map2) -> {
                    Object val1 = map1.get(fieldName);
                    Object val2 = map2.get(fieldName);
                    if (val1 == null && val2 == null) return 0;
                    if (val1 == null) return -1;
                    if (val2 == null) return 1;
                    if (val1 instanceof Comparable && val2 instanceof Comparable) {
                        return ((Comparable) val1).compareTo(val2);
                    }
                    return val1.toString().compareTo(val2.toString());
                });
                col.setSortable(true);

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
            com.vaadin.flow.component.contextmenu.ContextMenu contextMenu = new com.vaadin.flow.component.contextmenu.ContextMenu(filterButton);
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

            filterField.addValueChangeListener(e -> {
                criteria.value = e.getValue();
                applyFilters();
            });

            filterRow.getCell(col).setComponent(filterField);
        });

        // 3. Konfigurasi Drag and Drop untuk Row Reordering
        grid.setRowsDraggable(true);
        
        grid.addDragStartListener(event -> {
            if (!event.getDraggedItems().isEmpty()) {
                draggedItem = event.getDraggedItems().get(0);
                grid.setDropMode(GridDropMode.BETWEEN);
            }
        });

        grid.addDropListener(event -> {
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
                    applyFilters();
                }
            }
            draggedItem = null;
        });

        grid.addDragEndListener(event -> {
            draggedItem = null;
            grid.setDropMode(null);
        });

        // ====== 4. AKTIFKAN COLUMN REORDERING (Drag-and-drop GESER KOLOM) ======
        grid.setColumnReorderingAllowed(true);

        grid.addColumnReorderListener(event -> {
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
                dynamicDataService.saveColumnOrder(currentFormCode, orderedFieldNames);
                Notification.show("Urutan kolom disimpan", 1500, Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan urutan kolom: " + ex.getMessage(),
                        3000, Notification.Position.MIDDLE);
            }
        });
        // =========================================================================

        // Set Grid Items dari database
        refreshGridData(formDef);
    }

    private void refreshGridData(FormMeta formDef) {
        allGridItems.clear();
        allGridItems.addAll(dynamicDataService.fetchTableData(formDef.getTableName()));
        applyFilters();
    }

    private void applyFilters() {
        java.util.List<Map<String, Object>> filtered = allGridItems.stream().filter(item -> {
            for (Map.Entry<String, FilterCriteria> entry : filterValues.entrySet()) {
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
        }).collect(java.util.stream.Collectors.toList());
        
        gridItems.clear();
        gridItems.addAll(filtered);
        grid.setItems(new java.util.ArrayList<>(gridItems));
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
                if (field.getFormula() != null && !field.getFormula().trim().isEmpty()) {
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

    @SuppressWarnings("unchecked")
    private <V> V convertToFieldValue(Object value, Component component) {
        if (value == null) {
            if (component instanceof com.vaadin.flow.component.checkbox.Checkbox) {
                return (V) Boolean.FALSE;
            }
            return null;
        }
        if (component instanceof com.vaadin.flow.component.datepicker.DatePicker) {
            if (value instanceof java.time.LocalDate) {
                return (V) value;
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
        if (component instanceof com.vaadin.flow.component.textfield.BigDecimalField) {
            if (value instanceof java.math.BigDecimal) {
                return (V) value;
            }
            try {
                return (V) new java.math.BigDecimal(value.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if (component instanceof com.vaadin.flow.component.textfield.IntegerField) {
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
            if (value instanceof Number) {
                return (V) Boolean.valueOf(((Number) value).intValue() != 0);
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
            component instanceof com.vaadin.flow.component.select.Select) {
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
        com.vaadin.flow.component.HasValue<?, V> hasValue = (com.vaadin.flow.component.HasValue<?, V>) editComponent;
        Binder.BindingBuilder<Map<String, Object>, V> builder = binder.forField(hasValue);
        if (field.isRequired()) {
            builder.asRequired(field.getFieldLabel() + " wajib diisi");
        }
        builder.bind(
                map -> convertToFieldValue(map.get(field.getFieldName()), editComponent),
                (map, value) -> map.put(field.getFieldName(), value));
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
}
