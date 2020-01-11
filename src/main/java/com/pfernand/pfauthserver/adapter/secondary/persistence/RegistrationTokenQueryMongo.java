package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.port.secondary.persistence.RegistrationTokenQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.inject.Named;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class RegistrationTokenQueryMongo implements RegistrationTokenQuery {

    private static final String USER_UUID_FIELD = "userUuid";
    private static final String REG_TOKEN_FIELD = "regToken";

    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<RegistrationTokenEntity> retrieveFromUserAndToken(final UUID userUuid, final String regToken) {
        log.debug("Retrieving Reg Token: {} fo user: {}", regToken, userUuid);
        final Query query = new Query();
        query.addCriteria(Criteria.where(USER_UUID_FIELD).is(userUuid)
                .and(REG_TOKEN_FIELD).is(regToken));
        return Optional.ofNullable(mongoTemplate.findOne(
                query,
                RegistrationTokenEntity.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.REG_TOKEN_COLLECTION.collection())
        );
    }
}