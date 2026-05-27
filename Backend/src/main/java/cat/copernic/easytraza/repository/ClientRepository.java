package cat.copernic.easytraza.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.Client;

/**
 * REPOSITORI DE CLIENTS.
 *
 * Gestionat l'accés a les dades persistides dels clients i les consultes
 * necessàries per localitzar-los segons les seves dades identificatives.
 *
 * @author Ángel Jurado Herruz
 */
public interface ClientRepository extends JpaRepository<Client, Long> {


    /**
     * CERCA PER CIF.
     *
     * Obtingut el client que coincideix amb el CIF indicat.
     *
     * @param cif CIF del client que s'ha de cercar
     * @return client localitzat, si existeix
     */
    Optional<Client> findByCif(String cif);


    /**
     * CERCA PER TELÈFON.
     *
     * Obtingut el client que coincideix amb el telèfon indicat.
     *
     * @param telefon telèfon del client que s'ha de cercar
     * @return client localitzat, si existeix
     */
    Optional<Client> findByTelefon(String telefon);


    /**
     * CERCA PER CORREU ELECTRÒNIC.
     *
     * Obtingut el client que coincideix amb el correu electrònic indicat.
     *
     * @param email correu electrònic del client que s'ha de cercar
     * @return client localitzat, si existeix
     */
    Optional<Client> findByEmail(String email);


    /**
     * LLISTAT DE CLIENTS ORDENATS.
     *
     * Obtinguts tots els clients ordenats alfabèticament pel seu nom complet.
     *
     * @return llista de clients ordenada
     */
    List<Client> findAllByOrderByNomCompletAsc();
}
