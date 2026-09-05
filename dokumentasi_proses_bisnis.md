# Dokumentasi Proses Bisnis Vaadin ERP

**Sistem:** vaadinerp
**Stack:** Java 21 · Spring Boot 3.3 · Vaadin 24.10
**DB:** PostgreSQL — schema public + dynamic
**Tanggal:** September 2026

Dokumen ini menggambarkan alur proses bisnis yang terimplementasi di sistem ERP vaadinerp, disusun berdasarkan metadata aktual dari database (tabel `meta_form`, `meta_field`, `meta_form_action`, dan `app_menus`). Setiap proses diidentifikasi dari hubungan antar form dan aksi toolbar yang terdaftar di sistem.

---

## 0. Arsitektur Metadata-Driven

Seluruh form aplikasi didefinisikan secara dinamis dari database — bukan dari kode yang dikompilasi. Ini berarti form baru dapat dibuat dan dimodifikasi tanpa perubahan kode, cukup melalui Form Builder.

### Tabel Metadata

| Tabel Metadata | Fungsi | Dikelola Via |
|---|---|---|
| `meta_form` | Definisi form (judul, tabel DB, tipe form) | Form Builder |
| `meta_field` | Definisi field per form (label, komponen, validasi, LOV) | Form Builder |
| `meta_form_action`| Tombol aksi extra toolbar (Groovy script, popup picker, copy)| Action Builder |
| `meta_lov` | List of Values — sumber data ComboBox, Bandbox, Chosenbox | LOV Builder |
| `app_menus` | Navigasi menu hierarki (GROUP → ITEM) | Security Admin |
| `dynamic.*` | Tabel data runtime — di-generate otomatis dari `meta_form` | Table Designer / Form Builder|

### Tipe Form
- **SINGLE** — form tunggal (grid + detail). 
- **MASTER_DETAIL** — header + baris detail (satu tabel + tabel anak). 
- **SCHEDULER_SPLIT** — split view dengan kalender/gantt (dipakai di Scheduling).

---

## I. Data Master

Data master adalah prasyarat seluruh proses bisnis. Harus disiapkan terlebih dahulu sebelum transaksi operasional dapat dijalankan.

### Produk, BOM & Rute

**Urutan Setup Master Data Produk:**
1. Product Catalogue (`PRD_CATALOG` · `msitem`) → 
2. Bill of Material (`BOM_ALL` · `mhbom`/`mdbom`) → 
3. Process Route (`ROUTE_ALL` · `mhroute`/`mdroute`) → 
4. Method Master (`MST_METHOD` / `RBR_MAS_METHOD`)

| Form Code | Judul | Tabel DB | Keterangan |
|---|---|---|---|
| `PRD_CATALOG` | Product Catalogue | `msitem` | Master item/produk — kode, nama, grup, UOM, kategori pengadaan & penjualan |
| `BOM_ALL` | Bill of Material | `mhbom` | Header BOM — referensi ke Product Catalogue dan Resource Group |
| `MST_BOM_DET` | Master BOM Detail | `mdbom` | Baris BOM — komponen material per produk |
| `ROUTE_ALL` | All Route | `mhroute` | Header rute proses — referensi ke Product Catalogue |
| `ROUTE_DET` | Route Detail | `mdroute` | Tahapan rute — urutan resource/mesin per operasi |
| `MST_METHOD` | Method Master (Foundry) | `msmethod` | Parameter metode produksi Foundry — ballast type, pouring type, scope |
| `RBR_MAS_METHOD` | Method Master (Rubber) | `msmethod` | Parameter metode produksi Rubber — sama struktur, plant berbeda |
| `MAT_COMP` | Material Composition | `mdmaterialcomposition`| Komposisi material — grup item, UOM, kategori |
| `MAT_SPEC` | Material Specification | — | Spesifikasi material header |
| `MAT_SPEC_DET` | Material Specification Detail| `mdmaterialspec` | Detail elemen spesifikasi material |
| `MAS_E_SPEC` | Master Element Specification| — | Definisi elemen spesifikasi (referensi di `MAT_SPEC_DET`) |
| `RLS_MAT` | Release Material | — | Proses rilis material ke produksi |

### Sumber Daya & Aset

