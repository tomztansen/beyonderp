package com.vaadinerp.dashboard;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import com.vaadinerp.components.ComponentFactory;
import com.vaadinerp.dashboard.DashboardModel.*;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.report.ReportAccessService;
import com.vaadinerp.report.ReportDataService;
import com.vaadinerp.service.DynamicDataService;

/**
 * Dashboard generik: bar filter (parameter + chip + Refresh), grid 12 kolom berisi kartu widget.
 * Semua query lewat ReportDataService; hak widget = hak report. Refresh manual di tahap 1.
 * Tidak ada thread/state statis; refresh hanya setData pada komponen yang ada.
 */
public class DynamicDashboardView extends VerticalLayout {
    private static final Logger log = LoggerFactory.getLogger(DynamicDashboardView.class);

    private final MergedDashboard def;
    private final ReportDataService reportData;
    private final ReportMetaRepository reportRepo;
    private final ReportAccessService access;
    private final DynamicDataService dynamicDataService;

    private final FilterState filters = new FilterState();
    private final Set<String> declaredParams = new LinkedHashSet<>();
    private final Map<String, Supplier<Object>> paramReaders = new LinkedHashMap<>();
    private final FlexLayout chips = new FlexLayout();
    private final Div grid = new Div();
    private final List<CardEntry> cards = new ArrayList<>();

    private record CardEntry(ItemDef item, DashboardWidget widget, DashboardCard card, ReportMeta report) {}

    public DynamicDashboardView(MergedDashboard def, ReportDataService reportData, ReportMetaRepository reportRepo,
            ReportAccessService access, DynamicDataService dynamicDataService) {
        this.def = def;
        this.reportData = reportData;
        this.reportRepo = reportRepo;
        this.access = access;
        this.dynamicDataService = dynamicDataService;
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#f8fafc").set("overflow", "auto");

        for (ParamDef p : def.params()) declaredParams.add(p.name());
        for (ItemDef it : def.items()) if (it.widget().options().emit() != null) declaredParams.add(it.widget().options().emit());
        for (ItemDef it : def.items()) it.widget().options().listen().stream().filter(s -> s != null && !s.isBlank()).forEach(declaredParams::add);

        add(buildFilterBar());
        chips.getStyle().set("gap", "6px").set("flex-wrap", "wrap").set("margin", "6px 0");
        add(chips);
        grid.getStyle().set("display", "grid").set("grid-template-columns", "repeat(12, minmax(0, 1fr))")
                .set("gap", "12px").set("grid-auto-flow", "dense").set("width", "100%");
        grid.getElement().executeJs(
                "const mq=window.matchMedia('(max-width: 800px)');const f=()=>this.style.gridTemplateColumns=mq.matches?'minmax(0,1fr)':'repeat(12,minmax(0,1fr))';mq.addEventListener('change',f);f();");
        add(grid);
        buildCards();
    }

