# Report Designer Rework — Design Spec

Tanggal: 2026-08-27
Status: Draft (menunggu review)
Scope: **Report Designer (sisi admin)** + komponen bersama yang dipakai report.

---

## 1. Overview

Merombak manajemen report agar konsisten, aman, bebas memory-leak, dan cepat, dengan
dukungan **3 engine**: `STANDARD`, `STIMULSOFT`, `JASPER`. Spec ini fokus pada **designer
(admin)**: mendaftar, membuat, mengedit definisi report, mendefinisikan parameter, dan
mendesain layout per engine — termasuk **Preview**. Tampilan untuk end-user (viewer/print)
dan kustomisasi per-user ditunda ke spec terpisah.

## 2. Scope

**Masuk (spec ini):**
- Report Designer UI dengan pola **grid + toolbar + tab** (meniru `GenericFormView`).
- CRUD definisi report (`ReportMeta`) + definisi parameter (`ReportParamMeta` baru).
- Surface editor adaptif per engine (Standard band canvas / Stimulsoft IFrame / Jasper upload).
- Alur **save-first → designer** dan **Preview** (data sample).
- Pipeline inti bersama: `ReportResolver`, `ReportDataService`, `ReportRenderer` (dipakai Preview).
- Resolusi datasource (mengikuti `GenericFormView`) + penanganan LOV.
- Template **master** (belum ada salinan per-user).
- Keamanan (admin), anti-leak, dan performa untuk designer.

**Ditunda (spec berikutnya, user-facing):**
- Viewer/print surface untuk end-user, dialog parameter runtime, integrasi print-dari-form.
- **"Desain Punya Saya"**: copy-on-edit per-user + tombol Reset.
- "Cetak sesuai filter grid aktif".

## 3. Arsitektur (strategy pattern, unit kecil)

```
ReportResolver      → resolve engine + path template (master-only; interface siap versi-user)
ReportDataService   → params → data (1 pipeline; NamedParameterJdbcTemplate)
ReportRenderer      → interface render(ctx)+export(fmt)
   ├ StandardRenderer   (HTML "kertas" / PDF utk data besar)
   ├ StimulsoftRenderer (embed viewer)
   └ JasperRenderer     (.jasper run / .jrxml compile+cache → PDF/XLSX/...)
ReportParamMeta→FieldMeta adapter  → reuse ComponentFactory
ReportDesignerView  → Java bertyped, meniru pola GenericFormView (grid+toolbar+tab)
```

Pipeline inti (Resolver/DataService/Renderers) dibangun sekarang karena **Preview** designer
memakainya. Yang ditunda hanyalah UI viewer/print end-user dan per-user copy.

### Keputusan Java bertyped (bukan Groovy, bukan mesin metadata penuh)
- **Groovy (`ScriptExecutorService`)** di-sandbox ketat (`SecureASTCustomizer`, blokir
  `GroovyShell/ClassLoader/System`, `TimedInterrupt`) dan binding-nya hanya helper level field.
  Cocok untuk aturan bisnis per-field; **salah alat** untuk editor stateful (navigasi, IFrame
  lifecycle, simpan file, preview).
- **Mesin dynamic-form penuh** tidak dipakai karena `ReportMeta` adalah entity JPA dengan child
  (elements/parameter), dan surface editornya bukan "field" biasa.
- **Konsistensi UX tetap dijaga** dengan me-reuse komponen `GenericFormView` (`Grid`, `TabSheet`,
  toolbar `SafeButton`, `Dialog`) — bukan menjalankan mesin metadata-nya.

## 4. Komponen

### 4.1 `ReportResolver`
- **Tugas:** untuk `reportCode`, kembalikan engine + path file template. Resolusi: file
  master `report_templates/master/{code}.{ext}`. (Interface dirancang agar penambahan salinan
  per-user nanti = 1 implementasi, tanpa membongkar konsumen.)
- **Keamanan:** validasi `code` (regex `^[A-Za-z0-9_-]+$`) sebelum dipakai sebagai nama file
  → anti path-traversal.
- **Dependency:** `ReportMetaRepository`, `FileStorageService`.

