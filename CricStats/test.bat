@echo off
echo ==========================================
echo Running CricStats Test Suite...
echo ==========================================
if not exist "bin" mkdir bin
javac -d bin cricstats\models\*.java cricstats\engine\*.java cricstats\storage\*.java cricstats\ui\*.java cricstats\*.java tests\*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
java -cp bin tests.CricketScoringTest
