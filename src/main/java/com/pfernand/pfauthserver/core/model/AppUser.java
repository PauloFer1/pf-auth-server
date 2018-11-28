package com.pfernand.pfauthserver.core.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AppUser {

    private final Integer id;
    private final String username;
    private final String password;
    private final String role;

}
