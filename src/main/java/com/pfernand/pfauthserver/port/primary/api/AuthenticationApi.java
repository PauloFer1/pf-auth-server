package com.pfernand.pfauthserver.port.primary.api;

import com.pfernand.pfauthserver.port.primary.api.request.UserAuthApiRequest;

public interface AuthenticationApi<T> {

    T retrieveUserFromEmail(final String email);
    T insertUser(final UserAuthApiRequest userAuthApiRequest);
}
