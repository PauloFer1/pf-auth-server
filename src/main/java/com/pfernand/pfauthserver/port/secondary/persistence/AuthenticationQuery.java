package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.core.model.UserAuthDetails;

import java.util.Optional;

public interface AuthenticationQuery {

    Optional<UserAuthDetails> getUserFromEmail(final String email);
}
