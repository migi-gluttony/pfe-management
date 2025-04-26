package ma.estfbs.pfe_management.controller.etudiant;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.StudentDashboardDTOs.*;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.etudiant.StudentDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etudiant/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    /**
     * Get the dashboard data for the currently authenticated student
     */
    @GetMapping
    public ResponseEntity<StudentDashboardResponse> getStudentDashboard(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        StudentDashboardResponse dashboard = studentDashboardService.getStudentDashboard(utilisateur.getId());
        return ResponseEntity.ok(dashboard);
    }
}