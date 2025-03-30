package ma.estfbs.pfe_management.service.chefDeDepartement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.*;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Salle;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.SalleRepository;
import ma.estfbs.pfe_management.repository.SoutenanceRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

@Service
@RequiredArgsConstructor
public class SoutenanceManagementService {

    private final SoutenanceRepository soutenanceRepository;
    private final BinomeRepository binomeRepository;
    private final SalleRepository salleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final AcademicYearService academicYearService;
    
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Get all soutenances for current academic year
     */
    public List<SoutenanceDTO> getAllSoutenances() {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        return soutenanceRepository.findByAnneeScolaire(currentYear).stream()
                .map(this::mapToSoutenanceDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all salles
     */
    public List<SalleDTO> getAllSalles() {
        return salleRepository.findAll().stream()
                .map(this::mapToSalleDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all salles with response wrapper
     */
    public SalleManagementResponse getSalleManagementData() {
        List<SalleDTO> salles = getAllSalles();
        return SalleManagementResponse.builder()
                .salles(salles)
                .build();
    }
    
    /**
     * Add a new salle
     */
    @Transactional
    public SalleDTO addSalle(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new RuntimeException("Le nom de la salle est obligatoire");
        }
        
        if (salleRepository.existsByNom(nom.trim())) {
            throw new RuntimeException("Une salle avec ce nom existe déjà");
        }
        
        Salle salle = Salle.builder()
                .nom(nom.trim())
                .build();
        
        salle = salleRepository.save(salle);
        return mapToSalleDTO(salle);
    }
    
    /**
     * Edit a salle
     */
    @Transactional
    public SalleDTO editSalle(Long id, String nom) {
        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'id: " + id));
        
        if (nom == null || nom.trim().isEmpty()) {
            throw new RuntimeException("Le nom de la salle est obligatoire");
        }
        
        // Check if the new name already exists and belongs to a different salle
        if (!salle.getNom().equals(nom.trim()) && salleRepository.existsByNom(nom.trim())) {
            throw new RuntimeException("Une salle avec ce nom existe déjà");
        }
        
        salle.setNom(nom.trim());
        salle = salleRepository.save(salle);
        return mapToSalleDTO(salle);
    }
    
    /**
     * Delete a salle
     */
    @Transactional
    public void deleteSalle(Long id) {
        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'id: " + id));
        
        // Check if any soutenance is using this salle in the current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        List<Soutenance> soutenancesUsingSalle = soutenanceRepository.findBySalleAndAnneeScolaire(salle, currentYear);
        
        if (!soutenancesUsingSalle.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer cette salle car elle est utilisée par une ou plusieurs soutenances pour l'année courante");
        }
        
        salleRepository.delete(salle);
    }
    
    /**
     * Get soutenance by ID
     */
    public SoutenanceDTO getSoutenanceById(Long id) {
        Soutenance soutenance = soutenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soutenance non trouvée avec l'id: " + id));
        
        // Check if soutenance is from current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        if (!soutenance.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Cette soutenance n'appartient pas à l'année scolaire courante");
        }
        
        return mapToSoutenanceDTO(soutenance);
    }
    
    /**
     * Add a new soutenance for the current academic year
     */
    @Transactional
    public SoutenanceDTO addSoutenance(SoutenanceAddRequest request) {
        // Validate the request
        ValidationResponse validation = validateSoutenanceRequest(request, null);
        if (!validation.isValid()) {
            throw new RuntimeException(buildErrorMessage(validation.getErrors()));
        }
        
        // Get entities
        Binome binome = binomeRepository.findById(request.getBinomeId())
                .orElseThrow(() -> new RuntimeException("Binôme non trouvé avec l'id: " + request.getBinomeId()));
        
        Salle salle = salleRepository.findById(request.getSalleId())
                .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'id: " + request.getSalleId()));
        
        Utilisateur jury1 = utilisateurRepository.findById(request.getJury1Id())
                .orElseThrow(() -> new RuntimeException("Jury 1 non trouvé avec l'id: " + request.getJury1Id()));
        
        Utilisateur jury2 = utilisateurRepository.findById(request.getJury2Id())
                .orElseThrow(() -> new RuntimeException("Jury 2 non trouvé avec l'id: " + request.getJury2Id()));
        
        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Parse time
        LocalTime heure = LocalTime.parse(request.getHeure(), timeFormatter);
        
        // Create and save soutenance
        Soutenance soutenance = Soutenance.builder()
                .date(request.getDate())
                .heure(heure)
                .salle(salle)
                .binome(binome)
                .jury1(jury1)
                .jury2(jury2)
                .anneeScolaire(currentYear)
                .build();
        
        soutenance = soutenanceRepository.save(soutenance);
        
        return mapToSoutenanceDTO(soutenance);
    }
    
    /**
     * Update an existing soutenance
     */
    @Transactional
    public SoutenanceDTO updateSoutenance(Long id, SoutenanceUpdateRequest request) {
        Soutenance soutenance = soutenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soutenance non trouvée avec l'id: " + id));
        
        // Check if soutenance is from current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        if (!soutenance.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Impossible de modifier une soutenance d'une année précédente");
        }
        
        // Validate the request
        ValidationResponse validation = validateSoutenanceRequest(request, id);
        if (!validation.isValid()) {
            throw new RuntimeException(buildErrorMessage(validation.getErrors()));
        }
        
        // Get entities
        Binome binome = binomeRepository.findById(request.getBinomeId())
                .orElseThrow(() -> new RuntimeException("Binôme non trouvé avec l'id: " + request.getBinomeId()));
        
        Salle salle = salleRepository.findById(request.getSalleId())
                .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'id: " + request.getSalleId()));
        
        Utilisateur jury1 = utilisateurRepository.findById(request.getJury1Id())
                .orElseThrow(() -> new RuntimeException("Jury 1 non trouvé avec l'id: " + request.getJury1Id()));
        
        Utilisateur jury2 = utilisateurRepository.findById(request.getJury2Id())
                .orElseThrow(() -> new RuntimeException("Jury 2 non trouvé avec l'id: " + request.getJury2Id()));
        
        // Parse time
        LocalTime heure = LocalTime.parse(request.getHeure(), timeFormatter);
        
        // Update soutenance (keep the same academic year)
        soutenance.setDate(request.getDate());
        soutenance.setHeure(heure);
        soutenance.setSalle(salle);
        soutenance.setBinome(binome);
        soutenance.setJury1(jury1);
        soutenance.setJury2(jury2);
        
        soutenance = soutenanceRepository.save(soutenance);
        
        return mapToSoutenanceDTO(soutenance);
    }
    
    /**
     * Delete a soutenance
     */
    @Transactional
    public void deleteSoutenance(Long id) {
        Soutenance soutenance = soutenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soutenance non trouvée avec l'id: " + id));
                
        // Check if soutenance is from current year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        if (!soutenance.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Impossible de supprimer une soutenance d'une année précédente");
        }
        
        soutenanceRepository.deleteById(id);
    }
    
