# Print-dari-Form Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tombol Cetak di `GenericFormView` menjalankan report atas baris yang dicentang di grid, dengan parameter `FORM_FIELD` terisi otomatis dari baris tersebut, dan output dibuka sebagai tab aplikasi.

**Architecture:** Menyambungkan pipeline report yang sudah ada (`ReportRunService` / `ReportParamResolver` / `ReportAccessService`) ke tombol Cetak. Baris terpilih selalu dikirim sebagai `List`, query dijalankan sekali, dan template yang menentukan bentuk keluaran (dokumen per halaman lewat group band, atau listing). Logika output yang sekarang hanya ada di `ReportRunnerView` diekstrak ke `ReportLauncher` agar dipakai bersama.

**Tech Stack:** Java 21, Spring Boot 3.3.0, Vaadin 24.10.7, PostgreSQL, JasperReports 7.0.7, Stimulsoft 2026.3.2.

**Spec:** `docs/superpowers/specs/2026-08-29-print-from-form-design.md`

## Global Constraints

- Java 21; Spring Boot 3.3.0; build & test via Maven (`mvn`, tidak ada wrapper).
- `spring.jpa.hibernate.ddl-auto=validate` → **kolom DB harus di-ALTER sebelum entity dipakai**, kalau tidak aplikasi gagal start.
- Parameter query WAJIB via `NamedParameterJdbcTemplate` + `MapSqlParameterSource` — bukan perangkaian string.
- Untuk nilai `Collection`, bentuk SQL yang benar adalah `IN (:param)` — **bukan** `= ANY(:param)`. Spring meng-expand `Collection` hanya pada bentuk `IN (...)`.
- `reportCode` yang dipakai sebagai nama berkas WAJIB lolos regex `^[A-Za-z0-9_-]+$` (anti path-traversal).
- **SEMUA teks user-facing dalam Bahasa Inggris** — caption tombol, header kolom, label, placeholder, `Notification`, dan pesan error. (Narasi plan boleh Indonesia; string di dalam kode WAJIB Inggris.)
- Test: JUnit 5 + Mockito + AssertJ (dari `spring-boot-starter-test`). Satu test: `mvn -q -Dtest=ClassName#method test`.
- View Vaadin diverifikasi **manual** (belum ada Karibu-Testing); langkahnya tercantum di tiap task UI.
- DB pengembangan: `jdbc:postgresql://localhost:5432/grp`, user `postgres`, password `postgres`, schema `public,dynamic`.
- Jangan menyentuh cabang `operator = 'IN'` yang sudah ada di `buildModelBWhere` (di luar cakupan; dicatat sebagai future di spec §12).

---

### Task 1: Kolom DB + field entity (`usageScope`, `groupBy`, `reportSourceKey()`)

**Files:**
- Modify: `src/main/java/com/vaadinerp/meta/ReportMeta.java`
- Modify: `src/main/java/com/vaadinerp/meta/FormMeta.java`
- Modify: `src/main/resources/db-migration.sql`
- Test: `src/test/java/com/vaadinerp/meta/ReportMetaUsageScopeTest.java`

**Interfaces:**
- Consumes: —
- Produces:
  - `ReportMeta.getUsageScope()` / `setUsageScope(String)` — `FORM` | `RUNNER` | `BOTH`, default `"RUNNER"`.
  - `ReportMeta.getGroupBy()` / `setGroupBy(String)` — nama kolom hasil query untuk grouping engine STANDARD.
  - `ReportMeta.isUsableFrom(String scope)` — `true` bila `usageScope` sama dengan `scope` atau `BOTH`; `null` diperlakukan sebagai `RUNNER`.
  - `FormMeta.reportSourceKey()` — `tableName` bila ada, jika tidak `formCode`; `null` bila form tidak punya tabel maupun view.

- [ ] **Step 1: Jalankan ALTER (idempoten)**

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -d grp -c "
ALTER TABLE public.meta_report ADD COLUMN IF NOT EXISTS usage_scope VARCHAR(20) DEFAULT 'RUNNER';
ALTER TABLE public.meta_report ADD COLUMN IF NOT EXISTS group_by VARCHAR(100);
UPDATE public.meta_report SET usage_scope = 'RUNNER' WHERE usage_scope IS NULL;"
```

Verifikasi:

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -d grp -t -c "
SELECT count(*) FROM information_schema.columns
WHERE table_schema='public' AND table_name='meta_report'
  AND column_name IN ('usage_scope','group_by');"
```

Expected: `2`

- [ ] **Step 2: Catat ALTER yang sama ke `db-migration.sql`**

Tambahkan di akhir berkas:

```sql
-- Print-dari-form: cakupan pemakaian report + kunci grouping engine STANDARD
ALTER TABLE public.meta_report ADD COLUMN IF NOT EXISTS usage_scope VARCHAR(20) DEFAULT 'RUNNER';
ALTER TABLE public.meta_report ADD COLUMN IF NOT EXISTS group_by VARCHAR(100);
UPDATE public.meta_report SET usage_scope = 'RUNNER' WHERE usage_scope IS NULL;
```

- [ ] **Step 3: Tulis failing test**

Buat `src/test/java/com/vaadinerp/meta/ReportMetaUsageScopeTest.java`:

```java
package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportMetaUsageScopeTest {

    @Test
    void defaultsToRunnerOnly() {
        ReportMeta r = new ReportMeta();
        assertThat(r.getUsageScope()).isEqualTo("RUNNER");
        assertThat(r.isUsableFrom("RUNNER")).isTrue();
        assertThat(r.isUsableFrom("FORM")).isFalse();
    }

    @Test
    void bothIsUsableFromEitherSide() {
        ReportMeta r = new ReportMeta();
        r.setUsageScope("BOTH");
        assertThat(r.isUsableFrom("FORM")).isTrue();
        assertThat(r.isUsableFrom("RUNNER")).isTrue();
    }

    @Test
    void formOnlyIsHiddenFromRunner() {
        ReportMeta r = new ReportMeta();
        r.setUsageScope("FORM");
        assertThat(r.isUsableFrom("FORM")).isTrue();
        assertThat(r.isUsableFrom("RUNNER")).isFalse();
    }

    @Test
    void nullScopeBehavesAsRunner() {
        ReportMeta r = new ReportMeta();
        r.setUsageScope(null);
        assertThat(r.isUsableFrom("RUNNER")).isTrue();
        assertThat(r.isUsableFrom("FORM")).isFalse();
    }

    @Test
    void comparisonIgnoresCaseAndPadding() {
        ReportMeta r = new ReportMeta();
        r.setUsageScope("  both  ");
        assertThat(r.isUsableFrom("form")).isTrue();
    }

    @Test
    void groupByStoresColumnName() {
        ReportMeta r = new ReportMeta();
        r.setGroupBy("bom_id");
        assertThat(r.getGroupBy()).isEqualTo("bom_id");
    }

    @Test
    void formSourceKeyPrefersTableNameThenFormCode() {
        FormMeta withTable = new FormMeta();
        withTable.setFormCode("BOM_ALL");
        withTable.setTableName("mhbom");
        assertThat(withTable.reportSourceKey()).isEqualTo("mhbom");

        FormMeta viewOnly = new FormMeta();
        viewOnly.setFormCode("SO_LINE");
        viewOnly.setViewTable("select * from tssalesline");
        assertThat(viewOnly.reportSourceKey()).isEqualTo("SO_LINE");

        FormMeta empty = new FormMeta();
        empty.setFormCode("NOTHING");
        assertThat(empty.reportSourceKey()).isNull();
    }
}
```

- [ ] **Step 4: Jalankan test — pastikan GAGAL**

Run: `mvn -q -Dtest=ReportMetaUsageScopeTest test`
Expected: FAIL (kompilasi gagal — `getUsageScope`, `isUsableFrom`, `reportSourceKey` belum ada).

- [ ] **Step 5: Tambah field ke `ReportMeta`**

Sisipkan setelah field `description` (sekitar baris 43):

```java
    /** FORM | RUNNER | BOTH — di mana report ini boleh dijalankan. */
    @Column(name = "usage_scope", length = 20)
    private String usageScope = "RUNNER";

    /** Nama kolom hasil query yang menjadi kunci grouping (engine STANDARD). */
    @Column(name = "group_by", length = 100)
    private String groupBy;

    /**
     * Apakah report boleh dijalankan dari {@code scope} ("FORM" atau "RUNNER").
     * {@code usageScope} kosong diperlakukan sebagai RUNNER, sehingga report lama
     * tidak berubah perilaku.
     */
    @Transient
    public boolean isUsableFrom(String scope) {
        if (scope == null) return false;
        String s = (usageScope == null || usageScope.isBlank()) ? "RUNNER" : usageScope.trim();
        return s.equalsIgnoreCase("BOTH") || s.equalsIgnoreCase(scope.trim());
    }
```

> `ReportMeta` memakai Lombok `@Getter`/`@Setter`, jadi getter/setter kedua field terbentuk otomatis. `@Transient` di sini adalah `jakarta.persistence.Transient` (sudah ter-import lewat `jakarta.persistence.*`).

- [ ] **Step 6: Tambah `reportSourceKey()` ke `FormMeta`**

Sisipkan tepat setelah method `effectiveSource()` (sekitar baris 44):

```java
    /**
     * Kunci yang dipakai {@code meta_report.table_name} untuk menunjuk form ini:
     * nama tabel bila ada, jika tidak kode form (untuk form yang hanya punya view —
     * sebuah view bisa berupa SELECT utuh yang tak muat di table_name). Null bila
     * form tidak punya tabel maupun view, sehingga tidak bisa menjadi sumber report.
     */
    @Transient
    public String reportSourceKey() {
        if (tableName != null && !tableName.isBlank()) return tableName.trim();
        if (viewTable != null && !viewTable.isBlank()) return formCode;
        return null;
    }
```

- [ ] **Step 7: Jalankan test — pastikan LULUS**

Run: `mvn -q -Dtest=ReportMetaUsageScopeTest test`
Expected: PASS.

- [ ] **Step 8: Pakai ulang helper di `ReportDesignerView`**

Di `src/main/java/com/vaadinerp/views/ReportDesignerView.java`, ganti isi method `sourceKeyOf` (sekitar baris 232-241) agar mendelegasikan ke satu definisi:

```java
    private static String sourceKeyOf(FormMeta f) {
        return f.reportSourceKey();
    }
```

Hapus komentar lama di atasnya yang menjelaskan logika duplikat, ganti dengan:

```java
    /** Lihat {@link FormMeta#reportSourceKey()} — definisi tunggal ada di entity. */
```

- [ ] **Step 9: Kompilasi penuh**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/vaadinerp/meta/ReportMeta.java \
        src/main/java/com/vaadinerp/meta/FormMeta.java \
        src/main/java/com/vaadinerp/views/ReportDesignerView.java \
        src/main/resources/db-migration.sql \
        src/test/java/com/vaadinerp/meta/ReportMetaUsageScopeTest.java
