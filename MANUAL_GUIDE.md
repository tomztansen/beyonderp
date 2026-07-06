# Buku Panduan Lengkap (Manual Guide): Vaadin ERP

**Vaadin ERP** adalah platform *Enterprise Resource Planning* (ERP) modern berbasis konsep **Low-Code / Metadata-Driven Architecture**. Dibangun menggunakan ekosistem teknologi yang tangguh yaitu **Java 17+, Spring Boot 3, Vaadin Flow 24, dan PostgreSQL**, sistem ini memungkinkan pembuatan, penyesuaian, dan pengelolaan aplikasi bisnis skala enterprise secara dinamis tanpa perlu melakukan *hard-code* pada setiap form atau tampilan UI.

---

## Daftar Isi
1. [Arsitektur & Konsep Dasar](#1-arsitektur--konsep-dasar)
2. [Panduan Pengguna (User Manual)](#2-panduan-pengguna-user-manual)
   - [2.1 Login & Navigasi Portal](#21-login--navigasi-portal)
   - [2.2 Pengoperasian Form Tunggal (Single Form)](#22-pengoperasian-form-tunggal-single-form)
   - [2.3 Pengoperasian Form Master-Detail (Header-Detail)](#23-pengoperasian-form-master-detail-header-detail)
   - [2.4 Pencarian Data & Referensi (LOV & Bandbox)](#24-pencarian-data--referensi-lov--bandbox)
   - [2.5 Pengunggahan Dokumen & Gambar (Multi-File Upload)](#25-pengunggahan-dokumen--gambar-multi-file-upload)
   - [2.6 Audit Log & Riwayat Perubahan Data](#26-audit-log--riwayat-perubahan-data)
3. [Panduan Administrator & Builder (Low-Code Studio)](#3-panduan-administrator--builder-low-code-studio)
   - [3.1 Table Designer (Manajemen Skema DB)](#31-table-designer-manajemen-skema-db)
   - [3.2 Form Builder (Perancang Tampilan Form)](#32-form-builder-perancang-tampilan-form)
   - [3.3 LOV & Lookup Builder (Manajemen Data Master)](#33-lov--lookup-builder-manajemen-data-master)
   - [3.4 Report Builder (Perancang Laporan Dinamis)](#34-report-builder-perancang-laporan-dinamis)
   - [3.5 User Authority & Security (Manajemen Hak Akses)](#35-user-authority--security-manajemen-hak-akses)
4. [Panduan Pengembang (Developer Guide)](#4-panduan-pengembang-developer-guide)
   - [4.1 Standar Arsitektur File Upload & Storage](#41-standar-arsitektur-file-upload--storage)
   - [4.2 Struktur Tabel Referensi Lookup (mhlookup & mdlookup)](#42-struktur-tabel-referensi-lookup-mhlookup--mdlookup)
   - [4.3 Kompilasi & Menjalankan Aplikasi Lokal](#43-kompilasi--menjalankan-aplikasi-lokal)

---

## 1. Arsitektur & Konsep Dasar

Berbeda dengan aplikasi konvensional di mana setiap halaman (misal: Form Faktur, Form Pembelian, Form Barang) ditulis dalam file kode Java/HTML terpisah, **Vaadin ERP** menggunakan **Metadata-Driven Architecture**:
* **Definisi Form:** Disimpan di dalam tabel database (`dynamic.form_header` dan `dynamic.form_detail`). Metadata ini mengatur tata letak, label kolom, tipe input (angka, teks, tanggal, combobox, file), validasi, serta query SQL yang dijalankan.
* **UI Render Engine:** Kelas [GenericFormView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/GenericFormView.java) (untuk form tunggal) dan [GenericMasterDetailFormView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/GenericMasterDetailFormView.java) (untuk form transaksi master-detail) bertindak sebagai *engine* yang membaca metadata tersebut dan merender tampilan UI Vaadin secara *real-time*.
* **Dynamic Widget Engine:** [ComponentFactory.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/components/ComponentFactory.java) menghasilkan komponen UI yang tepat berdasarkan spesifikasi metadata di database.

---

## 2. Panduan Pengguna (User Manual)

### 2.1 Login & Navigasi Portal
1. **Masuk ke Sistem:** Buka browser dan akses URL sistem (default: `http://localhost:8080`). Anda akan diarahkan ke halaman [LoginView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/LoginView.java).
2. **Dashboard Portal:** Setelah berhasil login, Anda akan masuk ke [PortalView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/PortalView.java). Halaman ini menampilkan menu navigasi di sisi kiri (atau atas) yang dikelompokkan berdasarkan modul hak akses Anda (misal: *Procurement*, *Sales*, *Finance*, *System Admin*).

### 2.2 Pengoperasian Form Tunggal (Single Form)
Form tunggal ([GenericFormView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/GenericFormView.java)) digunakan untuk mengelola data master sederhana (seperti Daftar Pelanggan, Daftar Supplier, atau Gudang).
* **Melihat & Mencari Data:** Tabel utama menampilkan daftar data aktif. Gunakan kolom filter di bagian atas tabel untuk mencari berdasarkan kata kunci tertentu.
* **Menambah Data Baru:** Klik tombol **"New"** / **"Tambah"** pada [StandardActionToolbar.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/components/StandardActionToolbar.java). Form input akan terbuka di bagian bawah atau di panel samping.
* **Menyimpan Data:** Isi kolom yang wajib diisi (biasanya bertanda bintang merah), lalu klik **"Save"** / **"Simpan"**.
* **Menghapus Data:** Pilih baris data pada tabel, lalu klik tombol **"Delete"** / **"Hapus"**. Sistem akan meminta konfirmasi sebelum menghapus data.

### 2.3 Pengoperasian Form Master-Detail (Header-Detail)
Form Master-Detail ([GenericMasterDetailFormView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/GenericMasterDetailFormView.java)) digunakan untuk transaksi kompleks seperti *Purchase Order (PO)*, *Faktur Penjualan*, atau *Jurnal Akuntansi*.
* **Bagian Header (Master):** Berisi informasi umum transaksi (misal: Nomor Transaksi, Tanggal, Supplier, Keterangan).
* **Bagian Detail (Subform Grid):** Dikelola oleh komponen [SubformGridField.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/components/SubformGridField.java) yang berada di bagian bawah Header.
  * **Menambah Baris Rincian:** Klik tombol **"Add Row"** / **"+ Detail"** pada grid subform. Baris baru akan muncul dan siap diedit secara langsung (*inline editing* atau via modal).
  * **Fitur Auto-Populate:** Saat Anda memilih suatu item/produk di kolom detail atau memilih kode tertentu di Header, sistem secara otomatis dapat mengisi kolom-kolom lain terkait (seperti Satuan/UOM, Harga Satuan, atau Spesifikasi Barang) dengan mengambil nilai langsung dari tabel master referensi.
  * **Menghapus Baris Rincian:** Klik ikon tempat sampah/hapus pada baris detail yang ingin dibatalkan.

### 2.4 Pencarian Data & Referensi (LOV & Bandbox)
Untuk kolom yang memerlukan pemilihan data dari tabel referensi (misal: memilih Kode Produk, Satuan UOM, atau Kategori Pengadaan), sistem menyediakan widget canggih:
* **LOV ComboBox / Select ([LovComboBox.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/components/LovComboBox.java)):** Dropdown interaktif yang mendukung pencarian cepat dengan mengetikkan kata kunci.
* **Bandbox Field ([BandboxField.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/components/BandboxField.java)):** Komponen pencarian lanjutan dengan ikon kaca pembesar. Saat diklik, akan muncul jendela *popup* ([DynamicPickerPopupDialog.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/components/DynamicPickerPopupDialog.java)) berisi tabel lengkap dengan fitur filter multi-kolom dan pagination, memudahkan pencarian pada dataset berukuran besar (ribuan baris).

### 2.5 Pengunggahan Dokumen & Gambar (Multi-File Upload)
Komponen [FileUploadField.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/components/FileUploadField.java) memungkinkan Anda mengunggah lampiran (seperti bukti transfer, foto barang, kontrak, atau faktur pajak PDF).
* **Cara Unggah:** Drag-and-drop file ke area upload, atau klik tombol **"Browse/Pilih File"**. Anda dapat mengunggah lebih dari satu file sekaligus jika diizinkan oleh form.
* **Keamanan File:** Sistem otomatis memformat nama file Anda menjadi huruf kecil dan menambahkan kode pengaman acak (UUID) agar file tidak tertimpa oleh file pengguna lain.
* **Melihat & Unduh:** File yang telah diunggah akan muncul sebagai daftar tautan/chip. Klik nama file untuk melihat pratinjau (*preview*) atau mengunduhnya.

### 2.6 Audit Log & Riwayat Perubahan Data
Setiap perubahan pada kolom penting dicatat secara otomatis oleh sistem. Untuk melihat siapa yang mengubah data, nilai lama (*old value*), dan nilai baru (*new value*), buka menu **Audit Trail** atau akses [FieldAuditLogView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/FieldAuditLogView.java).

---

## 3. Panduan Administrator & Builder (Low-Code Studio)

Modul **Low-Code Studio** diperuntukkan bagi Administrator Sistem atau Analis Bisnis untuk merancang atau memodifikasi modul ERP tanpa menulis kode pemrograman.

### 3.1 Table Designer (Manajemen Skema DB)
Akses [TableDesignerView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/TableDesignerView.java) untuk membuat tabel baru di database PostgreSQL atau memodifikasi kolom tabel yang ada (menambah kolom, menentukan tipe data seperti `VARCHAR`, `INTEGER`, `NUMERIC`, `TIMESTAMP`, serta primary/foreign key).

### 3.2 Form Builder (Perancang Tampilan Form)
Akses [FormBuilderView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/FormBuilderView.java) untuk membuat antarmuka form:
1. **Pilih Tabel Utama:** Tentukan tabel database yang akan diikat ke form.
2. **Konfigurasi Kolom (Form Detail):** Tentukan kolom mana saja yang akan ditampilkan, label teksnya, urutan tampilan (sequence), tipe widget input (TextBox, ComboBox, Bandbox, DatePicker, FileUpload), serta apakah kolom tersebut wajib diisi (*required*) atau *read-only*.
3. **Konfigurasi Master-Detail / Subform:** Hubungkan form Header dengan form Detail menggunakan relasi Foreign Key untuk menciptakan transaksi subform grid terpadu.

### 3.3 LOV & Lookup Builder (Manajemen Data Master)
Akses [LovBuilderView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/LovBuilderView.java) untuk mengatur kamus referensi sistem:
* **Master Kategori (`dynamic.mhlookup`):** Membuat kategori referensi baru (misal: `MAS0020` untuk *Procurement Category*, `MAS0003` untuk *UOM*).
* **Master Detail (`dynamic.mdlookup`):** Menambahkan daftar rincian untuk setiap kategori tersebut yang nantinya akan muncul sebagai pilihan di ComboBox atau Bandbox pada form transaksi.

### 3.4 Report Builder (Perancang Laporan Dinamis)
Akses [ReportBuilderView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/ReportBuilderView.java) untuk merancang laporan bisnis:
1. Tulis query SQL atau pilih view database untuk sumber data laporan.
2. Tentukan parameter filter laporan (misal: Filter Periode Tanggal, Filter Gudang).
3. Atur format kolom keluaran dan ekspor ke Excel atau PDF yang dapat dilihat melalui [ReportViewerView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/ReportViewerView.java).

### 3.5 User Authority & Security (Manajemen Hak Akses)
Akses [UserAuthorityAdminView.java](file:///d:/PROJECT%20GMN/vaadinerp/src/main/java/com/vaadinerp/views/UserAuthorityAdminView.java) untuk mengelola keamanan:
* **Manajemen Role/Peran:** Buat grup hak akses (misal: *Admin*, *Staff Gudang*, *Manajer Keuangan*).
* **Matriks Hak Akses:** Tentukan menu, form, atau laporan mana saja yang dapat dibaca (*Read*), ditambah (*Create*), diedit (*Update*), atau dihapus (*Delete*) oleh setiap Role.

---

## 4. Panduan Pengembang (Developer Guide)

### 4.1 Standar Arsitektur File Upload & Storage
Sesuai dengan aturan arsitektur wajib proyek pada aturan pengembang (`AGENTS.md`), setiap fitur pengunggahan file dan penyimpanan dokumen wajib mematuhi 4 pilar arsitektur berikut:

1. **Separate Storage & DB Reference:**
   * **JANGAN PERNAH** menyimpan data binary file (`BLOB` / `BYTEA`) langsung ke dalam tabel database.
   * Simpan file fisik di disk server (pada folder `./uploads` atau konfigurasi direktori via properti `app.upload.dir`).
   * Simpan **hanya nama file** atau *relative path*-nya pada kolom database bertipe `TEXT` atau `VARCHAR`.
2. **Sanitasi & Penamaan UUID Aman:**
   * Semua file yang diunggah **wajib melewati proses sanitasi**: nama file diubah ke huruf kecil (*lowercase*), spasi dan karakter khusus diganti dengan garis bawah (*underscore* `_`).
   * Setiap file **wajib diberi awalan 8 karakter acak UUID** (contoh: `a1b2c3d4_faktur_pajak.pdf`).
   * Tujuan: Mencegah penimpaan file secara tidak sengaja (*overwrite*) dan menjamin kompatibilitas 100% antara sistem operasi Windows (*case-insensitive*) dan Linux (*case-sensitive*).
3. **Multi-File Delimited String:**
   * Untuk field atau kolom yang mendukung pengunggahan banyak file sekaligus, simpan daftar nama file sebagai **string terpisah koma** (`file1.pdf,file2.jpg`) pada kolom bertipe `TEXT` di PostgreSQL.
4. **Cascade Delete File Fisik:**
   * Saat melakukan penghapusan record database melalui service backend (seperti `DynamicDataService.deleteData`), baik pada data tunggal, master-detail, maupun subform, sistem **wajib memeriksa dan menghapus file fisik terkait di server**.
   * Tujuan: Mencegah penumpukan file sampah yang tidak terpakai (*disk space leak*).

### 4.2 Struktur Tabel Referensi Lookup (`mhlookup` & `mdlookup`)
Sistem menggunakan dua tabel utama di skema `dynamic` untuk menampung seluruh kamus referensi master (LOV):

* **`dynamic.mhlookup` (Master Header Lookup):**
  * `id`: Primary Key (BigInt / Serial).
  * `category_code`: Kode unik kategori (maksimal 50 karakter, misal: `MAS0001`, `MAS0020`).
  * `category_name`: Nama deskriptif kategori (maksimal 255 karakter, misal: `Master : Procurement Category`).
  * `status`: Status aktif (`true`/`false`).
* **`dynamic.mdlookup` (Master Detail Lookup):**
  * `id`: Primary Key (BigInt / Serial).
  * `global_category_id`: Foreign Key mengarah ke `dynamic.mhlookup(id)`.
  * `code`: Kode rincian item (maksimal 50 karakter, misal: `3.13-001`, `1/2 in`).
  * `name` / `codetext`: Teks tampilan atau nama barang/referensi (maksimal 255 karakter).
  * `description`: Keterangan tambahan atau spesifikasi detail.

### 4.3 Kompilasi & Menjalankan Aplikasi Lokal
Proyek ini dikelola menggunakan **Apache Maven**.

**Prasyarat Lingkungan:**
* Java Development Kit (JDK) 17 atau lebih tinggi.
* Apache Maven 3.8+.
* PostgreSQL 14+ (pastikan skema `public` dan `dynamic` telah dibuat).

**Langkah Menjalankan:**
1. Konfigurasi koneksi database pada file `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/vaadinerp?currentSchema=public,dynamic
   spring.datasource.username=postgres
   spring.datasource.password=password_anda
   app.upload.dir=./uploads
   ```
2. Buka terminal di direktori root proyek (`d:\PROJECT GMN\vaadinerp`).
3. Jalankan perintah Maven untuk memulai server pengembangan lokal:
   ```bash
   mvn spring-boot:run
   ```
   *(Atau di OS Windows, Anda dapat langsung mengeksekusi skrip [run.bat](file:///d:/PROJECT%20GMN/vaadinerp/run.bat))*
4. Akses aplikasi melalui browser pada alamat: `http://localhost:8080`.

---
*Dokumen ini dibuat dan diperbarui secara otomatis untuk proyek Vaadin ERP.*
