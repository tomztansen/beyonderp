# Report Runner Backend + Designer Additions — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tambahkan data model + service otorisasi + WHERE-builder Model B + field baru di Report Designer, sebagai fondasi untuk layar Report Runner (Plan B).

**Architecture:** `ReportMeta`/`ReportParamMeta` dapat kolom baru (category, description, allowedRoles, filterColumn, operator). `ReportAccessService` menentukan report yang boleh diakses user (super-admin bypass; roles ∩; kosong=super-admin only). `ReportDataService` membangun WHERE dari parameter Model B (reuse validasi operator/identifier existing) di atas base query, lalu bind via NamedParameterJdbcTemplate. Report Designer diberi input untuk field-field baru.

**Tech Stack:** Java 21, Spring Boot 3.3.0, Vaadin, Maven, PostgreSQL.

**Spec:** `docs/superpowers/specs/2026-08-28-report-runner-design.md`

## Global Constraints

- `ddl-auto=validate` → kolom/tabel baru WAJIB ada di DB sebelum entity dipakai (jalankan ALTER/CREATE dulu).
- Operator parameter WAJIB divalidasi whitelist (reuse `DynamicDataService.validateComparisonOperator`); `filterColumn` divalidasi identifier (reuse `DynamicDataService.validateSqlIdentifier`).
- Bind nilai via `MapSqlParameterSource` (NamedParameterJdbcTemplate) — bukan string concatenation.
- Semua teks UI dalam Bahasa Inggris.
- Test: JUnit 5 + Mockito + AssertJ. `mvn -q -Dtest=ClassName#method test`. View Vaadin diverifikasi manual.
- Report tanpa `allowedRoles` → hanya SUPER_ADMIN. Super-admin selalu lihat semua.

---

### Task 1: Skema DB (kolom + tabel baru)

**Files:** (DB only)

**Interfaces:**
- Produces: kolom `meta_report.category`, `meta_report.description`; kolom
  `meta_report_param.filter_column`, `meta_report_param.operator`; tabel `meta_report_role(report_code, role_code)`.

- [ ] **Step 1: Jalankan ALTER/CREATE (idempoten)**

```sql
ALTER TABLE public.meta_report ADD COLUMN IF NOT EXISTS category VARCHAR(50);
ALTER TABLE public.meta_report ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE public.meta_report_param ADD COLUMN IF NOT EXISTS filter_column VARCHAR(100);
ALTER TABLE public.meta_report_param ADD COLUMN IF NOT EXISTS operator VARCHAR(20);
CREATE TABLE IF NOT EXISTS public.meta_report_role (
    report_code VARCHAR(50) NOT NULL,
    role_code   VARCHAR(50) NOT NULL,
    PRIMARY KEY (report_code, role_code)
);
```

- [ ] **Step 2: Verifikasi**

Run (psql): `SELECT column_name FROM information_schema.columns WHERE table_name='meta_report' AND column_name IN ('category','description');`
Expected: 2 baris. Dan `SELECT to_regclass('public.meta_report_role');` → `meta_report_role`.

> Replikasi ALTER/CREATE ini di environment lain saat deploy (ddl-auto=validate).

---

### Task 2: Entity `ReportMeta` + `ReportParamMeta` — field baru

**Files:**
- Modify: `src/main/java/com/vaadinerp/meta/ReportMeta.java`
- Modify: `src/main/java/com/vaadinerp/meta/ReportParamMeta.java`
- Test: `src/test/java/com/vaadinerp/meta/ReportMetaNewFieldsTest.java`

**Interfaces:**
- Produces:
  - `ReportMeta`: `String getCategory()/setCategory`, `String getDescription()/setDescription`,
    `Set<String> getAllowedRoles()/setAllowedRoles` (@ElementCollection → `meta_report_role`).
  - `ReportParamMeta`: `String getFilterColumn()/setFilterColumn`, `String getOperator()/setOperator`.

- [ ] **Step 1: Tulis failing test**

