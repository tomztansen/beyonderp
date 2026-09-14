package com.vaadinerp.dashboard;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

import com.vaadinerp.components.ComponentFactory;
import com.vaadinerp.dashboard.DashboardModel.*;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.report.ReportAccessService;
import com.vaadinerp.report.ReportDataService;
import com.vaadinerp.report.ReportRunService;
import com.vaadinerp.security.service.SessionSecurityService;
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
    private final ReportRunService reportRunService;
    private final SessionSecurityService securityService;
    private final Consumer<String> saver;

    private LayoutPrefs prefs;
    private List<ItemDef> layout;
    private Set<String> hiddenNow;
    private boolean customizing;

    private final FilterState filters = new FilterState();
    private final Set<String> declaredParams = new LinkedHashSet<>();
    private final Map<String, HasValue<?, ?>> paramControlByDim = new LinkedHashMap<>();
    private boolean clearing;
    private final FlexLayout chips = new FlexLayout();
    private final Div customizePanel = new Div();
    private final Div grid = new Div();
    private final List<CardEntry> cards = new ArrayList<>();

    private Registration pollReg;
    private Registration tabReg;
    private final Map<String, Integer> failures = new HashMap<>();

    private record CardEntry(ItemDef item, DashboardWidget widget, DashboardCard card, ReportMeta report,
            Runnable drill, boolean drillNeedsRow) {}

    public DynamicDashboardView(MergedDashboard def, ReportDataService reportData, ReportMetaRepository reportRepo,
            ReportAccessService access, DynamicDataService dynamicDataService,
            ReportRunService reportRunService, SessionSecurityService securityService) {
        this(def, reportData, reportRepo, access, dynamicDataService, reportRunService, securityService, null, null);
    }

    public DynamicDashboardView(MergedDashboard def, ReportDataService reportData, ReportMetaRepository reportRepo,
            ReportAccessService access, DynamicDataService dynamicDataService,
            ReportRunService reportRunService, SessionSecurityService securityService,
            LayoutPrefs initialPrefs, Consumer<String> saver) {
        this.def = def;
        this.reportData = reportData;
        this.reportRepo = reportRepo;
        this.access = access;
        this.dynamicDataService = dynamicDataService;
        this.reportRunService = reportRunService;
        this.securityService = securityService;
        this.saver = saver;
        this.prefs = initialPrefs != null ? initialPrefs : new LayoutPrefs();
        this.layout = this.prefs.apply(def.items());
        this.hiddenNow = new LinkedHashSet<>(this.prefs.hidden);

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
        customizePanel.setVisible(false);
        customizePanel.getStyle().set("display", "flex").set("flex-wrap", "wrap").set("gap", "8px")
                .set("align-items", "center").set("padding", "6px 0");
        add(customizePanel);
        grid.getStyle().set("display", "grid").set("grid-template-columns", "repeat(12, minmax(0, 1fr))")
                .set("gap", "12px").set("grid-auto-flow", "dense").set("width", "100%");
        grid.getElement().executeJs(
                "const mq=window.matchMedia('(max-width: 800px)');const f=()=>this.style.gridTemplateColumns=mq.matches?'minmax(0,1fr)':'repeat(12,minmax(0,1fr))';mq.addEventListener('change',f);f();");
        add(grid);
        buildCards();
    }

    // ---------- lifecycle (poll + tab selection) ----------
    // Vaadin 24.10 TabSheet TIDAK melepas konten tab non-aktif; onAttach/onDetach hanya saat tab dibuka/ditutup.
    // Poll dikendalikan lewat SelectedChangeListener agar berjalan hanya saat tab ini yang dipilih.

    private void startPolling(com.vaadin.flow.component.UI ui) {
        if (def.refreshSeconds() <= 0) return;
        ui.setPollInterval(def.refreshSeconds() * 1000);
        if (pollReg == null) pollReg = ui.addPollListener(ev -> { if (isSelectedTab()) reloadAll(true); });
    }

    /** @param resetInterval false ketika tab yang baru dipilih adalah dashboard poller lain — biarkan ia yang set intervalnya sendiri */
    private void stopPolling(com.vaadin.flow.component.UI ui, boolean resetInterval) {
        if (def.refreshSeconds() <= 0) return;
        if (pollReg != null) { pollReg.remove(); pollReg = null; }
        if (resetInterval) ui.setPollInterval(-1);
    }

    private boolean isSelectedTab() {
        com.vaadin.flow.component.Component p = this;
        while (p.getParent().isPresent()) {
            p = p.getParent().get();
            if (p instanceof com.vaadin.flow.component.tabs.TabSheet ts) {
                return ts.getSelectedTab() != null && ts.getComponent(ts.getSelectedTab()) == this;
            }
        }
        return true; // tidak dalam TabSheet → selalu aktif
    }

    @Override
    protected void onAttach(AttachEvent e) {
        super.onAttach(e);
        if (!e.isInitialAttach()) reloadAll(false);
        com.vaadin.flow.component.Component p = this;
        com.vaadin.flow.component.tabs.TabSheet ts = null;
        while (p.getParent().isPresent()) {
            p = p.getParent().get();
            if (p instanceof com.vaadin.flow.component.tabs.TabSheet found) { ts = found; break; }
        }
        if (ts != null && tabReg == null) {
            final com.vaadin.flow.component.tabs.TabSheet tsF = ts;
            tabReg = ts.addSelectedChangeListener(ev -> {
                boolean sel = ev.getSelectedTab() != null && tsF.getComponent(ev.getSelectedTab()) == this;
                if (sel) {
                    startPolling(e.getUI());
                } else {
                    com.vaadin.flow.component.Component newContent = ev.getSelectedTab() == null ? null : tsF.getComponent(ev.getSelectedTab());
                    boolean otherPolls = newContent instanceof DynamicDashboardView d && d != this && d.def.refreshSeconds() > 0;
                    stopPolling(e.getUI(), !otherPolls);
                }
                if (sel && ev.isFromClient()) reloadAll(false);
            });
        }
        if (isSelectedTab()) startPolling(e.getUI()); else stopPolling(e.getUI(), false);
    }

    @Override
    protected void onDetach(DetachEvent e) {
        if (tabReg != null) { tabReg.remove(); tabReg = null; }
        // Reset interval hanya bila view ini sedang aktif poll; tab background sudah pollReg==null
        // (dimatikan saat deseleksi) — jangan reset interval milik dashboard lain yang sedang aktif.
        boolean wasActive = pollReg != null;
        stopPolling(e.getUI(), wasActive);
        super.onDetach(e);
    }

    // ---------- filter bar ----------
    private Component buildFilterBar() {
        FlexLayout bar = new FlexLayout();
        bar.getStyle().set("gap", "10px").set("flex-wrap", "wrap").set("align-items", "end").set("width", "100%");
        for (ParamDef p : def.params()) {
            Component c = buildParamControl(p);
            if (c != null) bar.add(c);
        }
        Button refresh = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> reloadAll(false));
        refresh.addThemeVariants(ButtonVariant.LUMO_SMALL);
        Button clear = new Button("Clear filters", e -> {
            clearing = true;
            try { filters.clear(); resetParamControls(); } finally { clearing = false; }
            reloadAll(false);
        });
        clear.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        bar.add(refresh, clear);
        if (saver != null) {
            Button customize = new Button("Customize", VaadinIcon.WRENCH.create(), e -> enterCustomize());
            customize.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            bar.add(customize);
        }
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
                if (prefs.params.containsKey(p.name())) {
                    try {
                        LocalDate saved = LocalDate.parse(prefs.params.get(p.name()));
                        if (months.contains(saved)) cb.setValue(saved);
                        else if ("current".equalsIgnoreCase(p.defaultValue())) cb.setValue(months.get(0));
                    } catch (Exception ignored) {
                        if ("current".equalsIgnoreCase(p.defaultValue())) cb.setValue(months.get(0));
                    }
                } else if ("current".equalsIgnoreCase(p.defaultValue())) cb.setValue(months.get(0));
                cb.addValueChangeListener(e -> onParam(p.name(), e.getValue(), e.getValue() == null ? null : cb.getItemLabelGenerator().apply(e.getValue())));
                paramControlByDim.put(p.name(), cb);
                paramControls.add(cb);
                if (cb.getValue() != null) filters.put(p.name(), cb.getValue(), cb.getItemLabelGenerator().apply(cb.getValue()));
                return cb;
            }
            case "DATE" -> {
                DatePicker dp = new DatePicker(label);
                if (prefs.params.containsKey(p.name())) {
                    try { dp.setValue(LocalDate.parse(prefs.params.get(p.name()))); }
                    catch (Exception ignored) { if ("today".equalsIgnoreCase(p.defaultValue())) dp.setValue(LocalDate.now()); }
                } else if ("today".equalsIgnoreCase(p.defaultValue())) dp.setValue(LocalDate.now());
                dp.addValueChangeListener(e -> onParam(p.name(), e.getValue(), String.valueOf(e.getValue())));
                paramControlByDim.put(p.name(), dp);
                paramControls.add(dp);
                if (dp.getValue() != null) filters.put(p.name(), dp.getValue(), String.valueOf(dp.getValue()));
                return dp;
            }
            case "DATE_RANGE" -> { // dua parameter: <name>_from dan <name>_to
                DatePicker from = new DatePicker(label + " from"), to = new DatePicker(label + " to");
                declaredParams.remove(p.name());
                declaredParams.add(p.name() + "_from");
                declaredParams.add(p.name() + "_to");
                if (prefs.params.containsKey(p.name() + "_from")) {
                    try { LocalDate v = LocalDate.parse(prefs.params.get(p.name() + "_from")); from.setValue(v); filters.put(p.name() + "_from", v, String.valueOf(v)); } catch (Exception ignored) {}
                }
                if (prefs.params.containsKey(p.name() + "_to")) {
                    try { LocalDate v = LocalDate.parse(prefs.params.get(p.name() + "_to")); to.setValue(v); filters.put(p.name() + "_to", v, String.valueOf(v)); } catch (Exception ignored) {}
                }
                from.addValueChangeListener(e -> onParam(p.name() + "_from", e.getValue(), String.valueOf(e.getValue())));
                to.addValueChangeListener(e -> onParam(p.name() + "_to", e.getValue(), String.valueOf(e.getValue())));
                paramControlByDim.put(p.name() + "_from", from);
                paramControlByDim.put(p.name() + "_to", to);
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
                    paramControlByDim.put(p.name(), hv);
                    paramControls.add(hv);
                    if (prefs.params.containsKey(p.name())) {
                        try { ((HasValue<?, Object>) hv).setValue(prefs.params.get(p.name())); } catch (Exception ignored) {}
                    }
                }
                return c;
            }
            default -> {
                TextField tf = new TextField(label);
                tf.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
                if (prefs.params.containsKey(p.name())) {
                    try { tf.setValue(prefs.params.get(p.name())); } catch (Exception ignored) {}
                }
                tf.addValueChangeListener(e -> onParam(p.name(), e.getValue(), e.getValue()));
                paramControlByDim.put(p.name(), tf);
                paramControls.add(tf);
                if (!tf.isEmpty()) filters.put(p.name(), tf.getValue(), tf.getValue());
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
        if (clearing) { filters.put(dim, value, label); return; }
        filters.put(dim, value, label);
        applyFilters(dim);
    }

    // ---------- cards ----------
    private void buildCards() {
        for (ItemDef it : layout) {
            ReportMeta report = reportRepo.findById(it.widget().reportCode()).orElse(null);
            if (report == null || !access.canAccess(report)) continue; // tidak berhak / report hilang -> dilewati diam-diam
            DashboardWidget widget = switch (it.widget().widgetType()) {
                case "TABLE" -> new TableWidget(it.widget().options());
                case "BAR", "LINE", "PIE", "COMBO", "GAUGE" -> new ApexChartWidget(it.widget().widgetType(), it.widget().options());
                default -> new KpiWidget(it.widget().options());
            };
            CardEntry[] holder = new CardEntry[1];
            DashboardCard card = new DashboardCard(it, widget, () -> load(holder[0], false));
            Runnable drill = buildDrill(it.widget(), widget);
            // Mapping yang memakai row.* butuh baris/bar terpilih: ikon ⤢ baru tampil setelah ada seleksi
            boolean needsRow = drill != null && it.widget().drillFilterMapping() != null
                    && it.widget().drillFilterMapping().contains("row.");
            card.setDrill(needsRow ? null : drill);
            if (drill != null && "KPI".equals(it.widget().widgetType())) {
                widget.asComponent().getElement().getStyle().set("cursor", "pointer");
                widget.asComponent().getElement().addEventListener("click", ev -> { if (!widget.selectedRow().isEmpty()) drill.run(); }); // sekali saat build
            }
            holder[0] = new CardEntry(it, widget, card, report, drill, needsRow);
            if (it.widget().options().emit() != null) {
                String dim = it.widget().options().emit();
                widget.addSelectListener((v, label) -> {
                    filters.toggle(dim, v, label);
                    applyFilters(dim);
                });
            }
            // Didaftarkan SETELAH listener emit: chart menandai baris terpilih (lastIdx) di highlight() yang dipicu applyFilters
            if (needsRow) {
                widget.addSelectListener((v, label) -> card.setDrill(widget.selectedRow().isEmpty() ? null : drill));
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

    private void rebuildCards() {
        grid.removeAll();
        cards.clear();
        failures.clear();
        buildCards();
        if (customizing) {
            for (CardEntry ce : cards) enableCardCustomize(ce);
            refreshCustomizePanel();
        }
    }

    // ---------- customize mode ----------
    private void enterCustomize() {
        if (customizing) return;
        customizing = true;
        refreshCustomizePanel();
        customizePanel.setVisible(true);
        for (CardEntry ce : cards) enableCardCustomize(ce);
    }

    private void exitCustomize() {
        customizing = false;
        customizePanel.setVisible(false);
        for (CardEntry ce : cards) ce.card().setCustomizing(false, null, null, null, null);
    }

    private void enableCardCustomize(CardEntry ce) {
        String code = ce.item().widget().widgetCode();
        ce.card().setCustomizing(true,
            () -> {
                int i = layoutIndexOf(code);
                if (i > 0) { swap(layout, i, i - 1); swap(cards, i, i - 1); reorderGrid(); }
            },
            () -> {
                int i = layoutIndexOf(code);
                if (i >= 0 && i < layout.size() - 1) { swap(layout, i, i + 1); swap(cards, i, i + 1); reorderGrid(); }
            },
            () -> {
                int s = ce.card().getSpan();
                int next = s == 3 ? 6 : s == 6 ? 12 : 3;
                ce.card().setSpan(next);
                updateLayoutSpan(code, next);
            },
            () -> {
                hiddenNow.add(code);
                layout.removeIf(x -> x.widget().widgetCode().equals(code));
                cards.removeIf(x -> x.item().widget().widgetCode().equals(code));
                grid.remove(ce.card());
                refreshCustomizePanel();
            }
        );
    }

    /** Re-add all card DOM elements in current `cards` order without rebuilding widgets. */
    private void reorderGrid() {
        grid.removeAll();
        for (CardEntry c : cards) grid.add(c.card());
        refreshCustomizePanel();
    }

    private void refreshCustomizePanel() {
        customizePanel.removeAll();
        if (!hiddenNow.isEmpty()) {
            Span lbl = new Span("Hidden:");
            lbl.getStyle().set("font-size", "0.85rem").set("color", "#6b7280");
            customizePanel.add(lbl);
            for (String code : hiddenNow) {
                def.items().stream().filter(it -> it.widget().widgetCode().equals(code)).findFirst().ifPresent(it -> {
                    Button showBtn = new Button(it.widget().title() + " Show", e -> {
                        hiddenNow.remove(code);
                        def.items().stream().filter(x -> x.widget().widgetCode().equals(code)).findFirst()
                            .ifPresent(x -> {
                                int savedSpan = prefs.span.getOrDefault(code, x.colSpan());
                                layout.add(new ItemDef(x.widget(), x.rowOrder(), savedSpan));
                            });
                        rebuildCards();
                    });
                    showBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                    customizePanel.add(showBtn);
                });
            }
        }
        Button save = new Button("Save", e -> doSave());
        Button reset = new Button("Reset to default", e -> doReset());
        Button cancel = new Button("Cancel", e -> doCancel());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        reset.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        customizePanel.add(save, reset, cancel);
    }

    private void doSave() {
        LayoutPrefs next = LayoutPrefs.fromLayout(layout, hiddenNow, currentParamValuesAsStrings());
        if (saver != null) {
            try { saver.accept(next.isEmpty() ? null : next.toJson()); }
            catch (Exception ex) {
                Notification.show("Failed to save layout: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                return;
            }
        }
        prefs = next;
        exitCustomize();
    }

    private void doReset() {
        if (saver != null) {
            try { saver.accept(null); }
            catch (Exception ex) {
                Notification.show("Failed to save layout: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                return;
            }
        }
        prefs = new LayoutPrefs();
        layout = prefs.apply(def.items());
        hiddenNow.clear();
        exitCustomize();
        rebuildCards();
    }

    private void doCancel() {
        layout = prefs.apply(def.items());
        hiddenNow = new LinkedHashSet<>(prefs.hidden);
        exitCustomize();
        rebuildCards();
    }

    private Map<String, String> currentParamValuesAsStrings() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, HasValue<?, ?>> e : paramControlByDim.entrySet()) {
            Object val = e.getValue().getValue();
            if (val == null) continue;
            String s = String.valueOf(val);
            if (s.isBlank() || "null".equals(s)) continue;
            result.put(e.getKey(), s);
        }
        return result;
    }

    private int layoutIndexOf(String code) {
        for (int i = 0; i < layout.size(); i++)
            if (layout.get(i).widget().widgetCode().equals(code)) return i;
        return -1;
    }

    private void updateLayoutSpan(String code, int newSpan) {
        for (int i = 0; i < layout.size(); i++) {
            ItemDef it = layout.get(i);
            if (it.widget().widgetCode().equals(code)) {
                layout.set(i, new ItemDef(it.widget(), it.rowOrder(), newSpan));
                return;
            }
        }
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T tmp = list.get(i); list.set(i, list.get(j)); list.set(j, tmp);
    }

    /** Null bila widget tidak punya tujuan drill atau user tidak berhak ke tujuannya (ikon ⤢ disembunyikan). */
    private Runnable buildDrill(WidgetDef w, DashboardWidget widget) {
        String form = w.drillFormCode() != null && !w.drillFormCode().isBlank() ? w.drillFormCode().trim() : null;
        String rpt = w.drillReportCode() != null && !w.drillReportCode().isBlank() ? w.drillReportCode().trim() : null;
        if (form != null) {
            if (securityService == null || !securityService.hasMenuAccess(form)) return null;
            return () -> {
                DrillMapping.DrillTarget t = DrillMapping.resolve(w.drillFilterMapping(), widget.selectedRow(), filters.paramsFor(List.of()));
                com.vaadinerp.views.PortalView portal = com.vaadinerp.report.ReportLauncher.findPortal(this);
                if (portal == null) { Notification.show("Cannot find app shell to open the form."); return; }
                portal.openTabByCode(form, form + ":" + w.widgetCode(), t.tabTitle(), t.extra().isEmpty() ? null : t.extra());
            };
        }
        if (rpt != null) {
            ReportMeta report = reportRepo.findById(rpt).orElse(null);
            if (report == null || !access.canAccess(report)) return null;
            return () -> {
                DrillMapping.DrillTarget t = DrillMapping.resolve(w.drillFilterMapping(), widget.selectedRow(), filters.paramsFor(List.of()));
                com.vaadinerp.report.ReportLauncher.runAndOpenTab(this, reportRunService, report, new java.util.HashMap<>(t.extra()), "PDF", null);
            };
        }
        return null;
    }

    public void reloadAll() {
        reloadAll(false);
    }

    public void reloadAll(boolean fromPoll) {
        for (CardEntry c : cards) load(c, fromPoll);
    }

    private void load(CardEntry c, boolean fromPoll) {
        String code = c.item().widget().widgetCode();
        if (fromPoll && failures.getOrDefault(code, 0) >= 3) return;
        try {
            List<Map<String, Object>> rows = reportData.fetchData(c.report(), filters.paramsFor(declaredParams), false);
            c.widget().setData(rows == null ? List.of() : rows);
            if (c.drillNeedsRow()) c.card().setDrill(c.widget().selectedRow().isEmpty() ? null : c.drill()); // seleksi hilang setelah reload
            c.widget().highlight(filters.get(c.item().widget().options().emit()));
            c.card().showLoaded();
            failures.remove(code);
        } catch (Exception ex) {
            log.warn("Dashboard widget {} failed: {}", code, DashboardCard.rootMessage(ex));
            c.card().showError(DashboardCard.rootMessage(ex));
            failures.merge(code, 1, Integer::sum);
        }
    }

    private void applyFilters(String changedDim) {
        renderChips();
        for (CardEntry c : cards) {
            WidgetDef w = c.item().widget();
            if (FilterState.reloads(w, changedDim)) load(c, false);
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
                String dim = e.getKey();
                HasValue<?, ?> hv = paramControlByDim.get(dim);
                if (hv != null) hv.clear();
                else { filters.remove(dim); applyFilters(dim); }
            });
            chips.add(chip);
        }
    }
}
