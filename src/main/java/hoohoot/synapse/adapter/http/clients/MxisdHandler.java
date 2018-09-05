package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import hoohoot.synapse.adapter.http.helpers.JoltMapper;
import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static hoohoot.synapse.adapter.http.clients.JsonResponseService.respondWithStatusCode502;
import static hoohoot.synapse.adapter.http.helpers.Routes.MX_SINGLE_PID_URI;
import static hoohoot.synapse.adapter.http.helpers.Routes.MX_USER_SEARCH_URI;

public class MxisdHandler extends AbstractVerticle {
    private final JsonHelper jsonHelper;
    private final String keycloakUserSearchURI;
    private final OauthService oauthService;
    private Logger logger = LoggerFactory.getLogger(MxisdHandler.class);

    private WebClient webClient;
    private MainConfiguration config;

    public MxisdHandler(WebClient webClient, MainConfiguration config, JsonHelper jsonHelper, OauthService oauthService) {
        this.oauthService = oauthService;
        this.jsonHelper = jsonHelper;
        this.webClient = webClient;
        this.config = config;
        this.keycloakUserSearchURI = "/auth/admin/realms/" + config.REALM + "/users";
    }


    public void loginHandler(RoutingContext routingContext) {
        JsonObject authRequestBody = routingContext.getBodyAsJson().getJsonObject("auth");

        final String keycloakPassword = authRequestBody.getString("password");
        final String username = authRequestBody.getString("localpart");

        MultiMap form = jsonHelper.getUserForm(keycloakPassword, username);
        logger.info("keycloak host : " + config.KEYCLOAK_HOST);
        logger.info("keycloak uri : " + config.REALM);
        logger.info("received login request with headers : " + routingContext.request().headers());
        logger.info("Processing access token request to" + config.KEYCLOAK_HOST);

        HttpRequest<Buffer> request = oauthService.generateAccessTokenRequest();

        request
                .sendForm(form, ar -> {
                    if (ar.succeeded()) {
                        logger.info(config.KEYCLOAK_HOST + "responded with status code " + ar.result().statusCode());
                        if (ar.result().statusCode() == 200) {
                            logger.info("pouet");
                            JsonObject keycloakResponse = ar.result().bodyAsJsonObject();
                            UserInfoDigest userinfo = jsonHelper.extractTokentInfo(keycloakResponse
                                    .getString("access_token"));
                            logger.info("response : " + jsonHelper.buildSynapseLoginJsonBody(userinfo)
                                    .encodePrettily());
                            routingContext.response().setStatusCode(200);
                            routingContext.response().end(
                                    jsonHelper.buildSynapseLoginJsonBody(userinfo)
                                            .encodePrettily());

                        } else if (ar.result().statusCode() == 401) {
                            logger.info(config.KEYCLOAK_HOST + " responded with status code " + ar.result().statusCode());
                            UserInfoDigest userInfoDigest = new UserInfoDigest(
                                    "", form.get("username"), false);
                            logger.info("response : " + jsonHelper.buildSynapseLoginJsonBody(userInfoDigest)
                                    .encodePrettily());
                            routingContext.response().setStatusCode(401);
                            routingContext.response().end(
                                    jsonHelper.buildSynapseLoginJsonBody(userInfoDigest)
                                            .encodePrettily());
                        }
                        ar.succeeded();
                    } else {
                        respondWithStatusCode502(routingContext);
                    }
                });
    }


    public void searchHandler(RoutingContext routingContext, String joltSpec) {

        JsonObject requestBody = routingContext.getBodyAsJson();
        String accessToken = routingContext.get("access_token");
        String mxRequestUri = routingContext.request().uri();
        logger.info(routingContext.request().uri());

        HttpRequest<Buffer> request = generateSearchRequest(
                mxRequestUri,
                requestBody,
                accessToken);

        sendKeycloakSearchRequest(routingContext, joltSpec, request);
    }

