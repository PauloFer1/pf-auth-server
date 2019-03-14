package com.pfernand.pfauthserver.port.secondary.persistence;

import com.pfernand.pfauthserver.core.model.UserAuthDetails;

public interface AuthenticationCommand {
    UserAuthDetails insertUser(final UserAuthDetails userAuthDetails);
}
