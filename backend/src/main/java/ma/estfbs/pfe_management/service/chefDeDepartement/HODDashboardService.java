package ma.estfbs.pfe_management.service.chefDeDepartement;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.chefDeDepartement.HODDashboardDTOs.*;
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
public class HODDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final BinomeRepository binomeRepository;
    private final SujetRepository sujetRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final FiliereRepository filiereRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProposerSujetsRepository proposerSujetsRepository;
    private final NoteFinaleRepository noteFinaleRepository;
    private final AcademicYearService academicYearService;

    @Transactional(readOnly = true)
    public HODDashboardResponse getHODDashboard(Long hodId) {
        Utilisateur hod = utilisateurRepository.findById(hodId)
                .orElseThrow(() -> new RuntimeException("Chef de département non trouvé"));
        
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        return HODDashboardResponse.builder()
                .hodInfo(getHODInfo(hod, currentYear))
                .stats(getDashboardStats(hodId))
                .upcomingSoutenances(getUpcomingSoutenances(hodId))
                .recentActivities(getRecentActivities(hodId))
                .build();
    }

    private HODInfo getHODInfo(Utilisateur hod, AnneeScolaire currentYear) {
        return HODInfo.builder()
                .id(hod.getId())
                .nom(hod.getNom())
                .prenom(hod.getPrenom())
                .email(hod.getEmail())
                .anneeAcademique(currentYear.getAnnee())
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(Long hodId) {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Calculate all required statistics
        List<Utilisateur> allUsers = utilisateurRepository.findAll();
        int totalUsers = allUsers.size();
        int totalStudents = (int) allUsers.stream().filter(u -> u.getRole() == Utilisateur.Role.ETUDIANT).count();
        int totalSupervisors = (int) allUsers.stream().filter(u -> u.getRole() == Utilisateur.Role.ENCADRANT).count();
        int totalJuries = (int) allUsers.stream().filter(u -> u.getRole() == Utilisateur.Role.JURY).count();
        
        List<Binome> binomes = binomeRepository.findByAnneeScolaire(currentYear);
        int totalBinomes = binomes.size();
        
        List<Soutenance> soutenances = soutenanceRepository.findByAnneeScolaire(currentYear);
        int binomesWithSoutenance = soutenances.size();
        
        LocalDate today = LocalDate.now();
        int plannedSoutenances = (int) soutenances.stream().filter(s -> s.getDate().isAfter(today)).count();
        int completedSoutenances = soutenances.size() - plannedSoutenances;
        
        List<Sujet> sujets = sujetRepository.findByAnneeScolaire(currentYear);
        int totalSujets = sujets.size();
        
        int pendingSuggestions = proposerSujetsRepository.findByStatusAndAnneeScolaire(
                ProposerSujets.Status.EN_ATTENTE, currentYear).size();
        
        List<NoteFinale> notesFinales = noteFinaleRepository.findByAnneeScolaire(currentYear);
        double averageGrade = notesFinales.isEmpty() ? 0.0 :
                notesFinales.stream()
                    .mapToDouble(nf -> nf.calculateFinalScore(currentYear.getPourcentage()))
                    .average()
                    .orElse(0.0);
        
        int honorsCount = (int) notesFinales.stream()
                .filter(nf -> nf.calculateFinalScore(currentYear.getPourcentage()) >= 16.0)
                .count();
        int goodCount = (int) notesFinales.stream()
                .filter(nf -> {
                    double score = nf.calculateFinalScore(currentYear.getPourcentage());
                    return score >= 14.0 && score < 16.0;
                })
                .count();
        int passCount = (int) notesFinales.stream()
                .filter(nf -> {
                    double score = nf.calculateFinalScore(currentYear.getPourcentage());
                    return score >= 10.0 && score < 14.0;
                })
                .count();
        
        List<Filiere> filieres = filiereRepository.findAll();
        int totalFilieres = filieres.size();
        
        List<FiliereStatsDTO> filiereStats = filieres.stream()
                .map(filiere -> {
                    int studentCount = etudiantRepository.findByFiliereAndAnneeScolaire(filiere, currentYear).size();
                    return FiliereStatsDTO.builder()
                            .nom(filiere.getNom())
                            .etudiantCount(studentCount)
                            .build();
                })
                .sorted(Comparator.comparing(FiliereStatsDTO::getEtudiantCount).reversed())
                .collect(Collectors.toList());
        
        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalStudents(totalStudents)
                .totalSupervisors(totalSupervisors)
                .totalJuries(totalJuries)
                .totalBinomes(totalBinomes)
                .binomesWithSoutenance(binomesWithSoutenance)
                .totalSujets(totalSujets)
                .pendingSuggestions(pendingSuggestions)
                .totalSoutenances(soutenances.size())
                .plannedSoutenances(plannedSoutenances)
                .completedSoutenances(completedSoutenances)
                .averageGrade(averageGrade)
                .honorsCount(honorsCount)
                .goodCount(goodCount)
                .passCount(passCount)
                .totalFilieres(totalFilieres)
                .filieres(filiereStats)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UpcomingSoutenanceDTO> getUpcomingSoutenances(Long hodId) {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        
        List<Soutenance> upcomingSoutenances = soutenanceRepository.findByDateBetweenAndAnneeScolaire(
                today, nextWeek, currentYear);
        
        return upcomingSoutenances.stream()
                .sorted(Comparator.comparing(Soutenance::getDate).thenComparing(Soutenance::getHeure))
                .limit(5)
                .map(this::mapToUpcomingSoutenanceDTO)
                .collect(Collectors.toList());
    }

    private UpcomingSoutenanceDTO mapToUpcomingSoutenanceDTO(Soutenance soutenance) {
        return UpcomingSoutenanceDTO.builder()
                .id(soutenance.getId())
                .date(soutenance.getDate())
                .heure(soutenance.getHeure())
                .salle(SalleDTO.builder()
                        .id(soutenance.getSalle().getId())
                        .nom(soutenance.getSalle().getNom())
                        .build())
                .binome(mapToBinomeDTO(soutenance.getBinome()))
                .jury1(JuryDTO.builder()
                        .id(soutenance.getJury1().getId())
                        .nom(soutenance.getJury1().getNom())
                        .prenom(soutenance.getJury1().getPrenom())
                        .build())
                .jury2(JuryDTO.builder()
                        .id(soutenance.getJury2().getId())
                        .nom(soutenance.getJury2().getNom())
                        .prenom(soutenance.getJury2().getPrenom())
                        .build())
                .build();
    }

    private BinomeDTO mapToBinomeDTO(Binome binome) {
        StudentDTO etudiant1 = StudentDTO.builder()
                .id(binome.getEtudiant1().getId())
                .nom(binome.getEtudiant1().getNom())
                .prenom(binome.getEtudiant1().getPrenom())
                .build();
        
        StudentDTO etudiant2 = null;
        if (binome.getEtudiant2() != null) {
            etudiant2 = StudentDTO.builder()
                    .id(binome.getEtudiant2().getId())
                    .nom(binome.getEtudiant2().getNom())
                    .prenom(binome.getEtudiant2().getPrenom())
                    .build();
        }
        
        SujetDTO sujet = null;
        if (binome.getSujet() != null) {
            sujet = SujetDTO.builder()
                    .id(binome.getSujet().getId())
                    .titre(binome.getSujet().getTitre())
                    .build();
        }
        
        return BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(etudiant1)
                .etudiant2(etudiant2)
                .sujet(sujet)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecentActivityDTO> getRecentActivities(Long hodId) {
        LocalDateTime now = LocalDateTime.now();
        
        // In a real implementation, would track database changes
        // For now, create sample activities
        List<RecentActivityDTO> activities = new ArrayList<>();
        
        activities.add(RecentActivityDTO.builder()
                .id(1L)
                .description("Nouvelle attribution d'encadrant")
                .timestamp(now.minusHours(2))
                .icon("pi pi-user-plus")
                .build());
        
        activities.add(RecentActivityDTO.builder()
                .id(2L)
                .description("Planification d'une nouvelle soutenance")
                .timestamp(now.minusDays(1))
                .icon("pi pi-calendar")
                .build());
        
        activities.add(RecentActivityDTO.builder()
                .id(3L)
                .description("Validation d'une proposition de sujet")
                .timestamp(now.minusDays(2))
                .icon("pi pi-check-circle")
                .build());
        
        activities.add(RecentActivityDTO.builder()
                .id(4L)
                .description("Nouvelle notation soumise")
                .timestamp(now.minusDays(3))
                .icon("pi pi-star")
                .build());
        
        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDTO::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
}