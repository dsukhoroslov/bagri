@echo off
@
@
setlocal

call set-tpox-env.cmd

rem The number of securities is fixed to 20,833.  Therefore, either 
rem "-u 83 -tr 251"  or "-u 251 -tr 83" can be use to insert the 20833
rem security documents  (because 83 * 251 = 20833). 4166*5 = 3472*6 = 2976*7 = 2604*8 = 2314*9

rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insSecurity.xml -u 83 -tr 251

rem insert customers to the cache
"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insCustacc.xml -u 20 -tr 5000

rem insert orders to the cache
"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insOrder.xml -u 25 -tr 20000


goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-add-mix.cmd
goto exit

:exit
endlocal
@echo on
