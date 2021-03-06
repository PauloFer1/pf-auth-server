package com.pfernand.pfauthserver.core.security;

import com.pfernand.pfauthserver.core.security.model.AccessTokenSession;
import com.pfernand.security.JwtConfig;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TokenFactoryTest {

    private static final String JWT_PREFIX = "Bearer";
    private static final String JWT_SECRET = "JwtSecret";
    private static final int JWT_EXPIRATION = 3600;
    private static final String SUBJECT_TYPE = "cst";

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private TokenFactory tokenFactory;

    @Test
    public void createAccessToken() {
        // Given
        final String subject = "user@email.com";
        final List<String> roles = Collections.singletonList("ROLE_ADMIN");

        // When
        Mockito.when(jwtConfig.getPrefix()).thenReturn(JWT_PREFIX);
        Mockito.when(jwtConfig.getSecret()).thenReturn(JWT_SECRET);
        Mockito.when(jwtConfig.getExpiration()).thenReturn(JWT_EXPIRATION);
        final AccessTokenSession accessTokenSession = tokenFactory.createAccessToken(subject, roles, SUBJECT_TYPE);

        // Then
        assertThat(accessTokenSession.getExpirationTime())
                .isBetween(Instant.now().plusSeconds(JWT_EXPIRATION
                ).minusSeconds(3), Instant.now().plusSeconds(JWT_EXPIRATION * 1000));
        assertThat(accessTokenSession.getNotBefore())
                .isBetween(Instant.now().minusSeconds(4), Instant.now());
        assertThat(accessTokenSession.getType()).isEqualTo(JWT_PREFIX);
        assertThat(Jwts.parser().isSigned(accessTokenSession.getSignedToken())).isTrue();
        assertThat(Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(JWT_SECRET))
                .parse(accessTokenSession.getSignedToken()).getBody().toString())
                .contains("sub=urn:pfernand:ww:" + SUBJECT_TYPE + ":" + subject);
    }

    @Test
    public void createRefreshTokenReturnString() {
        // Given
        // When
        final String refreshToken = tokenFactory.createRefreshToken();

        // Then
        assertThat(refreshToken.length()).isEqualTo(32);
    }
}