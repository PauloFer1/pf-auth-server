package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RefreshTokenCommandMongoTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RefreshTokenCommandMongo refreshTokenCommandMongo;

    @Test
    public void saveSession() {
        // Given
        Instant now = Instant.now();
        final RefreshTokenSession refreshTokenSession = RefreshTokenSession.builder()
                .refreshToken("token")
                .expirationDate(now)
                .userUuid("UUID")
                .build();

        // When
        Mockito.when(mongoTemplate.insert(refreshTokenSession, DatabaseConfiguration.MONGO_COLLECTIONS.REFRESH_TOKEN_COLLECTION.collection()))
            .thenReturn(refreshTokenSession);

        RefreshTokenSession refreshTokenSessionResult = refreshTokenCommandMongo.saveSession(refreshTokenSession);

        // Then
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .insert(refreshTokenSession, DatabaseConfiguration.MONGO_COLLECTIONS.REFRESH_TOKEN_COLLECTION.collection());
        assertEquals(refreshTokenSession, refreshTokenSessionResult);
    }

}