**Hierarki Sumber Daya Produksi:**
1. Asset Listing (`MST_AST` · `msasset`) → 
2. All Resources (`PRD_RCS_ALL` · `mhresource`) → 
3. Detail Resource (`RESOURCE_DET` · `mdresource`) → 
4. Resource Access (`FO_RSACCESS` / `RSACCESS_ALL`)

*Catatan:*
Asset (mesin/alat) didaftarkan di `MST_AST` terlebih dahulu. Resource adalah kumpulan asset yang membentuk satu unit kerja produksi (misal: satu fasilitas casting). Detail Resource (`mdresource`) mencatat kapasitas dan UOM per resource. Resource Access mengatur operator/user mana yang boleh mengakses resource tertentu per plant.

### Pelanggan & Referensi Global

| Form Code | Judul | Tabel DB | Keterangan |
|---|---|---|---|
| `MST_CST` / `CUST_ALL` | All Customers | `master_customer` | Master pelanggan — nama, grup, referensi ke `master_item` |
| `MS_CUSTOMER` | Customer | `mhcustomer` | Form pelanggan alternatif (tabel `mhcustomer`) |
| `GLOBAL_MASTER` | Global Master Data | `mhlookup` | Lookup value global — kode dan nama kategori referensi |
| `GLOBAL_MASTER_DTL` | Global Master Data Detail | `mdlookup` | Detail nilai per kategori lookup |
| `MD_SEQUENCE` | Master Sequence | `md_sequence` | Konfigurasi nomor urut dokumen — prefix, periode reset (DAILY/MONTHLY/YEARLY/NEVER) |

---

## II. Sales & Distribusi

Modul Sales & Distribusi mencatat seluruh transaksi penjualan mulai dari order pelanggan hingga faktur dan penerimaan pembayaran.

**Alur Proses Penjualan:**
Prasyarat Master Pelanggan (`MST_CST` · `master_customer`) → 
1. Sales Order Line (`SO_LINE` · `tssalesline`) → 
2. Faktur Penjualan (`INVOICE_MD` · `inv_hdr`) → 
3. Detail Faktur (`INVOICE_MD_DTL` · `inv_dtl`) → 
4. Pembayaran (`SALES_PAY` · menu saja)

- **Sales Order Line (`SO_LINE` — `tssalesline`)**
  Form utama pencatatan pesanan penjualan. Field kunci: `invoiceaccount` (Customer, LOV `MST_CST`), `itemid` (Product Code, LOV `PRD_CATALOG`), `itemsetid` (Item Set), `salescategorycode`, `statusrelease` (status pelepasan order), `unitid`, jumlah dan harga. Script `BEFORE_SAVE_SO_LINE` berjalan sebelum simpan untuk validasi bisnis.
- **Faktur Penjualan (`INVOICE_MD`)**
  Header faktur di tabel `inv_hdr` dengan referensi pelanggan dari LOV `MASTER_CUSTOMER`. Detail faktur di `INVOICE_MD_DTL` (tabel `inv_dtl`) dengan referensi item dari LOV `MASTER_ITEM`.
- **Status Implementasi**
  Form Sales Payment (`SALES_PAY`) dan Purchase Payment (`PRCH_PAY`) terdaftar di menu namun belum memiliki konfigurasi form di `meta_form`. Kemungkinan terintegrasi dengan modul FICO yang masih dalam tahap pengembangan.

---

## III. Pengadaan (Procurement)

Modul pengadaan mencakup siklus pembelian dari order hingga pembayaran ke supplier. Struktur menu sudah terdefinisi lengkap, namun sebagian form masih dalam pengembangan.

**Alur Proses Pengadaan:**
(Opsional) Purchase Down Payment `PCH_DP` → 
1. Purchase Order `PCH_ORDER` → 
2. Goods Receipt `PCH_RECV` → 
3. Purchase Invoice `PCH_INV` → 
(Opsional) Invoice Charge `PCH_INV_CHRG` → 
4. Purchase Payment `PRCH_PAY`

**Status Modul Procurement:**
Seluruh item di modul Procurement & Sourcing (`PCH_DP`, `PCH_ORDER`, `PCH_RECV`, `PCH_INV`, `PCH_INV_CHRG`, `PRCH_PAY`) terdaftar di menu namun belum ada entri di `meta_form`. Proses bisnis ini masih dalam tahap rancangan/pengembangan.

---

