# Print-dari-Form (Cetak Dokumen dari Grid) — Design Spec

Tanggal: 2026-08-29
Status: Draft (menunggu review)
Scope: Tombol **Cetak** di `GenericFormView` menjalankan report atas **baris yang dicentang di
grid**, dengan parameter `FORM_FIELD` terisi otomatis dari baris tersebut. Termasuk penyeragaman
jalur output dengan Report Runner, penambahan GROUP band ke engine STANDARD, dan tiga report
contoh (satu per engine) di atas form master-detail yang sudah ada.

---

## 1. Overview

Report Runner (`/report-runner`) sudah melayani end-user menjalankan report secara standalone.
Yang belum ada: menjalankan report **dari dalam form**, atas record yang sedang dilihat user.

Tombol Cetak sebenarnya sudah ada di `GenericFormView.java:607-660`, tetapi **jalurnya mati**:

| Engine | Perilaku sekarang | Status |
|---|---|---|
| STANDARD | `navigate("report-viewer")` | Route dihapus di commit `7c68179` → error |
| JASPER | `/api/report/engine/view/{code}` | Mengembalikan HTML placeholder, tidak me-render apa pun |
| STIMULSOFT | `/api/report/engine/view/{code}` | Jalan (iframe viewer) |

Selain itu jalur lama sama sekali tidak melewati `ReportRunService` maupun `ReportParamResolver`,
sehingga parameter `source = FORM_FIELD` tidak pernah terisi — yang dikirim hanya `?id=<PK>`
hardcoded.

Spec ini menggantinya dengan jalur yang memakai pipeline report yang sudah ada.

**Prinsip utama:** aplikasi tidak membedakan "report dokumen" dan "report listing". Baris terpilih
selalu dikirim sebagai satu list, query dijalankan sekali, dan **template** yang menentukan apakah
hasilnya menjadi N halaman dokumen (group + page break) atau satu tabel listing. Konsekuensinya
fleksibilitas ada di tangan perancang template, bukan di kolom konfigurasi.

## 2. Scope

**Masuk:**

- Kolom `meta_report.usage_scope` (`FORM` / `RUNNER` / `BOTH`) + `meta_report.group_by`.
- `ReportParamResolver.resolveFromRows(...)` — baris terpilih → nilai `FORM_FIELD` berupa `List`.
- `ReportDataService` — parameter `FORM_FIELD` selalu di-bind sebagai list (`= ANY(...)`).
- `ReportLauncher` — kelas bersama untuk render output + buka tab portal; menghapus duplikasi
  antara `ReportRunnerView` dan `GenericFormView`.
- `GenericFormView.btnPrint` ditulis ulang di atas `ReportRunService`.
- GROUP band (`GROUP_HEADER` / `GROUP_FOOTER`) pada engine STANDARD.
- Perbaikan parameter multi-nilai pada `StimulsoftJavaController`.
- Tiga report contoh di atas form `BOM_ALL`, satu per engine, dengan query identik.
- Penghapusan `ReportEngineController` (jalur mati).

**Ditunda:**

- Favorites/recent report per user, saved parameter variants.
- Penggabungan beberapa report menjadi satu berkas cetak.
- Export langsung ke Excel dari tombol Cetak (viewer/toolbar masing-masing engine tetap tersedia).
- Render Stimulsoft server-side menjadi PDF (tetap lewat viewer web).

## 3. Arsitektur

```
GenericFormView.btnPrint
        │
        ├── daftar report  ─→ FormMetaRepository.findByReportSourceKey(sourceKeyOf(form))
        │                     ∩ usage_scope ∈ {FORM, BOTH}
        │                     ∩ ReportAccessService.canAccess(report, user)
        │
        ├── nilai param    ─→ ReportParamResolver.resolveFromRows(params, selectedRows, user)
        │                     └── dialog ReportParameterForm bila ada param USER_INPUT
        │
        └── jalankan       ─→ ReportLauncher.runAndOpenTab(...)
                                    ├── ReportRunService.run(report, values, false)
                                    └── PortalView.openComponentTab(...)
```

