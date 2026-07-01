package com.vaadinerp.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadinerp.security.service.SessionSecurityService;
import com.vaadinerp.service.DynamicDataService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Route("field-audit-log")
public class FieldAuditLogView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    private final SessionSecurityService securityService;

    private final TextField filterFormCode = new TextField("Form Code");
    private final TextField filterTableName = new TextField("Table Name");
    private final TextField filterRecordId = new TextField("Record ID");
    private final TextField filterFieldName = new TextField("Kolom (Field)");
    private final TextField filterActionBy = new TextField("User (Action By)");

    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final Span recordCountBadge = new Span("0 records");

    public FieldAuditLogView(DynamicDataService dynamicDataService, SessionSecurityService securityService) {
        this.dynamicDataService = dynamicDataService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8fafc");

        add(createHeader(), createFilterToolbar(), createGridContainer());
        refreshGrid();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
                .set("background", "white")
                .set("padding", "16px 24px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.05), 0 1px 2px rgba(0,0,0,0.06)")
                .set("border", "1px solid #e2e8f0");

        HorizontalLayout titleBox = new HorizontalLayout();
        titleBox.setAlignItems(FlexComponent.Alignment.CENTER);
        titleBox.setSpacing(true);

        Div iconBox = new Div();
        iconBox.getStyle()
                .set("width", "42px")
                .set("height", "42px")
                .set("border-radius", "10px")
                .set("background", "linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "white")
                .set("box-shadow", "0 4px 6px -1px rgba(59, 130, 246, 0.3)");
        Icon icon = VaadinIcon.CLOCK.create();
        icon.setSize("22px");
        iconBox.add(icon);

        VerticalLayout textBox = new VerticalLayout();
        textBox.setPadding(false);
        textBox.setSpacing(false);

        H2 title = new H2("Field Audit Log Viewer");
        title.getStyle().set("margin", "0").set("font-size", "1.25rem").set("font-weight", "700").set("color", "#1e293b");
        Span subtitle = new Span("Riwayat perubahan data tingkat kolom (Audit Trail) pada semua tabel & formulir");
        subtitle.getStyle().set("font-size", "0.85rem").set("color", "#64748b");

        textBox.add(title, subtitle);
        titleBox.add(iconBox, textBox);

        recordCountBadge.getStyle()
                .set("background-color", "#eff6ff")
                .set("color", "#2563eb")
                .set("padding", "6px 14px")
                .set("border-radius", "9999px")
                .set("font-weight", "600")
                .set("font-size", "0.85rem")
                .set("border", "1px solid #bfdbfe");

        header.add(titleBox, recordCountBadge);
        return header;
    }

    private Component createFilterToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        toolbar.getStyle()
                .set("background", "white")
                .set("padding", "16px 20px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)")
                .set("border", "1px solid #e2e8f0");

        filterFormCode.setPlaceholder("Cari kode form...");
        filterFormCode.setClearButtonVisible(true);

        filterTableName.setPlaceholder("Cari nama tabel...");
        filterTableName.setClearButtonVisible(true);

        filterRecordId.setPlaceholder("ID record...");
        filterRecordId.setClearButtonVisible(true);
        filterRecordId.setWidth("130px");

        filterFieldName.setPlaceholder("Nama kolom...");
        filterFieldName.setClearButtonVisible(true);

        filterActionBy.setPlaceholder("Nama user...");
        filterActionBy.setClearButtonVisible(true);

        Button btnSearch = new Button("Cari / Filter", VaadinIcon.SEARCH.create());
        btnSearch.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSearch.getStyle().set("border-radius", "8px").set("font-weight", "600");
        btnSearch.addClickListener(e -> refreshGrid());

        Button btnReset = new Button("Reset", VaadinIcon.REFRESH.create());
        btnReset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnReset.addClickListener(e -> {
            filterFormCode.clear();
            filterTableName.clear();
            filterRecordId.clear();
            filterFieldName.clear();
            filterActionBy.clear();
            refreshGrid();
        });

        toolbar.add(filterFormCode, filterTableName, filterRecordId, filterFieldName, filterActionBy, btnSearch, btnReset);
        return toolbar;
    }

    private Component createGridContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setSizeFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)")
                .set("border", "1px solid #e2e8f0")
                .set("overflow", "hidden");

        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        grid.addComponentColumn(row -> {
            Object dt = row.get("action_dt");
            String formatted = "-";
            if (dt instanceof Timestamp ts) {
                formatted = sdf.format(ts);
            } else if (dt != null) {
                formatted = dt.toString();
            }
            Span s = new Span(formatted);
            s.getStyle().set("font-size", "0.82rem").set("color", "#475569").set("font-weight", "500");
            return s;
        }).setHeader("Waktu").setWidth("150px").setFlexGrow(0).setSortable(true);

        grid.addComponentColumn(row -> {
            String user = row.get("action_by") != null ? row.get("action_by").toString() : "-";
            Span badge = new Span(user);
            badge.getStyle()
                    .set("background-color", "#f1f5f9")
                    .set("color", "#334155")
                    .set("padding", "3px 8px")
                    .set("border-radius", "6px")
                    .set("font-size", "0.8rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("User").setWidth("120px").setFlexGrow(0);

        grid.addComponentColumn(row -> {
            String form = row.get("form_code") != null ? row.get("form_code").toString() : "-";
            String tbl = row.get("table_name") != null ? row.get("table_name").toString() : "-";
            VerticalLayout box = new VerticalLayout();
            box.setPadding(false);
            box.setSpacing(false);
            Span sForm = new Span(form);
            sForm.getStyle().set("font-weight", "600").set("font-size", "0.85rem").set("color", "#1e293b");
            Span sTbl = new Span("Tabel: " + tbl);
            sTbl.getStyle().set("font-size", "0.75rem").set("color", "#64748b");
            box.add(sForm, sTbl);
            return box;
        }).setHeader("Form / Tabel").setWidth("180px").setFlexGrow(1);

        grid.addComponentColumn(row -> {
            String recId = row.get("record_id") != null ? row.get("record_id").toString() : "-";
            Span badge = new Span("#" + recId);
            badge.getStyle()
                    .set("background-color", "#e2e8f0")
                    .set("color", "#1e293b")
                    .set("padding", "2px 8px")
                    .set("border-radius", "4px")
                    .set("font-family", "monospace")
                    .set("font-weight", "600")
                    .set("font-size", "0.82rem");
            return badge;
        }).setHeader("Rec ID").setWidth("100px").setFlexGrow(0);

        grid.addComponentColumn(row -> {
            String action = row.get("action_type") != null ? row.get("action_type").toString().toUpperCase() : "-";
            Span badge = new Span(action);
            if ("INSERT".equals(action)) {
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d").set("border", "1px solid #bbf7d0");
            } else if ("UPDATE".equals(action)) {
                badge.getStyle().set("background-color", "#fef9c3").set("color", "#a16207").set("border", "1px solid #fde047");
            } else {
                badge.getStyle().set("background-color", "#f3f4f6").set("color", "#4b5563");
            }
            badge.getStyle().set("padding", "3px 10px").set("border-radius", "9999px").set("font-size", "0.75rem").set("font-weight", "700");
            return badge;
        }).setHeader("Aksi").setWidth("100px").setFlexGrow(0);

        grid.addComponentColumn(row -> {
            String field = row.get("field_name") != null ? row.get("field_name").toString() : "-";
            Span s = new Span(field);
            s.getStyle().set("font-weight", "600").set("color", "#2563eb");
            return s;
        }).setHeader("Kolom").setWidth("140px").setFlexGrow(0);

        grid.addComponentColumn(row -> {
            String oldVal = row.get("old_value") != null ? row.get("old_value").toString() : "(kosong)";
            Div box = new Div();
            box.setText(oldVal);
            box.getStyle()
                    .set("color", "#ef4444")
                    .set("font-family", "monospace")
                    .set("font-size", "0.82rem")
                    .set("background-color", "#fef2f2")
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("word-break", "break-all")
                    .set("max-height", "60px")
                    .set("overflow-y", "auto");
            return box;
        }).setHeader("Nilai Lama (Old Value)").setFlexGrow(1);

        grid.addComponentColumn(row -> {
            String newVal = row.get("new_value") != null ? row.get("new_value").toString() : "(kosong)";
            Div box = new Div();
            box.setText(newVal);
            box.getStyle()
                    .set("color", "#10b981")
                    .set("font-family", "monospace")
                    .set("font-size", "0.82rem")
                    .set("background-color", "#ecfdf5")
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("word-break", "break-all")
                    .set("max-height", "60px")
                    .set("overflow-y", "auto");
            return box;
        }).setHeader("Nilai Baru (New Value)").setFlexGrow(1);

        container.add(grid);
        return container;
    }

    private void refreshGrid() {
        List<Map<String, Object>> logs = dynamicDataService.fetchFieldAuditLogs(
                filterFormCode.getValue(),
                filterTableName.getValue(),
                filterRecordId.getValue(),
                filterFieldName.getValue(),
                filterActionBy.getValue(),
                500
        );
        grid.setItems(logs);
        recordCountBadge.setText("Total: " + logs.size() + " log perubahan (Limit 500)");
    }
}
