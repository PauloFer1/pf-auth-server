package com.pfernand.pfauthserver.core.security;

import com.pfernand.pfauthserver.core.security.model.UserSecurity;
import com.pfernand.pfauthserver.port.secondary.persistence.AuthenticationQuery;
import com.pfernand.pfauthserver.port.secondary.persistence.entity.UserAuthEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
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

        final UserAuthEntity userAuthEntity = authenticationQuery.getUserFromEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));

        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_" + userAuthEntity.getRole());

        return new UserSecurity(userAuthEntity.getEmail(), userAuthEntity.getPassword(), grantedAuthorities, userAuthEntity.getSubject());
    }
}
