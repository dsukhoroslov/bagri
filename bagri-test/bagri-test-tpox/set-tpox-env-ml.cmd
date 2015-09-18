
:config

rem set TPoX HOME properly!

if "%TPOX_HOME%"=="" set TPOX_HOME=C:\Work\Bagri\TPoX

set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=1024m

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=hz-client-logging.xml
set java_opts=%java_opts% -Dlog.name=tpox-client -Dhz.log.level=warn -Dxdm.log.level=info
rem set java_opts=%java_opts% -Dxdm.spring.context=spring/tpox-xcc-context.xml

set java_opts=%java_opts% -Dxdm.schema.host=10.249.143.8 -Dxdm.schema.port=8003 -Dxdm.schema.name=TPoX 
set java_opts=%java_opts% -Dxdm.schema.user=admin
set java_opts=%java_opts% -Dxdm.schema.password=password

rem MarkLogic XCC properties
rem xcc.socket.pool.max=64
rem xcc.socket.sendbuf=128 * 1024
rem xcc.socket.recvbuf=128 * 1024
rem xcc.request.retries.delay=100
rem xcc.request.retries.max=4

set java_opts=%java_opts% -Dxcc.socket.pool.max=250

set java_opts=%java_opts% -Dxdm.client.fetchSize=1

set java_opts=%java_opts% -Duser.country=US -Duser.language=en

exit /b
