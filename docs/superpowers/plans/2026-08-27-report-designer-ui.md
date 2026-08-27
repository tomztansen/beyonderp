# Report Designer UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bangun Report Designer UI (grid daftar + toolbar + tab editor, editor adaptif per engine, definisi parameter, save-first, dan Preview) di atas pipeline backend dari Plan 1.

**Architecture:** `ReportDesignerView` (Vaadin, `@Route`) meniru pola `GenericFormView` (Grid + toolbar + TabSheet). Logika non-UI diekstrak ke `ReportRunService` (orkestrasi Preview memakai `ReportResolver`/`ReportDataService`/`ReportRendererRegistry`) dan helper pure yang bisa diuji. Parameter di-persist lewat cascade `ReportMeta.params` (pola sama seperti `ReportMeta.elements`). Editor surface adaptif: Standard = band canvas existing, Stimulsoft = IFrame designer existing, Jasper = panel upload.

**Tech Stack:** Java 21, Spring Boot 3.3.0, Vaadin (Flow), Maven, PostgreSQL, JasperReports 6.21.3, Stimulsoft 2026.3.2.

**Spec:** `docs/superpowers/specs/2026-08-27-report-designer-design.md`

## Global Constraints

- Semua dari Plan 1 berlaku (path validation, NamedParameterJdbcTemplate, template file `report_templates/master/`, no BLOB).
- Parameter di-persist via `report.setParams(list)` + tiap `param.setReportMeta(report)` lalu `reportMetaRepository.save(report)` (cascade ALL + orphanRemoval — sama seperti `elements` di `ReportBuilderView.saveReportDefinition`).
- `ReportParamMeta` API: `paramName`, `paramLabel`, `paramType`, `lovCode`, `source` (FORM_FIELD/USER_INPUT/SYSTEM), `sourceKey`, `defaultValue`, `required`, `colOrder`, `reportMeta`.
- **`paramType` menyimpan `componentType` penuh** yang dikenal `ComponentFactory` (TEXTBOX, TEXTAREA, INTEGERFIELD/INT, DECIMAL/NUMERIC, DATE/DATEPICKER, DATETIME, TIME, CHECKBOX, COMBOBOX, LISTBOX, CHOSENBOX, BANDBOX, dll). Komponen LOV-driven (COMBOBOX/LISTBOX/CHOSENBOX/BANDBOX) butuh `lovCode` di-set. Tipe non-filter (SUBFORM_GRID, FILE_UPLOAD, IMAGE_UPLOAD) TIDAK ditawarkan sebagai parameter. `ReportParamAdapter` meneruskan `paramType` apa adanya ke `FieldMeta.componentType` (dengan alias `STRING`/kosong → `TEXT`).
- Komponen parameter dibangun via `ReportParamAdapter.toFieldMeta(...)` → `ComponentFactory.create(fieldMeta, dynamicDataService, updateFn)` + `FormLayoutUtils`.
- Test: JUnit 5 + Mockito + AssertJ. `mvn -q -Dtest=ClassName#method test`. View Vaadin diverifikasi **manual** (jalankan app), langkahnya tercantum di tiap task UI.
- Engine editor surface: **STANDARD** = band canvas (reuse dari `ReportBuilderView`), **STIMULSOFT** = IFrame `/stimulsoft-java/designer?code=`, **JASPER** = panel upload. Save-first: tombol Design aktif hanya setelah report tersimpan.
- **SEMUA teks user-facing dalam Bahasa Inggris** — caption tombol, judul tab/kolom grid, label field & parameter, placeholder, `Notification`, header/isi `ConfirmDialog`, dan semua pesan error/validasi. (Narasi plan boleh Indonesia; string di dalam kode WAJIB Inggris.)
- Dialog parameter WAJIB punya kontrol **Required** (checkbox) yang mengeset `ReportParamMeta.required`; parameter `required` yang kosong saat dijalankan memunculkan validasi "This parameter is required".
- **Query timeout bertingkat (WAJIB):** `ReportDataService` (Plan 1) di-modif menyetel `setQueryTimeout` dari properti `app.report.query-timeout-seconds:30`. Preview memakai timeout pendek (data sample + LIMIT 50); full-run boleh nilai lebih longgar bila disediakan properti terpisah. Saat kena timeout, tangkap `org.springframework.dao.QueryTimeoutException` di `ReportRunService`/Preview → tampilkan pesan ramah (Inggris): *"The report query took too long and was stopped. Please narrow your filter/parameters."* Query runaway dibunuh → koneksi pool cepat kembali → melindungi user lain. Report yang memang berat = **jalur batch (future, lihat spec §14)**, bukan menaikkan timeout global.
- **Eksekusi async (Preview Standard/Jasper):** jalankan `ReportRunService.run` di background executor + `UI.access()` untuk push hasil, dengan `ProgressBar` selama menunggu; UI user tidak freeze. STIMULSOFT tetap via viewer (tak perlu async di sini).
- **Titik ekstensi before/after (no-op):** `ReportRunService.run()` memanggil `beforeRun`/`afterRun` no-op. Belum ada field script / kolom DB; wiring Groovy opsional ditambahkan nanti bila perlu.

