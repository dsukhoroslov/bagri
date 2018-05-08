FROM java:openjdk-8-jre

ENV BG_VERSION 1.2.1
ENV BG_HOME bagri-$BG_VERSION

LABEL maintainer "Bagri Project Team: support@bagridb.com"
LABEL version "$BG_VERSION"

ADD bagri-$BG_VERSION.zip /
RUN unzip -q bagri-$BG_VERSION.zip && rm bagri-$BG_VERSION.zip

RUN rm -rf /$BG_HOME/distr && rm -rf /$BG_HOME/docs && rm -rf /$BG_HOME/samples
RUN mkdir /$BG_HOME/logs && mkdir /$BG_HOME/logs/first && mkdir /$BG_HOME/logs/first/cache && mkdir /$BG_HOME/logs/first/gc

# looks like it is not usable on the Windows platform
VOLUME /$BG_HOME

WORKDIR /$BG_HOME/bin

# Start Bagri server.
ENTRYPOINT ["./bgserver.sh"]
CMD ["first", "0"]
EXPOSE 3331 3431 10500
  