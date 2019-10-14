package com.pfernand.pfauthserver.port.secondary.event;

import com.pfernand.pfauthserver.port.secondary.event.dto.UserAuthEvent;

public interface UserAuthenticationPublisher {
    void publishEvent(UserAuthEvent event);
}
