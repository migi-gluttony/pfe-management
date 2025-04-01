package ma.estfbs.pfe_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.PourcentageRapport;

public interface PourcentageRapportRepository extends JpaRepository<PourcentageRapport, Long> {
    // Get the most recent percentages configuration
    PourcentageRapport findTopByOrderByIdDesc();
    
    // Get percentage configuration for a specific year
    PourcentageRapport findByAnneeScolaire(AnneeScolaire anneeScolaire);
}