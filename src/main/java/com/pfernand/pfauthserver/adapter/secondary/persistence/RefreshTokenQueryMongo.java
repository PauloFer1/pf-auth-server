package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenQuery;
import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.inject.Named;
import java.util.Optional;

@Named
@RequiredArgsConstructor
public class RefreshTokenQueryMongo implements RefreshTokenQuery {

    private static final String REFRESH_TOKEN_CRITERIA = "refreshToken";
    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<RefreshTokenSession> getSession(String refreshToken) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(REFRESH_TOKEN_CRITERIA).is(refreshToken));
        return Optional.ofNullable(mongoTemplate.findOne(
                query,
                RefreshTokenSession.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.REFRESH_TOKEN_COLLECTION.collection()
        ));
    }
}
