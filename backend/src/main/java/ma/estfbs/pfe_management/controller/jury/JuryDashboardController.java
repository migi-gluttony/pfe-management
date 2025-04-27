package ma.estfbs.pfe_management.controller.jury;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.jury.JuryDashboardDTOs.*;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.jury.JuryDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jury/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JuryDashboardController {

    private final JuryDashboardService juryDashboardService;

    /**
     * Get the dashboard data for the currently authenticated jury member
     */
    @GetMapping
    public ResponseEntity<JuryDashboardResponse> getJuryDashboard(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        JuryDashboardResponse dashboard = juryDashboardService.getJuryDashboard(utilisateur.getId());
        return ResponseEntity.ok(dashboard);
    }
}