package hoohoot.synapse.adapter.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) {

    JsonObject clientConfig = config();
    WebClient webClient = WebClient.create(vertx, new WebClientOptions()
      .setSsl(true)
      .setUserAgent("synapse-adapter"));
    KeycloakClient keycloakClient = new KeycloakClient(webClient, clientConfig);

    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());
    router.post("/_matrix-internal/identity/v1/check_credentials")
      .handler(keycloakClient::requestBearerToken);

    Integer portNumber = config().getInteger("http.port");

    server.requestHandler(router::accept)
      .listen(portNumber, http -> {
        if (http.succeeded()) {
          startFuture.complete();
          System.out.println("HTTP server started on http://localhost:8080");
        } else {
          startFuture.fail(http.cause());
        }
      });
  }

}
