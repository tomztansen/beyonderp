package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.views.FormBuilderView.FieldMetaTemp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tiga dialog editor script yang tadinya ada di FormBuilderView: script level form
 * (ON_LOAD series, BEFORE_SAVE, AFTER_SAVE), ON_CHANGE per field, dan On-Add-Row
 * + AI Assistant untuk baris subform. Dipisah ke sini supaya FormBuilderView
 * tidak makin membengkak -- lihat pembahasan pemecahan file FormBuilderView.
 */
public final class ScriptEditorDialog {

    private ScriptEditorDialog() {
    }

    private static String formScopeHelp(String scope) {
        if (scope == null)
            return "";
        switch (scope.toUpperCase()) {
            case "ON_LOAD_NEW":
                return "Dijalankan saat tombol New ditekan. Cocok untuk mengisi nilai default record baru.";
            case "ON_LOAD_EDIT":
                return "Dijalankan saat record dibuka lewat tombol Edit, setelah readonly dikembalikan ke metadata.";
            case "ON_LOAD_VIEW":
                return "Dijalankan saat record dibuka dalam mode View. Perhatikan: script berjalan SETELAH semua "
                        + "field dikunci, jadi setElementReadonly(false) di sini akan membuka kunci mode lihat-saja.";
            case "BEFORE_SAVE":
                return "Dijalankan sebelum data disimpan. Script yang mengembalikan false akan MEMBATALKAN "
                        + "penyimpanan — dipakai untuk validasi.";
            case "AFTER_SAVE":
                return "Dijalankan setelah data tersimpan. Form langsung dikosongkan sesudahnya, jadi jangan "
                        + "dipakai untuk mengatur tampilan field.";
            case "ON_DETAIL_ADD":
                return "Dijalankan saat baris detail ditambahkan. Hanya berlaku untuk form MASTER_DETAIL; "
                        + "gunakan variabel row untuk baris yang baru dibuat.";
            default:
                return "";
        }
    }

    /**
     * action_code tidak punya unique constraint di DB, tapi findByActionCode
     * mengembalikan satu objek — duplikat akan meledak saat runtime. Jadi keunikan
     * dijamin di sini.
     */
    private static String generateUniqueActionCode(com.vaadinerp.meta.FormActionMetaRepository actionRepo,
            String rawBase) {
        String base = rawBase.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        if (base.length() > 50) {
            base = base.substring(0, 50);
        }
        String candidate = base;
        int suffix = 2;
        while (actionRepo.findByActionCodeIgnoreCase(candidate) != null) {
            String tail = "_" + suffix;
            candidate = (base.length() + tail.length() > 50 ? base.substring(0, 50 - tail.length()) : base) + tail;
            suffix++;
        }
        return candidate;
    }

