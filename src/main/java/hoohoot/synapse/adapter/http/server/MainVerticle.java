package hoohoot.synapse.adapter.http.server;

import hoohoot.synapse.adapter.conf.ServerConfig;
import hoohoot.synapse.adapter.http.clients.JsonHelper;
import hoohoot.synapse.adapter.http.clients.MxisdHandler;
import hoohoot.synapse.adapter.http.clients.OauthService;
import hoohoot.synapse.adapter.http.exceptions.ConfigurationException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;

import static hoohoot.synapse.adapter.http.commons.Routes.*;

public class MainVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws ConfigurationException  {

        ServerConfig config = new ServerConfig();

        final JsonHelper jsonHelper = new JsonHelper();

        WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                .setSsl(config.getSslActive())
                .setUserAgent(config.getUserAgent()));

        OauthService oauthService = new OauthService(jsonHelper, config, webClient);
        MxisdHandler mxisdHandler = new MxisdHandler(webClient, config, jsonHelper, oauthService);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route("/*").handler(BodyHandler.create());
        router.post("/_mxisd/*").handler(oauthService::getSearchAccessToken);

        router.post(MX_LOGIN_URI).handler(mxisdHandler::loginHandler);

        router.post(MX_USER_SEARCH_URI).handler( routingContext ->
                mxisdHandler.searchHandler(routingContext,
                "search-spec.json"));

        router.post(MX_SINGLE_PID_URI).handler(routingContext ->
                mxisdHandler.searchHandler(routingContext,
                "pid-search-spec.json"));

        router.post(MX_BULK_PID_URI).handler(mxisdHandler::bulkSearchHandler);

        router.get("/ping").handler(res -> res.response().end(new JsonObject()
                .put("ping", "pong")
                .encodePrettily()));

        router.get("/health_check").handler(MxisdHandler::healthCheckHandler);

        server.requestHandler(router::accept)
                .listen(config.getServerPort(), http -> {
                    if (http.succeeded()) {
                        startFuture.complete();
                        logger.info("Synapse Keycloak Adapter started @ http://localhost:" + config.getServerPort());
                    } else {
                        logger.info("Synapse Keycloak Adapter failed to start @ http://localhost:" + config.getServerPort());
                        startFuture.fail(http.cause());
                    }
                });
    }

}
