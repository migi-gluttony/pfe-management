package ma.estfbs.pfe_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.PourcentageSoutenance;

public interface PourcentageSoutenanceRepository extends JpaRepository<PourcentageSoutenance, Long> {
    // Get the most recent percentages configuration
    PourcentageSoutenance findTopByOrderByIdDesc();
    
    // Get percentage configuration for a specific year
    PourcentageSoutenance findByAnneeScolaire(AnneeScolaire anneeScolaire);
}