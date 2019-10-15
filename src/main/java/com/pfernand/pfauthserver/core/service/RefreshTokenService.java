package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.RefreshTokenExpiredException;
import com.pfernand.pfauthserver.core.exceptions.RefreshTokenNotFoundException;
import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuthProperties;
import com.pfernand.pfauthserver.core.security.model.AccessTokenSession;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenQuery;
import com.pfernand.pfauthserver.core.security.TokenFactory;
import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Named
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenQuery refreshTokenQuery;
    private final RefreshTokenCommand refreshTokenCommand;
    private final AuthenticationQuery authenticationQuery;
    private final TokenFactory tokenFactory;

    public UserAuthProperties refreshToken(final String refreshToken) {
        log.info("Refreshing {}", refreshToken);
        final RefreshTokenSession refreshTokenSession = refreshTokenQuery.getSession(refreshToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException(refreshToken));
        validateRefreshTokenSession(refreshTokenSession);
        final UserAuthEntity userAuthEntity = authenticationQuery.getUserFromEmail(refreshTokenSession.getUserUuid())
                .orElseThrow(() -> new UserDetailsNotFoundException(refreshTokenSession.getUserUuid()));

        final AccessTokenSession accessTokenSession = tokenFactory.createAccessToken(
                userAuthEntity.getEmail(),
                Collections.singletonList(userAuthEntity.getRole()),
                userAuthEntity.getSubject().getSubject()
        );

        refreshTokenCommand.saveSession(refreshTokenSession);

        return UserAuthProperties.builder()
                .accessToken(accessTokenSession.getSignedToken())
                .refreshToken(refreshToken)
                .expiresOn(new Date(accessTokenSession.getExpirationTime().toEpochMilli()))
                .tokenType(accessTokenSession.getType())
                .build();
    }

    private void validateRefreshTokenSession(final RefreshTokenSession refreshTokenSession) {
        if (refreshTokenSession.getExpirationDate().compareTo(Instant.now()) < 0) {
            throw new RefreshTokenExpiredException(refreshTokenSession.getRefreshToken());
        }
    }
}
