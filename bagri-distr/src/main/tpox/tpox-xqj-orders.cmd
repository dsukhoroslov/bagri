@echo off
@
@rem This will start a console application
@rem demonstrating the functionality of the Coherence(tm) API
@
setlocal

call set-tpox-env.cmd

rem think about how long time this should run
                                      
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w properties/orders-xqj.xml -u 10 %*

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj.cmd
goto exit

:exit
endlocal
@echo on
