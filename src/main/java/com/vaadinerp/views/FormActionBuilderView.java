package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route("action-builder")
@com.vaadin.flow.router.RouteAlias("form-action-builder")
public class FormActionBuilderView extends VerticalLayout {

    private final FormActionMetaRepository actionRepository;
    private final FormMetaRepository formRepository;
    private final LovMetaRepository lovRepository;

    private final Grid<FormActionMeta> grid = new Grid<>(FormActionMeta.class, false);
    private final ComboBox<String> formFilter = new ComboBox<>("Filter Form");

    private final ComboBox<String> formCodeCombo = new ComboBox<>("Form Code Target");
    private final TextField actionCodeField = new TextField("Action Code (Unik)");
    private final TextField actionLabelField = new TextField("Label Tombol");
    private final ComboBox<String> targetScopeCombo = new ComboBox<>("Posisi Toolbar");
    private final ComboBox<String> actionTypeCombo = new ComboBox<>("Tipe Aksi");
    private final ComboBox<String> iconNameCombo = new ComboBox<>("Ikon Tombol");
    private final ComboBox<String> buttonStyleCombo = new ComboBox<>("Gaya / Warna Tombol");
    private final ComboBox<String> sourceLovCodeCombo = new ComboBox<>("Sumber Data Pop-up (LOV / Form)");
    private final ComboBox<String> copySourceLovCodeCombo = new ComboBox<>("Copy Data Source (2-Stage)");
    private final TextArea filterMappingField = new TextArea("Filter Mapping (Pop-up)");
    private final TextArea copyFilterMappingField = new TextArea("Copy Filter Mapping (2-Stage)");
    private final TextArea targetMappingField = new TextArea("Target Mapping");
    private final TextField menuGroupField = new TextField("Menu Group (Dropdown)");
    private final TextArea scriptContentField = new TextArea("Groovy / Action Script Context");
    private final HorizontalLayout groovyHelperBar = new HorizontalLayout();
    private final HorizontalLayout filterLayout = new HorizontalLayout();
    private final HorizontalLayout copyFilterLayout = new HorizontalLayout();
    private final HorizontalLayout targetLayout = new HorizontalLayout();

    private FormActionMeta currentAction;

    private final com.vaadinerp.service.DynamicDataService dynamicDataService;

    @Autowired
    public FormActionBuilderView(FormActionMetaRepository actionRepository,
            FormMetaRepository formRepository,
            LovMetaRepository lovRepository,
            com.vaadinerp.service.DynamicDataService dynamicDataService) {
        this.actionRepository = actionRepository;
        this.formRepository = formRepository;
        this.lovRepository = lovRepository;
        this.dynamicDataService = dynamicDataService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "5px");

        HorizontalLayout toolbar = buildToolbar();

        setupFilterAndCombos();
        setupGrid();
        VerticalLayout editorLayout = buildEditorLayout();

        VerticalLayout leftPane = new VerticalLayout(formFilter, grid);
        leftPane.setSizeFull();
        leftPane.setPadding(false);

        SplitLayout splitLayout = new SplitLayout(leftPane, editorLayout);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(55);

        add(title, toolbar, splitLayout);

