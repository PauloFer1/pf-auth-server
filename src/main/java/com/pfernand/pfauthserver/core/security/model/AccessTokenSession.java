package com.pfernand.pfauthserver.core.security.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AccessTokenSession {
    private final String signedToken;
    private final Instant notBefore;
    private final Instant expirationTime;
    private final String type;
}
