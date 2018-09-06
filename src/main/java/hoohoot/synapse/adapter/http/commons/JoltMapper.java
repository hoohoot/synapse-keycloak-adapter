package hoohoot.synapse.adapter.http.commons;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;


public interface JoltMapper {

    static JsonObject transform(JsonArray responseBody, String specPath) {

        //Get Specification and load them into Chainr Object
        Object chainrSpecJSON = JsonUtils.jsonToObject(JoltMapper.class.getResourceAsStream("/jolt/" + specPath));
        Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

        // Transform the response into a pretty Json
        Object transformedResult = chainr.transform(JsonUtils.jsonToObject(responseBody.encode()));

        return new JsonArray(JsonUtils.toJsonString(transformedResult)).getJsonObject(0);
    }

    static JsonObject transform(JsonArray responseBody, String specPath, String matrixDomain) {

        Object chainrSpecJSON = JsonUtils.jsonToObject(JoltMapper.class.getResourceAsStream("/jolt/" + specPath));
        Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

        Map<String, Object> context = new HashMap<>();
        context.put("MATRIX_DOMAIN", matrixDomain);
        Object transformedResult = chainr.transform(JsonUtils.jsonToObject(responseBody.encode()), context);

        return new JsonArray(JsonUtils.toJsonString(transformedResult)).getJsonObject(0);
    }

}
