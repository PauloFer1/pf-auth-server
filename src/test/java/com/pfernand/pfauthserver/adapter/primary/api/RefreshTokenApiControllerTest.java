package com.pfernand.pfauthserver.adapter.primary.api;

import com.pfernand.pfauthserver.adapter.primary.api.mapper.AuthenticationResponseMapper;
import com.pfernand.pfauthserver.adapter.primary.api.validation.InputApiValidation;
import com.pfernand.pfauthserver.core.exceptions.RefreshTokenNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuthProperties;
import com.pfernand.pfauthserver.core.security.model.AuthenticationResponse;
import com.pfernand.pfauthserver.core.service.RefreshTokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RefreshTokenApiControllerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationResponseMapper authenticationResponseMapper;

    @Mock
    private InputApiValidation inputApiValidation;

    @InjectMocks
    private RefreshTokenApiController refreshTokenApiController;

    @Test
    public void refreshWhenValidInputReturnValidResponse() {
        // Given
        Instant now = Instant.now();
        final String refreshToken = "token";
        final UserAuthProperties userAuthProperties = UserAuthProperties.builder()
                .accessToken("access")
                .refreshToken(refreshToken)
                .expiresOn(new Date(now.getEpochSecond()))
                .tokenType("TYPE")
                .build();
        final AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .accessToken(userAuthProperties.getAccessToken())
                .refreshToken(userAuthProperties.getRefreshToken())
                .expiresOn(userAuthProperties.getExpiresOn().getTime())
                .tokenType(userAuthProperties.getTokenType())
                .build();

        // When
        Mockito.when(refreshTokenService.refreshToken(refreshToken))
                .thenReturn(userAuthProperties);
        Mockito.when(authenticationResponseMapper.map(userAuthProperties))
                .thenReturn(authenticationResponse);
        ResponseEntity<AuthenticationResponse> response = refreshTokenApiController.refresh(refreshToken);

        // Then
        Mockito.verify(inputApiValidation).validateLength(refreshToken);
        Mockito.verify(inputApiValidation).encodeForLog(refreshToken);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authenticationResponse, response.getBody());
    }

    @Test
    public void refreshWhenRefreshTokenNotFoundThenExpeptionIsEscalated() {
        // Given
        final String refreshToken = "token";

        // When
        Mockito.when(refreshTokenService.refreshToken(refreshToken))
                .thenThrow(new RefreshTokenNotFoundException(refreshToken));

        // Then
        assertThatExceptionOfType(RefreshTokenNotFoundException.class)
                .isThrownBy(() -> refreshTokenApiController.refresh(refreshToken))
                .withMessageContaining(refreshToken);
    }

}