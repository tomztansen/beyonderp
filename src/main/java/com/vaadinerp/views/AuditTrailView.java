package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadinerp.service.DynamicDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route("audit-trail")
@PageTitle("Audit Trail & Data Recovery Center | Vaadin ERP")
public class AuditTrailView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    private final JdbcTemplate jdbcTemplate;

    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> actionFilter = new ComboBox<>();

    @Autowired
    public AuditTrailView(DynamicDataService dynamicDataService, JdbcTemplate jdbcTemplate) {
        this.dynamicDataService = dynamicDataService;
        this.jdbcTemplate = jdbcTemplate;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8fafc");

        // Pastikan tabel sys_audit_log ada
        ensureAuditTableExists();

        buildHeader();
        buildToolbar();
        buildGrid();

        refreshGrid();
    }

    private void ensureAuditTableExists() {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_audit_log (" +
                    "id SERIAL PRIMARY KEY, " +
                    "table_name VARCHAR(100) NOT NULL, " +
                    "record_id VARCHAR(100), " +
                    "action_type VARCHAR(20) NOT NULL, " +
                    "action_by VARCHAR(100), " +
                    "action_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "old_data_json TEXT, " +
                    "new_data_json TEXT)");
        } catch (Exception ignored) {
        }
    }

    private void buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon shieldIcon = VaadinIcon.SHIELD.create();
        shieldIcon.setSize("28px");
        shieldIcon.setColor("#2563eb");
        H3 title = new H3("Audit Trail & Data Recovery Center (sys_audit_log)");
        title.getStyle().set("margin", "0").set("color", "#1e293b").set("font-weight", "700");
        titleLayout.add(shieldIcon, title);

        Span infoBadge = new Span(
                "💡 Click 'Restore' to restore deleted data to its original table.");
        infoBadge.getStyle()
                .set("background-color", "#e0f2fe")
                .set("color", "#0369a1")
                .set("padding", "6px 14px")
                .set("border-radius", "20px")
                .set("font-size", "0.85rem")
                .set("font-weight", "600");

        header.add(titleLayout, infoBadge);
        add(header);
    }

    private void buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);

        searchField.setPlaceholder("Cari Tabel / ID Record / User...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("350px");
        searchField.addValueChangeListener(e -> refreshGrid());

        actionFilter.setPlaceholder("All Action");
        actionFilter.setItems("All Action", "DELETE", "UPDATE", "INSERT", "RESTORE");
        actionFilter.setValue("All Action");
        actionFilter.setWidth("200px");
        actionFilter.addValueChangeListener(e -> refreshGrid());

        Button btnRefresh = new Button("Refresh", VaadinIcon.REFRESH.create());
        btnRefresh.addClickListener(e -> refreshGrid());

        Button btnRecycleBinHelp = new Button("ℹ️ Help & Restore Guide", e -> showHelpDialog());
        btnRecycleBinHelp.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        toolbar.add(searchField, actionFilter, btnRefresh, btnRecycleBinHelp);
        add(toolbar);
    }

    private void buildGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        grid.addColumn(row -> row.get("id"))
                .setHeader("Log ID").setWidth("100px").setFlexGrow(0);

        grid.addColumn(row -> row.get("action_dt") != null ? row.get("action_dt").toString() : "")
                .setHeader("Timestamp").setWidth("180px").setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(row -> {
            String action = row.get("action_type") != null ? row.get("action_type").toString() : "UNKNOWN";
            Span badge = new Span(action);
            badge.getStyle()
                    .set("padding", "4px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.78rem")
                    .set("font-weight", "700");

            if ("DELETE".equalsIgnoreCase(action)) {
                badge.getStyle().set("background-color", "#fee2e2").set("color", "#dc2626");
            } else if ("UPDATE".equalsIgnoreCase(action)) {
                badge.getStyle().set("background-color", "#fef3c7").set("color", "#d97706");
            } else if ("INSERT".equalsIgnoreCase(action)) {
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#16a34a");
            } else if ("RESTORE".equalsIgnoreCase(action)) {
                badge.getStyle().set("background-color", "#e0e7ff").set("color", "#4f46e5");
            } else {
                badge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
            }
            return badge;
        })).setHeader("Action").setWidth("130px").setFlexGrow(0);

        grid.addColumn(row -> row.get("table_name"))
                .setHeader("Tabel Target").setWidth("180px").setFlexGrow(0);

        grid.addColumn(row -> row.get("record_id"))
                .setHeader("ID Record / PK").setWidth("150px").setFlexGrow(0);

        grid.addColumn(row -> row.get("action_by"))
                .setHeader("Eksekutor").setWidth("140px").setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(row -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button btnViewJson = new Button("See Snapshot", VaadinIcon.FILE_TEXT.create());
            btnViewJson.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnViewJson.addClickListener(e -> showSnapshotDialog(row));
            actions.add(btnViewJson);

            String actionType = row.get("action_type") != null ? row.get("action_type").toString() : "";
            if ("DELETE".equalsIgnoreCase(actionType) || "UPDATE".equalsIgnoreCase(actionType)) {
                Button btnRestore = new Button("🔄 Pulihkan / Restore", VaadinIcon.ROTATE_LEFT.create());
                btnRestore.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY,
                        ButtonVariant.LUMO_SUCCESS);
                btnRestore.getStyle().set("font-weight", "600");
                btnRestore.addClickListener(e -> confirmRestore(row));
                actions.add(btnRestore);
            }

            return actions;
        })).setHeader("Aksi Pemulihan (Recovery & Snapshot)").setFlexGrow(1);

        add(grid);
    }

    private void refreshGrid() {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM sys_audit_log WHERE 1=1 ");
            List<Object> args = new ArrayList<>();

            String query = searchField.getValue();
            if (query != null && !query.trim().isEmpty()) {
                sql.append("AND (table_name ILIKE ? OR record_id ILIKE ? OR action_by ILIKE ?) ");
                String likeQuery = "%" + query.trim() + "%";
                args.add(likeQuery);
                args.add(likeQuery);
                args.add(likeQuery);
            }

            String action = actionFilter.getValue();
            if (action != null && !"Semua Aksi".equals(action)) {
                sql.append("AND action_type = ? ");
                args.add(action);
            }

            sql.append("ORDER BY action_dt DESC LIMIT 500");

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
            grid.setItems(rows);
        } catch (Exception ex) {
            Notification.show("Gagal memuat log audit: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
        }
    }

    private void showSnapshotDialog(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📋 Detail Snapshot Data Audit Log ID #" + row.get("id"));
        dialog.setWidth("750px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        String oldJson = row.get("old_data_json") != null ? row.get("old_data_json").toString()
                : "Tidak ada snapshot data lama.";
        String newJson = row.get("new_data_json") != null ? row.get("new_data_json").toString()
                : "Tidak ada snapshot data baru.";

        Span lblOld = new Span("Old Data Snapshot (Sebelum Perubahan/Penghapusan):");
        lblOld.getStyle().set("font-weight", "600").set("color", "#dc2626");
        Pre preOld = new Pre(oldJson);
        preOld.getStyle()
                .set("background-color", "#1e293b")
                .set("color", "#f8fafc")
                .set("padding", "12px")
                .set("border-radius", "8px")
                .set("max-height", "250px")
                .set("overflow", "auto")
                .set("width", "100%")
                .set("font-family", "monospace");

        Span lblNew = new Span("New Data Snapshot (Setelah Perubahan/Penambahan):");
        lblNew.getStyle().set("font-weight", "600").set("color", "#16a34a");
        Pre preNew = new Pre(newJson);
        preNew.getStyle()
                .set("background-color", "#1e293b")
                .set("color", "#f8fafc")
                .set("padding", "12px")
                .set("border-radius", "8px")
                .set("max-height", "250px")
                .set("overflow", "auto")
                .set("width", "100%")
                .set("font-family", "monospace");

        content.add(lblOld, preOld, lblNew, preNew);
        dialog.add(content);

        Button btnClose = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(btnClose);
        dialog.open();
    }

    private void confirmRestore(Map<String, Object> row) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("🛡️ Konfirmasi Pulihkan / Restore Data");

        String tableName = (String) row.get("table_name");
        String recordId = (String) row.get("record_id");
        Long logId = ((Number) row.get("id")).longValue();

        VerticalLayout layout = new VerticalLayout();
        layout.add(new Span("Apakah Anda yakin ingin memulihkan (restore) record berikut ke database?"));
        layout.add(new Span("🎯 Tabel: " + tableName + " | ID Record: " + recordId));
        layout.add(new Span("⚠️ Sistem akan memasukkan kembali snapshot data asli secara utuh ke dalam tabel tujuan."));

        confirmDialog.add(layout);

        Button btnConfirm = new Button("Ya, Pulihkan Sekarang!", VaadinIcon.ROTATE_LEFT.create());
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnConfirm.addClickListener(e -> {
            try {
                boolean success = dynamicDataService.restoreFromAuditLog(logId);
                if (success) {
                    Notification notif = Notification.show(
                            "🎉 Data ID [" + recordId + "] pada tabel [" + tableName + "] BERHASIL DIPULIHKAN 100%!",
                            5000, Notification.Position.TOP_CENTER);
                    notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    refreshGrid();
                }
            } catch (Exception ex) {
                Notification notif = Notification.show("❌ Gagal memulihkan data: " + ex.getMessage(), 6000,
                        Notification.Position.MIDDLE);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            confirmDialog.close();
        });

        Button btnCancel = new Button("Cancel", e -> confirmDialog.close());
        confirmDialog.getFooter().add(btnCancel, btnConfirm);
        confirmDialog.open();
    }

    private void showHelpDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("ℹ️ Panduan Audit Trail & Data Recovery Center");
        dialog.setWidth("650px");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3("Bagaimana cara kerja pemulihan data (Restore)?"));
        layout.add(new Span(
                "1. Setiap kali terjadi penghapusan (DELETE) atau pengubahan (UPDATE) pada menu ERP, sistem merekam snapshot isi data lengkap dalam format JSON ke dalam tabel sys_audit_log."));
        layout.add(new Span(
                "2. Jika ada user tidak sengaja menghapus data atau salah melakukan edit, Anda cukup mencari nama tabel atau ID record di halaman ini."));
        layout.add(new Span(
                "3. Klik tombol '🔄 Pulihkan / Restore'. Sistem otomatis membaca snapshot JSON lama dan memasukkan/mengembalikan data tersebut ke dalam tabel aslinya."));
        layout.add(new Span("4. Relasi dan data Anda pun kembali aman tanpa perlu merestore full backup database!"));

        dialog.add(layout);
        Button btnClose = new Button("Mengerti", e -> dialog.close());
        dialog.getFooter().add(btnClose);
        dialog.open();
    }
}
