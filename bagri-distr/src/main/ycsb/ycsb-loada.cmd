@echo off
@
@
setlocal

call set-ycsb-env.cmd

rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.yahoo.ycsb.Client -load -threads 50 -P workloada

rem perform queries loopig by user count
for /l %%x in (50, 10, 200) do (
 	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.yahoo.ycsb.Client -threads %%x -P workloada  
)


goto exit

:instructions

echo Usage:
echo %app_home%\ycsb-loada.cmd
goto exit

:exit
endlocal
@echo on

