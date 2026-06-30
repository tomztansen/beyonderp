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
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
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
import com.vaadinerp.security.repository.RoleMenuPermissionRepository;
import com.vaadinerp.security.service.SessionSecurityService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Panel Administrasi Keamanan Terpadu.
 * Dibuka sebagai Tab di dalam PortalView.
 * Fitur:
 *  - Menggunakan StandardActionToolbar (Tambah, Refresh) di atas setiap grid
 *  - Menggunakan StandardGridUtils.attachGridFilters untuk pencarian/filter di header row
 */
public class UserAuthorityAdminView extends VerticalLayout {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final AppMenuRepository menuRepository;
    private final RoleMenuPermissionRepository permissionRepository;
    private final SessionSecurityService securityService;

    private final Grid<AppUser> userGrid = new Grid<>(AppUser.class, false);
    private final Grid<AppRole> roleGrid = new Grid<>(AppRole.class, false);
    private final Grid<RoleMenuPermission> matrixGrid = new Grid<>(RoleMenuPermission.class, false);
    private final TreeGrid<AppMenu> menuTreeGrid = new TreeGrid<>();

    private Runnable userFilterRefresher;
    private Runnable roleFilterRefresher;
    private Runnable matrixFilterRefresher;

    public UserAuthorityAdminView(AppUserRepository userRepository, AppRoleRepository roleRepository,
                                  AppMenuRepository menuRepository,
                                  RoleMenuPermissionRepository permissionRepository,
                                  SessionSecurityService securityService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Paragraph desc = new Paragraph("Kelola pengguna, role, struktur menu tree, dan konfigurasi hak akses granular (RBAC per aksi) pada skema public PostgreSQL.");
        desc.getStyle().set("color", "#64748b").set("margin-top", "0");

        Tab tabUsers = new Tab("Manajemen Pengguna");
        Tab tabRoles = new Tab("Manajemen Role");
        Tab tabMenus = new Tab("Manajemen Menu");
        Tab tabMatrix = new Tab("Matriks Otoritas Akses");
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
            if (tabs.getSelectedTab() == tabUsers) refreshUserGrid();
            if (tabs.getSelectedTab() == tabRoles) refreshRoleGrid();
            if (tabs.getSelectedTab() == tabMenus) refreshMenuTreeGrid();
            if (tabs.getSelectedTab() == tabMatrix) refreshMatrixGrid();
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

        var colUser = userGrid.addColumn(AppUser::getUsername).setHeader("Username").setSortable(true).setAutoWidth(true);
        var colName = userGrid.addColumn(AppUser::getFullName).setHeader("Nama Lengkap").setSortable(true).setAutoWidth(true);
        var colRole = userGrid.addColumn(AppUser::getRoleCode).setHeader("Role").setSortable(true).setAutoWidth(true);
        var colStatus = userGrid.addColumn(u -> Boolean.TRUE.equals(u.getIsActive()) ? "✅ Aktif" : "❌ Non-Aktif").setHeader("Status").setAutoWidth(true);
        
        userGrid.addComponentColumn(u -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openUserDialog(u));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.getElement().setAttribute("title", "Edit User");
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                userRepository.delete(u);
                Notification.show("User '" + u.getUsername() + "' dihapus!", 2000, Notification.Position.BOTTOM_END);
                refreshUserGrid();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.getElement().setAttribute("title", "Hapus User");
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Aksi").setAutoWidth(true);

        userGrid.setHeight("100%");
        userGrid.getStyle().set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        Map<Grid.Column<AppUser>, Function<AppUser, String>> getterMap = Map.of(
                colUser, u -> u.getUsername() != null ? u.getUsername() : "",
                colName, u -> u.getFullName() != null ? u.getFullName() : "",
                colRole, u -> u.getRoleCode() != null ? u.getRoleCode() : "",
                colStatus, u -> Boolean.TRUE.equals(u.getIsActive()) ? "Aktif" : "Non-Aktif"
        );
        userFilterRefresher = StandardGridUtils.attachGridFilters(userGrid, getterMap, userRepository::findAll);

        l.add(toolbar, userGrid);
        l.setFlexGrow(1, userGrid);
        return l;
    }