`ReportLauncher` adalah kelas baru yang menampung dua hal yang sekarang hanya ada di
`ReportRunnerView`: membangun komponen output dari `ReportRunResult`, dan menjalankan report di
thread latar lalu membuka tab portal. Keduanya dipakai oleh dua pemanggil nyata
(`ReportRunnerView` dan `GenericFormView`), jadi ini ekstraksi duplikasi, bukan abstraksi
spekulatif.

## 4. Data model

Dua kolom baru pada `meta_report`. Karena `ddl-auto=validate`, ALTER harus dijalankan sebelum
aplikasi start.

```sql
ALTER TABLE public.meta_report
  ADD COLUMN IF NOT EXISTS usage_scope VARCHAR(20) DEFAULT 'RUNNER';
ALTER TABLE public.meta_report
  ADD COLUMN IF NOT EXISTS group_by VARCHAR(100);
```

### `usage_scope`

| Nilai | Muncul di Report Runner | Muncul di tombol Cetak form |
|---|---|---|
| `RUNNER` (default) | ✅ | ❌ |
| `FORM` | ❌ | ✅ |
| `BOTH` | ✅ | ✅ |

Namanya `usage_scope`, bukan `usage`, karena `USAGE` punya makna khusus di PostgreSQL.

Default `RUNNER` dipilih supaya report yang sudah ada tidak berubah perilaku — admin harus
mengaktifkan sendiri report untuk dicetak dari form.

Kolom ini juga menutup lubang keamanan: report dengan parameter `FORM_FIELD` yang dijalankan dari
Runner tidak punya baris terpilih, sehingga `resolveFromRows` tidak menghasilkan nilai apa pun dan
query berjalan **tanpa filter** — mencetak seluruh tabel. Menyetel `usage_scope = FORM` mencegah
report semacam itu muncul di Runner.

### `group_by`

Nama kolom hasil query yang menjadi kunci pengelompokan untuk engine STANDARD (mis. `bom_id`).
Kosong berarti tanpa grouping — perilaku sekarang. Tidak dipakai JASPER maupun STIMULSOFT, yang
menyimpan definisi group di dalam templatenya masing-masing.

### Relasi form → report

Tidak ada kolom baru. Relasi memakai `meta_report.table_name` yang sudah ada, dicocokkan lewat
`FormMetaRepository.findByReportSourceKey(key)` yang sudah menangani tiga jalur
(`tableName` / `viewTable` / `formCode`).

`GenericFormView` harus memakai helper yang sama dengan `ReportDesignerView.sourceKeyOf(FormMeta)`:
`tableName` bila ada, jika tidak `formCode` (untuk form yang hanya punya view). Perbandingan
`getTableName()` mentah yang dipakai sekarang membuat form view-only tidak pernah menemukan
report-nya.

Efek samping yang diterima: bila dua form memakai tabel yang sama, report muncul di keduanya. Itu
perilaku yang diinginkan.

## 5. Alur cetak

1. User mencentang N baris di grid (`Grid.SelectionMode.MULTI`, sudah aktif di
   `GenericFormView.java:232`), lalu menekan **Cetak**.
2. Kumpulkan report yang memenuhi: cocok dengan form ini, `usage_scope ∈ {FORM, BOTH}`, dan lolos
   `ReportAccessService.canAccess`.
   - Kosong → notifikasi "Belum ada laporan yang dikonfigurasi untuk form ini."
   - Satu → langsung dipakai.
   - Lebih dari satu → dialog pilih laporan (pola dialog yang sudah ada dipertahankan).
3. `ReportParamResolver.resolveFromRows(report.getParams(), selectedRows, currentUser)`:
   - `FORM_FIELD` → `List` nilai kolom `sourceKey` dari tiap baris, duplikat dibuang, `null` dibuang.
   - `SYSTEM` → `$CURRENT_USER` / `CURRENT_DATE` seperti sekarang.
   - `USER_INPUT` → dilewati.
4. Bila report punya parameter `USER_INPUT`, buka dialog berisi `ReportParameterForm`. Komponen itu
   sudah memfilter lewat `ReportParamResolver.userInputParams()`, sehingga parameter `FORM_FIELD`
   dan `SYSTEM` **tidak ikut tampil** — user hanya melihat yang memang perlu diisi. Bila tidak ada
   `USER_INPUT` sama sekali, cetak langsung tanpa dialog.
