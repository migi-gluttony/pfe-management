package ma.estfbs.pfe_management.service.chefDeDepartement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.*;
import ma.estfbs.pfe_management.model.*;
import ma.estfbs.pfe_management.repository.*;
import ma.estfbs.pfe_management.service.AcademicYearService;

@Service
@RequiredArgsConstructor
public class NoteManagementService {

        private final NoteFinaleRepository noteFinaleRepository;
        private final NoteEncadrantRepository noteEncadrantRepository;
        private final NoteRapportRepository noteRapportRepository;
        private final NoteSoutenanceRepository noteSoutenanceRepository;
        private final RapportRepository rapportRepository;
        private final BinomeRepository binomeRepository;
        private final EtudiantRepository etudiantRepository;
        private final FiliereRepository filiereRepository;
        private final PourcentageRepository pourcentageRepository;
        private final PourcentageEncadrantRepository pourcentageEncadrantRepository;
        private final PourcentageRapportRepository pourcentageRapportRepository;
        private final PourcentageSoutenanceRepository pourcentageSoutenanceRepository;
        private final AcademicYearService academicYearService;

        /**
         * Get all student notes with filières and percentages for the current academic
         * year
         */
        public NoteManagementResponse getAllNotesWithFilieres() {
                List<NoteDTO> notes = new ArrayList<>();

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Get all filières
                List<FiliereDTO> filieres = filiereRepository.findAll().stream()
                                .map(this::mapToFiliereDTO)
                                .collect(Collectors.toList());

                // Get the grade percentages configuration for current year
                Pourcentage pourcentage = pourcentageRepository.findByAnneeScolaire(currentYear);
                if (pourcentage == null) {
                        pourcentage = pourcentageRepository.findTopByOrderByIdDesc();
                }
                PourcentageDTO pourcentageDTO = mapToPourcentageDTO(pourcentage);

                // Get all students for current year
                List<Etudiant> allStudents = etudiantRepository.findByAnneeScolaire(currentYear);

                for (Etudiant etudiant : allStudents) {
                        Utilisateur utilisateur = etudiant.getUtilisateur();

                        // Find the student's binôme for current year
                        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                                        utilisateur, utilisateur, currentYear);

                        if (binomes.isEmpty())
                                continue;

                        Binome binome = binomes.get(0);

                        // Get aggregate notes for each component
                        Double noteRapport = calculateAggregateRapportNote(binome, currentYear);
                        Double noteSoutenance = calculateAggregateSoutenanceNote(utilisateur, currentYear);
                        Double noteEncadrant = calculateAggregateEncadrantNote(binome, currentYear);

                        // Get the filière info
                        Filiere filiere = etudiant.getFiliere();

                        // Create note DTO
                        NoteDTO noteDTO = NoteDTO.builder()
                                        .id(utilisateur.getId())
                                        .etudiant(mapToEtudiantDTO(utilisateur))
                                        .noteRapport(noteRapport != null ? (int) Math.round(noteRapport) : null)
                                        .noteSoutenance(noteSoutenance != null ? (int) Math.round(noteSoutenance)
                                                        : null)
                                        .noteEncadrant(noteEncadrant != null ? (int) Math.round(noteEncadrant) : null)
                                        .filiereId(filiere.getId())
                                        .filiereName(filiere.getNom())
                                        .build();

                        notes.add(noteDTO);
                }

