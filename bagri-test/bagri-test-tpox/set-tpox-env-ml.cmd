
:config

rem set TPoX HOME properly!

if "%TPOX_HOME%"=="" set TPOX_HOME=C:\Work\Bagri\TPoX

set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=256m

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=hz-client-logging.xml
set java_opts=%java_opts% -Dlog.name=tpox-client -Dhz.log.level=warn -Dxdm.log.level=info
set java_opts=%java_opts% -Dxdm.spring.context=spring/tpox-ml-context.xml

#xdbc, conformance
set java_opts=%java_opts% -Dxdm.schema.host=localhost -Dxdm.schema.port=8003 -Dxdm.schema.mode=xdbc
set java_opts=%java_opts% -Dxdm.schema.username=admin
set java_opts=%java_opts% -Dxdm.schema.password=admin

exit /b
