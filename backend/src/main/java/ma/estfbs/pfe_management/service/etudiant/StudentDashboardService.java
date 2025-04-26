package ma.estfbs.pfe_management.service.etudiant;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.StudentDashboardDTOs.*;
import ma.estfbs.pfe_management.model.*;
import ma.estfbs.pfe_management.repository.*;
import ma.estfbs.pfe_management.service.AcademicYearService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final BinomeRepository binomeRepository;
    private final RapportRepository rapportRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final DocumentsEvaluationRepository documentsEvaluationRepository;
    private final NoteFinaleRepository noteFinaleRepository;
    private final AcademicYearService academicYearService;

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard(Long studentId) {
        Utilisateur utilisateur = utilisateurRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                .orElseThrow(() -> new RuntimeException("Information étudiant non trouvée"));
        
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        return StudentDashboardResponse.builder()
                .studentInfo(getStudentInfo(utilisateur, etudiant, currentYear))
                .binomeInfo(getBinomeInfo(utilisateur, currentYear))
                .sujetInfo(getSujetInfo(utilisateur, currentYear))
                .soutenanceInfo(getSoutenanceInfo(utilisateur, currentYear))
                .encadrantInfo(getEncadrantInfo(utilisateur, currentYear))
                .documentStats(getDocumentStats(utilisateur, currentYear))
                .noteHistory(getNoteHistory(utilisateur))
                .currentNoteStatus(getCurrentNoteStatus(utilisateur, currentYear))
                .reminders(getReminders(utilisateur, currentYear))
                .quickActions(getQuickActions(utilisateur, currentYear))
                .build();
    }

    private StudentInfo getStudentInfo(Utilisateur utilisateur, Etudiant etudiant, AnneeScolaire currentYear) {
        return StudentInfo.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .filiere(etudiant.getFiliere().getNom())
                .anneeAcademique(currentYear.getAnnee())
                .build();
    }

    private BinomeInfo getBinomeInfo(Utilisateur utilisateur, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                utilisateur, utilisateur, currentYear);
        
        if (binomes.isEmpty()) {
            return BinomeInfo.builder()
                    .hasBinome(false)
                    .build();
        }
        
        Binome binome = binomes.get(0);
        boolean isSolo = binome.getEtudiant2() == null;
        
        Utilisateur partner = null;
        if (!isSolo) {
            partner = binome.getEtudiant1().getId().equals(utilisateur.getId()) ? 
                     binome.getEtudiant2() : binome.getEtudiant1();
        }
        
        return BinomeInfo.builder()
                .hasBinome(true)
                .binomeId(binome.getId())
                .isSolo(isSolo)
                .partner(partner != null ? StudentPartnerDTO.builder()
                        .id(partner.getId())
                        .nom(partner.getNom())
                        .prenom(partner.getPrenom())
                        .email(partner.getEmail())
                        .build() : null)
                .build();
    }

    private SujetInfo getSujetInfo(Utilisateur utilisateur, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                utilisateur, utilisateur, currentYear);
        
        if (binomes.isEmpty() || binomes.get(0).getSujet() == null) {
            return SujetInfo.builder()
                    .hasSujet(false)
                    .build();
        }
        
        Sujet sujet = binomes.get(0).getSujet();
        boolean hasEncadrant = binomes.get(0).getEncadrant() != null;
        
        return SujetInfo.builder()
                .hasSujet(true)
                .sujetId(sujet.getId())
                .titre(sujet.getTitre())
                .theme(sujet.getTheme())
                .description(sujet.getDescription())
                .hasEncadrant(hasEncadrant)
                .build();
    }

    private SoutenanceInfo getSoutenanceInfo(Utilisateur utilisateur, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                utilisateur, utilisateur, currentYear);
        
        if (binomes.isEmpty()) {
            return SoutenanceInfo.builder()
                    .isScheduled(false)
                    .build();
        }
        
        Soutenance soutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(
                binomes.get(0), currentYear).orElse(null);
        
        if (soutenance == null) {
            return SoutenanceInfo.builder()
                    .isScheduled(false)
                    .build();
        }
        
        int daysUntil = (int) ChronoUnit.DAYS.between(LocalDate.now(), soutenance.getDate());
        
        return SoutenanceInfo.builder()
                .isScheduled(true)
                .date(soutenance.getDate())
                .heure(soutenance.getHeure())
                .salle(soutenance.getSalle().getNom())
                .jury1(soutenance.getJury1().getNom() + " " + soutenance.getJury1().getPrenom())
                .jury2(soutenance.getJury2().getNom() + " " + soutenance.getJury2().getPrenom())
                .daysUntilSoutenance(daysUntil)
                .build();
    }

    private EncadrantInfo getEncadrantInfo(Utilisateur utilisateur, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                utilisateur, utilisateur, currentYear);
        
        if (binomes.isEmpty() || binomes.get(0).getEncadrant() == null) {
            return EncadrantInfo.builder()
                    .hasEncadrant(false)
                    .build();
        }
        
        Utilisateur encadrant = binomes.get(0).getEncadrant();
        
        return EncadrantInfo.builder()
                .hasEncadrant(true)
                .nom(encadrant.getNom())
                .prenom(encadrant.getPrenom())
                .email(encadrant.getEmail())
                .build();
    }

    private DocumentStats getDocumentStats(Utilisateur utilisateur, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                utilisateur, utilisateur, currentYear);
        
        if (binomes.isEmpty()) {
            return DocumentStats.builder()
                    .submittedDocuments(0)
                    .reviewedDocuments(0)
                    .hasSubmittedReport(false)
                    .reportCanBeModified(false)
                    .build();
        }
        
        Binome binome = binomes.get(0);
        List<DocumentsEvaluation> documents = documentsEvaluationRepository
                .findByBinomeAndAnneeScolaire(binome, currentYear);
        
        int submittedCount = documents.size();
        int reviewedCount = (int) documents.stream()
                .filter(doc -> doc.getCommentaire() != null)
                .count();
        
        Rapport rapport = rapportRepository.findTopByBinomeAndAnneeScolaireOrderByIdDesc(
                binome, currentYear);
        
        boolean hasReport = rapport != null;
        boolean canModifyReport = false;
        
        if (hasReport) {
            Soutenance soutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(
                    binome, currentYear).orElse(null);
            
            if (soutenance != null) {
                long daysUntilSoutenance = ChronoUnit.DAYS.between(LocalDate.now(), soutenance.getDate());
                canModifyReport = daysUntilSoutenance > 3;
            }
        }
        
        LocalDateTime lastSubmission = null;
        if (!documents.isEmpty()) {
            lastSubmission = documents.stream()
                    .map(DocumentsEvaluation::getDateSoumission)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }
        
        return DocumentStats.builder()
                .submittedDocuments(submittedCount)
                .reviewedDocuments(reviewedCount)
                .hasSubmittedReport(hasReport)
                .reportCanBeModified(canModifyReport)
                .lastSubmissionDate(lastSubmission)
                .build();
    }

    private List<NoteHistoryDTO> getNoteHistory(Utilisateur utilisateur) {
        List<NoteFinale> allNotes = noteFinaleRepository.findAll().stream()
                .filter(note -> note.getEtudiant().getId().equals(utilisateur.getId()))
                .sorted(Comparator.comparing(note -> 
                        note.getAnneeScolaire().getAnnee(), Comparator.reverseOrder()))
                .collect(Collectors.toList());
        
        return allNotes.stream()
                .map(this::mapToNoteHistoryDTO)
                .collect(Collectors.toList());
    }

    private NoteHistoryDTO mapToNoteHistoryDTO(NoteFinale note) {
        Pourcentage percentages = note.getAnneeScolaire().getPourcentage();
        double finalScore = note.calculateFinalScore(percentages);
        
        return NoteHistoryDTO.builder()
                .anneeAcademique(note.getAnneeScolaire().getAnnee())
                .noteFinale((int) Math.round(finalScore))
                .noteRapport(note.getNoteRapport())
                .noteSoutenance(note.getNoteSoutenance())
                .noteEncadrant(note.getNoteEncadrant())
                .mention(calculateMention(finalScore))
                .build();
    }

    private String calculateMention(double note) {
        if (note >= 16) return "Très Bien";
        if (note >= 14) return "Bien";
        if (note >= 12) return "Assez Bien";
        if (note >= 10) return "Passable";
        return "Insuffisant";
    }

    private CurrentNoteStatus getCurrentNoteStatus(Utilisateur utilisateur, AnneeScolaire currentYear) {
        Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                .orElseThrow(() -> new RuntimeException("Information étudiant non trouvée"));
        
        // Check if all notes for the filiere are published
        List<Etudiant> allStudentsInFiliere = etudiantRepository
                .findByFiliereAndAnneeScolaire(etudiant.getFiliere(), currentYear);
        
        long totalStudents = allStudentsInFiliere.size();
        long studentsWithNotes = allStudentsInFiliere.stream()
                .filter(student -> noteFinaleRepository
                        .findByEtudiantAndAnneeScolaire(student.getUtilisateur(), currentYear)
                        .isPresent())
                .count();
        
        boolean allNotesPublished = totalStudents == studentsWithNotes;
        
        NoteFinale currentNote = noteFinaleRepository
                .findByEtudiantAndAnneeScolaire(utilisateur, currentYear)
                .orElse(null);
        
        if (currentNote == null || !allNotesPublished) {
            return CurrentNoteStatus.builder()
                    .allNotesPublished(false)
                    .build();
        }
        
        Pourcentage percentages = currentYear.getPourcentage();
        double finalScore = currentNote.calculateFinalScore(percentages);
        
        return CurrentNoteStatus.builder()
                .allNotesPublished(true)
                .noteFinale((int) Math.round(finalScore))
                .noteRapport(currentNote.getNoteRapport())
                .noteSoutenance(currentNote.getNoteSoutenance())
                .noteEncadrant(currentNote.getNoteEncadrant())
                .hasNoteRapport(currentNote.getNoteRapport() != null)
                .hasNoteSoutenance(currentNote.getNoteSoutenance() != null)
                .hasNoteEncadrant(currentNote.getNoteEncadrant() != null)
                .build();
    }

    private List<ReminderDTO> getReminders(Utilisateur utilisateur, AnneeScolaire currentYear) {
        List<ReminderDTO> reminders = new ArrayList<>();
        
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                utilisateur, utilisateur, currentYear);
        
        if (binomes.isEmpty()) {
            reminders.add(ReminderDTO.builder()
                    .type("BINOME")
                    .title("Choisir un binôme")
                    .message("Vous n'avez pas encore de binôme. Veuillez choisir un partenaire ou continuer seul.")
                    .severity("critical")
                    .actionUrl("/etudiant/binome")
                    .build());
            return reminders;
        }
        
        Binome binome = binomes.get(0);
        
        if (binome.getSujet() == null) {
            reminders.add(ReminderDTO.builder()
                    .type("SUJET")
                    .title("Choisir un sujet")
                    .message("Vous devez choisir un sujet pour votre projet de fin d'études.")
                    .severity("critical")
                    .actionUrl("/etudiant/sujet")
                    .build());
        }
        
        // Report submission reminder
        Soutenance soutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(
                binome, currentYear).orElse(null);
        
        if (soutenance != null) {
            int daysUntil = (int) ChronoUnit.DAYS.between(LocalDate.now(), soutenance.getDate());
            
            Rapport rapport = rapportRepository.findTopByBinomeAndAnneeScolaireOrderByIdDesc(
                    binome, currentYear);
            
            if (rapport == null && daysUntil >= 0) {
                String severity = daysUntil <= 5 ? "critical" : 
                                daysUntil <= 10 ? "warning" : "info";
                
                reminders.add(ReminderDTO.builder()
                        .type("RAPPORT")
                        .title("Dépôt du rapport final")
                        .message("Vous devez déposer votre rapport au moins 3 jours avant la soutenance.")
                        .severity(severity)
                        .actionUrl("/etudiant/rapport")
                        .daysRemaining(daysUntil - 3)
                        .build());
            }
            
            // Soutenance preparation reminder
            if (daysUntil >= 0 && daysUntil <= 7) {
                reminders.add(ReminderDTO.builder()
                        .type("SOUTENANCE")
                        .title("Préparation de la soutenance")
                        .message("Votre soutenance approche. Assurez-vous d'être bien préparé.")
                        .severity("warning")
                        .daysRemaining(daysUntil)
                        .build());
            }
        }
        
        return reminders;
    }

    private QuickActionsInfo getQuickActions(Utilisateur utilisateur, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                utilisateur, utilisateur, currentYear);
        
        boolean hasBinome = !binomes.isEmpty();
        boolean hasSujet = hasBinome && binomes.get(0).getSujet() != null;
        
        // Check if report can be submitted/modified
        boolean canSubmitReport = false;
        if (hasBinome) {
            Soutenance soutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(
                    binomes.get(0), currentYear).orElse(null);
            
            if (soutenance != null) {
                long daysUntilSoutenance = ChronoUnit.DAYS.between(LocalDate.now(), soutenance.getDate());
                canSubmitReport = daysUntilSoutenance > 3;
            }
        }
        
        return QuickActionsInfo.builder()
                .canChooseBinome(!hasBinome)
                .canChooseSujet(hasBinome && !hasSujet)
                .canSubmitDocuments(hasBinome && hasSujet)
                .canSubmitReport(hasBinome && canSubmitReport)
                .build();
    }
}