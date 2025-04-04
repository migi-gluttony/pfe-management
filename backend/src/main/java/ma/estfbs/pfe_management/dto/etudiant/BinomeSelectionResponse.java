package ma.estfbs.pfe_management.dto.etudiant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BinomeSelectionResponse {
    private List<StudentDTO> availableStudents;
    private boolean hasSentRequest;
    private Long targetStudentId;
    private StudentDTO etudiant1;
    private StudentDTO etudiant2;
}