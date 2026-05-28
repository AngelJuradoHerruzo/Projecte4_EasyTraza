package cat.copernic.easytraza.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.AlbaraProveidor;

/**
 * REPOSITORI D'ALBARANS DE PROVEÏDOR.
 *
 * Gestionat l'accés a les dades persistides dels albarans de proveïdor
 * i les consultes necessàries per presentar les recepcions registrades.
 *
 * @author Ángel Jurado Herruzo
 */
public interface AlbaraProveidorRepository extends JpaRepository<AlbaraProveidor, Long> {


    /**
     * LLISTAT D'ALBARANS ORDENATS.
     *
     * Obtinguts tots els albarans de proveïdor ordenats de més recent
     * a més antic segons la data de recepció i l'identificador.
     *
     * @return llista d'albarans de proveïdor ordenada
     */
    List<AlbaraProveidor> findAllByOrderByDataRecepcioDescIdDesc();
}
