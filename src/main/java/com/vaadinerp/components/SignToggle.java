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
 *
 * Mode terkunci negatif (meta_field.sign_lock = 'NEG'): tombol tidak bisa diklik dan angka yang diketik
 * user selalu jadi negatif. Nilai yang dimuat/diisi script tetap ditampilkan apa adanya (tidak diubah diam-diam).
 */
final class SignToggle {
    private final Button button = new Button();
    private final boolean lockedNegative;
    private boolean negative;

    /** @param onUserFlip dipanggil setelah user menekan tombol, untuk menghitung ulang nilai field. */
    SignToggle(TextField textField, Runnable onUserFlip, boolean lockedNegative) {
        this.lockedNegative = lockedNegative;
        this.negative = lockedNegative; // kotak kosong di mode terkunci langsung menunjukkan minus
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        button.getStyle().set("min-width", "1.6em").set("margin", "0").set("padding", "0 2px")
                .set("font-weight", "700");
        if (lockedNegative) {
            button.setTooltipText("Negative only");
            button.setEnabled(false);
        } else {
            button.setTooltipText("Toggle +/-");
            button.addClickListener(e -> {
                negative = !negative;
                render();
                onUserFlip.run();
            });
        }
        // Prefix Rp/$ dari display format tetap tampil, tombol di depannya.
        Component existing = textField.getPrefixComponent();
        textField.setPrefixComponent(existing == null ? button : new Span(button, existing));
        render();
    }

    boolean isNegative() {
        return negative;
    }

    boolean isLockedNegative() {
        return lockedNegative;
    }

    void setNegative(boolean negative) {
        this.negative = negative;
        render();
    }

    /** Ikut status readonly field; tombol mode terkunci tetap nonaktif. */
    void setEnabled(boolean enabled) {
        button.setEnabled(enabled && !lockedNegative);
    }

    /** Kotak dikosongkan (record/baris baru): kembali ke tanda bawaan, minus untuk mode terkunci. */
    void reset() {
        setNegative(lockedNegative);
    }

    /** Dipanggil saat user mengetik angka: di mode terkunci, tanda dipaksa negatif. */
    void onUserInput() {
        if (lockedNegative && !negative) {
            setNegative(true);
        }
    }

    private void render() {
        button.setText(negative ? "−" : "+");
        // Inline style mengalahkan warna abu-abu tombol disabled, jadi tanda terkunci tetap terbaca.
        button.getStyle().set("color",
                negative ? "var(--lumo-error-text-color)" : "var(--lumo-success-text-color)");
    }
}
