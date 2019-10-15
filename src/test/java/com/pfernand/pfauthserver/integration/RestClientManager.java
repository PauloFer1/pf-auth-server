package com.pfernand.pfauthserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.pfernand.pfauthserver.core.model.UserAuthDto;
import com.pfernand.pfauthserver.core.model.UserAuthProperties;
import org.json.JSONObject;

import static org.assertj.core.api.Assertions.assertThat;

public class RestClientManager {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static String getAuthToken(int port, UserAuthDto adminUser) throws Exception {
        return getUserAuthProperties(port, adminUser).getAccessToken();
    }

    static String getRefreshToken(int port, UserAuthDto adminUser) throws Exception {
        return getUserAuthProperties(port, adminUser).getRefreshToken();
    }

    static JSONObject postJsonExpectingStatus(String path, String requestBody, String authToken, int expectedStatusCode) throws Exception {
        final HttpResponse<JsonNode> httpJson = postJson(path, requestBody, authToken);
        assertThat(httpJson.getStatus()).isEqualTo(expectedStatusCode);
        return httpJson.getBody().getObject();
    }

    static HttpResponse<JsonNode> postJson(String path, String requestBody, String authToken) throws Exception {
        return Unirest.post(path)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(requestBody)
                .asJson();
    }

    static HttpResponse<JsonNode> postRefreshToken(String path, String refreshToken) throws Exception {
        return Unirest.post(path)
                .header("X-Refresh-Token", refreshToken)
                .asJson();
    }

    private static UserAuthProperties getUserAuthProperties(int port, UserAuthDto adminUser) throws Exception {
        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        parameters
                .put("username", adminUser.getEmail())
                .put("password", adminUser.getPassword());
        HttpResponse<JsonNode> httpJson = Unirest.post("http://localhost:" + port + "/auth")
                .header("Content-Type", "application/json")
                .body(parameters.toString())
                .asJson();
        assertThat(httpJson.getStatus()).isEqualTo(200);
        return OBJECT_MAPPER.readValue(httpJson.getBody().toString(), UserAuthProperties.class);
    }
}
