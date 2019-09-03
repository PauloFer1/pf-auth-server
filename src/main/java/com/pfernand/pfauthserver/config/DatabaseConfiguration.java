package com.pfernand.pfauthserver.config;

import com.mongodb.MongoClient;
import com.pfernand.pfauthserver.adapter.secondary.persistence.converter.UserAuthSubjectReadConverter;
import com.pfernand.pfauthserver.adapter.secondary.persistence.converter.UserAuthSubjectWriterConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Arrays;

@Configuration
public class DatabaseConfiguration
{

    public enum MONGO_COLLECTIONS {
        AUTHENTICATION_COLLECTION("user"),
        REFRESH_TOKEN_COLLECTION("refresh_token");

        private String collection;

        public String collection() {
            return collection;
        }

        MONGO_COLLECTIONS(final String collection) {
            this.collection = collection;
        }
    }

//    @Bean
//    MongoDbFactory mongoDbFactory(@Value("${mongodb.host}") final String mongoDbHost,
//                                  @Value("${mongodb.database.name}") final String databaseName) {
//        return new SimpleMongoDbFactory(new MongoClient(mongoDbHost), databaseName);
//    }

//    @Bean
//    MongoDbFactory mongoDbFactory(@Value("${mongodb.connection.string}") final String mongoDbConnString,
//                                  @Value("${mongodb.database.name}") final String databaseName) {
//        return new SimpleMongoDbFactory(new MongoClient(mongoDbConnString), databaseName);
//    }

    @Bean
    public MongoTemplate mongoTemplate(final MongoDbFactory mongoDbFactory, final MappingMongoConverter mongoConverter) {
        return new MongoTemplate(mongoDbFactory, mongoConverter);
    }

    @Bean
    MongoTransactionManager transactionManager(MongoDbFactory mongoDbFactory) {
        return new MongoTransactionManager(mongoDbFactory);
    }

    @Bean
    @Primary
    public CustomConversions customConversions(final UserAuthSubjectReadConverter userAuthSubjectReadConverter, final UserAuthSubjectWriterConverter userAuthSubjectWriterConverter) {
        return new MongoCustomConversions(Arrays.asList(userAuthSubjectReadConverter, userAuthSubjectWriterConverter));
    }

    @Bean
    public MappingMongoConverter mongoConverter(final MongoDbFactory mongoDbFactory, final CustomConversions customConversions) {
        MongoMappingContext mappingContext = new MongoMappingContext();
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
        mongoConverter.setCustomConversions(customConversions);
        mongoConverter.afterPropertiesSet();
        return mongoConverter;
    }
}
