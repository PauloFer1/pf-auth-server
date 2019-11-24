package com.pfernand.pfauthserver.core.exceptions;

import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;

public class RegTokenExpiredException extends RuntimeException {
    private static final String TEMPLATE = "Registration token: %s has expired";

    public RegTokenExpiredException(final RegistrationTokenEntity registrationTokenEntity) {
        super(String.format(TEMPLATE, registrationTokenEntity));
    }
}
