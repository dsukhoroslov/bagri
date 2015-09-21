@echo off
@
setlocal

call set-tpox-env.cmd

setlocal enableDelayedExpansion
for /l %%x in (10, 10, 100) do (
	set n=%%x
	set /a count=100*n
	echo !count!0
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insOrder.xml -tr !count! -u 10
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement executeXQuery InsertOrders ./stats.txt false

	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/orders-%%x.xml -u 100
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement executeXQuery Orders=!count!0 ./stats.txt true
)

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-orders-scale.cmd
goto exit

:exit
endlocal
@echo on
