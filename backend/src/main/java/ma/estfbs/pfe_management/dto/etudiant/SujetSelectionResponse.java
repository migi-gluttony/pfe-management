package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.estfbs.pfe_management.dto.SujetDTO;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SujetSelectionResponse {
    private Long binomeId;
    private boolean hasSujet;
    private boolean hasPendingSuggestion;
    private List<SujetDTO> availableSujets;
    private SujetDTO selectedSujet;
    private StudentDTO etudiant1;
    private StudentDTO etudiant2;
}