    /**
     * Validate a soutenance request to prevent conflicts
     */
    public ValidationResponse validateSoutenanceRequest(Object requestObj, Long soutenanceId) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Extract common fields from request (which can be either AddRequest or UpdateRequest)
        LocalDate date;
        String heureStr;
        Long salleId, binomeId, jury1Id, jury2Id;
        
        if (requestObj instanceof SoutenanceAddRequest) {
            SoutenanceAddRequest request = (SoutenanceAddRequest) requestObj;
            date = request.getDate();
            heureStr = request.getHeure();
            salleId = request.getSalleId();
            binomeId = request.getBinomeId();
            jury1Id = request.getJury1Id();
            jury2Id = request.getJury2Id();
        } else if (requestObj instanceof SoutenanceUpdateRequest) {
            SoutenanceUpdateRequest request = (SoutenanceUpdateRequest) requestObj;
            date = request.getDate();
            heureStr = request.getHeure();
            salleId = request.getSalleId();
            binomeId = request.getBinomeId();
            jury1Id = request.getJury1Id();
            jury2Id = request.getJury2Id();
        } else {
            throw new IllegalArgumentException("Request object must be either SoutenanceAddRequest or SoutenanceUpdateRequest");
        }
        
        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Validate required fields
        if (date == null) {
            errors.add(new ValidationError("date", "La date est obligatoire"));
        } else if (date.isBefore(LocalDate.now())) {
            errors.add(new ValidationError("date", "La date ne peut pas être antérieure à aujourd'hui"));
        }
        
