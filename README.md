![quality-gate](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=alert_status)
![code-smells](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=code_smells)
![bugs](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=bugs)

[![pipeline status](https://framagit.org/hoohoot/synapse-keycloak-adapter/badges/master/pipeline.svg)](https://framagit.org/hoohoot/synapse-keycloak-adapter/commits/master)

# Synapse Keycloak Adapter

A simple keycloak adapter for Matrix Synapse allowing to 
login into Matrix using Keycloak as an identity provider. 

## Disclaimer

WORK IN PROGRESS : THIS IS STILL NOT COMPLETELY FUNCTIONAL

We cannot guaranty this is 100% secure, we are still learning and doing this for fun.
Enjoy!  

## Prerequisites 

To use this adapter you need to have a running Keycloak Instance.

Go to the [Official Keycloak Documentation](https://www.keycloak.org/docs/latest/getting_started/index.html) to learn how to do that.

The easiest way to set up a Keycloak Instance is to use Docker Compose (More info [here](https://github.com/jboss-dockerfiles/keycloak)).

## Getting started

### Adapter only

If you only need the adapter you can run only this part :

_With docker_ :

Run the following command after having replaced the environment variables with your own values :

```
docker run -e \

hoohoot/synapse-keycloak-adaper
```

_With docker compose_ :

1. Edit ``docker-compose-adapter-only.yml`` and edit the variables following your needs

2. Start the adapter : 
```docker-compose -f docker-compose-adapter-only.yml up -d```

### Complete setup

TODO

## Configuration


| Variable        | Status           | Description  |
| -------- |-------------| -----|
| SYNAPSE_HOST     | mandatory | hostname of your Synapse instance (eg. matrix.example.org)  |
| KEYCLOAK_CLIENT_ID | mandatory     |    The client ID of the client you've configured in Keycloak |
| KEYCLOAK_CLIENT_SECRET | mandatory     |    The client secret of the client you've configured in Keycloak |
| KEYCLOAK_SEARCH_USER | mandatory     | The username of the keycloak user dedicated to searching user. |
| KEYCLOAK_SEARCH_PASSWORD | mandatory     | The password of the keycloak user dedicated to searching users. |
| REALM     | optional (default value : master)      |   The name of your Keycloak Realm |
| SSL_ACTIVE | optional (default value: true)     | Activate or not HTTPS for requests  |
| SERVER_PORT | optional (default value: 8080)     |    The port the VertX server will listen on |
| USER_AGENT | optional (default value: synapse-adapter)     |  The user agent used for requests |

## Configuration


| Variable        | Status           | Description  |
| -------- |-------------| -----|
| SYNAPSE_HOST     | mandatory | hostname of your Synapse instance (eg. matrix.example.org)  |
| KEYCLOAK_CLIENT_ID | mandatory     |    The client ID of the client you've configured in Keycloak |
| KEYCLOAK_CLIENT_SECRET | mandatory     |    The client secret of the client you've configured in Keycloak |
| KEYCLOAK_SEARCH_USER | mandatory     | The username of the keycloak user dedicated to searching user. |
| KEYCLOAK_SEARCH_PASSWORD | mandatory     | The password of the keycloak user dedicated to searching users. |
| REALM     | optional (default value : master)      |   The name of your Keycloak Realm |
| SSL_ACTIVE | optional (default value: true)     | Activate or not HTTPS for requests  |
| SERVER_PORT | optional (default value: 8080)     |    The port the VertX server will listen on |
| USER_AGENT | optional (default value: synapse-adapter)     |  The user agent used for requests |

## Upgrade

In order to upgrade to the last version, just run the next two commands :

```css
docker-compose pull
docker-compose up -d
```

## Development

1. Set the following environment variables
```
SYNAPSE_HOST=matrix.example.org
KEYCLOAK_HOST=keycloak.example.org
KEYCLOAK_CLIENT_BASIC=${base64(client-id:client-secret)}
REALM=my-keycloak-realm;
KEYCLOAK_SEARCH_USERNAME=admin;
KEYCLOAK_SEARCH_PASSWORD=password
```

2. Package the application:
```
mvn clean install
```

3. Run the fat jar :
```
java -jar target/keycloak-synapse-adapter-${version}-SNAPSHOT-fat.jar -conf src/conf/config.json
```

# Ressources

We use the following projects :
- [Matrix Synapse](https://github.com/matrix-org/synapse)
- [Mxisd](https://github.com/kamax-io/mxisd)
- [Keycloak](https://github.com/jboss-dockerfiles/keycloak)
- [Matrix Synapse Rest Auth](https://github.com/kamax-io/matrix-synapse-rest-auth) plugin