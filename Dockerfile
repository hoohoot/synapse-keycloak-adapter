FROM openjdk:8-jre-alpine

ENV VERTICLE_NAME hoohoot.synapse.adapter.http.server.MainVerticle
ENV VERTICLE_FILE target/keycloak-synapse-adapter-1.0.0-fat.jar
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

COPY target/$VERTICLE_FILE $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $VERTICLE_FILE"]