package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SujetSuggestionRequest {
    private String titre;
    private String theme;
    private String description;
}