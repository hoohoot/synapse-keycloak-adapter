package hoohoot.synapse.adapter.helpers;

import hoohoot.synapse.adapter.models.UserInfoDigest;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Base64;
import java.util.List;

public class JsonHelper {

    public MultiMap buildUserForm(String keycloakPassword, String keycloakUsername) {
        MultiMap form = MultiMap.caseInsensitiveMultiMap();

        form.add("username", keycloakUsername);
        form.add("password", keycloakPassword);
        form.add("grant_type", "password");

        return form;
    }

    public JsonObject buildBulkSearchResponse(List<Future> pidFutures) {
        JsonArray lookups = new JsonArray();
        pidFutures.stream().map(Future::result)
                .forEach(lookups::add);
        return new JsonObject()
                .put("lookup", new JsonArray()
                        .addAll(lookups));
    }

    UserInfoDigest extractUserInfoFromToken(String bearer) {
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

    JsonObject buildSynapseLoginJsonBody(UserInfoDigest userInfoDigest) {

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
