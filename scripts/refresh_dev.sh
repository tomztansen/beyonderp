#!/bin/bash
# refresh_dev.sh - segarkan database DEV dari file backup LIVE (autopostgresqlbackup / backup_now.sh).
#
# HANYA UNTUK SERVER DEV. Skrip menolak berjalan kalau /etc/grp_env tidak berisi "DEV".
# Dev hasil clone punya hostname sama dengan produksi, jadi hostname TIDAK bisa dipakai sebagai pengaman.
#
# Pakai:  sudo ./refresh_dev.sh [-y] /path/grp_YYYY-MM-DD_HHhMMm.Hari.sql.gz
#   -y   lewati pertanyaan konfirmasi
#
# Langkah: cek penjaga -> cek file backup -> hentikan aplikasi -> hapus & restore database grp
#          -> dev_neutralize.sql (pg_cron, antrean email/WA, FDW baca-saja) -> ringkasan.
# Aplikasi TIDAK dinyalakan otomatis: periksa dulu app.environment dan app.scheduler.enabled.
#
# Opsional (kredensial FDW baca-saja, izin 600, milik root):  /opt/grp/dev_fdw_readonly.env
#   MYSQL_RO_USER=...  MYSQL_RO_PASS=...  SQLSERVER_RO_USER=...  SQLSERVER_RO_PASS=...
# Tanpa file ini, semua user mapping FDW dihapus (dev tidak bisa menyentuh server remote).

set -euo pipefail

APP_SERVICE="${APP_SERVICE:-grp}"
APP_PROPS="${APP_PROPS:-/opt/grp/application-prod.properties}"
ENV_MARKER="${ENV_MARKER:-/etc/grp_env}"
FDW_ENV="${FDW_ENV:-/opt/grp/dev_fdw_readonly.env}"
DB_NAME="grp"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NEUTRALIZE_SQL="${NEUTRALIZE_SQL:-$HERE/dev_neutralize.sql}"

die() { echo "ERROR: $*" >&2; exit 1; }
pg()  { sudo -u postgres "$@"; }

ASSUME_YES=0
if [ "${1:-}" = "-y" ]; then ASSUME_YES=1; shift; fi
FILE="${1:-}"
[ -n "$FILE" ] || die "pakai: sudo $0 [-y] /path/file-backup.sql.gz"

# --- 1. Penjaga: harus root dan server ini harus bertanda DEV -------------------------------
[ "$(id -u)" = "0" ] || die "jalankan sebagai root (sudo)."
[ -r "$ENV_MARKER" ] || die "$ENV_MARKER tidak ada. Buat dengan: echo DEV > $ENV_MARKER (HANYA di server dev)."
[ "$(tr -d '[:space:]' < "$ENV_MARKER")" = "DEV" ] || die "$ENV_MARKER tidak berisi DEV. Ini bukan server dev, dibatalkan."

# Konfigurasi aplikasi hasil clone sering masih sama dengan produksi.
if [ -r "$APP_PROPS" ]; then
    grep -qE '^[[:space:]]*app\.environment[[:space:]]*=[[:space:]]*PROD[[:space:]]*$' "$APP_PROPS" \
        && die "$APP_PROPS masih berisi app.environment=PROD. Ubah ke DEV dulu."
    grep -qE '^[[:space:]]*app\.scheduler\.enabled[[:space:]]*=[[:space:]]*true' "$APP_PROPS" \
        && echo "PERINGATAN: app.scheduler.enabled=true di $APP_PROPS. Ubah ke false sebelum menyalakan aplikasi dev."
else
    echo "PERINGATAN: $APP_PROPS tidak terbaca, konfigurasi aplikasi tidak diperiksa."
fi

# --- 2. File backup utuh ---------------------------------------------------------------------
[ -f "$FILE" ] || die "file tidak ditemukan: $FILE"
[ -f "$NEUTRALIZE_SQL" ] || die "tidak ada $NEUTRALIZE_SQL"
echo "Memeriksa file backup..."
gzip -t "$FILE" || die "file gzip rusak atau terpotong."
zcat "$FILE" | tail -n 5 | grep -q "PostgreSQL database dump complete" \
    || die "file tidak berakhir dengan 'PostgreSQL database dump complete' (kemungkinan terpotong)."