```java
package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class ReportMetaNewFieldsTest {

    @Test
    void reportMetaHasCategoryDescriptionRoles() {
        ReportMeta r = new ReportMeta();
        r.setCategory("Sales");
        r.setDescription("Invoice listing");
        r.setAllowedRoles(Set.of("ADMIN", "SALES"));
        assertThat(r.getCategory()).isEqualTo("Sales");
        assertThat(r.getDescription()).isEqualTo("Invoice listing");
        assertThat(r.getAllowedRoles()).containsExactlyInAnyOrder("ADMIN", "SALES");
    }

    @Test
    void reportParamHasFilterColumnAndOperator() {
        ReportParamMeta p = new ReportParamMeta();
        p.setFilterColumn("trx_date");
        p.setOperator(">=");
        assertThat(p.getFilterColumn()).isEqualTo("trx_date");
        assertThat(p.getOperator()).isEqualTo(">=");
    }
}
```

- [ ] **Step 2: Run — verify FAIL**

Run: `mvn -q -Dtest=ReportMetaNewFieldsTest test`
Expected: FAIL (kompilasi — method belum ada).

- [ ] **Step 3: Tambah field ke `ReportMeta`** (di antara field existing, sebelum `@OneToMany params`)

```java
    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "description", length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meta_report_role", schema = "public",
            joinColumns = @JoinColumn(name = "report_code"))
    @Column(name = "role_code")
    private java.util.Set<String> allowedRoles = new java.util.HashSet<>();
```

- [ ] **Step 4: Tambah field ke `ReportParamMeta`**

```java
    @Column(name = "filter_column", length = 100)
    private String filterColumn;

    @Column(name = "operator", length = 20)
    private String operator;
```

- [ ] **Step 5: Run — verify PASS**

Run: `mvn -q -Dtest=ReportMetaNewFieldsTest test`
Expected: PASS. (ReportMeta/ReportParamMeta pakai Lombok `@Getter/@Setter` → accessor otomatis.)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vaadinerp/meta/ReportMeta.java \
        src/main/java/com/vaadinerp/meta/ReportParamMeta.java \
        src/test/java/com/vaadinerp/meta/ReportMetaNewFieldsTest.java
git commit -m "feat: add category/description/allowedRoles to ReportMeta and filterColumn/operator to ReportParamMeta"
```

---

### Task 3: `ReportAccessService` (otorisasi katalog)

**Files:**
- Create: `src/main/java/com/vaadinerp/report/ReportAccessService.java`
- Test: `src/test/java/com/vaadinerp/report/ReportAccessServiceTest.java`

**Interfaces:**
- Consumes: `ReportMeta`; roles user + flag super-admin (parameter — pure logic).
- Produces:
  - `static boolean ReportAccessService.canAccess(java.util.Set<String> userRoles, boolean superAdmin, java.util.Set<String> allowedRoles)` —
    `true` bila `superAdmin`, atau `allowedRoles` non-kosong dan berpotongan dgn `userRoles`.
    `allowedRoles` kosong/null → hanya `superAdmin`.
  - `boolean canAccess(ReportMeta report)` — instance method memakai user aktif (roles + super-admin dari `SessionSecurityService`).
  - `List<ReportMeta> accessibleReports(List<ReportMeta> all)` — filter.

- [ ] **Step 1: Tulis failing test (logika pure)**

```java
package com.vaadinerp.report;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class ReportAccessServiceTest {

    @Test
    void superAdminSeesEverythingIncludingNoRoles() {
        assertThat(ReportAccessService.canAccess(Set.of("X"), true, Set.of())).isTrue();
        assertThat(ReportAccessService.canAccess(Set.of("X"), true, Set.of("ADMIN"))).isTrue();
    }

    @Test
    void emptyAllowedRolesVisibleOnlyToSuperAdmin() {
        assertThat(ReportAccessService.canAccess(Set.of("ADMIN"), false, Set.of())).isFalse();
        assertThat(ReportAccessService.canAccess(Set.of("ADMIN"), false, null)).isFalse();
    }

    @Test
    void roleIntersectionGrantsAccess() {
        assertThat(ReportAccessService.canAccess(Set.of("SALES", "HR"), false, Set.of("SALES"))).isTrue();
        assertThat(ReportAccessService.canAccess(Set.of("HR"), false, Set.of("SALES"))).isFalse();
    }
}
```

- [ ] **Step 2: Run — verify FAIL**

Run: `mvn -q -Dtest=ReportAccessServiceTest test`
Expected: FAIL.

- [ ] **Step 3: Implementasi**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.service.SessionSecurityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportAccessService {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final SessionSecurityService securityService;

    public ReportAccessService(SessionSecurityService securityService) {
        this.securityService = securityService;
    }

    /** Logika pure. Report tanpa allowedRoles → hanya super-admin. */
    public static boolean canAccess(Set<String> userRoles, boolean superAdmin, Set<String> allowedRoles) {
        if (superAdmin) return true;
        if (allowedRoles == null || allowedRoles.isEmpty()) return false;
        if (userRoles == null) return false;
        for (String r : allowedRoles) {
            if (userRoles.contains(r)) return true;
        }
        return false;
    }

    public boolean canAccess(ReportMeta report) {
        AppUser u = securityService.getCurrentUser();
        Set<String> roles = (u != null && u.getRoles() != null) ? u.getRoles() : Set.of();
        boolean superAdmin = roles.contains(SUPER_ADMIN);
        return canAccess(roles, superAdmin, report.getAllowedRoles());
    }

    public List<ReportMeta> accessibleReports(List<ReportMeta> all) {
        return all.stream().filter(this::canAccess).collect(Collectors.toList());
    }
}
```

