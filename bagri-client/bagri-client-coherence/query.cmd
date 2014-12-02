@echo off
@
@rem This will start a console application
@rem demonstrating the functionality of the Coherence(tm) API
@
setlocal

REM NOTE: It's custom CCS CoherenceConsole tool. See the QueryCacheClient for the logic.
REM NOTE: Put here you local paths instead of hardcoded ones
REM HOW_TO: http://docs.oracle.com/cd/E24290_01/coh.371/e22837/api_cq.htm#CDDIBCDH


:config
rem specify the Coherence installation directory
set coherence_home=C:\coherence-3.7.1.10

rem specify the jline installation directory
@set jline_home=.

rem set app_home=c:\workspace\cpg-creditcheck\ccs-cache-client
set app_home=.

rem specify if the console will also act as a server
set storage_enabled=false

rem specify the JVM heap size
rem set memory=1024m
set memory=128m

:start
if not exist "%coherence_home%\lib\coherence.jar" goto instructions

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

if "%storage_enabled%"=="true" (echo ** Starting storage enabled console **) else (echo ** Starting storage disabled console **)

set java_opts=-Xms%memory% -Xmx%memory% -Dtangosol.coherence.distributed.localstorage=%storage_enabled%
set java_opts=%java_opts% -Dtangosol.coherence.cluster=XDMCacheCluster -Dtangosol.pof.enabled=true
set java_opts=%java_opts% -Dtangosol.coherence.cacheconfig=%app_home%\target\classes\coherence\xdm-client-cache-config.xml

rem set java_opts=%java_opts% -Dtangosol.coherence.wka=localhost -Dtangosol.coherence.wka.port=8088

set java_opts=%java_opts% -Dtangosol.coherence.proxy.address=localhost
set java_opts=%java_opts% -Dtangosol.coherence.proxy.port=21000

rem set java_opts=%java_opts% -Dtangosol.coherence.proxy.address=linbox.sdv.home
rem set java_opts=%java_opts% -Dtangosol.coherence.proxy.port=17000

set java_opts=%java_opts% -Dtangosol.coherence.management=all -Dtangosol.coherence.management.remote=true
set java_opts=%java_opts% -Dlogback.configurationFile=%app_home%\target\classes\logback.xml

"%java_exec%" -server -showversion %java_opts% -cp "%coherence_home%\lib\coherence.jar;%app_home%\target\lib\*;%app_home%\target\*" com.bagri.xdm.access.coherence.impl.QueryCacheClient %*

goto exit

:instructions

echo Usage:
echo %app_home%\query.cmd
goto exit

:exit
endlocal
@echo on
