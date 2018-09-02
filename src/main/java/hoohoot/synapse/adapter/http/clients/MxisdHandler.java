package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import hoohoot.synapse.adapter.http.HttpJsonErrors;
import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

public class MxisdHandler extends AbstractVerticle {
    private final JsonHelper helper;
    private Logger logger = LoggerFactory.getLogger(MxisdHandler.class);

    private WebClient webClient;
    private MainConfiguration config;

    public MxisdHandler(WebClient webClient, MainConfiguration config, JsonHelper helper) {
        this.helper = helper;
        this.webClient = webClient;
        this.config = config;
    }

    public void loginHandler(RoutingContext routingContext) {
        JsonObject authRequestBody = routingContext.getBodyAsJson().getJsonObject("auth");

        final String keycloakPassword = authRequestBody.getString("password");
        final String username = authRequestBody.getString("localpart");

        MultiMap form = helper.getUserForm(keycloakPassword, username);
        logger.info("Processing access token request to" + config.KEYCLOAK_HOST);
        webClient.post(443, config.KEYCLOAK_HOST, config.KEYCLOAK_CLIENT_URI)
                .putHeader("Authorization", config.KEYCLOAK_CLIENT_BASIC)
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .ssl(true)
                .sendForm(form, ar -> {
                    if (ar.succeeded()) {
                        logger.info(config.KEYCLOAK_HOST + "responded with status code " + ar.result().statusCode());
                        if (ar.result().statusCode() == 200) {
                            JsonObject keycloakResponse = ar.result().bodyAsJsonObject();
                            UserInfoDigest userinfo = helper.extractTokentInfo(keycloakResponse
                                    .getString("access_token"));
                            logger.info("response : " + helper.buildSynapseLoginJsonBody(userinfo)
                                    .encodePrettily());
                            routingContext.response().setStatusCode(200);
                            routingContext.response().end(
                                    helper.buildSynapseLoginJsonBody(userinfo)
                                            .encodePrettily());

                        } else if (ar.result().statusCode() == 401) {
                            logger.info(config.KEYCLOAK_HOST + " responded with status code " + ar.result().statusCode());
                            UserInfoDigest userInfoDigest = new UserInfoDigest(
                                    "", form.get("username"), false);
                            logger.info("response : " + helper.buildSynapseLoginJsonBody(userInfoDigest)
                                    .encodePrettily());
                            routingContext.response().setStatusCode(401);
                            routingContext.response().end(
                                    helper.buildSynapseLoginJsonBody(userInfoDigest)
                                            .encodePrettily());
                        }
                        ar.succeeded();
                    } else {
                        logger.warn("Couldn't get response from keycloak");
                        routingContext.response().setStatusCode(502);
                        routingContext.response().end(HttpJsonErrors.BADGATEWAY.encodePrettily());
                    }
                });
    }

    public void searchHandler(RoutingContext routingContext) {

    }

    public void singlePIDQueryHandler(RoutingContext routingContext) {


    }

    public void bulkPIDQueryHandler(RoutingContext routingContext) {

    }

    public static void healthCheckHandler(RoutingContext routingContext) {

    }
}
