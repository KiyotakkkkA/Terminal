@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

set CONFIG_FILE=%1
if "%CONFIG_FILE%"=="" (
    echo Usage: plugin-build.bat ^<config_file^> or plugin-build.bat -all
    exit /b 1
)

if "%CONFIG_FILE%"=="-all" (
    echo Building all plugins...
    cd /d "%~dp0"
    echo Current directory: %CD%
    for %%f in (plugins\*.txt) do (
        echo.
        echo Building plugin from config: %%f
        call %0 %%f
    )
    echo.
    echo All plugins built successfully
    exit /b 0
)

echo Processing config file: %CONFIG_FILE%
if not exist "%CONFIG_FILE%" (
    echo Error: Config file not found: %CONFIG_FILE%
    exit /b 1
)

:: Читаем конфиг файл
for /f "usebackq tokens=1,* delims=:" %%a in ("%CONFIG_FILE%") do (
    set "%%a=%%b"
)

echo Debug values:
echo CREATION_KIT_PATH=!CREATION_KIT_PATH!
echo VERSION=!VERSION!
echo JAR_FILENAME=!JAR_FILENAME!
echo SOURCE_PATH=!SOURCE_PATH!
REM Get plugin class name from source file
for %%f in ("%SOURCE_PATH%\*.java") do (
set "PLUGIN_CLASS=%%~nf"
)
REM If no Java file found, use default naming convention
if not defined PLUGIN_CLASS (
set "PLUGIN_CLASS=%JAR_FILENAME%"
set "PLUGIN_CLASS=%PLUGIN_CLASS:~0,1%"
for %%A in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
if /i ""=="%%A" set "PLUGIN_CLASS=%%A"
)
set "PLUGIN_CLASS=%PLUGIN_CLASS%%JAR_FILENAME:~1%Plugin"
)
REM Validate required parameters
if "%CREATION_KIT_PATH%"=="" (
echo Error: CREATION_KIT_PATH not specified in config file
exit /b 1
)
if "%VERSION%"=="" (
echo Error: VERSION not specified in config file
exit /b 1
)
if "%JAR_FILENAME%"=="" (
echo Error: JAR_FILENAME not specified in config file
exit /b 1
)
if "%SOURCE_PATH%"=="" (
echo Error: SOURCE_PATH not specified in config file
exit /b 1
)
REM Directory and file paths
set "BUILD_DIR=build\plugins\%JAR_FILENAME%"
set "MANIFEST_DIR=%BUILD_DIR%\META-INF"
set "PLUGIN_JAR=plugins\build\%JAR_FILENAME%.jar"
set "LIBS_DIR=lib"
set "TERMINAL_CORE=target\terminal-clone-2.0.1-ALPHA.jar"
set "GSON_JAR=%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar"
REM Check dependencies
if not exist "%CREATION_KIT_PATH%" (
echo Error: CreationKit JAR not found at "%CREATION_KIT_PATH%"
exit /b 1
)
if not exist "%TERMINAL_CORE%" (
echo Error: Terminal core JAR not found at "%TERMINAL_CORE%"
echo Please build the main project first
exit /b 1
)
REM Create directories
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"
mkdir "%MANIFEST_DIR%"
mkdir "%BUILD_DIR%\com\terminal\plugins\%JAR_FILENAME%"
if not exist plugins\build mkdir plugins\build
REM Create MANIFEST.MF
(
echo Manifest-Version: 1.0
echo Plugin-Name: %JAR_FILENAME%
echo Plugin-Version: %VERSION%
echo Plugin-Class: com.terminal.plugins.%JAR_FILENAME%.%PLUGIN_CLASS%
) > "%MANIFEST_DIR%\MANIFEST.MF"
REM Compile sources
echo Compiling %JAR_FILENAME% plugin...
javac -source 8 -target 8 -encoding UTF-8 -cp "%CREATION_KIT_PATH%;%TERMINAL_CORE%;%GSON_JAR%;%LIBS_DIR%\*" -d "%BUILD_DIR%" "%SOURCE_PATH%\*.java"
if errorlevel 1 (
echo Compilation failed
exit /b 1
)
REM Create JAR file
echo Creating JAR file...
pushd "%BUILD_DIR%"
jar cvfm "..\..\..\%PLUGIN_JAR%" META-INF\MANIFEST.MF com\*
popd
echo Plugin %JAR_FILENAME% built successfully
exit /b 0 
