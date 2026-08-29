package com.vaadinerp.views;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadinerp.components.ReportParameterForm;
import com.vaadinerp.components.SafeButton;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.meta.ReportParamMeta;
import com.vaadinerp.report.*;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.security.service.SessionSecurityService;

import java.util.*;
import java.util.stream.Collectors;

/** Layar end-user menjalankan report: katalog (kiri) + selection (kanan). Output dibuka sebagai tab di aplikasi. */
@Route("report-runner")
public class ReportRunnerView extends VerticalLayout {

    private final ReportMetaRepository reportMetaRepository;
    private final ReportAccessService reportAccessService;
    private final ReportRunService reportRunService;
    private final DynamicDataService dynamicDataService;
    private final SessionSecurityService securityService;

    private final VerticalLayout catalog = new VerticalLayout();
    private final TextField searchField = new TextField();
    private final VerticalLayout selectionPanel = new VerticalLayout();

    private ReportParameterForm paramForm;

    public ReportRunnerView(ReportMetaRepository reportMetaRepository,
                            ReportAccessService reportAccessService,
                            ReportRunService reportRunService,
                            DynamicDataService dynamicDataService,
                            SessionSecurityService securityService) {
        this.reportMetaRepository = reportMetaRepository;
        this.reportAccessService = reportAccessService;
        this.reportRunService = reportRunService;
        this.dynamicDataService = dynamicDataService;
        this.securityService = securityService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        searchField.setPlaceholder("Search report...");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> rebuildCatalog(e.getValue()));
        catalog.setPadding(false);
        catalog.setSpacing(false);

