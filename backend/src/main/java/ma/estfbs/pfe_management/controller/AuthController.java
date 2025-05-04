package ma.estfbs.pfe_management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.auth.*;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.auth.AuthenticationService;
import ma.estfbs.pfe_management.service.auth.JwtService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(service.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(service.login(loginRequest));
    }

    /**
     * Step 1 of password reset - validate user info and issue token
     */
    @PostMapping("/reset-password-request")
    public ResponseEntity<AuthResponse> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(service.requestPasswordReset(request));
    }

    /**
     * Step 2 of password reset - validate token and update password
     */
    @PostMapping("/reset-password-confirm")
    public ResponseEntity<AuthResponse> confirmPasswordReset(@RequestBody PasswordResetConfirm request) {
        return ResponseEntity.ok(service.confirmPasswordReset(request));
    }

    /**
     * Change password for authenticated user
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Find user in database
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), utilisateur.getMotDePasse())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Le mot de passe actuel est incorrect"));
        }

        // Set new password
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(utilisateur);

        // Generate new token with updated user info
        String newToken = jwtService.generateToken(utilisateur);

        return ResponseEntity.ok(new AuthResponse(newToken));
    }
}