package ma.estfbs.pfe_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.AnneeScolaire;

public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, Long> {
    Optional<AnneeScolaire> findByCourante(boolean courante);
    Optional<AnneeScolaire> findByAnnee(String annee);
}