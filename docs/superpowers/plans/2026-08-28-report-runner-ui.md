# Report Runner UI + LOV Filter — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Bangun layar Report Runner (katalog + selection + output) untuk end-user menjalankan report, plus LOV Filter (parameter membatasi LOV generik yang reusable).

**Architecture:** `ReportRunnerView` (split: katalog kiri berkategori + filter otoritas, selection+output kanan). Reuse `ReportRunService`, `ReportParameterForm`, `ReportParamResolver`, `ReportAccessService`, dan pola output `preview()` dari `ReportDesignerView`. LOV Filter: `ReportParamMeta` + kolom `lov_filter_*`, adapter set `FieldMeta.filters` (FieldFilterMeta STATIC).

**Tech Stack:** Java 21, Spring Boot 3.3.0, Vaadin, PostgreSQL.

**Spec:** `docs/superpowers/specs/2026-08-28-report-runner-design.md`

## Global Constraints

- `ddl-auto=validate` → ALTER kolom baru sebelum entity dipakai.
- Semua teks UI Bahasa Inggris. Async render (background + `UI.access` + `ProgressBar`) + query timeout (existing).
- Otorisasi: `ReportAccessService` (super-admin=semua; kosong roles=super-admin only).
- Test: JUnit 5 + Mockito + AssertJ. View Vaadin diverifikasi manual (langkah tercantum).

---

### Task 1: LOV Filter — kolom DB + field entity

**Files:** Modify `src/main/java/com/vaadinerp/meta/ReportParamMeta.java`; Test `.../meta/ReportParamLovFilterTest.java`

**Interfaces:** Produces `ReportParamMeta`: `getLovFilterColumn/setLovFilterColumn`, `getLovFilterValue/set...`, `getLovFilterOperator/set...`.

- [ ] **Step 1: DB ALTER (idempoten)**

```sql
ALTER TABLE public.meta_report_param ADD COLUMN IF NOT EXISTS lov_filter_column VARCHAR(50);
ALTER TABLE public.meta_report_param ADD COLUMN IF NOT EXISTS lov_filter_value VARCHAR(255);
ALTER TABLE public.meta_report_param ADD COLUMN IF NOT EXISTS lov_filter_operator VARCHAR(10);
```
Verify: `SELECT count(*) FROM information_schema.columns WHERE table_name='meta_report_param' AND column_name LIKE 'lov_filter%';` → 3.

- [ ] **Step 2: Failing test**

```java
package com.vaadinerp.meta;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class ReportParamLovFilterTest {
    @Test void hasLovFilterFields() {
        ReportParamMeta p = new ReportParamMeta();
        p.setLovFilterColumn("custgroup");
        p.setLovFilterValue("Exp_3rd");
        p.setLovFilterOperator("=");
        assertThat(p.getLovFilterColumn()).isEqualTo("custgroup");
        assertThat(p.getLovFilterValue()).isEqualTo("Exp_3rd");
        assertThat(p.getLovFilterOperator()).isEqualTo("=");
    }
}
```
Run: `mvn -q -Dtest=ReportParamLovFilterTest test` → FAIL.

- [ ] **Step 3: Tambah field ke `ReportParamMeta`** (setelah `operator`)

```java
    @Column(name = "lov_filter_column", length = 50)
    private String lovFilterColumn;
    @Column(name = "lov_filter_value", length = 255)
    private String lovFilterValue;
    @Column(name = "lov_filter_operator", length = 10)
    private String lovFilterOperator;
```
Run: `mvn -q -Dtest=ReportParamLovFilterTest test` → PASS.

- [ ] **Step 4: Commit**
```bash
git add src/main/java/com/vaadinerp/meta/ReportParamMeta.java src/test/java/com/vaadinerp/meta/ReportParamLovFilterTest.java
git commit -m "feat: add LOV filter fields to ReportParamMeta"
```

---

### Task 2: Adapter — LOV filter → `FieldMeta.filters`

**Files:** Modify `src/main/java/com/vaadinerp/report/ReportParamAdapter.java`; Test `.../report/ReportParamAdapterLovFilterTest.java`

**Interfaces:** `ReportParamAdapter.toFieldMeta` menambahkan satu `FieldFilterMeta` (sourceType=STATIC, filterColumn=lovFilterColumn, sourceName=lovFilterValue, comparisonOperator=lovFilterOperator||"=") ke `FieldMeta.setFilters(...)` bila `lovFilterColumn` + `lovFilterValue` terisi.

