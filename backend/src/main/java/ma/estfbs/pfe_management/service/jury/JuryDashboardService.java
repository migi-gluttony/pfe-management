package ma.estfbs.pfe_management.service.jury;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.jury.JuryDashboardDTOs.*;
import ma.estfbs.pfe_management.model.*;
import ma.estfbs.pfe_management.repository.*;
import ma.estfbs.pfe_management.service.AcademicYearService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JuryDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final NoteSoutenanceRepository noteSoutenanceRepository;
    private final NoteRapportRepository noteRapportRepository;
    private final RapportRepository rapportRepository;
    private final AcademicYearService academicYearService;

    @Transactional(readOnly = true)
    public JuryDashboardResponse getJuryDashboard(Long juryId) {
        Utilisateur jury = utilisateurRepository.findById(juryId)
                .orElseThrow(() -> new RuntimeException("Jury not found with id: " + juryId));

        if (jury.getRole() != Utilisateur.Role.JURY) {
            throw new RuntimeException("User is not a jury member");
        }

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        return JuryDashboardResponse.builder()
                .juryInfo(buildJuryInfo(jury, currentYear))
                .soutenanceStats(buildSoutenanceStats(jury, currentYear))
                .reportStats(buildReportStats(jury, currentYear))
                .binomeStats(buildBinomeStats(jury, currentYear))
                .upcomingSoutenances(getUpcomingSoutenances(jury, currentYear))
                .recentActivities(getRecentActivities(jury, currentYear))
                .pendingEvaluations(getPendingEvaluations(jury, currentYear))
                .reminders(buildReminders(jury, currentYear))
                .build();
    }

    private JuryInfo buildJuryInfo(Utilisateur jury, AnneeScolaire currentYear) {
        return JuryInfo.builder()
                .id(jury.getId())
                .nom(jury.getNom())
                .prenom(jury.getPrenom())
                .email(jury.getEmail())
                .anneeAcademique(currentYear.getAnnee())
                .build();
    }

    private SoutenanceStats buildSoutenanceStats(Utilisateur jury, AnneeScolaire currentYear) {
        List<Soutenance> soutenances = soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury, currentYear);
        List<NoteSoutenance> evaluations = noteSoutenanceRepository.findByJuryAndAnneeScolaire(jury, currentYear);

        LocalDate today = LocalDate.now();
        int todaySoutenances = (int) soutenances.stream()
                .filter(s -> s.getDate().isEqual(today))
                .count();

        int upcomingSoutenances = (int) soutenances.stream()
                .filter(s -> s.getDate().isAfter(today))
                .count();

        int evaluatedSoutenances = evaluations.stream()
                .map(NoteSoutenance::getSoutenance)
                .collect(Collectors.toSet())
                .size();

        double averageScore = evaluations.stream()
                .mapToDouble(
                        eval -> (eval.getPresentationScore() + eval.getQaScore() + eval.getTimeManagementScore()) / 3.0)
                .average()
                .orElse(0.0);

        return SoutenanceStats.builder()
                .totalSoutenances(soutenances.size())
                .todaySoutenances(todaySoutenances)
                .upcomingSoutenances(upcomingSoutenances)
                .evaluatedSoutenances(evaluatedSoutenances)
                .pendingSoutenances(soutenances.size() - evaluatedSoutenances)
                .averageEvaluationScore(averageScore)
                .build();
    }

    private ReportStats buildReportStats(Utilisateur jury, AnneeScolaire currentYear) {
        List<Soutenance> soutenances = soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury, currentYear);
        List<Binome> binomes = soutenances.stream()
                .map(Soutenance::getBinome)
                .collect(Collectors.toList());

        List<Rapport> reports = new ArrayList<>();
        for (Binome binome : binomes) {
            reports.addAll(rapportRepository.findByBinomeAndAnneeScolaire(binome, currentYear));
        }

        List<NoteRapport> evaluations = noteRapportRepository.findByEvaluateurAndAnneeScolaire(jury, currentYear);

        double averageScore = evaluations.stream()
                .mapToDouble(eval -> (eval.getTechnicalScore() + eval.getStructureScore() + eval.getOriginalityScore())
                        / 3.0)
                .average()
                .orElse(0.0);

        return ReportStats.builder()
                .totalReports(reports.size())
                .evaluatedReports(evaluations.size())
                .pendingReports(reports.size() - evaluations.size())
                .averageReportScore(averageScore)
                .build();
    }

    private BinomeStats buildBinomeStats(Utilisateur jury, AnneeScolaire currentYear) {
        List<Soutenance> soutenances = soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury, currentYear);
        List<Binome> binomes = soutenances.stream()
                .map(Soutenance::getBinome)
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toList());

        int fullyEvaluated = 0;
        int partiallyEvaluated = 0;

        for (Binome binome : binomes) {
            boolean hasReportEvaluation = binome.getRapports().stream()
                    .anyMatch(rapport -> noteRapportRepository
                            .findByRapportAndEvaluateurAndAnneeScolaire(rapport, jury, currentYear).isPresent());

            boolean hasSoutenanceEvaluation = soutenances.stream()
                    .filter(s -> s.getBinome().equals(binome))
                    .anyMatch(s -> {
                        List<NoteSoutenance> evaluations = noteSoutenanceRepository.findBySoutenanceAndAnneeScolaire(s,
                                currentYear);
                        return evaluations.stream().anyMatch(eval -> eval.getJury().equals(jury));
                    });

            if (hasReportEvaluation && hasSoutenanceEvaluation) {
                fullyEvaluated++;
            } else if (hasReportEvaluation || hasSoutenanceEvaluation) {
                partiallyEvaluated++;
            }
        }

        return BinomeStats.builder()
                .totalBinomes(binomes.size())
                .fullyEvaluatedBinomes(fullyEvaluated)
                .partiallyEvaluatedBinomes(partiallyEvaluated)
                .pendingBinomes(binomes.size() - fullyEvaluated - partiallyEvaluated)
                .build();
    }

    private List<UpcomingSoutenanceDTO> getUpcomingSoutenances(Utilisateur jury, AnneeScolaire currentYear) {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        return soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury, currentYear).stream()
                .filter(s -> !s.getDate().isBefore(today) && !s.getDate().isAfter(nextWeek))
                .map(this::mapToUpcomingSoutenanceDTO)
                .sorted(Comparator.comparing(UpcomingSoutenanceDTO::getDate)
                        .thenComparing(UpcomingSoutenanceDTO::getHeure))
                .limit(5)
                .collect(Collectors.toList());
    }

    private UpcomingSoutenanceDTO mapToUpcomingSoutenanceDTO(Soutenance soutenance) {
        LocalDate today = LocalDate.now();
        Binome binome = soutenance.getBinome();

        return UpcomingSoutenanceDTO.builder()
                .id(soutenance.getId())
                .date(soutenance.getDate())
                .heure(soutenance.getHeure())
                .salle(soutenance.getSalle().getNom())
                .binome(mapToBinomeInfoDTO(binome))
                .sujetTitre(binome.getSujet() != null ? binome.getSujet().getTitre() : "Sans titre")
                .isEvaluated(checkIfSoutenanceEvaluated(soutenance))
                .daysUntilSoutenance((int) java.time.temporal.ChronoUnit.DAYS.between(today, soutenance.getDate()))
                .build();
    }

    private BinomeInfoDTO mapToBinomeInfoDTO(Binome binome) {
        return BinomeInfoDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null)
                .build();
    }

    private StudentDTO mapToStudentDTO(Utilisateur etudiant) {
        return StudentDTO.builder()
                .id(etudiant.getId())
                .nom(etudiant.getNom())
                .prenom(etudiant.getPrenom())
                .build();
    }

    private List<RecentActivityDTO> getRecentActivities(Utilisateur jury, AnneeScolaire currentYear) {
        List<RecentActivityDTO> activities = new ArrayList<>();

        // Add recent soutenance evaluations
        noteSoutenanceRepository.findByJuryAndAnneeScolaire(jury, currentYear).stream()
                .sorted(Comparator.comparing(NoteSoutenance::getDateEvaluation).reversed())
                .limit(5)
                .forEach(eval -> activities.add(RecentActivityDTO.builder()
                        .id(eval.getId())
                        .type("SOUTENANCE_EVALUATION")
                        .description(String.format("Évaluation de la soutenance de %s %s",
                                eval.getEtudiant().getPrenom(), eval.getEtudiant().getNom()))
                        .timestamp(eval.getDateEvaluation())
                        .icon("pi pi-microphone")
                        .build()));

        // Add recent report evaluations
        noteRapportRepository.findByEvaluateurAndAnneeScolaire(jury, currentYear).stream()
                .sorted(Comparator.comparing(NoteRapport::getDateEvaluation).reversed())
                .limit(5)
                .forEach(eval -> activities.add(RecentActivityDTO.builder()
                        .id(eval.getId())
                        .type("REPORT_EVALUATION")
                        .description(String.format("Évaluation du rapport: %s", eval.getRapport().getTitre()))
                        .timestamp(eval.getDateEvaluation())
                        .icon("pi pi-file-pdf")
                        .build()));

        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDTO::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<PendingEvaluationDTO> getPendingEvaluations(Utilisateur jury, AnneeScolaire currentYear) {
        List<PendingEvaluationDTO> pendingEvaluations = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Pending soutenance evaluations
        soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury, currentYear).stream()
                .filter(s -> s.getDate().isEqual(today) && !checkIfSoutenanceEvaluated(s))
                .forEach(s -> pendingEvaluations.add(PendingEvaluationDTO.builder()
                        .id(s.getId())
                        .type("SOUTENANCE")
                        .title(String.format("Soutenance de %s",
                                s.getBinome().getSujet() != null ? s.getBinome().getSujet().getTitre() : "Sans titre"))
                        .dueDate(LocalDateTime.of(s.getDate(), s.getHeure()))
                        .status("PENDING")
                        .urgency("HIGH")
                        .build()));

        // Pending report evaluations
        soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury, currentYear).stream()
                .flatMap(s -> rapportRepository.findByBinomeAndAnneeScolaire(s.getBinome(), currentYear).stream())
                .filter(r -> noteRapportRepository.findByRapportAndEvaluateurAndAnneeScolaire(r, jury, currentYear)
                        .isEmpty())
                .forEach(r -> pendingEvaluations.add(PendingEvaluationDTO.builder()
                        .id(r.getId())
                        .type("REPORT")
                        .title(r.getTitre())
                        .dueDate(r.getDateSoumission())
                        .status("PENDING")
                        .urgency("MEDIUM")
                        .build()));

        return pendingEvaluations;
    }

    private List<ReminderDTO> buildReminders(Utilisateur jury, AnneeScolaire currentYear) {
        List<ReminderDTO> reminders = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Today's soutenances reminder
        List<Soutenance> todaySoutenances = soutenanceRepository
                .findByJury1OrJury2AndAnneeScolaire(jury, jury, currentYear).stream()
                .filter(s -> s.getDate().isEqual(today))
                .collect(Collectors.toList());

        if (!todaySoutenances.isEmpty()) {
            reminders.add(ReminderDTO.builder()
                    .type("SOUTENANCE")
                    .title("Soutenances aujourd'hui")
                    .message(String.format("Vous avez %d soutenance(s) prévue(s) aujourd'hui", todaySoutenances.size()))
                    .severity("critical")
                    .actionUrl("/jury/grading")
                    .daysRemaining(0)
                    .build());
        }

        // Pending report evaluations reminder
        List<Rapport> pendingReports = soutenanceRepository.findByJury1OrJury2AndAnneeScolaire(jury, jury, currentYear)
                .stream()
                .flatMap(s -> rapportRepository.findByBinomeAndAnneeScolaire(s.getBinome(), currentYear).stream())
                .filter(r -> noteRapportRepository.findByRapportAndEvaluateurAndAnneeScolaire(r, jury, currentYear)
                        .isEmpty())
                .collect(Collectors.toList());

        if (!pendingReports.isEmpty()) {
            reminders.add(ReminderDTO.builder()
                    .type("RAPPORT")
                    .title("Rapports en attente")
                    .message(String.format("Vous avez %d rapport(s) à évaluer", pendingReports.size()))
                    .severity("warning")
                    .actionUrl("/jury/report-evaluation")
                    .build());
        }

        return reminders;
    }

    private boolean checkIfSoutenanceEvaluated(Soutenance soutenance) {
        Binome binome = soutenance.getBinome();
        boolean etudiant1Evaluated = noteSoutenanceRepository
                .findByEtudiantAndSoutenance(binome.getEtudiant1(), soutenance).size() == 2;
        boolean etudiant2Evaluated = binome.getEtudiant2() == null
                || noteSoutenanceRepository.findByEtudiantAndSoutenance(binome.getEtudiant2(), soutenance).size() == 2;
        return etudiant1Evaluated && etudiant2Evaluated;
    }
}