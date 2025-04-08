package ma.estfbs.pfe_management.dto.encadrant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentEvaluationDTO {
    private Long id;
    private String titre;
    private String localisationDoc;
    private String commentaire;
    private LocalDateTime dateSoumission;
    private Long binomeId;
    private String etudiant1Nom;
    private String etudiant1Prenom;
    private String etudiant2Nom;
    private String etudiant2Prenom;
}
