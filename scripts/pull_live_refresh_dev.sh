#!/bin/bash
# pull_live_refresh_dev.sh - di SERVER DEV: minta backup baru dari live, salin ke dev, lalu jalankan refresh_dev.sh.
#
# Pakai:  sudo ./pull_live_refresh_dev.sh [-y]
#   -y   diteruskan ke refresh_dev.sh (lewati konfirmasi ketik 'grp')
#
# Taruh di folder yang sama dengan refresh_dev.sh dan dev_neutralize.sql.
# Butuh SSH key khusus (tanpa passphrase, milik root, izin 600) yang di live hanya boleh menjalankan
# /usr/local/sbin/backup_for_dev.sh. Database live hanya dibaca; file backup sementara di live dihapus otomatis.
#
# Siapkan sekali (di dev, sebagai root):
#   ssh-keygen -t ed25519 -N '' -C dev-pull-live-backup -f /root/.ssh/grp_live_backup
#   ssh-keyscan -t ed25519 172.16.0.96 >> /root/.ssh/known_hosts     (cocokkan fingerprint-nya dengan live)

set -euo pipefail
umask 077

LIVE="${LIVE:-postgres@172.16.0.96}"
KEY="${PULL_KEY:-/root/.ssh/grp_live_backup}"
DEST="${DEST_DIR:-/var/backups/grp-dari-live}"
KEEP="${KEEP:-2}"
ENV_MARKER="${ENV_MARKER:-/etc/grp_env}"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REFRESH="$HERE/refresh_dev.sh"
TMP=""

die() { echo "ERROR: $*" >&2; exit 1; }

cleanup() { [ -z "$TMP" ] || rm -f "$TMP"; }
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM HUP

REFRESH_ARGS=()
if [ "${1:-}" = "-y" ]; then REFRESH_ARGS+=(-y); shift; fi

# --- 1. Penjaga: cek DEV sebelum menarik data live, bukan sesudahnya ----------------------------
[ "$(id -u)" = "0" ] || die "jalankan sebagai root (sudo)."
[ -r "$ENV_MARKER" ] && [ "$(tr -d '[:space:]' < "$ENV_MARKER")" = "DEV" ] \
    || die "$ENV_MARKER tidak berisi DEV. Ini bukan server dev, dibatalkan."
[ -x "$REFRESH" ] || die "tidak ada $REFRESH (taruh skrip ini satu folder dengan refresh_dev.sh)."
[ -r "$KEY" ] || die "SSH key $KEY tidak ada. Lihat petunjuk di kepala skrip."
[[ "$KEEP" =~ ^[1-9][0-9]*$ ]] || die "KEEP harus angka >= 1."

# --- 2. Tarik backup dari live ------------------------------------------------------------------
[ ! -L "$DEST" ] || die "$DEST adalah symlink, ditolak."
mkdir -p "$DEST"
chmod 700 "$DEST"
FILE="$DEST/grp_$(date +%F_%Hh%Mm)_live.sql.gz"
[ ! -e "$FILE" ] || die "file sudah ada: $FILE (tunggu semenit)."
TMP="$FILE.part"

echo "Meminta backup baru dari $LIVE (database live hanya dibaca)..."
ssh -T -i "$KEY" -o IdentitiesOnly=yes -o BatchMode=yes -o StrictHostKeyChecking=yes \
    "$LIVE" > "$TMP" || die "penarikan backup dari live gagal."

echo "Memeriksa file..."
gzip -t "$TMP" || die "file rusak atau terpotong."
zcat "$TMP" | tail -n 5 | grep -q "PostgreSQL database dump complete" || die "dump tidak utuh."
mv "$TMP" "$FILE"
TMP=""
echo "Tersimpan: $FILE ($(du -h "$FILE" | cut -f1))"

# --- 3. Simpan beberapa salinan terakhir saja (berisi seluruh data live) -------------------------
ls -1t "$DEST"/grp_*_live.sql.gz 2>/dev/null | tail -n +$((KEEP + 1)) | xargs -r rm -f --

# --- 4. Restore ke database dev (semua pengaman refresh_dev.sh tetap berlaku) --------------------
exec "$REFRESH" "${REFRESH_ARGS[@]}" "$FILE"
