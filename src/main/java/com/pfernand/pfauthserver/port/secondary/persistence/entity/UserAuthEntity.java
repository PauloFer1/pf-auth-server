package com.pfernand.pfauthserver.port.secondary.persistence.entity;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserAuthEntity {
    private final String email;
    private final String password;
    private final String role;
    private final UserAuthSubject subject;
    private final Instant createdAt;
}
