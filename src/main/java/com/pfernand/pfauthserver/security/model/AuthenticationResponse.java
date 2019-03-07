package com.pfernand.pfauthserver.security.model;

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

    @JsonPOJOBuilder(withPrefix = "")
    public static class AuthenticationResponseBuilder {
    }
}
