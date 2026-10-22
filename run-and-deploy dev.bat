@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "KEY=%USERPROFILE%\.ssh\grp_deploy"
set "SERVER=deploy@172.16.0.151"

rem Argumen diteruskan apa adanya ke grp-deploy di server, misalnya:
rem   deploy.bat            -> peringatan default 60 detik lalu restart
rem   deploy.bat 180        -> peringatan 180 detik
rem   deploy.bat --now      -> restart langsung tanpa peringatan

echo [1/4] Building production JAR...
call mvn -o -Pproduction clean package -DskipTests
if errorlevel 1 (
    echo.
    echo BUILD GAGAL - server tidak disentuh.
    goto :fail
)

set "JARFILE="
for %%f in (target\vaadinerp-*.jar) do set "JARFILE=%%f"
if not defined JARFILE (
    echo Tidak ketemu JAR hasil build di target\
    goto :fail
)
echo Built: %JARFILE%

echo.
echo [2/4] Mengirim JAR ke server...
scp -i "%KEY%" "%JARFILE%" %SERVER%:/tmp/
if errorlevel 1 (
    echo.
    echo SCP GAGAL - server tidak disentuh.
    goto :fail
)

echo.
echo [3/4] Deploy di server (backup, pasang, restart, tunggu start)...
ssh -i "%KEY%" %SERVER% "sudo grp-deploy %*"
if errorlevel 1 (
    echo.
    echo DEPLOY BERMASALAH - baca pesan di atas. Pertimbangkan rollback:
    echo   ssh -i "%KEY%" %SERVER% "ls /opt/grp/backup/"
    echo   ssh -i "%KEY%" %SERVER% "sudo cp /opt/grp/backup/<file> /opt/grp/vaadinerp.jar && sudo systemctl restart grp"
    goto :fail
)

echo.
echo [4/4] Selesai. Silakan login dan tes.
echo.
pause
endlocal
exit /b 0

:fail
echo.
pause
endlocal
exit /b 1
