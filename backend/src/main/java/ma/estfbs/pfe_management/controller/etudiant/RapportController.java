package ma.estfbs.pfe_management.controller.etudiant;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.RapportDTOs.*;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.etudiant.RapportService;

@RestController
@RequestMapping("/api/etudiant/rapport")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    /**
     * Get rapport information for the current student
     */
    @GetMapping
    public ResponseEntity<RapportResponseDTO> getRapport(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        RapportResponseDTO response = rapportService.getRapportInfo(utilisateur.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Upload or replace rapport
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadRapport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre,
            Authentication authentication) {
        
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        
        try {
            RapportDTO uploadedRapport = rapportService.uploadRapport(
                utilisateur.getId(), 
                titre, 
                file
            );
            
            return ResponseEntity.ok(uploadedRapport);
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
     * Download rapport
     */
    @GetMapping("/download/{rapportId}")
    public ResponseEntity<Resource> downloadRapport(
            @PathVariable Long rapportId,
            Authentication authentication) {
        
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        
        try {
            Resource fileResource = rapportService.getRapportResource(utilisateur.getId(), rapportId);
            String filename = rapportService.getRapportFileName(rapportId);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(fileResource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete rapport (only if soutenance date is more than 3 days away)
     */
    @DeleteMapping
    public ResponseEntity<?> deleteRapport(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        
        try {
            rapportService.deleteRapport(utilisateur.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Rapport supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * Check if student can upload/modify rapport (soutenance date > 3 days)
     */
    @GetMapping("/can-upload")
    public ResponseEntity<Map<String, Boolean>> canUploadRapport(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        boolean canUpload = rapportService.canUploadRapport(utilisateur.getId());
        return ResponseEntity.ok(Map.of("canUpload", canUpload));
    }
}
