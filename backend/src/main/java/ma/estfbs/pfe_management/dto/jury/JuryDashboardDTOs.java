package ma.estfbs.pfe_management.dto.jury;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class JuryDashboardDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JuryDashboardResponse {
        private JuryInfo juryInfo;
        private SoutenanceStats soutenanceStats;
        private ReportStats reportStats;
        private BinomeStats binomeStats;
        private List<UpcomingSoutenanceDTO> upcomingSoutenances;
        private List<RecentActivityDTO> recentActivities;
        private List<PendingEvaluationDTO> pendingEvaluations;
        private List<ReminderDTO> reminders;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JuryInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String anneeAcademique;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SoutenanceStats {
        private Integer totalSoutenances;
        private Integer todaySoutenances;
        private Integer upcomingSoutenances;
        private Integer evaluatedSoutenances;
        private Integer pendingSoutenances;
        private Double averageEvaluationScore;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportStats {
        private Integer totalReports;
        private Integer evaluatedReports;
        private Integer pendingReports;
        private Double averageReportScore;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeStats {
        private Integer totalBinomes;
        private Integer fullyEvaluatedBinomes;
        private Integer partiallyEvaluatedBinomes;
        private Integer pendingBinomes;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpcomingSoutenanceDTO {
        private Long id;
        private LocalDate date;
        private LocalTime heure;
        private String salle;
        private BinomeInfoDTO binome;
        private String sujetTitre;
        private Boolean isEvaluated;
        private Integer daysUntilSoutenance;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeInfoDTO {
        private Long id;
        private StudentDTO etudiant1;
        private StudentDTO etudiant2;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentDTO {
        private Long id;
        private String nom;
        private String prenom;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentActivityDTO {
        private Long id;
        private String type; // REPORT_EVALUATION, SOUTENANCE_EVALUATION
        private String description;
        private LocalDateTime timestamp;
        private String icon;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PendingEvaluationDTO {
        private Long id;
        private String type; // REPORT, SOUTENANCE
        private String title;
        private LocalDateTime dueDate;
        private String status;
        private String urgency; // HIGH, MEDIUM, LOW
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
}