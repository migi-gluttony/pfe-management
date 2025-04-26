package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class StudentDashboardDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentDashboardResponse {
        private StudentInfo studentInfo;
        private BinomeInfo binomeInfo;
        private SujetInfo sujetInfo;
        private SoutenanceInfo soutenanceInfo;
        private EncadrantInfo encadrantInfo;
        private DocumentStats documentStats;
        private List<NoteHistoryDTO> noteHistory;
        private CurrentNoteStatus currentNoteStatus;
        private List<ReminderDTO> reminders;
        private QuickActionsInfo quickActions;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String filiere;
        private String anneeAcademique;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeInfo {
        private Boolean hasBinome;
        private Long binomeId;
        private StudentPartnerDTO partner;
        private Boolean isSolo;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentPartnerDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetInfo {
        private Boolean hasSujet;
        private Long sujetId;
        private String titre;
        private String theme;
        private String description;
        private Boolean hasEncadrant;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SoutenanceInfo {
        private Boolean isScheduled;
        private LocalDate date;
        private LocalTime heure;
        private String salle;
        private String jury1;
        private String jury2;
        private Integer daysUntilSoutenance;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EncadrantInfo {
        private Boolean hasEncadrant;
        private String nom;
        private String prenom;
        private String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentStats {
        private Integer submittedDocuments;
        private Integer reviewedDocuments;
        private Boolean hasSubmittedReport;
        private Boolean reportCanBeModified;
        private LocalDateTime lastSubmissionDate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteHistoryDTO {
        private String anneeAcademique;
        private Integer noteFinale;
        private Integer noteRapport;
        private Integer noteSoutenance;
        private Integer noteEncadrant;
        private String mention; // Calculated based on final note
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CurrentNoteStatus {
        private Boolean allNotesPublished;
        private Integer noteFinale;
        private Integer noteRapport;
        private Integer noteSoutenance;
        private Integer noteEncadrant;
        private Boolean hasNoteRapport;
        private Boolean hasNoteSoutenance;
        private Boolean hasNoteEncadrant;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReminderDTO {
        private String type;
        private String title;
        private String message;
        private String severity; // info, warning, critical
        private String actionUrl;
        private Integer daysRemaining;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuickActionsInfo {
        private Boolean canSubmitDocuments;
        private Boolean canSubmitReport;
        private Boolean canChooseBinome;
        private Boolean canChooseSujet;
    }
}