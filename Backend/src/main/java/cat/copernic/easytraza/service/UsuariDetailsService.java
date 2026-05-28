package cat.copernic.easytraza.service;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.repository.UsuariRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SERVEI DE DETALLS D'USUARI.
 *
 * Carregades les dades dels usuaris necessàries per a l'autenticació amb Spring Security.
 * També adaptat el rol propi de l'aplicació al format utilitzat pel sistema de seguretat.
 *
 * @author Ángel Jurado Herruzo
 */
@Service
public class UsuariDetailsService implements UserDetailsService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final UsuariRepository usuariRepository;

    public UsuariDetailsService(UsuariRepository usuariRepository) {
        this.usuariRepository = usuariRepository;
    }


    /**
     * CÀRREGA DE L'USUARI.
     *
     * Carregades les dades de l'usuari a partir del correu electrònic
     * per preparar la seva autenticació.
     *
     * @param email valor de email utilitzat pel mètode
     * @return resultat obtingut pel mètode
     * @throws UsernameNotFoundException si es produeix un error durant el procés
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("El correu electrònic és obligatori");
        }

        String emailNormalitzat = email.trim().toLowerCase();

        Usuari usuari = usuariRepository.findByEmail(emailNormalitzat)
                .orElseThrow(() -> new UsernameNotFoundException("Usuari no trobat"));

        if (usuari.getPassword() == null || usuari.getPassword().trim().isEmpty()) {
            throw new UsernameNotFoundException("L'usuari no té contrasenya configurada");
        }

        String rol = "ROLE_" + usuari.getRolUsuari().name();

        return new User(
                usuari.getEmail(),
                usuari.getPassword(),
                List.of(new SimpleGrantedAuthority(rol))
        );
    }
}