@echo off
@
setlocal

call set-tpox-env.cmd

rem think about how long time this should run
                                      
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w properties/orders.xml -u 10 %*

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xdm-orders.cmd
goto exit

:exit
endlocal
@echo on
