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
set memory=256m

rem set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_65

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

rem set java_opts=%java_opts% -Dtangosol.coherence.proxy.address=localhost
rem set java_opts=%java_opts% -Dtangosol.coherence.proxy.port=21000

set java_opts=%java_opts% -Dlogback.configurationFile=%app_home%\target\classes\tpox-logging.xml
set java_opts=%java_opts% -Dxqj.spring.context=spring/tpox-client-context.xml

rem set java_opts=%java_opts% -Dxdm.schema.members=192.168.1.100:10500
set java_opts=%java_opts% -Dxdm.schema.members=localhost:10500
set java_opts=%java_opts% -Dxdm.schema.name=TPoX2
set java_opts=%java_opts% -Dxdm.schema.password=TPoX2
set java_opts=%java_opts% -Dxdm.client.submitTo=any
rem possible values are: member, owner, any

rem think about how long time this should run
                                      
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\lib\*;%app_home%\target\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/custaccs-xqj.xml -u 10 %*

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj.cmd
goto exit

:exit
endlocal
@echo on
