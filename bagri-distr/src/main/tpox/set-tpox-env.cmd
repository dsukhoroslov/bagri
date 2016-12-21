
:config

rem set TPoX HOME properly!

if "%TPOX_HOME%"=="" set TPOX_HOME=C:\Work\Bagri\TPoX

set app_home=../

rem specify the JVM heap size
set memory=2048m

rem specify schema and admin hosts:ports
set admin_addr=localhost:3330
set schema_addr=192.168.1.139:10500

set login=admin
set password=password
set schema=default

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=hz-client-logging.xml
set java_opts=%java_opts% -Dlog.name=tpox-client -Dhz.log.level=warn -Dbdb.log.level=info

set java_opts=%java_opts% -Dhazelcast.client.event.thread.count=1

set java_opts=%java_opts% -Dbdb.schema.address=%schema_addr%
set java_opts=%java_opts% -Dbdb.schema.name=%schema%
set java_opts=%java_opts% -Dbdb.schema.user=guest
set java_opts=%java_opts% -Dbdb.schema.password=password

rem possible values are: member, owner, any
set java_opts=%java_opts% -Dbdb.client.submitTo=owner
set java_opts=%java_opts% -Dbdb.client.bufferSize=32
set java_opts=%java_opts% -Dbdb.client.fetchSize=1
set java_opts=%java_opts% -Dbdb.client.connectAttempts=3
set java_opts=%java_opts% -Dbdb.client.loginTimeout=30
set java_opts=%java_opts% -Dbdb.client.smart=true
set java_opts=%java_opts% -Dbdb.client.poolSize=200
set java_opts=%java_opts% -Dbdb.client.healthCheck=skip

set java_opts=%java_opts% -Duser.country=US -Duser.language=en

exit /b
