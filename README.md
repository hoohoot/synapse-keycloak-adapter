# synapse-keycloak-adapter

A simple keycloak adapter for synapse-rest-auth allowing to 
login into matrix using keycloak as an identity provider. 

## Prerequisites 

To run this adapter you need to have a running instance of matrix/synapse installed
with the [rest-auth](https://github.com/kamax-io/matrix-synapse-rest-auth) plugin and a running keycloack server. 

## Running the adapter

To run the project you need the following enironement variable to be set: 
```
SYNAPSE_HOST=matrix.example.org
KEYCLOAK_HOST=keycloak.example.org
KEYCLOAK_CLIENT_URI=/auth/realms/${your-realm}/protocol/openid-connect/token```
KEYCLOAK_CLIENT_BASIC=${base64(client-id:client-secret)}
```

To launch your tests:
```
./mvnw clean test
```
Note that you will need to set your testing environement variable in the pom.xml properties.

To package your application:
```
./mvnw clean package
```

To run the fat-jar: 
```
java -jar target/keycloak-synapse-adapter-${version}-SNAPSHOT-fat.jar -conf src/conf/config.json
```

## Dislaimer

Do not use this in production! 
I cannot guaranty this is 100% secure, i am still learning and doing this for fun.
Enjoy!  
