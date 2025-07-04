@echo off
echo ========================================
echo SeamlessDeath Plugin Build Script
echo ========================================
echo.

REM Check if Maven is installed
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo.
    echo To build this plugin, you need to install Maven:
    echo 1. Download Maven from: https://maven.apache.org/download.cgi
    echo 2. Extract it to a folder (e.g., C:\apache-maven-3.9.5)
    echo 3. Add the bin folder to your PATH environment variable
    echo 4. Restart your command prompt and try again
    echo.
    echo Alternatively, you can:
    echo - Use an IDE like IntelliJ IDEA or Eclipse with Maven support
    echo - Download a pre-built JAR file if available
    echo.
    echo For manual compilation instructions, see INSTALLATION.md
    echo.
    pause
    exit /b 1
)

REM Clean and compile the project
echo Cleaning previous builds...
mvn clean

echo.
echo Compiling plugin...
mvn package

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo The plugin JAR file has been created in the 'target' folder.
    echo File: target\seamless-death-1.0.0.jar
    echo.
    echo Installation steps:
    echo 1. Copy the JAR file to your server's 'plugins' folder
    echo 2. Restart your server
    echo 3. Configure the plugin in 'plugins/SeamlessDeath/config.yml'
    echo 4. Use '/seamlessdeath reload' to apply changes
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Please check the error messages above.
    echo Common issues:
    echo - Missing dependencies (check pom.xml)
    echo - Java version compatibility
    echo - Network issues downloading dependencies
    echo.
)

pause