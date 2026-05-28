package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.Usuari;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * REPOSITORI D'USUARIS.
 *
 * Gestionat l'accés a les dades persistides dels usuaris i les consultes
 * necessàries per localitzar-los durant l'autenticació o la gestió de perfils.
 *
 * @author Ángel Jurado Herruzo
 */
public interface UsuariRepository extends JpaRepository<Usuari, Long> {


    /**
     * CERCA PER CORREU ELECTRÒNIC.
     *
     * Obtingut l'usuari que coincideix amb el correu electrònic indicat.
     *
     * @param email correu electrònic de l'usuari que s'ha de cercar
     * @return usuari localitzat, si existeix
     */
    Optional<Usuari> findByEmail(String email);


    /**
     * CERCA PER DNI.
     *
     * Obtingut l'usuari que coincideix amb el DNI indicat.
     *
     * @param dni DNI de l'usuari que s'ha de cercar
     * @return usuari localitzat, si existeix
     */
    Optional<Usuari> findByDni(String dni);
}
