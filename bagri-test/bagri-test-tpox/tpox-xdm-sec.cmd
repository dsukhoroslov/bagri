@echo off
@
@
setlocal

:config

set TPOX_HOME=C:\Work\Bagri\TPoX
rem set tpox_home=C:\Work\Bagri\TPoX
set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=128m

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dlogback.configurationFile=tpox-logging.xml
rem set java_opts=%java_opts% -Dxdm.spring.context=hazelcast/client-context.xml
rem set java_opts=%java_opts% -Dxdm.spring.context=spring/hz-client-context.xml
set java_opts=%java_opts% -Dxdm.spring.context=spring/tpox-client-context.xml

set java_opts=%java_opts% -Dxdm.schema.members=192.168.1.100:10500
rem set java_opts=%java_opts% -Dxdm.schema.members=localhost:10500
set java_opts=%java_opts% -Dxdm.schema.name=TPoX2
set java_opts=%java_opts% -Dxdm.schema.password=TPoX2
set java_opts=%java_opts% -Dxdm.client.submitTo=any
rem possible values are: member, owner, any


rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/insSecurity.xml -tr 2604 -u 8

rem get insert statistics
"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.client.tpox.StatisticsCollector localhost:3330 TPoX2 ./stats.txt InsertSecurities storeDocument

rem perform queries loopig by user count
for /l %%x in (5, 1, 10) do (
	echo %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/securities.xml -u %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.client.tpox.StatisticsCollector localhost:3330 TPoX2 ./stats.txt Users=%%x getXML
)


goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xdm-sec.cmd
goto exit

:exit
endlocal
@echo on

