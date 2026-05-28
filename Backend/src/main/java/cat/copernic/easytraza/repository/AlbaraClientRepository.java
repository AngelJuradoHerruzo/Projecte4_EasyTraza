package cat.copernic.easytraza.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.AlbaraClient;

/**
 * REPOSITORI D'ALBARANS DE CLIENT.
 *
 * Gestionat l'accés a les dades persistides dels albarans de client
 * i les consultes necessàries per ordenar-los o validar-ne les relacions.
 *
 * @author Ángel Jurado Herruzo
 */
public interface AlbaraClientRepository extends JpaRepository<AlbaraClient, Long> {


    /**
     * COMPROVACIÓ D'ALBARANS PER CLIENT.
     *
     * Comprovat si existeix algun albarà associat al client indicat.
     *
     * @param clientId identificador del client que s'ha de comprovar
     * @return cert si el client té algun albarà associat
     */
    boolean existsByClientId(Long clientId);


    /**
     * LLISTAT D'ALBARANS ORDENATS.
     *
     * Obtinguts tots els albarans de client ordenats de més recent
     * a més antic segons la data i l'identificador.
     *
     * @return llista d'albarans de client ordenada
     */
    List<AlbaraClient> findAllByOrderByDataAlbaraDescIdDesc();
}
