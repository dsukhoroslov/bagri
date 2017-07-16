
:config

set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=1024m

set schema_addr=localhost:10150
rem set schema_addr=192.168.1.100:10150

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=ycsb-logging.xml
rem set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=hz-client-logging.xml
set java_opts=%java_opts% -Dlog.name=ycsb-client -Dhz.log.level=warn -Dbdb.log.level=info

set java_opts=%java_opts% -Dhazelcast.client.event.thread.count=1

set java_opts=%java_opts% -Dbdb.schema.address=%schema_addr%
set java_opts=%java_opts% -Dbdb.schema.name=YCSB
set java_opts=%java_opts% -Dbdb.schema.user=guest
set java_opts=%java_opts% -Dbdb.schema.password=password

rem possible values are: member, owner, any
set java_opts=%java_opts% -Dbdb.client.submitTo=owner
set java_opts=%java_opts% -Dbdb.client.bufferSize=32
set java_opts=%java_opts% -Dbdb.client.fetchSize=10
set java_opts=%java_opts% -Dbdb.client.connectAttempts=3
set java_opts=%java_opts% -Dbdb.client.loginTimeout=30
set java_opts=%java_opts% -Dbdb.client.smart=true
set java_opts=%java_opts% -Dbdb.client.poolSize=50
set java_opts=%java_opts% -Dbdb.client.healthCheck=skip
set java_opts=%java_opts% -Dbdb.client.queryCache=true
set java_opts=%java_opts% -Dbdb.client.customAuth=true

rem set java_opts=%java_opts% -Dbdb.client.storeMode=merge
rem set java_opts=%java_opts% -Dbdb.client.txTimeout=100
set java_opts=%java_opts% -Dbdb.document.data.format=MAP


rem set java_opts=%java_opts% -Duser.country=US -Duser.language=en

exit /b
