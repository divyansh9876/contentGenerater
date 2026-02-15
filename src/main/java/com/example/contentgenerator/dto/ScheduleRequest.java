
package com.example.contentgenerator.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.ZonedDateTime;

/**
 * DTO for scheduling parameters.
 * Contains validation rules for dateTime and frequency.
 */
public class ScheduleRequest {

    @Future(message = "Schedule date must be in the future.")
    private ZonedDateTime dateTime;

    @NotBlank(message = "Frequency is required for scheduled posts.")
    @Pattern(regexp = "once|daily|weekly", message = "Frequency must be one of: once, daily, weekly")
    private String frequency;

    @Pattern(regexp = "individual|page", message = "PostTo must be one of: individual, page")
    private String postTo = "individual"; // Default to individual

    private String pageId;

    // Getters and Setters
    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
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
