package com.pfernand.pfauthserver.port.primary.api;

import java.util.UUID;

public interface ActivationApi<T> {
    T activateUser(final UUID userUuid, final String regToken);
}
