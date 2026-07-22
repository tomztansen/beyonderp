package com.vaadinerp.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.*;

public class FormDebugUtils {

    public static class DebugItem {
        private String key;
        private String value;
        private String type;

        public DebugItem(String key, String value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }
        public String getKey() { return key; }
        public String getValue() { return value; }
        public String getType() { return type; }
    }

    public static void showDebugDialog(Map<String, Object> bean) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🐞 Debug Context Form & Header (Filter Mapping Inspector)");
        dialog.setWidth("850px");
        dialog.setHeight("600px");
        dialog.setResizable(true);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);

        Span desc = new Span("Klik ikon salin 📋 pada baris untuk menyalin sintaks atau nilai secara individual, atau tombol 'Salin Semua Data' di bawah.");
        desc.getStyle().set("font-size", "0.85rem").set("color", "#4b5563").set("margin-bottom", "8px");

        TextField filterText = new TextField();
        filterText.setPlaceholder("Cari nama parameter atau nilai...");
        filterText.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.EAGER);
        filterText.setWidthFull();

        List<DebugItem> allItems = new ArrayList<>();
        if (bean != null) {
            for (Map.Entry<String, Object> entry : bean.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (k == null) continue;
                if (v instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> subMap = (Map<String, Object>) v;
                    allItems.add(new DebugItem("header." + k, subMap.toString(), "Map/Record (" + subMap.size() + " cols)"));
                } else if (v instanceof Collection) {
                    allItems.add(new DebugItem("header." + k, v.toString(), "Collection/Set (" + ((Collection<?>) v).size() + " items)"));
                } else {
                    allItems.add(new DebugItem("header." + k, v != null ? v.toString() : "null", v != null ? v.getClass().getSimpleName() : "null"));
                }
            }
        }
        allItems.sort(Comparator.comparing(DebugItem::getKey));

        Grid<DebugItem> grid = new Grid<>();
        
        // Kolom 1: Sintaks Filter Mapping dengan tombol Copy
        grid.addComponentColumn(item -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            hl.setSpacing(true);
            hl.setPadding(false);
            
            Button copyBtn = new Button(new Icon(VaadinIcon.COPY));
            copyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            copyBtn.getElement().setAttribute("title", "Salin sintaks ke clipboard");
            copyBtn.addClickListener(e -> {
                copyBtn.getElement().executeJs("navigator.clipboard.writeText($0)", item.getKey());
                Notification.show("📋 Sintaks disalin: " + item.getKey(), 1500, Notification.Position.BOTTOM_END);
            });
            
            Span span = new Span(item.getKey());
            span.getStyle().set("font-family", "monospace").set("font-weight", "600").set("color", "#2563eb");
            
            hl.add(copyBtn, span);
            return hl;
        }).setHeader("Sintaks Filter Mapping").setAutoWidth(true).setResizable(true).setFlexGrow(1);

        // Kolom 2: Nilai Saat Ini dengan tombol Copy
        grid.addComponentColumn(item -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            hl.setSpacing(true);
            hl.setPadding(false);
            
            String valStr = item.getValue() != null ? item.getValue() : "";
            Button copyBtn = new Button(new Icon(VaadinIcon.COPY));
            copyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            copyBtn.getElement().setAttribute("title", "Salin nilai ke clipboard");
            copyBtn.addClickListener(e -> {
                copyBtn.getElement().executeJs("navigator.clipboard.writeText($0)", valStr);
                Notification.show("📋 Nilai disalin: " + valStr, 1500, Notification.Position.BOTTOM_END);
            });
            
            Span span = new Span(valStr);
            span.getStyle().set("word-break", "break-all");
            
            hl.add(copyBtn, span);
            return hl;
        }).setHeader("Nilai Saat Ini").setAutoWidth(true).setResizable(true).setFlexGrow(2);

        grid.addColumn(DebugItem::getType).setHeader("Data Type").setAutoWidth(true).setResizable(true);

        grid.setItems(allItems);

        filterText.addValueChangeListener(e -> {
            String kw = e.getValue() != null ? e.getValue().toLowerCase().trim() : "";
            if (kw.isEmpty()) {
                grid.setItems(allItems);
            } else {
                List<DebugItem> filtered = allItems.stream()
                        .filter(it -> it.getKey().toLowerCase().contains(kw) || (it.getValue() != null && it.getValue().toLowerCase().contains(kw)))
                        .toList();
                grid.setItems(filtered);
            }
        });

        content.add(desc, filterText, grid);
        dialog.add(content);

        Button copyAllBtn = new Button("Salin Semua Data", new Icon(VaadinIcon.COPY));
        copyAllBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        copyAllBtn.addClickListener(e -> {
            StringBuilder sb = new StringBuilder();
            for (DebugItem it : allItems) {
                sb.append(it.getKey()).append(" = ").append(it.getValue()).append("\n");
            }
            copyAllBtn.getElement().executeJs("navigator.clipboard.writeText($0)", sb.toString());
            Notification.show("📋 Semua data berhasil disalin ke clipboard!", 2000, Notification.Position.BOTTOM_END);
        });

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(copyAllBtn, closeBtn);

        dialog.open();
    }
}