5. Validasi `required` (pesan sama seperti Runner), lalu `ReportLauncher.runAndOpenTab(...)`.
6. Output dibuka sebagai tab dalam aplikasi lewat `PortalView.openComponentTab("RPT_OUT_" + code,
   title, content)` — konsisten dengan Report Runner.

## 6. Parameter `FORM_FIELD` multi-baris

Nilai `FORM_FIELD` **selalu** berupa `List`, baik satu baris maupun banyak. Aturan tunggal ini
dipilih agar tidak ada report yang berjalan saat user mencentang satu baris lalu gagal saat
mencentang baris kedua.

**Model B** (`filterColumn` + `operator`): untuk parameter `FORM_FIELD`, `ReportDataService`
**mengabaikan kolom `operator`** dan selalu membangun `{col} = ANY(:param)`. Di PostgreSQL
`= ANY(ARRAY['x'])` tetap benar untuk satu elemen, sehingga tidak perlu percabangan. Admin tidak
perlu menyetel `IN` secara manual.

**Model A** (custom query): admin menulis sendiri, dan harus memakai bentuk list:

```sql
WHERE h.id IN (:bom_id)     -- ✅ Spring NamedParameterJdbcTemplate meng-expand otomatis
WHERE h.id = :bom_id        -- ❌ gagal bila nilainya list
```

Report Designer menampilkan teks bantuan ini di bawah area query. Tidak ada penulisan ulang SQL
otomatis — pendekatan itu rapuh dan sulit di-debug.

## 7. Perubahan per engine

Query dan data identik untuk ketiga engine: `ReportDataService.fetchData()` berjalan lebih dulu dan
menghasilkan `List<Map<String,Object>>` yang sama, baru diserahkan ke renderer. Yang berbeda hanya
cara menyusunnya menjadi halaman.

### JASPER — siap

`<group>` dengan `<groupExpression>` dan `isStartNewPage="true"` sudah menjadi fitur bawaan. Kolom
header ditempatkan di `groupHeader` band, kolom detail di `detail` band. Tidak ada perubahan kode.

### STIMULSOFT — satu perbaikan

`StimulsoftJavaController.java:77-80` hanya mengambil nilai pertama tiap parameter:

```java
request.getParameterMap().forEach((k, v) -> {
    if (!"code".equals(k) && v != null && v.length > 0) params.put(k, v[0]);
});
```

URL `?bom_id=38&bom_id=42` hanya terbaca sebagai `38`. Perbaikan: bila `v.length > 1`, masukkan
seluruh array sebagai `List`.

Sisi pengirimnya juga perlu diperbaiki. `ReportRunService.run()` (baris 38-48) menyusun URL viewer
dengan `e.getValue().toString()`, yang untuk sebuah `List` menghasilkan `[38, 42]` — bukan URL yang
valid. Perbaikan: bila nilainya `Collection`, ulangi key tersebut untuk tiap elemen
(`&bom_id=38&bom_id=42`), masing-masing di-encode tersendiri.

Stimulsoft tetap ditampilkan lewat viewer web — `StimulsoftRenderer.render()` melempar
`UnsupportedOperationException` secara sengaja. Output tab untuk engine ini berupa IFrame viewer,
bukan PDF, dan pencetakan memakai toolbar viewer bawaan.

### STANDARD — tambah GROUP band

`StandardRenderer` sudah band-based (`TITLE`, `PAGE_HEADER`, `COLUMN_HEADER`, `DETAIL`, `SUMMARY`,
`PAGE_FOOTER`) tetapi belum punya konsep group. Yang ditambahkan:

- Dua nilai `bandType` baru: `GROUP_HEADER` dan `GROUP_FOOTER`.
- Bila `report.groupBy` terisi, data dipecah per nilai kolom itu (urutan kemunculan dipertahankan).
  Untuk tiap kelompok: render `GROUP_HEADER` → tabel (`COLUMN_HEADER` + `DETAIL` baris kelompok itu)
  → `GROUP_FOOTER`.
