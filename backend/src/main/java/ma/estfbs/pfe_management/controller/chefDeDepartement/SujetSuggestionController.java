package ma.estfbs.pfe_management.controller.chefDeDepartement;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.service.chefDeDepartement.SujetSuggestionService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetSuggestionDTO;

@RestController
@RequestMapping("/api/chef_de_departement/sujet-suggestions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SujetSuggestionController {

    private final SujetSuggestionService sujetSuggestionService;

    /**
     * Get all sujet suggestions
     */
    @GetMapping
    public ResponseEntity<List<SujetSuggestionDTO>> getAllSuggestions() {
        return ResponseEntity.ok(sujetSuggestionService.getAllSuggestions());
    }

    /**
     * Accept a sujet suggestion
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> acceptSuggestion(@PathVariable Long id) {
        sujetSuggestionService.acceptSuggestion(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject a sujet suggestion
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectSuggestion(@PathVariable Long id) {
        sujetSuggestionService.rejectSuggestion(id);
        return ResponseEntity.ok().build();
    }
}