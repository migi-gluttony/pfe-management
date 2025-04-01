package ma.estfbs.pfe_management.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GradingService {

    private final NoteEncadrantRepository noteEncadrantRepository;
    private final NoteRapportRepository noteRapportRepository;
    private final NoteSoutenanceRepository noteSoutenanceRepository;
    private final RapportRepository rapportRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final BinomeRepository binomeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AcademicYearService academicYearService;

    /**
     * Submit supervisor (encadrant) evaluation for a binome
     */
    @Transactional
    public NoteEncadrantResponse submitEncadrantEvaluation(Long encadrantId, NoteEncadrantRequest request) {
        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Validate the encadrant
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant not found with id: " + encadrantId));
        
        // Validate the binome and check that this encadrant is assigned to it
        Binome binome = binomeRepository.findById(request.getBinomeId())
                .orElseThrow(() -> new RuntimeException("Binome not found with id: " + request.getBinomeId()));
        
        if (!binome.getEncadrant().getId().equals(encadrantId)) {
            throw new RuntimeException("This encadrant is not assigned to the specified binome");
        }
        
        // Check if an evaluation already exists for this encadrant and binome
        Optional<NoteEncadrant> existingEvaluation = noteEncadrantRepository
                .findByBinomeAndEncadrantAndAnneeScolaire(binome, encadrant, currentYear);
        
        NoteEncadrant noteEncadrant;
        
        if (existingEvaluation.isPresent()) {
            // Update existing evaluation
            noteEncadrant = existingEvaluation.get();
            noteEncadrant.setTechnicalScore(request.getTechnicalScore());
            noteEncadrant.setReportScore(request.getReportScore());
            noteEncadrant.setProgressScore(request.getProgressScore());
            noteEncadrant.setProfessionalismScore(request.getProfessionalismScore());
            noteEncadrant.setCommentaire(request.getCommentaire());
            noteEncadrant.setDateEvaluation(LocalDateTime.now());
        } else {
            // Create new evaluation
            noteEncadrant = NoteEncadrant.builder()
                    .binome(binome)
                    .encadrant(encadrant)
                    .technicalScore(request.getTechnicalScore())
                    .reportScore(request.getReportScore())
                    .progressScore(request.getProgressScore())
                    .professionalismScore(request.getProfessionalismScore())
                    .commentaire(request.getCommentaire())
                    .dateEvaluation(LocalDateTime.now())
                    .anneeScolaire(currentYear)
                    .build();
        }
        
        // Save the evaluation
        noteEncadrant = noteEncadrantRepository.save(noteEncadrant);
        
        // Map to response DTO
        return mapToNoteEncadrantResponse(noteEncadrant);
    }
    
    /**
     * Submit report evaluation by a jury member
     */
    @Transactional
    public NoteRapportResponse submitRapportEvaluation(Long juryId, NoteRapportRequest request) {
        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Validate the jury
        Utilisateur jury = utilisateurRepository.findById(juryId)
                .orElseThrow(() -> new RuntimeException("Jury not found with id: " + juryId));
        
        // Validate the rapport
        Rapport rapport = rapportRepository.findById(request.getRapportId())
                .orElseThrow(() -> new RuntimeException("Rapport not found with id: " + request.getRapportId()));
        
        // Check if this rapport belongs to current year
        if (!rapport.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Cannot evaluate a rapport from a previous academic year");
        }
        
        // Check if an evaluation already exists for this jury and rapport
        Optional<NoteRapport> existingEvaluation = noteRapportRepository
                .findByRapportAndEvaluateurAndAnneeScolaire(rapport, jury, currentYear);
        
        NoteRapport noteRapport;
        
        if (existingEvaluation.isPresent()) {
            // Update existing evaluation
            noteRapport = existingEvaluation.get();
            noteRapport.setTechnicalScore(request.getTechnicalScore());
            noteRapport.setStructureScore(request.getStructureScore());
            noteRapport.setOriginalityScore(request.getOriginalityScore());
            noteRapport.setCommentaire(request.getCommentaire());
            noteRapport.setDateEvaluation(LocalDateTime.now());
        } else {
            // Create new evaluation
            noteRapport = NoteRapport.builder()
                    .rapport(rapport)
                    .evaluateur(jury)
                    .technicalScore(request.getTechnicalScore())
                    .structureScore(request.getStructureScore())
                    .originalityScore(request.getOriginalityScore())
                    .commentaire(request.getCommentaire())
                    .dateEvaluation(LocalDateTime.now())
                    .anneeScolaire(currentYear)
                    .build();
        }
        
        // Save the evaluation
        noteRapport = noteRapportRepository.save(noteRapport);
        
        // Map to response DTO
        return mapToNoteRapportResponse(noteRapport);
    }
    
    /**
     * Submit presentation/defense evaluation by a jury member
     * Each student is evaluated individually
     */
    @Transactional
    public NoteSoutenanceResponse submitSoutenanceEvaluation(Long juryId, NoteSoutenanceRequest request) {
        // Get current academic year
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Validate the jury
        Utilisateur jury = utilisateurRepository.findById(juryId)
                .orElseThrow(() -> new RuntimeException("Jury not found with id: " + juryId));
        
        // Validate the soutenance
        Soutenance soutenance = soutenanceRepository.findById(request.getSoutenanceId())
                .orElseThrow(() -> new RuntimeException("Soutenance not found with id: " + request.getSoutenanceId()));
        
        // Check if this soutenance belongs to current year
        if (!soutenance.getAnneeScolaire().getId().equals(currentYear.getId())) {
            throw new RuntimeException("Cannot evaluate a soutenance from a previous academic year");
        }
        
        // Validate that this jury is assigned to this soutenance
        if (!soutenance.getJury1().getId().equals(juryId) && !soutenance.getJury2().getId().equals(juryId)) {
            throw new RuntimeException("This jury is not assigned to the specified soutenance");
        }
        
        // Validate the student (etudiant)
        Utilisateur etudiant = utilisateurRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + request.getEtudiantId()));
        
        // Check if the student is part of the binome for this soutenance
        Binome binome = soutenance.getBinome();
        if (!binome.getEtudiant1().getId().equals(etudiant.getId()) && 
            (binome.getEtudiant2() == null || !binome.getEtudiant2().getId().equals(etudiant.getId()))) {
            throw new RuntimeException("This student is not part of the binome for this soutenance");
        }
        
        // Check if an evaluation already exists for this jury, soutenance, and student
        Optional<NoteSoutenance> existingEvaluation = noteSoutenanceRepository
                .findByJuryAndSoutenanceAndEtudiantAndAnneeScolaire(jury, soutenance, etudiant, currentYear);
        
        NoteSoutenance noteSoutenance;
        
        if (existingEvaluation.isPresent()) {
            // Update existing evaluation
            noteSoutenance = existingEvaluation.get();
            noteSoutenance.setPresentationScore(request.getPresentationScore());
            noteSoutenance.setQaScore(request.getQaScore());
            noteSoutenance.setTimeManagementScore(request.getTimeManagementScore());
            noteSoutenance.setCommentaire(request.getCommentaire());
            noteSoutenance.setDateEvaluation(LocalDateTime.now());
        } else {
            // Create new evaluation
            noteSoutenance = NoteSoutenance.builder()
                    .soutenance(soutenance)
                    .jury(jury)
                    .etudiant(etudiant)
                    .presentationScore(request.getPresentationScore())
                    .qaScore(request.getQaScore())
                    .timeManagementScore(request.getTimeManagementScore())
                    .commentaire(request.getCommentaire())
                    .dateEvaluation(LocalDateTime.now())
                    .anneeScolaire(currentYear)
                    .build();
        }
        
        // Save the evaluation
        noteSoutenance = noteSoutenanceRepository.save(noteSoutenance);
        
        // Map to response DTO
        return mapToNoteSoutenanceResponse(noteSoutenance);
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
     * Map NoteRapport entity to NoteRapportResponse DTO
     */
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
}