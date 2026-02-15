
package com.example.contentgenerator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for the content generation request.
 * Includes validation rules for all fields.
 */
public class GenerateRequest {

    @NotBlank(message = "Business name is required.")
    private String businessName;

    @NotBlank(message = "Industry is required.")
    private String industry;

    @NotBlank(message = "Tone is required.")
    private String tone;

    @NotBlank(message = "Platform is required.")
    private String platform;

    @NotBlank(message = "Use case is required.")
    private String useCase;

    @NotBlank(message = "Content type is required.")
    @Pattern(regexp = "post|comment|image", message = "Content type must be one of: post, comment, image")
    private String contentType;

    private String timezone;

    private String accessToken;

    @Valid
    private ScheduleRequest schedule;

    @Pattern(regexp = "individual|page", message = "PostTo must be one of: individual, page")
    private String postTo = "individual";

    private String pageId;

    // Getters and Setters
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUseCase() {
        return useCase;
    }

    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ScheduleRequest getSchedule() {
        return schedule;
    }
	
    public void setSchedule(ScheduleRequest schedule) {
        this.schedule = schedule;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPostTo() {
        return postTo;
    }

    public void setPostTo(String postTo) {
        this.postTo = postTo;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
}
