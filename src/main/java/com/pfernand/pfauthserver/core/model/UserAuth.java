package com.pfernand.pfauthserver.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserAuth {
    private final String email;
    private final String password;
    private final String role;
    private final UserAuthSubject subject;
    private final Instant createdAt;
}
