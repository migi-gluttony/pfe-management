package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pourcentage_rapport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PourcentageRapport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pourcentage_technical", nullable = false)
    private Integer pourcentageTechnical;
    
    @Column(name = "pourcentage_structure", nullable = false)
    private Integer pourcentageStructure;
    
    @Column(name = "pourcentage_originality", nullable = false)
    private Integer pourcentageOriginality;
    
    @OneToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
}
