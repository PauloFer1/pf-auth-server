package com.pfernand.pfauthserver.adapter.secondary.event.exception;

public class EventSendException extends RuntimeException {

    private static final String TEMPLATE = "Could not send/acknowledged event from kafka. Will fail to insert user [Rollback].";

    public EventSendException() {
        super(TEMPLATE);
    }
}
