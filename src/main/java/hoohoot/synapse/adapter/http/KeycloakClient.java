package hoohoot.synapse.adapter.http;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

import java.util.Base64;


class KeycloakClient extends AbstractVerticle {

  private WebClient webClient;
  private MainConfiguration config;

  protected KeycloakClient(WebClient webClient, MainConfiguration config) {
    this.webClient = webClient;
    this.config = config;
  }

  public void requestBearerToken(RoutingContext routingContext) {

    JsonObject userInfo = routingContext.getBodyAsJson().getJsonObject("user");
    String username = userInfo.getString("id");

    final String keycloakPassword = userInfo.getString("password");
    username = username.replace(":" + config.SYNAPSE_HOST, "");
    username = username.substring(1);

    final String keycloakUsername = username;

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("username", keycloakUsername);
    form.add("password", keycloakPassword);
    form.add("grant_type", "password");

    webClient.post(443, config.KEYCLOAK_HOST, config.KEYCLOAK_CLIENT_URI)
      .putHeader("Authorization", config.KEYCLOAK_CLIENT_BASIC)
      .putHeader("content-type", "application/x-www-form-urlencoded")
      .ssl(true)
      .sendForm(form, ar -> {
        if (ar.succeeded()) {
          if (ar.result().statusCode() == 200) {
            JsonObject keycloakResponse = ar.result().bodyAsJsonObject();
            UserInfoDigest userinfo = extractTokentInfo(keycloakResponse.getString("access_token"));
            JsonObject synapseLoginBody = buildSynapseLoginJsonBody(userinfo);
            routingContext.response().end(synapseLoginBody.encodePrettily());
          } else if (ar.result().statusCode() == 401) {
            UserInfoDigest userInfoDigest = new UserInfoDigest("", keycloakUsername, false);
            JsonObject synapseLoginBody = buildSynapseLoginJsonBody(userInfoDigest);
            routingContext.response().end(synapseLoginBody.encodePrettily());
          }
          ar.succeeded();
        } else {
          System.out.println("nope");
          routingContext.response().end("error");
        }
      });
  }

  /**
   * This method decode the access token payload and return a UserInfoDigest object
   * with the necessary synapse login info
   * @param bearer the access token delivered by KC
   * @return UserInfoDigest
   */
  public UserInfoDigest extractTokentInfo(String bearer) {
    String[] splittedJWT = bearer.split("\\.");
    byte[] decodedBytes = Base64.getDecoder().decode(splittedJWT[1]);
    String decodedPayload = new String(decodedBytes);
    JsonObject payload = new JsonObject(decodedPayload);

    String preferedUsername = payload.getString("preferred_username");
    String email = payload.getString("email");

    if (payload.getJsonObject("realm_access")
      .getJsonArray("roles")
      .contains("matrix_user")) {
      return new UserInfoDigest(email, preferedUsername, true);
    }
    else {
      return new UserInfoDigest(email, preferedUsername, false);
    }

  }

  /**
   * Build the JsonObject expected synapse to log the user in
   * @param userInfoDigest UserInfoDigest
   * @return JsonObject containing synapse login request
   */
  public JsonObject buildSynapseLoginJsonBody(UserInfoDigest userInfoDigest) {

    JsonObject auth = new JsonObject();

    auth.put("success", userInfoDigest.isMatrixUser());
    auth.put("mxid", "@" + userInfoDigest.getPreferedUserName() + ":" + config.SYNAPSE_URL);

    JsonObject profile = new JsonObject();
    profile.put("display_name", userInfoDigest.getPreferedUserName());

    JsonArray treePids = new JsonArray();

    JsonObject mailPid = new JsonObject();
    mailPid.put("medium", "email");
    mailPid.put("address", userInfoDigest.getEmail());

    treePids.add(mailPid);

    profile.put("three_pids", treePids);
    auth.put("profile", profile);
    return new JsonObject().put("auth", auth);
  }
}
