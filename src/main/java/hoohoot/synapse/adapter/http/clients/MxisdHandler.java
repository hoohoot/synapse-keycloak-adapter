package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import hoohoot.synapse.adapter.http.HttpJsonErrors;
import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.lang.management.BufferPoolMXBean;

public class MxisdHandler extends AbstractVerticle {
    private final JsonHelper helper;
    private Logger logger = LoggerFactory.getLogger(MxisdHandler.class);

    private WebClient webClient;
    private MainConfiguration config;
    private final String loginUri;
    private final String searchUri;
    private final String searchBySinglePIDUri;
    private final String bulkPIDSearchUri;

    public MxisdHandler(WebClient webClient, MainConfiguration config, JsonHelper helper) {
        this.helper = helper;
        this.webClient = webClient;
        this.config = config;
        this.loginUri = "/auth/realms/testing/protocol/openid-connect/token";
        this.searchUri = "";
        this.searchBySinglePIDUri = "";
        this.bulkPIDSearchUri = "";
    }

    public void loginHandler(RoutingContext routingContext) {
        JsonObject authRequestBody = routingContext.getBodyAsJson().getJsonObject("auth");

        final String keycloakPassword = authRequestBody.getString("password");
        final String username = authRequestBody.getString("localpart");

        MultiMap form = helper.getUserForm(keycloakPassword, username);

        HttpRequest<Buffer> request = generateAccessTokenRequest(loginUri);

        request
                .sendForm(form, ar -> {
                    if (ar.succeeded()) {
                        if (ar.result().statusCode() == 200) {
                            logger.info("pouet");
                            JsonObject keycloakResponse = ar.result().bodyAsJsonObject();
                            UserInfoDigest userinfo = helper.extractTokentInfo(keycloakResponse
                                    .getString("access_token"));
                            routingContext.response().setStatusCode(200);
                            routingContext.response().end(
                                    helper.buildSynapseLoginJsonBody(userinfo)
                                            .encodePrettily());

                        } else if (ar.result().statusCode() == 401) {
                            logger.info("pouetpouet");

                            UserInfoDigest userInfoDigest = new UserInfoDigest(
                                    "", form.get("username"), false);
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

    private HttpRequest<Buffer> generateAccessTokenRequest(String uri) {
        HttpRequest<Buffer> request = this.webClient.post(443, config.KEYCLOAK_HOST, uri);
        request.headers().add("Authorization", config.KEYCLOAK_CLIENT_BASIC);
        request.headers().add("content-type", "application/x-www-form-urlencoded");
        request.ssl(true);
        request.method(HttpMethod.POST);
        return request;
    }
}
