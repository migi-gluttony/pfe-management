package ma.estfbs.pfe_management.dto.encadrant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentEvaluationRequest {
    private String commentaire;
}
