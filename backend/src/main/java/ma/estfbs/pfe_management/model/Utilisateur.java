// Utilisateur.java
package ma.estfbs.pfe_management.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilisateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String cni;

    @Column(unique = true)
    private String cne;

    @Column(name = "date_naissance")
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Enum for role type
    public enum Role {
        ETUDIANT, CHEF_DE_DEPARTEMENT, ENCADRANT, JURY
    }

    // Bidirectional relationships
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Etudiant> etudiants;

    @OneToMany(mappedBy = "etudiant1")
    private List<Binome> binomesAsPrimary;

    @OneToMany(mappedBy = "etudiant2")
    private List<Binome> binomesAsSecondary;

    @OneToMany(mappedBy = "encadrant")
    private List<Binome> binomesAsEncadrant;

    @OneToMany(mappedBy = "jury1")
    private List<Soutenance> soutenancesAsJury1;

    @OneToMany(mappedBy = "jury2")
    private List<Soutenance> soutenancesAsJury2;

    @OneToMany(mappedBy = "jury")
    private List<NoteSoutenance> notesSoutenance;

    @OneToMany(mappedBy = "etudiant")
    private List<NoteFinale> notesFinales;

    @OneToMany(mappedBy = "demandeur")
    private List<DemandeBinome> demandesEnvoyees;

    @OneToMany(mappedBy = "demande")
    private List<DemandeBinome> demandesRecues;

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}