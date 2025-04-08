package ma.estfbs.pfe_management.controller.etudiant;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.DocumentDTO;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.etudiant.DocumentsEvaluationService;

@RestController
@RequestMapping("/api/etudiant/documents-evaluation")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DocumentsEvaluationController {

    private final DocumentsEvaluationService documentsEvaluationService;

    /**
     * Get all evaluation documents for the current student
     */
    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAllDocuments(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        List<DocumentDTO> documents = documentsEvaluationService.getDocumentsByStudent(utilisateur.getId());
        return ResponseEntity.ok(documents);
    }

    /**
     * Upload a new document for evaluation
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre,
            Authentication authentication) {
        
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        
        try {
            DocumentDTO uploadedDocument = documentsEvaluationService.uploadDocument(
                utilisateur.getId(), 
                titre, 
                file
            );
            
            return ResponseEntity.ok(uploadedDocument);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Erreur lors du téléchargement du fichier: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * Download a document
     */
    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        
        try {
            Resource fileResource = documentsEvaluationService.getDocumentResource(utilisateur.getId(), documentId);
            String filename = documentsEvaluationService.getDocumentFileName(documentId);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(fileResource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a document (only if not reviewed yet)
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        
        try {
            documentsEvaluationService.deleteDocument(utilisateur.getId(), documentId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Document supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }
}
