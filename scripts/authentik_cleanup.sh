#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

export PYTHONIOENCODING=utf-8

# Deteksi perintah python3 atau python
PYTHON_CMD="python3"
if ! command -v python3 &>/dev/null; then
  if command -v python &>/dev/null; then
    PYTHON_CMD="python"
  else
    echo "Error: Python 3 tidak ditemukan di sistem Ubuntu ini. Silakan jalankan: sudo apt install -y python3"
    exit 1
  fi
fi

# ========================================================
# KONFIGURASI (Isi token di sini agar tidak ditanya lagi)
# ========================================================
FIXED_TOKEN=""

# Muat file .env jika ada (agar token tidak sengaja ter-commit ke git)
if [ -f "$SCRIPT_DIR/.env" ]; then
  # shellcheck disable=SC1091
  source "$SCRIPT_DIR/.env" 2>/dev/null || true
fi

# Baca token dari FIXED_TOKEN, atau dari env AK_TOKEN jika sudah ada
if [ -n "$FIXED_TOKEN" ]; then
  export AK_TOKEN="$FIXED_TOKEN"
fi

# Jika token belum ada sama sekali, baru tanyakan secara interaktif
if [ -z "$AK_TOKEN" ]; then
  read -s -p "Token API Authentik: " AK_TOKEN
  echo ""
  export AK_TOKEN
fi

if [ -z "$AK_TOKEN" ]; then
  echo "Token kosong, dibatalkan."
  exit 1
fi

BACKUP_DIR="${AK_BACKUP_DIR:-$HOME/authentik-backups}"

restore_menu() {
  echo ""
  echo "Cadangan tersedia (terbaru di atas):"

  if [ ! -d "$BACKUP_DIR" ]; then
    echo "Direktori $BACKUP_DIR tidak ditemukan."
    return
  fi

  # Ambil daftar file .json terurut waktu modifikasi terbaru di atas
  files=()
  while IFS= read -r file; do
    [ -n "$file" ] && files+=("$file")
  done < <(ls -t "$BACKUP_DIR"/*.json 2>/dev/null || true)

  total=${#files[@]}
  if [ "$total" -eq 0 ]; then
    echo "Tidak ada cadangan di $BACKUP_DIR."
    return
  fi

  for i in "${!files[@]}"; do
    num=$((i + 1))
    echo "  $num. $(basename "${files[$i]}")"
  done

  echo ""
  read -p "Pilih nomor (Enter = 1, yang terbaru; 0 = batal): " NO
  NO="${NO:-1}"

  if [ "$NO" = "0" ]; then
    return
  fi

  if ! [[ "$NO" =~ ^[0-9]+$ ]] || [ "$NO" -lt 1 ] || [ "$NO" -gt "$total" ]; then
    echo "Nomor tidak valid."
    return
  fi

  chosen_file="${files[$((NO - 1))]}"
  echo "Berkas: $(basename "$chosen_file")"

  read -p "Pulihkan sebagai SALINAN UJI saja? (y/n): " C
  if [[ "$C" =~ ^[yY]$ ]]; then
    "$PYTHON_CMD" authentik_app_cleanup.py restore "$chosen_file" --as-copy
  else
    "$PYTHON_CMD" authentik_app_cleanup.py restore "$chosen_file"
  fi
}

menu() {
  while true; do
    echo ""
    echo "================================================"
    echo "  PEMBERSIH APLIKASI AUTHENTIK  (group = Others)"
    echo "================================================"
    echo "  1. Lihat daftar aplikasi"
    echo "  2. Dry-run hapus (hanya menampilkan, aman)"
    echo "  3. HAPUS semua aplikasi ber-group Others"
    echo "  4. Pulihkan dari cadangan"
    echo "  0. Keluar"
    echo ""
    read -p "Pilih: " P
    case "$P" in
      1)
        "$PYTHON_CMD" authentik_app_cleanup.py list
        ;;
      2)
        "$PYTHON_CMD" authentik_app_cleanup.py delete-group
        ;;
      3)
        "$PYTHON_CMD" authentik_app_cleanup.py delete-group --apply
        ;;
      4)
        restore_menu
        ;;
      0)
        exit 0
        ;;
      *)
        echo "Pilihan tidak valid."
        ;;
    esac
  done
}

menu