> Verifikasi API `FieldMeta.setFilters(List<FieldFilterMeta>)` + setter `FieldFilterMeta` (setFilterColumn/setSourceType/setSourceName/setComparisonOperator/setLogicalOperator) — reuse pola yang dipakai `DataInitializer`.

- [ ] **Step 1: Failing test**

```java
package com.vaadinerp.report;
import com.vaadinerp.meta.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class ReportParamAdapterLovFilterTest {
    @Test void setsStaticLovFilterOnFieldMeta() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("cust"); p.setParamType("COMBOBOX"); p.setLovCode("MSCUSTOMER");
        p.setLovFilterColumn("custgroup"); p.setLovFilterValue("Exp_3rd");
        FieldMeta f = ReportParamAdapter.toFieldMeta(p);
        assertThat(f.getFilters()).hasSize(1);
        FieldFilterMeta flt = f.getFilters().get(0);
        assertThat(flt.getFilterColumn()).isEqualTo("custgroup");
        assertThat(flt.getSourceType()).isEqualTo("STATIC");
        assertThat(flt.getSourceName()).isEqualTo("Exp_3rd");
    }
    @Test void noFilterWhenEmpty() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("x"); p.setLovCode("L");
        FieldMeta f = ReportParamAdapter.toFieldMeta(p);
        assertThat(f.getFilters() == null || f.getFilters().isEmpty()).isTrue();
    }
}
```
Run → FAIL.

- [ ] **Step 2: Implementasi** — di `toFieldMeta`, sebelum `return f`:

```java
        if (p.getLovFilterColumn() != null && !p.getLovFilterColumn().isBlank()
                && p.getLovFilterValue() != null && !p.getLovFilterValue().isBlank()) {
            FieldFilterMeta flt = new FieldFilterMeta();
            flt.setFilterColumn(p.getLovFilterColumn().trim());
            flt.setSourceType("STATIC");
            flt.setSourceName(p.getLovFilterValue().trim());
            flt.setComparisonOperator(p.getLovFilterOperator() != null && !p.getLovFilterOperator().isBlank()
                    ? p.getLovFilterOperator().trim() : "=");
            flt.setLogicalOperator("AND");
            f.setFilters(new java.util.ArrayList<>(java.util.List.of(flt)));
        }
```
Run → PASS.

- [ ] **Step 3: Commit**
```bash
git add src/main/java/com/vaadinerp/report/ReportParamAdapter.java src/test/java/com/vaadinerp/report/ReportParamAdapterLovFilterTest.java
git commit -m "feat: adapter maps LOV filter to FieldMeta.filters (STATIC)"
```

---

### Task 3: Report Designer — kolom LOV Filter di grid parameter

**Files:** Modify `src/main/java/com/vaadinerp/views/ReportDesignerView.java`

- [ ] **Step 1:** Tambah 2 kolom inline-editable (pola sama seperti Filter Column/Operator): `LOV Filter Col` (bind `lovFilterColumn`) + `LOV Filter Value` (bind `lovFilterValue`). Tambah ke `pGetters`. Update `cloneParam` menyalin `lovFilterColumn/lovFilterValue/lovFilterOperator`.
- [ ] **Step 2:** `mvn -q -DskipTests compile` → BUILD SUCCESS.
- [ ] **Step 3 (manual):** restart → Designer → param grid punya kolom LOV Filter; isi `custgroup`/`Exp_3rd`, Save, cek DB.
- [ ] **Step 4:** commit.

---

### Task 4: `ReportRunnerView` — kerangka (katalog + panel)

**Files:** Create `src/main/java/com/vaadinerp/views/ReportRunnerView.java`

**Interfaces:** `@Route("report-runner")`; inject `ReportMetaRepository`, `ReportAccessService`, `ReportRunService`, `DynamicDataService`, `SessionSecurityService`.

- [ ] **Step 1:** Layout split: kiri = katalog, kanan = `VerticalLayout` (selection + output).
  - Katalog: `reportMetaRepository.findAll()` → `reportAccessService.accessibleReports(all)` → group per `category` (Accordion/Details), item = tombol/RouterLink `reportTitle`. Search `TextField` memfilter judul/description (rebuild katalog).
  - Klik item → `selectReport(report)` (Task 5).
- [ ] **Step 2:** `mvn -q -DskipTests compile` → SUCCESS.
- [ ] **Step 3 (manual):** login → `/report-runner` → katalog tampil report yang boleh diakses, berkategori, search jalan. Uji user non-admin vs super-admin.
- [ ] **Step 4:** commit.

---

### Task 5: Selection screen (parameter) + Run/Reset

**Files:** Modify `ReportRunnerView`