## IV. Estimasi Biaya

Modul Cost Estimation (di bawah Sales & Distribution) digunakan untuk menyiapkan data teknis produk sebelum Production Order dibuat. Output modul ini — BOM, Route, dan Method — menjadi referensi utama proses produksi.

**Alur Estimasi Biaya:**
Basis Product Catalogue (`PRD_CATALOG` · `msitem`) → 
1. Bill of Material (`BOM_ALL` + `MST_BOM_DET`) → 
2. Process Route (`ROUTE_ALL` + `ROUTE_DET`) → 
3. Method Master (`MST_METHOD` / `RBR_MAS_METHOD`)

Aksi toolbar tersedia di Production Order untuk menyalin BOM dan Route dari template ke Production Order aktif:

| Kode Aksi | Label | Fungsi |
|---|---|---|
| `COPY_BOM` | Copy and Insert BOM | Salin baris BOM dari template (popup picker) ke Production Order |
| `COPY_ROUTE` | Copy and Insert Route | Salin baris Route dari template ke Production Order |
| `UPDATE_BOMROUTE` | UPDATE BOM & ROUTE | Perbarui BOM & Route sekaligus via Groovy script |
| `REF_BOM` | BOM | Referensi cepat ke data BOM (Groovy script) |
| `REF_ROUTE` | ROUTE | Referensi cepat ke data Route (Groovy script) |

---

## V. Produksi

Modul produksi adalah inti dari operasi pabrik. Aplikasi mendukung empat plant produksi, masing-masing dengan alur yang serupa namun menggunakan form dan data tersendiri.

### Production Order (`PRD_ORDER_ALL`)

**Alur Pembuatan Production Order:**
Input Sales Order / Kebutuhan (`tssalesline` / manual) → 
1. Production Order (`PRD_ORDER_ALL` · `tsproductionorder`) → 
2. Production BOM (`PRD_BOM` · `tsproductionorderbomd`) + Production Route (`PRD_ROUTE` · `tsproductionorderrouted`) → 
Aksi Release to Production (`RLS_PROD` / `RLS_SUBCON`)

Production Order (SPK) menjadi dokumen induk seluruh aktivitas produksi berikutnya. Field kunci: `itemid` (produk dari `PRD_CATALOG`), `bomid` (referensi BOM), `routeid` (referensi Route), `resourcegroupid`, `status`. Script `ON_ADD_PRD_ORDER_ALL` berjalan saat form baru dibuka (auto-fill). Aksi Release to Production (`RLS_PROD`) atau Release to Subcon (`RLS_SUBCON`) mengubah status dan membuka akses ke plant.

**Aksi pada Production Order:**

| Kode Aksi | Label | Tipe | Fungsi |
|---|---|---|---|
| `RLS_PROD` | Release to Production | GROOVY | Merilis order ke plant produksi, mengubah status |
| `RLS_SUBCON` | Release to Subcon | GROOVY | Merilis order ke subkontraktor |
| `COPY_PRD` | Copy Production Order | GROOVY | Duplikasi Production Order |
| `COPY_BOM` | Copy and Insert BOM | PICKER | Salin baris BOM dari template |
| `COPY_ROUTE` | Copy and Insert Route | PICKER | Salin baris Route dari template |
| `UPDATE_BOMROUTE` | UPDATE BOM & ROUTE | GROOVY | Update BOM & Route sekaligus |

### FO - Plant Foundry
Proses pengecoran logam. Memerlukan Method Master (parameter teknis peleburan) dan data Spectro (hasil uji komposisi material cair) sebelum booking produksi.

**Alur Produksi Foundry:**
0. Method Master (`MST_METHOD` · `msmethod`) → 
1. Scheduling (`FO_SCH` · `tsprodschedule`) → 
2. Production Tag (`FO_PRD_TAG` · `thproductiontag`) → 
2a. Generate Tag (`GEN_TAG` · Groovy) → 
3. Serial No (`FO_PRD_SN` · `tsproductionserial`) → 
3a. Generate Serial (`GEN_SERIAL` · Groovy) → 
4. Production Booking (`FO_PRD_BOOK` · `thbook`) → 
5. Spectro Result (`FO_SPECTRO` · `msspectro`)

