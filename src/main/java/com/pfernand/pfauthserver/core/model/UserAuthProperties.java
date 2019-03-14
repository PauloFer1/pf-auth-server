package com.pfernand.pfauthserver.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserAuthProperties {
    private final String accessToken;
    private final String refreshToken;
    private final Date expiresOn;
    private final String tokenType;
}
