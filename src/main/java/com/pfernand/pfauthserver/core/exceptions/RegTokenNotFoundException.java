package com.pfernand.pfauthserver.core.exceptions;

import java.util.UUID;

public class RegTokenNotFoundException extends RuntimeException {

    private static final String TEMPLATE = "Registration token: %s not found, associated with user: %s";

    public RegTokenNotFoundException(final UUID userUuid, final String token) {
        super(String.format(TEMPLATE, token, userUuid));
    }
}
