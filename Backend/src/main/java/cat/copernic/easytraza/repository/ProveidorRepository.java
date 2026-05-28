package cat.copernic.easytraza.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.Proveidor;

/**
 * REPOSITORI DE PROVEÏDORS.
 *
 * Gestionat l'accés a les dades persistides dels proveïdors i la consulta
 * necessària per localitzar-los mitjançant el seu CIF.
 *
 * @author Ángel Jurado Herruzo
 */
public interface ProveidorRepository extends JpaRepository<Proveidor, Long> {


    /**
     * CERCA PER CIF.
     *
     * Obtingut el proveïdor que coincideix amb el CIF indicat.
     *
     * @param cif CIF del proveïdor que s'ha de cercar
     * @return proveïdor localitzat, si existeix
     */
    Optional<Proveidor> findByCif(String cif);
}
