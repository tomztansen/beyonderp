#!/bin/bash
# backup_for_dev.sh - dijalankan di server LIVE lewat SSH key khusus milik server dev (forced command).
#
# Membuat backup baru dengan backup_now.sh, mengalirkan isinya ke stdout (ditangkap oleh
# pull_live_refresh_dev.sh di dev), lalu MENGHAPUS file backup yang baru dibuat itu.
# Database live hanya dibaca (pg_dump); backup lain di /backup/postgresql tidak disentuh.
#
# Pasang (di live, sebagai root):
#   install -o root -g postgres -m 750 backup_for_dev.sh /usr/local/sbin/backup_for_dev.sh
# Lalu di /var/lib/postgresql/.ssh/authorized_keys (milik postgres, izin 600), satu baris:
#   restrict,from="172.16.0.151",command="/usr/local/sbin/backup_for_dev.sh" ssh-ed25519 AAAA... dev-pull-live-backup
#
# stdout HANYA berisi data backup; semua pesan ke stderr supaya file di dev tidak tercemar.

set -euo pipefail
umask 077

BACKUP_NOW="${BACKUP_NOW:-/usr/local/sbin/backup_now.sh}"
ADHOC_DIR="${ADHOC_DIR:-/backup/postgresql/adhoc}"
LABEL="devrefresh"
FILE=""

die() { echo "ERROR: $*" >&2; exit 1; }

# File yang dibuat untuk dev selalu dibuang, juga saat dev memutus koneksi di tengah jalan.
cleanup() { [ -z "$FILE" ] || rm -f "$FILE"; }
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM HUP PIPE

[ "$(id -un)" = "postgres" ] || die "harus berjalan sebagai user postgres (pasang key di authorized_keys milik postgres)."
[ -x "$BACKUP_NOW" ] || die "$BACKUP_NOW tidak ada atau tidak bisa dijalankan."

# Perintah yang dikirim klien (SSH_ORIGINAL_COMMAND) sengaja diabaikan: key ini hanya bisa membuat backup.
OUT="$("$BACKUP_NOW" "$LABEL" 2>&1)" || die "backup_now gagal: $OUT"
echo "$OUT" >&2

# backup_now.sh menutup dengan: "Selesai: <file> (<ukuran>)"
FILE="$(printf '%s\n' "$OUT" | sed -n 's/^Selesai: \(.*\.sql\.gz\) (.*$/\1/p' | tail -n 1)"
[[ "$FILE" =~ ^${ADHOC_DIR}/grp_[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}h[0-9]{2}m_${LABEL}\.sql\.gz$ ]] \
    || { FILE=""; die "nama file backup tidak dikenali: '$OUT'"; }
[ -f "$FILE" ] && [ ! -L "$FILE" ] || die "file backup tidak ditemukan: $FILE"

cat "$FILE"
echo "Terkirim ke dev: $FILE (dihapus dari live setelah ini)" >&2
