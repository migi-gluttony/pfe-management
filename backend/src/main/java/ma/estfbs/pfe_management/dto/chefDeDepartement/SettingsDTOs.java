package ma.estfbs.pfe_management.dto.chefDeDepartement;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SettingsDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageDTO {
        private Long id;
        private Integer pourcentageRapport;
        private Integer pourcentageSoutenance;
        private Integer pourcentageEncadrant;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageUpdateRequest {
        private Integer pourcentageRapport;
        private Integer pourcentageSoutenance;
        private Integer pourcentageEncadrant;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AcademicYearDTO {
        private Long id;
        private String annee;
        private Boolean courante;
    }
    
    
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SettingsResponse {
        private AcademicYearDTO currentYear;
        private PourcentageDTO pourcentages;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AcademicYearsResponse {
        private List<AcademicYearDTO> years;
    }
}