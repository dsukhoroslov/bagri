@echo off
@
@rem This will start XDM server application
@
setlocal

rem specify application home
set app_home=..\

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

if "%1"=="" (set profile=empty) else (set profile=%1)

if not exist ..\config\%profile%.properties goto usage

if "%2"=="" (set node_num=0) else (set node_num=%2)

rem specify the JVM heap size
if "%3"=="" (set memory=4g) else (set memory=%3)

rem specify JVM memory options
set java_opts=-Xms%memory% -Xmx%memory% -XX:NewSize=192m -XX:MaxNewSize=192m
rem set java_opts=%java_opts% -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=2048

rem specify JVM GC options
set java_opts=%java_opts% -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+ExplicitGCInvokesConcurrent
set java_opts=%java_opts% -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80
set java_opts=%java_opts% -XX:+CMSScavengeBeforeRemark -XX:+CMSConcurrentMTEnabled -XX:+ExplicitGCInvokesConcurrent

mkdir ..\logs\%profile%\gc

rem specify JVM GC logging
set java_opts=%java_opts% -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError 
set java_opts=%java_opts% -Xloggc:..\logs\%profile%\gc\gc.%node_num%.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=256M
   
rem specify logging & XDM options
set java_opts=%java_opts% -Dnode.name=%profile% -Dnode.instance=%node_num%
set java_opts=%java_opts% -Dnode.logdir=..\logs\%profile% -Dbdb.log.level=info
set java_opts=%java_opts% -Dlogback.configurationFile=..\config\hz-logging.xml
set java_opts=%java_opts% -Dbdb.config.path=..\config
set java_opts=%java_opts% -Dbdb.config.context.file=file:..\config\spring\cache-system-context.xml
set java_opts=%java_opts% -Dbdb.config.properties.file=%profile%.properties
set java_opts=%java_opts% -Dbdb.config.filename=..\config\config.xml
set java_opts=%java_opts% -Dbdb.access.filename=..\config\access.xml
set java_opts=%java_opts% -Dbdb.node.instance=%node_num%

rem specify JMX options
set java_opts=%java_opts% -Dcom.sun.management.jmxremote.authenticate=false
set java_opts=%java_opts% -Dcom.sun.management.jmxremote.ssl=false
set java_opts=%java_opts% -Dcom.sun.management.jmxremote
rem jmx_port=$(( 3333 + $nodeNum ))
rem set java_opts=%java_opts% -Dcom.sun.management.jmxremote.port=${jmx_port}

rem set java_opts=%java_opts% %*
rem for /f "usebackq tokens=1*" %%i in ('%*') do set params=%%j

rem setlocal enableDelayedExpansion

if "%4"=="" (
  if "empty"=="%profile%" (
    set java_opts=%java_opts% -Dbdb.cluster.members=localhost
    set java_opts=%java_opts% -Dbdb.cluster.node.schemas=default
  )
) else (
rem  echo %*
  for /f "usebackq tokens=4,5,6,7 delims= " %%a in ('%*') do (
    set java_opts=%java_opts% %%a=%%b
    set java_opts=%java_opts% %%c=%%d
  )
)

echo %java_opts%

"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.server.hazelcast.BagriCacheServer %*

goto exit

:usage

echo ERROR: config file ..\config\%profile%.properties not found

echo Usage:
echo   ^<app_home^>\bin\bgcache.cmd profile-name.properties instance-num
goto exit

:exit
endlocal
@echo on
