package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadinerp.service.DynamicDataService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProductionSchedulerView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    private final JdbcTemplate jdbcTemplate;

    private final TabSheet tabSheet = new TabSheet();
    private final VerticalLayout timelineContainer = new VerticalLayout();
    private final VerticalLayout loadMonitorContainer = new VerticalLayout();
    private final VerticalLayout woListContainer = new VerticalLayout();

    private ComboBox<String> filterWorkCenter = new ComboBox<>("Filter Mesin / Stasiun");
    private ComboBox<String> filterStatus = new ComboBox<>("Filter Status");

    public ProductionSchedulerView(DynamicDataService dynamicDataService) {
        this.dynamicDataService = dynamicDataService;
        this.jdbcTemplate = dynamicDataService.getJdbcTemplate();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8fafc");

        // Ensure tables and sample data exist
        initAndSeedManufacturingData(false);

        buildHeader();
        buildFilterBar();
        buildTabs();
        refreshAllViews();
    }

    private void buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleBox = new HorizontalLayout();
        titleBox.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon factoryIcon = VaadinIcon.FACTORY.create();
        factoryIcon.setSize("28px");
        factoryIcon.getStyle().set("color", "#4f46e5");

        VerticalLayout titleText = new VerticalLayout();
        titleText.setSpacing(false);
        titleText.setPadding(false);
        H2 title = new H2("Production & Shop Floor Scheduler");
        title.getStyle().set("margin", "0").set("color", "#1e293b").set("font-size", "22px");
        Span subTitle = new Span("Visual Gantt Timeline, Capacity Bottleneck Leveling & MRP Work Order Control");
        subTitle.getStyle().set("color", "#64748b").set("font-size", "13px");
        titleText.add(title, subTitle);
        titleBox.add(factoryIcon, titleText);

        HorizontalLayout actions = new HorizontalLayout();
        Button btnAddSlot = new Button("➕ Create Schedule / WO Slot", e -> openScheduleDialog(null));
        btnAddSlot.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAddSlot.getStyle().set("background", "linear-gradient(135deg, #4f46e5, #4338ca)").set("border-radius", "8px");

        Button btnSeed = new Button("🔄 Reset Sample Data", VaadinIcon.REFRESH.create(), e -> {
            initAndSeedManufacturingData(true);
            refreshAllViews();
            Notification.show("Manufacturing sample data reset & reloaded!", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        btnSeed.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        actions.add(btnAddSlot, btnSeed);
        header.add(titleBox, actions);
        add(header);
    }

    private void buildFilterBar() {
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.setAlignItems(FlexComponent.Alignment.BASELINE);
        filterBar.getStyle().set("background-color", "white")
                .set("padding", "12px 16px")
                .set("border-radius", "10px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)")
                .set("border", "1px solid #e2e8f0");

        filterWorkCenter.setItems(getWorkCenterCodesWithAll());
        filterWorkCenter.setValue("SEMUA MESIN / STASIUN");
        filterWorkCenter.addValueChangeListener(e -> refreshAllViews());

        filterStatus.setItems("SEMUA STATUS", "SCHEDULED", "RUNNING", "COMPLETED");
        filterStatus.setValue("SEMUA STATUS");
        filterStatus.addValueChangeListener(e -> refreshAllViews());

        Button btnClearFilter = new Button("Reset Filter", VaadinIcon.CLOSE_CIRCLE_O.create(), e -> {
            filterWorkCenter.setValue("SEMUA MESIN / STASIUN");
            filterStatus.setValue("SEMUA STATUS");
        });
        btnClearFilter.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        filterBar.add(filterWorkCenter, filterStatus, btnClearFilter);
        add(filterBar);
    }

    private List<String> getWorkCenterCodesWithAll() {
        List<String> codes = new ArrayList<>();
        codes.add("SEMUA MESIN / STASIUN");
        try {
            List<Map<String, Object>> wcs = jdbcTemplate.queryForList("SELECT code, name FROM mst_work_center ORDER BY code");
            for (Map<String, Object> m : wcs) {
                codes.add(m.get("code") + " - " + m.get("name"));
            }
        } catch (Exception ignored) {}
        return codes;
    }

    private void buildTabs() {
        tabSheet.setSizeFull();
        tabSheet.getStyle().set("background-color", "white").set("border-radius", "12px").set("box-shadow", "0 4px 6px -1px rgba(0,0,0,0.05)");

        timelineContainer.setSizeFull();
        timelineContainer.setPadding(true);
        timelineContainer.getStyle().set("overflow-y", "auto");

        loadMonitorContainer.setSizeFull();
        loadMonitorContainer.setPadding(true);
        loadMonitorContainer.getStyle().set("overflow-y", "auto");

        woListContainer.setSizeFull();
        woListContainer.setPadding(true);

        Tab tabTimeline = tabSheet.add("🕒 Interactive Gantt Board & Timeline", timelineContainer);
        Tab tabMonitor = tabSheet.add("📊 Machine Load & Bottleneck Leveling", loadMonitorContainer);
        Tab tabWo = tabSheet.add("📋 Master Work Order (Production WO)", woListContainer);

        add(tabSheet);
    }

    private void refreshAllViews() {
        refreshTimelineView();
        refreshLoadMonitorView();
        refreshWoListView();
    }

    // TAB 1: GANTT TIMELINE VIEW
    private void refreshTimelineView() {
        timelineContainer.removeAll();

        List<Map<String, Object>> workCenters = getFilteredWorkCenters();
        if (workCenters.isEmpty()) {
            timelineContainer.add(new Span("No workstations match the filter."));
            return;
        }

        for (Map<String, Object> wc : workCenters) {
            String wcCode = (String) wc.get("code");
            String wcName = (String) wc.get("name");
            Integer cap = ((Number) wc.get("capacity_hours")).intValue();

            VerticalLayout wcCard = new VerticalLayout();
            wcCard.setWidthFull();
            wcCard.setPadding(true);
            wcCard.getStyle()
                    .set("background", "#ffffff")
                    .set("border", "1px solid #cbd5e1")
                    .set("border-radius", "10px")
                    .set("margin-bottom", "16px")
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

            // WC Header
            HorizontalLayout wcHeader = new HorizontalLayout();
            wcHeader.setWidthFull();
            wcHeader.setAlignItems(FlexComponent.Alignment.CENTER);
            wcHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            HorizontalLayout wcTitleBox = new HorizontalLayout();
            wcTitleBox.setAlignItems(FlexComponent.Alignment.CENTER);
            Icon wcIcon = VaadinIcon.COGS.create();
            wcIcon.getStyle().set("color", "#4f46e5");
            H4 wcTitle = new H4("Stasiun Kerja: [" + wcCode + "] " + wcName);
            wcTitle.getStyle().set("margin", "0").set("color", "#0f172a");
            List<Map<String, Object>> slots = getSchedulesForWorkCenter(wcCode);
            Double capTon = wc.get("capacity_tonnage") != null ? ((Number) wc.get("capacity_tonnage")).doubleValue() : 0.0;
            Double totalPourTon = 0.0;
            for (Map<String, Object> s : slots) {
                if (s.get("pour_weight_kg") != null) {
                    totalPourTon += ((Number) s.get("pour_weight_kg")).doubleValue() / 1000.0;
                }
            }

            Span capBadge = new Span("⚡ Kapasitas: " + cap + " Jam/Hari");
            capBadge.getStyle().set("background", "#f1f5f9").set("color", "#475569").set("padding", "2px 8px").set("border-radius", "12px").set("font-size", "12px").set("font-weight", "600");
            wcTitleBox.add(wcIcon, wcTitle, capBadge);

            if (capTon > 0 || totalPourTon > 0) {
                Span tonBadge = new Span();
                if (capTon > 0) {
                    tonBadge.setText(String.format("⚖️ Beban Cor: %.2f / %.2f Ton (%.0f%%)", totalPourTon, capTon, (totalPourTon/capTon)*100.0));
                    if (totalPourTon > capTon) {
                        tonBadge.getStyle().set("background", "#fee2e2").set("color", "#dc2626").set("padding", "2px 8px").set("border-radius", "12px").set("font-size", "12px").set("font-weight", "bold");
                        tonBadge.setText("🔥 OVER CAPACITY: " + String.format("%.2f / %.2f Ton", totalPourTon, capTon));
                    } else {
                        tonBadge.getStyle().set("background", "#e0e7ff").set("color", "#3730a3").set("padding", "2px 8px").set("border-radius", "12px").set("font-size", "12px").set("font-weight", "600");
                    }
                } else {
                    tonBadge.setText(String.format("⚖️ Total Bobot Cor: %.2f Ton", totalPourTon));
                    tonBadge.getStyle().set("background", "#ecfdf5").set("color", "#047857").set("padding", "2px 8px").set("border-radius", "12px").set("font-size", "12px").set("font-weight", "600");
                }
                wcTitleBox.add(tonBadge);
            }

            Button btnQuickAdd = new Button("➕ Slot SPK di Mesin Ini", e -> {
                Map<String, Object> pre = new HashMap<>();
                pre.put("work_center_code", wcCode);
                openScheduleDialog(pre);
            });
            btnQuickAdd.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

            wcHeader.add(wcTitleBox, btnQuickAdd);
            wcCard.add(wcHeader);

            // Timeline slots for this work center
            HorizontalLayout slotsLayout = new HorizontalLayout();
            slotsLayout.setWidthFull();
            slotsLayout.getStyle().set("overflow-x", "auto").set("padding", "8px 0").set("min-height", "130px");

            if (slots.isEmpty()) {
                Div emptySlot = new Div();
                emptySlot.setText("✨ No production schedule queued for this machine.");
                emptySlot.getStyle().set("color", "#94a3b8").set("font-style", "italic").set("padding", "20px 10px");
                slotsLayout.add(emptySlot);
            } else {
                for (Map<String, Object> slot : slots) {
                    Component slotCard = buildSlotCard(slot);
                    slotsLayout.add(slotCard);
                }
            }

            wcCard.add(slotsLayout);

            DropTarget<VerticalLayout> dropTarget = DropTarget.create(wcCard);
            dropTarget.setDropEffect(DropEffect.MOVE);
            dropTarget.setActive(true);
            final String targetWcCode = wcCode;
            final String targetWcName = wcName;
            dropTarget.addDropListener(event -> {
                if (event.getDragSourceComponent().isPresent() && event.getDragData().isPresent()) {
                    Object data = event.getDragData().get();
                    if (data instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> draggedSlot = (Map<String, Object>) data;
                        Long slotId = ((Number) draggedSlot.get("id")).longValue();
                        String oldWcCode = (String) draggedSlot.get("work_center_code");
                        if (!targetWcCode.equals(oldWcCode)) {
                            jdbcTemplate.update("UPDATE trx_production_schedule SET work_center_code = ?, status = 'SCHEDULED' WHERE id = ?", targetWcCode, slotId);
                            Notification.show("✅ WO Schedule #" + draggedSlot.get("wo_no") + " (" + draggedSlot.get("operation_name") + ") berhasil dipindahkan ke Mesin: [" + targetWcCode + "] " + targetWcName, 4000, Notification.Position.BOTTOM_END)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                            refreshAllViews();
                        }
                    }
                }
            });

            timelineContainer.add(wcCard);
        }
    }

    private Component buildSlotCard(Map<String, Object> slot) {
        Long id = ((Number) slot.get("id")).longValue();
        String woNo = (String) slot.get("wo_no");
        String opName = (String) slot.get("operation_name");
        String status = (String) slot.get("status");
        String notes = (String) slot.get("notes");
        LocalDateTime start = parseToLocalDateTime(slot.get("start_time"));
        LocalDateTime end = parseToLocalDateTime(slot.get("end_time"));

        VerticalLayout card = new VerticalLayout();
        card.setWidth("280px");
        card.setPadding(true);
        card.setSpacing(false);

        String borderColor = "#6366f1";
        String bgColor = "#eff6ff";
        String statusText = status;
        String badgeBg = "#dbeafe";
        String badgeColor = "#1e40af";

        if ("RUNNING".equalsIgnoreCase(status)) {
            borderColor = "#eab308";
            bgColor = "#fefce8";
            statusText = "🔥 RUNNING (Sedang Proses)";
            badgeBg = "#fef08a";
            badgeColor = "#854d0e";
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            borderColor = "#22c55e";
            bgColor = "#f0fdf4";
            statusText = "✅ COMPLETED (Selesai)";
            badgeBg = "#bbf7d0";
            badgeColor = "#166534";
        }

        card.getStyle()
                .set("border-left", "5px solid " + borderColor)
                .set("border-top", "1px solid #e2e8f0")
                .set("border-right", "1px solid #e2e8f0")
                .set("border-bottom", "1px solid #e2e8f0")
                .set("background", bgColor)
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)")
                .set("flex-shrink", "0")
                .set("cursor", "pointer")
                .set("transition", "transform 0.15s ease");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        top.setAlignItems(FlexComponent.Alignment.CENTER);

        Span badgeWo = new Span(woNo);
        badgeWo.getStyle().set("font-weight", "bold").set("color", "#0f172a").set("font-size", "13px");

        Span badgeSt = new Span(statusText);
        badgeSt.getStyle().set("background", badgeBg).set("color", badgeColor).set("padding", "2px 6px").set("border-radius", "4px").set("font-size", "11px").set("font-weight", "600");

        top.add(badgeWo, badgeSt);

        Div opTitle = new Div();
        opTitle.setText(opName);
        opTitle.getStyle().set("font-weight", "600").set("color", "#334155").set("font-size", "14px").set("margin", "6px 0 4px 0");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Span timeSpan = new Span("🕒 " + (start != null ? fmt.format(start) : "-") + " s.d. " + (end != null ? fmt.format(end) : "-"));
        timeSpan.getStyle().set("color", "#64748b").set("font-size", "12px");

        card.add(top, opTitle, timeSpan);

        String heatNo = (String) slot.get("heat_no");
        String alloyGrade = (String) slot.get("alloy_grade");
        Double pourWeight = slot.get("pour_weight_kg") != null ? ((Number) slot.get("pour_weight_kg")).doubleValue() : 0.0;

        if ((heatNo != null && !heatNo.isBlank()) || (alloyGrade != null && !alloyGrade.isBlank()) || pourWeight > 0) {
            HorizontalLayout foundryBox = new HorizontalLayout();
            foundryBox.setSpacing(true);
            foundryBox.getStyle().set("margin", "4px 0").set("flex-wrap", "wrap");

            if (heatNo != null && !heatNo.isBlank()) {
                Span badgeHeat = new Span("🔥 " + heatNo);
                badgeHeat.getStyle().set("background", "#ffedd5").set("color", "#c2410c").set("padding", "1px 6px").set("border-radius", "4px").set("font-size", "11px").set("font-weight", "700");
                foundryBox.add(badgeHeat);
            }
            if (alloyGrade != null && !alloyGrade.isBlank()) {
                Span badgeGrade = new Span("🧪 " + alloyGrade);
                badgeGrade.getStyle().set("background", "#f3e8ff").set("color", "#6b21a8").set("padding", "1px 6px").set("border-radius", "4px").set("font-size", "11px").set("font-weight", "600");
                foundryBox.add(badgeGrade);
            }
            if (pourWeight > 0) {
                Span badgeTon = new Span(String.format("⚖️ %.2f Ton", pourWeight / 1000.0));
                badgeTon.getStyle().set("background", "#ecfdf5").set("color", "#047857").set("padding", "1px 6px").set("border-radius", "4px").set("font-size", "11px").set("font-weight", "600");
                foundryBox.add(badgeTon);
            }
            card.add(foundryBox);
        }

        if (notes != null && !notes.isBlank()) {
            Span noteSpan = new Span("📝 " + notes);
            noteSpan.getStyle().set("color", "#64748b").set("font-size", "11px").set("font-style", "italic").set("margin-top", "4px");
            card.add(noteSpan);
        }

        // Action Toolbar inside slot card
        HorizontalLayout cardActions = new HorizontalLayout();
        cardActions.setWidthFull();
        cardActions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        cardActions.getStyle().set("margin-top", "8px");

        if ("SCHEDULED".equalsIgnoreCase(status)) {
            Button btnStart = new Button("▶ Start", e -> updateSlotStatus(id, "RUNNING"));
            btnStart.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnStart.getStyle().set("background", "#eab308").set("border-radius", "4px");
            cardActions.add(btnStart);
        } else if ("RUNNING".equalsIgnoreCase(status)) {
            Button btnFinish = new Button("✅ Finish", e -> updateSlotStatus(id, "COMPLETED"));
            btnFinish.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            cardActions.add(btnFinish);
        }

        Button btnEdit = new Button("✏ Geser", e -> openScheduleDialog(slot));
        btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        cardActions.add(btnEdit);

        card.add(cardActions);

        DragSource<VerticalLayout> dragSource = DragSource.create(card);
        dragSource.setDragData(slot);
        card.getElement().setAttribute("title", "💡 Drag & Drop this card to another workstation/machine above/below to reschedule");
        card.getStyle().set("cursor", "grab");

        return card;
    }

    // TAB 2: LOAD & BOTTLENECK MONITOR VIEW
    private void refreshLoadMonitorView() {
        loadMonitorContainer.removeAll();

        HorizontalLayout summaryBar = new HorizontalLayout();
        summaryBar.setWidthFull();
        summaryBar.setSpacing(true);

        List<Map<String, Object>> workCenters = getFilteredWorkCenters();
        for (Map<String, Object> wc : workCenters) {
            String wcCode = (String) wc.get("code");
            String wcName = (String) wc.get("name");
            Integer dailyCap = ((Number) wc.get("capacity_hours")).intValue();

            // Calculate total scheduled hours for today/upcoming
            double scheduledHours = 0.0;
            try {
                List<Map<String, Object>> slots = jdbcTemplate.queryForList(
                        "SELECT start_time, end_time FROM trx_production_schedule WHERE work_center_code = ? AND status != 'COMPLETED'", wcCode);
                for (Map<String, Object> s : slots) {
                    LocalDateTime st = parseToLocalDateTime(s.get("start_time"));
                    LocalDateTime en = parseToLocalDateTime(s.get("end_time"));
                    if (st != null && en != null) {
                        scheduledHours += java.time.Duration.between(st, en).toMinutes() / 60.0;
                    }
                }
            } catch (Exception ignored) {}

            int loadPct = (int) Math.round((scheduledHours / dailyCap) * 100);

            VerticalLayout loadCard = new VerticalLayout();
            loadCard.setWidth("240px");
            loadCard.setPadding(true);
            loadCard.getStyle()
                    .set("border-radius", "12px")
                    .set("border", "1px solid #cbd5e1")
                    .set("background", "white")
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.03)");

            String color = "#10b981"; // Green
            String statusNote = "🟢 Normal Capacity";
            if (loadPct > 100) {
                color = "#ef4444"; // Red overload
                statusNote = "🔴 OVERLOAD (Bottleneck!)";
            } else if (loadPct >= 80) {
                color = "#f59e0b"; // Orange
                statusNote = "🟡 High Utilization";
            }

            H3 pctText = new H3(loadPct + "%");
            pctText.getStyle().set("color", color).set("margin", "0").set("font-size", "28px");

            Span nameSpan = new Span("[" + wcCode + "] " + wcName);
            nameSpan.getStyle().set("font-weight", "600").set("color", "#1e293b").set("font-size", "14px");

            Span detailSpan = new Span("Antrean: " + String.format("%.1f", scheduledHours) + " Jam dari Kapasitas " + dailyCap + " Jam");
            detailSpan.getStyle().set("color", "#64748b").set("font-size", "12px");

            Span badgeStatus = new Span(statusNote);
            badgeStatus.getStyle().set("font-weight", "bold").set("color", color).set("font-size", "12px").set("margin-top", "6px");

            loadCard.add(nameSpan, pctText, detailSpan, badgeStatus);
            summaryBar.add(loadCard);
        }

        loadMonitorContainer.add(new H4("📈 Capacity Utilization Dashboard (Antrean Aktif & Bottleneck Check)"));
        loadMonitorContainer.add(summaryBar);

        // Detailed table below
        loadMonitorContainer.add(new H4("📋 Rincian Jadwal Produksi Aktif per Mesin"));
        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeight("400px");
        grid.addColumn(m -> m.get("work_center_code")).setHeader("Machine / Station").setAutoWidth(true);
        grid.addColumn(m -> m.get("wo_no")).setHeader("WO No.").setAutoWidth(true);
        grid.addColumn(m -> m.get("operation_name")).setHeader("Process / Stage").setAutoWidth(true);
        grid.addColumn(m -> {
            LocalDateTime st = parseToLocalDateTime(m.get("start_time"));
            return st != null ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(st) : "-";
        }).setHeader("Start Time").setAutoWidth(true);
        grid.addColumn(m -> {
            LocalDateTime en = parseToLocalDateTime(m.get("end_time"));
            return en != null ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(en) : "-";
        }).setHeader("End Time").setAutoWidth(true);
        grid.addColumn(m -> m.get("status")).setHeader("Status").setAutoWidth(true);

        try {
            grid.setItems(jdbcTemplate.queryForList("SELECT * FROM trx_production_schedule ORDER BY start_time ASC"));
        } catch (Exception ignored) {}

        loadMonitorContainer.add(grid);
    }

    // TAB 3: WORK ORDER LIST VIEW
    private void refreshWoListView() {
        woListContainer.removeAll();

        HorizontalLayout woTop = new HorizontalLayout();
        woTop.setWidthFull();
        woTop.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        woTop.setAlignItems(FlexComponent.Alignment.CENTER);

        H4 title = new H4("📋 Master Work Order (Surat Perintah Kerja Produksi)");
        title.getStyle().set("margin", "0");

        Button btnAddWo = new Button("➕ Create New Work Order", e -> openWorkOrderDialog());
        btnAddWo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        woTop.add(title, btnAddWo);
        woListContainer.add(woTop);

        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeight("450px");
        grid.addColumn(m -> m.get("wo_no")).setHeader("WO No.").setAutoWidth(true);
        grid.addColumn(m -> m.get("product_name")).setHeader("Finished Goods").setAutoWidth(true);
        grid.addColumn(m -> m.get("alloy_grade") != null ? m.get("alloy_grade") : "-").setHeader("Alloy Grade").setAutoWidth(true);
        grid.addColumn(m -> m.get("target_qty")).setHeader("Target Qty").setAutoWidth(true);
        grid.addColumn(m -> m.get("total_pour_weight_kg") != null ? String.format("%.2f Kg", ((Number) m.get("total_pour_weight_kg")).doubleValue()) : "-").setHeader("Total Pour Weight").setAutoWidth(true);
        grid.addColumn(m -> {
            LocalDate dt = parseToLocalDate(m.get("due_date"));
            return dt != null ? DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dt) : "-";
        }).setHeader("Due Date").setAutoWidth(true);
        grid.addColumn(m -> m.get("status")).setHeader("Work Order Status").setAutoWidth(true);
        grid.addComponentColumn(m -> {
            Button btnBuatJadwal = new Button("➕ Jadwalkan Mesin", e -> {
                Map<String, Object> pre = new HashMap<>();
                pre.put("wo_no", m.get("wo_no"));
                openScheduleDialog(pre);
            });
            btnBuatJadwal.addThemeVariants(ButtonVariant.LUMO_SMALL);
            return btnBuatJadwal;
        }).setHeader("Action");

        try {
            grid.setItems(jdbcTemplate.queryForList("SELECT * FROM trx_work_order ORDER BY wo_no ASC"));
        } catch (Exception ignored) {}

        woListContainer.add(grid);
    }

    private void updateSlotStatus(Long id, String newStatus) {
        try {
            jdbcTemplate.update("UPDATE trx_production_schedule SET status = ? WHERE id = ?", newStatus, id);
            Notification.show("Production schedule status successfully changed to: " + newStatus, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshAllViews();
        } catch (Exception ex) {
            Notification.show("Error update status: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openScheduleDialog(Map<String, Object> existingOrPre) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingOrPre != null && existingOrPre.get("id") != null ? "✏ Edit / Move Production Schedule" : "➕ Create New Machine Schedule Slot");
        dialog.setWidth("520px");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        ComboBox<String> fieldWo = new ComboBox<>("Surat Perintah Kerja (SPK)");
        List<String> woNos = new ArrayList<>();
        try {
            List<Map<String, Object>> wos = jdbcTemplate.queryForList("SELECT wo_no, product_name FROM trx_work_order ORDER BY wo_no");
            for (Map<String, Object> w : wos) woNos.add((String) w.get("wo_no"));
        } catch (Exception ignored) {}
        fieldWo.setItems(woNos);

        ComboBox<String> fieldWc = new ComboBox<>("Mesin / Stasiun Kerja");
        List<String> wcCodes = new ArrayList<>();
        try {
            List<Map<String, Object>> wcs = jdbcTemplate.queryForList("SELECT code, name FROM mst_work_center ORDER BY code");
            for (Map<String, Object> w : wcs) wcCodes.add((String) w.get("code"));
        } catch (Exception ignored) {}
        fieldWc.setItems(wcCodes);

        TextField fieldOp = new TextField("Nama Proses / Operasi");
        fieldOp.setPlaceholder("e.g. Cutting & Profiling Besi");

        DateTimePicker fieldStart = new DateTimePicker("Waktu Mulai (Start Time)");
        fieldStart.setStep(java.time.Duration.ofMinutes(15));
        DateTimePicker fieldEnd = new DateTimePicker("Waktu Selesai (End Time)");
        fieldEnd.setStep(java.time.Duration.ofMinutes(15));

        ComboBox<String> fieldStatus = new ComboBox<>("Slot Status");
        fieldStatus.setItems("SCHEDULED", "RUNNING", "COMPLETED");
        fieldStatus.setValue("SCHEDULED");

        TextField fieldHeatNo = new TextField("🔥 No. Batch / Heat Pengecoran (Foundry)");
        fieldHeatNo.setPlaceholder("e.g. HEAT-101 / EAF-201");

        ComboBox<String> fieldAlloyGrade = new ComboBox<>("🧪 Grade Logam (Alloy Specification)");
        fieldAlloyGrade.setItems("Hadfield Mn18Cr2", "Hadfield Mn13Cr2", "High Chrome Cr20Mo2", "High Chrome Cr15Mo3", "Chrome-Moly Steel", "Low Alloy Steel");
        fieldAlloyGrade.setPlaceholder("Select / Type Metal Grade...");
        fieldAlloyGrade.setAllowCustomValue(true);
        fieldAlloyGrade.addCustomValueSetListener(e -> fieldAlloyGrade.setValue(e.getDetail()));

        com.vaadin.flow.component.textfield.NumberField fieldPourWeight = new com.vaadin.flow.component.textfield.NumberField("⚖️ Bobot Cor / Pouring Weight (Kg)");
        fieldPourWeight.setPlaceholder("e.g. 1450.00");
        fieldPourWeight.setStep(1.0);

        TextArea fieldNotes = new TextArea("Catatan / Instruksi Operator");

        if (existingOrPre != null) {
            if (existingOrPre.get("wo_no") != null) fieldWo.setValue((String) existingOrPre.get("wo_no"));
            if (existingOrPre.get("work_center_code") != null) fieldWc.setValue((String) existingOrPre.get("work_center_code"));
            if (existingOrPre.get("operation_name") != null) fieldOp.setValue((String) existingOrPre.get("operation_name"));
            if (existingOrPre.get("start_time") != null) fieldStart.setValue(parseToLocalDateTime(existingOrPre.get("start_time")));
            if (existingOrPre.get("end_time") != null) fieldEnd.setValue(parseToLocalDateTime(existingOrPre.get("end_time")));
            if (existingOrPre.get("status") != null) fieldStatus.setValue((String) existingOrPre.get("status"));
            if (existingOrPre.get("notes") != null) fieldNotes.setValue((String) existingOrPre.get("notes"));
            if (existingOrPre.get("heat_no") != null) fieldHeatNo.setValue((String) existingOrPre.get("heat_no"));
            if (existingOrPre.get("alloy_grade") != null) fieldAlloyGrade.setValue((String) existingOrPre.get("alloy_grade"));
            if (existingOrPre.get("pour_weight_kg") != null) fieldPourWeight.setValue(((Number) existingOrPre.get("pour_weight_kg")).doubleValue());
        } else {
            fieldStart.setValue(LocalDateTime.now().withMinute(0));
            fieldEnd.setValue(LocalDateTime.now().withMinute(0).plusHours(4));
        }

        form.add(fieldWo, fieldWc, fieldOp, fieldStart, fieldEnd, fieldStatus, fieldHeatNo, fieldAlloyGrade, fieldPourWeight, fieldNotes);
        dialog.add(form);

        Button btnSave = new Button("Save Schedule", e -> {
            if (fieldWo.isEmpty() || fieldWc.isEmpty() || fieldOp.isEmpty() || fieldStart.isEmpty() || fieldEnd.isEmpty()) {
                Notification.show("Mohon lengkapi data SPK, Mesin, Nama Operasi, serta Waktu Mulai & Selesai!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                Double pourW = fieldPourWeight.getValue() != null ? fieldPourWeight.getValue() : 0.0;
                if (existingOrPre != null && existingOrPre.get("id") != null) {
                    Long id = ((Number) existingOrPre.get("id")).longValue();
                    jdbcTemplate.update("UPDATE trx_production_schedule SET wo_no = ?, work_center_code = ?, operation_name = ?, start_time = ?, end_time = ?, status = ?, notes = ?, heat_no = ?, alloy_grade = ?, pour_weight_kg = ? WHERE id = ?",
                            fieldWo.getValue(), fieldWc.getValue(), fieldOp.getValue(), fieldStart.getValue(), fieldEnd.getValue(), fieldStatus.getValue(), fieldNotes.getValue(), fieldHeatNo.getValue(), fieldAlloyGrade.getValue(), pourW, id);
                } else {
                    jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            fieldWo.getValue(), fieldWc.getValue(), fieldOp.getValue(), fieldStart.getValue(), fieldEnd.getValue(), fieldStatus.getValue(), fieldNotes.getValue(), fieldHeatNo.getValue(), fieldAlloyGrade.getValue(), pourW);
                }
                Notification.show("Production schedule saved & machine capacity updated!", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshAllViews();

            } catch (Exception ex) {
                Notification.show("Error save: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(btnCancel, btnSave);
        dialog.open();
    }

    private void openWorkOrderDialog() {
        Dialog dialog = new Dialog("➕ Create Work Order (Production WO)");
        dialog.setWidth("450px");

        FormLayout form = new FormLayout();
        TextField fieldWo = new TextField("No. SPK");
        fieldWo.setValue("WO-2026-" + (System.currentTimeMillis() % 1000));
        TextField fieldProd = new TextField("Produk Jadi (Finished Goods)");
        fieldProd.setPlaceholder("e.g. Meja Kerja Industrial 140cm");
        TextField fieldQty = new TextField("Target Kuantitas (Pcs)");
        fieldQty.setValue("100");
        com.vaadin.flow.component.datepicker.DatePicker fieldDue = new com.vaadin.flow.component.datepicker.DatePicker("Tanggal Batas Waktu (Due Date)");
        fieldDue.setValue(LocalDate.now().plusDays(7));

        form.add(fieldWo, fieldProd, fieldQty, fieldDue);
        dialog.add(form);

        Button btnSave = new Button("Save Work Order", e -> {
            try {
                int qty = Integer.parseInt(fieldQty.getValue().trim());
                jdbcTemplate.update("INSERT INTO trx_work_order (wo_no, product_name, target_qty, due_date, status) VALUES (?, ?, ?, ?, 'SCHEDULED')",
                        fieldWo.getValue(), fieldProd.getValue(), qty, fieldDue.getValue());
                Notification.show("New Work Order created successfully!", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshAllViews();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(btnCancel, btnSave);
        dialog.open();
    }

    private List<Map<String, Object>> getFilteredWorkCenters() {
        String filter = filterWorkCenter.getValue();
        try {
            if (filter != null && !filter.equals("SEMUA MESIN / STASIUN")) {
                String code = filter.split(" - ")[0].trim();
                return jdbcTemplate.queryForList("SELECT * FROM mst_work_center WHERE code = ? ORDER BY code", code);
            }
            return jdbcTemplate.queryForList("SELECT * FROM mst_work_center ORDER BY code");
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> getSchedulesForWorkCenter(String wcCode) {
        String stFilter = filterStatus.getValue();
        StringBuilder sql = new StringBuilder("SELECT * FROM trx_production_schedule WHERE work_center_code = ? ");
        List<Object> args = new ArrayList<>();
        args.add(wcCode);

        if (stFilter != null && !stFilter.equals("SEMUA STATUS")) {
            sql.append("AND status = ? ");
            args.add(stFilter);
        }
        sql.append("ORDER BY start_time ASC");

        try {
            return jdbcTemplate.queryForList(sql.toString(), args.toArray());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void initAndSeedManufacturingData(boolean forceReset) {
        try {
            Integer meltCheck = 0;
            try {
                meltCheck = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mst_work_center WHERE code = 'WC-MELT-01'", Integer.class);
            } catch (Exception ignored) {}

            if (forceReset || meltCheck == null || meltCheck == 0) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS trx_production_schedule CASCADE");
                jdbcTemplate.execute("DROP TABLE IF EXISTS trx_work_order CASCADE");
                jdbcTemplate.execute("DROP TABLE IF EXISTS mst_work_center CASCADE");
            }

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS mst_work_center (" +
                    "id SERIAL PRIMARY KEY, " +
                    "code VARCHAR(50) UNIQUE, " +
                    "name VARCHAR(150), " +
                    "capacity_hours INT, " +
                    "capacity_tonnage DECIMAL(10,2) DEFAULT 0, " +
                    "status VARCHAR(20) DEFAULT 'ACTIVE')");
            try { jdbcTemplate.execute("ALTER TABLE mst_work_center ADD COLUMN IF NOT EXISTS capacity_tonnage DECIMAL(10,2) DEFAULT 0"); } catch (Exception ignored) {}

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS trx_work_order (" +
                    "id SERIAL PRIMARY KEY, " +
                    "wo_no VARCHAR(50) UNIQUE, " +
                    "product_name VARCHAR(200), " +
                    "target_qty INT, " +
                    "due_date DATE, " +
                    "status VARCHAR(20) DEFAULT 'SCHEDULED', " +
                    "alloy_grade VARCHAR(100), " +
                    "weight_per_pc_kg DECIMAL(10,2) DEFAULT 0, " +
                    "total_pour_weight_kg DECIMAL(10,2) DEFAULT 0)");
            try {
                jdbcTemplate.execute("ALTER TABLE trx_work_order ADD COLUMN IF NOT EXISTS alloy_grade VARCHAR(100)");
                jdbcTemplate.execute("ALTER TABLE trx_work_order ADD COLUMN IF NOT EXISTS weight_per_pc_kg DECIMAL(10,2) DEFAULT 0");
                jdbcTemplate.execute("ALTER TABLE trx_work_order ADD COLUMN IF NOT EXISTS total_pour_weight_kg DECIMAL(10,2) DEFAULT 0");
            } catch (Exception ignored) {}

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS trx_production_schedule (" +
                    "id SERIAL PRIMARY KEY, " +
                    "wo_no VARCHAR(50), " +
                    "work_center_code VARCHAR(50), " +
                    "operation_name VARCHAR(150), " +
                    "start_time TIMESTAMP, " +
                    "end_time TIMESTAMP, " +
                    "status VARCHAR(20) DEFAULT 'SCHEDULED', " +
                    "notes TEXT, " +
                    "heat_no VARCHAR(50), " +
                    "alloy_grade VARCHAR(100), " +
                    "pour_weight_kg DECIMAL(10,2) DEFAULT 0, " +
                    "cooling_hours INT DEFAULT 48)");
            try {
                jdbcTemplate.execute("ALTER TABLE trx_production_schedule ADD COLUMN IF NOT EXISTS heat_no VARCHAR(50)");
                jdbcTemplate.execute("ALTER TABLE trx_production_schedule ADD COLUMN IF NOT EXISTS alloy_grade VARCHAR(100)");
                jdbcTemplate.execute("ALTER TABLE trx_production_schedule ADD COLUMN IF NOT EXISTS pour_weight_kg DECIMAL(10,2) DEFAULT 0");
                jdbcTemplate.execute("ALTER TABLE trx_production_schedule ADD COLUMN IF NOT EXISTS cooling_hours INT DEFAULT 48");
            } catch (Exception ignored) {}

            Integer wcCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mst_work_center", Integer.class);
            if (wcCount == null || wcCount == 0) {
                // Seed Work Centers (Foundry Mill Liner & General)
                jdbcTemplate.update("INSERT INTO mst_work_center (code, name, capacity_hours, capacity_tonnage, status) VALUES (?, ?, ?, ?, ?)",
                        "WC-MOLD-01", "Stasiun Molding & Core Shop (Sand Box 4x4m)", 16, 25.0, "ACTIVE");
                jdbcTemplate.update("INSERT INTO mst_work_center (code, name, capacity_hours, capacity_tonnage, status) VALUES (?, ?, ?, ?, ?)",
                        "WC-MELT-01", "Induction Furnace A (Max 5.0 Ton / Heat Tapping)", 24, 5.0, "ACTIVE");
                jdbcTemplate.update("INSERT INTO mst_work_center (code, name, capacity_hours, capacity_tonnage, status) VALUES (?, ?, ?, ?, ?)",
                        "WC-MELT-02", "Electric Arc Furnace B - EAF (Max 10.0 Ton / Heat)", 24, 10.0, "ACTIVE");
                jdbcTemplate.update("INSERT INTO mst_work_center (code, name, capacity_hours, capacity_tonnage, status) VALUES (?, ?, ?, ?, ?)",
                        "WC-COOL-01", "In-Mold Solidification & Cooling Yard (24-72 Jam)", 24, 50.0, "ACTIVE");
                jdbcTemplate.update("INSERT INTO mst_work_center (code, name, capacity_hours, capacity_tonnage, status) VALUES (?, ?, ?, ?, ?)",
                        "WC-FETT-01", "Shakeout, Fettling & Arc-Air Riser Cutting", 16, 15.0, "ACTIVE");
                jdbcTemplate.update("INSERT INTO mst_work_center (code, name, capacity_hours, capacity_tonnage, status) VALUES (?, ?, ?, ?, ?)",
                        "WC-HT-01", "Heat Treatment Furnace (Q&T - Batch Max 15 Ton)", 24, 15.0, "ACTIVE");
                jdbcTemplate.update("INSERT INTO mst_work_center (code, name, capacity_hours, capacity_tonnage, status) VALUES (?, ?, ?, ?, ?)",
                        "WC-MACH-01", "CNC Boring & Milling (Bolt Hole Beveling)", 16, 10.0, "ACTIVE");

                // Seed Work Orders (Foundry Mill Liner)
                jdbcTemplate.update("INSERT INTO trx_work_order (wo_no, product_name, target_qty, due_date, status, alloy_grade, weight_per_pc_kg, total_pour_weight_kg) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-001", "Shell Liner SAG Mill 36x17ft (Part #SL-092)", 2, LocalDate.now().plusDays(5), "IN_PROGRESS", "Hadfield Mn18Cr2", 1450.0, 3770.0);
                jdbcTemplate.update("INSERT INTO trx_work_order (wo_no, product_name, target_qty, due_date, status, alloy_grade, weight_per_pc_kg, total_pour_weight_kg) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-002", "Feed End Liner Ball Mill 24x14ft (Part #FL-114)", 4, LocalDate.now().plusDays(8), "SCHEDULED", "Hadfield Mn18Cr2", 420.0, 2184.0);
                jdbcTemplate.update("INSERT INTO trx_work_order (wo_no, product_name, target_qty, due_date, status, alloy_grade, weight_per_pc_kg, total_pour_weight_kg) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-003", "Discharge Grate SAG Mill (Part #DG-005 Heavy)", 1, LocalDate.now().plusDays(10), "SCHEDULED", "High Chrome Cr20Mo2", 2800.0, 3640.0);
                jdbcTemplate.update("INSERT INTO trx_work_order (wo_no, product_name, target_qty, due_date, status, alloy_grade, weight_per_pc_kg, total_pour_weight_kg) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-004", "Lifter Bar Mining Mill (Part #LB-881 High Chrome)", 2, LocalDate.now().plusDays(12), "SCHEDULED", "High Chrome Cr20Mo2", 650.0, 1690.0);

                // Seed Schedules (realistic dates & hours around today)
                LocalDateTime now = LocalDateTime.now().withMinute(0);

                // WO-ML-001 schedules (Hadfield Mn18Cr2 in Induction Furnace A - Heat #H-101)
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-001", "WC-MOLD-01", "Molding Cope & Drag + Core Setting (2 Pcs)", now.minusHours(6), now.minusHours(2), "COMPLETED", "Pasir Resin CO2, Flask #Box-A2", "HEAT-101", "Hadfield Mn18Cr2", 3770.0, 48);
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-001", "WC-MELT-01", "Tapping & Pouring Heat #HEAT-101 (Hadfield Mn18)", now.minusHours(1), now.plusHours(2), "RUNNING", "Suhu Tapping 1480°C. Bobot Cor: 3.77 Ton", "HEAT-101", "Hadfield Mn18Cr2", 3770.0, 48);
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-001", "WC-COOL-01", "In-Mold Solidification & Cooling 48 Jam", now.plusHours(2), now.plusDays(2).plusHours(2), "SCHEDULED", "Tahan di dalam cetakan hingga suhu < 200°C sebelum Shakeout", "HEAT-101", "Hadfield Mn18Cr2", 3770.0, 48);
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-001", "WC-FETT-01", "Shakeout Pasir & Pemotongan Riser Arc-Air Carbon", now.plusDays(2).plusHours(3), now.plusDays(2).plusHours(8), "SCHEDULED", "Gerinda permukaan & pembersihan shot blasting", "HEAT-101", "Hadfield Mn18Cr2", 3770.0, 48);
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-001", "WC-HT-01", "Heat Treatment Q&T Austenitizing 1050°C", now.plusDays(3).withHour(8), now.plusDays(4).withHour(8), "SCHEDULED", "Water Quench cepat dalam waktu max 60 detik setelah keluar oven", "HEAT-101", "Hadfield Mn18Cr2", 3770.0, 48);

                // WO-ML-002 schedules (same Heat #HEAT-101 in Induction Furnace A)
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-002", "WC-MOLD-01", "Molding Green Sand (4 Pcs Feed End Liner)", now.minusHours(4), now, "COMPLETED", "Siap untuk digabung di Tungku A dengan Shell Liner", "HEAT-101", "Hadfield Mn18Cr2", 2184.0, 36);
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-002", "WC-MELT-01", "Tapping & Pouring Heat #HEAT-101 (Hadfield Mn18)", now.plusHours(2), now.plusHours(4), "SCHEDULED", "Tapping tahap 2 dari Tungku A. Bobot Cor 2.18 Ton. Total Tungku Load: 5.95 / 5.0 Ton", "HEAT-101", "Hadfield Mn18Cr2", 2184.0, 36);

                // WO-ML-003 & 004 schedules in EAF Furnace B (#HEAT-201 - High Chrome)
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-003", "WC-MELT-02", "EAF Tapping Heat #HEAT-201 (High Chrome White Iron)", now.plusDays(1).withHour(8), now.plusDays(1).withHour(12), "SCHEDULED", "Grade Cr20Mo2. Suhu tuang 1420°C. Bobot Cor 3.64 Ton", "HEAT-201", "High Chrome Cr20Mo2", 3640.0, 60);
                jdbcTemplate.update("INSERT INTO trx_production_schedule (wo_no, work_center_code, operation_name, start_time, end_time, status, notes, heat_no, alloy_grade, pour_weight_kg, cooling_hours) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "WO-ML-2026-004", "WC-MELT-02", "EAF Tapping Heat #HEAT-201 (High Chrome White Iron)", now.plusDays(1).withHour(13), now.plusDays(1).withHour(16), "SCHEDULED", "Grade Cr20Mo2. Bobot Cor 1.69 Ton. Total EAF Load: 5.33 / 10 Ton", "HEAT-201", "High Chrome Cr20Mo2", 1690.0, 36);
            }
        } catch (Exception e) {
            System.err.println("Error seeding manufacturing data: " + e.getMessage());
        }
    }

    private LocalDateTime parseToLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
        if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate().atStartOfDay();
        if (obj instanceof java.util.Date) return new java.sql.Timestamp(((java.util.Date) obj).getTime()).toLocalDateTime();
        if (obj instanceof String) {
            String str = ((String) obj).trim();
            if (str.isEmpty()) return null;
            try {
                return LocalDateTime.parse(str);
            } catch (Exception e) {
                try {
                    return java.sql.Timestamp.valueOf(str).toLocalDateTime();
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    private LocalDate parseToLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDate) return (LocalDate) obj;
        if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate();
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime().toLocalDate();
        if (obj instanceof java.util.Date) return new java.sql.Date(((java.util.Date) obj).getTime()).toLocalDate();
        if (obj instanceof String) {
            String str = ((String) obj).trim();
            if (str.isEmpty()) return null;
            try {
                return LocalDate.parse(str);
            } catch (Exception e) {
                try {
                    return java.sql.Date.valueOf(str).toLocalDate();
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
