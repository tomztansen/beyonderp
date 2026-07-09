package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.AppStandardFormat;
import com.vaadinerp.service.StandardFormatService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route("standard-format")
public class StandardFormatView extends VerticalLayout {

    private final StandardFormatService service;
    private final Grid<AppStandardFormat> grid = new Grid<>(AppStandardFormat.class, false);

    public StandardFormatView(StandardFormatService service) {
        this.service = service;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "#f8fafc");

        buildHeader();
        buildGrid();
        refreshData();
    }

    private void buildHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon headerIcon = VaadinIcon.SLIDERS.create();
        headerIcon.setSize("28px");
        headerIcon.getStyle().set("color", "#6366f1");

        VerticalLayout titleBox = new VerticalLayout();
        titleBox.setPadding(false);
        titleBox.setSpacing(false);

        H2 title = new H2("Konfigurasi Format Standar (Standard Format)");
        title.getStyle().set("margin", "0").set("color", "#0f172a").set("font-size", "1.4rem");

        Span subtitle = new Span("Atur format tampilan default secara global untuk setiap komponen yang formatnya kosong (NONE).");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "0.85rem");

        titleBox.add(title, subtitle);

        Button btnRefresh = new Button("Segarkan", VaadinIcon.REFRESH.create(), e -> {
            service.refreshCache();
            refreshData();
            Notification.show("Daftar format disegarkan!", 2000, Notification.Position.BOTTOM_END);
        });
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        headerLayout.add(headerIcon, titleBox);
        headerLayout.setFlexGrow(1, titleBox);
        headerLayout.add(btnRefresh);

        add(headerLayout);
    }

    private void buildGrid() {
        grid.setWidthFull();
        grid.getStyle().set("border", "1px solid #e2e8f0").set("border-radius", "8px").set("background", "white");

        grid.addComponentColumn(fmt -> {
            Span badge = new Span(fmt.getComponentType());
            badge.getStyle()
                    .set("padding", "4px 10px")
                    .set("border-radius", "6px")
                    .set("font-weight", "600")
                    .set("font-size", "0.8rem")
                    .set("background", "#e0e7ff")
                    .set("color", "#3730a3");
            return badge;
        }).setHeader("Tipe Komponen").setWidth("180px").setFlexGrow(0);

        grid.addComponentColumn(fmt -> {
            Span code = new Span(fmt.getFormatPattern());
            code.getStyle()
                    .set("font-family", "monospace")
                    .set("font-weight", "600")
                    .set("color", "#0f172a")
                    .set("background", "#f1f5f9")
                    .set("padding", "3px 8px")
                    .set("border-radius", "4px");
            return code;
        }).setHeader("Pola Format (Pattern)").setWidth("220px").setFlexGrow(0);

        grid.addColumn(AppStandardFormat::getDescription).setHeader("Deskripsi").setFlexGrow(1);

        grid.addComponentColumn(fmt -> {
            String preview = generateSamplePreview(fmt.getComponentType(), fmt.getFormatPattern());
            Span previewSpan = new Span(preview);
            previewSpan.getStyle()
                    .set("font-weight", "600")
                    .set("color", "#059669")
                    .set("background", "#ecfdf5")
                    .set("padding", "4px 10px")
                    .set("border-radius", "6px")
                    .set("border", "1px solid #a7f3d0");
            return previewSpan;
        }).setHeader("Contoh Tampilan (Live Preview)").setWidth("250px").setFlexGrow(0);

        grid.addComponentColumn(fmt -> {
            Button btnEdit = new Button("Ubah", VaadinIcon.EDIT.create(), e -> openEditDialog(fmt));
            btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            return btnEdit;
        }).setHeader("Aksi").setWidth("120px").setFlexGrow(0);

        add(grid);
        setFlexGrow(1, grid);
    }

    private void refreshData() {
        List<AppStandardFormat> list = service.getAllFormats();
        grid.setItems(list);
    }

    private void openEditDialog(AppStandardFormat format) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ubah Format Standar: " + format.getComponentType());
        dialog.setWidth("500px");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        TextField txtType = new TextField("Tipe Komponen");
        txtType.setValue(format.getComponentType());
        txtType.setReadOnly(true);
        txtType.setWidthFull();

        TextField txtPattern = new TextField("Pola Format (Pattern)");
        txtPattern.setValue(format.getFormatPattern() != null ? format.getFormatPattern() : "");
        txtPattern.setWidthFull();
        txtPattern.setValueChangeMode(ValueChangeMode.EAGER);

        TextField txtDesc = new TextField("Deskripsi");
        txtDesc.setValue(format.getDescription() != null ? format.getDescription() : "");
        txtDesc.setWidthFull();

        Div previewBox = new Div();
        previewBox.setWidthFull();
        previewBox.getStyle()
                .set("margin-top", "10px")
                .set("padding", "12px")
                .set("border-radius", "6px")
                .set("background", "#f0fdf4")
                .set("border", "1px solid #bbf7d0");

        Span previewTitle = new Span("Live Preview: ");
        previewTitle.getStyle().set("font-size", "0.8rem").set("color", "#166534").set("display", "block");

        Span previewValue = new Span(generateSamplePreview(format.getComponentType(), txtPattern.getValue()));
        previewValue.getStyle().set("font-size", "1.1rem").set("font-weight", "700").set("color", "#15803d");

        previewBox.add(previewTitle, previewValue);

        txtPattern.addValueChangeListener(e -> {
            String newPreview = generateSamplePreview(format.getComponentType(), e.getValue());
            previewValue.setText(newPreview);
        });

        formLayout.add(txtType, txtPattern, txtDesc, previewBox);
        dialog.add(formLayout);

        Button btnSave = new Button("Simpan Format", VaadinIcon.CHECK.create(), e -> {
            if (txtPattern.getValue() == null || txtPattern.getValue().trim().isEmpty()) {
                Notification.show("Pola format tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }
            service.saveFormat(format.getComponentType(), txtPattern.getValue(), txtDesc.getValue());
            refreshData();
            dialog.close();
            Notification.show("Format " + format.getComponentType() + " berhasil diperbarui!", 3000, Notification.Position.BOTTOM_END);
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new Button("Batal", e -> dialog.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(btnCancel, btnSave);
        dialog.open();
    }

    private String generateSamplePreview(String type, String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) return "-";
        String cleanPattern = pattern.trim();
        try {
            if ("DATEBOX".equalsIgnoreCase(type)) {
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern(cleanPattern));
            } else if ("DATETIMEBOX".equalsIgnoreCase(type)) {
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern(cleanPattern));
            } else if ("TIMEBOX".equalsIgnoreCase(type)) {
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern(cleanPattern));
            } else if ("INTBOX".equalsIgnoreCase(type) || "DECIMALBOX".equalsIgnoreCase(type) || "CURRENCY".equalsIgnoreCase(type)) {
                boolean hasRp = cleanPattern.startsWith("Rp ") || cleanPattern.startsWith("Rp");
                String numPattern = cleanPattern;
                if (hasRp) numPattern = cleanPattern.replace("Rp ", "").replace("Rp", "").trim();
                BigDecimal sampleNum = "INTBOX".equalsIgnoreCase(type) ? new BigDecimal("1250000") : new BigDecimal("1250000.75");
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("id", "ID"));
                DecimalFormat df = new DecimalFormat(numPattern, symbols);
                String formatted = df.format(sampleNum);
                return hasRp ? "Rp " + formatted : formatted;
            }
        } catch (Exception e) {
            return "(Invalid Pattern: " + cleanPattern + ")";
        }
        return pattern;
    }
}
