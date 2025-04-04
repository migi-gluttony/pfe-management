package ma.estfbs.pfe_management.service.chefDeDepartement;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.chefDeDepartement.CompteManagementDTOs.*;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

@Service
@RequiredArgsConstructor
public class CompteManagementService {

    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final FiliereRepository filiereRepository;
    private final PasswordEncoder passwordEncoder;
    private final AcademicYearService academicYearService;

    // Characters used for random password generation
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Get accounts by role for the current academic year
     */
    public CompteManagementResponse getComptesByRole(Role role) {
        List<CompteDTO> comptes = new ArrayList<>();
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        if (role == Role.ETUDIANT) {
            comptes = etudiantRepository.findByAnneeScolaire(currentYear).stream()
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

            // Add current year students
            List<CompteDTO> studentDTOs = etudiantRepository.findByAnneeScolaire(currentYear).stream()
                    .map(etudiant -> mapToCompteDTO(etudiant.getUtilisateur(), etudiant))
                    .collect(Collectors.toList());

            comptes.addAll(nonStudentDTOs);
            comptes.addAll(studentDTOs);
        }

        List<FiliereDTO> filieres = filiereRepository.findAll().stream()
                .map(this::mapToFiliereDTO)
                .collect(Collectors.toList());

        return CompteManagementResponse.builder()
                .comptes(comptes)
                .filieres(filieres)
                .build();
    }

    /**
     * Add a new account - if student, associate with current year
     */
    @Transactional
    public CompteDTO addCompte(CompteAddRequest request) {
        validateCompteRequest(request);

        String email = generateUniqueEmail(request.getPrenom(), request.getNom());
        String password = generateRandomPassword(10);

        Utilisateur utilisateur = createUtilisateur(request, email, password);
        Etudiant etudiant = null;

        if (request.getRole() == Role.ETUDIANT) {
            etudiant = createEtudiant(utilisateur, request.getFiliereId());
        }

        logGeneratedCredentials(email, password);

        return mapToCompteDTO(utilisateur, etudiant);
    }

