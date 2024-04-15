FROM eclipse-temurin:21_35-jre-jammy

ARG APPLICATION_VERSION=n.a
ENV APPLICATION_VERSION=$APPLICATION_VERSION

RUN mkdir /usr/local/cacerts
COPY startup.sh /root/startup.sh
RUN chmod +x /root/startup.sh

RUN mkdir /app
USER root
COPY target/*.jar /app/runme.jar

ENTRYPOINT ["/root/startup.sh"]
CMD ["/app/runme.jar"]
