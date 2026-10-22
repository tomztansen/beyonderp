package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Tombol +/- di prefix kotak angka (meta_field.show_sign_toggle). Tanda nilai ditentukan tombol;
 * kotak hanya menampilkan angka absolutnya. Listener-nya menempel di tombol milik field itu sendiri,
 * jadi ikut dibuang bersama field -- tidak ada state statis maupun state sesi.
 */
final class SignToggle {
    private final Button button = new Button();
    private boolean negative;

    /** @param onUserFlip dipanggil setelah user menekan tombol, untuk menghitung ulang nilai field. */
    SignToggle(TextField textField, Runnable onUserFlip) {
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        button.getStyle().set("min-width", "1.6em").set("margin", "0").set("padding", "0 2px")
                .set("font-weight", "700");
        button.setTooltipText("Toggle +/-");
        button.addClickListener(e -> {
            negative = !negative;
            render();
            onUserFlip.run();
        });
        // Prefix Rp/$ dari display format tetap tampil, tombol di depannya.
        Component existing = textField.getPrefixComponent();
        textField.setPrefixComponent(existing == null ? button : new Span(button, existing));
        render();
    }

    boolean isNegative() {
        return negative;
    }

    void setNegative(boolean negative) {
        this.negative = negative;
        render();
    }

    void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
    }

    private void render() {
        button.setText(negative ? "−" : "+");
        button.getStyle().set("color",
                negative ? "var(--lumo-error-text-color)" : "var(--lumo-success-text-color)");
    }
}
