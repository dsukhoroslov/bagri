
:config

rem set TPoX HOME properly!

if "%TPOX_HOME%"=="" set TPOX_HOME=C:\Work\Bagri\TPoX

set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=1024m

rem specify schema and admin hosts:ports
set admin_addr=localhost:3330
set schema_addr=localhost:10500
rem set schema_addr=192.168.1.100:10500

set login=admin
set password=password
set schema=default

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

rem set java_opts=%java_opts% -Dtangosol.coherence.proxy.address=localhost
rem set java_opts=%java_opts% -Dtangosol.coherence.proxy.port=21000

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=hz-client-logging.xml
set java_opts=%java_opts% -Dlog.name=tpox-client -Dhz.log.level=warn -Dxdm.log.level=info

set java_opts=%java_opts% -Dhazelcast.client.event.thread.count=1

set java_opts=%java_opts% -Dxdm.schema.address=%schema_addr%
set java_opts=%java_opts% -Dxdm.schema.name=%schema%
set java_opts=%java_opts% -Dxdm.schema.user=guest
set java_opts=%java_opts% -Dxdm.schema.password=password

rem possible values are: member, owner, any
set java_opts=%java_opts% -Dxdm.client.submitTo=owner
set java_opts=%java_opts% -Dxdm.client.bufferSize=32
set java_opts=%java_opts% -Dxdm.client.fetchSize=1
set java_opts=%java_opts% -Dxdm.client.connectAttempts=3
set java_opts=%java_opts% -Dxdm.client.loginTimeout=30
set java_opts=%java_opts% -Dxdm.client.smart=false
set java_opts=%java_opts% -Dxdm.client.poolSize=1

set java_opts=%java_opts% -Duser.country=US -Duser.language=en

exit /b