        if (heureStr == null || heureStr.isEmpty()) {
            errors.add(new ValidationError("heure", "L'heure est obligatoire"));
        }
        
        LocalTime heure = null;
        if (heureStr != null && !heureStr.isEmpty()) {
            try {
                heure = LocalTime.parse(heureStr, timeFormatter);
            } catch (Exception e) {
                errors.add(new ValidationError("heure", "Format d'heure invalide. Utilisez HH:MM"));
            }
        }
        
        if (salleId == null) {
            errors.add(new ValidationError("salleId", "La salle est obligatoire"));
        }
        
        if (binomeId == null) {
            errors.add(new ValidationError("binomeId", "Le binôme est obligatoire"));
        } else {
            // Check if binome is from current year
            Binome binome = binomeRepository.findById(binomeId).orElse(null);
            if (binome != null && !binome.getAnneeScolaire().getId().equals(currentYear.getId())) {
                errors.add(new ValidationError("binomeId", "Ce binôme n'appartient pas à l'année scolaire courante"));
            }
        }
        
        if (jury1Id == null) {
            errors.add(new ValidationError("jury1Id", "Le premier membre du jury est obligatoire"));
        }
        
        if (jury2Id == null) {
            errors.add(new ValidationError("jury2Id", "Le second membre du jury est obligatoire"));
        }
        
        // Check if jury1 and jury2 are the same
        if (jury1Id != null && jury2Id != null && jury1Id.equals(jury2Id)) {
            errors.add(new ValidationError("jury2Id", "Les deux membres du jury doivent être différents"));
        }
        
        // If there are basic validation errors, return them now
        if (!errors.isEmpty()) {
            return ValidationResponse.builder()
                    .valid(false)
                    .errors(errors)
                    .build();
        }
        
        // Check for scheduling conflicts within current year
        if (date != null && heure != null) {
            // Get all soutenances on the same date and time for current year
            List<Soutenance> existingSoutenances = soutenanceRepository.findByDateAndHeureAndAnneeScolaire(date, heure, currentYear);
            
            // Filter out the current soutenance if we're updating
            if (soutenanceId != null) {
                existingSoutenances = existingSoutenances.stream()
                        .filter(s -> !s.getId().equals(soutenanceId))
                        .collect(Collectors.toList());
            }
            
            // Check for salle conflict
            if (salleId != null) {
                boolean salleConflict = existingSoutenances.stream()
                        .anyMatch(s -> s.getSalle().getId().equals(salleId));
                
                if (salleConflict) {
                    errors.add(new ValidationError("salleId", 
                            "Cette salle est déjà réservée à cette date et heure"));
                }
            }
            
            // Check for jury conflicts
            if (jury1Id != null) {
                boolean jury1Conflict = existingSoutenances.stream()
                        .anyMatch(s -> s.getJury1().getId().equals(jury1Id) || s.getJury2().getId().equals(jury1Id));
                
                if (jury1Conflict) {
                    errors.add(new ValidationError("jury1Id", 
                            "Ce membre du jury est déjà assigné à une autre soutenance à cette date et heure"));
                }
            }
            
            if (jury2Id != null) {
                boolean jury2Conflict = existingSoutenances.stream()
                        .anyMatch(s -> s.getJury1().getId().equals(jury2Id) || s.getJury2().getId().equals(jury2Id));
                
                if (jury2Conflict) {
                    errors.add(new ValidationError("jury2Id", 
                            "Ce membre du jury est déjà assigné à une autre soutenance à cette date et heure"));
                }
            }
            
            // Check for binome conflict (a binome can only have one soutenance in current year)
            if (binomeId != null && soutenanceId == null) { // Only for new soutenances
                Optional<Soutenance> existingSoutenance = soutenanceRepository.findByBinomeIdAndAnneeScolaireId(binomeId, currentYear.getId());
                if (existingSoutenance.isPresent()) {
                    errors.add(new ValidationError("binomeId", 
                            "Ce binôme a déjà une soutenance programmée pour l'année courante"));
                }
            }
        }
        
