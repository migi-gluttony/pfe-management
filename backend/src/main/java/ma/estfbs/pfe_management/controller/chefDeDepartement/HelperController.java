package ma.estfbs.pfe_management.controller.chefDeDepartement;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.service.chefDeDepartement.SoutenanceManagementService;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.SalleDTO;

@RestController
@RequestMapping("/api/chef_de_departement")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class HelperController {

    private final FiliereRepository filiereRepository;
    private final SoutenanceManagementService soutenanceManagementService;

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