package ma.estfbs.pfe_management.dto.chefDeDepartement;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;

public class ArchiveDTOs {
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ArchiveResponse {
        private List<AcademicYearDTO> academicYears;
        private List<FiliereDTO> filieres;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AcademicYearDTO {
        private Long id;
        private String annee;
        private Boolean courante;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class YearDataExportDTO {
        private String academicYear;
        private List<CompteManagementDTOs.CompteDTO> comptes;
        private List<BinomeManagementDTOs.BinomeDTO> binomes;
        private List<ma.estfbs.pfe_management.dto.SujetDTO> sujets;
        private List<SoutenanceManagementDTOs.SoutenanceDTO> soutenances;
        private List<NoteManagementDTOs.NoteDTO> notes;
        private List<FiliereDTO> filieres;
        private NoteManagementDTOs.PourcentageDTO pourcentages;
    }
}
