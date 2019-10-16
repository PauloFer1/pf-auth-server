package com.pfernand.pfauthserver.adapter.primary.api.validation;

import com.pfernand.pfauthserver.port.primary.api.request.UserAuthApiRequest;
import org.owasp.esapi.ESAPI;

import javax.inject.Named;

@Named
public class InputApiValidation {

    private static final int MAX_PARAMETER_LENGTH = 256;

    public void validateUserAuth(final UserAuthApiRequest userAuthApiRequest) {
        validateLength(userAuthApiRequest.getEmail());
        validateLength(userAuthApiRequest.getPassword());
    }

    public void validateLength(final String value) {
        if (value.length() > MAX_PARAMETER_LENGTH) {
            throw new IllegalArgumentException("Input exceeds max length");
        }
    }

    public String encodeForLog(final String value) {
        return value == null ? value
            : ESAPI.encoder().encodeForHTML(
                    value.replace('\n', '_')
                            .replace('\r', '_')
                            .replace('\t', '_')
                            .replace("%0D", "_")
                            .replace("%0A", "_")
                            .replace("%0d", "_")
                            .replace("%0a", "_")
            );
    }
}
