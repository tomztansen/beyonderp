package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadinerp.components.ApexCapacityChart;
import com.vaadinerp.components.VisTimeline;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.SchedulerConfig;
import com.vaadinerp.meta.SchedulerConfigRepository;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.service.SessionSecurityService;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route("scheduler")
@PermitAll
public class DynamicSchedulerView extends VerticalLayout implements HasUrlParameter<String> {

    private final FormMetaRepository formMetaRepository;
    private final SchedulerConfigRepository schedulerConfigRepository;
    private final DynamicDataService dataService;
    private final SessionSecurityService securityService;
    private final JdbcTemplate jdbcTemplate;

    private String formCode;
    private FormMeta formMeta;
    private SchedulerConfig schedulerConfig;

    private Grid<Map<String, Object>> grid;
    private VisTimeline timeline;
    private ApexCapacityChart capacityChart1;
    private ApexCapacityChart capacityChart2;
    private ComboBox<String> viewModeCombo;
    private ComboBox<String> groupFilterCombo1;
    private ComboBox<String> groupFilterCombo2;
    private ComboBox<String> timelineGroupFilter;
    private ComboBox<String> timelineResourceFilter;
    private boolean isWeeklyView = true;
    private ComboBox<String> resourceFilterCombo1;
    private ComboBox<String> resourceFilterCombo2;
    private ComboBox<String> capacityModeCombo1;
    private ComboBox<String> capacityModeCombo2;
    private String currentResourceFilter1 = null;
    private String currentResourceFilter2 = null;
    private String currentTimelineGroupFilter = null;
    private String currentTimelineResourceFilter = null;
    private String currentCapacityMode1 = "QTYBOX";
    private String currentCapacityMode2 = "QTYBOX";
    private com.vaadin.flow.component.datepicker.DatePicker startDateFilter;
    private com.vaadin.flow.component.datepicker.DatePicker endDateFilter;

    private List<Map<String, Object>> currentData = new ArrayList<>();
    private java.util.Set<Map<String, Object>> modifiedRows = new java.util.HashSet<>();
    private java.util.Set<String> currentLateSpks = new java.util.HashSet<>();
    private Button btnSaveEdits;
    private com.vaadin.flow.component.checkbox.Checkbox chkHideUnassigned;

    private Runnable closeHandler;
    
    private final Map<Grid.Column<Map<String, Object>>, java.util.function.Function<Map<String, Object>, String>> colGetterMap = new java.util.concurrent.ConcurrentHashMap<>();
    private Runnable reapplyGridFilters;

    public DynamicSchedulerView(FormMetaRepository formMetaRepository,
            SchedulerConfigRepository schedulerConfigRepository,
            DynamicDataService dataService,
            SessionSecurityService securityService,
            JdbcTemplate jdbcTemplate) {
        this.formMetaRepository = formMetaRepository;
        this.schedulerConfigRepository = schedulerConfigRepository;
        this.dataService = dataService;
        this.securityService = securityService;
        this.jdbcTemplate = jdbcTemplate;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        this.formCode = parameter;
        if (formCode != null) {
            initView();
        }
    }

    public void applyInitialParameters(Object extra) {
        // Handle extra parameters if passed
    }

    public void hideTitle() {
        // Required method for generic view loader
    }

    // ================================================================
    // INIT VIEW
    // ================================================================
    private void initView() {
        removeAll();

        com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
            "if (!document.getElementById('late-timeline-styles')) {" +
            "  const style = document.createElement('style');" +
            "  style.id = 'late-timeline-styles';" +
            "  style.innerHTML = '.vis-item.is-late { background-color: #ef4444 !important; border-color: #b91c1c !important; color: white !important; font-weight: bold !important; } .vis-item.is-late.vis-selected { border-width: 2px !important; border-color: #7f1d1d !important; background-color: #b91c1c !important; }';" +
            "  document.head.appendChild(style);" +
            "}"
        );

        Optional<FormMeta> optForm = formMetaRepository.findById(formCode);
        if (optForm.isEmpty()) {
            add(new Span("Form " + formCode + " not found."));
            return;
        }
        formMeta = optForm.get();

        // Load scheduler config from meta_scheduler_config
        Optional<SchedulerConfig> optConfig = schedulerConfigRepository.findByFormCode(formCode);
        if (optConfig.isEmpty()) {
            add(new Span("⚠️ Scheduler configuration not found for form: " + formCode
                    + ". Please configure it in FormBuilder."));
            return;
        }
        schedulerConfig = optConfig.get();
        currentCapacityMode1 = schedulerConfig.getDefaultCapacityMode() != null
                ? schedulerConfig.getDefaultCapacityMode()
                : "QTYBOX";
        currentCapacityMode2 = currentCapacityMode1;

        // === TOOLBAR ===
        HorizontalLayout toolbar = buildToolbar();

        // === MAIN CONTENT: SplitLayout vertical (Timeline atas, Capacity chart bawah)
        // ===
        SplitLayout mainSplit = new SplitLayout(SplitLayout.Orientation.VERTICAL);
        mainSplit.setSizeFull();
        mainSplit.setSplitterPosition(60);

        // Timeline
        VerticalLayout timelineLayout = new VerticalLayout();
        timelineLayout.setSizeFull();
        timelineLayout.setPadding(false);
        timelineLayout.setSpacing(false);

        HorizontalLayout timelineHeader = new HorizontalLayout();
        timelineHeader.setWidthFull();
        timelineHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        timelineHeader.getStyle().set("background-color", "#ffffff").set("border-bottom", "1px solid #e2e8f0")
                .set("padding", "8px 16px");

        Span timelineTitle = new Span("🕒 Timeline");
        timelineTitle.getStyle().set("font-weight", "600").set("font-size", "14px").set("color", "#334155")
                .set("margin-right", "auto");

