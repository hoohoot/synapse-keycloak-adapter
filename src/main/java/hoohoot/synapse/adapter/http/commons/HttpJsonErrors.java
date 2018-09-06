package hoohoot.synapse.adapter.http.commons;

import io.vertx.core.json.JsonObject;

public class HttpJsonErrors {
    private HttpJsonErrors() {

    }

    private static final String ERROR_KEY = "error";

    // TODO : set error code according to synapse endpoint if possible
    public static final JsonObject BADGATEWAY = new JsonObject().put(ERROR_KEY, "502 bad gateway");
    public static final JsonObject UNAUTHORIZED = new JsonObject().put(ERROR_KEY, "401 Unauthorized");
    public static final JsonObject FORBIDDEN = new JsonObject().put(ERROR_KEY, "403 Forbidden");

}