### 4.2 `ReportDataService`
- **Tugas:** `(ReportMeta, Map<String,Object> params) → List<Map<String,Object>>`.
- **Resolusi datasource (identik `GenericFormView`):**
  1. `reportMeta.dataQuery` (custom SELECT) — dipakai duluan bila ada.
  2. else `FormMeta.viewTable` (view form; ditemukan via `tableName`).
  3. else `SELECT * FROM {qualified tableName}`.
- **Parameter:** substitusi `:name` via **`NamedParameterJdbcTemplate`** (bukan string-replace)
  → kebal SQL injection. Menggantikan implementasi `query.replace(...)` di `fetchReportData`.
- **LOV enrichment:** tambah kolom `{field}_label` via
  `ComponentFactory.formatFieldValueWithLov` (cache Caffeine) untuk field ber-LOV yang
  memetakan ke `FormMeta`.
- **Dependency:** `JdbcTemplate`/`NamedParameterJdbcTemplate`, `FormMetaRepository`,
  helper validasi query yang sudah ada (`validateAndSanitizeSelectQuery`, `resolveSqlKeywords`).

### 4.3 `ReportRenderer` (interface) + implementasi
- **Interface:** `render(ReportContext) → ReportOutput`, `export(format) → bytes/stream`.
  `ReportContext` = { template path, data, params, page/orientation }.
- **StandardRenderer:** render band → HTML "kertas" (pola `ReportViewerView` existing);
  untuk data besar arahkan ke PDF.
- **StimulsoftRenderer:** deserialize `.mrt`, suntik `StiJsonDatabase` "DynamicData",
  `render()`, tampilkan via viewer (embed). `StiReport` request-scoped.
- **JasperRenderer:** terima **`.jasper`** (fill langsung) **dan `.jrxml`** (compile + cache
  by mtime). Data via `JRMapCollectionDataSource`, params → parameter Jasper. Export via
  `JRExporter` (PDF/XLSX/DOCX/CSV).

### 4.4 `ReportParamMeta` → `FieldMeta` adapter
- **Tugas:** memetakan definisi parameter ke `FieldMeta` sehingga `ComponentFactory.create()`
  merender komponen yang tepat (termasuk LOV, DatePicker, dsb.) tanpa ubahan.

### 4.5 `ReportDesignerView` (Vaadin, bertyped)
- **Tugas:** orkestrator UI designer. Tab "Daftar Report" (grid+toolbar) + tab "Editor"
  (shell). Reuse komponen `GenericFormView`.
- **Anti-leak:** `addDetachListener` untuk lepas listener; kosongkan IFrame `src` saat ganti
  engine/detach.
- **Dependency:** `ReportMetaRepository`, `ReportParamMetaRepository` (baru), `ReportResolver`,
  `ReportDataService`, `ComponentFactory`, `FileStorageService`.

## 5. UI Designer (grid-first, pola GenericFormView)

**Tab "Daftar Report" (default):**
```
Toolbar: [+ Baru] [✎ Edit] [🎨 Desain] [🗑 Hapus] [👁 Preview] [↻ Refresh]
Grid:    ☑ | Code | Title | Engine(badge) | Source | Page | Updated   (single-select)
```
**Tab "Editor" (muncul saat Baru/Edit/Desain):**
```
Metadata  (Details, terlipat default): Code · Title · Source(table/form) · Custom Query · Page · Orient
Parameter (Details, terlipat default): Grid parameter + [+ Tambah Parameter] (dialog)
Surface editor (adaptif engine):
   STANDARD   → band canvas + properties panel (existing)
   STIMULSOFT → IFrame Web Designer penuh (master)
   JASPER     → panel Upload (.jasper & .jrxml) + Preview
```

**Aksi toolbar (baris terpilih):**
- **Baru** → Editor kosong (isi metadata → Simpan → baru bisa Desain).
- **Edit** → Editor, edit metadata + parameter.
- **Desain** → langsung ke surface editor layout.
- **Hapus** → `Dialog` konfirmasi → hapus report + file template.
- **Preview** → viewer surface (data sample `LIMIT 50`).
- **Refresh** → reload grid.

**Save-first → Designer:** report di grid = sudah tersimpan. Baris existing → "Desain" aktif.
"Baru" → wajib Simpan dulu (masuk grid) → "Desain" aktif. Sebelum simpan, tombol/IFrame designer
disabled dengan hint *"Simpan laporan dulu untuk mulai mendesain."*

## 6. Data model

