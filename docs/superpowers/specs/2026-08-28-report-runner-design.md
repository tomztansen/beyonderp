# Report Runner (Standalone) + Report Designer Additions — Design Spec

Tanggal: 2026-08-28
Status: Draft (menunggu review)
Scope: Layar **Report Runner** standalone untuk end-user (katalog + selection screen + output),
plus **tambahan Report Designer** yang diperlukan agar report bisa dijalankan di runner.

---

## 1. Overview

Menyediakan layar bagi end-user untuk **menjalankan report** (bukan mendesain): pilih report dari
**katalog** (berkategori + search, difilter otoritas), isi **parameter** (selection screen), lalu
**jalankan** dan lihat **output** (Standard HTML / Jasper PDF / Stimulsoft viewer) dengan Print/Export.
Untuk itu Report Designer ditambah field pengelompokan, otorisasi, dan kolom parameter Model B.

Dibangun di atas pipeline yang sudah ada: `ReportRunService`, `ReportParameterForm`,
`ReportParamResolver`, `ReportDataService`, `ReportRendererRegistry`.

## 2. Scope

**Masuk:**
- **Report Designer additions**: `Category`, `Description`, `Allowed Roles` (metadata); kolom
  parameter `Filter Column` + `Operator` (Model B).
- **Report Runner** (`@Route "report-runner"`): katalog (kategori + search + filter otoritas),
  selection screen (parameter A+B, Run/Reset), output inline per-engine (Print/Export PDF+Excel).
- **Model B parameter**: WHERE builder di `ReportDataService` (reuse validasi operator existing).

**Ditunda (spec lain):**
- Integrasi **print-dari-form** (tombol Cetak `GenericFormView`) + tautan report→form.
- Favorites/Recent, **header informatif** (ringkasan parameter/row count/waktu), saved variants,
  scheduling/email, jalur batch report berat.
- Copy-on-edit per-user (Stimulsoft).

## 3. Arsitektur

```
ReportRunnerView (@Route "report-runner")  → layar: katalog (kiri) + selection+output (kanan)
ReportCatalog (komponen)                   → daftar report berkategori + search + filter otoritas
ReportParameterForm (SUDAH ADA)            → selection screen (USER_INPUT params)
ReportRunService.run (SUDAH ADA)           → jalankan per-engine → ReportRunResult
ReportDataService (+WHERE-builder Model B) → base query + WHERE dari param operator; bind :param (A)
ReportOutputPanel (komponen)               → HTML / PDF embed / IFrame + Print/Export (async)
ReportAccessService                        → report yang boleh diakses user (roles ∩; super-admin=semua)
```

Unit kecil, dependency di-inject, bisa diuji terpisah.

## 4. Data model (tambahan)

- **`ReportMeta`** (existing) + kolom baru:
  - `category` VARCHAR — pengelompokan katalog.
  - `description` VARCHAR/TEXT — deskripsi katalog (opsional).
  - **Allowed roles**: `@ElementCollection Set<String> allowedRoles` → tabel
    `public.meta_report_role(report_code, role_code)`. Kosong = hanya SUPER_ADMIN.
- **`ReportParamMeta`** (existing) + kolom baru untuk Model B:
  - `filter_column` VARCHAR — kolom DB yang difilter.
  - `operator` VARCHAR — `=`, `!=`, `LIKE`, `ILIKE`, `>=`, `<=`, `>`, `<`, `IN` (whitelist).
- **`ReportParamMeta` — LOV Filter (reusable LOV):** agar satu LOV generik (mis. `MSCUSTOMER`)
  bisa dipakai berbagai parameter dgn batasan berbeda, tambah:
  - `lov_filter_column` VARCHAR — kolom LOV yang dibatasi (mis. `custgroup`).
  - `lov_filter_value` VARCHAR — nilai STATIC (mis. `Exp_3rd`).
  - `lov_filter_operator` VARCHAR — default `=` (whitelist).
  Adapter (`ReportParamAdapter`) menerjemahkan ini ke `FieldMeta.filters` (`FieldFilterMeta`
  sourceType=STATIC) sehingga ComboBox LOV hanya menampilkan baris yang cocok. Kosong = LOV penuh.
- **Inferensi model:** param dengan `filterColumn` + `operator` terisi = **Model B** (bangun WHERE);
  selain itu = **Model A** (bind `:paramName` ke `dataQuery`). Range (Between) = **2 param** pada
  kolom sama (mis. `>=` dan `<=`).

> Skema: tambahkan kolom via ALTER pada `meta_report` (category, description) + tabel
> `meta_report_role`, dan kolom `filter_column`/`operator` pada `meta_report_param`
> (ddl-auto=validate → tabel/kolom harus ada sebelum startup).

## 5. Report Designer additions

**Metadata form** (`ReportDesignerView`) — 3 field baru:
- **Category** — `ComboBox<String>` dengan `setAllowCustomValue(true)`, items = distinct category
  existing (dari `meta_report`), user boleh ketik baru.
- **Description** — `TextArea` (opsional).
- **Allowed Roles** — `MultiSelectComboBox<String>` items = daftar role (`app_role`). Kosong = hanya
  SUPER_ADMIN yang melihat report di runner.

**Grid parameter** — kolom inline-editable baru:
- **Filter Column** — `TextField` (nama kolom DB, Model B).
- **Operator** — `Select` (whitelist operator, Model B).
- **LOV Filter Column** / **LOV Filter Value** — `TextField` (batasi LOV generik, mis.
  `custgroup` = `Exp_3rd`). Reuse `FieldFilterMeta` (STATIC) via adapter.

