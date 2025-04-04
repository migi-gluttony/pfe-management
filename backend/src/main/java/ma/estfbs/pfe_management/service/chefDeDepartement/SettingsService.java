package ma.estfbs.pfe_management.service.chefDeDepartement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SettingsDTOs.*;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Pourcentage;
import ma.estfbs.pfe_management.model.PourcentageEncadrant;
import ma.estfbs.pfe_management.model.PourcentageRapport;
import ma.estfbs.pfe_management.model.PourcentageSoutenance;
import ma.estfbs.pfe_management.repository.AnneeScolaireRepository;
import ma.estfbs.pfe_management.repository.PourcentageEncadrantRepository;
import ma.estfbs.pfe_management.repository.PourcentageRapportRepository;
import ma.estfbs.pfe_management.repository.PourcentageRepository;
import ma.estfbs.pfe_management.repository.PourcentageSoutenanceRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final PourcentageRepository pourcentageRepository;
    private final PourcentageEncadrantRepository pourcentageEncadrantRepository;
    private final PourcentageRapportRepository pourcentageRapportRepository;
    private final PourcentageSoutenanceRepository pourcentageSoutenanceRepository;
    private final AcademicYearService academicYearService;

    /**
     * Get current settings (percentages and academic year)
     */
    public SettingsResponse getSettings() {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        Pourcentage pourcentage = pourcentageRepository.findByAnneeScolaire(currentYear);
        PourcentageEncadrant pourcentageEncadrant = pourcentageEncadrantRepository.findByAnneeScolaire(currentYear);
        PourcentageRapport pourcentageRapport = pourcentageRapportRepository.findByAnneeScolaire(currentYear);
        PourcentageSoutenance pourcentageSoutenance = pourcentageSoutenanceRepository.findByAnneeScolaire(currentYear);

        // If no percentages exist for current year, create default ones
        if (pourcentage == null) {
            pourcentage = createDefaultPourcentage(currentYear);
        }

        if (pourcentageEncadrant == null) {
            pourcentageEncadrant = createDefaultPourcentageEncadrant(currentYear);
        }

        if (pourcentageRapport == null) {
            pourcentageRapport = createDefaultPourcentageRapport(currentYear);
        }

        if (pourcentageSoutenance == null) {
            pourcentageSoutenance = createDefaultPourcentageSoutenance(currentYear);
        }

        return SettingsResponse.builder()
                .currentYear(mapToAcademicYearDTO(currentYear))
                .pourcentages(mapToPourcentageDTO(pourcentage))
                .pourcentagesEncadrant(mapToPourcentageEncadrantDTO(pourcentageEncadrant))
                .pourcentagesRapport(mapToPourcentageRapportDTO(pourcentageRapport))
                .pourcentagesSoutenance(mapToPourcentageSoutenanceDTO(pourcentageSoutenance))
                .build();
    }

    /**
     * Update percentages for current academic year
     */
    @Transactional
    public PourcentageDTO updatePercentages(PourcentageUpdateRequest request) {
        // Validate the sum is 100%
        if (request.getPourcentageRapport() + request.getPourcentageSoutenance()
                + request.getPourcentageEncadrant() != 100) {
            throw new RuntimeException("La somme des pourcentages doit être égale à 100%");
        }

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        Pourcentage pourcentage = pourcentageRepository.findByAnneeScolaire(currentYear);

        // If no percentages exist for current year, create new ones
        if (pourcentage == null) {
            pourcentage = Pourcentage.builder()
                    .pourcentageRapport(request.getPourcentageRapport())
                    .pourcentageSoutenance(request.getPourcentageSoutenance())
                    .pourcentageEncadrant(request.getPourcentageEncadrant())
                    .anneeScolaire(currentYear)
                    .build();
        } else {
            // Update existing percentages
            pourcentage.setPourcentageRapport(request.getPourcentageRapport());
            pourcentage.setPourcentageSoutenance(request.getPourcentageSoutenance());
            pourcentage.setPourcentageEncadrant(request.getPourcentageEncadrant());
        }

        pourcentage = pourcentageRepository.save(pourcentage);
        return mapToPourcentageDTO(pourcentage);
    }

    /**
     * Update encadrant percentages for current academic year
     */
    @Transactional
    public PourcentageEncadrantDTO updateEncadrantPercentages(PourcentageEncadrantUpdateRequest request) {
        // Validate the sum is 100%
        if (request.getPourcentageTechnical() + request.getPourcentageReport()
                + request.getPourcentageProgress() + request.getPourcentageProfessionalism() != 100) {
            throw new RuntimeException("La somme des pourcentages doit être égale à 100%");
        }

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        PourcentageEncadrant pourcentage = pourcentageEncadrantRepository.findByAnneeScolaire(currentYear);

        // If no percentages exist for current year, create new ones
        if (pourcentage == null) {
            pourcentage = PourcentageEncadrant.builder()
                    .pourcentageTechnical(request.getPourcentageTechnical())
                    .pourcentageReport(request.getPourcentageReport())
                    .pourcentageProgress(request.getPourcentageProgress())
                    .pourcentageProfessionalism(request.getPourcentageProfessionalism())
                    .anneeScolaire(currentYear)
                    .build();
        } else {
            // Update existing percentages
            pourcentage.setPourcentageTechnical(request.getPourcentageTechnical());
            pourcentage.setPourcentageReport(request.getPourcentageReport());
            pourcentage.setPourcentageProgress(request.getPourcentageProgress());
            pourcentage.setPourcentageProfessionalism(request.getPourcentageProfessionalism());
        }

        pourcentage = pourcentageEncadrantRepository.save(pourcentage);
        return mapToPourcentageEncadrantDTO(pourcentage);
    }

    /**
     * Update rapport percentages for current academic year
     */
    @Transactional
    public PourcentageRapportDTO updateRapportPercentages(PourcentageRapportUpdateRequest request) {
        // Validate the sum is 100%
        if (request.getPourcentageTechnical() + request.getPourcentageStructure()
                + request.getPourcentageOriginality() != 100) {
            throw new RuntimeException("La somme des pourcentages doit être égale à 100%");
        }

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        PourcentageRapport pourcentage = pourcentageRapportRepository.findByAnneeScolaire(currentYear);

        // If no percentages exist for current year, create new ones
        if (pourcentage == null) {
            pourcentage = PourcentageRapport.builder()
                    .pourcentageTechnical(request.getPourcentageTechnical())
                    .pourcentageStructure(request.getPourcentageStructure())
                    .pourcentageOriginality(request.getPourcentageOriginality())
                    .anneeScolaire(currentYear)
                    .build();
        } else {
            // Update existing percentages
            pourcentage.setPourcentageTechnical(request.getPourcentageTechnical());
            pourcentage.setPourcentageStructure(request.getPourcentageStructure());
            pourcentage.setPourcentageOriginality(request.getPourcentageOriginality());
        }

        pourcentage = pourcentageRapportRepository.save(pourcentage);
        return mapToPourcentageRapportDTO(pourcentage);
    }

    /**
     * Update soutenance percentages for current academic year
     */
    @Transactional
    public PourcentageSoutenanceDTO updateSoutenancePercentages(PourcentageSoutenanceUpdateRequest request) {
        // Validate the sum is 100%
        if (request.getPourcentagePresentation() + request.getPourcentageQa()
                + request.getPourcentageTimeManagement() != 100) {
            throw new RuntimeException("La somme des pourcentages doit être égale à 100%");
        }

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        PourcentageSoutenance pourcentage = pourcentageSoutenanceRepository.findByAnneeScolaire(currentYear);

        // If no percentages exist for current year, create new ones
        if (pourcentage == null) {
            pourcentage = PourcentageSoutenance.builder()
                    .pourcentagePresentation(request.getPourcentagePresentation())
                    .pourcentageQa(request.getPourcentageQa())
                    .pourcentageTimeManagement(request.getPourcentageTimeManagement())
                    .anneeScolaire(currentYear)
                    .build();
        } else {
            // Update existing percentages
            pourcentage.setPourcentagePresentation(request.getPourcentagePresentation());
            pourcentage.setPourcentageQa(request.getPourcentageQa());
            pourcentage.setPourcentageTimeManagement(request.getPourcentageTimeManagement());
        }

        pourcentage = pourcentageSoutenanceRepository.save(pourcentage);
        return mapToPourcentageSoutenanceDTO(pourcentage);
    }

    /**
     * Get all academic years
     */
    public AcademicYearsResponse getAllAcademicYears() {
        List<AnneeScolaire> years = anneeScolaireRepository.findAll();

        List<AcademicYearDTO> yearDTOs = years.stream()
                .map(this::mapToAcademicYearDTO)
                .collect(Collectors.toList());

        return AcademicYearsResponse.builder()
                .years(yearDTOs)
                .build();
    }

    /**
     * Archive current academic year and create next year
     */
    @Transactional
    public AcademicYearDTO archiveCurrentYear() {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Extract year values from current academic year (format: "YYYY-YYYY")
        String currentYearStr = currentYear.getAnnee();
        String[] yearParts = currentYearStr.split("-");

        if (yearParts.length != 2) {
            throw new RuntimeException("Format d'année invalide: " + currentYearStr);
        }

        try {
            // Parse years and calculate next academic year
            int startYear = Integer.parseInt(yearParts[0]);
            int endYear = Integer.parseInt(yearParts[1]);

            // Create next year string (e.g., "2024-2025")
            String nextYearStr = (startYear + 1) + "-" + (endYear + 1);

            // Archive current year
            currentYear.setCourante(false);
            anneeScolaireRepository.save(currentYear);

            // Create new academic year
            AnneeScolaire newYear = AnneeScolaire.builder()
                    .annee(nextYearStr)
                    .courante(true)
                    .build();

            newYear = anneeScolaireRepository.save(newYear);

            // Get all current percentages
            Pourcentage currentPourcentage = pourcentageRepository.findByAnneeScolaire(currentYear);
            PourcentageEncadrant currentEncadrantPourcentage = pourcentageEncadrantRepository
                    .findByAnneeScolaire(currentYear);
            PourcentageRapport currentRapportPourcentage = pourcentageRapportRepository
                    .findByAnneeScolaire(currentYear);
            PourcentageSoutenance currentSoutenancePourcentage = pourcentageSoutenanceRepository
                    .findByAnneeScolaire(currentYear);

            // Create new percentages based on current ones or defaults
            if (currentPourcentage != null) {
                createNewYearPourcentage(newYear, currentPourcentage);
            } else {
                createDefaultPourcentage(newYear);
            }

            if (currentEncadrantPourcentage != null) {
                createNewYearEncadrantPourcentage(newYear, currentEncadrantPourcentage);
            } else {
                createDefaultPourcentageEncadrant(newYear);
            }

            if (currentRapportPourcentage != null) {
                createNewYearRapportPourcentage(newYear, currentRapportPourcentage);
            } else {
                createDefaultPourcentageRapport(newYear);
            }

            if (currentSoutenancePourcentage != null) {
                createNewYearSoutenancePourcentage(newYear, currentSoutenancePourcentage);
            } else {
                createDefaultPourcentageSoutenance(newYear);
            }

            return mapToAcademicYearDTO(newYear);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Format d'année invalide: " + currentYearStr);
        }
    }

    /**
     * Create a new pourcentage entry for new academic year based on current year
     */
    private Pourcentage createNewYearPourcentage(AnneeScolaire newYear, Pourcentage currentPourcentage) {
        Pourcentage newPourcentage = Pourcentage.builder()
                .pourcentageRapport(currentPourcentage.getPourcentageRapport())
                .pourcentageSoutenance(currentPourcentage.getPourcentageSoutenance())
                .pourcentageEncadrant(currentPourcentage.getPourcentageEncadrant())
                .anneeScolaire(newYear)
                .build();

        return pourcentageRepository.save(newPourcentage);
    }

    /**
     * Create default pourcentage values
     */
    private Pourcentage createDefaultPourcentage(AnneeScolaire year) {
        Pourcentage pourcentage = Pourcentage.builder()
                .pourcentageRapport(40)
                .pourcentageSoutenance(40)
                .pourcentageEncadrant(20)
                .anneeScolaire(year)
                .build();

        return pourcentageRepository.save(pourcentage);
    }

    /**
     * Create a new pourcentage encadrant entry for new academic year based on
     * current year
     */
    private PourcentageEncadrant createNewYearEncadrantPourcentage(AnneeScolaire newYear,
            PourcentageEncadrant currentPourcentage) {
        PourcentageEncadrant newPourcentage = PourcentageEncadrant.builder()
                .pourcentageTechnical(currentPourcentage.getPourcentageTechnical())
                .pourcentageReport(currentPourcentage.getPourcentageReport())
                .pourcentageProgress(currentPourcentage.getPourcentageProgress())
                .pourcentageProfessionalism(currentPourcentage.getPourcentageProfessionalism())
                .anneeScolaire(newYear)
                .build();

        return pourcentageEncadrantRepository.save(newPourcentage);
    }

    /**
     * Create default pourcentage encadrant values
     */
    private PourcentageEncadrant createDefaultPourcentageEncadrant(AnneeScolaire year) {
        PourcentageEncadrant pourcentage = PourcentageEncadrant.builder()
                .pourcentageTechnical(40)
                .pourcentageReport(20)
                .pourcentageProgress(20)
                .pourcentageProfessionalism(20)
                .anneeScolaire(year)
                .build();

        return pourcentageEncadrantRepository.save(pourcentage);
    }

    /**
     * Create a new pourcentage rapport entry for new academic year based on current
     * year
     */
    private PourcentageRapport createNewYearRapportPourcentage(AnneeScolaire newYear,
            PourcentageRapport currentPourcentage) {
        PourcentageRapport newPourcentage = PourcentageRapport.builder()
                .pourcentageTechnical(currentPourcentage.getPourcentageTechnical())
                .pourcentageStructure(currentPourcentage.getPourcentageStructure())
                .pourcentageOriginality(currentPourcentage.getPourcentageOriginality())
                .anneeScolaire(newYear)
                .build();

        return pourcentageRapportRepository.save(newPourcentage);
    }

    /**
     * Create default pourcentage rapport values
     */
    private PourcentageRapport createDefaultPourcentageRapport(AnneeScolaire year) {
        PourcentageRapport pourcentage = PourcentageRapport.builder()
                .pourcentageTechnical(50)
                .pourcentageStructure(30)
                .pourcentageOriginality(20)
                .anneeScolaire(year)
                .build();

        return pourcentageRapportRepository.save(pourcentage);
    }

    /**
     * Create a new pourcentage soutenance entry for new academic year based on
     * current year
     */
    private PourcentageSoutenance createNewYearSoutenancePourcentage(AnneeScolaire newYear,
            PourcentageSoutenance currentPourcentage) {
        PourcentageSoutenance newPourcentage = PourcentageSoutenance.builder()
                .pourcentagePresentation(currentPourcentage.getPourcentagePresentation())
                .pourcentageQa(currentPourcentage.getPourcentageQa())
                .pourcentageTimeManagement(currentPourcentage.getPourcentageTimeManagement())
                .anneeScolaire(newYear)
                .build();

        return pourcentageSoutenanceRepository.save(newPourcentage);
    }

    /**
     * Create default pourcentage soutenance values
     */
    private PourcentageSoutenance createDefaultPourcentageSoutenance(AnneeScolaire year) {
        PourcentageSoutenance pourcentage = PourcentageSoutenance.builder()
                .pourcentagePresentation(50)
                .pourcentageQa(40)
                .pourcentageTimeManagement(10)
                .anneeScolaire(year)
                .build();

        return pourcentageSoutenanceRepository.save(pourcentage);
    }

    /**
     * Map AnneeScolaire entity to AcademicYearDTO
     */
    private AcademicYearDTO mapToAcademicYearDTO(AnneeScolaire anneeScolaire) {
        return AcademicYearDTO.builder()
                .id(anneeScolaire.getId())
                .annee(anneeScolaire.getAnnee())
                .courante(anneeScolaire.getCourante())
                .build();
    }

    /**
     * Map Pourcentage entity to PourcentageDTO
     */
    private PourcentageDTO mapToPourcentageDTO(Pourcentage pourcentage) {
        return PourcentageDTO.builder()
                .id(pourcentage.getId())
                .pourcentageRapport(pourcentage.getPourcentageRapport())
                .pourcentageSoutenance(pourcentage.getPourcentageSoutenance())
                .pourcentageEncadrant(pourcentage.getPourcentageEncadrant())
                .build();
    }

    /**
     * Map PourcentageEncadrant entity to PourcentageEncadrantDTO
     */
    private PourcentageEncadrantDTO mapToPourcentageEncadrantDTO(PourcentageEncadrant pourcentage) {
        return PourcentageEncadrantDTO.builder()
                .id(pourcentage.getId())
                .pourcentageTechnical(pourcentage.getPourcentageTechnical())
                .pourcentageReport(pourcentage.getPourcentageReport())
                .pourcentageProgress(pourcentage.getPourcentageProgress())
                .pourcentageProfessionalism(pourcentage.getPourcentageProfessionalism())
                .build();
    }

    /**
     * Map PourcentageRapport entity to PourcentageRapportDTO
     */
    private PourcentageRapportDTO mapToPourcentageRapportDTO(PourcentageRapport pourcentage) {
        return PourcentageRapportDTO.builder()
                .id(pourcentage.getId())
                .pourcentageTechnical(pourcentage.getPourcentageTechnical())
                .pourcentageStructure(pourcentage.getPourcentageStructure())
                .pourcentageOriginality(pourcentage.getPourcentageOriginality())
                .build();
    }

    /**
     * Map PourcentageSoutenance entity to PourcentageSoutenanceDTO
     */
    private PourcentageSoutenanceDTO mapToPourcentageSoutenanceDTO(PourcentageSoutenance pourcentage) {
        return PourcentageSoutenanceDTO.builder()
                .id(pourcentage.getId())
                .pourcentagePresentation(pourcentage.getPourcentagePresentation())
                .pourcentageQa(pourcentage.getPourcentageQa())
                .pourcentageTimeManagement(pourcentage.getPourcentageTimeManagement())
                .build();
    }
}