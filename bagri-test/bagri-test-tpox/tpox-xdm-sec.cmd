@echo off
@
@
setlocal

call set-tpox-env.cmd

rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/insSecurity.xml -tr 2604 -u 8

rem get insert statistics
"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% DocumentManagement storeDocument InsertSecurities ./stats.txt false

rem perform queries loopig by user count
for /l %%x in (5, 1, 10) do (
	echo %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/securities.xml -u %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement getXML Users=%%x ./stats.txt false
)


goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xdm-sec.cmd
goto exit

:exit
endlocal
@echo on

