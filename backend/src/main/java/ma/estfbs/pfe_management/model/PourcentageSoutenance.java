package ma.estfbs.pfe_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pourcentage_soutenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PourcentageSoutenance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pourcentage_presentation", nullable = false)
    private Integer pourcentagePresentation;
    
    @Column(name = "pourcentage_qa", nullable = false)
    private Integer pourcentageQa;
    
    @Column(name = "pourcentage_time_management", nullable = false)
    private Integer pourcentageTimeManagement;
    
    @OneToOne
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;
}