git commit -m "feat: add usage_scope and group_by to ReportMeta, shared reportSourceKey on FormMeta"
```

---

### Task 2: `ReportParamResolver.resolveFromRows` — baris terpilih jadi List

**Files:**
- Modify: `src/main/java/com/vaadinerp/report/ReportParamResolver.java`
- Test: `src/test/java/com/vaadinerp/report/ReportParamResolverFromRowsTest.java`

**Interfaces:**
- Consumes: `ReportParamMeta` (`getSource`, `getSourceKey`, `getParamName`).
- Produces: `static Map<String,Object> ReportParamResolver.resolveFromRows(List<ReportParamMeta> params, List<Map<String,Object>> rows, String currentUser)` — parameter `FORM_FIELD` menjadi `List<Object>` berisi nilai kolom `sourceKey` dari tiap baris (urutan dipertahankan, duplikat dan `null` dibuang; key tidak dimasukkan bila hasilnya kosong). `SYSTEM` sama seperti `resolveAuto`. `USER_INPUT` dilewati.

- [ ] **Step 1: Tulis failing test**

Buat `src/test/java/com/vaadinerp/report/ReportParamResolverFromRowsTest.java`:

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportParamResolverFromRowsTest {

    private ReportParamMeta param(String name, String source, String key) {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName(name);
        p.setSource(source);
        p.setSourceKey(key);
        return p;
    }

    private Map<String, Object> row(String key, Object value) {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        return m;
    }

    @Test
    void formFieldCollectsValuesFromEveryRow() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(row("id", 38), row("id", 42)), "bob");

        assertThat(out).containsKey("bom_id");
        assertThat((List<?>) out.get("bom_id")).containsExactly(38, 42);
    }

    @Test
    void singleRowStillProducesAList() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(row("id", 38)), "bob");

        assertThat(out.get("bom_id")).isInstanceOf(List.class);
        assertThat((List<?>) out.get("bom_id")).containsExactly(38);
    }

    @Test
    void duplicatesRemovedOrderPreserved() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(row("id", 42), row("id", 38), row("id", 42)), "bob");

        assertThat((List<?>) out.get("bom_id")).containsExactly(42, 38);
    }

    @Test
    void nullValuesDropped() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(row("id", 38), row("id", null)), "bob");

        assertThat((List<?>) out.get("bom_id")).containsExactly(38);
    }

    @Test
    void noRowsOmitsTheKeyEntirely() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                List.of(), "bob");

        assertThat(out).doesNotContainKey("bom_id");
    }

    @Test
    void nullRowsTreatedAsEmpty() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")), null, "bob");

        assertThat(out).isEmpty();
    }

    @Test
    void systemParamsStillResolved() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("u", "SYSTEM", "$CURRENT_USER")),
                List.of(row("id", 1)), "bob");

        assertThat(out).containsEntry("u", "bob");
    }

    @Test
    void userInputIgnored() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("x", "USER_INPUT", null)),
                List.of(row("id", 1)), "bob");

        assertThat(out).isEmpty();
    }

    @Test
    void formFieldWithoutSourceKeyIgnored() {
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", null)),
                List.of(row("id", 38)), "bob");

        assertThat(out).isEmpty();
    }

    @Test
    void resolveAutoStillReturnsScalarForFormField() {
        // Jalur lama (Report Runner) tidak boleh berubah.
        Map<String, Object> out = ReportParamResolver.resolveAuto(
                List.of(param("bom_id", "FORM_FIELD", "id")),
                row("id", 38), "bob");

        assertThat(out).containsEntry("bom_id", 38);
    }

    @Test
    void mutableRowListAccepted() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row("id", 7));
        Map<String, Object> out = ReportParamResolver.resolveFromRows(
                List.of(param("bom_id", "FORM_FIELD", "id")), rows, "bob");

        assertThat((List<?>) out.get("bom_id")).containsExactly(7);
    }
}
```

- [ ] **Step 2: Jalankan test — pastikan GAGAL**

Run: `mvn -q -Dtest=ReportParamResolverFromRowsTest test`
Expected: FAIL (`resolveFromRows` belum ada).

- [ ] **Step 3: Implementasi**

Di `ReportParamResolver.java`, ganti isi kelas menjadi (menambah `resolveFromRows` dan mengekstrak logika SYSTEM agar tidak terduplikasi):

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolusi parameter otomatis:
 * FORM_FIELD → ambil dari record form yang terbuka; SYSTEM → keyword ($CURRENT_USER, CURRENT_DATE);
 * USER_INPUT → diabaikan (diisi user via ReportParameterForm).
 */
public final class ReportParamResolver {

    private ReportParamResolver() {}

    private static String sourceOf(ReportParamMeta p) {
        return p.getSource() == null ? "USER_INPUT" : p.getSource().trim().toUpperCase();
    }

    /** SYSTEM keyword → nilai. Dipakai bersama oleh resolveAuto dan resolveFromRows. */
    private static void putSystem(ReportParamMeta p, String currentUser, Map<String, Object> out) {
        String key = p.getSourceKey() == null ? "" : p.getSourceKey().trim().toUpperCase();
        if (key.equals("$CURRENT_USER")) {
            out.put(p.getParamName(), currentUser);
        } else if (key.equals("CURRENT_DATE")) {
            out.put(p.getParamName(), LocalDate.now());
        }
    }

    /** Satu record form → nilai skalar untuk FORM_FIELD. Dipakai Report Runner. */
    public static Map<String, Object> resolveAuto(List<ReportParamMeta> params,
                                                  Map<String, Object> record, String currentUser) {
        Map<String, Object> out = new HashMap<>();
        if (params == null) return out;
        for (ReportParamMeta p : params) {
            String source = sourceOf(p);
            if ("FORM_FIELD".equals(source)) {
                if (record != null && p.getSourceKey() != null && record.containsKey(p.getSourceKey())) {
                    out.put(p.getParamName(), record.get(p.getSourceKey()));
                }
            } else if ("SYSTEM".equals(source)) {
                putSystem(p, currentUser, out);
            }
            // USER_INPUT: diisi user via ReportParameterForm
        }
        return out;
    }

    /**
     * Baris terpilih di grid → nilai FORM_FIELD berupa List, baik satu baris maupun banyak.
     * Aturan tunggal ini mencegah report berjalan saat user mencentang satu baris lalu gagal
     * saat mencentang baris kedua. Duplikat dan null dibuang; key tidak dimasukkan bila
     * hasilnya kosong, sehingga parameter yang tidak terisi tetap terdeteksi validasi required.
     */
    public static Map<String, Object> resolveFromRows(List<ReportParamMeta> params,
                                                      List<Map<String, Object>> rows, String currentUser) {
        Map<String, Object> out = new HashMap<>();
        if (params == null) return out;
        List<Map<String, Object>> safeRows = (rows == null) ? List.of() : rows;
        for (ReportParamMeta p : params) {
            String source = sourceOf(p);
            if ("FORM_FIELD".equals(source)) {
                String key = p.getSourceKey();
                if (key == null || key.isBlank()) continue;
                List<Object> values = new ArrayList<>();
                for (Map<String, Object> row : safeRows) {
                    if (row == null) continue;
                    Object v = row.get(key);
                    if (v != null && !values.contains(v)) values.add(v);
                }
                if (!values.isEmpty()) out.put(p.getParamName(), values);
            } else if ("SYSTEM".equals(source)) {
                putSystem(p, currentUser, out);
            }
            // USER_INPUT: diisi user via ReportParameterForm
        }
        return out;
    }

