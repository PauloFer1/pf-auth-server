package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.inject.Named;
import java.util.Optional;

@Slf4j
@Named
@RequiredArgsConstructor
public class AuthenticationQueryMongo implements AuthenticationQuery {

    private static final String EMAIL_CRITERIA = "email";

    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<UserAuthEntity> getUserFromEmail(String email) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_CRITERIA).is(email));
        return Optional.ofNullable(mongoTemplate.findOne(
                query,
                UserAuthEntity.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection())
        );
    }
}
