
package com.example.contentgenerator.dto;

/**
 * DTO for the content generation response.
 * Contains the generated content and metadata.
 */
public class GenerateResponse {

    private String generatedContent;
    private String platform;
    private String status;
    private String scheduledTime; // ISO 8601 format

    public GenerateResponse(String generatedContent, String platform, String status, String scheduledTime) {
        this.generatedContent = generatedContent;
        this.platform = platform;
        this.status = status;
        this.scheduledTime = scheduledTime;
    }

    // Getters and Setters
    public String getGeneratedContent() {
        return generatedContent;
    }

    public void setGeneratedContent(String generatedContent) {
        this.generatedContent = generatedContent;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
