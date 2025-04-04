package ma.estfbs.pfe_management.controller.chefDeDepartement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.service.chefDeDepartement.SettingsService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SettingsDTOs.*;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.PourcentageDTO;

@RestController
@RequestMapping("/api/chef_de_departement/settings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    /**
     * Get current settings (percentages and academic year)
     */
    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    /**
     * Update overall percentages
     */
    @PutMapping("/percentages")
    public ResponseEntity<PourcentageDTO> updatePercentages(
            @RequestBody PourcentageUpdateRequest request) {
        ma.estfbs.pfe_management.dto.chefDeDepartement.SettingsDTOs.PourcentageDTO settingsDTO = settingsService
                .updatePercentages(request);

        PourcentageDTO noteDTO = new PourcentageDTO();
        noteDTO.setPourcentageRapport(settingsDTO.getPourcentageRapport());
        noteDTO.setPourcentageSoutenance(settingsDTO.getPourcentageSoutenance());
        noteDTO.setPourcentageEncadrant(settingsDTO.getPourcentageEncadrant());

        return ResponseEntity.ok(noteDTO);
    }

    /**
     * Update encadrant percentages
     */
    @PutMapping("/percentages/encadrant")
    public ResponseEntity<PourcentageEncadrantDTO> updateEncadrantPercentages(
            @RequestBody PourcentageEncadrantUpdateRequest request) {
        return ResponseEntity.ok(settingsService.updateEncadrantPercentages(request));
    }

    /**
     * Update rapport percentages
     */
    @PutMapping("/percentages/rapport")
    public ResponseEntity<PourcentageRapportDTO> updateRapportPercentages(
            @RequestBody PourcentageRapportUpdateRequest request) {
        return ResponseEntity.ok(settingsService.updateRapportPercentages(request));
    }

    /**
     * Update soutenance percentages
     */
    @PutMapping("/percentages/soutenance")
    public ResponseEntity<PourcentageSoutenanceDTO> updateSoutenancePercentages(
            @RequestBody PourcentageSoutenanceUpdateRequest request) {
        return ResponseEntity.ok(settingsService.updateSoutenancePercentages(request));
    }

    /**
     * Get all academic years
     */
    @GetMapping("/academic-years")
    public ResponseEntity<AcademicYearsResponse> getAllAcademicYears() {
        return ResponseEntity.ok(settingsService.getAllAcademicYears());
    }

    /**
     * Archive current academic year
     */
    @PostMapping("/academic-years/archive")
    public ResponseEntity<AcademicYearDTO> archiveCurrentYear() {
        return ResponseEntity.ok(settingsService.archiveCurrentYear());
    }
}