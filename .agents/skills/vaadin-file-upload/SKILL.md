---
name: vaadin-file-upload
description: Panduan arsitektur dan implementasi fitur pengunggahan file dan gambar dinamis (multi-file) pada Vaadin Flow & Spring Boot ERP.
---

# Vaadin File Upload & Storage Pattern

Pola ini menjelaskan cara kerja integrasi upload file pada sistem ERP dinamis berbasis metadata (`vaadinerp`).

## 1. Komponen Utama
1. **`FileStorageService` (`com.vaadinerp.service`)**:
   - Menangani I/O disk (`storeFile`, `loadFileAsResource`, `deleteFile`, `deleteFilesByDelimitedString`).
   - Memvalidasi ekstensi (*whitelist*: PDF, DOCX, XLSX, JPG, PNG, dll) dan batas ukuran file.
2. **`FileUploadField` (`com.vaadinerp.components`)**:
   - Turunan `CustomField<String>` yang kompatibel dengan *form binding* Vaadin.
   - Menggunakan `Upload` dengan `MultiFileMemoryBuffer` (support hingga 10 file sekaligus).
   - Menampilkan kartu preview (*thumbnail* gambar atau ikon dokumen), tombol unduh (`StreamResource`), dan tombol hapus.
3. **`ComponentFactory` & `FormBuilderView`**:
   - Tipe komponen yang didukung: `FILE_UPLOAD` (dokumen/umum) dan `IMAGE_UPLOAD` (khusus gambar dengan preview thumbnail).
4. **`DynamicDataService`**:
   - Kolom database dibentuk sebagai `TEXT` agar mampu menampung string terpisah koma untuk *multiple files*.

## 2. Contoh Penggunaan di Form
Saat menambahkan field upload pada metadata form:
- Set `componentType` ke `"FILE_UPLOAD"` atau `"IMAGE_UPLOAD"`.
- Pada database, nilai yang disimpan berformat: `"a1b2c3d4_dokumen1.pdf,e5f6g7h8_foto.jpg"`.
