package com.pfernand.pfauthserver.adapter.secondary.persistence.converter;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserAuthSubjectWriterConverterTest {

    private final UserAuthSubjectWriterConverter userAuthSubjectWriterConverter = new UserAuthSubjectWriterConverter();

    @Test
    public void convertReturnsString() {
        // Given
        UserAuthSubject userAuthSubject = UserAuthSubject.CUSTOMER;

        // When
        String convertedString = userAuthSubjectWriterConverter.convert(userAuthSubject);

        // Then
        assertEquals(UserAuthSubject.CUSTOMER.getSubject(), convertedString);
    }
}