@echo off
@
@rem This will start a console application
@rem demonstrating the functionality of the Coherence(tm) API
@
setlocal

call set-tpox-env-ml.cmd

rem The number of securities is fixed to 20,833.  Therefore, either 
rem "-u 83 -tr 251"  or "-u 251 -tr 83" can be use to insert the 20833
rem security documents  (because 83 * 251 = 20833). 4166*5 = 3472*6 = 2976*7 = 2604*8 = 2314*9

rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XCC/insSecurity.xml -u 83 -tr 251

rem perform queries looping by user count
for /l %%x in (5, 15, 200) do (
	echo %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XCC/securities.xml -u %%x -r 10 -r 10 -pc 95 -cl 99
)


goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-sec-ml.cmd
goto exit

:exit
endlocal
@echo on

