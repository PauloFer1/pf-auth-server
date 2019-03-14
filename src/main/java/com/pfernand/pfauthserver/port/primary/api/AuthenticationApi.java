package com.pfernand.pfauthserver.port.primary.api;

import com.pfernand.pfauthserver.core.model.UserAuthDetails;

public interface AuthenticationApi<T> {

    T retrieveUserFromEmail(final String email);
    T insertUser(final UserAuthDetails userAuthDetails);
}
