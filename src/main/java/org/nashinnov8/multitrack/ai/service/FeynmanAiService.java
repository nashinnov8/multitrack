package org.nashinnov8.multitrack.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nashinnov8.multitrack.ai.dto.request.FeynmanEvaluationRequest;
import org.nashinnov8.multitrack.ai.dto.response.FeynmanEvaluationResponse;
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

        // If Gemini API key is provided, call Google Gemini 1.5 Flash AI API
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                return callGeminiApi(request.conceptName(), feynmanText);
            } catch (Exception e) {
                // Fallback to smart heuristic evaluation on error
            }
        }

        // Smart Heuristic Evaluation Fallback
        return evaluateHeuristically(request.conceptName(), feynmanText);
    }

    private FeynmanEvaluationResponse callGeminiApi(String conceptName, String feynmanText) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        String prompt = """
            You are an expert Feynman Technique tutor. Evaluate this student's explanation for topic: "%s".
            Student Explanation: "%s"

            Return ONLY a JSON object with these exact keys:
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

        if (length < 15) {
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

        if (text.toLowerCase().contains("polymorphism") || text.toLowerCase().contains("abstraction") || text.toLowerCase().contains("async/await")) {
            jargonWarning = "Có chứa một số thuật ngữ tiếng Anh chuyên ngành. Bạn có thể giải thích bằng ngôn ngữ đời thường!";
        }

        return new FeynmanEvaluationResponse(score, feedback, jargonWarning, suggestedGap);
    }
}
