package ma.estfbs.pfe_management.controller.chefDeDepartement;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.service.chefDeDepartement.BinomeManagementService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.*;

@RestController
@RequestMapping("/api/chef_de_departement/binomes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BinomeManagementController {

    private final BinomeManagementService binomeManagementService;

    /**
     * Get binome management data
     */
    @GetMapping
    public ResponseEntity<BinomeManagementResponse> getBinomeManagementData(
            @RequestParam(required = false) Long filiereId) {
        return ResponseEntity.ok(binomeManagementService.getBinomeManagementData(filiereId));
    }

    /**
     * Add a new binome
     */
    @PostMapping
    public ResponseEntity<BinomeDTO> addBinome(@RequestBody BinomeAddRequest request) {
        return ResponseEntity.ok(binomeManagementService.addBinome(request));
    }

    /**
     * Edit a binome's encadrant
     */
    @PutMapping("/{id}")
    public ResponseEntity<BinomeDTO> editBinome(
            @PathVariable Long id,
            @RequestBody BinomeEditRequest request) {
        return ResponseEntity.ok(binomeManagementService.editBinome(id, request));
    }

    /**
     * Delete a binome
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBinome(@PathVariable Long id) {
        binomeManagementService.deleteBinome(id);
        return ResponseEntity.ok().build();
    }
}