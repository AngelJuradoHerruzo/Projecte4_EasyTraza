package cat.copernic.easytraza.config;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.repository.UsuariRepository;
import cat.copernic.easytraza.service.UsuariDetailsService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * CONFIGURACIÓ DE SEGURETAT
 *
 * Defineix la configuració d'autenticació, login, logout,
 * permisos d'accés i codificació de contrasenyes.
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private UsuariDetailsService usuariDetailsService;

    @Autowired
    private UsuariRepository usuariRepository;


    // ---------------------------- PASSWORD ENCODER ----------------------------
    // Bean per codificar contrasenyes amb BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // ---------------------------- CONFIGURACIÓ DE SEGURETAT ----------------------------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ---------------------------- CSRF ----------------------------
            .csrf(csrf -> csrf.disable())

            // ---------------------------- AUTORITZACIÓ DE RUTES ----------------------------
            .authorizeHttpRequests(auth -> auth

                // Rutes públiques
                .requestMatchers(
                    "/login",
                    "/error",
                    "/error/**",
                    "/api/mobile/auth/identificar",
                    "/api/mobile/auth/usuaris",
                    "/api/mobile/lots/**"
                ).permitAll()

                // Recursos estàtics
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/images/**",
                    "/fonts/**",
                    "/uploads/**",
                    "/favicon.ico"
                ).permitAll()

                // Rutes inicials i perfil accessibles per qualsevol usuari autenticat
                .requestMatchers(
                    "/",
                    "/perfil/**"
                ).hasAnyRole("ADMIN", "OPERARI")

                // Manteniments administratius exclusius de l'administrador
                .requestMatchers(
                    "/productes/**",
                    "/usuaris/**",
                    "/proveidors/**",
                    "/materies-primeres/**",
                    "/clients/**"
                ).hasRole("ADMIN")

                // Operacions accessibles per administradors i operaris
                .requestMatchers(
                    "/albarans-proveidor/**",
                    "/albarans-client/**",
                    "/lots/**",
                    "/tracabilitat/**",
                    "/unitats-mesura/**"
                ).hasAnyRole("ADMIN", "OPERARI")

                // Qualsevol altra ruta requereix autenticació
                .anyRequest().authenticated()
            )

            // ---------------------------- LOGIN AMB FORMULARI ----------------------------
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {

                    String email = authentication.getName();

                    Usuari usuari = usuariRepository.findByEmail(email)
                            .orElse(null);

                    if (usuari != null) {

                        HttpSession session = request.getSession();

                        session.setAttribute("usuariId", usuari.getId());
                        session.setAttribute("usuariNom", usuari.getNomComplet());
                        session.setAttribute("usuariEmail", usuari.getEmail());
                        session.setAttribute("usuariRol", usuari.getRolUsuari());
                    }

                    response.sendRedirect("/");
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // ---------------------------- LOGOUT ----------------------------
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // ---------------------------- ERROR ACCÉS DENEGAT ----------------------------
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/error/403")
            )

            // ---------------------------- SERVEI D'USUARIS ----------------------------
            .userDetailsService(usuariDetailsService);

        return http.build();
    }
}
