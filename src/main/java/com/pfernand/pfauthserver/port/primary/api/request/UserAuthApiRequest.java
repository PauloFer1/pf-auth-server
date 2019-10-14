package com.pfernand.pfauthserver.port.primary.api.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder(builderClassName = "Builder")
@RequiredArgsConstructor
@JsonDeserialize(builder = UserAuthApiRequest.Builder.class)
public class UserAuthApiRequest {
    private final String email;
    private final String password;
    private final String role;
    private final UserAuthSubject subject;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
