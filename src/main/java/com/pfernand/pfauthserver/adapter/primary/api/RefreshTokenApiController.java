package com.pfernand.pfauthserver.adapter.primary.api;

import com.pfernand.pfauthserver.adapter.primary.api.mapper.AuthenticationResponseMapper;
import com.pfernand.pfauthserver.adapter.primary.api.validation.InputApiValidation;
import com.pfernand.pfauthserver.core.service.RefreshTokenService;
import com.pfernand.pfauthserver.port.primary.api.RefreshTokenApi;
import com.pfernand.pfauthserver.core.security.model.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RefreshTokenApiController implements RefreshTokenApi<ResponseEntity<AuthenticationResponse>> {

    private final RefreshTokenService refreshTokenService;
    private final AuthenticationResponseMapper authenticationResponseMapper;
    private final InputApiValidation inputApiValidation;

    @Override
    @PostMapping(value = "/refresh-token", produces = "application/json")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestHeader(value = "X-Refresh-Token", defaultValue = "") String refreshToken) {
        inputApiValidation.validateLength(refreshToken);
        log.info("POST /auth/refresh-token with params {}", inputApiValidation.encodeForLog(refreshToken));
        return ResponseEntity.ok(authenticationResponseMapper.map(refreshTokenService.refreshToken(refreshToken)));
    }

}
