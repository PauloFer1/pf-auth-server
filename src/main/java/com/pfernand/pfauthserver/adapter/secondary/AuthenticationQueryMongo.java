package com.pfernand.pfauthserver.adapter.secondary;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.port.secondary.AuthenticationQuery;
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
    public Optional<UserAuthDetails> getUserFromEmail(String email) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_CRITERIA).is(email));
        return Optional.ofNullable(mongoTemplate.findOne(
                query,
                UserAuthDetails.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection())
        );
    }
}
