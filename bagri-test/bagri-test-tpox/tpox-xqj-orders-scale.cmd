@echo off
@
@
setlocal

call set-tpox-env.cmd

rem think about how long time this should run
rem "%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/orders.xml -u 10 %*

setlocal enableDelayedExpansion
for /l %%x in (10, 10, 70) do (
	set n=%%x
	set /a count=100*n
	echo !count!0
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insOrder.xml -tr !count! -u 10
rem	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement executeXQuery InsertOrders ./stats.txt false

rem	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/orders-%%x.xml -u 10
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/orders-10.xml -u 10
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.test.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement executeXQuery Orders=!count!0 ./stats.txt true
)

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-orders.cmd
goto exit

:exit
endlocal
@echo on


