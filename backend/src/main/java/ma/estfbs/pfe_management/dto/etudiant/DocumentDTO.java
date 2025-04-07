package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    private Long id;
    private String titre;
    private String localisationDoc;
    private String commentaire; // Will be null if not reviewed yet
    private LocalDateTime dateSoumission;
    private boolean reviewed; // Indicates if the document has been reviewed
}
