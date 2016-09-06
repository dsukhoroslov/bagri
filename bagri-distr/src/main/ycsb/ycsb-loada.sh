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
#CLASSPATH="${CLASSPATH}:${apphome}/lib/bagri-test-tpox-1.0.0-EA2.jar"
CLASSPATH="${CLASSPATH}:${apphome}/lib/*"
export CLASSPATH

. "set-ycsb-env.conf"


# insert securities to the cache
$RUN_JAVA -server $JAVA_OPTS com.yahoo.ycsb.Client -load -threads 50 -P workloada

# perform workload loopig by user count
a=50
while [ $a -le 200 ]
do
   echo $a
   $RUN_JAVA -server $JAVA_OPTS com.yahoo.ycsb.Client -threads $a -P workloada  
   a=`expr $a + 15`
done