- [ ] **Step 4: Run — verify PASS**

Run: `mvn -q -Dtest=ReportAccessServiceTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportAccessService.java \
        src/test/java/com/vaadinerp/report/ReportAccessServiceTest.java
git commit -m "feat: add ReportAccessService (role-based report visibility, super-admin bypass)"
```

---

### Task 4: WHERE-builder Model B di `ReportDataService`

**Files:**
- Modify: `src/main/java/com/vaadinerp/report/ReportDataService.java`
- Test: `src/test/java/com/vaadinerp/report/ReportModelBWhereTest.java`

**Interfaces:**
- Consumes: `ReportParamMeta` (filterColumn, operator, paramName); nilai parameter.
- Produces:
  - `static String ReportDataService.buildModelBWhere(List<ReportParamMeta> params, Map<String,Object> values, Map<String,Object> outBind)` —
    kembalikan fragmen `" WHERE {col} {op} :{name} AND ..."` (atau "" bila tak ada param Model B ber-nilai),
    isi `outBind` dgn nilai (LIKE/ILIKE dibungkus `%..%`; IN → `= ANY`). Operator & kolom divalidasi.

- [ ] **Step 1: Tulis failing test**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class ReportModelBWhereTest {

    private ReportParamMeta p(String name, String col, String op) {
        ReportParamMeta m = new ReportParamMeta();
        m.setParamName(name); m.setFilterColumn(col); m.setOperator(op);
        return m;
    }

    @Test
    void buildsEqualsAndBindsValue() {
        Map<String,Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p("branch","branch_id","=")), Map.of("branch", 7), bind);
        assertThat(where).isEqualTo(" WHERE branch_id = :branch");
        assertThat(bind).containsEntry("branch", 7);
    }

    @Test
    void likeWrapsWithPercent() {
        Map<String,Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p("kw","name","ILIKE")), Map.of("kw","abc"), bind);
        assertThat(where).isEqualTo(" WHERE name ILIKE :kw");
        assertThat(bind).containsEntry("kw", "%abc%");
    }

    @Test
    void rangeTwoParams() {
        Map<String,Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p("d1","trx_date",">="), p("d2","trx_date","<=")),
                Map.of("d1","2026-01-01","d2","2026-01-31"), bind);
        assertThat(where).isEqualTo(" WHERE trx_date >= :d1 AND trx_date <= :d2");
        assertThat(bind).containsKeys("d1","d2");
    }

    @Test
    void inUsesAny() {
        Map<String,Object> bind = new HashMap<>();
        String where = ReportDataService.buildModelBWhere(
                List.of(p("st","status","IN")), Map.of("st", List.of("A","B")), bind);
        assertThat(where).isEqualTo(" WHERE status = ANY(:st)");
    }

    @Test
    void skipsParamsWithoutValueOrModelB() {
        Map<String,Object> bind = new HashMap<>();
        ReportParamMeta noValue = p("x","col","=");
        ReportParamMeta modelA = new ReportParamMeta(); modelA.setParamName("y"); // no filterColumn
        String where = ReportDataService.buildModelBWhere(
                List.of(noValue, modelA), new HashMap<>(), bind);
        assertThat(where).isEmpty();
    }

    @Test
    void rejectsInvalidOperator() {
        assertThatThrownBy(() -> ReportDataService.buildModelBWhere(
                List.of(p("x","col","DROP")), Map.of("x", 1), new HashMap<>()))
                .isInstanceOf(RuntimeException.class);
    }
}
```

- [ ] **Step 2: Run — verify FAIL**

Run: `mvn -q -Dtest=ReportModelBWhereTest test`
Expected: FAIL.

- [ ] **Step 3: Implementasi `buildModelBWhere`** (static, pure)

```java
    /**
     * Bangun WHERE dari parameter Model B (filterColumn + operator). Hanya param yang punya
     * filterColumn, operator, DAN nilai yang diproses. Nilai di-bind ke outBind.
     */
    public static String buildModelBWhere(List<com.vaadinerp.meta.ReportParamMeta> params,
                                          Map<String, Object> values, Map<String, Object> outBind) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (com.vaadinerp.meta.ReportParamMeta p : params) {
            String col = p.getFilterColumn();
            String opRaw = p.getOperator();
            if (col == null || col.isBlank() || opRaw == null || opRaw.isBlank()) continue; // Model A / bukan filter
            Object val = values != null ? values.get(p.getParamName()) : null;
            if (val == null || (val instanceof String s && s.isBlank())) continue; // tanpa nilai → dilewati

            validateSqlIdentifier(col, "filter column");
            String op = validateComparisonOperator(opRaw); // whitelist; melempar bila ilegal
            String name = p.getParamName();

            sb.append(sb.length() == 0 ? " WHERE " : " AND ");
            if ("IN".equals(op)) {
                sb.append(col).append(" = ANY(:").append(name).append(")");
                outBind.put(name, val);
            } else if ("LIKE".equals(op) || "ILIKE".equals(op)) {
                sb.append(col).append(" ").append(op).append(" :").append(name);
                String s = val.toString();
                outBind.put(name, s.contains("%") ? s : "%" + s + "%");
            } else {
                sb.append(col).append(" ").append(op).append(" :").append(name);
                outBind.put(name, val);
            }
        }
        return sb.toString();
    }
