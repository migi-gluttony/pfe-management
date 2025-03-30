package ma.estfbs.pfe_management.dto.chefDeDepartement;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.SujetDTO;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SujetManagementResponse {
    private List<SujetDTO> sujets;
    private List<FiliereDTO> filieres;
}
