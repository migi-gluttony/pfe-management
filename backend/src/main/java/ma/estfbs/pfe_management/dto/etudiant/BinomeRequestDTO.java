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
public class BinomeRequestDTO {
    private Long id;
    private Long demandeurId;
    private String demandeurNom;
    private String demandeurPrenom;
    private LocalDateTime dateDemande;
}