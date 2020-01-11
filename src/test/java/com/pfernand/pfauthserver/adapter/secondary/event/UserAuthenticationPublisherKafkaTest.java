package com.pfernand.pfauthserver.adapter.secondary.event;

import com.pfernand.avro.UserAuthentication;
import com.pfernand.pfauthserver.adapter.secondary.event.exception.EventSendException;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.port.secondary.event.dto.UserAuthEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(MockitoJUnitRunner.class)
public class UserAuthenticationPublisherKafkaTest {

    private static final String TOPIC_NAME = "topic";
    private static final String EMAIL = "paulo@mail.com";
    private static final String ROLE = "role";
    private static final int ACK_TIME = 5;
    private static final Instant NOW = Instant.now();

    @Mock
    private KafkaTemplate<String, UserAuthentication> kafkaTemplate;

    @Mock
    private ListenableFuture<SendResult<String, UserAuthentication>> futureSendResult;

    @Mock
    private SendResult<String, UserAuthentication> sendResult;

    private UserAuthenticationPublisherKafka userAuthenticationPublisherKafka;

    @Before
    public void setUp() {
        userAuthenticationPublisherKafka = new UserAuthenticationPublisherKafka(TOPIC_NAME, ACK_TIME, kafkaTemplate);
    }

    @Test
    public void publishEventSendMessage() throws Exception {
        // Given
        final UserAuthEvent userAuthEvent = UserAuthEvent.builder()
                .userUuid(UUID.randomUUID().toString())
                .email(EMAIL)
                .role(ROLE)
                .subject(UserAuthSubject.CUSTOMER)
                .createdAt(NOW)
                .authToken(UUID.randomUUID().toString())
                .build();

        // When
        Mockito.when(kafkaTemplate.send(Mockito.eq(TOPIC_NAME), Mockito.anyString(), Mockito.any(UserAuthentication.class)))
                .thenReturn(futureSendResult);
        Mockito.when(futureSendResult.get(ACK_TIME, TimeUnit.SECONDS))
                .thenReturn(sendResult);
        userAuthenticationPublisherKafka.publishEvent(userAuthEvent);

        // Then
        //Todo, Use Spy to verify UserAuthentication
        Mockito.verify(kafkaTemplate).send(Mockito.eq(TOPIC_NAME), Mockito.anyString(), Mockito.any(UserAuthentication.class));
    }

    @Test
    public void publishEventThrowsException() throws Exception {
        // Given
        final UserAuthEvent userAuthEvent = UserAuthEvent.builder()
                .userUuid(UUID.randomUUID().toString())
                .email(EMAIL)
                .role(ROLE)
                .subject(UserAuthSubject.CUSTOMER)
                .createdAt(NOW)
                .authToken(UUID.randomUUID().toString())
                .build();

        Mockito.when(kafkaTemplate.send(Mockito.eq(TOPIC_NAME), Mockito.anyString(), Mockito.any(UserAuthentication.class)))
                .thenReturn(futureSendResult);
        Mockito.when(futureSendResult.get(ACK_TIME, TimeUnit.SECONDS))
                .thenThrow(new TimeoutException("timeout"));

        // Then
        assertThatExceptionOfType(EventSendException.class)
                .isThrownBy(() -> userAuthenticationPublisherKafka.publishEvent(userAuthEvent))
                .withMessageContaining("Could not send/acknowledged event from kafka. Will fail to insert user [Rollback].");
    }

}