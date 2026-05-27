package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.MateriaPrimera;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * REPOSITORI DE MATÈRIES PRIMERES.
 *
 * Gestionat l'accés a les dades persistides de les matèries primeres
 * i la consulta necessària per localitzar-les pel seu nom.
 *
 * @author Ángel Jurado Herruz
 */
public interface MateriaPrimeraRepository extends JpaRepository<MateriaPrimera, Long> {


    /**
     * CERCA PER NOM.
     *
     * Obtinguda la matèria primera que coincideix amb el nom indicat.
     *
     * @param nomMateria nom de la matèria primera que s'ha de cercar
     * @return matèria primera localitzada, si existeix
     */
    Optional<MateriaPrimera> findByNomMateria(String nomMateria);
}
