package hoohoot.synapse.adapter.http.helpers;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public interface JoltMapper {

    /**
     * Transform a json object according to the provided jolt specification.
     * @param responseBody
     * @param specPath
     * @return
     */
    public static JsonArray transform(JsonObject responseBody, String specPath){

        //Get Specification and load them into Chainr Object
        Object chainrSpecJSON = JsonUtils.jsonToObject(JoltMapper.class.getResourceAsStream("/jolt/" + specPath));
        Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

        // Transform the response into a pretty Json
        Object transformedResult = chainr.transform(JsonUtils.jsonToObject(responseBody.encode()));

        return new JsonArray(JsonUtils.toJsonString(transformedResult));
    }

}