        return ValidationResponse.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .build();
    }
    
    // Update these mapper methods in SoutenanceManagementService.java to handle null values
  /**
     * Map Soutenance entity to SoutenanceDTO
     */
    private SoutenanceDTO mapToSoutenanceDTO(Soutenance soutenance) {
        return SoutenanceDTO.builder()
                .id(soutenance.getId())
                .date(soutenance.getDate())
                .heure(soutenance.getHeure())
                .salle(mapToSalleDTO(soutenance.getSalle()))
                .binome(mapToBinomeDTO(soutenance.getBinome()))
                .jury1(mapToJuryDTO(soutenance.getJury1()))
                .jury2(mapToJuryDTO(soutenance.getJury2()))
                .build();
    }
/**
 * Map Binome entity to BinomeDTO
 */
private BinomeDTO mapToBinomeDTO(Binome binome) {
    if (binome == null) {
        return null;
    }
    
    String filiereName = null;
    
    // Get filiere name from etudiant1
    if (binome.getEtudiant1() != null) {
        Etudiant etudiant1 = etudiantRepository.findByUtilisateur(binome.getEtudiant1())
                .orElse(null);
        if (etudiant1 != null && etudiant1.getFiliere() != null) {
            filiereName = etudiant1.getFiliere().getNom();
        }
    }
    
    return BinomeDTO.builder()
            .id(binome.getId())
            .etudiant1(binome.getEtudiant1() != null ? mapToStudentDTO(binome.getEtudiant1()) : null)
            .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null)
            .encadrant(binome.getEncadrant() != null ? mapToEncadrantDTO(binome.getEncadrant()) : null)
            .sujet(binome.getSujet() != null ? mapToSujetShortDTO(binome.getSujet()) : null)
            .filiereName(filiereName)
            .build();
}

/**
 * Map Utilisateur entity to StudentDTO
 */
private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
    if (utilisateur == null) {
        return null;
    }
    
    return StudentDTO.builder()
            .id(utilisateur.getId())
            .nom(utilisateur.getNom())
            .prenom(utilisateur.getPrenom())
            .build();
}

/**
 * Map Utilisateur entity to EncadrantDTO
 */
private EncadrantDTO mapToEncadrantDTO(Utilisateur utilisateur) {
    if (utilisateur == null) {
        return null;
    }
    
    return EncadrantDTO.builder()
            .id(utilisateur.getId())
            .nom(utilisateur.getNom())
            .prenom(utilisateur.getPrenom())
            .build();
}

/**
 * Map Utilisateur entity to JuryDTO
 */
private JuryDTO mapToJuryDTO(Utilisateur utilisateur) {
    if (utilisateur == null) {
        return null;
    }
    
    return JuryDTO.builder()
            .id(utilisateur.getId())
            .nom(utilisateur.getNom())
            .prenom(utilisateur.getPrenom())
            .build();
}

/**
 * Map Salle entity to SalleDTO
 */
private SalleDTO mapToSalleDTO(Salle salle) {
    if (salle == null) {
        return null;
    }
    
    return SalleDTO.builder()
            .id(salle.getId())
            .nom(salle.getNom())
            .build();
}

/**
 * Map Sujet entity to SujetShortDTO
 */
private SujetShortDTO mapToSujetShortDTO(Sujet sujet) {
    if (sujet == null) {
        return null;
    }
    
    return SujetShortDTO.builder()
            .id(sujet.getId())
            .titre(sujet.getTitre())
            .build();
}
    
    /**
     * Build error message from validation errors
     */
    private String buildErrorMessage(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Validation error";
        }
        
        return errors.stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.joining(", "));
    }
}