package com.pfernand.pfauthserver.security;

import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.port.secondary.AuthenticationQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceLookUp implements UserDetailsService {

    private final AuthenticationQuery authenticationQuery;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Authenticating {}", username);

        final UserAuthDetails userAuthDetails = authenticationQuery.getUserFromEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));

        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_" + userAuthDetails.getRole());

        return new User(userAuthDetails.getEmail(), userAuthDetails.getPassword(), grantedAuthorities);
    }
}
