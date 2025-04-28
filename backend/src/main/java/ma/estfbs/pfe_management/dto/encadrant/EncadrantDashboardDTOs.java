package ma.estfbs.pfe_management.dto.encadrant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class EncadrantDashboardDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EncadrantDashboardResponse {
        private EncadrantInfo encadrantInfo;
        private BinomeStats binomeStats;
        private DocumentStats documentStats;
        private EvaluationStats evaluationStats;
        private SoutenanceStats soutenanceStats;
        private List<UpcomingSoutenanceDTO> upcomingSoutenances;
        private List<PendingDocumentDTO> pendingDocuments;
        private List<RecentActivityDTO> recentActivities;
        private List<ReminderDTO> reminders;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EncadrantInfo {
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
    public static class BinomeStats {
        private Integer totalBinomes;
        private Integer activeBinomes;
        private Integer completedProjects;
        private Integer binomesWithPendingDocuments;
        private Integer evaluatedBinomes;
        private Double averageProgressScore;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentStats {
        private Integer totalDocuments;
        private Integer pendingReviews;
        private Integer reviewedToday;
        private Integer documentsSubmittedThisWeek;
        private Double averageResponseTime; // in hours
        private Integer overdueReviews;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EvaluationStats {
        private Integer evaluatedBinomes;
        private Integer pendingEvaluations;
        private Double averageGrade;
        private Integer highPerformingBinomes; // Grade >= 16
        private Integer needsImprovementBinomes; // Grade < 10
        private GradeDistributionDTO gradeDistribution;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GradeDistributionDTO {
        private Integer excellentCount; // 16-20
        private Integer goodCount; // 14-15.9
        private Integer fairCount; // 12-13.9
        private Integer passCount; // 10-11.9
        private Integer failCount; // < 10
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SoutenanceStats {
        private Integer totalSoutenances;
        private Integer upcomingSoutenances;
        private Integer todaySoutenances;
        private Integer scheduledThisWeek;
        private LocalDate nextSoutenanceDate;
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
        private Integer projectProgress; // percentage
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
    public static class PendingDocumentDTO {
        private Long id;
        private String titre;
        private LocalDateTime dateSoumission;
        private BinomeInfoDTO binome;
        private String urgency; // HIGH, MEDIUM, LOW
        private Integer daysWaiting;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentActivityDTO {
        private Long id;
        private String type; // DOCUMENT_REVIEWED, BINOME_EVALUATED, MEETING_SCHEDULED
        private String description;
        private LocalDateTime timestamp;
        private String icon;
        private String severity; // info, success, warning
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