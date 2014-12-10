@echo off
@
@rem This will start a console application
@rem demonstrating the functionality of the Coherence(tm) API
@
setlocal

call set-tpox-env.cmd

rem The number of securities is fixed to 20,833.  Therefore, either 
rem "-u 83 -tr 251"  or "-u 251 -tr 83" can be use to insert the 20833
rem security documents  (because 83 * 251 = 20833). 4166*5 = 3472*6 = 2976*7 = 2604*8 = 2314*9

rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w properties/insSecurity-xqj.xml -tr 2604 -u 8 %*

rem get insert statistics
"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector localhost:3330 TPoX2 ./stats.txt InsertSecurities

rem perform queries loopig by user count
for /l %%x in (5, 1, 10) do (
	echo %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w properties/queries-xqj.xml -u %%x %*
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector localhost:3330 TPoX2 ./stats.txt Users=%%x
)

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-seq-qs.cmd
goto exit

:exit
endlocal
@echo on
