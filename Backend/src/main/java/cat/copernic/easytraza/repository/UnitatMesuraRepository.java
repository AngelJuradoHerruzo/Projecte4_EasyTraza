package cat.copernic.easytraza.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.UnitatMesura;

/**
 * REPOSITORI D'UNITATS DE MESURA.
 *
 * Gestionat l'accés a les dades persistides de les unitats de mesura
 * i les consultes necessàries per cercar-les i presentar-les ordenades.
 *
 * @author Ángel Jurado Herruz
 */
public interface UnitatMesuraRepository extends JpaRepository<UnitatMesura, Long> {


    /**
     * CERCA PER NOM.
     *
     * Obtinguda la unitat de mesura que coincideix amb el nom indicat.
     *
     * @param nom nom de la unitat de mesura que s'ha de cercar
     * @return unitat de mesura localitzada, si existeix
     */
    Optional<UnitatMesura> findByNom(String nom);


    /**
     * COMPROVACIÓ D'EXISTÈNCIA PER NOM.
     *
     * Comprovat si existeix una unitat de mesura amb el nom indicat.
     *
     * @param nom nom de la unitat de mesura que s'ha de comprovar
     * @return cert si existeix una unitat amb el nom indicat
     */
    boolean existsByNom(String nom);


    /**
     * LLISTAT D'UNITATS ORDENADES.
     *
     * Obtingudes totes les unitats de mesura ordenades alfabèticament pel nom.
     *
     * @return llista d'unitats de mesura ordenada
     */
    List<UnitatMesura> findAllByOrderByNomAsc();
}
