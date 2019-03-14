package com.pfernand.pfauthserver.adapter.primary.api;

import com.pfernand.pfauthserver.core.service.AuthenticationService;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationApiControllerTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "admin";
    private static final UserAuthDetails USER_AUTH_DETAILS = UserAuthDetails.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .build();

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationApiController authenticationApiController;

    @Test
    public void retrieveUserFromEmailWithValidValues() {
        // Given
        // When
        Mockito.when(authenticationService.retrieveUserFromEmail(EMAIL))
                .thenReturn(USER_AUTH_DETAILS);
        ResponseEntity<UserAuthDetails> responseEntity = authenticationApiController.retrieveUserFromEmail(EMAIL);

        // Then
        assertEquals(ResponseEntity.ok(USER_AUTH_DETAILS), responseEntity);
    }

    @Test
    public void insertUserWithValidValues() {
        // Given
        final UserAuthDetails expectedUserAuthDetails = UserAuthDetails.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .role(ROLE)
                .id(UUID.randomUUID().toString())
                .build();
        // When
        Mockito.when(authenticationService.insertUser(USER_AUTH_DETAILS))
                .thenReturn(expectedUserAuthDetails);
        ResponseEntity<UserAuthDetails> reponseEntity = authenticationApiController.insertUser(USER_AUTH_DETAILS);

        // Then
        assertEquals(ResponseEntity.ok(expectedUserAuthDetails), reponseEntity);
    }
}