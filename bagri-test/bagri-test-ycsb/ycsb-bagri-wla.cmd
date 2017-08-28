@echo off
@
@
setlocal

call set-ycsb-env.cmd

rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -load -s -threads 20 -P bagri-workloada
rem "%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -load -P bagri-workloada

for /l %%x in (5, 1, 10) do (
rem 	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -threads %%x -P bagri-workloada  
)

rem perform queries loopig by thread count
setlocal enableDelayedExpansion
for /l %%x in (1, 1, 8) do (
	set /a cnt = %%x - 1
	set th=1
	for /l %%i in (1,1,!cnt!) do set /a th *= 2
rem	"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -threads !th! -P bagri-workloada >out!th!
)


"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -threads 32 -P bagri-workloada
rem "%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.yahoo.ycsb.Client -s -P bagri-workloada

goto exit

:instructions

echo Usage:
echo %app_home%\ycsb-bagri-wla.cmd
goto exit

:exit
endlocal
@echo on

