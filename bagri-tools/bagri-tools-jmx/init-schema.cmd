@echo off
@
@
setlocal

call set-tpox-env.cmd

rem The number of securities is fixed to 20,833.  Therefore, either 
rem "-u 83 -tr 251"  or "-u 251 -tr 83" can be use to insert the 20833
rem security documents  (because 83 * 251 = 20833). 4166*5 = 3472*6 = 2976*7 = 2604*8 = 2314*9

rem register schema
"%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.tools.jmx.MBeanInvoker %admin_addr% %login% %password% init_default_schema.xml

rem insert securities to the cache
rem "%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/insSecurity-xqj.xml -tr 2604 -u 8


goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-sec.cmd
goto exit

:exit
endlocal
@echo on
