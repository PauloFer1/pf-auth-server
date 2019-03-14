package com.pfernand.pfauthserver.port.primary.api;

public interface RefreshTokenApi<T> {
    T refresh(final String refreshToken);
}
