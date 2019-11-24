package com.pfernand.pfauthserver.port.secondary.persistence.entity;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RegistrationTokenEntity {
    private final String regToken;
    private final UUID userUuid;
    private final Instant expirationDate;
}
