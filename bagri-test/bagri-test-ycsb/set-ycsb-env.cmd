
set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=2048m

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dhazelcast.client.event.thread.count=1
set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=ycsb-logging.xml
set java_opts=%java_opts% -Dlog.name=ycsb-client -Dhz.log.level=warn -Dbdb.log.level=info

rem set java_opts=%java_opts% -Duser.country=US -Duser.language=en

exit /b


