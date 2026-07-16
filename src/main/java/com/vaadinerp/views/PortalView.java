package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.LovMetaRepository;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.security.entity.AppMenu;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.entity.RoleMenuPermission;
import com.vaadinerp.security.repository.AppMenuRepository;
import com.vaadinerp.security.repository.AppRoleRepository;
import com.vaadinerp.security.repository.AppUserRepository;
import com.vaadinerp.security.repository.RoleMenuPermissionRepository;
import com.vaadinerp.security.repository.AppUserFavoriteMenuRepository;
import com.vaadinerp.security.entity.AppUserFavoriteMenu;
import com.vaadinerp.security.service.SessionSecurityService;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.service.StandardFormatService;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Route("")
public class PortalView extends AppLayout {

    private final FormMetaRepository formMetaRepository;
    private final LovMetaRepository lovMetaRepository;
    private final ReportMetaRepository reportMetaRepository;
    private final DynamicDataService dynamicDataService;
    private final SessionSecurityService securityService;
    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final AppMenuRepository appMenuRepository;
    private final RoleMenuPermissionRepository roleMenuPermissionRepository;
    private final AppUserFavoriteMenuRepository appUserFavoriteMenuRepository;
    private final StandardFormatService standardFormatService;

    private String menuSearchText = "";
    private boolean showFavoritesOnly = false;

    // Sidebar elements
    private final VerticalLayout sidebar = new VerticalLayout();
    private final VerticalLayout menuContainer = new VerticalLayout();

    // TabSheet on the right
    private final TabSheet tabSheet = new TabSheet();
    private final Map<String, Tab> openTabs = new HashMap<>();
    private final List<Div> leafRows = new ArrayList<>();

    public PortalView(FormMetaRepository formMetaRepository, LovMetaRepository lovMetaRepository,
            ReportMetaRepository reportMetaRepository, DynamicDataService dynamicDataService,
            SessionSecurityService securityService,
            AppUserRepository appUserRepository, AppRoleRepository appRoleRepository,
            AppMenuRepository appMenuRepository, RoleMenuPermissionRepository roleMenuPermissionRepository,
            AppUserFavoriteMenuRepository appUserFavoriteMenuRepository,
            StandardFormatService standardFormatService) {
        this.formMetaRepository = formMetaRepository;
        this.lovMetaRepository = lovMetaRepository;
        this.reportMetaRepository = reportMetaRepository;
        this.dynamicDataService = dynamicDataService;
        this.securityService = securityService;
        this.appUserRepository = appUserRepository;
        this.appRoleRepository = appRoleRepository;
        this.appMenuRepository = appMenuRepository;
        this.roleMenuPermissionRepository = roleMenuPermissionRepository;
        this.appUserFavoriteMenuRepository = appUserFavoriteMenuRepository;
        this.standardFormatService = standardFormatService;

        setPrimarySection(Section.DRAWER);

        setupNavbar();
        setupSidebar();
        setupTabSheet();

        setContent(tabSheet);

        try {
            getElement().executeJs(com.vaadinerp.components.StandardGridUtils.getGlobalCellCopyJs());
        } catch (Exception ignored) {
        }
    }

    private void setupNavbar() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getStyle().set("color", "#4f46e5").set("cursor", "pointer");

