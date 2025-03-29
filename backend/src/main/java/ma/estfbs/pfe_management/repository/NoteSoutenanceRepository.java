package ma.estfbs.pfe_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import ma.estfbs.pfe_management.model.NoteSoutenance;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Utilisateur;

public interface NoteSoutenanceRepository extends JpaRepository<NoteSoutenance, Long> {
    List<NoteSoutenance> findByJury(Utilisateur jury);
    List<NoteSoutenance> findBySoutenance(Soutenance soutenance);
    List<NoteSoutenance> findByJuryAndSoutenance(Utilisateur jury, Soutenance soutenance);
}