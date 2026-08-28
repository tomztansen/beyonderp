package com.vaadinerp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
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
import com.vaadinerp.report.render.ReportOutput;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.security.service.SessionSecurityService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/** Layar end-user menjalankan report: katalog (kiri) + selection + output (kanan). */
@Route("report-runner")
public class ReportRunnerView extends HorizontalLayout {

    private final ReportMetaRepository reportMetaRepository;
    private final ReportAccessService reportAccessService;
    private final ReportRunService reportRunService;
    private final DynamicDataService dynamicDataService;
    private final SessionSecurityService securityService;

    private final VerticalLayout catalog = new VerticalLayout();
    private final TextField searchField = new TextField();
    private final VerticalLayout selectionPanel = new VerticalLayout();
    private final VerticalLayout outputPanel = new VerticalLayout();

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

        // Left: search + catalog
        searchField.setPlaceholder("Search report...");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> rebuildCatalog(e.getValue()));
        catalog.setPadding(false);
        catalog.setSpacing(false);
        VerticalLayout left = new VerticalLayout(new H4("Reports"), searchField, catalog);
        left.setWidth("320px");
        left.setHeightFull();
        left.getStyle().set("overflow", "auto").set("border-right", "1px solid #e2e8f0");

        // Right: selection + output
        selectionPanel.setPadding(true);
        outputPanel.setPadding(true);
        outputPanel.setSizeFull();
        SplitLayout right = new SplitLayout(selectionPanel, outputPanel);
        right.setOrientation(SplitLayout.Orientation.VERTICAL);
        right.setSplitterPosition(30);
        right.setSizeFull();

        add(left, right);
        setFlexGrow(1, right);
        rebuildCatalog("");
    }

    private void rebuildCatalog(String search) {
        catalog.removeAll();
        List<ReportMeta> reports = reportAccessService.accessibleReports(reportMetaRepository.findAll());
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
                SafeButton b = new SafeButton(r.getReportTitle() != null ? r.getReportTitle() : r.getReportCode(),
                        ev -> selectReport(r));
                b.setWidthFull();
                b.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
                b.getStyle().set("justify-content", "flex-start");
                items.add(b);
            }
            acc.add(e.getKey() + " (" + e.getValue().size() + ")", items);
        }
        acc.setWidthFull();
        catalog.add(acc);
    }

    private void selectReport(ReportMeta report) {
        selectionPanel.removeAll();
        outputPanel.removeAll();
        selectionPanel.add(new H4(report.getReportTitle() != null ? report.getReportTitle() : report.getReportCode()));
        if (report.getDescription() != null && !report.getDescription().isBlank()) {
            Span d = new Span(report.getDescription());
            d.getStyle().set("color", "#64748b");
            selectionPanel.add(d);
        }
        paramForm = new ReportParameterForm(report.getParams(), dynamicDataService);
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

        outputPanel.removeAll();
        if ("STIMULSOFT".equalsIgnoreCase(report.getEngineType() != null ? report.getEngineType() : "STANDARD")) {
            ReportRunResult res = reportRunService.run(report, values, false);
            IFrame ifr = new IFrame(res.viewerUrl());
            ifr.setSizeFull();
            ifr.getStyle().set("border", "none");
            outputPanel.add(ifr);
            outputPanel.setFlexGrow(1, ifr);
            return;
        }

        ProgressBar pb = new ProgressBar();
        pb.setIndeterminate(true);
        outputPanel.add(pb);
        UI ui = UI.getCurrent();
        new Thread(() -> {
            try {
                ReportRunResult res = reportRunService.run(report, values, false);
                ReportOutput out = res.output();
                ui.access(() -> {
                    outputPanel.removeAll();
                    HorizontalLayout bar = new HorizontalLayout(
                            new SafeButton("Print", e -> ui.getPage().executeJs("window.print()")));
                    outputPanel.add(bar);
                    if (out.contentType().startsWith("text/html")) {
                        outputPanel.add(new com.vaadin.flow.component.Html("<div style=\"overflow:auto;width:100%\">"
                                + new String(out.bytes(), StandardCharsets.UTF_8) + "</div>"));
                    } else {
                        String b64 = Base64.getEncoder().encodeToString(out.bytes());
                        IFrame ifr = new IFrame("data:" + out.contentType() + ";base64," + b64);
                        ifr.setSizeFull();
                        ifr.setHeight("70vh");
                        outputPanel.add(ifr);
                    }
                });
            } catch (org.springframework.dao.QueryTimeoutException te) {
                ui.access(() -> {
                    outputPanel.removeAll();
                    outputPanel.add(new Span("The report query took too long and was stopped. "
                            + "Please narrow your filter/parameters."));
                });
            } catch (Exception ex) {
                ui.access(() -> {
                    outputPanel.removeAll();
                    outputPanel.add(new Span("Failed to run report: "
                            + (ex.getMessage() != null ? ex.getMessage() : ex.toString())));
                });
            }
        }).start();
    }
}
