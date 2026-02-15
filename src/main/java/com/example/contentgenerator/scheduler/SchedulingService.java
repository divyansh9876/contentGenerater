
package com.example.contentgenerator.scheduler;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.contentgenerator.dto.GenerateRequest;
import com.example.contentgenerator.util.LinkedInUtil;

/**
 * Service for scheduling content posts.
 * Uses an in-memory map to store scheduled tasks.
 * In a production environment, a persistent task queue (e.g., using a database or message broker) is recommended.
 */
@Service
public class SchedulingService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingService.class);
    private final Map<ZonedDateTime, List<ScheduledPost>> scheduledPosts = new ConcurrentHashMap<>();
    private final LinkedInUtil linkedInUtil;

    public SchedulingService(LinkedInUtil linkedInUtil) {
        this.linkedInUtil = linkedInUtil;
    }

    /**
     * Adds a post to the in-memory schedule.
     *
     * @param request The original generation request.
     * @param content The content to be posted.
     */
    public void schedulePost(GenerateRequest request, String content, String accessToken) {
        ZonedDateTime dateTime = request.getSchedule().getDateTime();
        String frequency = request.getSchedule().getFrequency();
        String postTo = request.getSchedule().getPostTo();
        String pageId = request.getSchedule().getPageId();
        ScheduledPost post = new ScheduledPost(content, request.getPlatform(), frequency, accessToken, postTo, pageId);

        scheduledPosts.computeIfAbsent(dateTime, k -> new ArrayList<>()).add(post);
        logger.info("Scheduled post for {} at {}", request.getPlatform(), dateTime);
    }

    /**
     * Runs every minute to check for and execute scheduled posts.
     * This is a simple scheduler; for high precision, a more robust solution might be needed.
     */
    @Scheduled(cron = "0 * * * * *") // Runs at the start of every minute
    public void processScheduledPosts() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        List<ZonedDateTime> timesToProcess = scheduledPosts.keySet().stream()
                .filter(time -> !time.isAfter(now))
                .toList();

        for (ZonedDateTime scheduledTime : timesToProcess) {
            List<ScheduledPost> posts = scheduledPosts.remove(scheduledTime);
            if (posts != null) {
                posts.forEach(post -> {
                    // Post to the specified platform
                    postToPlatform(post.getContent(), post.getPlatform(), post.getAccessToken(), post.getPostTo(), post.getPageId());

                    // Reschedule if recurring
                    reschedulePost(post, scheduledTime);
                });
            }
        }
    }

    private void postToPlatform(String content, String platform, String accessToken, String postTo, String pageId) {
        // Modular design to support multiple platforms
        if ("linkedin".equalsIgnoreCase(platform)) {
            try {
                logger.info("Processing scheduled task: Sending content to LinkedInUtil...");
                if ("page".equalsIgnoreCase(postTo)) {
                    linkedInUtil.postToPage(accessToken, content, pageId);
                } else {
                    linkedInUtil.post(accessToken, content);
                }
            } catch (Exception e) {
                logger.error("Scheduled LinkedIn post failed", e);
            }
        } else {
            logger.warn("Platform '{}' not supported for posting.", platform);
        }
    }

    private void reschedulePost(ScheduledPost post, ZonedDateTime lastExecutionTime) {
        ZonedDateTime nextExecutionTime = null;
        if ("daily".equalsIgnoreCase(post.getFrequency())) {
            nextExecutionTime = lastExecutionTime.plusDays(1);
        } else if ("weekly".equalsIgnoreCase(post.getFrequency())) {
            nextExecutionTime = lastExecutionTime.plusWeeks(1);
        }

        if (nextExecutionTime != null) {
            scheduledPosts.computeIfAbsent(nextExecutionTime, k -> new ArrayList<>()).add(post);
            logger.info("Rescheduled post for {} at {}", post.getPlatform(), nextExecutionTime);
        }
    }

    /**
     * Inner class to hold scheduled post data.
     */
    private static class ScheduledPost {
        private final String content;
        private final String platform;
        private final String frequency;
        private final String accessToken;
        private final String postTo;
        private final String pageId;

        ScheduledPost(String content, String platform, String frequency, String accessToken, String postTo, String pageId) {
            this.content = content;
            this.platform = platform;
            this.frequency = frequency;
            this.accessToken = accessToken;
            this.postTo = postTo;
            this.pageId = pageId;
        }

        public String getContent() {
            return content;
        }

        public String getPlatform() {
            return platform;
        }

        public String getFrequency() {
            return frequency;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getPostTo() {
            return postTo;
        }

        public String getPageId() {
            return pageId;
        }
    }
}
