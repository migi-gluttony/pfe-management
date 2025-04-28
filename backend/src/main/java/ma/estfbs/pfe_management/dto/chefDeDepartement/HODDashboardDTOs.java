package ma.estfbs.pfe_management.dto.chefDeDepartement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class HODDashboardDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HODDashboardResponse {
        private HODInfo hodInfo;
        private DashboardStatsDTO stats;
        private List<UpcomingSoutenanceDTO> upcomingSoutenances;
        private List<RecentActivityDTO> recentActivities;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HODInfo {
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
    public static class DashboardStatsDTO {
        private Integer totalUsers;
        private Integer totalStudents;
        private Integer totalSupervisors;
        private Integer totalJuries;
        private Integer totalBinomes;
        private Integer binomesWithSoutenance;
        private Integer totalSujets;
        private Integer pendingSuggestions;
        private Integer totalSoutenances;
        private Integer plannedSoutenances;
        private Integer completedSoutenances;
        private Double averageGrade;
        private Integer honorsCount;
        private Integer goodCount;
        private Integer passCount;
        private Integer totalFilieres;
        private List<FiliereStatsDTO> filieres;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FiliereStatsDTO {
        private String nom;
        private Integer etudiantCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpcomingSoutenanceDTO {
        private Long id;
        private LocalDate date;
        private LocalTime heure;
        private SalleDTO salle;
        private BinomeDTO binome;
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
    public static class BinomeDTO {
        private Long id;
        private StudentDTO etudiant1;
        private StudentDTO etudiant2;
        private SujetDTO sujet;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetDTO {
        private Long id;
        private String titre;
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
    public static class JuryDTO {
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
        private String description;
        private LocalDateTime timestamp;
        private String icon;
    }
}