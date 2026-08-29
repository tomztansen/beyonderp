# Aturan Proyek Vaadin ERP (vaadinerp)

## Dokumentasi Vaadin — wajib lewat MCP

Untuk pertanyaan apa pun soal **API, komponen, atau styling Vaadin**, konsultasikan
MCP server `vaadin` lebih dulu sebelum menjawab atau menulis kode. Jangan menjawab
dari ingatan.

Tool-nya berstatus *deferred*: namanya terlihat tapi skemanya belum dimuat, jadi
panggil `ToolSearch` dulu, misalnya
`ToolSearch("select:mcp__plugin_vaadin-skills_vaadin__search_vaadin_docs")`,
baru tool-nya bisa dipakai.

Berlaku untuk: perilaku/properti komponen (Grid, ComboBox, Dialog, Upload, dsb.),
API Flow, theming/Lumo, dan pertanyaan spesifik versi. Versi Vaadin proyek ini ada
di `pom.xml` (`vaadin.version`) — sebutkan versinya saat bertanya ke MCP, karena
API berubah antar versi.

Tidak perlu untuk pekerjaan yang tidak menyentuh API Vaadin: logika bisnis, query
SQL, resolusi data report, atau rendering HTML.

## Database

PostgreSQL. Tabel metadata (`meta_*`) ada di schema `public`; tabel data dinamis
di schema `dynamic`. Jangan berasumsi soal isi tabel — periksa dulu, skema dan data
di lingkungan pengembangan berbeda dengan produksi.

## Verifikasi sebelum menyatakan selesai

`mvn -o compile` **tidak** memvalidasi JPQL pada `@Query`; query yang salah baru
gagal saat Spring context naik. Setelah mengubah query repository, jalankan
aplikasinya (atau test yang memuat context) sebelum menyatakan perubahan aman.

## Aturan lain

@.agents/AGENTS.md

## Panduan arsitektur — baca saat relevan

Berkas berikut ada di folder tool lain (`.agents/`, `.mimocode/`) sehingga tidak
dimuat otomatis. Baca file-nya lebih dulu sebelum menggarap area terkait — isinya
arsitektur nyata proyek ini, bukan teori umum.

| Baca saat | File |
|---|---|
| Upload file/gambar, `FileStorageService`, `FileUploadField` | `.agents/skills/vaadin-file-upload/SKILL.md` |
| Extra Toolbar, Action Builder, 2-Stage Copy, DSL Groovy (`ScriptExecutorService`) | `.agents/skills/vaadin-extra-toolbar/SKILL.md` |
| Menelusuri cara kerja sebuah form (metadata → view → komponen) | `.mimocode/skills/form-investigation/SKILL.md` |
| Grid "tambah baris" menampilkan baris kosong / editor tidak terbuka | `.mimocode/skills/vaadin-grid-focus-fix/SKILL.md` |

Catatan untuk `form-investigation`: panduannya menyebut `DataInitializer.java`
sebagai satu-satunya sumber kebenaran definisi form. Itu benar untuk *seed data*,
tetapi saat runtime sumber kebenarannya adalah tabel `meta_form` / `meta_field` di
database — form bisa dibuat lewat Form Builder tanpa menyentuh `DataInitializer`.
Periksa database, bukan hanya kode.

`.agents/skills/spring-boot-3/SKILL.md` **sengaja tidak didaftarkan**: isinya
boilerplate Spring Boot generik, tabel `references/*.md`-nya menunjuk file yang
tidak ada, dan sebagian anjurannya bertentangan dengan proyek ini (melarang Lombok,
padahal entity di sini memakai `@Getter`/`@Setter`).
