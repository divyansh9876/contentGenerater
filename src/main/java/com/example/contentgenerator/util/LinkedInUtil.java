
package com.example.contentgenerator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Utility for posting content to LinkedIn.
 * This is a placeholder to demonstrate modular design.
 * A real implementation would use the LinkedIn API with OAuth2 authentication.
 */
@Component
public class LinkedInUtil {

    private static final Logger logger = LoggerFactory.getLogger(LinkedInUtil.class);
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper;

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String apiUrl;

    public LinkedInUtil(
            @Value("${linkedin.client.id:}") String clientId,
            @Value("${linkedin.client.secret:}") String clientSecret,
            @Value("${linkedin.redirect.uri:}") String redirectUri,
            @Value("${linkedin.api.url:https://api.linkedin.com/v2/ugcPosts}") String apiUrl,
            ObjectMapper objectMapper) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates the LinkedIn OAuth 2.0 Authorization URL.
     */
    public String getAuthorizationUrl() {
        String state = UUID.randomUUID().toString(); // CSRF protection
        String scopes = "openid profile email w_member_social";
        
        try {
            return "https://www.linkedin.com/oauth/v2/authorization" +
                    "?response_type=code" +
                    "&client_id=" + clientId +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()) +
                    "&state=" + state +
                    "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // This should not happen with UTF-8
            throw new RuntimeException(e);
        }
    }

    /**
     * Exchanges the authorization code for an access token.
     */
    public String exchangeCodeForToken(String code) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build();

        Request request = new Request.Builder()
                .url("https://www.linkedin.com/oauth/v2/accessToken")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.has("access_token")) {
                return node.get("access_token").asText();
            } else {
                throw new IOException("Token not found in response: " + responseBody);
            }
        }
    }

    /**
     * Posts the given content to LinkedIn.
     *
     * @param accessToken The OAuth2 access token.
     * @param content The text content to be posted.
     * @throws IOException If the API call fails.
     */
    public void post(String accessToken, String content) throws IOException {
        if (accessToken == null || accessToken.isBlank()) {
            logger.warn("No access token provided. Skipping actual LinkedIn post.");
            return;
        }

        String personUrn = getMemberUrn(accessToken);
        
        // Construct the JSON payload for ugcPosts
        String jsonBody = String.format(
            "{\"author\":\"%s\",\"lifecycleState\":\"PUBLISHED\",\"specificContent\":{\"com.linkedin.ugc.ShareContent\":{\"shareCommentary\":{\"text\":\"%s\"},\"shareMediaCategory\":\"NONE\"}},\"visibility\":{\"com.linkedin.ugc.MemberNetworkVisibility\":\"PUBLIC\"}}",
            personUrn, content.replace("\"", "\\\"").replace("\n", "\\n")
        );

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Restli-Protocol-Version", "2.0.0")
                .post(body)
                .build();

        logger.info("Executing actual LinkedIn API call to: {}", apiUrl);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                logger.error("LinkedIn API error: {} - {}", response.code(), errorBody);
                throw new IOException("Failed to post to LinkedIn: " + errorBody);
            }
            logger.info("Successfully posted to LinkedIn. Response code: {}", response.code());
        }
    }

    /**
     * Posts the given content to a LinkedIn Page (Organization).
     *
     * @param accessToken The OAuth2 access token.
     * @param content The text content to be posted.
     * @param pageId The ID of the LinkedIn page (organization).
     * @throws IOException If the API call fails.
     */
    public void postToPage(String accessToken, String content, String pageId) throws IOException {
        if (accessToken == null || accessToken.isBlank()) {
            logger.warn("No access token provided. Skipping actual LinkedIn post.");
            return;
        }

        if (pageId == null || pageId.isBlank()) {
            throw new IllegalArgumentException("Page ID is required for posting to a page.");
        }

        String organizationUrn = "urn:li:organization:" + pageId;
        
        // Construct the JSON payload for ugcPosts
        String jsonBody = String.format(
            "{\"author\":\"%s\",\"lifecycleState\":\"PUBLISHED\",\"specificContent\":{\"com.linkedin.ugc.ShareContent\":{\"shareCommentary\":{\"text\":\"%s\"},\"shareMediaCategory\":\"NONE\"}},\"visibility\":{\"com.linkedin.ugc.MemberNetworkVisibility\":\"PUBLIC\"}}",
            organizationUrn, content.replace("\"", "\\\"").replace("\n", "\\n")
        );

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Restli-Protocol-Version", "2.0.0")
                .post(body)
                .build();

        logger.info("Executing actual LinkedIn API call to: {} for page: {}", apiUrl, pageId);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                logger.error("LinkedIn API error: {} - {}", response.code(), errorBody);
                throw new IOException("Failed to post to LinkedIn page: " + errorBody);
            }
            logger.info("Successfully posted to LinkedIn page. Response code: {}", response.code());
        }
    }

    /**
     * Fetches the member's profile URN using the OIDC userinfo endpoint.
     */
    private String getMemberUrn(String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url("https://api.linkedin.com/v2/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch LinkedIn user info: " + response.code());
            }
            JsonNode node = objectMapper.readTree(response.body().string());
            String id = node.get("sub").asText();
            return "urn:li:person:" + id;
        }
    }
}
