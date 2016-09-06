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
CLASSPATH="${CLASSPATH}:${apphome}/lib/bagri-test-tpox-1.0.0-EA2.jar"
CLASSPATH="${CLASSPATH}:${apphome}/lib/*"
export CLASSPATH

TPOX_HOME="${apphome}/../../TPoX"
export TPOX_HOME

. "set-tpox-env.conf"

#insert orders to the cache
$RUN_JAVA -server $JAVA_OPTS net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insOrder.xml -tr 5000 -u 50

#rem perform queries looping by user count
a=5
while [ $a -le 200 ]
do
   echo $a
   $RUN_JAVA -server $JAVA_OPTS net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/orders.xml -u $a -r 10 -pc 95 -cl 99
   a=`expr $a + 15`
done

                                      
