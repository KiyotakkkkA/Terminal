@echo off
call mvn clean package
if errorlevel 1 (
echo Ошибка при компиляции
pause
exit /b 1
)
echo Компиляция успешно завершена
echo Запуск приложения...
java -jar target/terminal-clone-2.0.3-ALPHA-jar-with-dependencies.jar
pause 