                return NoteManagementResponse.builder()
                                .notes(notes)
                                .filieres(filieres)
                                .pourcentages(pourcentageDTO)
                                .build();
        }

        /**
         * Get notes filtered by filière
         */
        public List<NoteDTO> getNotesByFiliere(Long filiereId) {
                Filiere filiere = filiereRepository.findById(filiereId)
                                .orElseThrow(() -> new RuntimeException("Filière not found with id: " + filiereId));

                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
                List<Etudiant> filiereStudents = etudiantRepository.findByFiliereAndAnneeScolaire(filiere, currentYear);
                List<NoteDTO> notes = new ArrayList<>();

                for (Etudiant etudiant : filiereStudents) {
                        Utilisateur utilisateur = etudiant.getUtilisateur();

                        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                                        utilisateur, utilisateur, currentYear);

                        if (binomes.isEmpty())
                                continue;

                        Binome binome = binomes.get(0);

                        Double noteRapport = calculateAggregateRapportNote(binome, currentYear);
                        Double noteSoutenance = calculateAggregateSoutenanceNote(utilisateur, currentYear);
                        Double noteEncadrant = calculateAggregateEncadrantNote(binome, currentYear);

                        NoteDTO noteDTO = NoteDTO.builder()
                                        .id(utilisateur.getId())
                                        .etudiant(mapToEtudiantDTO(utilisateur))
                                        .noteRapport(noteRapport != null ? (int) Math.round(noteRapport) : null)
                                        .noteSoutenance(noteSoutenance != null ? (int) Math.round(noteSoutenance)
                                                        : null)
                                        .noteEncadrant(noteEncadrant != null ? (int) Math.round(noteEncadrant) : null)
                                        .filiereId(filiere.getId())
                                        .filiereName(filiere.getNom())
                                        .build();

                        notes.add(noteDTO);
                }

                return notes;
        }

        /**
         * Get detailed note information for a specific student
         */
        public NoteDetailDTO getNoteDetailsForStudent(Long etudiantId) {
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                Utilisateur etudiant = etudiantRepository.findById(etudiantId)
                                .map(Etudiant::getUtilisateur)
                                .orElseThrow(() -> new RuntimeException("Student not found with id: " + etudiantId));

                List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                                etudiant, etudiant, currentYear);

                if (binomes.isEmpty()) {
                        // Return empty object instead of throwing exception
                        return NoteDetailDTO.builder()
                                        .etudiantId(etudiantId)
                                        .build();
                }

                Binome binome = binomes.get(0);

                // Get whatever data is available, even if partial
                NoteEncadrantDetailDTO encadrantDetail = getEncadrantDetail(binome, currentYear);
                NoteSoutenanceDetailDTO soutenanceDetail = getSoutenanceDetail(etudiant, currentYear);
                NoteRapportDetailDTO rapportDetail = getRapportDetail(binome, currentYear);

                return NoteDetailDTO.builder()
                                .etudiantId(etudiantId)
                                .noteEncadrantDetail(encadrantDetail)
                                .noteSoutenanceDetail(soutenanceDetail)
                                .noteRapportDetail(rapportDetail)
                                .build();
        }
        // Helper methods for calculating aggregate scores

        private Double calculateAggregateRapportNote(Binome binome, AnneeScolaire currentYear) {
                List<Rapport> rapports = rapportRepository.findByBinomeAndAnneeScolaire(binome, currentYear);
                if (rapports.isEmpty())
                        return null;

                List<NoteRapport> rapportNotes = new ArrayList<>();
                for (Rapport rapport : rapports) {
                        rapportNotes.addAll(noteRapportRepository.findByRapportAndAnneeScolaire(rapport, currentYear));
                }

                if (rapportNotes.isEmpty())
                        return null;

                PourcentageRapport percentages = pourcentageRapportRepository.findByAnneeScolaire(currentYear);

                return rapportNotes.stream()
                                .mapToDouble(note -> note.calculateWeightedScore(percentages))
                                .average()
                                .orElse(0.0);
        }

        private Double calculateAggregateSoutenanceNote(Utilisateur etudiant, AnneeScolaire currentYear) {
                List<NoteSoutenance> soutenanceNotes = noteSoutenanceRepository.findByEtudiantAndAnneeScolaire(etudiant,
                                currentYear);

                if (soutenanceNotes.isEmpty())
                        return null;

                PourcentageSoutenance percentages = pourcentageSoutenanceRepository.findByAnneeScolaire(currentYear);

                return soutenanceNotes.stream()
                                .mapToDouble(note -> note.calculateWeightedScore(percentages))
                                .average()
                                .orElse(0.0);
        }

        private Double calculateAggregateEncadrantNote(Binome binome, AnneeScolaire currentYear) {
                List<NoteEncadrant> encadrantNotes = noteEncadrantRepository.findByBinomeAndAnneeScolaire(binome,
                                currentYear);

                if (encadrantNotes.isEmpty())
                        return null;

                PourcentageEncadrant percentages = pourcentageEncadrantRepository.findByAnneeScolaire(currentYear);

                return encadrantNotes.stream()
                                .mapToDouble(note -> note.calculateWeightedScore(percentages))
                                .findFirst()
                                .orElse(0.0);
        }

        // Methods for fetching detailed note information

        private NoteEncadrantDetailDTO getEncadrantDetail(Binome binome, AnneeScolaire currentYear) {
                List<NoteEncadrant> encadrantNotes = noteEncadrantRepository.findByBinomeAndAnneeScolaire(binome,
                                currentYear);

                if (encadrantNotes.isEmpty())
                        return null;

                NoteEncadrant note = encadrantNotes.get(0);

                return NoteEncadrantDetailDTO.builder()
                                .technicalScore(note.getTechnicalScore())
                                .reportScore(note.getReportScore())
                                .progressScore(note.getProgressScore())
                                .professionalismScore(note.getProfessionalismScore())
                                .commentaire(note.getCommentaire())
                                .dateEvaluation(note.getDateEvaluation())
                                .encadrantNom(note.getEncadrant().getNom())
                                .encadrantPrenom(note.getEncadrant().getPrenom())
                                .build();
        }

        private NoteSoutenanceDetailDTO getSoutenanceDetail(Utilisateur etudiant, AnneeScolaire currentYear) {
                List<NoteSoutenance> soutenanceNotes = noteSoutenanceRepository.findByEtudiantAndAnneeScolaire(etudiant,
                                currentYear);

                if (soutenanceNotes.isEmpty())
                        return null;

                // Calculate average scores
                double avgPresentationScore = soutenanceNotes.stream()
                                .mapToInt(NoteSoutenance::getPresentationScore)
                                .average()
                                .orElse(0.0);

                double avgQaScore = soutenanceNotes.stream()
                                .mapToInt(NoteSoutenance::getQaScore)
                                .average()
                                .orElse(0.0);

                double avgTimeManagementScore = soutenanceNotes.stream()
                                .mapToInt(NoteSoutenance::getTimeManagementScore)
                                .average()
                                .orElse(0.0);

                String comments = soutenanceNotes.stream()
                                .map(NoteSoutenance::getCommentaire)
                                .filter(c -> c != null && !c.isEmpty())
                                .collect(Collectors.joining("\n"));

                // Map individual jury evaluations
                List<JuryEvaluationDTO> juryEvaluations = soutenanceNotes.stream()
                                .map(note -> JuryEvaluationDTO.builder()
                                                .presentationScore(note.getPresentationScore())
                                                .qaScore(note.getQaScore())
                                                .timeManagementScore(note.getTimeManagementScore())
                                                .commentaire(note.getCommentaire())
                                                .juryNom(note.getJury().getNom())
                                                .juryPrenom(note.getJury().getPrenom())
                                                .dateEvaluation(note.getDateEvaluation())
                                                .build())
                                .collect(Collectors.toList());

                return NoteSoutenanceDetailDTO.builder()
                                .presentationScore((int) Math.round(avgPresentationScore))
                                .qaScore((int) Math.round(avgQaScore))
                                .timeManagementScore((int) Math.round(avgTimeManagementScore))
                                .commentaire(comments)
                                .juryCount(soutenanceNotes.size())
                                .juryEvaluations(juryEvaluations)
                                .build();
        }

        private NoteRapportDetailDTO getRapportDetail(Binome binome, AnneeScolaire currentYear) {
                List<Rapport> rapports = rapportRepository.findByBinomeAndAnneeScolaire(binome, currentYear);

                if (rapports.isEmpty())
                        return null;

                Rapport rapport = rapports.get(0);
                List<NoteRapport> rapportNotes = noteRapportRepository.findByRapportAndAnneeScolaire(rapport,
                                currentYear);

                if (rapportNotes.isEmpty())
                        return null;

                double avgTechnicalScore = rapportNotes.stream()
                                .mapToInt(NoteRapport::getTechnicalScore)
                                .average()
                                .orElse(0.0);

                double avgStructureScore = rapportNotes.stream()
                                .mapToInt(NoteRapport::getStructureScore)
                                .average()
                                .orElse(0.0);

                double avgOriginalityScore = rapportNotes.stream()
                                .mapToInt(NoteRapport::getOriginalityScore)
                                .average()
                                .orElse(0.0);

                String comments = rapportNotes.stream()
                                .map(NoteRapport::getCommentaire)
                                .filter(c -> c != null && !c.isEmpty())
                                .collect(Collectors.joining("\n"));

                return NoteRapportDetailDTO.builder()
                                .technicalScore((int) Math.round(avgTechnicalScore))
                                .structureScore((int) Math.round(avgStructureScore))
                                .originalityScore((int) Math.round(avgOriginalityScore))
                                .commentaire(comments)
                                .titre(rapport.getTitre())
                                .dateSoumission(rapport.getDateSoumission())
                                .evaluateurCount(rapportNotes.size())
                                .build();
        }

        // Mapping methods

        private EtudiantDTO mapToEtudiantDTO(Utilisateur utilisateur) {
                return EtudiantDTO.builder()
                                .id(utilisateur.getId())
                                .nom(utilisateur.getNom())
                                .prenom(utilisateur.getPrenom())
                                .cne(utilisateur.getCne())
                                .build();
        }

        private FiliereDTO mapToFiliereDTO(Filiere filiere) {
                return FiliereDTO.builder()
                                .id(filiere.getId())
                                .nom(filiere.getNom())
                                .build();
        }

        private PourcentageDTO mapToPourcentageDTO(Pourcentage pourcentage) {
                if (pourcentage == null) {
                        return PourcentageDTO.builder()
                                        .pourcentageRapport(40)
                                        .pourcentageSoutenance(40)
                                        .pourcentageEncadrant(20)
                                        .build();
                }

                return PourcentageDTO.builder()
                                .pourcentageRapport(pourcentage.getPourcentageRapport())
                                .pourcentageSoutenance(pourcentage.getPourcentageSoutenance())
                                .pourcentageEncadrant(pourcentage.getPourcentageEncadrant())
                                .build();
        }
}