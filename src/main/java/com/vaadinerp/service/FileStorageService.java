package com.vaadinerp.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir:./uploads}")
    private String uploadDirStr;

    private Path uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "txt", "csv", "jpg", "jpeg", "png", "webp", "zip", "rar", "7z"));

    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "webp", "gif", "bmp"));

    @PostConstruct
    public void init() {
        try {
            this.uploadDir = Paths.get(uploadDirStr).toAbsolutePath().normalize();
            if (!Files.exists(this.uploadDir)) {
                Files.createDirectories(this.uploadDir);
                log.info("Direktori upload berhasil dibuat: {}", this.uploadDir);
            }
        } catch (Exception e) {
            log.error("Gagal menginisialisasi direktori upload: {}", uploadDirStr, e);
            throw new RuntimeException("Tidak dapat membuat direktori penyimpanan file!", e);
        }
    }

    /**
     * Menyimpan file dari input stream ke disk dengan penamaan UUID aman.
     *
     * @param inputStream      Stream data file
     * @param originalFilename Nama asli file
     * @return Nama file unik yang tersimpan di disk
     */
    public String storeFile(InputStream inputStream, String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "unnamed_file";
        }

        // 1. Sanitasi & normalisasi nama file asli
        String cleanName = sanitizeFilename(originalFilename);

        // 2. Validasi ekstensi
        String ext = getFileExtension(cleanName);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new SecurityException("Akses Ditolak: Ekstensi file '." + ext + "' tidak diizinkan untuk diunggah!");
        }

        // 3. Generate UUID prefix agar unik dan mencegah overwrite
        String storedFilename = UUID.randomUUID().toString().substring(0, 8) + "_" + cleanName;
        Path targetLocation = this.uploadDir.resolve(storedFilename);

        try {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File berhasil disimpan: {} -> {}", originalFilename, targetLocation);
            return storedFilename;
        } catch (IOException e) {
            log.error("Gagal menyimpan file: {}", originalFilename, e);
            throw new RuntimeException("Gagal menyimpan file " + originalFilename + ". Silakan coba lagi!", e);
        }
    }

    /**
     * Memuat file dari disk sebagai DownloadHandler Vaadin untuk preview atau
     * download.
     */
    public DownloadHandler loadFileAsResource(String storedFilename) {
        if (storedFilename == null || storedFilename.trim().isEmpty()) {
            return null;
        }

        // Mencegah path traversal
        Path filePath = this.uploadDir.resolve(storedFilename).normalize();
        if (!filePath.startsWith(this.uploadDir)) {
            throw new SecurityException("Akses Ditolak: Path traversal terdeteksi pada filename: " + storedFilename);
        }

        File file = filePath.toFile();
        if (!file.exists()) {
            log.warn("File tidak ditemukan di disk: {}", filePath);
            return null;
        }

        // Buat DownloadHandler (pengganti StreamResource yang deprecated)
        String displayFilename = getDisplayFilename(storedFilename);
        String mimeType = getMimeType(storedFilename);
        return DownloadHandler.fromInputStream(event -> {
            try {
                return new DownloadResponse(
                        new FileInputStream(file),
                        displayFilename,
                        mimeType,
                        file.length());
            } catch (FileNotFoundException e) {
                log.error("File hilang saat streaming: {}", filePath, e);
                return DownloadResponse.error(404);
            }
        }).inline();
    }

    /**
     * Menentukan MIME type berdasarkan ekstensi file.
     */
    public String getMimeType(String filename) {
        String ext = getFileExtension(filename).toLowerCase();
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv" -> "text/csv";
            case "txt" -> "text/plain";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "7z" -> "application/x-7z-compressed";
            default -> "application/octet-stream";
        };
    }

    /**
     * Menghapus file fisik dari disk.
     */
    public boolean deleteFile(String storedFilename) {
        if (storedFilename == null || storedFilename.trim().isEmpty()) {
            return false;
        }
        try {
            Path filePath = this.uploadDir.resolve(storedFilename).normalize();
            if (!filePath.startsWith(this.uploadDir)) {
                log.warn("Upaya hapus di luar direktori upload ditolak: {}", storedFilename);
                return false;
            }
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File berhasil dihapus dari disk: {}", filePath);
            }
            return deleted;
        } catch (Exception e) {
            log.warn("Gagal menghapus file fisik '{}': {}", storedFilename, e.getMessage());
            return false;
        }
    }

    /**
     * Menghapus banyak file sekaligus berdasarkan string koma-delimited (digunakan
     * untuk cascade delete record DB).
     */
    public void deleteFilesByDelimitedString(String delimitedFilenames) {
        if (delimitedFilenames == null || delimitedFilenames.trim().isEmpty()) {
            return;
        }
        List<String> files = parseDelimitedFilenames(delimitedFilenames);
        for (String file : files) {
            deleteFile(file);
        }
    }

    /**
     * Memeriksa apakah sebuah nama file adalah file gambar berdasarkan ekstensinya.
     */
    public boolean isImageFile(String filename) {
        if (filename == null)
            return false;
        String ext = getFileExtension(filename).toLowerCase();
        return IMAGE_EXTENSIONS.contains(ext);
    }

    /**
     * Mengambil nama file asli tanpa prefix UUID (untuk ditampilkan ke pengguna).
     * Contoh: "a1b2c3d4_faktur_pajak.pdf" -> "faktur_pajak.pdf"
     */
    public String getDisplayFilename(String storedFilename) {
        if (storedFilename == null)
            return "";
        int idx = storedFilename.indexOf('_');
        if (idx == 8 && storedFilename.length() > 9) { // Pola 8 karakter UUID + _
            return storedFilename.substring(idx + 1);
        }
        return storedFilename;
    }

    /**
     * Sanitasi nama file: ubah ke lowercase, ganti spasi dengan _, hapus karakter
     * aneh.
     */
    public String sanitizeFilename(String originalName) {
        String name = originalName.replace("\\", "/");
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        name = name.trim().toLowerCase();
        // Ganti spasi dengan underscore dan hapus karakter selain huruf, angka, titik,
        // strip, underscore
        name = name.replaceAll("\\s+", "_");
        name = name.replaceAll("[^a-z0-9\\.\\-_]", "");
        if (name.isEmpty() || name.startsWith(".")) {
            name = "file_" + System.currentTimeMillis() + (name.contains(".") ? name : ".bin");
        }
        return name;
    }

    /**
     * Mengambil ekstensi file dari nama file.
     */
    public String getFileExtension(String filename) {
        if (filename == null)
            return "";
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx >= 0 && dotIdx < filename.length() - 1) {
            return filename.substring(dotIdx + 1);
        }
        return "";
    }

    /**
     * Memecah string koma-delimited menjadi list nama file.
     */
    public static List<String> parseDelimitedFilenames(String delimitedStr) {
        if (delimitedStr == null || delimitedStr.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(delimitedStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Menggabungkan list nama file menjadi string koma-delimited.
     */
    public static String formatDelimitedFilenames(List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            return "";
        }
        return filenames.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}
