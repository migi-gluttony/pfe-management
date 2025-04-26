package ma.estfbs.pfe_management.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.GradingDtos.*;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.NoteEncadrant;
import ma.estfbs.pfe_management.model.NoteRapport;
import ma.estfbs.pfe_management.model.NoteSoutenance;
import ma.estfbs.pfe_management.model.Rapport;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.NoteEncadrantRepository;
import ma.estfbs.pfe_management.repository.NoteRapportRepository;
import ma.estfbs.pfe_management.repository.NoteSoutenanceRepository;
import ma.estfbs.pfe_management.repository.RapportRepository;
import ma.estfbs.pfe_management.repository.SoutenanceRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;
import ma.estfbs.pfe_management.service.GradingService;

@RestController
@RequestMapping("/api/grading")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;
    private final BinomeRepository binomeRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final NoteEncadrantRepository noteEncadrantRepository;
    private final NoteSoutenanceRepository noteSoutenanceRepository;
    private final AcademicYearService academicYearService;
    private final RapportRepository rapportRepository;
    private final NoteRapportRepository noteRapportRepository;

    // ==================== ENCADRANT ENDPOINTS ====================

    /**
     * Get all binomes assigned to the authenticated encadrant for the current
     * academic year
     */
    @GetMapping("/encadrant/binomes")
    public ResponseEntity<List<BinomeDTO>> getEncadrantBinomes(Authentication authentication) {
        // Get the authenticated user
        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();

        // Check if the user has ENCADRANT role
        if (encadrant.getRole() != Utilisateur.Role.ENCADRANT) {
            return ResponseEntity.status(403).build();
        }

        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Fetch binomes assigned to this encadrant
        List<Binome> encadrantBinomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);

        // Convert to DTOs
        List<BinomeDTO> binomeDTOs = encadrantBinomes.stream()
                .map(this::mapToBinomeDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(binomeDTOs);
    }

    /**
     * Get existing evaluation for a binome
     */
    @GetMapping("/encadrant/evaluations/{binomeId}")
    public ResponseEntity<NoteEncadrantResponse> getEncadrantEvaluation(
            Authentication authentication,
            @PathVariable Long binomeId) {

        // Get the authenticated user
        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();

        // Check if the user has ENCADRANT role
        if (encadrant.getRole() != Utilisateur.Role.ENCADRANT) {
            return ResponseEntity.status(403).build();
        }

        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find the binome
        Binome binome = binomeRepository.findById(binomeId)
                .orElseThrow(() -> new RuntimeException("Binome not found with id: " + binomeId));

        // Check if the encadrant is assigned to this binome
        if (!binome.getEncadrant().getId().equals(encadrant.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Find existing evaluation
        Optional<NoteEncadrant> existingEvaluation = noteEncadrantRepository
                .findByBinomeAndEncadrantAndAnneeScolaire(binome, encadrant, currentYear);

        if (existingEvaluation.isPresent()) {
            return ResponseEntity.ok(mapToNoteEncadrantResponse(existingEvaluation.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Submit supervisor (encadrant) evaluation
     * Only accessible by ENCADRANT role
     */
    @PostMapping("/encadrant/evaluation")
    public ResponseEntity<NoteEncadrantResponse> submitEncadrantEvaluation(
            Authentication authentication,
            @RequestBody NoteEncadrantRequest request) {

        // Get the authenticated user
        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();

        // Check if the user has ENCADRANT role
        if (encadrant.getRole() != Utilisateur.Role.ENCADRANT) {
            return ResponseEntity.status(403).build();
        }

        // Submit the evaluation
        NoteEncadrantResponse response = gradingService.submitEncadrantEvaluation(encadrant.getId(), request);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all reports for a specific binome (for encadrant)
     */
    @GetMapping("/encadrant/binome-reports/{binomeId}")
    public ResponseEntity<List<RapportDTO>> getBinomeReports(
            Authentication authentication,
            @PathVariable Long binomeId) {
        
        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();
        
        if (encadrant.getRole() != Utilisateur.Role.ENCADRANT) {
            return ResponseEntity.status(403).build();
        }
        
        Binome binome = binomeRepository.findById(binomeId)
                .orElseThrow(() -> new RuntimeException("Binome not found with id: " + binomeId));
        
        if (!binome.getEncadrant().getId().equals(encadrant.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        List<Rapport> reports = rapportRepository.findByBinomeAndAnneeScolaire(binome, currentYear);
        
        List<RapportDTO> reportDTOs = reports.stream()
                .map(rapport -> mapToRapportDTO(rapport, encadrant))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(reportDTOs);
    }

    /**
     * Download a report
     */
    @GetMapping("/encadrant/report-download/{reportId}")
    public ResponseEntity<Resource> downloadReport(
            Authentication authentication,
            @PathVariable Long reportId) {
        
        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();
        
        if (encadrant.getRole() != Utilisateur.Role.ENCADRANT) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            Rapport rapport = rapportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            if (!rapport.getBinome().getEncadrant().getId().equals(encadrant.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            Path path = Paths.get(rapport.getLocalisationRapport());
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(404).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Preview a report - with proper headers for content viewing
     */
    @GetMapping("/encadrant/report-preview/{reportId}")
    public ResponseEntity<Resource> previewReport(
            Authentication authentication,
            @PathVariable Long reportId) {
        
        Utilisateur encadrant = (Utilisateur) authentication.getPrincipal();
        
        if (encadrant.getRole() != Utilisateur.Role.ENCADRANT) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            Rapport rapport = rapportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            if (!rapport.getBinome().getEncadrant().getId().equals(encadrant.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            Path path = Paths.get(rapport.getLocalisationRapport());
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(path);
                if (contentType == null) contentType = "application/octet-stream";
                
                // Set the appropriate content type without any frame restrictions
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                
                // Add Access-Control headers to allow XHR requests
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "GET, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.status(404).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ==================== JURY ENDPOINTS ====================

    /**
     * Get all soutenances assigned to the authenticated jury for the current
     * academic year
     */
    @GetMapping("/jury/soutenances")
    public ResponseEntity<List<SoutenanceDTO>> getJurySoutenances(Authentication authentication) {
        // Get the authenticated user
        Utilisateur jury = (Utilisateur) authentication.getPrincipal();

        // Check if the user has JURY role
        if (jury.getRole() != Utilisateur.Role.JURY) {
            return ResponseEntity.status(403).build();
        }

        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Fetch soutenances where this user is jury1 or jury2
        List<Soutenance> jurySoutenances = soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury,
                currentYear);

        // Convert to DTOs
        List<SoutenanceDTO> soutenanceDTOs = jurySoutenances.stream()
                .map(this::mapToSoutenanceDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(soutenanceDTOs);
    }

    /**
     * Get existing evaluation for a student in a soutenance
     */
    @GetMapping("/jury/evaluations/{soutenanceId}/{etudiantId}")
    public ResponseEntity<NoteSoutenanceResponse> getJuryEvaluation(
            Authentication authentication,
            @PathVariable Long soutenanceId,
            @PathVariable Long etudiantId) {

        // Get the authenticated user
        Utilisateur jury = (Utilisateur) authentication.getPrincipal();

        // Check if the user has JURY role
        if (jury.getRole() != Utilisateur.Role.JURY) {
            return ResponseEntity.status(403).build();
        }

        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find the soutenance
        Soutenance soutenance = soutenanceRepository.findById(soutenanceId)
                .orElseThrow(() -> new RuntimeException("Soutenance not found with id: " + soutenanceId));

        // Check if the jury is assigned to this soutenance
        if (!soutenance.getJury1().getId().equals(jury.getId())
                && !soutenance.getJury2().getId().equals(jury.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Check if the student belongs to the binome
        Binome binome = soutenance.getBinome();
        if (!(binome.getEtudiant1().getId().equals(etudiantId) ||
                (binome.getEtudiant2() != null && binome.getEtudiant2().getId().equals(etudiantId)))) {
            return ResponseEntity.status(400).body(null);
        }

        // Find the student
        Utilisateur etudiant = binome.getEtudiant1().getId().equals(etudiantId)
                ? binome.getEtudiant1()
                : binome.getEtudiant2();

        // Find existing evaluation
        Optional<NoteSoutenance> existingEvaluation = noteSoutenanceRepository
                .findByJuryAndSoutenanceAndEtudiantAndAnneeScolaire(jury, soutenance, etudiant, currentYear);

        if (existingEvaluation.isPresent()) {
            return ResponseEntity.ok(mapToNoteSoutenanceResponse(existingEvaluation.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Submit presentation/defense evaluation by a jury member
     * Only accessible by JURY role
     */
    @PostMapping("/jury/soutenance-evaluation")
    public ResponseEntity<NoteSoutenanceResponse> submitSoutenanceEvaluation(
            Authentication authentication,
            @RequestBody NoteSoutenanceRequest request) {

        // Get the authenticated user
        Utilisateur jury = (Utilisateur) authentication.getPrincipal();

        // Check if the user has JURY role
        if (jury.getRole() != Utilisateur.Role.JURY) {
            return ResponseEntity.status(403).build();
        }

        // Submit the evaluation
        NoteSoutenanceResponse response = gradingService.submitSoutenanceEvaluation(jury.getId(), request);

        return ResponseEntity.ok(response);
    }

    // ==================== MAPPING METHODS ====================

    /**
     * Map Binome entity to BinomeDTO
     */
    private BinomeDTO mapToBinomeDTO(Binome binome) {
        return BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null)
                .sujet(mapToSujetDTO(binome.getSujet()))
                .filiereName(getFiliereName(binome))
                .noteEncadrant(hasEncadrantEvaluation(binome))
                .build();
    }

    /**
     * Map Utilisateur to StudentDTO
     */
    private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
        if (utilisateur == null)
            return null;

        return StudentDTO.builder()
                .id(utilisateur.getId())
                .prenom(utilisateur.getPrenom())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .build();
    }

    /**
     * Map Sujet to SujetDTO
     */
    private SujetDTO mapToSujetDTO(ma.estfbs.pfe_management.model.Sujet sujet) {
        if (sujet == null)
            return null;

        return SujetDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .theme(sujet.getTheme())
                .build();
    }

    /**
     * Map Soutenance entity to SoutenanceDTO
     */
    private SoutenanceDTO mapToSoutenanceDTO(Soutenance soutenance) {
        if (soutenance == null)
            return null;

        return SoutenanceDTO.builder()
                .id(soutenance.getId())
                .date(soutenance.getDate())
                .heure(soutenance.getHeure())
                .salle(mapToSalleDTO(soutenance.getSalle()))
                .binome(mapToBinomeDTO(soutenance.getBinome()))
                .evaluations(getExistingEvaluations(soutenance))
                .build();
    }

    /**
     * Map Salle to SalleDTO
     */
    private SalleDTO mapToSalleDTO(ma.estfbs.pfe_management.model.Salle salle) {
        if (salle == null)
            return null;

        return SalleDTO.builder()
                .id(salle.getId())
                .nom(salle.getNom())
                .build();
    }

    /**
     * Map NoteEncadrant entity to NoteEncadrantResponse DTO
     */
    private NoteEncadrantResponse mapToNoteEncadrantResponse(NoteEncadrant noteEncadrant) {
        return NoteEncadrantResponse.builder()
                .id(noteEncadrant.getId())
                .binomeId(noteEncadrant.getBinome().getId())
                .encadrantId(noteEncadrant.getEncadrant().getId())
                .technicalScore(noteEncadrant.getTechnicalScore())
                .reportScore(noteEncadrant.getReportScore())
                .progressScore(noteEncadrant.getProgressScore())
                .professionalismScore(noteEncadrant.getProfessionalismScore())
                .commentaire(noteEncadrant.getCommentaire())
                .dateEvaluation(noteEncadrant.getDateEvaluation())
                .build();
    }

    /**
     * Map NoteSoutenance entity to NoteSoutenanceResponse DTO
     */
    private NoteSoutenanceResponse mapToNoteSoutenanceResponse(NoteSoutenance noteSoutenance) {
        return NoteSoutenanceResponse.builder()
                .id(noteSoutenance.getId())
                .soutenanceId(noteSoutenance.getSoutenance().getId())
                .juryId(noteSoutenance.getJury().getId())
                .etudiantId(noteSoutenance.getEtudiant().getId())
                .presentationScore(noteSoutenance.getPresentationScore())
                .qaScore(noteSoutenance.getQaScore())
                .timeManagementScore(noteSoutenance.getTimeManagementScore())
                .commentaire(noteSoutenance.getCommentaire())
                .dateEvaluation(noteSoutenance.getDateEvaluation())
                .build();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get filiere name from binome
     */
    private String getFiliereName(Binome binome) {
        if (binome.getEtudiant1() == null)
            return null;

        // In a real implementation, you'd get this from the Etudiant entity
        // For now, we'll return a placeholder
        return "Informatique";
    }

    /**
     * Check if binome has encadrant evaluation
     */
    private boolean hasEncadrantEvaluation(Binome binome) {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        return noteEncadrantRepository
                .findByBinomeAndEncadrantAndAnneeScolaire(binome, binome.getEncadrant(), currentYear)
                .isPresent();
    }

    /**
     * Get existing evaluations for a soutenance
     */
    private List<EvaluationInfoDTO> getExistingEvaluations(Soutenance soutenance) {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        List<NoteSoutenance> evaluations = noteSoutenanceRepository
                .findBySoutenanceAndAnneeScolaire(soutenance, currentYear);

        return evaluations.stream()
                .map(eval -> EvaluationInfoDTO.builder()
                        .etudiantId(eval.getEtudiant().getId())
                        .juryId(eval.getJury().getId())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get all reports that need to be evaluated by the jury
     */
    @GetMapping("/jury/reports")
    public ResponseEntity<List<RapportDTO>> getJuryReports(Authentication authentication) {
        // Get the authenticated user
        Utilisateur jury = (Utilisateur) authentication.getPrincipal();

        // Check if the user has JURY role
        if (jury.getRole() != Utilisateur.Role.JURY) {
            return ResponseEntity.status(403).build();
        }

        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find soutenances where this user is assigned as jury1 or jury2
        List<Soutenance> jurySoutenances = soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury,
                currentYear);

        // Get the binomes from these soutenances
        List<Binome> binomes = jurySoutenances.stream()
                .map(Soutenance::getBinome)
                .collect(Collectors.toList());

        // Get reports for these binomes
        List<Rapport> reports = new ArrayList<>();
        for (Binome binome : binomes) {
            List<Rapport> binomeReports = rapportRepository.findByBinomeAndAnneeScolaire(binome, currentYear);
            reports.addAll(binomeReports);
        }

        // Convert to DTOs
        List<RapportDTO> reportDTOs = reports.stream()
                .map(rapport -> mapToRapportDTO(rapport, jury))
                .collect(Collectors.toList());

        return ResponseEntity.ok(reportDTOs);
    }

    /**
     * Get an existing report evaluation
     */
    @GetMapping("/jury/report-evaluations/{rapportId}")
    public ResponseEntity<NoteRapportResponse> getReportEvaluation(
            Authentication authentication,
            @PathVariable Long rapportId) {

        // Get the authenticated user
        Utilisateur jury = (Utilisateur) authentication.getPrincipal();

        // Check if the user has JURY role
        if (jury.getRole() != Utilisateur.Role.JURY) {
            return ResponseEntity.status(403).build();
        }

        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find the report
        Rapport rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new RuntimeException("Rapport not found with id: " + rapportId));

        // Check if report is from current year
        if (!rapport.getAnneeScolaire().getId().equals(currentYear.getId())) {
            return ResponseEntity.status(400).build();
        }

        // Find existing evaluation
        Optional<NoteRapport> existingEvaluation = noteRapportRepository
                .findByRapportAndEvaluateurAndAnneeScolaire(rapport, jury, currentYear);

        if (existingEvaluation.isPresent()) {
            return ResponseEntity.ok(mapToNoteRapportResponse(existingEvaluation.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Submit report evaluation by a jury member
     */
    @PostMapping("/jury/rapport-evaluation")
    public ResponseEntity<NoteRapportResponse> submitRapportEvaluation(
            Authentication authentication,
            @RequestBody NoteRapportRequest request) {

        // Get the authenticated user
        Utilisateur jury = (Utilisateur) authentication.getPrincipal();

        // Check if the user has JURY role
        if (jury.getRole() != Utilisateur.Role.JURY) {
            return ResponseEntity.status(403).build();
        }

        // Submit the evaluation using the service
        NoteRapportResponse response = gradingService.submitRapportEvaluation(jury.getId(), request);

        return ResponseEntity.ok(response);
    }

    /**
     * Map Rapport entity to RapportDTO
     */
    private RapportDTO mapToRapportDTO(Rapport rapport, Utilisateur evaluateur) {
        return RapportDTO.builder()
                .id(rapport.getId())
                .titre(rapport.getTitre())
                .localisationRapport(rapport.getLocalisationRapport())
                .binome(mapToBinomeDTO(rapport.getBinome()))
                .dateSoumission(rapport.getDateSoumission())
                .evaluated(hasReportEvaluationByUser(rapport, evaluateur))
                .build();
    }

    /**
     * Check if report has been evaluated by the specific user
     */
    private boolean hasReportEvaluationByUser(Rapport rapport, Utilisateur evaluateur) {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        return noteRapportRepository
                .findByRapportAndEvaluateurAndAnneeScolaire(rapport, evaluateur, currentYear)
                .isPresent();
    }


    private NoteRapportResponse mapToNoteRapportResponse(NoteRapport noteRapport) {
        return NoteRapportResponse.builder()
                .id(noteRapport.getId())
                .rapportId(noteRapport.getRapport().getId())
                .evaluateurId(noteRapport.getEvaluateur().getId())
                .technicalScore(noteRapport.getTechnicalScore())
                .structureScore(noteRapport.getStructureScore())
                .originalityScore(noteRapport.getOriginalityScore())
                .commentaire(noteRapport.getCommentaire())
                .dateEvaluation(noteRapport.getDateEvaluation())
                .build();
    }
}