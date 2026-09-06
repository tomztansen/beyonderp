package com.vaadinerp.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableConsumer;

/**
 * {@link CodeEditorField} lengkap dengan toolbar Format dan Check Syntax serta
 * baris status hasil pemeriksaan.
 *
 * Dipakai di dialog-dialog script supaya toolbar yang sama tidak ditulis ulang di
 * tiap tempat. Pemeriksaan sintaks hanya tersedia untuk mode Groovy — untuk SQL
 * tombolnya disembunyikan karena tidak ada pemeriksa yang setara.
 */
public class CodeEditorPanel extends VerticalLayout {

    private final CodeEditorField editor;
    private final Div status = new Div();

    private CodeEditorPanel(CodeEditorField editor, boolean groovy) {
        this.editor = editor;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "6px");

        Button formatBtn = new SafeButton("Format", VaadinIcon.ALIGN_LEFT.create(), e -> editor.format());
        formatBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Button checkBtn = new SafeButton("Check Syntax", VaadinIcon.CHECK_CIRCLE.create(),
                e -> editor.getValue(this::checkGroovySyntax));
        checkBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        checkBtn.setVisible(groovy);

        HorizontalLayout toolbar = new HorizontalLayout(checkBtn, formatBtn);
        toolbar.setWidthFull();
        toolbar.setSpacing(false);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.getStyle().set("gap", "6px").set("flex-shrink", "0");

        status.setWidthFull();
        status.getStyle().set("padding", "8px 12px").set("border-radius", "6px").set("font-weight", "500")
                .set("display", "none").set("white-space", "pre-wrap").set("flex-shrink", "0");

        add(toolbar, editor, status);
        setFlexGrow(1, editor);
    }

    public static CodeEditorPanel groovy() {
        return new CodeEditorPanel(CodeEditorField.groovy(), true);
    }

    public static CodeEditorPanel sql() {
        return new CodeEditorPanel(CodeEditorField.sql(), false);
    }

    public void setValue(String value) {
        editor.setValue(value);
    }

    /** Asinkron — isi editor hidup di browser, lihat {@link CodeEditorField#getValue}. */
    public void getValue(SerializableConsumer<String> callback) {
        editor.getValue(callback);
    }

    public void setReadOnly(boolean readOnly) {
        editor.setReadOnly(readOnly);
    }

    public void insertAtCursor(String text) {
        editor.insertAtCursor(text);
    }

    public void appendSnippet(String snippet) {
        editor.appendSnippet(snippet);
    }

    public CodeEditorField getEditor() {
        return editor;
    }

    private void checkGroovySyntax(String scriptText) {
        if (scriptText == null || scriptText.isBlank()) {
            showStatus("⚠️ The script is still empty.", "#fef3c7", "#92400e", "#f59e0b");
            return;
        }
        try {
            org.codehaus.groovy.control.CompilationUnit cu = new org.codehaus.groovy.control.CompilationUnit();
            cu.addSource("SyntaxCheck", scriptText);
            cu.compile(org.codehaus.groovy.control.Phases.SEMANTIC_ANALYSIS);
            showStatus("✅ Groovy syntax is valid - no syntax or structural errors.",
                    "#dcfce7", "#166534", "#22c55e");
        } catch (Exception ex) {
            // Pesan Groovy sudah menyebut nomor baris dan kolom, jadi tampilkan apa adanya.
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
            showStatus("❌ Syntax error:\n" + msg, "#fee2e2", "#991b1b", "#ef4444");
        }
    }

    private void showStatus(String text, String bg, String fg, String border) {
        status.getStyle().set("display", "block").set("background-color", bg).set("color", fg)
                .set("border", "1px solid " + border);
        status.setText(text);
    }
}