    public static List<ReportParamMeta> userInputParams(List<ReportParamMeta> params) {
        if (params == null) return List.of();
        return params.stream()
                .filter(p -> p.getSource() == null || "USER_INPUT".equalsIgnoreCase(p.getSource().trim()))
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 4: Jalankan test — pastikan LULUS**

Run: `mvn -q -Dtest=ReportParamResolverFromRowsTest test`
Expected: PASS.

- [ ] **Step 5: Pastikan test lama tetap lulus**

Run: `mvn -q -Dtest=ReportParamResolverTest test`
Expected: PASS (jalur `resolveAuto` tidak berubah perilakunya).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportParamResolver.java \
        src/test/java/com/vaadinerp/report/ReportParamResolverFromRowsTest.java
git commit -m "feat: resolveFromRows maps selected grid rows to list-valued FORM_FIELD params"
```

---

### Task 3: `ReportDataService` — parameter `FORM_FIELD` jadi `IN (:param)`

**Files:**
- Modify: `src/main/java/com/vaadinerp/report/ReportDataService.java:63-92`
- Test: `src/test/java/com/vaadinerp/report/ReportDataServiceFormFieldTest.java`

**Interfaces:**
- Consumes: `ReportParamMeta.getSource()`, `getFilterColumn()`, `getOperator()`, `getParamName()`.
- Produces: `ReportDataService.buildModelBWhere` (signature tidak berubah) kini menghasilkan `{col} IN (:param)` untuk parameter dengan `source = FORM_FIELD`, mengabaikan `operator`, dan melewati nilai `Collection` yang kosong.

- [ ] **Step 1: Tulis failing test**

Buat `src/test/java/com/vaadinerp/report/ReportDataServiceFormFieldTest.java`:

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportDataServiceFormFieldTest {

    private ReportParamMeta formFieldParam(String name, String column, String operator) {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName(name);
        p.setSource("FORM_FIELD");
        p.setSourceKey("id");
        p.setFilterColumn(column);
        p.setOperator(operator);
        return p;
    }

    @Test
    void formFieldUsesInClauseNotAny() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "=")),
                Map.of("bom_id", List.of(38, 42)), bind);

        // Spring hanya meng-expand Collection pada bentuk IN (...), bukan = ANY(...).
        assertThat(where).isEqualTo(" WHERE id IN (:bom_id)");
        assertThat(where).doesNotContain("ANY");
        assertThat(bind).containsEntry("bom_id", List.of(38, 42));
    }

    @Test
    void storedOperatorIsIgnoredForFormField() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "LIKE")),
                Map.of("bom_id", List.of(38)), bind);

        assertThat(where).isEqualTo(" WHERE id IN (:bom_id)");
        // LIKE tidak boleh membungkus nilai dengan %..%
        assertThat(bind).containsEntry("bom_id", List.of(38));
    }

    @Test
    void singleElementListStillUsesInClause() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "=")),
                Map.of("bom_id", List.of(38)), bind);

        assertThat(where).isEqualTo(" WHERE id IN (:bom_id)");
    }

    @Test
    void emptyCollectionProducesNoClause() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "=")),
                Map.of("bom_id", List.of()), bind);

        assertThat(where).isEmpty();
        assertThat(bind).isEmpty();
    }

    @Test
    void formFieldWithoutFilterColumnIsModelAAndSkipped() {
        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", null, null)),
                Map.of("bom_id", List.of(38)), bind);

        assertThat(where).isEmpty();
    }

    @Test
    void userInputParamKeepsItsOperator() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("name");
        p.setSource("USER_INPUT");
        p.setFilterColumn("itemname");
        p.setOperator("LIKE");

        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p), Map.of("name", "bolt"), bind);

        assertThat(where).isEqualTo(" WHERE itemname LIKE :name");
        assertThat(bind).containsEntry("name", "%bolt%");
    }

    @Test
    void multipleParamsJoinedWithAnd() {
        ReportParamMeta user = new ReportParamMeta();
        user.setParamName("name");
        user.setSource("USER_INPUT");
        user.setFilterColumn("itemname");
        user.setOperator("=");

        Map<String, Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(formFieldParam("bom_id", "id", "="), user),
                Map.of("bom_id", List.of(38), "name", "bolt"), bind);

        assertThat(where).isEqualTo(" WHERE id IN (:bom_id) AND itemname = :name");
    }
}
```

- [ ] **Step 2: Jalankan test — pastikan GAGAL**

Run: `mvn -q -Dtest=ReportDataServiceFormFieldTest test`
Expected: FAIL (saat ini `FORM_FIELD` dengan operator `=` menghasilkan `id = :bom_id`).

- [ ] **Step 3: Implementasi**

Di `ReportDataService.java`, ganti method `buildModelBWhere` (baris 63-92) menjadi:

```java
    /**
     * Bangun WHERE dari parameter Model B (filterColumn + operator + nilai). Param tanpa
     * filterColumn/operator (=Model A) atau tanpa nilai dilewati. Nilai di-bind ke outBind
     * (LIKE/ILIKE dibungkus %..%). Operator & kolom divalidasi.
     *
     * <p>Parameter {@code FORM_FIELD} selalu berisi List (lihat
     * {@link ReportParamResolver#resolveFromRows}), sehingga operator tersimpan diabaikan dan
     * klausanya selalu {@code IN (:param)} — satu-satunya bentuk yang di-expand
     * NamedParameterJdbcTemplate untuk Collection. {@code = ANY(:param)} akan gagal dengan
     * "Can't infer the SQL type ... java.util.ArrayList".
     */
    public static String buildModelBWhere(List<com.vaadinerp.meta.ReportParamMeta> params,
                                          Map<String, Object> values, Map<String, Object> outBind) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (com.vaadinerp.meta.ReportParamMeta p : params) {
            String col = p.getFilterColumn();
            String opRaw = p.getOperator();
            boolean formField = "FORM_FIELD".equalsIgnoreCase(
                    p.getSource() == null ? "" : p.getSource().trim());
            // Model A (tanpa filterColumn) dilewati. Operator boleh kosong untuk FORM_FIELD
            // karena bentuk klausanya sudah pasti IN.
            if (col == null || col.isBlank()) continue;
            if (!formField && (opRaw == null || opRaw.isBlank())) continue;

            Object val = values != null ? values.get(p.getParamName()) : null;
            if (val == null
                    || (val instanceof String s && s.isBlank())
                    || (val instanceof java.util.Collection<?> c && c.isEmpty())) continue;

            DynamicDataService.validateSqlIdentifier(col, "filter column");
            String name = p.getParamName();
            sb.append(sb.length() == 0 ? " WHERE " : " AND ");
            if (formField) {
                sb.append(col).append(" IN (:").append(name).append(")");
                outBind.put(name, val);
            } else if ("IN".equalsIgnoreCase(opRaw.trim())) {
                sb.append(col).append(" = ANY(:").append(name).append(")");
                outBind.put(name, val);
            } else {
                String op = DynamicDataService.validateComparisonOperator(opRaw);
                sb.append(col).append(" ").append(op).append(" :").append(name);
                if ("LIKE".equals(op) || "ILIKE".equals(op)) {
                    String s = val.toString();
                    outBind.put(name, s.contains("%") ? s : "%" + s + "%");
                } else {
                    outBind.put(name, val);
                }
            }
        }
        return sb.toString();
    }
```

- [ ] **Step 4: Jalankan test — pastikan LULUS**

Run: `mvn -q -Dtest=ReportDataServiceFormFieldTest test`
Expected: PASS.

- [ ] **Step 5: Pastikan test lama tetap lulus**

Run: `mvn -q -Dtest=ReportDataServiceTest test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportDataService.java \
        src/test/java/com/vaadinerp/report/ReportDataServiceFormFieldTest.java
git commit -m "feat: FORM_FIELD params bind as IN (:param) so Spring expands the collection"
```

---

### Task 4: GROUP band untuk engine STANDARD

**Files:**
- Modify: `src/main/java/com/vaadinerp/report/render/ReportContext.java`
- Modify: `src/main/java/com/vaadinerp/report/render/StandardRenderer.java`
- Modify: `src/main/java/com/vaadinerp/report/ReportRunService.java:56-57`
- Test: `src/test/java/com/vaadinerp/report/render/StandardRendererGroupTest.java`

**Interfaces:**
- Consumes: `ReportElementMeta.getBandType()`, `ReportMeta.getGroupBy()`.
- Produces:
  - `ReportContext` bertambah komponen terakhir `String groupBy` → konstruktor menjadi `ReportContext(reportCode, engineType, template, data, params, pageSize, orientation, reportTitle, elements, groupBy)`.
  - `static String StandardRenderer.renderHtml(List<Map<String,Object>> data, ReportMeta report, List<ReportElementMeta> elements, String groupBy)` — overload 3-argumen lama tetap ada dan mendelegasikan dengan `groupBy = null`.
  - Band baru yang dikenali: `GROUP_HEADER`, `GROUP_FOOTER`.

- [ ] **Step 1: Tulis failing test**

Buat `src/test/java/com/vaadinerp/report/render/StandardRendererGroupTest.java`:

```java
package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StandardRendererGroupTest {

    private ReportElementMeta element(String band, String type, String value, int order) {
        ReportElementMeta el = new ReportElementMeta();
        el.setBandType(band);
        el.setElementType(type);
        el.setElementValue(value);
        el.setColOrder(order);
        return el;
    }

    private Map<String, Object> row(Object bomId, String material, Object qty) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bom_id", bomId);
        m.put("material", material);
        m.put("qty", qty);
        return m;
    }

    private List<ReportElementMeta> bomElements() {
        List<ReportElementMeta> els = new ArrayList<>();
        els.add(element("GROUP_HEADER", "LABEL", "BOM No:", 1));
        els.add(element("GROUP_HEADER", "FIELD", "bom_id", 2));
        els.add(element("COLUMN_HEADER", "LABEL", "Material", 1));
        els.add(element("COLUMN_HEADER", "LABEL", "Qty", 2));
        els.add(element("DETAIL", "FIELD", "material", 1));
        els.add(element("DETAIL", "FIELD", "qty", 2));
        els.add(element("GROUP_FOOTER", "SYSTEM", "SUM(qty)", 1));
        return els;
    }

    private final List<Map<String, Object>> twoGroups = List.of(
            row(38, "PASIR", 10),
            row(38, "RESIN", 5),
            row(42, "SLEEVE", 7));

    @Test
    void groupHeaderRenderedOncePerGroup() {
        ReportMeta r = new ReportMeta();
        r.setReportTitle("BOM");
        String html = StandardRenderer.renderHtml(twoGroups, r, bomElements(), "bom_id");

        // Dua kelompok → label group header muncul dua kali, bukan tiga (sejumlah baris).
        assertThat(html.split("BOM No:", -1).length - 1).isEqualTo(2);
    }

    @Test
    void everyDetailRowStillRendered() {
        String html = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "bom_id");
        assertThat(html).contains("PASIR").contains("RESIN").contains("SLEEVE");
    }

    @Test
    void pageBreakBetweenGroupsButNotBeforeFirst() {
        String html = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "bom_id");
        // Dua kelompok → tepat satu page break (di antara keduanya).
        assertThat(html.split("page-break-before", -1).length - 1).isEqualTo(1);
    }

    @Test
    void groupFooterAggregatesOnlyItsOwnGroup() {
        String html = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "bom_id");
        // Grup 38 → 10+5 = 15; grup 42 → 7. Total keseluruhan (22) tidak boleh muncul.
        assertThat(html).contains("15").contains("7");
        assertThat(html).doesNotContain(">22<");
    }

    @Test
    void groupOrderFollowsFirstAppearance() {
        List<Map<String, Object>> rows = List.of(row(42, "SLEEVE", 7), row(38, "PASIR", 10));
        String html = StandardRenderer.renderHtml(rows, null, bomElements(), "bom_id");
        assertThat(html.indexOf("SLEEVE")).isLessThan(html.indexOf("PASIR"));
    }

    @Test
    void nullGroupByFallsBackToUngroupedOutput() {
        String grouped = StandardRenderer.renderHtml(twoGroups, null, bomElements(), null);
        String legacy = StandardRenderer.renderHtml(twoGroups, null, bomElements());
        assertThat(grouped).isEqualTo(legacy);
        assertThat(grouped).doesNotContain("page-break-before");
    }

    @Test
    void blankGroupByTreatedAsNull() {
        String blank = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "   ");
        String legacy = StandardRenderer.renderHtml(twoGroups, null, bomElements());
        assertThat(blank).isEqualTo(legacy);
    }

    @Test
    void unknownGroupColumnProducesSingleGroup() {
        String html = StandardRenderer.renderHtml(twoGroups, null, bomElements(), "no_such_column");
        // Semua baris punya nilai group null → satu kelompok, tanpa page break.
        assertThat(html).doesNotContain("page-break-before");
        assertThat(html).contains("PASIR").contains("SLEEVE");
    }

    @Test
    void titleAndSummaryRenderedOnceOutsideGroups() {
        List<ReportElementMeta> els = new ArrayList<>(bomElements());
        els.add(element("TITLE", "LABEL", "BILL OF MATERIAL", 1));
        els.add(element("SUMMARY", "SYSTEM", "COUNT()", 1));

        String html = StandardRenderer.renderHtml(twoGroups, null, els, "bom_id");

        assertThat(html.split("BILL OF MATERIAL", -1).length - 1).isEqualTo(1);
        // SUMMARY dihitung atas seluruh data (3 baris), bukan per kelompok.
        assertThat(html).contains(">3<");
    }
}
```

- [ ] **Step 2: Jalankan test — pastikan GAGAL**

Run: `mvn -q -Dtest=StandardRendererGroupTest test`
Expected: FAIL (overload 4-argumen `renderHtml` belum ada).

- [ ] **Step 3: Tambah `groupBy` ke `ReportContext`**

Ganti isi `ReportContext.java`:

```java
package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;

import java.io.File;
import java.util.List;
import java.util.Map;

/** Konteks render satu report: template + data + parameter + info halaman + band + grouping. */
public record ReportContext(
        String reportCode,
        String engineType,
        File template,
        List<Map<String, Object>> data,
        Map<String, Object> params,
        String pageSize,
        String orientation,
        String reportTitle,
        List<ReportElementMeta> elements,
        String groupBy) {

    public ReportContext {
        elements = (elements == null) ? List.of() : List.copyOf(elements);
    }
}
```

- [ ] **Step 4: Teruskan `groupBy` dari `ReportRunService`**

Di `ReportRunService.java`, ganti pembuatan `ReportContext` (baris 56-58) menjadi:

```java
        ReportContext ctx = new ReportContext(report.getReportCode(), engine, template, data, params,
                report.getPageSize(), report.getOrientation(), report.getReportTitle(),
                report.getElements(), report.getGroupBy());
```

> Bila konstruktor lama dipanggil di tempat lain, kompilasi akan menunjukkannya; tambahkan argumen `null` di pemanggil non-report (mis. test) agar tetap kompilasi.

- [ ] **Step 5: Implementasi grouping di `StandardRenderer`**

Di `StandardRenderer.java`:

**5a.** Ganti method `renderHtml` (baris 35-49) menjadi dua overload:

```java
    /** Overload lama: tanpa grouping. */
    public static String renderHtml(List<Map<String, Object>> data, ReportMeta report,
                                    List<ReportElementMeta> elements) {
        return renderHtml(data, report, elements, null);
    }

    /**
     * Render band-based bila report punya {@code elements}: TITLE / PAGE_HEADER di atas,
     * COLUMN_HEADER + DETAIL sebagai tabel (DETAIL diulang per baris data), lalu
     * SUMMARY / PAGE_FOOTER. Tanpa elements, jatuh ke dump semua kolom seperti semula
     * supaya report lama tetap tampil.
     *
     * <p>Bila {@code groupBy} terisi, data dipecah per nilai kolom itu (urutan kemunculan
     * dipertahankan) dan tiap kelompok dirender sebagai GROUP_HEADER → tabel → GROUP_FOOTER,
     * dipisah page break. Agregat di band group dihitung atas baris kelompoknya saja.
     */
    public static String renderHtml(List<Map<String, Object>> data, ReportMeta report,
                                    List<ReportElementMeta> elements, String groupBy) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"report-standard\">");
        if (report != null && report.getReportTitle() != null) {
            sb.append("<h2>").append(esc(report.getReportTitle())).append("</h2>");
        }
        if (elements == null || elements.isEmpty()) {
            appendAllColumns(sb, data);
        } else {
            appendBands(sb, data, elements, groupBy);
        }
        sb.append("</div>");
        return sb.toString();
    }
```

**5b.** Ganti method `appendBands` (baris 68-97) menjadi:

```java
    private static void appendBands(StringBuilder sb, List<Map<String, Object>> data,
                                    List<ReportElementMeta> elements, String groupBy) {
        List<Map<String, Object>> rows = (data != null) ? data : List.of();
        Map<String, List<ReportElementMeta>> bands = groupByBand(elements);

        for (String band : LEADING_BANDS) appendFreeBand(sb, bands.get(band), rows);

        if (groupBy == null || groupBy.isBlank()) {
            appendDataTable(sb, rows, bands, rows);
        } else {
            List<List<Map<String, Object>>> groups = partitionByGroup(rows, groupBy.trim());
            boolean first = true;
            for (List<Map<String, Object>> group : groups) {
                sb.append("<div");
                if (!first) sb.append(" style=\"page-break-before:always\"");
                sb.append('>');
                appendFreeBand(sb, bands.get("GROUP_HEADER"), group);
                appendDataTable(sb, group, bands, group);
                appendFreeBand(sb, bands.get("GROUP_FOOTER"), group);
                sb.append("</div>");
                first = false;
            }
        }

        for (String band : TRAILING_BANDS) appendFreeBand(sb, bands.get(band), rows);
    }

    /** COLUMN_HEADER + DETAIL untuk sekumpulan baris. {@code aggRows} jadi cakupan fungsi agregat. */
    private static void appendDataTable(StringBuilder sb, List<Map<String, Object>> rows,
                                        Map<String, List<ReportElementMeta>> bands,
                                        List<Map<String, Object>> aggRows) {
        List<ReportElementMeta> header = bands.get("COLUMN_HEADER");
        List<ReportElementMeta> detail = bands.get("DETAIL");
        if (header == null && detail == null) return;

        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\" style=\"width:100%\">");
        if (header != null) {
            sb.append("<thead><tr>");
            for (ReportElementMeta el : header) appendCell(sb, "th", el, null, aggRows);
            sb.append("</tr></thead>");
        }
        if (detail != null) {
            sb.append("<tbody>");
            for (Map<String, Object> row : rows) {
                sb.append("<tr>");
                for (ReportElementMeta el : detail) appendCell(sb, "td", el, row, aggRows);
                sb.append("</tr>");
            }
            sb.append("</tbody>");
        }
        sb.append("</table>");
    }

    /**
     * Pecah baris per nilai {@code groupBy}, mempertahankan urutan kemunculan pertama.
     * Baris tanpa kolom itu masuk ke satu kelompok bernilai null, sehingga kolom group
     * yang salah ketik menghasilkan satu kelompok — bukan satu kelompok per baris.
     */
    private static List<List<Map<String, Object>>> partitionByGroup(List<Map<String, Object>> rows,
                                                                    String groupBy) {
        Map<Object, List<Map<String, Object>>> byKey = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object key = (row == null) ? null : row.get(groupBy);
            byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        return new ArrayList<>(byKey.values());
    }
```

> `appendFreeBand` sudah memakai `rows.get(0)` sebagai baris sumber nilai FIELD dan `rows` sebagai cakupan agregat, sehingga memanggilnya dengan daftar baris satu kelompok otomatis membuat GROUP_HEADER/GROUP_FOOTER memakai data kelompok itu. Tidak ada perubahan pada method tersebut.

**5c.** Perbarui `render(ReportContext)` (baris 222-229) agar meneruskan `groupBy`:

```java
    @Override
    public ReportOutput render(ReportContext ctx) {
        ReportMeta head = null;
        if (ctx.reportTitle() != null) {
            head = new ReportMeta();
            head.setReportTitle(ctx.reportTitle());
        }
        return ReportOutput.html(renderHtml(ctx.data(), head, ctx.elements(), ctx.groupBy()));
    }
```

- [ ] **Step 6: Jalankan test — pastikan LULUS**

Run: `mvn -q -Dtest=StandardRendererGroupTest test`
Expected: PASS.

- [ ] **Step 7: Pastikan test renderer lama tetap lulus**

Run: `mvn -q -Dtest=StandardRendererTest test`
Expected: PASS (jalur tanpa `groupBy` tidak berubah).

- [ ] **Step 8: Kompilasi penuh + seluruh test**

Run: `mvn -q test`
Expected: BUILD SUCCESS, semua test PASS.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/vaadinerp/report/render/ReportContext.java \
        src/main/java/com/vaadinerp/report/render/StandardRenderer.java \
        src/main/java/com/vaadinerp/report/ReportRunService.java \
        src/test/java/com/vaadinerp/report/render/StandardRendererGroupTest.java
git commit -m "feat: GROUP_HEADER/GROUP_FOOTER bands with per-group aggregates for STANDARD engine"
```

---

### Task 5: Report Designer & Builder — field `Usage`, `Group By`, band GROUP

**Files:**
- Modify: `src/main/java/com/vaadinerp/views/ReportDesignerView.java`
- Modify: `src/main/java/com/vaadinerp/views/ReportBuilderView.java:525-531`

**Interfaces:**
- Consumes: `ReportMeta.getUsageScope/setUsageScope`, `getGroupBy/setGroupBy` (Task 1).
- Produces: Report Designer menyimpan kedua kolom; Report Builder menampilkan band `GROUP_HEADER` dan `GROUP_FOOTER` di kanvas.

- [ ] **Step 1: Tambah kontrol ke form metadata `ReportDesignerView`**

Di deklarasi field (dekat baris 68), tambahkan:

```java
    private final Select<String> usageScopeSelect = new Select<>();
    private final TextField groupByField = new TextField("Group By (STANDARD engine)");
```

Di konstanta (dekat baris 82), tambahkan:

```java
    private static final List<String> USAGE_SCOPES = List.of("RUNNER", "FORM", "BOTH");
```

Pada method yang membangun form metadata (dekat baris 268-310, tempat `sourceCombo` disiapkan), tambahkan sebelum `FormLayout meta = new FormLayout(...)`:

```java
        usageScopeSelect.setLabel("Usage");
        usageScopeSelect.setItems(USAGE_SCOPES);
        usageScopeSelect.setValue("RUNNER");
        usageScopeSelect.setHelperText(
                "RUNNER: Report Runner only. FORM: form Print button only. BOTH: available in both.");

        groupByField.setPlaceholder("e.g. bom_id");
        groupByField.setHelperText(
                "Result column that starts a new document per value. STANDARD engine only — "
                        + "JASPER and STIMULSOFT define grouping inside their own template.");
```

Lalu masukkan keduanya ke `FormLayout`:

```java
        FormLayout meta = new FormLayout(codeField, titleField, categoryCombo, sourceCombo,
                usageScopeSelect, groupByField, queryArea, ...);
```

> Pertahankan sisa argumen `FormLayout` yang sudah ada persis seperti semula — hanya sisipkan dua kontrol baru setelah `sourceCombo`.

- [ ] **Step 2: Tambah teks bantuan pada area query**

Tepat setelah `queryArea` dikonfigurasi, tambahkan:

```java
        queryArea.setHelperText(
                "Overrides Source Table. Use IN (:param) — not = :param — for parameters sourced "
                        + "from form rows, because they always arrive as a list.");
```

- [ ] **Step 3: Muat nilai saat report dibuka**

Pada method yang mengisi form dari `ReportMeta` (dekat baris 482-500, tempat `sourceCombo.setValue(...)` dipanggil), tambahkan:

```java
            usageScopeSelect.setValue(
                    report.getUsageScope() == null || report.getUsageScope().isBlank()
                            ? "RUNNER" : report.getUsageScope().trim().toUpperCase());
            groupByField.setValue(report.getGroupBy() == null ? "" : report.getGroupBy());
```

Dan pada cabang "report baru" (dekat baris 482, tempat `sourceCombo.clear()`):

```java
            usageScopeSelect.setValue("RUNNER");
            groupByField.clear();
```

- [ ] **Step 4: Simpan nilai saat Save**

Pada method save (dekat baris 561-570, tempat `rep.setTableName(...)`), tambahkan:

```java
        rep.setUsageScope(usageScopeSelect.getValue() != null ? usageScopeSelect.getValue() : "RUNNER");
        rep.setGroupBy(groupByField.getValue() == null || groupByField.getValue().isBlank()
                ? null : groupByField.getValue().trim());
```

- [ ] **Step 5: Tambah kolom `Usage` ke grid daftar report**

Setelah kolom `colSource` didefinisikan (dekat baris 168), tambahkan:

```java
        Grid.Column<ReportMeta> colUsage = grid.addColumn(
                        r -> r.getUsageScope() == null ? "RUNNER" : r.getUsageScope())
                .setHeader("Usage").setAutoWidth(true);
```

Dan daftarkan getter-nya bersama kolom lain (dekat baris 182):

```java
        colGetters.put(colUsage, r -> nz(r.getUsageScope() == null ? "RUNNER" : r.getUsageScope()));
```

- [ ] **Step 6: Tambah band GROUP ke kanvas `ReportBuilderView`**

Di `ReportBuilderView.java`, sisipkan dua baris di antara `COLUMN_HEADER` dan `DETAIL` (baris 528-529):

```java
        pageCanvas.add(buildBandLayout("GROUP_HEADER", "[GROUP HEADER BAND] - Printed once per group value (see report Group By)"));
```

dan setelah `DETAIL`:

```java
        pageCanvas.add(buildBandLayout("GROUP_FOOTER", "[GROUP FOOTER BAND] - Printed after each group; aggregates cover that group only"));
```

Perbarui juga komentar tipe band di baris 69:

```java
        String bandType; // TITLE, PAGE_HEADER, GROUP_HEADER, COLUMN_HEADER, DETAIL, GROUP_FOOTER, PAGE_FOOTER, SUMMARY
```

- [ ] **Step 7: Kompilasi**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 8: Verifikasi manual**

1. Restart aplikasi.
2. Buka Report Designer → pilih report mana pun → tab Editor.
3. Konfirmasi field **Usage** (default `RUNNER`) dan **Group By** muncul, beserta helper text-nya.
4. Ubah Usage menjadi `BOTH`, isi Group By dengan `bom_id`, tekan Save.
5. Verifikasi tersimpan:

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -d grp -c \
  "SELECT report_code, usage_scope, group_by FROM public.meta_report WHERE usage_scope <> 'RUNNER';"
```

6. Buka Report Builder → konfirmasi band **GROUP HEADER** dan **GROUP FOOTER** muncul di kanvas dan bisa menerima elemen.
7. Konfirmasi kolom **Usage** tampil di grid daftar report.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/vaadinerp/views/ReportDesignerView.java \
        src/main/java/com/vaadinerp/views/ReportBuilderView.java
git commit -m "feat: Usage and Group By fields in Report Designer, GROUP bands in Report Builder"
```

---

### Task 6: `ReportLauncher` — ekstrak jalur output, pakai di Runner

**Files:**
- Create: `src/main/java/com/vaadinerp/report/ReportLauncher.java`
- Modify: `src/main/java/com/vaadinerp/views/ReportRunnerView.java`
- Test: `src/test/java/com/vaadinerp/report/ReportLauncherTest.java`

**Interfaces:**
- Consumes: `ReportRunResult`, `ReportOutput`, `ReportRunService`, `PortalView.openComponentTab(String,String,Component)`.
- Produces:
  - `static Component ReportLauncher.buildOutput(ReportRunResult res)` — IFrame viewer untuk Stimulsoft; `StreamResource` di IFrame untuk PDF/HTML.
  - `static String ReportLauncher.outputFilename(String contentType)` — `report.pdf` / `report.html` / `report.bin`.
  - `static PortalView ReportLauncher.findPortal(Component origin)` — telusuri parent, lalu anak-anak `UI`; `null` bila tidak ketemu.
  - `static void ReportLauncher.runAndOpenTab(Component origin, ReportRunService svc, ReportMeta report, Map<String,Object> values, Runnable onFinish)` — jalankan di executor bersama, buka tab lewat `UI.access`, `onFinish` selalu dipanggil di thread UI (sukses maupun gagal).

- [ ] **Step 1: Tulis failing test**

Buat `src/test/java/com/vaadinerp/report/ReportLauncherTest.java`:

```java
package com.vaadinerp.report;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportLauncherTest {

    @Test
    void pdfContentTypeMapsToPdfFilename() {
        assertThat(ReportLauncher.outputFilename("application/pdf")).isEqualTo("report.pdf");
    }

    @Test
    void htmlContentTypeMapsToHtmlFilename() {
        assertThat(ReportLauncher.outputFilename("text/html;charset=UTF-8")).isEqualTo("report.html");
    }

    @Test
    void unknownContentTypeMapsToBin() {
        assertThat(ReportLauncher.outputFilename("application/octet-stream")).isEqualTo("report.bin");
    }

    @Test
    void nullContentTypeMapsToBin() {
        assertThat(ReportLauncher.outputFilename(null)).isEqualTo("report.bin");
    }

    @Test
    void tabIdIsPrefixedReportCode() {
        assertThat(ReportLauncher.tabId("RPT_BOM_DOC_STD")).isEqualTo("RPT_OUT_RPT_BOM_DOC_STD");
    }
}
```

- [ ] **Step 2: Jalankan test — pastikan GAGAL**

Run: `mvn -q -Dtest=ReportLauncherTest test`
Expected: FAIL (`ReportLauncher` belum ada).

- [ ] **Step 3: Buat `ReportLauncher`**

```java
package com.vaadinerp.report;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.report.render.ReportOutput;
import com.vaadinerp.views.PortalView;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Jalur bersama untuk menjalankan report dan menampilkan hasilnya sebagai tab aplikasi.
 * Dipakai Report Runner (report standalone) dan tombol Print di GenericFormView, supaya
 * keduanya punya perilaku output, penanganan error, dan eksekusi async yang identik.
 */
public final class ReportLauncher {

    private ReportLauncher() {}

    private static final ExecutorService RUN_EXEC = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "report-run");
        t.setDaemon(true);
        return t;
    });

    /** Nama berkas yang dilihat browser saat membuka output. */
    public static String outputFilename(String contentType) {
        if (contentType == null) return "report.bin";
        if (contentType.contains("pdf")) return "report.pdf";
        if (contentType.startsWith("text/html")) return "report.html";
        return "report.bin";
    }

    /** Id tab portal untuk sebuah report; sama untuk pemanggil mana pun agar tab tidak berganda. */
    public static String tabId(String reportCode) {
        return "RPT_OUT_" + reportCode;
    }

    /** Bangun komponen output: viewer Stimulsoft, atau berkas hasil render di IFrame. */
    public static Component buildOutput(ReportRunResult res) {
        VerticalLayout box = new VerticalLayout();
        box.setSizeFull();
        box.setPadding(false);
        box.setSpacing(false);
        if (res.stimulsoftViewer()) {
            IFrame ifr = new IFrame(res.viewerUrl());
            ifr.setSizeFull();
            ifr.getStyle().set("border", "none");
            box.add(ifr);
            box.setFlexGrow(1, ifr);
            return box;
        }
        // StreamResource menghindari pengiriman byte lewat WebSocket — browser mengambil via HTTP.
        ReportOutput out = res.output();
        byte[] bytes = out.bytes();
        StreamResource sr = new StreamResource(
                outputFilename(out.contentType()), () -> new ByteArrayInputStream(bytes));
        sr.setContentType(out.contentType());
        IFrame ifr = new IFrame();
        ifr.getElement().setAttribute("src", sr);
        ifr.setSizeFull();
        ifr.getStyle().set("border", "none");
        box.add(ifr);
        box.setFlexGrow(1, ifr);
        return box;
    }

    /** Cari PortalView pembungkus: naik lewat parent, lalu telusuri anak UI. */
    public static PortalView findPortal(Component origin) {
        Component c = origin;
        while (c != null) {
            if (c instanceof PortalView pv) return pv;
            c = c.getParent().orElse(null);
        }
        UI ui = UI.getCurrent();
        if (ui != null) {
            for (Component child : ui.getChildren().toList()) {
                if (child instanceof PortalView pv) return pv;
            }
        }
        return null;
    }

    /**
     * Jalankan report di thread latar lalu buka hasilnya sebagai tab. {@code onFinish}
     * dijalankan di thread UI setelah sukses maupun gagal, untuk memulihkan panel pemanggil.
     */
    public static void runAndOpenTab(Component origin, ReportRunService svc, ReportMeta report,
                                     Map<String, Object> values, Runnable onFinish) {
        UI ui = UI.getCurrent();
        String title = report.getReportTitle() != null ? report.getReportTitle() : report.getReportCode();
        RUN_EXEC.submit(() -> {
            try {
                ReportRunResult res = svc.run(report, values, false);
                ui.access(() -> {
                    PortalView portal = findPortal(origin);
                    if (portal == null) {
                        Notification.show("Cannot find app shell to open output tab.");
                    } else {
                        portal.openComponentTab(tabId(report.getReportCode()), title, buildOutput(res));
                    }
                    if (onFinish != null) onFinish.run();
                });
            } catch (org.springframework.dao.QueryTimeoutException te) {
                ui.access(() -> {
                    Notification.show("The report query took too long and was stopped. "
                            + "Please narrow your filter/parameters.");
                    if (onFinish != null) onFinish.run();
                });
            } catch (Exception ex) {
                ui.access(() -> {
                    Notification.show("Failed to run report: "
                            + (ex.getMessage() != null ? ex.getMessage() : ex.toString()));
                    if (onFinish != null) onFinish.run();
                });
            }
        });
    }
}
```

- [ ] **Step 4: Jalankan test — pastikan LULUS**

Run: `mvn -q -Dtest=ReportLauncherTest test`
Expected: PASS.

- [ ] **Step 5: Pakai `ReportLauncher` di `ReportRunnerView`**

**5a.** Hapus konstanta `RUN_EXEC` (baris 38-42) — sekarang milik `ReportLauncher`.

**5b.** Hapus method `buildOutput` (baris 220-248) dan `findPortal` (baris 268 sampai akhir methodnya).

**5c.** Ganti bagian eksekusi di `runReport` (baris 191-217) menjadi:

```java
        ReportLauncher.runAndOpenTab(this, reportRunService, report, values, () -> selectReport(report));
