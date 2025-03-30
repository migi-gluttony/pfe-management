package ma.estfbs.pfe_management.controller.chefDeDepartement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.service.chefDeDepartement.SujetManagementService;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetRequestDTOs.*;

@RestController
@RequestMapping("/api/chef_de_departement/sujets")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHEF_DE_DEPARTEMENT')")
public class SujetManagementController {

    private final SujetManagementService sujetManagementService;

    /**
     * Get all subjects and filieres for subject management page
     */
    @GetMapping
    public ResponseEntity<SujetManagementResponse> getAllSujetsAndFilieres() {
        return ResponseEntity.ok(sujetManagementService.getAllSujetsAndFilieres());
    }

    /**
     * Add new subject
     */
    @PostMapping
    public ResponseEntity<SujetDTO> addSujet(@RequestBody SujetAddRequest request) {
        return ResponseEntity.ok(sujetManagementService.addSujet(request));
    }

    /**
     * Edit subject (only title, theme, description)
     */
    @PutMapping("/{id}")
    public ResponseEntity<SujetDTO> editSujet(
            @PathVariable Long id,
            @RequestBody SujetEditRequest request) {
        return ResponseEntity.ok(sujetManagementService.editSujet(id, request));
    }

    /**
     * Delete subject
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSujet(@PathVariable Long id) {
        sujetManagementService.deleteSujet(id);
        return ResponseEntity.ok().build();
    }
}