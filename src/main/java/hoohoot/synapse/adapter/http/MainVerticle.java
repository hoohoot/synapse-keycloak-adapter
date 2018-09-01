package hoohoot.synapse.adapter.http;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;

import javax.naming.ConfigurationException;

public class MainVerticle extends AbstractVerticle {

  Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws ConfigurationException {
    MainConfiguration conf = new MainConfiguration();

    WebClient webClient = WebClient.create(vertx, new WebClientOptions()
      .setSsl(conf.SSL_ACTIVE)
      .setUserAgent(conf.USER_AGENT));

    KeycloakClient keycloakClient = new KeycloakClient(webClient, conf);

    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    final String loginUri = "/_matrix-internal/identity/v1/check_credentials";
    router.route(loginUri).handler(BodyHandler.create());
    router.post(loginUri)
      .handler(keycloakClient::requestBearerToken);
    router.get("/ping").handler(res -> {
      res.response().end(new JsonObject()
        .put("ping", "pong")
        .encodePrettily());
    });


    server.requestHandler(router::accept)
      .listen(conf.SERVER_PORT, http -> {
        if (http.succeeded()) {
          startFuture.complete();
          logger.info("HTTP server started on http://localhost:{}", conf.SERVER_PORT);
        } else {
          logger.info("HTTP server failed to start on http://localhost:{}", conf.SERVER_PORT);
          startFuture.fail(http.cause());
        }
      });
  }

}