```

> `validateComparisonOperator` mengembalikan operator ter-uppercase dari whitelist
> (`=,!=,<>,<,>,<=,>=,LIKE,ILIKE,...`). Pastikan whitelist mencakup operator yang ditawarkan
> di Report Designer; tambahkan bila kurang di `DynamicDataService.ALLOWED_COMPARISON_OPS`.

- [ ] **Step 4: Integrasikan ke `fetchData`** (Model B membungkus base query)

Di `fetchData`, setelah resolusi `sql` (dataQuery/viewTable/tableName) dan sebelum eksekusi:

```java
        Map<String, Object> bind = new HashMap<>();
        if (params != null) params.forEach(bind::putIfAbsent);
        String whereB = buildModelBWhere(reportParamsOf(report), params, bind);
        if (!whereB.isEmpty()) {
            sql = "SELECT * FROM ( " + sql + " ) AS _rpt " + whereB.trim();
        }
        MapSqlParameterSource src = new MapSqlParameterSource();
        bind.forEach(src::addValue);
        List<Map<String, Object>> rows = npjt.queryForList(sql, src);
```

> `reportParamsOf(report)` = `report.getParams()`. Model A (`:param` di dataQuery) tetap ter-bind
> dari `params`. Model B menambah WHERE + bind. Keduanya lewat `src` yang sama.
> Sesuaikan dgn struktur `fetchData` terkini (yang sudah memakai NamedParameterJdbcTemplate).

- [ ] **Step 5: Run — verify PASS + compile**

Run: `mvn -q -Dtest=ReportModelBWhereTest,ReportDataServiceTest test`
Expected: PASS. Lalu `mvn -q -DskipTests compile` → BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportDataService.java \
        src/test/java/com/vaadinerp/report/ReportModelBWhereTest.java
git commit -m "feat: Model B WHERE builder (filterColumn+operator) in ReportDataService"
```

---

### Task 5: Report Designer — field baru (metadata + kolom parameter)

**Files:**
- Modify: `src/main/java/com/vaadinerp/views/ReportDesignerView.java`

**Interfaces:**
- Consumes: `AppRoleRepository` (daftar role); field baru entity (Task 2).
- Produces: input `Category` (ComboBox custom-value), `Description` (TextArea), `Allowed Roles`
  (MultiSelectComboBox) di metadata; kolom `Filter Column` + `Operator` di grid parameter.
  Save existing mem-persist semuanya.

