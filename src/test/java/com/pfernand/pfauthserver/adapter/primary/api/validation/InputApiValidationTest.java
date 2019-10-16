package com.pfernand.pfauthserver.adapter.primary.api.validation;

import com.pfernand.pfauthserver.core.model.UserAuthRole;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.port.primary.api.request.UserAuthApiRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class InputApiValidationTest {

    private InputApiValidation inputApiValidation = new InputApiValidation();

    @Test
    public void validateUserAuthDoesntThrowException() {
        // Given
        final UserAuthApiRequest userAuthApiRequest = UserAuthApiRequest.builder()
                .email("test@mail.com")
                .password("pass")
                .role(UserAuthRole.ADMIN)
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        // When
        inputApiValidation.validateUserAuth(userAuthApiRequest);
        // Then
    }

    @Test
    public void validateUserAuthMaxLengthDoesntThrowException() {
        // Given
        final StringBuilder email = new StringBuilder();
        for (int i = 0; i < 256; ++i) {
            email.append('a');
        }
        final UserAuthApiRequest userAuthApiRequest = UserAuthApiRequest.builder()
                .email(email.toString())
                .password("pass")
                .role(UserAuthRole.ADMIN)
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        // When
        inputApiValidation.validateUserAuth(userAuthApiRequest);
        // Then
    }

    @Test
    public void validateUserAuthWhenInvalidEmailLengthThenThrowException() {
        // Given
        final StringBuilder email = new StringBuilder();
        for (int i = 0; i < 257; ++i) {
            email.append('a');
        }
        final UserAuthApiRequest userAuthApiRequest = UserAuthApiRequest.builder()
                .email(email.toString())
                .password("pass")
                .role(UserAuthRole.ADMIN)
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        // When
        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inputApiValidation.validateUserAuth(userAuthApiRequest))
                .withMessage("Input exceeds max length");
    }

    @Test
    public void validateUserAuthWhenInvalidPasswordLengthThenThrowException() {
        // Given
        final StringBuilder pass = new StringBuilder();
        for (int i = 0; i < 257; ++i) {
            pass.append('a');
        }
        final UserAuthApiRequest userAuthApiRequest = UserAuthApiRequest.builder()
                .email("email@mail.com")
                .password(pass.toString())
                .role(UserAuthRole.ADMIN)
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        // When
        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inputApiValidation.validateUserAuth(userAuthApiRequest))
                .withMessage("Input exceeds max length");
    }

    @Test
    public void encodeForLogWhenNullInputThenReturnNull() {
        // Given
        // When
        String result = inputApiValidation.encodeForLog(null);

        // Then
        assertNull(result);
    }

    @Test
    public void encodeForLogReturnsEncoded() {
        // Given
        final String input = "a\nb\rc\td%0De%0Af%0dg%0ah";
        final String expected = "a_b_c_d_e_f_g_h";
        // When
        String result = inputApiValidation.encodeForLog(input);

        // Then
        assertEquals(expected, result);
    }
}