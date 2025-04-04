package ma.estfbs.pfe_management.service.etudiant;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.EtudiantStatusResponse;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.DemandeBinome;
import ma.estfbs.pfe_management.model.ProposerSujets;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.DemandeBinomeRepository;
import ma.estfbs.pfe_management.repository.ProposerSujetsRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtudiantStatusService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final BinomeRepository binomeRepository;
    private final DemandeBinomeRepository demandeBinomeRepository;
    private final ProposerSujetsRepository proposerSujetsRepository;
    private final AcademicYearService academicYearService;
    
    public EtudiantStatusResponse getStatus(Long etudiantId) {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Etudiant not found with id: " + etudiantId));
        
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        
        // Check if student has a binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);
        boolean hasBinome = !binomes.isEmpty();
        
        Binome binome = hasBinome ? binomes.get(0) : null;
        boolean hasSujet = hasBinome && binome.getSujet() != null;
        
        // Check for pending binome requests
        List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByDemandeAndStatut(
                etudiant, DemandeBinome.Statut.EN_ATTENTE);
        boolean hasPendingBinomeRequests = !pendingRequests.isEmpty();
        
        // Check if student has sent a binome request
        List<DemandeBinome> sentRequests = demandeBinomeRepository.findByDemandeurAndStatut(
                etudiant, DemandeBinome.Statut.EN_ATTENTE);
        boolean hasSentBinomeRequest = !sentRequests.isEmpty();
        
        // Check for pending sujet suggestions
        boolean hasPendingSujetSuggestion = false;
        if (hasBinome) {
            List<ProposerSujets> suggestions = proposerSujetsRepository.findByBinomeProposerParIdAndAnneeScolaireId(
                    binome.getId(), currentYear.getId());
            
            hasPendingSujetSuggestion = suggestions.stream()
                    .anyMatch(s -> s.getStatus() == ProposerSujets.Status.EN_ATTENTE);
        }
        
        // Determine next action
        String nextAction;
        if (!hasBinome) {
            nextAction = "CHOOSE_BINOME";
        } else if (!hasSujet) {
            nextAction = "CHOOSE_SUJET";
        } else {
            nextAction = "PROCEED";
        }
        
        return EtudiantStatusResponse.builder()
                .hasBinome(hasBinome)
                .hasSujet(hasSujet)
                .hasPendingBinomeRequests(hasPendingBinomeRequests)
                .hasSentBinomeRequest(hasSentBinomeRequest)
                .hasPendingSujetSuggestion(hasPendingSujetSuggestion)
                .binomeId(binome != null ? binome.getId() : null)
                .sujetId(hasSujet ? binome.getSujet().getId() : null)
                .nextAction(nextAction)
                .build();
    }
}