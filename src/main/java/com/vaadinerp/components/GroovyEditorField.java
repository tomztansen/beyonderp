package com.vaadinerp.components;

import com.vaadin.flow.component.customfield.CustomField;
import java.util.Map;
import java.util.Set;

/**
 * Field form berisi editor Groovy (CodeMirror + toolbar Format/Check Syntax/Cheat Sheet/Snippet).
 * Nilai hidup di browser; CodeEditorField mengirimnya ke server saat blur / jeda ketik, dan field ini
 * menyimpannya sebagai String sehingga Binder mendapat nilai sinkron saat Save.
 */
public class GroovyEditorField extends CustomField<String> {

    private static final String EDITOR_HEIGHT = "360px";

    private final CodeEditorPanel panel;
    private String current = "";

    public GroovyEditorField(String label, Set<String> knownNames, Map<String, String> snippets) {
        // Default "" (bukan null) supaya asRequired Binder menganggap "" kosong; manualValueUpdate=true
        // supaya event "change" DOM dari dalam tidak memicu updateValue() -- hanya callback editor di bawah.
        super("", true);
        if (label != null && !label.isBlank()) {
            setLabel(label);
        }
        panel = CodeEditorPanel.groovy(knownNames, snippets);
        panel.getEditor().setOnValueChanged(v -> {
            current = v;
            // updateValue() = setModelValue(generateModelValue(), true): event bertanda fromClient,
            // sama seperti pola QrScanField, jadi Binder ikut terbarui.
            updateValue();
        });
        setWidthFull();
        // CodeEditorField mengisi tinggi induknya (flex 1, min-height 0, overflow hidden) dan itu cocok untuk
        // dialog. Di sini induknya slot CustomField yang tingginya auto: "100%" tidak punya acuan sehingga
        // panel menciut setinggi toolbar dan editor CodeMirror terpotong jadi 0 px. Beri tinggi px eksplisit
        // pada panel (bukan pada host) supaya flex editor punya ruang yang pasti.
        panel.setHeight(EDITOR_HEIGHT);
        // Bingkai seperti field Lumo lain; hanya untuk field form ini (dialog script tetap tanpa bingkai).
        panel.getEditor().getStyle()
                .set("border", "1px solid var(--lumo-contrast-30pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-sizing", "border-box")
                // Hasil diagnosis: panel selebar form (376 px) tetapi host editor hanya 81 px, jadi host tidak
                // meregang di dalam layout vertikal. Lebar dipaksa penuh dan boleh menyusut (min-width 0).
                .set("width", "100%")
                .set("align-self", "stretch")
                .set("min-width", "0");
        add(panel);
    }

    /** Bukan super.setReadOnly(): lihat catatan di QrScanField.setReadOnly (compiler Eclipse). */
    @Override
    public void setReadOnly(boolean readOnly) {
        getElement().setProperty("readonly", readOnly);
        panel.setReadOnly(readOnly);
        panel.setToolbarEnabled(!readOnly);
    }

    @Override
    protected String generateModelValue() {
        return current;
    }

    @Override
    protected void setPresentationValue(String value) {
        current = value != null ? value : "";
        panel.setValue(current);
    }
}
