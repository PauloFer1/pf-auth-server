package com.pfernand.pfauthserver.port.secondary;

import com.pfernand.pfauthserver.core.model.UserAuthDetails;

public interface AuthenticationCommand {
    UserAuthDetails insertUser(final UserAuthDetails userAuthDetails);
}
