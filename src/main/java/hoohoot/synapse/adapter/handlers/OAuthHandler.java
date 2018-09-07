package hoohoot.synapse.adapter.handlers;

import hoohoot.synapse.adapter.conf.ServerConfig;
import hoohoot.synapse.adapter.helpers.JsonHelper;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import static hoohoot.synapse.adapter.helpers.ResponseHelper.respondWithStatusCode401;
import static hoohoot.synapse.adapter.helpers.ResponseHelper.respondWithStatusCode502;

public class OAuthHandler {
    private final Logger logger = LoggerFactory.getLogger(OAuthHandler.class);

    private final ServerConfig config;
    private final JsonHelper jsonHelper;
    private final WebClient webClient;
    private final String loginUri;

    public OAuthHandler(JsonHelper jsonHelper, ServerConfig config, WebClient webClient) {
        this.config = config;
        this.jsonHelper = jsonHelper;
        this.webClient = webClient;
        this.loginUri = "/auth/realms/" + config.getRealm() + "/protocol/openid-connect/token";
    }

    public void getSearchAccessToken(RoutingContext routingContext) {
        logger.debug("Trying to retrieve search access token");
        HttpRequest<Buffer> request = generateAccessTokenRequest();
        MultiMap form = jsonHelper.buildUserForm(config.getKeycloakSearchPassword(), config.getKeycloakSearchUsername());

        request.sendForm(form, ar -> {
            if (ar.succeeded()) {
                if (ar.result().statusCode() == 200) {
                    final String access_token = "access_token";
                    routingContext.put(access_token, ar.result().bodyAsJsonObject()
                            .getString(access_token));
                    routingContext.next();
                } else {
                    respondWithStatusCode401(routingContext);
                }
            } else {
                respondWithStatusCode502(routingContext);
            }
        });
    }

    protected HttpRequest<Buffer> generateAccessTokenRequest() {
        HttpRequest<Buffer> request = this.webClient.post(443, config.getKeycloakHost(), loginUri);
        request.headers().add("Authorization", config.getKeycloakClientBasicAuth());
        request.headers().add("content-type", "application/x-www-form-urlencoded");
        request.ssl(true);
        request.method(HttpMethod.POST);
        return request;
    }
}
