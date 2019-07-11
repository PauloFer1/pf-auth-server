package com.pfernand.pfauthserver.util;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import java.util.Map;

@Configuration
public class MockSerdeConfig {

    @Bean
    @Primary
    SchemaRegistryClient schemaRegistryClient() {
        return new MockSchemaRegistryClient();
    }

    @Bean
    @Primary
    KafkaAvroSerializer kafkaAvroSerializer() {
       return new KafkaAvroSerializer(schemaRegistryClient());
    }

    @Bean
    @Primary
    KafkaAvroDeserializer kafkaAvroDeserializer(final KafkaProperties kafkaProperties, @Value("${spring.kafka.producer.properties.schema.registry.url}") String schemaUrl) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaUrl);
        return new KafkaAvroDeserializer(schemaRegistryClient(), props);
    }

    @Bean
    @Primary
    DefaultKafkaProducerFactory producerFactory(final KafkaProperties kafkaProperties, final KafkaAvroSerializer kafkaAvroSerializer) {
        return new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                kafkaAvroSerializer
        );
    }

    @Bean
    @Primary
    DefaultKafkaConsumerFactory consumerFactory(final KafkaProperties kafkaProperties, final KafkaAvroDeserializer kafkaAvroDeserializer) {
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                kafkaAvroDeserializer
        );
    }

    @Bean
    @Primary
    AbstractKafkaListenerContainerFactory kafkaListenerContainerFactory(final DefaultKafkaConsumerFactory defaultKafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(defaultKafkaConsumerFactory);
        return factory;
    }
}
