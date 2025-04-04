package ma.estfbs.pfe_management.controller.etudiant;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.dto.etudiant.SujetSelectionResponse;
import ma.estfbs.pfe_management.dto.etudiant.SujetSuggestionRequest;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.etudiant.SujetSelectionService;

@RestController
@RequestMapping("/api/etudiant/sujet")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SujetSelectionController {

    private final SujetSelectionService sujetSelectionService;

    /**
     * Get all available sujets for the student's filiere
     */
    @GetMapping("/available")
    public ResponseEntity<SujetSelectionResponse> getAvailableSujets(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(sujetSelectionService.getAvailableSujets(utilisateur.getId()));
    }

    /**
     * Select a specific sujet
     */
    @PostMapping("/select/{sujetId}")
    public ResponseEntity<SujetDTO> selectSujet(
            @PathVariable Long sujetId, 
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(sujetSelectionService.selectSujet(utilisateur.getId(), sujetId));
    }

    /**
     * Get a random sujet assignment
     */
    @PostMapping("/random")
    public ResponseEntity<SujetDTO> getRandomSujet(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(sujetSelectionService.assignRandomSujet(utilisateur.getId()));
    }

    /**
     * Suggest a new sujet
     */
    @PostMapping("/suggest")
    public ResponseEntity<Void> suggestSujet(
            @RequestBody SujetSuggestionRequest suggestion, 
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        sujetSelectionService.suggestSujet(utilisateur.getId(), suggestion);
        return ResponseEntity.ok().build();
    }

    /**
     * Check if student has a pending suggestion
     */
    @GetMapping("/suggestion-status")
    public ResponseEntity<Boolean> hasPendingSuggestion(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(sujetSelectionService.hasPendingSuggestion(utilisateur.getId()));
    }
}