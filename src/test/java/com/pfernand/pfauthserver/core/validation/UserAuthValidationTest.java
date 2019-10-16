package com.pfernand.pfauthserver.core.validation;

import com.pfernand.pfauthserver.core.exceptions.InvalidEmailException;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UserAuthValidationTest {

    private UserAuthValidation userAuthValidation = new UserAuthValidation();

    @Test
    public void validateDoesNotThrowException() {
        // Given
        final UserAuthDto userAuthDto = UserAuthDto.builder()
                .email("paulo@mail.com")
                .password("pass")
                .role("cst")
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        // When
        userAuthValidation.validate(userAuthDto);

        // Then
    }

    @Test
    public void validateWhenInvalidEmailThenThrowException() {
        // Given
        final UserAuthDto userAuthDto = UserAuthDto.builder()
                .email("paulomail.com")
                .password("pass")
                .role("cst")
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        // When
        // Then
        assertThatExceptionOfType(InvalidEmailException.class)
                .isThrownBy(() -> userAuthValidation.validate(userAuthDto))
                .withMessage(String.format("Invalid email: %s", userAuthDto.getEmail()));
    }

}