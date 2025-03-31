package ma.estfbs.pfe_management.service.chefDeDepartement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.BinomeDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.BinomeManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.EncadrantDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.StudentDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.BinomeManagementDTOs.SujetDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.CompteManagementDTOs.CompteDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.CompteManagementDTOs.CompteManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.EtudiantDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.NoteDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.NoteManagementResponse;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.PourcentageDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SettingsDTOs.AcademicYearDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.JuryDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.SalleDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.SoutenanceDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.SujetShortDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.SujetManagementResponse;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.NoteFinale;
import ma.estfbs.pfe_management.model.NoteSoutenance;
import ma.estfbs.pfe_management.model.Pourcentage;
import ma.estfbs.pfe_management.model.Rapport;
import ma.estfbs.pfe_management.model.Salle;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.repository.AnneeScolaireRepository;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.repository.NoteFinaleRepository;
import ma.estfbs.pfe_management.repository.NoteSoutenanceRepository;
import ma.estfbs.pfe_management.repository.PourcentageRepository;
import ma.estfbs.pfe_management.repository.RapportRepository;
import ma.estfbs.pfe_management.repository.SoutenanceRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final FiliereRepository filiereRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final BinomeRepository binomeRepository;
    private final SujetRepository sujetRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final NoteFinaleRepository noteFinaleRepository;
    private final RapportRepository rapportRepository;
    private final NoteSoutenanceRepository noteSoutenanceRepository;
    private final PourcentageRepository pourcentageRepository;

    /**
     * Get all academic years
     */
    public List<AcademicYearDTO> getAllAcademicYears() {
        return anneeScolaireRepository.findAll().stream()
                .map(this::mapToAcademicYearDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all filieres
     */
    public List<FiliereDTO> getAllFilieres() {
        return filiereRepository.findAll().stream()
                .map(this::mapToFiliereDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by role and academic year
     */
    public CompteManagementResponse getComptesByRoleAndYear(Role role, Long anneeScolaireId) {
        List<CompteDTO> comptes = new ArrayList<>();
        AnneeScolaire year = findAnneeScolaireById(anneeScolaireId);

        if (role == Role.ETUDIANT) {
            comptes = etudiantRepository.findByAnneeScolaire(year).stream()
                    .map(etudiant -> mapToCompteDTO(etudiant.getUtilisateur(), etudiant))
                    .collect(Collectors.toList());
        } else if (role != null) {
            comptes = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() == role)
                    .map(user -> mapToCompteDTO(user, null))
                    .collect(Collectors.toList());
        } else {
            // Add non-student users
            List<CompteDTO> nonStudentDTOs = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() != Role.ETUDIANT)
                    .map(user -> mapToCompteDTO(user, null))
                    .collect(Collectors.toList());

            // Add students for specified year
            List<CompteDTO> studentDTOs = etudiantRepository.findByAnneeScolaire(year).stream()
                    .map(etudiant -> mapToCompteDTO(etudiant.getUtilisateur(), etudiant))
                    .collect(Collectors.toList());

            comptes.addAll(nonStudentDTOs);
            comptes.addAll(studentDTOs);
        }

        List<FiliereDTO> filieres = getAllFilieres();

        return CompteManagementResponse.builder()
                .comptes(comptes)
                .filieres(filieres)
                .build();
    }

    /**
     * Get binomes by filiere and academic year
     */
    public BinomeManagementResponse getBinomeManagementDataByYear(Long filiereId, Long anneeScolaireId) {
        List<BinomeDTO> binomes;
        AnneeScolaire year = findAnneeScolaireById(anneeScolaireId);

        // Get all filieres for dropdown
        List<FiliereDTO> filieres = getAllFilieres();

        // Get all encadrants
        List<EncadrantDTO> encadrants = utilisateurRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ENCADRANT)
                .map(this::mapToEncadrantDTO)
                .collect(Collectors.toList());

        if (filiereId != null) {
            // Get binomes filtered by filiere for specified year
            Filiere filiere = filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + filiereId));

            // Get students in this filiere
            List<Etudiant> filiereStudents = etudiantRepository.findByFiliere(filiere);
            List<Utilisateur> filiereUtilisateurs = filiereStudents.stream()
                    .map(Etudiant::getUtilisateur)
                    .collect(Collectors.toList());

            // Get binomes where at least one student is from this filiere AND binome is for specified year
            binomes = binomeRepository.findAll().stream()
                    .filter(binome -> binome.getAnneeScolaire().getId().equals(year.getId())
                            && (filiereUtilisateurs.contains(binome.getEtudiant1()) ||
                                    (binome.getEtudiant2() != null
                                            && filiereUtilisateurs.contains(binome.getEtudiant2()))))
                    .map(this::mapToBinomeDTO)
                    .collect(Collectors.toList());

            // Get available students filtered by filiere
            List<StudentDTO> availableStudents = new ArrayList<>();

            // Get available subjects filtered by filiere
            List<SujetDTO> availableSujets = sujetRepository
                    .findByAnneeScolaireAndFiliere(year, filiere)
                    .stream()
                    .map(this::mapToSujetDTO)
                    .collect(Collectors.toList());

            return BinomeManagementResponse.builder()
                    .binomes(binomes)
                    .filieres(filieres)
                    .availableStudents(availableStudents)
                    .encadrants(encadrants)
                    .availableSujets(availableSujets)
                    .build();
        } else {
            // Get all binomes for specified year
            binomes = binomeRepository.findAll().stream()
                    .filter(binome -> binome.getAnneeScolaire().getId().equals(year.getId()))
                    .map(this::mapToBinomeDTO)
                    .collect(Collectors.toList());

            // No available students for archive view
            List<StudentDTO> availableStudents = new ArrayList<>();

            // Get all subjects for the specified year
            List<SujetDTO> availableSujets = sujetRepository.findByAnneeScolaire(year)
                    .stream()
                    .map(this::mapToSujetDTO)
                    .collect(Collectors.toList());

            return BinomeManagementResponse.builder()
                    .binomes(binomes)
                    .filieres(filieres)
                    .availableStudents(availableStudents)
                    .encadrants(encadrants)
                    .availableSujets(availableSujets)
                    .build();
        }
    }

    /**
     * Get subjects by filiere and academic year
     */
    public SujetManagementResponse getSujetsByFiliereAndYear(Long filiereId, Long anneeScolaireId) {
        AnneeScolaire year = findAnneeScolaireById(anneeScolaireId);
        
        List<ma.estfbs.pfe_management.dto.SujetDTO> sujets;
        if (filiereId != null) {
            Filiere filiere = filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + filiereId));
            
            sujets = sujetRepository.findByFiliereAndAnneeScolaire(filiere, year).stream()
                    .map(this::mapToSujetFullDTO)
                    .collect(Collectors.toList());
        } else {
            sujets = sujetRepository.findByAnneeScolaire(year).stream()
                    .map(this::mapToSujetFullDTO)
                    .collect(Collectors.toList());
        }

        List<FiliereDTO> filieres = getAllFilieres();

        return SujetManagementResponse.builder()
                .sujets(sujets)
                .filieres(filieres)
                .build();
    }

    /**
     * Get soutenances by filiere and academic year
     */
    public List<SoutenanceDTO> getSoutenancesByFiliereAndYear(Long filiereId, Long anneeScolaireId) {
        AnneeScolaire year = findAnneeScolaireById(anneeScolaireId);
        
        List<Soutenance> soutenances;
        if (filiereId != null) {
            Filiere filiere = filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + filiereId));
            
            // Filter soutenances by filiere and year
            soutenances = soutenanceRepository.findByAnneeScolaire(year).stream()
                    .filter(soutenance -> {
                        Binome binome = soutenance.getBinome();
                        if (binome != null && binome.getEtudiant1() != null) {
                            Etudiant etudiant = etudiantRepository.findByUtilisateur(binome.getEtudiant1()).orElse(null);
                            return etudiant != null && etudiant.getFiliere().getId().equals(filiere.getId());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        } else {
            soutenances = soutenanceRepository.findByAnneeScolaire(year);
        }
        
        return soutenances.stream()
                .map(this::mapToSoutenanceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get notes by filiere and academic year
     */
    public NoteManagementResponse getNotesByFiliereAndYear(Long filiereId, Long anneeScolaireId) {
        List<NoteDTO> notes = new ArrayList<>();
        AnneeScolaire year = findAnneeScolaireById(anneeScolaireId);
        
        // Get all filieres
        List<FiliereDTO> filieres = getAllFilieres();
        
        // Get the grade percentages configuration for specified year
        Pourcentage pourcentage = pourcentageRepository.findByAnneeScolaire(year);
        PourcentageDTO pourcentageDTO = mapToPourcentageDTO(pourcentage);
        
        List<Etudiant> students;
        if (filiereId != null) {
            Filiere filiere = filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + filiereId));
            students = etudiantRepository.findByFiliereAndAnneeScolaire(filiere, year);
        } else {
            students = etudiantRepository.findByAnneeScolaire(year);
        }
        
        for (Etudiant etudiant : students) {
            Utilisateur utilisateur = etudiant.getUtilisateur();
            
            // Find the student's binôme (as etudiant1 or etudiant2) for specified year
            List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2(utilisateur, utilisateur)
                    .stream()
                    .filter(binome -> binome.getAnneeScolaire().getId().equals(year.getId()))
                    .collect(Collectors.toList());
            
            if (binomes.isEmpty()) {
                continue; // Skip students without a binôme for specified year
            }
            
            Binome binome = binomes.get(0); // Get the first binôme if multiple
            
            // Get rapport note for specified year
            Integer noteRapport = null;
            List<Rapport> rapports = rapportRepository.findByBinome(binome)
                    .stream()
                    .filter(rapport -> rapport.getAnneeScolaire().getId().equals(year.getId()))
                    .collect(Collectors.toList());
            
            if (!rapports.isEmpty()) {
                Rapport rapport = rapports.get(0);
                noteRapport = rapport.getNote();
            }
            
            // Get soutenance note
            Integer noteSoutenance = null;
            List<NoteSoutenance> noteSoutenances = noteSoutenanceRepository.findByJury(binome.getEncadrant());
            if (!noteSoutenances.isEmpty()) {
                // Calculate average if multiple jury evaluations
                noteSoutenance = (int) noteSoutenances.stream()
                        .mapToInt(NoteSoutenance::getNote)
                        .average()
                        .orElse(0);
            }
            
            // Get encadrant evaluation from NoteFinale entity
            Integer noteEncadrant = null;
            Optional<NoteFinale> noteFinale = noteFinaleRepository.findByEtudiantAndAnneeScolaire(utilisateur, year);
            if (noteFinale.isPresent()) {
                noteEncadrant = noteFinale.get().getNoteEncadrant();
            }
            
            // Get the filière info
            Filiere filiere = etudiant.getFiliere();
            
            // Create note DTO
            NoteDTO noteDTO = NoteDTO.builder()
                    .id(utilisateur.getId()) // Using user ID as note ID for now
                    .etudiant(mapToEtudiantDTO(utilisateur))
                    .noteRapport(noteRapport)
                    .noteSoutenance(noteSoutenance)
                    .noteEncadrant(noteEncadrant)
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

    // Helper methods

    private AnneeScolaire findAnneeScolaireById(Long anneeScolaireId) {
        return anneeScolaireRepository.findById(anneeScolaireId)
                .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée avec l'id: " + anneeScolaireId));
    }

    // Mapping methods

    private AcademicYearDTO mapToAcademicYearDTO(AnneeScolaire anneeScolaire) {
        return AcademicYearDTO.builder()
                .id(anneeScolaire.getId())
                .annee(anneeScolaire.getAnnee())
                .courante(anneeScolaire.getCourante())
                .build();
    }

    private FiliereDTO mapToFiliereDTO(Filiere filiere) {
        return FiliereDTO.builder()
                .id(filiere.getId())
                .nom(filiere.getNom())
                .build();
    }

    private CompteDTO mapToCompteDTO(Utilisateur utilisateur, Etudiant etudiant) {
        String filiereName = null;

        // If the role is ETUDIANT, get the filiere name
        if (utilisateur.getRole() == Role.ETUDIANT) {
            if (etudiant == null) {
                etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                        .orElse(null);
            }

            if (etudiant != null && etudiant.getFiliere() != null) {
                filiereName = etudiant.getFiliere().getNom();
            }
        }

        return CompteDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .cni(utilisateur.getCni())
                .cne(utilisateur.getCne())
                .dateNaissance(utilisateur.getDateNaissance())
                .role(utilisateur.getRole())
                .filiereName(filiereName)
                .build();
    }

    private BinomeDTO mapToBinomeDTO(Binome binome) {
        // Get filiere from etudiant1
        Etudiant etudiant1 = etudiantRepository.findByUtilisateur(binome.getEtudiant1())
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: "
                        + binome.getEtudiant1().getId()));

        String filiereName = etudiant1.getFiliere().getNom();

        return BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2())
                        : null)
                .encadrant(binome.getEncadrant() != null ? mapToEncadrantDTO(binome.getEncadrant())
                        : null)
                .sujet(binome.getSujet() != null ? mapToSujetDTO(binome.getSujet()) : null)
                .filiereName(filiereName)
                .build();
    }

    private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
        // Get filiere info for the student
        Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur).orElse(null);
        String filiereName = null;
        if (etudiant != null && etudiant.getFiliere() != null) {
            filiereName = etudiant.getFiliere().getNom();
        }

        return StudentDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .cne(utilisateur.getCne())
                .filiereName(filiereName)
                .build();
    }

    private EncadrantDTO mapToEncadrantDTO(Utilisateur utilisateur) {
        return EncadrantDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .build();
    }

    private SujetDTO mapToSujetDTO(Sujet sujet) {
        return SujetDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .filiereId(sujet.getFiliere() != null ? sujet.getFiliere().getId() : null)
                .build();
    }

    private ma.estfbs.pfe_management.dto.SujetDTO mapToSujetFullDTO(Sujet sujet) {
        return ma.estfbs.pfe_management.dto.SujetDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .theme(sujet.getTheme())
                .description(sujet.getDescription())
                .filiereName(sujet.getFiliere() != null ? sujet.getFiliere().getNom() : null)
                .build();
    }

    private SoutenanceDTO mapToSoutenanceDTO(Soutenance soutenance) {
        return SoutenanceDTO.builder()
                .id(soutenance.getId())
                .date(soutenance.getDate())
                .heure(soutenance.getHeure())
                .salle(mapToSalleDTO(soutenance.getSalle()))
                .binome(mapToSoutenanceBinomeDTO(soutenance.getBinome()))
                .jury1(mapToJuryDTO(soutenance.getJury1()))
                .jury2(mapToJuryDTO(soutenance.getJury2()))
                .build();
    }

    private ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.BinomeDTO mapToSoutenanceBinomeDTO(Binome binome) {
        if (binome == null) {
            return null;
        }
        
        String filiereName = null;
        
        // Get filiere name from etudiant1
        if (binome.getEtudiant1() != null) {
            Etudiant etudiant1 = etudiantRepository.findByUtilisateur(binome.getEtudiant1())
                    .orElse(null);
            if (etudiant1 != null && etudiant1.getFiliere() != null) {
                filiereName = etudiant1.getFiliere().getNom();
            }
        }
        
        return ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(binome.getEtudiant1() != null ? mapToSoutenanceStudentDTO(binome.getEtudiant1()) : null)
                .etudiant2(binome.getEtudiant2() != null ? mapToSoutenanceStudentDTO(binome.getEtudiant2()) : null)
                .encadrant(binome.getEncadrant() != null ? mapToSoutenanceEncadrantDTO(binome.getEncadrant()) : null)
                .sujet(binome.getSujet() != null ? mapToSujetShortDTO(binome.getSujet()) : null)
                .filiereName(filiereName)
                .build();
    }

    private ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.StudentDTO mapToSoutenanceStudentDTO(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }
        
        return ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.StudentDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }

    private ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.EncadrantDTO mapToSoutenanceEncadrantDTO(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }
        
        return ma.estfbs.pfe_management.dto.chefDeDepartement.SoutenanceManagementDTOs.EncadrantDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }

    private JuryDTO mapToJuryDTO(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }
        
        return JuryDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }

    private SalleDTO mapToSalleDTO(Salle salle) {
        if (salle == null) {
            return null;
        }
        
        return SalleDTO.builder()
                .id(salle.getId())
                .nom(salle.getNom())
                .build();
    }

    private SujetShortDTO mapToSujetShortDTO(Sujet sujet) {
        if (sujet == null) {
            return null;
        }
        
        return SujetShortDTO.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .build();
    }

    private EtudiantDTO mapToEtudiantDTO(Utilisateur utilisateur) {
        return EtudiantDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .cne(utilisateur.getCne())
                .build();
    }

    private PourcentageDTO mapToPourcentageDTO(Pourcentage pourcentage) {
        if (pourcentage == null) {
            // Default percentages if none configured
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