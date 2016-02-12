
:config

set app_home=../

rem specify the JVM heap size
rem set memory=1024m
set memory=1024m

set schema_addr=localhost:10150
rem set schema_addr=192.168.1.100:10150,192.168.1.181:10150,192.168.1.202:10150

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=hz-client-logging.xml
set java_opts=%java_opts% -Dlog.name=tpox-client -Dhz.log.level=warn -Dxdm.log.level=info

set java_opts=%java_opts% -Dhazelcast.client.event.thread.count=1

set java_opts=%java_opts% -Dxdm.schema.address=%schema_addr%
set java_opts=%java_opts% -Dxdm.schema.name=YCSB
set java_opts=%java_opts% -Dxdm.schema.user=guest
set java_opts=%java_opts% -Dxdm.schema.password=password

rem possible values are: member, owner, any
set java_opts=%java_opts% -Dxdm.client.submitTo=owner
set java_opts=%java_opts% -Dxdm.client.bufferSize=64
set java_opts=%java_opts% -Dxdm.client.fetchSize=1
set java_opts=%java_opts% -Dxdm.client.connectAttempts=3
set java_opts=%java_opts% -Dxdm.client.loginTimeout=30
set java_opts=%java_opts% -Dxdm.client.smart=true
set java_opts=%java_opts% -Dxdm.client.poolSize=200

rem set java_opts=%java_opts% -Duser.country=US -Duser.language=en

exit /b


