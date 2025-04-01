package ma.estfbs.pfe_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.PourcentageEncadrant;

public interface PourcentageEncadrantRepository extends JpaRepository<PourcentageEncadrant, Long> {
    // Get the most recent percentages configuration
    PourcentageEncadrant findTopByOrderByIdDesc();
    
    // Get percentage configuration for a specific year
    PourcentageEncadrant findByAnneeScolaire(AnneeScolaire anneeScolaire);
}