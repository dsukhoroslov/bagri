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

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=hz-client-logging.xml
set java_opts=%java_opts% -Dlog.name=tpox-client -Dhz.log.level=warn -Dxdm.log.level=warn

rem set java_opts=%java_opts% -Dhazelcast.client.event.thread.count=1

rem possible values are: member, owner, any
set java_opts=%java_opts% -Dxdm.client.submitTo=owner
set java_opts=%java_opts% -Dxdm.client.bufferSize=32
set java_opts=%java_opts% -Dxdm.client.fetchSize=1
set java_opts=%java_opts% -Dxdm.client.connectAttempts=3
set java_opts=%java_opts% -Dxdm.client.loginTimeout=30
set java_opts=%java_opts% -Dxdm.client.smart=true
set java_opts=%java_opts% -Dxdm.client.poolSize=200
set java_opts=%java_opts% -Dxdm.client.healthCheck=skip

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" com.bagri.samples.book.BookingApp %1 %2 %3 %4

goto exit

:instructions

echo Usage:
echo %app_home%\booking.cmd <schemaAddress> <schemaName> <userName> <password>
goto exit

:exit
endlocal
@echo on
