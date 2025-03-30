package ma.estfbs.pfe_management.controller.chefDeDepartement;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.service.chefDeDepartement.NoteManagementService;
import ma.estfbs.pfe_management.dto.chefDeDepartement.NoteManagementDTOs.*;

@RestController
@RequestMapping("/api/chef_de_departement/notes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NoteManagementController {

    private final NoteManagementService noteManagementService;

    /**
     * Get all student notes with filieres and pourcentages
     */
    @GetMapping
    public ResponseEntity<NoteManagementResponse> getAllNotes() {
        return ResponseEntity.ok(noteManagementService.getAllNotesWithFilieres());
    }

    /**
     * Get notes filtered by filiere
     */
    @GetMapping("/filiere/{filiereId}")
    public ResponseEntity<List<NoteDTO>> getNotesByFiliere(@PathVariable Long filiereId) {
        return ResponseEntity.ok(noteManagementService.getNotesByFiliere(filiereId));
    }
}