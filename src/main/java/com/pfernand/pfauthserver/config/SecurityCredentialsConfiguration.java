package com.pfernand.pfauthserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfernand.pfauthserver.port.secondary.persistence.RefreshTokenCommand;
import com.pfernand.pfauthserver.core.security.TokenFactory;
import com.pfernand.pfauthserver.core.security.JwtUserAuthenticationFilter;
import com.pfernand.security.JwtConfig;
import com.pfernand.security.JwtTokenAuthenticationFilter;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
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
//
//    @Bean
//    public ServletWebServerFactory servletContainer() {
//        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
////        {
////            @Override
////            protected void postProcessContext(Context context) {
////                SecurityConstraint securityConstraint = new SecurityConstraint();
////                securityConstraint.setUserConstraint("CONFIDENTIAL");
////                SecurityCollection collection = new SecurityCollection();
////                collection.addPattern("/*");
////                securityConstraint.addCollection(collection);
////                context.addConstraint(securityConstraint);
////            }
////        };
//        tomcat.addAdditionalTomcatConnectors(redirectConnector());
//        return tomcat;
//    }
//
//    private Connector redirectConnector() {
//        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
//        connector.setScheme("http");
//        connector.setPort(8080);
//        connector.setSecure(false);
//        connector.setRedirectPort(8443);
//        return connector;
//    }
}