---

### Task 1: `ReportRunResult` + `ReportRunService` (orkestrasi Preview)

**Files:**
- Create: `src/main/java/com/vaadinerp/report/ReportRunResult.java`
- Create: `src/main/java/com/vaadinerp/report/ReportRunService.java`
- Test: `src/test/java/com/vaadinerp/report/ReportRunServiceTest.java`

**Interfaces:**
- Consumes: `ReportResolver`, `ReportDataService`, `ReportRendererRegistry` (Plan 1); `ReportMeta`.
- Produces:
  - `ReportRunResult` (record): `boolean stimulsoftViewer`, `String viewerUrl`, `com.vaadinerp.report.render.ReportOutput output` + factory `ReportRunResult.stimulsoft(String url)` dan `ReportRunResult.rendered(ReportOutput out)`.
  - `ReportRunResult ReportRunService.run(ReportMeta report, Map<String,Object> params, boolean sample)` — untuk STIMULSOFT kembalikan `stimulsoft("/stimulsoft-java/viewer?code=" + code + paramQuery)`; selain itu resolve template + fetch data + render via registry → `rendered(output)`.

- [ ] **Step 1: Tulis failing test**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.report.render.*;
import com.vaadinerp.service.DynamicDataService;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportRunServiceTest {

    @Test
    void stimulsoftReturnsViewerUrlWithCode() {
        ReportRunService svc = new ReportRunService(mock(ReportResolver.class),
                mock(ReportDataService.class), mock(ReportRendererRegistry.class));
        ReportMeta r = new ReportMeta();
        r.setReportCode("INV");
        r.setEngineType("STIMULSOFT");

        ReportRunResult res = svc.run(r, Map.of(), false);

        assertThat(res.stimulsoftViewer()).isTrue();
        assertThat(res.viewerUrl()).contains("/stimulsoft-java/viewer?code=INV");
    }

    @Test
    void standardRendersViaRegistry() {
        ReportResolver resolver = mock(ReportResolver.class);
        ReportDataService data = mock(ReportDataService.class);
        ReportRendererRegistry registry = mock(ReportRendererRegistry.class);

        ReportMeta r = new ReportMeta();
        r.setReportCode("PO");
        r.setEngineType("STANDARD");

        when(data.fetchData(eq(r), anyMap())).thenReturn(List.<Map<String,Object>>of());
        ReportRenderer standard = new ReportRenderer() {
            public String engine() { return "STANDARD"; }
            public ReportOutput render(ReportContext c) { return ReportOutput.html("<p>ok</p>"); }
            public ReportOutput export(ReportContext c, String f) { return render(c); }
        };
        when(registry.forEngine("STANDARD")).thenReturn(standard);

        ReportRunResult res = svc.run(resolver, data, registry, r);
        assertThat(res.stimulsoftViewer()).isFalse();
        assertThat(new String(res.output().bytes())).contains("ok");
    }

    // helper agar test kedua ringkas
    private ReportRunResult svcRun(ReportRunService s, ReportMeta r) { return s.run(r, Map.of(), false); }
}
```

> Catatan: sesuaikan test kedua agar memakai konstruktor `new ReportRunService(resolver, data, registry)` lalu `svc.run(r, Map.of(), false)`; contoh di atas menyederhanakan — tulis test kedua dengan instance `ReportRunService` yang di-wire mock seperti test pertama.

- [ ] **Step 2: Run — verify FAIL**

Run: `mvn -q -Dtest=ReportRunServiceTest test`
Expected: FAIL (kelas belum ada).

- [ ] **Step 3: Implementasi `ReportRunResult`**

```java
package com.vaadinerp.report;

import com.vaadinerp.report.render.ReportOutput;

