package com.pfernand.pfauthserver.core.exceptions;

public class RefreshTokenNotFoundException extends RuntimeException {

    private static final String TEMPLATE = "RefreshTokenSession not found for: %s";

    public RefreshTokenNotFoundException(final String refreshToken) {
        super(String.format(TEMPLATE, refreshToken));
    }
}
