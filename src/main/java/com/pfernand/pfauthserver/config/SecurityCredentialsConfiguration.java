package com.pfernand.pfauthserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenCommand;
import com.pfernand.pfauthserver.core.security.TokenFactory;
import com.pfernand.pfauthserver.core.security.JwtUserAuthenticationFilter;
import com.pfernand.security.JwtConfig;
import com.pfernand.security.JwtTokenAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;


@EnableWebSecurity
public class SecurityCredentialsConfiguration extends WebSecurityConfigurerAdapter {

    private UserDetailsService userDetailsService;
    private JwtConfig jwtConfig;
    private ObjectMapper objectMapper;
    private TokenFactory tokenFactory;
    private RefreshTokenCommand refreshTokenCommand;

    @Inject
    public SecurityCredentialsConfiguration(final UserDetailsService userDetailsService,
                                            final JwtConfig jwtConfig,
                                            final ObjectMapper objectMapper,
                                            final TokenFactory tokenFactory,
                                            final RefreshTokenCommand refreshTokenCommand) {
        this.userDetailsService = userDetailsService;
        this.jwtConfig = jwtConfig;
        this.objectMapper = objectMapper;
        this.tokenFactory = tokenFactory;
        this.refreshTokenCommand = refreshTokenCommand;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
                .addFilterAfter(new JwtTokenAuthenticationFilter(jwtConfig), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new JwtUserAuthenticationFilter(authenticationManager(), objectMapper, tokenFactory, jwtConfig, refreshTokenCommand))
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, jwtConfig.getUri()).permitAll()
                .antMatchers(HttpMethod.POST, "/refresh-token").permitAll()
                .antMatchers("/metrics/**").permitAll()
                .antMatchers("/info").permitAll()
                .antMatchers("/health").permitAll()
                .anyRequest().authenticated();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    @Primary
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
