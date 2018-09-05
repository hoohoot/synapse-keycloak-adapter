package hoohoot.synapse.adapter.http.helpers;

import io.vertx.core.json.JsonObject;

public class HttpJsonErrors {
    private HttpJsonErrors() {

    }

    // TODO : set error code according to synapse endpoint if possible
    public static final JsonObject BADGATEWAY = new JsonObject().put("error", "502 bad gateway");
    public static final JsonObject UNAUTHORIZED = new JsonObject().put("error", "401 Unauthorized");
    public static final JsonObject FORBIDDEN = new JsonObject().put("error", "403 Forbidden");
    public static final JsonObject NOT_FOUND = new JsonObject().put("error", "404 Not found");

}
