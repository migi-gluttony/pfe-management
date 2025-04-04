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
    public static class PourcentageEncadrantDTO {
        private Long id;
        private Integer pourcentageTechnical;
        private Integer pourcentageReport;
        private Integer pourcentageProgress;
        private Integer pourcentageProfessionalism;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageEncadrantUpdateRequest {
        private Integer pourcentageTechnical;
        private Integer pourcentageReport;
        private Integer pourcentageProgress;
        private Integer pourcentageProfessionalism;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageRapportDTO {
        private Long id;
        private Integer pourcentageTechnical;
        private Integer pourcentageStructure;
        private Integer pourcentageOriginality;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageRapportUpdateRequest {
        private Integer pourcentageTechnical;
        private Integer pourcentageStructure;
        private Integer pourcentageOriginality;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageSoutenanceDTO {
        private Long id;
        private Integer pourcentagePresentation;
        private Integer pourcentageQa;
        private Integer pourcentageTimeManagement;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageSoutenanceUpdateRequest {
        private Integer pourcentagePresentation;
        private Integer pourcentageQa;
        private Integer pourcentageTimeManagement;
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
        private PourcentageEncadrantDTO pourcentagesEncadrant;
        private PourcentageRapportDTO pourcentagesRapport;
        private PourcentageSoutenanceDTO pourcentagesSoutenance;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AcademicYearsResponse {
        private List<AcademicYearDTO> years;
    }
}