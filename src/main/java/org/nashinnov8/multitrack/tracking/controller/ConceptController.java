package org.nashinnov8.multitrack.tracking.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.dto.ApiResponse;
import org.nashinnov8.multitrack.tracking.dto.request.ConceptRequest;
import org.nashinnov8.multitrack.tracking.dto.response.ConceptResponse;
import org.nashinnov8.multitrack.tracking.service.ConceptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tracks/{trackId}/concepts")
public class ConceptController {

    private final ConceptService conceptService;

    public ConceptController(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConceptResponse>> createConcept(
            @PathVariable UUID trackId,
            @Valid @RequestBody ConceptRequest request) {
        ConceptResponse concept = conceptService.createConcept(trackId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Concept created successfully", concept));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConceptResponse>>> getConcepts(
            @PathVariable UUID trackId) {
        List<ConceptResponse> concepts = conceptService.getConceptsByTrack(trackId);
        return ResponseEntity.ok(new ApiResponse<>("Concepts retrieved successfully", concepts));
    }

    @PatchMapping("/{conceptId}")
    public ResponseEntity<ApiResponse<ConceptResponse>> updateConcept(
            @PathVariable UUID trackId,
            @PathVariable UUID conceptId,
            @Valid @RequestBody ConceptRequest request) {
        ConceptResponse concept = conceptService.updateConcept(trackId, conceptId, request);
        return ResponseEntity.ok(new ApiResponse<>("Concept updated successfully", concept));
    }

    @DeleteMapping("/{conceptId}")
    public ResponseEntity<ApiResponse<Void>> deleteConcept(
            @PathVariable UUID trackId,
            @PathVariable UUID conceptId) {
        conceptService.deleteConcept(trackId, conceptId);
        return ResponseEntity.ok(new ApiResponse<>("Concept deleted successfully", null));
    }
}
