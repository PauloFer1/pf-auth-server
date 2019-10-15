package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.RefreshTokenExpiredException;
import com.pfernand.pfauthserver.core.exceptions.RefreshTokenNotFoundException;
import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuthProperties;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.security.TokenFactory;
import com.pfernand.pfauthserver.core.security.model.AccessTokenSession;
import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(MockitoJUnitRunner.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenQuery refreshTokenQuery;

    @Mock
    private RefreshTokenCommand refreshTokenCommand;

    @Mock
    private AuthenticationQuery authenticationQuery;

    @Mock
    private TokenFactory tokenFactory;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    public void refreshTokenWhenTokenNotFoundThenThrowException() {
        // Given
        final String refreshToken = "token";

        // When
        Mockito.when(refreshTokenQuery.getSession(refreshToken))
                .thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(RefreshTokenNotFoundException.class)
                .isThrownBy(() -> refreshTokenService.refreshToken(refreshToken))
                .withMessage("RefreshTokenSession not found for: " + refreshToken);
    }

    @Test
    public void refreshTokenWhenTokenExpiredThenThrowException() {
        // Given
        final String refreshToken = "token";
        final RefreshTokenSession refreshTokenSession = RefreshTokenSession.builder()
                .refreshToken(refreshToken)
                .expirationDate(Instant.now().minusSeconds(1))
                .userUuid("UUID")
                .build();

        // When
        Mockito.when(refreshTokenQuery.getSession(refreshToken))
                .thenReturn(Optional.ofNullable(refreshTokenSession));

        // Then
        assertThatExceptionOfType(RefreshTokenExpiredException.class)
                .isThrownBy(() -> refreshTokenService.refreshToken(refreshToken))
                .withMessage("RefreshToken has expired: " + refreshToken);
    }

    @Test
    public void refreshTokenWhenUserNotFoundThrowException() {
        // Given
        final String refreshToken = "token";
        final RefreshTokenSession refreshTokenSession = RefreshTokenSession.builder()
                .refreshToken(refreshToken)
                .expirationDate(Instant.now().plusSeconds(60))
                .userUuid("UUID")
                .build();

        // When
        Mockito.when(refreshTokenQuery.getSession(refreshToken))
                .thenReturn(Optional.of(refreshTokenSession));
        Mockito.when(authenticationQuery.getUserFromEmail(refreshTokenSession.getUserUuid()))
                .thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(UserDetailsNotFoundException.class)
                .isThrownBy(() -> refreshTokenService.refreshToken(refreshToken))
                .withMessage("UserAuthDetails details not found for: " + refreshTokenSession.getUserUuid());
    }

    @Test
    public void refreshTokenWhenValidInputSavesAndReturnsObject() {
        // Given
        final String refreshToken = "token";
        final RefreshTokenSession refreshTokenSession = RefreshTokenSession.builder()
                .refreshToken(refreshToken)
                .expirationDate(Instant.now().plusSeconds(60))
                .userUuid("UUID")
                .build();
        final UserAuthEntity userAuthEntity = UserAuthEntity.builder()
                .role("role")
                .password("password")
                .email("email")
                .subject(UserAuthSubject.CUSTOMER)
                .build();
        final AccessTokenSession accessTokenSession = AccessTokenSession.builder()
                .signedToken("token")
                .expirationTime(Instant.now().plusSeconds(60))
                .notBefore(Instant.now())
                .type("type")
                .build();

        // When
        Mockito.when(refreshTokenQuery.getSession(refreshToken))
                .thenReturn(Optional.of(refreshTokenSession));
        Mockito.when(authenticationQuery.getUserFromEmail(refreshTokenSession.getUserUuid()))
                .thenReturn(Optional.of(userAuthEntity));
        Mockito.when(tokenFactory.createAccessToken(userAuthEntity.getEmail(),
                Collections.singletonList(userAuthEntity.getRole()),
                userAuthEntity.getSubject().getSubject()))
                .thenReturn(accessTokenSession);
        Mockito.when(refreshTokenCommand.saveSession(refreshTokenSession))
                .thenReturn(refreshTokenSession);
        UserAuthProperties userAuthProperties = refreshTokenService.refreshToken(refreshToken);

        // Then
        Mockito.verify(refreshTokenCommand, Mockito.times(1)).saveSession(refreshTokenSession);
        assertThat(userAuthProperties.getAccessToken()).isEqualTo(accessTokenSession.getSignedToken());
        assertThat(userAuthProperties.getRefreshToken()).isEqualTo(refreshTokenSession.getRefreshToken());
        assertThat(userAuthProperties.getExpiresOn()).isEqualTo(new Date(accessTokenSession.getExpirationTime().toEpochMilli()));
        assertThat(userAuthProperties.getTokenType()).isEqualTo(accessTokenSession.getType());
    }
}