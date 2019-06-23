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

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserAuthenticationPublisherKafkaTest {

    private static final String TOPIC_NAME = "topic";
    private static final String EMAIL = "paulo@mail.com";
    private static final String PASSWORD = "pass";
    private static final String ROLE = "role";

    @Mock
    private KafkaTemplate<String, UserAuthentication> kafkaTemplate;

    private UserAuthenticationPublisherKafka userAuthenticationPublisherKafka;

    @Before
    public void setUp() {
        userAuthenticationPublisherKafka = new UserAuthenticationPublisherKafka(TOPIC_NAME, kafkaTemplate);
    }

    @Test
    public void publishEventSendMessage() {
        // Given
        final UserAuthDetails userAuthDetails = UserAuthDetails.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .role(ROLE)
                .subject(UserAuthSubject.CUSTOMER)
                .build();

        // When
        userAuthenticationPublisherKafka.publishEvent(userAuthDetails);

        // Then
        //Todo, Use Spy to verify UserAuthentication
        Mockito.verify(kafkaTemplate).send(Mockito.eq(TOPIC_NAME), Mockito.any(UserAuthentication.class));
    }

}