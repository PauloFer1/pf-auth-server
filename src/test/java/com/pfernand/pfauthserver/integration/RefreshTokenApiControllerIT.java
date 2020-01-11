package com.pfernand.pfauthserver.integration;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.pfernand.avro.UserAuthentication;
import com.pfernand.pfauthserver.PfAuthServerApplication;
import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import com.pfernand.pfauthserver.util.MockSerdeConfig;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;


import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@EnableKafka
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PfAuthServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(MockSerdeConfig.class)
@EmbeddedKafka(partitions = 1, controlledShutdown = true,
        brokerProperties = {"listeners=PLAINTEXT://localhost:3332", "port=3332"})
public class RefreshTokenApiControllerIT {

    private static final String TOPIC_NAME = "poc";
    private static final UserAuthDto ADMIN_USER = UserAuthDto.builder()
            .email("admin@email.com")
            .password("pass")
            .role("admin")
            .subject(UserAuthSubject.EMPLOYEE)
            .build();


    @LocalServerPort
    private int port;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SchemaRegistryClient schemaRegistryClient;

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
        mongoTemplate.createCollection("reg_token");
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
        mongoTemplate.dropCollection("reg_token");
    }

    @Test
    public void refreshTokenHappyPath() throws Exception {
        // Given
        final String refreshToken = RestClientManager.getRefreshToken(port, ADMIN_USER);

        // When
        HttpResponse<JsonNode> response =
                RestClientManager.postRefreshToken("http://localhost:" + port + "/refresh-token", refreshToken);

        // When
        assertThat(response.getStatus()).isEqualTo(200);
    }
}