```

Sehingga akhir `runReport` menjadi:

```java
        // Show loading state while report runs off the UI thread
        selectionPanel.removeAll();
        com.vaadin.flow.component.progressbar.ProgressBar pb = new com.vaadin.flow.component.progressbar.ProgressBar();
        pb.setIndeterminate(true);
        selectionPanel.add(new H4("Running report…"), pb);

        ReportLauncher.runAndOpenTab(this, reportRunService, report, values, () -> selectReport(report));
    }
```

**5d.** Bersihkan import yang tidak lagi terpakai: `java.io.ByteArrayInputStream`, `java.util.concurrent.ExecutorService`, `java.util.concurrent.Executors`, `com.vaadinerp.report.render.ReportOutput`, dan `com.vaadin.flow.component.html.IFrame` (bila tidak dipakai lagi di berkas ini). `com.vaadinerp.report.*` sudah mencakup `ReportLauncher`.

- [ ] **Step 6: Filter `usage_scope` di katalog Runner**

Pada method yang menyusun katalog (`rebuildCatalog`, dekat baris 120-130), tambahkan filter setelah `reportAccessService.accessibleReports(...)`:

```java
        // Report khusus form (param FORM_FIELD tak bisa terisi tanpa baris grid) disembunyikan
        // di sini — menjalankannya tanpa filter akan menarik seluruh tabel.
        List<ReportMeta> visible = accessible.stream()
                .filter(r -> r.isUsableFrom("RUNNER"))
                .collect(Collectors.toList());
