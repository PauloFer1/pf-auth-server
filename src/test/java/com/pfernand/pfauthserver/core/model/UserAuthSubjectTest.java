package com.pfernand.pfauthserver.core.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;

public class UserAuthSubjectTest {


    @Test
    public void fromStringWhenValidStringReturnsEnum() {
        // Given
        final String value = "cst";

        // When
        UserAuthSubject userAuthSubject = UserAuthSubject.fromString(value);

        // Then
        assertEquals(UserAuthSubject.CUSTOMER, userAuthSubject);
    }

    @Test
    public void fromStringWhenInvalidStringThrowsException() {
        // Given
        final String value = "cstInvalid";

        // When
        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> UserAuthSubject.fromString(value))
                .withMessage("No enum with name: " + value);
    }

}