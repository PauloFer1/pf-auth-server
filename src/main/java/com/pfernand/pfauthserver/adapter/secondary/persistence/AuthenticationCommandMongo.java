package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.inject.Named;

@Slf4j
@Named
@RequiredArgsConstructor
public class AuthenticationCommandMongo implements AuthenticationCommand {

    private final MongoTemplate mongoTemplate;

    @Override
    public UserAuthDetails insertUser(final UserAuthDetails userAuthDetails) {
        log.info("Saving {}", userAuthDetails);
        mongoTemplate.insert(userAuthDetails, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
        return userAuthDetails;
    }
}
