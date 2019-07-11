package com.pfernand.pfauthserver.integration;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.pfernand.avro.UserAuthentication;
import com.pfernand.pfauthserver.PfAuthServerApplication;
import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import com.pfernand.pfauthserver.util.MockSerdeConfig;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.junit.After;
import org.junit.Before;
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

import static org.assertj.core.api.Assertions.assertThat;

@EnableKafka
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PfAuthServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(MockSerdeConfig.class)
@EmbeddedKafka(partitions = 1, controlledShutdown = false,
        brokerProperties = {"listeners=PLAINTEXT://localhost:3333", "port=3333"})
public class RefreshTokenApiControllerIT {

    private static final String TOPIC_NAME = "poc";
    private static final UserAuthDetails ADMIN_USER = UserAuthDetails.builder()
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


    @Before
    public void setUp() throws Exception {
        schemaRegistryClient.register(TOPIC_NAME + "-value", UserAuthentication.SCHEMA$);
        authenticationService.insertUser(ADMIN_USER);
    }

    @After
    public void tearDown() {
        final Query query = new Query();
        query.addCriteria(Criteria.where("email").is(ADMIN_USER.getEmail()));
        mongoTemplate.remove(query, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
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
