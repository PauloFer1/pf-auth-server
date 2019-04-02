package com.pfernand.pfauthserver.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.stream.Stream;

public enum UserAuthSubject {
    @JsonProperty("cst")
    CUSTOMER("cst"),
    @JsonProperty("emp")
    EMPLOYEE("emp"),
    @JsonProperty("vst")
    VISITOR("vst"),
    @JsonProperty("svc")
    SERVICE("svc"),
    @JsonProperty("ext")
    EXTERNAL("ext");

    private final String subject;

    UserAuthSubject(final String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return subject;
    }

    public static UserAuthSubject fromString(final String value) {
        return Stream.of(UserAuthSubject.values()).filter(v -> v.getSubject().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum with name: " + value));
    }
}
