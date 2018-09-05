package hoohoot.synapse.adapter.http.helpers;

import io.vertx.core.json.JsonObject;

public class HttpJsonErrors {
    private HttpJsonErrors() {

    }

    public static final JsonObject BADGATEWAY = new JsonObject().put("error", "502 bad gateway");

}
