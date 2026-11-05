@echo off
chcp 65001 >nul
setlocal
set PYTHONIOENCODING=utf-8
cd /d "%~dp0"
title Pembersih Aplikasi Authentik

rem Token: dipakai dari env AK_TOKEN bila sudah ada, selain itu diminta (ketikan tidak tampil, tidak disimpan).
if not defined AK_TOKEN (
  for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command "$s=Read-Host 'Token API Authentik' -AsSecureString; [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($s))"`) do set "AK_TOKEN=%%i"
)
if not defined AK_TOKEN (echo Token kosong, dibatalkan. & pause & exit /b 1)

:menu
echo.
echo ================================================
echo   PEMBERSIH APLIKASI AUTHENTIK  (group = Others)
echo ================================================
echo   1. Lihat daftar aplikasi
echo   2. Dry-run hapus (hanya menampilkan, aman)
echo   3. HAPUS semua aplikasi ber-group Others
echo   4. Pulihkan dari cadangan
echo   0. Keluar
echo.
set "P="
set /p P=Pilih:
if "%P%"=="1" python authentik_app_cleanup.py list & goto menu
if "%P%"=="2" python authentik_app_cleanup.py delete-group & goto menu
if "%P%"=="3" python authentik_app_cleanup.py delete-group --apply & goto menu
if "%P%"=="4" goto restore
if "%P%"=="0" exit /b 0
goto menu

:restore
setlocal enabledelayedexpansion
set N=0
echo.
echo Cadangan tersedia (terbaru di atas):
for /f "delims=" %%f in ('dir /b /o-d "%USERPROFILE%\authentik-backups\*.json" 2^>nul') do (
  set /a N+=1
  set "FILE!N!=%%f"
  echo   !N!. %%f
)
if !N!==0 (
  echo Tidak ada cadangan.
  endlocal
  goto menu
)
echo.
set "NO="
set /p NO=Pilih nomor ^(Enter = 1, yang terbaru; 0 = batal^):
if not defined NO set NO=1
if "!NO!"=="0" (
  endlocal
  goto menu
)
set "F="
for %%n in (!NO!) do set "F=!FILE%%n!"
if not defined F (
  echo Nomor tidak valid.
  endlocal
  goto menu
)
echo Berkas: !F!
set "C="
set /p C=Pulihkan sebagai SALINAN UJI saja? ^(y/n^):
if /i "!C!"=="y" (
  python authentik_app_cleanup.py restore "%USERPROFILE%\authentik-backups\!F!" --as-copy
) else (
  python authentik_app_cleanup.py restore "%USERPROFILE%\authentik-backups\!F!"
)
endlocal
goto menu
