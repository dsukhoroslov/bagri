FROM java:openjdk-8-jre

# RUN mkdir -p bagri
# ADD bagri-1.2.0-SNAPSHOT.zip /bagri

ADD bagri-1.2.0-SNAPSHOT.zip /
RUN unzip -q bagri-1.2.0-SNAPSHOT.zip && rm bagri-1.2.0-SNAPSHOT.zip

WORKDIR /bagri-1.2.0-SNAPSHOT

# Start Bagri server.
ENTRYPOINT ["./bin/bgcache.sh"]
CMD ["start", "default", "0"]
EXPOSE 3330 10500

 