        timelineGroupFilter = new ComboBox<>();
        timelineGroupFilter.setPlaceholder("Group (Semua)");
        timelineGroupFilter.setClearButtonVisible(true);
        timelineGroupFilter.setWidth("160px");
        timelineGroupFilter.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        timelineGroupFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                currentTimelineGroupFilter = e.getValue();
                populateTimelineResourceFilter();
                updateTimelineData();
            }
        });

        timelineResourceFilter = new ComboBox<>();
        timelineResourceFilter.setPlaceholder("Mesin (Semua)");
        timelineResourceFilter.setClearButtonVisible(true);
        timelineResourceFilter.setWidth("160px");
        timelineResourceFilter.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        timelineResourceFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                currentTimelineResourceFilter = e.getValue();
                updateTimelineData();
            }
        });

        timelineHeader.add(timelineTitle, timelineGroupFilter, timelineResourceFilter);

        timeline = new VisTimeline();
        timeline.setSizeFull();
        timeline.setItemMoveListener(this::handleItemMove);
        timeline.setItemClickListener(this::handleItemClick);
        timeline.setItemContextMenuListener(this::handleItemContextMenu);

        timelineLayout.add(timelineHeader, timeline);

        // Capacity Chart
        HorizontalLayout capacityPanel = new HorizontalLayout();
        capacityPanel.setSizeFull();
        capacityPanel.setPadding(false);
        capacityPanel.setSpacing(true);

        VerticalLayout col1 = new VerticalLayout();
        col1.setSizeFull();
        col1.setPadding(false);
        col1.setSpacing(false);

        HorizontalLayout header1 = new HorizontalLayout();
        header1.setWidthFull();
        header1.setAlignItems(FlexComponent.Alignment.CENTER);
        Span title1 = new Span("📊 Chart 1");
        title1.getStyle().set("font-weight", "600").set("font-size", "14px").set("padding", "8px").set("color",
                "#334155");
        groupFilterCombo1 = new ComboBox<>();
        groupFilterCombo1.setPlaceholder("Group 1 (Semua)");
        groupFilterCombo1.setClearButtonVisible(true);
        groupFilterCombo1.setWidth("160px");
        groupFilterCombo1.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        groupFilterCombo1.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                populateResourceFilters1();
                updateSingleChart(currentResourceFilter1, currentCapacityMode1, capacityChart1,
                        groupFilterCombo1.getValue());
            }
        });

        resourceFilterCombo1 = new ComboBox<>();
        resourceFilterCombo1.setPlaceholder("Mesin 1 (Semua)");
        resourceFilterCombo1.setClearButtonVisible(true);
        resourceFilterCombo1.setWidth("160px");
        resourceFilterCombo1.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        resourceFilterCombo1.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                currentResourceFilter1 = e.getValue();
                updateSingleChart(currentResourceFilter1, currentCapacityMode1, capacityChart1,
                        groupFilterCombo1.getValue());
            }
        });

        capacityModeCombo1 = new ComboBox<>();
        capacityModeCombo1.setItems("Per Qty Box", "Per Weight");
        capacityModeCombo1.setValue("QTYBOX".equals(currentCapacityMode1) ? "Per Qty Box" : "Per Weight");
        capacityModeCombo1.setWidth("140px");
        capacityModeCombo1.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        capacityModeCombo1.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                currentCapacityMode1 = "Per Weight".equals(e.getValue()) ? "WEIGHT" : "QTYBOX";
                updateSingleChart(currentResourceFilter1, currentCapacityMode1, capacityChart1,
                        groupFilterCombo1.getValue());
                updateTimelineCapacityColors();
            }
        });

        header1.add(title1, groupFilterCombo1, resourceFilterCombo1, capacityModeCombo1);
        capacityChart1 = new ApexCapacityChart();
        capacityChart1.setSizeFull();
        capacityChart1.setWeeklyView(true);
        capacityChart1.addChartItemClickListener(e -> handleChartItemClick(e.getTaskName(), e.getDate()));
        col1.add(header1, capacityChart1);

        VerticalLayout col2 = new VerticalLayout();
        col2.setSizeFull();
        col2.setPadding(false);
        col2.setSpacing(false);

        HorizontalLayout header2 = new HorizontalLayout();
        header2.setWidthFull();
        header2.setAlignItems(FlexComponent.Alignment.CENTER);
        Span title2 = new Span("📊 Chart 2");
        title2.getStyle().set("font-weight", "600").set("font-size", "14px").set("padding", "8px").set("color",
                "#334155");
        groupFilterCombo2 = new ComboBox<>();
        groupFilterCombo2.setPlaceholder("Group 2 (Semua)");
        groupFilterCombo2.setClearButtonVisible(true);
        groupFilterCombo2.setWidth("160px");
        groupFilterCombo2.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        groupFilterCombo2.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                populateResourceFilters2();
                updateSingleChart(currentResourceFilter2, currentCapacityMode2, capacityChart2,
                        groupFilterCombo2.getValue());
            }
        });

        resourceFilterCombo2 = new ComboBox<>();
        resourceFilterCombo2.setPlaceholder("Mesin 2 (Semua)");
        resourceFilterCombo2.setClearButtonVisible(true);
        resourceFilterCombo2.setWidth("160px");
        resourceFilterCombo2.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        resourceFilterCombo2.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                currentResourceFilter2 = e.getValue();
                updateSingleChart(currentResourceFilter2, currentCapacityMode2, capacityChart2,
                        groupFilterCombo2.getValue());
            }
        });

        capacityModeCombo2 = new ComboBox<>();
        capacityModeCombo2.setItems("Per Qty Box", "Per Weight");
        capacityModeCombo2.setValue("QTYBOX".equals(currentCapacityMode2) ? "Per Qty Box" : "Per Weight");
        capacityModeCombo2.setWidth("140px");
        capacityModeCombo2.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        capacityModeCombo2.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                currentCapacityMode2 = "Per Weight".equals(e.getValue()) ? "WEIGHT" : "QTYBOX";
                updateSingleChart(currentResourceFilter2, currentCapacityMode2, capacityChart2,
                        groupFilterCombo2.getValue());
            }
        });

        header2.add(title2, groupFilterCombo2, resourceFilterCombo2, capacityModeCombo2);
        capacityChart2 = new ApexCapacityChart();
        capacityChart2.setSizeFull();
        capacityChart2.setWeeklyView(true);
        capacityChart2.addChartItemClickListener(e -> handleChartItemClick(e.getTaskName(), e.getDate()));
        col2.add(header2, capacityChart2);

        capacityPanel.add(col1, col2);

        mainSplit.addToPrimary(timelineLayout);
        mainSplit.addToSecondary(capacityPanel);

        // === OUTER SPLIT: Grid (kiri 25%) | Main content (kanan 75%) ===
        SplitLayout outerSplit = new SplitLayout();
        outerSplit.setSizeFull();
        outerSplit.setSplitterPosition(25);

        // Grid panel
        VerticalLayout gridPanel = new VerticalLayout();
        gridPanel.setSizeFull();
        gridPanel.setPadding(false);
        gridPanel.setSpacing(false);

        HorizontalLayout gridToolbar = new HorizontalLayout();
        gridToolbar.setWidthFull();
        gridToolbar.setPadding(true);
        gridToolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        Span gridTitle = new Span(formMeta != null ? formMeta.getFormTitle() : "Daftar Jadwal");
        gridTitle.getStyle().set("font-weight", "bold").set("flex-grow", "1");
        
        btnSaveEdits = new Button("Save Edits", VaadinIcon.CHECK.create(), e -> saveInlineEdits());
        btnSaveEdits.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btnSaveEdits.setEnabled(false);
        
        gridToolbar.add(gridTitle, btnSaveEdits);

        grid = new Grid<>();
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        buildGridColumns();

        gridPanel.add(gridToolbar, grid);

        outerSplit.addToPrimary(gridPanel);
        outerSplit.addToSecondary(mainSplit);

        add(toolbar, outerSplit);

        refreshData();
    }

    // ================================================================
    // TOOLBAR
    // ================================================================
    private HorizontalLayout buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setPadding(true);
        toolbar.setSpacing(true);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.getStyle().set("background-color", "#f8fafc").set("border-bottom", "1px solid #e2e8f0");

        startDateFilter = new com.vaadin.flow.component.datepicker.DatePicker();
        startDateFilter.setPlaceholder("Start Date");
        startDateFilter.setValue(LocalDate.now().minusDays(7));
        startDateFilter.setWidth("140px");

        endDateFilter = new com.vaadin.flow.component.datepicker.DatePicker();
        endDateFilter.setPlaceholder("End Date");
        endDateFilter.setValue(LocalDate.now().plusDays(30));
        endDateFilter.setWidth("140px");

        Button btnRefresh = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> refreshData());
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button btnZoomIn = new Button("", VaadinIcon.SEARCH_PLUS.create(), e -> timeline.zoomIn());
        btnZoomIn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        btnZoomIn.setTooltipText("Zoom In");

        Button btnZoomOut = new Button("", VaadinIcon.SEARCH_MINUS.create(), e -> timeline.zoomOut());
        btnZoomOut.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        btnZoomOut.setTooltipText("Zoom Out");

        Button btnFitAll = new Button("Fit All", VaadinIcon.EXPAND_SQUARE.create(), e -> timeline.fitAll());
        btnFitAll.addThemeVariants(ButtonVariant.LUMO_SMALL);

        chkHideUnassigned = new com.vaadin.flow.component.checkbox.Checkbox("Sembunyikan Unassigned");
        chkHideUnassigned.setValue(true);
        chkHideUnassigned.getStyle().set("margin-left", "8px");
        chkHideUnassigned.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refreshData();
            }
        });

        // View Mode Toggle
        viewModeCombo = new ComboBox<>();
        viewModeCombo.setItems("Harian (Daily)", "Mingguan (Weekly)");
        viewModeCombo.setValue("Mingguan (Weekly)");
        viewModeCombo.setWidth("160px");
        viewModeCombo.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        viewModeCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                isWeeklyView = "Mingguan (Weekly)".equals(e.getValue());
                capacityChart1.setWeeklyView(isWeeklyView);
                capacityChart2.setWeeklyView(isWeeklyView);
                updateAllCharts();
            }
        });

        toolbar.add(startDateFilter, endDateFilter, btnRefresh, btnZoomIn, btnZoomOut, btnFitAll, viewModeCombo, chkHideUnassigned);
        return toolbar;
    }

    // ================================================================
    // GRID COLUMNS
    // ================================================================
    private void buildGridColumns() {
        com.vaadinerp.components.StandardGridUtils.cleanGridBeforeRebuild(grid);
        colGetterMap.clear();

        com.vaadin.flow.data.binder.Binder<Map<String, Object>> binder = new com.vaadin.flow.data.binder.Binder<>();
        grid.getEditor().setBinder(binder);
        grid.getEditor().setBuffered(false);

        grid.addItemDoubleClickListener(e -> {
            grid.getEditor().editItem(e.getItem());
        });

        List<FieldMeta> sortedFields = new ArrayList<>(formMeta.getFields());
        sortedFields.sort((f1, f2) -> {
            Integer o1 = f1.getColOrder() != null ? f1.getColOrder() : Integer.MAX_VALUE;
            Integer o2 = f2.getColOrder() != null ? f2.getColOrder() : Integer.MAX_VALUE;
            return o1.compareTo(o2);
        });

        for (FieldMeta field : sortedFields) {
            if (field.isShowInGrid()) {
                String fieldName = field.getFieldName();
                
                java.util.function.Function<Map<String, Object>, String> valueGetter = map -> {
                    Object valObj = map.get(fieldName);
                    if (valObj == null) {
                        for(Map.Entry<String, Object> entry : map.entrySet()) {
                            if(entry.getKey().equalsIgnoreCase(fieldName)) {
                                valObj = entry.getValue();
                                break;
                            }
                        }
                    }
                    String formatted = com.vaadinerp.components.ComponentFactory.formatFieldValueWithLov(field, valObj, dataService);
                    return formatted != null ? formatted : "";
                };
                
                Grid.Column<Map<String, Object>> col = grid.addColumn(valueGetter::apply)
                        .setHeader(field.getFieldLabel())
                        .setAutoWidth(true)
                        .setResizable(true)
                        .setSortable(field.isSortable());
                
                if (field.isSortable()) {
                    col.setComparator((map1, map2) -> {
                        Object val1 = map1.get(fieldName);
                        if (val1 == null) {
                            for (Map.Entry<String, Object> entry : map1.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(fieldName)) {
                                    val1 = entry.getValue();
                                    break;
                                }
                            }
                        }
                        Object val2 = map2.get(fieldName);
                        if (val2 == null) {
                            for (Map.Entry<String, Object> entry : map2.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(fieldName)) {
                                    val2 = entry.getValue();
                                    break;
                                }
                            }
                        }
                        
                        if (val1 == null && val2 == null) return 0;
                        if (val1 == null) return -1;
                        if (val2 == null) return 1;
                        
                        if (val1 instanceof Comparable && val2 instanceof Comparable && val1.getClass().equals(val2.getClass())) {
                            return ((Comparable<Object>) val1).compareTo(val2);
                        }
                        
                        // Fallback to string comparison for numbers that might be different types (e.g., Integer vs BigDecimal)
                        if (val1 instanceof Number && val2 instanceof Number) {
                            return Double.compare(((Number) val1).doubleValue(), ((Number) val2).doubleValue());
                        }
                        
                        return val1.toString().compareTo(val2.toString());
                    });
                }
                        
                colGetterMap.put(col, valueGetter);

                String fType = field.getComponentType() != null ? field.getComponentType().toUpperCase() : "";
                if ("INTBOX".equals(fType) || "DECIMALBOX".equals(fType)) {
                    col.setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
                }

                if (!field.isReadonly()) {
                    // Prevent editing core scheduler identifiers via grid inline edit to avoid sync issues with VisTimeline
                    boolean isCoreField = fieldName.equalsIgnoreCase(schedulerConfig.getColPrimaryKey()) ||
                                          fieldName.equalsIgnoreCase(schedulerConfig.getColStartDate()) ||
                                          fieldName.equalsIgnoreCase(schedulerConfig.getColEndDate()) ||
                                          fieldName.equalsIgnoreCase(schedulerConfig.getColResource());
                    
                    if (!isCoreField) {
                        com.vaadin.flow.component.Component editorComp = com.vaadinerp.components.ComponentFactory.create(field, dataService, null);
                        if (editorComp instanceof com.vaadin.flow.component.HasValue) {
                            @SuppressWarnings("unchecked")
                            com.vaadin.flow.component.HasValue<?, Object> hasValueComp = (com.vaadin.flow.component.HasValue<?, Object>) editorComp;
                            com.vaadin.flow.data.binder.Binder.BindingBuilder<Map<String, Object>, Object> binding = binder.forField(hasValueComp);
                            binding.bind(
                                map -> {
                                    Object val = map.get(fieldName);
                                    if (val == null) {
                                        for(Map.Entry<String, Object> entry : map.entrySet()) {
                                            if(entry.getKey().equalsIgnoreCase(fieldName)) {
                                                return entry.getValue();
                                            }
                                        }
                                    }
                                    return val;
                                },
                                (map, val) -> {
                                    Object oldVal = map.get(fieldName);
                                    if (oldVal == null) {
                                        for(String key : map.keySet()) {
                                            if(key.equalsIgnoreCase(fieldName)) {
                                                oldVal = map.get(key);
                                                break;
                                            }
                                        }
                                    }
                                    if (!java.util.Objects.equals(oldVal, val)) {
                                        map.put(fieldName, val);
                                        modifiedRows.add(map);
                                        btnSaveEdits.setEnabled(true);
                                    }
                                }
                            );
                            col.setEditorComponent(editorComp);
                        }
                    }
                }
            }
        }
        
        reapplyGridFilters = com.vaadinerp.components.StandardGridUtils.attachGridFilters(
                grid,
                colGetterMap,
                () -> currentData
        );
    }

    private void saveInlineEdits() {
        if (modifiedRows.isEmpty()) {
            Notification.show("Tidak ada perubahan untuk disimpan.", 3000, Notification.Position.BOTTOM_END);
            return;
        }

        String tableName = schedulerConfig.getUpdateTable();
        if (tableName == null || tableName.trim().isEmpty()) {
            tableName = formMeta.getTableName();
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            Notification.show("Update table belum dikonfigurasi!", 4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        String pkCol = schedulerConfig.getColPrimaryKey();
        if (pkCol == null || pkCol.isEmpty()) {
            pkCol = formMeta.getPrimaryKey();
        }
        if (pkCol == null || pkCol.isEmpty()) pkCol = "id";

        int successCount = 0;
        try {
            for (Map<String, Object> row : modifiedRows) {
                Object pkVal = row.get(pkCol);
                if (pkVal == null) continue;

                StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
                List<Object> args = new ArrayList<>();
                boolean first = true;
                
                for (FieldMeta field : formMeta.getFields()) {
                    if (!field.isReadonly() && field.isShowInGrid()) {
                        String fName = field.getFieldName();
                        if (fName.equalsIgnoreCase(pkCol)) continue;
                        if (!first) sql.append(", ");
                        sql.append(fName).append(" = ?");
                        args.add(row.get(fName));
                        first = false;
                    }
                }
                
                if (!first) {
                    sql.append(" WHERE ").append(pkCol).append(" = ?");
                    args.add(pkVal);
                    jdbcTemplate.update(sql.toString(), args.toArray());
                    successCount++;
                }
            }
            
            modifiedRows.clear();
            btnSaveEdits.setEnabled(false);
            Notification.show(successCount + " baris berhasil disimpan.", 3000, Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Refresh visuals to reflect new capacity if qty was changed
            updateAllCharts();
            updateTimelineCapacityColors();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Gagal menyimpan: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // ================================================================
    // REFRESH DATA
    // ================================================================
    private void refreshData() {
        try {
            String query = schedulerConfig.getSchedulerQuery();
            String dateCol = schedulerConfig.getColStartDate();

            if (query == null || query.trim().isEmpty()) {
                currentData = new ArrayList<>();
            } else if (dateCol != null && !dateCol.trim().isEmpty() && startDateFilter.getValue() != null
                    && endDateFilter.getValue() != null) {
                String wrappedQuery = "SELECT * FROM (" + query + ") AS sq WHERE " + dateCol + " >= ? AND " + dateCol
                        + " <= ?";
                currentData = jdbcTemplate.queryForList(wrappedQuery, startDateFilter.getValue(),
                        endDateFilter.getValue());
            } else {
                currentData = jdbcTemplate.queryForList(query);
            }
            
            // Apply unassigned filter
            if (chkHideUnassigned != null && chkHideUnassigned.getValue()) {
                String resCol = schedulerConfig.getColResource();
                if (resCol != null) {
                    currentData = currentData.stream()
                            .filter(row -> row.get(resCol) != null && !row.get(resCol).toString().trim().isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                }
            }
        } catch (Exception e) {
            Notification.show("Error loading scheduler data: " + e.getMessage(), 5000,
                    Notification.Position.MIDDLE);
            currentData = new ArrayList<>();
        }

        populateGroupFilter();
        populateTimelineResourceFilter();
        populateResourceFilters1();
        populateResourceFilters2();

        updateTimelineData();
        updateAllCharts();

        // Terapkan filter grid (secara otomatis akan memanggil grid.setItems(...))
        if (reapplyGridFilters != null) {
            reapplyGridFilters.run();
        } else {
            grid.setItems(currentData);
        }
        
        if (grid.getDataProvider() != null) {
            grid.getDataProvider().refreshAll();
        }
    }

    // ================================================================
    // GROUP FILTER LOGIC
    // ================================================================
    private void populateGroupFilter() {
        String groupCol = schedulerConfig.getColResourceGroup();
        if (groupCol == null || groupCol.trim().isEmpty()) {
            groupFilterCombo1.setItems();
            groupFilterCombo2.setItems();
            timelineGroupFilter.setItems();
            return;
        }
        
        String curr1 = groupFilterCombo1.getValue();
        String curr2 = groupFilterCombo2.getValue();
        String currTimeline = timelineGroupFilter.getValue();

        Set<String> groups = new LinkedHashSet<>();
        for (Map<String, Object> row : currentData) {
            Object val = row.get(groupCol);
            if (val != null && !val.toString().trim().isEmpty()) {
                groups.add(val.toString());
            }
        }
        groupFilterCombo1.setItems(groups);
        groupFilterCombo2.setItems(groups);
        timelineGroupFilter.setItems(groups);
        
        if (curr1 != null && groups.contains(curr1)) groupFilterCombo1.setValue(curr1);
        if (curr2 != null && groups.contains(curr2)) groupFilterCombo2.setValue(curr2);
        if (currTimeline != null && groups.contains(currTimeline)) timelineGroupFilter.setValue(currTimeline);
    }

    // ================================================================
    // POPULATE RESOURCE FILTER
    // ================================================================
    private void populateTimelineResourceFilter() {
        if (schedulerConfig.getColResource() == null)
            return;
        String groupCol = schedulerConfig.getColResourceGroup();
        String selectedGroup = currentTimelineGroupFilter;
        String currResource = timelineResourceFilter.getValue();

        Set<String> resources = new LinkedHashSet<>();
        for (Map<String, Object> row : currentData) {
            boolean matchesGroup = true;
            if (groupCol != null && !groupCol.trim().isEmpty() && selectedGroup != null
                    && !selectedGroup.trim().isEmpty()) {
                Object gVal = row.get(groupCol);
                if (gVal == null || !selectedGroup.equals(gVal.toString())) {
                    matchesGroup = false;
                }
            }
            if (matchesGroup) {
                Object val = row.get(schedulerConfig.getColResource());
                if (val != null)
                    resources.add(val.toString());
            }
        }
        timelineResourceFilter.setItems(resources);
        if (currResource != null && resources.contains(currResource)) timelineResourceFilter.setValue(currResource);
    }

    private void populateResourceFilters1() {
        if (schedulerConfig.getColResource() == null)
            return;
        String groupCol = schedulerConfig.getColResourceGroup();
        String selectedGroup = groupFilterCombo1.getValue();
        String currResource = resourceFilterCombo1.getValue();

        Set<String> resources = new LinkedHashSet<>();
        for (Map<String, Object> row : currentData) {
            boolean matchesGroup = true;
            if (groupCol != null && !groupCol.trim().isEmpty() && selectedGroup != null
                    && !selectedGroup.trim().isEmpty()) {
                Object gVal = row.get(groupCol);
                if (gVal == null || !selectedGroup.equals(gVal.toString())) {
                    matchesGroup = false;
                }
            }
            if (matchesGroup) {
                Object val = row.get(schedulerConfig.getColResource());
                if (val != null)
                    resources.add(val.toString());
            }
        }
        resourceFilterCombo1.setItems(resources);
        if (currResource != null && resources.contains(currResource)) resourceFilterCombo1.setValue(currResource);
    }

    private void populateResourceFilters2() {
        if (schedulerConfig.getColResource() == null)
            return;
        String groupCol = schedulerConfig.getColResourceGroup();
        String selectedGroup = groupFilterCombo2.getValue();
        String currResource = resourceFilterCombo2.getValue();

        Set<String> resources = new LinkedHashSet<>();
        for (Map<String, Object> row : currentData) {
            boolean matchesGroup = true;
            if (groupCol != null && !groupCol.trim().isEmpty() && selectedGroup != null
                    && !selectedGroup.trim().isEmpty()) {
                Object gVal = row.get(groupCol);
                if (gVal == null || !selectedGroup.equals(gVal.toString())) {
                    matchesGroup = false;
                }
            }
            if (matchesGroup) {
                Object val = row.get(schedulerConfig.getColResource());
                if (val != null)
                    resources.add(val.toString());
            }
        }
        resourceFilterCombo2.setItems(resources);
        if (currResource != null && resources.contains(currResource)) resourceFilterCombo2.setValue(currResource);
    }

    // ================================================================
    // UPDATE TIMELINE (VisTimeline)
    private void updateTimelineData() {
        String resourceCol = schedulerConfig.getColResource();
        String taskNameCol = schedulerConfig.getColTaskName();
        String startDateCol = schedulerConfig.getColStartDate();
        String endDateCol = schedulerConfig.getColEndDate();
        String pkCol = schedulerConfig.getColPrimaryKey();
        String qtyCol = schedulerConfig.getColQty();
        String groupCol = schedulerConfig.getColResourceGroup();
        String shippingDateCol = schedulerConfig.getColShippingDate();

        if (resourceCol == null || startDateCol == null || pkCol == null) {
            Notification.show("Scheduler config incomplete: resource, start_date, and primary_key are required",
                    5000, Notification.Position.MIDDLE);
            return;
        }

        JsonArray groups = Json.createArray();
        List<String> addedGroups = new ArrayList<>();
        int groupIndex = 0;

        JsonArray items = Json.createArray();
        int itemIndex = 0;

        for (Map<String, Object> row : currentData) {
            // Apply timeline filters
            if (currentTimelineGroupFilter != null && !currentTimelineGroupFilter.isEmpty() && groupCol != null
                    && !groupCol.trim().isEmpty()) {
                Object gVal = row.get(groupCol);
                if (gVal == null || !currentTimelineGroupFilter.equals(gVal.toString())) {
                    continue;
                }
            }
            if (currentTimelineResourceFilter != null && !currentTimelineResourceFilter.isEmpty()) {
                Object rVal = row.get(resourceCol);
                if (rVal == null || !currentTimelineResourceFilter.equals(rVal.toString())) {
                    continue;
                }
            }
        }

        // Lateness Tracking Logic
        Map<String, LocalDate> spkMaxEndDate = new HashMap<>();
        Map<String, LocalDate> spkShippingDate = new HashMap<>();
        Set<String> lateSpks = new HashSet<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (taskNameCol != null) {
            for (Map<String, Object> row : currentData) {
                Object taskNameVal = row.get(taskNameCol);
                if (taskNameVal == null) continue;
                String spk = taskNameVal.toString();

                Object dateVal = endDateCol != null && row.get(endDateCol) != null ? row.get(endDateCol) : row.get(startDateCol);
                if (dateVal != null) {
                    try {
                        String ds = dateVal.toString();
                        if (ds.length() > 10) ds = ds.substring(0, 10);
                        LocalDate d = LocalDate.parse(ds, dtf);
                        LocalDate max = spkMaxEndDate.get(spk);
                        if (max == null || d.isAfter(max)) {
                            spkMaxEndDate.put(spk, d);
                        }
                    } catch (Exception ignored) {}
                }

                if (shippingDateCol != null && row.get(shippingDateCol) != null) {
                    try {
                        String ds = row.get(shippingDateCol).toString();
                        if (ds.length() > 10) ds = ds.substring(0, 10);
                        spkShippingDate.put(spk, LocalDate.parse(ds, dtf));
                    } catch (Exception ignored) {}
                }
            }

            for (String spk : spkMaxEndDate.keySet()) {
                LocalDate maxDate = spkMaxEndDate.get(spk);
                LocalDate shipDate = spkShippingDate.get(spk);
                if (shipDate != null && maxDate != null && maxDate.isAfter(shipDate)) {
                    lateSpks.add(spk);
                }
            }
        }
        
        currentLateSpks.clear();
        currentLateSpks.addAll(lateSpks);

        for (Map<String, Object> row : currentData) {
            // Apply timeline filters again for building items
            if (currentTimelineGroupFilter != null && !currentTimelineGroupFilter.isEmpty() && groupCol != null
                    && !groupCol.trim().isEmpty()) {
                Object gVal = row.get(groupCol);
                if (gVal == null || !currentTimelineGroupFilter.equals(gVal.toString())) {
                    continue;
                }
            }
            if (currentTimelineResourceFilter != null && !currentTimelineResourceFilter.isEmpty()) {
                Object rVal = row.get(resourceCol);
                if (rVal == null || !currentTimelineResourceFilter.equals(rVal.toString())) {
                    continue;
                }
            }

            Object resourceVal = row.get(resourceCol);
            String groupId = resourceVal != null ? resourceVal.toString() : "unassigned";

            if (!addedGroups.contains(groupId)) {
                JsonObject groupObj = Json.createObject();
                groupObj.put("id", groupId);
                groupObj.put("content", groupId);
                groups.set(groupIndex++, groupObj);
                addedGroups.add(groupId);
            }

            Object idVal = row.get(pkCol);
            if (idVal == null)
                continue;

            Object startVal = row.get(startDateCol);
            if (startVal == null)
                continue;

            // Build item
            JsonObject itemObj = Json.createObject();
            itemObj.put("id", idVal.toString());
            itemObj.put("group", groupId);

            // Content: taskName + qty
            String content = idVal.toString();
            if (taskNameCol != null && row.get(taskNameCol) != null) {
                content = row.get(taskNameCol).toString();
            }
            if (qtyCol != null && row.get(qtyCol) != null) {
                content += " (Qty: " + row.get(qtyCol) + ")";
            }
            itemObj.put("content", content);

            // Start date
            String startStr = startVal.toString();
            String dateOnly = startStr.length() >= 10 ? startStr.substring(0, 10) : startStr;

            // End date: if not configured, treat as a shorter range (06:00 to 18:00) so it
            // doesn't fill the whole day width
            if (endDateCol != null && row.get(endDateCol) != null) {
                // It has an end date, render as a range starting from 00:00 to 23:59
                itemObj.put("start", dateOnly + "T00:00:00");
                String endStr = row.get(endDateCol).toString();
                if (endStr.length() == 10)
                    endStr += "T23:59:59";
                itemObj.put("end", endStr);
            } else {
                // Default: range from 06:00 to 18:00 (so it doesn't touch the borders of the
                // day column)
                itemObj.put("start", dateOnly + "T00:00:00");
                itemObj.put("end", dateOnly + "T23:59:59");
                // Don't set type to 'box', let it default to 'range' since we provide start and
                // end
            }

            // Tooltip
            StringBuilder tooltip = new StringBuilder();
            tooltip.append("<b>").append(content).append("</b><br>");
            tooltip.append("Mesin: ").append(groupId).append("<br>");
            tooltip.append("Tanggal: ").append(startVal).append("<br>");
            if (qtyCol != null && row.get(qtyCol) != null) {
                tooltip.append("Qty: ").append(row.get(qtyCol)).append("<br>");
            }
            
            boolean isLate = false;
            if (taskNameCol != null && row.get(taskNameCol) != null) {
                String spk = row.get(taskNameCol).toString();
                LocalDate maxDate = spkMaxEndDate.get(spk);
                if (maxDate != null) {
                    tooltip.append("Last Process: ").append(maxDate.format(dtf)).append("<br>");
                }
                
                if (lateSpks.contains(spk)) {
                    isLate = true;
                    tooltip.append("<span style='color:red; font-weight:bold;'>⚠️ STATUS: LATE</span>");
                }
            }
            
            if (isLate) {
                itemObj.put("className", "is-late");
            } else {
                // use default coloring mechanism for timeline
            }

            itemObj.put("title", tooltip.toString());

            items.set(itemIndex++, itemObj);
        }

        // Add custom time markers for Shipping Dates
        JsonArray customTimes = Json.createArray();
        int ctIndex = 0;
        for (String spk : spkShippingDate.keySet()) {
            LocalDate sDate = spkShippingDate.get(spk);
            if (sDate == null) continue;
            
            JsonObject ctObj = Json.createObject();
            ctObj.put("id", "SHIP_" + spk);
            ctObj.put("date", sDate.toString() + "T23:59:59");
            ctObj.put("title", spk + " Due");
            ctObj.put("isLate", lateSpks.contains(spk));
            customTimes.set(ctIndex++, ctObj);
        }

        timeline.setGroups(groups);
        timeline.setItems(items);
        timeline.setCustomTimes(customTimes);

        // Set capacity colors
        updateTimelineCapacityColors();
    }

    // ================================================================
    // UPDATE TIMELINE CAPACITY COLORS
    // ================================================================
    private void updateTimelineCapacityColors() {
        String resourceCol = schedulerConfig.getColResource();
        String startDateCol = schedulerConfig.getColStartDate();
        String pkCol = schedulerConfig.getColPrimaryKey();
        String qtyCol = "QTYBOX".equals(currentCapacityMode1)
                ? schedulerConfig.getColQty()
                : schedulerConfig.getColWeight();
        String capCol = "QTYBOX".equals(currentCapacityMode1)
                ? schedulerConfig.getColMaxCapacity()
                : schedulerConfig.getColMaxCapacityWeight();

        if (qtyCol == null || capCol == null || resourceCol == null || startDateCol == null)
            return;

        // Calculate totals per resource + date
        Map<String, Double> totalsMap = new HashMap<>(); // key: "resource|date"
        Map<String, Double> capacityMap = new HashMap<>(); // key: "resource" → max capacity
        Map<String, String> itemResourceDateMap = new HashMap<>(); // itemId → "resource|date"

        for (Map<String, Object> row : currentData) {
            Object idVal = row.get(pkCol);
            if (idVal == null)
                continue;

            String resource = row.get(resourceCol) != null ? row.get(resourceCol).toString() : "";
            String date = row.get(startDateCol) != null ? row.get(startDateCol).toString().substring(0, 10) : "";
            String key = resource + "|" + date;

            double qty = 0;
            try {
                qty = Double.parseDouble(row.get(qtyCol).toString());
            } catch (Exception ignored) {
            }

            double cap = 0;
            try {
                cap = Double.parseDouble(row.get(capCol).toString());
            } catch (Exception ignored) {
            }

            totalsMap.merge(key, qty, Double::sum);
            capacityMap.putIfAbsent(resource, cap);
            itemResourceDateMap.put(idVal.toString(), key);
        }

        // Build capacity status array
        JsonArray statusArray = Json.createArray();
        int idx = 0;
        for (Map.Entry<String, String> entry : itemResourceDateMap.entrySet()) {
            String itemId = entry.getKey();
            String key = entry.getValue();
            String resource = key.split("\\|")[0];

            double total = totalsMap.getOrDefault(key, 0.0);
            double maxCap = capacityMap.getOrDefault(resource, 0.0);

            JsonObject status = Json.createObject();
            status.put("itemId", itemId);
            status.put("overcapacity", maxCap > 0 && total > maxCap);
            status.put("warningCapacity", maxCap > 0 && total > maxCap * 0.8 && total <= maxCap);
            statusArray.set(idx++, status);
        }

        timeline.setItemCapacityStatus(statusArray);
    }

    // ================================================================
    // UPDATE CAPACITY CHART (ApexCharts)
    // ================================================================
    private void updateAllCharts() {
        updateSingleChart(currentResourceFilter1, currentCapacityMode1, capacityChart1, groupFilterCombo1.getValue());
        updateSingleChart(currentResourceFilter2, currentCapacityMode2, capacityChart2, groupFilterCombo2.getValue());
    }

    private void updateSingleChart(String resourceFilter, String capacityMode, ApexCapacityChart chart,
            String groupFilter) {
        String resourceCol = schedulerConfig.getColResource();
        String startDateCol = schedulerConfig.getColStartDate();
        String taskNameCol = schedulerConfig.getColTaskName();
        String groupCol = schedulerConfig.getColResourceGroup();
        String qtyCol = "QTYBOX".equals(capacityMode)
                ? schedulerConfig.getColQty()
                : schedulerConfig.getColWeight();
        String capCol = "QTYBOX".equals(capacityMode)
                ? schedulerConfig.getColMaxCapacity()
                : schedulerConfig.getColMaxCapacityWeight();
        String capacityLabel = "QTYBOX".equals(capacityMode) ? "Qty Box" : "Weight (kg)";

        if (qtyCol == null || capCol == null)
            return;

        // Filter by group and resource
        List<Map<String, Object>> filteredData = currentData;
        if (groupFilter != null && !groupFilter.trim().isEmpty() && groupCol != null && !groupCol.trim().isEmpty()) {
            filteredData = filteredData.stream()
                    .filter(row -> groupFilter.equals(
                            row.get(groupCol) != null ? row.get(groupCol).toString() : ""))
                    .collect(Collectors.toList());
        }
        if (resourceFilter != null && !resourceFilter.isEmpty()) {
            filteredData = filteredData.stream()
                    .filter(row -> resourceFilter.equals(
                            row.get(resourceCol) != null ? row.get(resourceCol).toString() : ""))
                    .collect(Collectors.toList());
        }

        // Get max capacity (from first row of filtered data, or all data)
        int maxCapacity = 80;
        for (Map<String, Object> row : filteredData) {
            if (row.get(capCol) != null) {
                try {
                    maxCapacity = (int) Double.parseDouble(row.get(capCol).toString());
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        // Build chart data: {date, taskName, value}
        JsonArray chartData = Json.createArray();
        int idx = 0;
        for (Map<String, Object> row : filteredData) {
            Object dateVal = row.get(startDateCol);
            Object qtyVal = row.get(qtyCol);
            Object taskVal = row.get(taskNameCol);
            if (dateVal == null || qtyVal == null)
                continue;

            JsonObject point = Json.createObject();
            point.put("date", dateVal.toString().substring(0, 10));
            
            String tName = taskVal != null ? taskVal.toString() : "Unknown";
            if (currentLateSpks.contains(tName)) {
                tName += " (LATE)";
            }
            point.put("taskName", tName);
            
            try {
                point.put("value", Double.parseDouble(qtyVal.toString()));
            } catch (Exception e) {
                point.put("value", 0);
            }
            chartData.set(idx++, point);
        }

        String startDateStr = startDateFilter.getValue() != null ? startDateFilter.getValue().toString() : null;
        String endDateStr = endDateFilter.getValue() != null ? endDateFilter.getValue().toString() : null;
        chart.setChartData(chartData, maxCapacity, capacityLabel, startDateStr, endDateStr);
    }

    // ================================================================
    // HANDLE CHART ITEM CLICK
    // ================================================================
    private void handleChartItemClick(String taskName, String clickedDate) {
        if (taskName == null || "Unknown".equals(taskName))
            return;
            
        String originalTaskName = taskName;
        if (taskName.endsWith(" (LATE)")) {
            originalTaskName = taskName.substring(0, taskName.length() - 7);
        }
            
        String taskNameCol = schedulerConfig.getColTaskName();
        String pkCol = schedulerConfig.getColPrimaryKey();
        String dateCol = schedulerConfig.getColStartDate();
        
        elemental.json.JsonArray selectedIds = elemental.json.Json.createArray();
        int idx = 0;
        java.util.Set<Map<String, Object>> selectedRows = new java.util.HashSet<>();
        
        for (Map<String, Object> row : currentData) {
            Object tn = row.get(taskNameCol);
            Object rowDate = row.get(dateCol);
            
            boolean nameMatch = tn != null && originalTaskName.equals(tn.toString());
            boolean dateMatch = true;
            
            if (clickedDate != null && !clickedDate.isEmpty() && rowDate != null) {
                // rowDate is usually a java.sql.Date or Timestamp or String "YYYY-MM-DD..."
                dateMatch = rowDate.toString().startsWith(clickedDate);
            }
            
            if (nameMatch && dateMatch) {
                selectedRows.add(row);
                Object id = row.get(pkCol);
                if (id != null) {
                    selectedIds.set(idx++, id.toString());
                }
            }
        }
        
        if (!selectedRows.isEmpty()) {
            grid.asMultiSelect().setValue(selectedRows);
        }
        if (idx > 0) {
            timeline.setSelection(selectedIds);
        }
    }

    // ================================================================
    // HANDLE ITEM CLICK (select in grid)
    // ================================================================
    private void handleItemClick(String itemId) {
        if (itemId == null)
            return;
        String pkCol = schedulerConfig.getColPrimaryKey();
        for (Map<String, Object> row : currentData) {
            Object id = row.get(pkCol);
            if (id != null && id.toString().equals(itemId)) {
                grid.select(row);
                return;
            }
        }
    }

    // ================================================================
    // HANDLE ITEM CONTEXT MENU (Right Click in Timeline)
    // ================================================================
    private void handleItemContextMenu(String clickedItemId, String[] allSelectedItems) {
        if (clickedItemId == null)
            return;

        // Select in grid if not already selected
        String pkCol = schedulerConfig.getColPrimaryKey();
        Map<String, Object> clickedRow = null;
        for (Map<String, Object> row : currentData) {
            Object id = row.get(pkCol);
            if (id != null && id.toString().equals(clickedItemId)) {
                clickedRow = row;
                grid.select(row);
                break;
            }
        }

        if (clickedRow == null)
            return;

        com.vaadin.flow.component.dialog.Dialog actionDialog = new com.vaadin.flow.component.dialog.Dialog();
        actionDialog.setHeaderTitle("Task Actions - " + clickedItemId);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        Button btnSplit = new Button("Split Task", VaadinIcon.SCISSORS.create(), e -> {
            actionDialog.close();
            openSplitDialog(clickedItemId);
        });
        btnSplit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSplit.setWidthFull();

        Button btnMerge = new Button("Merge Selected Tasks", VaadinIcon.COMPRESS.create(), e -> {
            actionDialog.close();
            Set<Map<String, Object>> selectedRows = grid.getSelectedItems();
            if (selectedRows.size() < 2) {
                Notification.show("Pilih minimal 2 baris (dengan Ctrl+Click) untuk digabung!", 3000,
                        Notification.Position.MIDDLE);
                return;
            }
            executeMerge(selectedRows);
        });
        btnMerge.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnMerge.setWidthFull();

        // Only enable merge if > 1 selected
        Set<Map<String, Object>> currentGridSelection = grid.getSelectedItems();
        btnMerge.setEnabled(currentGridSelection.size() >= 2);

        dialogLayout.add(btnSplit, btnMerge);
        actionDialog.add(dialogLayout);
        actionDialog.open();
    }

    private void openSplitDialog(String itemId) {
        String pkCol = schedulerConfig.getColPrimaryKey();
        String qtyCol = schedulerConfig.getColQty();
        if (qtyCol == null) {
            Notification.show("Kolom Qty belum disetting di Config!", 3000, Notification.Position.MIDDLE);
            return;
        }

        Map<String, Object> targetRow = null;
        for (Map<String, Object> row : currentData) {
            Object id = row.get(pkCol);
            if (id != null && id.toString().equals(itemId)) {
                targetRow = row;
                break;
            }
        }
        if (targetRow == null)
            return;

        Object currentQtyObj = targetRow.get(qtyCol);
        if (currentQtyObj == null) {
            Notification.show("Qty baris ini kosong!", 3000, Notification.Position.MIDDLE);
            return;
        }

        double currentQty = Double.parseDouble(currentQtyObj.toString());

        com.vaadin.flow.component.dialog.Dialog splitDialog = new com.vaadin.flow.component.dialog.Dialog();
        splitDialog.setHeaderTitle("Split Task");

        com.vaadin.flow.component.textfield.IntegerField splitField = new com.vaadin.flow.component.textfield.IntegerField(
                "Qty Baru (yang dipisah)");
        splitField.setMin(1);
        splitField.setMax((int) currentQty - 1);
        splitField.setValue((int) currentQty / 2); // Default to half
        splitField.setStep(1);
        splitField.setWidthFull();
        splitField.setHelperText("Qty Awal: " + currentQty);

        final Map<String, Object> finalTargetRow = targetRow;
        final double finalCurrentQty = currentQty;

        Button btnSave = new Button("Proses Split", VaadinIcon.SCISSORS.create(), e -> {
            Integer splitQty = splitField.getValue();
            if (splitQty == null || splitQty <= 0 || splitQty >= finalCurrentQty) {
                Notification.show("Qty split tidak valid!", 3000, Notification.Position.MIDDLE);
                return;
            }
            splitDialog.close();
            executeSplit(finalTargetRow, (double) splitQty, finalCurrentQty);
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button btnCancel = new Button("Batal", e -> splitDialog.close());

        HorizontalLayout actions = new HorizontalLayout(btnCancel, btnSave);
        splitDialog.add(new VerticalLayout(splitField, actions));
        splitDialog.open();
    }

    private void executeSplit(Map<String, Object> targetRow, double splitQty, double currentQty) {
        String updateTable = schedulerConfig.getUpdateTable();
        String pkCol = schedulerConfig.getColPrimaryKey();
        String qtyCol = schedulerConfig.getColQty();
        String weightCol = null; // Always null to exclude weight calculation
        String splitGroupCol = schedulerConfig.getColSplitGroup();
        String groupIdCol = schedulerConfig.getColGroupId();
        String sequenceCol = schedulerConfig.getColSequence();
        String qtyProdCol = schedulerConfig.getColQtyProd();
        String pcsPerBoxCol = schedulerConfig.getColPcsPerBox();

        if (updateTable == null || pkCol == null || qtyCol == null) {
            Notification.show("Config Update Table/PK/Qty belum lengkap!", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Determine matching criteria for downstream cascade
        String targetSplitGroup = splitGroupCol != null && targetRow.get(splitGroupCol) != null
                ? targetRow.get(splitGroupCol).toString()
                : null;
        String targetGroupId = groupIdCol != null && targetRow.get(groupIdCol) != null
                ? targetRow.get(groupIdCol).toString()
                : null;
        Integer targetSequence = sequenceCol != null && targetRow.get(sequenceCol) != null
                ? Integer.parseInt(targetRow.get(sequenceCol).toString())
                : 0;

        // Find max split index for the current base prefix
        String basePrefix = (targetGroupId != null && !targetGroupId.trim().isEmpty() ? targetGroupId : "GRP") + "-S";
        int maxSplitIndex = 0;
        if (splitGroupCol != null) {
            for (Map<String, Object> row : currentData) {
                String sg = row.get(splitGroupCol) != null ? row.get(splitGroupCol).toString() : "";
                if (sg.startsWith(basePrefix)) {
                    try {
                        int idx = Integer.parseInt(sg.substring(basePrefix.length()));
                        if (idx > maxSplitIndex)
                            maxSplitIndex = idx;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Generate readable group IDs for this split transaction
        String newRemainSplitGroup = basePrefix + (maxSplitIndex + 1);
        String newSplitSplitGroup = basePrefix + (maxSplitIndex + 2);

        // Find all rows in currentData that match the criteria (including the target
        // row itself)
        List<Map<String, Object>> rowsToSplit = new ArrayList<>();
        for (Map<String, Object> row : currentData) {
            Integer rowSeq = sequenceCol != null && row.get(sequenceCol) != null
                    ? Integer.parseInt(row.get(sequenceCol).toString())
                    : 0;
            if (rowSeq >= targetSequence) {
                // If the target had a split group, match it
                if (targetSplitGroup != null && !targetSplitGroup.trim().isEmpty()) {
                    String rowSplitGroup = splitGroupCol != null && row.get(splitGroupCol) != null
                            ? row.get(splitGroupCol).toString()
                            : null;
                    if (targetSplitGroup.equals(rowSplitGroup)) {
                        rowsToSplit.add(row);
                    }
                }
                // Otherwise, match by original group ID (e.g. tsProductionOrderId)
                else if (targetGroupId != null && !targetGroupId.trim().isEmpty()) {
                    String rowGroupId = groupIdCol != null && row.get(groupIdCol) != null
                            ? row.get(groupIdCol).toString()
                            : null;
                    if (targetGroupId.equals(rowGroupId)) {
                        rowsToSplit.add(row);
                    }
                }
            }
        }

        // If no matching logic possible (e.g. no split_group and no group_id config),
        // just split the single row
        if (rowsToSplit.isEmpty()) {
            rowsToSplit.add(targetRow);
        }

        double splitRatio = splitQty / currentQty;
        double remainRatio = 1.0 - splitRatio;

        try {
            for (Map<String, Object> row : rowsToSplit) {
                Object rowPkVal = row.get(pkCol);

                // Get current row Qty & Weight
                double rowCurrentQty = Double.parseDouble(row.get(qtyCol).toString());
                double rowRemainQty, rowSplitQty;
                if (rowCurrentQty == currentQty) {
                    rowSplitQty = splitQty;
                    rowRemainQty = currentQty - splitQty;
                } else {
                    rowSplitQty = rowCurrentQty * splitRatio;
                    rowRemainQty = rowCurrentQty * remainRatio;
                }

                Double rowOriginalWeight = weightCol != null && row.get(weightCol) != null
                        ? Double.parseDouble(row.get(weightCol).toString())
                        : null;
                Double rowRemainWeight = null;
                Double rowSplitWeight = null;
                if (rowOriginalWeight != null && rowCurrentQty > 0) {
                    if (rowCurrentQty == currentQty) {
                        rowSplitWeight = rowOriginalWeight * (splitQty / currentQty);
                        rowRemainWeight = rowOriginalWeight - rowSplitWeight;
                    } else {
                        rowSplitWeight = rowOriginalWeight * splitRatio;
                        rowRemainWeight = rowOriginalWeight * remainRatio;
                    }
                }

                // Hitung Qty Prod (berdasarkan asumsi box diisi urut)
                Double rowOriginalQtyProd = qtyProdCol != null && row.get(qtyProdCol) != null
                        ? Double.parseDouble(row.get(qtyProdCol).toString())
                        : null;
                Double rowPcsPerBox = pcsPerBoxCol != null && row.get(pcsPerBoxCol) != null
                        ? Double.parseDouble(row.get(pcsPerBoxCol).toString())
                        : null;
                
                Double rowRemainQtyProd = null;
                Double rowSplitQtyProd = null;

                if (rowOriginalQtyProd != null && rowPcsPerBox != null && rowCurrentQty > 0) {
                    // Baris asli (remain) menempati urutan awal, sehingga diasumsikan box nya terisi penuh
                    // Batasnya adalah tidak melebihi rowOriginalQtyProd (total pcs saat ini)
                    rowRemainQtyProd = Math.min(rowRemainQty * rowPcsPerBox, rowOriginalQtyProd);
                    rowSplitQtyProd = rowOriginalQtyProd - rowRemainQtyProd;
                }

                // 1. Ambil data asli dari DB (agar kolom-kolom persis sama)
                String selectSql = "SELECT * FROM " + updateTable + " WHERE " + pkCol + " = ?";
                Map<String, Object> dbRow = jdbcTemplate.queryForMap(selectSql, rowPkVal);

                // Cari nama kolom asli (case-insensitive) dari dbRow
                String actualPkKey = pkCol;
                String actualQtyKey = qtyCol;
                String actualWeightKey = weightCol;
                String actualSplitGroupKey = splitGroupCol;
                String actualQtyProdKey = qtyProdCol;
                for (String key : dbRow.keySet()) {
                    if (key.equalsIgnoreCase(pkCol))
                        actualPkKey = key;
                    if (key.equalsIgnoreCase(qtyCol))
                        actualQtyKey = key;
                    if (weightCol != null && key.equalsIgnoreCase(weightCol))
                        actualWeightKey = key;
                    if (splitGroupCol != null && key.equalsIgnoreCase(splitGroupCol))
                        actualSplitGroupKey = key;
                    if (qtyProdCol != null && key.equalsIgnoreCase(qtyProdCol))
                        actualQtyProdKey = key;
                }

                if (actualSplitGroupKey == null && splitGroupCol != null) {
                    actualSplitGroupKey = splitGroupCol; // Fallback jika tidak ditemukan (misal karena tabel baru
                                                         // di-alter tapi belum ada isi)
                }

                // 2. Update baris asli (remainQty & remainWeight & splitGroup & qtyprod)
                AppUser currentUser = securityService.getCurrentUser();
                String currentUsername = currentUser != null ? currentUser.getUsername() : "system";
                java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

                String updateSql = "UPDATE " + updateTable + " SET " + qtyCol + " = ?, updateby = ?, updatedt = ?";
                List<Object> updateParams = new ArrayList<>();
                updateParams.add(rowRemainQty);
                updateParams.add(currentUsername);
                updateParams.add(currentTimestamp);
                if (weightCol != null) {
                    updateSql += ", " + weightCol + " = ?";
                    updateParams.add(rowRemainWeight);
                }
                if (qtyProdCol != null && rowRemainQtyProd != null) {
                    updateSql += ", " + qtyProdCol + " = ?";
                    updateParams.add(rowRemainQtyProd);
                }
                if (splitGroupCol != null) {
                    updateSql += ", " + splitGroupCol + " = ?";
                    updateParams.add(newRemainSplitGroup);
                }
                updateSql += " WHERE " + pkCol + " = ?";
                updateParams.add(rowPkVal);
                jdbcTemplate.update(updateSql, updateParams.toArray());

                // 3. Duplikasi baris (rowSplitQty & rowSplitWeight & splitGroup)
                // Generate PK Baru (Bypass masalah Auto-Increment Hibernate/DB yang tidak
                // ter-set)
                Object originalPkVal = dbRow.get(actualPkKey);
                Object newPkVal;
                if (originalPkVal instanceof Number) {
                    String maxSql = "SELECT MAX(" + actualPkKey + ") FROM " + updateTable;
                    Long maxId = jdbcTemplate.queryForObject(maxSql, Long.class);
                    newPkVal = (maxId != null ? maxId : 0) + 1;
                } else {
                    newPkVal = java.util.UUID.randomUUID().toString().substring(0, 8); // Random string PK
                }

                dbRow.put(actualPkKey, newPkVal);
                dbRow.put(actualQtyKey, rowSplitQty);
                if (weightCol != null && rowSplitWeight != null) {
                    dbRow.put(actualWeightKey, rowSplitWeight);
                }
                if (qtyProdCol != null && actualQtyProdKey != null && rowSplitQtyProd != null) {
                    dbRow.put(actualQtyProdKey, rowSplitQtyProd);
                }
                if (splitGroupCol != null && actualSplitGroupKey != null) {
                    dbRow.put(actualSplitGroupKey, newSplitSplitGroup);
                }
                
                AppUser currentUserInsert = securityService.getCurrentUser();
                String currentUsernameInsert = currentUserInsert != null ? currentUserInsert.getUsername() : "system";
                java.sql.Timestamp currentTimestampInsert = new java.sql.Timestamp(System.currentTimeMillis());
                
                // Cari key aktual untuk kolom audit
                String actualInputByKey = "inputby";
                String actualInputDtKey = "inputdt";
                String actualUpdateByKey = "updateby";
                String actualUpdateDtKey = "updatedt";
                for (String key : dbRow.keySet()) {
                    if (key.equalsIgnoreCase("inputby")) actualInputByKey = key;
                    if (key.equalsIgnoreCase("inputdt")) actualInputDtKey = key;
                    if (key.equalsIgnoreCase("updateby")) actualUpdateByKey = key;
                    if (key.equalsIgnoreCase("updatedt")) actualUpdateDtKey = key;
                }
                
                dbRow.put(actualInputByKey, currentUsernameInsert);
                dbRow.put(actualInputDtKey, currentTimestampInsert);
                dbRow.put(actualUpdateByKey, currentUsernameInsert);
                dbRow.put(actualUpdateDtKey, currentTimestampInsert);

                // Build Insert SQL
                List<String> columns = new ArrayList<>(dbRow.keySet());
                List<Object> values = new ArrayList<>();
                StringBuilder sqlCols = new StringBuilder();
                StringBuilder sqlVals = new StringBuilder();

                for (int i = 0; i < columns.size(); i++) {
                    String col = columns.get(i);
                    sqlCols.append(col);
                    sqlVals.append("?");
                    if (i < columns.size() - 1) {
                        sqlCols.append(", ");
                        sqlVals.append(", ");
                    }
                    values.add(dbRow.get(col));
                }

                String insertSql = "INSERT INTO " + updateTable + " (" + sqlCols.toString() + ") VALUES ("
                        + sqlVals.toString() + ")";
                jdbcTemplate.update(insertSql, values.toArray());
            }

            Notification.show("Berhasil memecah " + rowsToSplit.size() + " task secara berantai (Cascading Split)!",
                    4000, Notification.Position.MIDDLE);
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error splitting task: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void executeMerge(Set<Map<String, Object>> selectedRows) {
        String updateTable = schedulerConfig.getUpdateTable();
        String pkCol = schedulerConfig.getColPrimaryKey();
        String qtyCol = schedulerConfig.getColQty();
        String weightCol = schedulerConfig.getColWeight();

        if (updateTable == null || pkCol == null || qtyCol == null) {
            Notification.show("Config Update Table/PK/Qty belum lengkap!", 3000, Notification.Position.MIDDLE);
            return;
        }

        List<Map<String, Object>> rowsList = new ArrayList<>(selectedRows);
        Map<String, Object> parentRow = rowsList.get(0); // Jadikan baris pertama sebagai Induk
        Object parentPk = parentRow.get(pkCol);

        double totalQty = 0;
        double totalWeight = 0;
        boolean hasWeight = (weightCol != null);

        List<Object> pksToDelete = new ArrayList<>();

        for (int i = 0; i < rowsList.size(); i++) {
            Map<String, Object> r = rowsList.get(i);
            Object qObj = r.get(qtyCol);
            if (qObj != null)
                totalQty += Double.parseDouble(qObj.toString());

            if (hasWeight) {
                Object wObj = r.get(weightCol);
                if (wObj != null)
                    totalWeight += Double.parseDouble(wObj.toString());
            }

            if (i > 0) {
                pksToDelete.add(r.get(pkCol));
            }
        }

        try {
            // 1. Update Induk
            AppUser currentUser = securityService.getCurrentUser();
            String currentUsername = currentUser != null ? currentUser.getUsername() : "system";
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

            String updateSql = "UPDATE " + updateTable + " SET " + qtyCol + " = ?, updateby = ?, updatedt = ?";
            List<Object> updateParams = new ArrayList<>();
            updateParams.add(totalQty);
            updateParams.add(currentUsername);
            updateParams.add(currentTimestamp);
            if (hasWeight) {
                updateSql += ", " + weightCol + " = ?";
                updateParams.add(totalWeight);
            }
            updateSql += " WHERE " + pkCol + " = ?";
            updateParams.add(parentPk);
            jdbcTemplate.update(updateSql, updateParams.toArray());

            // 2. Delete sisa
            if (!pksToDelete.isEmpty()) {
                StringBuilder deleteSql = new StringBuilder("DELETE FROM " + updateTable + " WHERE " + pkCol + " IN (");
                for (int i = 0; i < pksToDelete.size(); i++) {
                    deleteSql.append("?");
                    if (i < pksToDelete.size() - 1)
                        deleteSql.append(", ");
                }
                deleteSql.append(")");
                jdbcTemplate.update(deleteSql.toString(), pksToDelete.toArray());
            }

            Notification.show("Tasks berhasil digabung!", 3000, Notification.Position.MIDDLE);
            refreshData();
        } catch (Exception ex) {
            Notification.show("Gagal menggabung data: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    // ================================================================
    // HANDLE ITEM MOVE (drag & drop)
    // ================================================================
    private void handleItemMove(String itemId, String newStart, String newEnd, String newGroup) {
        try {
            String pkCol = schedulerConfig.getColPrimaryKey();
            String resourceCol = schedulerConfig.getColResource();
            String startDateCol = schedulerConfig.getColStartDate();
            String qtyCol = "QTYBOX".equals(currentCapacityMode1)
                    ? schedulerConfig.getColQty()
                    : schedulerConfig.getColWeight();
            String capCol = "QTYBOX".equals(currentCapacityMode1)
                    ? schedulerConfig.getColMaxCapacity()
                    : schedulerConfig.getColMaxCapacityWeight();
            String groupIdCol = schedulerConfig.getColGroupId();
            String seqCol = schedulerConfig.getColSequence();
            String leadDayCol = schedulerConfig.getColLeadDay();
            String updateTable = schedulerConfig.getUpdateTable();
            String updateDateCol = schedulerConfig.getUpdateDateColumn();

            if (updateTable == null || updateDateCol == null) {
                Notification.show("Scheduler config: update_table and update_date_column are required",
                        5000, Notification.Position.MIDDLE);
                return;
            }

            // Parse new date
            String newDateStr = newStart != null && newStart.length() >= 10
                    ? newStart.substring(0, 10)
                    : null;
            if (newDateStr == null)
                return;
            LocalDate newDate = LocalDate.parse(newDateStr);

            // Find the dragged task data
            Map<String, Object> draggedTask = null;
            for (Map<String, Object> row : currentData) {
                Object id = row.get(pkCol);
                if (id != null && id.toString().equals(itemId)) {
                    draggedTask = row;
                    break;
                }
            }
            if (draggedTask == null)
                return;

            // Resource baru dari target drag
            String resource = newGroup != null ? newGroup : "";

            // Validasi: Cegah pindah antar Resource Group (Departemen) yang berbeda
            String resourceGroupCol = schedulerConfig.getColResourceGroup();
            if (resourceGroupCol != null) {
                String sourceGroup = draggedTask.get(resourceGroupCol) != null ? draggedTask.get(resourceGroupCol).toString() : "";
                String targetGroup = null;
                for (Map<String, Object> row : currentData) {
                    Object rVal = row.get(resourceCol);
                    if (rVal != null && rVal.toString().equals(resource)) {
                        targetGroup = row.get(resourceGroupCol) != null ? row.get(resourceGroupCol).toString() : "";
                        break;
                    }
                }
                
                if (targetGroup != null && !sourceGroup.equals(targetGroup)) {
                    Notification n = Notification.show("Tidak bisa memindahkan jadwal ke departemen/grup mesin yang berbeda (" + targetGroup + ")!", 5000, Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    refreshData(); // Revert
                    return;
                }
            }

            // --- CAPACITY VALIDATION ---
            double itemQty = 0;
            try {
                itemQty = Double.parseDouble(draggedTask.get(qtyCol).toString());
            } catch (Exception ignored) {
            }
            double maxCapacity = 0;
            try {
                maxCapacity = Double.parseDouble(draggedTask.get(capCol).toString());
            } catch (Exception ignored) {
            }

            // Calculate existing total at target date (excluding this item)
            double existingTotal = 0;
            try {
                String sumSql = "SELECT COALESCE(SUM(\"" + qtyCol + "\"), 0) FROM \"" + updateTable
                        + "\" WHERE \"" + resourceCol + "\" = ? AND \"" + updateDateCol + "\" = ?::date AND \""
                        + pkCol + "\" != ?";
                Object sumResult = jdbcTemplate.queryForObject(sumSql, Object.class,
                        resource, newDateStr, Integer.parseInt(itemId));
                existingTotal = Double.parseDouble(sumResult.toString());
            } catch (Exception ex) {
                System.err.println("Capacity check error: " + ex.getMessage());
            }

            double newTotal = existingTotal + itemQty;
            boolean isOvercapacity = maxCapacity > 0 && newTotal > maxCapacity;

            final Map<String, Object> finalDraggedTask = draggedTask;
            if (isOvercapacity) {
                // Show confirm dialog
                String capacityLabel = "QTYBOX".equals(currentCapacityMode1) ? "Qty Box" : "Weight (kg)";
                ConfirmDialog confirmDlg = new ConfirmDialog();
                confirmDlg.setHeader("⚠️ Peringatan Kapasitas");
                confirmDlg.setText(String.format(
                        "Mesin: %s\nTanggal: %s\nTotal %s: %.0f / %.0f (MELEBIHI KAPASITAS)\n\nApakah Anda yakin ingin melanjutkan?",
                        resource, newDateStr, capacityLabel, newTotal, maxCapacity));
                confirmDlg.setCancelable(true);
                confirmDlg.setCancelText("Batalkan");
                confirmDlg.setConfirmText("Ya, Lanjutkan");
                confirmDlg.setConfirmButtonTheme("error primary");

                confirmDlg.addConfirmListener(event -> {
                    executeDragUpdate(itemId, newDate, resource, finalDraggedTask);
                });
                confirmDlg.addCancelListener(event -> {
                    // Revert: refresh data to restore original positions
                    refreshData();
                });

                confirmDlg.open();
                return;
            }

            // No overcapacity — proceed directly
            executeDragUpdate(itemId, newDate, resource, finalDraggedTask);

        } catch (Exception e) {
            Notification n = Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            refreshData();
        }
    }

    // ================================================================
    // EXECUTE DRAG UPDATE (save + cascade shift)
    // ================================================================
    private void executeDragUpdate(String itemId, LocalDate newDate, String newResource, Map<String, Object> draggedTask) {
        try {
            String pkCol = schedulerConfig.getColPrimaryKey();
            String dependencyIdCol = schedulerConfig.getColDependencyId(); // Changed from groupIdCol
            String splitGroupCol = schedulerConfig.getColSplitGroup();
            String seqCol = schedulerConfig.getColSequence();
            String leadDayCol = schedulerConfig.getColLeadDay();
            String updateTable = schedulerConfig.getUpdateTable();
            String updateDateCol = schedulerConfig.getUpdateDateColumn();

            // Get original date of dragged task before moving
            Object oldDateObj = draggedTask.get(updateDateCol);
            LocalDate oldDate = null;
            if (oldDateObj != null) {
                try {
                    oldDate = LocalDate.parse(oldDateObj.toString().substring(0, 10));
                } catch (Exception e) {
                }
            }

            long daysShifted = 0;
            if (oldDate != null) {
                daysShifted = java.time.temporal.ChronoUnit.DAYS.between(oldDate, newDate);
            }

            // 1. UPDATE the dragged task's date
            AppUser currentUser = securityService.getCurrentUser();
            String currentUsername = currentUser != null ? currentUser.getUsername() : "system";
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

            String resourceCol = schedulerConfig.getColResource();
            String updateResourceCol = schedulerConfig.getUpdateResourceColumn();
            
            Object newResourceValToUpdate = newResource;
            if (updateResourceCol != null && !updateResourceCol.trim().isEmpty() && resourceCol != null) {
                // Cari ID aktual berdasarkan nama resource di data cache
                for (Map<String, Object> row : currentData) {
                    Object rVal = row.get(resourceCol);
                    if (rVal != null && rVal.toString().equals(newResource)) {
                        newResourceValToUpdate = row.get(updateResourceCol);
                        break;
                    }
                }
            }

            String updateSql = "UPDATE " + updateTable + " SET " + updateDateCol
                    + " = ?::date, updateby = ?, updatedt = ?";
                    
            List<Object> updateParams = new ArrayList<>();
            updateParams.add(newDate.toString());
            updateParams.add(currentUsername);
            updateParams.add(currentTimestamp);

            if (updateResourceCol != null && !updateResourceCol.trim().isEmpty()) {
                updateSql += ", " + updateResourceCol + " = ?";
                updateParams.add(newResourceValToUpdate);
            } else if (resourceCol != null && newResource != null) {
                updateSql += ", " + resourceCol + " = ?";
                updateParams.add(newResource);
            }
            
            updateSql += " WHERE " + pkCol + " = ?";
            updateParams.add(draggedTask.get(pkCol));
            
            jdbcTemplate.update(updateSql, updateParams.toArray());

            // 2. CASCADE SHIFT — move related tasks by the same delta days
            if (dependencyIdCol != null && seqCol != null) {
                Object seqVal = draggedTask.get(seqCol);
                Object splitGroupVal = (splitGroupCol != null) ? draggedTask.get(splitGroupCol) : null;
                Object dependencyIdVal = draggedTask.get(dependencyIdCol);

                String targetMatchCol = null;
                Object targetMatchVal = null;

                if (splitGroupVal != null && !splitGroupVal.toString().trim().isEmpty()) {
                    targetMatchCol = splitGroupCol;
                    targetMatchVal = splitGroupVal;
                } else if (dependencyIdVal != null) {
                    targetMatchCol = dependencyIdCol;
                    targetMatchVal = dependencyIdVal;
                }

                Object oldResourceObj = draggedTask.get(resourceCol);
                boolean resourceChanged = resourceCol != null && newResource != null && !newResource.equals(oldResourceObj != null ? oldResourceObj.toString() : "");

                if (targetMatchCol != null && seqVal != null && (daysShifted != 0 || resourceChanged)) {
                    // Find all tasks with same target match and sequence > current
                    String findSql = "SELECT " + pkCol + ", " + seqCol + ", " + updateDateCol
                            + " FROM (" + schedulerConfig.getSchedulerQuery() + ") AS sq WHERE "
                            + targetMatchCol + " = ? AND "
                            + seqCol + " > ? ORDER BY " + seqCol + " ASC";

                    List<Map<String, Object>> relatedTasks = jdbcTemplate.queryForList(findSql,
                            targetMatchVal, seqVal);

                    for (Map<String, Object> related : relatedTasks) {
                        Object relatedDateObj = related.get(updateDateCol);
                        if (relatedDateObj != null) {
                            try {
                                LocalDate currentRelatedDate = LocalDate
                                        .parse(relatedDateObj.toString().substring(0, 10));
                                LocalDate cascadeDate = currentRelatedDate.plusDays(daysShifted);
                                Object relatedId = related.get(pkCol);
                                
                                String cascadeSql = "UPDATE " + updateTable + " SET " + updateDateCol
                                        + " = ?::date, updateby = ?, updatedt = ? WHERE " + pkCol + " = ?";
                                
                                List<Object> cascadeParams = new ArrayList<>();
                                cascadeParams.add(cascadeDate.toString());
                                cascadeParams.add(currentUsername);
                                cascadeParams.add(currentTimestamp);
                                cascadeParams.add(relatedId);
                                
                                jdbcTemplate.update(cascadeSql, cascadeParams.toArray());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    if (!relatedTasks.isEmpty()) {
                        Notification.show("✅ Jadwal digeser " + daysShifted + " hari beserta " + relatedTasks.size()
                                + " task terkait", 3000, Notification.Position.BOTTOM_END);
                    }
                }
            }

            if (currentData.stream().noneMatch(r -> false)) { // always show
                Notification.show("✅ Jadwal task " + itemId + " diperbarui ke " + newDate,
                        3000, Notification.Position.BOTTOM_END);
            }

            refreshData();

        } catch (Exception e) {
            Notification n = Notification.show("Error updating schedule: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            refreshData();
        }
    }
}