```

Lalu pakai `visible` sebagai sumber pengelompokan kategori, menggantikan daftar sebelumnya.

> Nama variabel `accessible` di atas mengikuti variabel hasil `accessibleReports(...)` yang sudah ada di method tersebut; sesuaikan bila namanya berbeda.

- [ ] **Step 7: Kompilasi + seluruh test**

Run: `mvn -q test`
Expected: BUILD SUCCESS, semua test PASS.

- [ ] **Step 8: Verifikasi manual**

1. Restart aplikasi, buka **Run Report**.
2. Jalankan report `RPT_SALESLINE_EXP3RD` → output tetap terbuka sebagai tab seperti sebelumnya.
3. Ubah salah satu report menjadi `usage_scope = 'FORM'` lewat Designer → refresh Runner → report itu **hilang** dari katalog.
4. Kembalikan ke `RUNNER` → report muncul lagi.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportLauncher.java \
        src/main/java/com/vaadinerp/views/ReportRunnerView.java \
        src/test/java/com/vaadinerp/report/ReportLauncherTest.java
git commit -m "refactor: extract ReportLauncher and filter Runner catalog by usage_scope"
```

---

### Task 7: Stimulsoft — parameter multi-nilai

**Files:**
- Modify: `src/main/java/com/vaadinerp/report/ReportRunService.java:37-50`
- Modify: `src/main/java/com/vaadinerp/controller/StimulsoftJavaController.java:77-80`
- Test: `src/test/java/com/vaadinerp/report/ReportRunServiceViewerUrlTest.java`

**Interfaces:**
- Consumes: `ReportMeta.getReportCode()`, `getEngineType()`.
- Produces: `ReportRunService.run(...)` untuk engine STIMULSOFT menghasilkan `viewerUrl` yang mengulang key untuk tiap elemen `Collection` (`&bom_id=38&bom_id=42`), tiap nilai di-encode tersendiri.

- [ ] **Step 1: Tulis failing test**

