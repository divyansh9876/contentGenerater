
package com.example.contentgenerator.service;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.contentgenerator.dto.GenerateRequest;
import com.example.contentgenerator.scheduler.SchedulingService;
import com.example.contentgenerator.util.LinkedInUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service layer for handling marketing content generation logic.
 * It orchestrates calls to the AI service and the scheduling service.
 */
@Service
public class MarketingService {

    private static final Logger logger = LoggerFactory.getLogger(MarketingService.class);
    private final GeminiAiService geminiAiService;
    private final SchedulingService schedulingService;
    private final ObjectMapper objectMapper;
    private final LinkedInUtil linkedInUtil;

    public MarketingService(GeminiAiService geminiAiService, SchedulingService schedulingService, ObjectMapper objectMapper, LinkedInUtil linkedInUtil) {
        this.geminiAiService = geminiAiService;
        this.schedulingService = schedulingService;
        this.objectMapper = objectMapper;
        this.linkedInUtil = linkedInUtil;
    }

    /**
     * Generates content based on the request.
     * If a schedule is present, it schedules the content for later posting.
     * Otherwise, it generates and posts immediately.
     *
     * @param request The content generation request.
     * @return A response indicating the result of the operation.
     */
    public Map<String, Object> generateContent(GenerateRequest request, String accessToken) {
        // Ensure return type is Map<String, Object> to handle dynamic AI response
        // Generate content using the AI service
        String generatedJson = geminiAiService.generateContent(request);

        // Determine the timezone to use for response metadata
        ZoneId userZoneId = ZoneOffset.UTC;
        if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
            try {
                userZoneId = ZoneId.of(request.getTimezone());
            } catch (Exception e) {
                logger.warn("Invalid timezone '{}' provided. Falling back to UTC.", request.getTimezone());
                // Fallback to UTC if invalid zone ID is provided
            }
        }

        Map<String, Object> response = new HashMap<>();
        
        // Parse AI response
        try {
            Map<String, Object> aiData = objectMapper.readValue(generatedJson, new TypeReference<Map<String, Object>>() {});
            response.putAll(aiData);
        } catch (Exception e) {
            // Fallback if parsing fails
            response.put("content", generatedJson);
            response.put("error", "Failed to parse AI JSON response");
        }

        // Add System Metadata
        response.put("postId", UUID.randomUUID().toString());
        response.put("requestId", UUID.randomUUID().toString());
        response.put("userId", "user-123"); // Placeholder for authenticated user
        response.put("platform", request.getPlatform());
        response.put("postType", request.getContentType());
        response.put("createdAt", ZonedDateTime.now(userZoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.put("aiModel", "Gemini-1.5-flash");
        
        Map<String, Object> platformOptions = new HashMap<>();
        platformOptions.put("visibility", "PUBLIC");
        platformOptions.put("contentFormat", request.getContentType());
        response.put("platformOptions", platformOptions);

        if (request.getSchedule() != null) {
            response.put("status", "SCHEDULED");
            response.put("schedule", request.getSchedule());
            response.put("postedTime", null);
            
            // Extract content string for scheduling
            String contentText = (String) response.getOrDefault("content", "");
            schedulingService.schedulePost(request, contentText, accessToken);
        } else {
            response.put("status", "POSTED_IMMEDIATELY");
            response.put("postedTime", ZonedDateTime.now(userZoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            
            String contentText = (String) response.getOrDefault("content", "");
            if ("linkedin".equalsIgnoreCase(request.getPlatform())) {
                try {
                    if ("page".equalsIgnoreCase(request.getPostTo())) {
                        linkedInUtil.postToPage(accessToken, contentText, request.getPageId());
                    } else {
                        linkedInUtil.post(accessToken, contentText);
                    }
                } catch (Exception e) {
                    logger.error("Immediate LinkedIn post failed", e);
                    response.put("error", "LinkedIn post failed: " + e.getMessage());
                    response.put("status", "POST_FAILED");
                }
            }
        }
        
        return response;
    }
}
