
package com.example.contentgenerator.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.contentgenerator.dto.GenerateRequest;
import com.example.contentgenerator.service.MarketingService;
import com.example.contentgenerator.util.LinkedInUtil;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * REST controller for marketing content generation.
 * Exposes endpoints for content generation and LinkedIn authentication.
 */
@RestController
@RequestMapping("/api/marketing")
public class MarketingController {

    private final MarketingService marketingService;
    private final LinkedInUtil linkedInUtil;

    public MarketingController(MarketingService marketingService, LinkedInUtil linkedInUtil) {
        this.marketingService = marketingService;
        this.linkedInUtil = linkedInUtil;
    }

    /**
     * Endpoint to generate marketing content.
     * Supports both Bearer token and session-based authentication.
     *
     * @param request The request body containing content generation parameters.
     * @param authorizationHeader The Authorization header (optional).
     * @param session The HTTP session.
     * @return A response entity with the generated content or scheduling status.
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateContent(
            @Valid @RequestBody GenerateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            HttpSession session) {

        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        } else {
            accessToken = (String) session.getAttribute("linkedin_access_token");
        }

        if (accessToken == null && "linkedin".equalsIgnoreCase(request.getPlatform())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "LinkedIn access token not found. Please authenticate."));
        }

        Map<String, Object> response = marketingService.generateContent(request, accessToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Redirects the user to LinkedIn's authorization page.
     */
    @GetMapping("/linkedin/auth")
    public ResponseEntity<Void> linkedInAuth() {
        String authorizationUrl = linkedInUtil.getAuthorizationUrl();
        System.out.println(authorizationUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(authorizationUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Handles the callback from LinkedIn after authorization.
     * Exchanges the authorization code for an access token, stores it in the session,
     * and returns the token in the response body for easier use with API clients.
     */
    @GetMapping("/linkedin/callback")
    public ResponseEntity<String> linkedInCallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpSession session) {
        try {
            String accessToken = linkedInUtil.exchangeCodeForToken(code);
            session.setAttribute("linkedin_access_token", accessToken);
            // Return the token in the body for easy use in API clients like Postman
            return ResponseEntity.ok("Authentication successful. Your access token is: " + accessToken);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during LinkedIn authentication: " + e.getMessage());
        }
    }
}
