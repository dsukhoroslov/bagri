#!/bin/sh

if [ $JAVA_HOME ]
then
    echo "JAVA_HOME found at $JAVA_HOME"
    RUN_JAVA=$JAVA_HOME/bin/java
else
    echo "JAVA_HOME environment variable not available."
    RUN_JAVA=`which java 2>/dev/null`
fi

if [ -z $RUN_JAVA ]
then
    echo "JAVA could not be found in your system."
    echo "please install Java 1.7 or higher!!!"
    exit 1
fi


appname="`basename $0`"
appname=${appname/\.sh/}
apphome="`cd \`dirname $0\`/.. && pwd && cd - >/dev/null`"


CLASSPATH="${apphome}/config/*"
CLASSPATH="${CLASSPATH}:${apphome}/lib/bagri-test-tpox-1.2.0.jar"
CLASSPATH="${CLASSPATH}:${apphome}/lib/*"
export CLASSPATH

TPOX_HOME="${apphome}/../../TPoX"
export TPOX_HOME

. "set-tpox-env.conf"

#rem perform queries looping by user count
a=10
count=0
while [ $a -le 100 ]
do
    echo $a
    count=`expr $a \* 100`
    $RUN_JAVA -server $JAVA_OPTS net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insOrder.xml -tr $count -u 100

    $RUN_JAVA -server $JAVA_OPTS net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/orders-100.xml -u 80 -r 10
    $RUN_JAVA -server $JAVA_OPTS com.bagri.test.tpox.StatisticsCollector $admin_addr $schema QueryManagement executeXQuery Orders=${count}0 ./stats.txt true
 
    a=`expr $a + 10`
done

