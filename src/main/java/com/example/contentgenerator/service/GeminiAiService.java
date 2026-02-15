package com.example.contentgenerator.service;

import com.example.contentgenerator.dto.GenerateRequest;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to interact with the Gemini AI API.
 * Builds the prompt and sends the request to generate content.
 */
@Service
public class GeminiAiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAiService.class);
    private final Client client;

    @Value("${gemini.model.name}")
    private String modelName;

    public GeminiAiService(@Value("${gemini.api.key}") String apiKey) {
        // Initialize the Google Gen AI Client
        this.client = Client.builder().apiKey(apiKey).build();
    }

    /**
     * Calls the Gemini API to generate content.
     *
     * @param request The request containing details for content generation.
     * @return The generated content as a string.
     */
    public String generateContent(GenerateRequest request) {
        String prompt = buildPrompt(request);

        logger.info("Sending request to Gemini API using model: {}", modelName);

        try {
            GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);
            String text = response.text();
            // Clean markdown code blocks more robustly
            return text.replaceAll("(?s)^```(?:json)?\\n?|\\n?```$", "").trim();
        } catch (Exception e) {
            logger.error("Error during Gemini API call", e);
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    /**
     * Builds the prompt for the AI based on the request parameters.
     *
     * @param request The content generation request.
     * @return A formatted prompt string.
     */
    private String buildPrompt(GenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a marketing ").append(request.getContentType());
        prompt.append(" for ").append(request.getPlatform()).append(". ");
        prompt.append("The business is '" ).append(request.getBusinessName());
        prompt.append("' in the '" ).append(request.getIndustry()).append("' industry. ");
        prompt.append("The desired tone is '" ).append(request.getTone()).append("'. ");
        prompt.append("The use case is '" ).append(request.getUseCase()).append("'. ");

        prompt.append("\n\nIMPORTANT: Return the result strictly as a valid JSON object. ");
        prompt.append("Do not include any markdown formatting, backticks, or explanations outside the JSON. ");
        prompt.append("The JSON object must have the following fields:\n");
        prompt.append("- headline (string)\n");
        prompt.append("- content (string: the main body text)\n");
        prompt.append("- tagline (string)\n");
        prompt.append("- hashtags (array of strings)\n");
        prompt.append("- mentions (array of strings)\n");
        prompt.append("- aiScore (integer 0-100)\n");
        prompt.append("- predictedEngagement (object with fields: likes, comments, shares)\n");

        return prompt.toString();
    }
}
