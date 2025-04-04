package ma.estfbs.pfe_management.service.etudiant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.dto.etudiant.StudentDTO;
import ma.estfbs.pfe_management.dto.etudiant.SujetSelectionResponse;
import ma.estfbs.pfe_management.dto.etudiant.SujetSuggestionRequest;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.ProposerSujets;
import ma.estfbs.pfe_management.model.Sujet;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.ProposerSujetsRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SujetSelectionService {

        private final UtilisateurRepository utilisateurRepository;
        private final EtudiantRepository etudiantRepository;
        private final BinomeRepository binomeRepository;
        private final SujetRepository sujetRepository;
        private final ProposerSujetsRepository proposerSujetsRepository;
        private final AcademicYearService academicYearService;
        private final Random random = new Random();

        /**
         * Get available sujets for a student's filiere
         */
        public SujetSelectionResponse getAvailableSujets(Long etudiantId) {
                Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Etudiant not found with id: " + etudiantId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Check if student has a binome
                List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                                etudiant, etudiant, currentYear);

                if (binomes.isEmpty()) {
                        throw new RuntimeException("You need to be in a binome first to choose a sujet");
                }

                Binome binome = binomes.get(0);

                // Check if binome already has a sujet
                if (binome.getSujet() != null) {
                        List<SujetDTO> currentSujet = new ArrayList<>();
                        currentSujet.add(mapToSujetDTO(binome.getSujet()));

                        return SujetSelectionResponse.builder()
                                        .binomeId(binome.getId())
                                        .hasSujet(true)
                                        .hasPendingSuggestion(false) // Already has a sujet, so no pending suggestion
                                        .selectedSujet(mapToSujetDTO(binome.getSujet()))
                                        .availableSujets(new ArrayList<>()) // No need to show available sujets
                                        .build();
                }

                // Check for pending sujet suggestion
                List<ProposerSujets> pendingSuggestions = proposerSujetsRepository
                                .findByBinomeProposerParIdAndAnneeScolaireId(binome.getId(), currentYear.getId());

                boolean hasPendingSuggestion = pendingSuggestions.stream()
                                .anyMatch(s -> s.getStatus() == ProposerSujets.Status.EN_ATTENTE);

                // If there's a pending suggestion, don't show available sujets
                if (hasPendingSuggestion) {
                        return SujetSelectionResponse.builder()
                                        .binomeId(binome.getId())
                                        .hasSujet(false)
                                        .hasPendingSuggestion(true)
                                        .availableSujets(new ArrayList<>())
                                        .build();
                }

                // Get student's filiere
                Etudiant etudiantInfo = etudiantRepository.findByUtilisateur(etudiant)
                                .orElseThrow(() -> new RuntimeException(
                                                "Etudiant info not found for user: " + etudiantId));

                Filiere filiere = etudiantInfo.getFiliere();

                // Get available sujets for this filiere that are not assigned to any binome
                List<Sujet> availableSujets = sujetRepository.findByAnneeScolaireAndFiliere(currentYear, filiere);

                // Filter out sujets already assigned to other binomes
                availableSujets = availableSujets.stream()
                                .filter(sujet -> sujet.getBinomes().isEmpty())
                                .collect(Collectors.toList());

                List<SujetDTO> sujetDTOs = availableSujets.stream()
                                .map(this::mapToSujetDTO)
                                .collect(Collectors.toList());

                // Get binome members
                StudentDTO etudiant1DTO = mapToStudentDTO(binome.getEtudiant1());
                StudentDTO etudiant2DTO = binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2()) : null;

                return SujetSelectionResponse.builder()
                                .binomeId(binome.getId())
                                .hasSujet(false)
                                .hasPendingSuggestion(false)
                                .availableSujets(sujetDTOs)
                                .etudiant1(etudiant1DTO)
                                .etudiant2(etudiant2DTO)
                                .build();
        }

        /**
         * Select a specific sujet for a binome
         */
        @Transactional
        public SujetDTO selectSujet(Long etudiantId, Long sujetId) {
                Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Etudiant not found with id: " + etudiantId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Get student's binome
                List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                                etudiant, etudiant, currentYear);

                if (binomes.isEmpty()) {
                        throw new RuntimeException("You need to be in a binome first to choose a sujet");
                }

                Binome binome = binomes.get(0);

                // Check if binome already has a sujet
                if (binome.getSujet() != null) {
                        throw new RuntimeException("Your binome already has a sujet assigned");
                }

                // Check for pending sujet suggestion
                List<ProposerSujets> pendingSuggestions = proposerSujetsRepository
                                .findByBinomeProposerParIdAndAnneeScolaireId(binome.getId(), currentYear.getId());

                boolean hasPendingSuggestion = pendingSuggestions.stream()
                                .anyMatch(s -> s.getStatus() == ProposerSujets.Status.EN_ATTENTE);

                if (hasPendingSuggestion) {
                        throw new RuntimeException(
                                        "You have a pending sujet suggestion. Wait for a response before choosing a sujet");
                }

                // Get student's filiere
                Etudiant etudiantInfo = etudiantRepository.findByUtilisateur(etudiant)
                                .orElseThrow(() -> new RuntimeException(
                                                "Etudiant info not found for user: " + etudiantId));

                // Get and lock the sujet for assignment
                Sujet sujet = sujetRepository.findById(sujetId)
                                .orElseThrow(() -> new RuntimeException("Sujet not found with id: " + sujetId));

                // Verify sujet is from current year
                if (!sujet.getAnneeScolaire().getId().equals(currentYear.getId())) {
                        throw new RuntimeException("This sujet is not from the current academic year");
                }

                // Check if sujet is already assigned to another binome
                if (!sujet.getBinomes().isEmpty()) {
                        throw new RuntimeException("This sujet is already assigned to another binome");
                }

                // Verify sujet is from student's filiere
                if (!sujet.getFiliere().getId().equals(etudiantInfo.getFiliere().getId())) {
                        throw new RuntimeException("This sujet is not from your filiere");
                }

                // Assign sujet to binome
                binome.setSujet(sujet);
                binomeRepository.save(binome);

                return mapToSujetDTO(sujet);
        }

        /**
         * Assign a random available sujet to a binome
         */
        @Transactional
        public SujetDTO assignRandomSujet(Long etudiantId) {
                Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Etudiant not found with id: " + etudiantId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Get student's binome
                List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                                etudiant, etudiant, currentYear);

                if (binomes.isEmpty()) {
                        throw new RuntimeException("You need to be in a binome first to choose a sujet");
                }

                Binome binome = binomes.get(0);

                // Check if binome already has a sujet
                if (binome.getSujet() != null) {
                        throw new RuntimeException("Your binome already has a sujet assigned");
                }

                // Check for pending sujet suggestion
                List<ProposerSujets> pendingSuggestions = proposerSujetsRepository
                                .findByBinomeProposerParIdAndAnneeScolaireId(binome.getId(), currentYear.getId());

                boolean hasPendingSuggestion = pendingSuggestions.stream()
                                .anyMatch(s -> s.getStatus() == ProposerSujets.Status.EN_ATTENTE);

                if (hasPendingSuggestion) {
                        throw new RuntimeException(
                                        "You have a pending sujet suggestion. Wait for a response before choosing a sujet");
                }

                // Get student's filiere
                Etudiant etudiantInfo = etudiantRepository.findByUtilisateur(etudiant)
                                .orElseThrow(() -> new RuntimeException(
                                                "Etudiant info not found for user: " + etudiantId));

                Filiere filiere = etudiantInfo.getFiliere();

                // Get available sujets for this filiere that are not assigned to any binome
                List<Sujet> availableSujets = sujetRepository.findByAnneeScolaireAndFiliere(currentYear, filiere);

                // Filter out sujets already assigned to other binomes
                availableSujets = availableSujets.stream()
                                .filter(sujet -> sujet.getBinomes().isEmpty())
                                .collect(Collectors.toList());

                if (availableSujets.isEmpty()) {
                        throw new RuntimeException("No available sujets for your filiere");
                }

                // Choose a random sujet
                int randomIndex = random.nextInt(availableSujets.size());
                Sujet randomSujet = availableSujets.get(randomIndex);

                // Assign sujet to binome
                binome.setSujet(randomSujet);
                binomeRepository.save(binome);

                return mapToSujetDTO(randomSujet);
        }

        /**
         * Suggest a new sujet
         */
        @Transactional
        public void suggestSujet(Long etudiantId, SujetSuggestionRequest suggestion) {
                Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Etudiant not found with id: " + etudiantId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Get student's binome
                List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                                etudiant, etudiant, currentYear);

                if (binomes.isEmpty()) {
                        throw new RuntimeException("You need to be in a binome first to suggest a sujet");
                }

                Binome binome = binomes.get(0);

                // Check if binome already has a sujet
                if (binome.getSujet() != null) {
                        throw new RuntimeException("Your binome already has a sujet assigned");
                }

                // Check for existing pending sujet suggestion
                List<ProposerSujets> existingSuggestions = proposerSujetsRepository
                                .findByBinomeProposerParIdAndAnneeScolaireId(binome.getId(), currentYear.getId());

                boolean hasPendingSuggestion = existingSuggestions.stream()
                                .anyMatch(s -> s.getStatus() == ProposerSujets.Status.EN_ATTENTE);

                if (hasPendingSuggestion) {
                        throw new RuntimeException("You already have a pending sujet suggestion");
                }

                // Validate suggestion
                if (suggestion.getTitre() == null || suggestion.getTitre().trim().isEmpty()) {
                        throw new RuntimeException("Title is required");
                }

                if (suggestion.getTheme() == null || suggestion.getTheme().trim().isEmpty()) {
                        throw new RuntimeException("Theme is required");
                }

                if (suggestion.getDescription() == null || suggestion.getDescription().trim().isEmpty()) {
                        throw new RuntimeException("Description is required");
                }

                // Create new sujet suggestion
                ProposerSujets newSuggestion = ProposerSujets.builder()
                                .binomeProposerPar(binome)
                                .titre(suggestion.getTitre())
                                .theme(suggestion.getTheme())
                                .description(suggestion.getDescription())
                                .status(ProposerSujets.Status.EN_ATTENTE)
                                .dateProposition(LocalDateTime.now())
                                .anneeScolaire(currentYear)
                                .build();

                proposerSujetsRepository.save(newSuggestion);
        }

        /**
         * Check if student has a pending sujet suggestion
         */
        public boolean hasPendingSuggestion(Long etudiantId) {
                Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Etudiant not found with id: " + etudiantId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Get student's binome
                List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                                etudiant, etudiant, currentYear);

                if (binomes.isEmpty()) {
                        return false;
                }

                Binome binome = binomes.get(0);

                // Check for pending sujet suggestion
                List<ProposerSujets> pendingSuggestions = proposerSujetsRepository
                                .findByBinomeProposerParIdAndAnneeScolaireId(binome.getId(), currentYear.getId());

                return pendingSuggestions.stream()
                                .anyMatch(s -> s.getStatus() == ProposerSujets.Status.EN_ATTENTE);
        }

        // Mapping methods

        private SujetDTO mapToSujetDTO(Sujet sujet) {
                return SujetDTO.builder()
                                .id(sujet.getId())
                                .titre(sujet.getTitre())
                                .theme(sujet.getTheme())
                                .description(sujet.getDescription())
                                .filiereName(sujet.getFiliere().getNom())
                                .build();
        }

        private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
                return StudentDTO.builder()
                                .id(utilisateur.getId())
                                .nom(utilisateur.getNom())
                                .prenom(utilisateur.getPrenom())
                                .email(utilisateur.getEmail())
                                .build();
        }
}