        Span titleSpan = new Span("PT. GROWTH ASIA");
        titleSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "1.15rem")
                .set("color", "#1e293b")
                .set("margin-left", "8px");

        HorizontalLayout leftSection = new HorizontalLayout(toggle, titleSpan);
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        // Right Section: IP Badge, User Profile, Change Password, Logout
        HorizontalLayout rightSection = new HorizontalLayout();
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setSpacing(true);
        rightSection.getStyle().set("margin-left", "auto").set("gap", "14px");

        // 1. IP Address Badge
        String ip = "127.0.0.1";
        try {
            if (com.vaadin.flow.server.VaadinSession.getCurrent() != null
                    && com.vaadin.flow.server.VaadinSession.getCurrent().getBrowser() != null
                    && com.vaadin.flow.server.VaadinSession.getCurrent().getBrowser().getAddress() != null) {
                ip = com.vaadin.flow.server.VaadinSession.getCurrent().getBrowser().getAddress();
            } else if (com.vaadin.flow.server.VaadinRequest.getCurrent() != null) {
                String xff = com.vaadin.flow.server.VaadinRequest.getCurrent().getHeader("X-Forwarded-For");
                if (xff != null && !xff.isEmpty()) {
                    ip = xff.split(",")[0].trim();
                } else {
                    ip = com.vaadin.flow.server.VaadinRequest.getCurrent().getRemoteAddr();
                }
            }
            if ("0:0:0:0:0:0:0:1".equals(ip))
                ip = "127.0.0.1";
        } catch (Exception ignored) {
        }

        Div ipBadge = new Div();
        Icon globeIcon = VaadinIcon.GLOBE.create();
        globeIcon.getStyle().set("width", "14px").set("height", "14px").set("margin-right", "6px").set("color",
                "#6366f1");
        Span ipText = new Span("IP: " + ip);
        ipBadge.add(globeIcon, ipText);
        ipBadge.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("background", "#f1f5f9")
                .set("border", "1px solid #cbd5e1")
                .set("border-radius", "20px")
                .set("padding", "4px 12px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("color", "#334155");

        // 2. User Info
        AppUser curr = securityService.getCurrentUser();
        String uName = curr != null ? curr.getFullName() : "Admin Demo";
        String uRole = curr != null ? curr.getRoleCode() : "SUPER_ADMIN";

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%")
                .set("background", "linear-gradient(135deg, #6366f1, #8b5cf6)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "white")
                .set("font-weight", "700")
                .set("font-size", "0.9rem")
                .set("box-shadow", "0 2px 4px rgba(99, 102, 241, 0.25)");
        avatar.setText(uName != null && !uName.isEmpty() ? uName.substring(0, 1).toUpperCase() : "?");

        VerticalLayout textBlock = new VerticalLayout();
        textBlock.setPadding(false);
        textBlock.setSpacing(false);
        Span nameSpan = new Span(uName);
        nameSpan.getStyle().set("font-weight", "600").set("font-size", "0.85rem").set("color", "#0f172a")
                .set("line-height", "1.2");
        Span roleSpan = new Span(uRole);
        roleSpan.getStyle().set("font-size", "0.7rem").set("color", "#64748b").set("line-height", "1.2");
        textBlock.add(nameSpan, roleSpan);

        HorizontalLayout userProfile = new HorizontalLayout(avatar, textBlock);
        userProfile.setAlignItems(FlexComponent.Alignment.CENTER);
        userProfile.setSpacing(true);
        userProfile.getStyle().set("padding", "2px 10px").set("border-right", "1px solid #cbd5e1").set("margin-right",
                "2px");

        // 3. Action Buttons (Change Password & Logout)
        Button changePassBtn = new Button("Ganti Password", VaadinIcon.KEY.create(), e -> openChangePasswordDialog());
        changePassBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        changePassBtn.getStyle().set("color", "#4f46e5").set("font-weight", "600").set("cursor", "pointer");

        Button logoutBtn = new Button("Logout", VaadinIcon.SIGN_OUT.create(), e -> {
            securityService.logout();
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        logoutBtn.getStyle().set("color", "#ef4444").set("font-weight", "600").set("cursor", "pointer");

        rightSection.add(ipBadge, userProfile, changePassBtn, logoutBtn);

        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
                .set("padding", "0 24px 0 16px")
                .set("height", "60px")
                .set("background", "rgba(255, 255, 255, 0.95)")
                .set("backdrop-filter", "blur(8px)")
                .set("border-bottom", "1px solid #cbd5e1")
                .set("box-shadow", "0 1px 3px 0 rgba(0, 0, 0, 0.05)");

        addToNavbar(header);
    }

    private void setupSidebar() {
        sidebar.setWidth("280px");
        sidebar.setHeightFull();
        sidebar.addClassName("sidebar");
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.getStyle()
                .set("overflow-y", "auto")
                .set("overflow-x", "hidden")
                .set("box-sizing", "border-box")
                .set("background", "linear-gradient(180deg, #1a233a 0%, #111827 50%, #0b0f17 100%)")
                .set("border-right", "1px solid rgba(255, 255, 255, 0.08)");

        // === HEADER / LOGO ===
        Div logoDiv = new Div();
        logoDiv.setWidthFull();
        logoDiv.getStyle()
                .set("padding", "20px 16px 14px 16px")
                .set("border-bottom", "1px solid rgba(255, 255, 255, 0.08)")
                .set("box-sizing", "border-box");

        Span logoText = new Span("GRP");
        logoText.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "800")
                .set("color", "#f8fafc")
                .set("letter-spacing", "0.5px")
                .set("display", "block");

        Span subtitle = new Span("Growth Resource Planning");
        subtitle.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "#94a3b8")
                .set("letter-spacing", "1px")
                .set("text-transform", "uppercase")
                .set("display", "block")
                .set("margin-top", "2px");

        logoDiv.add(logoText, subtitle);
        sidebar.add(logoDiv);

        // === SEARCH & FAVORITE TOOLBAR ===
        Div searchToolbar = new Div();
        searchToolbar.setWidthFull();
        searchToolbar.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("padding", "12px 12px")
                .set("gap", "8px")
                .set("background", "rgba(0, 0, 0, 0.15)")
                .set("border-bottom", "1px solid rgba(255, 255, 255, 0.08)")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Cari menu.....");
        searchField.setWidth("75");
        Icon searchIcon = VaadinIcon.SEARCH.create();
        searchIcon.setSize("13px");
        searchIcon.getStyle().set("color", "#475569");
        searchField.setPrefixComponent(searchIcon);
        searchField.getStyle()
                .set("--vaadin-input-field-background", "#f8fafc")
                .set("--vaadin-input-field-border-color", "#cbd5e1")
                .set("border-radius", "6px")
                .set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)")
                .set("min-width", "0");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);

        Button favButton = new Button(VaadinIcon.STAR.create());
        favButton.setTooltipText("Tampilkan Menu Favorit");
        favButton.getStyle()
                .set("background", "#f8fafc")
                .set("color", "#64748b")
                .set("border", "1px solid #cbd5e1")
                .set("border-radius", "6px")
                .set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)")
                .set("cursor", "pointer")
                .set("height", "36px")
                .set("width", "36px")
                .set("min-width", "36px")
                .set("padding", "0")
                .set("flex", "0 0 36px")
                .set("transition", "all 0.2s ease");

        favButton.addClickListener(e -> {
            showFavoritesOnly = !showFavoritesOnly;
            if (showFavoritesOnly) {
                favButton.getStyle()
                        .set("color", "#d97706")
                        .set("border", "1px solid #f59e0b")
                        .set("background", "#fef3c7")
                        .set("box-shadow", "0 0 8px rgba(245, 158, 11, 0.4)");
            } else {
                favButton.getStyle()
                        .set("color", "#64748b")
                        .set("border", "1px solid #cbd5e1")
                        .set("background", "#f8fafc")
                        .set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)");
            }
            refreshFormMenu();
        });

        searchField.addValueChangeListener(e -> {
            menuSearchText = e.getValue() != null ? e.getValue().trim().toLowerCase() : "";
            refreshFormMenu();
        });

        searchToolbar.add(searchField, favButton);
        sidebar.add(searchToolbar);

        // === CUSTOM TREE MENU (Pure Flexbox Divs - Presisi Matematik Rata Kiri
        // Sempurna) ===
        menuContainer.setPadding(false);
        menuContainer.setSpacing(false);
        menuContainer.setWidthFull();
        menuContainer.getStyle().set("padding", "10px 0");

        cleanAndSyncMenuTree();
        buildCustomMenuTree(null, menuContainer, 0);

        sidebar.add(menuContainer);
        sidebar.setFlexGrow(1, menuContainer);

        addToDrawer(sidebar);
    }

    /**
     * Membersihkan dan menormalisasi struktur menu di database.
     * Mengunci hanya 4 Group Utama yang sah berada di Root (parent IS NULL).
     */
    private void cleanAndSyncMenuTree() {
        if (!appMenuRepository.existsById("GRP_FORMS")) {
            AppMenu grp = new AppMenu();
            grp.setMenuCode("GRP_FORMS");
            grp.setMenuTitle("Formulir & Transaksi");
            grp.setIconName("BRIEFCASE");
            grp.setDisplayOrder(5);
            grp.setMenuType("GROUP");
            appMenuRepository.save(grp);
        }

        // Pastikan menu Report & Cetak selalu ada
        ensureMenuExists("GRP_REPORTS", "Report & Cetak", null, "FILE_TEXT", 20, "GROUP");
        ensureAdminOnlyMenuExists("REPORT_BUILDER", "Report Designer", "GRP_REPORTS", "EDIT", 10, "ITEM");
        ensureMenuExists("REPORT_VIEWER", "Report Viewer", "GRP_REPORTS", "PRINT", 20, "ITEM");

        // Pastikan LOV Builder selalu ada (di bawah SYS_FORM jika ada, atau root)
        String sysParent = appMenuRepository.existsById("SYS_FORM") ? "SYS_FORM"
                : (appMenuRepository.existsById("GRP_DEV_TOOLS") ? "GRP_DEV_TOOLS" : null);
        ensureAdminOnlyMenuExists("LOV_BUILDER", "LOV Metadata Builder", sysParent, "LIST", 30, "ITEM");
        ensureAdminOnlyMenuExists("STANDARD_FORMAT", "Konfigurasi Format Standar", sysParent, "SLIDERS", 40, "ITEM");

        AppMenu lov = appMenuRepository.findById("LOV_BUILDER").orElse(null);
        String targetParent = (lov != null && lov.getParentMenuCode() != null) ? lov.getParentMenuCode() : sysParent;

        AppMenu actionMenu = appMenuRepository.findById("FORM_ACTION_BUILDER").orElseGet(AppMenu::new);
        actionMenu.setMenuCode("FORM_ACTION_BUILDER");
        actionMenu.setMenuTitle("Extra Toolbar Builder");
        actionMenu.setParentMenuCode(targetParent);
        actionMenu.setIconName("BOLT");
        actionMenu.setDisplayOrder(35);
        actionMenu.setMenuType("ITEM");
        actionMenu.setRoutePath("action-builder");
        appMenuRepository.save(actionMenu);

        if (roleMenuPermissionRepository != null) {
            for (String rCode : java.util.List.of("SUPER_ADMIN", "ADMIN")) {
                if (roleMenuPermissionRepository.findByRoleCodeAndMenuCode(rCode, "FORM_ACTION_BUILDER").isEmpty()) {
                    RoleMenuPermission p = new RoleMenuPermission();
                    p.setRoleCode(rCode);
                    p.setMenuCode("FORM_ACTION_BUILDER");
                    p.setCanAdd(true);
                    p.setCanEdit(true);
                    p.setCanDelete(true);
                    p.setCanPrint(true);
                    roleMenuPermissionRepository.save(p);
                }
            }
            if (appRoleRepository != null) {
                for (com.vaadinerp.security.entity.AppRole r : appRoleRepository.findAll()) {
                    if (r.getRoleCode() != null && r.getRoleCode().toUpperCase().contains("ADMIN")
                            && roleMenuPermissionRepository
                                    .findByRoleCodeAndMenuCode(r.getRoleCode(), "FORM_ACTION_BUILDER").isEmpty()) {
                        RoleMenuPermission p = new RoleMenuPermission();
                        p.setRoleCode(r.getRoleCode());
                        p.setMenuCode("FORM_ACTION_BUILDER");
                        p.setCanAdd(true);
                        p.setCanEdit(true);
                        p.setCanDelete(true);
                        p.setCanPrint(true);
                        roleMenuPermissionRepository.save(p);
                    }
                }
            }
        }

        AppMenu seqMenu = appMenuRepository.findById("MD_SEQUENCE").orElseGet(AppMenu::new);
        seqMenu.setMenuCode("MD_SEQUENCE");
        seqMenu.setMenuTitle("Master Penomoran Dokumen");
        seqMenu.setParentMenuCode(targetParent);
        seqMenu.setIconName("BARCODE");
        seqMenu.setDisplayOrder(45);
        seqMenu.setMenuType("ITEM");
        seqMenu.setRoutePath("MD_SEQUENCE");
        appMenuRepository.save(seqMenu);

        if (roleMenuPermissionRepository != null) {
            for (String rCode : java.util.List.of("ADMIN", "SUPER_ADMIN")) {
                if (roleMenuPermissionRepository.findByRoleCodeAndMenuCode(rCode, "MD_SEQUENCE").isEmpty()) {
                    RoleMenuPermission p = new RoleMenuPermission();
                    p.setRoleCode(rCode);
                    p.setMenuCode("MD_SEQUENCE");
                    p.setCanAdd(true);
                    p.setCanEdit(true);
                    p.setCanDelete(true);
                    p.setCanPrint(true);
                    roleMenuPermissionRepository.save(p);
                }
            }
            if (appRoleRepository != null) {
                for (com.vaadinerp.security.entity.AppRole r : appRoleRepository.findAll()) {
                    if (r.getRoleCode() != null && r.getRoleCode().toUpperCase().contains("ADMIN")
                            && roleMenuPermissionRepository.findByRoleCodeAndMenuCode(r.getRoleCode(), "MD_SEQUENCE")
                                    .isEmpty()) {
                        RoleMenuPermission p = new RoleMenuPermission();
                        p.setRoleCode(r.getRoleCode());
                        p.setMenuCode("MD_SEQUENCE");
                        p.setCanAdd(true);
                        p.setCanEdit(true);
                        p.setCanDelete(true);
                        p.setCanPrint(true);
                        roleMenuPermissionRepository.save(p);
                    }
                }
            }
        }

        ensureMenuExists("GRP_SYSTEM", "Sistem & Keamanan", null, "COG", 30, "GROUP");
        ensureAdminOnlyMenuExists("FIELD_AUDIT_LOG", "Field Audit Log Viewer", "GRP_SYSTEM", "CLOCK", 20, "ITEM");

        AppMenu fieldLogMenu = appMenuRepository.findById("FIELD_AUDIT_LOG").orElse(null);
        String actualSysParent = fieldLogMenu != null && fieldLogMenu.getParentMenuCode() != null
                ? fieldLogMenu.getParentMenuCode()
                : (appMenuRepository.existsById("SYS_FORM") ? "SYS_FORM" : "GRP_SYSTEM");
        ensureAdminOnlyMenuExists("AUDIT_TRAIL_RESTORE", "Audit Trail & Restore Center", actualSysParent, "SHIELD", 25,
                "ITEM");

        ensureMenuExists("GRP_MFG", "Manufaktur & Produksi", null, "FACTORY", 15, "GROUP");
        ensureMenuExists("PRODUCTION_SCHEDULER", "Production Gantt Scheduler", "GRP_MFG", "CALENDAR_CLOCK", 10, "ITEM");
        if (roleMenuPermissionRepository != null) {
            for (String rCode : java.util.List.of("STAFF", "ADMIN", "SUPER_ADMIN")) {
                if (roleMenuPermissionRepository.findByRoleCodeAndMenuCode("SUPER_ADMIN", "PRODUCTION_SCHEDULER")
                        .isEmpty()
                        && roleMenuPermissionRepository.findByRoleCodeAndMenuCode(rCode, "PRODUCTION_SCHEDULER")
                                .isEmpty()) {
                    RoleMenuPermission p = new RoleMenuPermission();
                    p.setRoleCode(rCode);
                    p.setMenuCode("PRODUCTION_SCHEDULER");
                    p.setCanAdd(true);
                    p.setCanEdit(true);
                    p.setCanDelete(true);
                    p.setCanPrint(true);
                    roleMenuPermissionRepository.save(p);
                }
            }
        }

        List<FormMeta> forms = formMetaRepository.findAll();
        int order = 10;
        for (FormMeta f : forms) {
            String code = f.getFormCode();
            if ("MD_SEQUENCE".equalsIgnoreCase(code)) {
                continue;
            }
            boolean isSubform = "SUBFORM".equalsIgnoreCase(f.getFormType())
                    || code.toUpperCase().endsWith("_DTL")
                    || code.toUpperCase().endsWith("_DETAIL")
                    || (f.getFormTitle() != null && f.getFormTitle().toLowerCase().startsWith("detail "));

            if (isSubform) {
                appMenuRepository.findById(code).ifPresent(appMenuRepository::delete);
                continue;
            }

            AppMenu m = appMenuRepository.findById(code).orElse(null);
            if (m == null) {
                // Menu untuk form ini sudah tidak ada atau telah dihapus oleh admin dari
                // Manajemen Menu.
                // Jangan buat ulang secara otomatis agar menu yang dihapus tetap terhapus.
                continue;
            }
            m.setMenuTitle(f.getFormTitle());
            m.setRoutePath(code);
            m.setIconName("FILE_TEXT_O");
            if (m.getDisplayOrder() == null)
                m.setDisplayOrder(order);
            appMenuRepository.save(m);
            order += 10;

            if (roleMenuPermissionRepository.findByRoleCodeAndMenuCode("SUPER_ADMIN", code).isEmpty()
                    && roleMenuPermissionRepository.findByRoleCodeAndMenuCode("STAFF", code).isEmpty()) {
                RoleMenuPermission perm = new RoleMenuPermission();
                perm.setRoleCode("STAFF");
                perm.setMenuCode(code);
                perm.setCanAdd(true);
                perm.setCanEdit(true);
                perm.setCanDelete(false);
                perm.setCanPrint(true);
                roleMenuPermissionRepository.save(perm);
            }
        }

        if (roleMenuPermissionRepository != null) {
            java.util.List<String> adminTools = java.util.List.of("LOV_BUILDER", "STANDARD_FORMAT",
                    "FORM_ACTION_BUILDER", "MD_SEQUENCE", "FIELD_AUDIT_LOG", "REPORT_BUILDER",
                    "AUDIT_TRAIL_RESTORE", "DB_EXPLORER", "SECURITY_ADMIN", "FORM_BUILDER");
            for (String toolCode : adminTools) {
                for (RoleMenuPermission p : roleMenuPermissionRepository.findAll()) {
                    if (toolCode.equalsIgnoreCase(p.getMenuCode())) {
                        String r = p.getRoleCode();
                        if (r != null && !"SUPER_ADMIN".equalsIgnoreCase(r) && !"ADMIN".equalsIgnoreCase(r)
                                && !r.toUpperCase().contains("ADMIN")) {
                            roleMenuPermissionRepository.delete(p);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper: membuat menu jika belum ada di database.
     */
    private void ensureMenuExists(String code, String title, String parentCode, String icon, int order, String type) {
        if (!appMenuRepository.existsById(code)) {
            AppMenu m = new AppMenu();
            m.setMenuCode(code);
            m.setMenuTitle(title);
            m.setParentMenuCode(parentCode);
            m.setIconName(icon);
            m.setDisplayOrder(order);
            m.setMenuType(type);
            m.setRoutePath(code.toLowerCase().replace('_', '-'));
            appMenuRepository.save(m);
        }
        if (!"GROUP".equals(type) && roleMenuPermissionRepository != null
                && roleMenuPermissionRepository.findByRoleCodeAndMenuCode("SUPER_ADMIN", code).isEmpty()
                && roleMenuPermissionRepository.findByRoleCodeAndMenuCode("STAFF", code).isEmpty()) {
            RoleMenuPermission perm = new RoleMenuPermission();
            perm.setRoleCode("STAFF");
            perm.setMenuCode(code);
            perm.setCanAdd(true);
            perm.setCanEdit(true);
            perm.setCanDelete(true);
            perm.setCanPrint(true);
            roleMenuPermissionRepository.save(perm);
        }
    }

    private void ensureAdminOnlyMenuExists(String code, String title, String parentCode, String icon, int order,
            String type) {
        AppMenu m = appMenuRepository.findById(code).orElseGet(AppMenu::new);
        m.setMenuCode(code);
        m.setMenuTitle(title);
        m.setParentMenuCode(parentCode);
        m.setIconName(icon);
        m.setDisplayOrder(order);
        m.setMenuType(type);
        if (m.getRoutePath() == null || m.getRoutePath().isEmpty()) {
            m.setRoutePath(code.toLowerCase().replace('_', '-'));
        }
        appMenuRepository.save(m);
        if (!"GROUP".equals(type) && roleMenuPermissionRepository != null) {
            for (String rCode : java.util.List.of("ADMIN", "SUPER_ADMIN")) {
                if (roleMenuPermissionRepository.findByRoleCodeAndMenuCode(rCode, code).isEmpty()) {
                    RoleMenuPermission perm = new RoleMenuPermission();
                    perm.setRoleCode(rCode);
                    perm.setMenuCode(code);
                    perm.setCanAdd(true);
                    perm.setCanEdit(true);
                    perm.setCanDelete(true);
                    perm.setCanPrint(true);
                    roleMenuPermissionRepository.save(perm);
                }
            }
            if (appRoleRepository != null) {
                for (com.vaadinerp.security.entity.AppRole r : appRoleRepository.findAll()) {
                    if (r.getRoleCode() != null && r.getRoleCode().toUpperCase().contains("ADMIN") &&
                            roleMenuPermissionRepository.findByRoleCodeAndMenuCode(r.getRoleCode(), code).isEmpty()) {
                        RoleMenuPermission perm = new RoleMenuPermission();
                        perm.setRoleCode(r.getRoleCode());
                        perm.setMenuCode(code);
                        perm.setCanAdd(true);
                        perm.setCanEdit(true);
                        perm.setCanDelete(true);
                        perm.setCanPrint(true);
                        roleMenuPermissionRepository.save(perm);
                    }
                }
            }
        }
    }

    private boolean matchesSearchOrFav(AppMenu menu, Set<String> favMenuCodes) {
        List<AppMenu> children = appMenuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(menu.getMenuCode());
        List<AppMenu> accessibleChildren = children.stream()
                .filter(c -> securityService.hasMenuAccess(c.getMenuCode()))
                .toList();

        boolean isGroup = "GROUP".equalsIgnoreCase(menu.getMenuType()) || !accessibleChildren.isEmpty();
        if (isGroup) {
            for (AppMenu child : accessibleChildren) {
                if (matchesSearchOrFav(child, favMenuCodes)) {
                    return true;
                }
            }
            return false;
        } else {
            if (!securityService.hasMenuAccess(menu.getMenuCode()))
                return false;
            if (showFavoritesOnly && (favMenuCodes == null || !favMenuCodes.contains(menu.getMenuCode()))) {
                return false;
            }
            if (!menuSearchText.isEmpty()
                    && (menu.getMenuTitle() == null || !menu.getMenuTitle().toLowerCase().contains(menuSearchText))) {
                return false;
            }
            return true;
        }
    }

    /**
     * Membangun Tree Sidebar murni menggunakan HTML Flexbox Div.
     * Menggantikan Vaadin Details agar bebas dari gangguan Shadow DOM browser.
     * Indentasi dihitung pasti dalam pixel: BaseIndent = 16 + (depth * 24).
     */
    private void buildCustomMenuTree(String parentCode, VerticalLayout container, int depth) {
        List<AppMenu> menus;
        if (parentCode == null) {
            menus = appMenuRepository.findByParentMenuCodeIsNullOrderByDisplayOrderAsc();
        } else {
            menus = appMenuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(parentCode);
        }

        AppUser currentUser = securityService.getCurrentUser();
        Set<String> favMenuCodes = currentUser != null
                ? appUserFavoriteMenuRepository.findByUsername(currentUser.getUsername()).stream()
                        .map(menu -> menu.getMenuCode())
                        .collect(Collectors.toSet())
                : java.util.Collections.emptySet();

        for (AppMenu menu : menus) {
            if (!matchesSearchOrFav(menu, favMenuCodes)) {
                continue;
            }

            List<AppMenu> children = appMenuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(menu.getMenuCode());
            List<AppMenu> accessibleChildren = children.stream()
                    .filter(c -> securityService.hasMenuAccess(c.getMenuCode()))
                    .toList();

            boolean isGroup = "GROUP".equalsIgnoreCase(menu.getMenuType()) || !accessibleChildren.isEmpty();

            System.out.println("[MENU-TREE] code=" + menu.getMenuCode()
                    + " | type=" + menu.getMenuType()
                    + " | parent=" + menu.getParentMenuCode()
                    + " | isGroup=" + isGroup
                    + " | children=" + children.size()
                    + " | accessibleChildren=" + accessibleChildren.size()
                    + " | hasAccess=" + securityService.hasMenuAccess(menu.getMenuCode()));

            if (isGroup) {
                // GROUP: tampilkan hanya jika punya minimal 1 child yang accessible
                if (accessibleChildren.isEmpty()) {
                    System.out.println("[MENU-TREE]   → SKIPPED (group tanpa child accessible)");
                    continue;
                }
            } else {
                // ITEM: cek permission langsung
                if (!securityService.hasMenuAccess(menu.getMenuCode())) {
                    System.out.println("[MENU-TREE]   → SKIPPED (item tanpa permission)");
                    continue;
                }
            }
            System.out.println("[MENU-TREE]   → SHOWN");

            int baseIndent = 16 + (depth * 22);

            if (isGroup) {
                Div groupRow = new Div();
                groupRow.setWidthFull();
                groupRow.getStyle()
                        .set("padding", "9px 14px 9px " + baseIndent + "px")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("cursor", "pointer")
                        .set("user-select", "none")
                        .set("transition", "background 0.15s ease")
                        .set("box-sizing", "border-box");

                groupRow.getElement().addEventListener("mouseover", e -> {
                }).addEventData("element.style.background='rgba(255,255,255,0.06)'");
                groupRow.getElement().addEventListener("mouseout", e -> {
                }).addEventData("element.style.background=''");

                Icon chevron = VaadinIcon.CHEVRON_RIGHT.create();
                chevron.setSize("13px");
                chevron.getStyle()
                        .set("color", "#64748b")
                        .set("margin-right", "8px")
                        .set("flex-shrink", "0")
                        .set("transition", "transform 0.2s ease");

                Icon folder = resolveIcon(menu.getIconName());
                folder.setSize("16px");
                folder.getStyle()
                        .set("color", depth == 0 ? "#38bdf8" : "#818cf8")
                        .set("margin-right", "10px")
                        .set("flex-shrink", "0");

                Span title = new Span(menu.getMenuTitle());
                title.getStyle()
                        .set("font-size", depth == 0 ? "0.82rem" : "0.80rem")
                        .set("font-weight", depth == 0 ? "700" : "600")
                        .set("color", depth == 0 ? "#f1f5f9" : "#cbd5e1")
                        .set("letter-spacing", depth == 0 ? "0.6px" : "0px")
                        .set("text-transform", depth == 0 ? "uppercase" : "none")
                        .set("white-space", "nowrap")
                        .set("overflow", "hidden")
                        .set("text-overflow", "ellipsis");

                groupRow.add(chevron, folder, title);

                VerticalLayout childBox = new VerticalLayout();
                childBox.setPadding(false);
                childBox.setSpacing(false);
                childBox.setWidthFull();

                boolean isFiltering = !menuSearchText.isEmpty() || showFavoritesOnly;
                boolean isOpenByDefault = isFiltering;
                childBox.setVisible(isOpenByDefault);
                if (isOpenByDefault) {
                    chevron.getStyle().set("transform", "rotate(90deg)");
                }

                groupRow.addClickListener(e -> {
                    boolean nowVisible = !childBox.isVisible();
                    childBox.setVisible(nowVisible);
                    chevron.getStyle().set("transform", nowVisible ? "rotate(90deg)" : "rotate(0deg)");
                });

                buildCustomMenuTree(menu.getMenuCode(), childBox, depth + 1);

                container.add(groupRow, childBox);
            } else {
                // Leaf Clickable Item
                Div leafRow = new Div();
                leafRow.setWidthFull();
                // Indentasi leaf sama persis (baseIndent + 21px) sehingga sejajar sempurna
                // vertikal lurus
                leafRow.getStyle()
                        .set("padding", "8px 14px 8px " + (baseIndent + 21) + "px")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("cursor", "pointer")
                        .set("user-select", "none")
                        .set("transition", "all 0.15s ease")
                        .set("color", "#cbd5e1")
                        .set("box-sizing", "border-box")
                        .set("border-left", "3px solid transparent");

                leafRow.getElement().addEventListener("mouseover", e -> {
                }).addEventData("element.style.background='rgba(99,102,241,0.15)'");
                leafRow.getElement().addEventListener("mouseout", e -> {
                }).addEventData("element.style.background=''");

                Icon ic = resolveIcon(menu.getIconName());
                ic.setSize("16px");
                ic.getStyle()
                        .set("color", "#a5b4fc")
                        .set("margin-right", "10px")
                        .set("flex-shrink", "0");

                Span t = new Span(menu.getMenuTitle());
                t.getStyle()
                        .set("font-size", "0.85rem")
                        .set("font-weight", "400")
                        .set("white-space", "nowrap")
                        .set("overflow", "hidden")
                        .set("text-overflow", "ellipsis");

                leafRow.add(ic, t);

                boolean isFav = favMenuCodes.contains(menu.getMenuCode());
                if (isFav) {
                    Icon starIc = VaadinIcon.STAR.create();
                    starIc.setSize("12px");
                    starIc.getStyle().set("color", "#f59e0b").set("margin-left", "auto");
                    leafRow.add(starIc);
                }

                // Context Menu (Klik kanan pada menu yang ada routenya)
                ContextMenu contextMenu = new ContextMenu(leafRow);
                if (isFav) {
                    contextMenu.addItem("❌ Hapus dari Favorit", ev -> {
                        if (currentUser != null) {
                            appUserFavoriteMenuRepository.deleteByUsernameAndMenuCode(currentUser.getUsername(),
                                    menu.getMenuCode());
                            Notification.show("Dihapus dari favorit: " + menu.getMenuTitle(), 2500,
                                    Notification.Position.BOTTOM_END);
                            refreshFormMenu();
                        }
                    });
                } else {
                    contextMenu.addItem("⭐ Add to Favorite", ev -> {
                        if (currentUser != null) {
                            AppUserFavoriteMenu fav = new AppUserFavoriteMenu();
                            fav.setUsername(currentUser.getUsername());
                            fav.setMenuCode(menu.getMenuCode());
                            appUserFavoriteMenuRepository.save(fav);
                            Notification.show("Ditambahkan ke favorit: " + menu.getMenuTitle(), 2500,
                                    Notification.Position.BOTTOM_END);
                            refreshFormMenu();
                        }
                    });
                }

                leafRow.addClickListener(e -> {
                    setActiveLeaf(leafRow);
                    openMenuTab(menu);
                });

                leafRows.add(leafRow);
                container.add(leafRow);
            }
        }
    }

    private void setActiveLeaf(Div clickedRow) {
        clearActiveLeaves();
        clickedRow.getStyle().set("background", "rgba(99,102,241,0.28)");
        clickedRow.getStyle().set("color", "#ffffff");
        clickedRow.getStyle().set("border-left-color", "#818cf8");
    }

    private void clearActiveLeaves() {
        for (Div row : leafRows) {
            row.getStyle().remove("background");
            row.getStyle().set("color", "#cbd5e1");
            row.getStyle().set("border-left-color", "transparent");
        }
    }

    public void openMenuTab(AppMenu menu) {
        openMenuTab(menu, (Object) null);
    }

    public void openMenuTab(AppMenu menu, String extra) {
        openMenuTab(menu, (Object) extra);
    }

    public void openMenuTab(AppMenu menu, Object extra) {
        String code = menu.getMenuCode();
        String title = menu.getMenuTitle();

        Component content = switch (code) {
            case "FORM_BUILDER" ->
                new FormBuilderView(formMetaRepository, lovMetaRepository, dynamicDataService, this::refreshFormMenu);
            case "DB_EXPLORER" -> new DbExplorerView(dynamicDataService, securityService);
            case "REPORT_BUILDER" -> new ReportBuilderView(reportMetaRepository, formMetaRepository);
            case "REPORT_VIEWER" -> new ReportViewerView(reportMetaRepository, dynamicDataService);
            case "LOV_BUILDER" -> new LovBuilderView(lovMetaRepository, dynamicDataService);
            case "FORM_ACTION_BUILDER" -> new FormActionBuilderView(dynamicDataService.getFormActionMetaRepository(),
                    formMetaRepository, lovMetaRepository, dynamicDataService);
            case "STANDARD_FORMAT" -> new StandardFormatView(standardFormatService);
            case "FIELD_AUDIT_LOG" -> new FieldAuditLogView(dynamicDataService, securityService);
            case "AUDIT_TRAIL_RESTORE" -> new AuditTrailView(dynamicDataService, dynamicDataService.getJdbcTemplate());
            case "PRODUCTION_SCHEDULER" -> new ProductionSchedulerView(dynamicDataService);
            case "SECURITY_ADMIN" -> new UserAuthorityAdminView(appUserRepository, appRoleRepository, appMenuRepository,
                    roleMenuPermissionRepository, appUserFavoriteMenuRepository, formMetaRepository,
                    reportMetaRepository, securityService);
            default -> {
                Optional<FormMeta> optForm = formMetaRepository.findById(code);
                if (optForm.isPresent()) {
                    FormMeta form = optForm.get();
                    if ("MASTER_DETAIL".equalsIgnoreCase(form.getFormType())) {
                        GenericMasterDetailFormView mdView = new GenericMasterDetailFormView(formMetaRepository,
                                dynamicDataService, securityService);
                        mdView.setParameter(null, code);
                        mdView.hideTitle();
                        mdView.getStyle().set("padding", "4px");
                        mdView.applyInitialParameters(extra);
                        mdView.setCloseHandler(() -> {
                            Tab tab = openTabs.get(code);
                            if (tab != null) {
                                tabSheet.remove(tab);
                                openTabs.remove(code);
                                if (openTabs.isEmpty()) {
                                    tabSheet.setSelectedIndex(0);
                                    clearActiveLeaves();
                                }
                            }
                        });
                        yield mdView;
                    } else {
                        GenericFormView gView = new GenericFormView(formMetaRepository, dynamicDataService,
                                securityService);
                        gView.setParameter(null, code);
                        gView.hideTitle();
                        gView.getStyle().set("padding", "4px");
                        gView.applyInitialParameters(extra);
                        gView.setCloseHandler(() -> {
                            Tab tab = openTabs.get(code);
                            if (tab != null) {
                                tabSheet.remove(tab);
                                openTabs.remove(code);
                                if (openTabs.isEmpty()) {
                                    tabSheet.setSelectedIndex(0);
                                    clearActiveLeaves();
                                }
                            }
                        });
                        yield gView;
                    }
                }

                VerticalLayout placeholder = new VerticalLayout();
                placeholder.setSizeFull();
                placeholder.setAlignItems(Alignment.CENTER);
                placeholder.setJustifyContentMode(JustifyContentMode.CENTER);
                Span ph = new Span("Menu '" + title + "' belum memiliki implementasi View.");
                ph.getStyle().set("color", "#64748b");
                placeholder.add(ph);
                yield placeholder;
            }
        };

        openTab(code, title, content);
    }

    public void openTabByCode(String code, String title) {
        openTabByCode(code, title, (Object) null);
    }

    public void openTabByCode(String code, String title, String extra) {
        openTabByCode(code, title, (Object) extra);
    }

    public void openTabByCode(String code, String title, Object extra) {
        if (code == null || code.isBlank())
            return;
        if (openTabs.containsKey(code)) {
            Tab tab = openTabs.get(code);
            tabSheet.setSelectedTab(tab);
            Component content = tabSheet.getComponent(tab);
            if (content instanceof GenericMasterDetailFormView mdView) {
                mdView.applyInitialParameters(extra);
            } else if (content instanceof GenericFormView gView) {
                gView.applyInitialParameters(extra);
            }
            return;
        }
        String targetCode = code;
        String targetTitle = title != null && !title.isBlank() ? title : code;

        Optional<FormMeta> optForm = formMetaRepository.findById(targetCode);
        if (optForm.isEmpty()) {
            for (FormMeta fm : formMetaRepository.findAll()) {
                if (fm.getFormCode() != null && fm.getFormCode().equalsIgnoreCase(targetCode)) {
                    optForm = Optional.of(fm);
                    targetCode = fm.getFormCode();
                    break;
                } else if (title != null && fm.getFormTitle() != null
                        && fm.getFormTitle().equalsIgnoreCase(title.trim())) {
                    optForm = Optional.of(fm);
                    targetCode = fm.getFormCode();
                    break;
                }
            }
        }
        if (optForm.isEmpty()) {
            for (AppMenu m : appMenuRepository.findAll()) {
                if (m.getMenuCode() != null && m.getMenuCode().equalsIgnoreCase(targetCode)) {
                    targetCode = m.getMenuCode();
                    if (title == null || title.isBlank())
                        targetTitle = m.getMenuTitle();
                    break;
                } else if (title != null && m.getMenuTitle() != null
                        && m.getMenuTitle().equalsIgnoreCase(title.trim())) {
                    targetCode = m.getMenuCode();
                    targetTitle = m.getMenuTitle();
                    break;
                }
            }
        } else {
            if (title != null && !title.isBlank())
                targetTitle = title;
            else if (optForm.get().getFormTitle() != null)
                targetTitle = optForm.get().getFormTitle();
        }

        AppMenu menu = new AppMenu();
        menu.setMenuCode(targetCode);
        menu.setMenuTitle(targetTitle);
        openMenuTab(menu, extra);
    }

    private void openChangePasswordDialog() {
        AppUser curr = securityService.getCurrentUser();
        if (curr == null) {
            Notification.show("Anda belum login!", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        H3 title = new H3("Ganti Password");
        title.getStyle().set("margin", "0 0 16px 0").set("color", "#1e293b");

        PasswordField oldPassField = new PasswordField("Password Lama");
        oldPassField.setWidthFull();
        PasswordField newPassField = new PasswordField("Password Baru");
        newPassField.setWidthFull();
        PasswordField confirmPassField = new PasswordField("Konfirmasi Password Baru");
        confirmPassField.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(title, oldPassField, newPassField, confirmPassField);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        Button btnSimpan = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            String oldP = oldPassField.getValue();
            String newP = newPassField.getValue();
            String confP = confirmPassField.getValue();

            if (oldP.isEmpty() || newP.isEmpty() || confP.isEmpty()) {
                Notification.show("Semua field harus diisi!", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (!newP.equals(confP)) {
                Notification.show("Password baru dan konfirmasi tidak cocok!", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            String error = securityService.changePassword(curr, oldP, newP);
            if (error != null) {
                Notification.show(error, 3000, Notification.Position.TOP_CENTER);
                return;
            }
            Notification.show("Password berhasil diubah!", 3000, Notification.Position.TOP_CENTER);
            dialog.close();
        });
        btnSimpan.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSimpan.getStyle().set("background", "#4f46e5");

        Button btnBatal = new Button("Batal", e -> dialog.close());
        btnBatal.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout btnLayout = new HorizontalLayout(btnBatal, btnSimpan);
        btnLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btnLayout.setWidthFull();
        btnLayout.getStyle().set("margin-top", "20px");

        formLayout.add(btnLayout);
        dialog.add(formLayout);
        dialog.open();
    }

    private Icon resolveIcon(String iconName) {
        if (iconName == null || iconName.isBlank())
            return VaadinIcon.FILE.create();
        try {
            return VaadinIcon.valueOf(iconName.toUpperCase().replace("-", "_")).create();
        } catch (IllegalArgumentException e) {
            return switch (iconName.toUpperCase()) {
                case "FILE_TEXT" -> VaadinIcon.FILE_TEXT.create();
                case "FILE_TEXT_O" -> VaadinIcon.FILE_TEXT_O.create();
                case "COG" -> VaadinIcon.COG.create();
                case "BRIEFCASE" -> VaadinIcon.BRIEFCASE.create();
                default -> VaadinIcon.FILE.create();
            };
        }
    }

    private void setupTabSheet() {
        tabSheet.setSizeFull();
        tabSheet.getStyle().set("flex-grow", "1");
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

        H1 mainTitle = new H1("Selamat Datang");
        mainTitle.getStyle().set("margin", "10px 0").set("color", "#111827");

        Paragraph desc = new Paragraph(
                "Gunakan menu di sebelah kiri untuk mengelola data form dinamis, membuat form baru menggunakan Form Builder, atau mengeksplorasi tabel fisik yang terbuat di schema database 'dynamic'.");
        desc.getStyle().set("color", "#4b5563").set("text-align", "center").set("max-width", "600px");

        VerticalLayout cards = new VerticalLayout();
        cards.setPadding(false);
        cards.setSpacing(true);
        cards.setMaxWidth("600px");

        cards.add(
                createFeatureCard("Form Builder",
                        "Buat form baru secara interaktif, definisikan field, tipe data, LOV, serta cascading filters secara visual.",
                        VaadinIcon.TOOLS),
                createFeatureCard("Multi-tab Workspace",
                        "Buka beberapa form dinamis sekaligus dalam bentuk tab dan kelola data transaksi secara terisolasi.",
                        VaadinIcon.GRID_BIG),
                createFeatureCard("Database Schema Isolation",
                        "Semua tabel dinamis di-generate dan dikelola dalam schema PostgreSQL terpisah bernama 'dynamic'.",
                        VaadinIcon.DATABASE));

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
            closeTabById(tabId);
        });

        tabSheet.setSelectedTab(tab);
    }

    public void closeTabById(String tabId) {
        Tab tab = openTabs.remove(tabId);
        if (tab != null) {
            tabSheet.remove(tab);
        }
        if (openTabs.isEmpty()) {
            tabSheet.setSelectedIndex(0);
            clearActiveLeaves();
        }
    }

    private void refreshFormMenu() {
        cleanAndSyncMenuTree();
        menuContainer.removeAll();
        leafRows.clear();
        buildCustomMenuTree(null, menuContainer, 0);
    }
}