Production Tag (`thproductiontag`) mencatat kelompok produksi yang terhubung ke Production Order via field `tsproductionorderid`. Tag di-generate otomatis menggunakan aksi `GEN_TAG` (Groovy script). Serial No (`tsproductionserial`) mencatat identitas fisik per unit produksi, di-generate via `GEN_SERIAL`. Production Booking (`thbook`) mencatat pemakaian resource aktual: mesin (Resource), shift, operator (`MST_PEG`/`msmember`), dan team leader. Script `BEFORE_SAVE` dan `AFTER_SAVE` berjalan untuk validasi dan update status Production Order.

**Aksi Foundry:**
| Aksi | Label | Fungsi |
|---|---|---|
| `GEN_TAG` | Generate Tag | Buat tag produksi dari Production Order |
| `GEN_TAG_EXEC` | Generate (exec) | Eksekusi generate tag |
| `GEN_SERIAL` | Generate Serial No | Buat nomor serial dari tag yang sudah ada |
| `GEN_SERIAL_EXEC` | Generate (exec) | Eksekusi generate serial |
| `INST_TAG` | Insert Tag | Pilih dan masukkan tag ke booking detail (popup picker) |
| `INST_SERIAL` | Insert Serial/Batch | Pilih dan masukkan serial/batch ke booking detail |
| `PICK_SERIAL` | Pick Serial No/Batch | Pilih serial dari daftar untuk booking |
| `PULL_OC_DATA` | Pull OC Data | Tarik data Order Confirmation dari sistem eksternal |

### FAST - Plant Fastener
Proses produksi baut, mur, dan produk fastener. Alur serupa dengan Foundry namun tanpa Spectro Result dan Method Master khusus.

**Alur Produksi Fastener:**
1. Scheduling (`FAST_SCH` · `tsprodschedule`) → 
2. Production Tag (`FAST_PRD_TAG` · `thproductiontag`) → 
2a. Generate Tag (`FAST_GEN_TAG` · Groovy) → 
3. Production Serial No (`FAS_SN` · `tsproductionserial`) → 
3a. Generate Serial (`FAST_GEN_SERIAL` · Groovy) → 
4. Production Booking (`FAST_PRD_BOOK` · `thbook`)

Aksi `FAST_PRD_BOOK_VALIDATE_QTY` (`BEFORE_SAVE`) memvalidasi jumlah sebelum booking disimpan. Aksi `FAST_GEN_TAG` dan `FAST_GEN_SERIAL` beroperasi pada tag/serial khusus plant Fastener. Picker `FAST_INSRT_TAG` dan `FAST_INST_SERIAL` digunakan di layar booking detail untuk memilih tag/serial yang akan di-booking.

### RBR - Plant Rubber
Proses produksi produk karet. Memerlukan Method Master tersendiri (`RBR_MAS_METHOD`) dan mendukung `isunique` flag pada serial number.

**Alur Produksi Rubber:**
0. Method Master (`RBR_MAS_METHOD` · `msmethod`) → 
1. Scheduling (`RBR_SCH` · `tsprodschedule`) → 
2. Rubber Prod Tag (`RBR_PRD_TAG` · `thproductiontag`) → 
2a. Generate Tag (`RBR_GEN_TAG` · Groovy) → 
3. Serial No (`RBR_PRD_SN` · `tsproductionserial`) → 
3a. Generate Serial (`RBR_GEN_SERIAL` · Groovy) → 
4. Production Booking (`RBR_PRD_BOOK` · `thbook`)

Script `RBR_UPDATE_ST_PRODORDER` (`AFTER_SAVE` di `RBR_PRD_BOOK`) memperbarui status Production Order setelah booking disimpan — sama seperti `UPDATE_ST_PRODORDER` di plant Foundry. Aksi `RBR_PICK_SERIAL` di Production Tag mendukung pemilihan serial dengan filter status dan flag `isunique`.

### GM - Plant Grinding Media
Proses produksi grinding media (bola baja/logam penggiling). Alur lebih sederhana — tidak ada tag atau serial number, langsung ke scheduling dan booking.

**Alur Produksi Grinding Media:**
1. Scheduling (`GM_SCH` · `tsprodschedule`) → 
2. Production Booking (`GM_BOOK` · `thbook`)

*Catatan Implementasi:*
Form `GM_SCH` dan `GM_BOOK` terdaftar di menu namun belum ada konfigurasi di `meta_form`. Plant Grinding Media sepertinya masih dalam tahap setup.

