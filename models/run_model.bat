@echo off
rem =====================================================================
rem  COMSOL 6.0 Java modelini kompilyatsiya qilish va ishga tushirish.
rem  Foydalanish: shu faylni .java fayl turgan papkada ikki marta bosing.
rem  Default model: Model1_Vertical. Boshqasi uchun: run_model.bat Model2_FET
rem =====================================================================
setlocal
cd /d "%~dp0"

rem COMSOL o'rnatilgan papka. Boshqa joyda bo'lsa, shu qatorni o'zgartiring.
set "COMSOL_BIN=C:\Program Files\COMSOL\COMSOL60\Multiphysics\bin\win64"

set "MODEL=%~1"
if "%MODEL%"=="" set "MODEL=Model1_Vertical"

if not exist "%COMSOL_BIN%\comsolbatch.exe" goto no_comsol
if not exist "%MODEL%.java" goto no_java

echo.
echo [1/2] Kompilyatsiya: %MODEL%.java
"%COMSOL_BIN%\comsolcompile.exe" "%MODEL%.java" > log_compile.txt 2>&1
type log_compile.txt
if not exist "%MODEL%.class" goto compile_failed

echo.
echo [2/2] Hisoblash boshlandi. Bir necha daqiqa kuting...
"%COMSOL_BIN%\comsolbatch.exe" -inputfile "%MODEL%.class" -outputfile "%MODEL%.mph" > log_run.txt 2>&1
type log_run.txt

rem Ikkala logni bitta faylga yig'amiz: shu faylni yuborasiz.
copy /y log_compile.txt + log_run.txt LOG_YUBORISH_UCHUN.txt >nul
echo.
echo =====================================================================
echo  TUGADI. LOG_YUBORISH_UCHUN.txt faylini Claude ga yuboring.
echo =====================================================================
pause
exit /b 0

:compile_failed
copy /y log_compile.txt LOG_YUBORISH_UCHUN.txt >nul
echo.
echo XATO: kompilyatsiya muvaffaqiyatsiz. LOG_YUBORISH_UCHUN.txt ni yuboring.
pause
exit /b 1

:no_comsol
echo XATO: comsolbatch.exe topilmadi:
echo   %COMSOL_BIN%
echo run_model.bat ni Notepad da oching va COMSOL_BIN qatorini to'g'rilang.
pause
exit /b 1

:no_java
echo XATO: %MODEL%.java fayli shu papkada yo'q: %CD%
pause
exit /b 1
