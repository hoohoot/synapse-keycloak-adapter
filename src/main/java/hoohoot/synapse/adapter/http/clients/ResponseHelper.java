package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.http.commons.HttpJsonErrors;
import hoohoot.synapse.adapter.http.commons.JoltMapper;
import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;

class ResponseHelper {

    private static final Logger logger = LoggerFactory.getLogger(ResponseHelper.class);

    private ResponseHelper() {

    }

    static void respondWithStatusCode502(RoutingContext routingContext) {
        logger.warn("Couldn't get response from keycloak");
        routingContext.response().setStatusCode(502);
        routingContext.response().end(HttpJsonErrors.BADGATEWAY.encodePrettily());
    }

    public static void respondWithStatusCode401(RoutingContext routingContext) {
        logger.warn("Keycloak responded with Status code 401");
        routingContext.response().setStatusCode(401);
        routingContext.response().end(HttpJsonErrors.UNAUTHORIZED.encodePrettily());
    }

    private static void respondWithStatusCode403(RoutingContext routingContext) {
        logger.warn("Keycloak responded with Status code 403");
        routingContext.response().setStatusCode(403);
        routingContext.response().end(HttpJsonErrors.FORBIDDEN.encodePrettily());
    }

    static void checkUserAndRequestSynapseLogin(AsyncResult<HttpResponse<Buffer>> ar,
                                                RoutingContext routingContext,
                                                MultiMap form) {
        JsonHelper jsonHelper = new JsonHelper();

        ar.result();
        switch (ar.result().statusCode()) {
            case 200:
                JsonObject keycloakResponse = ar.result().bodyAsJsonObject();
                UserInfoDigest userinfo = jsonHelper
                        .extractTokentInfo(keycloakResponse
                        .getString("access_token"));

                routingContext.response().setStatusCode(200);
                routingContext.response().end(
                        jsonHelper
                                .buildSynapseLoginJsonBody(userinfo)
                                .encodePrettily());
                break;
            case 401:
                UserInfoDigest userInfoDigest = new UserInfoDigest(
                        "", form.get("username"), false);

                routingContext.response().setStatusCode(401);
                routingContext.response().end(
                        jsonHelper.buildSynapseLoginJsonBody(userInfoDigest)
                                .encodePrettily());
                break;
            case 403:
                respondWithStatusCode403(routingContext);
                break;
            default:
                respondWithStatusCode502(routingContext);
                break;
        }
        routingContext.response().end();
    }

    static void checkStatusCodeAndRespond(AsyncResult<HttpResponse<Buffer>> ar,
                                          RoutingContext routingContext,
                                          String joltSpec) {
        ar.result();
        switch (ar.result().statusCode()) {
            case 200:
                routingContext.response().setChunked(true);
                JsonObject transformedResponse = JoltMapper.transform(ar.result().bodyAsJsonArray(), joltSpec);
                routingContext.response().headers().add("content-type", "application/json");
                routingContext.response().setStatusCode(200);
                routingContext.response().write(transformedResponse.encodePrettily());
                break;
            case 401:
                respondWithStatusCode401(routingContext);
                break;
            case 403:
                respondWithStatusCode403(routingContext);
                break;
            default:
                respondWithStatusCode502(routingContext);
                break;
        }
        routingContext.response().end();
    }

    static void checkFutureStatusCodeAndRespond(AsyncResult<HttpResponse<Buffer>> ar,
                                                RoutingContext routingContext,
                                                Future<JsonObject> future,
                                                String matrixDomain) {
        ar.result();
        switch (ar.result().statusCode()) {
            case 200:
                JsonObject transformedResponse = JoltMapper.transform(
                        ar.result().bodyAsJsonArray(), "bulk-search-spec.json", matrixDomain);
                routingContext.response().setChunked(true);
                routingContext.response().headers().add("content-type", "application/json");
                future.complete(transformedResponse);
                break;
            case 400:
                logger.warn("A request in the bulk failed : 400 Method not allowed");
                future.fail("Method not allowed");
                break;
            case 401:
                logger.warn("A request in the bulk failed : 401 Unothorized");
                future.fail("Unauthorized");
                break;
            case 403:
                logger.warn("A request in the bulk failed : 403 Forbidden");
                future.fail("Forbidden");
                break;
            default:
                logger.warn("A request in the bulk did not reach keycloak");
                future.fail("Unable to contact keycloak");
                break;
        }
    }

}
