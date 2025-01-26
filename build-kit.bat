@echo off
setlocal enabledelayedexpansion

:: Пути к директориям и файлам
set SRC_DIR=src\main\java
set BUILD_DIR=build\creationkit
set JAR_FILE=lib\terminal-creation-kit.jar
set TERMINAL_CORE=target\terminal-clone-1.2-ALPHA.jar
set GSON_JAR=%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar

:: Создаем необходимые директории
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist lib mkdir lib

:: Проверяем наличие зависимостей
if not exist "%TERMINAL_CORE%" (
    echo Error: Terminal core JAR not found at %TERMINAL_CORE%
    echo Please build the main project first
    exit /b 1
)

if not exist "%GSON_JAR%" (
    echo Error: Gson library not found at %GSON_JAR%
    echo Please run 'mvn install' first
    exit /b 1
)

:: Компилируем исходники
echo Compiling CreationKit...
javac -encoding UTF-8 -cp "%TERMINAL_CORE%;%GSON_JAR%;lib\*" -d "%BUILD_DIR%" "%SRC_DIR%\com\terminal\sdk\*.java"

if errorlevel 1 (
    echo Compilation failed
    exit /b 1
)

:: Создаем JAR файл
echo Creating JAR file...
pushd "%BUILD_DIR%"
jar cvf "..\..\%JAR_FILE%" com\terminal\sdk\*.class
popd

echo CreationKit built successfully
exit /b 0 