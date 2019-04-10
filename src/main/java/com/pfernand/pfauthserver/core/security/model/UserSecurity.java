package com.pfernand.pfauthserver.core.security.model;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserSecurity extends User {

    private static final long serialVersionUID = 2039922502174576769L;

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
