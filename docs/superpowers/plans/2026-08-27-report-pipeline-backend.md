# Report Pipeline Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bangun pipeline backend report yang engine-agnostic (resolve template, ambil data ber-parameter yang aman, render ke 3 engine) sebagai fondasi untuk Report Designer UI.

**Architecture:** Strategy pattern. `ReportResolver` menentukan engine + file template (master). `ReportDataService` menyusun query (dataQuery → viewTable form → tableName), mengikat parameter via `NamedParameterJdbcTemplate`, dan memperkaya label LOV. `ReportRenderer` adalah interface dengan 3 implementasi (Standard/Stimulsoft/Jasper). Semua unit kecil, dependency di-inject, bisa diuji terpisah.

**Tech Stack:** Java 21, Spring Boot 3.3.0, Maven, PostgreSQL, Vaadin (view menyusul di Plan 2), JasperReports 6.21.3, Stimulsoft Reports.JAVA 2026.3.2 (sudah ada).

**Spec:** `docs/superpowers/specs/2026-08-27-report-designer-design.md`

## Global Constraints

- Java 21; Spring Boot 3.3.0; build & test via Maven (`mvn`, tidak ada wrapper).
- Template disimpan sebagai **file** di bawah `uploadDir/report_templates/master/`; **tidak** menyimpan BLOB di DB (aturan `.agents/AGENTS.md`).
- Parameter query WAJIB via `NamedParameterJdbcTemplate` + `MapSqlParameterSource` — **bukan** string concatenation/replace.
- `reportCode` yang dipakai sebagai nama file WAJIB divalidasi regex `^[A-Za-z0-9_-]+$` (anti path-traversal).
- Resolusi datasource WAJIB mengikuti urutan `GenericFormView`: `dataQuery` → `FormMeta.viewTable` → `SELECT * FROM {qualified tableName}`.
- Reuse yang sudah ada: `FileStorageService`, `ComponentFactory.formatFieldValueWithLov`, `DynamicDataService.getQualifiedTableName / validateAndSanitizeSelectQuery / resolveSqlKeywords`.
- JasperReports runtime version = **6.21.3**; ini versi acuan untuk `.jasper` yang di-upload author.
- Test framework: JUnit 5 (Jupiter) + Mockito + AssertJ (dari `spring-boot-starter-test`). Jalankan satu test: `mvn -q -Dtest=ClassName#method test`.

---

### Task 1: Tambah dependency JasperReports

**Files:**
- Modify: `pom.xml` (blok `<dependencies>`, dekat dependency Stimulsoft ~baris 99-104)

**Interfaces:**
- Consumes: —
- Produces: kelas `net.sf.jasperreports.engine.*` (`JasperCompileManager`, `JasperFillManager`, `JasperReport`, `JRMapCollectionDataSource`) tersedia di classpath.

- [ ] **Step 1: Tambah dependency**

Sisipkan setelah dependency Stimulsoft di `pom.xml`:

```xml
<!-- JasperReports Engine -->
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>6.21.3</version>
</dependency>
```

- [ ] **Step 2: Verifikasi dependency ter-resolve & compile**

