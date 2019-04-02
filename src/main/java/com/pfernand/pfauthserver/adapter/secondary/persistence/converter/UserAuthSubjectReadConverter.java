package com.pfernand.pfauthserver.adapter.secondary.persistence.converter;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import javax.inject.Named;

@Named
@ReadingConverter
public class UserAuthSubjectReadConverter implements Converter<String, UserAuthSubject> {

    @Override
    public UserAuthSubject convert(String source) {
        return UserAuthSubject.fromString(source);
    }
}
