package cat.copernic.easytraza.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cat.copernic.easytraza.entities.TokenRecuperacioContrasenya;
import cat.copernic.easytraza.entities.Usuari;

/**
 * REPOSITORI DE TOKENS DE RECUPERACIÓ.
 *
 * Gestionat l'accés als tokens temporals utilitzats per restablir
 * de manera segura la contrasenya dels usuaris.
 *
 * @author Ángel Jurado Herruz
 */
public interface TokenRecuperacioContrasenyaRepository extends JpaRepository<TokenRecuperacioContrasenya, Long> {


    /**
     * CERCA DEL TOKEN ACTIU.
     *
     * Obtingut el token no utilitzat que coincideix amb el valor
     * protegit rebut durant el procés de recuperació.
     *
     * @param tokenHash valor protegit del token que s'ha de cercar
     * @return token localitzat, si existeix i no ha estat utilitzat
     */
    Optional<TokenRecuperacioContrasenya> findByTokenHashAndUtilitzatFalse(String tokenHash);


    /**
     * ELIMINACIÓ DELS TOKENS ANTERIORS.
     *
     * Eliminats els tokens de recuperació associats a l'usuari
     * abans de generar una nova sol·licitud.
     *
     * @param usuari usuari del qual s'han d'eliminar els tokens
     */
    void deleteByUsuari(Usuari usuari);
}
