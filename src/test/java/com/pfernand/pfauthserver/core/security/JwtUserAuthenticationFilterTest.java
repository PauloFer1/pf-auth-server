package com.pfernand.pfauthserver.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.security.model.AccessTokenSession;
import com.pfernand.pfauthserver.core.security.model.AuthenticationResponse;
import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;
import com.pfernand.pfauthserver.core.security.model.UserCredentials;
import com.pfernand.pfauthserver.core.security.model.UserSecurity;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenCommand;
import com.pfernand.security.JwtConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class JwtUserAuthenticationFilterTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String AUTH_NAME = "name";
    private static final UserSecurity AUTH_USER = new UserSecurity(USERNAME, PASSWORD, Collections.emptyList(), UserAuthSubject.CUSTOMER);

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TokenFactory tokenFactory;

    @Mock
    private RefreshTokenCommand refreshTokenCommand;

    @Mock
    private JwtConfig jwtConfig;

    private JwtUserAuthenticationFilter jwtUserAuthenticationFilter;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Authentication authentication;

    @Mock
    private ServletInputStream servletInputStream;

    @Before
    public void SetupClass() {
        Mockito.when(jwtConfig.getUri())
                .thenReturn("/auth");
        jwtUserAuthenticationFilter = new JwtUserAuthenticationFilter(
                authenticationManager,
                objectMapper,
                tokenFactory,
                jwtConfig,
                refreshTokenCommand
        );
    }

    @Test
    public void attemptAuthenticationWhenRequestInvalidThrowException() throws IOException {
        // Given
        // When
        Mockito.when(httpServletRequest.getInputStream())
                .thenReturn(servletInputStream);
        Mockito.when(objectMapper.readValue(servletInputStream, UserCredentials.class))
                .thenThrow(new IOException("error"));

        // Then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> jwtUserAuthenticationFilter.attemptAuthentication(httpServletRequest, httpServletResponse))
                .withMessageContaining("error");
    }

    @Test
    public void attemptAuthenticationWhenValidRequestReturnAuthentication() throws IOException {
        // Given
        final UserCredentials userCredentials = new UserCredentials(USERNAME, PASSWORD);
        final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD, Collections.emptyList());
        // When
        Mockito.when(httpServletRequest.getInputStream())
                .thenReturn(servletInputStream);
        Mockito.when(objectMapper.readValue(servletInputStream, UserCredentials.class))
                .thenReturn(userCredentials);
        Mockito.when(authenticationManager.authenticate(Mockito.eq(usernamePasswordAuthenticationToken)))
                .thenReturn(authentication);

        Authentication authenticationResult = jwtUserAuthenticationFilter.attemptAuthentication(httpServletRequest, httpServletResponse);

        // Then
        assertEquals(authentication, authenticationResult);
    }

    @Test
    public void successfulAuthenticationSaveValidSession() throws IOException {
        // Given
        Instant now = Instant.now();
        final AccessTokenSession accessTokenSession = AccessTokenSession.builder()
                .type("type")
                .notBefore(now)
                .expirationTime(now)
                .signedToken("signed")
                .build();
        final String refreshToken = "refresh";
        final AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .accessToken(accessTokenSession.getSignedToken())
                .refreshToken(refreshToken)
                .tokenType(accessTokenSession.getType())
                .expiresOn(accessTokenSession.getExpirationTime().toEpochMilli())
                .build();
        final PrintWriter out = new PrintWriter("string");
        final RefreshTokenSession refreshTokenSession = RefreshTokenSession.builder()
                .refreshToken(refreshToken)
                .expirationDate(accessTokenSession.getExpirationTime())
                .userUuid(AUTH_NAME)
                .build();

        // When
        Mockito.when(objectMapper.writeValueAsString(authenticationResponse))
                .thenReturn("authenticationString");
        Mockito.when(httpServletResponse.getWriter())
                .thenReturn(out);
        Mockito.when(authentication.getName())
                .thenReturn(AUTH_NAME);
        Mockito.when(authentication.getAuthorities())
                .thenReturn(Collections.emptyList());
        Mockito.when(authentication.getPrincipal())
                .thenReturn(AUTH_USER);
        Mockito.when(tokenFactory.createAccessToken(AUTH_NAME, Collections.emptyList(), AUTH_USER.getSubject().getSubject()))
                .thenReturn(accessTokenSession);
        Mockito.when(tokenFactory.createRefreshToken())
                .thenReturn(refreshToken);
        Mockito.when(refreshTokenCommand.saveSession(refreshTokenSession))
                .thenReturn(refreshTokenSession);

        jwtUserAuthenticationFilter.successfulAuthentication(httpServletRequest, httpServletResponse, filterChain, authentication);

        // Then
        Mockito.verify(refreshTokenCommand, Mockito.times(1)).saveSession(refreshTokenSession);
        Mockito.verify(objectMapper, Mockito.times(1)).writeValueAsString(authenticationResponse);

    }

}