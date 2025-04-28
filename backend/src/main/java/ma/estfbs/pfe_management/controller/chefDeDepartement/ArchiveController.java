package ma.estfbs.pfe_management.controller.chefDeDepartement;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.ArchiveDTOs.*;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.BinomeManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.CompteManagementDTOs.CompteManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.NoteManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.SoutenanceDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetManagementResponse;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.service.chefDeDepartement.ArchiveService;

@RestController
@RequestMapping("/api/chef_de_departement/archive")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    /**
     * Get archive metadata (academic years and filieres)
     */
    @GetMapping("/metadata")
    public ResponseEntity<ArchiveResponse> getArchiveMetadata() {
        return ResponseEntity.ok(archiveService.getArchiveMetadata());
    }

    /**
     * Get all academic years
     */
    @GetMapping("/annees")
    public ResponseEntity<List<AcademicYearDTO>> getAllAcademicYears() {
        return ResponseEntity.ok(archiveService.getAllAcademicYears());
    }

    /**
     * Get all filieres
     */
    @GetMapping("/filieres")
    public ResponseEntity<List<FiliereDTO>> getAllFilieres() {
        return ResponseEntity.ok(archiveService.getAllFilieres());
    }

    /**
     * Get accounts by role and academic year
     */
    @GetMapping("/comptes/{compteType}/{anneeScolaireId}")
    public ResponseEntity<CompteManagementResponse> getComptesByTypeAndYear(
            @PathVariable String compteType,
            @PathVariable Long anneeScolaireId) {
        Role role = null;
        if (!compteType.equalsIgnoreCase("all")) {
            try {
                role = Role.valueOf(compteType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Type de compte non valide: " + compteType);
            }
        }
        return ResponseEntity.ok(archiveService.getComptesByRoleAndYear(role, anneeScolaireId));
    }

    /**
     * Get binomes by filiere and academic year
     */
    @GetMapping("/binomes/{filiereId}/{anneeScolaireId}")
    public ResponseEntity<BinomeManagementResponse> getBinomesByFiliereAndYear(
            @PathVariable(required = false) Long filiereId,
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.getBinomeManagementDataByYear(filiereId, anneeScolaireId));
    }
    
    @GetMapping("/binomes/all/{anneeScolaireId}")
    public ResponseEntity<BinomeManagementResponse> getAllBinomesByYear(
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.getBinomeManagementDataByYear(null, anneeScolaireId));
    }

    /**
     * Get subjects by filiere and academic year
     */
    @GetMapping("/sujets/{filiereId}/{anneeScolaireId}")
    public ResponseEntity<SujetManagementResponse> getSujetsByFiliereAndYear(
            @PathVariable Long filiereId,
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.getSujetsByFiliereAndYear(filiereId, anneeScolaireId));
    }
    
    @GetMapping("/sujets/all/{anneeScolaireId}")
    public ResponseEntity<SujetManagementResponse> getAllSujetsByYear(
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.getSujetsByFiliereAndYear(null, anneeScolaireId));
    }

    /**
     * Get soutenances by filiere and academic year
     */
    @GetMapping("/soutenances/{filiereId}/{anneeScolaireId}")
    public ResponseEntity<List<SoutenanceDTO>> getSoutenancesByFiliereAndYear(
            @PathVariable Long filiereId,
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.getSoutenancesByFiliereAndYear(filiereId, anneeScolaireId));
    }
    
    @GetMapping("/soutenances/all/{anneeScolaireId}")
    public ResponseEntity<List<SoutenanceDTO>> getAllSoutenancesByYear(
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.getSoutenancesByFiliereAndYear(null, anneeScolaireId));
    }

    /**
     * Get notes by filiere and academic year
     */
    @GetMapping("/notes/{filiereId}/{anneeScolaireId}")
    public ResponseEntity<NoteManagementResponse> getNotesByFiliereAndYear(
            @PathVariable Long filiereId,
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.getNotesByFiliereAndYear(filiereId, anneeScolaireId));
    }
    
    @GetMapping("/notes/all/{anneeScolaireId}")
    public ResponseEntity<NoteManagementResponse> getAllNotesByYear(
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.getNotesByFiliereAndYear(null, anneeScolaireId));
    }
    
    /**
     * Export all data for a specific academic year
     */
    @GetMapping("/export/{anneeScolaireId}")
    public ResponseEntity<YearDataExportDTO> exportYearData(
            @PathVariable Long anneeScolaireId) {
        return ResponseEntity.ok(archiveService.exportYearData(anneeScolaireId));
    }
}
