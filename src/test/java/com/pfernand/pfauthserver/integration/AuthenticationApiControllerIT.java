package com.pfernand.pfauthserver.integration;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.pfernand.pfauthserver.PfAuthServerApplication;
import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PfAuthServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthenticationApiControllerIT {

    private static final UserAuthDetails ADMIN_USER = UserAuthDetails.builder()
            .email("admin@email.com")
            .password("pass")
            .role("admin")
            .subject(UserAuthSubject.EMPLOYEE)
            .build();
    private static final UserAuthDetails TEST_USER = UserAuthDetails.builder()
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
    private AuthenticationService authenticationService;

    @Autowired
    private MongoTemplate mongoTemplate;


    @LocalServerPort
    private int port;


    @Before
    public void setUp() {
        authenticationService.insertUser(ADMIN_USER);
    }

    @After
    public void tearDown() {
        final Query query = new Query();
        query.addCriteria(Criteria.where("email").is(ADMIN_USER.getEmail()));
        mongoTemplate.remove(query, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
    }

    @Test
    public void insertUserHappyPath() throws Exception {
        // Given
        final String authToken = RestClientManager.getAuthToken(port, ADMIN_USER);
        // When
        RestClientManager.postJsonExpectingStatus("http://localhost:" + port + "/user", TEST_USER_PARAMS.toString(), authToken, 200);

        // Then
        UserAuthDetails userAuthDetails = authenticationService.retrieveUserFromEmail(TEST_USER.getEmail());
        assertThat(userAuthDetails).isEqualToIgnoringGivenFields(TEST_USER, "password");
        assertThat(userAuthDetails.getPassword()).isNotEmpty();
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
}
