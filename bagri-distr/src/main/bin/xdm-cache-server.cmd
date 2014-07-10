@echo off
@
@rem This will start XDM server application
@
setlocal

rem specify application home
set app_home=../

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

rem specify the JVM heap size
set memory=2048m

rem specify JVM memory options
set java_opts=-Xms%memory% -Xmx%memory% -XX:NewSize=256m -XX:MaxNewSize=256m
set java_opts=%java_opts% -XX:PermSize=256m -XX:MaxPermSize=256m -XX:+ExplicitGCInvokesConcurrent
set java_opts=%java_opts% -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=2048

rem specify JVM GC options
set java_opts=%java_opts% -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSScavengeBeforeRemark 
set java_opts=%java_opts% -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 
set java_opts=%java_opts% -XX:+CMSConcurrentMTEnabled

rem specify JVM GC logging
set java_opts=%java_opts% -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError 
set java_opts=%java_opts% -Xloggc:../logs/gc.log -XX:GCLogFileSize=50M -XX:NumberOfGCLogFiles=10 -XX:+UseGCLogFileRotation
   
set java_opts=%java_opts% -Dnode.logdir=../logs/
set java_opts=%java_opts% -Dlogback.configurationFile=../config/xdm-cache-logging.xml
set java_opts=%java_opts% -Dxdm.config.path=../config
set java_opts=%java_opts% -Dxdm.server.properties.file=xdm-server-first.properties

rem jmx_port=$(( 6200 + $nodeNum ))
rem set java_opts=%java_opts% -Dcom.sun.management.jmxremote.authenticate=false
rem set java_opts=%java_opts% -Dcom.sun.management.jmxremote.ssl=false

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.xdm.cache.hazelcast.XDMCacheServer %*

endlocal
@echo on

