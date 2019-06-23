# Authentication Service
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=PauloFer1_pf-auth-server&metric=alert_status)](https://sonarcloud.io/dashboard?id=PauloFer1_pf-auth-server)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=PauloFer1_pf-auth-server&metric=coverage)](https://sonarcloud.io/dashboard?id=PauloFer1_pf-auth-server)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=PauloFer1_pf-auth-server&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=PauloFer1_pf-auth-server)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=PauloFer1_pf-auth-server&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=PauloFer1_pf-auth-server)

- Provide Authentication/Authorization to use in pf applications (JWT access and refresh token).
- Creates Users with specific roles.
(Work in progress)

## Requests
POST /user
```
Authorization:Bearer token
{
	"email" : "admin11",
	"password" : "pass",
	"role" : "admin",
	"subject" : "vst"
}
```
GET /user/{email}
```
Authorization:Bearer token
```
POST /refresh-token
```
X-Refresh-Token:d26aefe8413c49088b1f3ce487e97a29
```
POST /auth
```
{
    "username": "admin",
    "password": "pass"
}
```

## Setup locally 
### Mongo DB
##### This service needs MongoDB 4.0+, as uses transactions
Start Mongo:
```
    ./tools/mongoDB/start-mongo.sh
```
Stop Mongo:
```
    ./tools/mongoDB/stop-mongo.sh
```

#### User for manual test
```
db.user.insertOne({email:"admin", password: "$2a$10$SOnxme6xPgTap68/jUzmnOdzPIpVUlUqmHbiA5Q0i6D3JLi2G8DlG", role: "admin", subject: "emp"})

{
	"username": "admin",
	"password": "pass"
}
```

#### MongoDb Collections
```
db.user.find({})
db.refresh_token.find({})
```

### Kafka and Schema registry
##### This service uses Kafka and Confluence Schema registry, a docker-compose is provided with both running under Zookeeper 
```
    1. go to tools/kafka
    2. docker-compose up -d
```


### AVRO
Generate AVRO sources:
```
mvn generate-sources
```
