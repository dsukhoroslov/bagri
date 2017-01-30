FROM java:openjdk-8-jre

ENV BG_VERSION 1.2.0-SNAPSHOT
ENV BG_HOME bagri-$BG_VERSION

ADD bagri-$BG_VERSION.zip /
RUN unzip -q bagri-$BG_VERSION.zip && rm bagri-$BG_VERSION.zip

# how can we take them from CMD line?!
ENV NODE_NAME first
ENV NODE_NUM 0

RUN mkdir -p /bagri-$BG_VERSION/logs/$NODE_NAME
RUN mkdir -p /bagri-$BG_VERSION/logs/$NODE_NAME/gc
#RUN mkdir -p /bagri-$BG_VERSION/run

# looks like it is not usable on the Windows platform
#VOLUME /bagri-$BG_VERSION/logs


WORKDIR /bagri-$BG_VERSION/bin

#jmx_port=$(( 3431 + $nodeNum ))

ENV JAVA_OPTS -Xms1g -Xmx2g -XX:NewSize=64m -XX:MaxNewSize=64m \
-XX:+UseParNewGC -XX:+UseConcMarkSweepGC \ 
-XX:+ExplicitGCInvokesConcurrent -XX:+UseCMSInitiatingOccupancyOnly \
-XX:CMSInitiatingOccupancyFraction=80 -XX:+CMSScavengeBeforeRemark \
-XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=256M \
-Xloggc:../logs/$NODE_NAME/gc/gc.$NODE_NUM.log \
-Dnode.logdir=../logs/$NODE_NAME \
-Dnode.name=$NODE_NAME \
-Dnode.instance=$NODE_NUM \
-Dlogback.configurationFile=../config/hz-logging.xml \
-Dbdb.log.level=info \
-Dbdb.config.path=../config \
-Dbdb.config.context.file=spring/cache-system-context.xml \
-Dbdb.config.properties.file=$NODE_NAME.properties \
-Dbdb.config.filename=../config/config.xml \
-Dbdb.access.filename=../config/access.xml \
-Dbdb.node.instance=$NODE_NUM \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.port=3431 \ 
-Dcom.sun.management.jmxremote.rmi.port=3431 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Djava.rmi.server.hostname=192.168.99.100


ENV CLASSPATH /bagri-$BG_VERSION/config/*:/bagri-$BG_VERSION/lib/*

# Start Bagri server.
#ENTRYPOINT ["sh", "-c"]
CMD java -showversion -server $JAVA_OPTS -cp $CLASSPATH com.bagri.server.hazelcast.BagriCacheServer
EXPOSE 3331 3431 10500
 