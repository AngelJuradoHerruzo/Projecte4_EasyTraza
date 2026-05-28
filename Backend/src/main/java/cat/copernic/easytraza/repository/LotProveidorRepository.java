package cat.copernic.easytraza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.enums.EstatLot;

/**
 * REPOSITORI DE LOTS DE PROVEÏDOR.
 *
 * Gestionat l'accés a les dades persistides dels lots de proveïdor
 * i les consultes necessàries per controlar-ne l'estat i la matèria primera.
 *
 * @author Ángel Jurado Herruzo
 */
public interface LotProveidorRepository extends JpaRepository<LotProveidor, Long> {


    /**
     * CERCA D'UN ALTRE LOT PER MATÈRIA I ESTAT.
     *
     * Obtingut el primer lot d'una matèria primera que es troba en l'estat
     * indicat, excloent el lot que s'està gestionant.
     *
     * @param materiaPrimeraId identificador de la matèria primera
     * @param estat estat del lot que s'ha de localitzar
     * @param id identificador del lot que s'ha d'excloure
     * @return lot localitzat, si existeix
     */
    Optional<LotProveidor> findFirstByMateriaPrimeraIdAndEstatAndIdNot(Long materiaPrimeraId, EstatLot estat, Long id);


    /**
     * CERCA DE LOTS PER ESTAT.
     *
     * Obtinguts els lots que es troben en l'estat indicat.
     *
     * @param estat estat dels lots que s'han de cercar
     * @return llista de lots que coincideixen amb l'estat
     */
    List<LotProveidor> findByEstat(EstatLot estat);


    /**
     * COMPROVACIÓ DE LOTS PER MATÈRIA PRIMERA.
     *
     * Comprovat si existeix algun lot associat a la matèria primera indicada.
     *
     * @param materiaPrimeraId identificador de la matèria primera
     * @return cert si la matèria primera està associada a algun lot
     */
    boolean existsByMateriaPrimeraId(Long materiaPrimeraId);
}
