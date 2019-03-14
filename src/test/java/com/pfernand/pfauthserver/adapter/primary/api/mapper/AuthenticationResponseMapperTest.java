package com.pfernand.pfauthserver.adapter.primary.api.mapper;

import com.pfernand.pfauthserver.core.model.UserAuthProperties;
import com.pfernand.pfauthserver.core.security.model.AuthenticationResponse;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.*;

public class AuthenticationResponseMapperTest {

    private AuthenticationResponseMapper authenticationResponseMapper = new AuthenticationResponseMapper();

    @Test
    public void mapTest() {
        // Given
        Instant now = Instant.now();
        final UserAuthProperties userAuthProperties = UserAuthProperties.builder()
                .tokenType("type")
                .expiresOn(new Date(now.getEpochSecond()))
                .refreshToken("refreshToken")
                .accessToken("accessToken")
                .build();

        // When
        AuthenticationResponse authenticationResponse = authenticationResponseMapper.map(userAuthProperties);

        // Then
        assertEquals(userAuthProperties.getAccessToken(), authenticationResponse.getAccessToken());
        assertEquals(userAuthProperties.getRefreshToken(), authenticationResponse.getRefreshToken());
        assertEquals(userAuthProperties.getTokenType(), authenticationResponse.getTokenType());
        assertEquals(userAuthProperties.getExpiresOn().getTime(), authenticationResponse.getExpiresOn());
    }

}