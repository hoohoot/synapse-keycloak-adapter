![quality-gate](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=alert_status)
![code-smells](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=code_smells)
![bugs](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=bugs)
# Synapse Keycloak Adapter

A simple keycloak adapter for Matrix Synapse allowing to 
login into Matrix using Keycloak as an identity provider. 

## Disclaimer

We cannot guaranty this is 100% secure, we are still learning and doing this for fun.
Enjoy!  

## Prerequisites 

To use this adapter you need to have a running Keycloak Instance.

Go to the [Official Keycloak Documentation](https://www.keycloak.org/docs/latest/getting_started/index.html) to learn how to do that.

The easiest way to set up a Keycloak Instance is to use Docker Compose (More info [here](https://github.com/jboss-dockerfiles/keycloak)).

## Getting started

1. Edit ``docker-compose.yml`` and edit the variables following your needs

2. Start everything ``docker-compose up -d``

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
KEYCLOAK_CLIENT_URI=/auth/realms/${your-realm}/protocol/openid-connect/token```
KEYCLOAK_CLIENT_BASIC=${base64(client-id:client-secret)}
```

2. Package the application:
```
mvn clean install
```

3. Run the fat jar :
```
java -jar target/keycloak-synapse-adapter-${version}-SNAPSHOT-fat.jar -conf src/conf/config.json
```

(optional) You can also run the app using docker :

```css
docker-compose -f docker-compose-adapter-only.yml up
```

# Ressources

We use the following projects :
- [Matrix Synapse](https://github.com/matrix-org/synapse)
- [Mxisd](https://github.com/kamax-io/mxisd)
- [Keycloak](https://github.com/jboss-dockerfiles/keycloak)
- [Matrix Synapse Rest Auth](https://github.com/kamax-io/matrix-synapse-rest-auth) plugin