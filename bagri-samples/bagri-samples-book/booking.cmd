@echo off
@
@
setlocal

set app_home=.

rem specify the JVM heap size
set memory=1024m

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

rem C:\Work\Bagri\booking\TVvkE5NOHCwyjESRAdTWIw_Europe_12.tsv

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.samples.book.BookingApp %1 %2 %3 %4

goto exit

:instructions

echo Usage:
echo %app_home%\booking.cmd <schemaAddress> <schemaName> <userName> <password>
goto exit

:exit
endlocal
@echo on