# --- 3. Konfirmasi --------------------------------------------------------------------------
if [ "$ASSUME_YES" != "1" ]; then
    echo
    echo "Database '$DB_NAME' di server DEV ini ($(hostname)) akan DIHAPUS dan diganti dari:"
    echo "  $FILE ($(stat -c '%y' "$FILE" | cut -d. -f1))"
    read -r -p "Ketik '$DB_NAME' untuk melanjutkan: " ANSWER
    [ "$ANSWER" = "$DB_NAME" ] || die "dibatalkan."
fi

# --- 4. Hentikan aplikasi, hapus database lama ------------------------------------------------
if systemctl is-active --quiet "$APP_SERVICE" 2>/dev/null; then
    echo "Menghentikan $APP_SERVICE..."
    systemctl stop "$APP_SERVICE"
fi

# Peluncur pg_cron selalu terhubung ke database grp dan bisa menghalangi DROP; coba beberapa kali.
dropped=0
for i in 1 2 3 4 5; do
    if pg dropdb --if-exists --force "$DB_NAME"; then dropped=1; break; fi
    echo "dropdb gagal (percobaan $i/5), mencoba lagi..."; sleep 2
done
[ "$dropped" = "1" ] || die "database $DB_NAME tidak bisa dihapus (masih ada koneksi?). Hentikan PostgreSQL dev sebentar lalu ulangi."

# --- 5. Restore (berhenti di error pertama) ---------------------------------------------------
# File dari autopostgresqlbackup (CREATE_DATABASE=yes) sudah berisi CREATE DATABASE (encoding SQL_ASCII
# terjaga) dan \connect, jadi dijalankan lewat database 'postgres'.
echo "Restore dimulai..."
zcat "$FILE" | pg psql -X -q -d postgres -v ON_ERROR_STOP=1 > /dev/null \
    || die "restore GAGAL. Database $DB_NAME mungkin setengah jadi; perbaiki penyebabnya lalu jalankan ulang skrip ini."

# --- 6. Netralisasi ---------------------------------------------------------------------------
if [ -r "$FDW_ENV" ]; then
    mode="$(stat -c '%a' "$FDW_ENV")"
    [ "$mode" = "600" ] || echo "PERINGATAN: izin $FDW_ENV adalah $mode, seharusnya 600."
    set -a; . "$FDW_ENV"; set +a
else
    echo "Catatan: $FDW_ENV tidak ada, semua user mapping FDW akan dihapus."
fi
echo "Menjalankan netralisasi..."
sudo --preserve-env=MYSQL_RO_USER,MYSQL_RO_PASS,SQLSERVER_RO_USER,SQLSERVER_RO_PASS -u postgres \
    psql -X -d "$DB_NAME" -v ON_ERROR_STOP=1 -f "$NEUTRALIZE_SQL" \
    || die "netralisasi GAGAL. JANGAN nyalakan aplikasi dev sebelum ini beres."

# --- 7. Ringkasan untuk dibandingkan dengan produksi -----------------------------------------------
echo
echo "Jumlah objek (bandingkan dengan produksi):"
pg psql -X -d "$DB_NAME" -P pager=off -c "
SELECT 'tables' AS jenis, count(*) FROM pg_tables WHERE schemaname IN ('public','dynamic')
UNION ALL SELECT 'indexes', count(*) FROM pg_indexes WHERE schemaname IN ('public','dynamic')
UNION ALL SELECT 'views', count(*) FROM pg_views WHERE schemaname IN ('public','dynamic')
UNION ALL SELECT 'sequences', count(*) FROM pg_sequences WHERE schemaname IN ('public','dynamic')
UNION ALL SELECT 'functions+procedures', count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace WHERE n.nspname IN ('public','dynamic')
UNION ALL SELECT 'triggers', count(*) FROM pg_trigger t JOIN pg_class c ON c.oid = t.tgrelid JOIN pg_namespace n ON n.oid = c.relnamespace WHERE NOT t.tgisinternal AND n.nspname IN ('public','dynamic');"

echo
echo "SELESAI. Aplikasi dev belum dinyalakan. Sebelum menyalakan, pastikan di $APP_PROPS:"
echo "  app.environment=DEV, app.scheduler.enabled=false, email/WhatsApp nonaktif."
echo "Lalu: systemctl start $APP_SERVICE"