public record ReportRunResult(boolean stimulsoftViewer, String viewerUrl, ReportOutput output) {
    public static ReportRunResult stimulsoft(String url) { return new ReportRunResult(true, url, null); }
    public static ReportRunResult rendered(ReportOutput out) { return new ReportRunResult(false, null, out); }
}
```

- [ ] **Step 4: Implementasi `ReportRunService`**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.report.render.ReportContext;
import com.vaadinerp.report.render.ReportOutput;
import com.vaadinerp.report.render.ReportRenderer;
import com.vaadinerp.report.render.ReportRendererRegistry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class ReportRunService {

    private final ReportResolver resolver;
    private final ReportDataService dataService;
    private final ReportRendererRegistry registry;

    public ReportRunService(ReportResolver resolver, ReportDataService dataService,
                            ReportRendererRegistry registry) {
        this.resolver = resolver;
        this.dataService = dataService;
        this.registry = registry;
    }

    public ReportRunResult run(ReportMeta report, Map<String, Object> params, boolean sample) {
        String engine = report.getEngineType() != null ? report.getEngineType() : "STANDARD";
        beforeRun(report, params); // titik ekstensi (no-op)

        if ("STIMULSOFT".equalsIgnoreCase(engine)) {
            StringBuilder url = new StringBuilder("/stimulsoft-java/viewer?code=").append(report.getReportCode());
            if (params != null) {
                Object id = params.get("id");
                if (id != null) url.append("&id=").append(id);
            }
            return ReportRunResult.stimulsoft(url.toString());
        }

        List<Map<String, Object>> data = dataService.fetchData(report, params);
        File template = "STANDARD".equalsIgnoreCase(engine)
                ? null
                : resolver.resolveMasterTemplate(report.getReportCode(), engine, report.getTemplatePath());
        ReportContext ctx = new ReportContext(report.getReportCode(), engine, template, data, params,
                report.getPageSize(), report.getOrientation());
        ReportRenderer renderer = registry.forEngine(engine);
        ReportOutput out = renderer.render(ctx);
        afterRun(report, params, data.size()); // titik ekstensi (no-op)
        return ReportRunResult.rendered(out);
    }

    /**
     * Titik ekstensi sebelum report dijalankan. No-op sekarang; wiring Groovy opsional
     * (ScriptExecutorService) ditambahkan di sini bila ada kebutuhan konkret — belum ada
     * field script / kolom DB (YAGNI).
     */
    protected void beforeRun(ReportMeta report, Map<String, Object> params) {}

    /** Titik ekstensi setelah report dijalankan (mis. audit). No-op sekarang. */
    protected void afterRun(ReportMeta report, Map<String, Object> params, int rowCount) {}
}
```

- [ ] **Step 5: Run — verify PASS**

Run: `mvn -q -Dtest=ReportRunServiceTest test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportRunResult.java \
        src/main/java/com/vaadinerp/report/ReportRunService.java \
        src/test/java/com/vaadinerp/report/ReportRunServiceTest.java
git commit -m "feat: add ReportRunService orchestrating preview per engine"
```

---

### Task 2: Resolusi auto-parameter (FORM_FIELD / SYSTEM) — helper pure

**Files:**
- Create: `src/main/java/com/vaadinerp/report/ReportParamResolver.java`
- Test: `src/test/java/com/vaadinerp/report/ReportParamResolverTest.java`

**Interfaces:**
- Consumes: `ReportParamMeta`.
- Produces:
  - `static Map<String,Object> ReportParamResolver.resolveAuto(List<ReportParamMeta> params, Map<String,Object> record, String currentUser)` — untuk tiap param: `FORM_FIELD` → ambil `record.get(sourceKey)`; `SYSTEM` + `sourceKey`=`$CURRENT_USER` → `currentUser`; `SYSTEM`+`CURRENT_DATE` → `LocalDate.now()`; lainnya (USER_INPUT) diabaikan (diisi user via form). Kembalikan hanya param yang ter-resolve.
  - `static List<ReportParamMeta> ReportParamResolver.userInputParams(List<ReportParamMeta> params)` — param dengan `source == USER_INPUT`.

- [ ] **Step 1: Tulis failing test**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class ReportParamResolverTest {

    private ReportParamMeta p(String name, String source, String key) {
        ReportParamMeta m = new ReportParamMeta();
        m.setParamName(name); m.setSource(source); m.setSourceKey(key);
        return m;
    }

    @Test
    void resolvesFormFieldFromRecord() {
        Map<String,Object> out = ReportParamResolver.resolveAuto(
                List.of(p("id", "FORM_FIELD", "invoice_id")),
                Map.of("invoice_id", 123), "bob");
        assertThat(out).containsEntry("id", 123);
    }

    @Test
    void resolvesSystemCurrentUser() {
        Map<String,Object> out = ReportParamResolver.resolveAuto(
                List.of(p("u", "SYSTEM", "$CURRENT_USER")), Map.of(), "bob");
        assertThat(out).containsEntry("u", "bob");
    }

    @Test
    void ignoresUserInput() {
        Map<String,Object> out = ReportParamResolver.resolveAuto(
                List.of(p("x", "USER_INPUT", null)), Map.of(), "bob");
        assertThat(out).isEmpty();
    }

    @Test
    void listsUserInputParams() {
        List<ReportParamMeta> ui = ReportParamResolver.userInputParams(
                List.of(p("a","USER_INPUT",null), p("b","FORM_FIELD","k")));
        assertThat(ui).hasSize(1);
        assertThat(ui.get(0).getParamName()).isEqualTo("a");
    }
}
```

- [ ] **Step 2: Run — verify FAIL**

Run: `mvn -q -Dtest=ReportParamResolverTest test`
Expected: FAIL.

- [ ] **Step 3: Implementasi**

```java
package com.vaadinerp.report;

