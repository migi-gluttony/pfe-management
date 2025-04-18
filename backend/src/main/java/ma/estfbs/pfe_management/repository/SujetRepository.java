package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Sujet;

public interface SujetRepository extends JpaRepository<Sujet, Long> {
    List<Sujet> findByFiliere(Filiere filiere);
    List<Sujet> findByThemeContainingIgnoreCase(String theme);
    List<Sujet> findByTitreContainingIgnoreCase(String titre);
    List<Sujet> findByAnneeScolaire(AnneeScolaire anneeScolaire);
    List<Sujet> findByFiliereAndAnneeScolaire(Filiere filiere, AnneeScolaire anneeScolaire);
    
    // Renamed method to match existing pattern
    default List<Sujet> findByAnneeScolaireAndFiliere(AnneeScolaire anneeScolaire, Filiere filiere) {
        return findByFiliereAndAnneeScolaire(filiere, anneeScolaire);
    }
    
    // Find subjects that don't have any binome assigned yet
    List<Sujet> findByBinomesIsEmpty();
    
    // Find subjects that don't have any binome assigned for a specific year
    @Query("SELECT s FROM Sujet s WHERE s.anneeScolaire = :anneeScolaire AND NOT EXISTS (SELECT b FROM Binome b WHERE b.sujet = s AND b.anneeScolaire = :anneeScolaire)")
    List<Sujet> findByAnneeScolaireAndBinomesIsEmptyForYear(AnneeScolaire anneeScolaire);
}