package com.pfernand.pfauthserver.adapter.secondary.event;

import com.pfernand.avro.UserAuthentication;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
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
    private static final String PASSWORD = "pass";
    private static final String ROLE = "role";
    private static final int ACK_TIME = 5;

    @Mock
    private KafkaTemplate<String, UserAuthentication> kafkaTemplate;

    @Mock
    private ListenableFuture<SendResult<String, UserAuthentication>> futureSendResult;

    @Mock
    private SendResult<String, UserAuthentication> sendResult;

    private UserAuthenticationPublisherKafka userAuthenticationPublisherKafka;

    @Before
    public void setUp() {
        userAuthenticationPublisherKafka = new UserAuthenticationPublisherKafka(TOPIC_NAME, ACK_TIME, 3, kafkaTemplate);
    }

    @Test
    public void publishEventSendMessage() throws Exception {
        // Given
        final UserAuthDetails userAuthDetails = UserAuthDetails.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .role(ROLE)
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        final UserAuthentication userAuthentication = UserAuthentication.newBuilder()
                .setEmail(EMAIL)
                .setRole(ROLE)
                .setIndex(1)
                .setUniqueId(UUID.randomUUID().toString())
                .setTime(Instant.now().getEpochSecond())
                .build();

        // When
        Mockito.when(kafkaTemplate.send(Mockito.eq(TOPIC_NAME), Mockito.anyString(), Mockito.any(UserAuthentication.class)))
                .thenReturn(futureSendResult);
        Mockito.when(futureSendResult.get(ACK_TIME, TimeUnit.SECONDS))
                .thenReturn(sendResult);
        userAuthenticationPublisherKafka.publishEvent(userAuthDetails);

        // Then
        //Todo, Use Spy to verify UserAuthentication
        Mockito.verify(kafkaTemplate).send(Mockito.eq(TOPIC_NAME), Mockito.anyString(), Mockito.any(UserAuthentication.class));
    }

    //Todo, Tests for catch exception
    @Test
    public void publishEventThrowsException() throws Exception {
        // Given
        final UserAuthDetails userAuthDetails = UserAuthDetails.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .role(ROLE)
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        final UserAuthentication userAuthentication = UserAuthentication.newBuilder()
                .setEmail(EMAIL)
                .setRole(ROLE)
                .setIndex(1)
                .setUniqueId(UUID.randomUUID().toString())
                .setTime(Instant.now().getEpochSecond())
                .build();

        Mockito.when(kafkaTemplate.send(Mockito.eq(TOPIC_NAME), Mockito.anyString(), Mockito.any(UserAuthentication.class)))
                .thenReturn(futureSendResult);
        Mockito.when(futureSendResult.get(ACK_TIME, TimeUnit.SECONDS))
                .thenThrow(new TimeoutException("timeout"));

        // Then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> userAuthenticationPublisherKafka.publishEvent(userAuthDetails))
                .withMessageContaining("Failed to store");
    }

}