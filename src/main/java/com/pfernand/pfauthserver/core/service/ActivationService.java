package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.RegTokenExpiredException;
import com.pfernand.pfauthserver.core.exceptions.RegTokenNotFoundException;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.RegistrationTokenQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class ActivationService {

    private final Clock clock;
    private final RegistrationTokenQuery registrationTokenQuery;
    private final AuthenticationCommand authenticationCommand;

    public UUID activateUser(final UUID userUuid, final String regToken) {
        log.debug("Activating user: {} with token: {}", userUuid, regToken);
        validateRegToken(userUuid, regToken);
        return authenticationCommand.activateUser(userUuid).getUserUuid();
    }

    private void validateRegToken(final UUID userUuid, final String regToken) {
        final RegistrationTokenEntity registrationTokenEntity =
                registrationTokenQuery.retrieveFromUserAndToken(userUuid, regToken)
//                        .filter(r -> !r.getExpirationDate().isBefore(Instant.now(clock)))
                        .orElseThrow(() -> new RegTokenNotFoundException(userUuid, regToken));
        if (registrationTokenEntity.getExpirationDate().isBefore(Instant.now(clock))) {
            throw new RegTokenExpiredException(registrationTokenEntity);
        }
    }
}