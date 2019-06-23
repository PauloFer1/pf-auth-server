package com.pfernand.pfauthserver.util;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.log4j.Log4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import java.util.Map;

@Log4j
@Configuration
public class MockSerdeConfig {

    /**
     * Mock schema registry bean used by Kafka Avro Serde since
     * the @EmbeddedKafka setup doesn't include a schema registry.
     * @return MockSchemaRegistryClient instance
     */
    @Bean
    MockSchemaRegistryClient schemaRegistryClient() {
        return new MockSchemaRegistryClient();
    }

    /**
     * KafkaAvroSerializer that uses the MockSchemaRegistryClient
     * @return KafkaAvroSerializer instance
     */
    @Bean
    @Primary
    KafkaAvroSerializer kafkaAvroSerializer() {
       return new KafkaAvroSerializer(schemaRegistryClient());
    }

    /**
     * KafkaAvroDeserializer that uses the MockSchemaRegistryClient.
     * The props must be provided so that specific.avro.reader: true
     * is set. Without this, the consumer will receive GenericData records.
     * @return KafkaAvroDeserializer instance
     */
    @Bean
    @Primary
    KafkaAvroDeserializer kafkaAvroDeserializer(final KafkaProperties kafkaProperties, @Value("${schema.registry.url}") String schemaUrl) {
        log.info("£££££££££££££££££££");
        log.info(schemaUrl);
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaUrl);
        return new KafkaAvroDeserializer(schemaRegistryClient(), props);
    }

    /**
     * Configures the kafka producer factory to use the overridden
     * KafkaAvroDeserializer so that the MockSchemaRegistryClient
     * is used rather than trying to reach out via HTTP to a schema registry
     * @return DefaultKafkaProducerFactory instance
     */
    @Bean
    @Primary
    DefaultKafkaProducerFactory producerFactory(final KafkaProperties kafkaProperties, final KafkaAvroSerializer kafkaAvroSerializer) {
        return new DefaultKafkaProducerFactory(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                kafkaAvroSerializer
        );
    }

    /**
     * Configures the kafka consumer factory to use the overridden
     * KafkaAvroSerializer so that the MockSchemaRegistryClient
     * is used rather than trying to reach out via HTTP to a schema registry
     * @return DefaultKafkaConsumerFactory instance
     */
    @Bean
    @Primary
    DefaultKafkaConsumerFactory consumerFactory(final KafkaProperties kafkaProperties, final KafkaAvroDeserializer kafkaAvroDeserializer) {
        return new DefaultKafkaConsumerFactory(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                kafkaAvroDeserializer
        );
    }

    /**
     * Configure the ListenerContainerFactory to use the overridden
     * consumer factory so that the MockSchemaRegistryClient is used
     * under the covers by all consumers when deserializing Avro data.
     * @return ConcurrentKafkaListenerContainerFactory instance
     */
    @Bean
    @Primary
    ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory(final DefaultKafkaConsumerFactory defaultKafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(defaultKafkaConsumerFactory);
        return factory;
    }
}
