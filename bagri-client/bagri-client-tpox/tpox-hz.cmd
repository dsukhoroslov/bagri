@echo off
@
@rem This will start a console application
@rem running TPoX workbench
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

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dlogback.configurationFile=%app_home%\target\classes\tpox-logging.xml
set java_opts=%java_opts% -Dxdm.spring.context=hazelcast/client-context.xml
                                      
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\lib\*;%app_home%\target\*" net.sf.tpox.workload.core.WorkloadDriver -w properties/queries.xml %*

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-hz.cmd
goto exit

:exit
endlocal
@echo on
