package com.pfernand.pfauthserver.adapter.secondary.persistence.converter;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserAuthSubjectReadConverterTest {

    private final UserAuthSubjectReadConverter userAuthSubjectReadConverter = new UserAuthSubjectReadConverter();

    @Test
    public void convertReturnsEnum() {
        // Given
        final String source = UserAuthSubject.CUSTOMER.getSubject();

        // When
        UserAuthSubject userAuthSubject = userAuthSubjectReadConverter.convert(source);

        // Then
        assertEquals(UserAuthSubject.CUSTOMER, userAuthSubject);
    }

}