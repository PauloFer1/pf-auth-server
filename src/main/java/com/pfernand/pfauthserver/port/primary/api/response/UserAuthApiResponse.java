package com.pfernand.pfauthserver.port.primary.api.response;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserAuthApiResponse {
    private final String email;
    private final String role;
    private final UserAuthSubject subject;
    private final Instant createdAt;
}
