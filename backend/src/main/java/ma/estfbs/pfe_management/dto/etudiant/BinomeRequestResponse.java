package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BinomeRequestResponse {
    private boolean success;
    private String message;
    private BinomeDTO binome;  // Optional, set when a request is auto-accepted
}