    /**
     * Editor script level form untuk scope On-Load dan Save. Toolbar (MASTER_TOOLBAR
     * / DETAIL_TOOLBAR) sengaja tidak ditangani di sini karena butuh konfigurasi
     * tombol lengkap — tetap di Form Action Builder.
     */
    public static void openFormScript(String formCode, String formType, DynamicDataService dynamicDataService) {
        if (formCode == null || formCode.trim().isEmpty()) {
            Notification.show("Isi Form Code terlebih dahulu.", 3000, Notification.Position.MIDDLE);
            return;
        }
        formCode = formCode.trim();
        FormMeta targetForm = dynamicDataService.getFormMetaRepository().findById(formCode).orElse(null);
        if (targetForm == null) {
            Notification.show("Save this form first before configuring Form Scripts.", 5000,
                    Notification.Position.MIDDLE);
            return;
        }
        com.vaadinerp.meta.FormActionMetaRepository actionRepo = dynamicDataService != null
                ? dynamicDataService.getFormActionMetaRepository()
                : null;
        if (actionRepo == null) {
            Notification.show("Action repository is not available.", 3000, Notification.Position.MIDDLE);
            return;
        }
        final String formCodeFinal = formCode;

        List<String> scopes = new ArrayList<>(
                Arrays.asList("ON_LOAD_NEW", "ON_LOAD_EDIT", "ON_LOAD_VIEW", "BEFORE_SAVE", "AFTER_SAVE"));
        if ("MASTER_DETAIL".equals(formType)) {
            scopes.add("ON_DETAIL_ADD");
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚡ Script Form - " + formCode);
        dialog.setWidth("860px");
        dialog.setHeight("660px");

        ComboBox<String> scopeCombo = new ComboBox<>("Scope");
        scopeCombo.setItems(scopes);
        scopeCombo.setWidthFull();
        scopeCombo.setClearButtonVisible(false);

        Span scopeHelp = new Span();
        scopeHelp.getStyle().set("font-size", "0.85em").set("color", "var(--lumo-secondary-text-color)");

        CodeEditorPanel scriptArea = CodeEditorPanel.groovy(
                com.vaadinerp.service.ScriptExecutorService.ACTION_SCRIPT_NAMES,
                GroovyDsl.actionSnippets());
        scriptArea.setWidthFull();
        scriptArea.setHeight("340px");

        // Baris yang sedang diedit + isi terakhir yang dimuat, untuk deteksi perubahan
        // yang belum disimpan saat user berpindah scope.
        final com.vaadinerp.meta.FormActionMeta[] currentRow = new com.vaadinerp.meta.FormActionMeta[1];
        final String[] loadedScript = new String[] { "" };
        final boolean[] locked = new boolean[] { false };

        Runnable loadScope = () -> {
            String scope = scopeCombo.getValue();
            scopeHelp.setText(formScopeHelp(scope));
            currentRow[0] = null;
            locked[0] = false;
            if (scope == null) {
                scriptArea.setValue("");
                loadedScript[0] = "";
                return;
            }
            List<com.vaadinerp.meta.FormActionMeta> rows = actionRepo
                    .findByFormMeta_FormCodeAndTargetScope(formCodeFinal, scope);
            if (rows == null)
                rows = new ArrayList<>();
            if (rows.size() > 1) {
                // Jangan menebak baris mana yang dimaksud — data lama bisa punya beberapa
                // action untuk satu scope.
                locked[0] = true;
                scriptArea.setValue("");
                scriptArea.setReadOnly(true);
                loadedScript[0] = "";
                Notification.show("Scope " + scope + " punya " + rows.size()
                        + " action(s) on this form. Edit them in the Form Action Builder to avoid hitting the wrong target.",
                        6000, Notification.Position.MIDDLE);
                return;
            }
            scriptArea.setReadOnly(false);
            if (rows.isEmpty()) {
                scriptArea.setValue("");
                loadedScript[0] = "";
                return;
            }
            com.vaadinerp.meta.FormActionMeta row = rows.get(0);
            if (row.getActionType() != null && !"GROOVY_SCRIPT".equalsIgnoreCase(row.getActionType())) {
                // Baris POPUP_PICKER tidak boleh diubah jadi script dari sini.
                locked[0] = true;
                scriptArea.setValue("");
                scriptArea.setReadOnly(true);
                loadedScript[0] = "";
                Notification.show("Action '" + row.getActionCode() + "' pada scope " + scope + " bertipe "
                        + row.getActionType() + ", bukan GROOVY_SCRIPT. Edit lewat Form Action Builder.", 6000,
                        Notification.Position.MIDDLE);
                return;
            }
            currentRow[0] = row;
            loadedScript[0] = row.getScriptContent() != null ? row.getScriptContent() : "";
            scriptArea.setValue(loadedScript[0]);
        };

        // Isi editor ada di browser, jadi pembacaannya asinkron: peringatan perubahan
        // yang belum disimpan baru bisa dinilai setelah nilainya sampai ke server.
        scopeCombo.addValueChangeListener(e -> scriptArea.getValue(pending -> {
            if (!locked[0] && pending != null && !pending.equals(loadedScript[0])) {
                Notification.show("Unsaved changes discarded.", 3000, Notification.Position.MIDDLE);
            }
            loadScope.run();
        }));

        VerticalLayout layout = new VerticalLayout(scopeCombo, scopeHelp, scriptArea);
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);
        dialog.add(layout);

        Button saveBtn = new SafeButton("Save", VaadinIcon.CHECK.create(), ev -> {
            String scope = scopeCombo.getValue();
            if (scope == null) {
                Notification.show("Select a scope first.", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (locked[0]) {
                Notification.show("This scope is locked here. Use the Form Action Builder.", 4000,
                        Notification.Position.MIDDLE);
                return;
            }
            scriptArea.getValue(raw -> {
                String script = raw != null ? raw.trim() : "";
                try {
                    if (script.isEmpty()) {
                        if (currentRow[0] != null) {
                            actionRepo.delete(currentRow[0]);
                            Notification.show("Script " + scope + " deleted.", 3000, Notification.Position.MIDDLE);
                        }
                        loadScope.run();
                        return;
                    }
                    com.vaadinerp.meta.FormActionMeta target = currentRow[0];
                    if (target == null) {
                        target = new com.vaadinerp.meta.FormActionMeta();
                        target.setFormMeta(targetForm);
                        target.setActionCode(generateUniqueActionCode(actionRepo, scope + "_" + formCodeFinal));
                        target.setActionLabel("-");
                        target.setActionType("GROOVY_SCRIPT");
                        target.setTargetScope(scope);
                    }
                    // Kolom lain (icon, style, menu group, mapping) sengaja tidak disentuh supaya
                    // baris lama yang dibuat lewat Form Action Builder tetap utuh.
                    target.setScriptContent(script);
                    actionRepo.save(target);
                    Notification.show("Script " + scope + " saved.", 3000, Notification.Position.MIDDLE);
                    loadScope.run();
                } catch (Exception ex) {
                    Notification.show(
                            "Failed to save: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()),
                            5000, Notification.Position.MIDDLE);
                }
            });
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeBtn = new SafeButton("Close", ev -> dialog.close());
        dialog.getFooter().add(closeBtn, saveBtn);

        scopeCombo.setValue("ON_LOAD_EDIT");
        loadScope.run();
        dialog.open();
    }

    /**
     * Editor script ON_CHANGE untuk satu field. Berbeda dengan Configure Filters /
     * LOV Targets yang menyimpan ke objek temp dan baru masuk DB saat Save Form,
     * dialog ini menulis langsung ke meta_form_action karena FormMeta tidak punya
     * relasi cascade ke FormActionMeta.
     */
    public static void openOnChangeScript(FieldMetaTemp fieldTemp, String formCode,
            DynamicDataService dynamicDataService) {
        if (fieldTemp == null || fieldTemp.fieldName == null || fieldTemp.fieldName.trim().isEmpty()) {
            Notification.show("Select a field first.", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (formCode == null || formCode.trim().isEmpty()) {
            Notification.show("Isi Form Code terlebih dahulu.", 3000, Notification.Position.MIDDLE);
            return;
        }
        formCode = formCode.trim();
        // meta_form_action.form_code punya FK ke meta_form, jadi form wajib sudah
        // tersimpan sebelum action bisa dibuat.
        FormMeta targetForm = dynamicDataService.getFormMetaRepository().findById(formCode).orElse(null);
        if (targetForm == null) {
            Notification.show("Save this form first before configuring the On-Change Script.", 5000,
                    Notification.Position.MIDDLE);
            return;
        }
        com.vaadinerp.meta.FormActionMetaRepository actionRepo = dynamicDataService != null
                ? dynamicDataService.getFormActionMetaRepository()
                : null;
        if (actionRepo == null) {
            Notification.show("Action repository is not available.", 3000, Notification.Position.MIDDLE);
            return;
        }
        final String formCodeFinal = formCode;

        final String fieldName = fieldTemp.fieldName.trim();
        com.vaadinerp.meta.FormActionMeta existing = null;
        for (com.vaadinerp.meta.FormActionMeta a : actionRepo.findByFormMeta_FormCodeAndTargetScope(formCodeFinal,
                "ON_CHANGE")) {
            if (a.getTriggerField() != null && fieldName.equalsIgnoreCase(a.getTriggerField().trim())) {
                existing = a;
                break;
            }
        }
        final com.vaadinerp.meta.FormActionMeta action = existing;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚡ On-Change Script - " + fieldName);
        dialog.setWidth("820px");
        dialog.setHeight("620px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);

        Span info = new Span("Script dijalankan saat user mengubah field \"" + fieldName
                + "\". Changes made by the system (formula, LOV target fill, setElementValue) do not trigger it. "
                + "Tersedia: setElementReadonly, setElementEnabled, setElementDisabled, setElementValue, "
                + "getElementValue, refreshForm, clearForm, db.getValue, db.find, executeProcedure, msgBox, "
                + "showSuccess, showError, header.*. "
                + "Kalau form ini dipakai sebagai child form di Subform Grid, script yang sama jalan per baris "
                + "saat sel diedit — di sana tersedia row.*, rowIndex, items, dan perubahan row.* langsung "
                + "tersalin ke editor grid.");
        info.getStyle().set("font-size", "0.85em").set("color", "var(--lumo-secondary-text-color)");

        Span warn = new Span("Jangan setElementValue ke field yang punya Formula — nilainya akan ditimpa "
                + "perhitungan ulang. Isi field inputnya, biarkan formula yang menghitung.");
        warn.getStyle().set("font-size", "0.85em").set("color", "var(--lumo-error-text-color)");

        CodeEditorPanel scriptArea = CodeEditorPanel.groovy(
                com.vaadinerp.service.ScriptExecutorService.ACTION_SCRIPT_NAMES,
                GroovyDsl.actionSnippets());
        scriptArea.setWidthFull();
        scriptArea.setHeight("330px");
        scriptArea.setValue(action != null && action.getScriptContent() != null ? action.getScriptContent() : "");

        layout.add(info, warn, scriptArea);
        dialog.add(layout);

        Button saveBtn = new SafeButton("Save", VaadinIcon.CHECK.create(), ev -> {
            scriptArea.getValue(raw -> {
                String script = raw != null ? raw.trim() : "";
                try {
                    if (script.isEmpty()) {
                        if (action != null) {
                            actionRepo.delete(action);
                            Notification.show("On-Change script deleted.", 3000, Notification.Position.MIDDLE);
                        }
                        dialog.close();
                        return;
                    }
                    com.vaadinerp.meta.FormActionMeta target = action;
                    if (target == null) {
                        target = new com.vaadinerp.meta.FormActionMeta();
                        target.setFormMeta(targetForm);
                        target.setActionCode(
                                generateUniqueActionCode(actionRepo, "ONCHG_" + formCodeFinal + "_" + fieldName));
                        target.setActionLabel("-");
                        target.setActionType("GROOVY_SCRIPT");
                        target.setTargetScope("ON_CHANGE");
                        target.setTriggerField(fieldName);
                    }
                    target.setScriptContent(script);
                    actionRepo.save(target);
                    Notification.show("On-Change script saved.", 3000, Notification.Position.MIDDLE);
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show(
                            "Failed to save: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()),
                            5000, Notification.Position.MIDDLE);
                }
            });
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new SafeButton("Cancel", ev -> dialog.close());
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    /** Editor On-Add-Row Script + Asisten AI untuk baris subform. */
    public static void openOnAddScript(FieldMetaTemp fieldTemp, List<FieldMetaTemp> fieldsList,
            DynamicDataService dynamicDataService) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚡ On-Add-Row Script & AI Assistant - " + fieldTemp.fieldName);
        dialog.setWidth("880px");
        dialog.setHeight("720px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setPadding(false);

        // 1. AI Assistant Panel
        com.vaadin.flow.component.details.Details aiDetails = new com.vaadin.flow.component.details.Details(
                "✨ Asisten AI (Bahasa Manusia ke Groovy Script)");
        aiDetails.setOpened(true);
        aiDetails.setWidthFull();
        aiDetails.getStyle().set("background", "var(--lumo-contrast-5pct)").set("padding", "10px")
                .set("border-radius", "8px");

        VerticalLayout aiLayout = new VerticalLayout();
        aiLayout.setPadding(false);
        aiLayout.setSpacing(true);
        Span aiHelp = new Span(
                "Ketik instruksi aturan dalam bahasa Indonesia (misal: 'baris pertama status centang, lainnya false' atau 'baris 1 sampai 3 aktif'):");
        aiHelp.getStyle().set("font-size", "0.85em").set("color", "var(--lumo-secondary-text-color)");

        TextField aiInput = new TextField();
        aiInput.setPlaceholder("Contoh: jika baris pertama maka status = true, baris kedua dst false...");
        aiInput.setWidthFull();

        Button btnGenerateAi = new SafeButton("✨ Buatkan Aturan (AI)", VaadinIcon.LIGHTBULB.create());
        btnGenerateAi.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS,
                ButtonVariant.LUMO_SMALL);

        aiLayout.add(aiHelp, aiInput, btnGenerateAi);
        aiDetails.add(aiLayout);

        // 2. Quick Snippets & Variable Pickers
        HorizontalLayout pickersLayout = new HorizontalLayout();
        pickersLayout.setWidthFull();
        pickersLayout.setSpacing(true);

        ComboBox<String> templatePicker = new ComboBox<>("💡 Sisipkan Template");
        templatePicker.setItems(
                "Baris Pertama Centang (if rowIndex == 1)",
                "Baris 1 s/d 3 Centang (if rowIndex <= 3)",
                "Ambil dari Form Header (row.field = header.field)",
                "Lookup Tabel Lain (db.find)",
                "Kalkulasi Matematika (row.total = row.qty * row.price)");
        templatePicker.setWidth("36%");

        ComboBox<String> rowVarPicker = new ComboBox<>("📄 Columns of this row (row)");
        List<String> childCols = new ArrayList<>();
        if (fieldTemp.lovCode != null && !fieldTemp.lovCode.trim().isEmpty()) {
            FormMeta childForm = dynamicDataService.getFormMetaRepository().findById(fieldTemp.lovCode).orElse(null);
            if (childForm != null && childForm.getFields() != null) {
                for (com.vaadinerp.meta.FieldMeta fm : childForm.getFields()) {
                    childCols.add("row." + fm.getFieldName());
                }
            }
        }
        if (childCols.isEmpty()) {
            childCols.addAll(Arrays.asList("row.status", "row.qty", "row.price", "row.item_code", "row.description"));
        }
        rowVarPicker.setItems(childCols);
        rowVarPicker.setWidth("34%");

        ComboBox<String> headerVarPicker = new ComboBox<>("🏢 Header columns (header)");
        List<String> headerCols = new ArrayList<>();
        for (FieldMetaTemp fm : fieldsList) {
            if (!"SUBFORM_GRID".equalsIgnoreCase(fm.componentType)) {
                headerCols.add("header." + fm.fieldName);
            }
        }
        if (headerCols.isEmpty()) {
            headerCols.addAll(Arrays.asList("header.qty", "header.date", "header.customer_id", "header.order_no"));
        }
        headerVarPicker.setItems(headerCols);
        headerVarPicker.setWidth("30%");

        pickersLayout.add(templatePicker, rowVarPicker, headerVarPicker);

        // 3. Code Editor Area
        CodeEditorPanel scriptArea = CodeEditorPanel.groovy(
                com.vaadinerp.service.ScriptExecutorService.ROW_SCRIPT_NAMES,
                GroovyDsl.rowSnippets());
        scriptArea.setValue(fieldTemp.onAddScript != null ? fieldTemp.onAddScript : "");
        scriptArea.setWidthFull();
        scriptArea.setHeight("260px");

        // Picker listeners to insert text into scriptArea
        templatePicker.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val == null)
                return;
            String snippet = "";
            if (val.startsWith("Baris Pertama")) {
                snippet = "// Aturan: Baris pertama aktif (true), baris kedua dst false\nif (rowIndex == 1) {\n    row.status = true\n} else {\n    row.status = false\n}\n";
            } else if (val.startsWith("Baris 1 s/d 3")) {
                snippet = "// Aturan: Baris 1 sampai 3 aktif (true), baris selanjutnya false\nif (rowIndex <= 3) {\n    row.status = true\n} else {\n    row.status = false\n}\n";
            } else if (val.startsWith("Ambil dari Form Header")) {
                snippet = "// Mengambil nilai dari kolom di header/master form\nrow.perseries = header.qty != null ? header.qty : 1\n";
            } else if (val.startsWith("Lookup Tabel Lain")) {
                snippet = "// Lookup data dari tabel lain di database\ndef item = db.find('lov_item', 'item_code', row.item_code)\nif (item != null) {\n    row.price = item.default_price\n}\n";
            } else if (val.startsWith("Kalkulasi Matematika")) {
                snippet = "// Kalkulasi antar kolom di baris yang sama\nrow.total = (row.qty != null ? row.qty : 0) * (row.price != null ? row.price : 0)\n";
            }
            scriptArea.appendSnippet(snippet);
            templatePicker.clear();
        });

        rowVarPicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                scriptArea.insertAtCursor(e.getValue());
                rowVarPicker.clear();
            }
        });

        headerVarPicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                scriptArea.insertAtCursor(e.getValue());
                headerVarPicker.clear();
            }
        });

        // AI Generator logic (Ollama Integration)
        btnGenerateAi.addClickListener(e -> {
            String prompt = aiInput.getValue().trim();
            if (prompt.isEmpty()) {
                Notification.show("Type an instruction for the AI first!", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Siapkan UI untuk loading state
            btnGenerateAi.setEnabled(false);
            btnGenerateAi.setText("⏳ Memikirkan...");

            // Menggunakan Java Reflection untuk membaca fungsi db.* secara dinamis
            StringBuilder dbFunctions = new StringBuilder();
            try {
                for (java.lang.reflect.Method m : com.vaadinerp.service.ScriptExecutorService.DatabaseHelper.class
                        .getDeclaredMethods()) {
                    if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) && !m.getName().startsWith("get")) {
                        dbFunctions.append("- db.").append(m.getName()).append("(");
                        java.lang.reflect.Parameter[] params = m.getParameters();
                        for (int i = 0; i < params.length; i++) {
                            dbFunctions.append(params[i].getType().getSimpleName()).append(" ")
                                    .append(params[i].getName());
                            if (i < params.length - 1)
                                dbFunctions.append(", ");
                        }
                        dbFunctions.append(") mengembalikan ").append(m.getReturnType().getSimpleName()).append("\n");
                    }
                }
            } catch (Exception ex) {
                dbFunctions.append(
                        "- db.find(String tableName, String keyColumn, Object keyValue)\n- db.getValue(String sql, Object[] args)\n");
            }

            // Variabel/fungsi yang tersedia di scope ini -- dibaca dari
            // ScriptExecutorService.ROW_SCRIPT_NAMES (bukan ditulis manual), jadi kalau
            // ada closure baru ditambahkan ke binding, prompt AI otomatis ikut tahu
            // setelah restart, tanpa perlu menyentuh kode ini lagi.
            StringBuilder dslHelp = new StringBuilder();
            for (String name : com.vaadinerp.service.ScriptExecutorService.ROW_SCRIPT_NAMES) {
                String desc = GroovyDsl.signature(name);
                dslHelp.append("- ").append(name);
                if (!desc.isEmpty()) {
                    dslHelp.append(": ").append(desc.replace("\n", " "));
                }
                dslHelp.append("\n");
            }

            // Siapkan konteks (System Prompt)
            String sysPrompt = "Kamu adalah asisten ahli pembuat Groovy Script untuk ERP.\n" +
                    "Aturan wajib:\n" +
                    "1. Jika instruksi user relevan dengan logika kode, balas HANYA dengan kode Groovy murni. Tanpa penjelasan, tanpa markdown (```).\n"
                    +
                    "2. Jika instruksi user TIDAK relevan (sekadar bertanya/mengobrol di luar kode), berikan jawaban dalam bahasa Indonesia, tetapi WAJIB awali setiap baris jawaban dengan komentar ganda (//) agar tidak memicu error sintaks.\n"
                    +
                    "Variabel dan fungsi yang tersedia di scope ini:\n" + dslHelp +
                    "Fungsi database dinamis (terbaca dari Java Reflection):\n" + dbFunctions.toString() +
                    "Valid child/row columns: " + String.join(", ", childCols) + "\n" +
                    "Valid header columns: " + String.join(", ", headerCols);

            // Jalankan Asynchronous agar UI tidak freeze
            com.vaadin.flow.component.UI ui = e.getSource().getUI().orElse(com.vaadin.flow.component.UI.getCurrent());

            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    // Model dibaca dari properties (ai.ollama.model), bukan hardcode -- ganti model
                    // tinggal edit application-prod.properties + restart, tanpa build ulang JAR.
                    org.springframework.core.env.Environment env = com.vaadinerp.config.SpringContextHolder
                            .getBean(org.springframework.core.env.Environment.class);
                    String aiModel = env != null ? env.getProperty("ai.ollama.model", "qwen2.5-coder:14b")
                            : "qwen2.5-coder:14b";
                    Map<String, Object> payloadMap = new HashMap<>();
                    payloadMap.put("model", aiModel);
                    payloadMap.put("system", sysPrompt);
                    payloadMap.put("prompt", prompt);
                    payloadMap.put("stream", false);

                    String jsonPayload = mapper.writeValueAsString(payloadMap);

                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://172.16.0.63:11434/api/generate"))
                            .header("Content-Type", "application/json")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                            .timeout(java.time.Duration.ofSeconds(60)) // Proses AI bisa butuh waktu agak lama
                            .build();

                    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                            .connectTimeout(java.time.Duration.ofSeconds(10))
                            .build();

                    java.net.http.HttpResponse<String> response = client.send(request,
                            java.net.http.HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.body());
                        String aiResponse = rootNode.path("response").asText();

                        // Bersihkan markdown ```groovy jika AI membandel
                        aiResponse = aiResponse.replaceAll("(?s)^```[a-zA-Z]*\\n?", "").replaceAll("(?s)\\n?```$", "")
                                .trim();

                        final String finalCode = "// ✨ Di-generate oleh Ollama (" + aiModel + ") dari perintah: \""
                                + prompt + "\"\n" + aiResponse;

                        ui.access(() -> {
                            scriptArea.appendSnippet(finalCode);
                            btnGenerateAi.setEnabled(true);
                            btnGenerateAi.setText("✨ Buatkan Aturan (AI)");
                            Notification.show("✅ Generated from Ollama!", 3000,
                                    Notification.Position.BOTTOM_END);
                            aiInput.clear();
                        });
                    } else {
                        ui.access(() -> {
                            scriptArea.setValue("// ❌ Failed to call Ollama.\n// HTTP Status: " + response.statusCode()
                                    + "\n// Response:\n" + response.body());
                            btnGenerateAi.setEnabled(true);
                            btnGenerateAi.setText("✨ Buatkan Aturan (AI)");
                            Notification.show("Failed to call the AI!", 4000, Notification.Position.MIDDLE);
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    ui.access(() -> {
                        scriptArea.setValue(
                                "// ❌ Terjadi kesalahan saat memanggil Ollama di IP 172.16.0.63:11434.\n// Pastikan server Ollama menyala dan bisa diping dari server ini.\n// Error: "
                                        + ex.getMessage());
                        btnGenerateAi.setEnabled(true);
                        btnGenerateAi.setText("✨ Buatkan Aturan (AI)");
                        Notification.show("Connection to Ollama failed: " + ex.getMessage(), 5000,
                                Notification.Position.MIDDLE);
                    });
                }
            });
        });

