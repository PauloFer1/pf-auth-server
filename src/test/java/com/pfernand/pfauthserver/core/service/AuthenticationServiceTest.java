package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.ExistentUserEmailException;
import com.pfernand.pfauthserver.core.exceptions.InvalidEmailException;
import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuth;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.security.TokenFactory;
import com.pfernand.pfauthserver.core.validation.UserAuthValidation;
import com.pfernand.pfauthserver.port.secondary.event.UserAuthenticationPublisher;
import com.pfernand.pfauthserver.port.secondary.event.dto.UserAuthEvent;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.RegistrationTokenCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "admin";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final Instant NOW = Instant.now();
    private static final UserAuthDto USER_AUTH_DTO = UserAuthDto.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .build();
    private static final UserAuthEntity USER_AUTH_ENTITY = UserAuthEntity.builder()
            .email(EMAIL)
            .password(ENCODED_PASSWORD)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .createdAt(NOW)
            .userUuid(UUID.randomUUID())
            .build();
    private static final RegistrationTokenEntity REGISTRATION_TOKEN_ENTITY = RegistrationTokenEntity.builder()
            .userUuid(USER_AUTH_ENTITY.getUserUuid())
            .expirationDate(NOW.plus(24, ChronoUnit.HOURS))
            .regToken(UUID.randomUUID().toString())
            .build();
    private static final UserAuthEvent USER_AUTH_EVENT = UserAuthEvent.builder()
            .userUuid(USER_AUTH_ENTITY.getUserUuid().toString())
            .email(EMAIL)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .createdAt(USER_AUTH_ENTITY.getCreatedAt())
            .authToken(REGISTRATION_TOKEN_ENTITY.getRegToken())
            .build();

    @Mock
    private AuthenticationQuery authenticationQuery;

    @Mock
    private AuthenticationCommand authenticationCommand;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private UserAuthenticationPublisher userAuthenticationPublisher;

    @Mock
    private UserAuthValidation userAuthValidation;

    @Mock
    private Clock clock;

    @Mock
    private TokenFactory tokenFactory;

    @Mock
    private RegistrationTokenCommand registrationTokenCommand;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void insertUserWhenValidValuesThenReturnUser() {
        // Given
        final UserAuth expectedUserAuth = UserAuth.builder()
                .role(USER_AUTH_ENTITY.getRole())
                .password(USER_AUTH_ENTITY.getPassword())
                .email(USER_AUTH_ENTITY.getEmail())
                .subject(UserAuthSubject.CUSTOMER)
                .createdAt(NOW)
                .userUuid(USER_AUTH_ENTITY.getUserUuid())
                .build();

        // When
        Mockito.when(clock.instant())
                .thenReturn(NOW);
        Mockito.when(bCryptPasswordEncoder.encode(PASSWORD))
                .thenReturn(ENCODED_PASSWORD);
        Mockito.when(tokenFactory.generateUuid())
                .thenReturn(USER_AUTH_ENTITY.getUserUuid());
        Mockito.when(tokenFactory.createRefreshToken())
                .thenReturn(REGISTRATION_TOKEN_ENTITY.getRegToken());
        Mockito.when(authenticationCommand.insertUser(USER_AUTH_ENTITY))
                .thenReturn(USER_AUTH_ENTITY);
        RegistrationTokenEntity registrationTokenEntity = REGISTRATION_TOKEN_ENTITY;
        Mockito.when(registrationTokenCommand.insert(REGISTRATION_TOKEN_ENTITY))
                .thenReturn(REGISTRATION_TOKEN_ENTITY);
        UserAuth userAuth = authenticationService.insertUser(USER_AUTH_DTO);

        // Then
        assertThat(expectedUserAuth).isEqualTo(userAuth);
        Mockito.verify(userAuthenticationPublisher).publishEvent(USER_AUTH_EVENT);
        Mockito.verify(userAuthValidation).validate(USER_AUTH_DTO);
    }

    @Test
    public void insertUserWhenInvalidEmailThrowsException() {
        // Given
        final UserAuthDto invalid = UserAuthDto.builder()
                .email("paulo@mail.c")
                .password(PASSWORD)
                .role(ROLE)
                .subject(UserAuthSubject.CUSTOMER)
                .build();
        // When
        Mockito.doThrow(new InvalidEmailException(invalid.getEmail()))
                .when(userAuthValidation).validate(invalid);
        // Then
        assertThatExceptionOfType(InvalidEmailException.class)
                .isThrownBy(() -> authenticationService.insertUser(invalid))
                .withMessage(String.format("Invalid email: %s", invalid.getEmail()));
    }

    @Test
    public void insertUserWhenEmailAlreadyExistsThrowsException() {
        // Given
        // When
        Mockito.when(authenticationQuery.getUserFromEmail(EMAIL))
                .thenReturn(Optional.of(USER_AUTH_ENTITY));

        // Then
        assertThatExceptionOfType(ExistentUserEmailException.class)
                .isThrownBy(() -> authenticationService.insertUser(USER_AUTH_DTO))
                .withMessage(String.format("Cannot create user. Email %s already existent in the system.", USER_AUTH_DTO.getEmail()));
    }

    @Test
    public void retrieveUserFromEmailWhenEmailNotFoundThenThrowException() {
        // Given
        // When
        Mockito.when(authenticationQuery.getUserFromEmail(EMAIL))
                .thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(UserDetailsNotFoundException.class)
                .isThrownBy(() -> authenticationService.retrieveUserFromEmail(EMAIL))
                .withMessageContaining(String.format("UserAuthDetails details not found for: %s", EMAIL));

    }

    @Test
    public void retrieveUserFromEmailWhenValidEmailThenReturnUser() {
        // Given
        final UserAuth expectedUserAuth = UserAuth.builder()
                .userUuid(USER_AUTH_ENTITY.getUserUuid())
                .role(USER_AUTH_ENTITY.getRole())
                .password(USER_AUTH_ENTITY.getPassword())
                .email(USER_AUTH_ENTITY.getEmail())
                .subject(UserAuthSubject.CUSTOMER)
                .createdAt(NOW)
                .build();
        // When
        Mockito.when(authenticationQuery.getUserFromEmail(EMAIL))
                .thenReturn(Optional.of(USER_AUTH_ENTITY));
        UserAuth userAuth = authenticationService.retrieveUserFromEmail(EMAIL);

        // Then
        assertEquals(expectedUserAuth, userAuth);
    }

}