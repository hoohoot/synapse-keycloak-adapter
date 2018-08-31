![quelity-gate](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=alert_status)
![code-smells](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=code_smells)
![bugs](https://sonar.hoohoot.org/api/project_badges/measure?project=io.vertx%3Akeycloak-synapse-adapter&metric=bugs)
# synapse-keycloak-adapter

A simple keycloak adapter for synapse-rest-auth allowing to 
login into matrix using keycloak as an identity provider. 

## Prerequisites 

To run this adapter you need to have a running instance of matrix/synapse installed
with the [rest-auth](https://github.com/kamax-io/matrix-synapse-rest-auth) plugin and a running keycloack server. 

## Running the adapter

Edit the `/src/config.example.json` according to your setup. 
Rename it `config.json`

To launch your tests:
```
./mvnw clean test
```

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
