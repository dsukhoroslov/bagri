@echo off
@
@rem This will start a console application
@rem demonstrating the functionality of the Coherence(tm) API
@
setlocal

rem cd to current directory
cd %~dp0

set PWD=%CD%

set app_home=%PWD%\..\
echo APP_HOME: %app_home%

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms512m -Xmx512m -XX:NewSize=128m -XX:MaxNewSize=128m -Denv=local
set java_opts=%java_opts% -XX:+ExplicitGCInvokesConcurrent -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode
set java_opts=%java_opts% -Dclearsmart.jbax.host=10.249.143.65 -Dclearsmart.jbax.port=1099
set java_opts=%java_opts% -Dtangosol.coherence.override=..\config\coherence\cos-coherence-override.xml
set java_opts=%java_opts% -Dtangosol.coherence.wka=localhost
set java_opts=%java_opts% -Dlogback.configurationFile=..\config\cos-cache-logging.xml

"%java_exec%" -server -showversion %java_opts% -cp "..\lib\*;..\config" com.tangosol.net.DefaultCacheServer

goto exit

:instructions

echo Can't find coherence jar

echo Usage:
echo   ^<app_home^>\asclear-cos-cache-server.cmd
goto exit

:exit
endlocal
@echo on