Buat `src/test/java/com/vaadinerp/report/ReportRunServiceViewerUrlTest.java`:

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.report.render.ReportRendererRegistry;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReportRunServiceViewerUrlTest {

    private ReportRunService service() {
        return new ReportRunService(mock(ReportResolver.class), mock(ReportDataService.class),
                mock(ReportRendererRegistry.class));
    }

    private ReportMeta stimulsoftReport() {
        ReportMeta r = new ReportMeta();
        r.setReportCode("RPT_BOM_DOC_STI");
        r.setEngineType("STIMULSOFT");
        return r;
    }

    @Test
    void listParameterRepeatsTheKey() {
        ReportRunResult res = service().run(stimulsoftReport(),
                Map.of("bom_id", List.of(38, 42)), false);

        assertThat(res.stimulsoftViewer()).isTrue();
        assertThat(res.viewerUrl()).contains("bom_id=38").contains("bom_id=42");
        // Bentuk toString sebuah List tidak boleh bocor ke URL.
        assertThat(res.viewerUrl()).doesNotContain("[").doesNotContain("%5B");
    }

    @Test
    void scalarParameterUnchanged() {
        ReportRunResult res = service().run(stimulsoftReport(), Map.of("id", 7), false);
        assertThat(res.viewerUrl()).contains("id=7");
    }

    @Test
    void nullValuesSkipped() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", null);
        ReportRunResult res = service().run(stimulsoftReport(), params, false);
        assertThat(res.viewerUrl()).doesNotContain("id=");
    }

    @Test
    void emptyListProducesNoParameter() {
        ReportRunResult res = service().run(stimulsoftReport(),
                Map.of("bom_id", List.of()), false);
        assertThat(res.viewerUrl()).doesNotContain("bom_id");
    }

    @Test
    void valuesAreUrlEncoded() {
        ReportRunResult res = service().run(stimulsoftReport(),
                Map.of("name", List.of("a b", "c&d")), false);
        assertThat(res.viewerUrl()).contains("name=a+b").contains("name=c%26d");
    }

    @Test
    void codeAlwaysPresent() {
        ReportRunResult res = service().run(stimulsoftReport(), Map.of(), false);
        assertThat(res.viewerUrl()).startsWith("/stimulsoft-java/viewer?code=RPT_BOM_DOC_STI");
    }
}
```

- [ ] **Step 2: Jalankan test — pastikan GAGAL**

Run: `mvn -q -Dtest=ReportRunServiceViewerUrlTest test`
Expected: FAIL (`bom_id=[38, 42]` ter-encode sebagai satu nilai).

- [ ] **Step 3: Perbaiki penyusunan URL di `ReportRunService`**

Ganti blok STIMULSOFT (baris 37-50) menjadi:

```java
        if ("STIMULSOFT".equalsIgnoreCase(engine)) {
            StringBuilder url = new StringBuilder("/stimulsoft-java/viewer?code=").append(report.getReportCode());
            if (params != null) {
                for (Map.Entry<String, Object> e : params.entrySet()) {
                    Object v = e.getValue();
                    if (v == null) continue;
                    // Parameter FORM_FIELD berisi List: ulangi key untuk tiap nilai, karena
                    // toString sebuah List ("[38, 42]") bukan parameter query yang valid.
                    if (v instanceof java.util.Collection<?> c) {
                        for (Object item : c) {
                            if (item != null) appendParam(url, e.getKey(), item);
                        }
                    } else {
                        appendParam(url, e.getKey(), v);
                    }
                }
            }
            return ReportRunResult.stimulsoft(url.toString());
        }
```

Tambahkan helper privat di kelas yang sama (setelah method `run`):

```java
    private static void appendParam(StringBuilder url, String key, Object value) {
        url.append("&")
           .append(java.net.URLEncoder.encode(key, java.nio.charset.StandardCharsets.UTF_8))
           .append("=")
           .append(java.net.URLEncoder.encode(value.toString(), java.nio.charset.StandardCharsets.UTF_8));
    }
```

- [ ] **Step 4: Jalankan test — pastikan LULUS**

Run: `mvn -q -Dtest=ReportRunServiceViewerUrlTest test`
Expected: PASS.

- [ ] **Step 5: Terima parameter berulang di `StimulsoftJavaController`**

Ganti blok pembacaan parameter (baris 77-80) menjadi:

```java
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            request.getParameterMap().forEach((k, v) -> {
                if ("code".equals(k) || v == null || v.length == 0) return;
                // Satu key bisa muncul beberapa kali (baris grid terpilih); ambil semuanya,
                // supaya ReportDataService bisa membangun IN (:param).
                params.put(k, v.length == 1 ? v[0] : java.util.List.of((Object[]) v));
            });
```

- [ ] **Step 6: Kompilasi + seluruh test**

Run: `mvn -q test`
Expected: BUILD SUCCESS, semua test PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportRunService.java \
        src/main/java/com/vaadinerp/controller/StimulsoftJavaController.java \
        src/test/java/com/vaadinerp/report/ReportRunServiceViewerUrlTest.java
git commit -m "fix: carry multi-valued report params through the Stimulsoft viewer URL"
```

---

### Task 8: `GenericFormView.btnPrint` — cetak baris terpilih

**Files:**
- Modify: `src/main/java/com/vaadinerp/views/GenericFormView.java:606-660`
- Delete: `src/main/java/com/vaadinerp/controller/ReportEngineController.java`

**Interfaces:**
- Consumes: `FormMeta.reportSourceKey()` (Task 1), `ReportMeta.isUsableFrom` (Task 1), `ReportParamResolver.resolveFromRows` (Task 2), `ReportLauncher.runAndOpenTab` (Task 6), `ReportAccessService.canAccess`, `ReportParameterForm`, `ReportParamResolver.userInputParams`.
- Produces: tombol Cetak yang menjalankan report atas baris terpilih.

- [ ] **Step 1: Ganti listener tombol Cetak**

Ganti seluruh `btnPrint.addClickListener(...)` (baris 613-660) menjadi:

```java
        btnPrint.addClickListener(e -> openPrintDialog());
```

- [ ] **Step 2: Tambah method pendukung**

Sisipkan method berikut ke `GenericFormView` (letakkan setelah method `refreshExtraToolbarButtons`, dekat baris 940):

```java
    /** Report yang boleh dicetak dari form ini, untuk baris yang sedang tercentang. */
    private void openPrintDialog() {
        if (currentFormDef == null) {
            Notification.show("Form definition is not loaded yet.", 3000, Notification.Position.MIDDLE);
            return;
        }
        String sourceKey = currentFormDef.reportSourceKey();
        if (sourceKey == null) {
            Notification.show("This form has no table or view to report on.", 3000, Notification.Position.MIDDLE);
            return;
        }

        java.util.List<com.vaadinerp.meta.ReportMeta> available;
        try {
            com.vaadinerp.meta.ReportMetaRepository reportRepo = com.vaadinerp.config.SpringContextHolder
                    .getBean(com.vaadinerp.meta.ReportMetaRepository.class);
            com.vaadinerp.report.ReportAccessService access = com.vaadinerp.config.SpringContextHolder
                    .getBean(com.vaadinerp.report.ReportAccessService.class);
            available = reportRepo.findAll().stream()
                    .filter(r -> sourceKey.equalsIgnoreCase(r.getTableName()))
                    .filter(r -> r.isUsableFrom("FORM"))
                    .filter(access::canAccess)
                    .toList();
        } catch (Exception ex) {
            Notification.show("Failed to load the report list.", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (available.isEmpty()) {
            Notification.show("No report is configured for this form.", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (available.size() == 1) {
            preparePrint(available.get(0));
            return;
        }

        com.vaadin.flow.component.dialog.Dialog chooser = new com.vaadin.flow.component.dialog.Dialog();
        chooser.setHeaderTitle("Select Report");
        VerticalLayout list = new VerticalLayout();
        for (com.vaadinerp.meta.ReportMeta rep : available) {
            String engine = rep.getEngineType() != null ? rep.getEngineType() : "STANDARD";
            com.vaadinerp.components.SafeButton pick = new com.vaadinerp.components.SafeButton(
                    (rep.getReportTitle() != null ? rep.getReportTitle() : rep.getReportCode())
                            + " (" + engine + ")",
                    ev -> {
                        chooser.close();
                        preparePrint(rep);
                    });
            pick.setWidthFull();
            list.add(pick);
        }
        chooser.add(list);
        chooser.open();
    }

    /**
     * Kumpulkan nilai parameter dari baris tercentang, tanyakan parameter USER_INPUT bila ada,
     * lalu jalankan. Parameter FORM_FIELD dan SYSTEM tidak ditampilkan — keduanya terisi sendiri.
     */
    private void preparePrint(com.vaadinerp.meta.ReportMeta report) {
        java.util.List<Map<String, Object>> rows = new ArrayList<>();
        if (grid != null && grid.getSelectedItems() != null) rows.addAll(grid.getSelectedItems());

        boolean needsRows = report.getParams() != null && report.getParams().stream()
                .anyMatch(p -> "FORM_FIELD".equalsIgnoreCase(p.getSource() == null ? "" : p.getSource().trim())
                        && p.isRequired());
        if (needsRows && rows.isEmpty()) {
            Notification.show("Please select at least one row.", 3000, Notification.Position.MIDDLE);
            return;
        }

        String user = null;
        try {
            com.vaadinerp.security.service.SessionSecurityService sec = com.vaadinerp.config.SpringContextHolder
                    .getBean(com.vaadinerp.security.service.SessionSecurityService.class);
            if (sec.getCurrentUser() != null) user = sec.getCurrentUser().getUsername();
        } catch (Exception ignored) {
            // tanpa user: parameter SYSTEM $CURRENT_USER tetap null, bukan alasan membatalkan cetak
        }

        Map<String, Object> values = new HashMap<>(
                com.vaadinerp.report.ReportParamResolver.resolveFromRows(report.getParams(), rows, user));

        java.util.List<com.vaadinerp.meta.ReportParamMeta> asked =
                com.vaadinerp.report.ReportParamResolver.userInputParams(report.getParams());
        if (asked.isEmpty()) {
            launchPrint(report, values);
            return;
        }

        com.vaadin.flow.component.dialog.Dialog paramDialog = new com.vaadin.flow.component.dialog.Dialog();
        paramDialog.setHeaderTitle("Report Parameters");
        com.vaadinerp.components.ReportParameterForm form =
                new com.vaadinerp.components.ReportParameterForm(report.getParams(), dynamicDataService);
        com.vaadinerp.components.SafeButton run = new com.vaadinerp.components.SafeButton("Print", ev -> {
            values.putAll(form.collectValues());
            if (!validateRequired(report, values)) return;
            paramDialog.close();
            launchPrint(report, values);
        });
        run.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        com.vaadinerp.components.SafeButton cancel =
                new com.vaadinerp.components.SafeButton("Cancel", ev -> paramDialog.close());
        paramDialog.add(form);
        paramDialog.getFooter().add(cancel, run);
        paramDialog.open();
    }

    /** Isi default untuk parameter kosong, lalu pastikan yang required benar-benar terisi. */
    private boolean validateRequired(com.vaadinerp.meta.ReportMeta report, Map<String, Object> values) {
        if (report.getParams() == null) return true;
        for (com.vaadinerp.meta.ReportParamMeta p : report.getParams()) {
            values.putIfAbsent(p.getParamName(), p.getDefaultValue());
            if (!p.isRequired()) continue;
            Object v = values.get(p.getParamName());
            boolean empty = v == null
                    || (v instanceof String s && s.isBlank())
                    || (v instanceof java.util.Collection<?> c && c.isEmpty());
            if (empty) {
                Notification.show("Parameter '"
                        + (p.getParamLabel() != null ? p.getParamLabel() : p.getParamName())
                        + "' is required.", 3000, Notification.Position.MIDDLE);
                return false;
            }
        }
        return true;
    }

    private void launchPrint(com.vaadinerp.meta.ReportMeta report, Map<String, Object> values) {
        if (!validateRequired(report, values)) return;
        try {
            com.vaadinerp.report.ReportRunService runService = com.vaadinerp.config.SpringContextHolder
                    .getBean(com.vaadinerp.report.ReportRunService.class);
            com.vaadinerp.report.ReportLauncher.runAndOpenTab(this, runService, report, values, null);
        } catch (Exception ex) {
            Notification.show("Failed to start the report: "
                    + (ex.getMessage() != null ? ex.getMessage() : ex.toString()),
                    4000, Notification.Position.MIDDLE);
        }
    }
```

- [ ] **Step 3: Hapus controller yang tidak terpakai**

`ReportEngineController` hanya dipakai jalur cetak lama, dan cabang JASPER-nya mengembalikan HTML placeholder yang tidak me-render apa pun.

```bash
git rm src/main/java/com/vaadinerp/controller/ReportEngineController.java
```

Pastikan tidak ada rujukan tersisa:

```bash
grep -rn "api/report/engine\|ReportEngineController" src/main --include=*.java
```

Expected: tidak ada hasil.

- [ ] **Step 4: Kompilasi**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Kompilasi + seluruh test**

