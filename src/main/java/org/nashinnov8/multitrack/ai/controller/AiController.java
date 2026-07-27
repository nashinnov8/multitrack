package org.nashinnov8.multitrack.ai.controller;

import org.nashinnov8.multitrack.ai.dto.request.FeynmanEvaluationRequest;
import org.nashinnov8.multitrack.ai.dto.response.FeynmanEvaluationResponse;
import org.nashinnov8.multitrack.ai.service.FeynmanAiService;
import org.nashinnov8.multitrack.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final FeynmanAiService feynmanAiService;

    public AiController(FeynmanAiService feynmanAiService) {
        this.feynmanAiService = feynmanAiService;
    }

    @PostMapping("/evaluate-feynman")
    public ResponseEntity<ApiResponse<FeynmanEvaluationResponse>> evaluateFeynman(
            @RequestBody FeynmanEvaluationRequest request) {
        FeynmanEvaluationResponse response = feynmanAiService.evaluate(request);
        return ResponseEntity.ok(new ApiResponse<>("Feynman evaluation generated successfully", response));
    }
}
