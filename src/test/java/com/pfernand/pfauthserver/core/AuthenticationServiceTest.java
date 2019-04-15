package com.pfernand.pfauthserver.core;

import com.pfernand.pfauthserver.core.exceptions.UserDetailsNotFoundException;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationCommand;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "admin";
    private static final UserAuthDetails USER_AUTH_DETAILS = UserAuthDetails.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .build();

    @Mock
    private AuthenticationQuery authenticationQuery;

    @Mock
    private AuthenticationCommand authenticationCommand;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void insertUserWhenValidValuesThenReturnUser() {
        // Given
        final String encodedPassword = "encoded";
        final UserAuthDetails userAuthDetailsAfterEncoded = UserAuthDetails.builder()
                .role(USER_AUTH_DETAILS.getRole())
                .password(encodedPassword)
                .email(USER_AUTH_DETAILS.getEmail())
                .subject(UserAuthSubject.CUSTOMER)
                .build();
        final UserAuthDetails expectedUserAuthDetails = UserAuthDetails.builder()
                .role(USER_AUTH_DETAILS.getRole())
                .password(USER_AUTH_DETAILS.getPassword())
                .email(USER_AUTH_DETAILS.getEmail())
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        // When
        Mockito.when(bCryptPasswordEncoder.encode(USER_AUTH_DETAILS.getPassword()))
                .thenReturn(encodedPassword);
        Mockito.when(authenticationCommand.insertUser(userAuthDetailsAfterEncoded))
                .thenReturn(expectedUserAuthDetails);
        UserAuthDetails userAuthDetails = authenticationService.insertUser(USER_AUTH_DETAILS);

        // Then
        assertEquals(expectedUserAuthDetails, userAuthDetails);
    }

    @Test
    public void retrieveUserFromEmailWhenEmailNotFoundThenThrowException() {
        // Given
        // When
        Mockito.when(authenticationQuery.getUserFromEmail(EMAIL))
                .thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(UserDetailsNotFoundException.class)
                .isThrownBy(() -> authenticationService.retrieveUserFromEmail(EMAIL))
                .withMessageContaining(String.format("UserAuthDetails details not found for: %s", EMAIL));

    }

    @Test
    public void retrieveUserFromEmailWhenValidEmailThenReturnUser() {
        // Given
        // When
        Mockito.when(authenticationQuery.getUserFromEmail(EMAIL))
                .thenReturn(Optional.of(USER_AUTH_DETAILS));
        UserAuthDetails userAuthDetails = authenticationService.retrieveUserFromEmail(EMAIL);

        // Then
        assertEquals(USER_AUTH_DETAILS, userAuthDetails);
    }

}