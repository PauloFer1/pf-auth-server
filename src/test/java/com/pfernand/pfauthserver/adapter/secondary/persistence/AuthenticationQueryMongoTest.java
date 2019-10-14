package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationQueryMongoTest {

    private static final String EMAIL_CRITERIA = "email";
    private static final String EMAIL = "paulo@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "admin";
    private static final UserAuthEntity USER_AUTH_ENTITY = UserAuthEntity.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .build();

    private final Query query = new Query();

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AuthenticationQueryMongo authenticationQueryMongo;

    @Before
    public void setUp() {
        query.addCriteria(Criteria.where(EMAIL_CRITERIA).is(EMAIL));
    }

    @Test
    public void getUserFromEmailWhenEmailNotFoundThenReturnEmpty() {
        // Given
        // When
        Optional<UserAuthEntity> optionalUserAuthEntity = authenticationQueryMongo.getUserFromEmail(EMAIL);

        // Then
        assertEquals(Optional.empty(), optionalUserAuthEntity);
    }

    @Test
    public void getUserFromEmailWhenValidEmailThenReturnsUser() {
        // Given
        // When
        Mockito.when(mongoTemplate.findOne(query,
                UserAuthEntity.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection()))
                .thenReturn(USER_AUTH_ENTITY);
        Optional<UserAuthEntity> optionalUserAuthEntity = authenticationQueryMongo.getUserFromEmail(EMAIL);

        // Then
        assertEquals(Optional.of(USER_AUTH_ENTITY), optionalUserAuthEntity);
    }
}