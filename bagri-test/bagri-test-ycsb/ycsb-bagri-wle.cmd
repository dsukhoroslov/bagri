@echo off
@
@
setlocal

call set-ycsb-env.cmd

rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -load -s -threads 16 -P bagri-workloade > load.txt
rem "%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -load -P bagri-workloade


rem perform queries loopig by thread count
setlocal enableDelayedExpansion
for /l %%x in (1, 1, 8) do (
	set /a cnt = %%x - 1
	set th=1
	for /l %%i in (1,1,!cnt!) do set /a th *= 2
rem	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -threads !th! -P bagri-workloade >out!th!
)

rem "%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -P bagri-workloade
rem "%java_exec%" -server -XX:+UnlockCommercialFeatures -XX:+FlightRecorder %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -threads 32 -P bagri-workloade
"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -threads 32 -P bagri-workloade > scan.txt

endlocal
@echo on

