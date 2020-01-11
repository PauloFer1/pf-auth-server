package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.RegTokenExpiredException;
import com.pfernand.pfauthserver.core.exceptions.RegTokenNotFoundException;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.RegistrationTokenQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(MockitoJUnitRunner.class)
public class ActivationServiceTest {

    private static final UUID USER_UUID = UUID.randomUUID();
    private static final String TOKEN = UUID.randomUUID().toString();

    @Mock
    private Clock clock;

    @Mock
    private RegistrationTokenQuery registrationTokenQuery;

    @Mock
    private AuthenticationCommand authenticationCommand;

    @InjectMocks
    private ActivationService activationService;

    @Test
    public void activateUserWhenTokenNotFoundThenThrowsException() {
        // Given
        // When
        Mockito.when(registrationTokenQuery.retrieveFromUserAndToken(USER_UUID, TOKEN))
                .thenReturn(Optional.empty());
        // Then
        assertThatExceptionOfType(RegTokenNotFoundException.class)
                .isThrownBy(() -> activationService.activateUser(USER_UUID, TOKEN))
                .withMessage(String.format("Registration token: %s not found, associated with user: %s", TOKEN, USER_UUID));
    }

    @Test
    public void activateUserWhenTokenExpiredThenThrowsException() {
        // Given
        final Instant now = Instant.now();
        final RegistrationTokenEntity registrationTokenEntity = RegistrationTokenEntity.builder()
                .expirationDate(now.minusSeconds(1))
                .userUuid(USER_UUID)
                .regToken(TOKEN)
                .build();

        // When
        Mockito.when(clock.instant()).thenReturn(now);
        Mockito.when(registrationTokenQuery.retrieveFromUserAndToken(USER_UUID, TOKEN))
                .thenReturn(Optional.of(registrationTokenEntity));

        // Then
        assertThatExceptionOfType(RegTokenExpiredException.class)
                .isThrownBy(() -> activationService.activateUser(USER_UUID, TOKEN))
                .withMessage(String.format("Registration token: %s has expired", registrationTokenEntity));
    }

    @Test
    public void activateUserWhenTokenHasSameThenIsNotExpiredAndReturnsUuid() {
        // Given
        final Instant now = Instant.now();
        final RegistrationTokenEntity registrationTokenEntity = RegistrationTokenEntity.builder()
                .expirationDate(now)
                .userUuid(USER_UUID)
                .regToken(TOKEN)
                .build();
        final UserAuthEntity userAuthEntity = UserAuthEntity.builder()
                .userUuid(USER_UUID)
                .build();

        // When
        Mockito.when(clock.instant()).thenReturn(now);
        Mockito.when(registrationTokenQuery.retrieveFromUserAndToken(USER_UUID, TOKEN))
                .thenReturn(Optional.of(registrationTokenEntity));
        Mockito.when(authenticationCommand.activateUser(USER_UUID))
                .thenReturn(userAuthEntity);
        final UUID result = activationService.activateUser(USER_UUID, TOKEN);

        // Then
        assertEquals(USER_UUID, result);
        Mockito.verify(authenticationCommand, Mockito.times(1))
                .activateUser(USER_UUID);
    }
}