        // 4. Live Simulator (Dry-Run)
        Button btnSimulate = new SafeButton("▶ Simulasikan Script pada 3 Baris Dummy", VaadinIcon.PLAY.create());
        btnSimulate.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);

        Grid<Map<String, Object>> simGrid = new Grid<>();
        simGrid.setHeight("140px");
        simGrid.setVisible(false);
        StandardGridUtils.enableCellClipboardCopy(simGrid);

        btnSimulate.addClickListener(e -> scriptArea.getValue(rawScript -> {
            String scriptText = rawScript != null ? rawScript.trim() : "";
            if (scriptText.isEmpty()) {
                Notification.show("The script is still empty!", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (dynamicDataService.getScriptExecutorService() == null) {
                Notification.show("ScriptExecutorService is not active!", 3000, Notification.Position.MIDDLE);
                return;
            }
            try {
                List<Map<String, Object>> simRows = new ArrayList<>();
                Map<String, Object> dummyHeader = new HashMap<>();
                dummyHeader.put("qty", 100);
                dummyHeader.put("customer_id", "CUST-DEMO");
                dummyHeader.put("date", new java.util.Date());

                for (int i = 1; i <= 3; i++) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("baris_ke", i);
                    r.put("status", false);
                    r.put("perseries", 0);
                    dynamicDataService.getScriptExecutorService().executeScript(
                            "sim_" + System.currentTimeMillis() + "_" + i,
                            scriptText, r, i, dummyHeader, simRows, null);
                    simRows.add(r);
                }

                simGrid.removeAllColumns();
                if (!simRows.isEmpty()) {
                    for (String key : simRows.get(0).keySet()) {
                        simGrid.addColumn(row -> row.get(key)).setHeader(key.toUpperCase()).setAutoWidth(true);
                    }
                }
                simGrid.setItems(simRows);
                simGrid.setVisible(true);
                Notification.show("✅ Simulasi sukses! Lihat tabel hasil di bawah.", 3000,
                        Notification.Position.BOTTOM_END);
            } catch (Exception ex) {
                Notification.show("❌ Simulation error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        }));

        layout.add(aiDetails, pickersLayout, scriptArea, btnSimulate, simGrid);

        Button btnSave = new SafeButton("Save Script", VaadinIcon.CHECK.create(), e -> {
            scriptArea.getValue(raw -> {
                fieldTemp.onAddScript = raw != null ? raw.trim() : "";
                Notification.show("On-Add-Row script kept in memory - remember to click Save Form!", 4000,
                        Notification.Position.BOTTOM_END);
                dialog.close();
            });
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnClear = new SafeButton("Delete Script", VaadinIcon.TRASH.create(), e -> {
            scriptArea.setValue("");
            fieldTemp.onAddScript = null;
            dialog.close();
        });
        btnClear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button btnCancel = new SafeButton("Cancel", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(btnClear, btnCancel, btnSave);
        dialog.open();
    }
}