    /**
     * Edit an account
     */
    @Transactional
    public CompteDTO editCompte(Long id, CompteEditRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));

        verifyEditPermissions(utilisateur);
        validateEditRequest(request, utilisateur);
        updateUtilisateur(utilisateur, request);

        Etudiant etudiant = null;
        if (utilisateur.getRole() == Role.ETUDIANT && request.getFiliereId() != null) {
            etudiant = updateEtudiantFiliere(utilisateur, request.getFiliereId());
        }

        return mapToCompteDTO(utilisateur, etudiant);
    }

    /**
     * Delete an account
     */
    @Transactional
    public void deleteCompte(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));

        if (utilisateur.getRole() == Role.ETUDIANT) {
            Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + id));

            // Verify student is from current year
            verifyCurrentYear(etudiant.getAnneeScolaire(), "supprimer");
            etudiantRepository.delete(etudiant);
        }

        utilisateurRepository.deleteById(id);
    }

    /**
     * Import multiple accounts in batch
     * For students, automatically assign the current academic year
     */
    @Transactional
    public BatchImportResponse importComptes(BatchImportRequest request) {
        List<ImportItemResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (CompteAddRequest compteRequest : request.getComptes()) {
            try {
                validateCompteRequest(compteRequest);

                String email = generateUniqueEmail(compteRequest.getPrenom(), compteRequest.getNom());
                String password = generateRandomPassword(10);

                Utilisateur utilisateur = createUtilisateur(compteRequest, email, password);

                if (compteRequest.getRole() == Role.ETUDIANT) {
                    createEtudiant(utilisateur, compteRequest.getFiliereId());
                }

                logGeneratedCredentials(email, password);

                results.add(ImportItemResult.builder()
                        .success(true)
                        .message("Compte créé avec succès. Email: " + email)
                        .data(compteRequest)
                        .build());

                successCount++;
            } catch (Exception e) {
                results.add(ImportItemResult.builder()
                        .success(false)
                        .message("Erreur: " + e.getMessage())
                        .data(compteRequest)
                        .build());

                failedCount++;
            }
        }

        return BatchImportResponse.builder()
                .results(results)
                .totalCount(request.getComptes().size())
                .successCount(successCount)
                .failedCount(failedCount)
                .build();
    }

    // ======================= HELPER METHODS =======================

    private void validateCompteRequest(CompteAddRequest request) {
        if (isNullOrEmpty(request.getNom())) {
            throw new RuntimeException("Le nom est obligatoire");
        }

        if (isNullOrEmpty(request.getPrenom())) {
            throw new RuntimeException("Le prénom est obligatoire");
        }

        if (request.getRole() == null) {
            throw new RuntimeException("Le rôle est obligatoire");
        }

        if (request.getDateNaissance() == null) {
            throw new RuntimeException("La date de naissance est obligatoire");
        }

        if (request.getRole() == Role.ETUDIANT) {
            validateStudentFields(request.getCne(), request.getFiliereId());
        } else {
            validateStaffFields(request.getCni());
        }
    }

    private void validateStudentFields(String cne, Long filiereId) {
        if (isNullOrEmpty(cne)) {
            throw new RuntimeException("Le CNE est obligatoire pour les étudiants");
        }

        if (utilisateurRepository.existsByCne(cne)) {
            throw new RuntimeException("CNE déjà utilisé");
        }

        if (filiereId == null) {
            throw new RuntimeException("La filière est obligatoire pour les étudiants");
        }
    }

    private void validateStaffFields(String cni) {
        if (isNullOrEmpty(cni)) {
            throw new RuntimeException("Le CNI est obligatoire pour les encadrants et jurés");
        }

        if (utilisateurRepository.existsByCni(cni)) {
            throw new RuntimeException("CNI déjà utilisé");
        }
    }

    private void validateEditRequest(CompteEditRequest request, Utilisateur utilisateur) {
        // Validate CNI uniqueness if changed
        if (request.getCni() != null && !request.getCni().equals(utilisateur.getCni()) &&
                utilisateur.getRole() != Role.ETUDIANT) {
            if (utilisateurRepository.existsByCni(request.getCni())) {
                throw new RuntimeException("CNI déjà utilisé");
            }
        }

        // Validate CNE uniqueness if changed
        if (request.getCne() != null && !request.getCne().equals(utilisateur.getCne()) &&
                utilisateur.getRole() == Role.ETUDIANT) {
            if (utilisateurRepository.existsByCne(request.getCne())) {
                throw new RuntimeException("CNE déjà utilisé");
            }
        }
    }

    private void verifyEditPermissions(Utilisateur utilisateur) {
        if (utilisateur.getRole() == Role.ETUDIANT) {
            Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                    .orElseThrow(() -> new RuntimeException(
                            "Étudiant non trouvé pour l'utilisateur: " + utilisateur.getId()));
            verifyCurrentYear(etudiant.getAnneeScolaire(), "modifier");
        }
    }

    private void verifyCurrentYear(AnneeScolaire anneeScolaire, String action) {
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        if (!anneeScolaire.getId().equals(currentYear.getId())) {
            throw new RuntimeException("Impossible de " + action + " un étudiant d'une année précédente");
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String generateUniqueEmail(String prenom, String nom) {
        // Remove accents and special characters
        String normalizedPrenom = Normalizer.normalize(prenom.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\s+", "");

        String normalizedNom = Normalizer.normalize(nom.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\s+", "");

        String baseEmail = normalizedPrenom + normalizedNom + ".efb@usms.ac.ma";
        String email = baseEmail;

        // Check if email already exists, add number if needed
        int counter = 1;
        while (utilisateurRepository.findByEmail(email).isPresent()) {
            email = normalizedPrenom + normalizedNom + counter + ".efb@usms.ac.ma";
            counter++;
        }

        return email;
    }

    private String generateRandomPassword(int length) {
        // StringBuilder sb = new StringBuilder(length);
        // for (int i = 0; i < length; i++) {
        //     sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        // }
        // return sb.toString();
        return "password123"; // For testing purposes, use a fixed password
    }

    private Utilisateur createUtilisateur(CompteAddRequest request, String email, String password) {
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(email)
                .cni(request.getRole() != Role.ETUDIANT ? request.getCni() : null)
                .cne(request.getRole() == Role.ETUDIANT ? request.getCne() : null)
                .dateNaissance(request.getDateNaissance())
                .motDePasse(passwordEncoder.encode(password))
                .role(request.getRole())
                .build();

        return utilisateurRepository.save(utilisateur);
    }

    private Etudiant createEtudiant(Utilisateur utilisateur, Long filiereId) {
        Filiere filiere = filiereRepository.findById(filiereId)
                .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + filiereId));

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        Etudiant etudiant = Etudiant.builder()
                .id(utilisateur.getId())
                .utilisateur(utilisateur)
                .filiere(filiere)
                .anneeScolaire(currentYear)
                .build();

        return etudiantRepository.save(etudiant);
    }

    private void updateUtilisateur(Utilisateur utilisateur, CompteEditRequest request) {
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());

        if (utilisateur.getRole() == Role.ETUDIANT) {
            utilisateur.setCne(request.getCne());
        } else {
            utilisateur.setCni(request.getCni());
        }

        utilisateur.setDateNaissance(request.getDateNaissance());
        utilisateurRepository.save(utilisateur);
    }

    private Etudiant updateEtudiantFiliere(Utilisateur utilisateur, Long filiereId) {
        Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur)
                .orElseThrow(
                        () -> new RuntimeException("Étudiant non trouvé pour l'utilisateur: " + utilisateur.getId()));

        Filiere filiere = filiereRepository.findById(filiereId)
                .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'id: " + filiereId));

        etudiant.setFiliere(filiere);
        return etudiantRepository.save(etudiant);
    }

    private void logGeneratedCredentials(String email, String password) {
        System.out.println("Generated email: " + email);
        System.out.println("Generated password: " + password);
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

    private FiliereDTO mapToFiliereDTO(Filiere filiere) {
        return FiliereDTO.builder()
                .id(filiere.getId())
                .nom(filiere.getNom())
                .build();
    }
}