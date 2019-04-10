package com.pfernand.pfauthserver.core.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class UserAuthProperties {
    private final String accessToken;
    private final String refreshToken;
    private final Date expiresOn;
    private final String tokenType;
}