- Fungsi agregat (`SUM`/`AVG`/`COUNT`) di dalam band group dihitung atas baris **kelompok itu saja**,
  bukan seluruh data. Di `SUMMARY` tetap atas seluruh data.
- Antar kelompok diberi `page-break-before: always` agar tercetak sebagai halaman terpisah, dan
  tetap tampil menyambung di layar.
- `groupBy` kosong → jalur lama persis, tanpa perubahan perilaku.
- `ReportBuilderView.java:525-531` menambah dua `buildBandLayout(...)` untuk band baru, dan
  Report Designer menambah field `Group By`.

## 8. Report contoh

Tiga report di atas form **`BOM_ALL`** ("All Bill of Material", `table_name = mhbom`), yang punya
`SUBFORM_GRID` ke `MST_BOM_DET`. Data terverifikasi: 16 header / 195 detail.

Skema: `dynamic.mdbom.mhbomid` → `dynamic.mhbom.id` (FK, ON DELETE CASCADE).

**Query — identik untuk ketiganya:**

```sql
SELECT h.id AS bom_id, h.idno, h.itemname AS product,
       h.abmdrawingnumber AS drawing, h.netweight,
       d.itemname AS material, d.itemgroup, d.qty, d.perseries
FROM dynamic.mhbom h
LEFT JOIN dynamic.mdbom d ON d.mhbomid = h.id
WHERE h.id IN (:bom_id)
ORDER BY h.id, d.id
```

Header ikut berulang di tiap baris detail. Itu disengaja: pipeline mengembalikan satu koleksi datar,
dan setiap engine sudah punya group band untuk menampilkan kolom header sekali per kelompok.
Alternatifnya (subreport Jasper dengan datasource bersarang) ditolak karena `ReportContext` hanya
membawa satu `List<Map>`, dan pendekatan itu menjadi 1+N query.

**Parameter — sama untuk ketiganya:**

| param_name | source | source_key | required |
|---|---|---|---|
| `bom_id` | `FORM_FIELD` | `id` | ✅ |

**Metadata:**

| report_code | engine_type | usage_scope | group_by | Template |
|---|---|---|---|---|
| `RPT_BOM_DOC_STD` | `STANDARD` | `FORM` | `bom_id` | Band via `meta_report_element` |
| `RPT_BOM_DOC_JSP` | `JASPER` | `FORM` | — | `RPT_BOM_DOC_JSP.jrxml` |
| `RPT_BOM_DOC_STI` | `STIMULSOFT` | `FORM` | — | `RPT_BOM_DOC_STI.mrt` |

Ketiganya `table_name = 'mhbom'`, `category = 'Production'`.

**Tata letak dokumen (sama untuk ketiga engine):**

```
┌─ Group header (per bom_id) ─────────────────────┐
│  BILL OF MATERIAL              No : BOM00053    │
│  Product : 1.5" OVAL HEAD TAPERED BOLT 280 (C)  │
│  Drawing : —          Net Weight : —            │
├─ Column header ─────────────────────────────────┤
│  #  Material                Group      Qty  /Ser│
├─ Detail (berulang) ─────────────────────────────┤
│  1  PASIR KWARSA HALUS      RM Sand  1731.4   4 │
│  2  Resin APR101A           Binder     53.5   4 │
├─ Group footer ──────────────────────────────────┤
│                          Total item :  12       │
└─────────────────────────────────────────────────┘
```

Mencentang 2 BOM menghasilkan satu query dan dua halaman dokumen, masing-masing lengkap dengan
materialnya.

**Template `.mrt` Stimulsoft tidak dibuat dari nol dalam kode.** Formatnya proprietary dan rawan
salah bila ditulis manual. Report `RPT_BOM_DOC_STI` diseed dengan metadata + parameter, lalu
templatenya dirancang lewat Stimulsoft Designer web yang sudah tersedia
(`/stimulsoft-java/designer?code=RPT_BOM_DOC_STI`); langkahnya dicantumkan sebagai verifikasi manual.

