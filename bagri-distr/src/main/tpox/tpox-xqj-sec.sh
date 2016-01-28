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
    echo "please install Java 1.6 or higher!!!"
    exit 1
fi


appname="`basename $0`"
appname=${appname/\.sh/}
apphome="`cd \`dirname $0\`/.. && pwd && cd - >/dev/null`"


CLASSPATH="${apphome}/config/*"
CLASSPATH="${CLASSPATH}:${apphome}/lib/bagri-test-tpox-0.5.1-SNAPSHOT.jar"
CLASSPATH="${CLASSPATH}:${apphome}/lib/*"
export CLASSPATH

TPOX_HOME="${apphome}/../../TPoX"
export TPOX_HOME

#. "${apphome}/bin/${appname}.conf"
. "set-tpox-env.conf"

# The number of securities is fixed to 20,833.  Therefore, either 
# "-u 83 -tr 251"  or "-u 251 -tr 83" can be use to insert the 20833
# security documents  (because 83 * 251 = 20833). 4166*5 = 3472*6 = 2976*7 = 2604*8 = 2314*9

#insert securities to the cache
$RUN_JAVA -server $JAVA_OPTS net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insSecurity.xml -tr 2604 -u 8

#rem perform queries loopig by user count
a=5
while [ $a -le 200 ]
do
   echo $a
   $RUN_JAVA -server $JAVA_OPTS net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/securities.xml -u $a -r 10 -pc 95 -cl 99
   a=`expr $a + 15`
done