---

## VI. Quality Management

Modul quality management saat ini berfokus pada proses subkontrak dengan kontrol kualitas sebelum produk diterima kembali.

**Alur Subcontract & Quality Control:**
1. Release to Subcon (`RLS_SUBCON` · Groovy dari Prod Order) → 
2. All Subcontracts (`ALL_SUBCONT` · menu item) → 
3. Pass QC (`SUBCONT_PASS_QC` · menu item)

**Status Implementasi:**
`ALL_SUBCONT` dan `SUBCONT_PASS_QC` terdaftar di menu Quality Management namun form-nya belum dikonfigurasi di `meta_form`. Modul ini dalam tahap pengembangan.

---

## VII. Keuangan, Akuntansi & Aset

Modul FICO mencakup akuntansi umum, manajemen kas, piutang, hutang, dan aset tetap. Dari form yang terimplementasi di metadata, hanya Asset Listing yang sudah aktif sepenuhnya.

| Modul | Sub-modul | Form / Menu | Status |
|---|---|---|---|
| Akuntansi | General Ledger | `FICO_ACC_GL` | Pengembangan |
| Kas & Bank | Purchase Payment | `PRCH_PAY` | Pengembangan |
| Kas & Bank | Sales Payment | `SALES_PAY` | Pengembangan |
| Aset | Asset Listing | `MST_AST` · `msasset` | Aktif |
| Piutang | Receivable Management | `FICO_AR` | Pengembangan |
| Hutang | Payable Management | `PAY_MGMT` | Pengembangan |

**Asset Listing (`MST_AST` — `msasset`)**
Form aset aktif lengkap. Field kunci: kategori aset, grup aset, status aset, sumber energi, lokasi, departemen, business unit, kapasitas (UOM + nilai), konsumsi daya (UOM + nilai), dan umur layanan (service life). Aset ini direferensikan oleh Detail Resource (`RESOURCE_DET`) sebagai komponen mesin/fasilitas produksi.

---

## VIII. Human Capital Management

Modul HCM saat ini berfokus pada data karyawan yang digunakan sebagai referensi di proses produksi (operator, team leader di booking).

| Form Code | Judul | Tabel DB | Keterangan |
|---|---|---|---|
| `MST_PEG` | Employee Data | `msmember` | Data karyawan — direferensikan di booking produksi sebagai operator/team leader |
| `USER_APP` | User App | `app_users` (LOV) | Pengguna aplikasi — digunakan di field `inputby`/`updateby` seluruh form |
| `USER_ROLE` | User Role | `app_roles` | Role pengguna — digunakan di Resource Access untuk pengaturan hak akses resource |

**Sub-modul HCM dalam Pengembangan:**
Menu Recruitment (`MST_PEG` sudah aktif), Organization, dan Position sudah terdaftar di menu `HC_MGMT` namun belum ada form yang dikonfigurasi di `meta_form` selain Employee Data.

---

## A. Indeks Form Aktif

Seluruh 67 form yang terdaftar di `meta_form`, dikelompokkan berdasarkan modul.