        H4 header = new H4("Reports");
        header.getStyle()
                .set("margin", "0").set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)").set("width", "100%");

        VerticalLayout left = new VerticalLayout(header, searchField, catalog);
        left.setSizeFull();
        left.getStyle()
                .set("overflow", "auto")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-right", "1px solid var(--lumo-contrast-10pct)");

        selectionPanel.setPadding(true);
        selectionPanel.setSizeFull();
        selectionPanel.getStyle().set("overflow", "auto");
        selectionPanel.add(buildEmptyState());

        SplitLayout split = new SplitLayout(left, selectionPanel);
        split.setSizeFull();
        split.setSplitterPosition(24);
        add(split);
        // CSS Shadow Parts injection — targets button internals inaccessible from Java getStyle()
        UI.getCurrent().getPage().executeJs(
            "if(!document.getElementById('rr-styles')){" +
            "var s=document.createElement('style');s.id='rr-styles';" +
            "s.textContent='vaadin-button.rr-item{justify-content:flex-start!important}" +
            "vaadin-button.rr-item::part(label){flex-grow:1;text-align:left}';" +
            "document.head.appendChild(s)}");
        rebuildCatalog("");
    }

    private void rebuildCatalog(String search) {
        catalog.removeAll();
        List<ReportMeta> reports = reportAccessService.accessibleReports(reportMetaRepository.findAll());
        // Report khusus form (param FORM_FIELD tak bisa terisi tanpa baris grid) disembunyikan
        // di sini — menjalankannya tanpa filter akan menarik seluruh tabel.
        reports = reports.stream().filter(r -> r.isUsableFrom("RUNNER")).collect(Collectors.toList());
        String q = search == null ? "" : search.trim().toLowerCase();
        if (!q.isEmpty()) {
            reports = reports.stream().filter(r ->
                    (r.getReportTitle() != null && r.getReportTitle().toLowerCase().contains(q))
                    || (r.getDescription() != null && r.getDescription().toLowerCase().contains(q))
                    || (r.getReportCode() != null && r.getReportCode().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }
        Map<String, List<ReportMeta>> byCat = new TreeMap<>();
        for (ReportMeta r : reports) {
            String cat = (r.getCategory() != null && !r.getCategory().isBlank()) ? r.getCategory() : "Uncategorized";
            byCat.computeIfAbsent(cat, k -> new ArrayList<>()).add(r);
        }
        if (byCat.isEmpty()) {
            catalog.add(new Span("No reports available."));
            return;
        }
        Accordion acc = new Accordion();
        for (Map.Entry<String, List<ReportMeta>> e : byCat.entrySet()) {
            VerticalLayout items = new VerticalLayout();
            items.setPadding(false);
            items.setSpacing(false);
            for (ReportMeta r : e.getValue()) {
                String label = r.getReportTitle() != null ? r.getReportTitle() : r.getReportCode();
                SafeButton b = new SafeButton(label, VaadinIcon.FILE_TEXT.create(), ev -> selectReport(r));
                b.setWidthFull();
                b.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
                b.addClassName("rr-item");
                items.add(b);
            }
            acc.add(e.getKey() + " (" + e.getValue().size() + ")", items);
        }
        acc.setWidthFull();
        catalog.add(acc);
    }

    private void selectReport(ReportMeta report) {
        selectionPanel.removeAll();
        selectionPanel.add(new H4(report.getReportTitle() != null ? report.getReportTitle() : report.getReportCode()));
        if (report.getDescription() != null && !report.getDescription().isBlank()) {
            Span d = new Span(report.getDescription());
            d.getStyle().set("color", "#64748b");
            selectionPanel.add(d);
        }
        paramForm = new ReportParameterForm(report.getParams(), dynamicDataService);
        paramForm.setMaxWidth("520px");
        selectionPanel.add(paramForm);

        SafeButton run = new SafeButton("Run", e -> runReport(report));
        run.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        SafeButton reset = new SafeButton("Reset", e -> selectReport(report));
        selectionPanel.add(new HorizontalLayout(run, reset));
    }

    private void runReport(ReportMeta report) {
        String user = (securityService.getCurrentUser() != null) ? securityService.getCurrentUser().getUsername() : null;
        Map<String, Object> values = new HashMap<>(
                ReportParamResolver.resolveAuto(report.getParams(), Map.of(), user));
        if (paramForm != null) values.putAll(paramForm.collectValues());
        if (report.getParams() != null) {
            for (ReportParamMeta p : report.getParams()) {
                values.putIfAbsent(p.getParamName(), p.getDefaultValue());
                if (p.isRequired()) {
                    Object v = values.get(p.getParamName());
                    if (v == null || (v instanceof String s && s.isBlank())) {
                        Notification.show("Parameter '" + (p.getParamLabel() != null ? p.getParamLabel() : p.getParamName())
                                + "' is required.");
                        return;
                    }
                }
            }
        }

        // Show loading state while report runs off the UI thread
        selectionPanel.removeAll();
        com.vaadin.flow.component.progressbar.ProgressBar pb = new com.vaadin.flow.component.progressbar.ProgressBar();
        pb.setIndeterminate(true);
        selectionPanel.add(new H4("Running report…"), pb);

        ReportLauncher.runAndOpenTab(this, reportRunService, report, values, () -> selectReport(report));
    }

    private Div buildEmptyState() {
        Icon icon = VaadinIcon.BAR_CHART.create();
        icon.getStyle().set("width", "4rem").set("height", "4rem").set("opacity", "0.2")
                .set("margin-bottom", "var(--lumo-space-m)");
        Span title = new Span("Pilih laporan dari katalog");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("font-weight", "500");
        Span hint = new Span("Output akan dibuka sebagai tab baru");
        hint.getStyle().set("font-size", "var(--lumo-font-size-s)");
        Div empty = new Div(icon, title, hint);
        empty.getStyle()
                .set("display", "flex").set("flex-direction", "column")
                .set("align-items", "center").set("justify-content", "center")
                .set("gap", "var(--lumo-space-s)").set("height", "100%")
                .set("color", "var(--lumo-secondary-text-color)");
        return empty;
    }

}
