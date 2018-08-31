package hoohoot.synapse.adapter;

import hoohoot.synapse.adapter.http.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  private JsonObject config;
  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("config.json").getFile());

    config = new JsonObject(vertx.fileSystem().readFileBlocking(file.toString()));

    vertx.deployVerticle(new MainVerticle(), new DeploymentOptions().setConfig(config),
      testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  @DisplayName("Should start a Web Server on port 8080")
  @Timeout(value = 1500, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) throws Throwable {
    vertx.createHttpClient().getNow(8080, "localhost", "/ping", response -> testContext.verify(() -> {
      assertEquals(200, response.statusCode());
      response.handler(body -> {
        assertEquals("pong", body.toJsonObject().getString("ping"));
        testContext.completeNow();
      });
    }));
  }

}
