package ma.estfbs.pfe_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.service.chefDeDepartement.BinomeManagementService;
import ma.estfbs.pfe_management.service.chefDeDepartement.CompteManagementService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.*;
import ma.estfbs.pfe_management.dto.chefDeDepartement.CompteManagementDTOs.*;

@RestController
@RequestMapping("/api/chef_de_departement")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHEF_DE_DEPARTEMENT')")
public class ChefDepartementController {

    private final CompteManagementService compteManagementService;
    private final BinomeManagementService binomeManagementService;

    // ============= COMPTE MANAGEMENT ENDPOINTS =============

    /**
     * Get accounts by role
     */
    @GetMapping("/comptes")
    public ResponseEntity<CompteManagementResponse> getComptesByRole(
            @RequestParam(required = false) Role role) {
        return ResponseEntity.ok(compteManagementService.getComptesByRole(role));
    }

    /**
     * Add a new account
     */
    @PostMapping("/comptes")
    public ResponseEntity<CompteDTO> addCompte(@RequestBody CompteAddRequest request) {
        return ResponseEntity.ok(compteManagementService.addCompte(request));
    }

    /**
     * Edit an account
     */
    @PutMapping("/comptes/{id}")
    public ResponseEntity<CompteDTO> editCompte(
            @PathVariable Long id,
            @RequestBody CompteEditRequest request) {
        return ResponseEntity.ok(compteManagementService.editCompte(id, request));
    }

    /**
     * Delete an account
     */
    @DeleteMapping("/comptes/{id}")
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        compteManagementService.deleteCompte(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Import multiple accounts in batch
     */
    @PostMapping("/comptes/import")
    public ResponseEntity<BatchImportResponse> importComptes(@RequestBody BatchImportRequest request) {
        return ResponseEntity.ok(compteManagementService.importComptes(request));
    }

    // ============= BINOME MANAGEMENT ENDPOINTS =============

    /**
     * Get binome management data
     */
    @GetMapping("/binomes")
    public ResponseEntity<BinomeManagementResponse> getBinomeManagementData(
            @RequestParam(required = false) Long filiereId) {
        return ResponseEntity.ok(binomeManagementService.getBinomeManagementData(filiereId));
    }

    /**
     * Add a new binome
     */
    @PostMapping("/binomes")
    public ResponseEntity<BinomeDTO> addBinome(@RequestBody BinomeAddRequest request) {
        return ResponseEntity.ok(binomeManagementService.addBinome(request));
    }

    /**
     * Edit a binome's encadrant
     */
    @PutMapping("/binomes/{id}")
    public ResponseEntity<BinomeDTO> editBinome(
            @PathVariable Long id,
            @RequestBody BinomeEditRequest request) {
        return ResponseEntity.ok(binomeManagementService.editBinome(id, request));
    }

    /**
     * Delete a binome
     */
    @DeleteMapping("/binomes/{id}")
    public ResponseEntity<Void> deleteBinome(@PathVariable Long id) {
        binomeManagementService.deleteBinome(id);
        return ResponseEntity.ok().build();
    }

}