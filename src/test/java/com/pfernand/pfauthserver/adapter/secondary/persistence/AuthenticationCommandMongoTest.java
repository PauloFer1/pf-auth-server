package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationCommandMongoTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "admin";
    private static final UserAuthEntity USER_AUTH_ENTITY = UserAuthEntity.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .build();

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AuthenticationCommandMongo authenticationCommandMongo;

    @Test
    public void insertUserWithValidValues() {
        // Given
        // When
        Mockito.when(mongoTemplate.insert(USER_AUTH_ENTITY, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection()))
                .thenReturn(USER_AUTH_ENTITY);
        UserAuthEntity userAuthEntity = authenticationCommandMongo.insertUser(USER_AUTH_ENTITY);

        // Then
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .insert(USER_AUTH_ENTITY, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
        assertEquals(USER_AUTH_ENTITY, userAuthEntity);
    }

    @Test
    public void activateUserShouldCallMongoTemplate() {
        // Given
        final UUID userUuid = UUID.randomUUID();
        final Query query = new Query();
        query.addCriteria(Criteria.where("userUuid").is(userUuid));
        final Update update = new Update().set("active", true);

        // When
        Mockito.when(mongoTemplate.findAndModify(
                query,
                update,
                UserAuthEntity.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection()))
                .thenReturn(USER_AUTH_ENTITY);
        UserAuthEntity result = authenticationCommandMongo.activateUser(userUuid);

        // Then
        assertEquals(USER_AUTH_ENTITY, result);
    }

}