@echo off
@
@rem This will start XDM server application
@
setlocal

@rem specify application home
set app_home=../

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

@rem specify the JVM heap size
set memory=4g

@rem specify JVM memory options
set java_opts=-Xms%memory% -Xmx%memory% -XX:NewSize=192m -XX:MaxNewSize=192m
set java_opts=%java_opts% -XX:PermSize=192m -XX:MaxPermSize=192m -XX:+ExplicitGCInvokesConcurrent
@rem set java_opts=%java_opts% -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=2048

@rem specify JVM GC options
set java_opts=%java_opts% -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+ExplicitGCInvokesConcurrent
set java_opts=%java_opts% -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80
set java_opts=%java_opts% -XX:+CMSScavengeBeforeRemark -XX:+CMSConcurrentMTEnabled

@rem specify JVM GC logging
set java_opts=%java_opts% -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError 
set java_opts=%java_opts% -Xloggc:../logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=256M
   
@rem specify logging & XDM options
set java_opts=%java_opts% -Dnode.name=first -Dnode.logdir=../logs -Dxdm.log.level=info
set java_opts=%java_opts% -Dlogback.configurationFile=../config/xdm-cache-logging.xml
set java_opts=%java_opts% -Dxdm.config.path=../config
set java_opts=%java_opts% -Dxdm.config.context.file=spring/system-server-context.xml
set java_opts=%java_opts% -Dxdm.config.properties.file=xdm-third.properties
set java_opts=%java_opts% -Dxdm.config.filename=../config/config.xml

@rem specify JMX options
set java_opts=%java_opts% -Dcom.sun.management.jmxremote.authenticate=false
set java_opts=%java_opts% -Dcom.sun.management.jmxremote.ssl=false
set java_opts=%java_opts% -Dcom.sun.management.jmxremote
@rem jmx_port=$(( 3333 + $nodeNum ))
@rem set java_opts=%java_opts% -Dcom.sun.management.jmxremote.port=${jmx_port}

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.xdm.cache.hazelcast.XDMCacheServer %*

endlocal
@echo on
