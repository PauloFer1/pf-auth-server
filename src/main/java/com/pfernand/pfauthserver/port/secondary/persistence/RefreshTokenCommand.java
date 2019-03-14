package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;

public interface RefreshTokenCommand {
    RefreshTokenSession saveSession(final RefreshTokenSession refreshTokenSession);
}
