---
name: vaadin-extra-toolbar
description: Panduan arsitektur dan implementasi fitur Extra Toolbar (Form Action) dan mekanisme 2-Stage Copy Data pada Vaadin Flow & Spring Boot ERP.
---

# Panduan Fitur Extra Toolbar & 2-Stage Copy Data (Vaadin ERP)

Dokumen ini adalah *Skill* yang menjelaskan arsitektur dan cara kerja fitur **Extra Toolbar** (Tombol Aksi Kustom) dan **2-Stage Copy Data** di sistem Vaadin ERP. Saat Anda ditugaskan untuk memperbaiki, memodifikasi, atau mencari tahu tentang *Action Builder* atau *Dynamic Picker*, bacalah panduan ini terlebih dahulu.

## 1. Konsep Dasar (Extra Toolbar)
*Extra Toolbar* memungkinkan *developer* untuk menambahkan tombol aksi kustom ke bagian atas (*toolbar*) sebuah form.
- Metadata disimpan di `meta_form_action` (`FormActionMeta.java`).
- Konfigurasi ini dilakukan melalui halaman antarmuka **Action Builder** (`FormActionBuilderView.java`).
- Ketika di-klik, tombol aksi ini akan memunculkan sebuah **Popup Picker** (`DynamicPickerPopupDialog.java`) yang berisi daftar/grid data (*lookup*) yang diambil dari `sourceLovCode`.
- Baris data yang dipilih (*checked*) di dalam grid tersebut kemudian akan di-*copy* ke form utama berdasarkan relasi `targetMapping`.

## 2. Arsitektur "2-Stage Copy"
Fitur ini dirancang untuk mengatasi *use case* di mana popup picker menampilkan rangkuman/header data (misal: *Master Item* atau *Header PO*), namun data yang sebenarnya ingin dikopi ke dalam form transaksi adalah **detail/child** dari baris yang dipilih tersebut (misal: *BOM Detail* atau *PO Lines*).

### Properti Tambahan pada `FormActionMeta`:
1. `copySourceLovCode`: Menyimpan referensi tabel/form anak (*Data Source 2*). Contoh: `BOM_DETAIL`.
2. `copyFilterMapping`: Menyimpan format JSON untuk memfilter `copySourceLovCode` berdasarkan baris yang baru saja diklik pada *Data Source 1*. Contoh: `{"item_id": "picked.id"}`.

### Alur Eksekusi 2-Stage (di `DynamicPickerPopupDialog.java`):
- Jika `copySourceLovCode` **kosong**, sistem melakukan 1-Stage Copy (*backward compatible*): Baris yang dipilih langsung diserahkan ke *callback* form utama.
- Jika `copySourceLovCode` **diisi**:
  1. Sistem melooping tiap baris yang diceklis (kita sebut `pickedRecord`).
  2. Sistem menjalankan `dynamicDataService.fetchLovDataWithActionFilters(copySourceLovCode, copyFilterMapping, headerRecord, pickedRecord, search)`.
  3. Hasil tarikan anak-anak data dari setiap `pickedRecord` dikumpulkan menjadi satu `List` gabungan (*aggregated*).
  4. List gabungan itulah yang diserahkan ke *callback* form utama untuk diproses oleh `targetMapping`.

## 3. Konteks Parsing Filter (di `DynamicDataService.java`)
Saat mengevaluasi filter mapping berformat JSON, *DynamicDataService* memiliki sistem deteksi konteks:
- **`header.field`** (Contoh: `{"customer": "header.customer_id"}`): Mengambil nilai dari record/form **utama** yang sedang aktif/berjalan di belakang popup.
- **`picked.field`** (Contoh: `{"item_id": "picked.id"}`): Mengambil nilai dari record yang **baru saja dipilih** di dalam *Popup Picker*.

### Pengambilan Nested Object (Bandbox)
Fungsi `getCaseInsensitiveValue` secara otomatis me-*resolve* notasi titik (`.`) berlapis (*dot-notation*).
Jika `pickedRecord` menyimpan field `customer` yang isinya berupa `Map<String, Object>` (seperti halnya komponen Bandbox Picker), Anda cukup menggunakan:
`{"cust_id": "picked.customer.id"}`
Sistem akan otomatis masuk ke objek `customer` lalu mengekstrak nilai `id`-nya.