    public void bulkSearchHandler(RoutingContext routingContext) {
        String accessToken = routingContext.get("access_token");
        logger.info(routingContext.request().uri());

        JsonArray bulkPID = routingContext.getBodyAsJson().getJsonArray("lookup");
        ArrayList<String> searchStrings = getSearchStringsFromBulkPids(bulkPID);
        List<Future> pidFutures = startRequestFuture(searchStrings, routingContext, accessToken);

        CompositeFuture.join(pidFutures).setHandler(ar -> {
            if (ar.succeeded()) {
                logger.info("composite future succeeded");
                JsonObject bulkResult = jsonHelper.buildBulkResponse(pidFutures);
                routingContext.response().end(bulkResult.encodePrettily());
            } else {
                logger.info("composite future failed");
                respondWithStatusCode502(routingContext);
            }
        });
    }

    private List<Future> startRequestFuture(ArrayList<String> searchStrings, RoutingContext context, String accesstoken) {
        return searchStrings.stream().map(email -> {

            Future<JsonObject> requestFuture = Future.future();
            HttpRequest<Buffer> request = initRequest(accesstoken);

            //forcing email on 3PID bulk search for now
            request.addQueryParam("email", email);

            request.send(ar -> {
                if (ar.succeeded()) {
                    logger.info("Success");
                    context.response().headers().add("content-type", "application/json");
                    context.response().setChunked(true);
                    final JsonObject userinfo = ar.result().bodyAsJsonArray().getJsonObject(0);
                    JsonObject pidREsponse = new JsonObject()
                            .put("type", "email")
                            .put("address", ar.result()
                                    .bodyAsJsonArray()
                                    .getJsonObject(0)
                                    .getString("email")
                            )
                            .put("id", new JsonObject()
                            .put("type", "localpart")
                            .put("value", userinfo
                                    .getString("username")));
                    requestFuture.complete(pidREsponse);
                } else {
                    requestFuture.fail("failed to query email " + email);
                }
            });

            return requestFuture;
        }).collect(Collectors.toList());
    }

    private ArrayList<String> getSearchStringsFromBulkPids(JsonArray bulkPID) {
        ArrayList<String> searchStrings = new ArrayList<>();

        for (int i = 0; i < bulkPID.size(); i++) {
            searchStrings.add(bulkPID.getJsonObject(i).getString("address"));
        }

        return searchStrings;
    }

    private void sendKeycloakSearchRequest(RoutingContext routingContext,
                                           String joltSpec,
                                           HttpRequest<Buffer> request) {
        request.send(ar -> {
            if (ar.succeeded()) {
                logger.info(ar.result().statusCode());
                if (ar.result().statusCode() == 200) {

                    JsonObject searchResult = new JsonObject()
                            .put("results", ar.result().bodyAsJsonArray());

                    JsonArray synapsifiedSearchResponse = JoltMapper
                            .transform(searchResult, joltSpec);

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

    public static void healthCheckHandler(RoutingContext routingContext) {

    }

    private HttpRequest<Buffer> generateSearchRequest(
            String mxRequestUri,
            JsonObject requestBody,
            String accessToken) {

        HttpRequest<Buffer> request = initRequest(accessToken);

        switch (mxRequestUri) {
            case (MX_USER_SEARCH_URI):
                String searchTerm = requestBody.getString("search_term");
                request.addQueryParam("search", searchTerm);
                break;
            case (MX_SINGLE_PID_URI):
                // TODO : write jolt specs for pid search
                JsonObject lookup = requestBody.getJsonObject("lookup");
                String value = lookup.getString("address");
                // for now we will force email on 3PID search
                request.addQueryParam("email", value);
                //artificially return a single response
                request.addQueryParam("max", "1");
                break;
            default:
                break;
        }

        request.method(HttpMethod.GET);

        return request;
    }

    private HttpRequest<Buffer> initRequest(String accessToken) {
        HttpRequest<Buffer> request = this.webClient.get(443, config.KEYCLOAK_HOST, keycloakUserSearchURI);
        request.headers().add("Authorization", "Bearer " + accessToken);
        return request;
    }
}
