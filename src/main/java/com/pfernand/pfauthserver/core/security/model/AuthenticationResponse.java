package com.pfernand.pfauthserver.core.security.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder(builderClassName = "AuthenticationResponseBuilder")
@RequiredArgsConstructor
@JsonDeserialize(builder = AuthenticationResponse.AuthenticationResponseBuilder.class)
public class AuthenticationResponse {
    private final String accessToken;
    private final String refreshToken;
    private final long expiresOn;
    private final String tokenType;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AuthenticationResponseBuilder {
    }
}