- **`ReportMeta`** (existing): `reportCode, reportTitle, tableName, dataQuery, pageSize,
  orientation, engineType, elements, templatePath`.
- **`ReportParamMeta`** (baru): `id, reportCode(FK), paramName, label, dataType
  (TEXT/NUMBER/DATE/BOOLEAN/LOV), lovCode?, source (FORM_FIELD/USER_INPUT/SYSTEM), sourceKey,
  defaultValue, required, colOrder`.
- **Template file** (reuse `FileStorageService`; path di DB; **no BLOB**):
  - `uploadDir/report_templates/master/{code}.mrt` (Stimulsoft)
  - `uploadDir/report_templates/master/{code}.jasper` atau `{code}.jrxml` (Jasper)
  - Salinan per-user = **ditunda**.

## 7. Jasper — dukungan dua format

- Terima **`.jasper`** dan **`.jrxml`** saat upload.
  - `.jasper` → fill langsung (ringan, tanpa compile).
  - `.jrxml` → **compile saat upload** (validasi dini; gagal = tolak dengan pesan jelas) →
    simpan + cache hasil compile by mtime.
- **Editor** = JasperSoft Studio (desktop, eksternal). Tidak ada designer in-app.
- **Versi:** acuan = versi `jasperreports` runtime app. Tampilkan versi ini di halaman upload.
  - `.jasper`: versi Studio harus **cocok** dengan runtime.
  - `.jrxml`: Studio **≤** runtime (fitur lebih baru dari runtime berisiko gagal compile).
- **Kelemahan `.jrxml`** yang diterima secara sadar: compile runtime (diredam cache),
  butuh compiler di classpath, error muncul saat upload (mitigasi: compile-on-upload),
  eksekusi ekspresi author (upload = aksi privileged/admin).

## 8. LOV

- **Tampilan report:** kolom `{field}_label` ditambah pipeline (cache Caffeine). Template ikat
  `{field}` (ID, untuk key/grouping) dan `{field}_label` (label). Sama untuk 3 engine (diperkaya
  sebelum renderer).
- **Parameter LOV:** komponen menampilkan label, mengembalikan value (ID) → cocok dengan ID
  tersimpan di `WHERE field = :param`.
- **Data besar:** utamakan JOIN di `viewTable` form (set-based, cepat); `_label` = fallback.
- **Batasan:** custom query murni tanpa mapping ke `FormMeta` ber-LOV → resolusi label harus via
  JOIN di query itu. (Mapping LOV per-kolom di level report = future, YAGNI.)

## 9. Keamanan

- Reuse role/permission existing; **edit master = admin** (`isCurrentUserSuperAdmin`).
- Parameter → query via **`NamedParameterJdbcTemplate`** (bukan string-replace).
- Validasi `code`/nama file (anti path-traversal) di resolver & controller.
- Upload report = aksi privileged (kode/ekspresi author dieksekusi baik `.jasper` maupun `.jrxml`).
- **License Stimulsoft di-set** (hindari mode trial/watermark).
- `ReportSecurityInterceptor`: wajib login (sudah) + opsi cek otorisasi per-report.

## 10. Performa & memory-leak

- **Anti-leak:** unregister listener di `addDetachListener`; kosongkan IFrame `src` saat ganti
  engine/detach; `StiReport` request-scoped (jangan simpan di session/field).
- **Data:** tak simpan dataset besar di field; lazy `DataProvider` + LIMIT/paging
  (`fetchTableDataPaged`); lazy LOV di komponen parameter.
- **Cache:** deserialize `.mrt` / compiled `.jrxml` di-cache by mtime; `.jasper` precompiled.
- **Query:** filter di DB (params → WHERE), bukan load-semua-lalu-filter; tanpa fetch ganda
  (fetch ganda di designer sudah dibuang).
- **Query timeout bertingkat (WAJIB):** `ReportDataService` menyetel batas waktu eksekusi query
  (via `setQueryTimeout(N)` atau, lebih andal, Postgres `statement_timeout` per koneksi):
  - **Preview** = pendek (mis. 30s; data sample + LIMIT 50).
  - **Full run** = lebih longgar & configurable (mis. `app.report.query-timeout-seconds:30` untuk
    interaktif; nilai lebih besar khusus full-run bila perlu).
  Query runaway dibunuh → koneksi HikariCP (pool=25) cepat kembali → melindungi user lain. Saat
  timeout, `QueryTimeoutException` ditangkap → pesan ramah *"The report query took too long and was
  stopped. Please narrow your filter/parameters."* (bukan hang/stacktrace). Ini proteksi lintas-user
  terpenting. Report yang memang berat → jalur batch (lihat §14), **bukan** menaikkan timeout global.