- [ ] **Step 1: Tambah field metadata**

- `Category`: `ComboBox<String> categoryCombo` dgn `setAllowCustomValue(true)`; items =
  `reportMetaRepository.findAll().stream().map(ReportMeta::getCategory).filter(...distinct...)`.
  `addCustomValueSetListener(e -> categoryCombo.setValue(e.getDetail()))`.
- `Description`: `TextArea`.
- `Allowed Roles`: `MultiSelectComboBox<String> rolesSelect` items = role codes dari
  `AppRoleRepository.findAll()` (inject via constructor atau `SpringContextHolder`).

Tambahkan ke `FormLayout meta`. Di `loadReportState`: set nilai dari report
(`categoryCombo.setValue(report.getCategory())`, `description`, `rolesSelect.setValue(report.getAllowedRoles())`).
Di `saveReport`: `rep.setCategory(...)`, `rep.setDescription(...)`,
`rep.setAllowedRoles(new HashSet<>(rolesSelect.getValue()))`.

- [ ] **Step 2: Tambah kolom parameter grid** (inline-editable, pola sama seperti kolom lain)

```java
        TextField edFilterCol = new TextField();
        Grid.Column<ReportParamMeta> pColFilter = paramGrid.addColumn(ReportParamMeta::getFilterColumn)
                .setHeader("Filter Column").setEditorComponent(edFilterCol);
        pBinder.forField(edFilterCol).bind(ReportParamMeta::getFilterColumn, ReportParamMeta::setFilterColumn);

        Select<String> edOperator = new Select<>();
        edOperator.setItems("", "=", "!=", "LIKE", "ILIKE", ">=", "<=", ">", "<", "IN");
        Grid.Column<ReportParamMeta> pColOp = paramGrid.addColumn(ReportParamMeta::getOperator)
                .setHeader("Operator").setEditorComponent(edOperator);
        pBinder.forField(edOperator).bind(ReportParamMeta::getOperator, ReportParamMeta::setOperator);
```

Tambahkan `pColFilter`/`pColOp` ke `pGetters` (filter/sort/export ikut). `cloneParam` juga menyalin
`filterColumn`/`operator`.

- [ ] **Step 3: Verifikasi compile**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Verifikasi manual (jalankan app)**

Restart → `/report-designer` → New/Edit: ada field **Category** (bisa ketik baru), **Description**,
**Allowed Roles** (multiselect). Grid parameter punya kolom **Filter Column** & **Operator** yang
bisa diedit inline. Save → cek tersimpan (buka DB `meta_report`/`meta_report_role`/`meta_report_param`).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/views/ReportDesignerView.java
git commit -m "feat: Report Designer fields for category/description/allowedRoles + param filterColumn/operator"
```

---

### Task 6: Verifikasi menyeluruh

- [ ] **Step 1: Jalankan seluruh test**

Run: `mvn test`
Expected: semua PASS (termasuk test Plan 1/2 + baru), BUILD SUCCESS.

- [ ] **Step 2: Verifikasi startup (JPA validate)**

Reuse pola `ReportMappingValidationTest` (atau jalankan app) untuk memastikan entity baru lolos
`ddl-auto=validate` terhadap DB (kolom/tabel dari Task 1 sudah ada).

- [ ] **Step 3: Commit (bila ada perbaikan)**

```bash
git add -A && git commit -m "chore: report runner backend verification fixes"
```

---

## Self-Review Checklist (sudah dijalankan)

- **Spec coverage:** data model category/description/allowedRoles/filterColumn/operator (T1-T2),
  otorisasi (T3), Model B WHERE (T4), Designer additions (T5). Report Runner **view** = Plan B.
- **Placeholder scan:** langkah UI (T5) memberi kode konkret + verifikasi manual; tidak ada TODO.
- **Type consistency:** `ReportAccessService.canAccess` (T3), `buildModelBWhere` (T4) dipakai
  konsisten; field entity (T2) dipakai T4/T5.

## Catatan
- Test unit fokus logika pure (akses role, WHERE builder). Integrasi DB (query nyata) + view Designer
  diverifikasi manual. `@ElementCollection allowedRoles` butuh tabel `meta_report_role` (Task 1).
