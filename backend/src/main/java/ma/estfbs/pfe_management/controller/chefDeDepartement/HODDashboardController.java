package ma.estfbs.pfe_management.controller.chefDeDepartement;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.chefDeDepartement.HODDashboardDTOs.*;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.service.chefDeDepartement.HODDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chef_de_departement/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class HODDashboardController {

    private final HODDashboardService hodDashboardService;

    @GetMapping
    public ResponseEntity<HODDashboardResponse> getHODDashboard(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(hodDashboardService.getHODDashboard(utilisateur.getId()));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(hodDashboardService.getDashboardStats(utilisateur.getId()));
    }
    
    @GetMapping("/upcoming-soutenances")
    public ResponseEntity<List<UpcomingSoutenanceDTO>> getUpcomingSoutenances(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(hodDashboardService.getUpcomingSoutenances(utilisateur.getId()));
    }
    
    @GetMapping("/activities")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivities(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return ResponseEntity.ok(hodDashboardService.getRecentActivities(utilisateur.getId()));
    }
}