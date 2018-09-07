package hoohoot.synapse.adapter.handlers;

import hoohoot.synapse.adapter.conf.ServerConfig;
import hoohoot.synapse.adapter.helpers.JsonHelper;
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

import static hoohoot.synapse.adapter.conf.Routes.MXISD_SINGLEPID_SEARCH;
import static hoohoot.synapse.adapter.conf.Routes.MXISD_USER_SEARCH;
import static hoohoot.synapse.adapter.helpers.ResponseHelper.*;

public class MxisdHandler extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(MxisdHandler.class);

    private static final String ACCESS_TOKEN = "access_token";

    private final JsonHelper jsonHelper;
    private final String keycloakUserSearchURI;
    private final OAuthHandler oauthHandler;
    private final WebClient webClient;
    private final ServerConfig config;

    public MxisdHandler(WebClient webClient, ServerConfig config, JsonHelper jsonHelper, OAuthHandler oauthHandler) {
        this.oauthHandler = oauthHandler;
        this.jsonHelper = jsonHelper;
        this.webClient = webClient;
        this.config = config;
        this.keycloakUserSearchURI = new StringBuffer("/auth/admin/realms/")
                .append(config.getRealm())
                .append("/users")
                .toString();
    }

    public void loginHandler(RoutingContext routingContext) {
        logger.info("Authentication process started");
        JsonObject authRequestBody = routingContext.getBodyAsJson().getJsonObject("auth");

        final String keycloakPassword = authRequestBody.getString("password");
        final String username = authRequestBody.getString("localpart");

        MultiMap form = jsonHelper.buildUserForm(keycloakPassword, username);

        HttpRequest<Buffer> request = oauthHandler.generateAccessTokenRequest();

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
        logger.info("Starting a directory search");
        JsonObject requestBody = routingContext.getBodyAsJson();
        String accessToken = routingContext.get(ACCESS_TOKEN);
        String mxRequestUri = routingContext.request().uri();

        HttpRequest<Buffer> request = buildSearchRequest(mxRequestUri, requestBody, accessToken);

        request.send(ar -> checkStatusCodeAndRespond(ar, routingContext, joltSpec));
    }

    public void bulkSearchHandler(RoutingContext routingContext) {
        logger.info("Starting a Bulk Search");
        String accessToken = routingContext.get(ACCESS_TOKEN);

        JsonArray bulkPID = routingContext.getBodyAsJson().getJsonArray("lookup");
        ArrayList<String> searchStrings = getSearchStringsFromBulkPids(bulkPID);
        List<Future> pidFutures = buildBulkSearchFutures(searchStrings, routingContext, accessToken);

        CompositeFuture.join(pidFutures).setHandler(ar -> {
            if (ar.succeeded()) {
                logger.debug("Bulk Search succeeded");
                JsonObject bulkResult = jsonHelper.buildBulkSearchResponse(pidFutures);
                routingContext.response().end(bulkResult.encodePrettily());
            } else {
                logger.warn("Bulk request failed");
                respondWithStatusCode502(routingContext);
            }
        });
    }

    private List<Future> buildBulkSearchFutures(ArrayList<String> searchStrings, RoutingContext routingContext,
                                                String accessToken) {
        return searchStrings.stream().map(email -> {

            Future<JsonObject> requestFuture = Future.future();
            HttpRequest<Buffer> request = initializeRequestWithAuthorization(accessToken);

            //forcing email on 3PID bulk search for now
            request.addQueryParam("email", email);

            request.send(ar -> {
                if (ar.succeeded()) {
                    checkFutureStatusCodeAndRespond(ar,
                            routingContext,
                            requestFuture,
                            config.getSynapseHost()
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

    private HttpRequest<Buffer> buildSearchRequest(String mxRequestUri, JsonObject requestBody, String accessToken) {
        HttpRequest<Buffer> request = initializeRequestWithAuthorization(accessToken);

        switch (mxRequestUri) {
            case (MXISD_USER_SEARCH):
                String searchTerm = requestBody.getString("search_term");
                request.addQueryParam("search", searchTerm);
                break;
            case (MXISD_SINGLEPID_SEARCH):
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

    private HttpRequest<Buffer> initializeRequestWithAuthorization(String accessToken) {
        HttpRequest<Buffer> request = this.webClient.get(443, config.getKeycloakHost(), keycloakUserSearchURI);
        request.headers().add("Authorization", "Bearer " + accessToken);
        return request;
    }
}
