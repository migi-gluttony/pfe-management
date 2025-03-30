package ma.estfbs.pfe_management.controller.chefDeDepartement;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.service.chefDeDepartement.SoutenanceManagementService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.*;

@RestController
@RequestMapping("/api/chef_de_departement/soutenances")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SoutenanceManagementController {

    private final SoutenanceManagementService soutenanceManagementService;

    /**
     * Get all soutenances
     */
    @GetMapping
    public ResponseEntity<List<SoutenanceDTO>> getAllSoutenances() {
        return ResponseEntity.ok(soutenanceManagementService.getAllSoutenances());
    }

    /**
     * Get soutenance by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SoutenanceDTO> getSoutenanceById(@PathVariable Long id) {
        return ResponseEntity.ok(soutenanceManagementService.getSoutenanceById(id));
    }

    /**
     * Schedule a new soutenance
     */
    @PostMapping
    public ResponseEntity<SoutenanceDTO> scheduleSoutenance(@RequestBody SoutenanceAddRequest request) {
        return ResponseEntity.ok(soutenanceManagementService.addSoutenance(request));
    }

    /**
     * Update an existing soutenance
     */
    @PutMapping("/{id}")
    public ResponseEntity<SoutenanceDTO> updateSoutenance(
            @PathVariable Long id,
            @RequestBody SoutenanceUpdateRequest request) {
        return ResponseEntity.ok(soutenanceManagementService.updateSoutenance(id, request));
    }

    /**
     * Delete a soutenance
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoutenance(@PathVariable Long id) {
        soutenanceManagementService.deleteSoutenance(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Validate a soutenance request (dry run)
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateSoutenanceRequest(
            @RequestBody SoutenanceAddRequest request) {
        ValidationResponse response = soutenanceManagementService.validateSoutenanceRequest(request, null);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate a soutenance update request (dry run)
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<ValidationResponse> validateSoutenanceUpdateRequest(
            @PathVariable Long id,
            @RequestBody SoutenanceUpdateRequest request) {
        ValidationResponse response = soutenanceManagementService.validateSoutenanceRequest(request, id);
        return ResponseEntity.ok(response);
    }
}