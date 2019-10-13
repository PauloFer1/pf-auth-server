package com.pfernand.pfauthserver.port.primary.api;

import com.pfernand.pfauthserver.port.primary.api.dto.UserAuthApiDto;

public interface AuthenticationApi<T> {

    T retrieveUserFromEmail(final String email);
    T insertUser(final UserAuthApiDto userAuthApiDto);
}
