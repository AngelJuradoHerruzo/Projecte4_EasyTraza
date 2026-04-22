package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.entities.Usuari;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariRepository extends JpaRepository<Usuari, Long> {

    Optional<Usuari> findByEmail(String email); // Buscar un usuari pel seu correu electrònic
}