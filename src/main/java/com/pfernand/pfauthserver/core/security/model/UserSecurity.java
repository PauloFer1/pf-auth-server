package com.pfernand.pfauthserver.core.security.model;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class UserSecurity extends User {

    private final UserAuthSubject subject;

    public UserSecurity(String username, String password,
                        Collection<? extends GrantedAuthority> authorities, UserAuthSubject subject) {
        super(username, password, authorities);
        this.subject = subject;
    }

    public UserAuthSubject getSubject() {
        return subject;
    }
}
