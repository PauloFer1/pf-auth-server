package com.pfernand.pfauthserver.port.primary.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder(builderClassName = "Builder")
@RequiredArgsConstructor
@JsonDeserialize(builder = UserAuthApiDto.Builder.class)
public class UserAuthApiDto {
    private final String email;
    private final String password;
    private final String role;
    private final UserAuthSubject subject;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
