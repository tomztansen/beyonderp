package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.LovMetaRepository;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.service.DynamicDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route("")
public class PortalView extends HorizontalLayout {

    private final FormMetaRepository formMetaRepository;
    private final LovMetaRepository lovMetaRepository;
    private final ReportMetaRepository reportMetaRepository;
    private final DynamicDataService dynamicDataService;

    // Sidebar elements
    private final VerticalLayout sidebar = new VerticalLayout();
    private final VerticalLayout dynamicMenuLayout = new VerticalLayout();

    // TabSheet on the right
    private final TabSheet tabSheet = new TabSheet();
    private final Map<String, Tab> openTabs = new HashMap<>();
    private final List<Button> menuButtons = new ArrayList<>();

    public PortalView(FormMetaRepository formMetaRepository, LovMetaRepository lovMetaRepository,
                      ReportMetaRepository reportMetaRepository, DynamicDataService dynamicDataService) {
        this.formMetaRepository = formMetaRepository;
        this.lovMetaRepository = lovMetaRepository;
        this.reportMetaRepository = reportMetaRepository;
        this.dynamicDataService = dynamicDataService;

        setSizeFull();
        setSpacing(false);
        setPadding(false);

        setupSidebar();
        setupTabSheet();

        add(sidebar, tabSheet);
    }

    private void setupSidebar() {
        sidebar.setWidth("280px");
        sidebar.setHeightFull();
        sidebar.addClassName("sidebar");
        sidebar.setPadding(true);
        sidebar.setSpacing(true);

        // Header / Logo
        Div logoDiv = new Div();
        logoDiv.addClassName("sidebar-logo");
        logoDiv.setText("VaadinERP");
        
        Span subtitle = new Span("Portal Form Dinamis");
        subtitle.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#6b7280")
                .set("margin-top", "-5px")
                .set("margin-bottom", "15px");

        // Core Tools Buttons
        Button btnBuilder = createMenuButton("Form Builder", VaadinIcon.TOOLS, e -> {
            setActiveButton((Button) e.getSource());
            openTab("FORM_BUILDER", "Form Builder", new FormBuilderView(formMetaRepository, lovMetaRepository, dynamicDataService, this::refreshFormMenu));
        });

        Button btnDbManager = createMenuButton("Database Manager", VaadinIcon.DATABASE, e -> {
            setActiveButton((Button) e.getSource());
            openTab("DB_EXPLORER", "Database Manager", new DbExplorerView(dynamicDataService));
        });

        // Report Tools Buttons
        Button btnReportBuilder = createMenuButton("Report Designer", VaadinIcon.EDIT, e -> {
            setActiveButton((Button) e.getSource());
            openTab("REPORT_BUILDER", "Report Designer", new ReportBuilderView(reportMetaRepository, formMetaRepository));
        });

        Button btnReportViewer = createMenuButton("Report Viewer", VaadinIcon.PRINT, e -> {
            setActiveButton((Button) e.getSource());
            openTab("REPORT_VIEWER", "Report Viewer", new ReportViewerView(reportMetaRepository, dynamicDataService));
        });

        // Section label for dynamic forms
        Span formsLabel = new Span("DAFTAR FORM DINAMIS");
        formsLabel.getStyle()
                .set("font-size", "0.75rem")
                .set("font-weight", "700")
                .set("color", "#4b5563")
                .set("margin-top", "20px")
                .set("margin-left", "12px")
                .set("letter-spacing", "1px");

        dynamicMenuLayout.setPadding(false);
        dynamicMenuLayout.setSpacing(false);
        dynamicMenuLayout.setWidthFull();

        sidebar.add(logoDiv, subtitle, btnBuilder, btnDbManager, btnReportBuilder, btnReportViewer, formsLabel, dynamicMenuLayout);

        // Load menu dynamically
        refreshFormMenu();
    }

    private void setupTabSheet() {
        tabSheet.setSizeFull();
        tabSheet.getStyle().set("flex-grow", "1");

        // Open a default welcome/dashboard tab
        openWelcomeTab();
    }

    private void openWelcomeTab() {
        VerticalLayout welcomeContent = new VerticalLayout();
        welcomeContent.setSizeFull();
        welcomeContent.setPadding(true);
        welcomeContent.setSpacing(true);
        welcomeContent.setJustifyContentMode(JustifyContentMode.CENTER);
        welcomeContent.setAlignItems(Alignment.CENTER);
        welcomeContent.getStyle().set("background", "#ffffff");

        Icon portalIcon = VaadinIcon.GRID_BIG_O.create();
        portalIcon.setSize("70px");
        portalIcon.setColor("#6366f1");

        H1 mainTitle = new H1("Selamat Datang di VaadinERP");
        mainTitle.getStyle().set("margin", "10px 0").set("color", "#111827");
        
        Paragraph desc = new Paragraph("Gunakan menu di sebelah kiri untuk mengelola data form dinamis, membuat form baru menggunakan Form Builder, atau mengeksplorasi tabel fisik yang terbuat di schema database 'dynamic'.");
        desc.getStyle().set("color", "#4b5563").set("text-align", "center").set("max-width", "600px");

        VerticalLayout cards = new VerticalLayout();
        cards.setPadding(false);
        cards.setSpacing(true);
        cards.setMaxWidth("600px");

        cards.add(
            createFeatureCard("Form Builder", "Buat form baru secara interaktif, definisikan field, tipe data, LOV, serta cascading filters secara visual.", VaadinIcon.TOOLS),
            createFeatureCard("Multi-tab Workspace", "Buka beberapa form dinamis sekaligus dalam bentuk tab dan kelola data transaksi secara terisolasi.", VaadinIcon.GRID_BIG),
            createFeatureCard("Database Schema Isolation", "Semua tabel dinamis di-generate dan dikelola dalam schema PostgreSQL terpisah bernama 'dynamic'.", VaadinIcon.DATABASE)
        );

        welcomeContent.add(portalIcon, mainTitle, desc, cards);

        tabSheet.add("Dashboard", welcomeContent);
    }

