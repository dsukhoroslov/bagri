FROM java:openjdk-8-jre

ADD bagri-1.2.0-SNAPSHOT.zip /
RUN unzip -q bagri-1.2.0-SNAPSHOT.zip && rm bagri-1.2.0-SNAPSHOT.zip

RUN mkdir -p /bagri-1.2.0-SNAPSHOT/logs
RUN mkdir -p /bagri-1.2.0-SNAPSHOT/logs/gc
RUN mkdir -p /bagri-1.2.0-SNAPSHOT/run

VOLUME /bagri-1.2.0-SNAPSHOT/logs

WORKDIR /bagri-1.2.0-SNAPSHOT

# Start Bagri server.
ENTRYPOINT ["./bin/bgcache.sh"]
CMD ["start", "default", "0"]
EXPOSE 3330 10500
 