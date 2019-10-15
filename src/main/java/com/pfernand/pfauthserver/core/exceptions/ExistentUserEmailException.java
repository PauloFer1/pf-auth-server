package com.pfernand.pfauthserver.core.exceptions;

public class ExistentUserEmailException extends RuntimeException {

    private static final String TEMPLATE = "Cannot create user. Email %s already existent in the system.";

    public ExistentUserEmailException(final String email) {
        super(String.format(TEMPLATE, email));
    }
}
