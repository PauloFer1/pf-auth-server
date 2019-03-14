package com.pfernand.pfauthserver.core.security;

import com.pfernand.pfauthserver.core.security.model.AccessTokenSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
    private static final String AUDIENCE = "urn:pfernand:ww";
    private static final String SUBJECT_TYPE = "cst";
    private static final String AUTHORITIES_HEADER = "authorities";
    private static final String ISSUER = "auth-server";

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
        final AccessTokenSession accessTokenSession = tokenFactory.createAccessToken(subject, roles);

        // Then
        assertThat(accessTokenSession.getExpirationTime())
                .isBetween(Instant.now().plusSeconds(JWT_EXPIRATION * 1000).minusSeconds(3), Instant.now().plusSeconds(JWT_EXPIRATION * 1000));
        assertThat(accessTokenSession.getNotBefore())
                .isBetween(Instant.now().minusSeconds(4), Instant.now());
        assertThat(accessTokenSession.getType()).isEqualTo(JWT_PREFIX);
        assertThat(Jwts.parser().isSigned(accessTokenSession.getSignedToken())).isTrue();
        log.info(Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(JWT_SECRET))
                .parse(accessTokenSession.getSignedToken()).getBody().toString());
    }
}