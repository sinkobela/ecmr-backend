FROM eclipse-temurin:21_35-jre-jammy

ARG APPLICATION_VERSION=n.a
ENV APPLICATION_VERSION=$APPLICATION_VERSION


COPY startup.sh /root/startup.sh
RUN chmod +x /root/startup.sh \
&& mkdir /app \
&& mkdir /usr/local/cacerts
COPY /src/main/resources/eseal/test-keystore.jks /app

USER root
COPY target/*.jar /app/runme.jar

ENTRYPOINT ["/root/startup.sh"]
CMD ["/app/runme.jar"]
