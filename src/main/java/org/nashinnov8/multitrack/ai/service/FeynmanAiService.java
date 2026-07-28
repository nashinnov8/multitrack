package org.nashinnov8.multitrack.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nashinnov8.multitrack.ai.dto.request.FeynmanEvaluationRequest;
import org.nashinnov8.multitrack.ai.dto.response.FeynmanEvaluationResponse;
import org.nashinnov8.multitrack.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FeynmanAiService {

    private static final Logger log = LoggerFactory.getLogger(FeynmanAiService.class);

    @Value("${deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String deepseekApiKey;

    @Value("${deepseek.model:${DEEPSEEK_MODEL:deepseek-chat}}")
    private String deepseekModel;

    @Value("${gemini.api-key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${gemini.model:${GEMINI_MODEL:gemini-1.5-flash-latest}}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public FeynmanEvaluationResponse evaluate(FeynmanEvaluationRequest request) {
        String feynmanText = (request.explainSimply() != null ? request.explainSimply() : "")
                + " " + (request.whatLearned() != null ? request.whatLearned() : "")
                + " " + (request.note() != null ? request.note() : "");

        boolean isEn = "en".equalsIgnoreCase(request.lang());
        String langName = isEn ? "English" : "Vietnamese";

        feynmanText = feynmanText.trim();
        if (feynmanText.isEmpty()) {
            throw new BusinessException(isEn
                ? "Explanation is empty. Please enter your explanation before requesting AI evaluation!"
                : "Bài giải thích còn trống. Vui lòng nhập nội dung giải thích trước khi yêu cầu AI chấm điểm!");
        }

        boolean isDeepseekPresent = deepseekApiKey != null && !deepseekApiKey.isBlank();
        boolean isGeminiPresent = apiKey != null && !apiKey.isBlank();

        if (!isDeepseekPresent && !isGeminiPresent) {
            log.warn("[FeynmanAiService] Neither DEEPSEEK_API_KEY nor GEMINI_API_KEY configured.");
            throw new BusinessException(isEn
                ? "AI Evaluation Service is currently unavailable. Please configure DEEPSEEK_API_KEY or GEMINI_API_KEY."
                : "Dịch vụ Trợ lý AI hiện chưa khả dụng. Vui lòng cấu hình DEEPSEEK_API_KEY hoặc GEMINI_API_KEY!");
        }

        log.info("[FeynmanAiService] Evaluating explanation. Lang: {}, Length: {}", langName, feynmanText.length());

        // 1. Try DeepSeek API first if DEEPSEEK_API_KEY is provided
        if (isDeepseekPresent) {
            String maskedDsKey = deepseekApiKey.length() > 6 ? deepseekApiKey.substring(0, 6) + "..." : "SET";
            log.info("[FeynmanAiService] Calling DeepSeek API. Key: {}, Model: {}", maskedDsKey, deepseekModel);
            try {
                return callDeepSeekApi(request.conceptName(), feynmanText, langName);
            } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
                log.error("[FeynmanAiService] DeepSeek API HTTP error status: {}, body: {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("[FeynmanAiService] DeepSeek API call failed with error: {}", e.getMessage(), e);
            }
        }

        // 2. Try Gemini API if GEMINI_API_KEY is provided
        if (isGeminiPresent) {
            String maskedKey = apiKey.length() > 6 ? apiKey.substring(0, 6) + "..." : "SET";
            log.info("[FeynmanAiService] Calling Gemini API. Key: {}, Preferred Model: {}", maskedKey, model);
            try {
                FeynmanEvaluationResponse aiResponse = callGeminiApi(request.conceptName(), feynmanText, langName);
                log.info("[FeynmanAiService] Gemini API call succeeded. Score: {}", aiResponse.score());
                return aiResponse;
            } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
                log.error("[FeynmanAiService] Gemini API HTTP error status: {}, body: {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("[FeynmanAiService] Gemini API call failed with error: {}", e.getMessage(), e);
            }
        }

        // If AI calls fail, throw explicit BusinessException
        throw new BusinessException(isEn
            ? "AI Evaluation Service is temporarily unavailable. Please check your API Key or rate limits."
            : "Dịch vụ Trợ lý AI tạm thời không khả dụng. Vui lòng kiểm tra lại API Key hoặc hạn mức sử dụng!");
    }

    private FeynmanEvaluationResponse callDeepSeekApi(String conceptName, String feynmanText, String langName) throws Exception {
        String url = "https://api.deepseek.com/chat/completions";

        String prompt = """
            You are an expert Feynman Technique tutor. Evaluate this student's explanation for topic: "%s".
            Student Explanation: "%s"

            Critically analyze the explanation in %s:
            1. If the student uses overly complex jargon (hard-to-understand technical buzzwords or acronyms), warn them in 'jargonWarning' in %s and penalize the 'score' (give 5 or 6 out of 10).
            2. If the explanation is simple, clear, and uses real-world analogies, give a high score (8-10).
            3. If the explanation is incorrect or vague, suggest a specific knowledge gap topic in 'suggestedGap' in %s.

            Return ONLY a valid JSON object with these exact keys:
            {
              "score": <integer 1-10 rating simplicity and clarity>,
              "feedback": "<1-2 sentence constructive feedback in %s>",
              "jargonWarning": "<warning in %s if they used overly complex jargon, or empty string if clear>",
              "suggestedGap": "<1 potential knowledge gap topic in %s they should review, or empty string if clear>"
            }
            """.formatted(conceptName != null ? conceptName : "General Topic", feynmanText, langName, langName, langName, langName, langName, langName);

        Map<String, Object> requestBody = Map.of(
            "model", deepseekModel,
            "messages", List.of(
                Map.of("role", "system", "content", "You are an AI assistant that outputs JSON."),
                Map.of("role", "user", "content", prompt)
            ),
            "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepseekApiKey);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        String responseStr = restTemplate.postForObject(url, entity, String.class);

        JsonNode root = objectMapper.readTree(responseStr);
        String jsonText = root.path("choices").get(0).path("message").path("content").asText();

        JsonNode evalJson = objectMapper.readTree(jsonText);

        int score = evalJson.path("score").asInt(8);
        String feedback = evalJson.path("feedback").asText(langName.equals("English") ? "Great explanation! Keep it up." : "Lời giải thích khá tốt, hãy tiếp tục duy trì!");
        String jargonWarning = evalJson.path("jargonWarning").asText("");
        String suggestedGap = evalJson.path("suggestedGap").asText("");

        log.info("[FeynmanAiService] DeepSeek API call succeeded with model '{}'. Score: {}", deepseekModel, score);
        return new FeynmanEvaluationResponse(score, feedback, jargonWarning, suggestedGap);
    }

    private FeynmanEvaluationResponse callGeminiApi(String conceptName, String feynmanText, String langName) throws Exception {
        Set<String> candidateModels = new LinkedHashSet<>();
        candidateModels.add(model);
        candidateModels.add("gemini-1.5-flash-latest");
        candidateModels.add("gemini-2.0-flash-exp");
        candidateModels.add("gemini-2.0-flash");
        candidateModels.add("gemini-1.5-flash");
        candidateModels.add("gemini-1.5-pro-latest");
        candidateModels.add("gemini-1.5-pro");

        Exception lastException = null;

        for (String targetModel : candidateModels) {
            try {
                return tryCallModel(targetModel, conceptName, feynmanText, langName);
            } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
                lastException = httpEx;
                if (httpEx.getStatusCode().value() == 404) {
                    log.warn("[FeynmanAiService] Model '{}' returned 404 NOT_FOUND, trying next candidate model...", targetModel);
                    continue;
                }
                throw httpEx;
            } catch (Exception e) {
                lastException = e;
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new IllegalStateException("Failed to get response from Gemini API");
    }

    private FeynmanEvaluationResponse tryCallModel(String targetModel, String conceptName, String feynmanText, String langName) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + targetModel + ":generateContent?key=" + apiKey;

        String prompt = """
            You are an expert Feynman Technique tutor. Evaluate this student's explanation for topic: "%s".
            Student Explanation: "%s"

            Critically analyze the explanation in %s:
            1. If the student uses overly complex jargon (hard-to-understand technical buzzwords or acronyms), warn them in 'jargonWarning' in %s and penalize the 'score' (give 5 or 6 out of 10).
            2. If the explanation is simple, clear, and uses real-world analogies, give a high score (8-10).
            3. If the explanation is incorrect or vague, suggest a specific knowledge gap topic in 'suggestedGap' in %s.

            Return ONLY a valid JSON object with these exact keys:
            {
              "score": <integer 1-10 rating simplicity and clarity>,
              "feedback": "<1-2 sentence constructive feedback in %s>",
              "jargonWarning": "<warning in %s if they used overly complex jargon, or empty string if clear>",
              "suggestedGap": "<1 potential knowledge gap topic in %s they should review, or empty string if clear>"
            }
            """.formatted(conceptName != null ? conceptName : "General Topic", feynmanText, langName, langName, langName, langName, langName, langName);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            ),
            "generationConfig", Map.of("response_mime_type", "application/json")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        String responseStr = restTemplate.postForObject(url, entity, String.class);

        JsonNode root = objectMapper.readTree(responseStr);
        String jsonText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        JsonNode evalJson = objectMapper.readTree(jsonText);

        int score = evalJson.path("score").asInt(8);
        String feedback = evalJson.path("feedback").asText(langName.equals("English") ? "Great explanation! Keep it up." : "Lời giải thích khá tốt, hãy tiếp tục duy trì!");
        String jargonWarning = evalJson.path("jargonWarning").asText("");
        String suggestedGap = evalJson.path("suggestedGap").asText("");

        log.info("[FeynmanAiService] Successfully called model '{}'", targetModel);
        return new FeynmanEvaluationResponse(score, feedback, jargonWarning, suggestedGap);
    }
}
