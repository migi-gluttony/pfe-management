package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pourcentage_encadrant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PourcentageEncadrant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pourcentage_technical", nullable = false)
    private Integer pourcentageTechnical;
    
    @Column(name = "pourcentage_report", nullable = false)
    private Integer pourcentageReport;
    
    @Column(name = "pourcentage_progress", nullable = false)
    private Integer pourcentageProgress;
    
    @Column(name = "pourcentage_professionalism", nullable = false)
    private Integer pourcentageProfessionalism;
    
    @OneToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
}
