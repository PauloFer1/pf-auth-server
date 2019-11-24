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

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationTokenCommandMongoTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RegistrationTokenCommandMongo registrationTokenCommandMongo;

    @Test
    public void insertShouldCallMongoTemplate() {
        // Given
        final RegistrationTokenEntity registrationTokenEntity = RegistrationTokenEntity.builder()
                .regToken(UUID.randomUUID().toString())
                .expirationDate(Instant.now())
                .userUuid(UUID.randomUUID())
                .build();

        // When
        Mockito.when(mongoTemplate.insert(registrationTokenEntity, DatabaseConfiguration.MONGO_COLLECTIONS.REG_TOKEN_COLLECTION.collection()))
                .thenReturn(registrationTokenEntity);
        RegistrationTokenEntity result = registrationTokenCommandMongo.insert(registrationTokenEntity);

        // Then
        assertEquals(registrationTokenEntity, result);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .insert(registrationTokenEntity, DatabaseConfiguration.MONGO_COLLECTIONS.REG_TOKEN_COLLECTION.collection());
    }
}