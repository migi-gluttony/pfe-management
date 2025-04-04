package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtudiantStatusResponse {
    private boolean hasBinome;
    private boolean hasSujet;
    private boolean hasPendingBinomeRequests;
    private boolean hasSentBinomeRequest;
    private boolean hasPendingSujetSuggestion;
    private Long binomeId;
    private Long sujetId;
    private String nextAction; // "CHOOSE_BINOME", "CHOOSE_SUJET", "PROCEED"
}