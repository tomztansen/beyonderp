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

## 4. Lokasi File Kunci
- `com.vaadinerp.meta.FormActionMeta`: Entitas database.
- `com.vaadinerp.views.FormActionBuilderView`: UI konfigurasi.
- `com.vaadinerp.components.DynamicPickerPopupDialog`: Komponen popup (logika 2-stage berada di tombol `btnOk`).
- `com.vaadinerp.service.DynamicDataService`: Logic *backend* (fokus pada fungsi `fetchLovDataWithActionFilters` dan `evaluateFilterMappingDiagnostic`).

> [!NOTE]
> Ingatlah arsitektur dua tahap (*2-stage*) ini ketika ada kebutuhan untuk menambah tingkat logika penyalinan form (misalnya *3-stage copy* di masa depan) atau men-debug masalah *filter mapping* yang tidak ter-*resolve*.
