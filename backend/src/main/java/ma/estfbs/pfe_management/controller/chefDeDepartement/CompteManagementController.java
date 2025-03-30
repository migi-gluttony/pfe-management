package ma.estfbs.pfe_management.controller.chefDeDepartement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.service.chefDeDepartement.CompteManagementService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.CompteManagementDTOs.*;

@RestController
@RequestMapping("/api/chef_de_departement/comptes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CompteManagementController {

    private final CompteManagementService compteManagementService;

    /**
     * Get accounts by role
     */
    @GetMapping
    public ResponseEntity<CompteManagementResponse> getComptesByRole(
            @RequestParam(required = false) Role role) {
        return ResponseEntity.ok(compteManagementService.getComptesByRole(role));
    }

    /**
     * Add a new account
     */
    @PostMapping
    public ResponseEntity<CompteDTO> addCompte(@RequestBody CompteAddRequest request) {
        return ResponseEntity.ok(compteManagementService.addCompte(request));
    }

    /**
     * Edit an account
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompteDTO> editCompte(
            @PathVariable Long id,
            @RequestBody CompteEditRequest request) {
        return ResponseEntity.ok(compteManagementService.editCompte(id, request));
    }

    /**
     * Delete an account
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        compteManagementService.deleteCompte(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Import multiple accounts in batch
     */
    @PostMapping("/import")
    public ResponseEntity<BatchImportResponse> importComptes(@RequestBody BatchImportRequest request) {
        return ResponseEntity.ok(compteManagementService.importComptes(request));
    }
}