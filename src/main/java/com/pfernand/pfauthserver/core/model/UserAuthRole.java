package com.pfernand.pfauthserver.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.stream.Stream;

public enum UserAuthRole {
    @JsonProperty("admin")
    ADMIN("admin"),
    @JsonProperty("user")
    USER("user");

    private final String role;


    UserAuthRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return role;
    }

    public static UserAuthRole fromString(final String value) {
        return Stream.of(UserAuthRole.values()).filter(v -> v.getRole().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No Role with name: %s", value)));
    }
}
