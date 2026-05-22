@echo off
chcp 65001 >nul
setlocal

set "APP_HOME=%~dp0"
set "APP_JAR=%APP_HOME%codeclean.jar"

if not exist "%APP_JAR%" (
  set "APP_JAR=%APP_HOME%target\codeclean.jar"
)

if not exist "%APP_JAR%" (
  echo codeclean.jar not found. Please run build-cli.ps1 first.
  exit /b 1
)

if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)

if not exist "%JAVA_EXE%" (
  set "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp "%APP_JAR%;%APP_HOME%lib\*" com.source.CodeCleanCli %*
exit /b %ERRORLEVEL%
