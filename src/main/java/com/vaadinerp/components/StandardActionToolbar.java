package com.vaadinerp.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Komponen Toolbar Aksi Standar ERP (Tambah, Hapus, Simpan, Batal, Refresh, Tutup, Cetak).
 * Didesain dengan Fluent API dan kesiapan Otoritas/RBAC (Role-Based Access Control) per User per Menu.
 */
public class StandardActionToolbar extends HorizontalLayout {

    /**
     * DTO Otoritas Menu Granular.
     * Digunakan untuk mengatur hak akses tombol aksi berdasarkan otoritas user yang sedang login.
     */
    public static class MenuAccessAuthority {
        public boolean canAdd = true;
        public boolean canEdit = true;
        public boolean canDelete = true;
        public boolean canPrint = true;

        public static MenuAccessAuthority fullAccess() {
            return new MenuAccessAuthority();
        }

        public static MenuAccessAuthority readOnly() {
            MenuAccessAuthority auth = new MenuAccessAuthority();
            auth.canAdd = false;
            auth.canEdit = false;
            auth.canDelete = false;
            return auth;
        }

        public static MenuAccessAuthority noPrint() {
            MenuAccessAuthority auth = new MenuAccessAuthority();
            auth.canPrint = false;
            return auth;
        }
    }

    private final Button btnNew = new Button("Tambah");
    private final Button btnDelete = new Button("Hapus");
    private final Button btnSave = new Button("Simpan");
    private final Button btnCancel = new Button("Batal");
    private final Button btnRefresh = new Button("Refresh");

    private final Button btnPrint = new Button("Cetak");

    public StandardActionToolbar() {
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        addClassName("sticky-toolbar");
        getStyle()
                .set("background", "linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%)")
                .set("border", "1px solid #cbd5e1")
                .set("border-radius", "10px")
                .set("padding", "8px 16px")
                .set("gap", "12px")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.08)")
                .set("transition", "all 0.25s ease")
                .set("flex-wrap", "wrap")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "50");

        getElement().addEventListener("mouseover", e -> {}).addEventData("element.style.boxShadow='0 8px 20px rgba(0,0,0,0.08)'; element.style.borderColor='#94a3b8'; element.style.background='linear-gradient(180deg, #ffffff 0%, #f8fafc 100%)'");
        getElement().addEventListener("mouseout", e -> {}).addEventData("element.style.boxShadow='0 1px 3px rgba(0,0,0,0.05)'; element.style.borderColor='#cbd5e1'; element.style.background='linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%)'");

        // 1. TAMBAH
        setupButton(btnNew, VaadinIcon.PLUS_CIRCLE, "#22c55e");
        
        // 2. HAPUS
        setupButton(btnDelete, VaadinIcon.CLOSE_CIRCLE, "#ef4444");

        // 3. SIMPAN
        setupButton(btnSave, VaadinIcon.DOWNLOAD, "#3b82f6");
        btnSave.getStyle().set("color", "#3b82f6");

        // 4. BATAL
        setupButton(btnCancel, VaadinIcon.BAN, "#ef4444");

        // 5. REFRESH
        setupButton(btnRefresh, VaadinIcon.REFRESH, "#3b82f6");

        // 7. CETAK
        setupButton(btnPrint, VaadinIcon.PRINT, "#374151");

        // Default: Sembunyikan tombol sampai ada action listener yang di-bind
        btnNew.setVisible(false);
        btnDelete.setVisible(false);
        btnSave.setVisible(false);
        btnCancel.setVisible(false);
        btnRefresh.setVisible(false);
        btnPrint.setVisible(false);

        add(btnNew, btnDelete, btnSave, btnCancel, btnRefresh, btnPrint);
    }

    private void setupButton(Button btn, VaadinIcon iconType, String iconColor) {
        Icon icon = iconType.create();
        icon.getStyle().set("color", iconColor).set("font-size", "1.2rem");
        btn.setIcon(icon);
        styleToolbarButton(btn, iconColor);
    }

    public static void styleToolbarButton(Button btn, String iconColor) {
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getStyle()
                .set("font-weight", "600")
                .set("color", "#1e293b")
                .set("padding", "6px 14px")
                .set("border-radius", "8px")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease-in-out")
                .set("border", "1px solid transparent");

        btn.getElement().addEventListener("mouseover", e -> {}).addEventData("element.style.backgroundColor='#ffffff'; element.style.boxShadow='0 4px 12px rgba(0,0,0,0.14)'; element.style.borderColor='#cbd5e1'; element.style.transform='translateY(-1px)'");
        btn.getElement().addEventListener("mouseout", e -> {}).addEventData("element.style.backgroundColor=''; element.style.boxShadow=''; element.style.borderColor='transparent'; element.style.transform=''");
    }

    // --- FLUENT ACTION SETTERS ---

    public StandardActionToolbar onNew(Runnable action) {
        btnNew.setVisible(true);
        btnNew.addClickListener(e -> action.run());
        return this;
    }

    public StandardActionToolbar onDelete(Runnable action) {
        btnDelete.setVisible(true);
        btnDelete.addClickListener(e -> action.run());
        return this;
    }

    public StandardActionToolbar onSave(Runnable action) {
        btnSave.setVisible(true);
        btnSave.addClickListener(e -> action.run());
        return this;
    }

    public StandardActionToolbar onCancel(Runnable action) {
        btnCancel.setVisible(true);
        btnCancel.addClickListener(e -> action.run());
        return this;
    }

    public StandardActionToolbar onRefresh(Runnable action) {
        btnRefresh.setVisible(true);
        btnRefresh.addClickListener(e -> action.run());
        return this;
    }

    public StandardActionToolbar onPrint(Runnable action) {
        btnPrint.setVisible(true);
        btnPrint.addClickListener(e -> action.run());
        return this;
    }

    // --- RBAC & AUTHORITY HANDLING ---

    /**
     * Menerapkan aturan otoritas menu ke toolbar ini.
     * Tombol aksi (Tambah, Edit/Simpan, Hapus, Cetak) akan otomatis disembunyikan/disabled jika user tidak berwenang.
     */
    public StandardActionToolbar applyAuthority(MenuAccessAuthority auth) {
        if (auth == null) return this;

        if (!auth.canAdd) {
            btnNew.setVisible(false);
            btnNew.setEnabled(false);
        }
        if (!auth.canEdit) {
            btnSave.setVisible(false);
            btnSave.setEnabled(false);
        }
        if (!auth.canDelete) {
            btnDelete.setVisible(false);
            btnDelete.setEnabled(false);
        }
        if (!auth.canPrint) {
            btnPrint.setVisible(false);
            btnPrint.setEnabled(false);
        }
        
        // Logika Opsi 1/2: Tombol Batal hanya aktif jika user memiliki hak Add atau Edit
        if (!auth.canAdd && !auth.canEdit) {
            btnCancel.setVisible(false);
            btnCancel.setEnabled(false);
        }
        
        return this;
    }

    // --- DIRECT COMPONENT ACCESSORS ---

    public Button getBtnNew() { return btnNew; }
    public Button getBtnDelete() { return btnDelete; }
    public Button getBtnSave() { return btnSave; }
    public Button getBtnCancel() { return btnCancel; }
    public Button getBtnRefresh() { return btnRefresh; }

    public Button getBtnPrint() { return btnPrint; }
}
