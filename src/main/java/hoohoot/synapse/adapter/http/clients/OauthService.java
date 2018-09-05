package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import static hoohoot.synapse.adapter.http.clients.JsonResponseService.respondWithStatusCode502;

public class OauthService {

    private final Logger logger = LoggerFactory.getLogger(OauthService.class);
    private final MainConfiguration config;
    private final JsonHelper jsonHelper;
    private final WebClient webClient;
    private final String loginUri;

    public OauthService(JsonHelper jsonHelper, MainConfiguration config, WebClient webClient) {
        this.config = config;
        this.jsonHelper = jsonHelper;
        this.webClient = webClient;
        this.loginUri = "/auth/realms/" + config.REALM + "/protocol/openid-connect/token";
    }

    public void getSearchAccessToken(RoutingContext routingContext) {
        HttpRequest<Buffer> request = generateAccessTokenRequest();
        MultiMap form = jsonHelper.getUserForm(config.KEYCLOAK_SEARCH_PASSWORD, config.KEYCLOAK_SEARCH_USERNAME);
        logger.info(form);

        request.sendForm(form, ar -> {
            if (ar.succeeded()) {
                logger.info(ar.result().statusCode());
                if (ar.result().statusCode() == 200) {
                    routingContext.put("access_token", ar.result().bodyAsJsonObject()
                            .getString("access_token"));
                    routingContext.next();
                } else {
                    logger.error("Couldn't get access token for search user");
                    logger.info(routingContext.response().getStatusCode());
                    routingContext.response().end("placeholder");
                }
            } else {
                respondWithStatusCode502(routingContext);
            }
        });
    }

    public HttpRequest<Buffer> generateAccessTokenRequest() {
        HttpRequest<Buffer> request = this.webClient.post(443, config.KEYCLOAK_HOST, loginUri);
        request.headers().add("Authorization", config.KEYCLOAK_CLIENT_BASIC);
        request.headers().add("content-type", "application/x-www-form-urlencoded");
        request.ssl(true);
        request.method(HttpMethod.POST);
        return request;
    }
}
