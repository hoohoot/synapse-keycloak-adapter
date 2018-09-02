package hoohoot.synapse.adapter.conf;

import hoohoot.synapse.adapter.http.exceptions.ConfigurationException;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

public class MainConfiguration {

    public final String SYNAPSE_HOST;

    public final String KEYCLOAK_HOST;
    public final String KEYCLOAK_CLIENT_URI;
    public final String KEYCLOAK_CLIENT_BASIC;

    public final String USER_AGENT = "synapse-adapter";
    public final Boolean SSL_ACTIVE = true;
    public final Integer SERVER_PORT = 8080;

    public MainConfiguration() throws ConfigurationException {
            SYNAPSE_HOST = getEnvironmentVariable("SYNAPSE_HOST");

            KEYCLOAK_HOST = getEnvironmentVariable("KEYCLOAK_HOST");
            KEYCLOAK_CLIENT_URI = getEnvironmentVariable("KEYCLOAK_CLIENT_URI");
            KEYCLOAK_CLIENT_BASIC = getEnvironmentVariable("KEYCLOAK_CLIENT_BASIC");


    }

    private String getEnvironmentVariable(String key) throws ConfigurationException {
        Optional<String> environmentVariable = Optional.ofNullable(System.getenv(key));
        return environmentVariable.orElseThrow(ConfigurationException::new);
    }
}
