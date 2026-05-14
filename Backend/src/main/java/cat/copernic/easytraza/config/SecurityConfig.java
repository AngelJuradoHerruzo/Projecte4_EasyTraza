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
 * Defineix la configuració d'autenticació, login, logout i codificació de contrasenyes.
 *
 * @author Ángel Jurado
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
                .requestMatchers(
                    "/",
                    "/login",
                    "/error"
                ).permitAll()

                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/fonts/**",
                    "/uploads/**"
                ).permitAll()

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
                .permitAll()
            )

            // ---------------------------- SERVEI D'USUARIS ----------------------------
            .userDetailsService(usuariDetailsService);

        return http.build();
    }
}