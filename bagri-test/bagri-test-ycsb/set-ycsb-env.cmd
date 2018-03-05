
:config

set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=2048m

rem set schema_addr=localhost:10150
rem ,localhost:10151
rem set schema_addr=192.168.1.139:10150

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=ycsb-logging.xml
set java_opts=%java_opts% -Dlog.name=ycsb-client -Dhz.log.level=warn -Dbdb.log.level=info

set java_opts=%java_opts% -Dhazelcast.client.event.thread.count=1

rem set java_opts=%java_opts% -Duser.country=US -Duser.language=en

exit /b


