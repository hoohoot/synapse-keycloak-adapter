package hoohoot.synapse.adapter.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;


class KeycloakClient extends AbstractVerticle {

  private WebClient webClient;
  private JsonObject config;

  protected KeycloakClient(WebClient webClient, JsonObject config) {
    this.webClient = webClient;
    this.config = config;
  }

  public void requestBearerToken(RoutingContext routingContext) {

    final JsonObject keycloakConfig = config.getJsonObject("keycloak");
    final String keycloakHost = keycloakConfig.getString("host");
    final String keycloakClientUri = keycloakConfig.getString("client.uri");
    final String keycloakBasicAuth = keycloakConfig.getString("client.basic");

    final String synapseHost = config.getString("synapse.host");

    JsonObject userInfo = routingContext.getBodyAsJson();
    String username = userInfo.getJsonObject("user").getString("id");

    final String keycloakPassword = userInfo.getJsonObject("user").getString("password");
    username = username.replace(":" + synapseHost, "");
    username = username.substring(1);
    System.out.println(username);
    final String keycloakUsername = username;

    System.out.println(userInfo);
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("username", keycloakUsername);
    form.add("password", keycloakPassword);
    form.add("grant_type", "password");

    webClient.post(443, keycloakHost, keycloakClientUri)
      .putHeader("Authorization", keycloakBasicAuth)
      .putHeader("content-type", "application/x-www-form-urlencoded")
      .ssl(true)
      .sendForm(form, ar -> {
        if (ar.succeeded()) {
          System.out.println(ar.result().statusCode());
          System.out.println(ar.result().bodyAsJsonObject());
          ar.succeeded();
        } else {
          System.out.println("nope");
        }
      });
  }
}
