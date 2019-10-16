package com.pfernand.pfauthserver.core.exceptions;

public class InvalidEmailException extends RuntimeException {

    private static final String TEMPLATE = "Invalid email: %s";

    public InvalidEmailException(final String email) {
        super(String.format(TEMPLATE, email));
    }
}
