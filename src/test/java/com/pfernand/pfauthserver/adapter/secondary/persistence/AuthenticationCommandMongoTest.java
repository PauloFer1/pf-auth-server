package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.adapter.secondary.persistence.AuthenticationCommandMongo;
import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationCommandMongoTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "admin";
    private static final UserAuthDetails USER_AUTH_DETAILS = UserAuthDetails.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .build();

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AuthenticationCommandMongo authenticationCommandMongo;

    @Test
    public void insertUserWithValidValues() {
        // Given
        // When
        Mockito.doNothing()
                .when(mongoTemplate)
                .insert(USER_AUTH_DETAILS, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
        UserAuthDetails userAuthDetails = authenticationCommandMongo.insertUser(USER_AUTH_DETAILS);

        // Then
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .insert(USER_AUTH_DETAILS, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
        assertEquals(USER_AUTH_DETAILS, userAuthDetails);
    }

}