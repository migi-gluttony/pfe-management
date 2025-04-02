package ma.estfbs.pfe_management.dto.chefDeDepartement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;

import java.time.LocalDateTime;
import java.util.List;

public class NoteManagementDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteDTO {
        private Long id;
        private EtudiantDTO etudiant;
        private Integer noteRapport;
        private Integer noteSoutenance;
        private Integer noteEncadrant;
        private Long filiereId;
        private String filiereName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EtudiantDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String cne;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PourcentageDTO {
        private Integer pourcentageRapport;
        private Integer pourcentageSoutenance;
        private Integer pourcentageEncadrant;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteManagementResponse {
        private List<NoteDTO> notes;
        private List<FiliereDTO> filieres;
        private PourcentageDTO pourcentages;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteDetailDTO {
        private Long etudiantId;
        private NoteEncadrantDetailDTO noteEncadrantDetail;
        private NoteSoutenanceDetailDTO noteSoutenanceDetail;
        private NoteRapportDetailDTO noteRapportDetail;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteEncadrantDetailDTO {
        private Integer technicalScore;
        private Integer reportScore;
        private Integer progressScore;
        private Integer professionalismScore;
        private String commentaire;
        private LocalDateTime dateEvaluation;
        private String encadrantNom;
        private String encadrantPrenom;
    }

    @Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public static class NoteSoutenanceDetailDTO {
    private Integer presentationScore;
    private Integer qaScore;
    private Integer timeManagementScore;
    private String commentaire;
    private Integer juryCount;
    private List<JuryEvaluationDTO> juryEvaluations;
}

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteRapportDetailDTO {
        private Integer technicalScore;
        private Integer structureScore;
        private Integer originalityScore;
        private String commentaire;
        private String titre;
        private LocalDateTime dateSoumission;
        private Integer evaluateurCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JuryEvaluationDTO {
        private Integer presentationScore;
        private Integer qaScore;
        private Integer timeManagementScore;
        private String commentaire;
        private String juryNom;
        private String juryPrenom;
        private LocalDateTime dateEvaluation;
    }
}