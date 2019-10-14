package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
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
    public UserAuthEntity insertUser(final UserAuthEntity userAuthEntity) {
        log.debug("Saving {}", userAuthEntity);
        mongoTemplate.insert(userAuthEntity, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
        return userAuthEntity;
    }
}
