package hoohoot.synapse.adapter.conf;

import hoohoot.synapse.adapter.http.exceptions.ConfigurationException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ServerConfig {
    private final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

    private static final String USER_AGENT_DEFAULT = "synapse-adapter";
    private static final Boolean SSL_ACTIVE_DEFAULT = true;
    private static final String SERVER_PORT_DEFAULT = "8080";
    private static final String KEYCLOAK_REALM_DEFAULT = "master";

    private String synapseHost;
    private String realm;
    private String keycloakHost;
    private String keycloakClientId;
    private String keycloakClientSecret;
    private String keycloakSearchUsername;
    private String keycloakSearchPassword;
    private String userAgent;
    private Boolean sslActive;
    private String serverPort;

    public ServerConfig() throws ConfigurationException {
        synapseHost = System.getenv("SYNAPSE_HOST");
        keycloakHost = System.getenv("KEYCLOAK_HOST");
        realm = System.getenv("REALM");
        keycloakSearchUsername = System.getenv("KEYCLOAK_SEARCH_USERNAME");
        keycloakSearchPassword = System.getenv("KEYCLOAK_SEARCH_PASSWORD");
        keycloakClientId = System.getenv("KEYCLOAK_CLIENT_ID");
        keycloakClientSecret = System.getenv("KEYCLOAK_CLIENT_SECRET");
        userAgent = System.getenv("USER_AGENT");
        sslActive = Boolean.parseBoolean(System.getenv("SSL_ACTIVE"));
        serverPort = System.getenv("SERVER_PORT");

        postConstruct();
    }

    public String getKeycloakClientBasicAuth() {
        byte[] bytesToEncode = new StringBuffer(keycloakClientId)
                .append(":")
                .append(keycloakClientSecret)
                .toString()
                .getBytes(StandardCharsets.UTF_8);

        return new StringBuffer("Basic ")
                .append(Base64.getEncoder().encodeToString(bytesToEncode))
                .toString();
    }

    private void postConstruct() throws ConfigurationException {
        logger.info("====== Server Config ======");

        if (StringUtils.isBlank(synapseHost)) {
            logger.error("SYNAPSE_HOST is empty !");
            throw new ConfigurationException();
        }
        logger.info("SYNAPSE HOST: " + synapseHost);

        if (StringUtils.isBlank(keycloakHost)) {
            logger.error("KEYCLOAK_HOST is empty !");
            throw new ConfigurationException();
        }

        if (StringUtils.isBlank(realm)) {
            logger.debug("REALM is empty, using '{}' by default", KEYCLOAK_REALM_DEFAULT);
            realm = KEYCLOAK_REALM_DEFAULT;
        }

        if (StringUtils.isBlank(keycloakSearchUsername)) {
            logger.error("KEYCLOAK_SEARCH_USERNAME is empty !");
            throw new ConfigurationException();
        }

        if (StringUtils.isBlank(keycloakSearchPassword)) {
            logger.error("KEYCLOAK_SEARCH_USERNAME is empty !");
            throw new ConfigurationException();
        }

        if (StringUtils.isBlank(keycloakClientId)) {
            logger.error("KEYCLOAK_CLIENT_ID is empty !");
            throw new ConfigurationException();
        }

        if (StringUtils.isBlank(keycloakClientSecret)) {
            logger.error("KEYCLOAK_CLIENT_SECRET is empty !");
            throw new ConfigurationException();
        }

        if (StringUtils.isBlank(userAgent)) {
            logger.debug("USER_AGENT is empty, using default value : '{}'", SERVER_PORT_DEFAULT);
            userAgent = USER_AGENT_DEFAULT;
        }

        if (serverPort == null) {
            logger.debug("SERVER_PORT is empty, using default value : '{}'", SERVER_PORT_DEFAULT);
            serverPort = SERVER_PORT_DEFAULT;
        }

        if (sslActive == null) {
            logger.debug("SSL_ACTIVE not set, using default value : {}", SSL_ACTIVE_DEFAULT);
            sslActive = SSL_ACTIVE_DEFAULT;
        }

        logger.info("KEYCLOAK CLIENT URI: " + realm);
        logger.info("KEYCLOAK HOST: " + keycloakHost);
        logger.info("KEYCLOAK SEARCH_USER: " + keycloakSearchUsername);
        logger.debug("KEYCLOAK SEARCH PASSWORD" + keycloakSearchPassword);
        logger.info("USER_AGENT : " + userAgent);
        logger.info("SERVER_PORT : " + serverPort);
    }

    public String getSynapseHost() {
        return synapseHost;
    }

    public String getKeycloakHost() {
        return keycloakHost;
    }

    public String getRealm() {
        return realm;
    }

    public String getKeycloakSearchUsername() {
        return keycloakSearchUsername;
    }

    public String getKeycloakSearchPassword() {
        return keycloakSearchPassword;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Boolean getSslActive() {
        return sslActive;
    }

    public Integer getServerPort() {
        return Integer.parseInt(serverPort);
    }
}
