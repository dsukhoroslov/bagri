#!/bin/bash
PID=0
sigterm_handler() {
  echo "Bagri Term Handler received shutdown signal. Signaling Bagri instance on PID: ${PID}"
  if [ ${PID} -ne 0 ]; then
    kill "${PID}"
  fi
}

# if we receive SIGTERM (from docker stop) or SIGINT (ctrl+c if not running as daemon)
# trap the signal and delegate to sigterm_handler function, which will notify hazelcast instance process
trap sigterm_handler SIGTERM SIGINT


nodeName=first
if [ $# -gt 0 ]
then
   nodeName=$1
fi

nodeNum=0
if [ $# -gt 1 ]
then
   nodeNum=$2
fi

mkdir -p /$BG_HOME/logs/$nodeName
mkdir -p /$BG_HOME/logs/$nodeName/gc

CLASSPATH=/$BG_HOME/config/*:/$BG_HOME/lib/*
export CLASSPATH

jmx_port=3431

JAVA_OPTS="-Xms2g -Xmx2g -XX:NewSize=64m -XX:MaxNewSize=64m"
JAVA_OPTS="$JAVA_OPTS -XX:+UseParNewGC -XX:+UseConcMarkSweepGC"
JAVA_OPTS="$JAVA_OPTS -XX:+ExplicitGCInvokesConcurrent -XX:+UseCMSInitiatingOccupancyOnly"
JAVA_OPTS="$JAVA_OPTS -XX:CMSInitiatingOccupancyFraction=80 -XX:+CMSScavengeBeforeRemark"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps"
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="$JAVA_OPTS -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=256M"
JAVA_OPTS="$JAVA_OPTS -Xloggc:/$BG_HOME/logs/$nodeName/gc/gc.$nodeNum.log"
JAVA_OPTS="$JAVA_OPTS -Dnode.logdir=/$BG_HOME/logs/$nodeName"
JAVA_OPTS="$JAVA_OPTS -Dnode.name=$nodeName"
JAVA_OPTS="$JAVA_OPTS -Dnode.instance=$nodeNum"
JAVA_OPTS="$JAVA_OPTS -Dlogback.configurationFile=/$BG_HOME/config/hz-logging.xml"
#JAVA_OPTS="$JAVA_OPTS -Dhz.log.level=debug"
JAVA_OPTS="$JAVA_OPTS -Dbdb.log.level=info"
JAVA_OPTS="$JAVA_OPTS -Dbdb.config.path=/$BG_HOME/config"
JAVA_OPTS="$JAVA_OPTS -Dbdb.config.context.file=file:/$BG_HOME/config/spring/cache-system-context.xml"
JAVA_OPTS="$JAVA_OPTS -Dbdb.config.properties.file=$nodeName.properties"
JAVA_OPTS="$JAVA_OPTS -Dbdb.config.filename=/$BG_HOME/config/config.xml"
JAVA_OPTS="$JAVA_OPTS -Dbdb.access.filename=/$BG_HOME/config/access.xml"
JAVA_OPTS="$JAVA_OPTS -Dbdb.node.instance=$nodeNum"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.local.only=false"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=$jmx_port"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.rmi.port=$jmx_port"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=192.168.99.100"

echo "Starting Bagri with options: $JAVA_OPTS"
echo "and classpath: $CLASSPATH"

java -server $JAVA_OPTS com.bagri.server.hazelcast.BagriCacheServer
PID="$!"
#PID_FILE=$BG_HOME/bg_instance.pid
#echo ${PID} > ${PID_FILE}

# wait on bagri instance process
wait ${PID}
# if a signal came up, remove previous traps on signals and wait again (noop if process stopped already)
trap - SIGTERM SIGINT
wait ${PID}