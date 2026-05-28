package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.Producte;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * REPOSITORI DE PRODUCTES.
 *
 * Gestionat l'accés a les dades persistides dels productes i la consulta
 * necessària per localitzar-los pel seu nom.
 *
 * @author Ángel Jurado Herruzo
 */
public interface ProducteRepository extends JpaRepository<Producte, Long> {


    /**
     * CERCA PER NOM.
     *
     * Obtingut el producte que coincideix amb el nom indicat.
     *
     * @param nomProducte nom del producte que s'ha de cercar
     * @return producte localitzat, si existeix
     */
    Optional<Producte> findByNomProducte(String nomProducte);
}
