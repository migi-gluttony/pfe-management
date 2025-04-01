package ma.estfbs.pfe_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.NoteEncadrant;
import ma.estfbs.pfe_management.model.Utilisateur;

public interface NoteEncadrantRepository extends JpaRepository<NoteEncadrant, Long> {
    // Find evaluation by binome and encadrant
    Optional<NoteEncadrant> findByBinomeAndEncadrant(Binome binome, Utilisateur encadrant);
    
    // Find evaluation by binome, encadrant and academic year
    Optional<NoteEncadrant> findByBinomeAndEncadrantAndAnneeScolaire(Binome binome, Utilisateur encadrant, AnneeScolaire anneeScolaire);
    
    // Find all evaluations by encadrant
    List<NoteEncadrant> findByEncadrant(Utilisateur encadrant);
    
    // Find all evaluations by encadrant and academic year
    List<NoteEncadrant> findByEncadrantAndAnneeScolaire(Utilisateur encadrant, AnneeScolaire anneeScolaire);
    
    // Find all evaluations by binome
    List<NoteEncadrant> findByBinome(Binome binome);
    
    // Find all evaluations by binome and academic year
    List<NoteEncadrant> findByBinomeAndAnneeScolaire(Binome binome, AnneeScolaire anneeScolaire);
    
    // Find all evaluations for a specific academic year
    List<NoteEncadrant> findByAnneeScolaire(AnneeScolaire anneeScolaire);
}