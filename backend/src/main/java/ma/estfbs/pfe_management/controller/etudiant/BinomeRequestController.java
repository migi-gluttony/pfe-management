package ma.estfbs.pfe_management.controller.etudiant;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.BinomeDTO;
import ma.estfbs.pfe_management.dto.etudiant.BinomeRequestDTO;
import ma.estfbs.pfe_management.dto.etudiant.BinomeRequestResponse;
import ma.estfbs.pfe_management.dto.etudiant.BinomeSelectionResponse;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.etudiant.BinomeRequestService;

@RestController
@RequestMapping("/api/etudiant/binome")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BinomeRequestController {

    private final BinomeRequestService binomeRequestService;

    /**
     * Get pending binome requests for the current student
     */
    @GetMapping("/requests")
    public ResponseEntity<List<BinomeRequestDTO>> getPendingRequests(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(binomeRequestService.getPendingRequestsForStudent(utilisateur.getId()));
    }

    /**
     * Get available students for binome formation (from same filiere)
     */
    @GetMapping("/available-students")
    public ResponseEntity<BinomeSelectionResponse> getAvailableStudents(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(binomeRequestService.getAvailableStudentsFromSameFiliere(utilisateur.getId()));
    }

    /**
     * Send a binome request to another student
     */
    @PostMapping("/request")
    public ResponseEntity<BinomeRequestResponse> sendRequest(
            @RequestBody Long targetStudentId, 
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(binomeRequestService.sendBinomeRequest(utilisateur.getId(), targetStudentId));
    }

    /**
     * Accept a binome request
     */
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<BinomeDTO> acceptRequest(
            @PathVariable Long requestId, 
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(binomeRequestService.acceptBinomeRequest(utilisateur.getId(), requestId));
    }

    /**
     * Reject a binome request
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Void> rejectRequest(
            @PathVariable Long requestId, 
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        binomeRequestService.rejectBinomeRequest(utilisateur.getId(), requestId);
        return ResponseEntity.ok().build();
    }

    /**
     * Create a binome with just the current student (continue alone)
     */
    @PostMapping("/continue-alone")
    public ResponseEntity<BinomeDTO> continueAlone(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(binomeRequestService.createSingleStudentBinome(utilisateur.getId()));
    }
}