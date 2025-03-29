package ma.estfbs.pfe_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Pourcentage;

public interface PourcentageRepository extends JpaRepository<Pourcentage, Long> {
    // Get the most recent percentages configuration
    Pourcentage findTopByOrderByIdDesc();
    
    // Get percentage configuration for a specific year
    Pourcentage findByAnneeScolaire(AnneeScolaire anneeScolaire);
}