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

//    @Bean
//    @Primary
//	public Mongo mongoTest() throws IOException {
//		return new EmbeddedMongoBuilder()
//				.version("4.0.2")
//				.bindIp("127.0.0.1")
//				.port(27017)
//				.build();
//	}

//    @Bean
//    @Primary
//    public MongoTemplate mongoTemplateTest() throws IOException {
//        EmbeddedMongoFactoryBean mongo = new EmbeddedMongoFactoryBean();
//        mongo.setBindIp("localhost");
//        MongoClient mongoClient = mongo.getObject();
//        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "pf-auth-db");
//        return mongoTemplate;
//    }

//    @Bean
//    @Primary
//    public MongoClient mongoClientTest() throws IOException {
//        MongodStarter runtime = MongodStarter.getDefaultInstance();
//        int node1Port = 27017;
//        int node2Port = 27018;
//        MongodExecutable node1MongodExe;
//        MongodProcess node1Mongod;
//        MongoClient mongo;
//        MongodExecutable node2MongodExe;
//        MongodProcess node2Mongod;
//
//        node1MongodExe = runtime.prepare(new MongodConfigBuilder().version(Version.Main.V4_0)
//                .withLaunchArgument("--replSet", "rs0")
//                .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
//                .net(new Net(node1Port, Network.localhostIsIPv6())).build());
//        node1Mongod = node1MongodExe.start();
//
//        node2MongodExe = runtime.prepare(new MongodConfigBuilder().version(Version.Main.V4_0)
//                .withLaunchArgument("--replSet", "rs0")
//                .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
//                .net(new Net(node2Port, Network.localhostIsIPv6())).build());
//        node2Mongod = node2MongodExe.start();
//
//        mongo = new MongoClient(new ServerAddress(Network.getLocalHost(), node1Port));
//
//        MongoDatabase adminDatabase = mongo.getDatabase("admin");
//
//        Document config = new Document("_id", "rs0");
//        BasicDBList members = new BasicDBList();
//        members.add(new Document("_id", 0)
//                .append("host", "localhost:" + node1Port));
//        members.add(new Document("_id", 1)
//                .append("host", "localhost:" + node2Port));
//        config.put("members", members);
//
//        adminDatabase.runCommand(new org.bson.Document("replSetInitiate", config));
//
//        System.out.println(">>>>>>>>" + adminDatabase.runCommand(new org.bson.Document("replSetGetStatus", 1)));
//
//        return mongo;
//    }

//    @Bean
//    @Primary
//    public MongoDbFactory mongoDbFactoryTest(MongoClient mongoClient) {
//        return new SimpleMongoDbFactory(mongoClient, "pf-auth-db");
//    }

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
