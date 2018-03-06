@echo off
@
@
setlocal

call set-ycsb-env.cmd

rem insert documents to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -load -s -threads 20 -P bagri-workloada
rem "%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -load -P bagri-workloada

rem perform queries loopig by thread count
setlocal enableDelayedExpansion
for /l %%x in (1, 1, 8) do (
	set /a cnt = %%x - 1
	set th=1
	for /l %%i in (1,1,!cnt!) do set /a th *= 2
	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -threads !th! -P bagri-workloada >out!th!
)


rem "%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -threads 32 -P bagri-workloada
rem "%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -P bagri-workloada

endlocal
@echo on

