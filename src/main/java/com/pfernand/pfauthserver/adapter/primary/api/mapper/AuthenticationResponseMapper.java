package com.pfernand.pfauthserver.adapter.primary.api.mapper;

import com.pfernand.pfauthserver.core.model.UserAuthProperties;
import com.pfernand.pfauthserver.core.security.model.AuthenticationResponse;

import javax.inject.Named;

@Named
public class AuthenticationResponseMapper {

    public AuthenticationResponse map(final UserAuthProperties userAuthProperties) {
        return AuthenticationResponse.builder()
                .accessToken(userAuthProperties.getAccessToken())
                .refreshToken(userAuthProperties.getRefreshToken())
                .expiresOn(userAuthProperties.getExpiresOn().getTime())
                .tokenType(userAuthProperties.getTokenType())
                .build();
    }
}
