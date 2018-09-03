package hoohoot.synapse.adapter.http.server;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import hoohoot.synapse.adapter.http.clients.JsonHelper;
import hoohoot.synapse.adapter.http.clients.MxisdHandler;
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

public class MainVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws ConfigurationException  {

        final String loginUri = "/_mxisd/backend/api/v1/auth/login";
        final String userSearchUri = "/_mxisd/backend/api/v1/directory/user/search";
        final String singlePIDQueryHandler = "/_mxisd/backend/api/v1/identity/single";
        final String bulkPIDQueryHandler = "/_mxisd/backend/api/v1/identity/bulk";

        MainConfiguration conf = new MainConfiguration();

        final JsonHelper helper = new JsonHelper(conf);

        WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                .setSsl(conf.SSL_ACTIVE)
                .setUserAgent(conf.USER_AGENT));

        MxisdHandler mxisdHandler = new MxisdHandler(webClient, conf, helper);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route("/*").handler(BodyHandler.create());
        router.post(loginUri).handler(mxisdHandler::loginHandler);
        router.post(userSearchUri).handler(mxisdHandler::getSearchAccessToken);
        router.post(userSearchUri).handler(mxisdHandler::searchHandler);
        router.post(singlePIDQueryHandler).handler(mxisdHandler::singlePIDQueryHandler);
        router.post(bulkPIDQueryHandler).handler(mxisdHandler::bulkPIDQueryHandler);

        router.get("/ping").handler(res -> res.response().end(new JsonObject()
                .put("ping", "pong")
                .encodePrettily()));

        router.get("/health_check").handler(MxisdHandler::healthCheckHandler);


        server.requestHandler(router::accept)
                .listen(conf.SERVER_PORT, http -> {
                    if (http.succeeded()) {
                        startFuture.complete();
                        logger.info("HTTP server started on http://localhost:{}", conf.SERVER_PORT);
                    } else {
                        logger.info("HTTP server failed to start on http://localhost:{}", conf.SERVER_PORT);
                        startFuture.fail(http.cause());
                    }
                });
    }

}
