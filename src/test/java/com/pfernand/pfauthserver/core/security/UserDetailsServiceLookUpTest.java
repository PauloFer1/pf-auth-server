package com.pfernand.pfauthserver.core.security;

import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.security.model.UserSecurity;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsServiceLookUpTest {

    private static final String USERNAME = "paulo";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "ADMIN";
    private static final List<GrantedAuthority> AUTHORITIES = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + ROLE));
    private static final UserAuthSubject SUBJECT = UserAuthSubject.CUSTOMER;

    @Mock
    private AuthenticationQuery authenticationQuery;

    @InjectMocks
    private UserDetailsServiceLookUp userDetailsServiceLookUp;

    @Test
    public void loadUserByUsernameWhenUsernameNotFoundThrowException() {
        // Given
        // When
        Mockito.when(authenticationQuery.getUserFromEmail(USERNAME))
                .thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(UsernameNotFoundException.class)
                .isThrownBy(() -> userDetailsServiceLookUp.loadUserByUsername(USERNAME))
                .withMessageContaining("Username: " + USERNAME + " not found");
    }

    @Test
    public void loadUserByUsernameWhenValidUsernameReturnsUserSecurity() {
        // Given
        UserAuthDetails userAuthDetails = UserAuthDetails.builder()
                .email(USERNAME)
                .password(PASSWORD)
                .subject(SUBJECT)
                .role(ROLE)
                .build();
        UserSecurity expected = new UserSecurity(USERNAME, PASSWORD, AUTHORITIES, SUBJECT);

        // When
        Mockito.when(authenticationQuery.getUserFromEmail(USERNAME))
                .thenReturn(Optional.of(userAuthDetails));
        UserDetails userSecurity = userDetailsServiceLookUp.loadUserByUsername(USERNAME);

        // Then
        assertEquals(expected, userSecurity);
    }

}