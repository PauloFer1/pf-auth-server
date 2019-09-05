#!/usr/bin/env bash
docker run --rm -d -p 27017:27017 -e MONGO_INITDB_DATABASE=pf-auth-db --name mongo mongo:4.0.2 --replSet rs
sleep 1
docker exec -it mongo mongo --eval 'rs.initiate()'