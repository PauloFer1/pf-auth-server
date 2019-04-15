# Authentication Service
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=PauloFer1_pf-auth-server&metric=alert_status)](https://sonarcloud.io/dashboard?id=PauloFer1_pf-auth-server)

Provide JWT tokens to authenticate in pf applications.

## Setup
This service uses MongoDB, to run it locally:
- docker run -d -p 27017:27017 -p 28017:28017 -e AUTH=no bitnami/mongodb:latest

### User for test
db.user.insertOne({email:"admin", password: "$2a$10$SOnxme6xPgTap68/jUzmnOdzPIpVUlUqmHbiA5Q0i6D3JLi2G8DlG", role: "admin", subject: "emp"})

{
	"username": "admin",
	"password": "pass"
}

### MongoDb Collections
db.user.find({})
db.refresh_token.find({})