- **Eksekusi async (opsional untuk report berat):** Preview/run Standard & Jasper dijalankan
  off UI thread (background executor + `UI.access()` untuk push hasil) dengan `ProgressBar`,
  supaya UI user tidak freeze. Async + query timeout + LIMIT adalah satu paket.
- **Titik ekstensi before/after (no-op sekarang):** `ReportRunService.run()` memanggil
  `beforeRun(report, params, user)` dan `afterRun(report, params, rowCount, user)` yang
  saat ini no-op. Wiring Groovy opsional (sandbox `ScriptExecutorService`) ditambahkan nanti
  bila ada kebutuhan konkret (audit/param-prep), tanpa membongkar — belum ada field script /
  kolom DB sekarang (YAGNI).

## 11. Error handling & UX state

- `ProgressBar` + disable tombol saat render/preview (anti "muter tanpa umpan balik").
- Badge engine jelas; surface error ramah (generalisasi `stimulsoft-error`).
- `Notification` untuk validasi parameter; pesan jelas untuk template hilang / SQL error /
  compile `.jrxml` gagal.
- Validasi saat simpan (code & source wajib); `Dialog` konfirmasi untuk Hapus.

## 12. Testing

- **Unit:**
  - `ReportResolver`: resolusi master + validasi path (tolak `../`, karakter ilegal).
  - `ReportDataService`: urutan datasource (dataQuery → viewTable → tableName), substitusi
    parameter, anti-injeksi (stacked/komentar ditolak), LOV enrichment.
  - Tiap `ReportRenderer`: data → output; `JasperRenderer` untuk **kedua** format; compile-on-upload
    `.jrxml` (valid & invalid).
  - Adapter `ReportParamMeta → FieldMeta`.
- **Integrasi:**
  - Endpoint tiap engine mengembalikan konten benar.
  - **Regresi fix preview**: `/stimulsoft_webdesigner_action/stimulsoft_webviewer_action`
    dilayani servlet viewer (bukan balas kosong).
  - Validasi tolak `.jrxml` rusak saat upload.
- **Manual/UX:** loading state, preview per engine, export tiap format.

## 13. Sudah diperbaiki sesi ini (dilipat masuk)

- Filter `:id`/`:param` di alur Stimulsoft (`fetchReportData` dipakai controller).
- Validasi path-traversal pada `code` di `StimulsoftJavaController`.
- **Fix preview Stimulsoft**: mapping servlet viewer untuk path ganda
  `/stimulsoft_webdesigner_action/stimulsoft_webviewer_action` di `StimulsoftConfig`
  (root cause: preview viewer mewarisi controller designer sebagai base lalu menambahkan
  `/stimulsoft_webviewer_action` → nyasar ke servlet designer yang balas kosong → `StiJsViewer`
  undefined). **Status: terverifikasi bekerja (preview designer sudah jalan).**

## 14. Future / open (di luar spec ini)

- Viewer/print surface end-user + dialog parameter runtime + integrasi print-dari-form.
- Copy-on-edit per-user (Stimulsoft) + tombol Reset ke master.
- "Cetak sesuai filter grid aktif".
- Mapping LOV per-kolom untuk custom query murni.
- Tabel tracking `report_user_template` (bila butuh listing/audit versi user).
- **Jalur batch/async untuk report berat**: submit job → jalan di background dengan timeout longgar
  di **pool koneksi khusus report (kecil, mis. 3–5)** → hasil disimpan ke file (PDF/Excel) →
  user dinotifikasi + unduh. Memanfaatkan infra `@Route("scheduler")` yang sudah ada. Dibangun
  saat ada report yang benar-benar butuh > timeout interaktif — bukan dengan menaikkan timeout global.
- Perbaikan akar report berat berulang: **materialized view** (refresh berkala), index kolom
  filter/join, read-replica untuk reporting, atau scheduled+cached.
- Tombol **Cancel** manual saat async run (lacak statement → `cancel()`).
