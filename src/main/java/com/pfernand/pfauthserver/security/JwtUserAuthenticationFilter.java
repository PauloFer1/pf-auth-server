package com.pfernand.pfauthserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfernand.pfauthserver.security.model.AuthenticationResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

public class JwtUserAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authManager;
    private final ObjectMapper objectMapper;
    private final TokenFactory tokenFactory;

    public JwtUserAuthenticationFilter(final AuthenticationManager authManager,
                                       final ObjectMapper objectMapper,
                                       final TokenFactory tokenFactory,
                                       final JwtConfig jwtConfig) {
        this.authManager = authManager;
        this.objectMapper = objectMapper;
        this.tokenFactory = tokenFactory;
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(jwtConfig.getUri(), "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        return authManager.authenticate(buildAuthenticationToken(request));
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication auth) throws IOException {
        final String token = tokenFactory.createAccessToken(auth);
        buildResponseBody(response, token);
    }

    private void buildResponseBody(final HttpServletResponse response, final String token) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        final PrintWriter out = response.getWriter();
        final AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().accessToken(token).build();
        out.print(objectMapper.writeValueAsString(authenticationResponse));
        out.flush();
    }

    private UsernamePasswordAuthenticationToken buildAuthenticationToken(final HttpServletRequest request) {
        // Todo - Why try catch?????
        UserCredentials creds;
        try {
            creds = objectMapper.readValue(request.getInputStream(), UserCredentials.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword(), Collections.emptyList());
    }
}