| Form Code | Judul Form | Tipe | Tabel DB | Modul |
|---|---|---|---|---|
| `PRD_CATALOG` | Product Catalogue | SINGLE | `msitem` | Master Data |
| `PROD_CAT` | Product Catalogue (alt) | SINGLE | `msitem` | Master Data |
| `BOM_ALL` | All Bill of Material | SINGLE | `mhbom` | Cost Estimation |
| `MST_BOM_DET` | Master BOM Detail | SINGLE | `mdbom` | Cost Estimation |
| `ROUTE_ALL` | All Route | SINGLE | `mhroute` | Cost Estimation |
| `ROUTE_DET` | Route Detail | SINGLE | `mdroute` | Cost Estimation |
| `MST_METHOD` | Method Master (Foundry) | SINGLE | `msmethod` | Foundry |
| `RBR_MAS_METHOD` | Method Master (Rubber) | SINGLE | `msmethod` | Rubber |
| `MAT_COMP` | Material Composition | SINGLE | `mdmaterialcomposition` | Material |
| `DET_MAT_COMP` | Detail Material Composition | SINGLE | `mdmaterialcomposition` | Material |
| `MAT_SPEC` | Material Specification | SINGLE | — | Material |
| `MAT_SPEC_DET` | Material Specification Detail | SINGLE | `mdmaterialspec` | Material |
| `MAS_E_SPEC` | Master Element Specification | SINGLE | — | Material |
| `RLS_MAT` | Release Material | SINGLE | — | Material |
| `MST_AST` | Asset Listing | SINGLE | `msasset` | Aset |
| `PRD_RCS_ALL` | All Resources | SINGLE | `mhresource` | Resource |
| `RESOURCE_DET` | Detail Resource | SINGLE | `mdresource` | Resource |
| `RSACCESS_ALL` | All Resource Access | SINGLE | `tsresourceaccess` | Resource |
| `FO_RSACCESS` | Resource Access (Foundry) | SINGLE | `tsresourceaccess` | Foundry |
| `MST_CST` / `CUST_ALL` | All Customers | SINGLE | `master_customer` | Sales |
| `MS_CUSTOMER` | Customer | SINGLE | `mhcustomer` | Sales |
| `SO_LINE` | Sales Order Line | SINGLE | `tssalesline` | Sales |
| `INVOICE_MD` | Faktur Penjualan | SINGLE | `inv_hdr` | Sales |
| `INVOICE_MD_DTL` | Detail Faktur Penjualan | SINGLE | `inv_dtl` | Sales |
| `PRD_ORDER_ALL` | All Production Order | SINGLE | `tsproductionorder` | Produksi |
| `PRD_BOM` | Production BOM | SINGLE | `tsproductionorderbomd` | Produksi |
| `PRD_ROUTE` | Production Route | SINGLE | `tsproductionorderrouted` | Produksi |
| `FO_SCH` | Scheduling (Foundry) | SCHEDULER_SPLIT| `tsprodschedule` | Foundry |
| `FO_PRD_TAG` | Production Tag (Foundry) | SINGLE | `thproductiontag` | Foundry |
| `FO_PRD_SN` | Production Serial No (Foundry)| SINGLE | `tsproductionserial` | Foundry |
| `FO_PRD_BOOK` | Production Booking (Foundry)| SINGLE | `thbook` | Foundry |
| `FO_SPECTRO` | Spectro Result | SINGLE | `msspectro` | Foundry |
| `FAST_SCH` | Scheduling (Fastener) | SINGLE | — | Fastener |
| `FAST_PRD_TAG` | Production Tag (Fastener) | SINGLE | `thproductiontag` | Fastener |
| `FAS_SN` | Production Serial No (Fastener) | SINGLE | `tsproductionserial` | Fastener |
| `FAST_PRD_BOOK` | Production Booking (Fastener) | SINGLE | `thbook` | Fastener |
| `FAST_TS_BOOK_DET`| Detail Booking (Fastener) | SINGLE | `tdbook` | Fastener |
| `RBR_PRD_TAG` | Rubber Production Tag | SINGLE | `thproductiontag` | Rubber |
| `RBR_PRD_SN` | Production Serial No (Rubber) | SINGLE | `tsproductionserial` | Rubber |
| `RBR_PRD_BOOK` | Production Booking (Rubber) | SINGLE | `thbook` | Rubber |
| `RBR_TS_BOOK_DET` | Rubber Detail Booking | SINGLE | `tdbook` | Rubber |
| `TS_BOOK_DET` | Detail Booking | SINGLE | `tdbook` | Produksi |
| `TS_SERIAL` | Production Serial Track | SINGLE | `tsproductionserial` | Produksi |
| `TS_FAST_SERIAL` | Production Serial Track (Fastener)| SINGLE | `tsproductionserial` | Fastener |
| `TS_PROD_TAG_DET` | Production Tag Detail | SINGLE | `tdproductiontag` | Produksi |
| `MST_PEG` | Employee Data | SINGLE | `msmember` | HCM |
| `GLOBAL_MASTER` | Global Master Data | SINGLE | `mhlookup` | Sistem |
| `GLOBAL_MASTER_DTL`| Global Master Data Detail | SINGLE | `mdlookup` | Sistem |
| `MD_SEQUENCE` | Master Sequence | SINGLE | `md_sequence` | Sistem |

---

## B. Indeks Aksi Extra Toolbar

