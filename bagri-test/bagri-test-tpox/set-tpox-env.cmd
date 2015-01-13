
:config

rem set TPoX HOME properly!

if "%TPOX_HOME%"=="" set TPOX_HOME=C:\Work\Bagri\TPoX

set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=256m

rem specify schema and admin hosts:ports
set admin_addr=localhost:3330
set schema_addr=localhost:10500
rem set schema_addr=192.168.1.100:10500

set schema=TPoX2

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dlogback.configurationFile=hz-client-logging.xml -Dlog.name=tpox-client
set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dhz.log.level=warn -Dxdm.log.level=info
set java_opts=%java_opts% -Dxdm.spring.context=spring/tpox-client-context.xml

set java_opts=%java_opts% -Dxdm.schema.members=%schema_addr%
set java_opts=%java_opts% -Dxdm.schema.name=%schema%
set java_opts=%java_opts% -Dxdm.schema.password=%schema%
set java_opts=%java_opts% -Dxdm.client.submitTo=any
rem possible values are: member, owner, any

exit /b