Run: `mvn -q -o dependency:resolve 2>/dev/null || mvn -q dependency:resolve`
Then: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS; tidak ada error "package net.sf.jasperreports does not exist".

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: add JasperReports 6.21.3 dependency"
```

---

### Task 2: Entity `ReportParamMeta` + repository

**Files:**
- Create: `src/main/java/com/vaadinerp/meta/ReportParamMeta.java`
- Create: `src/main/java/com/vaadinerp/meta/ReportParamMetaRepository.java`
- Test: `src/test/java/com/vaadinerp/meta/ReportParamMetaTest.java`

**Interfaces:**
- Consumes: —
- Produces:
  - `ReportParamMeta` dengan getter/setter: `Long getId()`, `String getReportCode()`, `String getParamName()`, `String getLabel()`, `String getDataType()`, `String getLovCode()`, `String getSource()`, `String getSourceKey()`, `String getDefaultValue()`, `boolean isRequired()`, `Integer getColOrder()` (+ setter masing-masing).
  - `ReportParamMetaRepository extends JpaRepository<ReportParamMeta, Long>` dengan `List<ReportParamMeta> findByReportCodeOrderByColOrderAsc(String reportCode)`.

- [ ] **Step 1: Tulis failing test (entity field mapping)**

```java
package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamMetaTest {

    @Test
    void gettersReturnSetValues() {
        ReportParamMeta p = new ReportParamMeta();
        p.setReportCode("INV");
        p.setParamName("id");
        p.setLabel("Invoice ID");
        p.setDataType("NUMBER");
        p.setSource("FORM_FIELD");
        p.setSourceKey("invoice_id");
        p.setRequired(true);
        p.setColOrder(1);

        assertThat(p.getReportCode()).isEqualTo("INV");
        assertThat(p.getParamName()).isEqualTo("id");
        assertThat(p.getDataType()).isEqualTo("NUMBER");
        assertThat(p.getSource()).isEqualTo("FORM_FIELD");
        assertThat(p.getSourceKey()).isEqualTo("invoice_id");
        assertThat(p.isRequired()).isTrue();
        assertThat(p.getColOrder()).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=ReportParamMetaTest test`
Expected: FAIL (kompilasi gagal, `ReportParamMeta` belum ada).

- [ ] **Step 3: Buat entity**

```java
package com.vaadinerp.meta;

import jakarta.persistence.*;

@Entity
@Table(name = "meta_report_param")
public class ReportParamMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_code", nullable = false, length = 100)
    private String reportCode;

    @Column(name = "param_name", nullable = false, length = 100)
    private String paramName;

    @Column(name = "label", length = 200)
    private String label;

    /** TEXT | NUMBER | DATE | BOOLEAN | LOV */
    @Column(name = "data_type", length = 20)
    private String dataType = "TEXT";

    @Column(name = "lov_code", length = 100)
    private String lovCode;

    /** FORM_FIELD | USER_INPUT | SYSTEM */
    @Column(name = "source", length = 20)
    private String source = "USER_INPUT";

    /** nama field form (FORM_FIELD) atau keyword (SYSTEM, mis. $CURRENT_USER) */
    @Column(name = "source_key", length = 200)
    private String sourceKey;

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    @Column(name = "required")
    private boolean required = false;

    @Column(name = "col_order")
    private Integer colOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
    public String getParamName() { return paramName; }
    public void setParamName(String paramName) { this.paramName = paramName; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getLovCode() { return lovCode; }
    public void setLovCode(String lovCode) { this.lovCode = lovCode; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public Integer getColOrder() { return colOrder; }
    public void setColOrder(Integer colOrder) { this.colOrder = colOrder; }
}
```

- [ ] **Step 4: Buat repository**

```java
package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportParamMetaRepository extends JpaRepository<ReportParamMeta, Long> {
    List<ReportParamMeta> findByReportCodeOrderByColOrderAsc(String reportCode);
}
```

- [ ] **Step 5: Run test — verify PASS**

Run: `mvn -q -Dtest=ReportParamMetaTest test`
Expected: PASS.

> Catatan skema DB: entity memakai `hibernate.ddl-auto` yang berlaku di project (cek `application.properties`). Jika `ddl-auto=none`, tambahkan tabel `meta_report_param` lewat mekanisme migrasi yang biasa dipakai project. Kolom minimal: `id BIGSERIAL PK, report_code VARCHAR(100), param_name VARCHAR(100), label VARCHAR(200), data_type VARCHAR(20), lov_code VARCHAR(100), source VARCHAR(20), source_key VARCHAR(200), default_value VARCHAR(500), required BOOLEAN, col_order INT`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vaadinerp/meta/ReportParamMeta.java \
        src/main/java/com/vaadinerp/meta/ReportParamMetaRepository.java \
        src/test/java/com/vaadinerp/meta/ReportParamMetaTest.java
git commit -m "feat: add ReportParamMeta entity and repository"
```

---

### Task 3: Adapter `ReportParamMeta` → `FieldMeta`

**Files:**
- Create: `src/main/java/com/vaadinerp/report/ReportParamAdapter.java`
- Test: `src/test/java/com/vaadinerp/report/ReportParamAdapterTest.java`

**Interfaces:**
- Consumes: `ReportParamMeta` (Task 2); `FieldMeta` (existing, `com.vaadinerp.meta.FieldMeta`).
- Produces: `static FieldMeta ReportParamAdapter.toFieldMeta(ReportParamMeta p)` — `FieldMeta` dengan `fieldName=paramName`, `label`, `required`, `lovCode`, dan `inputType` dipetakan dari `dataType` (LOV→"LOV"/"COMBOBOX" sesuai konvensi FieldMeta project, DATE→"DATE", NUMBER→"NUMBER", BOOLEAN→"CHECKBOX", selain itu "TEXT").

> Sebelum implementasi, buka `com.vaadinerp.meta.FieldMeta` untuk nama properti input-type yang benar (mis. `setInputType`/`setFieldType`) dan nilai yang dikenali `ComponentFactory.createInternal`. Sesuaikan pemetaan agar `ComponentFactory.create(fieldMeta, ...)` menghasilkan komponen yang tepat. Ganti `setInputType`/nilai di bawah bila konvensi project berbeda.

- [ ] **Step 1: Tulis failing test**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamAdapterTest {

    @Test
    void mapsLovParamToLovFieldMeta() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("branch");
        p.setLabel("Cabang");
        p.setDataType("LOV");
        p.setLovCode("BRANCH");
        p.setRequired(true);

        FieldMeta f = ReportParamAdapter.toFieldMeta(p);

        assertThat(f.getFieldName()).isEqualTo("branch");
        assertThat(f.getLabel()).isEqualTo("Cabang");
        assertThat(f.getLovCode()).isEqualTo("BRANCH");
        assertThat(f.isRequired()).isTrue();
    }

    @Test
    void mapsDateParamToDateInput() {
        ReportParamMeta p = new ReportParamMeta();
        p.setParamName("trx_date");
        p.setDataType("DATE");

        FieldMeta f = ReportParamAdapter.toFieldMeta(p);

        // sesuaikan getter/expected dengan konvensi FieldMeta project
        assertThat(f.getFieldName()).isEqualTo("trx_date");
        assertThat(ReportParamAdapter.resolveInputType("DATE")).isEqualTo("DATE");
    }
}
```

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=ReportParamAdapterTest test`
Expected: FAIL (`ReportParamAdapter` belum ada).

- [ ] **Step 3: Implementasi adapter**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportParamMeta;

public final class ReportParamAdapter {

    private ReportParamAdapter() {}

    public static FieldMeta toFieldMeta(ReportParamMeta p) {
        FieldMeta f = new FieldMeta();
        f.setFieldName(p.getParamName());
        f.setLabel(p.getLabel() != null ? p.getLabel() : p.getParamName());
        f.setRequired(p.isRequired());
        f.setLovCode(p.getLovCode());
        f.setInputType(resolveInputType(p.getDataType())); // sesuaikan nama setter FieldMeta
        return f;
    }

    /** Petakan dataType parameter → inputType FieldMeta yang dikenali ComponentFactory. */
    public static String resolveInputType(String dataType) {
        if (dataType == null) return "TEXT";
        switch (dataType.trim().toUpperCase()) {
            case "LOV":     return "LOV";       // sesuaikan bila project pakai "COMBOBOX"
            case "DATE":    return "DATE";
            case "NUMBER":  return "NUMBER";
            case "BOOLEAN": return "CHECKBOX";
            default:        return "TEXT";
        }
    }
}
```

- [ ] **Step 4: Run test — verify PASS**

Run: `mvn -q -Dtest=ReportParamAdapterTest test`
Expected: PASS. Jika gagal karena nama setter/nilai inputType berbeda, sesuaikan dengan `FieldMeta` lalu jalankan lagi.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportParamAdapter.java \
        src/test/java/com/vaadinerp/report/ReportParamAdapterTest.java
git commit -m "feat: add ReportParamMeta to FieldMeta adapter"
```

---

### Task 4: `ReportResolver` (validasi path + resolusi template master)

**Files:**
- Create: `src/main/java/com/vaadinerp/report/ReportResolver.java`
- Test: `src/test/java/com/vaadinerp/report/ReportResolverTest.java`

**Interfaces:**
- Consumes: `ReportMeta` + `ReportMetaRepository` (existing); `@Value("${app.upload.dir:./uploads}")`.
- Produces:
  - `boolean ReportResolver.isValidReportCode(String code)` — true hanya untuk `^[A-Za-z0-9_-]+$`.
  - `String ReportResolver.masterExtension(String engineType, String templatePath)` — "mrt" untuk STIMULSOFT; untuk JASPER pakai ekstensi dari `templatePath` (`jasper`/`jrxml`), default "jasper"; null untuk STANDARD.
  - `java.io.File ReportResolver.resolveMasterTemplate(String code, String engineType, String templatePath)` — file di `{uploadDir}/report_templates/master/{code}.{ext}`; lempar `IllegalArgumentException` bila code invalid.

- [ ] **Step 1: Tulis failing test**

```java
package com.vaadinerp.report;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ReportResolverTest {

    private ReportResolver newResolver(String uploadDir) {
        ReportResolver r = new ReportResolver(null); // repo tak dipakai di test ini
        r.setUploadDirForTest(uploadDir);
        return r;
    }

    @Test
    void rejectsInvalidCode() {
        ReportResolver r = newResolver("./uploads");
        assertThat(r.isValidReportCode("INV_2024")).isTrue();
        assertThat(r.isValidReportCode("../etc/passwd")).isFalse();
        assertThat(r.isValidReportCode("a b")).isFalse();
        assertThat(r.isValidReportCode(null)).isFalse();
    }

    @Test
    void masterExtensionByEngine() {
        ReportResolver r = newResolver("./uploads");
        assertThat(r.masterExtension("STIMULSOFT", null)).isEqualTo("mrt");
        assertThat(r.masterExtension("JASPER", "anything.jrxml")).isEqualTo("jrxml");
        assertThat(r.masterExtension("JASPER", null)).isEqualTo("jasper");
        assertThat(r.masterExtension("STANDARD", null)).isNull();
    }

    @Test
    void resolveMasterTemplateThrowsOnTraversal() {
        ReportResolver r = newResolver("./uploads");
        assertThatThrownBy(() -> r.resolveMasterTemplate("../x", "STIMULSOFT", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolveMasterTemplateBuildsExpectedPath() {
        ReportResolver r = newResolver("/tmp/up");
        java.io.File f = r.resolveMasterTemplate("INV", "STIMULSOFT", null);
        assertThat(f.getPath().replace('\\','/'))
                .endsWith("report_templates/master/INV.mrt");
    }
}
```

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=ReportResolverTest test`
Expected: FAIL (`ReportResolver` belum ada).

- [ ] **Step 3: Implementasi resolver**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMetaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class ReportResolver {

    private static final java.util.regex.Pattern CODE = java.util.regex.Pattern.compile("^[A-Za-z0-9_-]+$");

    private final ReportMetaRepository reportMetaRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public ReportResolver(ReportMetaRepository reportMetaRepository) {
        this.reportMetaRepository = reportMetaRepository;
    }

    /** hanya untuk test: override uploadDir tanpa Spring. */
    void setUploadDirForTest(String dir) { this.uploadDir = dir; }

    public boolean isValidReportCode(String code) {
        return code != null && CODE.matcher(code).matches();
    }

    public String masterExtension(String engineType, String templatePath) {
        if (engineType == null) return null;
        switch (engineType.trim().toUpperCase()) {
            case "STIMULSOFT": return "mrt";
            case "JASPER":
                if (templatePath != null && templatePath.trim().toLowerCase().endsWith(".jrxml")) return "jrxml";
                return "jasper";
            default: return null; // STANDARD tak punya file template
        }
    }

    public File resolveMasterTemplate(String code, String engineType, String templatePath) {
        if (!isValidReportCode(code)) {
            throw new IllegalArgumentException("Invalid report code: " + code);
        }
        String ext = masterExtension(engineType, templatePath);
        if (ext == null) {
            throw new IllegalStateException("Engine " + engineType + " tidak punya file template");
        }
        File dir = new File(new File(uploadDir, "report_templates"), "master");
        return new File(dir, code + "." + ext);
    }
}
```

- [ ] **Step 4: Run test — verify PASS**

Run: `mvn -q -Dtest=ReportResolverTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportResolver.java \
        src/test/java/com/vaadinerp/report/ReportResolverTest.java
git commit -m "feat: add ReportResolver with path validation and master template resolution"
```

---

### Task 5: `ReportDataService` (resolusi query + parameter aman + LOV)

**Files:**
- Create: `src/main/java/com/vaadinerp/report/ReportDataService.java`
- Test: `src/test/java/com/vaadinerp/report/ReportDataServiceTest.java`

**Interfaces:**
- Consumes: `ReportMeta`, `FormMeta`, `FormMetaRepository`, `DynamicDataService` (untuk `getQualifiedTableName`, `validateAndSanitizeSelectQuery`, `resolveSqlKeywords`, dan LOV), `NamedParameterJdbcTemplate`.
- Produces:
  - `static String ReportDataService.resolveBaseQuery(ReportMeta report, FormMeta form, DynamicDataService dyn)` — kembalikan SQL sesuai urutan `dataQuery` → `form.viewTable` → `SELECT * FROM {qualified tableName}`; **pure** (tanpa akses DB selain `getQualifiedTableName`).
  - `List<Map<String,Object>> fetchData(ReportMeta report, Map<String,Object> params)` — jalankan query via `NamedParameterJdbcTemplate` dengan `MapSqlParameterSource`, lalu LOV-enrich.

- [ ] **Step 1: Tulis failing test (resolusi query — pure)**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.service.DynamicDataService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportDataServiceTest {

    private DynamicDataService dynStub() {
        DynamicDataService dyn = mock(DynamicDataService.class);
        when(dyn.getQualifiedTableName("invoice")).thenReturn("dynamic.invoice");
        return dyn;
    }

    @Test
    void prefersCustomDataQuery() {
        ReportMeta r = new ReportMeta();
        r.setDataQuery("SELECT * FROM v_inv WHERE id = :id");
        r.setTableName("invoice");
        FormMeta form = new FormMeta();
        form.setViewTable("v_should_not_be_used");

        String sql = ReportDataService.resolveBaseQuery(r, form, dynStub());
        assertThat(sql).isEqualTo("SELECT * FROM v_inv WHERE id = :id");
    }

    @Test
    void fallsBackToFormViewTable() {
        ReportMeta r = new ReportMeta();
        r.setTableName("invoice");
        FormMeta form = new FormMeta();
        form.setViewTable("SELECT * FROM v_inv");

        String sql = ReportDataService.resolveBaseQuery(r, form, dynStub());
        assertThat(sql).isEqualTo("SELECT * FROM v_inv");
    }

    @Test
    void fallsBackToTableName() {
        ReportMeta r = new ReportMeta();
        r.setTableName("invoice");

        String sql = ReportDataService.resolveBaseQuery(r, null, dynStub());
        assertThat(sql).isEqualTo("SELECT * FROM dynamic.invoice");
    }
}
```

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=ReportDataServiceTest test`
Expected: FAIL (`ReportDataService` belum ada).

- [ ] **Step 3: Implementasi (resolusi + fetch berparameter)**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.components.ComponentFactory;
import com.vaadinerp.service.DynamicDataService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportDataService {

    private final NamedParameterJdbcTemplate npjt;
    private final FormMetaRepository formMetaRepository;
    private final DynamicDataService dynamicDataService;

    public ReportDataService(NamedParameterJdbcTemplate npjt,
                             FormMetaRepository formMetaRepository,
                             DynamicDataService dynamicDataService) {
        this.npjt = npjt;
        this.formMetaRepository = formMetaRepository;
        this.dynamicDataService = dynamicDataService;
    }

    /** Urutan datasource: dataQuery → form.viewTable → SELECT * FROM {qualified tableName}. Pure. */
    public static String resolveBaseQuery(ReportMeta report, FormMeta form, DynamicDataService dyn) {
        if (report.getDataQuery() != null && !report.getDataQuery().trim().isEmpty()) {
            return report.getDataQuery().trim();
        }
        if (form != null && form.getViewTable() != null && !form.getViewTable().trim().isEmpty()) {
            return form.getViewTable().trim();
        }
        if (report.getTableName() != null && !report.getTableName().trim().isEmpty()) {
            return "SELECT * FROM " + dyn.getQualifiedTableName(report.getTableName().trim());
        }
        return null;
    }

    public List<Map<String, Object>> fetchData(ReportMeta report, Map<String, Object> params) {
        FormMeta form = (report.getTableName() != null)
                ? formMetaRepository.findByTableName(report.getTableName()).orElse(null)
                : null;

        String sql = resolveBaseQuery(report, form, dynamicDataService);
        if (sql == null) return new ArrayList<>();

        // resolve keyword ($CURRENT_USER dll) & validasi read-only
        sql = dynamicDataService.validateAndSanitizeSelectQuery(
                dynamicDataService.resolveSqlKeywords(sql));

        // NamedParameterJdbcTemplate menangani cast PostgreSQL '::type' dengan benar (bukan parameter).
        MapSqlParameterSource src = new MapSqlParameterSource();
        if (params != null) params.forEach(src::addValue);

        List<Map<String, Object>> rows = npjt.queryForList(sql, src);
        return enrichLov(report, form, rows);
    }

    /** Tambah kolom {field}_label untuk field ber-LOV pada form terkait. */
    private List<Map<String, Object>> enrichLov(ReportMeta report, FormMeta form,
                                                List<Map<String, Object>> rows) {
        if (form == null || form.getFields() == null || rows.isEmpty()) return rows;
        List<FieldMeta> lovFields = new ArrayList<>();
        for (FieldMeta f : form.getFields()) {
            if (f.getLovCode() != null && !f.getLovCode().trim().isEmpty()) lovFields.add(f);
        }
        if (lovFields.isEmpty()) return rows;

        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> nr = new HashMap<>(row);
            for (FieldMeta f : lovFields) {
                String col = f.getFieldName().toLowerCase();
                if (nr.containsKey(col) && nr.get(col) != null) {
                    nr.put(col + "_label",
                            ComponentFactory.formatFieldValueWithLov(f, nr.get(col), dynamicDataService));
                }
            }
            out.add(nr);
        }
        return out;
    }
}
```

- [ ] **Step 4: Run test — verify PASS**

Run: `mvn -q -Dtest=ReportDataServiceTest test`
Expected: PASS (test hanya menguji `resolveBaseQuery` yang pure).

> `NamedParameterJdbcTemplate` sudah tersedia sebagai bean auto-config Spring Boot (dari `DataSource`). Jika belum, tambahkan `@Bean NamedParameterJdbcTemplate(DataSource ds){ return new NamedParameterJdbcTemplate(ds); }` di kelas config yang ada.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportDataService.java \
        src/test/java/com/vaadinerp/report/ReportDataServiceTest.java
git commit -m "feat: add ReportDataService with safe named params and datasource resolution"
```

---

### Task 6: Interface `ReportRenderer` + tipe konteks/output

**Files:**
- Create: `src/main/java/com/vaadinerp/report/render/ReportContext.java`
- Create: `src/main/java/com/vaadinerp/report/render/ReportOutput.java`
- Create: `src/main/java/com/vaadinerp/report/render/ReportRenderer.java`
- Test: `src/test/java/com/vaadinerp/report/render/ReportOutputTest.java`

**Interfaces:**
- Consumes: —
- Produces:
  - `ReportContext` (record): `String reportCode`, `String engineType`, `java.io.File template`, `List<Map<String,Object>> data`, `Map<String,Object> params`, `String pageSize`, `String orientation`.
  - `ReportOutput` (record): `String contentType`, `byte[] bytes` + static factory `ReportOutput.pdf(byte[])`, `ReportOutput.html(String)`.
  - `ReportRenderer` (interface): `String engine()`, `ReportOutput render(ReportContext ctx)`, `ReportOutput export(ReportContext ctx, String format)`.

- [ ] **Step 1: Tulis failing test**

```java
package com.vaadinerp.report.render;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportOutputTest {
    @Test
    void pdfFactorySetsContentType() {
        ReportOutput o = ReportOutput.pdf(new byte[]{1,2,3});
        assertThat(o.contentType()).isEqualTo("application/pdf");
        assertThat(o.bytes()).hasSize(3);
    }
    @Test
    void htmlFactorySetsContentType() {
        ReportOutput o = ReportOutput.html("<b>x</b>");
        assertThat(o.contentType()).isEqualTo("text/html;charset=UTF-8");
        assertThat(new String(o.bytes(), java.nio.charset.StandardCharsets.UTF_8)).contains("<b>x</b>");
    }
}
```

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=ReportOutputTest test`
Expected: FAIL (tipe belum ada).

- [ ] **Step 3: Buat tipe & interface**

`ReportContext.java`:
```java
package com.vaadinerp.report.render;

import java.io.File;
import java.util.List;
import java.util.Map;

public record ReportContext(
        String reportCode,
        String engineType,
        File template,
        List<Map<String, Object>> data,
        Map<String, Object> params,
        String pageSize,
        String orientation) {}
```

`ReportOutput.java`:
```java
package com.vaadinerp.report.render;

import java.nio.charset.StandardCharsets;

public record ReportOutput(String contentType, byte[] bytes) {
    public static ReportOutput pdf(byte[] b) { return new ReportOutput("application/pdf", b); }
    public static ReportOutput html(String html) {
        return new ReportOutput("text/html;charset=UTF-8", html.getBytes(StandardCharsets.UTF_8));
    }
}
```

`ReportRenderer.java`:
```java
package com.vaadinerp.report.render;

public interface ReportRenderer {
    /** Nilai engineType yang ditangani: STANDARD | STIMULSOFT | JASPER. */
    String engine();
    ReportOutput render(ReportContext ctx);
    ReportOutput export(ReportContext ctx, String format);
}
```

- [ ] **Step 4: Run test — verify PASS**

Run: `mvn -q -Dtest=ReportOutputTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/report/render/ \
        src/test/java/com/vaadinerp/report/render/ReportOutputTest.java
git commit -m "feat: add ReportRenderer interface with context and output types"
```

---

### Task 7: `JasperRenderer` (.jasper run + .jrxml compile+cache) & compile-on-upload

**Files:**
- Create: `src/main/java/com/vaadinerp/report/render/JasperRenderer.java`
- Create: `src/main/java/com/vaadinerp/report/JasperTemplateService.java` (compile-on-upload + cache)
- Test: `src/test/java/com/vaadinerp/report/JasperTemplateServiceTest.java`

**Interfaces:**
- Consumes: `ReportContext`, `ReportOutput`, `ReportRenderer` (Task 6); JasperReports (Task 1).
- Produces:
  - `net.sf.jasperreports.engine.JasperReport JasperTemplateService.loadCompiled(java.io.File template)` — jika `.jasper` → `JRLoader.loadObject`; jika `.jrxml` → `JasperCompileManager.compileReport`; hasil di-cache by `absolutePath + lastModified`.
  - `net.sf.jasperreports.engine.JasperReport JasperTemplateService.compileForUpload(java.io.InputStream jrxml)` — compile; lempar `JRException` bila invalid (dipakai validasi saat upload).
  - `JasperRenderer implements ReportRenderer` dengan `engine()=="JASPER"`.

- [ ] **Step 1: Tulis failing test (compile-on-upload valid & invalid)**

```java
package com.vaadinerp.report;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;

class JasperTemplateServiceTest {

    private static final String VALID_JRXML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" " +
        "name=\"t\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"555\" " +
        "leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\">" +
        "<detail><band height=\"20\"/></detail></jasperReport>";

    @Test
    void compilesValidJrxml() throws Exception {
        JasperTemplateService svc = new JasperTemplateService();
        JasperReport jr = svc.compileForUpload(
                new ByteArrayInputStream(VALID_JRXML.getBytes(StandardCharsets.UTF_8)));
        assertThat(jr).isNotNull();
        assertThat(jr.getName()).isEqualTo("t");
    }

    @Test
    void rejectsInvalidJrxml() {
        JasperTemplateService svc = new JasperTemplateService();
        assertThatThrownBy(() -> svc.compileForUpload(
                new ByteArrayInputStream("<not-jasper/>".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(JRException.class);
    }
}
```

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=JasperTemplateServiceTest test`
Expected: FAIL (`JasperTemplateService` belum ada).

- [ ] **Step 3: Implementasi `JasperTemplateService`**

```java
package com.vaadinerp.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JasperTemplateService {

    private final ConcurrentHashMap<String, JasperReport> cache = new ConcurrentHashMap<>();

    /** Compile .jrxml dari stream (dipakai validasi saat upload). */
    public JasperReport compileForUpload(InputStream jrxml) throws JRException {
        return JasperCompileManager.compileReport(jrxml);
    }

    /** .jasper → load; .jrxml → compile. Hasil di-cache by path+mtime. */
    public JasperReport loadCompiled(File template) throws JRException {
        String key = template.getAbsolutePath() + "#" + template.lastModified();
        JasperReport cached = cache.get(key);
        if (cached != null) return cached;

        JasperReport jr;
        if (template.getName().toLowerCase().endsWith(".jrxml")) {
            jr = JasperCompileManager.compileReport(template.getAbsolutePath());
        } else {
            jr = (JasperReport) JRLoader.loadObject(template);
        }
        cache.put(key, jr);
        return jr;
    }
}
```

- [ ] **Step 4: Run test — verify PASS**

Run: `mvn -q -Dtest=JasperTemplateServiceTest test`
Expected: PASS.

- [ ] **Step 5: Implementasi `JasperRenderer`**

```java
package com.vaadinerp.report.render;

import com.vaadinerp.report.JasperTemplateService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class JasperRenderer implements ReportRenderer {

    private final JasperTemplateService templates;

    public JasperRenderer(JasperTemplateService templates) {
        this.templates = templates;
    }

    @Override public String engine() { return "JASPER"; }

    private JasperPrint fill(ReportContext ctx) throws JRException {
        JasperReport jr = templates.loadCompiled(ctx.template());
        Map<String, Object> params = ctx.params() != null ? new HashMap<>(ctx.params()) : new HashMap<>();
        JRMapCollectionDataSource ds = new JRMapCollectionDataSource(
                ctx.data() != null ? ctx.data() : java.util.List.of());
        return JasperFillManager.fillReport(jr, params, ds);
    }

    @Override
    public ReportOutput render(ReportContext ctx) {
        return export(ctx, "PDF");
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        try {
            JasperPrint print = fill(ctx);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if ("XLSX".equalsIgnoreCase(format)) {
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                exporter.exportReport();
                return new ReportOutput(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
            }
            // default PDF
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            return ReportOutput.pdf(pdf);
        } catch (JRException e) {
            throw new RuntimeException("Gagal render Jasper: " + e.getMessage(), e);
        }
    }
}
```

- [ ] **Step 6: Verifikasi compile modul**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/vaadinerp/report/JasperTemplateService.java \
        src/main/java/com/vaadinerp/report/render/JasperRenderer.java \
        src/test/java/com/vaadinerp/report/JasperTemplateServiceTest.java
git commit -m "feat: add JasperRenderer with compile-on-upload and template cache"
```

---

### Task 8: `StimulsoftRenderer` (refactor logika viewer existing ke renderer)

**Files:**
- Create: `src/main/java/com/vaadinerp/report/render/StimulsoftRenderer.java`
- Modify: `src/main/java/com/vaadinerp/controller/StimulsoftJavaController.java` (pakai `ReportResolver`+`ReportDataService` untuk resolusi template & data; hilangkan duplikasi)
- Test: `src/test/java/com/vaadinerp/report/render/StimulsoftRendererTest.java`

**Interfaces:**
- Consumes: `ReportContext` (template `.mrt`, data), Stimulsoft `StiReport`/`StiSerializeManager`/`StiJsonDatabase`.
- Produces:
  - `StimulsoftRenderer implements ReportRenderer`, `engine()=="STIMULSOFT"`.
  - `static StiReport StimulsoftRenderer.bindData(StiReport report, List<Map<String,Object>> data)` — inject `StiJsonDatabase("DynamicData")` dengan JSON `{DynamicData: data}` lalu `synchronize()`. **Pure-ish** (tak sentuh DB), bisa diuji.

- [ ] **Step 1: Tulis failing test (bindData memasang datasource JSON)**

```java
package com.vaadinerp.report.render;

import com.stimulsoft.report.StiReport;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class StimulsoftRendererTest {

    @Test
    void bindDataAddsSingleJsonDatabaseNamedDynamicData() throws Exception {
        StiReport report = new StiReport();
        List<Map<String, Object>> data = List.of(Map.of("id", 1, "name", "A"));

        StimulsoftRenderer.bindData(report, data);

        assertThat(report.getDictionary().getDatabases().getList()).hasSize(1);
        assertThat(report.getDictionary().getDatabases().get(0).getName()).isEqualTo("DynamicData");
    }
}
```

> Verifikasi API koleksi database Stimulsoft (`getList()`/`get(0)`/`size()`) lewat versi 2026.3.2 yang dipakai controller existing; sesuaikan pemanggilan bila berbeda.

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=StimulsoftRendererTest test`
Expected: FAIL (`StimulsoftRenderer` belum ada).

- [ ] **Step 3: Implementasi `StimulsoftRenderer.bindData` + kelas**

```java
package com.vaadinerp.report.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stimulsoft.report.StiReport;
import com.stimulsoft.report.dictionary.databases.StiJsonDatabase;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StimulsoftRenderer implements ReportRenderer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override public String engine() { return "STIMULSOFT"; }

    public static StiReport bindData(StiReport report, List<Map<String, Object>> data) throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("DynamicData", data != null ? data : List.of());
        String json = MAPPER.writeValueAsString(root);

        report.getDictionary().getDatabases().clear();
        StiJsonDatabase db = new StiJsonDatabase("DynamicData", "");
        db.setJsonData(json);
        report.getDictionary().getDatabases().add(db);
        report.getDictionary().synchronize();
        return report;
    }

    @Override
    public ReportOutput render(ReportContext ctx) {
        // Stimulsoft ditampilkan via web viewer (embed) di layer controller/UI.
        // Renderer ini menyediakan bindData; render server-side ke output tunggal
        // (mis. PDF) diserahkan ke controller viewer yang sudah ada.
        throw new UnsupportedOperationException("Stimulsoft dirender via web viewer (lihat StimulsoftJavaController)");
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        throw new UnsupportedOperationException("Export Stimulsoft via toolbar viewer");
    }
}
```

- [ ] **Step 4: Run test — verify PASS**

Run: `mvn -q -Dtest=StimulsoftRendererTest test`
Expected: PASS.

- [ ] **Step 5: Refactor controller memakai `bindData` + resolver**

Di `StimulsoftJavaController.viewerAction` dan handler designer, ganti blok inline injeksi JSON dengan `StimulsoftRenderer.bindData(report, rawData)`; ganti pembentukan `File` manual dengan `reportResolver.resolveMasterTemplate(code, "STIMULSOFT", null)`. Inject `ReportResolver` dan `ReportDataService` via constructor. Pertahankan perilaku Preview (data sample) dan mapping servlet preview (sudah ada di `StimulsoftConfig`).

- [ ] **Step 6: Verifikasi compile & test modul**

Run: `mvn -q -Dtest=StimulsoftRendererTest test && mvn -q -DskipTests compile`
Expected: PASS + BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/vaadinerp/report/render/StimulsoftRenderer.java \
        src/main/java/com/vaadinerp/controller/StimulsoftJavaController.java \
        src/test/java/com/vaadinerp/report/render/StimulsoftRendererTest.java
git commit -m "feat: add StimulsoftRenderer.bindData and reuse resolver in controller"
```

---

### Task 9: `StandardRenderer` (bungkus render band HTML existing)

**Files:**
- Create: `src/main/java/com/vaadinerp/report/render/StandardRenderer.java`
- Test: `src/test/java/com/vaadinerp/report/render/StandardRendererTest.java`

**Interfaces:**
- Consumes: `ReportContext` (data + params); logika band existing dari `ReportViewerView` (dipindah/di-reuse sebagai util murni).
- Produces:
  - `StandardRenderer implements ReportRenderer`, `engine()=="STANDARD"`.
  - `static String StandardRenderer.renderHtml(List<Map<String,Object>> data, ReportMeta report, List<ReportElementMeta> elements)` — kembalikan HTML "kertas" untuk band; **pure**.

- [ ] **Step 1: Tulis failing test (HTML memuat nilai data)**

```java
package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportMeta;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class StandardRendererTest {

    @Test
    void renderHtmlContainsDetailValues() {
        ReportMeta r = new ReportMeta();
        r.setReportTitle("Daftar Item");
        List<Map<String, Object>> data = List.of(
                Map.of("code", "A1", "name", "Item A"),
                Map.of("code", "B2", "name", "Item B"));

        String html = StandardRenderer.renderHtml(data, r, java.util.List.of());

        assertThat(html).contains("Daftar Item");
        assertThat(html).contains("Item A").contains("Item B");
    }
}
```

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=StandardRendererTest test`
Expected: FAIL (`StandardRenderer` belum ada).

- [ ] **Step 3: Implementasi `StandardRenderer.renderHtml` minimal**

```java
package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMeta;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class StandardRenderer implements ReportRenderer {

    @Override public String engine() { return "STANDARD"; }

    /** Render sederhana: judul + tabel semua kolom. (Band detail dari `elements` menyusul.) */
    public static String renderHtml(List<Map<String, Object>> data, ReportMeta report,
                                    List<ReportElementMeta> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"report-standard\">");
        if (report != null && report.getReportTitle() != null) {
            sb.append("<h2>").append(esc(report.getReportTitle())).append("</h2>");
        }
        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\">");
        if (data != null && !data.isEmpty()) {
            sb.append("<thead><tr>");
            for (String col : data.get(0).keySet()) sb.append("<th>").append(esc(col)).append("</th>");
            sb.append("</tr></thead><tbody>");
            for (Map<String, Object> row : data) {
                sb.append("<tr>");
                for (Object v : row.values()) sb.append("<td>").append(esc(String.valueOf(v))).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody>");
        }
        sb.append("</table></div>");
        return sb.toString();
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public ReportOutput render(ReportContext ctx) {
        return ReportOutput.html(renderHtml(ctx.data(), null, java.util.List.of()));
    }

    @Override
    public ReportOutput export(ReportContext ctx, String format) {
        return render(ctx); // export lanjutan (PDF/Excel) menyusul di Plan 2/berikut
    }
}
```

> Band detail berbasis `elements` (TITLE/HEADER/DETAIL/SUMMARY) dari `ReportViewerView` existing dipindahkan ke sini secara bertahap. Task ini menetapkan fondasi + test; migrasi band penuh boleh jadi task lanjutan bila diperlukan Plan 2.

- [ ] **Step 4: Run test — verify PASS**

Run: `mvn -q -Dtest=StandardRendererTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/report/render/StandardRenderer.java \
        src/test/java/com/vaadinerp/report/render/StandardRendererTest.java
git commit -m "feat: add StandardRenderer HTML output"
```

---

### Task 10: `ReportRendererRegistry` (pilih renderer per engine)

**Files:**
- Create: `src/main/java/com/vaadinerp/report/render/ReportRendererRegistry.java`
- Test: `src/test/java/com/vaadinerp/report/render/ReportRendererRegistryTest.java`

**Interfaces:**
- Consumes: `List<ReportRenderer>` (Spring meng-inject semua implementasi).
- Produces: `ReportRenderer ReportRendererRegistry.forEngine(String engineType)` — kembalikan renderer dengan `engine()` cocok (case-insensitive); lempar `IllegalArgumentException` bila tak ada.

- [ ] **Step 1: Tulis failing test**

```java
package com.vaadinerp.report.render;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ReportRendererRegistryTest {

    private ReportRenderer stub(String engine) {
        return new ReportRenderer() {
            public String engine() { return engine; }
            public ReportOutput render(ReportContext c) { return ReportOutput.html("x"); }
            public ReportOutput export(ReportContext c, String f) { return ReportOutput.html("x"); }
        };
    }

    @Test
    void returnsRendererForKnownEngineCaseInsensitive() {
        ReportRendererRegistry reg = new ReportRendererRegistry(List.of(stub("STANDARD"), stub("JASPER")));
        assertThat(reg.forEngine("jasper").engine()).isEqualTo("JASPER");
    }

    @Test
    void throwsForUnknownEngine() {
        ReportRendererRegistry reg = new ReportRendererRegistry(List.of(stub("STANDARD")));
        assertThatThrownBy(() -> reg.forEngine("STIMULSOFT"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Run test — verify FAIL**

Run: `mvn -q -Dtest=ReportRendererRegistryTest test`
Expected: FAIL.

- [ ] **Step 3: Implementasi registry**

```java
package com.vaadinerp.report.render;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ReportRendererRegistry {

    private final List<ReportRenderer> renderers;

    public ReportRendererRegistry(List<ReportRenderer> renderers) {
        this.renderers = renderers;
    }

    public ReportRenderer forEngine(String engineType) {
        String e = engineType == null ? "STANDARD" : engineType.trim();
        return renderers.stream()
                .filter(r -> r.engine().equalsIgnoreCase(e))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Engine tidak didukung: " + engineType));
    }
}
```

- [ ] **Step 4: Run test — verify PASS**

Run: `mvn -q -Dtest=ReportRendererRegistryTest test`
Expected: PASS.

- [ ] **Step 5: Run seluruh test + compile penuh**

Run: `mvn -q test`
Expected: semua test PASS, BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vaadinerp/report/render/ReportRendererRegistry.java \
        src/test/java/com/vaadinerp/report/render/ReportRendererRegistryTest.java
git commit -m "feat: add ReportRendererRegistry to select renderer by engine"
```

---

## Self-Review Checklist (untuk penulis plan — sudah dijalankan)

- **Spec coverage:** datasource resolution (T5), NamedParameterJdbcTemplate (T5), LOV `_label` (T5), path validation/resolver (T4), ReportParamMeta (T2) + adapter (T3), Renderer×3 + interface (T6-T9), Jasper dua-format + compile-on-upload (T7), registry (T10), Jasper dependency (T1). UI designer, per-user copy, viewer end-user = **Plan 2 / spec berikut** (di luar cakupan, sesuai spec §2).
- **Placeholder scan:** tidak ada TODO/TBD; setiap step berkode nyata. Bagian band-detail Standard sengaja disebut "menyusul" sebagai batasan sadar, bukan placeholder wajib untuk pipeline.
- **Type consistency:** `ReportContext`/`ReportOutput`/`ReportRenderer.engine()` konsisten dipakai T6-T10; `resolveBaseQuery` (T5) dan `bindData` (T8) signature konsisten dengan pemanggilnya.

## Catatan integrasi (bukan unit test)
- Test unit fokus ke logika pure (resolusi query, validasi path, adapter, compile jrxml, bindData, registry) karena `DynamicDataService` sangat terikat PostgreSQL. Verifikasi end-to-end (query nyata, render Stimulsoft/Jasper dengan template & DB nyata) dilakukan manual saat Plan 2 (UI) tersedia, plus regresi fix preview yang sudah terverifikasi.
