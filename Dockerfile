# Extend vert.x image
FROM vertx/vertx3

ENV VERTICLE_NAME hoohoot.synapse.adapter.http.MainVerticle
ENV VERTICLE_FILE target/keycloak-synapse-adapter-1.0.0-SNAPSHOT.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

COPY $VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]