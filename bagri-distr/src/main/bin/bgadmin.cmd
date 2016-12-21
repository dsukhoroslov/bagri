@echo off
@
@rem This will start XDM server application
@
setlocal

rem specify application home
set app_home=..\

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

if "%1"=="" (set node_num=0) else (set node_num=%1)

rem specify the JVM heap size
rem set memory=1028m

rem specify JVM memory options
set java_opts=-Xms512m -Xmx1024m

rem specify JVM GC options
set java_opts=%java_opts% -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+ExplicitGCInvokesConcurrent
set java_opts=%java_opts% -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80
set java_opts=%java_opts% -XX:+CMSScavengeBeforeRemark

mkdir ..\logs\admin\gc

rem specify JVM GC logging
set java_opts=%java_opts% -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError 
set java_opts=%java_opts% -Xloggc:..\logs\admin\gc\gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=256M
   
rem specify logging & XDM options -Dbdb.cluster.node.name=admin 
set java_opts=%java_opts% -Dnode.name=admin -Dnode.instance=%node_num%
set java_opts=%java_opts% -Dnode.logdir=..\logs\admin -Dbdb.log.level=info
set java_opts=%java_opts% -Dlogback.configurationFile=..\config\hz-logging.xml
set java_opts=%java_opts% -Dbdb.config.path=..\config
set java_opts=%java_opts% -Dbdb.config.context.file=file:..\config\spring\admin-system-context.xml
set java_opts=%java_opts% -Dbdb.config.properties.file=admin.properties
set java_opts=%java_opts% -Dbdb.cluster.node.role=admin -Dbdb.cluster.node.schemas=
set java_opts=%java_opts% -Dbdb.config.filename=..\config\config.xml -Dbdb.access.filename=..\config\access.xml

rem specify JMX options
set java_opts=%java_opts% -Dcom.sun.management.jmxremote.authenticate=false
set java_opts=%java_opts% -Dcom.sun.management.jmxremote.ssl=false
set java_opts=%java_opts% -Dcom.sun.management.jmxremote
rem jmx_port=$(( 3333 + $nodeNum ))
rem set java_opts=%java_opts% -Dcom.sun.management.jmxremote.port=${jmx_port}

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.server.hazelcast.BagriCacheServer %*

endlocal
@echo on
