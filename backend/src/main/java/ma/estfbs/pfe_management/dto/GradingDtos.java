package ma.estfbs.pfe_management.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GradingDtos {
    /**
     * Request DTO for supervisor evaluation
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteEncadrantRequest {
        private Long binomeId;
        private Integer technicalScore; // 0-20 score for technical skills
        private Integer reportScore; // 0-20 score for report quality
        private Integer progressScore; // 0-20 score for progress tracking
        private Integer professionalismScore; // 0-20 score for professionalism
        private String commentaire; // Optional feedback
    }

    /**
     * Response DTO for supervisor evaluation
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteEncadrantResponse {
        private Long id;
        private Long binomeId;
        private Long encadrantId;
        private Integer technicalScore;
        private Integer reportScore;
        private Integer progressScore;
        private Integer professionalismScore;
        private String commentaire;
        private LocalDateTime dateEvaluation;
    }

    /**
     * Request DTO for report evaluation
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteRapportRequest {
        private Long rapportId;
        private Integer technicalScore; // 0-20 score for technical content
        private Integer structureScore; // 0-20 score for structure and organization
        private Integer originalityScore; // 0-20 score for originality
        private String commentaire; // Optional feedback
    }

    /**
     * Response DTO for report evaluation
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteRapportResponse {
        private Long id;
        private Long rapportId;
        private Long evaluateurId;
        private Integer technicalScore;
        private Integer structureScore;
        private Integer originalityScore;
        private String commentaire;
        private LocalDateTime dateEvaluation;
    }

    /**
     * Request DTO for defense presentation evaluation
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteSoutenanceRequest {
        private Long soutenanceId;
        private Long etudiantId; // ID of the student being evaluated
        private Integer presentationScore; // 0-20 score for presentation quality
        private Integer qaScore; // 0-20 score for Q&A responses
        private Integer timeManagementScore; // 0-20 score for time management
        private String commentaire; // Optional feedback
    }

    /**
     * Response DTO for defense presentation evaluation
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteSoutenanceResponse {
        private Long id;
        private Long soutenanceId;
        private Long juryId;
        private Long etudiantId;
        private Integer presentationScore;
        private Integer qaScore;
        private Integer timeManagementScore;
        private String commentaire;
        private LocalDateTime dateEvaluation;
    }

    /**
     * DTO for binome information
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeDTO {
        private Long id;
        private StudentDTO etudiant1;
        private StudentDTO etudiant2;
        private SujetDTO sujet;
        private String filiereName;
        private Boolean noteEncadrant; // Whether the binome has been evaluated by encadrant
    }

    /**
     * DTO for student information
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }

    /**
     * DTO for sujet information
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetDTO {
        private Long id;
        private String titre;
        private String theme;
    }

    /**
     * DTO for soutenance information
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SoutenanceDTO {
        private Long id;
        private LocalDate date;
        private LocalTime heure;
        private SalleDTO salle;
        private BinomeDTO binome;
        private List<EvaluationInfoDTO> evaluations; // Existing evaluations
    }

    /**
     * DTO for salle information
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SalleDTO {
        private Long id;
        private String nom;
    }

    /**
     * Simple DTO to track existing evaluations
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EvaluationInfoDTO {
        private Long etudiantId;
        private Long juryId;
    }

    /**
     * DTO for rapport information
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RapportDTO {
        private Long id;
        private String titre;
        private String localisationRapport;
        private BinomeDTO binome;
        private LocalDateTime dateSoumission;
        private Boolean evaluated; // Whether the report has been evaluated by current jury
    }
}