package com.pfernand.pfauthserver.port.primary.api.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ActivationApiResponse {
    private final UUID userUuid;
}
