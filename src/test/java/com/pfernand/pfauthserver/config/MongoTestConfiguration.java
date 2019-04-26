package com.pfernand.pfauthserver.config;

import com.github.fakemongo.Fongo;
import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoTestConfiguration {

    @Bean
    public Mongo mongo() {
        return new Fongo("auth").getMongo();
    }
}
