package ma.estfbs.pfe_management.controller.encadrant;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.encadrant.EncadrantDashboardDTOs.*;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.encadrant.EncadrantDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/encadrant/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EncadrantDashboardController {

    private final EncadrantDashboardService encadrantDashboardService;

    /**
     * Get the dashboard data for the currently authenticated encadrant
     */
    @GetMapping
    public ResponseEntity<EncadrantDashboardResponse> getEncadrantDashboard(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        EncadrantDashboardResponse dashboard = encadrantDashboardService.getEncadrantDashboard(utilisateur.getId());
        return ResponseEntity.ok(dashboard);
    }
}