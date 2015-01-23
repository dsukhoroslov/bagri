@echo off
@
setlocal

call set-tpox-env.cmd

rem think about how long time this should run
rem "%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/orders-xqj.xml -u 10 %*

setlocal enableDelayedExpansion
for /l %%x in (10, 10, 100) do (
	set n=%%x
	set /a count=100*n
	echo !count!0
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/insOrder-xqj.xml -tr !count! -u 10
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% ./stats.txt InsertOrders executeXQuery false

	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/orders-xqj-%%x.xml -u 100
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% ./stats.txt Orders=!count!0 executeXQuery true
)

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-orders.cmd
goto exit

:exit
endlocal
@echo on
