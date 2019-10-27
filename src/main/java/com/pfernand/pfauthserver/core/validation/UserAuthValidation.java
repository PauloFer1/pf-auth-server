package com.pfernand.pfauthserver.core.validation;

import com.pfernand.pfauthserver.core.exceptions.InvalidEmailException;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import org.apache.commons.validator.routines.EmailValidator;

import javax.inject.Named;

@Named
public class UserAuthValidation {

    public void validate(final UserAuthDto userAuthDto) {
        validateEmail(userAuthDto.getEmail());
    }

    private void validateEmail(final String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new InvalidEmailException(email);
        }
    }
}
