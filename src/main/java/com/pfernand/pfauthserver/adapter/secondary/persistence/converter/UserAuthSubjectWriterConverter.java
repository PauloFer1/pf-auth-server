package com.pfernand.pfauthserver.adapter.secondary.persistence.converter;

import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import javax.inject.Named;

@Named
@WritingConverter
public class UserAuthSubjectWriterConverter implements Converter<UserAuthSubject, String> {
    @Override
    public String convert(UserAuthSubject source) {
        return source.getSubject();
    }
}
