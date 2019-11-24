package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.port.secondary.persistence.RegistrationTokenCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.inject.Named;

@Slf4j
@Named
@RequiredArgsConstructor
public class RegistrationTokenCommandMongo implements RegistrationTokenCommand {

    private final MongoTemplate mongoTemplate;

    @Override
    public RegistrationTokenEntity insert(RegistrationTokenEntity registrationTokenEntity) {
        log.debug("Saving registration token: {}", registrationTokenEntity);
        return mongoTemplate.insert(registrationTokenEntity, DatabaseConfiguration.MONGO_COLLECTIONS.REG_TOKEN_COLLECTION.collection());
    }
}
