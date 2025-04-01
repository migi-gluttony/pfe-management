package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note_finale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteFinale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Utilisateur etudiant;
    
    @ManyToOne
    @JoinColumn(name = "binome_id", nullable = false)
    private Binome binome;
    
    @Column(name = "note_rapport", nullable = false)
    private Integer noteRapport;
    
    @Column(name = "note_soutenance", nullable = false)
    private Integer noteSoutenance;
    
    @Column(name = "note_encadrant", nullable = false)
    private Integer noteEncadrant;
    
    @ManyToOne
    @JoinColumn(name = "note_rapport_id")
    private NoteRapport noteRapportId;
    
    @ManyToOne
    @JoinColumn(name = "note_soutenance_id")
    private NoteSoutenance noteSoutenanceId;
    
    @ManyToOne
    @JoinColumn(name = "note_encadrant_id")
    private NoteEncadrant noteEncadrantId;
    
    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
    
    /**
     * Calculate the final weighted score based on the configured percentages
     */
    public double calculateFinalScore(Pourcentage percentages) {
        if (percentages == null) {
            // Default to equal weighting if no percentages provided
            return (noteRapport + noteSoutenance + noteEncadrant) / 3.0;
        }
        
        double finalScore = 
            (noteRapport * percentages.getPourcentageRapport() / 100.0) +
            (noteSoutenance * percentages.getPourcentageSoutenance() / 100.0) +
            (noteEncadrant * percentages.getPourcentageEncadrant() / 100.0);
            
        return finalScore;
    }
    
    // For backward compatibility
    public NoteFinale(Utilisateur etudiant, Integer noteRapport, Integer noteSoutenance) {
        this.etudiant = etudiant;
        this.noteRapport = noteRapport;
        this.noteSoutenance = noteSoutenance;
        this.noteEncadrant = 0; // Default value
    }
}