    private HorizontalLayout createFeatureCard(String title, String desc, VaadinIcon icon) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "#f9fafb")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "8px");

        Icon ic = icon.create();
        ic.setColor("#6366f1");
        ic.setSize("30px");

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        Span tSpan = new Span(title);
        tSpan.getStyle().set("font-weight", "600").set("color", "#111827");

        Span dSpan = new Span(desc);
        dSpan.getStyle().set("font-size", "0.85rem").set("color", "#6b7280");

        textLayout.add(tSpan, dSpan);
        card.add(ic, textLayout);
        card.setVerticalComponentAlignment(Alignment.CENTER, ic);
        return card;
    }

    private void openTab(String tabId, String tabTitle, Component content) {
        if (openTabs.containsKey(tabId)) {
            tabSheet.setSelectedTab(openTabs.get(tabId));
            return;
        }

        // Layout tab header with title and close button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setSpacing(true);
        headerLayout.setAlignItems(Alignment.CENTER);

        Span titleSpan = new Span(tabTitle);
        titleSpan.getStyle().set("font-size", "0.9rem");

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        closeBtn.getStyle().set("cursor", "pointer");

        headerLayout.add(titleSpan, closeBtn);

        Tab tab = tabSheet.add(headerLayout, content);
        openTabs.put(tabId, tab);

        closeBtn.addClickListener(e -> {
            tabSheet.remove(tab);
            openTabs.remove(tabId);
            // If all tabs closed, select the dashboard (first tab)
            if (openTabs.isEmpty()) {
                tabSheet.setSelectedIndex(0);
                clearActiveButtons();
            }
        });

        tabSheet.setSelectedTab(tab);
    }

    private Button createMenuButton(String label, VaadinIcon icon, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button button = new Button(label, icon.create());
        button.setWidthFull();
        button.addClassName("menu-item-button");
        button.addClickListener(listener);
        menuButtons.add(button);
        return button;
    }

    private void setActiveButton(Button clickedButton) {
        clearActiveButtons();
        clickedButton.addClassName("active");
    }

    private void clearActiveButtons() {
        for (Button btn : menuButtons) {
            btn.removeClassName("active");
        }
    }

    private void refreshFormMenu() {
        dynamicMenuLayout.removeAll();
        // Remove menu buttons that were in the dynamic menu from the tracking list
        menuButtons.removeIf(btn -> btn.getParent().isPresent() && btn.getParent().get() == dynamicMenuLayout);

        List<FormMeta> forms = formMetaRepository.findAll();
        for (FormMeta form : forms) {
            String code = form.getFormCode();
            String title = form.getFormTitle();

            Button btnForm = createMenuButton(title, VaadinIcon.FILE_TEXT_O, e -> {
                setActiveButton((Button) e.getSource());
                
                Component formView;
                if ("MASTER_DETAIL".equalsIgnoreCase(form.getFormType())) {
                    GenericMasterDetailFormView mdView = new GenericMasterDetailFormView(formMetaRepository, dynamicDataService);
                    mdView.setParameter(null, code);
                    mdView.getStyle().set("padding", "20px");
                    mdView.setCloseHandler(() -> {
                        Tab tab = openTabs.get(code);
                        if (tab != null) {
                            tabSheet.remove(tab);
                            openTabs.remove(code);
                            if (openTabs.isEmpty()) {
                                tabSheet.setSelectedIndex(0);
                                clearActiveButtons();
                            }
                        }
                    });
                    formView = mdView;
                } else {
                    GenericFormView gView = new GenericFormView(formMetaRepository, dynamicDataService);
                    gView.setParameter(null, code);
                    gView.getStyle().set("padding", "20px");
                    gView.setCloseHandler(() -> {
                        Tab tab = openTabs.get(code);
                        if (tab != null) {
                            tabSheet.remove(tab);
                            openTabs.remove(code);
                            if (openTabs.isEmpty()) {
                                tabSheet.setSelectedIndex(0);
                                clearActiveButtons();
                            }
                        }
                    });
                    formView = gView;
                }

                openTab(code, title, formView);
            });
            dynamicMenuLayout.add(btnForm);
        }
    }
}
