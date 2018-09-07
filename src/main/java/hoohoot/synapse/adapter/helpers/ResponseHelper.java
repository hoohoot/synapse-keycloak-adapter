package hoohoot.synapse.adapter.helpers;

import hoohoot.synapse.adapter.common.HttpJsonErrors;
import hoohoot.synapse.adapter.common.JoltMapper;
import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;

public class ResponseHelper {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHelper.class);

    private ResponseHelper() { }

    public static void respondWithStatusCode502(RoutingContext routingContext) {
        logger.warn("Couldn't get response from keycloak");
        routingContext.response().setStatusCode(502);
        routingContext.response().end(HttpJsonErrors.BADGATEWAY.encodePrettily());
    }

    public static void respondWithStatusCode401(RoutingContext routingContext) {
        logger.warn("Keycloak responded with Status code 401");
        routingContext.response().setStatusCode(401);
        routingContext.response().end(HttpJsonErrors.UNAUTHORIZED.encodePrettily());
    }

    public static void checkUserAndRequestSynapseLogin(AsyncResult<HttpResponse<Buffer>> ar,
                                                       RoutingContext routingContext,
                                                       MultiMap form) {
        JsonHelper jsonHelper = new JsonHelper();

        ar.result();
        switch (ar.result().statusCode()) {
            case 200:
                JsonObject keycloakResponse = ar.result().bodyAsJsonObject();
                UserInfoDigest userinfo = jsonHelper
                        .extractUserInfoFromToken(keycloakResponse
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

    public static void checkStatusCodeAndRespond(AsyncResult<HttpResponse<Buffer>> ar,
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

    public static void checkFutureStatusCodeAndRespond(AsyncResult<HttpResponse<Buffer>> ar,
                                                       RoutingContext routingContext,
                                                       Future<JsonObject> future,
                                                       String matrixDomain) {
        ar.result();
        Integer responseStatusCode = ar.result().statusCode();

        if (responseStatusCode.equals(HttpResponseStatus.OK.code())) {
            JsonObject transformedResponse = JoltMapper.transform(
                    ar.result().bodyAsJsonArray(), "bulk-search-spec.json", matrixDomain);

            routingContext.response().setChunked(true);
            routingContext.response().headers().add("content-type", "application/json");

            future.complete(transformedResponse);
        } else if (responseStatusCode.equals(HttpResponseStatus.METHOD_NOT_ALLOWED.code())){
            logger.warn("A request in the bulk failed with status : " + HttpResponseStatus.METHOD_NOT_ALLOWED.reasonPhrase());
            future.fail(HttpResponseStatus.METHOD_NOT_ALLOWED.reasonPhrase());
        } else if (responseStatusCode.equals(HttpResponseStatus.UNAUTHORIZED.code())) {
            logger.warn("A request in the bulk failed with status : 403 Forbidden" + HttpResponseStatus.FORBIDDEN.reasonPhrase());
            future.fail(HttpResponseStatus.FORBIDDEN.reasonPhrase());
        } else {
            logger.warn("A request in the bulk did not reach keycloak");
            future.fail("Unable to contact Keycloak");
        }

    }

    private static void respondWithStatusCode403(RoutingContext routingContext) {
        logger.warn("Keycloak responded with Status code 403");
        routingContext.response().setStatusCode(403);
        routingContext.response().end(HttpJsonErrors.FORBIDDEN.encodePrettily());
    }
}
