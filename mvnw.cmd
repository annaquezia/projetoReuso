@echo off
setlocal
set MAVEN_VERSION=3.9.11
if "%MAVEN_USER_HOME%"=="" set MAVEN_USER_HOME=%USERPROFILE%\.m2\wrapper\dists
set MAVEN_DIR=%MAVEN_USER_HOME%\apache-maven-%MAVEN_VERSION%
set MAVEN_HOME=%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$u='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest -Uri $u -OutFile '%MAVEN_DIR%\maven.zip'; Expand-Archive -Path '%MAVEN_DIR%\maven.zip' -DestinationPath '%MAVEN_DIR%' -Force; Remove-Item '%MAVEN_DIR%\maven.zip'"
  if errorlevel 1 exit /b 1
)
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %errorlevel%
