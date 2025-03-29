package ma.estfbs.pfe_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.NoteFinale;
import ma.estfbs.pfe_management.model.Utilisateur;

public interface NoteFinaleRepository extends JpaRepository<NoteFinale, Long> {
    Optional<NoteFinale> findByEtudiant(Utilisateur etudiant);
    Optional<NoteFinale> findByEtudiantAndAnneeScolaire(Utilisateur etudiant, AnneeScolaire anneeScolaire);
    List<NoteFinale> findByAnneeScolaire(AnneeScolaire anneeScolaire);
    List<NoteFinale> findByOrderByNoteRapportDescNoteSoutenanceDesc();
    List<NoteFinale> findByAnneeScolaireOrderByNoteRapportDescNoteSoutenanceDesc(AnneeScolaire anneeScolaire);
}