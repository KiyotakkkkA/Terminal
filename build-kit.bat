@echo off
setlocal enabledelayedexpansion

:: Читаем версию из version.properties
for /f "tokens=1,* delims==" %%a in ('type version.properties') do (
    if "%%a"=="version" (
        set "VERSION=%%b"
        set "VERSION=!VERSION: =!"
    )
)

:: Пути к директориям и файлам
set SRC_DIR=src\main\java
set BUILD_DIR=build\sdk
set JAR_FILE=lib\terminal-sdk.jar
set TERMINAL_CORE=target\terminal-clone-!VERSION!.jar
set GSON_JAR=%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar

:: Очищаем старые файлы
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%JAR_FILE%" del /f /q "%JAR_FILE%"

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
echo Compiling SDK...
javac -encoding UTF-8 -cp "%TERMINAL_CORE%;%GSON_JAR%;lib\*" -d "%BUILD_DIR%" ^
    "%SRC_DIR%\com\terminal\sdk\*.java" ^
    "%SRC_DIR%\com\terminal\sdk\animating\*.java" ^
    "%SRC_DIR%\com\terminal\sdk\core\*.java" ^
    "%SRC_DIR%\com\terminal\sdk\events\*.java" ^
    "%SRC_DIR%\com\terminal\sdk\output\*.java" ^
    "%SRC_DIR%\com\terminal\sdk\services\*.java" ^
    "%SRC_DIR%\com\terminal\sdk\system\*.java"

if errorlevel 1 (
    echo Compilation failed
    exit /b 1
)

:: Создаем JAR файл
echo Creating JAR file...
pushd "%BUILD_DIR%"
jar cvf "..\..\%JAR_FILE%" com\terminal\sdk\*.class com\terminal\sdk\animating\*.class com\terminal\sdk\core\*.class com\terminal\sdk\events\*.class com\terminal\sdk\output\*.class com\terminal\sdk\services\*.class com\terminal\sdk\system\*.class
popd

echo SDK built successfully
exit /b 0 