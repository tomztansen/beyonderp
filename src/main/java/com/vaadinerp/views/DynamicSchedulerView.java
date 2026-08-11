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
    private boolean isWeeklyView = false;
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
    private LocalDate chartViewStartDate = LocalDate.now();
    private boolean isGridCollapsed = false;

    private List<Map<String, Object>> currentData = new ArrayList<>();
    private java.util.Set<Map<String, Object>> modifiedRows = new java.util.HashSet<>();
    private java.util.Set<String> currentLateSpks = new java.util.HashSet<>();
    private java.util.Set<LocalDate> holidaySet = new java.util.HashSet<>();
    private Button btnSaveEdits;
    private com.vaadin.flow.component.checkbox.Checkbox chkHideUnassigned;

    @SuppressWarnings("unused")
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
        getStyle().set("background-color", "var(--lumo-base-color)");
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    private void loadHolidayData() {
        holidaySet.clear();
        if (schedulerConfig != null) {
            String hTable = schedulerConfig.getHolidayTable();
            String hCol = schedulerConfig.getHolidayDateCol();
            if (hTable != null && !hTable.trim().isEmpty() && hCol != null && !hCol.trim().isEmpty()) {
                try {
                    String sql = "SELECT " + hCol + " FROM " + hTable;
                    List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
                    for (Map<String, Object> row : rows) {
                        Object val = row.get(hCol);
                        if (val instanceof java.sql.Date) {
                            holidaySet.add(((java.sql.Date) val).toLocalDate());
                        } else if (val instanceof java.sql.Timestamp) {
                            holidaySet.add(((java.sql.Timestamp) val).toLocalDateTime().toLocalDate());
                        } else if (val != null) {
                            try {
                                holidaySet.add(LocalDate.parse(val.toString()));
                            } catch (Exception e) {}
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Gagal meload data hari libur: " + e.getMessage());
                }
            }
        }
    }

    private boolean isHolidayOrSunday(LocalDate date) {
        if (date == null) return false;
        if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) return true;
        return holidaySet.contains(date);
    }

    private java.time.LocalDateTime getNextWorkingTime(java.time.LocalDateTime time) {
        if (time == null) return null;
        java.time.LocalDateTime validTime = time;
        while (isHolidayOrSunday(validTime.toLocalDate())) {
            validTime = validTime.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        }
        return validTime;
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
                        "  style.innerHTML = '.vis-item.is-late { background-color: #ef4444 !important; border-color: #b91c1c !important; color: white !important; font-weight: bold !important; } .vis-item.is-late.vis-selected { border-width: 2px !important; border-color: #7f1d1d !important; background-color: #b91c1c !important; }';"
                        +
                        "  document.head.appendChild(style);" +
                        "}");

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

        HorizontalLayout legendLayout = new HorizontalLayout();
        legendLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        legendLayout.getStyle().set("margin-right", "15px").set("gap", "6px");

        Span boxHoliday = new Span();
        boxHoliday.setWidth("12px");
        boxHoliday.setHeight("12px");
        boxHoliday.getStyle().set("background-color", "rgba(239, 68, 68, 0.5)").set("border-radius", "2px");
        Span lblHoliday = new Span("Public Holiday");
        lblHoliday.getStyle().set("font-size", "11px").set("color", "#64748b").set("margin-right", "8px");

        Span boxSunday = new Span();
        boxSunday.setWidth("12px");
        boxSunday.setHeight("12px");
        boxSunday.getStyle().set("background-color", "rgba(245, 158, 11, 0.5)").set("border-radius", "2px");
        Span lblSunday = new Span("Sunday");
        lblSunday.getStyle().set("font-size", "11px").set("color", "#64748b");

        legendLayout.add(boxHoliday, lblHoliday, boxSunday, lblSunday);

        timelineHeader.add(timelineTitle, legendLayout, timelineGroupFilter, timelineResourceFilter);

        timeline = new VisTimeline();
        timeline.setSizeFull();
        timeline.setItemMoveListener(this::handleItemMove);
        timeline.setItemsSelectedListener(this::handleItemsSelected);
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

        Button btnChartPrev1 = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> {
            chartViewStartDate = chartViewStartDate.minusDays(1);
            updateAllCharts();
        });
        btnChartPrev1.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        btnChartPrev1.setTooltipText("◀ Mundur 1 Hari");

        Button btnChartNext1 = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> {
            chartViewStartDate = chartViewStartDate.plusDays(1);
            updateAllCharts();
        });
        btnChartNext1.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        btnChartNext1.setTooltipText("▶ Maju 1 Hari");

        header1.add(title1, btnChartPrev1, btnChartNext1, groupFilterCombo1, resourceFilterCombo1, capacityModeCombo1);
        capacityChart1 = new ApexCapacityChart();
        capacityChart1.setSizeFull();
        capacityChart1.setWeeklyView(isWeeklyView);
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

        Button btnChartPrev2 = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> {
            chartViewStartDate = chartViewStartDate.minusDays(1);
            updateAllCharts();
        });
        btnChartPrev2.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        btnChartPrev2.setTooltipText("◀ Mundur 1 Hari");

        Button btnChartNext2 = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> {
            chartViewStartDate = chartViewStartDate.plusDays(1);
            updateAllCharts();
        });
        btnChartNext2.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        btnChartNext2.setTooltipText("▶ Maju 1 Hari");

        header2.add(title2, btnChartPrev2, btnChartNext2, groupFilterCombo2, resourceFilterCombo2, capacityModeCombo2);
        capacityChart2 = new ApexCapacityChart();
        capacityChart2.setSizeFull();
        capacityChart2.setWeeklyView(isWeeklyView);
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

        Button btnToggleGrid = new Button(VaadinIcon.CARET_LEFT.create());
        btnToggleGrid.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        btnToggleGrid.setTooltipText("Collapse/Expand Grid");

        btnToggleGrid.addClickListener(e -> {
            if (!isGridCollapsed) {
                // Collapse
                grid.setVisible(false);
                gridTitle.setVisible(false);
                btnSaveEdits.setVisible(false);
                outerSplit.setSplitterPosition(3);
                btnToggleGrid.setIcon(VaadinIcon.CARET_RIGHT.create());
                isGridCollapsed = true;
            } else {
                // Expand
                grid.setVisible(true);
                gridTitle.setVisible(true);
                btnSaveEdits.setVisible(true);
                outerSplit.setSplitterPosition(25);
                btnToggleGrid.setIcon(VaadinIcon.CARET_LEFT.create());
                isGridCollapsed = false;
            }
        });

        grid = new Grid<>() {
            @Override
            public void setDataProvider(
                    com.vaadin.flow.data.provider.DataProvider<Map<String, Object>, ?> dataProvider) {
                if (dataProvider instanceof com.vaadin.flow.data.provider.ListDataProvider) {
                    com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>> ldp = (com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>>) dataProvider;

                    com.vaadin.flow.data.provider.ListDataProvider<Map<String, Object>> newDp = new com.vaadin.flow.data.provider.ListDataProvider<>(
                            ldp.getItems()) {
                        @Override
                        public Object getId(Map<String, Object> item) {
                            if (item == null)
                                return "null-item-" + java.util.UUID.randomUUID().toString();
                            return System.identityHashCode(item);
                        }
                    };
                    super.setDataProvider(newDp);
                } else {
                    super.setDataProvider(dataProvider);
                }
            }
        };
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        buildGridColumns();

        com.vaadin.flow.component.html.Anchor btnExportExcel = com.vaadinerp.components.StandardGridUtils
                .createExportExcelButton(grid, formCode != null ? formCode + "_export" : "scheduler_export",
                        colGetterMap);

        gridToolbar.add(btnToggleGrid, gridTitle, btnExportExcel, btnSaveEdits);

        gridPanel.add(gridToolbar, grid);

        outerSplit.addToPrimary(gridPanel);
        outerSplit.addToSecondary(mainSplit);

        add(toolbar, outerSplit);

        // Fetch data
        refreshData();

        // Set initial visual timeline zoom: Hari Ini + 7 hingga + 14
        timeline.setWindow(LocalDate.now().plusDays(7).toString(), LocalDate.now().plusDays(14).toString());
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
        startDateFilter.setValue(LocalDate.now().minusMonths(1));
        startDateFilter.setWidth("140px");

        endDateFilter = new com.vaadin.flow.component.datepicker.DatePicker();
        endDateFilter.setPlaceholder("End Date");
        endDateFilter.setValue(LocalDate.now().plusMonths(1));
        endDateFilter.setWidth("140px");

        Button btnShiftLeft = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> {
            if (startDateFilter.getValue() != null)
                startDateFilter.setValue(startDateFilter.getValue().minusDays(1));
            if (endDateFilter.getValue() != null)
                endDateFilter.setValue(endDateFilter.getValue().minusDays(1));
            refreshData();
        });
        btnShiftLeft.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        btnShiftLeft.setTooltipText("Mundur 1 Hari");

        Button btnShiftRight = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> {
            if (startDateFilter.getValue() != null)
                startDateFilter.setValue(startDateFilter.getValue().plusDays(1));
            if (endDateFilter.getValue() != null)
                endDateFilter.setValue(endDateFilter.getValue().plusDays(1));
            refreshData();
        });
        btnShiftRight.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        btnShiftRight.setTooltipText("Maju 1 Hari");

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

        Button btnFullScreen = new Button("Full Screen", VaadinIcon.EXPAND_FULL.create(), e -> {
            getElement().executeJs(
                    "const view = this;" +
                            "if (!document.fullscreenElement) { " +
                            "  document.documentElement.requestFullscreen().then(() => { " +
                            "    view.style.position = 'fixed';" +
                            "    view.style.top = '0';" +
                            "    view.style.left = '0';" +
                            "    view.style.width = '100vw';" +
                            "    view.style.height = '100vh';" +
                            "    view.style.zIndex = '99';" +
                            "    const handler = () => {" +
                            "      if (!document.fullscreenElement) {" +
                            "        view.style.position = '';" +
                            "        view.style.top = '';" +
                            "        view.style.left = '';" +
                            "        view.style.width = '';" +
                            "        view.style.height = '';" +
                            "        view.style.zIndex = '';" +
                            "        document.removeEventListener('fullscreenchange', handler);" +
                            "      }" +
                            "    };" +
                            "    document.addEventListener('fullscreenchange', handler);" +
                            "  }).catch(err => console.error('Error enabling full-screen: ' + err.message)); " +
                            "} else { " +
                            "  if (document.exitFullscreen) { document.exitFullscreen(); } " +
                            "}");
        });
        btnFullScreen.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnFullScreen.setTooltipText("Toggle Full Screen");

        chkHideUnassigned = new com.vaadin.flow.component.checkbox.Checkbox();
        chkHideUnassigned.setValue(true);
        chkHideUnassigned.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refreshData();
            }
        });

        com.vaadin.flow.component.html.Span chkLabel = new com.vaadin.flow.component.html.Span(
                "Hide Unassigned Facility");
        chkLabel.getStyle().set("margin-left", "8px").set("font-size", "var(--lumo-font-size-m)");
        chkLabel.getStyle().set("cursor", "pointer");
        chkLabel.addClickListener(e -> chkHideUnassigned.setValue(!chkHideUnassigned.getValue()));

        com.vaadin.flow.component.orderedlayout.HorizontalLayout chkWrapper = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(
                chkHideUnassigned, chkLabel);
        chkWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        chkWrapper.setSpacing(false);
        chkWrapper.getStyle().set("margin-left", "8px");

        // View Mode Toggle
        viewModeCombo = new ComboBox<>();
        viewModeCombo.setItems("Harian (Daily)", "Mingguan (Weekly)");
        viewModeCombo.setValue("Harian (Daily)");
        viewModeCombo.setWidth("160px");
        viewModeCombo.addThemeVariants(com.vaadin.flow.component.combobox.ComboBoxVariant.LUMO_SMALL);
        viewModeCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                isWeeklyView = "Mingguan (Weekly)".equals(e.getValue());
                timeline.setTimelineScale(isWeeklyView ? "weekly" : "daily");
                capacityChart1.setWeeklyView(isWeeklyView);
                capacityChart2.setWeeklyView(isWeeklyView);
                updateAllCharts();
            }
        });

        toolbar.add(btnShiftLeft, startDateFilter, endDateFilter, btnShiftRight, btnRefresh, btnZoomIn, btnZoomOut,
                btnFitAll, btnFullScreen, viewModeCombo,
                chkWrapper);
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

        // Flag to prevent binder from writing null to LOV fields during editor row
        // switch
        final boolean[] isEditorSwitching = { false };

        Map<String, com.vaadin.flow.component.Component> editorComponents = new HashMap<>();
        Map<Grid.Column<Map<String, Object>>, String> columnToFieldNameMap = new HashMap<>();

        java.util.function.BiFunction<Map<String, Object>, String, Object> getCaseInsensitiveVal = (map, key) -> {
            if (map == null || key == null)
                return null;
            if (map.containsKey(key))
                return map.get(key);
            String normalizedKey = key.replace("_", "").toLowerCase();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)
                        || entry.getKey().replace("_", "").toLowerCase().equals(normalizedKey)) {
                    return entry.getValue();
                }
            }
            return null;
        };

        java.util.function.BiConsumer<com.vaadin.flow.component.Component, com.vaadinerp.components.FilterCondition> applyFilter = (
                comp, cond) -> {
            if (comp instanceof com.vaadinerp.components.LovComboBox lovCombo) {
                lovCombo.setFilterValue(cond);
            } else if (comp instanceof com.vaadinerp.components.LovSelect lovSelect) {
                lovSelect.setFilterValue(cond);
            } else if (comp instanceof com.vaadinerp.components.BandboxField bandbox) {
                ((com.vaadinerp.components.BandboxField<?, ?>) bandbox).setFilterValue(cond);
            }
        };

        java.util.function.Consumer<com.vaadin.flow.component.Component> clearComp = (comp) -> {
            if (comp instanceof com.vaadinerp.components.LovComboBox lovCombo) {
                lovCombo.clear();
            } else if (comp instanceof com.vaadinerp.components.LovSelect lovSelect) {
                lovSelect.clear();
            } else if (comp instanceof com.vaadinerp.components.BandboxField bandbox) {
                ((com.vaadinerp.components.BandboxField<?, ?>) bandbox).clear();
            }
        };

        grid.addItemClickListener(e -> {
            try {
                if (e.getItem() == null) {
                    return;
                }
                if (grid.getEditor().isOpen()) {
                    if (grid.getEditor().getItem() == e.getItem()) {
                        return; // already editing this item
                    }
                    // Set flag BEFORE cancel to prevent binder from writing null to LOV fields
                    isEditorSwitching[0] = true;
                    grid.getEditor().cancel();
                }

                Map<String, Object> row = e.getItem();
                // Apply cross-field LOV filters BEFORE entering edit mode to prevent ComboBox
                // value clearing
                for (FieldMeta field : formMeta.getFields()) {
                    if (field.isShowInGrid() && !field.isReadonly()) {
                        com.vaadin.flow.component.Component targetComp = editorComponents.get(field.getFieldName());
                        if (targetComp != null) {
                            java.util.List<com.vaadinerp.meta.FieldFilterMeta> filters = field.getFilters();
                            if (filters != null && !filters.isEmpty()) {
                                for (com.vaadinerp.meta.FieldFilterMeta filter : filters) {
                                    if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                                        String lookupKey = filter.getSourceName();
                                        if (lookupKey != null && !lookupKey.trim().isEmpty()) {
                                            Object val = getCaseInsensitiveVal.apply(row, lookupKey.trim());
                                            com.vaadinerp.components.FilterCondition cond = new com.vaadinerp.components.FilterCondition(
                                                    String.valueOf(filter.getId()), filter.getFilterColumn(), val,
                                                    filter.getLogicalOperator(), filter.getComparisonOperator());
                                            applyFilter.accept(targetComp, cond);
                                        }
                                    } else if ("STATIC".equalsIgnoreCase(filter.getSourceType())) {
                                        Object staticVal = filter.getSourceName();
                                        String lookupKey = staticVal != null ? staticVal.toString() : "";
                                        if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.")) {
                                            lookupKey = lookupKey.replaceAll("[\"']", "")
                                                    .substring(lookupKey.indexOf("header.") + "header.".length())
                                                    .trim();
                                            staticVal = getCaseInsensitiveVal.apply(row, lookupKey);
                                        }
                                        com.vaadinerp.components.FilterCondition cond = new com.vaadinerp.components.FilterCondition(
                                                String.valueOf(filter.getId()), filter.getFilterColumn(), staticVal,
                                                filter.getLogicalOperator(), filter.getComparisonOperator());
                                        applyFilter.accept(targetComp, cond);
                                    }
                                }
                            }
                        }
                    }
                }

                grid.getEditor().editItem(e.getItem());

                // Manually populate editor components from row data (binder no longer does
                // this)
                for (Map.Entry<String, com.vaadin.flow.component.Component> compEntry : editorComponents.entrySet()) {
                    String fn = compEntry.getKey();
                    com.vaadin.flow.component.Component comp = compEntry.getValue();
                    if (comp instanceof com.vaadin.flow.component.HasValue) {
                        @SuppressWarnings("unchecked")
                        com.vaadin.flow.component.HasValue<?, Object> hv = (com.vaadin.flow.component.HasValue<?, Object>) comp;
                        Object val = getCaseInsensitiveVal.apply(row, fn);
                        try {
                            if (val == null) {
                                hv.clear();
                            } else {
                                // Convert to String for text-based components (LOV, ComboBox, TextField, etc.)
                                if (comp instanceof com.vaadinerp.components.LovComboBox
                                        || comp instanceof com.vaadinerp.components.BandboxField
                                        || comp instanceof com.vaadinerp.components.LovSelect
                                        || comp instanceof com.vaadin.flow.component.textfield.TextField
                                        || comp instanceof com.vaadin.flow.component.textfield.TextArea
                                        || comp instanceof com.vaadin.flow.component.combobox.ComboBox
                                        || comp instanceof com.vaadin.flow.component.select.Select) {
                                    hv.setValue(val.toString());
                                } else if (comp instanceof com.vaadin.flow.component.textfield.IntegerField
                                        || comp instanceof com.vaadinerp.components.FormattedIntegerField) {
                                    hv.setValue(val instanceof Number ? ((Number) val).intValue()
                                            : Integer.parseInt(val.toString()));
                                } else if (comp instanceof com.vaadin.flow.component.textfield.BigDecimalField
                                        || comp instanceof com.vaadinerp.components.FormattedBigDecimalField) {
                                    hv.setValue(val instanceof java.math.BigDecimal ? val
                                            : new java.math.BigDecimal(val.toString()));
                                } else if (comp instanceof com.vaadin.flow.component.textfield.NumberField) {
                                    hv.setValue(val instanceof Number ? ((Number) val).doubleValue()
                                            : Double.parseDouble(val.toString()));
                                } else if (comp instanceof com.vaadin.flow.component.datepicker.DatePicker) {
                                    if (val instanceof java.time.LocalDate)
                                        hv.setValue(val);
                                    else if (val instanceof java.sql.Date)
                                        hv.setValue(((java.sql.Date) val).toLocalDate());
                                    else
                                        hv.setValue(java.time.LocalDate.parse(val.toString()));
                                } else if (comp instanceof com.vaadin.flow.component.datetimepicker.DateTimePicker) {
                                    if (val instanceof java.time.LocalDateTime)
                                        hv.setValue(val);
                                    else if (val instanceof java.sql.Timestamp)
                                        hv.setValue(((java.sql.Timestamp) val).toLocalDateTime());
                                    else
                                        hv.setValue(java.time.LocalDateTime.parse(val.toString()));
                                } else if (comp instanceof com.vaadin.flow.component.timepicker.TimePicker) {
                                    if (val instanceof java.time.LocalTime)
                                        hv.setValue(val);
                                    else if (val instanceof java.sql.Time)
                                        hv.setValue(((java.sql.Time) val).toLocalTime());
                                    else
                                        hv.setValue(java.time.LocalTime.parse(val.toString()));
                                } else if (comp instanceof com.vaadin.flow.component.checkbox.Checkbox) {
                                    if (val instanceof Boolean)
                                        hv.setValue(val);
                                    else if (val instanceof Number)
                                        hv.setValue(((Number) val).intValue() != 0);
                                    else {
                                        String s = val.toString().trim().toLowerCase();
                                        hv.setValue(
                                                "true".equals(s) || "1".equals(s) || "t".equals(s) || "yes".equals(s));
                                    }
                                } else {
                                    hv.setValue(val);
                                }
                            }
                        } catch (Exception ignored) {
                            // Type conversion error - leave component as-is
                        }
                    }
                }
                isEditorSwitching[0] = false;
            } catch (Exception ex) {
                com.vaadin.flow.component.notification.Notification.show("Gagal membuka editor: " + ex.getMessage(),
                        5000, com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
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
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            if (entry.getKey().equalsIgnoreCase(fieldName)) {
                                valObj = entry.getValue();
                                break;
                            }
                        }
                    }
                    String formatted = com.vaadinerp.components.ComponentFactory.formatFieldValueWithLov(field, valObj,
                            dataService);
                    return formatted != null ? formatted : "";
                };

                Grid.Column<Map<String, Object>> col = grid.addColumn(valueGetter::apply)
                        .setHeader(field.getFieldLabel())
                        .setKey(fieldName)
                        .setAutoWidth(true)
                        .setResizable(true)
                        .setSortable(field.isSortable());

                columnToFieldNameMap.put(col, fieldName);

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

                        if (val1 == null && val2 == null)
                            return 0;
                        if (val1 == null)
                            return -1;
                        if (val2 == null)
                            return 1;

                        if (val1 instanceof Comparable && val2 instanceof Comparable
                                && val1.getClass().equals(val2.getClass())) {
                            return ((Comparable<Object>) val1).compareTo(val2);
                        }

                        // Fallback to string comparison for numbers that might be different types
                        // (e.g., Integer vs BigDecimal)
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
                    // Prevent editing core scheduler identifiers via grid inline edit to avoid sync
                    // issues with VisTimeline
                    boolean isCoreField = fieldName.equalsIgnoreCase(schedulerConfig.getColPrimaryKey()) ||
                            fieldName.equalsIgnoreCase(schedulerConfig.getColStartDate()) ||
                            fieldName.equalsIgnoreCase(schedulerConfig.getColEndDate()) ||
                            fieldName.equalsIgnoreCase(schedulerConfig.getColResource());

                    if (!isCoreField) {
                        com.vaadin.flow.component.Component editorComp = com.vaadinerp.components.ComponentFactory
                                .create(field, dataService, null, true);
                        if (editorComp != null) {
                            editorComponents.put(fieldName, editorComp);
                        }
                        // Use manual approach (like SubformGridField) instead of
                        // binder.forField().bind()
                        // to prevent Vaadin binder from auto-writing null to data map on ComboBox blur
                        col.setEditorComponent(editorComp);
                        if (editorComp instanceof com.vaadin.flow.component.HasValue) {
                            @SuppressWarnings("unchecked")
                            com.vaadin.flow.component.HasValue<?, Object> hasValueComp = (com.vaadin.flow.component.HasValue<?, Object>) editorComp;
                            final String fTypeForVcl = fType;
                            hasValueComp.addValueChangeListener(ev -> {
                                if (!grid.getEditor().isOpen())
                                    return;
                                Map<String, Object> editingItem = grid.getEditor().getItem();
                                if (editingItem == null)
                                    return;
                                Object val = ev.getValue();
                                Object oldVal = editingItem.get(fieldName);
                                if (oldVal == null) {
                                    for (String key : editingItem.keySet()) {
                                        if (key.equalsIgnoreCase(fieldName)) {
                                            oldVal = editingItem.get(key);
                                            break;
                                        }
                                    }
                                }
                                if (!java.util.Objects.equals(oldVal, val)) {
                                    // Skip null writes during editor row switch (ComboBox blur side effect)
                                    if (isEditorSwitching[0] && val == null && oldVal != null) {
                                        return;
                                    }
                                    editingItem.put(fieldName, val);
                                    modifiedRows.add(editingItem);
                                    btnSaveEdits.setEnabled(true);
                                }
                            });
                        }
                    }
                }
            }
        }

        // Support cross-field LOV filtering in Grid Editor
        for (FieldMeta field : sortedFields) {
            if (field.isShowInGrid() && !field.isReadonly()) {
                com.vaadin.flow.component.Component targetComp = editorComponents.get(field.getFieldName());
                if (targetComp != null) {
                    java.util.List<com.vaadinerp.meta.FieldFilterMeta> filters = field.getFilters();
                    if (filters != null && !filters.isEmpty()) {
                        for (com.vaadinerp.meta.FieldFilterMeta filter : filters) {
                            if ("FIELD".equalsIgnoreCase(filter.getSourceType())) {
                                String lookupKey = filter.getSourceName();
                                if (lookupKey != null && !lookupKey.trim().isEmpty()) {
                                    com.vaadin.flow.component.Component sourceComp = editorComponents
                                            .get(lookupKey.trim());
                                    if (sourceComp instanceof com.vaadin.flow.component.HasValue<?, ?> hv) {
                                        hv.addValueChangeListener(ev -> {
                                            if (ev.isFromClient()) {
                                                Object val = ev.getValue();
                                                com.vaadinerp.components.FilterCondition cond = new com.vaadinerp.components.FilterCondition(
                                                        String.valueOf(filter.getId()), filter.getFilterColumn(), val,
                                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                                applyFilter.accept(targetComp, cond);
                                                clearComp.accept(targetComp);
                                            }
                                        });
                                    }
                                }
                            } else if ("STATIC".equalsIgnoreCase(filter.getSourceType())) {
                                Object staticVal = filter.getSourceName();
                                String lookupKey = staticVal != null ? staticVal.toString() : "";
                                if (lookupKey.startsWith("header.") || lookupKey.startsWith("\"header.")) {
                                    lookupKey = lookupKey.replaceAll("[\"']", "")
                                            .substring(lookupKey.indexOf("header.") + "header.".length()).trim();
                                    com.vaadin.flow.component.Component sourceComp = editorComponents.get(lookupKey);
                                    if (sourceComp instanceof com.vaadin.flow.component.HasValue<?, ?> hv) {
                                        hv.addValueChangeListener(ev -> {
                                            if (ev.isFromClient()) {
                                                Object val = ev.getValue();
                                                com.vaadinerp.components.FilterCondition cond = new com.vaadinerp.components.FilterCondition(
                                                        String.valueOf(filter.getId()), filter.getFilterColumn(), val,
                                                        filter.getLogicalOperator(), filter.getComparisonOperator());
                                                applyFilter.accept(targetComp, cond);
                                                clearComp.accept(targetComp);
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Enable column reordering and save state
        grid.setColumnReorderingAllowed(true);
        grid.addColumnReorderListener(event -> {
            java.util.List<Grid.Column<Map<String, Object>>> newOrder = event.getColumns();
            java.util.List<String> orderedFieldNames = new java.util.ArrayList<>();
            for (Grid.Column<Map<String, Object>> c : newOrder) {
                String fName = columnToFieldNameMap.get(c);
                if (fName != null) {
                    orderedFieldNames.add(fName);
                }
            }
            try {
                dataService.saveUserGridOrder(formCode != null ? formCode : "schedulerGrid", "mainGrid",
                        orderedFieldNames);
                com.vaadin.flow.component.notification.Notification.show("Urutan kolom disimpan", 1500,
                        com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        java.util.List<String> userOrder = dataService.getUserGridOrder(formCode != null ? formCode : "schedulerGrid",
                "mainGrid");
        com.vaadinerp.components.StandardGridUtils.applySafeColumnOrder(grid, columnToFieldNameMap, userOrder);

        reapplyGridFilters = com.vaadinerp.components.StandardGridUtils.attachGridFilters(
                grid,
                colGetterMap,
                () -> currentData);
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
            Notification.show("Update table belum dikonfigurasi!", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        String pkCol = schedulerConfig.getColPrimaryKey();
        if (pkCol == null || pkCol.isEmpty()) {
            pkCol = formMeta.getPrimaryKey();
        }
        if (pkCol == null || pkCol.isEmpty())
            pkCol = "id";

        List<Map<String, Object>> tableCols = null;
        try {
            tableCols = dataService.fetchTableSchemaDetails(tableName);
        } catch (Exception ignored) {}

        int successCount = 0;
        try {
            for (Map<String, Object> row : modifiedRows) {
                Object pkVal = row.get(pkCol);
                if (pkVal == null)
                    continue;

                StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
                List<Object> args = new ArrayList<>();
                boolean first = true;

                for (FieldMeta field : formMeta.getFields()) {
                    if (!field.isReadonly() && field.isShowInGrid()) {
                        String fName = field.getFieldName();
                        if (fName.equalsIgnoreCase(pkCol))
                            continue;
                        if (!first)
                            sql.append(", ");
                        sql.append(fName).append(" = ?");
                        
                        Object newVal = row.get(fName);
                        if (newVal instanceof String && tableCols != null) {
                            String strVal = ((String) newVal).trim();
                            if (strVal.isEmpty()) {
                                newVal = null;
                            } else {
                                String colType = "";
                                for (Map<String, Object> c : tableCols) {
                                    if (fName.equalsIgnoreCase((String) c.get("column_name"))) {
                                        colType = ((String) c.get("data_type")).toLowerCase();
                                        break;
                                    }
                                }
                                try {
                                    if (colType.contains("int") || colType.contains("serial")) {
                                        newVal = Integer.parseInt(strVal);
                                    } else if (colType.contains("numeric") || colType.contains("decimal")) {
                                        newVal = new java.math.BigDecimal(strVal);
                                    } else if (colType.contains("float") || colType.contains("double") || colType.contains("real")) {
                                        newVal = Double.parseDouble(strVal);
                                    } else if (colType.contains("bool")) {
                                        newVal = Boolean.parseBoolean(strVal);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                        args.add(newVal);
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
            Notification.show(successCount + " baris berhasil disimpan.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Refresh visuals to reflect new capacity if qty was changed
            updateAllCharts();
            updateTimelineCapacityColors();

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.show("Gagal menyimpan: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
                String gCol = schedulerConfig.getColResourceGroup();
                String resCol = schedulerConfig.getColResource();
                currentData = currentData.stream()
                        .filter(row -> {
                            boolean hasGroup = gCol != null && row.get(gCol) != null
                                    && !row.get(gCol).toString().trim().isEmpty();
                            boolean hasResource = resCol != null && row.get(resCol) != null
                                    && !row.get(resCol).toString().trim().isEmpty();
                            return hasGroup && hasResource;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
        } catch (Exception e) {
            Notification.show("Error loading scheduler data: " + e.getMessage(), 5000,
                    Notification.Position.MIDDLE);
            currentData = new ArrayList<>();
        }

        loadHolidayData();
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

        if (curr1 != null && groups.contains(curr1))
            groupFilterCombo1.setValue(curr1);
        if (curr2 != null && groups.contains(curr2))
            groupFilterCombo2.setValue(curr2);

        // ALWAYS default to the group with the smallest sequence for the timeline upon data refresh
        String seqCol = schedulerConfig.getColSequence();
        if (seqCol != null && !seqCol.trim().isEmpty()) {
            String groupWithMinSeq = null;
            int minSeq = Integer.MAX_VALUE;
            for (Map<String, Object> row : currentData) {
                Object seqVal = row.get(seqCol);
                Object groupVal = row.get(groupCol);
                if (seqVal != null && !seqVal.toString().trim().isEmpty() && groupVal != null
                        && !groupVal.toString().trim().isEmpty()) {
                    try {
                        int seq = Integer.parseInt(seqVal.toString().trim());
                        if (seq < minSeq) {
                            minSeq = seq;
                            groupWithMinSeq = groupVal.toString().trim();
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            if (groupWithMinSeq != null && groups.contains(groupWithMinSeq)) {
                currentTimelineGroupFilter = groupWithMinSeq;
                timelineGroupFilter.setValue(groupWithMinSeq);
            } else {
                currentTimelineGroupFilter = null;
                timelineGroupFilter.clear();
            }
        }
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
        if (currResource != null && resources.contains(currResource))
            timelineResourceFilter.setValue(currResource);
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
        if (currResource != null && resources.contains(currResource))
            resourceFilterCombo1.setValue(currResource);
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
        if (currResource != null && resources.contains(currResource))
            resourceFilterCombo2.setValue(currResource);
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
        int groupIndex = 0;

        JsonArray items = Json.createArray();
        int itemIndex = 0;

        List<Map<String, Object>> filteredTimelineData = new ArrayList<>();
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
            filteredTimelineData.add(row);
        }

        // Lateness Tracking Logic
        Map<String, LocalDate> spkMaxEndDate = new HashMap<>();
        Map<String, LocalDate> spkShippingDate = new HashMap<>();
        Set<String> lateSpks = new HashSet<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (taskNameCol != null) {
            Set<String> visibleSpks = new HashSet<>();
            for (Map<String, Object> row : filteredTimelineData) {
                Object taskNameVal = row.get(taskNameCol);
                if (taskNameVal != null && !taskNameVal.toString().trim().isEmpty()) {
                    visibleSpks.add(taskNameVal.toString());
                }
            }

            if (!visibleSpks.isEmpty()) {
                String query = schedulerConfig.getSchedulerQuery();
                if (query != null && !query.trim().isEmpty()) {
                    try {
                        StringBuilder spkList = new StringBuilder();
                        List<Object> args = new ArrayList<>();
                        for (String spk : visibleSpks) {
                            if (spkList.length() > 0)
                                spkList.append(",");
                            spkList.append("?");
                            args.add(spk);
                        }
                        String fullQuery = "SELECT * FROM (" + query + ") AS sq WHERE " + taskNameCol + " IN ("
                                + spkList.toString() + ")";
                        List<Map<String, Object>> fullSpkData = jdbcTemplate.queryForList(fullQuery, args.toArray());

                        for (Map<String, Object> row : fullSpkData) {
                            Object taskNameVal = row.get(taskNameCol);
                            if (taskNameVal == null)
                                continue;
                            String spk = taskNameVal.toString();

                            Object dateVal = endDateCol != null && row.get(endDateCol) != null ? row.get(endDateCol)
                                    : row.get(startDateCol);
                            if (dateVal != null) {
                                try {
                                    String ds = dateVal.toString();
                                    if (ds.length() > 10)
                                        ds = ds.substring(0, 10);
                                    LocalDate d = LocalDate.parse(ds, dtf);
                                    LocalDate max = spkMaxEndDate.get(spk);
                                    if (max == null || d.isAfter(max)) {
                                        spkMaxEndDate.put(spk, d);
                                    }
                                } catch (Exception ignored) {
                                }
                            }

                            if (shippingDateCol != null && row.get(shippingDateCol) != null) {
                                try {
                                    String ds = row.get(shippingDateCol).toString();
                                    if (ds.length() > 10)
                                        ds = ds.substring(0, 10);
                                    spkShippingDate.put(spk, LocalDate.parse(ds, dtf));
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // Fallback to memory data if query fails
                        for (Map<String, Object> row : filteredTimelineData) {
                            Object taskNameVal = row.get(taskNameCol);
                            if (taskNameVal == null)
                                continue;
                            String spk = taskNameVal.toString();

                            Object dateVal = endDateCol != null && row.get(endDateCol) != null ? row.get(endDateCol)
                                    : row.get(startDateCol);
                            if (dateVal != null) {
                                try {
                                    String ds = dateVal.toString();
                                    if (ds.length() > 10)
                                        ds = ds.substring(0, 10);
                                    LocalDate d = LocalDate.parse(ds, dtf);
                                    LocalDate max = spkMaxEndDate.get(spk);
                                    if (max == null || d.isAfter(max)) {
                                        spkMaxEndDate.put(spk, d);
                                    }
                                } catch (Exception ignored) {
                                }
                            }

                            if (shippingDateCol != null && row.get(shippingDateCol) != null) {
                                try {
                                    String ds = row.get(shippingDateCol).toString();
                                    if (ds.length() > 10)
                                        ds = ds.substring(0, 10);
                                    spkShippingDate.put(spk, LocalDate.parse(ds, dtf));
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
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

        Map<String, Set<String>> parentToChildren = new LinkedHashMap<>();
        Set<String> unassignedResources = new LinkedHashSet<>();

        for (Map<String, Object> row : filteredTimelineData) {
            Object resourceVal = row.get(resourceCol);
            String resourceId = resourceVal != null && !resourceVal.toString().trim().isEmpty()
                    ? resourceVal.toString().trim()
                    : null;

            String parentId = null;
            if (groupCol != null && !groupCol.trim().isEmpty()) {
                Object gVal = row.get(groupCol);
                if (gVal != null && !gVal.toString().trim().isEmpty()) {
                    parentId = gVal.toString().trim();
                }
            }

            // Fallback: Extract parent group from resource string if missing
            if (parentId == null && resourceId != null && resourceId.contains(" : ")) {
                String fullPrefix = resourceId.substring(0, resourceId.indexOf(" : ")).trim();
                if (fullPrefix.contains(",")) {
                    parentId = fullPrefix.substring(fullPrefix.lastIndexOf(",") + 1).trim();
                } else {
                    parentId = fullPrefix;
                }
            }

            // If resourceId is still null, give it a unique unassigned name based on its
            // parent
            if (resourceId == null) {
                if (parentId != null && !parentId.isEmpty()) {
                    resourceId = "Unassigned (" + parentId + ")";
                } else {
                    resourceId = "unassigned";
                }
            }

            if (parentId != null && !parentId.isEmpty()) {
                parentToChildren.computeIfAbsent(parentId, k -> new LinkedHashSet<>()).add(resourceId);
            } else {
                unassignedResources.add(resourceId);
            }
        }

        groups = Json.createArray();
        groupIndex = 0;

        for (Map.Entry<String, Set<String>> entry : parentToChildren.entrySet()) {
            String parentId = "parent_" + entry.getKey();
            JsonObject parentGroup = Json.createObject();
            parentGroup.put("id", parentId);
            parentGroup.put("content", "<b style='font-size: 13px; color: #475569;'>" + entry.getKey() + "</b>");

            JsonArray nestedGroups = Json.createArray();
            int nIdx = 0;
            for (String childId : entry.getValue()) {
                nestedGroups.set(nIdx++, childId);
            }
            parentGroup.put("nestedGroups", nestedGroups);
            parentGroup.put("showNested", true);
            parentGroup.put("treeLevel", 1);
            parentGroup.put("orderIndex", groupIndex);
            groups.set(groupIndex++, parentGroup);

            for (String childId : entry.getValue()) {
                JsonObject childGroup = Json.createObject();
                childGroup.put("id", childId);
                childGroup.put("content", "<span style='color:#1e293b;font-size:12px;'>" + childId + "</span>");
                childGroup.put("treeLevel", 2);
                childGroup.put("orderIndex", groupIndex);
                groups.set(groupIndex++, childGroup);
            }
        }

        // Ensure unassignedResources does not contain any resources that are already
        // children of a parent
        Set<String> allChildren = new HashSet<>();
        for (Set<String> children : parentToChildren.values()) {
            allChildren.addAll(children);
        }
        unassignedResources.removeAll(allChildren);

        for (String childId : unassignedResources) {
            JsonObject groupObj = Json.createObject();
            groupObj.put("id", childId);
            groupObj.put("content", childId);
            groupObj.put("treeLevel", 1);
            groupObj.put("orderIndex", groupIndex);
            groups.set(groupIndex++, groupObj);
        }

        for (Map<String, Object> row : filteredTimelineData) {

            Object resourceVal = row.get(resourceCol);
            String groupId = resourceVal != null && !resourceVal.toString().trim().isEmpty()
                    ? resourceVal.toString().trim()
                    : null;

            String parentId = null;
            if (groupCol != null && !groupCol.trim().isEmpty()) {
                Object gVal = row.get(groupCol);
                if (gVal != null && !gVal.toString().trim().isEmpty()) {
                    parentId = gVal.toString().trim();
                }
            }

            // Fallback: Extract parent group from resource string if missing
            if (parentId == null && groupId != null && groupId.contains(" : ")) {
                String fullPrefix = groupId.substring(0, groupId.indexOf(" : ")).trim();
                if (fullPrefix.contains(",")) {
                    parentId = fullPrefix.substring(fullPrefix.lastIndexOf(",") + 1).trim();
                } else {
                    parentId = fullPrefix;
                }
            }

            // Apply same unique unassigned name based on parent
            if (groupId == null) {
                if (parentId != null && !parentId.isEmpty()) {
                    groupId = "Unassigned (" + parentId + ")";
                } else {
                    groupId = "unassigned";
                }
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

                LocalDate shipDate = spkShippingDate.get(spk);
                if (shipDate != null) {
                    tooltip.append("Due Date: ").append(shipDate.format(dtf)).append("<br>");
                }

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

        // Add background items for Holidays & Sundays
        if (startDateFilter != null && endDateFilter != null && startDateFilter.getValue() != null && endDateFilter.getValue() != null) {
            LocalDate start = startDateFilter.getValue().minusDays(14); // extend visual range a bit
            LocalDate end = endDateFilter.getValue().plusDays(30);
            LocalDate curr = start;
            while (!curr.isAfter(end)) {
                boolean isSunday = curr.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
                boolean isHoliday = holidaySet.contains(curr);

                if (isSunday || isHoliday) {
                    elemental.json.JsonObject bgObj = elemental.json.Json.createObject();
                    bgObj.put("id", "bg_holiday_" + curr.toString());
                    bgObj.put("start", curr.toString() + "T00:00:00");
                    bgObj.put("end", curr.toString() + "T23:59:59");
                    bgObj.put("type", "background");
                    
                    if (isHoliday) {
                        bgObj.put("className", "vis-holiday-bg");
                    } else {
                        bgObj.put("className", "vis-sunday-bg");
                    }
                    items.set(itemIndex++, bgObj);
                }
                curr = curr.plusDays(1);
            }
        }

        // Add custom time markers for Shipping Dates
        JsonArray customTimes = Json.createArray();
        int ctIndex = 0;
        for (String spk : spkShippingDate.keySet()) {
            LocalDate sDate = spkShippingDate.get(spk);
            if (sDate == null)
                continue;

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

        // Inject Custom CSS globally to ensure the top-left corner ALWAYS shows the header,
        // even if vis-timeline destroys and recreates the DOM elements on redraw.
        timeline.getElement().executeJs(
            "if (!document.getElementById('timeline-header-css')) {" +
            "  const style = document.createElement('style');" +
            "  style.id = 'timeline-header-css';" +
            "  style.innerHTML = `" +
            "    .vis-panel.vis-left.vis-top {" +
            "      display: flex !important;" +
            "      align-items: center;" +
            "      justify-content: center;" +
            "      background-color: #f9fafb !important;" +
            "      border-bottom: 1px solid #e5e7eb !important;" +
            "      border-right: 1px solid #e5e7eb !important;" +
            "    }" +
            "    .vis-panel.vis-left.vis-top::before {" +
            "      content: 'Mesin / Stasiun Kerja';" +
            "      font-weight: 600;" +
            "      font-size: 13px;" +
            "      color: #4b5563;" +
            "      text-align: center;" +
            "      width: 100%;" +
            "      padding: 5px;" +
            "      box-sizing: border-box;" +
            "    }" +
            "    .vis-holiday-bg {" +
            "      background-color: rgba(239, 68, 68, 0.15) !important;" + // Lumo error color
            "    }" +
            "    .vis-sunday-bg {" +
            "      background-color: rgba(245, 158, 11, 0.15) !important;" + // Lumo warning color (orange/yellow)
            "    }`;" +
            "  document.head.appendChild(style);" +
            "}"
        );
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

            totalsMap.merge(key, qty, (a, b) -> a + b);
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

        String startDateStr = chartViewStartDate != null ? chartViewStartDate.toString() : null;
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
    // HANDLE ITEMS SELECTED (select in grid)
    // ================================================================
    private void handleItemsSelected(List<String> itemIds) {
        grid.deselectAll(); // clear the old selection
        if (itemIds == null || itemIds.isEmpty())
            return;

        String pkCol = schedulerConfig.getColPrimaryKey();
        java.util.Set<Map<String, Object>> rowsToSelect = new java.util.HashSet<>();

        for (Map<String, Object> row : currentData) {
            Object id = row.get(pkCol);
            if (id != null && itemIds.contains(id.toString())) {
                rowsToSelect.add(row);
            }
        }

        if (!rowsToSelect.isEmpty()) {
            grid.asMultiSelect().setValue(rowsToSelect);
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
                if (!grid.getSelectedItems().contains(row)) {
                    grid.select(row);
                }
                break;
            }
        }

        if (clickedRow == null)
            return;

        boolean isMultiSelect = allSelectedItems != null && allSelectedItems.length > 1;

        com.vaadin.flow.component.dialog.Dialog actionDialog = new com.vaadin.flow.component.dialog.Dialog();
        actionDialog.setHeaderTitle(isMultiSelect ? "Multiple Tasks Actions" : "Task Actions - " + clickedItemId);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        double currentQty = 0;
        String qtyCol = schedulerConfig.getColQty();
        if (qtyCol != null && clickedRow.get(qtyCol) != null) {
            try {
                currentQty = Double.parseDouble(clickedRow.get(qtyCol).toString());
            } catch (Exception ignored) {
            }
        }

        Button btnEditQty = new Button("Edit Qty", VaadinIcon.EDIT.create(), e -> {
            actionDialog.close();
            openEditQtyDialog(clickedItemId);
        });
        btnEditQty.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnEditQty.setWidthFull();

        Button btnSplit = new Button("Split Task", VaadinIcon.SCISSORS.create(), e -> {
            actionDialog.close();
            openSplitDialog(clickedItemId);
        });
        btnSplit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSplit.setWidthFull();

        if (currentQty <= 1) {
            btnSplit.setEnabled(false);
            btnSplit.setTooltipText("Qty <= 1 tidak bisa displit");
        }

        Button btnMerge = new Button("Merge Selected Tasks", VaadinIcon.COMPRESS.create(), e -> {
            actionDialog.close();
            Set<Map<String, Object>> selectedRows = grid.getSelectedItems();
            executeMerge(selectedRows);
        });
        btnMerge.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnMerge.setWidthFull();

        // Validasi syarat merge: Group ID dan Resource harus sama
        Set<Map<String, Object>> currentGridSelection = grid.getSelectedItems();
        if (currentGridSelection.size() >= 2) {
            String groupIdCol = schedulerConfig.getColGroupId();
            String resourceCol = schedulerConfig.getColResource();
            boolean canMerge = true;
            String firstGroupId = null;
            String firstResource = null;

            for (Map<String, Object> row : currentGridSelection) {
                String rowGroupId = groupIdCol != null && row.get(groupIdCol) != null ? row.get(groupIdCol).toString()
                        : "";
                String rowResource = resourceCol != null && row.get(resourceCol) != null
                        ? row.get(resourceCol).toString()
                        : "";

                if (firstGroupId == null) {
                    firstGroupId = rowGroupId;
                    firstResource = rowResource;
                } else {
                    if (!java.util.Objects.equals(firstGroupId, rowGroupId)
                            || !java.util.Objects.equals(firstResource, rowResource)) {
                        canMerge = false;
                        break;
                    }
                }
            }

            if (canMerge) {
                btnMerge.setEnabled(true);
            } else {
                btnMerge.setEnabled(false);
                btnMerge.setTooltipText("Merge hanya bisa dilakukan jika Dependency ID dan Mesin/Resource sama");
            }
        } else {
            btnMerge.setEnabled(false);
            btnMerge.setTooltipText("Pilih minimal 2 baris (dengan Ctrl+Click) untuk digabung");
        }

        if (isMultiSelect) {
            dialogLayout.add(btnMerge); // Hide Edit and Split when multiple items selected
        } else {
            dialogLayout.add(btnEditQty, btnSplit, btnMerge);
        }

        actionDialog.add(dialogLayout);
        actionDialog.open();
    }

    private void openEditQtyDialog(String itemId) {
        String pkCol = schedulerConfig.getColPrimaryKey();
        String qtyProdCol = schedulerConfig.getColQtyProd();

        if (qtyProdCol == null) {
            Notification.show("Kolom Qty Prod belum disetting di Config!", 3000, Notification.Position.MIDDLE);
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

        Object currentQtyProdObj = targetRow.get(qtyProdCol);
        if (currentQtyProdObj == null) {
            Notification.show("Qty Prod baris ini kosong!", 3000, Notification.Position.MIDDLE);
            return;
        }

        double currentQtyProd = Double.parseDouble(currentQtyProdObj.toString());

        double maxLimit = currentQtyProd; // default fallback if columns are missing
        try {
            String qtyCol = schedulerConfig.getColQty();
            String pcsPerBoxCol = schedulerConfig.getColPcsPerBox();

            Object qtyboxObj = qtyCol != null ? targetRow.get(qtyCol) : null;
            Object pcsperboxObj = pcsPerBoxCol != null ? targetRow.get(pcsPerBoxCol) : null;

            if (qtyboxObj != null && pcsperboxObj != null) {
                double qbox = Double.parseDouble(qtyboxObj.toString());
                double pcs = Double.parseDouble(pcsperboxObj.toString());
                maxLimit = qbox * pcs;
            }
        } catch (Exception e) {
        }

        if (maxLimit < currentQtyProd) {
            maxLimit = currentQtyProd;
        }

        final double finalMaxLimit = maxLimit;

        com.vaadin.flow.component.dialog.Dialog editDialog = new com.vaadin.flow.component.dialog.Dialog();
        editDialog.setHeaderTitle("Edit Qty Prod");

        com.vaadin.flow.component.textfield.NumberField qtyField = new com.vaadin.flow.component.textfield.NumberField(
                "Qty Prod Baru");
        qtyField.setMin(0.001); // greater than 0
        qtyField.setMax(finalMaxLimit);
        qtyField.setValue(currentQtyProd);
        qtyField.setWidthFull();
        qtyField.setHelperText("Min: > 0 | Max: " + finalMaxLimit);

        final Map<String, Object> finalTargetRow = targetRow;

        Button btnSave = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            Double newQty = qtyField.getValue();
            if (newQty == null || newQty <= 0 || newQty > finalMaxLimit) {
                Notification.show("Qty tidak valid!", 3000, Notification.Position.MIDDLE);
                return;
            }
            editDialog.close();

            String tableName = schedulerConfig.getUpdateTable();
            if (tableName == null || tableName.trim().isEmpty()) {
                tableName = formMeta.getTableName();
            }

            if (tableName != null && !tableName.trim().isEmpty()) {
                try {
                    Object actualPkVal = finalTargetRow.get(pkCol);
                    jdbcTemplate.update("UPDATE " + tableName + " SET " + qtyProdCol + " = ? WHERE " + pkCol + " = ?",
                            newQty, actualPkVal);
                    Notification.show("Qty Prod berhasil diupdate.", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    refreshData();
                } catch (Exception ex) {
                    Notification.show("Gagal update Qty: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("Update table belum dikonfigurasi!", 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new Button("Batal", e -> editDialog.close());

        HorizontalLayout actions = new HorizontalLayout(btnCancel, btnSave);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setWidthFull();

        VerticalLayout vl = new VerticalLayout(qtyField, actions);
        vl.setPadding(true);
        vl.setSpacing(true);

        editDialog.add(vl);
        editDialog.open();
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

        // Find the maximum split index for the current group
        int maxSplitIndex = 0;
        String basePrefix = "";

        if (targetSplitGroup != null && !targetSplitGroup.trim().isEmpty()) {
            int dashIndex = targetSplitGroup.lastIndexOf("-");
            if (dashIndex > 0) {
                basePrefix = targetSplitGroup.substring(0, dashIndex + 1);
            } else {
                basePrefix = targetSplitGroup + "-";
            }
        } else if (targetGroupId != null && !targetGroupId.trim().isEmpty()) {
            basePrefix = targetGroupId + "-";
        } else {
            basePrefix = "SPLIT-" + System.currentTimeMillis() + "-";
        }

        // FETCH ALL RELATED DATA FROM DATABASE to bypass UI Date Filters
        List<Map<String, Object>> allRelatedData = new ArrayList<>();
        try {
            String query = schedulerConfig.getSchedulerQuery();
            if (query != null && !query.trim().isEmpty() && targetGroupId != null && !targetGroupId.trim().isEmpty() && groupIdCol != null) {
                String sql = "SELECT * FROM (" + query + ") AS sq WHERE " + groupIdCol + " = ?";
                allRelatedData = jdbcTemplate.queryForList(sql, targetGroupId);
            } else {
                allRelatedData.addAll(currentData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            allRelatedData.addAll(currentData); // fallback
        }

        // Ensure targetRow is in the list just in case
        boolean foundTarget = false;
        for (Map<String, Object> r : allRelatedData) {
            if (java.util.Objects.equals(r.get(pkCol), targetRow.get(pkCol))) {
                foundTarget = true; break;
            }
        }
        if (!foundTarget) allRelatedData.add(targetRow);

        for (Map<String, Object> row : allRelatedData) {
            if (splitGroupCol != null && row.get(splitGroupCol) != null) {
                String sg = row.get(splitGroupCol).toString();
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

        // Find all rows in allRelatedData that match the criteria
        List<Map<String, Object>> rowsToSplit = new ArrayList<>();
        for (Map<String, Object> row : allRelatedData) {
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
                Object rowPkVal = row.get(pkCol); // still read from original alias

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
                    // Baris asli (remain) menempati urutan awal, sehingga diasumsikan box nya
                    // terisi penuh
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
                    if (key.equalsIgnoreCase("inputby"))
                        actualInputByKey = key;
                    if (key.equalsIgnoreCase("inputdt"))
                        actualInputDtKey = key;
                    if (key.equalsIgnoreCase("updateby"))
                        actualUpdateByKey = key;
                    if (key.equalsIgnoreCase("updatedt"))
                        actualUpdateDtKey = key;
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
        String groupIdCol = schedulerConfig.getColGroupId();
        String resourceCol = schedulerConfig.getColResource();
        String sequenceCol = schedulerConfig.getColSequence();
        String splitGroupCol = schedulerConfig.getColSplitGroup();
        String qtyProdCol = schedulerConfig.getColQtyProd();

        if (updateTable == null || pkCol == null || qtyCol == null) {
            Notification.show("Config Update Table/PK/Qty belum lengkap!", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (selectedRows.size() < 2) {
            Notification.show("Pilih minimal 2 baris untuk digabung!", 3000, Notification.Position.MIDDLE);
            return;
        }

        List<Map<String, Object>> selectedRowsList = new ArrayList<>(selectedRows);

        // 1. Validasi syarat merge pada baris awal
        String firstGroupId = null;
        String firstResource = null;
        Integer minTargetSequence = Integer.MAX_VALUE;
        Set<String> targetSplitGroups = new java.util.HashSet<>();

        for (Map<String, Object> row : selectedRowsList) {
            String rowGroupId = groupIdCol != null && row.get(groupIdCol) != null ? row.get(groupIdCol).toString() : "";
            String rowResource = resourceCol != null && row.get(resourceCol) != null ? row.get(resourceCol).toString()
                    : "";

            if (firstGroupId == null) {
                firstGroupId = rowGroupId;
                firstResource = rowResource;
            } else {
                if (!java.util.Objects.equals(firstGroupId, rowGroupId)
                        || !java.util.Objects.equals(firstResource, rowResource)) {
                    Notification.show(
                            "Gagal! Merge hanya bisa dilakukan jika Dependency ID dan Mesin/Resource sama pada titik awal",
                            4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
            }

            Integer rowSeq = sequenceCol != null && row.get(sequenceCol) != null
                    ? Integer.parseInt(row.get(sequenceCol).toString())
                    : 0;
            if (rowSeq < minTargetSequence) {
                minTargetSequence = rowSeq;
            }

            if (splitGroupCol != null && row.get(splitGroupCol) != null) {
                targetSplitGroups.add(row.get(splitGroupCol).toString());
            } else if (firstGroupId != null && !firstGroupId.isEmpty()) {
                // Fallback jika splitGroup kosong, gunakan groupId
                targetSplitGroups.add(firstGroupId);
            }
        }

        // 2. Kumpulkan semua pecahan task yang akan di-merge (Cascading)
        // FETCH ALL RELATED DATA FROM DATABASE to bypass UI Date Filters
        List<Map<String, Object>> allRelatedData = new ArrayList<>();
        try {
            String query = schedulerConfig.getSchedulerQuery();
            if (query != null && !query.trim().isEmpty() && firstGroupId != null && !firstGroupId.trim().isEmpty() && groupIdCol != null) {
                String sql = "SELECT * FROM (" + query + ") AS sq WHERE " + groupIdCol + " = ?";
                allRelatedData = jdbcTemplate.queryForList(sql, firstGroupId);
            } else {
                allRelatedData.addAll(currentData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            allRelatedData.addAll(currentData); // fallback
        }

        // Ensure selectedRowsList are in the list just in case
        for (Map<String, Object> sr : selectedRowsList) {
            boolean found = false;
            for (Map<String, Object> r : allRelatedData) {
                if (java.util.Objects.equals(r.get(pkCol), sr.get(pkCol))) {
                    found = true; break;
                }
            }
            if (!found) allRelatedData.add(sr);
        }

        List<Map<String, Object>> allRowsToMerge = new ArrayList<>();
        for (Map<String, Object> row : allRelatedData) {
            Integer rowSeq = sequenceCol != null && row.get(sequenceCol) != null
                    ? Integer.parseInt(row.get(sequenceCol).toString())
                    : 0;

            if (rowSeq >= minTargetSequence) {
                String rowSplitGroup = splitGroupCol != null && row.get(splitGroupCol) != null
                        ? row.get(splitGroupCol).toString()
                        : "";
                String rowGroupId = groupIdCol != null && row.get(groupIdCol) != null ? row.get(groupIdCol).toString()
                        : "";

                boolean match = false;
                if (!rowSplitGroup.isEmpty() && targetSplitGroups.contains(rowSplitGroup)) {
                    match = true;
                } else if (rowSplitGroup.isEmpty() && targetSplitGroups.contains(rowGroupId)) {
                    match = true;
                }

                if (match) {
                    allRowsToMerge.add(row);
                }
            }
        }

        // 3. Kelompokkan row berdasarkan sequence
        Map<Integer, List<Map<String, Object>>> groupedBySequence = new java.util.HashMap<>();
        for (Map<String, Object> row : allRowsToMerge) {
            Integer rowSeq = sequenceCol != null && row.get(sequenceCol) != null
                    ? Integer.parseInt(row.get(sequenceCol).toString())
                    : 0;
            groupedBySequence.computeIfAbsent(rowSeq, k -> new ArrayList<>()).add(row);
        }

        try {
            AppUser currentUser = securityService.getCurrentUser();
            String currentUsername = currentUser != null ? currentUser.getUsername() : "system";
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

            // 4. Proses merge per sequence
            for (Map.Entry<Integer, List<Map<String, Object>>> entry : groupedBySequence.entrySet()) {
                List<Map<String, Object>> sequenceRows = entry.getValue();
                if (sequenceRows.size() < 2)
                    continue; // Tidak ada yang perlu digabung di sequence ini

                Map<String, Object> parentRow = sequenceRows.get(0);
                Object parentPk = parentRow.get(pkCol);

                double totalQty = 0;
                double totalWeight = 0;
                double totalQtyProd = 0;
                boolean hasWeight = (weightCol != null);
                boolean hasQtyProd = (qtyProdCol != null);

                List<Object> pksToDeleteForSeq = new ArrayList<>();

                for (int i = 0; i < sequenceRows.size(); i++) {
                    Map<String, Object> r = sequenceRows.get(i);
                    Object qObj = r.get(qtyCol);
                    if (qObj != null)
                        totalQty += Double.parseDouble(qObj.toString());

                    if (hasWeight) {
                        Object wObj = r.get(weightCol);
                        if (wObj != null)
                            totalWeight += Double.parseDouble(wObj.toString());
                    }

                    if (hasQtyProd) {
                        Object qpObj = r.get(qtyProdCol);
                        if (qpObj != null)
                            totalQtyProd += Double.parseDouble(qpObj.toString());
                    }

                    if (i > 0) {
                        pksToDeleteForSeq.add(r.get(pkCol));
                    }
                }

                // Update Induk (Qty, QtyProd)
                String updateSql = "UPDATE " + updateTable + " SET " + qtyCol + " = ?, updateby = ?, updatedt = ?";
                List<Object> updateParams = new ArrayList<>();
                updateParams.add(totalQty);
                updateParams.add(currentUsername);
                updateParams.add(currentTimestamp);

                if (hasQtyProd) {
                    updateSql += ", " + qtyProdCol + " = ?";
                    updateParams.add(totalQtyProd);
                }

                // if (hasWeight) {
                // updateSql += ", " + weightCol + " = ?";
                // updateParams.add(totalWeight);
                // }

                // Menghapus Split Group (Mengembalikan ke null jika disatukan 100%)
                // Kita anggap untuk simplifikasi sistem, jika digabung maka pecahan tersebut
                // kehilangan suffix-nya
                if (splitGroupCol != null) {
                    updateSql += ", " + splitGroupCol + " = NULL";
                }

                updateSql += " WHERE " + pkCol + " = ?";
                updateParams.add(parentPk);
                jdbcTemplate.update(updateSql, updateParams.toArray());

                // Delete Sisa
                if (!pksToDeleteForSeq.isEmpty()) {
                    StringBuilder deleteSql = new StringBuilder(
                            "DELETE FROM " + updateTable + " WHERE " + pkCol + " IN (");
                    for (int i = 0; i < pksToDeleteForSeq.size(); i++) {
                        deleteSql.append("?");
                        if (i < pksToDeleteForSeq.size() - 1)
                            deleteSql.append(", ");
                    }
                    deleteSql.append(")");
                    jdbcTemplate.update(deleteSql.toString(), pksToDeleteForSeq.toArray());
                }
            }

            Notification
                    .show("Berhasil merge " + allRowsToMerge.size()
                            + " pecahan task secara berantai (Cascading Merge)!", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshData();
        } catch (Exception ex) {
            Notification.show("Gagal menggabung data: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
            String qtyCol = "QTYBOX".equals(currentCapacityMode1)
                    ? schedulerConfig.getColQty()
                    : schedulerConfig.getColWeight();
            String capCol = "QTYBOX".equals(currentCapacityMode1)
                    ? schedulerConfig.getColMaxCapacity()
                    : schedulerConfig.getColMaxCapacityWeight();
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
                String sourceGroup = draggedTask.get(resourceGroupCol) != null
                        ? draggedTask.get(resourceGroupCol).toString()
                        : "";
                String targetGroup = null;
                for (Map<String, Object> row : currentData) {
                    Object rVal = row.get(resourceCol);
                    if (rVal != null && rVal.toString().equals(resource)) {
                        targetGroup = row.get(resourceGroupCol) != null ? row.get(resourceGroupCol).toString() : "";
                        break;
                    }
                }

                if (targetGroup != null && !sourceGroup.equals(targetGroup)) {
                    Notification n = Notification
                            .show("Tidak bisa memindahkan jadwal ke departemen/grup mesin yang berbeda (" + targetGroup
                                    + ")!", 5000, Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    refreshData(); // Revert
                    return;
                }
            }

            // --- CAPACITY VALIDATION & EXECUTION WRAPPER ---
            final Map<String, Object> finalDraggedTask = draggedTask;
            Runnable checkCapacityAndExecute = () -> {
                double itemQty = 0;
                try {
                    itemQty = Double.parseDouble(finalDraggedTask.get(qtyCol).toString());
                } catch (Exception ignored) {
                }
                double maxCapacity = 0;
                try {
                    maxCapacity = Double.parseDouble(finalDraggedTask.get(capCol).toString());
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
                } else {
                    // No overcapacity — proceed directly
                    executeDragUpdate(itemId, newDate, resource, finalDraggedTask);
                }
            };

            // --- SUNDAY / HOLIDAY CHECK ---
            if (isHolidayOrSunday(newDate)) {
                ConfirmDialog holidayDlg = new ConfirmDialog();
                holidayDlg.setHeader("⚠️ Hari Libur / Minggu");
                holidayDlg.setText(String.format(
                        "Tanggal %s adalah hari libur atau hari Minggu.\n\nApakah Anda yakin ingin menjadwalkan lembur pada hari ini?",
                        newDateStr));
                holidayDlg.setCancelable(true);
                holidayDlg.setCancelText("Batalkan");
                holidayDlg.setConfirmText("Ya, Lanjutkan");
                holidayDlg.setConfirmButtonTheme("warning primary");

                holidayDlg.addConfirmListener(event -> checkCapacityAndExecute.run());
                holidayDlg.addCancelListener(event -> refreshData());

                holidayDlg.open();
            } else {
                checkCapacityAndExecute.run();
            }

        } catch (Exception e) {
            Notification n = Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            refreshData();
        }
    }

    // ================================================================
    // EXECUTE DRAG UPDATE (save + cascade shift)
    // ================================================================
    private void executeDragUpdate(String itemId, LocalDate newDate, String newResource,
            Map<String, Object> draggedTask) {
        try {
            String pkCol = schedulerConfig.getColPrimaryKey();
            String dependencyIdCol = schedulerConfig.getColDependencyId(); // Changed from groupIdCol
            String splitGroupCol = schedulerConfig.getColSplitGroup();
            String seqCol = schedulerConfig.getColSequence();
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
                boolean resourceChanged = resourceCol != null && newResource != null
                        && !newResource.equals(oldResourceObj != null ? oldResourceObj.toString() : "");

                if (targetMatchCol != null && seqVal != null && (daysShifted != 0 || resourceChanged)) {
                    // Find all tasks with same target match and sequence > current
                    String findSql = "SELECT " + pkCol + ", " + seqCol + ", " + updateDateCol
                            + " FROM (" + schedulerConfig.getSchedulerQuery() + ") AS sq WHERE "
                            + targetMatchCol + " = ? AND "
                            + seqCol + " > ? ORDER BY " + seqCol + " ASC";

                    List<Map<String, Object>> relatedTasks = jdbcTemplate.queryForList(findSql,
                            targetMatchVal, seqVal);

                    long accumulatedOffsetDays = daysShifted;

                    for (Map<String, Object> related : relatedTasks) {
                        Object relatedDateObj = related.get(updateDateCol);
                        if (relatedDateObj != null) {
                            try {
                                LocalDate currentRelatedDate = LocalDate
                                        .parse(relatedDateObj.toString().substring(0, 10));
                                
                                // Apply accumulated offset
                                LocalDate cascadeDate = currentRelatedDate.plusDays(accumulatedOffsetDays);
                                
                                // Validate against holidays/Sundays
                                java.time.LocalDateTime cascadeDateTime = cascadeDate.atStartOfDay();
                                java.time.LocalDateTime validDateTime = getNextWorkingTime(cascadeDateTime);
                                LocalDate finalCascadeDate = validDateTime.toLocalDate();
                                
                                // If bounced, increase accumulated offset for the NEXT sequence
                                long bounceDays = java.time.temporal.ChronoUnit.DAYS.between(cascadeDate, finalCascadeDate);
                                if (bounceDays > 0) {
                                    accumulatedOffsetDays += bounceDays;
                                }

                                Object relatedId = related.get(pkCol);

                                String cascadeSql = "UPDATE " + updateTable + " SET " + updateDateCol
                                        + " = ?::date, updateby = ?, updatedt = ? WHERE " + pkCol + " = ?";

                                List<Object> cascadeParams = new ArrayList<>();
                                cascadeParams.add(finalCascadeDate.toString());
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
