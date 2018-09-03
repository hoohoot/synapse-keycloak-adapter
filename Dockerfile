FROM openjdk:8-jre-alpine

ENV VERTICLE_FILE keycloak-synapse-adapter-1.0.0-fat.jar

ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

COPY target/$VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $VERTICLE_FILE"]