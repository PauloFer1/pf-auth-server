package com.pfernand.pfauthserver.port.secondary.event;

import com.pfernand.pfauthserver.core.model.UserAuthDetails;

public interface UserAuthenticationPublisher {
    void publishEvent(UserAuthDetails event);
}
