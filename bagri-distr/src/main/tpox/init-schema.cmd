@echo off
@
@
setlocal

call set-tpox-env.cmd

rem register schema
"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.common.manage.MBeanInvoker %admin_addr% %login% %password% init_default_schema.xml

rem get insert statistics
rem "%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement executeXQuery InsertSecurities ./stats.txt false


goto exit

:instructions

echo Usage:
echo %app_home%\init-schema.cmd
goto exit

:exit
endlocal
@echo on
