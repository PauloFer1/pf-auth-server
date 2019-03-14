package com.pfernand.pfauthserver.core.exceptions;

public class RefreshTokenExpiredException extends RuntimeException {

    private static final String TEMPLATE = "RefreshToken has expired: %s";

    public RefreshTokenExpiredException(final String refreshToken) {
        super(String.format(TEMPLATE, refreshToken));
    }
}
