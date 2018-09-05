package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.http.helpers.HttpJsonErrors;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

class JsonResponseService {

    private static final Logger logger = LoggerFactory.getLogger(JsonResponseService.class);

    static void respondWithStatusCode502(RoutingContext routingContext) {
        logger.warn("Couldn't get response from keycloak");
        routingContext.response().setStatusCode(502);
        routingContext.response().end(HttpJsonErrors.BADGATEWAY.encodePrettily());
    }
    static void respondWithStatusCode401(RoutingContext routingContext) {
        logger.warn("Couldn't get response from keycloak");
        routingContext.response().setStatusCode(401);
        routingContext.response().end(HttpJsonErrors.BADGATEWAY.encodePrettily());
    }
    static void respondWithStatusCode403(RoutingContext routingContext) {
        logger.warn("Couldn't get response from keycloak");
        routingContext.response().setStatusCode(403);
        routingContext.response().end(HttpJsonErrors.BADGATEWAY.encodePrettily());
    }



}
