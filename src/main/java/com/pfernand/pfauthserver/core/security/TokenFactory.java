package com.pfernand.pfauthserver.core.security;

import com.pfernand.pfauthserver.core.security.model.AccessTokenSession;
import com.pfernand.security.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Named
@RequiredArgsConstructor
public class TokenFactory {

    private static final String AUDIENCE = "urn:pfernand:ww";
    private static final String SUBJECT_TYPE = "cst";
    private static final String AUTHORITIES_HEADER = "authorities";
    private static final String ISSUER = "auth-server";

    private final JwtConfig jwtConfig;

    public AccessTokenSession createAccessToken(final String subject, final List<String> roles) {
        final Instant now = Instant.now();
        final Instant expirationTime = now.plusSeconds(jwtConfig.getExpiration());
        final Instant notBefore = now.minusSeconds(1);

        final String signedToken = Jwts.builder()
                .setClaims(buildClaims(subject, roles))
                .setId(UUID.randomUUID().toString())
                .setSubject(AUDIENCE + ":" + SUBJECT_TYPE + ":" + subject)
                .setAudience(AUDIENCE)
                .setIssuer(AUDIENCE + ":" + ISSUER)
                .setNotBefore(dateFromInstant(notBefore))
                .setIssuedAt(dateFromInstant(now))
                .setExpiration(dateFromInstant(expirationTime))
                .signWith(SignatureAlgorithm.HS512, DatatypeConverter.parseBase64Binary(jwtConfig.getSecret()))
                .setHeaderParam("typ", jwtConfig.getPrefix())
                .compact();

        return AccessTokenSession.builder()
                .expirationTime(expirationTime)
                .notBefore(notBefore)
                .signedToken(signedToken)
                .type(jwtConfig.getPrefix())
                .build();
    }

    public String createRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private Date dateFromInstant(final Instant instant) {
        return new Date(instant.toEpochMilli());
    }

    private Claims buildClaims(final String subject, final List<String> roles) {
        final Claims claims = Jwts.claims().setSubject(subject);
        claims.put(AUTHORITIES_HEADER, roles);
        return claims;
    }
}
