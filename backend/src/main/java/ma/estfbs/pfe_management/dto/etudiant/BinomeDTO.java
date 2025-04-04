package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BinomeDTO {
    private Long id;
    private StudentDTO etudiant1;
    private StudentDTO etudiant2;
    private Long sujetId;
}