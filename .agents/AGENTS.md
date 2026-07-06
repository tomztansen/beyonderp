# Aturan Pengembangan Proyek Vaadin ERP (vaadinerp)

## 1. Arsitektur File Upload & Penyimpanan Dokumen
- **Separate Storage & DB Reference**: Jangan pernah menyimpan data binary file (BLOB/BYTEA) langsung ke dalam tabel database. Simpan file fisik di disk server (folder `./uploads` atau dikonfigurasi via `app.upload.dir`) dan simpan hanya nama file atau *relative path*-nya di kolom database bertipe `TEXT` atau `VARCHAR`.
- **Sanitasi & Penamaan UUID Aman**: Semua file yang diunggah wajib melewati sanitasi (diubah ke huruf kecil / *lowercase*, spasi dan karakter khusus diganti *underscore* `_`) dan diberi awalan 8 karakter acak UUID (contoh: `a1b2c3d4_faktur_pajak.pdf`). Ini wajib untuk mencegah penimpaan file (*overwrite*) dan menjamin kompatibilitas 100% antara Windows (*case-insensitive*) dan Linux (*case-sensitive*).
- **Multi-File Delimited String**: Untuk field yang mendukung pengunggahan banyak file sekaligus, simpan daftar nama file sebagai string terpisah koma (`file1.pdf,file2.jpg`) pada kolom bertipe `TEXT` di PostgreSQL.
- **Cascade Delete File Fisik**: Saat menghapus record database melalui `DynamicDataService.deleteData` (baik data tunggal, master-detail, maupun subform), wajib memeriksa dan menghapus file fisik terkait di server untuk mencegah penumpukan file sampah (*disk space leak*).
