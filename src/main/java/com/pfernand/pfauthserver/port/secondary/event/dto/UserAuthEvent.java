package com.pfernand.pfauthserver.port.secondary.event.dto;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class UserAuthEvent {
    private final String userUuid;
    private final String email;
    private final String role;
    private final UserAuthSubject subject;
    private final Instant createdAt;
    private final String authToken;
}
