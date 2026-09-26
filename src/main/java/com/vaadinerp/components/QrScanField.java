package com.vaadinerp.components;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.internal.AllowInert;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Field teks yang bisa diisi lewat scan QR (kamera) atau ketik manual.
 *
 * Decode QR terjadi SELURUHNYA di browser lewat library "qr-scanner" (nimiq),
 * dimuat lazy dari CDN saat dialog scan pertama kali dibuka -- server tidak
 * pernah menerima frame video/gambar, hanya string hasil decode saat berhasil.
 * Library dipakai bersama (cache di window) antar-instance field, jadi tidak
 * di-load ulang tiap kali dialog dibuka.
 *
 * Kamera & worker decoder selalu dilepas (stop+destroy) begitu dialog scan
 * ditutup dengan cara apa pun (tombol Cancel, ESC, klik luar, atau hasil scan
 * diterima) lewat satu titik pembersihan (Dialog#addOpenedChangeListener),
 * plus jaring pengaman di addDetachListener kalau seluruh field dilepas dari
 * halaman saat dialog masih terbuka.
 */
public class QrScanField extends CustomField<String> {

    private static final String QR_SCANNER_CDN = "https://cdn.jsdelivr.net/npm/qr-scanner@1.4.2/qr-scanner.min.js";

    private final TextField textField = new TextField();
    private final Button btnScan = new Button(VaadinIcon.CAMERA.create());
    private final Dialog scanDialog = new Dialog();
    private final Html video;

    public QrScanField(String label) {
        if (label != null && !label.isBlank()) {
            setLabel(label);
        }

        textField.setWidthFull();
        textField.setValueChangeMode(ValueChangeMode.ON_BLUR);
        textField.setClearButtonVisible(true);
        // Ketik manual (atau scanner-gun fisik yang "mengetik" ke field) tetap
        // dianggap perubahan dari user, sama seperti hasil scan kamera di bawah.
        textField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                setModelValue(e.getValue(), true);
            }
        });

        btnScan.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnScan.getElement().setAttribute("title", "Scan QR");
        btnScan.addClickListener(e -> openScanDialog());

        HorizontalLayout row = new HorizontalLayout(textField, btnScan);
        row.setWidthFull();
        row.setFlexGrow(1, textField);
        row.setPadding(false);
        row.setAlignItems(FlexComponent.Alignment.END);
        add(row);

        video = new Html("<video style=\"width:100%;border-radius:8px;background:#000;display:block\" "
                + "playsinline muted></video>");
        scanDialog.setHeaderTitle("Scan QR Code");
        scanDialog.setWidth("420px");
        scanDialog.add(video);
        Button btnCancel = new Button("Cancel", e -> scanDialog.close());
        scanDialog.getFooter().add(btnCancel);

        // Satu-satunya titik cleanup: dipicu tiap kali dialog TIDAK lagi terbuka,
        // apa pun sebabnya (Cancel, ESC, klik luar layar, atau close() dari Java) --
        // supaya kamera & worker qr-scanner tidak pernah nyangkut aktif di background.
        scanDialog.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                stopScanJs();
            }
        });

        // Jaring pengaman: kalau seluruh field ini dilepas dari halaman (mis. tab
        // ditutup) selagi dialog scan masih terbuka, pastikan kamera tetap dilepas.
        addDetachListener(e -> stopScanJs());

        applyState();
    }

    /** BOTH = boleh scan & ketik, SCAN_ONLY = wajib scan, TYPE_ONLY = tombol kamera disembunyikan. */
    public enum InputMode {
        BOTH, SCAN_ONLY, TYPE_ONLY
    }

    private InputMode inputMode = InputMode.BOTH;

    public void setInputMode(InputMode mode) {
        this.inputMode = mode != null ? mode : InputMode.BOTH;
        applyState();
    }

    public InputMode getInputMode() {
        return inputMode;
    }

    /**
     * Readonly keseluruhan field (mis. mode View form) digabung dengan inputMode
     * supaya keduanya konsisten: readonly form selalu menang atas apa pun mode-nya.
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        applyState();
    }

    private void applyState() {
        boolean ro = isReadOnly();
        boolean showScanBtn = inputMode != InputMode.TYPE_ONLY;
        boolean typingAllowed = inputMode != InputMode.SCAN_ONLY;
        textField.setReadOnly(ro || !typingAllowed);
        textField.setPlaceholder(!typingAllowed ? "Klik ikon kamera untuk scan..." : "Ketik atau scan...");
        btnScan.setVisible(showScanBtn);
        btnScan.setEnabled(!ro && showScanBtn);
    }

    private void openScanDialog() {
        scanDialog.open();
        startScanJs();
    }

    private void startScanJs() {
        // Dipanggil lewat getElement() milik QrScanField sendiri (bukan elemen video)
        // supaya "this.$server" di JS merujuk ke instance QrScanField ini -- di situlah
        // method @ClientCallable onScanResult/onScanError dideklarasikan.
        getElement().executeJs(
                "const videoEl = $0;" +
                        "const self = this;" +
                        "if (!window.__qrScannerModulePromise) {" +
                        "  window.__qrScannerModulePromise = import($1).then(m => m.default);" +
                        "}" +
                        "window.__qrScannerModulePromise.then(QrScanner => {" +
                        "  if (videoEl.__qrScanner) { try { videoEl.__qrScanner.destroy(); } catch(e) {} }" +
                        "  videoEl.__qrScanner = new QrScanner(videoEl, result => {" +
                        "    const text = (result && typeof result === 'object') ? result.data : result;" +
                        "    self.$server.onScanResult(text);" +
                        "  }, { maxScansPerSecond: 5, highlightScanRegion: true, highlightCodeOutline: true });" +
                        "  videoEl.__qrScanner.start().catch(err => {" +
                        "    self.$server.onScanError(String(err && err.message ? err.message : err));" +
                        "  });" +
                        "}).catch(err => {" +
                        "  self.$server.onScanError('Gagal memuat scanner: ' + String(err && err.message ? err.message : err));" +
                        "});",
                video.getElement(), QR_SCANNER_CDN);
    }

    private void stopScanJs() {
        getElement().executeJs(
                "const videoEl = $0;" +
                        "if (videoEl && videoEl.__qrScanner) {" +
                        "  try { videoEl.__qrScanner.stop(); } catch(e) {}" +
                        "  try { videoEl.__qrScanner.destroy(); } catch(e) {}" +
                        "  videoEl.__qrScanner = null;" +
                        "}",
                video.getElement());
    }

    @ClientCallable
    @AllowInert
    private void onScanResult(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        scanDialog.close(); // memicu addOpenedChangeListener -> stopScanJs()
        String trimmed = text.trim();
        // setModelValue saja tidak mendorong nilai ke tampilan textField (itu tugas
        // setPresentationValue, yang hanya dipanggil framework lewat setValue() publik
        // dari LUAR). Set eksplisit di sini supaya hasil scan langsung terlihat di kotak
        // teks, sekaligus tetap lapor ke binder dengan fromClient=true agar ON_CHANGE ikut terpicu.
        textField.setValue(trimmed);
        setModelValue(trimmed, true);
    }

    @ClientCallable
    @AllowInert
    private void onScanError(String message) {
        Notification n = Notification.show("Tidak bisa mengakses kamera: " + message, 4000,
                Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    @Override
    protected String generateModelValue() {
        return textField.getValue();
    }

    @Override
    protected void setPresentationValue(String value) {
        textField.setValue(value != null ? value : "");
    }
}