- [ ] **Step 1:** `selectReport(report)`: bangun `ReportParameterForm(report.getParams(), dynamicDataService)` di panel kanan-atas + tombol **Run**/**Reset**.
  - **Run**: kumpulkan `SYSTEM` via `ReportParamResolver.resolveAuto(params, Map.of(), currentUser)` + nilai `USER_INPUT` dari `form.collectValues()`; untuk tiap param isi default bila kosong; validasi `required`; panggil `runReport(report, values)` (Task 6).
  - **Reset**: rebuild form kosong.
- [ ] **Step 2:** compile + manual (pilih report seed → ComboBox Customer muncul dari LOV).
- [ ] **Step 3:** commit.

---

### Task 6: Output panel (engine-aware, async) + Print/Export

**Files:** Modify `ReportRunnerView` (reuse pola `ReportDesignerView.preview()`)

- [ ] **Step 1:** `runReport(report, values)`:
  - `ReportRunResult res = reportRunService.run(report, values, false);`
  - STIMULSOFT → set IFrame `res.viewerUrl()` di panel output.
  - else → async (`UI ui = UI.getCurrent()`, background thread, `ui.access`): HTML → `com.vaadin.flow.component.Html`; PDF → `IFrame data:...base64`. `ProgressBar` saat menunggu; tangkap `QueryTimeoutException` → pesan ramah.
  - **Print**: `ui.getPage().executeJs("window.print()")` (Standard/Jasper) atau toolbar viewer (Stimulsoft).
  - **Export**: tombol untuk PDF/Excel (Jasper via renderer.export; Standard via generator — bila belum ada, tandai future).
- [ ] **Step 2:** compile + manual (Run report seed → pilih customer → output terfilter `invoiceaccount = :cust`).
- [ ] **Step 3:** commit.

---

### Task 7: Menu + verifikasi + ganti contoh ke LOV generik

- [ ] **Step 1 (DB):** daftarkan menu Report Runner:
```sql
INSERT INTO public.app_menus (menu_code, menu_title, route_path, icon_name, parent_menu_code, display_order, menu_type)
SELECT 'REPORT_RUNNER','Run Report','report-runner','PLAY','RPT_MGMT',5,'ITEM'
WHERE NOT EXISTS (SELECT 1 FROM public.app_menus WHERE menu_code='REPORT_RUNNER');
INSERT INTO public.app_role_menu_permissions (role_code, menu_code, can_add, can_edit, can_delete, can_print)
SELECT role_code,'REPORT_RUNNER',false,false,false,true FROM public.app_role_menu_permissions WHERE menu_code='REPORT_DESIGNER';
```
Dan tambah `case "REPORT_RUNNER" -> new ReportRunnerView(...)` di `PortalView` (deps via SpringContextHolder bila perlu).
- [ ] **Step 2 (contoh reusable LOV):** buat LOV generik + ubah parameter seed:
```sql
INSERT INTO public.meta_lov (lov_code, lov_name, table_name, value_column, label_column, search_column)
VALUES ('MSCUSTOMER','Customer','dynamic.mscustomer','accountnum','name','name')
ON CONFLICT (lov_code) DO NOTHING;
UPDATE public.meta_report_param SET lov_code='MSCUSTOMER', lov_filter_column='custgroup', lov_filter_value='Exp_3rd', lov_filter_operator='='
WHERE report_code='RPT_SALESLINE_EXP3RD' AND param_name='cust';
```
- [ ] **Step 3:** `mvn test` → semua PASS.
- [ ] **Step 4 (manual e2e):** restart → menu **Run Report** → pilih report Sales → ComboBox Customer **hanya Exp_3rd** (dari LOV generik + filter) → pilih → output tssalesline terfilter. Uji user non-admin.
- [ ] **Step 5:** commit.

---

## Self-Review Checklist (sudah dijalankan)
- **Spec coverage:** LOV Filter (T1-T3), Runner katalog+otoritas (T4), selection A+B (T5), output per-engine+async (T6), menu+contoh reusable (T7).
- **Placeholder scan:** UI diberi langkah manual konkret; testable (entity+adapter) berkode nyata. Export Standard PDF/Excel ditandai future bila belum ada generator.
- **Type consistency:** `ReportParamAdapter`/`ReportRunService`/`ReportAccessService`/`ReportParameterForm` dipakai konsisten dengan Plan A.

## Catatan
- View Vaadin manual (belum ada Karibu-Testing). Verifikasi otorisasi wajib pakai user non-admin.
- Pastikan ComboBox LOV dari `ComponentFactory.create` menerapkan `FieldMeta.filters`; bila jalur main belum, reuse logika filter dari `SubformGridField`.
