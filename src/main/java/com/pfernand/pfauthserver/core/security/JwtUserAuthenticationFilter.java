package com.pfernand.pfauthserver.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfernand.pfauthserver.core.security.model.AccessTokenSession;
import com.pfernand.pfauthserver.core.security.model.UserSecurity;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenCommand;
import com.pfernand.pfauthserver.core.security.model.AuthenticationResponse;
import com.pfernand.pfauthserver.core.security.model.RefreshTokenSession;
import com.pfernand.pfauthserver.core.security.model.UserCredentials;
import com.pfernand.security.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
public class JwtUserAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authManager;
    private final ObjectMapper objectMapper;
    private final TokenFactory tokenFactory;
    private final RefreshTokenCommand refreshTokenCommand;

    public JwtUserAuthenticationFilter(final AuthenticationManager authManager,
                                       final ObjectMapper objectMapper,
                                       final TokenFactory tokenFactory,
                                       final JwtConfig jwtConfig,
                                       final RefreshTokenCommand refreshTokenCommand) {
        this.authManager = authManager;
        this.objectMapper = objectMapper;
        this.tokenFactory = tokenFactory;
        this.refreshTokenCommand = refreshTokenCommand;
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(jwtConfig.getUri(), "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        return authManager.authenticate(buildAuthentication(request));
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication auth) throws IOException {
        final AccessTokenSession accessTokenSession = tokenFactory.createAccessToken(
                auth.getName(),
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
                ((UserSecurity) auth.getPrincipal()).getSubject().getSubject() );
        final String refreshToken = tokenFactory.createRefreshToken();

        final AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .accessToken(accessTokenSession.getSignedToken())
                .refreshToken(refreshToken)
                .tokenType(accessTokenSession.getType())
                .expiresOn(accessTokenSession.getExpirationTime().toEpochMilli())
                .build();

        updateResponseWithAuthentication(response, authenticationResponse);

        refreshTokenCommand.saveSession(RefreshTokenSession.builder()
                .refreshToken(refreshToken)
                .expirationDate(accessTokenSession.getExpirationTime())
                .userUuid(auth.getName())
                .build());
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(final HttpServletRequest request) {
        UserCredentials userCredentials;
        try {
            userCredentials = objectMapper.readValue(request.getInputStream(), UserCredentials.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new UsernamePasswordAuthenticationToken(userCredentials.getUsername(), userCredentials.getPassword(), Collections.emptyList());
    }

    private void updateResponseWithAuthentication(final HttpServletResponse response, final AuthenticationResponse authenticationResponse)
            throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        final PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(authenticationResponse));
        out.flush();
    }
}
