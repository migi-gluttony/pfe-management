package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RapportDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RapportDTO {
        private Long id;
        private String titre;
        private String localisationRapport;
        private LocalDateTime dateSoumission;
        private Integer note; // Will be null if not graded yet
        private String commentaire; // Will be null if not graded yet
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SoutenanceInfoDTO {
        private Long id;
        private LocalDate date;
        private LocalTime heure;
        private SalleDTO salle;
        private JuryDTO jury1;
        private JuryDTO jury2;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SalleDTO {
        private Long id;
        private String nom;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JuryDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RapportResponseDTO {
        private RapportDTO rapport; // Will be null if no rapport has been uploaded yet
        private SoutenanceInfoDTO soutenance; // Will be null if no soutenance scheduled yet
    }
}
