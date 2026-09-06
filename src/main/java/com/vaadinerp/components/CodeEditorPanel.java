package com.vaadinerp.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableConsumer;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link CodeEditorField} lengkap dengan toolbar Format dan Check Syntax serta
 * baris status hasil pemeriksaan.
 *
 * Dipakai di dialog-dialog script supaya toolbar yang sama tidak ditulis ulang di
 * tiap tempat. Pemeriksaan hanya tersedia untuk mode Groovy — untuk SQL tombolnya
 * disembunyikan karena tidak ada pemeriksa yang setara.
 */
public class CodeEditorPanel extends VerticalLayout {

    /** Nama bawaan Groovy yang boleh dipanggil tanpa penerima. */
    private static final Set<String> BUILTIN_CALLS = Set.of("println", "print", "printf", "sprintf");

    private final CodeEditorField editor;
    private final Div status = new Div();

    /**
     * Variabel dan fungsi yang tersedia di scope script ini. Kosong berarti
     * pemeriksaan nama dilewati dan hanya struktur yang diperiksa.
     */
    private Set<String> knownNames = Set.of();
    /** Potongan kode untuk scope ini; kosong berarti ComboBox snippet disembunyikan. */
    private java.util.Map<String, String> snippets = java.util.Map.of();

    private final Button cheatBtn = new SafeButton("Cheat Sheet", VaadinIcon.BOOK.create());
    private final com.vaadin.flow.component.combobox.ComboBox<String> snippetCombo =
            new com.vaadin.flow.component.combobox.ComboBox<>();

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

        cheatBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        cheatBtn.addClickListener(e -> openCheatSheet());
        cheatBtn.setVisible(false); // dinyalakan saat daftar nama diberikan

