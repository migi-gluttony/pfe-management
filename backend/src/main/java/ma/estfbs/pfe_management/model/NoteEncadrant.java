package ma.estfbs.pfe_management.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note_encadrant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteEncadrant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "binome_id", nullable = false)
    private Binome binome;
    
    @ManyToOne
    @JoinColumn(name = "encadrant_id", nullable = false)
    private Utilisateur encadrant;
    
    @Column(name = "technical_score", nullable = false)
    private Integer technicalScore;
    
    @Column(name = "report_score", nullable = false)
    private Integer reportScore;
    
    @Column(name = "progress_score", nullable = false)
    private Integer progressScore;
    
    @Column(name = "professionalism_score", nullable = false)
    private Integer professionalismScore;
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    
    @Builder.Default
    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
    
    @OneToMany(mappedBy = "noteEncadrantId")
    private List<NoteFinale> notesFinales;

    /**
     * Calculate the weighted average score based on the configured percentages
     */
    public double calculateWeightedScore(PourcentageEncadrant percentages) {
        if (percentages == null) {
            // Default to equal weighting if no percentages provided
            return (technicalScore + reportScore + progressScore + professionalismScore) / 4.0;
        }
        
        double weightedScore = 
            (technicalScore * percentages.getPourcentageTechnical() / 100.0) +
            (reportScore * percentages.getPourcentageReport() / 100.0) +
            (progressScore * percentages.getPourcentageProgress() / 100.0) +
            (professionalismScore * percentages.getPourcentageProfessionalism() / 100.0);
            
        return weightedScore;
    }
}