    // ---------- filter bar ----------
    private Component buildFilterBar() {
        FlexLayout bar = new FlexLayout();
        bar.getStyle().set("gap", "10px").set("flex-wrap", "wrap").set("align-items", "end").set("width", "100%");
        for (ParamDef p : def.params()) {
            Component c = buildParamControl(p);
            if (c != null) bar.add(c);
        }
        Button refresh = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> reloadAll());
        refresh.addThemeVariants(ButtonVariant.LUMO_SMALL);
        Button clear = new Button("Clear filters", e -> { filters.clear(); resetParamControls(); reloadAll(); });
        clear.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        bar.add(refresh, clear);
        return bar;
    }

    private final List<HasValue<?, ?>> paramControls = new ArrayList<>();

    private Component buildParamControl(ParamDef p) {
        String label = p.name();
        switch (p.type()) {
            case "MONTH" -> {
                ComboBox<LocalDate> cb = new ComboBox<>(label);
                List<LocalDate> months = new ArrayList<>();
                YearMonth ym = YearMonth.now();
                for (int i = 0; i < 12; i++) months.add(ym.minusMonths(i).atDay(1));
                cb.setItems(months);
                cb.setItemLabelGenerator(d -> d.format(DateTimeFormatter.ofPattern("MMM yyyy")));
                if ("current".equalsIgnoreCase(p.defaultValue())) cb.setValue(months.get(0));
                cb.addValueChangeListener(e -> onParam(p.name(), e.getValue(), e.getValue() == null ? null : cb.getItemLabelGenerator().apply(e.getValue())));
                paramReaders.put(p.name(), cb::getValue);
                paramControls.add(cb);
                if (cb.getValue() != null) filters.put(p.name(), cb.getValue(), cb.getItemLabelGenerator().apply(cb.getValue()));
                return cb;
            }
            case "DATE" -> {
                DatePicker dp = new DatePicker(label);
                if ("today".equalsIgnoreCase(p.defaultValue())) dp.setValue(LocalDate.now());
                dp.addValueChangeListener(e -> onParam(p.name(), e.getValue(), String.valueOf(e.getValue())));
                paramReaders.put(p.name(), dp::getValue);
                paramControls.add(dp);
                if (dp.getValue() != null) filters.put(p.name(), dp.getValue(), String.valueOf(dp.getValue()));
                return dp;
            }
            case "DATE_RANGE" -> { // dua parameter: <name>_from dan <name>_to
                DatePicker from = new DatePicker(label + " from"), to = new DatePicker(label + " to");
                declaredParams.remove(p.name());
                declaredParams.add(p.name() + "_from");
                declaredParams.add(p.name() + "_to");
                from.addValueChangeListener(e -> onParam(p.name() + "_from", e.getValue(), String.valueOf(e.getValue())));
                to.addValueChangeListener(e -> onParam(p.name() + "_to", e.getValue(), String.valueOf(e.getValue())));
                paramControls.add(from);
                paramControls.add(to);
                Div d = new Div(from, to);
                d.getStyle().set("display", "flex").set("gap", "6px");
                return d;
            }
            case "LOV" -> {
                FieldMeta f = new FieldMeta();
                f.setFieldName(p.name());
                f.setFieldLabel(label);
                f.setComponentType("COMBOBOX");
                f.setLovCode(p.lov());
                Component c = ComponentFactory.create(f, dynamicDataService, (name, val) -> {});
                if (c instanceof HasValue<?, ?> hv) {
                    hv.addValueChangeListener(e -> onParam(p.name(), e.getValue(),
                            c instanceof ComboBox<?> cb && e.getValue() != null ? labelOf(cb, e.getValue()) : String.valueOf(e.getValue())));
                    paramControls.add(hv);
                }
                return c;
            }
            default -> {
                TextField tf = new TextField(label);
                tf.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
                tf.addValueChangeListener(e -> onParam(p.name(), e.getValue(), e.getValue()));
                paramControls.add(tf);
                return tf;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static String labelOf(ComboBox<?> cb, Object value) {
        try {
            return ((ComboBox<Object>) cb).getItemLabelGenerator().apply(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private void resetParamControls() {
        for (HasValue<?, ?> hv : paramControls) hv.clear();
    }

    private void onParam(String dim, Object value, String label) {
        filters.put(dim, value, label);
        applyFilters(dim);
    }

    // ---------- cards ----------
    private void buildCards() {
        for (ItemDef it : def.items()) {
            ReportMeta report = reportRepo.findById(it.widget().reportCode()).orElse(null);
            if (report == null || !access.canAccess(report)) continue; // tidak berhak / report hilang -> dilewati diam-diam
            DashboardWidget widget = switch (it.widget().widgetType()) {
                case "TABLE" -> new TableWidget(it.widget().options());
                default -> new KpiWidget(it.widget().options()); // KPI; tipe chart menyusul tahap 2 (sementara tampil sebagai KPI)
            };
            CardEntry[] holder = new CardEntry[1];
            DashboardCard card = new DashboardCard(it, widget, () -> load(holder[0]));
            card.setDrill(null); // drill-down: tahap 3
            holder[0] = new CardEntry(it, widget, card, report);
            if (it.widget().options().emit() != null) {
                String dim = it.widget().options().emit();
                widget.addSelectListener((v, label) -> {
                    filters.toggle(dim, v, label);
                    applyFilters(dim);
                });
            }
            cards.add(holder[0]);
            grid.add(card);
        }
        if (cards.isEmpty()) {
            Span empty = new Span("No widgets available for your role.");
            empty.getStyle().set("color", "#6b7280");
            grid.add(empty);
        }
        reloadAll();
    }

    public void reloadAll() {
        for (CardEntry c : cards) load(c);
    }

    private void load(CardEntry c) {
        try {
            List<Map<String, Object>> rows = reportData.fetchData(c.report(), filters.paramsFor(declaredParams), false);
            c.widget().setData(rows == null ? List.of() : rows);
            c.widget().highlight(filters.get(c.item().widget().options().emit()));
            c.card().showLoaded();
        } catch (Exception ex) {
            log.warn("Dashboard widget {} failed: {}", c.item().widget().widgetCode(), DashboardCard.rootMessage(ex));
            c.card().showError(DashboardCard.rootMessage(ex));
        }
    }

    private void applyFilters(String changedDim) {
        renderChips();
        for (CardEntry c : cards) {
            WidgetDef w = c.item().widget();
            if (FilterState.reloads(w, changedDim)) load(c);
            else if (FilterState.highlights(w, changedDim)) c.widget().highlight(filters.get(changedDim));
        }
    }

    private void renderChips() {
        chips.removeAll();
        for (Map.Entry<String, String> e : filters.labels().entrySet()) {
            Span chip = new Span(e.getKey() + ": " + e.getValue() + "  ✕");
            chip.getStyle().set("background", "#e0e7ff").set("color", "#3730a3").set("border-radius", "999px")
                    .set("padding", "2px 10px").set("font-size", "0.8rem").set("cursor", "pointer");
            chip.addClickListener(ev -> {
                filters.remove(e.getKey());
                paramReaders.remove(e.getKey());
                applyFilters(e.getKey());
            });
            chips.add(chip);
        }
    }
}
