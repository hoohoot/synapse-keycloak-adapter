package hoohoot.synapse.adapter.conf;

import hoohoot.synapse.adapter.http.exceptions.ConfigurationException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Optional;

public class MainConfiguration {

    private Logger logger = LoggerFactory.getLogger(MainConfiguration.class);

    public final String SYNAPSE_HOST;

    public final String KEYCLOAK_HOST;
    public final String REALM;
    public final String KEYCLOAK_CLIENT_BASIC;
    private final String KEYCLOAK_SEARCH_USERNAME;
    private final String KEYCLOAK_SEARCH_PASSWORD;

    public final String USER_AGENT = "synapse-adapter";
    public final Boolean SSL_ACTIVE = true;
    public final Integer SERVER_PORT = 8080;

    public MainConfiguration() throws ConfigurationException {
        logger.info("=== SETTING UP CONFIGURATION VARIABLES ===");

        SYNAPSE_HOST = getEnvironmentVariable("SYNAPSE_HOST");
        logger.info("SYNAPSE HOST: " + SYNAPSE_HOST);

        KEYCLOAK_HOST = getEnvironmentVariable("KEYCLOAK_HOST");
        logger.info("KEYCLOAK HOST: " + KEYCLOAK_HOST);

        REALM = getEnvironmentVariable("REALM");
        logger.info("KEYCLOAK CLIENT URI: " + REALM);

        KEYCLOAK_SEARCH_USERNAME = getEnvironmentVariable("KEYCLOAK_SEARCH_USERNAME");
        logger.info("KEYCLOAK SEARCH_USER: " + KEYCLOAK_SEARCH_USERNAME);

        KEYCLOAK_SEARCH_PASSWORD = getEnvironmentVariable("KEYCLOAK_SEARCH_PASSWORD");
        logger.info("KEYCLOAK SEARCH PASSWORD" + KEYCLOAK_SEARCH_PASSWORD);

        KEYCLOAK_CLIENT_BASIC = getEnvironmentVariable("KEYCLOAK_CLIENT_BASIC");
        logger.info("KEYCLOAK_CLIENT_BASIC: " + KEYCLOAK_CLIENT_BASIC);
    }

    private String getEnvironmentVariable(String key) throws ConfigurationException {
        Optional<String> environmentVariable = Optional.ofNullable(System.getenv(key));
        return environmentVariable.orElseThrow(ConfigurationException::new).replace("\"", "");
    }
}