Run: `mvn -q test`
Expected: BUILD SUCCESS, semua test PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vaadinerp/views/GenericFormView.java
git commit -m "feat: form Print button runs reports over selected grid rows; drop dead ReportEngineController"
```

---

### Task 9: Report contoh — tiga engine di atas form `BOM_ALL`

**Files:**
- Create: `src/main/resources/report-templates/RPT_BOM_DOC_JSP.jrxml` (sumber, ter-commit)
- Copy to: `uploads/report_templates/master/RPT_BOM_DOC_JSP.jrxml` (runtime, **di-gitignore**)
- Modify: `src/main/resources/db-migration.sql`

> `uploads/` ada di `.gitignore:25` karena berisi berkas unggahan runtime. Template contoh adalah
> artefak sumber yang harus ikut versi, jadi master copy-nya disimpan di `src/main/resources/` dan
> disalin ke `uploads/` sebagai langkah pemasangan — sama seperti langkah deploy di server lain.

**Interfaces:**
- Consumes: kolom `usage_scope` / `group_by` (Task 1), band GROUP (Task 4).
- Produces: report `RPT_BOM_DOC_STD`, `RPT_BOM_DOC_JSP`, `RPT_BOM_DOC_STI` — `table_name = 'mhbom'`, `usage_scope = 'FORM'`, satu parameter `bom_id` (`FORM_FIELD` / `sourceKey = id` / required), `data_query` identik.

- [ ] **Step 1: Buat template Jasper**

Buat `src/main/resources/report-templates/RPT_BOM_DOC_JSP.jrxml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
              name="RPT_BOM_DOC_JSP" pageWidth="595" pageHeight="842" columnWidth="555"
              leftMargin="20" rightMargin="20" topMargin="20" bottomMargin="20">
    <field name="bom_id" class="java.lang.Object"/>
    <field name="idno" class="java.lang.String"/>
    <field name="product" class="java.lang.String"/>
    <field name="drawing" class="java.lang.String"/>
    <field name="netweight" class="java.lang.String"/>
    <field name="material" class="java.lang.String"/>
    <field name="itemgroup" class="java.lang.String"/>
    <field name="qty" class="java.math.BigDecimal"/>
    <field name="perseries" class="java.math.BigDecimal"/>

    <variable name="groupItemCount" class="java.lang.Integer" resetType="Group" resetGroup="BomGroup" calculation="Count">
        <variableExpression><![CDATA[$F{material}]]></variableExpression>
    </variable>

    <group name="BomGroup" isStartNewPage="true">
        <groupExpression><![CDATA[$F{bom_id}]]></groupExpression>
        <groupHeader>
            <band height="86">
                <staticText>
                    <reportElement x="0" y="0" width="330" height="22"/>
                    <textElement><font size="14" isBold="true"/></textElement>
                    <text><![CDATA[BILL OF MATERIAL]]></text>
                </staticText>
                <staticText>
                    <reportElement x="380" y="4" width="40" height="16"/>
                    <textElement><font isBold="true"/></textElement>
                    <text><![CDATA[No :]]></text>
                </staticText>
                <textField isBlankWhenNull="true">
                    <reportElement x="420" y="4" width="135" height="16"/>
                    <textFieldExpression><![CDATA[$F{idno}]]></textFieldExpression>
                </textField>

                <staticText>
                    <reportElement x="0" y="28" width="60" height="16"/>
                    <text><![CDATA[Product]]></text>
                </staticText>
                <textField isBlankWhenNull="true">
                    <reportElement x="60" y="28" width="495" height="16"/>
                    <textFieldExpression><![CDATA[": " + ($F{product} == null ? "" : $F{product})]]></textFieldExpression>
                </textField>

                <staticText>
                    <reportElement x="0" y="46" width="60" height="16"/>
                    <text><![CDATA[Drawing]]></text>
                </staticText>
                <textField isBlankWhenNull="true">
                    <reportElement x="60" y="46" width="220" height="16"/>
                    <textFieldExpression><![CDATA[": " + ($F{drawing} == null ? "-" : $F{drawing})]]></textFieldExpression>
                </textField>

                <staticText>
                    <reportElement x="300" y="46" width="80" height="16"/>
                    <text><![CDATA[Net Weight]]></text>
                </staticText>
                <textField isBlankWhenNull="true">
                    <reportElement x="380" y="46" width="175" height="16"/>
                    <textFieldExpression><![CDATA[": " + ($F{netweight} == null ? "-" : $F{netweight})]]></textFieldExpression>
                </textField>

                <line>
                    <reportElement x="0" y="68" width="555" height="1"/>
                </line>
                <staticText>
                    <reportElement x="0" y="70" width="255" height="16"/>
                    <textElement><font isBold="true"/></textElement>
                    <text><![CDATA[Material]]></text>
                </staticText>
                <staticText>
                    <reportElement x="255" y="70" width="160" height="16"/>
                    <textElement><font isBold="true"/></textElement>
                    <text><![CDATA[Group]]></text>
                </staticText>
                <staticText>
                    <reportElement x="415" y="70" width="80" height="16"/>
                    <textElement textAlignment="Right"><font isBold="true"/></textElement>
                    <text><![CDATA[Qty]]></text>
                </staticText>
                <staticText>
                    <reportElement x="495" y="70" width="60" height="16"/>
                    <textElement textAlignment="Right"><font isBold="true"/></textElement>
                    <text><![CDATA[/Series]]></text>
                </staticText>
            </band>
        </groupHeader>
        <groupFooter>
            <band height="30">
                <line>
                    <reportElement x="0" y="2" width="555" height="1"/>
                </line>
                <staticText>
                    <reportElement x="300" y="6" width="115" height="16"/>
                    <textElement textAlignment="Right"><font isBold="true"/></textElement>
                    <text><![CDATA[Total item :]]></text>
                </staticText>
                <textField>
                    <reportElement x="415" y="6" width="80" height="16"/>
                    <textElement textAlignment="Right"><font isBold="true"/></textElement>
                    <textFieldExpression><![CDATA[$V{groupItemCount}]]></textFieldExpression>
                </textField>
            </band>
        </groupFooter>
    </group>

    <detail>
        <band height="18">
            <textField isBlankWhenNull="true">
                <reportElement x="0" y="0" width="255" height="16"/>
                <textFieldExpression><![CDATA[$F{material}]]></textFieldExpression>
            </textField>
            <textField isBlankWhenNull="true">
                <reportElement x="255" y="0" width="160" height="16"/>
                <textFieldExpression><![CDATA[$F{itemgroup}]]></textFieldExpression>
            </textField>
            <textField pattern="#,##0.00" isBlankWhenNull="true">
                <reportElement x="415" y="0" width="80" height="16"/>
                <textElement textAlignment="Right"/>
                <textFieldExpression><![CDATA[$F{qty}]]></textFieldExpression>
            </textField>
            <textField pattern="#,##0.##" isBlankWhenNull="true">
                <reportElement x="495" y="0" width="60" height="16"/>
                <textElement textAlignment="Right"/>
                <textFieldExpression><![CDATA[$F{perseries}]]></textFieldExpression>
            </textField>
        </band>
    </detail>

    <pageFooter>
        <band height="20">
            <textField>
                <reportElement x="455" y="4" width="100" height="14"/>
                <textElement textAlignment="Right"><font size="8"/></textElement>
                <textFieldExpression><![CDATA["Page " + $V{PAGE_NUMBER}]]></textFieldExpression>
            </textField>
        </band>
    </pageFooter>
</jasperReport>
```

- [ ] **Step 2: Verifikasi template bisa dikompilasi Jasper**

Tambahkan test sementara `src/test/java/com/vaadinerp/report/BomTemplateCompileTest.java`:

```java
package com.vaadinerp.report;

import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class BomTemplateCompileTest {

    @Test
    void bomTemplateCompiles() throws Exception {
        File f = new File("src/main/resources/report-templates/RPT_BOM_DOC_JSP.jrxml");
        assumeTrue(f.exists(), "template not present in this checkout");
        try (FileInputStream in = new FileInputStream(f)) {
            JasperReport jr = new JasperTemplateService().compileForUpload(in);
            assertThat(jr).isNotNull();
            assertThat(jr.getName()).isEqualTo("RPT_BOM_DOC_JSP");
        }
    }
}
```

Run: `mvn -q -Dtest=BomTemplateCompileTest test`
Expected: PASS. Bila gagal, perbaiki `.jrxml` sampai lulus — pesan `JRException` menyebut baris yang bermasalah.

- [ ] **Step 3: Salin template ke direktori runtime**

`ReportResolver.resolveMasterTemplate` mencari template di `{app.upload.dir}/report_templates/master/`,
dan `app.upload.dir=./uploads` (`application.properties:57`).

```bash
mkdir -p uploads/report_templates/master
cp src/main/resources/report-templates/RPT_BOM_DOC_JSP.jrxml \
   uploads/report_templates/master/RPT_BOM_DOC_JSP.jrxml
```

Verifikasi:

```bash
ls -l uploads/report_templates/master/RPT_BOM_DOC_JSP.jrxml
```

Expected: berkas ada. (Direktori ini di-gitignore — langkah salin ini harus diulang di tiap
lingkungan deploy.)

- [ ] **Step 4: Seed ketiga report**

Tambahkan ke akhir `src/main/resources/db-migration.sql`:

```sql
-- ── Report contoh: dokumen Bill of Material, satu per engine ────────────────────
-- Ketiganya memakai data_query yang sama; hanya engine + template yang berbeda.
-- table_name = 'mhbom' menghubungkan report ke form BOM_ALL (bukan sumber query,
-- karena data_query menang di resolveBaseQuery).

DELETE FROM public.meta_report_param
 WHERE report_code IN ('RPT_BOM_DOC_STD','RPT_BOM_DOC_JSP','RPT_BOM_DOC_STI');
DELETE FROM public.meta_report_element
 WHERE report_code IN ('RPT_BOM_DOC_STD','RPT_BOM_DOC_JSP','RPT_BOM_DOC_STI');
DELETE FROM public.meta_report
 WHERE report_code IN ('RPT_BOM_DOC_STD','RPT_BOM_DOC_JSP','RPT_BOM_DOC_STI');

INSERT INTO public.meta_report
    (report_code, report_title, table_name, page_size, orientation, engine_type,
     data_query, category, description, usage_scope, group_by)
VALUES
    ('RPT_BOM_DOC_STD', 'Bill of Material Document (Standard)', 'mhbom', 'A4', 'PORTRAIT', 'STANDARD',
     'SELECT h.id AS bom_id, h.idno, h.itemname AS product, h.abmdrawingnumber AS drawing, h.netweight, d.itemname AS material, d.itemgroup, d.qty, d.perseries FROM dynamic.mhbom h LEFT JOIN dynamic.mdbom d ON d.mhbomid = h.id WHERE h.id IN (:bom_id) ORDER BY h.id, d.id',
     'Production', 'Prints the selected BOM rows as documents, one page per BOM.', 'FORM', 'bom_id'),
    ('RPT_BOM_DOC_JSP', 'Bill of Material Document (Jasper)', 'mhbom', 'A4', 'PORTRAIT', 'JASPER',
     'SELECT h.id AS bom_id, h.idno, h.itemname AS product, h.abmdrawingnumber AS drawing, h.netweight, d.itemname AS material, d.itemgroup, d.qty, d.perseries FROM dynamic.mhbom h LEFT JOIN dynamic.mdbom d ON d.mhbomid = h.id WHERE h.id IN (:bom_id) ORDER BY h.id, d.id',
     'Production', 'Prints the selected BOM rows as documents, one page per BOM.', 'FORM', NULL),
    ('RPT_BOM_DOC_STI', 'Bill of Material Document (Stimulsoft)', 'mhbom', 'A4', 'PORTRAIT', 'STIMULSOFT',
     'SELECT h.id AS bom_id, h.idno, h.itemname AS product, h.abmdrawingnumber AS drawing, h.netweight, d.itemname AS material, d.itemgroup, d.qty, d.perseries FROM dynamic.mhbom h LEFT JOIN dynamic.mdbom d ON d.mhbomid = h.id WHERE h.id IN (:bom_id) ORDER BY h.id, d.id',
     'Production', 'Prints the selected BOM rows as documents, one page per BOM.', 'FORM', NULL);