        snippetCombo.setPlaceholder("Insert snippet...");
        snippetCombo.setWidth("260px");
        snippetCombo.setClearButtonVisible(false);
        snippetCombo.setVisible(false);
        snippetCombo.addValueChangeListener(e -> {
            String label = e.getValue();
            if (label != null) {
                editor.appendSnippet(snippets.get(label));
                snippetCombo.clear();
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(checkBtn, formatBtn, cheatBtn, snippetCombo);
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

    /**
     * Editor Groovy yang juga memeriksa nama variabel/fungsi terhadap daftar yang
     * benar-benar tersedia di scope tersebut — lihat konstanta di
     * {@code ScriptExecutorService}.
     */
    public static CodeEditorPanel groovy(Set<String> knownNames) {
        return groovy(knownNames, java.util.Map.of());
    }

    /** Editor Groovy dengan pemeriksa nama, cheat sheet, dan daftar snippet. */
    public static CodeEditorPanel groovy(Set<String> knownNames, java.util.Map<String, String> snippets) {
        CodeEditorPanel panel = new CodeEditorPanel(CodeEditorField.groovy(), true);
        panel.knownNames = knownNames != null ? knownNames : Set.of();
        panel.snippets = snippets != null ? snippets : java.util.Map.of();
        panel.cheatBtn.setVisible(!panel.knownNames.isEmpty());
        panel.snippetCombo.setItems(panel.snippets.keySet());
        panel.snippetCombo.setVisible(!panel.snippets.isEmpty());
        return panel;
    }

    /**
     * Cheat sheet dibangun dari knownNames — daftar yang sama dengan yang dipakai
     * pemeriksa nama, jadi isinya tidak bisa menyimpang dari yang benar-benar
     * tersedia di scope ini.
     */
    private void openCheatSheet() {
        com.vaadin.flow.component.dialog.Dialog dlg = new com.vaadin.flow.component.dialog.Dialog();
        dlg.setHeaderTitle("Available in this script");
        dlg.setWidth("620px");

        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(false);
        body.getStyle().set("gap", "10px");

        for (String name : new java.util.TreeSet<>(knownNames)) {
            Div entry = new Div();
            Div title = new Div();
            title.setText(name);
            title.getStyle().set("font-family", "monospace").set("font-weight", "600")
                    .set("color", "var(--lumo-primary-text-color)");
            entry.add(title);
            String help = GroovyDsl.signature(name);
            if (!help.isEmpty()) {
                Div desc = new Div();
                desc.setText(help);
                desc.getStyle().set("font-size", "var(--lumo-font-size-s)")
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("white-space", "pre-wrap");
                entry.add(desc);
            }
            body.add(entry);
        }

        dlg.add(body);
        dlg.getFooter().add(new SafeButton("Close", e -> dlg.close()));
        dlg.open();
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
            CompilationUnit cu = new CompilationUnit();
            cu.addSource("SyntaxCheck", scriptText);
            cu.compile(Phases.SEMANTIC_ANALYSIS);

            List<String> unknown = findUnknownNames(cu, knownNames);
            if (unknown.isEmpty()) {
                // Sengaja tidak menjanjikan "script benar": Groovy menyelesaikan nama saat
                // runtime, jadi yang bisa dipastikan di sini hanya struktur dan nama.
                showStatus("✅ Structure is valid and all names are recognized.",
                        "#dcfce7", "#166534", "#22c55e");
            } else {
                showStatus("⚠️ Structure is valid, but these names are not available in this scope:\n"
                        + String.join(", ", unknown)
                        + "\nCheck the spelling. Groovy resolves names only at runtime, "
                        + "so this would fail when the script runs.",
                        "#fef3c7", "#92400e", "#f59e0b");
            }
        } catch (Exception ex) {
            // Pesan Groovy sudah menyebut nomor baris dan kolom, jadi tampilkan apa adanya.
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
            showStatus("❌ Syntax error:\n" + msg, "#fee2e2", "#991b1b", "#ef4444");
        }
    }

    /**
     * Telusuri AST dua kali: pertama kumpulkan nama yang dideklarasikan sendiri oleh
     * script, lalu cocokkan pemakaian terhadap daftar itu ditambah {@code knownNames}.
     *
     * Hasilnya peringatan, bukan error — Groovy sah memakai nama dinamis, jadi daftar
     * nama yang dikenal bisa saja belum lengkap untuk pemakaian yang tidak biasa.
     */
    static List<String> findUnknownNames(CompilationUnit cu, Set<String> knownNames) {
        if (knownNames.isEmpty() || cu.getAST() == null) {
            return List.of();
        }
        Set<String> declared = new HashSet<>();
        declared.add("it"); // parameter implisit closure
        Set<String> unknown = new LinkedHashSet<>();

        CodeVisitorSupport collector = new CodeVisitorSupport() {
            @Override
            public void visitDeclarationExpression(DeclarationExpression expr) {
                if (expr.getLeftExpression() instanceof VariableExpression v) {
                    declared.add(v.getName());
                }
                super.visitDeclarationExpression(expr);
            }

            @Override
            public void visitBinaryExpression(BinaryExpression expr) {
                // "x = 5" tanpa def tetap membuat variabel baru di binding.
                if ("=".equals(expr.getOperation().getText())
                        && expr.getLeftExpression() instanceof VariableExpression v) {
                    declared.add(v.getName());
                }
                super.visitBinaryExpression(expr);
            }

            @Override
            public void visitClosureExpression(ClosureExpression expr) {
                if (expr.getParameters() != null) {
                    for (Parameter param : expr.getParameters()) {
                        declared.add(param.getName());
                    }
                }
                super.visitClosureExpression(expr);
            }

            @Override
            public void visitForLoop(ForStatement stmt) {
                if (stmt.getVariable() != null) {
                    declared.add(stmt.getVariable().getName());
                }
                super.visitForLoop(stmt);
            }

            @Override
            public void visitCatchStatement(CatchStatement stmt) {
                if (stmt.getVariable() != null) {
                    declared.add(stmt.getVariable().getName());
                }
                super.visitCatchStatement(stmt);
            }
        };

        CodeVisitorSupport checker = new CodeVisitorSupport() {
            @Override
            public void visitVariableExpression(VariableExpression expr) {
                String name = expr.getName();
                if (!"this".equals(name) && !"super".equals(name)
                        && !declared.contains(name) && !knownNames.contains(name)) {
                    unknown.add(name);
                }
                super.visitVariableExpression(expr);
            }

            @Override
            public void visitMethodCallExpression(MethodCallExpression call) {
                // Hanya panggilan tanpa penerima. "db.find(...)" cukup diperiksa lewat "db"
                // karena isi DatabaseHelper bukan urusan pemeriksa nama.
                if (call.isImplicitThis()) {
                    String method = call.getMethodAsString();
                    if (method != null && !declared.contains(method) && !knownNames.contains(method)
                            && !BUILTIN_CALLS.contains(method)) {
                        unknown.add(method + "()");
                    }
                }
                super.visitMethodCallExpression(call);
            }
        };

        for (ModuleNode module : cu.getAST().getModules()) {
            if (module.getStatementBlock() == null) {
                continue;
            }
            module.getStatementBlock().visit(collector);
            module.getStatementBlock().visit(checker);
        }
        return new ArrayList<>(unknown);
    }

    private void showStatus(String text, String bg, String fg, String border) {
        status.getStyle().set("display", "block").set("background-color", bg).set("color", fg)
                .set("border", "1px solid " + border);
        status.setText(text);
    }
}
