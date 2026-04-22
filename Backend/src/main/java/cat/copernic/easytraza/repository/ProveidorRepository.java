package cat.copernic.easytraza.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cat.copernic.easytraza.entities.Proveidor;

public interface ProveidorRepository extends JpaRepository<Proveidor, Long> {

    Optional<Proveidor> findByCif(String cif);
}