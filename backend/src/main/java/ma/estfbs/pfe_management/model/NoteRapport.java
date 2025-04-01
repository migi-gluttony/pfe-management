package ma.estfbs.pfe_management.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note_rapport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteRapport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "rapport_id", nullable = false)
    private Rapport rapport;
    
    @ManyToOne
    @JoinColumn(name = "evaluateur_id", nullable = false)
    private Utilisateur evaluateur;
    
    @Column(name = "technical_score", nullable = false)
    private Integer technicalScore;
    
    @Column(name = "structure_score", nullable = false)
    private Integer structureScore;
    
    @Column(name = "originality_score", nullable = false)
    private Integer originalityScore;
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    
    @Builder.Default
    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
    
    @OneToMany(mappedBy = "noteRapportId")
    private List<NoteFinale> notesFinales;
    
    /**
     * Calculate the weighted average score based on the configured percentages
     */
    public double calculateWeightedScore(PourcentageRapport percentages) {
        if (percentages == null) {
            // Default to equal weighting if no percentages provided
            return (technicalScore + structureScore + originalityScore) / 3.0;
        }
        
        double weightedScore = 
            (technicalScore * percentages.getPourcentageTechnical() / 100.0) +
            (structureScore * percentages.getPourcentageStructure() / 100.0) +
            (originalityScore * percentages.getPourcentageOriginality() / 100.0);
            
        return weightedScore;
    }
}
