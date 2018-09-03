package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import hoohoot.synapse.adapter.http.helpers.HttpJsonErrors;
import hoohoot.synapse.adapter.http.helpers.JoltMapper;
import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

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

        this.loginUri = "/auth/realms/" + config.REALM + "/protocol/openid-connect/token";
        this.searchUri = "/auth/admin/realms/" + config.REALM + "/users";
        this.searchBySinglePIDUri = "";
        this.bulkPIDSearchUri = "";
    }

    public void loginHandler(RoutingContext routingContext) {
        JsonObject authRequestBody = routingContext.getBodyAsJson().getJsonObject("auth");

        final String keycloakPassword = authRequestBody.getString("password");
        final String username = authRequestBody.getString("localpart");

        MultiMap form = helper.getUserForm(keycloakPassword, username);
        logger.info("keycloak host : " + config.KEYCLOAK_HOST);
        logger.info("keycloak uri : " + config.REALM);
        logger.info("received login request with headers : " + routingContext.request().headers());
        logger.info("Processing access token request to" + config.KEYCLOAK_HOST);

        HttpRequest<Buffer> request = generateAccessTokenRequest(loginUri);

        request
                .sendForm(form, ar -> {
                    if (ar.succeeded()) {
                        logger.info(config.KEYCLOAK_HOST + "responded with status code " + ar.result().statusCode());
                        if (ar.result().statusCode() == 200) {
                            logger.info("pouet");
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
                        respondWithStatusCode502(routingContext);
                    }
                });
    }

    public void getSearchAccessToken(RoutingContext routingContext) {
        HttpRequest<Buffer> request = generateAccessTokenRequest(loginUri);
        MultiMap form = helper.getUserForm(config.KEYCLOAK_SEARCH_PASSWORD, config.KEYCLOAK_SEARCH_USERNAME);
        logger.info(form);

        request.sendForm(form, ar -> {
            if (ar.succeeded()) {
                logger.info(ar.result().statusCode());
                if (ar.result().statusCode() == 200) {
                    routingContext.put("access_token", ar.result().bodyAsJsonObject().getString("access_token"));
                    routingContext.next();
                } else {
                    logger.error("Couldn't get access token for search user");
                    logger.info(routingContext.response().getStatusCode());
                    routingContext.response().end("placeholder");
                }
            } else {
                respondWithStatusCode502(routingContext);
            }
        });
    }

    public void searchHandler(RoutingContext routingContext) {
        String searchTerm = routingContext.getBodyAsJson().getString("search_term");
        String accessToken = routingContext.get("access_token");

        HttpRequest<Buffer> request = generateSearchRequest(searchTerm, searchUri, accessToken);
        request.send(ar -> {
            if (ar.succeeded()) {
                logger.info(ar.result().statusCode());
                if (ar.result().statusCode() == 200) {

                    JsonObject searchResult = new JsonObject()
                            .put("results", ar.result().bodyAsJsonArray());

                    JsonArray synapsifiedSearchResponse = JoltMapper
                            .transform(searchResult, "search-spec.json");

                    final JsonArray results = synapsifiedSearchResponse
                            .getJsonObject(0)
                            .getJsonArray("results");

                    JsonObject finalResponse = new JsonObject()
                            .put("limited", false)
                            .put("results", results);
                    logger.info(synapsifiedSearchResponse.encodePrettily());

                    routingContext.response().setStatusCode(200);
                    routingContext.response().end(finalResponse.encodePrettily());
                } else {
                    // TODO
                }
            } else {
                respondWithStatusCode502(routingContext);
            }
        });
    }

    private void respondWithStatusCode502(RoutingContext routingContext) {
        logger.warn("Couldn't get response from keycloak");
        routingContext.response().setStatusCode(502);
        routingContext.response().end(HttpJsonErrors.BADGATEWAY.encodePrettily());
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

    private HttpRequest<Buffer> generateSearchRequest(String searchTerm, String uri, String accessToken) {
        HttpRequest<Buffer> request = this.webClient.get(443, config.KEYCLOAK_HOST, uri);
        request.headers().add("Authorization", "Bearer " + accessToken);
        request.addQueryParam("search", searchTerm);
        request.method(HttpMethod.GET);

        return request;
    }
}
