package hoohoot.synapse.adapter.http.clients;

import hoohoot.synapse.adapter.conf.MainConfiguration;
import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Base64;

public class JsonHelper {
    private final MainConfiguration config;

    public JsonHelper(MainConfiguration config) {
        this.config = config;
    }

    public String desynapsifyUsername(String username) {
        username = username.replace(":" + config.SYNAPSE_HOST, "");
        username = username.substring(1);
        return username;
    }

    public UserInfoDigest extractTokentInfo(String bearer) {
        String[] splittedJWT = bearer.split("\\.");
        byte[] decodedBytes = Base64.getDecoder().decode(splittedJWT[1]);
        String decodedPayload = new String(decodedBytes);
        JsonObject payload = new JsonObject(decodedPayload);

        String preferedUsername = payload.getString("preferred_username");
        String email = payload.getString("email");

        if (payload.getJsonObject("realm_access")
                .getJsonArray("roles")
                .contains("matrix_user")) {
            return new UserInfoDigest(email, preferedUsername, true);
        } else {
            return new UserInfoDigest(email, preferedUsername, false);
        }
    }

    public MultiMap getUserForm(String keycloakPassword, String keycloakUsername) {
        MultiMap form = MultiMap.caseInsensitiveMultiMap();

        form.add("username", keycloakUsername);
        form.add("password", keycloakPassword);
        form.add("grant_type", "password");

        return form;
    }

    public JsonObject buildSynapseLoginJsonBody(UserInfoDigest userInfoDigest) {

        JsonObject auth = new JsonObject();

        auth.put("success", userInfoDigest.isMatrixUser());
        auth.put("id", new JsonObject()
                .put("value", userInfoDigest.getPreferedUserName())
                .put("type", "localpart")
        );

        JsonObject profile = new JsonObject();
        profile.put("display_name", userInfoDigest.getPreferedUserName());

        JsonArray treePids = new JsonArray();

        JsonObject mailPid = new JsonObject();
        mailPid.put("medium", "email");
        mailPid.put("address", userInfoDigest.getEmail());

        treePids.add(mailPid);

        profile.put("three_pids", treePids);
        auth.put("profile", profile);
        return new JsonObject().put("auth", auth);
    }

}
