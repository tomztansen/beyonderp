package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadinerp.service.FileStorageService;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

/**
 * Field tanda tangan: user menggambar di kanvas (mouse, jari, atau stylus), nilainya PNG transparan
 * sebagai data URL ("data:image/png;base64,..."), atau null bila kosong.
 *
 * Komponen ini hanya menangkap gambar. Menyimpannya ke file storage dan mengaitkannya ke user
 * adalah tugas form yang memakainya. Tidak terdaftar di ComponentFactory/designer.
 *
 * Tidak ada state static dan tidak ada listener yang didaftarkan ke UI; semua pendengar sisi browser
 * dilepas oleh signature-pad-wrapper.js saat elemen dilepas dari halaman.
 */
public class SignaturePadField extends CustomField<String> {

    private static final String PNG_PREFIX = "data:image/png;base64,";
    /** Batas panjang data URL (karakter). Ekspor JS dibatasi 600 px lebar, jadi normalnya jauh di bawah ini. */
    private static final int MAX_VALUE_CHARS = 300_000;

    @Tag("signature-pad-wrapper")
    @NpmPackage(value = "signature_pad", version = "^5.1.4")
    @JsModule("./components/signature-pad-wrapper.js")
    public static class Pad extends Component {
    }

    @DomEvent("signature-change")
    public static class SignatureChangeEvent extends ComponentEvent<Pad> {
        private final String value;

