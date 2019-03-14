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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RefreshTokenQueryMongoTest {

    private static final String REFRESH_TOKEN_CRITERIA = "refreshToken";

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RefreshTokenQueryMongo refreshTokenQueryMongo;

    @Test
    public void getSessionWhenFoundThenReturnOptionalWithObject() {
        // Given
        final String refreshToken = "token";
        final Query query = new Query();
        query.addCriteria(Criteria.where(REFRESH_TOKEN_CRITERIA).is(refreshToken));
        final RefreshTokenSession refreshTokenSession = RefreshTokenSession.builder()
                .userUuid("UUID")
                .expirationDate(Instant.now())
                .refreshToken("token")
                .build();

        // When
        Mockito.when(mongoTemplate.findOne(
                query,
                RefreshTokenSession.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.REFRESH_TOKEN_COLLECTION.collection()
        )).thenReturn(refreshTokenSession);
        Optional<RefreshTokenSession> refreshTokenSessionOptional = refreshTokenQueryMongo.getSession(refreshToken);

        // Then
        assertEquals(refreshTokenSession, refreshTokenSessionOptional.get());
    }

    @Test
    public void getSessionWhenNotFoundThenReturnOptionalEmpty() {
        // Given
        final String refreshToken = "token";
        final Query query = new Query();
        query.addCriteria(Criteria.where(REFRESH_TOKEN_CRITERIA).is(refreshToken));

        // When
        Mockito.when(mongoTemplate.findOne(
                query,
                RefreshTokenSession.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.REFRESH_TOKEN_COLLECTION.collection()
        )).thenReturn(null);
        Optional<RefreshTokenSession> refreshTokenSessionOptional = refreshTokenQueryMongo.getSession(refreshToken);

        // Then
        assertEquals(Optional.empty(), refreshTokenSessionOptional);
    }
}