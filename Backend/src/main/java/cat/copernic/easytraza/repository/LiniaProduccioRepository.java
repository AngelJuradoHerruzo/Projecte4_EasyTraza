package cat.copernic.easytraza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.LiniaProduccio;

/**
 * REPOSITORI DE LÍNIES DE PRODUCCIÓ.
 *
 * Gestionat l'accés a les dades persistides de les línies de producció
 * associades als albarans de client.
 *
 * @author Ángel Jurado Herruz
 */
public interface LiniaProduccioRepository extends JpaRepository<LiniaProduccio, Long> {}