Seed SQL masuk ke `db-migration.sql`. Berkas `.jrxml` ditempatkan di
`{app.upload.dir}/report_templates/master/`.

## 9. Keamanan dan otorisasi

- Tombol Cetak sudah tunduk pada `auth.canPrint` (izin menu) — dipertahankan.
- Per-report memakai `ReportAccessService.canAccess(report, user)`: SUPER_ADMIN melihat semua;
  selain itu `allowedRoles ∩ userRoles ≠ ∅`; report tanpa `allowedRoles` hanya untuk SUPER_ADMIN.
- Akses **diperiksa ulang saat Run**, bukan hanya saat menyusun daftar. Filter daftar bukan kontrol
  keamanan.
- Nilai dari baris grid di-bind lewat `MapSqlParameterSource`, tidak pernah dirangkai ke SQL.
- `reportCode` tetap divalidasi `^[A-Za-z0-9_-]+$` sebelum dipakai sebagai nama berkas template.
- URL viewer Stimulsoft di-encode per nilai saat menyusun parameter berulang.

## 10. Error handling

| Kondisi | Perilaku |
|---|---|
| Tidak ada baris dicentang, report punya `FORM_FIELD` `required` | "Please select at least one row." — cetak dibatalkan |
| Tidak ada baris dicentang, `FORM_FIELD` tidak `required` | Jalan tanpa filter (disengaja: report dua guna) |
| Tidak ada report untuk form ini | Notifikasi informatif, bukan dialog kosong |
| Parameter `required` kosong | Pesan sama dengan Runner |
| `QueryTimeoutException` | "The report query took too long and was stopped. Please narrow your filter/parameters." |
| Template Jasper tidak ada / gagal compile | Pesan berisi nama berkas yang dicari |
| `PortalView` tidak ditemukan | Notifikasi, bukan `NullPointerException` |

Render berjalan di thread latar dengan `ProgressBar`, hasil dikirim lewat `UI.access` — pola yang
sama dengan `ReportRunnerView`.

## 11. Testing

**Unit** (JUnit 5 + Mockito + AssertJ):

- `ReportParamResolver.resolveFromRows`: N baris → list distinct; satu baris → list berisi satu
  elemen (bukan skalar); nol baris → key tidak muncul; `null` dibuang; `SYSTEM` tetap ter-resolve;
  `USER_INPUT` tetap dilewati.
- `ReportDataService`: parameter `FORM_FIELD` menghasilkan `= ANY(:param)` dan mengabaikan
  `operator` yang tersimpan.
- `StandardRenderer`: `groupBy` terisi → satu blok per nilai group, `GROUP_HEADER` muncul sekali per
  kelompok, agregat di `GROUP_FOOTER` dihitung per kelompok; `groupBy` kosong → keluaran identik
  dengan sebelum perubahan (uji regresi).
- Penyaringan katalog: `usage_scope` × otorisasi, untuk daftar Runner maupun daftar tombol Cetak.
- `sourceKeyOf` untuk form view-only mengembalikan `formCode`.

**Manual:**

- Form `BOM_ALL` → centang 2 BOM → Cetak → pilih `RPT_BOM_DOC_JSP` → dua halaman dokumen, masing-
  masing berisi material BOM tersebut.
- Ulangi untuk `RPT_BOM_DOC_STD` (page break antar kelompok) dan `RPT_BOM_DOC_STI` (viewer, dua
  nilai parameter terbaca).
- Centang 1 baris → satu dokumen, tidak error.
- Tidak mencentang apa pun → pesan "Please select at least one row."
- Report `usage_scope = RUNNER` tidak muncul di tombol Cetak; `usage_scope = FORM` tidak muncul di
  Report Runner.
- Uji dengan user non-admin dan super-admin.
- Report lama (`RPT_SALESLINE_EXP3RD`) tetap berjalan seperti sebelumnya di Runner.

## 12. Future

- GROUP band bertingkat (group di dalam group) untuk engine STANDARD.
- Export PDF/Excel langsung dari tombol Cetak untuk engine STANDARD.
- Render Stimulsoft server-side agar keluarannya seragam PDF.
- Menyimpan pilihan report terakhir per form per user.
