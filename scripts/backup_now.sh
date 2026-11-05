#!/bin/bash
# backup_now.sh - backup database grp SESUAI PERMINTAAN (jalankan di server LIVE sebagai user postgres).
#
# Pakai:  sudo -u postgres /usr/local/sbin/backup_now.sh [label]     contoh: ... backup_now.sh sebelum-deploy
# Hasil:  /backup/postgresql/adhoc/grp_YYYY-MM-DD_HHhMMm_<label>.sql.gz   (izin 600)
#
# - Jalan sebagai postgres, BUKAN root: semua operasi file terjadi di folder milik postgres, jadi tidak ada
#   langkah berhak istimewa yang bisa dibelokkan (mis. lewat symlink). Skrip menolak dijalankan sebagai root.
# - Format sama dengan autopostgresqlbackup (CREATE_DATABASE=yes: berisi CREATE DATABASE dan \connect),
#   jadi langsung bisa dipakai refresh_dev.sh.
# - Disimpan di folder terpisah, di luar rotasi/penghapusan otomatis autopostgresqlbackup.
# - pg_dump memakai snapshot konsisten dan tidak mengunci tabel; aman dijalankan saat aplikasi live berjalan.
# - Folder ini satu disk dengan database dan TIDAK dirotasi: hapus file lama sendiri / salin ke tempat lain.
# - File berisi seluruh data live dan kata sandi user mapping FDW: jangan taruh di folder bersama.
#
# Pasang: sudo install -o root -g postgres -m 750 backup_now.sh /usr/local/sbin/backup_now.sh
#         Milik root (tidak bisa diubah user lain), grup postgres supaya postgres boleh menjalankannya.
#         Letakkan di folder milik root seperti /usr/local/sbin, JANGAN di folder milik user aplikasi
#         (mis. /opt/grp milik user grp: user itu bisa mengganti isi skrip yang dijalankan sebagai postgres).

set -euo pipefail
umask 077   # file dan folder baru hanya bisa dibaca pemiliknya sejak detik pertama

DIR="${ADHOC_DIR:-/backup/postgresql/adhoc}"
DB_NAME="grp"
LABEL="${1:-manual}"
RUN_AS="${BACKUP_RUN_AS:-postgres}"   # hanya diubah untuk pengujian
FREE_MARGIN_MB="${FREE_MARGIN_MB:-200}"
TMP=""

die() { echo "ERROR: $*" >&2; exit 1; }

# File sementara selalu dibersihkan, juga saat skrip dihentikan (Ctrl+C, SSH putus, kill).
cleanup() { [ -z "$TMP" ] || rm -f "$TMP"; }
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM HUP

# --- Penjaga ----------------------------------------------------------------------------------
[ "$(id -u)" != "0" ] || die "jangan jalankan sebagai root. Pakai: sudo -u $RUN_AS $0 [label]"
[ "$(id -un)" = "$RUN_AS" ] || die "harus dijalankan sebagai user $RUN_AS. Pakai: sudo -u $RUN_AS $0 [label]"
[[ "$LABEL" =~ ^[A-Za-z0-9_-]{1,40}$ ]] || die "label hanya boleh huruf, angka, _ dan - (maksimal 40 karakter)."
[[ "$DIR" = /* ]] || die "ADHOC_DIR harus jalur absolut."

# --- Folder tujuan ------------------------------------------------------------------------------
[ ! -L "$DIR" ] || die "$DIR adalah symlink, ditolak."
mkdir -p "$DIR"
[ -d "$DIR" ] && [ ! -L "$DIR" ] || die "$DIR bukan folder biasa."
[ "$(stat -c '%u' "$DIR")" = "$(id -u)" ] || die "$DIR bukan milik user $RUN_AS."
chmod 700 "$DIR"

# --- Satu backup pada satu waktu ---------------------------------------------------------------
exec 9> "$DIR/.backup_now.lock"
flock -n 9 || die "backup lain sedang berjalan."

# --- Ruang disk: ukuran database (batas atas, belum terkompresi) + margin ---------------------------
DB_BYTES="$(psql -X -d "$DB_NAME" -tAc "SELECT pg_database_size('$DB_NAME')")" || die "tidak bisa membaca ukuran database."
[[ "$DB_BYTES" =~ ^[0-9]+$ ]] || die "ukuran database tidak valid: '$DB_BYTES'"
AVAIL_BYTES="$(df --output=avail -B1 "$DIR" | tail -n 1 | tr -d ' ')"
NEED_BYTES=$(( DB_BYTES + FREE_MARGIN_MB * 1024 * 1024 ))
[ "$AVAIL_BYTES" -ge "$NEED_BYTES" ] \
    || die "ruang disk tidak cukup: tersedia $((AVAIL_BYTES / 1048576)) MB, dibutuhkan $((NEED_BYTES / 1048576)) MB."

# --- Backup --------------------------------------------------------------------------------------
FILE="$DIR/grp_$(date +%F_%Hh%Mm)_${LABEL}.sql.gz"
[ ! -e "$FILE" ] || die "file sudah ada: $FILE (tunggu semenit atau pakai label lain)."
TMP="$FILE.tmp"

echo "Membackup database $DB_NAME ke $FILE ..."
# pipefail: kegagalan pg_dump membatalkan seluruh perintah; file sementara dibuang oleh trap.
pg_dump -C -d "$DB_NAME" | gzip -6 > "$TMP" || die "pg_dump gagal."

# Uji hasilnya sebelum diberi nama final.
gzip -t "$TMP" || die "hasil backup rusak, dibuang."
zcat "$TMP" | tail -n 5 | grep -q "PostgreSQL database dump complete" || die "hasil backup tidak utuh, dibuang."

mv "$TMP" "$FILE"
TMP=""

echo "Selesai: $FILE ($(du -h "$FILE" | cut -f1))"
echo "Untuk restore ke dev: salin file ini ke server dev lalu jalankan: sudo ./refresh_dev.sh <file>"
