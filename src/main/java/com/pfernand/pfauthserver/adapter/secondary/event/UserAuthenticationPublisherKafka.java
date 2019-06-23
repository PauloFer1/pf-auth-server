package com.pfernand.pfauthserver.adapter.secondary.event;

import com.pfernand.avro.UserAuthentication;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.port.secondary.event.UserAuthenticationPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.inject.Named;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Named
public class UserAuthenticationPublisherKafka implements UserAuthenticationPublisher {

    private final String topicName;

    private final AtomicInteger indexCounter = new AtomicInteger(1);
    private final KafkaTemplate<String, UserAuthentication> kafkaTemplate;

    public UserAuthenticationPublisherKafka(@Value("${kafka.topicName:poc}") final String topicName,
                                            final KafkaTemplate<String, UserAuthentication> kafkaTemplate) {
        this.topicName = topicName;
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
        ListenableFuture<SendResult<String, UserAuthentication>> futureSendResult = kafkaTemplate.send(topicName, UUID.randomUUID().toString(), userAuthentication);
        futureSendResult.addCallback(new ListenableFutureCallback<SendResult<String, UserAuthentication>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error(String.format("Event [%s] failed to store into kafka: %s", userAuthentication.getUniqueId(), ex.getMessage()));
                if (userAuthentication.getEmail().equals("admin100")) {
                    throw new RuntimeException("Success: Upppss...");
                }
            }

            @Override
            public void onSuccess(SendResult<String, UserAuthentication> result) {
                log.info(String.format("Event [%s] stored into kafka", userAuthentication.getUniqueId()));
                if (userAuthentication.getEmail().equals("admin100")) {
                    throw new RuntimeException("Failed: Upppss...");
                }
            }
        });
    }
}