        refreshGrid();
        clearForm();
    }

    private HorizontalLayout buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.addClassName("sticky-toolbar");
        toolbar.getStyle()
                .set("background-color", "#f3f4f6")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "6px")
                .set("padding", "6px 12px")
                .set("align-items", "center")
                .set("gap", "15px")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "100")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.08)");

        Button btnNew = new Button("Baru", VaadinIcon.PLUS_CIRCLE.create(), e -> clearForm());
        btnNew.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnNew.getStyle().set("color", "#22c55e");

        Button btnSave = new Button("Simpan", VaadinIcon.DOWNLOAD.create(), e -> saveAction());
        btnSave.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnSave.getStyle().set("color", "#3b82f6");

        Button btnDelete = new Button("Hapus", VaadinIcon.CLOSE_CIRCLE.create(), e -> deleteAction());
        btnDelete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        toolbar.add(btnNew, btnSave, btnDelete);
        return toolbar;
    }

    private void setupFilterAndCombos() {
        List<String> formCodes = new ArrayList<>(formRepository.findAll().stream()
                .map(FormMeta::getFormCode)
                .sorted()
                .toList());

        List<String> filterItems = new ArrayList<>();
        filterItems.add("[Katalog Global Reusable]");
        filterItems.addAll(formCodes);

        formFilter.setItems(filterItems);
        formFilter.setClearButtonVisible(true);
        formFilter.setPlaceholder("Tampilkan Semua Aksi...");
        formFilter.setWidthFull();
        formFilter.addValueChangeListener(e -> refreshGrid());

        formCodeCombo.setLabel("Form Code Target (Kosongkan = Katalog Reusable)");
        formCodeCombo.setItems(formCodes);
        formCodeCombo.setClearButtonVisible(true);

        targetScopeCombo.setItems("MASTER_TOOLBAR", "DETAIL_TOOLBAR", "ON_LOAD_NEW", "ON_DETAIL_ADD", "ON_LOAD_EDIT");
        targetScopeCombo.setValue("MASTER_TOOLBAR");

        actionTypeCombo.setItems("POPUP_PICKER", "GROOVY_SCRIPT");
        actionTypeCombo.setValue("POPUP_PICKER");
        actionTypeCombo.addValueChangeListener(e -> updateEditorVisibility(e.getValue()));

        iconNameCombo.setItems(java.util.Arrays.stream(com.vaadin.flow.component.icon.VaadinIcon.values())
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toList()));
        iconNameCombo.setAllowCustomValue(true);
        iconNameCombo.addCustomValueSetListener(e -> {
            if (e.getDetail() != null)
                iconNameCombo.setValue(e.getDetail().toUpperCase());
        });
        iconNameCombo.setRenderer(com.vaadin.flow.data.renderer.LitRenderer.<String>of(
                "<div style='display: flex; align-items: center; gap: 8px; padding: 2px 0;'>"
                        + "  <vaadin-icon icon='vaadin:${item.iconCode}' style='color: #3b82f6; font-size: 18px; flex-shrink: 0;'></vaadin-icon>"
                        + "  <span style='font-size: 0.9rem;'>${item.name}</span>"
                        + "</div>")
                .withProperty("iconCode", name -> name != null ? name.toLowerCase().replace("_", "-") : "")
                .withProperty("name", name -> name != null ? name : ""));
        iconNameCombo.setClearButtonVisible(true);

        buttonStyleCombo.setItems("PRIMARY", "SUCCESS", "ERROR", "TERTIARY");
        buttonStyleCombo.setValue("PRIMARY");

        List<String> sources = new ArrayList<>();
        lovRepository.findAll().forEach(l -> sources.add(l.getLovCode()));
        formRepository.findAll().forEach(f -> {
            if (!sources.contains(f.getFormCode())) {
                sources.add(f.getFormCode());
            }
        });
        sources.sort(String::compareToIgnoreCase);
        sourceLovCodeCombo.setItems(sources);

        copySourceLovCodeCombo.setItems(sources);
        copySourceLovCodeCombo.setClearButtonVisible(true);
    }

    private void createGroovyHelperBar() {
        groovyHelperBar.removeAll();
        groovyHelperBar.setWidthFull();
        groovyHelperBar.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE);
        groovyHelperBar.getStyle().set("margin-top", "8px").set("background-color", "#f8fafc")
                .set("padding", "8px 12px").set("border-radius", "6px").set("border", "1px dashed #cbd5e1");

        Button openFullEditorBtn = new Button("🖥️ Buka Editor Fullscreen & Cek Sintaks",
                VaadinIcon.EXPAND_FULL.create(), e -> showGroovyEditorModal());
        openFullEditorBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS);

        Button checkSyntaxBtn = new Button("🔍 Cek Sintaks", VaadinIcon.CHECK_CIRCLE.create(),
                e -> checkAndNotifyGroovySyntax(scriptContentField.getValue()));
        checkSyntaxBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);

        Button cheatSheetBtn = new Button("📖 Cheat Sheet", VaadinIcon.BOOK.create(),
                e -> showGroovyCheatSheetDialog());
        cheatSheetBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);

        ComboBox<String> snippetCombo = new ComboBox<>("⚡ Sisipkan Template Cepat");
        snippetCombo.setItems(
                "1. Cek Pilihan (Hentikan jika tidak ada baris yang dipilih)",
                "2. Dialog Konfirmasi + Eksekusi Stored Procedure",
                "3. Looping ID + Panggil Procedure + Buka Tab Baru",
                "4. Tampilkan Notifikasi Sukses / Error",
                "5. Debug Isi Header & Grid (msgBox)",
                "6. Bersihkan/Reset Form (clearForm & setElementValue)",
                "7. Query Database Ringan di Script (db.getValue & db.find)");
        snippetCombo.setWidth("260px");
        snippetCombo.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val != null) {
                insertSnippetToText(scriptContentField, val);
                snippetCombo.clear();
            }
        });

        groovyHelperBar.add(openFullEditorBtn, checkSyntaxBtn, cheatSheetBtn, snippetCombo);
    }

    private void showGroovyCheatSheetDialog() {
        com.vaadin.flow.component.dialog.Dialog dlg = new com.vaadin.flow.component.dialog.Dialog();
        dlg.setHeaderTitle("📖 Panduan & Daftar Sintaks Groovy Action DSL");
        dlg.setWidth("820px");

        com.vaadin.flow.component.html.Div content = new com.vaadin.flow.component.html.Div();
        content.getStyle().set("max-height", "75vh").set("overflow-y", "auto").set("line-height", "1.6")
                .set("font-size", "14px");

        String html = """
                <p><b>Daftar Fungsi (DSL Macro & Context Methods):</b></p>
                <table border='1' cellpadding='6' style='border-collapse: collapse; width: 100%; border-color: #cbd5e1;'>
                  <tr style='background-color: #f1f5f9;'>
                    <th style='text-align: left;'>Sintaks / Fungsi</th>
                    <th style='text-align: left;'>Keterangan & Kegunaan</th>
                  </tr>
                  <tr>
                    <td><code>msgBox(data)</code><br/><i>atau</i> <code>msgBox("Judul", data)</code></td>
                    <td>Memunculkan modal window inspektur bergaya *Dark Code Viewer* untuk memeriksa isi JSON/Map/String (sangat berguna untuk debugging <code>header</code> atau <code>selectedRows</code>).</td>
                  </tr>
                  <tr>
                    <td><code>clearForm()</code></td>
                    <td>Mengosongkan/me-reset seluruh isian form aktif di layar (Bandbox, Textbox, dll.) dan memperbarui UI seketika.</td>
                  </tr>
                  <tr>
                    <td><code>setElementValue("field", nilai)</code></td>
                    <td>Mengubah atau mengosongkan (<code>null</code> atau <code>0</code>) nilai pada field form tertentu dan langsung merefresh tampilan layar browser.</td>
                  </tr>
                  <tr>
                    <td><code>refreshForm()</code></td>
                    <td>Memperbarui/merefresh tampilan komponen UI di layar browser setelah objek <code>header</code> dimodifikasi secara dinamis (misal: <code>header.quantity = 0</code>).</td>
                  </tr>
                  <tr>
                    <td><code>header.field.subfield</code><br/><i>atau</i> <code>header.field_subfield</code></td>
                    <td>Berkat fitur <i>SmartHeaderNode</i>, Anda dapat langsung memanggil properti bersarang (seperti <code>header.tsproductionorderid.idno</code> dari hasil copy Debug Context) tanpa khawatir error atau null.</td>
                  </tr>
                  <tr>
                    <td><code>db.getValue("SELECT ... WHERE id = ?", arg)</code></td>
                    <td>Mengambil 1 nilai tunggal secara langsung dari database menggunakan query murni yang aman dari SQL Injection.</td>
                  </tr>
                  <tr>
                    <td><code>db.find("tabel", "keyCol", val)</code></td>
                    <td>Mengambil 1 baris data utuh (Map) dari tabel database berdasarkan primary key/kolom unik tertentu.</td>
                  </tr>
                  <tr>
                    <td><code>getElementValue("grid1", true)</code><br/><i>atau</i> <code>selectedRows</code></td>
                    <td>Mengambil daftar baris data (Map) yang sedang dicentang/pilih oleh user pada tabel grid aktif.</td>
                  </tr>
                  <tr>
                    <td><code>ctx.userId</code><br/><i>atau</i> <code>@app{userid}</code></td>
                    <td>Mengambil Username atau ID pengguna yang sedang login saat ini untuk keperluan parameter prosedur/audit.</td>
                  </tr>
                  <tr>
                    <td><code>showYesNoDialog("Judul", "Pesan", callback)</code></td>
                    <td>Menampilkan dialog konfirmasi Yes / No. Perintah di dalam <code>callback</code> hanya dijalankan jika user klik Yes.</td>
                  </tr>
                  <tr>
                    <td><code>executeProcedure(procId, callback, jsonParams, userId)</code></td>
                    <td>Menjalankan Stored Procedure di server database secara aman dan mengembalikan status sukses (true/false) ke callback.</td>
                  </tr>
                  <tr>
                    <td><code>showSuccess("Judul", "Pesan")</code></td>
                    <td>Menampilkan notifikasi Lumo berwarna hijau (Sukses) di posisi atas layar user.</td>
                  </tr>
                  <tr>
                    <td><code>showError("Judul", "Pesan")</code></td>
                    <td>Menampilkan notifikasi Lumo berwarna merah (Error) di posisi atas layar user.</td>
                  </tr>
                  <tr>
                    <td><code>showMainTab(tabId, "Judul Tab", url, extra)</code></td>
                    <td>Membuka atau berpindah secara dinamis ke tab/halaman menu ERP lain setelah aksi selesai.</td>
                  </tr>
                </table>
                <br/>
                <p><b>💡 Tips Penting:</b></p>
                <ul>
                  <li>Gunakan <code>return</code> di dalam kondisi <code>if (!selectedRows)</code> untuk menghentikan eksekusi script seketika jika prasyarat tidak terpenuhi.</li>
                  <li>Untuk mengambil nilai kolom tertentu dari sebuah baris dalam loop: <code>row.salesid</code> atau <code>row['salesid']</code>.</li>
                </ul>
                """;
        content.add(new com.vaadin.flow.component.Html("<div>" + html + "</div>"));
        dlg.add(content);

        Button closeBtn = new Button("Tutup", e -> dlg.close());
        closeBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        dlg.getFooter().add(closeBtn);
        dlg.open();
    }

    private void insertSnippetToScript(String snippetType) {
        insertSnippetToText(scriptContentField, snippetType);
    }

    private void insertSnippetToText(com.vaadin.flow.component.textfield.TextArea editor, String snippetType) {
        if (editor == null)
            return;
        String currentText = editor.getValue() != null ? editor.getValue() : "";
        String template = getSnippetTemplate(snippetType);
        if (!currentText.isBlank()) {
            editor.setValue(currentText + "\n\n" + template);
        } else {
            editor.setValue(template);
        }
    }

    private String getSnippetTemplate(String snippetType) {
        if (snippetType == null)
            return "";
        if (snippetType.startsWith("1.")) {
            return """
                    def selectedRows = getElementValue("grid1", true)
                    if (!selectedRows || selectedRows.isEmpty()) {
                        showError("Peringatan", "Silakan pilih atau centang minimal satu baris data terlebih dahulu!")
                        return
                    }
                    """.stripIndent();
        } else if (snippetType.startsWith("2.")) {
            return """
                    showYesNoDialog("Konfirmasi Eksekusi", "Apakah Anda yakin ingin melanjutkan proses pada data terpilih?", {
                        executeProcedure(3, { status ->
                            if (status) {
                                showSuccess("Berhasil", "Prosedur berhasil dijalankan di database!")
                            }
                        }, "{}", ctx.userId)
                    })
                    """
                    .stripIndent();
        } else if (snippetType.startsWith("3.")) {
            return """
                    def selectedRows = getElementValue("grid1", true)
                    if (!selectedRows || selectedRows.isEmpty()) {
                        showError("Peringatan", "Silakan pilih minimal 1 baris data!")
                        return
                    }
                    def ids = []
                    for (def row in selectedRows) {
                        ids.push(row.id)
                    }
                    showYesNoDialog("Release Confirmation", "Lanjutkan rilis untuk " + ids.size() + " data terpilih?", {
                        executeProcedure(3, { status ->
                            if (status) {
                                showSuccess("Sukses", "Data berhasil dirilis!")
                                showMainTab(691, "Sales Line - Production Order", null, null)
                            }
                        }, groovy.json.JsonOutput.toJson(ids), ctx.userId)
                    })
                    """.stripIndent();
        } else if (snippetType.startsWith("4.")) {
            return """
                    showSuccess("Pemberitahuan", "Aksi Groovy berhasil dijalankan tanpa kendala.")
                    """.stripIndent();
        } else if (snippetType.startsWith("5.")) {
            return """
                    // Menampilkan jendela debug bergaya Dark Code Viewer untuk memeriksa isi variabel
                    msgBox("🛠️ Debug Form Header", header)
                    // msgBox("Baris Terpilih (${selectedRows.size()})", selectedRows)
                    return
                    """.stripIndent();
        } else if (snippetType.startsWith("6.")) {
            return """
                    // Mengosongkan field spesifik & memperbarui tampilan UI di layar
                    setElementValue("tsproductionorderid", null)
                    setElementValue("quantity", 0)
                    setElementValue("quantityperbox", 0)
                    setElementValue("itemcode", null)

                    // ATAU gunakan clearForm() untuk mengosongkan seluruh form sekaligus:
                    // clearForm()
                    """.stripIndent();
        } else if (snippetType.startsWith("7.")) {
            return """
                    // Query nilai langsung dari tabel database
                    def prodIdno = header.tsproductionorderid_idno ?: db.getValue("SELECT idno FROM tsproductionorder WHERE id = ?", header.tsproductionorderid)
                    msgBox("Nomor PO Terpilih", prodIdno)
                    """
                    .stripIndent();
        }
        return "";
    }

    private void showGroovyEditorModal() {
        com.vaadin.flow.component.dialog.Dialog dlg = new com.vaadin.flow.component.dialog.Dialog();
        dlg.setHeaderTitle("🖥️ Groovy Action Studio & Syntax Checker");
        dlg.setWidth("90vw");
        dlg.setHeight("86vh");
        dlg.setResizable(true);
        dlg.setDraggable(true);

        com.vaadin.flow.component.orderedlayout.VerticalLayout layout = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);

        com.vaadin.flow.component.orderedlayout.HorizontalLayout toolbar = new com.vaadin.flow.component.orderedlayout.HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE);
        toolbar.getStyle().set("background-color", "#f1f5f9").set("padding", "8px 16px").set("border-radius", "6px")
                .set("border-bottom", "1px solid #cbd5e1");

        Button checkBtn = new Button("🔍 Cek & Validasi Sintaks", VaadinIcon.CHECK_CIRCLE.create());
        checkBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS);

        Button formatBtn = new Button("🧹 Format Indentasi", VaadinIcon.ALIGN_LEFT.create());
        formatBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);

        Button cheatBtn = new Button("📖 Cheat Sheet DSL", VaadinIcon.BOOK.create(), e -> showGroovyCheatSheetDialog());
        cheatBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);

        ComboBox<String> snippetComboModal = new ComboBox<>("⚡ Sisipkan Snippet");
        snippetComboModal.setItems(
                "1. Cek Pilihan (Hentikan jika tidak ada baris yang dipilih)",
                "2. Dialog Konfirmasi + Eksekusi Stored Procedure",
                "3. Looping ID + Panggil Procedure + Buka Tab Baru",
                "4. Tampilkan Notifikasi Sukses / Error",
                "5. Debug Isi Header & Grid (msgBox)",
                "6. Bersihkan/Reset Form (clearForm & setElementValue)",
                "7. Query Database Ringan di Script (db.getValue & db.find)");
        snippetComboModal.setWidth("280px");

        com.vaadin.flow.component.textfield.TextArea modalEditor = new com.vaadin.flow.component.textfield.TextArea();
        modalEditor.setSizeFull();
        modalEditor.setValue(scriptContentField.getValue() != null ? scriptContentField.getValue() : "");
        modalEditor.getStyle().set("font-family", "Consolas, 'Courier New', monospace").set("font-size", "14px")
                .set("line-height", "1.5");
        modalEditor.setPlaceholder("Tulis atau sisipkan script Groovy Anda di sini...");

        com.vaadin.flow.component.html.Div statusArea = new com.vaadin.flow.component.html.Div();
        statusArea.setWidthFull();
        statusArea.getStyle().set("padding", "10px 14px").set("border-radius", "6px").set("font-weight", "500")
                .set("display", "none").set("white-space", "pre-wrap");

        checkBtn.addClickListener(e -> {
            checkGroovySyntaxInternal(modalEditor.getValue(), statusArea);
        });

        formatBtn.addClickListener(e -> {
            String formatted = formatGroovyScript(modalEditor.getValue());
            modalEditor.setValue(formatted);
            checkGroovySyntaxInternal(formatted, statusArea);
        });

        snippetComboModal.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val != null) {
                insertSnippetToText(modalEditor, val);
                snippetComboModal.clear();
            }
        });

        toolbar.add(checkBtn, formatBtn, snippetComboModal, cheatBtn);
        layout.add(toolbar, modalEditor, statusArea);
        layout.setFlexGrow(1, modalEditor);

        dlg.add(layout);

        Button saveBtn = new Button("💾 Terapkan ke Form", VaadinIcon.CHECK.create(), e -> {
            scriptContentField.setValue(modalEditor.getValue());
            dlg.close();
            com.vaadin.flow.component.notification.Notification
                    .show("✅ Script berhasil diterapkan!", 3000,
                            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER)
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Batal", e -> dlg.close());

        dlg.getFooter().add(cancelBtn, saveBtn);
        dlg.open();
    }

    private boolean checkGroovySyntaxInternal(String scriptText, com.vaadin.flow.component.html.Div statusArea) {
        if (scriptText == null || scriptText.isBlank()) {
            if (statusArea != null) {
                statusArea.getStyle().set("display", "block").set("background-color", "#fef3c7").set("color", "#92400e")
                        .set("border", "1px solid #f59e0b");
                statusArea.setText("⚠️ Script masih kosong.");
            }
            return false;
        }
        try {
            new groovy.lang.GroovyShell().parse(scriptText);
            if (statusArea != null) {
                statusArea.getStyle().set("display", "block").set("background-color", "#dcfce7").set("color", "#166534")
                        .set("border", "1px solid #22c55e");
                statusArea.setText("✅ Sintaks Groovy Valid! Tidak ada kesalahan penulisan atau struktur kode.");
            }
            return true;
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (statusArea != null) {
                statusArea.getStyle().set("display", "block").set("background-color", "#fee2e2").set("color", "#991b1b")
                        .set("border", "1px solid #ef4444");
                statusArea.setText("❌ Kesalahan Sintaks Groovy:\n" + errMsg);
            } else {
                com.vaadin.flow.component.notification.Notification
                        .show("❌ Kesalahan Sintaks Groovy:\n" + errMsg, 5000,
                                com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER)
                        .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
            }
            return false;
        }
    }

    private void checkAndNotifyGroovySyntax(String scriptText) {
        if (scriptText == null || scriptText.isBlank()) {
            com.vaadin.flow.component.notification.Notification
                    .show("⚠️ Script masih kosong.", 3000,
                            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER)
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_WARNING);
            return;
        }
        try {
            new groovy.lang.GroovyShell().parse(scriptText);
            com.vaadin.flow.component.notification.Notification
                    .show("✅ Sintaks Groovy VALID! Tidak ada kesalahan struktur.", 4000,
                            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER)
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification
                    .show("❌ Kesalahan Sintaks:\n" + e.getMessage(), 6000,
                            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER)
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
        }
    }

    private String formatGroovyScript(String scriptText) {
        if (scriptText == null)
            return "";
        String[] lines = scriptText.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        int indentLevel = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("}") || trimmed.startsWith(")") || trimmed.startsWith("]")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }
            if (!trimmed.isEmpty()) {
                sb.append("    ".repeat(indentLevel)).append(trimmed).append("\n");
            } else {
                sb.append("\n");
            }
            if (trimmed.endsWith("{") || trimmed.endsWith("(") || trimmed.endsWith("[")) {
                indentLevel++;
            }
        }
        return sb.toString().trim();
    }

    private void updateEditorVisibility(String actionType) {
        boolean isGroovy = "GROOVY_SCRIPT".equalsIgnoreCase(actionType);
        groovyHelperBar.setVisible(isGroovy);
        scriptContentField.setVisible(isGroovy);

        sourceLovCodeCombo.setVisible(!isGroovy);
        filterLayout.setVisible(!isGroovy);
        copySourceLovCodeCombo.setVisible(!isGroovy);
        copyFilterLayout.setVisible(!isGroovy);
        targetLayout.setVisible(!isGroovy);
    }

    private void setupGrid() {
        grid.setSizeFull();
        grid.addColumn(a -> a.getFormMeta() != null ? a.getFormMeta().getFormCode() : "[Katalog Global]")
                .setHeader("Form Target").setSortable(true).setAutoWidth(true);
        grid.addColumn(FormActionMeta::getActionCode).setHeader("Action Code").setSortable(true).setAutoWidth(true);
        grid.addColumn(FormActionMeta::getActionLabel).setHeader("Label Tombol").setAutoWidth(true);
        grid.addColumn(FormActionMeta::getTargetScope).setHeader("Posisi").setAutoWidth(true);
        grid.addColumn(FormActionMeta::getActionType).setHeader("Tipe Aksi").setAutoWidth(true);
        grid.addColumn(FormActionMeta::getMenuGroup).setHeader("Menu Group").setAutoWidth(true);
        grid.addColumn(FormActionMeta::getSourceLovCode).setHeader("Source LOV").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                populateForm(event.getValue());
            } else {
                clearForm();
            }
        });
    }

    private VerticalLayout buildEditorLayout() {
        VerticalLayout editor = new VerticalLayout();
        editor.setPadding(true);
        editor.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "8px")
                .set("border", "1px solid #e2e8f0");

        formCodeCombo.setWidthFull();
        actionCodeField.setWidthFull();
        actionLabelField.setWidthFull();
        targetScopeCombo.setWidthFull();
        actionTypeCombo.setWidthFull();
        iconNameCombo.setWidthFull();
        buttonStyleCombo.setWidthFull();
        sourceLovCodeCombo.setWidthFull();
        copySourceLovCodeCombo.setWidthFull();

        filterMappingField.setWidthFull();
        filterMappingField.setPlaceholder("Contoh: status:'Active',customer_id:header.cust_code");
        filterMappingField.setHeight("70px");
        filterLayout.removeAll();
        filterLayout.add(filterMappingField, createBuilderButton(filterMappingField, false));
        filterLayout.setWidthFull();
        filterLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

        copyFilterMappingField.setWidthFull();
        copyFilterMappingField.setPlaceholder("Contoh: item_id:picked.id");
        copyFilterMappingField.setHeight("70px");
        copyFilterLayout.removeAll();
        copyFilterLayout.add(copyFilterMappingField, createBuilderButton(copyFilterMappingField, false));
        copyFilterLayout.setWidthFull();
        copyFilterLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

        targetMappingField.setWidthFull();
        targetMappingField.setPlaceholder("Contoh: item_code:code,price:sell_price,qty:1");
        targetMappingField.setHeight("80px");
        targetLayout.removeAll();
        targetLayout.add(targetMappingField, createBuilderButton(targetMappingField, true));
        targetLayout.setWidthFull();
        targetLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

        menuGroupField.setWidthFull();
        menuGroupField.setPlaceholder("Contoh: Action (kosongkan untuk tombol biasa)");

        createGroovyHelperBar();

        scriptContentField.setWidthFull();
        scriptContentField.setPlaceholder(
                "Groovy / Action Context script. Klik '🖥️ Buka Editor Fullscreen' untuk tampilan luas dan fitur cek sintaks...");
        scriptContentField.setHeight("280px");
        scriptContentField.getStyle().set("font-family", "Consolas, 'Courier New', monospace").set("font-size", "14px")
                .set("line-height", "1.5");

        editor.add(formCodeCombo, actionCodeField, actionLabelField, menuGroupField, targetScopeCombo, actionTypeCombo,
                iconNameCombo, buttonStyleCombo, sourceLovCodeCombo, filterLayout,
                copySourceLovCodeCombo, copyFilterLayout, targetLayout, groovyHelperBar, scriptContentField);

        updateEditorVisibility("POPUP_PICKER");

        return editor;
    }

    private void refreshGrid() {
        List<FormActionMeta> items;
        String filterVal = formFilter.getValue();
        if (filterVal != null && !filterVal.isBlank()) {
            if ("[Katalog Global Reusable]".equals(filterVal)) {
                items = actionRepository.findByFormMetaIsNull();
            } else {
                items = actionRepository.findByFormMeta_FormCode(filterVal);
            }
        } else {
            items = actionRepository.findAll();
        }
        grid.setItems(items);
    }

    private void populateForm(FormActionMeta action) {
        this.currentAction = action;
        formCodeCombo.setValue(action.getFormMeta() != null ? action.getFormMeta().getFormCode() : null);
        actionCodeField.setValue(action.getActionCode() != null ? action.getActionCode() : "");
        actionLabelField.setValue(action.getActionLabel() != null ? action.getActionLabel() : "");
        targetScopeCombo.setValue(action.getTargetScope() != null ? action.getTargetScope() : "DETAIL_TOOLBAR");
        actionTypeCombo.setValue(action.getActionType() != null ? action.getActionType() : "POPUP_PICKER");
        iconNameCombo.setValue(action.getIconName());
        buttonStyleCombo.setValue(action.getButtonStyle() != null ? action.getButtonStyle() : "PRIMARY");
        sourceLovCodeCombo.setValue(action.getSourceLovCode());
        copySourceLovCodeCombo.setValue(action.getCopySourceLovCode());
        filterMappingField.setValue(action.getFilterMapping() != null ? action.getFilterMapping() : "");
        copyFilterMappingField.setValue(action.getCopyFilterMapping() != null ? action.getCopyFilterMapping() : "");
        targetMappingField.setValue(action.getTargetMapping() != null ? action.getTargetMapping() : "");
        menuGroupField.setValue(action.getMenuGroup() != null ? action.getMenuGroup() : "");
        scriptContentField.setValue(action.getScriptContent() != null ? action.getScriptContent() : "");
    }

    private void clearForm() {
        grid.asSingleSelect().clear();
        this.currentAction = null;
        formCodeCombo.clear();
        actionCodeField.clear();
        actionLabelField.clear();
        targetScopeCombo.setValue("MASTER_TOOLBAR");
        actionTypeCombo.setValue("POPUP_PICKER");
        iconNameCombo.clear();
        buttonStyleCombo.setValue("PRIMARY");
        sourceLovCodeCombo.clear();
        copySourceLovCodeCombo.clear();
        filterMappingField.clear();
        copyFilterMappingField.clear();
        targetMappingField.clear();
        menuGroupField.clear();
        scriptContentField.clear();
    }

    private void saveAction() {
        if (actionCodeField.getValue().isBlank() || actionLabelField.getValue().isBlank()) {
            Notification.show("Action Code dan Label Tombol wajib diisi!", 3000, Notification.Position.MIDDLE);
            return;
        }

        FormMeta targetForm = null;
        if (formCodeCombo.getValue() != null && !formCodeCombo.getValue().isBlank()) {
            targetForm = formRepository.findById(formCodeCombo.getValue()).orElse(null);
            if (targetForm == null) {
                Notification.show("Form Code Target tidak ditemukan!", 3000, Notification.Position.MIDDLE);
                return;
            }
        }

        if (currentAction == null) {
            currentAction = new FormActionMeta();
        }

        currentAction.setFormMeta(targetForm);
        currentAction.setActionCode(actionCodeField.getValue().trim());
        currentAction.setActionLabel(actionLabelField.getValue().trim());
        currentAction.setTargetScope(targetScopeCombo.getValue());
        currentAction.setActionType(actionTypeCombo.getValue() != null ? actionTypeCombo.getValue() : "POPUP_PICKER");
        currentAction.setIconName(iconNameCombo.getValue());
        currentAction.setButtonStyle(buttonStyleCombo.getValue());
        currentAction.setSourceLovCode(sourceLovCodeCombo.getValue());
        currentAction.setCopySourceLovCode(copySourceLovCodeCombo.getValue());
        currentAction.setFilterMapping(filterMappingField.getValue());
        currentAction.setCopyFilterMapping(copyFilterMappingField.getValue());
        currentAction.setTargetMapping(targetMappingField.getValue());
        currentAction.setMenuGroup(menuGroupField.getValue() != null ? menuGroupField.getValue().trim() : null);
        currentAction.setScriptContent(scriptContentField.getValue() != null ? scriptContentField.getValue() : null);

        try {
            actionRepository.save(currentAction);
            Notification.show("Aksi berhasil disimpan!", 3000, Notification.Position.MIDDLE);
            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Gagal menyimpan: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private Button createBuilderButton(TextArea targetField, boolean isTargetMapping) {
        Button btn = new Button(VaadinIcon.MAGIC.create());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getElement().setProperty("title", "Mapping Builder");
        btn.addClickListener(e -> openMappingBuilderDialog(targetField, isTargetMapping));
        return btn;
    }

    private void openMappingBuilderDialog(TextArea targetField, boolean isTargetMapping) {
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle(isTargetMapping ? "Target Mapping Builder" : "Filter Mapping Builder");
        dialog.setWidth("700px");

        VerticalLayout rowsLayout = new VerticalLayout();
        rowsLayout.setPadding(false);

        Button btnAddRow = new Button("Tambah Baris", VaadinIcon.PLUS.create());
        btnAddRow.addClickListener(e -> addMappingRow(rowsLayout, isTargetMapping));

        Button btnSave = new Button("Simpan", e -> {
            String generated = generateMappingString(rowsLayout, isTargetMapping);
            targetField.setValue(generated);
            dialog.close();
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button btnCancel = new Button("Batal", e -> dialog.close());

        String existing = targetField.getValue();
        if (existing != null && !existing.isBlank()) {
            parseExistingMapping(existing, rowsLayout, isTargetMapping);
        } else {
            addMappingRow(rowsLayout, isTargetMapping);
        }

        dialog.add(rowsLayout, btnAddRow);
        dialog.getFooter().add(btnCancel, btnSave);
        dialog.open();
    }

    @SuppressWarnings("unchecked")
    private void addMappingRow(VerticalLayout layout, boolean isTargetMapping) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE);

        Button btnRemove = new Button(VaadinIcon.TRASH.create(), e -> layout.remove(row));
        btnRemove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        if (isTargetMapping) {
            ComboBox<String> scopeCombo = new ComboBox<>();
            scopeCombo.setItems("Header", "Detail");
            scopeCombo.setValue("Detail");
            scopeCombo.setWidth("120px");

            TextField destField = new TextField("Target Field");
            destField.setWidth("150px");

            ComboBox<String> srcField = new ComboBox<>("Source Field");
            srcField.setWidth("150px");
            srcField.setAllowCustomValue(true);
            srcField.addCustomValueSetListener(e -> setComboBoxValue(srcField, e.getDetail()));

            // Populate items from data source
            String srcLovCode = copySourceLovCodeCombo.getValue() != null
                    && !copySourceLovCodeCombo.getValue().isBlank()
                            ? copySourceLovCodeCombo.getValue()
                            : sourceLovCodeCombo.getValue();
            if (srcLovCode != null && !srcLovCode.isBlank()) {
                String tableName = srcLovCode;
                LovMeta lovMeta = lovRepository.findById(srcLovCode).orElse(null);
                if (lovMeta != null && lovMeta.getTableName() != null && !lovMeta.getTableName().isBlank()) {
                    tableName = lovMeta.getTableName();
                } else {
                    FormMeta formMeta = formRepository.findById(srcLovCode).orElse(null);
                    if (formMeta != null) {
                        if (formMeta.getViewTable() != null && !formMeta.getViewTable().isBlank()) {
                            tableName = formMeta.getViewTable();
                        } else if (formMeta.getTableName() != null && !formMeta.getTableName().isBlank()) {
                            tableName = formMeta.getTableName();
                        }
                    }
                }
                try {
                    List<String> cols = dynamicDataService.getColumnsForQueryOrTable(tableName);
                    if (cols != null && !cols.isEmpty()) {
                        srcField.setItems(cols);
                    } else {
                        srcField.setItems(new ArrayList<>());
                    }
                } catch (Exception ignored) {
                    srcField.setItems(new ArrayList<>());
                }
            } else {
                srcField.setItems(new ArrayList<>());
            }

            row.add(scopeCombo, destField, new com.vaadin.flow.component.html.Span("="), srcField, btnRemove);
        } else {
            TextField targetCol = new TextField("Filter Field");
            targetCol.setWidth("150px");

            ComboBox<String> typeCombo = new ComboBox<>();
            typeCombo.setItems("Literal", "header.", "picked.", "IS NULL", "IS NOT NULL");
            typeCombo.setValue("Literal");
            typeCombo.setWidth("130px");

            TextField valField = new TextField("Value");
            valField.setWidth("150px");

            typeCombo.addValueChangeListener(ev -> {
                String selected = ev.getValue();
                if ("IS NULL".equals(selected) || "IS NOT NULL".equals(selected)) {
                    valField.clear();
                    valField.setEnabled(false);
                    valField.setPlaceholder("(" + selected + ")");
                } else {
                    valField.setEnabled(true);
                    valField.setPlaceholder("");
                }
            });

            row.add(targetCol, new com.vaadin.flow.component.html.Span("="), typeCombo, valField, btnRemove);
        }
        layout.add(row);
    }

    @SuppressWarnings("unchecked")
    private String generateMappingString(VerticalLayout layout, boolean isTargetMapping) {
        List<String> pairs = new ArrayList<>();
        for (com.vaadin.flow.component.Component c : layout.getChildren().toList()) {
            if (c instanceof HorizontalLayout row) {
                if (isTargetMapping) {
                    ComboBox<String> scope = (ComboBox<String>) row.getComponentAt(0);
                    TextField dest = (TextField) row.getComponentAt(1);
                    ComboBox<String> src = (ComboBox<String>) row.getComponentAt(3);

                    if (dest.getValue() != null && !dest.getValue().isBlank() && src.getValue() != null
                            && !src.getValue().isBlank()) {
                        String prefix = "Detail".equals(scope.getValue()) ? "detail." : "";
                        pairs.add(prefix + dest.getValue().trim() + ":" + src.getValue().trim());
                    }
                } else {
                    TextField target = (TextField) row.getComponentAt(0);
                    ComboBox<String> type = (ComboBox<String>) row.getComponentAt(2);
                    TextField val = (TextField) row.getComponentAt(3);

                    if (target.getValue() != null && !target.getValue().isBlank()) {
                        String colName = target.getValue().trim();
                        if ("IS NULL".equals(type.getValue())) {
                            pairs.add(colName + ":IS NULL");
                        } else if ("IS NOT NULL".equals(type.getValue())) {
                            pairs.add(colName + ":IS NOT NULL");
                        } else if (val.getValue() != null && !val.getValue().isBlank()) {
                            String valStr = val.getValue().trim();
                            if ("Literal".equals(type.getValue())) {
                                if (!valStr.startsWith("'") && !valStr.endsWith("'"))
                                    valStr = "'" + valStr + "'";
                            } else {
                                valStr = type.getValue() + valStr;
                            }
                            pairs.add(colName + ":" + valStr);
                        }
                    }
                }
            }
        }
        if (pairs.isEmpty())
            return "";
        if (isTargetMapping) {
            return String.join(",", pairs);
        } else {
            return "{" + String.join(",", pairs) + "}";
        }
    }

    @SuppressWarnings("unchecked")
    private void parseExistingMapping(String mapping, VerticalLayout layout, boolean isTargetMapping) {
        String clean = mapping.trim();
        if (clean.startsWith("{") && clean.endsWith("}"))
            clean = clean.substring(1, clean.length() - 1).trim();
        if (clean.isEmpty())
            return;

        String[] pairs = com.vaadinerp.util.FormulaEvaluator.splitTopLevelComma(clean);
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length < 2)
                kv = pair.split("=", 2);
            if (kv.length == 2) {
                addMappingRow(layout, isTargetMapping);
                HorizontalLayout row = (HorizontalLayout) layout.getComponentAt(layout.getComponentCount() - 1);
                String k = kv[0].replaceAll("[\"']", "").trim();
                String v = kv[1].trim();

                if (isTargetMapping) {
                    ComboBox<String> scope = (ComboBox<String>) row.getComponentAt(0);
                    TextField dest = (TextField) row.getComponentAt(1);
                    ComboBox<String> src = (ComboBox<String>) row.getComponentAt(3);

                    if (k.toLowerCase().startsWith("detail.")) {
                        scope.setValue("Detail");
                        dest.setValue(k.substring(7));
                    } else {
                        scope.setValue("Header");
                        dest.setValue(k);
                    }
                    setComboBoxValue(src, v.replaceAll("[\"']", ""));
                } else {
                    TextField target = (TextField) row.getComponentAt(0);
                    ComboBox<String> type = (ComboBox<String>) row.getComponentAt(2);
                    TextField val = (TextField) row.getComponentAt(3);

                    target.setValue(k);
                    if ("IS NULL".equalsIgnoreCase(v) || "'IS NULL'".equalsIgnoreCase(v)
                            || "\"IS NULL\"".equalsIgnoreCase(v) || "IS_NULL".equalsIgnoreCase(v)) {
                        type.setValue("IS NULL");
                        val.clear();
                        val.setEnabled(false);
                    } else if ("IS NOT NULL".equalsIgnoreCase(v) || "'IS NOT NULL'".equalsIgnoreCase(v)
                            || "\"IS NOT NULL\"".equalsIgnoreCase(v) || "IS_NOT_NULL".equalsIgnoreCase(v)) {
                        type.setValue("IS NOT NULL");
                        val.clear();
                        val.setEnabled(false);
                    } else if (v.startsWith("header.") || v.startsWith("\"header.")) {
                        type.setValue("header.");
                        val.setValue(v.replace("\"", "").substring(7));
                    } else if (v.startsWith("picked.") || v.startsWith("\"picked.")) {
                        type.setValue("picked.");
                        val.setValue(v.replace("\"", "").substring(7));
                    } else {
                        type.setValue("Literal");
                        val.setValue(v.replace("\"", "").replace("'", ""));
                    }
                }
            }
        }
    }

    private void setComboBoxValue(ComboBox<String> combo, String value) {
        if (value == null || value.isEmpty())
            return;

        com.vaadin.flow.data.provider.DataProvider<String, ?> dp = combo.getDataProvider();
        if (dp instanceof com.vaadin.flow.data.provider.ListDataProvider<?> listDp) {
            @SuppressWarnings("unchecked")
            java.util.Collection<String> items = (java.util.Collection<String>) listDp.getItems();
            if (items == null || items.isEmpty()) {
                combo.setItems(value);
            } else if (!items.contains(value)) {
                java.util.List<String> newItems = new java.util.ArrayList<>(items);
                newItems.add(value);
                combo.setItems(newItems);
            }
        } else {
            combo.setItems(value);
        }
        combo.setValue(value);
    }

    private void deleteAction() {
        if (currentAction == null || currentAction.getId() == null) {
            Notification.show("Pilih aksi yang akan dihapus dari tabel!", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            actionRepository.delete(currentAction);
            Notification.show("Extra Toolbar berhasil dihapus!", 3000, Notification.Position.BOTTOM_END);
            refreshGrid();
            clearForm();
        } catch (Exception e) {
            Notification.show("Gagal menghapus: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }
}
