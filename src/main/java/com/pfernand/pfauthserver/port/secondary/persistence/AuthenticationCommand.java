package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;

public interface AuthenticationCommand {
    UserAuthEntity insertUser(final UserAuthEntity userAuthEntity);
}
