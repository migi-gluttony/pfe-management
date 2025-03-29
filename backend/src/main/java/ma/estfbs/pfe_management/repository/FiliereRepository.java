package ma.estfbs.pfe_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.Filiere;

public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    boolean existsByNom(String nom);
}
