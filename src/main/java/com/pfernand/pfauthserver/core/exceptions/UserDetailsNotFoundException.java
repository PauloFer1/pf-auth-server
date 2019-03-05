package com.pfernand.pfauthserver.core.exceptions;

public class UserDetailsNotFoundException extends RuntimeException {

    private static final String TEMPLATE = "UserAuthDetails details not found for: %s";

    public UserDetailsNotFoundException(final String userEmail) {
        super(String.format(TEMPLATE, userEmail));
    }
}
