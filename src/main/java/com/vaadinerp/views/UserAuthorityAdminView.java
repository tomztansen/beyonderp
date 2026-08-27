package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadinerp.components.StandardActionToolbar;
import com.vaadinerp.components.StandardGridUtils;
import com.vaadinerp.security.entity.AppMenu;
import com.vaadinerp.security.entity.AppRole;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.entity.RoleMenuPermission;
import com.vaadinerp.security.repository.AppMenuRepository;
import com.vaadinerp.security.repository.AppRoleRepository;
import com.vaadinerp.security.repository.AppUserRepository;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.security.repository.AppUserFavoriteMenuRepository;
import com.vaadinerp.security.repository.RoleMenuPermissionRepository;
import com.vaadinerp.security.service.SessionSecurityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Panel Administrasi Keamanan Terpadu.
 * Dibuka sebagai Tab di dalam PortalView.
 * Fitur:
 * - Menggunakan StandardActionToolbar (Tambah, Refresh) di atas setiap grid
 * - Menggunakan StandardGridUtils.attachGridFilters untuk pencarian/filter di
 * header row
 */
@SuppressWarnings({ "null", "unused" })
public class UserAuthorityAdminView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(UserAuthorityAdminView.class);

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final AppMenuRepository menuRepository;
    private final RoleMenuPermissionRepository permissionRepository;
    private final AppUserFavoriteMenuRepository favoriteMenuRepository;
    private final FormMetaRepository formMetaRepository;
    private final ReportMetaRepository reportMetaRepository;
    private final SessionSecurityService securityService;

    private final Grid<AppUser> userGrid = new Grid<>(AppUser.class, false);
    private final Grid<AppRole> roleGrid = new Grid<>(AppRole.class, false);
    private final TreeGrid<AppMenu> matrixTreeGrid = new TreeGrid<>();
    private final Map<String, RoleMenuPermission> currentRolePermissions = new java.util.concurrent.ConcurrentHashMap<>();
    private final TreeGrid<AppMenu> menuTreeGrid = new TreeGrid<>();

    private Runnable userFilterRefresher;
    private Runnable roleFilterRefresher;
    private Runnable matrixFilterRefresher;
    private Runnable menuFilterRefresher;

    public UserAuthorityAdminView(AppUserRepository userRepository, AppRoleRepository roleRepository,
            AppMenuRepository menuRepository,
            RoleMenuPermissionRepository permissionRepository,
            AppUserFavoriteMenuRepository favoriteMenuRepository,
            FormMetaRepository formMetaRepository,
            ReportMetaRepository reportMetaRepository,
            SessionSecurityService securityService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
        this.favoriteMenuRepository = favoriteMenuRepository;
        this.formMetaRepository = formMetaRepository;
        this.reportMetaRepository = reportMetaRepository;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Paragraph desc = new Paragraph(
                "Manage users, roles, menu tree structure, and granular access rights (RBAC per action) on the PostgreSQL public schema.");
        desc.getStyle().set("color", "#64748b").set("margin-top", "0");

        Tab tabUsers = new Tab("User Management");
        Tab tabRoles = new Tab("Role Management");
        Tab tabMenus = new Tab("Menu Management");
        Tab tabMatrix = new Tab("Access Authority Matrix");
        Tabs tabs = new Tabs(tabUsers, tabRoles, tabMenus, tabMatrix);
        tabs.setWidthFull();

        VerticalLayout layoutUsers = setupUserTab();
        VerticalLayout layoutRoles = setupRoleTab();
        VerticalLayout layoutMenus = setupMenuTab();
        VerticalLayout layoutMatrix = setupMatrixTab();
        layoutRoles.setVisible(false);
        layoutMenus.setVisible(false);
        layoutMatrix.setVisible(false);

        tabs.addSelectedChangeListener(e -> {
            layoutUsers.setVisible(tabs.getSelectedTab() == tabUsers);
            layoutRoles.setVisible(tabs.getSelectedTab() == tabRoles);
            layoutMenus.setVisible(tabs.getSelectedTab() == tabMenus);
            layoutMatrix.setVisible(tabs.getSelectedTab() == tabMatrix);
            if (tabs.getSelectedTab() == tabUsers)
                refreshUserGrid();
            if (tabs.getSelectedTab() == tabRoles)
                refreshRoleGrid();
            if (tabs.getSelectedTab() == tabMenus)
                refreshMenuTreeGrid();
            if (tabs.getSelectedTab() == tabMatrix)
                refreshMatrixGrid();
        });

        add(desc, tabs, layoutUsers, layoutRoles, layoutMenus, layoutMatrix);
        refreshUserGrid();
    }

    // ============================
    // TAB 1: MANAJEMEN PENGGUNA
    // ============================

    private VerticalLayout setupUserTab() {
        VerticalLayout l = new VerticalLayout();
        l.setSizeFull();
        l.setPadding(false);

        StandardActionToolbar toolbar = new StandardActionToolbar()
                .onNew(() -> openUserDialog(null))
                .onRefresh(this::refreshUserGrid);

        var colUser = userGrid.addColumn(AppUser::getUsername).setHeader("Username").setSortable(true)
                .setAutoWidth(true);
        var colName = userGrid.addColumn(AppUser::getFullName).setHeader("Full Name").setSortable(true)
                .setAutoWidth(true);
        var colRole = userGrid.addColumn(u -> u.getRoles() != null ? String.join(", ", u.getRoles()) : "")
                .setHeader("Role(s)").setSortable(false).setAutoWidth(true);
        var colStatus = userGrid.addColumn(u -> Boolean.TRUE.equals(u.getIsActive()) ? "✅ Active" : "❌ Inactive")
                .setHeader("Status").setAutoWidth(true);

        userGrid.addComponentColumn(u -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            Button editBtn = new com.vaadinerp.components.SafeButton(VaadinIcon.EDIT.create(), e -> openUserDialog(u));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.getElement().setAttribute("title", "Edit User");
            Button deleteBtn = new com.vaadinerp.components.SafeButton(VaadinIcon.TRASH.create(), e -> {
                ConfirmDialog confirmDlg = new ConfirmDialog();
                confirmDlg.setHeader("Confirm Delete");
                confirmDlg.setText("Are you sure you want to delete user '" + u.getUsername() + "'?");
                confirmDlg.setCancelable(true);
                confirmDlg.setConfirmText("Delete");
                confirmDlg.setConfirmButtonTheme("error primary");
                confirmDlg.addConfirmListener(ev -> {
                    userRepository.delete(u);
                    log.info("[AUDIT] User '{}' deleted user '{}'",
                            securityService.getCurrentUser() != null ? securityService.getCurrentUser().getUsername()
                                    : "SYSTEM",
                            u.getUsername());
                    Notification.show("User '" + u.getUsername() + "' deleted!", 2000,
                            Notification.Position.BOTTOM_END);
                    refreshUserGrid();
                });
                confirmDlg.open();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.getElement().setAttribute("title", "Delete User");
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Action").setAutoWidth(true);

        userGrid.setHeight("100%");
        userGrid.getStyle().set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        Map<Grid.Column<AppUser>, Function<AppUser, String>> getterMap = Map.of(
                colUser, u -> u.getUsername() != null ? u.getUsername() : "",
                colName, u -> u.getFullName() != null ? u.getFullName() : "",
                colRole, u -> u.getRoles() != null ? String.join(", ", u.getRoles()) : "",
                colStatus, u -> Boolean.TRUE.equals(u.getIsActive()) ? "Active" : "Inactive");
        userFilterRefresher = StandardGridUtils.attachGridFilters(userGrid, getterMap, userRepository::findAll);
        toolbar.add(StandardGridUtils.createExportExcelButton(userGrid, "users_export", getterMap));

        l.add(toolbar, userGrid);
        l.setFlexGrow(1, userGrid);
        return l;
    }

    private void openUserDialog(AppUser existing) {
        boolean isNew = existing == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((existing == null) ? "Add New User" : "Edit User: " + existing.getUsername());
        dialog.setWidth("460px");

        FormLayout form = new FormLayout();

        TextField usernameField = new TextField("Username");
        usernameField.setWidthFull();
        usernameField.setRequired(true);

        TextField fullNameField = new TextField("Full Name");
        fullNameField.setWidthFull();

        PasswordField passwordField = new PasswordField(isNew ? "Password" : "New Password (leave blank if unchanged)");
        passwordField.setWidthFull();
        if (isNew)
            passwordField.setRequired(true);

        com.vaadin.flow.component.combobox.MultiSelectComboBox<String> roleSelect = new com.vaadin.flow.component.combobox.MultiSelectComboBox<>(
                "Role(s)");
        roleSelect.setWidthFull();
        roleSelect.setItems(query -> {
            String filter = query.getFilter().orElse("");
            org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest
                    .of(query.getPage(), query.getPageSize());
            return roleRepository.findByRoleCodeContainingIgnoreCase(filter, pageRequest).stream()
                    .map(com.vaadinerp.security.entity.AppRole::getRoleCode);
        });
        roleSelect.setRequired(true);

        Checkbox activeCheckbox = new Checkbox("Active");
        activeCheckbox.setValue(true);

        if (existing != null) {
            usernameField.setValue(existing.getUsername());
            usernameField.setReadOnly(true);
            fullNameField.setValue(existing.getFullName() != null ? existing.getFullName() : "");
            roleSelect.setValue(existing.getRoles() != null ? existing.getRoles() : java.util.Collections.emptySet());
            activeCheckbox.setValue(Boolean.TRUE.equals(existing.getIsActive()));
        }

        form.add(usernameField, fullNameField, passwordField, roleSelect, activeCheckbox);

        Button saveBtn = new com.vaadinerp.components.SafeButton("Save", VaadinIcon.CHECK.create(), e -> {
            String username = usernameField.getValue();
            if (username == null || username.isBlank()) {
                Notification n = Notification.show("Username is required!", 3000, Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            java.util.Set<String> roles = roleSelect.getValue();
            if (roles == null || roles.isEmpty()) {
                Notification n = Notification.show("At least one role must be selected!", 3000,
                        Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (isNew && (passwordField.getValue() == null || passwordField.getValue().isBlank())) {
                Notification n = Notification.show("Password is required for new users!", 3000,
                        Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (isNew && userRepository.findByUsernameIgnoreCase(username.trim()).isPresent()) {
                Notification n = Notification.show("Username '" + username.trim() + "' is already in use!", 3000,
                        Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            AppUser user = isNew ? new AppUser() : existing;
            user.setUsername(username.trim());
            user.setFullName(fullNameField.getValue());
            user.setRoles(new java.util.HashSet<>(roles));
            user.setIsActive(activeCheckbox.getValue());

            String pwd = passwordField.getValue();
            if (pwd != null && !pwd.isBlank()) {
                user.setPasswordHash(securityService.encodePassword(pwd));
            }

            userRepository.save(user);
            log.info("[AUDIT] User '{}' {} user '{}'",
                    securityService.getCurrentUser() != null ? securityService.getCurrentUser().getUsername()
                            : "SYSTEM",
                    (isNew ? "created" : "updated"), user.getUsername());
            dialog.close();
            refreshUserGrid();
            Notification.show("User '" + username + "' successfully " + (isNew ? "created" : "updated") + "!", 2000,
                    Notification.Position.BOTTOM_END);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new com.vaadinerp.components.SafeButton("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    // ============================
    // TAB 2: MANAJEMEN ROLE
    // ============================

    private VerticalLayout setupRoleTab() {
        VerticalLayout l = new VerticalLayout();
        l.setSizeFull();
        l.setPadding(false);

        StandardActionToolbar toolbar = new StandardActionToolbar()
                .onNew(() -> openRoleDialog(null))
                .onRefresh(this::refreshRoleGrid);

        var colCode = roleGrid.addColumn(AppRole::getRoleCode).setHeader("Role Code").setSortable(true)
                .setAutoWidth(true);
        var colName = roleGrid.addColumn(AppRole::getRoleName).setHeader("Role Name").setSortable(true)
                .setAutoWidth(true);
        var colDesc = roleGrid.addColumn(AppRole::getDescription).setHeader("Description").setAutoWidth(true);

        roleGrid.addComponentColumn(r -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            Button editBtn = new com.vaadinerp.components.SafeButton(VaadinIcon.EDIT.create(), e -> openRoleDialog(r));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            Button deleteBtn = new com.vaadinerp.components.SafeButton(VaadinIcon.TRASH.create(), e -> {
                if (userRepository.existsByRoleCode(r.getRoleCode())) {
                    Notification n = Notification.show(
                            "Cannot delete role '" + r.getRoleCode()
                                    + "' because it is still assigned to one or more users!",
                            4000, Notification.Position.TOP_CENTER);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                ConfirmDialog confirmDlg = new ConfirmDialog();
                confirmDlg.setHeader("Confirm Delete");
                confirmDlg.setText("Are you sure you want to delete role '" + r.getRoleCode() + "'?");
                confirmDlg.setCancelable(true);
                confirmDlg.setConfirmText("Delete");
                confirmDlg.setConfirmButtonTheme("error primary");
                confirmDlg.addConfirmListener(ev -> {
                    roleRepository.delete(r);
                    log.info("[AUDIT] User '{}' deleted role '{}'",
                            securityService.getCurrentUser() != null ? securityService.getCurrentUser().getUsername()
                                    : "SYSTEM",
                            r.getRoleCode());
                    refreshRoleGrid();
                    Notification.show("Role '" + r.getRoleCode() + "' deleted!", 2000,
                            Notification.Position.BOTTOM_END);
                });
                confirmDlg.open();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Action").setAutoWidth(true);

        roleGrid.setHeight("100%");
        roleGrid.getStyle().set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        Map<Grid.Column<AppRole>, Function<AppRole, String>> getterMap = Map.of(
                colCode, r -> r.getRoleCode() != null ? r.getRoleCode() : "",
                colName, r -> r.getRoleName() != null ? r.getRoleName() : "",
                colDesc, r -> r.getDescription() != null ? r.getDescription() : "");
        roleFilterRefresher = StandardGridUtils.attachGridFilters(roleGrid, getterMap, roleRepository::findAll);
        toolbar.add(StandardGridUtils.createExportExcelButton(roleGrid, "roles_export", getterMap));

        l.add(toolbar, roleGrid);
        l.setFlexGrow(1, roleGrid);
        return l;
    }

    private void openRoleDialog(AppRole existing) {
        boolean isNew = existing == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((existing == null) ? "Add New Role" : "Edit Role: " + existing.getRoleCode());
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();

        TextField codeField = new TextField("Role Code");
        codeField.setWidthFull();
        codeField.setRequired(true);

        TextField nameField = new TextField("Role Name");
        nameField.setWidthFull();

        TextField descField = new TextField("Description");
        descField.setWidthFull();

        if (existing != null) {
            codeField.setValue(existing.getRoleCode());
            codeField.setReadOnly(true);
            nameField.setValue(existing.getRoleName() != null ? existing.getRoleName() : "");
            descField.setValue(existing.getDescription() != null ? existing.getDescription() : "");
        }

        form.add(codeField, nameField, descField);

        Button saveBtn = new com.vaadinerp.components.SafeButton("Save", e -> {
            if (codeField.getValue() == null || codeField.getValue().isBlank()) {
                Notification.show("Role Code is required!", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            AppRole role = (existing == null) ? new AppRole() : existing;
            if (existing == null)
                role.setRoleCode(codeField.getValue().trim().toUpperCase());
            role.setRoleName(nameField.getValue());
            role.setDescription(descField.getValue());
            roleRepository.save(role);
            log.info("[AUDIT] User '{}' {} role '{}'",
                    securityService.getCurrentUser() != null ? securityService.getCurrentUser().getUsername()
                            : "SYSTEM",
                    (isNew ? "created" : "updated"), role.getRoleCode());
            dialog.close();
            refreshRoleGrid();
            Notification.show("Role successfully " + (isNew ? "created" : "updated") + "!", 2000,
                    Notification.Position.BOTTOM_END);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new com.vaadinerp.components.SafeButton("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    // ============================
    // TAB 3: MANAJEMEN MENU (TreeGrid)
    // ============================

    private VerticalLayout setupMenuTab() {
        VerticalLayout l = new VerticalLayout();
        l.setSizeFull();
        l.setPadding(false);

        StandardActionToolbar toolbar = new StandardActionToolbar()
                .onNew(() -> openMenuDialog(null, null))
                .onRefresh(this::refreshMenuTreeGrid);
        toolbar.getBtnNew().setText("Add Menu Group");

        Button addItemBtn = new com.vaadinerp.components.SafeButton("Add Menu Item (Root)", VaadinIcon.PLUS.create(),
                e -> openMenuDialogItem(null, null));
        StandardActionToolbar.styleToolbarButton(addItemBtn, "#3b82f6");
        toolbar.add(addItemBtn);

        var colTitle = menuTreeGrid.addHierarchyColumn(AppMenu::getMenuTitle)
                .setHeader("Menu Title")
                .setAutoWidth(true)
                .setFlexGrow(1);

        var colCode = menuTreeGrid.addColumn(AppMenu::getMenuCode)
                .setHeader("Menu Code")
                .setSortable(true)
                .setAutoWidth(true);

        var colType = menuTreeGrid.addColumn(m -> {
            if ("GROUP".equalsIgnoreCase(m.getMenuType()))
                return "📂 Folder (Group)";
            return "📄 Item (Click)";
        }).setHeader("Type").setAutoWidth(true);

        menuTreeGrid.addColumn(AppMenu::getIconName)
                .setHeader("Icon")
                .setAutoWidth(true);

        menuTreeGrid.addColumn(m -> m.getDisplayOrder() != null ? m.getDisplayOrder() : 10)
                .setHeader("Order")
                .setAutoWidth(true);

        menuTreeGrid.addComponentColumn(m -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if ("GROUP".equalsIgnoreCase(m.getMenuType())) {
                Button addChildBtn = new com.vaadinerp.components.SafeButton(VaadinIcon.PLUS_CIRCLE_O.create(),
                        e -> openMenuDialog(null, m.getMenuCode()));
                addChildBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                addChildBtn.getElement().setAttribute("title", "Add Child Menu");
                addChildBtn.getStyle().set("color", "#22c55e");
                actions.add(addChildBtn);
            }

            Button editBtn = new com.vaadinerp.components.SafeButton(VaadinIcon.EDIT.create(),
                    e -> openMenuDialog(m, m.getParentMenuCode()));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.getElement().setAttribute("title", "Edit Menu");

            Button deleteBtn = new com.vaadinerp.components.SafeButton(VaadinIcon.TRASH.create(), e -> {
                ConfirmDialog confirmDlg = new ConfirmDialog();
                confirmDlg.setHeader("Confirm Delete");
                confirmDlg.setText("Are you sure you want to delete menu '" + m.getMenuCode()
                        + "' and all its submenus/permissions?");
                confirmDlg.setCancelable(true);
                confirmDlg.setConfirmText("Delete");
                confirmDlg.setConfirmButtonTheme("error primary");
                confirmDlg.addConfirmListener(ev -> {
                    deleteMenuAndDependencies(m);
                    log.info("[AUDIT] User '{}' deleted menu '{}'",
                            securityService.getCurrentUser() != null ? securityService.getCurrentUser().getUsername()
                                    : "SYSTEM",
                            m.getMenuCode());
                    refreshMenuTreeGrid();
                    Notification.show("Menu '" + m.getMenuCode() + "' deleted!", 2000,
                            Notification.Position.BOTTOM_END);
                });
                confirmDlg.open();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.getElement().setAttribute("title", "Delete Menu");

            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Action").setAutoWidth(true);

        menuTreeGrid.setHeight("100%");
        menuTreeGrid.getStyle().set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        Paragraph hint = new Paragraph(
                "💡 Hierarchical menu: Create a GROUP first, then add ITEMs underneath. Use the ⊕ button on GROUP rows to add children.");
        hint.getStyle().set("font-size", "0.8rem").set("color", "#64748b").set("margin", "0");

        Map<Grid.Column<AppMenu>, Function<AppMenu, String>> getterMap = Map.of(
                colTitle, m -> m.getMenuTitle() != null ? m.getMenuTitle() : "",
                colCode, m -> m.getMenuCode() != null ? m.getMenuCode() : "",
                colType, m -> m.getMenuType() != null ? m.getMenuType() : "");
        menuFilterRefresher = StandardGridUtils.attachTreeGridFilters(
                menuTreeGrid,
                getterMap,
                menuRepository::findAllByOrderByDisplayOrderAsc,
                AppMenu::getMenuCode,
                m -> m.getParentMenuCode() != null ? m.getParentMenuCode() : "",
                java.util.Comparator.comparing(AppMenu::getDisplayOrder,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        toolbar.add(StandardGridUtils.createExportExcelButton(menuTreeGrid, "menus_export", getterMap));

        l.add(toolbar, hint, menuTreeGrid);
        l.setFlexGrow(1, menuTreeGrid);
        return l;
    }

    private record AvailableMenuItem(String code, String title, String route, String icon, String type,
            String defaultParent) {
        @Override
        public String toString() {
            return "[" + type + "] " + code + " — " + title;
        }
    }

    private void openMenuDialog(AppMenu existing, String defaultParentCode) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((existing == null) ? "Add New Menu"
                : "Edit Menu: " + existing.getMenuCode()
                        + (existing.getMenuTitle() != null ? " - " + existing.getMenuTitle() : ""));
        dialog.setWidth("540px");

        FormLayout form = new FormLayout();

        TextField codeField = new TextField("Menu Code (unique)");
        codeField.setWidthFull();
        codeField.setRequired(true);

        TextField titleField = new TextField("Menu Title");
        titleField.setWidthFull();
        titleField.setRequired(true);

        ComboBox<String> typeSelect = new ComboBox<>("Menu Type");
        typeSelect.setWidthFull();
        typeSelect.setItems("GROUP", "ITEM", "FORM");
        typeSelect.setRequired(true);

        ComboBox<String> parentSelect = new ComboBox<>("Parent Menu (Group)");
        parentSelect.setWidthFull();
        parentSelect.setClearButtonVisible(true);
        List<AppMenu> groups = menuRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(m -> "GROUP".equalsIgnoreCase(m.getMenuType()))
                .toList();
        parentSelect.setItems(groups.stream().map(g -> g.getMenuCode() + " — " + g.getMenuTitle()).toList());
        parentSelect.setItemLabelGenerator(s -> s);

        ComboBox<String> iconField = new ComboBox<>("Icon Name (VaadinIcon)");
        iconField.setWidthFull();
        iconField.setItems(java.util.Arrays.stream(com.vaadin.flow.component.icon.VaadinIcon.values())
                .map(Enum::name)
                .toList());
        iconField.setAllowCustomValue(true);
        iconField.addCustomValueSetListener(e -> {
            iconField.setValue(e.getDetail().toUpperCase());
        });
        iconField.setRenderer(com.vaadin.flow.data.renderer.LitRenderer.<String>of(
                "<div style='display: flex; align-items: center; gap: 8px; padding: 4px 0;'>"
                        + "  <vaadin-icon icon='vaadin:${item.iconCode}' style='color: #3b82f6; font-size: 18px; flex-shrink: 0;'></vaadin-icon>"
                        + "  <div style='font-size: 14px; font-weight: 500;'>${item.name}</div>"
                        + "</div>")
                .withProperty("iconCode", name -> name != null ? name.toLowerCase().replace("_", "-") : "")
                .withProperty("name", name -> name));
        iconField.setClearButtonVisible(true);

        TextField routeField = new TextField("Route Path");
        routeField.setWidthFull();
        routeField.setVisible(false);

        IntegerField orderField = new IntegerField("Display Order");
        orderField.setWidthFull();
        orderField.setValue(10);
        orderField.setStepButtonsVisible(true);

        if (existing != null) {
            codeField.setValue(existing.getMenuCode());
            codeField.setReadOnly(true);
            titleField.setValue(existing.getMenuTitle() != null ? existing.getMenuTitle() : "");
            typeSelect.setValue(existing.getMenuType() != null ? existing.getMenuType() : "ITEM");
            iconField.setValue(existing.getIconName() != null ? existing.getIconName() : "");
            routeField.setValue(existing.getRoutePath() != null ? existing.getRoutePath() : "");
            orderField.setValue(existing.getDisplayOrder() != null ? existing.getDisplayOrder() : 10);

            if (existing.getParentMenuCode() != null) {
                groups.stream()
                        .filter(g -> g.getMenuCode().equals(existing.getParentMenuCode()))
                        .findFirst()
                        .ifPresent(g -> parentSelect.setValue(g.getMenuCode() + " — " + g.getMenuTitle()));
            }
        } else {
            typeSelect.setValue(defaultParentCode != null ? "ITEM" : "GROUP");
            if (defaultParentCode != null) {
                groups.stream()
                        .filter(g -> g.getMenuCode().equals(defaultParentCode))
                        .findFirst()
                        .ifPresent(g -> parentSelect.setValue(g.getMenuCode() + " — " + g.getMenuTitle()));
            }
        }

        ComboBox<AvailableMenuItem> autoFillSelect = new ComboBox<>(
                "⚡ Select from Available Forms / Modules (Auto-Fill)");
        autoFillSelect.setWidthFull();
        autoFillSelect.setPlaceholder("-- Select Module / Form for Auto-Fill --");
        autoFillSelect.setClearButtonVisible(true);

        List<AvailableMenuItem> availableList = new ArrayList<>();
        if (formMetaRepository != null) {
            for (FormMeta f : formMetaRepository.findAll()) {
                String code = f.getFormCode();
                if (code == null || "MD_SEQUENCE".equalsIgnoreCase(code))
                    continue;
                boolean isSubform = "SUBFORM".equalsIgnoreCase(f.getFormType())
                        || code.toUpperCase().endsWith("_DTL")
                        || code.toUpperCase().endsWith("_DETAIL")
                        || (f.getFormTitle() != null && f.getFormTitle().toLowerCase().startsWith("detail "));
                if (isSubform)
                    continue;
                availableList.add(new AvailableMenuItem(code, f.getFormTitle() != null ? f.getFormTitle() : code, code,
                        "FILE_TEXT_O", "Form", "GRP_FORMS"));
            }
        }
        if (reportMetaRepository != null) {
            for (ReportMeta r : reportMetaRepository.findAll()) {
                String code = r.getReportCode();
                if (code == null)
                    continue;
                availableList.add(new AvailableMenuItem(code, r.getReportTitle() != null ? r.getReportTitle() : code,
                        "report-viewer/" + code, "PRINT", "Report", "GRP_REPORTS"));
            }
        }
        availableList.add(new AvailableMenuItem("REPORT_BUILDER", "Report Designer", "REPORT_BUILDER", "EDIT", "System",
                "GRP_REPORTS"));
        availableList.add(new AvailableMenuItem("REPORT_VIEWER", "Report Viewer", "REPORT_VIEWER", "PRINT", "System",
                "GRP_REPORTS"));
        availableList.add(new AvailableMenuItem("LOV_BUILDER", "LOV Metadata Builder", "LOV_BUILDER", "LIST", "System",
                "SYS_FORM"));
        availableList.add(new AvailableMenuItem("STANDARD_FORMAT", "Konfigurasi Format Standar", "STANDARD_FORMAT",
                "SLIDERS", "System", "SYS_FORM"));
        availableList.add(new AvailableMenuItem("FORM_ACTION_BUILDER", "Extra Toolbar Builder", "action-builder",
                "BOLT", "System", "SYS_FORM"));
        availableList.add(new AvailableMenuItem("MD_SEQUENCE", "Master Penomoran Dokumen", "MD_SEQUENCE", "BARCODE",
                "System", "SYS_FORM"));
        availableList.add(new AvailableMenuItem("FIELD_AUDIT_LOG", "Field Audit Log Viewer", "FIELD_AUDIT_LOG", "CLOCK",
                "System", "GRP_SYSTEM"));

        autoFillSelect.setItems(availableList);
        autoFillSelect.setItemLabelGenerator(AvailableMenuItem::toString);

        autoFillSelect.addValueChangeListener(e -> {
            if (!e.isFromClient())
                return;
            AvailableMenuItem item = e.getValue();
            if (item != null) {
                codeField.setReadOnly(false);
                codeField.setValue(item.code());
                if (existing != null)
                    codeField.setReadOnly(true);
                titleField.setValue(item.title());
                typeSelect.setValue("ITEM");
                iconField.setValue(item.icon());
                routeField.setValue(item.route());
            }
        });

        form.add(autoFillSelect);

        if (existing != null) {
            availableList.stream()
                    .filter(a -> a.code().equalsIgnoreCase(existing.getMenuCode()))
                    .findFirst()
                    .ifPresent(autoFillSelect::setValue);
        }

        form.add(codeField, titleField, typeSelect, parentSelect, iconField, routeField, orderField);

        Button saveBtn = new com.vaadinerp.components.SafeButton("Save", VaadinIcon.CHECK.create(), e -> {
            if (codeField.getValue() == null || codeField.getValue().isBlank() || titleField.getValue() == null
                    || titleField.getValue().isBlank()) {
                Notification.show("Menu Code and Title are required!", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            String newCode = codeField.getValue().trim().toUpperCase().replace(" ", "_");
            String oldCode = (existing != null) ? existing.getMenuCode() : null;
            boolean codeChanged = (existing != null && !newCode.equals(oldCode));

            AppMenu menu = (existing == null || codeChanged) ? new AppMenu() : existing;
            menu.setMenuCode(newCode);
            menu.setMenuTitle(titleField.getValue());
            menu.setMenuType(typeSelect.getValue() != null ? typeSelect.getValue() : "ITEM");
            menu.setIconName(iconField.getValue());
            menu.setRoutePath(routeField.getValue());
            menu.setDisplayOrder(orderField.getValue() != null ? orderField.getValue() : 10);

            String parentDisplay = parentSelect.getValue();
            if (parentDisplay != null && parentDisplay.contains(" — ")) {
                menu.setParentMenuCode(parentDisplay.split(" — ")[0].trim());
            } else {
                menu.setParentMenuCode(null);
            }

            menuRepository.save(menu);
            log.info("[AUDIT] User '{}' {} menu '{}'",
                    securityService.getCurrentUser() != null ? securityService.getCurrentUser().getUsername()
                            : "SYSTEM",
                    (existing == null ? "created" : "updated"), menu.getMenuCode());

            if (codeChanged) {
                // Migrate children to new parent code
                if (existing != null && "GROUP".equalsIgnoreCase(existing.getMenuType())) {
                    List<AppMenu> children = menuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(oldCode);
                    for (AppMenu child : children) {
                        child.setParentMenuCode(newCode);
                        menuRepository.save(child);
                    }
                }

                // Migrate permissions
                List<RoleMenuPermission> oldPerms = permissionRepository.findByMenuCode(oldCode);
                for (RoleMenuPermission p : oldPerms) {
                    RoleMenuPermission newP = new RoleMenuPermission();
                    newP.setRoleCode(p.getRoleCode());
                    newP.setMenuCode(newCode);
                    newP.setCanView(p.getCanView());
                    newP.setCanAdd(p.getCanAdd());
                    newP.setCanEdit(p.getCanEdit());
                    newP.setCanDelete(p.getCanDelete());
                    newP.setCanPrint(p.getCanPrint());
                    permissionRepository.save(newP);
                }

                // Delete the old menu (this will cleanly remove orphaned permissions/favorites)
                deleteMenuAndDependencies(existing);
            }
            dialog.close();
            refreshMenuTreeGrid();
            Notification.show("Menu saved successfully!", 2000, Notification.Position.BOTTOM_END);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new com.vaadinerp.components.SafeButton("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private void openMenuDialogItem(AppMenu existing, String parentCode) {
        openMenuDialog(existing, parentCode);
    }

    private void deleteMenuAndDependencies(AppMenu menu) {
        if (menu == null || menu.getMenuCode() == null)
            return;
        if ("GROUP".equalsIgnoreCase(menu.getMenuType())) {
            List<AppMenu> children = menuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(menu.getMenuCode());
            for (AppMenu child : children) {
                deleteMenuAndDependencies(child);
            }
        }
        permissionRepository.deleteByMenuCode(menu.getMenuCode());
        if (favoriteMenuRepository != null) {
            favoriteMenuRepository.deleteByMenuCode(menu.getMenuCode());
        }
        menuRepository.delete(menu);
    }

    // ============================
    // TAB 4: MATRIKS OTORITAS AKSES
    // ============================

    private VerticalLayout setupMatrixTab() {
        VerticalLayout l = new VerticalLayout();
        l.setSizeFull();
        l.setPadding(false);

        StandardActionToolbar toolbar = new StandardActionToolbar()
                .onRefresh(this::refreshMatrixGrid);

        Button btnCopy = new com.vaadinerp.components.SafeButton("Copy Akses", VaadinIcon.COPY.create(),
                e -> openCopyAccessDialog());
        btnCopy.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        toolbar.add(btnCopy);

        var colTitle = matrixTreeGrid.addHierarchyColumn(m -> m.getMenuTitle() != null ? m.getMenuTitle() : "")
                .setHeader("Menu Title").setSortable(true).setAutoWidth(true);
        var colCode = matrixTreeGrid.addColumn(AppMenu::getMenuCode).setHeader("Menu Code").setSortable(true)
                .setAutoWidth(true);

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setDefaultVerticalComponentAlignment(
                com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE);

        ComboBox<String> roleSelect = new ComboBox<>("Select Role to Edit");
        roleSelect.setItems(roleRepository.findAll().stream().map(AppRole::getRoleCode).toList());
        roleSelect.setWidth("300px");
        roleSelect.setClearButtonVisible(true);

        Button btnCheckAll = new com.vaadinerp.components.SafeButton("Grant All Access (Blank)",
                VaadinIcon.CHECK_SQUARE_O.create(), e -> {
                    if (roleSelect.getValue() == null)
                        return;
                    String role = roleSelect.getValue();
                    List<AppMenu> menus = menuRepository.findAll();
                    for (AppMenu m : menus) {
                        if ("GROUP".equalsIgnoreCase(m.getMenuType())) {
                            continue; // Skip folders
                        }
                        RoleMenuPermission perm = permissionRepository.findByRoleCodeAndMenuCode(role, m.getMenuCode())
                                .orElse(new RoleMenuPermission());
                        perm.setRoleCode(role);
                        perm.setMenuCode(m.getMenuCode());
                        if (perm.getId() == null) {
                            perm.setCanView(false);
                            perm.setCanAdd(false);
                            perm.setCanEdit(false);
                            perm.setCanDelete(false);
                            perm.setCanPrint(false);
                        }
                        permissionRepository.save(perm);
                    }
                    refreshMatrixGrid(role);
                    Notification.show("All menus have been granted screen access rights", 2000,
                            Notification.Position.BOTTOM_END);
                });
        btnCheckAll.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button btnUncheckAll = new com.vaadinerp.components.SafeButton("Uncheck All",
                VaadinIcon.CLOSE_CIRCLE_O.create(), e -> {
                    if (roleSelect.getValue() == null)
                        return;
                    String role = roleSelect.getValue();
                    List<RoleMenuPermission> existing = permissionRepository.findByRoleCode(role);
                    permissionRepository.deleteAll(existing);
                    refreshMatrixGrid();
                    Notification.show("All access rights have been revoked", 2000, Notification.Position.BOTTOM_END);
                });
        btnUncheckAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        HorizontalLayout actions = new HorizontalLayout(btnCheckAll, btnUncheckAll, btnCopy);
        actions.setVisible(false);

        Button btnRefreshRole = new com.vaadinerp.components.SafeButton(VaadinIcon.REFRESH.create(), e -> {
            String current = roleSelect.getValue();
            roleSelect.setItems(roleRepository.findAll().stream().map(AppRole::getRoleCode).toList());
            if (current != null) {
                roleSelect.setValue(current);
            }
            Notification.show("Role list refreshed", 1500, Notification.Position.BOTTOM_END);
        });
        btnRefreshRole.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnRefreshRole.setTooltipText("Refresh Role List");

        topBar.add(roleSelect, btnRefreshRole, actions);

        matrixTreeGrid.addComponentColumn(m -> createMasterAccessCheckbox(m, roleSelect)).setHeader("Access Screen");
        matrixTreeGrid.addComponentColumn(m -> createPermissionCheckbox(m, roleSelect, "VIEW")).setHeader("View");
        matrixTreeGrid.addComponentColumn(m -> createPermissionCheckbox(m, roleSelect, "ADD")).setHeader("Add");
        matrixTreeGrid.addComponentColumn(m -> createPermissionCheckbox(m, roleSelect, "EDIT")).setHeader("Edit");
        matrixTreeGrid.addComponentColumn(m -> createPermissionCheckbox(m, roleSelect, "DELETE")).setHeader("Delete");
        matrixTreeGrid.addComponentColumn(m -> createPermissionCheckbox(m, roleSelect, "PRINT")).setHeader("Print");

        matrixTreeGrid.setHeight("100%");
        matrixTreeGrid.getStyle().set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
        matrixTreeGrid.setVisible(false);

        roleSelect.addValueChangeListener(e -> {
            boolean hasRole = e.getValue() != null;
            matrixTreeGrid.setVisible(hasRole);
            actions.setVisible(hasRole);
            if (hasRole) {
                refreshMatrixGrid(e.getValue());
            } else {
                currentRolePermissions.clear();
            }
        });

        Paragraph hint = new Paragraph(
                "💡 Please select a Role first. Check the box to instantly grant access.");
        hint.getStyle().set("font-size", "0.8rem").set("color", "#64748b").set("margin", "0");

        Map<Grid.Column<AppMenu>, Function<AppMenu, String>> getterMap = Map.of(
                colTitle, m -> m.getMenuTitle() != null ? m.getMenuTitle() : "",
                colCode, m -> m.getMenuCode() != null ? m.getMenuCode() : "");
        matrixFilterRefresher = StandardGridUtils.attachTreeGridFilters(
                matrixTreeGrid,
                getterMap,
                menuRepository::findAllByOrderByDisplayOrderAsc,
                AppMenu::getMenuCode,
                m -> m.getParentMenuCode() != null ? m.getParentMenuCode() : "",
                java.util.Comparator.comparing(AppMenu::getDisplayOrder,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        toolbar.add(StandardGridUtils.createExportExcelButton(matrixTreeGrid, "matrix_export", getterMap));

        l.add(topBar, hint, matrixTreeGrid);
        l.setFlexGrow(1, matrixTreeGrid);
        return l;
    }

    private Checkbox createMasterAccessCheckbox(AppMenu m, ComboBox<String> roleSelect) {
        if ("GROUP".equalsIgnoreCase(m.getMenuType())) {
            Checkbox cb = new Checkbox();
            cb.setVisible(false);
            return cb;
        }

        String role = roleSelect.getValue();
        if (role == null) {
            Checkbox cb = new Checkbox();
            cb.setEnabled(false);
            return cb;
        }

        RoleMenuPermission perm = currentRolePermissions.get(m.getMenuCode());
        Checkbox cb = new Checkbox();
        cb.setValue(perm != null);

        cb.addValueChangeListener(ev -> {
            if (ev.isFromClient()) {
                if (ev.getValue()) {
                    // Beri Akses (Insert Record)
                    RoleMenuPermission newPerm = permissionRepository.findByRoleCodeAndMenuCode(role, m.getMenuCode())
                            .orElse(new RoleMenuPermission());
                    newPerm.setRoleCode(role);
                    newPerm.setMenuCode(m.getMenuCode());
                    newPerm.setCanView(false);
                    newPerm.setCanAdd(false);
                    newPerm.setCanEdit(false);
                    newPerm.setCanDelete(false);
                    newPerm.setCanPrint(false);
                    newPerm = permissionRepository.save(newPerm);
                    currentRolePermissions.put(m.getMenuCode(), newPerm);
                } else {
                    // Cabut Akses (Delete Record)
                    permissionRepository.findByRoleCodeAndMenuCode(role, m.getMenuCode()).ifPresent(p -> {
                        permissionRepository.delete(p);
                    });
                    currentRolePermissions.remove(m.getMenuCode());
                }
                matrixTreeGrid.getDataProvider().refreshItem(m); // Refresh to update other checkboxes
            }
        });
        return cb;
    }

    private Checkbox createPermissionCheckbox(AppMenu m, ComboBox<String> roleSelect, String actionType) {
        if ("GROUP".equalsIgnoreCase(m.getMenuType())) {
            Checkbox cb = new Checkbox();
            cb.setVisible(false);
            return cb;
        }

        String role = roleSelect.getValue();
        if (role == null) {
            Checkbox cb = new Checkbox();
            cb.setEnabled(false);
            return cb;
        }

        RoleMenuPermission perm = currentRolePermissions.get(m.getMenuCode());
        Checkbox cb = new Checkbox();

        if (perm != null) {
            cb.setEnabled(true);
            switch (actionType) {
                case "VIEW" -> cb.setValue(Boolean.TRUE.equals(perm.getCanView()));
                case "ADD" -> cb.setValue(Boolean.TRUE.equals(perm.getCanAdd()));
                case "EDIT" -> cb.setValue(Boolean.TRUE.equals(perm.getCanEdit()));
                case "DELETE" -> cb.setValue(Boolean.TRUE.equals(perm.getCanDelete()));
                case "PRINT" -> cb.setValue(Boolean.TRUE.equals(perm.getCanPrint()));
            }
        } else {
            cb.setValue(false);
            cb.setEnabled(false);
        }

        cb.addValueChangeListener(ev -> {
            if (ev.isFromClient()) {
                RoleMenuPermission latest = permissionRepository.findByRoleCodeAndMenuCode(role, m.getMenuCode())
                        .orElse(null);
                if (latest != null) {
                    switch (actionType) {
                        case "VIEW" -> latest.setCanView(ev.getValue());
                        case "ADD" -> latest.setCanAdd(ev.getValue());
                        case "EDIT" -> latest.setCanEdit(ev.getValue());
                        case "DELETE" -> latest.setCanDelete(ev.getValue());
                        case "PRINT" -> latest.setCanPrint(ev.getValue());
                    }
                    latest = permissionRepository.save(latest);
                    currentRolePermissions.put(m.getMenuCode(), latest);
                }
            }
        });

        return cb;
    }

    private void openCopyAccessDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Copy Access Rights Between Roles");
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();

        List<String> roles = roleRepository.findAll().stream().map(AppRole::getRoleCode).toList();

        ComboBox<String> sourceRoleSelect = new ComboBox<>("Source Role (From)");
        sourceRoleSelect.setItems(roles);
        sourceRoleSelect.setRequired(true);
        sourceRoleSelect.setWidthFull();

        ComboBox<String> targetRoleSelect = new ComboBox<>("Role Tujuan (Ke)");
        targetRoleSelect.setItems(roles);
        targetRoleSelect.setRequired(true);
        targetRoleSelect.setWidthFull();

        form.add(sourceRoleSelect, targetRoleSelect);

        Button saveBtn = new com.vaadinerp.components.SafeButton("Copy Akses", VaadinIcon.COPY.create(), e -> {
            String source = sourceRoleSelect.getValue();
            String target = targetRoleSelect.getValue();

            if (source == null || target == null) {
                Notification.show("Source and Target Role must be selected!", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (source.equals(target)) {
                Notification.show("Source and Target Role cannot be the same!", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            List<RoleMenuPermission> sourcePerms = permissionRepository.findByRoleCode(source);
            List<RoleMenuPermission> targetPerms = permissionRepository.findByRoleCode(target);

            List<String> targetMenuCodes = targetPerms.stream().map(RoleMenuPermission::getMenuCode).toList();

            int copiedCount = 0;
            for (RoleMenuPermission sPerm : sourcePerms) {
                if (!targetMenuCodes.contains(sPerm.getMenuCode())) {
                    RoleMenuPermission newPerm = new RoleMenuPermission();
                    newPerm.setRoleCode(target);
                    newPerm.setMenuCode(sPerm.getMenuCode());
                    newPerm.setCanView(sPerm.getCanView());
                    newPerm.setCanAdd(sPerm.getCanAdd());
                    newPerm.setCanEdit(sPerm.getCanEdit());
                    newPerm.setCanDelete(sPerm.getCanDelete());
                    newPerm.setCanPrint(sPerm.getCanPrint());
                    permissionRepository.save(newPerm);
                    copiedCount++;
                }
            }

            dialog.close();
            refreshMatrixGrid();
            Notification.show(copiedCount + " menus successfully copied from " + source + " to " + target, 3000,
                    Notification.Position.BOTTOM_END);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new com.vaadinerp.components.SafeButton("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);

        Paragraph hint = new Paragraph("Note: Menus already existing in the Target Role will not be overwritten.");
        hint.getStyle().set("font-size", "0.8rem").set("color", "#64748b").set("margin-top", "10px");
        dialog.add(hint);

        dialog.open();
    }

    // ============================
    // DATA REFRESH
    // ============================

    private void refreshUserGrid() {
        if (userFilterRefresher != null)
            userFilterRefresher.run();
    }

    private void refreshRoleGrid() {
        if (roleFilterRefresher != null)
            roleFilterRefresher.run();
    }

    private void refreshMenuTreeGrid() {
        if (menuFilterRefresher != null)
            menuFilterRefresher.run();
    }

    private final Map<String, String> menuTitleCache = new java.util.concurrent.ConcurrentHashMap<>();

    private String getMenuTitleByCode(String menuCode) {
        if (menuCode == null)
            return "";
        return menuTitleCache.computeIfAbsent(menuCode,
                code -> menuRepository.findById(code).map(AppMenu::getMenuTitle).orElse(""));
    }

    private void refreshMatrixGrid(String roleCode) {
        menuTitleCache.clear();
        currentRolePermissions.clear();
        if (roleCode != null) {
            permissionRepository.findByRoleCode(roleCode)
                    .forEach(p -> currentRolePermissions.put(p.getMenuCode(), p));
        }
        if (matrixFilterRefresher != null)
            matrixFilterRefresher.run();
    }

    private void refreshMatrixGrid() {
        if (matrixFilterRefresher != null)
            matrixFilterRefresher.run();
    }
}