| Kode Aksi | Label | Tipe | Scope | Form |
|---|---|---|---|---|
| `RLS_PROD` | Release to Production | GROOVY | MASTER_TOOLBAR | — |
| `RLS_SUBCON` | Release to Subcon | GROOVY | MASTER_TOOLBAR | — |
| `COPY_PRD` | Copy Production Order | GROOVY | MASTER_TOOLBAR | — |
| `COPY_BOM` | Copy and Insert BOM | PICKER | DETAIL_TOOLBAR | — |
| `COPY_ROUTE` | Copy and Insert Route | PICKER | DETAIL_TOOLBAR | — |
| `UPDATE_BOMROUTE` | UPDATE BOM & ROUTE | GROOVY | MASTER_TOOLBAR | — |
| `REF_BOM` | BOM | GROOVY | MASTER_TOOLBAR | — |
| `REF_ROUTE` | ROUTE | GROOVY | MASTER_TOOLBAR | — |
| `GEN_TAG` / `GEN_TAG_EXEC` | Generate Tag (FO) | GROOVY | MASTER_TOOLBAR | — |
| `FAST_GEN_TAG` / `_EXEC` | Generate Tag (FAST) | GROOVY | MASTER_TOOLBAR | — |
| `RBR_GEN_TAG` / `_EXEC` | Generate Tag (RBR) | GROOVY | MASTER_TOOLBAR | `RBR_PRD_TAG` |
| `GEN_SERIAL` / `_EXEC` | Generate Serial (FO) | GROOVY | MASTER_TOOLBAR | — |
| `FAST_GEN_SERIAL` / `_EXEC` | Generate Serial (FAST)| GROOVY | MASTER_TOOLBAR | — |
| `RBR_GEN_SERIAL` / `_EXEC` | Generate Serial (RBR) | GROOVY | MASTER_TOOLBAR | — |
| `INST_TAG` | Insert Tag (FO) | PICKER | DETAIL_TOOLBAR | — |
| `FAST_INSRT_TAG` | Insert Tag (FAST) | PICKER | DETAIL_TOOLBAR | — |
| `RBR_INS_TAG` | Insert Tag (RBR) | PICKER | DETAIL_TOOLBAR | `RBR_PRD_TAG` |
| `INST_SERIAL` | Insert Serial/Batch (FO) | PICKER | DETAIL_TOOLBAR | — |
| `FAST_INST_SERIAL` | Insert Serial/Batch (FAST)| PICKER | DETAIL_TOOLBAR | — |
| `RBR_INST_SERIAL` | Insert Serial (RBR) | PICKER | DETAIL_TOOLBAR | `RBR_PRD_TAG` |
| `PICK_SERIAL` | Pick Serial No/Batch (FO) | PICKER | DETAIL_TOOLBAR | — |
| `FAST_PICK_SERIAL` | Pick Serial No/Batch (FAST)| PICKER | DETAIL_TOOLBAR | — |
| `RBR_PICK_SERIAL` | Pick Serial No (RBR) | PICKER | DETAIL_TOOLBAR | `RBR_PRD_TAG` |
| `PULL_OC_DATA` | Pull OC Data | GROOVY | MASTER_TOOLBAR | — |
| `PICK_GLOBAL_CATEGORY`| Pick Category Items | PICKER | MASTER_TOOLBAR | — |
| `BEFORE_SAVE_SO_LINE` | — (validasi) | GROOVY | BEFORE_SAVE | `SO_LINE` |
| `BEFORE_SAVE_ROUTE_ALL`| — (validasi) | GROOVY | BEFORE_SAVE | `ROUTE_ALL` |
| `ON_ADD_PRD_ORDER_ALL`| — (auto-fill) | GROOVY | ON_LOAD_NEW | `PRD_ORDER_ALL`|
| `ON_ADD_FO_RSACCESS` | — (auto-fill) | GROOVY | ON_LOAD_NEW | `FO_RSACCESS` |
| `ON_LOAD_NEW_MST_METHOD`| — (auto-fill) | GROOVY | ON_LOAD_NEW | `MST_METHOD` |
| `UPDATE_ST_PRODORDER` | — (update status) | GROOVY | AFTER_SAVE | `FO_PRD_BOOK` |
| `RBR_UPDATE_ST_PRODORDER`| — (update status) | GROOVY | AFTER_SAVE | `RBR_PRD_BOOK` |