    private void openUserDialog(AppUser existing) {
        boolean isNew = existing == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((existing == null) ? "Tambah User Baru" : "Edit User: " + existing.getUsername());
        dialog.setWidth("460px");

        FormLayout form = new FormLayout();

        TextField usernameField = new TextField("Username");
        usernameField.setWidthFull();
        usernameField.setRequired(true);

        TextField fullNameField = new TextField("Nama Lengkap");
        fullNameField.setWidthFull();

        PasswordField passwordField = new PasswordField(isNew ? "Password" : "Password Baru (kosongkan jika tidak diubah)");
        passwordField.setWidthFull();
        if (isNew) passwordField.setRequired(true);

        ComboBox<String> roleSelect = new ComboBox<>("Role");
        roleSelect.setWidthFull();
        roleSelect.setItems(roleRepository.findAll().stream().map(AppRole::getRoleCode).toList());
        roleSelect.setRequired(true);

        Checkbox activeCheckbox = new Checkbox("Aktif");
        activeCheckbox.setValue(true);

        if (existing != null) {
            usernameField.setValue(existing.getUsername());
            usernameField.setReadOnly(true);
            fullNameField.setValue(existing.getFullName() != null ? existing.getFullName() : "");
            roleSelect.setValue(existing.getRoleCode());
            activeCheckbox.setValue(Boolean.TRUE.equals(existing.getIsActive()));
        }

        form.add(usernameField, fullNameField, passwordField, roleSelect, activeCheckbox);

        Button saveBtn = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            String username = usernameField.getValue();
            String role = roleSelect.getValue();
            if (username == null || username.isBlank()) {
                Notification n = Notification.show("Username wajib diisi!", 3000, Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (role == null || role.isBlank()) {
                Notification n = Notification.show("Role wajib dipilih!", 3000, Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (isNew && (passwordField.getValue() == null || passwordField.getValue().isBlank())) {
                Notification n = Notification.show("Password wajib diisi untuk user baru!", 3000, Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            AppUser user = (existing == null) ? new AppUser() : existing;
            if (existing == null) user.setUsername(username.trim());
            user.setFullName(fullNameField.getValue());
            user.setRoleCode(role);
            user.setIsActive(activeCheckbox.getValue());

            String pwd = passwordField.getValue();
            if (pwd != null && !pwd.isBlank()) {
                user.setPasswordHash(securityService.encodePassword(pwd));
            }

            userRepository.save(user);
            dialog.close();
            refreshUserGrid();
            Notification.show("User '" + username + "' berhasil " + (isNew ? "dibuat" : "diperbarui") + "!", 2000, Notification.Position.BOTTOM_END);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Batal", e -> dialog.close());

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

        var colCode = roleGrid.addColumn(AppRole::getRoleCode).setHeader("Kode Role").setSortable(true).setAutoWidth(true);
        var colName = roleGrid.addColumn(AppRole::getRoleName).setHeader("Nama Role").setSortable(true).setAutoWidth(true);
        var colDesc = roleGrid.addColumn(AppRole::getDescription).setHeader("Deskripsi").setAutoWidth(true);
        
        roleGrid.addComponentColumn(r -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openRoleDialog(r));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                roleRepository.delete(r);
                refreshRoleGrid();
                Notification.show("Role '" + r.getRoleCode() + "' dihapus!", 2000, Notification.Position.BOTTOM_END);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Aksi").setAutoWidth(true);

        roleGrid.setHeight("100%");
        roleGrid.getStyle().set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        Map<Grid.Column<AppRole>, Function<AppRole, String>> getterMap = Map.of(
                colCode, r -> r.getRoleCode() != null ? r.getRoleCode() : "",
                colName, r -> r.getRoleName() != null ? r.getRoleName() : "",
                colDesc, r -> r.getDescription() != null ? r.getDescription() : ""
        );
        roleFilterRefresher = StandardGridUtils.attachGridFilters(roleGrid, getterMap, roleRepository::findAll);

        l.add(toolbar, roleGrid);
        l.setFlexGrow(1, roleGrid);
        return l;
    }

    private void openRoleDialog(AppRole existing) {
        boolean isNew = existing == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((existing == null) ? "Tambah Role Baru" : "Edit Role: " + existing.getRoleCode());
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();

        TextField codeField = new TextField("Kode Role");
        codeField.setWidthFull();
        codeField.setRequired(true);

        TextField nameField = new TextField("Nama Role");
        nameField.setWidthFull();

        TextField descField = new TextField("Deskripsi");
        descField.setWidthFull();

        if (existing != null) {
            codeField.setValue(existing.getRoleCode());
            codeField.setReadOnly(true);
            nameField.setValue(existing.getRoleName() != null ? existing.getRoleName() : "");
            descField.setValue(existing.getDescription() != null ? existing.getDescription() : "");
        }

        form.add(codeField, nameField, descField);

        Button saveBtn = new Button("Simpan", e -> {
            if (codeField.getValue() == null || codeField.getValue().isBlank()) {
                Notification.show("Kode Role wajib diisi!", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            AppRole role = (existing == null) ? new AppRole() : existing;
            if (existing == null) role.setRoleCode(codeField.getValue().trim().toUpperCase());
            role.setRoleName(nameField.getValue());
            role.setDescription(descField.getValue());
            roleRepository.save(role);
            dialog.close();
            refreshRoleGrid();
            Notification.show("Role berhasil " + (isNew ? "dibuat" : "diperbarui") + "!", 2000, Notification.Position.BOTTOM_END);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Batal", e -> dialog.close());

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
        toolbar.getBtnNew().setText("Tambah Group Menu");

        Button addItemBtn = new Button("Tambah Menu Item (Root)", VaadinIcon.PLUS.create(), e -> openMenuDialogItem(null, null));
        StandardActionToolbar.styleToolbarButton(addItemBtn, "#3b82f6");
        toolbar.add(addItemBtn);

        menuTreeGrid.addHierarchyColumn(AppMenu::getMenuTitle)
                .setHeader("Judul Menu")
                .setAutoWidth(true)
                .setFlexGrow(1);

        menuTreeGrid.addColumn(AppMenu::getMenuCode)
                .setHeader("Kode Menu")
                .setSortable(true)
                .setAutoWidth(true);

        menuTreeGrid.addColumn(m -> {
            if ("GROUP".equalsIgnoreCase(m.getMenuType())) return "📂 Folder (Group)";
            return "📄 Item (Klik)";
        }).setHeader("Tipe").setAutoWidth(true);

        menuTreeGrid.addColumn(AppMenu::getIconName)
                .setHeader("Icon")
                .setAutoWidth(true);

        menuTreeGrid.addColumn(m -> m.getRoutePath() != null ? m.getRoutePath() : "-")
                .setHeader("Route Path")
                .setAutoWidth(true);

        menuTreeGrid.addColumn(m -> m.getDisplayOrder() != null ? m.getDisplayOrder() : 10)
                .setHeader("Urutan")
                .setAutoWidth(true);

        menuTreeGrid.addComponentColumn(m -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if ("GROUP".equalsIgnoreCase(m.getMenuType())) {
                Button addChildBtn = new Button(VaadinIcon.PLUS_CIRCLE_O.create(), e -> openMenuDialog(null, m.getMenuCode()));
                addChildBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                addChildBtn.getElement().setAttribute("title", "Tambah Child Menu");
                addChildBtn.getStyle().set("color", "#22c55e");
                actions.add(addChildBtn);
            }

            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openMenuDialog(m, m.getParentMenuCode()));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.getElement().setAttribute("title", "Edit Menu");

            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                if ("GROUP".equalsIgnoreCase(m.getMenuType())) {
                    List<AppMenu> children = menuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(m.getMenuCode());
                    if (!children.isEmpty()) {
                        menuRepository.deleteAll(children);
                    }
                }
                menuRepository.delete(m);
                refreshMenuTreeGrid();
                Notification.show("Menu '" + m.getMenuCode() + "' dihapus!", 2000, Notification.Position.BOTTOM_END);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.getElement().setAttribute("title", "Hapus Menu");

            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Aksi").setAutoWidth(true);

        menuTreeGrid.setHeight("100%");
        menuTreeGrid.getStyle().set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        Paragraph hint = new Paragraph("💡 Struktur menu hierarkis: Buat GROUP terlebih dahulu, lalu tambahkan ITEM di bawahnya. Gunakan tombol ⊕ pada baris GROUP untuk menambah child.");
        hint.getStyle().set("font-size", "0.8rem").set("color", "#64748b").set("margin", "0");

        l.add(toolbar, hint, menuTreeGrid);
        l.setFlexGrow(1, menuTreeGrid);
        return l;
    }

    private void openMenuDialog(AppMenu existing, String defaultParentCode) {
        boolean isNew = existing == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((existing == null) ? "Tambah Menu Baru" : "Edit Menu: " + existing.getMenuCode());
        dialog.setWidth("520px");

        FormLayout form = new FormLayout();

        TextField codeField = new TextField("Kode Menu (unik)");
        codeField.setWidthFull();
        codeField.setRequired(true);

        TextField titleField = new TextField("Judul Menu");
        titleField.setWidthFull();
        titleField.setRequired(true);

        ComboBox<String> typeSelect = new ComboBox<>("Tipe Menu");
        typeSelect.setWidthFull();
        typeSelect.setItems("GROUP", "ITEM");
        typeSelect.setRequired(true);

        ComboBox<String> parentSelect = new ComboBox<>("Parent Menu (Group)");
        parentSelect.setWidthFull();
        parentSelect.setClearButtonVisible(true);
        List<AppMenu> groups = menuRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(m -> "GROUP".equalsIgnoreCase(m.getMenuType()))
                .toList();
        parentSelect.setItems(groups.stream().map(g -> g.getMenuCode() + " — " + g.getMenuTitle()).toList());
        parentSelect.setItemLabelGenerator(s -> s);

        TextField iconField = new TextField("Nama Icon (VaadinIcon)");
        iconField.setWidthFull();

        TextField routeField = new TextField("Route Path");
        routeField.setWidthFull();

        IntegerField orderField = new IntegerField("Urutan Tampil");
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

        form.add(codeField, titleField, typeSelect, parentSelect, iconField, routeField, orderField);

        Button saveBtn = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            if (codeField.getValue() == null || codeField.getValue().isBlank() || titleField.getValue() == null || titleField.getValue().isBlank()) {
                Notification.show("Kode dan Judul Menu wajib diisi!", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            AppMenu menu = (existing == null) ? new AppMenu() : existing;
            if (existing == null) menu.setMenuCode(codeField.getValue().trim().toUpperCase().replace(" ", "_"));
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
            dialog.close();
            refreshMenuTreeGrid();
            Notification.show("Menu berhasil disimpan!", 2000, Notification.Position.BOTTOM_END);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Batal", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private void openMenuDialogItem(AppMenu existing, String parentCode) {
        openMenuDialog(existing, parentCode);
    }

    // ============================
    // TAB 4: MATRIKS OTORITAS AKSES
    // ============================

    private VerticalLayout setupMatrixTab() {
        VerticalLayout l = new VerticalLayout();
        l.setSizeFull();
        l.setPadding(false);

        StandardActionToolbar toolbar = new StandardActionToolbar()
                .onNew(this::openPermissionDialog)
                .onRefresh(this::refreshMatrixGrid);

        var colRole = matrixGrid.addColumn(RoleMenuPermission::getRoleCode).setHeader("Role").setSortable(true).setAutoWidth(true);
        var colMenu = matrixGrid.addColumn(RoleMenuPermission::getMenuCode).setHeader("Kode Menu").setSortable(true).setAutoWidth(true);

        matrixGrid.addComponentColumn(p -> {
            Checkbox cb = new Checkbox();
            cb.setValue(Boolean.TRUE.equals(p.getCanAdd()));
            cb.addValueChangeListener(ev -> { p.setCanAdd(ev.getValue()); permissionRepository.save(p); });
            return cb;
        }).setHeader("Can Add");

        matrixGrid.addComponentColumn(p -> {
            Checkbox cb = new Checkbox();
            cb.setValue(Boolean.TRUE.equals(p.getCanEdit()));
            cb.addValueChangeListener(ev -> { p.setCanEdit(ev.getValue()); permissionRepository.save(p); });
            return cb;
        }).setHeader("Can Edit");

        matrixGrid.addComponentColumn(p -> {
            Checkbox cb = new Checkbox();
            cb.setValue(Boolean.TRUE.equals(p.getCanDelete()));
            cb.addValueChangeListener(ev -> { p.setCanDelete(ev.getValue()); permissionRepository.save(p); });
            return cb;
        }).setHeader("Can Delete");

        matrixGrid.addComponentColumn(p -> {
            Checkbox cb = new Checkbox();
            cb.setValue(Boolean.TRUE.equals(p.getCanPrint()));
            cb.addValueChangeListener(ev -> { p.setCanPrint(ev.getValue()); permissionRepository.save(p); });
            return cb;
        }).setHeader("Can Print");

        matrixGrid.addComponentColumn(p -> {
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                permissionRepository.delete(p);
                refreshMatrixGrid();
                Notification.show("Izin dihapus!", 1500, Notification.Position.BOTTOM_END);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            return deleteBtn;
        }).setHeader("Hapus").setAutoWidth(true);

        matrixGrid.setHeight("100%");
        matrixGrid.getStyle().set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        Paragraph hint = new Paragraph("💡 Setiap role harus memiliki record izin menu agar menu tersebut muncul di sidebar user. SUPER_ADMIN otomatis mendapat akses seluruh menu.");
        hint.getStyle().set("font-size", "0.8rem").set("color", "#64748b").set("margin", "0");

        Map<Grid.Column<RoleMenuPermission>, Function<RoleMenuPermission, String>> getterMap = Map.of(
                colRole, p -> p.getRoleCode() != null ? p.getRoleCode() : "",
                colMenu, p -> p.getMenuCode() != null ? p.getMenuCode() : ""
        );
        matrixFilterRefresher = StandardGridUtils.attachGridFilters(matrixGrid, getterMap, permissionRepository::findAll);

        l.add(toolbar, hint, matrixGrid);
        l.setFlexGrow(1, matrixGrid);
        return l;
    }

    private void openPermissionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tambah Izin Akses Menu untuk Role");
        dialog.setWidth("440px");

        FormLayout form = new FormLayout();

        ComboBox<String> roleSelect = new ComboBox<>("Role");
        roleSelect.setWidthFull();
        roleSelect.setItems(roleRepository.findAll().stream().map(AppRole::getRoleCode).toList());
        roleSelect.setRequired(true);

        ComboBox<String> menuSelect = new ComboBox<>("Menu");
        menuSelect.setWidthFull();
        menuSelect.setItems(menuRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(m -> !"GROUP".equalsIgnoreCase(m.getMenuType()))
                .map(AppMenu::getMenuCode)
                .toList());
        menuSelect.setRequired(true);

        Checkbox cbAdd = new Checkbox("Can Add"); cbAdd.setValue(true);
        Checkbox cbEdit = new Checkbox("Can Edit"); cbEdit.setValue(true);
        Checkbox cbDelete = new Checkbox("Can Delete"); cbDelete.setValue(true);
        Checkbox cbPrint = new Checkbox("Can Print"); cbPrint.setValue(true);

        HorizontalLayout cbLayout = new HorizontalLayout(cbAdd, cbEdit, cbDelete, cbPrint);
        cbLayout.setWidthFull();

        form.add(roleSelect, menuSelect);
        form.setColspan(roleSelect, 2);
        form.setColspan(menuSelect, 2);

        Button saveBtn = new Button("Simpan", e -> {
            if (roleSelect.getValue() == null || menuSelect.getValue() == null) {
                Notification.show("Role dan Menu wajib dipilih!", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            var existing = permissionRepository.findByRoleCodeAndMenuCode(roleSelect.getValue(), menuSelect.getValue());
            if (existing.isPresent()) {
                Notification n = Notification.show("Izin untuk kombinasi Role & Menu tersebut sudah ada!", 3000, Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            RoleMenuPermission perm = new RoleMenuPermission();
            perm.setRoleCode(roleSelect.getValue());
            perm.setMenuCode(menuSelect.getValue());
            perm.setCanAdd(cbAdd.getValue());
            perm.setCanEdit(cbEdit.getValue());
            perm.setCanDelete(cbDelete.getValue());
            perm.setCanPrint(cbPrint.getValue());
            permissionRepository.save(perm);
            dialog.close();
            refreshMatrixGrid();
            Notification.show("Izin akses menu berhasil ditambahkan!", 2000, Notification.Position.BOTTOM_END);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Batal", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form, cbLayout);
        dialog.open();
    }

    // ============================
    // DATA REFRESH
    // ============================

    private void refreshUserGrid() {
        if (userFilterRefresher != null) userFilterRefresher.run();
    }

    private void refreshRoleGrid() {
        if (roleFilterRefresher != null) roleFilterRefresher.run();
    }

    private void refreshMenuTreeGrid() {
        menuTreeGrid.setDataProvider(new AbstractBackEndHierarchicalDataProvider<AppMenu, Void>() {
            @Override
            public int getChildCount(HierarchicalQuery<AppMenu, Void> query) {
                AppMenu parent = query.getParent();
                if (parent == null) {
                    return (int) menuRepository.findByParentMenuCodeIsNullOrderByDisplayOrderAsc().size();
                }
                return (int) menuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(parent.getMenuCode()).size();
            }

            @Override
            public boolean hasChildren(AppMenu item) {
                return "GROUP".equalsIgnoreCase(item.getMenuType())
                        && !menuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(item.getMenuCode()).isEmpty();
            }

            @Override
            protected Stream<AppMenu> fetchChildrenFromBackEnd(HierarchicalQuery<AppMenu, Void> query) {
                AppMenu parent = query.getParent();
                if (parent == null) {
                    return menuRepository.findByParentMenuCodeIsNullOrderByDisplayOrderAsc().stream();
                }
                return menuRepository.findByParentMenuCodeOrderByDisplayOrderAsc(parent.getMenuCode()).stream();
            }
        });
        menuTreeGrid.expand(menuRepository.findByParentMenuCodeIsNullOrderByDisplayOrderAsc());
    }

    private void refreshMatrixGrid() {
        if (matrixFilterRefresher != null) matrixFilterRefresher.run();
    }
}
