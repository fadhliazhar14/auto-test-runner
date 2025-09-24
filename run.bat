@echo off
echo Starting BDD Test Runner Application...
echo.

REM Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found in PATH
    echo Please install Maven and add it to your PATH
    pause
    exit /b 1
)

REM Check if Java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found in PATH
    echo Please install Java 17 or higher and add it to your PATH
    pause
    exit /b 1
)

REM Compile and run the application
echo Compiling application...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    echo Please check the errors above and fix them.
    pause
    exit /b 1
)

echo.
echo Starting application...
echo NOTE: Make sure MySQL server is running before proceeding!
echo.

REM Run the application
call mvn exec:java -Dexec.mainClass="com.fadhli.automation.BddTestRunnerApplication"

pause