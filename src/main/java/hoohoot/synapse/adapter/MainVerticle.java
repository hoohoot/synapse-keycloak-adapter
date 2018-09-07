package hoohoot.synapse.adapter;

import hoohoot.synapse.adapter.conf.Routes;
import hoohoot.synapse.adapter.conf.ServerConfig;
import hoohoot.synapse.adapter.handlers.OAuthHandler;
import hoohoot.synapse.adapter.helpers.JsonHelper;
import hoohoot.synapse.adapter.handlers.MxisdHandler;
import hoohoot.synapse.adapter.exceptions.ConfigurationException;
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


public class MainVerticle extends AbstractVerticle {
    private Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    private OAuthHandler oAuthHandler;
    private MxisdHandler mxisdHandler;
    private ServerConfig config;

    @Override
    public void start(Future<Void> startFuture) throws ConfigurationException {
        initialize();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route("/*").handler(BodyHandler.create());
        router.post("/_mxisd/*").handler(oAuthHandler::getSearchAccessToken);

        router.post(Routes.MXISD_LOGIN).handler(mxisdHandler::loginHandler);

        router.post(Routes.MXISD_USER_SEARCH).handler( routingContext ->
                mxisdHandler.searchHandler(routingContext,
                "search-spec.json"));

        router.post(Routes.MXISD_SINGLEPID_SEARCH).handler(routingContext ->
                mxisdHandler.searchHandler(routingContext,
                "pid-search-spec.json"));

        router.post(Routes.MXISD_BULKPID_SEARCH).handler(mxisdHandler::bulkSearchHandler);

        router.get("/ping").handler(res -> res.response().end(new JsonObject()
                .put("ping", "pong")
                .encodePrettily()));

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

    private void initialize() throws ConfigurationException {
        config = new ServerConfig();
        JsonHelper jsonHelper = new JsonHelper();
        WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                .setSsl(config.getSslActive())
                .setUserAgent(config.getUserAgent()));
        oAuthHandler = new OAuthHandler(jsonHelper, config, webClient);
        mxisdHandler = new MxisdHandler(webClient, config, jsonHelper, oAuthHandler);
    }
}
