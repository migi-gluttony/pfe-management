package ma.estfbs.pfe_management.controller.encadrant;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.encadrant.DocumentEvaluationDTO;
import ma.estfbs.pfe_management.dto.encadrant.DocumentEvaluationRequest;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.encadrant.EncadrantDocumentEvaluationService;

@RestController
@RequestMapping("/api/encadrant/documents-evaluation")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EncadrantDocumentEvaluationController {

    private final EncadrantDocumentEvaluationService documentEvaluationService;

    /**
     * Get all documents from student binomes assigned to the encadrant
     */
    @GetMapping
    public ResponseEntity<List<DocumentEvaluationDTO>> getAllDocuments(Authentication authentication) {
        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();
        List<DocumentEvaluationDTO> documents = documentEvaluationService.getDocumentsByEncadrant(encadrant.getId());
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents for a specific binome
     */
    @GetMapping("/binome/{binomeId}")
    public ResponseEntity<List<DocumentEvaluationDTO>> getDocumentsForBinome(
            @PathVariable Long binomeId,
            Authentication authentication) {

        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();
        List<DocumentEvaluationDTO> documents = documentEvaluationService.getDocumentsByBinome(
                encadrant.getId(), binomeId);

        return ResponseEntity.ok(documents);
    }

    /**
     * Preview a document directly in the browser
     * This endpoint serves the file directly without Content-Disposition:
     * attachment
     */
    @GetMapping("/preview/{documentId}")
    public ResponseEntity<Resource> previewDocument(
            @PathVariable Long documentId,
            Authentication authentication) {

        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();

        try {
            Resource fileResource = documentEvaluationService.getDocumentResource(encadrant.getId(), documentId);
            String contentType = documentEvaluationService.getDocumentContentType(documentId);

            // Set appropriate headers to allow iframe embedding
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            // Explicitly allow embedding in iframes from the same origin
            headers.set("X-Frame-Options", "SAMEORIGIN");
            // Also set Content-Security-Policy to be safe
            headers.set("Content-Security-Policy", "frame-ancestors 'self'");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileResource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download a document
     */
    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            Authentication authentication) {

        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();

        try {
            Resource fileResource = documentEvaluationService.getDocumentResource(encadrant.getId(), documentId);
            String filename = documentEvaluationService.getDocumentFileName(documentId);

            // Use APPLICATION_OCTET_STREAM instead of parsing the content type
            // This ensures consistent behavior with the student controller
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(fileResource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Evaluate a document (add comment)
     */
    @PostMapping("/evaluate/{documentId}")
    public ResponseEntity<?> evaluateDocument(
            @PathVariable Long documentId,
            @RequestBody DocumentEvaluationRequest request,
            Authentication authentication) {

        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();

        try {
            DocumentEvaluationDTO evaluatedDocument = documentEvaluationService.evaluateDocument(
                    encadrant.getId(), documentId, request.getCommentaire());

            return ResponseEntity.ok(evaluatedDocument);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }
}