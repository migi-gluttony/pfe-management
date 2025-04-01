package ma.estfbs.pfe_management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.GradingDtos.*;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.GradingService;

@RestController
@RequestMapping("/api/grading")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;

    /**
     * Submit supervisor (encadrant) evaluation
     * Only accessible by ENCADRANT role
     */
    @PostMapping("/encadrant/evaluation")
    public ResponseEntity<NoteEncadrantResponse> submitEncadrantEvaluation(
            Authentication authentication,
            @RequestBody NoteEncadrantRequest request) {
        
        // Get the authenticated user
        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();
        
        // Check if the user has ENCADRANT role
        if (encadrant.getRole() != Utilisateur.Role.ENCADRANT) {
            return ResponseEntity.status(403).build();
        }
        
        // Submit the evaluation
        NoteEncadrantResponse response = gradingService.submitEncadrantEvaluation(encadrant.getId(), request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Submit report evaluation by a jury member
     * Only accessible by JURY role
     */
    @PostMapping("/jury/rapport-evaluation")
    public ResponseEntity<NoteRapportResponse> submitRapportEvaluation(
            Authentication authentication,
            @RequestBody NoteRapportRequest request) {
        
        // Get the authenticated user
        Utilisateur jury = (Utilisateur) authentication.getPrincipal();
        
        // Check if the user has JURY role
        if (jury.getRole() != Utilisateur.Role.JURY) {
            return ResponseEntity.status(403).build();
        }
        
        // Submit the evaluation
        NoteRapportResponse response = gradingService.submitRapportEvaluation(jury.getId(), request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Submit presentation/defense evaluation by a jury member
     * Only accessible by JURY role
     */
    @PostMapping("/jury/soutenance-evaluation")
    public ResponseEntity<NoteSoutenanceResponse> submitSoutenanceEvaluation(
            Authentication authentication,
            @RequestBody NoteSoutenanceRequest request) {
        
        // Get the authenticated user
        Utilisateur jury = (Utilisateur) authentication.getPrincipal();
        
        // Check if the user has JURY role
        if (jury.getRole() != Utilisateur.Role.JURY) {
            return ResponseEntity.status(403).build();
        }
        
        // Submit the evaluation
        NoteSoutenanceResponse response = gradingService.submitSoutenanceEvaluation(jury.getId(), request);
        
        return ResponseEntity.ok(response);
    }
}