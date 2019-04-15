package com.pfernand.pfauthserver.core.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder(builderClassName = "UserBuilder")
@RequiredArgsConstructor
@JsonDeserialize(builder = UserAuthDetails.UserBuilder.class)
public class UserAuthDetails {
    private final String email;
    private final String password;
    private final String role;
    private final UserAuthSubject subject;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserBuilder {
    }
}
