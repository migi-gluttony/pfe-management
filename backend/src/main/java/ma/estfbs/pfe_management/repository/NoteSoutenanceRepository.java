package ma.estfbs.pfe_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.NoteSoutenance;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Utilisateur;

/**
 * Repository for handling defense presentation evaluations
 */
public interface NoteSoutenanceRepository extends JpaRepository<NoteSoutenance, Long> {
    // Find evaluation by jury, soutenance, and student
    Optional<NoteSoutenance> findByJuryAndSoutenanceAndEtudiant(
            Utilisateur jury, Soutenance soutenance, Utilisateur etudiant);
    
    // Find evaluation by jury, soutenance, student and academic year
    Optional<NoteSoutenance> findByJuryAndSoutenanceAndEtudiantAndAnneeScolaire(
            Utilisateur jury, Soutenance soutenance, Utilisateur etudiant, AnneeScolaire anneeScolaire);
    
    // Find all evaluations by jury
    List<NoteSoutenance> findByJury(Utilisateur jury);
    
    // Find all evaluations by jury and academic year
    List<NoteSoutenance> findByJuryAndAnneeScolaire(Utilisateur jury, AnneeScolaire anneeScolaire);
    
    // Find all evaluations by soutenance
    List<NoteSoutenance> findBySoutenance(Soutenance soutenance);
    
    // Find all evaluations by soutenance and academic year
    List<NoteSoutenance> findBySoutenanceAndAnneeScolaire(Soutenance soutenance, AnneeScolaire anneeScolaire);
    
    // Find all evaluations by student
    List<NoteSoutenance> findByEtudiant(Utilisateur etudiant);
    
    // Find all evaluations by student and academic year
    List<NoteSoutenance> findByEtudiantAndAnneeScolaire(Utilisateur etudiant, AnneeScolaire anneeScolaire);
    
    // Find all evaluations for a specific academic year
    List<NoteSoutenance> findByAnneeScolaire(AnneeScolaire anneeScolaire);
    
    // Find evaluations by student and soutenance
    List<NoteSoutenance> findByEtudiantAndSoutenance(Utilisateur etudiant, Soutenance soutenance);
    
    // Find evaluations by student, soutenance and academic year
    List<NoteSoutenance> findByEtudiantAndSoutenanceAndAnneeScolaire(
            Utilisateur etudiant, Soutenance soutenance, AnneeScolaire anneeScolaire);
}