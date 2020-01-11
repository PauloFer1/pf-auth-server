package com.pfernand.pfauthserver.core.service;

import com.pfernand.pfauthserver.core.exceptions.ExistentUserEmailException;
import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuth;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.security.TokenFactory;
import com.pfernand.pfauthserver.core.validation.UserAuthValidation;
import com.pfernand.pfauthserver.port.secondary.event.UserAuthenticationPublisher;
import com.pfernand.pfauthserver.port.secondary.event.dto.UserAuthEvent;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.RegistrationTokenCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.RegistrationTokenEntity;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.inject.Named;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationQuery authenticationQuery;
    private final AuthenticationCommand authenticationCommand;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserAuthenticationPublisher userAuthenticationPublisher;
    private final UserAuthValidation userAuthValidation;
    private final RegistrationTokenCommand registrationTokenCommand;
    private final TokenFactory tokenFactory;
    private final Clock clock;

    @Retryable(include = {IllegalStateException.class}, exceptionExpression = "#{message.contains('no can do')}")
    @Transactional("chainedTransactionManager")
    public UserAuth insertUser(final UserAuthDto userAuthDto) {
        log.info("++++++++++++++++++++++++++++++++++++");
        userAuthValidation.validate(userAuthDto);
        log.debug("Inserting: {}", userAuthDto.getEmail());
        validateEmailDoesntExist(userAuthDto.getEmail());
        UserAuthEntity userAuthEntity = mapToEntity(userAuthDto);
        final UserAuthEntity savedUserAuthDetails = authenticationCommand.insertUser(userAuthEntity);
        final RegistrationTokenEntity registrationTokenEntity = createRegistrationToken(savedUserAuthDetails.getUserUuid());
        if (userAuthDto.getEmail().contains("p"))
        {
            throw new IllegalStateException("no can do");
        }
        userAuthenticationPublisher.publishEvent(mapToEvent(savedUserAuthDetails, registrationTokenEntity.getRegToken()));
        return mapToModel(savedUserAuthDetails);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserAuth insertUserWithRetry(final UserAuthDto userAuthDto) {
//        RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
//                .handle(IllegalStateException.class)
//                .onRetry(r -> TransactionAspectSupport.currentTransactionStatus().setRollbackOnly())
//                .onRetry(r -> log.info("retrying: {}", r.getLastResult().toString()))
//                .onFailure(r -> TransactionAspectSupport.currentTransactionStatus().setRollbackOnly())
//                .withDelay(Duration.ofMillis(50))
//                .withMaxRetries(3);
//
//        return Failsafe
//                .with(retryPolicy)
//                .get(() -> insertUser(userAuthDto));
        int retries = 3;

        return retry(userAuthDto, retries);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserAuth retry(final UserAuthDto userAuthDto, final int retries) {
        try {
            return insertUser(userAuthDto);
        } catch (IllegalStateException ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            if (retries > 0) {
                return retry(userAuthDto, retries - 1);
            } else {
                throw ex;
            }
        }
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

    private RegistrationTokenEntity createRegistrationToken(final UUID userUuid) {
        final RegistrationTokenEntity registrationTokenEntity = RegistrationTokenEntity.builder()
                .regToken(tokenFactory.createRefreshToken())
                .expirationDate(Instant.now(clock).plus(24, ChronoUnit.HOURS))
                .userUuid(userUuid)
                .build();
        return registrationTokenCommand.insert(registrationTokenEntity);
    }

    private UserAuthEntity mapToEntity(final UserAuthDto userAuthDto) {
        return UserAuthEntity.builder()
                .userUuid(tokenFactory.generateUuid())
                .email(userAuthDto.getEmail())
                .password(bCryptPasswordEncoder.encode(userAuthDto.getPassword()))
                .role(userAuthDto.getRole())
                .subject(userAuthDto.getSubject())
                .createdAt(Instant.now(clock))
                .active(false)
                .build();
    }

    private UserAuthEvent mapToEvent(final UserAuthEntity userAuthEntity, final String regToken) {
        return UserAuthEvent.builder()
                .userUuid(userAuthEntity.getUserUuid().toString())
                .email(userAuthEntity.getEmail())
                .role(userAuthEntity.getRole())
                .subject(userAuthEntity.getSubject())
                .createdAt(userAuthEntity.getCreatedAt())
                .authToken(regToken)
                .build();
    }

    private UserAuth mapToModel(final UserAuthEntity userAuthEntity) {
        return UserAuth.builder()
                .userUuid(userAuthEntity.getUserUuid())
                .email(userAuthEntity.getEmail())
                .password(userAuthEntity.getPassword())
                .subject(userAuthEntity.getSubject())
                .role(userAuthEntity.getRole())
                .createdAt(userAuthEntity.getCreatedAt())
                .build();
    }
}
