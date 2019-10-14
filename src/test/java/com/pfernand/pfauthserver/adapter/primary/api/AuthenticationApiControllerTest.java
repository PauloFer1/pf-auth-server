package com.pfernand.pfauthserver.adapter.primary.api;

import com.pfernand.pfauthserver.core.model.UserAuth;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import com.pfernand.pfauthserver.port.primary.api.request.UserAuthApiRequest;
import com.pfernand.pfauthserver.port.primary.api.response.UserAuthApiResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationApiControllerTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "admin";
    private static final Instant NOW = Instant.now();
    private static final UserAuthDto USER_AUTH_DETAILS = UserAuthDto.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .build();
    private static final UserAuthApiResponse USER_AUTH_API_RESPONSE = UserAuthApiResponse.builder()
            .email(EMAIL)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .createdAt(NOW)
            .build();
    private static final UserAuth USER_AUTH = UserAuth.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
            .createdAt(NOW)
            .build();
    private static final UserAuthApiRequest USER_AUTH_API_DTO = UserAuthApiRequest.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .role(ROLE)
            .subject(UserAuthSubject.CUSTOMER)
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
                .thenReturn(USER_AUTH);
        ResponseEntity<UserAuthApiResponse> responseEntity = authenticationApiController.retrieveUserFromEmail(EMAIL);

        // Then
        assertEquals(ResponseEntity.ok(USER_AUTH_API_RESPONSE), responseEntity);
    }

    @Test
    public void insertUserWithValidValues() {
        // Given
        final UserAuthApiResponse expectedUserAuthApiResponse = UserAuthApiResponse.builder()
                .email(EMAIL)
                .role(ROLE)
                .subject(UserAuthSubject.CUSTOMER)
                .createdAt(NOW)
                .build();
        // When
        Mockito.when(authenticationService.insertUser(USER_AUTH_DETAILS))
                .thenReturn(USER_AUTH);
        ResponseEntity<UserAuthApiResponse> reponseEntity = authenticationApiController.insertUser(USER_AUTH_API_DTO);

        // Then
        assertEquals(ResponseEntity.ok(expectedUserAuthApiResponse), reponseEntity);
    }
}