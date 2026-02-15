package com.example.contentgenerator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/api/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        logger.info("Calling /api/user endpoint");
        if (principal == null) {
            logger.warn("Principal is null in /api/user");
            return Collections.singletonMap("error", "Not authenticated");
        }
        logger.info("Principal in /api/user: {}", principal.getAttributes());
        return principal.getAttributes();
    }

    @GetMapping("/hello")
    public String hello() {
        logger.info("Calling /hello endpoint");
        return "Hello";
    }
}
