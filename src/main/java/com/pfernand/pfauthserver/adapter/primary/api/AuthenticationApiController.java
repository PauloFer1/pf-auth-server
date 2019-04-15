package com.pfernand.pfauthserver.adapter.primary.api;

import com.pfernand.pfauthserver.core.service.AuthenticationService;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.port.primary.api.AuthenticationApi;
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
public class AuthenticationApiController implements AuthenticationApi<ResponseEntity<UserAuthDetails>> {

    private final AuthenticationService authenticationService;

    @Override
    @GetMapping(value = "/user/{email}", produces = "application/json")
    public ResponseEntity<UserAuthDetails> retrieveUserFromEmail(@PathVariable final String email) {
        log.info("GET /user with params {}", email);
        return ResponseEntity.ok(authenticationService.retrieveUserFromEmail(email));
    }

    @Override
    @PostMapping(value = "/user", produces = "application/json")
    public ResponseEntity<UserAuthDetails> insertUser(@RequestBody final UserAuthDetails userAuthDetails) {
        log.info("POST /userAuthDetails with params {}", userAuthDetails);
        return ResponseEntity.ok(authenticationService.insertUser(userAuthDetails));
    }
}