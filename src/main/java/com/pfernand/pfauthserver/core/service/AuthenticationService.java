package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.ExistentUserEmailException;
import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuth;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.validation.UserAuthValidation;
import com.pfernand.pfauthserver.port.secondary.event.UserAuthenticationPublisher;
import com.pfernand.pfauthserver.port.secondary.event.dto.UserAuthEvent;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Named;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Named
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationQuery authenticationQuery;
    private final AuthenticationCommand authenticationCommand;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserAuthenticationPublisher userAuthenticationPublisher;
    private final UserAuthValidation userAuthValidation;
    private final Clock clock;

    @Transactional
    public UserAuth insertUser(final UserAuthDto userAuthDto) {
        userAuthValidation.validate(userAuthDto);
        log.debug("Inserting: {}", userAuthDto.getEmail());
        validateEmailDoesntExist(userAuthDto.getEmail());
        UserAuthEntity userAuthEntity = mapToEntity(userAuthDto);
        final UserAuthEntity savedUserAuthDetails = authenticationCommand.insertUser(userAuthEntity);
        userAuthenticationPublisher.publishEvent(mapToEvent(savedUserAuthDetails));
        return mapToModel(userAuthEntity);
    }

    public UserAuth retrieveUserFromEmail(final String email) {
        log.debug("Retrieving info for: {}", email);
        final UserAuthEntity userAuthEntity = authenticationQuery.getUserFromEmail(email)
                .orElseThrow(() -> new UserDetailsNotFoundException(email));
        return mapToModel(userAuthEntity);
    }

    private void validateEmailDoesntExist(final String email) {
        final Optional<UserAuthEntity> userAuthEntity = authenticationQuery.getUserFromEmail(email);
        if (userAuthEntity.isPresent()) {
            throw new ExistentUserEmailException(email);
        }
    }

    private UserAuthEntity mapToEntity(final UserAuthDto userAuthDto) {
        return UserAuthEntity.builder()
                .email(userAuthDto.getEmail())
                .password(bCryptPasswordEncoder.encode(userAuthDto.getPassword()))
                .role(userAuthDto.getRole())
                .subject(userAuthDto.getSubject())
                .createdAt(Instant.now(clock))
                .build();
    }

    private UserAuthEvent mapToEvent(final UserAuthEntity userAuthEntity) {
        return UserAuthEvent.builder()
                .email(userAuthEntity.getEmail())
                .role(userAuthEntity.getRole())
                .subject(userAuthEntity.getSubject())
                .createdAt(userAuthEntity.getCreatedAt())
                .build();
    }

    private UserAuth mapToModel(final UserAuthEntity userAuthEntity) {
        return UserAuth.builder()
                .email(userAuthEntity.getEmail())
                .password(userAuthEntity.getPassword())
                .subject(userAuthEntity.getSubject())
                .role(userAuthEntity.getRole())
                .createdAt(userAuthEntity.getCreatedAt())
                .build();
    }
}