## 4. Groovy Scripting Engine & Action DSL Cheatsheet (`ScriptExecutorService`)
Selain tipe aksi `LOV_POPUP`, *Extra Toolbar* mendukung tipe `GROOVY_SCRIPT` yang dijalankan via `ScriptExecutorService.java`. Berikut adalah **Cheat Sheet DSL & Context Methods** yang tersedia secara langsung di dalam script:

### A. Inspeksi Data & Debugging (`msgBox`)
- `msgBox(Object data)` atau `msgBox(String title, Object data)`:
  Memunculkan jendela modal bergaya *Dark Code Viewer* berukuran 650x450px (resizable & draggable) dengan kemampuan *pretty-print JSON* otomatis. Sangat disarankan untuk men-debug variabel `header` maupun `selectedRows` sebelum atau saat mengeksekusi aksi.

### B. Manipulasi & Pembersihan Form UI (`clearForm`, `setElementValue`, `refreshForm`)
- `clearForm()`: Mengosongkan/me-reset seluruh isian form aktif di layar (Bandbox, Textbox, Numeric, dll.) dan merefresh tampilan UI seketika.
- `setElementValue(String fieldName, Object val)`: Mengosongkan (`null` atau `0`) atau mengisi field tertentu pada form, sekaligus memperbarui `headerBean` dan merefresh UI layar browser.
- `refreshForm()`: Memperbarui tampilan komponen UI di layar browser setelah objek `header` dimodifikasi secara dinamis di dalam Groovy (`header.quantity = 0; refreshForm()`).

### C. SmartHeaderNode & Nested Bandbox Properties
- Objek `header` di-binding menggunakan `SmartHeaderNode`. Kelas pintar ini bertindak ganda:
  1. Sebagai angka (`Number/Long`): Ketika dipanggil langsung seperti `header.tsproductionorderid` (mengembalikan `77`).
  2. Sebagai `Map` bersarang: Ketika dipanggil menggunakan notasi titik seperti `header.tsproductionorderid.idno` atau `header.tsproductionorderid.ocproduct`.
- Sintaks yang di-copy user dari tabel **Debug Context Form & Header (Filter Mapping Inspector)** dijamin 100% langsung bekerja tanpa error `No such property` atau `null`.

### D. Akses Database Ringan (`db.getValue`, `db.find`, `executeProcedure`)
- `db.getValue("SELECT col FROM tbl WHERE id = ?", arg)`: Mengambil 1 nilai scalar secara aman (Parameterized Query).
- `db.find("table_name", "key_col", val)`: Mengambil 1 baris record utuh berformat `Map<String, Object>`.
- `executeProcedure(Object procRef, Closure callback, String jsonParams, String userId)`: Mengeksekusi Stored Procedure dan mengembalikan status sukses boolean ke dalam closure `callback`.

### E. Dialog & Notifikasi UI (`showYesNoDialog`, `showSuccess`, `showError`, `showMainTab`)
- `showYesNoDialog("Judul", "Pesan", callback)`: Dialog konfirmasi Yes/No bergaya Lumo.
- `showSuccess("Judul", "Pesan")` & `showError("Judul", "Pesan")`: Notifikasi Lumo di top-right layar.
- `showMainTab(tabId, "Judul Tab", url, extra)`: Navigasi antar tab menu di ERP setelah aksi selesai.

## 5. Lokasi File Kunci
- `com.vaadinerp.meta.FormActionMeta`: Entitas database.
- `com.vaadinerp.views.FormActionBuilderView`: UI konfigurasi & Cheat Sheet Dialog.
- `com.vaadinerp.components.DynamicPickerPopupDialog`: Komponen popup (logika 2-stage berada di tombol `btnOk`).
- `com.vaadinerp.service.DynamicDataService`: Logic *backend* (fokus pada fungsi `fetchLovDataWithActionFilters` dan `evaluateFilterMappingDiagnostic`).
- `com.vaadinerp.service.ScriptExecutorService`: Engine eksekutor Groovy & binding DSL macro.
- `com.vaadinerp.service.ActionContext`: Konteks aksi yang menyediakan implementasi metode UI/DB untuk Groovy.

> [!NOTE]
> Ingatlah arsitektur dua tahap (*2-stage*) dan kelengkapan DSL Groovy ini ketika ada kebutuhan untuk menambah tingkat logika penyalinan form atau men-debug eksekusi script aksi kustom di Extra Toolbar.
