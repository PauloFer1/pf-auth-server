package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;

import java.util.Optional;

public interface AuthenticationQuery {

    Optional<UserAuthEntity> getUserFromEmail(final String email);
}
