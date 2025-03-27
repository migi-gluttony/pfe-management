package ma.estfbs.pfe_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Allow authentication endpoints without authentication
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/reset-password-request").permitAll()
                .requestMatchers("/api/auth/reset-password-confirm").permitAll()
                .requestMatchers("/api/chef_de_departement/**").hasRole("CHEF_DE_DEPARTEMENT")
                .requestMatchers("/api/etudiant/**").hasRole("ETUDIANT")
                .requestMatchers("/api/encadrant/**").hasRole("ENCADRANT")
                .requestMatchers("/api/jury/**").hasRole("JURY")
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}