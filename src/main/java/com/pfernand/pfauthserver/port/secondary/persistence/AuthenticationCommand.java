package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;

import java.util.UUID;

public interface AuthenticationCommand {
    UserAuthEntity insertUser(final UserAuthEntity userAuthEntity);
    UserAuthEntity activateUser(final UUID userUuid);
}
