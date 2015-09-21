@echo off
@
@rem This will start a console application
@rem demonstrating the functionality of the Coherence(tm) API
@
setlocal

:config

set TPoX_HOME=C:\Work\Bagri\TPoX
set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=128m

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% -Dtangosol.coherence.distributed.localstorage=false
set java_opts=%java_opts% -Dtangosol.coherence.cluster=XDMCacheCluster -Dtangosol.pof.enabled=true
set java_opts=%java_opts% -Dtangosol.coherence.cacheconfig=%app_home%\target\classes\coherence\tpox-client-cache-config.xml

set java_opts=%java_opts% -Dtangosol.coherence.proxy.address=linbox.sdv.home
set java_opts=%java_opts% -Dtangosol.coherence.proxy.port=17000

rem set java_opts=%java_opts% -Dtangosol.coherence.management=all -Dtangosol.coherence.management.remote=true
set java_opts=%java_opts% -Dlogback.configurationFile=ch-client-logging.xml
set java_opts=%java_opts% -Dxdm.data.manager=Hazelcast

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\lib\*;%app_home%\target\*" net.sf.tpox.workload.core.WorkloadDriver -u 25 -w queries/XDM/queries.xml %*

goto exit

:instructions

echo Usage:
echo %app_home%\tpox.cmd
goto exit

:exit
endlocal
@echo on
