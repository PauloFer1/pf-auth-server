package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;

import java.util.Optional;

public interface RefreshTokenQuery {
    Optional<RefreshTokenSession> getSession(final String refreshToken);
}
