package ma.estfbs.pfe_management.dto;

import java.time.LocalDateTime;

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
    private Integer reportScore;    // 0-20 score for report quality
    private Integer progressScore;  // 0-20 score for progress tracking
    private Integer professionalismScore; // 0-20 score for professionalism
    private String commentaire;     // Optional feedback
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
    private Integer technicalScore;   // 0-20 score for technical content
    private Integer structureScore;   // 0-20 score for structure and organization
    private Integer originalityScore; // 0-20 score for originality
    private String commentaire;       // Optional feedback
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
    private Long etudiantId;        // ID of the student being evaluated
    private Integer presentationScore;     // 0-20 score for presentation quality
    private Integer qaScore;              // 0-20 score for Q&A responses
    private Integer timeManagementScore;  // 0-5 score for time management
    private String commentaire;           // Optional feedback
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
}
