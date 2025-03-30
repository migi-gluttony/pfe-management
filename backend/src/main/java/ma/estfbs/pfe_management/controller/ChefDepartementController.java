package ma.estfbs.pfe_management.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.service.chefDeDepartement.BinomeManagementService;
import ma.estfbs.pfe_management.service.chefDeDepartement.CompteManagementService;
import ma.estfbs.pfe_management.service.chefDeDepartement.SujetManagementService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.*;
import ma.estfbs.pfe_management.dto.chefDeDepartement.CompteManagementDTOs.*;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetRequestDTOs.*;
import ma.estfbs.pfe_management.service.chefDeDepartement.SujetSuggestionService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetSuggestionDTO;

@RestController
@RequestMapping("/api/chef_de_departement")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHEF_DE_DEPARTEMENT')")
public class ChefDepartementController {

    private final CompteManagementService compteManagementService;
    private final BinomeManagementService binomeManagementService;
    private final SujetManagementService sujetManagementService;
    private final SujetSuggestionService sujetSuggestionService;
    private final FiliereRepository filiereRepository;

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
    // ============= SUJET MANAGEMENT ENDPOINTS =============

    /**
     * Get all subjects and filieres for subject management page
     */
    @GetMapping("/sujets")
    public ResponseEntity<SujetManagementResponse> getAllSujetsAndFilieres() {
        return ResponseEntity.ok(sujetManagementService.getAllSujetsAndFilieres());
    }

    /**
     * Add new subject
     */
    @PostMapping("/sujets")
    public ResponseEntity<SujetDTO> addSujet(@RequestBody SujetAddRequest request) {
        return ResponseEntity.ok(sujetManagementService.addSujet(request));
    }

    /**
     * Edit subject (only title, theme, description)
     */
    @PutMapping("/sujets/{id}")
    public ResponseEntity<SujetDTO> editSujet(
            @PathVariable Long id,
            @RequestBody SujetEditRequest request) {
        return ResponseEntity.ok(sujetManagementService.editSujet(id, request));
    }

    /**
     * Delete subject
     */
    @DeleteMapping("/sujets/{id}")
    public ResponseEntity<Void> deleteSujet(@PathVariable Long id) {
        sujetManagementService.deleteSujet(id);
        return ResponseEntity.ok().build();
    }

    // ============= SUJET SUGGESTIONS ENDPOINTS =============

    /**
     * Get all sujet suggestions
     */
    @GetMapping("/sujet-suggestions")
    public ResponseEntity<List<SujetSuggestionDTO>> getAllSuggestions() {
        return ResponseEntity.ok(sujetSuggestionService.getAllSuggestions());
    }

    /**
     * Accept a sujet suggestion
     */
    @PostMapping("/sujet-suggestions/{id}/accept")
    public ResponseEntity<Void> acceptSuggestion(@PathVariable Long id) {
        sujetSuggestionService.acceptSuggestion(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject a sujet suggestion
     */
    @PostMapping("/sujet-suggestions/{id}/reject")
    public ResponseEntity<Void> rejectSuggestion(@PathVariable Long id) {
        sujetSuggestionService.rejectSuggestion(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all filieres for dropdown menus
     */
    @GetMapping("/filieres")
    public ResponseEntity<List<FiliereDTO>> getAllFilieres() {
        List<Filiere> filieres = filiereRepository.findAll();
        List<FiliereDTO> filiereDTOs = filieres.stream()
                .map(filiere -> FiliereDTO.builder()
                        .id(filiere.getId())
                        .nom(filiere.getNom())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(filiereDTOs);
    }
}