package com.pfernand.pfauthserver.adapter.secondary.event;

import com.pfernand.avro.UserAuthentication;
import com.pfernand.pfauthserver.adapter.secondary.event.exception.EventSendException;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.port.secondary.event.UserAuthenticationPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import javax.inject.Named;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Named
public class UserAuthenticationPublisherKafka implements UserAuthenticationPublisher {

    private final String topicName;

    private final AtomicInteger indexCounter = new AtomicInteger(1);
    private final KafkaTemplate<String, UserAuthentication> kafkaTemplate;
    private final int ackTimeoutInSeconds;

    public UserAuthenticationPublisherKafka(@Value("${kafka.topicName:poc}") final String topicName,
                                            @Value("${spring.kafka.producer.ack-timeout-secs:5}") final int ackTimeoutInSeconds,
                                            final KafkaTemplate<String, UserAuthentication> kafkaTemplate) {
        this.topicName = topicName;
        this.ackTimeoutInSeconds = ackTimeoutInSeconds;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishEvent(UserAuthDetails event) {
        final UserAuthentication userAuthentication = UserAuthentication.newBuilder()
                .setEmail(event.getEmail())
                .setRole(event.getRole())
                .setIndex(indexCounter.getAndIncrement())
                .setUniqueId(UUID.randomUUID().toString())
                .setTime(Instant.now().getEpochSecond())
                .build();
        log.info(String.format("Sending event: %s", userAuthentication.getUniqueId()));
        sendEventToKafka(userAuthentication);
    }

    private void sendEventToKafka(final UserAuthentication userAuthentication) {
        ListenableFuture<SendResult<String, UserAuthentication>> futureSendResult = kafkaTemplate.send(topicName, UUID.randomUUID().toString(), userAuthentication);
        try {
            futureSendResult.get(ackTimeoutInSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            throw new EventSendException();
        }
        log.info(String.format("Event [%s] acknowledged from kafka", userAuthentication.getUniqueId()));
    }
}
