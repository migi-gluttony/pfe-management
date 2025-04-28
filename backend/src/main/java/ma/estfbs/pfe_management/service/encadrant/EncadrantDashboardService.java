package ma.estfbs.pfe_management.service.encadrant;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.encadrant.EncadrantDashboardDTOs.*;
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
public class EncadrantDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final BinomeRepository binomeRepository;
    private final DocumentsEvaluationRepository documentsEvaluationRepository;
    private final NoteEncadrantRepository noteEncadrantRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final AcademicYearService academicYearService;

    @Transactional(readOnly = true)
    public EncadrantDashboardResponse getEncadrantDashboard(Long encadrantId) {
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));
        
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        return EncadrantDashboardResponse.builder()
                .encadrantInfo(getEncadrantInfo(encadrant, currentYear))
                .binomeStats(getBinomeStats(encadrant, currentYear))
                .documentStats(getDocumentStats(encadrant, currentYear))
                .evaluationStats(getEvaluationStats(encadrant, currentYear))
                .soutenanceStats(getSoutenanceStats(encadrant, currentYear))
                .upcomingSoutenances(getUpcomingSoutenances(encadrant, currentYear))
                .pendingDocuments(getPendingDocuments(encadrant, currentYear))
                .recentActivities(getRecentActivities(encadrant, currentYear))
                .reminders(getReminders(encadrant, currentYear))
                .build();
    }

    private EncadrantInfo getEncadrantInfo(Utilisateur encadrant, AnneeScolaire currentYear) {
        return EncadrantInfo.builder()
                .id(encadrant.getId())
                .nom(encadrant.getNom())
                .prenom(encadrant.getPrenom())
                .email(encadrant.getEmail())
                .anneeAcademique(currentYear.getAnnee())
                .build();
    }

    private BinomeStats getBinomeStats(Utilisateur encadrant, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        
        int evaluatedCount = 0;
        int completedCount = 0;
        int pendingDocsCount = 0;
        double totalProgressScore = 0;
        int progressScoreCount = 0;

        for (Binome binome : binomes) {
            // Check if binome has evaluation
            if (noteEncadrantRepository.findByBinomeAndEncadrantAndAnneeScolaire(binome, encadrant, currentYear).isPresent()) {
                evaluatedCount++;
                NoteEncadrant note = noteEncadrantRepository.findByBinomeAndEncadrantAndAnneeScolaire(binome, encadrant, currentYear).get();
                totalProgressScore += note.getProgressScore();
                progressScoreCount++;
            }
            
            // Check if project is completed (has final report and soutenance)
            boolean hasFinalReport = !binome.getRapports().isEmpty();
            boolean hasSoutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(binome, currentYear).isPresent();
            if (hasFinalReport && hasSoutenance) {
                completedCount++;
            }
            
            // Check for pending documents
            List<DocumentsEvaluation> docs = documentsEvaluationRepository.findByBinomeAndAnneeScolaire(binome, currentYear);
            if (docs.stream().anyMatch(doc -> doc.getCommentaire() == null)) {
                pendingDocsCount++;
            }
        }

        return BinomeStats.builder()
                .totalBinomes(binomes.size())
                .activeBinomes(binomes.size() - completedCount)
                .completedProjects(completedCount)
                .binomesWithPendingDocuments(pendingDocsCount)
                .evaluatedBinomes(evaluatedCount)
                .averageProgressScore(progressScoreCount > 0 ? totalProgressScore / progressScoreCount : 0.0)
                .build();
    }

    private DocumentStats getDocumentStats(Utilisateur encadrant, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        
        int totalDocs = 0;
        int pendingReviews = 0;
        int reviewedToday = 0;
        int submittedThisWeek = 0;
        int overdueReviews = 0;
        double totalResponseTime = 0;
        int responseTimeCount = 0;

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        for (Binome binome : binomes) {
            List<DocumentsEvaluation> docs = documentsEvaluationRepository.findByBinomeAndAnneeScolaire(binome, currentYear);
            totalDocs += docs.size();

            for (DocumentsEvaluation doc : docs) {
                if (doc.getCommentaire() == null) {
                    pendingReviews++;
                    
                    // Check if overdue (more than 5 days)
                    if (ChronoUnit.DAYS.between(doc.getDateSoumission().toLocalDate(), today) > 5) {
                        overdueReviews++;
                    }
                } else {
                    // Check if reviewed today
                    if (doc.getDateSoumission().toLocalDate().equals(today)) {
                        reviewedToday++;
                    }
                    
                    // Calculate response time (assuming comment date is in the commentaire field)
                    // This would need actual implementation based on your data model
                    // For now, using a placeholder
                    totalResponseTime += 48; // placeholder 48 hours
                    responseTimeCount++;
                }
                
                // Check if submitted this week
                if (doc.getDateSoumission().toLocalDate().isAfter(weekAgo)) {
                    submittedThisWeek++;
                }
            }
        }

        return DocumentStats.builder()
                .totalDocuments(totalDocs)
                .pendingReviews(pendingReviews)
                .reviewedToday(reviewedToday)
                .documentsSubmittedThisWeek(submittedThisWeek)
                .averageResponseTime(responseTimeCount > 0 ? totalResponseTime / responseTimeCount : 0.0)
                .overdueReviews(overdueReviews)
                .build();
    }

    private EvaluationStats getEvaluationStats(Utilisateur encadrant, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        List<NoteEncadrant> evaluations = noteEncadrantRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        
        int highPerforming = 0;
        int needsImprovement = 0;
        double totalGrade = 0;
        
        // Grade distribution
        int excellent = 0;
        int good = 0;
        int fair = 0;
        int pass = 0;
        int fail = 0;

        for (NoteEncadrant note : evaluations) {
            double weightedScore = note.calculateWeightedScore(null); // Using default weights
            totalGrade += weightedScore;

            if (weightedScore >= 16) {
                highPerforming++;
                excellent++;
            } else if (weightedScore >= 14) {
                good++;
            } else if (weightedScore >= 12) {
                fair++;
            } else if (weightedScore >= 10) {
                pass++;
            } else {
                needsImprovement++;
                fail++;
            }
        }

        GradeDistributionDTO distribution = GradeDistributionDTO.builder()
                .excellentCount(excellent)
                .goodCount(good)
                .fairCount(fair)
                .passCount(pass)
                .failCount(fail)
                .build();

        return EvaluationStats.builder()
                .evaluatedBinomes(evaluations.size())
                .pendingEvaluations(binomes.size() - evaluations.size())
                .averageGrade(evaluations.isEmpty() ? 0.0 : totalGrade / evaluations.size())
                .highPerformingBinomes(highPerforming)
                .needsImprovementBinomes(needsImprovement)
                .gradeDistribution(distribution)
                .build();
    }

    private SoutenanceStats getSoutenanceStats(Utilisateur encadrant, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        List<Soutenance> soutenances = new ArrayList<>();
        
        for (Binome binome : binomes) {
            soutenanceRepository.findByBinomeAndAnneeScolaire(binome, currentYear).ifPresent(soutenances::add);
        }

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        int upcoming = 0;
        int todayCount = 0;
        int thisWeek = 0;
        LocalDate nextSoutenance = null;

        for (Soutenance soutenance : soutenances) {
            if (soutenance.getDate().isAfter(today)) {
                upcoming++;
                if (nextSoutenance == null || soutenance.getDate().isBefore(nextSoutenance)) {
                    nextSoutenance = soutenance.getDate();
                }
            }
            if (soutenance.getDate().equals(today)) {
                todayCount++;
            }
            if (soutenance.getDate().isAfter(today) && soutenance.getDate().isBefore(nextWeek)) {
                thisWeek++;
            }
        }

        return SoutenanceStats.builder()
                .totalSoutenances(soutenances.size())
                .upcomingSoutenances(upcoming)
                .todaySoutenances(todayCount)
                .scheduledThisWeek(thisWeek)
                .nextSoutenanceDate(nextSoutenance)
                .build();
    }

    private List<UpcomingSoutenanceDTO> getUpcomingSoutenances(Utilisateur encadrant, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        List<UpcomingSoutenanceDTO> upcomingList = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        for (Binome binome : binomes) {
            soutenanceRepository.findByBinomeAndAnneeScolaire(binome, currentYear).ifPresent(soutenance -> {
                if (soutenance.getDate().isAfter(today) || soutenance.getDate().equals(today)) {
                    boolean isEvaluated = noteEncadrantRepository.findByBinomeAndEncadrantAndAnneeScolaire(binome, encadrant, currentYear).isPresent();
                    int daysUntil = (int) ChronoUnit.DAYS.between(today, soutenance.getDate());
                    
                    upcomingList.add(UpcomingSoutenanceDTO.builder()
                            .id(soutenance.getId())
                            .date(soutenance.getDate())
                            .heure(soutenance.getHeure())
                            .salle(soutenance.getSalle().getNom())
                            .binome(mapToBinomeInfoDTO(binome))
                            .sujetTitre(binome.getSujet().getTitre())
                            .isEvaluated(isEvaluated)
                            .daysUntilSoutenance(daysUntil)
                            .build());
                }
            });
        }
        
        return upcomingList.stream()
                .sorted(Comparator.comparing(UpcomingSoutenanceDTO::getDate)
                        .thenComparing(UpcomingSoutenanceDTO::getHeure))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<PendingDocumentDTO> getPendingDocuments(Utilisateur encadrant, AnneeScolaire currentYear) {
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        List<PendingDocumentDTO> pendingDocs = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        for (Binome binome : binomes) {
            List<DocumentsEvaluation> docs = documentsEvaluationRepository.findByBinomeAndAnneeScolaire(binome, currentYear);
            
            for (DocumentsEvaluation doc : docs) {
                if (doc.getCommentaire() == null) {
                    int daysWaiting = (int) ChronoUnit.DAYS.between(doc.getDateSoumission().toLocalDate(), today);
                    String urgency = daysWaiting > 7 ? "HIGH" : daysWaiting > 3 ? "MEDIUM" : "LOW";
                    
                    pendingDocs.add(PendingDocumentDTO.builder()
                            .id(doc.getId())
                            .titre(doc.getTitre())
                            .dateSoumission(doc.getDateSoumission())
                            .binome(mapToBinomeInfoDTO(binome))
                            .urgency(urgency)
                            .daysWaiting(daysWaiting)
                            .build());
                }
            }
        }
        
        return pendingDocs.stream()
                .sorted(Comparator.comparing(PendingDocumentDTO::getUrgency)
                        .thenComparing(PendingDocumentDTO::getDateSoumission))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<RecentActivityDTO> getRecentActivities(Utilisateur encadrant, AnneeScolaire currentYear) {
        List<RecentActivityDTO> activities = new ArrayList<>();
        
        // Get recent evaluations
        List<NoteEncadrant> recentEvaluations = noteEncadrantRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear)
                .stream()
                .sorted(Comparator.comparing(NoteEncadrant::getDateEvaluation).reversed())
                .limit(3)
                .collect(Collectors.toList());
        
        for (NoteEncadrant evaluation : recentEvaluations) {
            activities.add(RecentActivityDTO.builder()
                    .id(evaluation.getId())
                    .type("BINOME_EVALUATED")
                    .description("Évaluation du binôme de " + 
                            evaluation.getBinome().getEtudiant1().getPrenom() + " " + 
                            evaluation.getBinome().getEtudiant1().getNom())
                    .timestamp(evaluation.getDateEvaluation())
                    .icon("pi pi-check-circle")
                    .severity("success")
                    .build());
        }
        
        // Get recent document reviews
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        for (Binome binome : binomes) {
            List<DocumentsEvaluation> docs = documentsEvaluationRepository.findByBinomeAndAnneeScolaire(binome, currentYear);
            
            docs.stream()
                .filter(doc -> doc.getCommentaire() != null)
                .sorted(Comparator.comparing(DocumentsEvaluation::getDateSoumission).reversed())
                .limit(3)
                .forEach(doc -> {
                    activities.add(RecentActivityDTO.builder()
                            .id(doc.getId())
                            .type("DOCUMENT_REVIEWED")
                            .description("Révision du document \"" + doc.getTitre() + "\"")
                            .timestamp(doc.getDateSoumission())
                            .icon("pi pi-file-edit")
                            .severity("info")
                            .build());
                });
        }
        
        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDTO::getTimestamp).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<ReminderDTO> getReminders(Utilisateur encadrant, AnneeScolaire currentYear) {
        List<ReminderDTO> reminders = new ArrayList<>();
        
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);
        LocalDate today = LocalDate.now();
        
        // Check for pending evaluations
        int pendingEvaluations = 0;
        for (Binome binome : binomes) {
            if (!noteEncadrantRepository.findByBinomeAndEncadrantAndAnneeScolaire(binome, encadrant, currentYear).isPresent()) {
                pendingEvaluations++;
            }
        }
        
        if (pendingEvaluations > 0) {
            reminders.add(ReminderDTO.builder()
                    .type("PENDING_EVALUATIONS")
                    .title("Évaluations en attente")
                    .message(pendingEvaluations + " binôme(s) à évaluer")
                    .severity("warning")
                    .actionUrl("/encadrant/grading")
                    .build());
        }
        
        // Check for pending document reviews
        int pendingReviews = 0;
        List<DocumentsEvaluation> overdueDocuments = new ArrayList<>();
        
        for (Binome binome : binomes) {
            List<DocumentsEvaluation> docs = documentsEvaluationRepository.findByBinomeAndAnneeScolaire(binome, currentYear);
            for (DocumentsEvaluation doc : docs) {
                if (doc.getCommentaire() == null) {
                    pendingReviews++;
                    int daysWaiting = (int) ChronoUnit.DAYS.between(doc.getDateSoumission().toLocalDate(), today);
                    if (daysWaiting > 5) {
                        overdueDocuments.add(doc);
                    }
                }
            }
        }
        
        if (pendingReviews > 0) {
            String severity = overdueDocuments.isEmpty() ? "info" : "critical";
            reminders.add(ReminderDTO.builder()
                    .type("PENDING_REVIEWS")
                    .title("Documents à réviser")
                    .message(pendingReviews + " document(s) en attente de révision" + 
                            (overdueDocuments.isEmpty() ? "" : " dont " + overdueDocuments.size() + " en retard"))
                    .severity(severity)
                    .actionUrl("/encadrant/document-evaluation")
                    .build());
        }
        
        // Check for upcoming soutenances
        List<UpcomingSoutenanceDTO> upcomingSoutenances = getUpcomingSoutenances(encadrant, currentYear);
        for (UpcomingSoutenanceDTO soutenance : upcomingSoutenances) {
            if (soutenance.getDaysUntilSoutenance() <= 7) {
                reminders.add(ReminderDTO.builder()
                        .type("UPCOMING_SOUTENANCE")
                        .title("Soutenance à venir")
                        .message("Soutenance de " + 
                                soutenance.getBinome().getEtudiant1().getPrenom() + " " + 
                                soutenance.getBinome().getEtudiant1().getNom() + 
                                " dans " + soutenance.getDaysUntilSoutenance() + " jours")
                        .severity(soutenance.getDaysUntilSoutenance() <= 2 ? "critical" : "warning")
                        .actionUrl("/encadrant/soutenances")
                        .daysRemaining(soutenance.getDaysUntilSoutenance())
                        .build());
            }
        }
        
        return reminders;
    }

    private BinomeInfoDTO mapToBinomeInfoDTO(Binome binome) {
        // Calculate project progress based on documents submitted, evaluation, etc.
        int progress = calculateProjectProgress(binome);
        
        return BinomeInfoDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null)
                .projectProgress(progress)
                .build();
    }

    private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
        return StudentDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }

    private int calculateProjectProgress(Binome binome) {
        // Simple progress calculation based on milestones
        int progress = 0;
        
        // Has documents submitted: 30%
        if (!binome.getDocumentsEvaluations().isEmpty()) {
            progress += 30;
        }
        
        // Has encadrant evaluation: 30%
        if (binome.getNotesFinales().stream().anyMatch(n -> n.getNoteEncadrant() != null)) {
            progress += 30;
        }
        
        // Has final report: 20%
        if (!binome.getRapports().isEmpty()) {
            progress += 20;
        }
        
        // Has soutenance scheduled: 20%
        if (binome.getSoutenance() != null) {
            progress += 20;
        }
        
        return progress;
    }
}