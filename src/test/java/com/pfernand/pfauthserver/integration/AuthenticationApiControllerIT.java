package com.pfernand.pfauthserver.integration;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.pfernand.avro.UserAuthentication;
import com.pfernand.pfauthserver.PfAuthServerApplication;
import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuth;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnableKafka
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PfAuthServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@EmbeddedKafka(partitions = 1, controlledShutdown = true,
        brokerProperties = {"listeners=PLAINTEXT://localhost:3333", "port=3333"})
public class AuthenticationApiControllerIT {

    private static final String TOPIC_NAME = "poc";
    private static final UserAuthDto ADMIN_USER = UserAuthDto.builder()
            .email("admin@email.com")
            .password("pass")
            .role("admin")
            .subject(UserAuthSubject.EMPLOYEE)
            .build();
    private static final UserAuth TEST_USER = UserAuth.builder()
            .email("paulo@email.com")
            .password("pass")
            .role("admin")
            .subject(UserAuthSubject.CUSTOMER)
            .build();
    private static final ObjectNode TEST_USER_PARAMS = JsonNodeFactory.instance.objectNode()
            .put("email", TEST_USER.getEmail())
            .put("password", TEST_USER.getPassword())
            .put("role", TEST_USER.getRole())
            .put("subject", TEST_USER.getSubject().getSubject());


    @Autowired
    private SchemaRegistryClient schemaRegistryClient;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MongoTemplate mongoTemplate;


    @LocalServerPort
    private int port;

    @Autowired
    private ConsumerFactory<String, UserAuthentication> consumerFactory;

    private KafkaMessageListenerContainer<String, UserAuthentication> kafkaMessageListenerContainer;

    private static final ContainerProperties CONTAINER_PROPERTIES = new ContainerProperties(TOPIC_NAME);


    @ClassRule
    public static GenericContainer mongoContainer = new GenericContainer("mongo:4.0.8")
            .withExposedPorts(27017)
            .waitingFor(Wait.forLogMessage(".*waiting for connections on port 27017.*", 1))
            .withCommand("--replSet rs");


    static {
        Runnable runnable = () -> {
            final long maxSecondsTry = 3;
            final Instant now = Instant.now();
            Instant inRunning;
            while (!mongoContainer.isRunning()) {
                inRunning = Instant.now();
                if (Duration.between(now, inRunning).getSeconds() > maxSecondsTry) {
                    throw new RuntimeException(String.format("Container is taking more then %d seconds to start", maxSecondsTry));
                }
            }
            try {
                Thread.sleep(1000);
                String address = "mongodb://" + mongoContainer.getContainerIpAddress() + ":" + mongoContainer.getFirstMappedPort() + "/pf-auth-db";
                System.setProperty("spring.data.mongodb.uri", address);
                Container.ExecResult lsResult = mongoContainer.execInContainer("/bin/bash", "-c", "mongo --eval 'rs.initiate()'");
                System.out.println(String.format("[OUTPUT]: %s", lsResult.getStdout()));
            } catch (Exception ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex.getMessage());
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Before
    public void setUp() throws Exception {
        mongoTemplate.createCollection("user");
        mongoTemplate.createCollection("refresh-token");

        schemaRegistryClient.register(TOPIC_NAME + "-value", UserAuthentication.SCHEMA$);

        authenticationService.insertUser(ADMIN_USER);
    }

    @After
    public void tearDown() {
        final Query query = new Query();
        query.addCriteria(Criteria.where("email").is(ADMIN_USER.getEmail()));
        mongoTemplate.remove(query, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
        mongoTemplate.dropCollection("user");
        mongoTemplate.dropCollection("refresh-token");
        if (kafkaMessageListenerContainer != null && kafkaMessageListenerContainer.isRunning()) {
            kafkaMessageListenerContainer.stop();
        }
    }

    @Test
    public void insertUserHappyPath() throws Exception {
        // Given
        final String authToken = RestClientManager.getAuthToken(port, ADMIN_USER);
        kafkaMessageListenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, CONTAINER_PROPERTIES);
        MessageListener<String, UserAuthentication> messageListener =
                (ConsumerRecord<String, UserAuthentication> c) -> assertKafkaMessage(c.value(), TEST_USER);
        kafkaMessageListenerContainer.setupMessageListener(messageListener);
        kafkaMessageListenerContainer.start();
        // When
        RestClientManager.postJsonExpectingStatus("http://localhost:" + port + "/user", TEST_USER_PARAMS.toString(), authToken, 200);


        // Then
        UserAuth userAuth = authenticationService.retrieveUserFromEmail(TEST_USER.getEmail());
        assertThat(userAuth).isEqualToIgnoringGivenFields(TEST_USER, "password", "createdAt");
        assertThat(userAuth.getPassword()).isNotEmpty();
        assertThat(userAuth.getCreatedAt()).isBetween(Instant.now().minusSeconds(1), Instant.now());
    }

    @Test
    public void insertUserAlreadyExistentReturns500() throws Exception {
        // Given
        final String authToken = RestClientManager.getAuthToken(port, ADMIN_USER);
        kafkaMessageListenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, CONTAINER_PROPERTIES);
        MessageListener<String, UserAuthentication> messageListener =
                (ConsumerRecord<String, UserAuthentication> c) -> assertKafkaMessage(c.value(), TEST_USER);
        kafkaMessageListenerContainer.setupMessageListener(messageListener);
        kafkaMessageListenerContainer.start();
        RestClientManager.postJsonExpectingStatus("http://localhost:" + port + "/user", TEST_USER_PARAMS.toString(), authToken, 200);

        // When
        RestClientManager.postJsonExpectingStatus("http://localhost:" + port + "/user", TEST_USER_PARAMS.toString(), authToken, 500);


        // Then
        UserAuth userAuth = authenticationService.retrieveUserFromEmail(TEST_USER.getEmail());
        assertThat(userAuth).isEqualToIgnoringGivenFields(TEST_USER, "password", "createdAt");
        assertThat(userAuth.getPassword()).isNotEmpty();
        assertThat(userAuth.getCreatedAt()).isBetween(Instant.now().minusSeconds(1), Instant.now());
    }

    @Test
    public void insertUserWithoutInvalidTokenReturns401() throws Exception {
        // Given
        final String authToken = "Invalid token";
        // When
        HttpResponse<JsonNode> response = RestClientManager.postJson("http://localhost:" + port + "/user", TEST_USER_PARAMS.toString(), authToken);

        // Then
        assertThat(response.getStatus()).isEqualTo(401);
    }

    private void assertKafkaMessage(final UserAuthentication userAuthentication, final UserAuth userAuth) {
        log.info("ASSERT KAFKA EVENT");
        log.info(userAuthentication.toString());
        assertThat(userAuthentication.getEmail().toString()).isEqualTo(userAuth.getEmail());
        assertThat(userAuthentication.getRole().toString()).isEqualTo(userAuth.getRole());
    }
}
