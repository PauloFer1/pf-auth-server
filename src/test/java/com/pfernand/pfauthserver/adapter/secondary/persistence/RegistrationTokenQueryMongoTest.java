package com.pfernand.pfauthserver.adapter.secondary.persistence;

import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;
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
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationTokenQueryMongoTest {

    private static final UUID USER_UUID = UUID.randomUUID();
    private static final String REG_TOKEN = UUID.randomUUID().toString();

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RegistrationTokenQueryMongo registrationTokenQueryMongo;

    @Test
    public void retrieveFromUserAndTokenWhenFoundThenReturnToken() {
        // Given
        final RegistrationTokenEntity registrationTokenEntity = RegistrationTokenEntity.builder()
                .regToken(REG_TOKEN)
                .userUuid(USER_UUID)
                .expirationDate(Instant.now())
                .build();
        final Query query = new Query();
        query.addCriteria(Criteria.where("userUuid").is(USER_UUID)
                .and("regToken").is(REG_TOKEN));

        // When
        Mockito.when(mongoTemplate.findOne(
                query,
                RegistrationTokenEntity.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.REG_TOKEN_COLLECTION.collection())
        ).thenReturn(registrationTokenEntity);
        Optional<RegistrationTokenEntity> result = registrationTokenQueryMongo.retrieveFromUserAndToken(USER_UUID, REG_TOKEN);

        // Then
        assertEquals(result.get(), registrationTokenEntity);
    }

    @Test
    public void retrieveFromUserAndTokenWhenNotFounfThenReturnsEmpty() {
        // Given
        final Query query = new Query();
        query.addCriteria(Criteria.where("userUuid").is(USER_UUID)
                .and("regToken").is(REG_TOKEN));

        // When
        Mockito.when(mongoTemplate.findOne(
                query,
                RegistrationTokenEntity.class,
                DatabaseConfiguration.MONGO_COLLECTIONS.REG_TOKEN_COLLECTION.collection())
        ).thenReturn(null);
        Optional<RegistrationTokenEntity> result = registrationTokenQueryMongo.retrieveFromUserAndToken(USER_UUID, REG_TOKEN);

        // Then
        assertFalse(result.isPresent());
    }
}