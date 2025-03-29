package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Rapport;

public interface RapportRepository extends JpaRepository<Rapport, Long> {
    List<Rapport> findByBinome(Binome binome);
    List<Rapport> findByTitreContainingIgnoreCase(String titre);
    List<Rapport> findByAnneeScolaire(AnneeScolaire anneeScolaire);
    List<Rapport> findByBinomeAndAnneeScolaire(Binome binome, AnneeScolaire anneeScolaire);
    
    // Find the latest report for a binome
    Rapport findTopByBinomeOrderByIdDesc(Binome binome);
    Rapport findTopByBinomeAndAnneeScolaireOrderByIdDesc(Binome binome, AnneeScolaire anneeScolaire);
}