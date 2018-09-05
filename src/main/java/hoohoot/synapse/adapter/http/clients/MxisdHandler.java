package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static hoohoot.synapse.adapter.http.clients.ResponseHelper.*;
import static hoohoot.synapse.adapter.http.commons.Routes.MX_SINGLE_PID_URI;
import static hoohoot.synapse.adapter.http.commons.Routes.MX_USER_SEARCH_URI;

public class MxisdHandler extends AbstractVerticle {
    private final JsonHelper jsonHelper;
    private final String keycloakUserSearchURI;
    private final OauthService oauthService;
    private Logger logger = LoggerFactory.getLogger(MxisdHandler.class);

    private WebClient webClient;
    private MainConfiguration config;
    private  final String ACCESS_TOKEN = "access_token";


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

        HttpRequest<Buffer> request = oauthService.generateAccessTokenRequest();

        request
                .sendForm(form, ar -> {
                    if (ar.succeeded()) {
                        checkUserAndRequestSynapseLogin(ar, routingContext, form);
                    } else {
                        respondWithStatusCode502(routingContext);
                    }
                });
    }


    public void searchHandler(RoutingContext routingContext, String joltSpec) {

        JsonObject requestBody = routingContext.getBodyAsJson();
        String accessToken = routingContext.get(ACCESS_TOKEN);
        String mxRequestUri = routingContext.request().uri();
        logger.info(routingContext.request().uri());

        HttpRequest<Buffer> request = generateSearchRequest(
                mxRequestUri,
                requestBody,
                accessToken);

        request.send(ar -> checkStatusCodeAndRespond(ar, routingContext, joltSpec));
    }

    public void bulkSearchHandler(RoutingContext routingContext) {
        String accessToken = routingContext.get(ACCESS_TOKEN);
        logger.info(routingContext.request().uri());

        JsonArray bulkPID = routingContext.getBodyAsJson().getJsonArray("lookup");
        ArrayList<String> searchStrings = getSearchStringsFromBulkPids(bulkPID);
        List<Future> pidFutures = startRequestFuture(searchStrings, routingContext, accessToken);

        CompositeFuture.join(pidFutures).setHandler(ar -> {
            if (ar.succeeded()) {
                logger.info("Bulk request succeeded");
                JsonObject bulkResult = jsonHelper.buildBulkResponse(pidFutures);
                routingContext.response().end(bulkResult.encodePrettily());
            } else {
                logger.info("Bulk request failed");
                respondWithStatusCode502(routingContext);
            }
        });
    }

    private List<Future> startRequestFuture(ArrayList<String> searchStrings,
                                            RoutingContext routingContext,
                                            String accessToken) {
        return searchStrings.stream().map(email -> {

            Future<JsonObject> requestFuture = Future.future();
            HttpRequest<Buffer> request = initRequest(accessToken);

            //forcing email on 3PID bulk search for now
            request.addQueryParam("email", email);

            request.send(ar -> {
                if (ar.succeeded()) {
                    checkFutureStatusCodeAndRespond(ar,
                            routingContext,
                            requestFuture,
                            config.SYNAPSE_HOST
                    );
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

    // TODO : implement this
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
