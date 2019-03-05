package com.pfernand.pfauthserver.core;

import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.port.secondary.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.AuthenticationQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.inject.Named;

@Slf4j
@Named
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationQuery authenticationQuery;
    private final AuthenticationCommand authenticationCommand;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserAuthDetails insertUser(final UserAuthDetails userAuthDetails) {
        final UserAuthDetails encriptedUserAuthDetails = UserAuthDetails.builder()
                .email(userAuthDetails.getEmail())
                .password(bCryptPasswordEncoder.encode(userAuthDetails.getPassword()))
                .role(userAuthDetails.getRole())
                .build();
        log.info("Inserting: {}", encriptedUserAuthDetails);
        return authenticationCommand.insertUser(encriptedUserAuthDetails);
    }

    public UserAuthDetails retrieveUserFromEmail(final String email) {
        log.info("Retrieving info for: {}", email);
        return authenticationQuery.getUserFromEmail(email)
                .orElseThrow(() -> new UserDetailsNotFoundException(email));
    }
}
