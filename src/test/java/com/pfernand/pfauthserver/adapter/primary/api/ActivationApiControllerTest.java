package com.pfernand.pfauthserver.adapter.primary.api;

import com.pfernand.pfauthserver.core.service.ActivationService;
import com.pfernand.pfauthserver.port.primary.api.response.ActivationApiResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ActivationApiControllerTest {

    @Mock
    private ActivationService activationService;

    @InjectMocks
    private ActivationApiController activationApiController;

    @Test
    public void activateUserShouldCallActivationService() {
        // Given
        final UUID userUuid = UUID.randomUUID();
        final String token = UUID.randomUUID().toString();

        // When
        Mockito.when(activationService.activateUser(userUuid, token))
                .thenReturn(userUuid);
        final ResponseEntity<ActivationApiResponse> result = activationApiController.activateUser(userUuid, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(userUuid, result.getBody().getUserUuid());
        Mockito.verify(activationService, Mockito.times(1))
                .activateUser(userUuid, token);
    }
}