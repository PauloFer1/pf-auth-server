package com.pfernand.pfauthserver.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.pfernand.pfauthserver.PfAuthServerApplication;
import com.pfernand.pfauthserver.config.DatabaseConfiguration;
import com.pfernand.pfauthserver.core.model.UserAuthDetails;
import com.pfernand.pfauthserver.core.model.UserAuthSubject;
import com.pfernand.pfauthserver.core.service.AuthenticationService;
import com.pfernand.pfauthserver.util.MockSerdeConfig;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PfAuthServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(MockSerdeConfig.class)
public class RefreshTokenApiControllerIT {

    private static final UserAuthDetails ADMIN_USER = UserAuthDetails.builder()
            .email("admin@email.com")
            .password("pass")
            .role("admin")
            .subject(UserAuthSubject.EMPLOYEE)
            .build();

//    private static final WireMockServer wireMockServer = new WireMockServer();

    @LocalServerPort
    private int port;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MongoTemplate mongoTemplate;

//    @ClassRule
//    public static WireMockClassRule wiremock = new WireMockClassRule(
//            WireMockSpring.options().port(8081));

    @BeforeClass
    public static void setUpStatic() throws Exception {
//        WireMock.configureFor("schema-registry-mock", 8081);
//        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/subjects/poc-value/versions"))
//                .willReturn(WireMock.aResponse()
//                        .withHeader("Content-Type", "application/json")
//                        .withBody("{\"id\":117}"))
//        );
//        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/subjects/poc-value"))
//                .willReturn(WireMock.aResponse()
//                        .withHeader("Content-Type", "application/json")
//                        .withBody("{\"subject\":\"poc-value\",\"version\":11,\"id\":117,\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"name\\\":\\\"UserAuthentication\\\",\\\"namespace\\\":\\\"com.pfernand.avro\\\",\\\"fields\\\":[" +
//                                " {\"name\": \"time\", \"type\": \"long\", \"doc\": \"Time of message generation\"},\n" +
//                                "        {\"name\": \"email\", \"type\": \"string\", \"doc\": \"Email of the user\"},\n" +
//                                "        {\"name\": \"role\", \"type\": \"string\", \"doc\": \"Auth role of the user\"},\n" +
//                                "        {\"name\": \"uniqueId\", \"type\": \"string\", \"doc\": \"Unique Message id\"},\n" +
//                                "        {\"name\": \"index\", \"type\": \"int\", \"default\": 0, \"doc\" : \"Message index\"}],\\\"default\\\":null}],\\\"_topic\\\":\\\"actioned\\\"}\"}")));
//
//        wireMockServer.start();
//        Thread.sleep(60000);
    }

    @Before
    public void setUp() {

        authenticationService.insertUser(ADMIN_USER);

    }

    @After
    public void tearDown() {
        final Query query = new Query();
        query.addCriteria(Criteria.where("email").is(ADMIN_USER.getEmail()));
        mongoTemplate.remove(query, DatabaseConfiguration.MONGO_COLLECTIONS.AUTHENTICATION_COLLECTION.collection());
//        wireMockServer.resetAll();
//        wireMockServer.stop();
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
