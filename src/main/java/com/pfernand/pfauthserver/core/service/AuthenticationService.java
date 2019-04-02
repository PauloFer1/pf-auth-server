package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
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
        log.info("Inserting: {}", userAuthDetails.getEmail());
        return authenticationCommand.insertUser(encriptUserPassword(userAuthDetails));
    }

    public UserAuthDetails retrieveUserFromEmail(final String email) {
        log.info("Retrieving info for: {}", email);
        return authenticationQuery.getUserFromEmail(email)
                .orElseThrow(() -> new UserDetailsNotFoundException(email));
    }

    private UserAuthDetails encriptUserPassword(final UserAuthDetails userAuthDetails) {
        return UserAuthDetails.builder()
                .email(userAuthDetails.getEmail())
                .password(bCryptPasswordEncoder.encode(userAuthDetails.getPassword()))
                .role(userAuthDetails.getRole())
                .subject(userAuthDetails.getSubject())
                .build();
    }
}
