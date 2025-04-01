package ma.estfbs.pfe_management.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note_soutenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteSoutenance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "soutenance_id", nullable = false)
    private Soutenance soutenance;
    
    @ManyToOne
    @JoinColumn(name = "jury_id", nullable = false)
    private Utilisateur jury;
    
    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Utilisateur etudiant;
    
    @Column(name = "presentation_score", nullable = false)
    private Integer presentationScore;
    
    @Column(name = "qa_score", nullable = false)
    private Integer qaScore;
    
    @Column(name = "time_management_score", nullable = false)
    private Integer timeManagementScore;
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    
    @Builder.Default
    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
    
    @OneToMany(mappedBy = "noteSoutenanceId")
    private List<NoteFinale> notesFinales;
    
    /**
     * Calculate the weighted average score based on the configured percentages
     */
    public double calculateWeightedScore(PourcentageSoutenance percentages) {
        if (percentages == null) {
            // Default to equal weighting if no percentages provided
            return (presentationScore + qaScore + timeManagementScore) / 3.0;
        }
        
        double weightedScore = 
            (presentationScore * percentages.getPourcentagePresentation() / 100.0) +
            (qaScore * percentages.getPourcentageQa() / 100.0) +
            (timeManagementScore * percentages.getPourcentageTimeManagement() / 100.0);
            
        return weightedScore;
    }
}