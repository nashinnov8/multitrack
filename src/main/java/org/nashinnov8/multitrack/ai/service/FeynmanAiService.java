package org.nashinnov8.multitrack.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nashinnov8.multitrack.ai.dto.request.FeynmanEvaluationRequest;
import org.nashinnov8.multitrack.ai.dto.response.FeynmanEvaluationResponse;
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

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public FeynmanEvaluationResponse evaluate(FeynmanEvaluationRequest request) {
        String feynmanText = (request.explainSimply() != null ? request.explainSimply() : "")
                + " " + (request.whatLearned() != null ? request.whatLearned() : "")
                + " " + (request.note() != null ? request.note() : "");

        feynmanText = feynmanText.trim();
        if (feynmanText.isEmpty()) {
            return new FeynmanEvaluationResponse(
                3,
                "Bài giải thích còn trống. Hãy thử dùng ngôn ngữ đơn giản của riêng bạn để diễn đạt bài học!",
                "",
                ""
            );
        }

        boolean isKeyPresent = apiKey != null && !apiKey.isBlank();
        log.info("[FeynmanAiService] Evaluating explanation length: {}. Gemini API Key configured: {}", feynmanText.length(), isKeyPresent);

        // If Gemini API key is provided, call Google Gemini 1.5 Flash AI API
        if (isKeyPresent) {
            try {
                FeynmanEvaluationResponse aiResponse = callGeminiApi(request.conceptName(), feynmanText);
                log.info("[FeynmanAiService] Gemini API call succeeded. Score: {}", aiResponse.score());
                return aiResponse;
            } catch (Exception e) {
                log.error("[FeynmanAiService] Gemini API call failed with error: {}", e.getMessage(), e);
                // Fallback to smart heuristic evaluation on error
            }
        } else {
            log.warn("[FeynmanAiService] GEMINI_API_KEY is not set on server. Using heuristic evaluation fallback.");
        }

        // Smart Heuristic Evaluation Fallback
        return evaluateHeuristically(request.conceptName(), feynmanText);
    }

    private FeynmanEvaluationResponse callGeminiApi(String conceptName, String feynmanText) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        String prompt = """
            You are an expert Feynman Technique tutor. Evaluate this student's explanation for topic: "%s".
            Student Explanation: "%s"

            Critically analyze the explanation:
            1. If the student uses overly complex jargon (e.g., "asynchronous KMS envelope encryption", "TLS mutual handshake", "OAuth2 grant type"), warn them in 'jargonWarning' in Vietnamese and penalize the 'score' (give 5 or 6 out of 10).
            2. If the explanation is simple, clear, and uses real-world analogies, give a high score (8-10).
            3. If the explanation is incorrect or vague, suggest a specific knowledge gap topic in 'suggestedGap' in Vietnamese.

            Return ONLY a valid JSON object with these exact keys:
            {
              "score": <integer 1-10 rating simplicity and clarity>,
              "feedback": "<1-2 sentence constructive feedback in Vietnamese>",
              "jargonWarning": "<warning in Vietnamese if they used overly complex jargon, or empty string if clear>",
              "suggestedGap": "<1 potential knowledge gap topic in Vietnamese they should review, or empty string if clear>"
            }
            """.formatted(conceptName != null ? conceptName : "General Topic", feynmanText);

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
        String feedback = evalJson.path("feedback").asText("Lời giải thích khá tốt, hãy tiếp tục duy trì!");
        String jargonWarning = evalJson.path("jargonWarning").asText("");
        String suggestedGap = evalJson.path("suggestedGap").asText("");

        return new FeynmanEvaluationResponse(score, feedback, jargonWarning, suggestedGap);
    }

    private FeynmanEvaluationResponse evaluateHeuristically(String conceptName, String text) {
        int length = text.length();
        int score;
        String feedback;
        String jargonWarning = "";
        String suggestedGap = "";

        if (text.toLowerCase().contains("asynchronous") || text.toLowerCase().contains("kms") || text.toLowerCase().contains("tls") || text.toLowerCase().contains("oauth")) {
            score = 5;
            feedback = "Lời giải thích chứa khá nhiều thuật ngữ chuyên ngành phức tạp.";
            jargonWarning = "Từ ngữ như KMS, TLS handshake, OAuth2 quá hàn lâm. Hãy thử giải thích đơn giản bằng ví dụ chiếc két sắt!";
            suggestedGap = "Cách đơn giản hóa các thuật ngữ mã hóa và bảo mật";
        } else if (length < 15) {
            score = 4;
            feedback = "Lời giải thích hơi ngắn! Thử thêm 1 ví dụ thực tế đơn giản để nhớ lâu hơn.";
            suggestedGap = "Cần thêm ví dụ minh họa thực tế cho " + (conceptName != null ? conceptName : "bài học");
        } else if (length < 50) {
            score = 7;
            feedback = "Diễn đạt khá tốt! Lời giải thích tương đối dễ hiểu như đang dạy cho học sinh 12 tuổi.";
        } else {
            score = 9;
            feedback = "Tuyệt vời! Bạn giải thích vô cùng chi tiết, mạch lạc và nắm vững bản chất kiến thức.";
        }

        return new FeynmanEvaluationResponse(score, feedback, jargonWarning, suggestedGap);
    }
}
