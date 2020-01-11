package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;

import java.util.Optional;
import java.util.UUID;

public interface RegistrationTokenQuery {
    Optional<RegistrationTokenEntity> retrieveFromUserAndToken(final UUID userUuid, final String regToken);
}