package ma.estfbs.pfe_management.controller.etudiant;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.EtudiantStatusResponse;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.etudiant.EtudiantStatusService;

@RestController
@RequestMapping("/api/etudiant/status")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EtudiantStatusController {

    private final EtudiantStatusService etudiantStatusService;

    /**
     * Get student's current status (has binome, has sujet, etc.)
     */
    @GetMapping
    public ResponseEntity<EtudiantStatusResponse> getStatus(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(etudiantStatusService.getStatus(utilisateur.getId()));
    }
}