package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.IFrame;
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
import com.vaadinerp.report.render.ReportOutput;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.security.service.SessionSecurityService;

import java.nio.charset.StandardCharsets;
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
        VerticalLayout left = new VerticalLayout(new H4("Reports"), searchField, catalog);
        left.setSizeFull();
        left.getStyle().set("overflow", "auto");

        selectionPanel.setPadding(true);
        selectionPanel.setSizeFull();
        selectionPanel.getStyle().set("overflow", "auto");
        selectionPanel.add(new Span("Select a report from the catalog."));

        SplitLayout split = new SplitLayout(left, selectionPanel);
        split.setSizeFull();
        split.setSplitterPosition(24);
        add(split);
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

        try {
            ReportRunResult res = reportRunService.run(report, values, false);
            Component content = buildOutput(res);
            PortalView portal = findPortal();
            if (portal == null) {
                Notification.show("Cannot find app shell to open output tab.");
                return;
            }
            String title = report.getReportTitle() != null ? report.getReportTitle() : report.getReportCode();
            portal.openComponentTab("RPT_OUT_" + report.getReportCode(), title, content);
        } catch (org.springframework.dao.QueryTimeoutException te) {
            Notification.show("The report query took too long and was stopped. Please narrow your filter/parameters.");
        } catch (Exception ex) {
            Notification.show("Failed to run report: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()));
        }
    }

    private Component buildOutput(ReportRunResult res) {
        VerticalLayout box = new VerticalLayout();
        box.setSizeFull();
        box.setPadding(false);
        box.setSpacing(false);
        if (res.stimulsoftViewer()) {
            IFrame ifr = new IFrame(res.viewerUrl());
            ifr.setSizeFull();
            ifr.getStyle().set("border", "none");
            box.add(ifr);
            box.setFlexGrow(1, ifr);
            return box;
        }
        ReportOutput out = res.output();
        if (out.contentType().startsWith("text/html")) {
            Div d = new Div();
            d.getElement().setProperty("innerHTML", new String(out.bytes(), StandardCharsets.UTF_8));
            d.setSizeFull();
            d.getStyle().set("overflow", "auto");
            box.add(d);
            box.setFlexGrow(1, d);
        } else {
            String b64 = Base64.getEncoder().encodeToString(out.bytes());
            IFrame ifr = new IFrame("data:" + out.contentType() + ";base64," + b64);
            ifr.setSizeFull();
            ifr.getStyle().set("border", "none");
            box.add(ifr);
            box.setFlexGrow(1, ifr);
        }
        return box;
    }

    /** Cari PortalView (app shell) untuk membuka tab: naik parent, fallback ke children UI. */
    private PortalView findPortal() {
        Component c = this;
        while (c != null) {
            if (c instanceof PortalView pv) return pv;
            c = c.getParent().orElse(null);
        }
        UI ui = UI.getCurrent();
        if (ui != null) {
            for (Component child : ui.getChildren().toList()) {
                if (child instanceof PortalView pv) return pv;
            }
        }
        return null;
    }
}
