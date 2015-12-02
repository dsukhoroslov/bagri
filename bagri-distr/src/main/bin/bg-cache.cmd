@echo off
@
@rem This will start XDM server application
@
setlocal

rem specify application home
set app_home=../

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

if "%1"=="" (set node_name=first) else (set node_name=%1)

if not exist ..\config\%node_name%.properties goto usage

if "%2"=="" (set node_num=0) else (set node_num=%2)

rem specify the JVM heap size
set memory=4g

rem specify JVM memory options
set java_opts=-Xms%memory% -Xmx%memory% -XX:NewSize=192m -XX:MaxNewSize=192m
set java_opts=%java_opts% -XX:PermSize=192m -XX:MaxPermSize=192m -XX:+ExplicitGCInvokesConcurrent
rem set java_opts=%java_opts% -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=2048

rem specify JVM GC options
set java_opts=%java_opts% -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+ExplicitGCInvokesConcurrent
set java_opts=%java_opts% -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80
set java_opts=%java_opts% -XX:+CMSScavengeBeforeRemark -XX:+CMSConcurrentMTEnabled

mkdir ..\logs\%node_name%\gc

rem specify JVM GC logging
set java_opts=%java_opts% -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError 
set java_opts=%java_opts% -Xloggc:../logs/%node_name%/gc/gc.%node_num%.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=256M
   
rem specify logging & XDM options
set java_opts=%java_opts% -Dnode.name=%node_name% -Dnode.instance=%node_num%
set java_opts=%java_opts% -Dnode.logdir=../logs/%node_name% -Dxdm.log.level=info
set java_opts=%java_opts% -Dlogback.configurationFile=../config/hz-logging.xml
set java_opts=%java_opts% -Dxdm.config.path=../config
set java_opts=%java_opts% -Dxdm.config.context.file=spring/cache-system-context.xml
set java_opts=%java_opts% -Dxdm.config.properties.file=%node_name%.properties
set java_opts=%java_opts% -Dxdm.config.filename=../config/config.xml
set java_opts=%java_opts% -Dxdm.access.filename=../config/access.xml
set java_opts=%java_opts% -Dxdm.node.instance=%node_num%

rem specify JMX options
set java_opts=%java_opts% -Dcom.sun.management.jmxremote.authenticate=false
set java_opts=%java_opts% -Dcom.sun.management.jmxremote.ssl=false
set java_opts=%java_opts% -Dcom.sun.management.jmxremote
rem jmx_port=$(( 3333 + $nodeNum ))
rem set java_opts=%java_opts% -Dcom.sun.management.jmxremote.port=${jmx_port}

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.xdm.cache.hazelcast.XDMCacheServer %*

goto exit

:usage

echo ERROR: config file ..\config\%node_name%.properties not found

echo Usage:
echo   ^<app_home^>\bin\bg-cache.cmd properties-name instance-num
goto exit

:exit
endlocal
@echo on
