
:config

if "%tpox_home%"=="" set tpox_home=.

set app_home=../

rem specify the JVM heap size
rem set memory=1024m
set memory=256m

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dlogback.configurationFile=tpox-logging.xml
set java_opts=%java_opts% -Dxqj.spring.context=spring/tpox-client-context.xml

rem set java_opts=%java_opts% -Dxdm.schema.members=192.168.1.100:10500
set java_opts=%java_opts% -Dxdm.schema.members=localhost:10500
set java_opts=%java_opts% -Dxdm.schema.name=TPoX2
set java_opts=%java_opts% -Dxdm.schema.password=TPoX2
set java_opts=%java_opts% -Dxdm.client.submitTo=any
rem possible values are: member, owner, any

exit /b
