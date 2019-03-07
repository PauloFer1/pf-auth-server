package com.pfernand.pfauthserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.inject.Named;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Named
@RequiredArgsConstructor
public class TokenFactory {

    private final JwtConfig jwtConfig;

    public String createAccessToken(final Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        claims.put("authorities", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return createToken(authentication, jwtConfig.getExpiration(), claims);
    }

    public String createRefreshToken(final Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        claims.put("scopes", Collections.singleton("REFRESH_TOKEN"));
        return createToken(authentication, jwtConfig.getRefreshExpiration(), claims);
    }

    private String createToken(final Authentication authentication, final int expiration, final Claims claims) {
        final Long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(authentication.getName())
                //.setIssuer("auth-server")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret().getBytes())
                .compact();
    }
}
