package com.pfernand.pfauthserver.config;

//import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//@Configuration
public class MongoTestConfiguration {

//    @Bean
//    public Mongo mongo() {
//        return new Fongo("pf-auth").getMongo();
//    }
//
//    @Bean
//    @Primary
//    public MongoClient mongoClient() {
//        return new Fongo(getDatabaseName()).getMongo();
//    }


    protected String getDatabaseName() {
        return "pf-auth";
    }
}
