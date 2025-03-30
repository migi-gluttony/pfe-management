package ma.estfbs.pfe_management.service.chefDeDepartement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SettingsDTOs.*;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Pourcentage;
import ma.estfbs.pfe_management.repository.AnneeScolaireRepository;
import ma.estfbs.pfe_management.repository.PourcentageRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final PourcentageRepository pourcentageRepository;
    private final AcademicYearService academicYearService;

    /**
     * Get current settings (percentages and academic year)
     */
    public SettingsResponse getSettings() {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        Pourcentage pourcentage = pourcentageRepository.findByAnneeScolaire(currentYear);

        // If no percentages exist for current year, create default ones
        if (pourcentage == null) {
            pourcentage = Pourcentage.builder()
                    .pourcentageRapport(40)
                    .pourcentageSoutenance(40)
                    .pourcentageEncadrant(20)
                    .anneeScolaire(currentYear)
                    .build();
            pourcentage = pourcentageRepository.save(pourcentage);
        }

        return SettingsResponse.builder()
                .currentYear(mapToAcademicYearDTO(currentYear))
                .pourcentages(mapToPourcentageDTO(pourcentage))
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

            // Create default percentages for the new year
            Pourcentage pourcentage = Pourcentage.builder()
                    .pourcentageRapport(40)
                    .pourcentageSoutenance(40)
                    .pourcentageEncadrant(20)
                    .anneeScolaire(newYear)
                    .build();

            pourcentageRepository.save(pourcentage);

            return mapToAcademicYearDTO(newYear);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Format d'année invalide: " + currentYearStr);
        }
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
}