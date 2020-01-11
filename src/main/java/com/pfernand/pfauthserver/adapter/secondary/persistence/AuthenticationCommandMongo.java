package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.inject.Named;
import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class AuthenticationCommandMongo implements AuthenticationCommand {

    private static final String USER_UUID_FIELD = "userUuid";
    private static final String ACTIVE_FIELD = "active";

    private final MongoTemplate mongoTemplate;

    @Override
    public UserAuthEntity insertUser(final UserAuthEntity userAuthEntity) {
        log.debug("Saving {}", userAuthEntity);
        mongoTemplate.insert(userAuthEntity, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
        return userAuthEntity;
    }

    @Override
    public UserAuthEntity activateUser(final UUID userUuid) {
        log.debug("Activating user: {}", userUuid);
        final Query query = new Query();
        query.addCriteria(Criteria.where(USER_UUID_FIELD).is(userUuid));
        final Update update = new Update().set(ACTIVE_FIELD, true);
        return mongoTemplate.findAndModify(
                query,
                update,
                UserAuthEntity.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection()
        );
    }


}