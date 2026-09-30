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
        // CodeEditorField default mengisi tinggi induk (untuk dialog); di form biasa perlu tinggi tetap.
        setHeight("360px");
        panel.setHeightFull();
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
