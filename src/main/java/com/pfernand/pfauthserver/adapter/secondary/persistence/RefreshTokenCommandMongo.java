package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenCommand;
import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.inject.Named;

@Slf4j
@Named
@RequiredArgsConstructor
public class RefreshTokenCommandMongo implements RefreshTokenCommand {

    private final MongoTemplate mongoTemplate;

    @Override
    public RefreshTokenSession saveSession(RefreshTokenSession refreshTokenSession) {
        log.info("Saving {}", refreshTokenSession);
        mongoTemplate.insert(refreshTokenSession, DatabaseConfiguration.MONGO_COLLECTIONS.REFRESH_TOKEN_COLLECTION.collection());
        return refreshTokenSession;
    }
}