INSERT INTO public.meta_report_param
    (report_code, param_name, param_label, param_type, source, source_key, required, col_order)
SELECT r.report_code, 'bom_id', 'BOM', 'TEXTBOX', 'FORM_FIELD', 'id', true, 1
  FROM public.meta_report r
 WHERE r.report_code IN ('RPT_BOM_DOC_STD','RPT_BOM_DOC_JSP','RPT_BOM_DOC_STI');

-- Band untuk engine STANDARD (dua engine lain menyimpan tata letak di templatenya).
INSERT INTO public.meta_report_element
    (report_code, band_type, element_type, element_value, column_width, alignment, font_weight, col_order, format_pattern)
VALUES
    ('RPT_BOM_DOC_STD', 'TITLE',        'LABEL',  'BILL OF MATERIAL', '100%', 'LEFT',  'BOLD',   1, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_HEADER', 'LABEL',  'BOM No:',          '80px', 'LEFT',  'BOLD',   1, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_HEADER', 'FIELD',  'idno',             '120px','LEFT',  'BOLD',   2, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_HEADER', 'LABEL',  'Product:',         '80px', 'LEFT',  'NORMAL', 3, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_HEADER', 'FIELD',  'product',          '275px','LEFT',  'NORMAL', 4, NULL),
    ('RPT_BOM_DOC_STD', 'COLUMN_HEADER','LABEL',  'Material',         '45%',  'LEFT',  'BOLD',   1, NULL),
    ('RPT_BOM_DOC_STD', 'COLUMN_HEADER','LABEL',  'Group',            '30%',  'LEFT',  'BOLD',   2, NULL),
    ('RPT_BOM_DOC_STD', 'COLUMN_HEADER','LABEL',  'Qty',              '15%',  'RIGHT', 'BOLD',   3, NULL),
    ('RPT_BOM_DOC_STD', 'COLUMN_HEADER','LABEL',  '/Series',          '10%',  'RIGHT', 'BOLD',   4, NULL),
    ('RPT_BOM_DOC_STD', 'DETAIL',       'FIELD',  'material',         '45%',  'LEFT',  'NORMAL', 1, NULL),
    ('RPT_BOM_DOC_STD', 'DETAIL',       'FIELD',  'itemgroup',        '30%',  'LEFT',  'NORMAL', 2, NULL),
    ('RPT_BOM_DOC_STD', 'DETAIL',       'FIELD',  'qty',              '15%',  'RIGHT', 'NORMAL', 3, '#,##0.00'),
    ('RPT_BOM_DOC_STD', 'DETAIL',       'FIELD',  'perseries',        '10%',  'RIGHT', 'NORMAL', 4, '#,##0.##'),
    ('RPT_BOM_DOC_STD', 'GROUP_FOOTER', 'LABEL',  'Total item:',      '85%',  'RIGHT', 'BOLD',   1, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_FOOTER', 'SYSTEM', 'COUNT()',          '15%',  'RIGHT', 'BOLD',   2, NULL);
```

- [ ] **Step 5: Jalankan seed**

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -d grp -f src/main/resources/db-migration.sql
```

Verifikasi:

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -d grp -c "
SELECT r.report_code, r.engine_type, r.usage_scope, r.group_by,
       (SELECT count(*) FROM public.meta_report_param p WHERE p.report_code = r.report_code) AS params,
       (SELECT count(*) FROM public.meta_report_element e WHERE e.report_code = r.report_code) AS elements
FROM public.meta_report r WHERE r.report_code LIKE 'RPT_BOM_DOC%' ORDER BY 1;"
```

Expected: tiga baris; `params = 1` untuk semuanya; `elements = 15` untuk `_STD` dan `0` untuk dua lainnya.

- [ ] **Step 6: Verifikasi query seed menghasilkan data**

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -d grp -c "
SELECT h.id AS bom_id, h.idno, count(d.id) AS materials
FROM dynamic.mhbom h LEFT JOIN dynamic.mdbom d ON d.mhbomid = h.id
GROUP BY 1,2 ORDER BY 3 DESC LIMIT 3;"
```

Catat dua `bom_id` teratas — dipakai untuk verifikasi manual di Task 10.

- [ ] **Step 7: Pertahankan test kompilasi template**

Berbeda dari rencana awal, test di Step 2 **tetap dipertahankan**: kini ia membaca berkas di
`src/main/resources/` yang ikut versi, sehingga akan menangkap `.jrxml` yang rusak pada tiap
`mvn test` — bukan bergantung pada direktori runtime yang di-gitignore.

Run: `mvn -q -Dtest=BomTemplateCompileTest test`
Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/resources/report-templates/RPT_BOM_DOC_JSP.jrxml \
        src/main/resources/db-migration.sql \
        src/test/java/com/vaadinerp/report/BomTemplateCompileTest.java
git commit -m "feat: BOM document report examples for all three engines with a shared query"
```

---

### Task 10: Verifikasi menyeluruh

**Files:** (verifikasi) semua.

- [ ] **Step 1: Seluruh test**

Run: `mvn test`
Expected: BUILD SUCCESS, semua test PASS.

- [ ] **Step 2: Restart aplikasi**

Run: `mvn -q spring-boot:run`
Expected: start tanpa error validasi Hibernate (kolom `usage_scope` / `group_by` sudah ada).

- [ ] **Step 3: Cetak Jasper — dua dokumen**

1. Login, buka form **All Bill of Material** (`BOM_ALL`).
2. Di tab Historis, centang **dua** BOM (pakai `bom_id` yang dicatat di Task 9 Step 6).
3. Tekan **Cetak** → dialog menampilkan tiga report BOM.
4. Pilih **Bill of Material Document (Jasper)**.
5. Expected: tab baru berisi PDF **dua halaman**, tiap halaman satu BOM lengkap dengan materialnya, dan `Total item` sesuai jumlah material BOM tersebut.

- [ ] **Step 4: Cetak Standard — page break antar kelompok**

Ulangi dengan **Bill of Material Document (Standard)**.
Expected: keluaran HTML menampilkan dua blok, tiap blok punya header BOM sendiri dan `Total item` kelompoknya. Cetak lewat browser (Ctrl+P) menunjukkan dua halaman.

- [ ] **Step 5: Cetak Stimulsoft — parameter ganda terbaca**

1. Rancang dulu templatenya: buka Report Designer → pilih `RPT_BOM_DOC_STI` → **Design** → tambahkan GroupHeaderBand pada `bom_id` dengan *New Page Before* aktif, DataBand untuk material, lalu Save.
2. Kembali ke form `BOM_ALL`, centang dua BOM, Cetak → pilih report Stimulsoft.
3. Expected: viewer menampilkan data **dua** BOM (bukan satu). Bila hanya satu yang muncul, perbaikan parameter di Task 7 belum berlaku.

- [ ] **Step 6: Satu baris**

Centang **satu** BOM → Cetak → salah satu report.
Expected: satu dokumen, tanpa error. Ini menangkap regresi bila list satu elemen diperlakukan sebagai skalar.

- [ ] **Step 7: Tanpa baris terpilih**

Kosongkan centang → Cetak → pilih report mana pun.
Expected: "Please select at least one row." dan tidak ada query yang dijalankan.

- [ ] **Step 8: Pemisahan Runner dan form**

1. Buka **Run Report**: ketiga report `RPT_BOM_DOC_*` **tidak boleh** muncul (semuanya `usage_scope = FORM`).
2. Ubah `RPT_BOM_DOC_STD` menjadi `BOTH` lewat Designer → refresh Runner → report muncul, dan tetap ada di tombol Cetak form.
3. Kembalikan ke `FORM`.

- [ ] **Step 9: Regresi report lama**

Buka **Run Report** → jalankan `RPT_SALESLINE_EXP3RD`.
Expected: berjalan seperti sebelumnya (LOV filter, output tab).

- [ ] **Step 10: Otorisasi**

Login sebagai user non-super-admin tanpa role yang diizinkan pada report BOM.
Expected: tombol Cetak tidak menampilkan report tersebut. Bila `canPrint` menu dimatikan, tombol Cetak tidak terlihat sama sekali.

- [ ] **Step 11: Commit perbaikan bila ada**

```bash
git add -A
git commit -m "chore: print-from-form end-to-end verification fixes"
```

---

## Self-Review Checklist (sudah dijalankan)

**Spec coverage:**

| Bagian spec | Task |
|---|---|
| §4 kolom `usage_scope` + `group_by` | Task 1 |
| §4 relasi form→report, `reportSourceKey` bersama | Task 1 (entity), Task 8 (pemakaian) |
| §5 alur cetak (daftar → param → jalankan → tab) | Task 8 |
| §6 `FORM_FIELD` selalu List, `IN (:param)` | Task 2, Task 3 |
| §7 JASPER | Task 9 (template) |
| §7 STIMULSOFT (URL + controller) | Task 7 |
| §7 STANDARD GROUP band | Task 4, Task 5 (editor band) |
| §8 tiga report contoh | Task 9 |
| §9 otorisasi (cek ulang saat Run) | Task 8, verifikasi Task 10 Step 10 |
| §10 error handling | Task 6 (`ReportLauncher`), Task 8 (validasi) |
| §11 testing | Task 2/3/4/6/7 unit, Task 10 manual |
| §2 hapus `ReportEngineController` | Task 8 |
| §2 `ReportLauncher` hilangkan duplikasi | Task 6 |

**Placeholder scan:** tidak ada TBD/TODO. Setiap langkah kode berisi kode nyata; setiap langkah manual berisi langkah dan hasil yang diharapkan.

**Type consistency:**
- `ReportMeta.isUsableFrom(String)` — didefinisikan Task 1, dipakai Task 6 (`"RUNNER"`) dan Task 8 (`"FORM"`).
- `FormMeta.reportSourceKey()` — didefinisikan Task 1, dipakai Task 1 Step 8 (`ReportDesignerView`) dan Task 8.
- `ReportParamResolver.resolveFromRows(List, List, String)` — Task 2, dipakai Task 8.
- `ReportLauncher.runAndOpenTab(Component, ReportRunService, ReportMeta, Map, Runnable)` — Task 6, dipakai Task 6 Step 5c dan Task 8.
- `ReportContext` bertambah komponen `groupBy` di Task 4; satu-satunya pemanggil produksi (`ReportRunService`) diperbarui di langkah yang sama.
- `StandardRenderer.renderHtml` overload 3 dan 4 argumen — Task 4; nama kolom band `GROUP_HEADER`/`GROUP_FOOTER` sama persis di Task 4, Task 5, dan seed Task 9.

## Catatan

- Task 4 mengubah komponen `record ReportContext`. Kompilator akan menandai pemanggil lain; tambahkan argumen `null` di pemanggil non-report.
- `uploads/` di-gitignore (`.gitignore:25`), jadi template `.jrxml` disimpan sumbernya di `src/main/resources/report-templates/` dan disalin ke `uploads/report_templates/master/` saat pemasangan. Langkah salin itu harus diulang di tiap lingkungan deploy — kalau terlewat, report JASPER gagal dengan pesan "template tidak ditemukan".
- Cabang `operator = 'IN'` lama (`= ANY`) sengaja tidak disentuh; perbaikannya tercatat di spec §12.
- Task 9 Step 6 mencatat dua `bom_id` dengan material terbanyak; nilai itu dipakai di verifikasi manual Task 10.
