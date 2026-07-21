package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadinerp.service.FileStorageService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUploadField extends CustomField<String> {

    private final FileStorageService fileStorageService;
    private final boolean isImageOnly;
    private final List<String> currentFiles = new ArrayList<>();

    private final Upload upload;
    private final VerticalLayout fileListLayout;
    private final VerticalLayout mainLayout;
    private Button hapusBtn;

    public FileUploadField(String label, FileStorageService fileStorageService, boolean isImageOnly) {
        this.fileStorageService = fileStorageService;
        this.isImageOnly = isImageOnly;

        setLabel(label);

        this.upload = new Upload((event) -> {
            String fileName = event.getFileName();
            try (InputStream inputStream = event.getInputStream()) {
                String storedFilename = fileStorageService.storeFile(inputStream, fileName);
                if (storedFilename != null) {
                    if (isImageOnly) {
                        // Untuk Photo Box, jika upload foto baru, hapus foto lama terlebih dahulu agar tergantikan (replace)
                        for (String oldFile : new ArrayList<>(currentFiles)) {
                            fileStorageService.deleteFile(oldFile);
                        }
                        currentFiles.clear();
                    }
                    currentFiles.add(storedFilename);
                    event.getUI().access(() -> {
                        updateValueAndUI();
                        Notification.show("Berhasil mengunggah: " + fileName, 3000, Notification.Position.BOTTOM_END);
                    });
                }
            } catch (Exception e) {
                event.getUI().access(() -> {
                    Notification.show("Gagal menyimpan file " + fileName + ": " + e.getMessage(), 5000, Notification.Position.BOTTOM_END);
                });
            }
        });
        this.upload.setMaxFiles(10); // Mendukung upload sampai 10 file sekaligus
        this.upload.setMaxFileSize(10 * 1024 * 1024); // Maksimal 10 MB per file
        this.upload.setDropAllowed(true);

        if (isImageOnly) {
            this.upload.setMaxFiles(1); // Untuk kotak foto profil/barang, 1 foto utama
            this.upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp", "image/gif", ".jpg", ".jpeg", ".png", ".webp", ".gif");

            Button setFotoBtn = new Button("Set Foto");
            setFotoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            setFotoBtn.setWidthFull();
            setFotoBtn.getStyle().set("cursor", "pointer");

            this.upload.setUploadButton(setFotoBtn);
            this.upload.setDropAllowed(false);
            this.upload.getElement().getStyle()
                    .set("padding", "0")
                    .set("margin", "0")
                    .set("border", "none")
                    .set("background", "transparent")
                    .set("width", "50%");
        } else {
            this.upload.setAcceptedFileTypes(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt", ".csv", ".jpg", ".jpeg", ".png", ".webp", ".zip", ".rar");
            Span dropLabel = new Span("Drag & drop dokumen/file di sini atau klik untuk memilih");
            dropLabel.getStyle().set("font-size", "13px").set("color", "#64748b");
            this.upload.setDropLabel(dropLabel);
        }

        this.upload.addFileRejectedListener(event -> {
            Notification.show("File ditolak: " + event.getErrorMessage(), 4000, Notification.Position.BOTTOM_END);
        });

        this.fileListLayout = new VerticalLayout();
        this.fileListLayout.setSpacing(true);
        this.fileListLayout.setPadding(false);
        this.fileListLayout.setWidthFull();

        if (isImageOnly) {
            this.hapusBtn = new Button("Hapus", e -> {
                for (String stored : new ArrayList<>(currentFiles)) {
                    fileStorageService.deleteFile(stored);
                }
                currentFiles.clear();
                updateValueAndUI();
                Notification.show("Foto berhasil dihapus", 3000, Notification.Position.BOTTOM_END);
            });
            this.hapusBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            this.hapusBtn.setWidth("50%");
            this.hapusBtn.getStyle().set("cursor", "pointer");

            HorizontalLayout buttonRow = new HorizontalLayout(this.upload, this.hapusBtn);
            buttonRow.setWidth("160px"); // Sejajar dengan lebar kotak foto
            buttonRow.setSpacing(true);
            buttonRow.setPadding(false);
            buttonRow.setAlignItems(FlexComponent.Alignment.CENTER);

            this.mainLayout = new VerticalLayout(this.fileListLayout, buttonRow);
            this.mainLayout.setSpacing(true);
            this.mainLayout.setPadding(false);
            this.mainLayout.setWidth("160px");
        } else {
            this.mainLayout = new VerticalLayout(this.upload, this.fileListLayout);
            this.mainLayout.setSpacing(true);
            this.mainLayout.setPadding(false);
            this.mainLayout.setWidthFull();
        }

        add(this.mainLayout);
    }

    @Override
    protected String generateModelValue() {
        return FileStorageService.formatDelimitedFilenames(currentFiles);
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        currentFiles.clear();
        if (newPresentationValue != null && !newPresentationValue.trim().isEmpty()) {
            currentFiles.addAll(FileStorageService.parseDelimitedFilenames(newPresentationValue));
        }
        refreshFileList();
    }

    private void updateValueAndUI() {
        setModelValue(generateModelValue(), true);
        refreshFileList();
        // Bersihkan daftar di dalam komponen Upload setelah dipindahkan ke fileListLayout
        upload.clearFileList();
    }

    private void refreshFileList() {
        fileListLayout.removeAll();
        if (isImageOnly) {
            Div photoFrame = new Div();
            photoFrame.getStyle()
                    .set("width", "160px")
                    .set("height", "200px")
                    .set("border", "2px solid #cbd5e1")
                    .set("border-radius", "6px")
                    .set("background-color", "#f8fafc")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("overflow", "hidden")
                    .set("position", "relative")
                    .set("box-shadow", "inset 0 2px 4px 0 rgba(0, 0, 0, 0.05)");

            if (!currentFiles.isEmpty()) {
                String storedFilename = currentFiles.get(0);
                DownloadHandler resource = fileStorageService.loadFileAsResource(storedFilename);
                String displayFilename = fileStorageService.getDisplayFilename(storedFilename);
                if (resource != null) {
                    Image img = new Image();
                    img.setSrc(resource);
                    img.getStyle()
                            .set("width", "100%")
                            .set("height", "100%")
                            .set("object-fit", "cover")
                            .set("cursor", "pointer")
                            .set("transition", "transform 0.2s");
                    img.setTitle("Klik untuk memperbesar foto: " + displayFilename);
                    img.addClickListener(e -> showImagePreviewDialog(storedFilename, displayFilename));
                    photoFrame.add(img);
                } else {
                    Icon errIcon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
                    errIcon.setSize("36px");
                    errIcon.setColor("#ef4444");
                    photoFrame.add(errIcon);
                    photoFrame.setTitle("File fisik tidak ditemukan di server");
                }
            } else {
                Icon placeholder = VaadinIcon.USER.create();
                placeholder.setSize("64px");
                placeholder.setColor("#cbd5e1");
                photoFrame.add(placeholder);
                photoFrame.setTitle("Belum ada foto");
            }

            fileListLayout.add(photoFrame);
            return;
        }

        if (currentFiles.isEmpty()) {
            return;
        }

        for (String storedFilename : new ArrayList<>(currentFiles)) {
            HorizontalLayout card = createFileCard(storedFilename);
            fileListLayout.add(card);
        }
    }

    private HorizontalLayout createFileCard(String storedFilename) {
        HorizontalLayout card = new HorizontalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setSpacing(true);
        card.setWidthFull();
        card.getStyle()
                .set("padding", "8px 12px")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "6px")
                .set("background-color", "#f8fafc")
                .set("box-shadow", "0 1px 2px rgba(0, 0, 0, 0.05)");

        // 1. Nama File
        String displayFilename = fileStorageService.getDisplayFilename(storedFilename);

        // 2. Icon atau Thumbnail Preview
        Component previewComp;
        DownloadHandler resource = fileStorageService.loadFileAsResource(storedFilename);
        if (resource != null && (this.isImageOnly || fileStorageService.isImageFile(storedFilename))) {
            Image img = new Image();
            img.setSrc(resource);
            img.setWidth(this.isImageOnly ? "56px" : "40px");
            img.setHeight(this.isImageOnly ? "56px" : "40px");
            img.getStyle()
                    .set("object-fit", "cover")
                    .set("border-radius", "6px")
                    .set("border", "1px solid #cbd5e1")
                    .set("cursor", "pointer")
                    .set("transition", "all 0.2s ease");
            img.setTitle("Klik untuk memperbesar foto barang: " + displayFilename);
            img.addClickListener(e -> showImagePreviewDialog(storedFilename, displayFilename));
            previewComp = img;
        } else {
            Icon icon = VaadinIcon.FILE_TEXT.create();
            icon.setSize("24px");
            icon.setColor("#3b82f6");
            previewComp = icon;
        }

        Span nameSpan = new Span(displayFilename);
        nameSpan.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "500")
                .set("color", "#1e293b")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");
        nameSpan.setTitle(displayFilename);

        HorizontalLayout infoLayout = new HorizontalLayout(previewComp, nameSpan);
        infoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        infoLayout.setSpacing(true);
        infoLayout.getStyle().set("flex-grow", "1").set("overflow", "hidden");

        // 3. Tombol Download / Lihat
        Button downloadBtn = new Button("Unduh", VaadinIcon.DOWNLOAD.create());
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        downloadBtn.getStyle().set("cursor", "pointer");

        Anchor downloadAnchor = new Anchor();
        if (resource != null) {
            downloadAnchor.setHref(resource);
            downloadAnchor.getElement().setAttribute("download", true);
            downloadAnchor.getElement().setAttribute("target", "_blank");
        } else {
            downloadBtn.setEnabled(false);
            downloadBtn.setTooltipText("File fisik tidak ditemukan di server");
            downloadBtn.getElement().setAttribute("title", "File fisik tidak ditemukan di server");
        }
        downloadAnchor.add(downloadBtn);

        // 4. Tombol Hapus
        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            currentFiles.remove(storedFilename);
            fileStorageService.deleteFile(storedFilename);
            updateValueAndUI();
            Notification.show("File dihapus: " + displayFilename, 3000, Notification.Position.BOTTOM_END);
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteBtn.getStyle().set("cursor", "pointer");
        deleteBtn.setVisible(!isReadOnly());

        card.add(infoLayout, downloadAnchor, deleteBtn);
        return card;
    }

    private void showImagePreviewDialog(String storedFilename, String displayFilename) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Preview Foto: " + displayFilename);
        dialog.setWidth("auto");
        dialog.setMaxWidth("85vw");
        dialog.setMaxHeight("85vh");

        DownloadHandler resource = fileStorageService.loadFileAsResource(storedFilename);
        Image fullImg = new Image();
        fullImg.setSrc(resource);
        fullImg.getStyle()
                .set("max-width", "100%")
                .set("max-height", "65vh")
                .set("object-fit", "contain")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.1)");

        VerticalLayout layout = new VerticalLayout(fullImg);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.setSizeFull();
        dialog.add(layout);

        Button closeBtn = new Button("Tutup", VaadinIcon.CLOSE.create(), e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeBtn);

        dialog.open();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        getElement().setProperty("readonly", readOnly);
        this.upload.setVisible(!readOnly);
        if (this.hapusBtn != null) {
            this.hapusBtn.setVisible(!readOnly);
        }
        refreshFileList();
    }

    @Override
    public boolean isReadOnly() {
        return getElement().getProperty("readonly", false);
    }
}
