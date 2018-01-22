@echo off
@
setlocal

call set-tpox-env.cmd

rem "%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insTpox.xml -u 10 -tr 5000 %*

rem "%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/delTpox.xml -u 10 -tr 5000 %*

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/mixTpox.xml -u 10 -tr 5000 %*

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj.cmd
goto exit

:exit
endlocal
@echo on
