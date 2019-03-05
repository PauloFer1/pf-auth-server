package com.pfernand.pfauthserver.config;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

@Configuration
public class DatabaseConfiguration
{

    public enum MONGO_COLLECTIONS {
        AUTHENTICATION_COLLECTION("user");

        private String collection;

        public String collection() {
            return collection;
        }

        MONGO_COLLECTIONS(final String collection) {
            this.collection = collection;
        }
    }

    @Bean
    MongoDbFactory mongoDbFactory(@Value("${mongodb.host}") final String mongoDbHost,
                                  @Value("${mongodb.database.name}") final String databaseName) {
        return new SimpleMongoDbFactory(new MongoClient(mongoDbHost), databaseName);
    }
}
