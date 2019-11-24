package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;

public interface RegistrationTokenCommand {
    RegistrationTokenEntity insert(final RegistrationTokenEntity registrationTokenEntity);
}
