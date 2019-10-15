package com.pfernand.pfauthserver.adapter.primary.api;

import com.pfernand.pfauthserver.core.model.UserAuth;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.port.primary.api.AuthenticationApi;
import com.pfernand.pfauthserver.port.primary.api.request.UserAuthApiRequest;
import com.pfernand.pfauthserver.port.primary.api.response.UserAuthApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationApiController implements AuthenticationApi<ResponseEntity<UserAuthApiResponse>> {

    private final AuthenticationService authenticationService;

    @Override
    @GetMapping(value = "/user/{email}", produces = "application/json")
    public ResponseEntity<UserAuthApiResponse> retrieveUserFromEmail(@PathVariable final String email) {
        log.info("GET /user with params {}", email);
        return ResponseEntity.ok(mapToResponse(authenticationService.retrieveUserFromEmail(email)));
    }

    @Override
    @PostMapping(value = "/user", produces = "application/json")
    public ResponseEntity<UserAuthApiResponse> insertUser(@RequestBody final UserAuthApiRequest userAuthApiRequest) {
        log.info("POST /userAuthDetails with params {}", userAuthApiRequest);
        return ResponseEntity.ok(mapToResponse(authenticationService.insertUser(UserAuthDto.builder()
                .email(userAuthApiRequest.getEmail())
                .password(userAuthApiRequest.getPassword())
                .subject(userAuthApiRequest.getSubject())
                .role(userAuthApiRequest.getRole())
                .build())));
    }

    private UserAuthApiResponse mapToResponse(final UserAuth userAuth) {
        return UserAuthApiResponse.builder()
                .email(userAuth.getEmail())
                .role(userAuth.getRole())
                .createdAt(userAuth.getCreatedAt())
                .subject(userAuth.getSubject())
                .build();
    }
}