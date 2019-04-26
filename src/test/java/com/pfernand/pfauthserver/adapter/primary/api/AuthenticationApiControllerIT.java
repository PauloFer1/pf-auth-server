package com.pfernand.pfauthserver.adapter.primary.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.pfernand.pfauthserver.PfAuthServerApplication;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.core.model.UserAuthProperties;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PfAuthServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthenticationApiControllerIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @LocalServerPort
    private int port;


    @Before
    public void setUp() {
        authenticationService.insertUser(ADMIN_USER);
    }

    @Test
    public void insertUserHappyPath() throws Exception {
        // Given
        final String authToken = getAuthToken();
        // When
        postJsonExpectingStatus("http://localhost:" + port + "/user", TEST_USER_PARAMS.toString(), authToken, 200);

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
        HttpResponse<JsonNode> response = postJson("http://localhost:" + port + "/user", TEST_USER_PARAMS.toString(), authToken);

        // Then
        assertThat(response.getStatus()).isEqualTo(401);
    }

    private String getAuthToken() throws Exception {
        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        parameters
                .put("username", ADMIN_USER.getEmail())
                .put("password", ADMIN_USER.getPassword());
        HttpResponse<JsonNode> httpJson = Unirest.post("http://localhost:" + port + "/auth")
                .header("Content-Type", "application/json")
                .body(parameters.toString())
                .asJson();
        assertThat(httpJson.getStatus()).isEqualTo(200);
        UserAuthProperties userAuthProperties =
                OBJECT_MAPPER.readValue(httpJson.getBody().toString(), UserAuthProperties.class);
        return userAuthProperties.getAccessToken();
    }

    private JSONObject postJsonExpectingStatus(String path, String requestBody, String authToken, int expectedStatusCode) throws Exception {
            final HttpResponse<JsonNode> httpJson = postJson(path, requestBody, authToken);
            assertThat(httpJson.getStatus()).isEqualTo(expectedStatusCode);
            return httpJson.getBody().getObject();
    }

    private HttpResponse<JsonNode> postJson(String path, String requestBody, String authToken) throws Exception {
        return Unirest.post(path)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(requestBody)
                .asJson();
    }
}