import com.vaadinerp.meta.ReportParamMeta;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public final class ReportParamResolver {

    private ReportParamResolver() {}

    public static Map<String, Object> resolveAuto(List<ReportParamMeta> params,
                                                  Map<String, Object> record, String currentUser) {
        Map<String, Object> out = new HashMap<>();
        if (params == null) return out;
        for (ReportParamMeta p : params) {
            String source = p.getSource() == null ? "USER_INPUT" : p.getSource().trim().toUpperCase();
            if ("FORM_FIELD".equals(source)) {
                if (record != null && p.getSourceKey() != null && record.containsKey(p.getSourceKey())) {
                    out.put(p.getParamName(), record.get(p.getSourceKey()));
                }
            } else if ("SYSTEM".equals(source)) {
                String key = p.getSourceKey() == null ? "" : p.getSourceKey().trim().toUpperCase();
                if (key.equals("$CURRENT_USER")) {
                    out.put(p.getParamName(), currentUser);
                } else if (key.equals("CURRENT_DATE")) {
                    out.put(p.getParamName(), LocalDate.now());
                }
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

- [ ] **Step 4: Run — verify PASS**

Run: `mvn -q -Dtest=ReportParamResolverTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportParamResolver.java \
        src/test/java/com/vaadinerp/report/ReportParamResolverTest.java
git commit -m "feat: add ReportParamResolver for auto (form/system) parameters"
```

---

### Task 3: Generalisasi adapter (semua komponen) + `ReportParameterForm`

**Files:**
- Modify: `src/main/java/com/vaadinerp/report/ReportParamAdapter.java` (passthrough componentType)
- Modify: `src/test/java/com/vaadinerp/report/ReportParamAdapterTest.java`
- Create: `src/main/java/com/vaadinerp/components/ReportParameterForm.java`

**Interfaces:**
- Consumes: `ReportParamMeta`, `ReportParamAdapter`, `ComponentFactory`, `FormLayoutUtils`, `DynamicDataService`.
- Produces:
  - `ReportParamAdapter.resolveComponentType(...)` meneruskan `paramType` apa adanya (mendukung TEXTBOX/COMBOBOX/BANDBOX/CHOSENBOX/LISTBOX/… penuh; `STRING`/kosong → `TEXT`; kosong + ada `lovCode` → `COMBOBOX`).
  - `ReportParameterForm(List<ReportParamMeta> params, DynamicDataService dyn)` — render satu `FormLayout` berisi input untuk tiap param USER_INPUT (via `ReportParamAdapter.toFieldMeta` → `ComponentFactory.create`).
  - `Map<String,Object> collectValues()` — kumpulkan nilai tiap input by `paramName`.

- [ ] **Step 1: Ubah `ReportParamAdapter.resolveComponentType` jadi passthrough**

Ganti isi method menjadi:

```java
public static String resolveComponentType(String paramType, String lovCode) {
    if (paramType != null && !paramType.trim().isEmpty()) {
        String t = paramType.trim().toUpperCase();
        if (t.equals("STRING")) return "TEXT";
        return t; // ComponentFactory mengenali TEXTBOX/COMBOBOX/BANDBOX/CHOSENBOX/LISTBOX/DATE/NUMERIC/CHECKBOX/...
    }
    if (lovCode != null && !lovCode.trim().isEmpty()) return "COMBOBOX";
    return "TEXT";
}
```

- [ ] **Step 2: Perbarui test adapter untuk passthrough**

Ganti test `resolvesComponentTypePerDataType` menjadi mencakup passthrough:

```java
@Test
void passesComponentTypeThrough() {
    assertThat(ReportParamAdapter.resolveComponentType("BANDBOX", "BR")).isEqualTo("BANDBOX");
    assertThat(ReportParamAdapter.resolveComponentType("CHOSENBOX", "BR")).isEqualTo("CHOSENBOX");
    assertThat(ReportParamAdapter.resolveComponentType("DATE", null)).isEqualTo("DATE");
    assertThat(ReportParamAdapter.resolveComponentType("STRING", null)).isEqualTo("TEXT");
    assertThat(ReportParamAdapter.resolveComponentType(null, "BR")).isEqualTo("COMBOBOX");
    assertThat(ReportParamAdapter.resolveComponentType(null, null)).isEqualTo("TEXT");
}
```

Sesuaikan test `mapsLovParamToComboboxWithLovCode`: karena `paramType="LOV"` kini diteruskan apa adanya, ubah ekspektasi `getComponentType()` menjadi `"LOV"` **atau** ganti `setParamType("COMBOBOX")` agar mencerminkan pemakaian nyata (pilih COMBOBOX). Pastikan `lovCode` tetap ter-set di `FieldMeta`.

Run: `mvn -q -Dtest=ReportParamAdapterTest test` → PASS.

- [ ] **Step 3: Bangun komponen `ReportParameterForm`**

```java
package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.ReportParamMeta;
import com.vaadinerp.report.ReportParamAdapter;
import com.vaadinerp.report.ReportParamResolver;
import com.vaadinerp.service.DynamicDataService;

import java.util.*;

public class ReportParameterForm extends VerticalLayout {

    private final Map<String, Component> inputs = new LinkedHashMap<>();

    public ReportParameterForm(List<ReportParamMeta> params, DynamicDataService dyn) {
        setPadding(false);
        setSpacing(false);
        List<ReportParamMeta> userParams = ReportParamResolver.userInputParams(params);

        List<FieldMeta> fields = new ArrayList<>();
        for (ReportParamMeta p : userParams) fields.add(ReportParamAdapter.toFieldMeta(p));

        FormLayout layout = new FormLayout();
        int cols = com.vaadinerp.components.FormLayoutUtils.calculateMaxColsInForm(fields);
        com.vaadinerp.components.FormLayoutUtils.applyResponsiveSteps(layout, Math.max(1, cols));

        for (int i = 0; i < userParams.size(); i++) {
            ReportParamMeta p = userParams.get(i);
            Component input = ComponentFactory.create(fields.get(i), dyn, (k, v) -> {});
            inputs.put(p.getParamName(), input);
            layout.add(input);
        }
        add(layout);
    }

    public Map<String, Object> collectValues() {
        Map<String, Object> out = new HashMap<>();
        for (Map.Entry<String, Component> e : inputs.entrySet()) {
            if (e.getValue() instanceof HasValue<?, ?> hv) {
                out.put(e.getKey(), hv.getValue());
            }
        }
        return out;
    }
}
```

- [ ] **Step 4: Verifikasi compile**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/report/ReportParamAdapter.java \
        src/test/java/com/vaadinerp/report/ReportParamAdapterTest.java \
        src/main/java/com/vaadinerp/components/ReportParameterForm.java
git commit -m "feat: full componentType support in params + dynamic ReportParameterForm"
```

---

### Task 4: `ReportDesignerView` — kerangka grid + toolbar + tab

**Files:**
- Create: `src/main/java/com/vaadinerp/views/ReportDesignerView.java`

**Interfaces:**
- Consumes: `ReportMetaRepository`, `FormMetaRepository`, `DynamicDataService`, `ReportRunService`, `ReportResolver`, `JasperTemplateService`.
- Produces: `@Route("report-designer")` view dengan `Grid<ReportMeta>` + toolbar + `TabSheet` (Daftar/Editor).

- [ ] **Step 1: Bangun kerangka view**

```java
package com.vaadinerp.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;

@Route("report-designer")
public class ReportDesignerView extends VerticalLayout {

    private final ReportMetaRepository reportMetaRepository;
    private final Grid<ReportMeta> grid = new Grid<>(ReportMeta.class, false);
    private final TabSheet tabs = new TabSheet();
    private final VerticalLayout editorTab = new VerticalLayout();

    public ReportDesignerView(ReportMetaRepository reportMetaRepository) {
        this.reportMetaRepository = reportMetaRepository;
        setSizeFull();

        grid.addColumn(ReportMeta::getReportCode).setHeader("Code");
        grid.addColumn(ReportMeta::getReportTitle).setHeader("Title");
        grid.addColumn(r -> r.getEngineType() != null ? r.getEngineType() : "STANDARD").setHeader("Engine");
        grid.addColumn(ReportMeta::getTableName).setHeader("Source");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        refreshGrid();

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.add(
            new com.vaadinerp.components.SafeButton("New", e -> openEditor(null)),
            new com.vaadinerp.components.SafeButton("Edit", e -> withSelected(this::openEditor)),
            new com.vaadinerp.components.SafeButton("Design", e -> withSelected(this::openDesigner)),
            new com.vaadinerp.components.SafeButton("Delete", e -> withSelected(this::deleteReport)),
            new com.vaadinerp.components.SafeButton("Preview", e -> withSelected(this::preview)),
            new com.vaadinerp.components.SafeButton("Refresh", e -> refreshGrid())
        );

        VerticalLayout listTab = new VerticalLayout(toolbar, grid);
        listTab.setSizeFull();
        tabs.add("Report List", listTab);
        tabs.add("Editor", editorTab);
        tabs.setSizeFull();
        add(tabs);
    }

    private void refreshGrid() { grid.setItems(reportMetaRepository.findAll()); }

    private void withSelected(java.util.function.Consumer<ReportMeta> action) {
        ReportMeta sel = grid.asSingleSelect().getValue();
        if (sel == null) {
            com.vaadin.flow.component.notification.Notification.show("Please select a report first.");
            return;
        }
        action.accept(sel);
    }

    // Diisi di task berikutnya:
    private void openEditor(ReportMeta report) { tabs.setSelectedIndex(1); }
    private void openDesigner(ReportMeta report) { /* Task 5 */ }
    private void deleteReport(ReportMeta report) { /* Task 5 */ }
    private void preview(ReportMeta report) { /* Task 6 */ }
}
```

- [ ] **Step 2: Verifikasi compile**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Verifikasi manual (jalankan app)**

Restart app, buka `/report-designer`. Expected: tab "Daftar Report" menampilkan grid report (Code/Title/Engine/Source) + toolbar; tab "Editor" kosong; "Pilih report dulu" muncul bila klik aksi tanpa seleksi.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/vaadinerp/views/ReportDesignerView.java
git commit -m "feat: add ReportDesignerView skeleton (grid + toolbar + tabs)"
```

---

### Task 5: Editor metadata + parameter, save-first, hapus, dan Desain

**Files:**
- Modify: `src/main/java/com/vaadinerp/views/ReportDesignerView.java`

**Interfaces:**
- Consumes: `ReportParamMeta`, `ReportResolver`; cascade save via `ReportMeta.setParams` + `reportMetaRepository.save`.
- Produces: implementasi `openEditor`, `openDesigner`, `deleteReport` + form metadata + grid parameter (add/edit/hapus).

- [ ] **Step 1: Bangun form metadata + parameter grid di `editorTab`**

Isi `openEditor(ReportMeta)`: bersihkan `editorTab`, tambahkan `FormLayout` metadata (`reportCode` [readonly bila edit], `reportTitle`, `tableName`/source, `dataQuery` TextArea, `pageSize`, `orientation`, `engineType` Select STANDARD/STIMULSOFT/JASPER) + `Grid<ReportParamMeta>` parameter dengan tombol "Tambah Parameter" (Dialog berisi: `paramName`, `paramLabel`, `paramType` Select berisi komponen input penuh — **TEXTBOX, TEXTAREA, INTEGERFIELD, DECIMAL, DATE, DATETIME, TIME, CHECKBOX, COMBOBOX, LISTBOX, CHOSENBOX, BANDBOX** (kecualikan SUBFORM_GRID/FILE_UPLOAD/IMAGE_UPLOAD), `lovCode` (wajib untuk COMBOBOX/LISTBOX/CHOSENBOX/BANDBOX), `source` Select FORM_FIELD/USER_INPUT/SYSTEM, `sourceKey`, `defaultValue`, `required`). Simpan daftar param ke state view. Tombol **Simpan**:

```java
ReportMeta rep = (editingCode == null) ? new ReportMeta() : reportMetaRepository.findById(editingCode).orElse(new ReportMeta());
rep.setReportCode(codeField.getValue().trim());
rep.setReportTitle(titleField.getValue().trim());
rep.setTableName(sourceField.getValue());
rep.setDataQuery(queryArea.getValue().isBlank() ? null : queryArea.getValue());
rep.setPageSize(pageSelect.getValue());
rep.setOrientation(orientSelect.getValue());
rep.setEngineType(engineSelect.getValue());

if (rep.getParams() == null) rep.setParams(new java.util.ArrayList<>());
rep.getParams().clear();                       // orphanRemoval hapus yang lama
for (int i = 0; i < paramState.size(); i++) {
    ReportParamMeta p = paramState.get(i);
    p.setReportMeta(rep);                       // set sisi pemilik relasi
    p.setColOrder(i + 1);
    rep.getParams().add(p);
}
reportMetaRepository.save(rep);                 // cascade persist params
editingCode = rep.getReportCode();
com.vaadin.flow.component.notification.Notification.show("Report saved.");
refreshGrid();
enableDesignerButtons(true);                    // save-first: Design enabled after save
```

> Pola ini identik dengan `ReportBuilderView.saveReportDefinition` untuk `elements`. Reuse Select page/orientation/engine dari sana.

- [ ] **Step 2: Implementasi `deleteReport`**

```java
com.vaadin.flow.component.confirmdialog.ConfirmDialog dlg = new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
dlg.setHeader("Delete Report");
dlg.setText("Delete report " + report.getReportCode() + " including its template and parameters?");
dlg.setConfirmText("Delete");
dlg.setCancelable(true);
dlg.addConfirmListener(e -> {
    reportMetaRepository.deleteById(report.getReportCode());   // cascade deletes params/elements
    // hapus file template master bila ada
    try {
        if (report.getEngineType() != null && !"STANDARD".equalsIgnoreCase(report.getEngineType())) {
            java.io.File f = reportResolver.resolveMasterTemplate(
                    report.getReportCode(), report.getEngineType(), report.getTemplatePath());
            if (f.exists()) f.delete();
        }
    } catch (Exception ignored) {}
    refreshGrid();
    com.vaadin.flow.component.notification.Notification.show("Report deleted.");
});
dlg.open();
```

- [ ] **Step 3: Implementasi `openDesigner`** (engine-adaptif akan dilengkapi Task 6; untuk sekarang arahkan Stimulsoft ke IFrame designer existing)

```java
tabs.setSelectedIndex(1);
if ("STIMULSOFT".equalsIgnoreCase(report.getEngineType())) {
    com.vaadin.flow.component.html.IFrame ifr = new com.vaadin.flow.component.html.IFrame(
            "/stimulsoft-java/designer?code=" + report.getReportCode());
    ifr.setSizeFull();
    editorTab.add(ifr);
}
```

- [ ] **Step 4: Verifikasi compile + manual**

Run: `mvn -q -DskipTests compile` → BUILD SUCCESS.
Manual: restart app → `/report-designer` → **Baru** → isi metadata + tambah 1 parameter → **Simpan** → report muncul di grid, tombol Desain aktif. **Hapus** → konfirmasi → hilang dari grid. Untuk report STIMULSOFT tersimpan → **Desain** → IFrame designer muncul & Preview di dalamnya jalan (fix Plan 0 sudah beres).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/vaadinerp/views/ReportDesignerView.java
git commit -m "feat: report editor with metadata, parameter grid, save-first, delete"
```

---

### Task 6: Editor Jasper (upload) + Preview per engine

**Files:**
- Modify: `src/main/java/com/vaadinerp/views/ReportDesignerView.java`
- Create: `src/main/java/com/vaadinerp/report/JasperUploadService.java`
- Test: `src/test/java/com/vaadinerp/report/JasperUploadServiceTest.java`

**Interfaces:**
- Consumes: `JasperTemplateService`, `ReportResolver`, `ReportRunService`.
- Produces:
  - `void JasperUploadService.saveUpload(String code, String filename, byte[] bytes)` — bila `.jrxml` → validasi `compileForUpload` (lempar bila invalid) → simpan ke `resolveMasterTemplate(code, "JASPER", filename)`; bila `.jasper` → simpan langsung. Buat folder bila belum ada.
  - Preview: `preview(ReportMeta)` memakai `ReportRunService.run` → STIMULSOFT buka viewer URL (`UI.getPage().open` / IFrame dialog), STANDARD tampilkan HTML di `Dialog`, JASPER embed PDF (base64 data URI di `<object>`).

- [ ] **Step 1: Tulis failing test (upload Jasper simpan + validasi)**

```java
package com.vaadinerp.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;

class JasperUploadServiceTest {

    private static final String VALID_JRXML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" " +
        "name=\"t\" pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"555\" " +
        "leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\">" +
        "<detail><band height=\"20\"/></detail></jasperReport>";

    private JasperUploadService svc(String uploadDir) {
        ReportResolver r = new ReportResolver(null);
        r.setUploadDirForTest(uploadDir);
        return new JasperUploadService(new JasperTemplateService(), r);
    }

    @Test
    void savesValidJrxmlToMasterPath(@TempDir Path dir) throws Exception {
        JasperUploadService s = svc(dir.toString());
        s.saveUpload("INV", "report.jrxml", VALID_JRXML.getBytes(StandardCharsets.UTF_8));
        File f = new File(dir.toFile(), "report_templates/master/INV.jrxml");
        assertThat(f).exists();
    }

    @Test
    void rejectsInvalidJrxml(@TempDir Path dir) {
        JasperUploadService s = svc(dir.toString());
        assertThatThrownBy(() -> s.saveUpload("INV", "bad.jrxml",
                "<nope/>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(RuntimeException.class);
    }
}
```

- [ ] **Step 2: Run — verify FAIL**

Run: `mvn -q -Dtest=JasperUploadServiceTest test`
Expected: FAIL.

- [ ] **Step 3: Implementasi `JasperUploadService`**

```java
package com.vaadinerp.report;

import net.sf.jasperreports.engine.JRException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

@Service
public class JasperUploadService {

    private final JasperTemplateService templates;
    private final ReportResolver resolver;

    public JasperUploadService(JasperTemplateService templates, ReportResolver resolver) {
        this.templates = templates;
        this.resolver = resolver;
    }

    public void saveUpload(String code, String filename, byte[] bytes) {
        String lower = filename == null ? "" : filename.toLowerCase();
        if (lower.endsWith(".jrxml")) {
            try {
                templates.compileForUpload(new ByteArrayInputStream(bytes)); // early validation
            } catch (JRException e) {
                throw new RuntimeException("Invalid .jrxml file: " + e.getMessage(), e);
            }
        }
        File target = resolver.resolveMasterTemplate(code, "JASPER", filename);
        try {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            Files.write(target.toPath(), bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save template: " + e.getMessage(), e);
        }
    }
}
```

- [ ] **Step 4: Run — verify PASS**

Run: `mvn -q -Dtest=JasperUploadServiceTest test`
Expected: PASS.

- [ ] **Step 5: Tambah panel Upload (Jasper) + implementasi `preview` di view**

Di `openDesigner`, cabang JASPER: tampilkan `com.vaadin.flow.component.upload.Upload` + `MemoryBuffer`; pada `addSucceededListener` panggil `jasperUploadService.saveUpload(code, fileName, buffer.getInputStream().readAllBytes())` lalu `Notification`. Implementasi `preview`:

```java
ReportRunResult res = reportRunService.run(report,
        ReportParamResolver.resolveAuto(report.getParams(), java.util.Map.of(), currentUsername()), true);
if (res.stimulsoftViewer()) {
    com.vaadin.flow.component.UI.getCurrent().getPage().open(res.viewerUrl(), "_blank");
} else {
    com.vaadin.flow.component.dialog.Dialog d = new com.vaadin.flow.component.dialog.Dialog();
    d.setWidth("80vw"); d.setHeight("80vh");
    if (res.output().contentType().startsWith("text/html")) {
        d.add(new com.vaadin.flow.component.Html(
                "<div>" + new String(res.output().bytes(), java.nio.charset.StandardCharsets.UTF_8) + "</div>"));
    } else { // PDF
        String b64 = java.util.Base64.getEncoder().encodeToString(res.output().bytes());
        com.vaadin.flow.component.html.IFrame ifr = new com.vaadin.flow.component.html.IFrame(
                "data:application/pdf;base64," + b64);
        ifr.setSizeFull();
        d.add(ifr);
    }
    d.open();
}
```

> `currentUsername()`: ambil dari `SessionSecurityService.getCurrentUser().getUsername()` (pola yang sudah dipakai `DynamicDataService`). Inject service atau ambil via `SpringContextHolder`.
>
> **Async (Standard/Jasper):** bungkus pemanggilan `reportRunService.run(...)` yang me-render (cabang non-Stimulsoft) dalam background executor; tampilkan `ProgressBar` + disable tombol, lalu tampilkan hasil via `ui.access(() -> { ... d.open(); })`. Ambil `UI ui = UI.getCurrent()` sebelum masuk thread. Cabang STIMULSOFT (buka viewer URL) tetap sinkron. Ini mencegah UI freeze saat render berat; query timeout (lihat Global Constraints) tetap membebaskan koneksi bila query menggantung.

- [ ] **Step 6: Verifikasi compile + manual**

Run: `mvn -q -DskipTests compile` → BUILD SUCCESS.
Manual: report JASPER → **Desain** → upload `.jasper`/`.jrxml` → tersimpan (upload `.jrxml` rusak ditolak dengan pesan). **Preview**: STANDARD → HTML di dialog; JASPER → PDF embed; STIMULSOFT → tab viewer.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/vaadinerp/report/JasperUploadService.java \
        src/test/java/com/vaadinerp/report/JasperUploadServiceTest.java \
        src/main/java/com/vaadinerp/views/ReportDesignerView.java
git commit -m "feat: Jasper upload (compile-on-upload) and engine-aware preview"
```

---

### Task 7: Verifikasi menyeluruh + rapikan

**Files:**
- (verifikasi) semua.

- [ ] **Step 1: Jalankan seluruh test**

Run: `mvn test`
Expected: semua PASS (termasuk test Plan 1 + Plan 2), BUILD SUCCESS.

- [ ] **Step 2: Verifikasi manual end-to-end (jalankan app)**

Restart app. Untuk tiap engine, buat report → simpan → desain/upload → preview:
- STANDARD: metadata + parameter → Preview → HTML tampil dengan data terfilter parameter.
- STIMULSOFT: Simpan → Desain (IFrame) → drag komponen → Preview di designer jalan.
- JASPER: upload `.jasper` → Preview → PDF.

- [ ] **Step 3: Commit (bila ada perbaikan)**

```bash
git add -A && git commit -m "chore: report designer end-to-end verification fixes"
```

---

## Self-Review Checklist (sudah dijalankan)

- **Spec coverage:** UI grid+toolbar+tab (T4), metadata+parameter+save-first (T5), editor adaptif + Jasper upload (T6), Preview per engine + parameter dinamis (T1/T3/T6), auto-param FORM_FIELD/SYSTEM (T2). Datasource/LOV/renderer dari Plan 1 dipakai via `ReportRunService`. Copy-on-edit per-user & viewer end-user tetap di luar cakupan (spec §2).
- **Placeholder scan:** langkah UI memberi kode konkret + verifikasi manual eksplisit (bukan "implement later"). Tidak ada TODO menggantung.
- **Type consistency:** `ReportRunResult`/`ReportRunService.run` (T1) dipakai konsisten di T6; `ReportParamResolver` (T2) dipakai T3/T6; `ReportParamMeta` API (paramName/paramLabel/paramType/lovCode/source/sourceKey/required/colOrder) konsisten dengan Plan 1 (pasca-fix relasi).

## Catatan
- View Vaadin diuji **manual** (project belum punya Karibu-Testing). Logika inti (orkestrasi, resolusi param, upload Jasper) di-unit-test.
- Parameter & template dihapus via cascade `ReportMeta` + hapus file master di `deleteReport`.