        public SignatureChangeEvent(Pad source, boolean fromClient, @EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @DomEvent("signature-error")
    public static class SignatureErrorEvent extends ComponentEvent<Pad> {
        private final String message;

        public SignatureErrorEvent(Pad source, boolean fromClient, @EventData("event.detail.message") String message) {
            super(source, fromClient);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};

    private final Pad pad = new Pad();
    private final Button btnUndo = new Button(VaadinIcon.ARROW_BACKWARD.create());
    private final Button btnClear = new Button(VaadinIcon.TRASH.create());
    private final Button btnUpload = new Button(VaadinIcon.UPLOAD.create());
    private String currentValue;

    // Mode file (storage != null): nilai field = nama file PNG di FileStorageService, sama seperti IMAGE_UPLOAD,
    // jadi kolom DB hanya berisi nama file. Tanpa storage: nilai = data URL (dipakai preview FormBuilder).
    private final FileStorageService storage;
    private final Supplier<String> baseName;
    // File yang dimuat dari record (sudah tersimpan) dan file sementara buatan field ini. Yang pertama baru
    // dihapus setelah form disimpan (commitPendingDeletes); yang kedua belum pernah disimpan, langsung dihapus.
    private String originalFile;
    private String tmpFile;
    private final List<String> pendingDeletes = new ArrayList<>();

    public SignaturePadField(String label) {
        this(label, null, null);
    }

    /**
     * @param storage  penyimpan file; null = mode data URL
     * @param baseName awalan nama file (mis. user id) -> "<awalan>_signature.png"; boleh null
     */
    public SignaturePadField(String label, FileStorageService storage, Supplier<String> baseName) {
        this.storage = storage;
        this.baseName = baseName;
        if (label != null && !label.isBlank()) {
            setLabel(label);
        }

        pad.getElement().getStyle()
                .set("display", "block")
                .set("width", "100%")
                .set("height", "160px")
                .set("background", "#fff")
                .set("border", "1px dashed var(--lumo-contrast-40pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-sizing", "border-box");

        btnUndo.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnUndo.getElement().setAttribute("title", "Undo");
        btnUndo.addClickListener(e -> pad.getElement().callJsFunction("undo"));

        btnClear.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnClear.getElement().setAttribute("title", "Clear");
        btnClear.addClickListener(e -> pad.getElement().callJsFunction("clear"));

        // Dialog file hanya boleh dibuka dari klik pengguna di browser, bukan dari panggilan server, jadi klik
        // tombol dihubungkan langsung ke pad.pickImage() di sisi klien. Dipasang tiap tombol dipasang ke halaman
        // (elemen browser dibuat baru); penanda __sigPick mencegah pemasangan ganda pada elemen yang sama.
        btnUpload.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnUpload.getElement().setAttribute("title", "Upload image (PNG/JPG, replaces the signature)");
        btnUpload.addAttachListener(e -> btnUpload.getElement().executeJs(
                "if (!this.__sigPick) { this.__sigPick = true; this.addEventListener('click', () => $0.pickImage()); }",
                pad.getElement()));

        ComponentUtil.addListener(pad, SignatureChangeEvent.class, this::onSignatureChange);
        ComponentUtil.addListener(pad, SignatureErrorEvent.class,
                e -> Notification.show(e.getMessage(), 4000, Notification.Position.MIDDLE));
        // Elemen browser dibuat baru tiap dipasang ke halaman (mis. tab dibuka lagi), jadi kanvasnya kosong
        // sementara nilai di server masih ada. Kirim ulang nilai dan status read-only. Listener ini milik
        // pad sendiri, bukan UI, jadi ikut hilang bersama komponennya.
        pad.addAttachListener(e -> {
            if (currentValue != null) {
                pad.getElement().callJsFunction("setValue", displayValue());
            }
            pad.getElement().callJsFunction("setReadOnly", isReadOnly());
        });

        HorizontalLayout tools = new HorizontalLayout(btnUndo, btnClear, btnUpload);
        tools.setPadding(false);
        tools.setSpacing(false);

        Div box = new Div(pad, tools);
        box.setWidthFull();
        // Kanvas selebar baris form (bisa 1500 px lebih) membuat tanda tangan gepeng; 480 px mendekati bentuk
        // aslinya (ekspor 600x200).
        box.getStyle().set("max-width", "480px");
        add(box);
        applyState();
    }

    private void onSignatureChange(SignatureChangeEvent event) {
        String v = event.getValue();
        if (v != null && (!v.startsWith(PNG_PREFIX) || v.length() > MAX_VALUE_CHARS)) {
            // Bukan PNG hasil kanvas kita atau terlalu besar: abaikan dan kembalikan gambar sebelumnya.
            pad.getElement().callJsFunction("setValue", displayValue());
            return;
        }
        if (storage == null) {
            currentValue = v;
            setModelValue(v, true);
            return;
        }
        String stored = null;
        if (v != null) {
            try {
                byte[] png = Base64.getDecoder().decode(v.substring(PNG_PREFIX.length()));
                if (png.length < PNG_MAGIC.length || !Arrays.equals(Arrays.copyOf(png, PNG_MAGIC.length), PNG_MAGIC)) {
                    throw new IllegalArgumentException("not a PNG image");
                }
                stored = storage.storeFile(new ByteArrayInputStream(png), fileName());
            } catch (Exception ex) {
                Notification.show("Failed to save signature: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                pad.getElement().callJsFunction("setValue", displayValue());
                return;
            }
        }
        if (tmpFile != null) {
            storage.deleteFile(tmpFile); // goresan sebelumnya belum pernah disimpan
        } else if (originalFile != null && !pendingDeletes.contains(originalFile)) {
            pendingDeletes.add(originalFile); // tanda tangan tersimpan diganti: hapus fisiknya setelah form disimpan
        }
        tmpFile = stored;
        currentValue = stored;
        setModelValue(stored, true);
    }

    private String fileName() {
        String base = baseName != null ? baseName.get() : null;
        return (base == null || base.isBlank() ? "signature" : base.trim() + "_signature") + ".png";
    }

    /** Isi untuk kanvas: data URL. Mode file membaca file-nya; file hilang = kanvas kosong. */
    private String displayValue() {
        if (currentValue == null) {
            return "";
        }
        if (storage == null) {
            return currentValue;
        }
        try {
            byte[] bytes = Files.readAllBytes(storage.resolveUploadedFile(currentValue));
            return PNG_PREFIX + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Hapus fisik file tersimpan yang digantikan atau dikosongkan. Dipanggil setelah record berhasil disimpan
     * (sama seperti FileUploadField.commitPendingDeletes). Kalau form dibatalkan, tidak dipanggil dan
     * setPresentationValue membuang daftarnya.
     */
    public void commitPendingDeletes() {
        if (storage == null) {
            return;
        }
        for (String f : new ArrayList<>(pendingDeletes)) {
            storage.deleteFile(f);
        }
        pendingDeletes.clear();
    }

    /**
     * Bukan super.setReadOnly(): compiler Eclipse menolak super-call ke default method interface
     * (lihat QrScanField). Isinya sama dengan default HasValueAndElement.
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        getElement().setProperty("readonly", readOnly);
        applyState();
    }

    private void applyState() {
        boolean ro = isReadOnly();
        btnUndo.setEnabled(!ro);
        btnClear.setEnabled(!ro);
        btnUpload.setEnabled(!ro);
        pad.getElement().callJsFunction("setReadOnly", ro);
    }

    @Override
    protected String generateModelValue() {
        return currentValue;
    }

    @Override
    protected void setPresentationValue(String value) {
        currentValue = value == null || value.isBlank() ? null : value;
        if (storage != null) {
            // Nilai dipaksa dari luar (buka record lain, New, Cancel, atau selesai simpan): rencana hapus lama dibuang.
            originalFile = currentValue;
            tmpFile = null;
            pendingDeletes.clear();
        }
        pad.getElement().callJsFunction("setValue", displayValue());
    }
}
