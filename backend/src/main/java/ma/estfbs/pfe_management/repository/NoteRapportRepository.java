package ma.estfbs.pfe_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.NoteRapport;
import ma.estfbs.pfe_management.model.Rapport;
import ma.estfbs.pfe_management.model.Utilisateur;

/**
 * Repository for handling report evaluations
 */
public interface NoteRapportRepository extends JpaRepository<NoteRapport, Long> {
    // Find evaluation by rapport and evaluator
    Optional<NoteRapport> findByRapportAndEvaluateur(Rapport rapport, Utilisateur evaluateur);
    
    // Find evaluation by rapport, evaluator and academic year
    Optional<NoteRapport> findByRapportAndEvaluateurAndAnneeScolaire(Rapport rapport, Utilisateur evaluateur, AnneeScolaire anneeScolaire);
    
    // Find all evaluations by evaluator
    List<NoteRapport> findByEvaluateur(Utilisateur evaluateur);
    
    // Find all evaluations by evaluator and academic year
    List<NoteRapport> findByEvaluateurAndAnneeScolaire(Utilisateur evaluateur, AnneeScolaire anneeScolaire);
    
    // Find all evaluations by rapport
    List<NoteRapport> findByRapport(Rapport rapport);
    
    // Find all evaluations by rapport and academic year
    List<NoteRapport> findByRapportAndAnneeScolaire(Rapport rapport, AnneeScolaire anneeScolaire);
    
    // Find all evaluations for a specific academic year
    List<NoteRapport> findByAnneeScolaire(AnneeScolaire anneeScolaire);
}