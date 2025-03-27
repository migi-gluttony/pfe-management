package ma.estfbs.pfe_management.service;

import java.text.Normalizer;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.auth.*;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Generates standardized email for users based on their first and last name
     * Format: prenomnom.efb@usms.ac.ma
     */
    public String generateEmail(String prenom, String nom) {
        // Remove accents and special characters
        String normalizedPrenom = Normalizer.normalize(prenom.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        String normalizedNom = Normalizer.normalize(nom.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        // Remove spaces
        normalizedPrenom = normalizedPrenom.replaceAll("\\s+", "");
        normalizedNom = normalizedNom.replaceAll("\\s+", "");

        return normalizedPrenom + normalizedNom + ".efb@usms.ac.ma";
    }

    /**
     * Register a new user with automatic email generation
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        // Generate email from nom and prenom
        String email = generateEmail(registerRequest.getPrenom(), registerRequest.getNom());

        // Set the generated email
        registerRequest.setEmail(email);

        // Create user entity
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(registerRequest.getNom())
                .prenom(registerRequest.getPrenom())
                .email(email) // Use the generated email
                .cni(registerRequest.getCni())
                .cne(registerRequest.getCne())
                .dateNaissance(registerRequest.getDateNaissance())
                .motDePasse(passwordEncoder.encode(registerRequest.getMotDePasse()))
                .role(registerRequest.getRole())
                .build();

        utilisateurRepository.save(utilisateur);
        String jwtToken = jwtService.generateToken(utilisateur);
        return AuthResponse.builder().token(jwtToken).build();
    }

    /**
     * Authenticate a user and generate a JWT token
     */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));
            Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            String jwtToken = jwtService.generateToken(utilisateur);
            return AuthResponse.builder().token(jwtToken).build();
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
    }

    /**
     * First step of password reset - validate user info and generate token
     */
    public AuthResponse requestPasswordReset(PasswordResetRequest request) {
        Utilisateur utilisateur;
        // Check based on role-specific identification
        if (request.getCne() != null && !request.getCne().isEmpty()) {
            // Student identification via CNE
            utilisateur = utilisateurRepository.findByEmailAndDateNaissanceAndCne(
                    request.getEmail(), request.getDateNaissance(), request.getCne())
                    .orElseThrow(() -> new RuntimeException("Informations d'utilisateur invalides"));
        } else if (request.getCni() != null && !request.getCni().isEmpty()) {
            // Staff identification via CNI
            utilisateur = utilisateurRepository.findByEmailAndDateNaissanceAndCni(
                    request.getEmail(), request.getDateNaissance(), request.getCni())
                    .orElseThrow(() -> new RuntimeException("Informations d'utilisateur invalides"));
        } else {
            throw new RuntimeException("Informations d'identification manquantes");
        }
        // Generate password reset token
        String resetToken = jwtService.generatePasswordResetToken(utilisateur.getEmail());
        return AuthResponse.builder().token(resetToken).build();
    }

    /**
     * Second step of password reset - verify token and update password
     */
    public AuthResponse confirmPasswordReset(PasswordResetConfirm request) {
        // Validate token
        String email = jwtService.extractUsername(request.getToken());
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Invalid token");
        }

        // Find the user
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if token is valid
        if (!jwtService.isTokenValid(request.getToken(), utilisateur)) {
            throw new RuntimeException("Invalid or expired token");
        }

        // Update password
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(utilisateur);

        // Generate a new regular token for the user
        String newToken = jwtService.generateToken(utilisateur);

        return AuthResponse.builder().token(newToken).build();
    }
}