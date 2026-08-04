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

import java.util.List;
import java.util.Map;

@Service
public class FeynmanAiService {

    private static final Logger log = LoggerFactory.getLogger(FeynmanAiService.class);

    @Value("${deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String deepseekApiKey;

    @Value("${deepseek.model:${DEEPSEEK_MODEL:deepseek-chat}}")
    private String deepseekModel;

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

        if (deepseekApiKey == null || deepseekApiKey.isBlank()) {
            log.warn("[FeynmanAiService] DEEPSEEK_API_KEY is not configured.");
            throw new BusinessException(isEn
                ? "AI Evaluation Service is currently unavailable. Please configure DEEPSEEK_API_KEY."
                : "Dịch vụ Trợ lý AI hiện chưa khả dụng. Vui lòng cấu hình DEEPSEEK_API_KEY!");
        }

        log.info("[FeynmanAiService] Evaluating explanation with DeepSeek. Lang: {}, Length: {}", langName, feynmanText.length());

        String maskedDsKey = deepseekApiKey.length() > 6 ? deepseekApiKey.substring(0, 6) + "..." : "SET";
        log.info("[FeynmanAiService] Calling DeepSeek API. Key: {}, Model: {}", maskedDsKey, deepseekModel);

        try {
            return callDeepSeekApi(request.conceptName(), feynmanText, langName);
        } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
            log.error("[FeynmanAiService] DeepSeek API HTTP error status: {}, body: {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
            throw new BusinessException(isEn
                ? "AI Evaluation Service received an error from DeepSeek. Please check your API Key or quota."
                : "Dịch vụ Trợ lý AI nhận được lỗi từ DeepSeek. Vui lòng kiểm tra lại API Key hoặc hạn mức!");
        } catch (Exception e) {
            log.error("[FeynmanAiService] DeepSeek API call failed with error: {}", e.getMessage(), e);
            throw new BusinessException(isEn
                ? "AI Evaluation Service is temporarily unavailable. Please try again later."
                : "Dịch vụ Trợ lý AI tạm thời không khả dụng. Vui lòng thử lại sau!");
        }
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
}
