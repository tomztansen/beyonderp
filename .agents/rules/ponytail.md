# Ponytail — aturan kemalasan yang disiplin

Malas = efisien, bukan ceroboh. Kode terbaik adalah kode yang tidak ditulis.

## Tangga — berhenti di anak tangga pertama yang cukup

1. Apakah ini perlu ada? Kebutuhan spekulatif = lewati, katakan alasannya satu baris.
2. Sudah ada di codebase ini? Helper/util/pattern yang sudah hidup di sini → pakai ulang.
3. Cukup dengan stdlib / JDK? Pakai.
4. Fitur bawaan framework (Vaadin, Spring) sudah menutupi? Pakai itu.
5. Dependency yang sudah terpasang bisa? Pakai. Jangan tambah dependency baru.
6. Bisa satu baris? Satu baris.
7. Baru setelah itu: kode minimum yang jalan.

## Larangan

- Tanpa abstraksi yang tidak diminta: interface dengan satu implementasi, factory
  untuk satu produk, config untuk nilai yang tidak pernah berubah.
- Tanpa scaffolding "untuk nanti".
- Hapus lebih baik daripada tambah. Membosankan lebih baik daripada pintar.
- Diff terpendek menang — tapi hanya setelah paham masalahnya.

## Jangan pernah dimalaskan

Validasi input, error handling yang mencegah kehilangan data, keamanan,
aksesibilitas, dan apa pun yang diminta eksplisit.

## Bug

Perbaiki akar masalah, bukan gejala. Grep semua pemanggil fungsi sebelum mengedit.
Satu guard di fungsi bersama lebih kecil daripada guard di setiap pemanggil.

## Output

Kode dulu. Maksimal tiga baris penjelasan. Kalau penjelasan lebih panjang
daripada kodenya, hapus penjelasannya.
