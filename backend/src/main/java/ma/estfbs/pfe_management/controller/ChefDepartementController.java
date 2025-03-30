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
import ma.estfbs.pfe_management.service.chefDeDepartement.SoutenanceManagementService;
import ma.estfbs.pfe_management.service.chefDeDepartement.SujetManagementService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.*;
import ma.estfbs.pfe_management.dto.chefDeDepartement.CompteManagementDTOs.*;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetRequestDTOs.*;
import ma.estfbs.pfe_management.service.chefDeDepartement.SujetSuggestionService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetSuggestionDTO;
import ma.estfbs.pfe_management.service.chefDeDepartement.SoutenanceManagementService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.*;
import ma.estfbs.pfe_management.service.chefDeDepartement.NoteManagementService;
import ma.estfbs.pfe_management.service.chefDeDepartement.SettingsService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.*;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SettingsDTOs;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SettingsDTOs.*;

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
    private final SoutenanceManagementService soutenanceManagementService;
    private final NoteManagementService noteManagementService;
    private final SettingsService settingsService;

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
    public ResponseEntity<ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.BinomeDTO> addBinome(
            @RequestBody BinomeAddRequest request) {
        return ResponseEntity.ok(binomeManagementService.addBinome(request));
    }

    /**
     * Edit a binome's encadrant
     */
    @PutMapping("/binomes/{id}")
    public ResponseEntity<ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.BinomeDTO> editBinome(
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

    // ============= SOUTENANCE MANAGEMENT ENDPOINTS =============

    /**
     * Get all soutenances
     */
    @GetMapping("/soutenances")
    public ResponseEntity<List<SoutenanceDTO>> getAllSoutenances() {
        return ResponseEntity.ok(soutenanceManagementService.getAllSoutenances());
    }

    /**
     * Get soutenance by ID
     */
    @GetMapping("/soutenances/{id}")
    public ResponseEntity<SoutenanceDTO> getSoutenanceById(@PathVariable Long id) {
        return ResponseEntity.ok(soutenanceManagementService.getSoutenanceById(id));
    }

    /**
     * Schedule a new soutenance
     */
    @PostMapping("/soutenances")
    public ResponseEntity<SoutenanceDTO> scheduleSoutenance(@RequestBody SoutenanceAddRequest request) {
        return ResponseEntity.ok(soutenanceManagementService.addSoutenance(request));
    }

    /**
     * Update an existing soutenance
     */
    @PutMapping("/soutenances/{id}")
    public ResponseEntity<SoutenanceDTO> updateSoutenance(
            @PathVariable Long id,
            @RequestBody SoutenanceUpdateRequest request) {
        return ResponseEntity.ok(soutenanceManagementService.updateSoutenance(id, request));
    }

    /**
     * Delete a soutenance
     */
    @DeleteMapping("/soutenances/{id}")
    public ResponseEntity<Void> deleteSoutenance(@PathVariable Long id) {
        soutenanceManagementService.deleteSoutenance(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Validate a soutenance request (dry run)
     */
    @PostMapping("/soutenances/validate")
    public ResponseEntity<ValidationResponse> validateSoutenanceRequest(
            @RequestBody SoutenanceAddRequest request) {
        ValidationResponse response = soutenanceManagementService.validateSoutenanceRequest(request, null);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate a soutenance update request (dry run)
     */
    @PostMapping("/soutenances/{id}/validate")
    public ResponseEntity<ValidationResponse> validateSoutenanceUpdateRequest(
            @PathVariable Long id,
            @RequestBody SoutenanceUpdateRequest request) {
        ValidationResponse response = soutenanceManagementService.validateSoutenanceRequest(request, id);
        return ResponseEntity.ok(response);
    }

    // ============= NOTE MANAGEMENT ENDPOINTS =============

    /**
     * Get all student notes with filieres and pourcentages
     */
    @GetMapping("/notes")
    public ResponseEntity<NoteManagementResponse> getAllNotes() {
        return ResponseEntity.ok(noteManagementService.getAllNotesWithFilieres());
    }

    /**
     * Get notes filtered by filiere
     */
    @GetMapping("/notes/filiere/{filiereId}")
    public ResponseEntity<List<NoteDTO>> getNotesByFiliere(@PathVariable Long filiereId) {
        return ResponseEntity.ok(noteManagementService.getNotesByFiliere(filiereId));
    }

    // ============= SETTINGS ENDPOINTS =============

    /**
     * Get current settings (percentages and academic year)
     */
    @GetMapping("/settings")
    public ResponseEntity<SettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    /**
     * Update percentages
     */
    @PutMapping("/settings/percentages")
    public ResponseEntity<ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.PourcentageDTO> updatePercentages(
            @RequestBody PourcentageUpdateRequest request) {
        SettingsDTOs.PourcentageDTO settingsDTO = settingsService.updatePercentages(request);
        ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.PourcentageDTO noteDTO = new ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.PourcentageDTO();
        noteDTO.setPourcentageRapport(settingsDTO.getPourcentageRapport());
        noteDTO.setPourcentageSoutenance(settingsDTO.getPourcentageSoutenance());
        noteDTO.setPourcentageEncadrant(settingsDTO.getPourcentageEncadrant());

        return ResponseEntity.ok(noteDTO);
    }

    /**
     * Get all academic years
     */
    @GetMapping("/settings/academic-years")
    public ResponseEntity<AcademicYearsResponse> getAllAcademicYears() {
        return ResponseEntity.ok(settingsService.getAllAcademicYears());
    }

    /**
     * Archive current academic year
     */
    @PostMapping("/settings/academic-years/archive")
    public ResponseEntity<AcademicYearDTO> archiveCurrentYear() {
        return ResponseEntity.ok(settingsService.archiveCurrentYear());
    }

    // ============= HELPER ENDPOINTS =============

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

    /**
     * Get all salles for dropdown menus
     */
    @GetMapping("/salles")
    public ResponseEntity<List<SalleDTO>> getAllSalles() {
        return ResponseEntity.ok(soutenanceManagementService.getAllSalles());
    }
}