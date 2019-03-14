package com.pfernand.pfauthserver.core.security.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder(builderClassName = "RefreshTokenSessionBuilder")
@JsonDeserialize(builder = RefreshTokenSession.RefreshTokenSessionBuilder.class)
public class RefreshTokenSession {
    private final String refreshToken;
    private final String userUuid;
    private final Instant expirationDate;

    @JsonPOJOBuilder(withPrefix = "")
    public static class RefreshTokenSessionBuilder {

    }
}
