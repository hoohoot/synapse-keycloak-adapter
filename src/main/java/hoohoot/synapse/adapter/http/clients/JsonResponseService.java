package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.http.helpers.HttpJsonErrors;
import hoohoot.synapse.adapter.http.helpers.JoltMapper;
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

class JsonResponseService {

    private static final Logger logger = LoggerFactory.getLogger(JsonResponseService.class);

    static void respondWithStatusCode502(RoutingContext routingContext) {
        logger.warn("Couldn't get response from keycloak");
        routingContext.response().setStatusCode(502);
        routingContext.response().end(HttpJsonErrors.BADGATEWAY.encodePrettily());
    }

    private static void respondWithStatusCode401(RoutingContext routingContext) {
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
                UserInfoDigest userinfo = jsonHelper.extractTokentInfo(keycloakResponse
                        .getString("access_token"));
                logger.info("response : " + jsonHelper.buildSynapseLoginJsonBody(userinfo)
                        .encodePrettily());
                routingContext.response().setStatusCode(200);
                routingContext.response().end(
                        jsonHelper.buildSynapseLoginJsonBody(userinfo)
                                .encodePrettily());
                break;

            case 401:
                UserInfoDigest userInfoDigest = new UserInfoDigest(
                        "", form.get("username"), false);
                logger.info("response : " + jsonHelper.buildSynapseLoginJsonBody(userInfoDigest)
                        .encodePrettily());
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
                                                String joltSpec,
                                                Future<JsonObject> future) {
        ar.result();
        switch (ar.result().statusCode()) {
            case 200:
                JsonObject transformedResponse = JoltMapper.transform(ar.result().bodyAsJsonArray(), joltSpec);
                routingContext.response().setChunked(true);
                routingContext.response().headers().add("content-type", "application/json");
                routingContext.response().setStatusCode(200);
                routingContext.response().write(transformedResponse.encodePrettily());
                future.complete(transformedResponse);
                break;
            case 401:
                future.fail("Unauthorized");
                break;
            case 403:
                future.fail("Forbidden");
                break;
            default:
                future.fail("Unable to contact keycloak");
                break;
        }
    }

}