Save (single Save yang sudah ada) mem-persist field baru + `allowedRoles` (cascade/@ElementCollection).

## 6. Report Runner (`@Route "report-runner"`)

**Layout** (split kiri/kanan):
```
┌ Report Runner ─────────────────────────────────────────────┐
│ [🔍 Search]                                                 │
├───────────────┬────────────────────────────────────────────┤
│ Katalog       │  Selection Screen (ReportParameterForm)     │
│ ▸ Sales       │  <param USER_INPUT: LOV/date/multi/number>  │
│   • Invoice   │  [Run]  [Reset]                              │
│ ▸ Inventory   ├────────────────────────────────────────────┤
│   • Stock     │  Output (HTML / PDF / IFrame)  [🖨][⬇ ▼]    │
└───────────────┴────────────────────────────────────────────┘
```

**Katalog (kiri):**
- Report dikelompokkan per `category` (Accordion/Details per kategori), tiap item tampil
  `reportTitle` (+ description sebagai tooltip/subtitle).
- **Search** memfilter judul/deskripsi.
- **Filter otoritas** (via `ReportAccessService`): report tampil bila user SUPER_ADMIN, atau
  `allowedRoles ∩ userRoles ≠ ∅`. Report tanpa `allowedRoles` → hanya SUPER_ADMIN.

**Selection screen (kanan-atas):**
- Pilih report → build `ReportParameterForm` dari param `USER_INPUT` (semua componentType).
- **Run** → validasi required → kumpulkan nilai → jalankan. **Reset** → kosongkan nilai.

**Output (kanan-bawah, inline):**
- `ReportOutputPanel` menampilkan sesuai engine (Standard=HTML, Jasper=PDF embed, Stimulsoft=IFrame
  viewer). Render **async** (background + `UI.access` + `ProgressBar`); **query timeout** existing →
  pesan ramah bila lama.
- **Print** (window.print area / toolbar viewer) + **Export** PDF & Excel (Jasper via `JRExporter`;
  Standard via generator PDF/Excel; Stimulsoft via toolbar viewer bawaan).

## 7. Parameter model A + B

- Kumpulkan nilai: `SYSTEM` auto (`$CURRENT_USER`/`CURRENT_DATE`) + `USER_INPUT` dari form.
  (`FORM_FIELD` tidak relevan di runner standalone — itu untuk print-dari-form.)
- **Model A:** nilai → bind `:paramName` di `dataQuery` (NamedParameterJdbcTemplate).
- **Model B:** param `filterColumn`+`operator` → `ReportDataService` bangun WHERE di atas base query:
  `SELECT * FROM ( {baseQuery} ) sub WHERE {col} {op} :{paramName} [AND ...]`.
  - `LIKE`/`ILIKE` → nilai dibungkus `%..%` bila belum mengandung wildcard.
  - `IN` → `{col} = ANY(:param)` (array).
  - Operator divalidasi whitelist (reuse `DynamicDataService.validateComparisonOperator`);
    `filterColumn` divalidasi identifier (reuse `validateSqlIdentifier`).
- Nilai di-bind via `MapSqlParameterSource` (kebal injeksi). Required kosong → validasi
  "This parameter is required".

## 8. Keamanan / otorisasi

- Di balik login (guard existing). Runner sendiri gate via izin menu existing.
- **Per-report** via `ReportAccessService.canAccess(report, user)`: `true` bila SUPER_ADMIN atau
  `allowedRoles ∩ userRoles ≠ ∅`. Report tanpa `allowedRoles` → hanya SUPER_ADMIN.
- Saat **Run**, server memverifikasi ulang akses (jangan hanya andalkan filter katalog).
- SUPER_ADMIN dideteksi via roles user (reuse pola `isCurrentUserSuperAdmin`).
- `SessionSecurityService.getCurrentUser().getRoles()` = sumber role user.

## 9. Performa & memory-leak

- Reuse: query timeout (`ReportDataService`), async render + `UI.access`, `ProgressBar`.
- Katalog: muat metadata report (ringan) + roles; jangan muat data report sampai Run.
- Anti-leak: unregister listener di detach; kosongkan IFrame `src` saat ganti report/detach;
  tak simpan dataset besar di field; LIMIT saat perlu.

## 10. Testing

- **Unit:**
  - `ReportAccessService`: super-admin bypass; role ∩; report tanpa roles → hanya super-admin.
  - `ReportDataService` WHERE-builder Model B: operator (=, LIKE→%wrap, IN→ANY, ≥/≤ range 2 param),
    validasi operator/identifier, bind nilai.
  - Katalog: grouping per kategori + filter search.
  - Output routing per engine (Standard/Jasper/Stimulsoft) — reuse test renderer existing.
- **Integrasi/manual:** katalog+search+kategori; selection tiap tipe param; Run tiap engine;
  Print/Export; uji dengan user **non-admin** (lihat hanya report yang diizinkan) dan **super-admin**
  (lihat semua).

## 11. Future / open (di luar spec ini)

- Print-dari-form (tombol Cetak `GenericFormView`) + tautan report→form + param `FORM_FIELD`.
- Favorites/Recent, header informatif (ringkasan parameter/row count/waktu), saved variants.
- Scheduling/email + jalur batch report berat.
- Otorisasi per-user granular (di atas role), copy-on-edit per-user.
