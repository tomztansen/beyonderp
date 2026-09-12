package com.vaadinerp.dashboard;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import com.vaadinerp.dashboard.DashboardModel.ItemDef;

/** Kartu widget: judul + stempel waktu + tombol drill, isi, dan baris error dengan Retry. Satu kartu rusak tidak mengganggu yang lain. */
public class DashboardCard extends Div {
    private final Span stamp = new Span();
    private final Div error = new Div();
    private final Button drill = new Button(VaadinIcon.EXPAND_SQUARE.create());
    private final Button retry = new Button("Retry");

    public DashboardCard(ItemDef item, DashboardWidget widget, Runnable onRetry) {
        getStyle().set("background", "#fff").set("border", "1px solid #e5e7eb").set("border-radius", "10px")
                .set("padding", "10px 12px").set("box-shadow", "0 1px 2px rgba(0,0,0,.04)")
                .set("grid-column", "span " + Math.max(1, Math.min(12, item.colSpan())))
                .set("min-width", "0").set("overflow", "hidden");
        Span title = new Span(item.widget().title());
        title.getStyle().set("font-weight", "600").set("color", "#111827");
        stamp.getStyle().set("font-size", "0.75rem").set("color", "#6b7280");
        drill.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        drill.setVisible(false);
        HorizontalLayout head = new HorizontalLayout(title, stamp, drill);
        head.setWidthFull();
        head.setAlignItems(HorizontalLayout.Alignment.CENTER);
        head.expand(title);
        error.getStyle().set("color", "#b91c1c").set("font-size", "0.85rem").set("white-space", "pre-wrap");
        error.setVisible(false);
        retry.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        retry.addClickListener(e -> onRetry.run());
        retry.setVisible(false);
        add(head, widget.asComponent(), error, retry);
    }

    public void showError(String msg) {
        error.setText("⚠ " + msg);
        error.setVisible(true);
        retry.setVisible(true);
        stamp.setText("");
    }

    public void showLoaded() {
        error.setVisible(false);
        retry.setVisible(false);
        stamp.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    public void setDrill(Runnable action) {
        drill.setVisible(action != null);
        if (action != null) drill.addClickListener(e -> action.run());
    }

    public static String rootMessage(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }
}
