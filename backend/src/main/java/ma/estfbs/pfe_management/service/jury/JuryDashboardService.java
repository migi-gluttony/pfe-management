package ma.estfbs.pfe_management.service.jury;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.jury.JuryDashboardDTOs.*;
import ma.estfbs.pfe_management.model.*;
import ma.estfbs.pfe_management.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JuryDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final NoteSoutenanceRepository noteSoutenanceRepository;
    private final NoteRapportRepository noteRapportRepository;
    private final RapportRepository rapportRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public JuryDashboardResponse getJuryDashboard(Long juryId) {
        Utilisateur utilisateur = utilisateurRepository.findById(juryId)
                .orElseThrow(() -> new RuntimeException("Membre du jury non trouvé"));
        
        // Get current year string directly from database
        String currentYearAnnee = jdbcTemplate.queryForObject(
            "SELECT annee FROM annee_scolaire WHERE courante = true", 
            String.class
        );
        
        return JuryDashboardResponse.builder()
                .juryInfo(getJuryInfo(utilisateur, currentYearAnnee))
                .soutenanceStats(getSoutenanceStats(utilisateur))
                .reportStats(getReportStats(utilisateur))
                .binomeStats(getBinomeStats(utilisateur))
                .upcomingSoutenances(getUpcomingSoutenances(utilisateur))
                .recentActivities(getRecentActivities(utilisateur))
                .pendingEvaluations(getPendingEvaluations(utilisateur))
                .reminders(getReminders(utilisateur))
                .build();
    }

    private JuryInfo getJuryInfo(Utilisateur utilisateur, String anneeAcademique) {
        return JuryInfo.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .anneeAcademique(anneeAcademique)
                .build();
    }

    private SoutenanceStats getSoutenanceStats(Utilisateur jury) {
        // Get all soutenances for current year through SQL
        String sql = "SELECT s.* FROM soutenance s " +
                     "JOIN annee_scolaire a ON s.annee_scolaire_id = a.id " +
                     "WHERE a.courante = true AND (s.jury1_id = ? OR s.jury2_id = ?)";
        
        List<Long> soutenanceIds = jdbcTemplate.queryForList(
            sql.replace("s.*", "s.id"), 
            Long.class, 
            jury.getId(), 
            jury.getId()
        );
        
        List<LocalDate> dates = jdbcTemplate.queryForList(
            sql.replace("s.*", "s.date"), 
            LocalDate.class, 
            jury.getId(), 
            jury.getId()
        );
        
        LocalDate today = LocalDate.now();
        long todayCount = dates.stream().filter(d -> d.equals(today)).count();
        long upcomingCount = dates.stream().filter(d -> d.isAfter(today)).count();
        
        // Get evaluations count
        Long evaluatedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM note_soutenance ns " +
            "JOIN annee_scolaire a ON ns.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND ns.jury_id = ?",
            Long.class,
            jury.getId()
        );
        
        Double avgScore = jdbcTemplate.queryForObject(
            "SELECT AVG(presentation_score) FROM note_soutenance ns " +
            "JOIN annee_scolaire a ON ns.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND ns.jury_id = ?",
            Double.class,
            jury.getId()
        );
        
        return SoutenanceStats.builder()
                .totalSoutenances(soutenanceIds.size())
                .todaySoutenances((int) todayCount)
                .upcomingSoutenances((int) upcomingCount)
                .evaluatedSoutenances(evaluatedCount != null ? evaluatedCount.intValue() : 0)
                .pendingSoutenances(soutenanceIds.size() - (evaluatedCount != null ? evaluatedCount.intValue() : 0))
                .averageEvaluationScore(avgScore != null ? avgScore : 0.0)
                .build();
    }

    private ReportStats getReportStats(Utilisateur jury) {
        // Get reports count
        Long totalReports = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rapport r " +
            "JOIN binome b ON r.binome_id = b.id " +
            "JOIN soutenance s ON s.binome_id = b.id " +
            "JOIN annee_scolaire a ON r.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND (s.jury1_id = ? OR s.jury2_id = ?)",
            Long.class,
            jury.getId(), jury.getId()
        );
        
        Long evaluatedReports = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM note_rapport nr " +
            "JOIN annee_scolaire a ON nr.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND nr.evaluateur_id = ?",
            Long.class,
            jury.getId()
        );
        
        Double avgScore = jdbcTemplate.queryForObject(
            "SELECT AVG(technical_score) FROM note_rapport nr " +
            "JOIN annee_scolaire a ON nr.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND nr.evaluateur_id = ?",
            Double.class,
            jury.getId()
        );
        
        return ReportStats.builder()
                .totalReports(totalReports != null ? totalReports.intValue() : 0)
                .evaluatedReports(evaluatedReports != null ? evaluatedReports.intValue() : 0)
                .pendingReports(totalReports != null && evaluatedReports != null ? 
                               totalReports.intValue() - evaluatedReports.intValue() : 0)
                .averageReportScore(avgScore != null ? avgScore : 0.0)
                .build();
    }

    private BinomeStats getBinomeStats(Utilisateur jury) {
        // Get binomes count
        Long totalBinomes = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT b.id) FROM binome b " +
            "JOIN soutenance s ON s.binome_id = b.id " +
            "JOIN annee_scolaire a ON b.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND (s.jury1_id = ? OR s.jury2_id = ?)",
            Long.class,
            jury.getId(), jury.getId()
        );
        
        Long fullyEvaluated = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT b.id) FROM binome b " +
            "JOIN soutenance s ON s.binome_id = b.id " +
            "JOIN note_soutenance ns ON ns.soutenance_id = s.id " +
            "JOIN rapport r ON r.binome_id = b.id " +
            "JOIN note_rapport nr ON nr.rapport_id = r.id " +
            "JOIN annee_scolaire a ON b.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND ns.jury_id = ? AND nr.evaluateur_id = ?",
            Long.class,
            jury.getId(), jury.getId()
        );
        
        Long partiallyEvaluated = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT b.id) FROM binome b " +
            "JOIN soutenance s ON s.binome_id = b.id " +
            "LEFT JOIN note_soutenance ns ON ns.soutenance_id = s.id AND ns.jury_id = ? " +
            "LEFT JOIN rapport r ON r.binome_id = b.id " +
            "LEFT JOIN note_rapport nr ON nr.rapport_id = r.id AND nr.evaluateur_id = ? " +
            "JOIN annee_scolaire a ON b.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND (s.jury1_id = ? OR s.jury2_id = ?) " +
            "AND ((ns.id IS NOT NULL AND nr.id IS NULL) OR (ns.id IS NULL AND nr.id IS NOT NULL))",
            Long.class,
            jury.getId(), jury.getId(), jury.getId(), jury.getId()
        );
        
        return BinomeStats.builder()
                .totalBinomes(totalBinomes != null ? totalBinomes.intValue() : 0)
                .fullyEvaluatedBinomes(fullyEvaluated != null ? fullyEvaluated.intValue() : 0)
                .partiallyEvaluatedBinomes(partiallyEvaluated != null ? partiallyEvaluated.intValue() : 0)
                .pendingBinomes(totalBinomes != null && fullyEvaluated != null && partiallyEvaluated != null ?
                               totalBinomes.intValue() - fullyEvaluated.intValue() - partiallyEvaluated.intValue() : 0)
                .build();
    }

    private List<UpcomingSoutenanceDTO> getUpcomingSoutenances(Utilisateur jury) {
        LocalDate today = LocalDate.now();
        LocalDate upcoming = today.plusDays(7);
        
        List<Soutenance> soutenances = jdbcTemplate.query(
            "SELECT s.* FROM soutenance s " +
            "JOIN annee_scolaire a ON s.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND (s.jury1_id = ? OR s.jury2_id = ?) " +
            "AND s.date > ? AND s.date <= ? ORDER BY s.date, s.heure LIMIT 5",
            (rs, rowNum) -> {
                Soutenance s = new Soutenance();
                s.setId(rs.getLong("id"));
                s.setDate(rs.getDate("date").toLocalDate());
                s.setHeure(rs.getTime("heure").toLocalTime());
                return s;
            },
            jury.getId(), jury.getId(), today, upcoming
        );
        
        List<UpcomingSoutenanceDTO> upcomingList = new ArrayList<>();
        
        for (Soutenance s : soutenances) {
            // Load full soutenance data
            Soutenance fullSoutenance = soutenanceRepository.findById(s.getId()).orElse(null);
            if (fullSoutenance != null) {
                boolean isEvaluated = noteSoutenanceRepository
                        .findBySoutenance(fullSoutenance)
                        .stream()
                        .anyMatch(note -> note.getJury().getId().equals(jury.getId()));
                
                int daysUntil = (int) ChronoUnit.DAYS.between(today, fullSoutenance.getDate());
                
                upcomingList.add(UpcomingSoutenanceDTO.builder()
                        .id(fullSoutenance.getId())
                        .date(fullSoutenance.getDate())
                        .heure(fullSoutenance.getHeure())
                        .salle(fullSoutenance.getSalle().getNom())
                        .binome(mapToBinomeInfoDTO(fullSoutenance.getBinome()))
                        .sujetTitre(fullSoutenance.getBinome().getSujet().getTitre())
                        .isEvaluated(isEvaluated)
                        .daysUntilSoutenance(daysUntil)
                        .build());
            }
        }
        
        return upcomingList;
    }

    private BinomeInfoDTO mapToBinomeInfoDTO(Binome binome) {
        return BinomeInfoDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null)
                .build();
    }

    private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
        return StudentDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }

    private List<RecentActivityDTO> getRecentActivities(Utilisateur jury) {
        List<RecentActivityDTO> activities = new ArrayList<>();
        
        // Get recent report evaluations
        List<NoteRapport> recentReportEvals = jdbcTemplate.query(
            "SELECT nr.* FROM note_rapport nr " +
            "JOIN annee_scolaire a ON nr.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND nr.evaluateur_id = ? " +
            "ORDER BY nr.date_evaluation DESC LIMIT 3",
            (rs, rowNum) -> {
                NoteRapport nr = new NoteRapport();
                nr.setId(rs.getLong("id"));
                nr.setDateEvaluation(rs.getTimestamp("date_evaluation").toLocalDateTime());
                return nr;
            },
            jury.getId()
        );
        
        for (NoteRapport eval : recentReportEvals) {
            NoteRapport fullEval = noteRapportRepository.findById(eval.getId()).orElse(null);
            if (fullEval != null) {
                activities.add(RecentActivityDTO.builder()
                        .id(fullEval.getId())
                        .type("REPORT_EVALUATION")
                        .description("Évaluation du rapport \"" + fullEval.getRapport().getTitre() + "\"")
                        .timestamp(fullEval.getDateEvaluation())
                        .icon("pi pi-file-pdf")
                        .build());
            }
        }
        
        // Get recent soutenance evaluations
        List<NoteSoutenance> recentSoutenanceEvals = jdbcTemplate.query(
            "SELECT ns.* FROM note_soutenance ns " +
            "JOIN annee_scolaire a ON ns.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND ns.jury_id = ? " +
            "ORDER BY ns.date_evaluation DESC LIMIT 3",
            (rs, rowNum) -> {
                NoteSoutenance ns = new NoteSoutenance();
                ns.setId(rs.getLong("id"));
                ns.setDateEvaluation(rs.getTimestamp("date_evaluation").toLocalDateTime());
                return ns;
            },
            jury.getId()
        );
        
        for (NoteSoutenance eval : recentSoutenanceEvals) {
            NoteSoutenance fullEval = noteSoutenanceRepository.findById(eval.getId()).orElse(null);
            if (fullEval != null) {
                activities.add(RecentActivityDTO.builder()
                        .id(fullEval.getId())
                        .type("SOUTENANCE_EVALUATION")
                        .description("Évaluation de la soutenance de " + 
                                    fullEval.getEtudiant().getPrenom() + " " + fullEval.getEtudiant().getNom())
                        .timestamp(fullEval.getDateEvaluation())
                        .icon("pi pi-calendar")
                        .build());
            }
        }
        
        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDTO::getTimestamp).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<PendingEvaluationDTO> getPendingEvaluations(Utilisateur jury) {
        List<PendingEvaluationDTO> pendingEvals = new ArrayList<>();
        
        // Get upcoming soutenances that haven't been evaluated
        List<Soutenance> pendingSoutenances = jdbcTemplate.query(
            "SELECT s.* FROM soutenance s " +
            "JOIN annee_scolaire a ON s.annee_scolaire_id = a.id " +
            "WHERE a.courante = true AND (s.jury1_id = ? OR s.jury2_id = ?) " +
            "AND s.date <= ? AND NOT EXISTS (" +
            "SELECT 1 FROM note_soutenance ns WHERE ns.soutenance_id = s.id AND ns.jury_id = ?)",
            (rs, rowNum) -> {
                Soutenance s = new Soutenance();
                s.setId(rs.getLong("id"));
                s.setDate(rs.getDate("date").toLocalDate());
                s.setHeure(rs.getTime("heure").toLocalTime());
                return s;
            },
            jury.getId(), jury.getId(), LocalDate.now(), jury.getId()
        );
        
        for (Soutenance s : pendingSoutenances) {
            Soutenance fullSoutenance = soutenanceRepository.findById(s.getId()).orElse(null);
            if (fullSoutenance != null) {
                String urgency = ChronoUnit.DAYS.between(s.getDate(), LocalDate.now()) > 3 ? "HIGH" : "MEDIUM";
                
                pendingEvals.add(PendingEvaluationDTO.builder()
                        .id(fullSoutenance.getId())
                        .type("SOUTENANCE")
                        .title("Soutenance de " + fullSoutenance.getBinome().getEtudiant1().getPrenom() + 
                               " " + fullSoutenance.getBinome().getEtudiant1().getNom())
                        .dueDate(fullSoutenance.getDate().atTime(fullSoutenance.getHeure()))
                        .status("EN_ATTENTE")
                        .urgency(urgency)
                        .build());
            }
        }
        
        return pendingEvals;
    }

    private List<ReminderDTO> getReminders(Utilisateur jury) {
        List<ReminderDTO> reminders = new ArrayList<>();
        
        List<UpcomingSoutenanceDTO> upcomingSoutenances = getUpcomingSoutenances(jury);
        for (UpcomingSoutenanceDTO soutenance : upcomingSoutenances) {
            if (soutenance.getDaysUntilSoutenance() <= 7) {
                String severity = soutenance.getDaysUntilSoutenance() <= 2 ? "critical" : "warning";
                BinomeInfoDTO binome = soutenance.getBinome();
                String studentName = binome.getEtudiant1().getPrenom() + " " + binome.getEtudiant1().getNom();
                
                reminders.add(ReminderDTO.builder()
                        .type("SOUTENANCE")
                        .title("Soutenance à venir")
                        .message("Soutenance de " + studentName + " dans " + soutenance.getDaysUntilSoutenance() + " jours")
                        .severity(severity)
                        .actionUrl("/jury/soutenances")
                        .daysRemaining(soutenance.getDaysUntilSoutenance())
                        .build());
            }
        }
        
        return reminders;
    }
}