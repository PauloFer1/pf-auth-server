package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuth;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.port.secondary.event.UserAuthenticationPublisher;
import com.pfernand.pfauthserver.port.secondary.event.dto.UserAuthEvent;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "admin";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final Instant NOW = Instant.now();
    private static final UserAuthDto USER_AUTH_DETAILS = UserAuthDto.builder()
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
            .build();
    private static final UserAuthEvent USER_AUTH_EVENT = UserAuthEvent.builder()
            .email(EMAIL)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .createdAt(USER_AUTH_ENTITY.getCreatedAt())
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
    private Clock clock;

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
                .build();

        // When
        Mockito.when(clock.instant())
                .thenReturn(NOW);
        Mockito.when(bCryptPasswordEncoder.encode(PASSWORD))
                .thenReturn(ENCODED_PASSWORD);
        Mockito.when(authenticationCommand.insertUser(USER_AUTH_ENTITY))
                .thenReturn(USER_AUTH_ENTITY);
        UserAuth userAuth = authenticationService.insertUser(USER_AUTH_DETAILS);

        // Then
        assertEquals(expectedUserAuth, userAuth);
        Mockito.verify(userAuthenticationPublisher).publishEvent(USER_AUTH_EVENT);
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