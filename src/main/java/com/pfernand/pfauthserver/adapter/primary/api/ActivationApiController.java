package com.pfernand.pfauthserver.adapter.primary.api;

import com.pfernand.pfauthserver.core.service.ActivationService;
import com.pfernand.pfauthserver.port.primary.api.ActivationApi;
import com.pfernand.pfauthserver.port.primary.api.response.ActivationApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ActivationApiController implements ActivationApi<ResponseEntity<ActivationApiResponse>> {

    private final ActivationService activationService;

    @Override
    @PutMapping(value = "/user/{userUuid}/activation/{regToken}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActivationApiResponse> activateUser(@PathVariable final UUID userUuid, @PathVariable final String regToken) {
        log.info("PUT /user/{}/activation/{}", userUuid, regToken);
        return ResponseEntity.ok(ActivationApiResponse.builder()
                .userUuid(activationService.activateUser(userUuid, regToken))
                .build());
    }
}